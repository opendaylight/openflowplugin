/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.applications.frm.impl.reconciliate;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.util.concurrent.CheckedFuture;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.Callable;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.DataChangeListener;
import org.opendaylight.controller.md.sal.binding.api.ReadOnlyTransaction;
import org.opendaylight.controller.md.sal.common.api.data.AsyncDataBroker;
import org.opendaylight.controller.md.sal.common.api.data.AsyncDataChangeEvent;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.controller.md.sal.common.api.data.ReadFailedException;
import org.opendaylight.openflowplugin.applications.frm.FlowNodeReconciliation;
import org.opendaylight.openflowplugin.applications.frm.ForwardingRulesCommiter;
import org.opendaylight.openflowplugin.applications.frm.ForwardingRulesManager;
import org.opendaylight.openflowplugin.applications.frm.ForwardingRulesUpdateCommiter;
import org.opendaylight.openflowplugin.applications.frm.impl.ForwardingRulesManagerImpl;
import org.opendaylight.openflowplugin.common.wait.SimpleTaskRetryLooper;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNode;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.meters.Meter;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.table.Flow;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.AddFlowOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.RemoveFlowOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.UpdateFlowOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.transaction.rev150304.FlowCapableTransactionService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.service.rev130918.AddGroupOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.service.rev130918.RemoveGroupOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.service.rev130918.UpdateGroupOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.groups.Group;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.Nodes;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.service.rev130918.AddMeterOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.service.rev130918.RemoveMeterOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.service.rev130918.UpdateMeterOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.service.rev131026.UpdateTableOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table.features.TableFeatures;
import org.opendaylight.yangtools.concepts.ListenerRegistration;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Common code used by reconciliation implementations
 */
public abstract class AbstractFlowNodeReconciliator implements FlowNodeReconciliation {

    private static final Logger LOG = LoggerFactory.getLogger(AbstractFlowNodeReconciliator.class);

    public static final InstanceIdentifier<FlowCapableNode> FLOW_NODE_WILD_CARD_IDENTIFIER = InstanceIdentifier
            .create(Nodes.class)
            .child(Node.class)
            .augmentation(FlowCapableNode.class);

    private final ForwardingRulesManager provider;

    private ListenerRegistration<DataChangeListener> listenerRegistration;

    public AbstractFlowNodeReconciliator (final ForwardingRulesManager manager, final DataBroker db) {
        this.provider = Preconditions.checkNotNull(manager, "ForwardingRulesManager can not be null!");
        Preconditions.checkNotNull(db, "DataBroker can not be null!");
        /* Build Path */

        SimpleTaskRetryLooper looper = new SimpleTaskRetryLooper(ForwardingRulesManagerImpl.STARTUP_LOOP_TICK,
                ForwardingRulesManagerImpl.STARTUP_LOOP_MAX_RETRIES);
        try {
            listenerRegistration = looper.loopUntilNoException(new Callable<ListenerRegistration<DataChangeListener>>() {
                @Override
                public ListenerRegistration<DataChangeListener> call() throws Exception {
                    return db.registerDataChangeListener(LogicalDatastoreType.OPERATIONAL,
                            FLOW_NODE_WILD_CARD_IDENTIFIER, AbstractFlowNodeReconciliator.this, AsyncDataBroker.DataChangeScope.BASE);
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
            } finally {
                listenerRegistration = null;
            }
        }
    }

    @Override
    public void onDataChanged(final AsyncDataChangeEvent<InstanceIdentifier<?>, DataObject> changeEvent) {
        Preconditions.checkNotNull(changeEvent,"Async ChangeEvent can not be null!");
        /* All DataObjects for create */
        final Set<InstanceIdentifier<?>> createdData = changeEvent.getCreatedData() != null
                ? changeEvent.getCreatedData().keySet() : Collections.<InstanceIdentifier<?>> emptySet();
        /* All DataObjects for remove */
        final Set<InstanceIdentifier<?>> removeData = changeEvent.getRemovedPaths() != null
                ? changeEvent.getRemovedPaths() : Collections.<InstanceIdentifier<?>> emptySet();

        for (InstanceIdentifier<?> entryKey : removeData) {
            final InstanceIdentifier<FlowCapableNode> nodeIdent = entryKey
                    .firstIdentifierOf(FlowCapableNode.class);
            if ( ! nodeIdent.isWildcarded()) {
                flowNodeDisconnected(nodeIdent);
            }
        }
        for (InstanceIdentifier<?> entryKey : createdData) {
            final InstanceIdentifier<FlowCapableNode> nodeIdent = entryKey
                    .firstIdentifierOf(FlowCapableNode.class);
            if ( ! nodeIdent.isWildcarded()) {
                flowNodeConnected(nodeIdent, (FlowCapableNode) changeEvent.getCreatedData().get(entryKey));
            }
        }
    }

    @Override
    public void flowNodeDisconnected(InstanceIdentifier<FlowCapableNode> disconnectedNode) {
        provider.unregistrateNode(disconnectedNode);
    }

    @Override
    public void flowNodeConnected(InstanceIdentifier<FlowCapableNode> connectedNode, FlowCapableNode flowCapableNodeOperational) {
        if ( ! provider.isNodeActive(connectedNode)) {
            provider.registrateNewNode(connectedNode);
            reconciliation(connectedNode, flowCapableNodeOperational);
        }
    }

    abstract void reconciliation(final InstanceIdentifier<FlowCapableNode> nodeIdent, final FlowCapableNode flowCapableNodeOperational);

    /**
     * @return delegated flow commiter
     */
    public ForwardingRulesCommiter<Flow, AddFlowOutput, RemoveFlowOutput, UpdateFlowOutput> getFlowCommiter() {
        return provider.getFlowCommiter();
    }

    /**
     * @return delegated group commiter
     */
    public ForwardingRulesCommiter<Group, AddGroupOutput, RemoveGroupOutput, UpdateGroupOutput> getGroupCommiter() {
        return provider.getGroupCommiter();
    }

    /**
     * @return delegated meter commiter
     */
    public ForwardingRulesCommiter<Meter, AddMeterOutput, RemoveMeterOutput, UpdateMeterOutput> getMeterCommiter() {
        return provider.getMeterCommiter();
    }

    /**
     * @return delegated table features commiter
     */
    public ForwardingRulesUpdateCommiter<TableFeatures, UpdateTableOutput> getTableFeaturesCommiter() {
        return provider.getTableFeaturesCommiter();
    }

    /**
     * @return delegated barrier service
     */
    public FlowCapableTransactionService getFlowCapableTransactionService() {
        return provider.getFlowCapableTransactionService();
    }

    /**
     * @return delegated read tx (fresh created)
     */
    public ReadOnlyTransaction getReadTransaction() {
        return provider.getReadTranaction();
    }

    /**
     * @param nodeIdent path to target node
     * @return operational state of given node
     */
    public CheckedFuture<Optional<FlowCapableNode>, ReadFailedException> readConfiguredFlowCapableNode(
            final InstanceIdentifier<FlowCapableNode> nodeIdent) {
        return getReadTransaction().read(LogicalDatastoreType.CONFIGURATION, nodeIdent);
    }
}
