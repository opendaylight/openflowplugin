/**
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.applications.statistics.manager.impl;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.Map;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.ReadOnlyTransaction;
import org.opendaylight.controller.md.sal.binding.api.ClusteredDataChangeListener;
import org.opendaylight.controller.md.sal.common.api.data.AsyncDataBroker.DataChangeScope;
import org.opendaylight.controller.md.sal.common.api.data.AsyncDataChangeEvent;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;
import org.opendaylight.controller.md.sal.common.api.clustering.Entity;
import org.opendaylight.controller.md.sal.common.api.clustering.EntityOwnershipChange;
import org.opendaylight.controller.md.sal.common.api.clustering.EntityOwnershipService;
import org.opendaylight.controller.md.sal.common.api.clustering.EntityOwnershipState;
import org.opendaylight.controller.md.sal.common.api.clustering.EntityOwnershipListener;
import org.opendaylight.controller.md.sal.common.api.clustering.EntityOwnershipListenerRegistration;
import org.opendaylight.controller.sal.binding.api.NotificationProviderService;
import org.opendaylight.openflowplugin.applications.statistics.manager.StatNodeRegistration;
import org.opendaylight.openflowplugin.applications.statistics.manager.StatPermCollector.StatCapabTypes;
import org.opendaylight.openflowplugin.applications.statistics.manager.StatisticsManager;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FeatureCapability;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNode;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNodeUpdated;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowFeatureCapabilityFlowStats;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowFeatureCapabilityGroupStats;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowFeatureCapabilityPortStats;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowFeatureCapabilityQueueStats;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowFeatureCapabilityTableStats;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.flow.node.SwitchFeatures;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeConnectorRemoved;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeConnectorUpdated;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeRef;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeRemoved;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeUpdated;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.Nodes;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.NodeKey;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifierWithPredicates;
import org.opendaylight.yangtools.concepts.ListenerRegistration;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier.InstanceIdentifierBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * statistics-manager
 * org.opendaylight.openflowplugin.applications.statistics.manager.impl
 *
 * StatNodeRegistrationImpl
 * {@link FlowCapableNode} Registration Implementation contains two method for registration/unregistration
 * {@link FeatureCapability} for every connect/disconnect {@link FlowCapableNode}. Process of connection/disconnection
 * is substituted by listening Operation/DS for add/delete {@link FeatureCapability}.
 * All statistic capabilities are reading from new Node directly without contacting device or DS.
 *
 * @author <a href="mailto:vdemcak@cisco.com">Vaclav Demcak</a>
 *
 * Created: Aug 28, 2014
 */
public class StatNodeRegistrationImpl implements StatNodeRegistration, ClusteredDataChangeListener, EntityOwnershipListener {

    private static final Logger LOG = LoggerFactory.getLogger(StatNodeRegistrationImpl.class);

    private final StatisticsManager manager;
    private ListenerRegistration<?> listenerRegistration;
    private ListenerRegistration<?> notifListenerRegistration;
    private DataBroker db;
    private EntityOwnershipListenerRegistration ofListenerRegistration = null;

    public StatNodeRegistrationImpl(final StatisticsManager manager, final DataBroker db,
            final NotificationProviderService notificationService) {
        this.manager = Preconditions.checkNotNull(manager, "StatisticManager can not be null!");
        this.db = Preconditions.checkNotNull(db, "DataBroker can not be null!");
        Preconditions.checkArgument(notificationService != null, "NotificationProviderService can not be null!");
        notifListenerRegistration = notificationService.registerNotificationListener(this);
        /* Build Path */
        final InstanceIdentifier<FlowCapableNode> flowNodeWildCardIdentifier = InstanceIdentifier.create(Nodes.class)
                .child(Node.class).augmentation(FlowCapableNode.class);
        listenerRegistration = db.registerDataChangeListener(LogicalDatastoreType.OPERATIONAL,
                flowNodeWildCardIdentifier, StatNodeRegistrationImpl.this, DataChangeScope.BASE);
        if(manager.getOwnershipService() != null) {
            ofListenerRegistration = manager.getOwnershipService().registerListener("openflow", this);
        }
    }

    @Override
    public void close() throws Exception {

        if (notifListenerRegistration != null) {
            try {
                notifListenerRegistration.close();
            }
            catch (final Exception e) {
                LOG.warn("Error by stop FlowCapableNode Notification StatNodeRegistration. Exception {}", e);
            }
            notifListenerRegistration = null;
        }

        if (listenerRegistration != null) {
            try {
                listenerRegistration.close();
            } catch (final Exception e) {
                LOG.warn("Error by stop FlowCapableNode DataChange StatListeningCommiter.", e);
            }
            listenerRegistration = null;
        }

        if (ofListenerRegistration!= null) {
            try {
                ofListenerRegistration.close();
            } catch (final Exception e) {
                LOG.warn("Error by stop FlowCapableNode EntityOwnershipListener.", e);
            }
            ofListenerRegistration = null;
        }
    }

    @Override
    public void connectFlowCapableNode(final InstanceIdentifier<SwitchFeatures> keyIdent,
            final SwitchFeatures data, final InstanceIdentifier<Node> nodeIdent) {
        Preconditions.checkNotNull(keyIdent, "InstanceIdentifier can not be null!");
        Preconditions.checkNotNull(data, "SwitchFeatures data for {} can not be null!", keyIdent);
        Preconditions.checkArgument(( ! keyIdent.isWildcarded()), "InstanceIdentifier is WildCarded!");

        LOG.trace("STAT-MANAGER - connecting flow capable node {}", nodeIdent);
        final List<StatCapabTypes> statCapabTypes = new ArrayList<>();
        Short maxCapTables = Short.valueOf("1");

        final List<Class<? extends FeatureCapability>> capabilities = data.getCapabilities() != null
                ? data.getCapabilities() : Collections.<Class<? extends FeatureCapability>> emptyList();
        for (final Class<? extends FeatureCapability> capability : capabilities) {
            if (FlowFeatureCapabilityTableStats.class.equals(capability)) {
                statCapabTypes.add(StatCapabTypes.TABLE_STATS);
            } else if (FlowFeatureCapabilityFlowStats.class.equals(capability)) {
                statCapabTypes.add(StatCapabTypes.FLOW_STATS);
            } else if (FlowFeatureCapabilityGroupStats.class.equals(capability)) {
                statCapabTypes.add(StatCapabTypes.GROUP_STATS);
            } else if (FlowFeatureCapabilityPortStats.class.equals(capability)) {
                statCapabTypes.add(StatCapabTypes.PORT_STATS);
            } else if (FlowFeatureCapabilityQueueStats.class.equals(capability)) {
                statCapabTypes.add(StatCapabTypes.QUEUE_STATS);
            }
        }
        maxCapTables = data.getMaxTables();

        final Optional<Short> maxTables = Optional.<Short> of(maxCapTables);
        manager.connectedNodeRegistration(nodeIdent, Collections.unmodifiableList(statCapabTypes), maxTables.get());
    }

    @Override
    public void disconnectFlowCapableNode(final InstanceIdentifier<Node> nodeIdent) {
        Preconditions.checkArgument(nodeIdent != null, "InstanceIdentifier can not be NULL!");
        Preconditions.checkArgument(( ! nodeIdent.isWildcarded()),
                "InstanceIdentifier {} is WildCarded!", nodeIdent);
        LOG.trace("STAT-MANAGER - disconnect flow capable node {}", nodeIdent);
        manager.disconnectedNodeUnregistration(nodeIdent);
    }

    private boolean preConfigurationCheck(final InstanceIdentifier<Node> nodeIdent) {
        Preconditions.checkNotNull(nodeIdent, "Node Instance Identifier can not be null!");
        NodeId nodeId = InstanceIdentifier.keyOf(nodeIdent).getId();
        final Entity entity = new Entity("openflow", nodeId.getValue());
        EntityOwnershipService ownershipService = manager.getOwnershipService();
        if(ownershipService == null) {
            LOG.error("preConfigCheck: EntityOwnershipService is null");
            return false;
        }
        Optional<EntityOwnershipState> entityOwnershipStateOptional = ownershipService.getOwnershipState(entity);
        if(!entityOwnershipStateOptional.isPresent()) { //abset - assume this ofp is owning entity
            LOG.warn("preConfigCheck: Entity state of {} is absent - acting as a non-owner",nodeId.getValue());
            return false;
        }
        final EntityOwnershipState entityOwnershipState = entityOwnershipStateOptional.get();
        if(!(entityOwnershipState.hasOwner() && entityOwnershipState.isOwner())) {
            LOG.info("preConfigCheck: Controller is not the owner of {}",nodeId.getValue());
            return false;
        }
        return true;
    }

    @Override
    public void onNodeConnectorRemoved(final NodeConnectorRemoved notification) {
        // NOOP
    }

    @Override
    public void onNodeConnectorUpdated(final NodeConnectorUpdated notification) {
        // NOOP
    }

    @Override
    public void onNodeRemoved(final NodeRemoved notification) {
        Preconditions.checkNotNull(notification);
        final NodeRef nodeRef = notification.getNodeRef();
        final InstanceIdentifier<?> nodeRefIdent = nodeRef.getValue();
        final InstanceIdentifier<Node> nodeIdent =
                nodeRefIdent.firstIdentifierOf(Node.class);
        if (nodeIdent != null) {
            LOG.debug("Received onNodeRemoved for node:{} ", nodeIdent);
            disconnectFlowCapableNode(nodeIdent);
        }
    }

    @Override
    public void onNodeUpdated(final NodeUpdated notification) {
        Preconditions.checkNotNull(notification);
        final FlowCapableNodeUpdated newFlowNode =
                notification.getAugmentation(FlowCapableNodeUpdated.class);
        LOG.info("Received onNodeUpdated for node {} ", newFlowNode);
        if (newFlowNode != null && newFlowNode.getSwitchFeatures() != null) {
            final NodeRef nodeRef = notification.getNodeRef();
            final InstanceIdentifier<?> nodeRefIdent = nodeRef.getValue();
            final InstanceIdentifier<Node> nodeIdent =
                    nodeRefIdent.firstIdentifierOf(Node.class);

            final InstanceIdentifier<SwitchFeatures> swichFeaturesIdent =
                    nodeIdent.augmentation(FlowCapableNode.class).child(SwitchFeatures.class);
            final SwitchFeatures switchFeatures = newFlowNode.getSwitchFeatures();
            connectFlowCapableNode(swichFeaturesIdent, switchFeatures, nodeIdent);
        }
    }

    @Override
    public void onDataChanged(final AsyncDataChangeEvent<InstanceIdentifier<?>, DataObject> changeEvent) {
        Preconditions.checkNotNull(changeEvent,"Async ChangeEvent can not be null!");
        /* All DataObjects for create */
        final Set<InstanceIdentifier<?>>  createdData = changeEvent.getCreatedData() != null
                ? changeEvent.getCreatedData().keySet() : Collections.<InstanceIdentifier<?>> emptySet();

        for (final InstanceIdentifier<?> entryKey : createdData) {
            final InstanceIdentifier<Node> nodeIdent = entryKey
                    .firstIdentifierOf(Node.class);
            if ( ! nodeIdent.isWildcarded()) {
                final NodeRef nodeRef = new NodeRef(nodeIdent);
                // FIXME: these calls is a job for handshake or for inventory manager
                /* check Group and Meter future */
                if(preConfigurationCheck(nodeIdent)) {
                    manager.getRpcMsgManager().getGroupFeaturesStat(nodeRef);
                    manager.getRpcMsgManager().getMeterFeaturesStat(nodeRef);
                }
            }
        }
    }

    @Override
    public void ownershipChanged(EntityOwnershipChange ownershipChange) {
        //I believe the only scenario we need to handle here is
        // isOwner=false && hasOwner=false. E.g switch is connected to only
        // one controller and that goes down, all other controller will get
        // notification about ownership change with the flag set as above.
        // In this scenario, topology manager should remove the node from
        // operational data store, so no explict action is required here.
        // But I need to test this behavior and then if requires implement
        // the logic here.
    }

}
