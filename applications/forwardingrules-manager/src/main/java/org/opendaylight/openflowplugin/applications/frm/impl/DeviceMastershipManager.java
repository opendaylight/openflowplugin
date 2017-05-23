/**
 * Copyright (c) 2016, 2017 Pantheon Technologies s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.applications.frm.impl;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import javax.annotation.Nonnull;
import org.opendaylight.controller.md.sal.binding.api.ClusteredDataTreeChangeListener;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.DataObjectModification;
import org.opendaylight.controller.md.sal.binding.api.DataTreeIdentifier;
import org.opendaylight.controller.md.sal.binding.api.DataTreeModification;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.controller.sal.binding.api.NotificationProviderService;
import org.opendaylight.mdsal.singleton.common.api.ClusterSingletonServiceProvider;
import org.opendaylight.openflowplugin.applications.frm.FlowNodeReconciliation;
import org.opendaylight.openflowplugin.common.wait.SimpleTaskRetryLooper;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNode;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeConnectorRemoved;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeConnectorUpdated;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeRemoved;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeUpdated;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.Nodes;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.OpendaylightInventoryListener;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.NodeKey;
import org.opendaylight.yangtools.concepts.ListenerRegistration;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Manager for clustering service registrations of {@link DeviceMastership}.
 */
public class DeviceMastershipManager implements ClusteredDataTreeChangeListener<FlowCapableNode>,
        OpendaylightInventoryListener, AutoCloseable{
    private static final Logger LOG = LoggerFactory.getLogger(DeviceMastershipManager.class);
    private static final InstanceIdentifier<FlowCapableNode> II_TO_FLOW_CAPABLE_NODE
            = InstanceIdentifier.builder(Nodes.class)
            .child(Node.class)
            .augmentation(FlowCapableNode.class)
            .build();

    private final ClusterSingletonServiceProvider clusterSingletonService;
    private final ListenerRegistration<?> notifListenerRegistration;
    private final FlowNodeReconciliation reconcliationAgent;
    private final DataBroker dataBroker;
    private final ConcurrentHashMap<NodeId, DeviceMastership> deviceMasterships = new ConcurrentHashMap();
    private final Object lockObj = new Object();
    private ListenerRegistration<DeviceMastershipManager> listenerRegistration;
    private Set<InstanceIdentifier<FlowCapableNode>> activeNodes = Collections.emptySet();

    public DeviceMastershipManager(final ClusterSingletonServiceProvider clusterSingletonService,
                                   final NotificationProviderService notificationService,
                                   final FlowNodeReconciliation reconcliationAgent,
                                   final DataBroker dataBroker) {
        this.clusterSingletonService = clusterSingletonService;
        this.notifListenerRegistration = notificationService.registerNotificationListener(this);
        this.reconcliationAgent = reconcliationAgent;
        this.dataBroker = dataBroker;
        registerNodeListener();
    }

    public boolean isDeviceMastered(final NodeId nodeId) {
        return deviceMasterships.get(nodeId) != null && deviceMasterships.get(nodeId).isDeviceMastered();
    }

    public boolean isNodeActive(final NodeId nodeId) {
        final InstanceIdentifier<FlowCapableNode> flowNodeIdentifier = InstanceIdentifier.create(Nodes.class)
                .child(Node.class, new NodeKey(nodeId)).augmentation(FlowCapableNode.class);
        return activeNodes.contains(flowNodeIdentifier);

    }

    @VisibleForTesting
    ConcurrentHashMap<NodeId, DeviceMastership> getDeviceMasterships() {
        return deviceMasterships;
    }

    /**
     * Temporary solution before Mastership manager from plugin.
     * Remove notification after update.
     * Update node notification should be send only when mastership in plugin was granted.
     * @param notification received notification
     */
    @Override
    public void onNodeUpdated(NodeUpdated notification) {
        LOG.debug("NodeUpdate notification received : {}", notification);
        DeviceMastership membership = deviceMasterships.computeIfAbsent(notification.getId(), device ->
                new DeviceMastership(notification.getId(), reconcliationAgent));
        membership.reconcile();
    }

    @Override
    public void onNodeConnectorUpdated(NodeConnectorUpdated notification) {
        //Not published by plugin
    }

    @Override
    public void onNodeRemoved(NodeRemoved notification) {
        LOG.debug("NodeRemoved notification received : {}", notification);
        NodeId nodeId = notification.getNodeRef().getValue().firstKeyOf(Node.class).getId();
        final DeviceMastership mastership = deviceMasterships.remove(nodeId);
        if (mastership != null) {
            mastership.close();
            LOG.info("Unregistered FRM cluster singleton service for service id : {}", nodeId.getValue());
        }
    }

    @Override
    public void onNodeConnectorRemoved(NodeConnectorRemoved notification) {
        //Not published by plugin
    }

    @Override
    public void onDataTreeChanged(@Nonnull Collection<DataTreeModification<FlowCapableNode>> changes) {
        Preconditions.checkNotNull(changes, "Changes may not be null!");

        for (DataTreeModification<FlowCapableNode> change : changes) {
            final InstanceIdentifier<FlowCapableNode> key = change.getRootPath().getRootIdentifier();
            final DataObjectModification<FlowCapableNode> mod = change.getRootNode();
            final InstanceIdentifier<FlowCapableNode> nodeIdent =
                    key.firstIdentifierOf(FlowCapableNode.class);

            switch (mod.getModificationType()) {
                case DELETE:
                    if (mod.getDataAfter() == null) {
                        remove(key, mod.getDataBefore(), nodeIdent);
                    }
                    break;
                case SUBTREE_MODIFIED:
                    //NO-OP since we do not need to reconcile on Node-updated
                    break;
                case WRITE:
                    if (mod.getDataBefore() == null) {
                        add(key, mod.getDataAfter(), nodeIdent);
                    }
                    break;
                default:
                    throw new IllegalArgumentException("Unhandled modification type " + mod.getModificationType());
            }
        }
    }

    public void remove(InstanceIdentifier<FlowCapableNode> identifier, FlowCapableNode del,
                       InstanceIdentifier<FlowCapableNode> nodeIdent) {
        if(compareInstanceIdentifierTail(identifier,II_TO_FLOW_CAPABLE_NODE)){
            if (LOG.isDebugEnabled()) {
                LOG.debug("Node removed: {}",nodeIdent.firstKeyOf(Node.class).getId().getValue());
            }

            if ( ! nodeIdent.isWildcarded()) {
                if (activeNodes.contains(nodeIdent)) {
                    synchronized (lockObj) {
                        if (activeNodes.contains(nodeIdent)) {
                            Set<InstanceIdentifier<FlowCapableNode>> set =
                                    Sets.newHashSet(activeNodes);
                            set.remove(nodeIdent);
                            activeNodes = Collections.unmodifiableSet(set);
                            setNodeOperationalStatus(nodeIdent,false);
                        }
                    }
                }
            }

        }
    }

    public void add(InstanceIdentifier<FlowCapableNode> identifier, FlowCapableNode add,
                    InstanceIdentifier<FlowCapableNode> nodeIdent) {
        if(compareInstanceIdentifierTail(identifier,II_TO_FLOW_CAPABLE_NODE)){
            if (LOG.isDebugEnabled()) {
                LOG.debug("Node added: {}",nodeIdent.firstKeyOf(Node.class).getId().getValue());
            }

            if ( ! nodeIdent.isWildcarded()) {
                if (!activeNodes.contains(nodeIdent)) {
                    synchronized (lockObj) {
                        if (!activeNodes.contains(nodeIdent)) {
                            Set<InstanceIdentifier<FlowCapableNode>> set = Sets.newHashSet(activeNodes);
                            set.add(nodeIdent);
                            activeNodes = Collections.unmodifiableSet(set);
                            setNodeOperationalStatus(nodeIdent,true);
                        }
                    }
                }
            }
        }
    }

    @Override
    public void close() {
        if (listenerRegistration != null) {
            try {
                listenerRegistration.close();
            } catch (Exception e) {
                LOG.warn("Error occurred while closing operational Node listener: {}", e.getMessage());
                LOG.debug("Error occurred while closing operational Node listener", e);
            }
            listenerRegistration = null;
        }
        if (notifListenerRegistration != null) {
            notifListenerRegistration.close();
        }
    }


    private boolean compareInstanceIdentifierTail(InstanceIdentifier<?> identifier1,
                                                  InstanceIdentifier<?> identifier2) {
        return Iterables.getLast(identifier1.getPathArguments()).equals(Iterables.getLast(identifier2.getPathArguments()));
    }

    private void setNodeOperationalStatus(InstanceIdentifier<FlowCapableNode> nodeIid, boolean status) {
        NodeId nodeId = nodeIid.firstKeyOf(Node.class).getId();
        if (nodeId != null ) {
            if (deviceMasterships.containsKey(nodeId) ) {
                deviceMasterships.get(nodeId).setDeviceOperationalStatus(status);
                LOG.debug("Operational status of device {} is set to {}",nodeId, status);
            }
        }
    }
    private void registerNodeListener(){

        final InstanceIdentifier<FlowCapableNode> flowNodeWildCardIdentifier = InstanceIdentifier.create(Nodes.class)
                .child(Node.class).augmentation(FlowCapableNode.class);

        final DataTreeIdentifier<FlowCapableNode> treeId =
                new DataTreeIdentifier<>(LogicalDatastoreType.OPERATIONAL, flowNodeWildCardIdentifier);

        try {
            SimpleTaskRetryLooper looper = new SimpleTaskRetryLooper(ForwardingRulesManagerImpl.STARTUP_LOOP_TICK,
                    ForwardingRulesManagerImpl.STARTUP_LOOP_MAX_RETRIES);

            listenerRegistration = looper.loopUntilNoException(() ->
                    dataBroker.registerDataTreeChangeListener(treeId, DeviceMastershipManager.this));
        } catch (Exception e) {
            LOG.warn("Data listener registration failed: {}", e.getMessage());
            LOG.debug("Data listener registration failed ", e);
            throw new IllegalStateException("Node listener registration failed!", e);
        }
    }
}
