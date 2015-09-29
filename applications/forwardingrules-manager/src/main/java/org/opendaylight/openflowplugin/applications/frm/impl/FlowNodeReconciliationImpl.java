/**
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.applications.frm.impl;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.DataChangeListener;
import org.opendaylight.controller.md.sal.binding.api.ReadOnlyTransaction;
import org.opendaylight.controller.md.sal.common.api.data.AsyncDataBroker.DataChangeScope;
import org.opendaylight.controller.md.sal.common.api.data.AsyncDataChangeEvent;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.openflowplugin.applications.frm.FlowNodeReconciliation;
import org.opendaylight.openflowplugin.applications.frm.ForwardingRulesManager;
import org.opendaylight.openflowplugin.common.wait.SimpleTaskRetryLooper;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNode;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.meters.Meter;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.meters.MeterKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.Table;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.TableKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.table.Flow;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.table.FlowKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.groups.Group;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.groups.GroupKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.Nodes;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table.features.TableFeatures;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table.features.TableFeaturesKey;
import org.opendaylight.yangtools.concepts.ListenerRegistration;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.binding.KeyedInstanceIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;
import org.opendaylight.controller.md.sal.common.api.clustering.Entity;
import org.opendaylight.controller.md.sal.common.api.clustering.EntityOwnershipService;
import org.opendaylight.controller.md.sal.common.api.clustering.EntityOwnershipState;


/**
 * forwardingrules-manager
 * org.opendaylight.openflowplugin.applications.frm
 *
 * FlowNode Reconciliation Listener
 * Reconciliation for a new FlowNode
 *
 * @author <a href="mailto:vdemcak@cisco.com">Vaclav Demcak</a>
 *
 * Created: Jun 13, 2014
 */
public class FlowNodeReconciliationImpl implements FlowNodeReconciliation {

    private static final Logger LOG = LoggerFactory.getLogger(FlowNodeReconciliationImpl.class);

    private final ForwardingRulesManager provider;

    private ListenerRegistration<DataChangeListener> listenerRegistration;

    public FlowNodeReconciliationImpl (final ForwardingRulesManager manager, final DataBroker db) {
        this.provider = Preconditions.checkNotNull(manager, "ForwardingRulesManager can not be null!");
        Preconditions.checkNotNull(db, "DataBroker can not be null!");
        /* Build Path */
        final InstanceIdentifier<FlowCapableNode> flowNodeWildCardIdentifier = InstanceIdentifier.create(Nodes.class)
                .child(Node.class).augmentation(FlowCapableNode.class);

        SimpleTaskRetryLooper looper = new SimpleTaskRetryLooper(ForwardingRulesManagerImpl.STARTUP_LOOP_TICK,
                ForwardingRulesManagerImpl.STARTUP_LOOP_MAX_RETRIES);
        try {
            listenerRegistration = looper.loopUntilNoException(new Callable<ListenerRegistration<DataChangeListener>>() {
                @Override
                public ListenerRegistration<DataChangeListener> call() throws Exception {
                    return db.registerDataChangeListener(LogicalDatastoreType.OPERATIONAL,
                            flowNodeWildCardIdentifier, FlowNodeReconciliationImpl.this, DataChangeScope.BASE);
                }
            });
        } catch (Exception e) {
            LOG.warn("data listener registration failed: {}", e.getMessage());
            LOG.debug("data listener registration failed.. ", e);
            throw new IllegalStateException("FlowNodeReconciliation startup fail! System needs restart.", e);
        }
    }

    @Override
    public void close() {
        if (listenerRegistration != null) {
            try {
                listenerRegistration.close();
            } catch (Exception e) {
                LOG.warn("Error by stop FRM FlowNodeReconilListener: {}", e.getMessage());
                LOG.debug("Error by stop FRM FlowNodeReconilListener..", e);
            }
            listenerRegistration = null;
        }
    }

    private boolean preConfigurationCheck(final InstanceIdentifier<FlowCapableNode> fNodeIdent) {
        Preconditions.checkNotNull(fNodeIdent, "fNodeIdent can not be null!");
        EntityOwnershipService ownershipService = provider.getOwnershipService();
        if(ownershipService == null) {
            LOG.debug("preConfigCheck: entityOwnershipService is null - assuming ownership");
            return true;
        }

        InstanceIdentifier<Node> nodeIdent = fNodeIdent.firstIdentifierOf(Node.class);
        NodeId nodeId = InstanceIdentifier.keyOf(nodeIdent).getId();
        final Entity entity = new Entity("openflow", nodeId.getValue());
        Optional<EntityOwnershipState> entityOwnershipStateOptional = ownershipService.getOwnershipState(entity);
        if(!entityOwnershipStateOptional.isPresent()) { //abset - assume this ofp is owning entity
            LOG.debug("preConfigCheck: entity state of " + nodeId.getValue() + " is absent - assuming ownership");
            return true;
        }
        final EntityOwnershipState entityOwnershipState = entityOwnershipStateOptional.get();
        if(!(entityOwnershipState.hasOwner() && entityOwnershipState.isOwner())) {
            LOG.debug("preConfigCheck: not owner of " + nodeId.getValue() + " - skipping configuration");
            return false;
        }
        return true;
    }

    @Override
    public void onDataChanged(final AsyncDataChangeEvent<InstanceIdentifier<?>, DataObject> changeEvent) {
        Preconditions.checkNotNull(changeEvent,"Async ChangeEvent can not be null!");
        /* All DataObjects for create */
        final Set<InstanceIdentifier<?>>  createdData = changeEvent.getCreatedData() != null
                ? changeEvent.getCreatedData().keySet() : Collections.<InstanceIdentifier<?>> emptySet();
        /* All DataObjects for remove */
        final Set<InstanceIdentifier<?>> removeData = changeEvent.getRemovedPaths() != null
                ? changeEvent.getRemovedPaths() : Collections.<InstanceIdentifier<?>> emptySet();

        for (InstanceIdentifier<?> entryKey : removeData) {
            final InstanceIdentifier<FlowCapableNode> nodeIdent = entryKey
                    .firstIdentifierOf(FlowCapableNode.class);
            if (preConfigurationCheck(nodeIdent) && (! nodeIdent.isWildcarded())) {
                flowNodeDisconnected(nodeIdent);
            }
        }
        for (InstanceIdentifier<?> entryKey : createdData) {
            final InstanceIdentifier<FlowCapableNode> nodeIdent = entryKey
                    .firstIdentifierOf(FlowCapableNode.class);
            if (preConfigurationCheck(nodeIdent) && (! nodeIdent.isWildcarded())) {
                flowNodeConnected(nodeIdent);
            }
        }
    }

    @Override
    public void flowNodeDisconnected(InstanceIdentifier<FlowCapableNode> disconnectedNode) {
        provider.unregistrateNode(disconnectedNode);
    }

    @Override
    public void flowNodeConnected(InstanceIdentifier<FlowCapableNode> connectedNode) {
        if ( ! provider.isNodeActive(connectedNode)) {
            provider.registrateNewNode(connectedNode);
            reconciliation(connectedNode);
        }
    }

    private void reconciliation(final InstanceIdentifier<FlowCapableNode> nodeIdent) {

        ReadOnlyTransaction trans = provider.getReadTranaction();
        Optional<FlowCapableNode> flowNode = Optional.absent();

        try {
            flowNode = trans.read(LogicalDatastoreType.CONFIGURATION, nodeIdent).get();
        }
        catch (Exception e) {
            LOG.error("Fail with read Config/DS for Node {} !", nodeIdent, e);
        }

        if (flowNode.isPresent()) {
            /* Tables - have to be pushed before groups */
            // CHECK if while pusing the update, updateTableInput can be null to emulate a table add
            List<Table> tableList = flowNode.get().getTable() != null
                    ? flowNode.get().getTable() : Collections.<Table> emptyList() ;
            for (Table table : tableList) {
                TableKey tableKey = table.getKey();
                KeyedInstanceIdentifier<TableFeatures, TableFeaturesKey> tableFeaturesII
                    = nodeIdent.child(Table.class, tableKey).child(TableFeatures.class, new TableFeaturesKey(tableKey.getId()));
                List<TableFeatures> tableFeatures = table.getTableFeatures();
                if (tableFeatures != null) {
                    for (TableFeatures tableFeaturesItem : tableFeatures) {
                        provider.getTableFeaturesCommiter().update(tableFeaturesII, tableFeaturesItem, null, nodeIdent);
                    }
                }
            }

            /* Groups - have to be first */
            List<Group> groups = flowNode.get().getGroup() != null
                    ? flowNode.get().getGroup() : Collections.<Group> emptyList();
            for (Group group : groups) {
                final KeyedInstanceIdentifier<Group, GroupKey> groupIdent =
                        nodeIdent.child(Group.class, group.getKey());
                this.provider.getGroupCommiter().add(groupIdent, group, nodeIdent);
            }
            /* Meters */
            List<Meter> meters = flowNode.get().getMeter() != null
                    ? flowNode.get().getMeter() : Collections.<Meter> emptyList();
            for (Meter meter : meters) {
                final KeyedInstanceIdentifier<Meter, MeterKey> meterIdent =
                        nodeIdent.child(Meter.class, meter.getKey());
                this.provider.getMeterCommiter().add(meterIdent, meter, nodeIdent);
            }
            /* Flows */
            List<Table> tables = flowNode.get().getTable() != null
                    ? flowNode.get().getTable() : Collections.<Table> emptyList();
            for (Table table : tables) {
                final KeyedInstanceIdentifier<Table, TableKey> tableIdent =
                        nodeIdent.child(Table.class, table.getKey());
                List<Flow> flows = table.getFlow() != null ? table.getFlow() : Collections.<Flow> emptyList();
                for (Flow flow : flows) {
                    final KeyedInstanceIdentifier<Flow, FlowKey> flowIdent =
                            tableIdent.child(Flow.class, flow.getKey());
                    this.provider.getFlowCommiter().add(flowIdent, flow, nodeIdent);
                }
            }
        }
        /* clean transaction */
        trans.close();
    }
}

