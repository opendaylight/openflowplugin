/*
 * Copyright (c) 2016, 2017 Pantheon Technologies s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.applications.frm.impl;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.mdsal.binding.api.ClusteredDataTreeChangeListener;
import org.opendaylight.mdsal.binding.api.DataBroker;
import org.opendaylight.mdsal.binding.api.DataObjectModification;
import org.opendaylight.mdsal.binding.api.DataTreeIdentifier;
import org.opendaylight.mdsal.binding.api.DataTreeModification;
import org.opendaylight.mdsal.binding.api.RpcProviderService;
import org.opendaylight.mdsal.common.api.LogicalDatastoreType;
import org.opendaylight.mdsal.singleton.common.api.ClusterSingletonServiceProvider;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceInfo;
import org.opendaylight.openflowplugin.api.openflow.mastership.MastershipChangeRegistration;
import org.opendaylight.openflowplugin.api.openflow.mastership.MastershipChangeService;
import org.opendaylight.openflowplugin.api.openflow.mastership.MastershipChangeServiceManager;
import org.opendaylight.openflowplugin.applications.frm.FlowNodeReconciliation;
import org.opendaylight.openflowplugin.common.wait.SimpleTaskRetryLooper;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNode;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.Nodes;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.NodeKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.openflowplugin.app.frm.reconciliation.service.rev180227.FrmReconciliationService;
import org.opendaylight.yangtools.concepts.ListenerRegistration;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Manager for clustering service registrations of {@link DeviceMastership}.
 */
public class DeviceMastershipManager implements ClusteredDataTreeChangeListener<FlowCapableNode>, AutoCloseable,
        MastershipChangeService {
    private static final Logger LOG = LoggerFactory.getLogger(DeviceMastershipManager.class);
    private static final InstanceIdentifier<FlowCapableNode> II_TO_FLOW_CAPABLE_NODE = InstanceIdentifier
            .builder(Nodes.class).child(Node.class).augmentation(FlowCapableNode.class).build();

    private final ClusterSingletonServiceProvider clusterSingletonService;
    private final FlowNodeReconciliation reconcliationAgent;
    private final DataBroker dataBroker;
    private final ConcurrentHashMap<NodeId, DeviceMastership> deviceMasterships = new ConcurrentHashMap<>();
    private final Object lockObj = new Object();
    private final RpcProviderService rpcProviderService;
    private final FrmReconciliationService reconcliationService;

    private ListenerRegistration<DeviceMastershipManager> listenerRegistration;
    private Set<InstanceIdentifier<FlowCapableNode>> activeNodes = Collections.emptySet();
    private MastershipChangeRegistration mastershipChangeServiceRegistration;

    public DeviceMastershipManager(final ClusterSingletonServiceProvider clusterSingletonService,
                                   final FlowNodeReconciliation reconcliationAgent,
                                   final DataBroker dataBroker,
                                   final MastershipChangeServiceManager mastershipChangeServiceManager,
                                   final RpcProviderService rpcProviderService,
                                   final FrmReconciliationService reconciliationService) {
        this.clusterSingletonService = clusterSingletonService;
        this.reconcliationAgent = reconcliationAgent;
        this.rpcProviderService = rpcProviderService;
        this.reconcliationService = reconciliationService;
        this.dataBroker = dataBroker;
        registerNodeListener();
        this.mastershipChangeServiceRegistration = mastershipChangeServiceManager.register(this);
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

    @Override
    public void onDataTreeChanged(@NonNull final Collection<DataTreeModification<FlowCapableNode>> changes) {
        for (DataTreeModification<FlowCapableNode> change : changes) {
            final InstanceIdentifier<FlowCapableNode> key = change.getRootPath().getRootIdentifier();
            final DataObjectModification<FlowCapableNode> mod = change.getRootNode();
            final InstanceIdentifier<FlowCapableNode> nodeIdent = key.firstIdentifierOf(FlowCapableNode.class);

            switch (mod.getModificationType()) {
                case DELETE:
                    if (mod.getDataAfter() == null) {
                        remove(key, mod.getDataBefore(), nodeIdent);
                    }
                    break;
                case SUBTREE_MODIFIED:
                    // NO-OP since we do not need to reconcile on Node-updated
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

    public void remove(final InstanceIdentifier<FlowCapableNode> identifier, final FlowCapableNode del,
            final InstanceIdentifier<FlowCapableNode> nodeIdent) {
        if (compareInstanceIdentifierTail(identifier, II_TO_FLOW_CAPABLE_NODE)) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Node removed: {}", nodeIdent.firstKeyOf(Node.class).getId().getValue());
            }

            if (!nodeIdent.isWildcarded() && activeNodes.contains(nodeIdent)) {
                synchronized (lockObj) {
                    if (activeNodes.contains(nodeIdent)) {
                        Set<InstanceIdentifier<FlowCapableNode>> set = Sets.newHashSet(activeNodes);
                        set.remove(nodeIdent);
                        reconcliationAgent.flowNodeDisconnected(nodeIdent);
                        activeNodes = Collections.unmodifiableSet(set);
                        setNodeOperationalStatus(nodeIdent, false);
                    }
                }
            }
        }
    }

    public void add(final InstanceIdentifier<FlowCapableNode> identifier, final FlowCapableNode add,
            final InstanceIdentifier<FlowCapableNode> nodeIdent) {
        if (compareInstanceIdentifierTail(identifier, II_TO_FLOW_CAPABLE_NODE)) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Node added: {}", nodeIdent.firstKeyOf(Node.class).getId().getValue());
            }

            if (!nodeIdent.isWildcarded() && !activeNodes.contains(nodeIdent)) {
                synchronized (lockObj) {
                    if (!activeNodes.contains(nodeIdent)) {
                        Set<InstanceIdentifier<FlowCapableNode>> set = Sets.newHashSet(activeNodes);
                        set.add(nodeIdent);
                        activeNodes = Collections.unmodifiableSet(set);
                        setNodeOperationalStatus(nodeIdent, true);
                    }
                }
            }
        }
    }

    @Override
    public void close() throws Exception {
        if (listenerRegistration != null) {
            listenerRegistration.close();
            listenerRegistration = null;
        }
        if (mastershipChangeServiceRegistration != null) {
            mastershipChangeServiceRegistration.close();
            mastershipChangeServiceRegistration = null;
        }
    }

    private static boolean compareInstanceIdentifierTail(final InstanceIdentifier<?> identifier1,
            final InstanceIdentifier<?> identifier2) {
        return Iterables.getLast(identifier1.getPathArguments())
                .equals(Iterables.getLast(identifier2.getPathArguments()));
    }

    private void setNodeOperationalStatus(final InstanceIdentifier<FlowCapableNode> nodeIid, final boolean status) {
        NodeId nodeId = nodeIid.firstKeyOf(Node.class).getId();
        if (nodeId != null && deviceMasterships.containsKey(nodeId)) {
            deviceMasterships.get(nodeId).setDeviceOperationalStatus(status);
            LOG.debug("Operational status of device {} is set to {}", nodeId, status);
        }
    }

    @SuppressWarnings("IllegalCatch")
    private void registerNodeListener() {

        final InstanceIdentifier<FlowCapableNode> flowNodeWildCardIdentifier = InstanceIdentifier.create(Nodes.class)
                .child(Node.class).augmentation(FlowCapableNode.class);

        final DataTreeIdentifier<FlowCapableNode> treeId = DataTreeIdentifier.create(LogicalDatastoreType.OPERATIONAL,
                flowNodeWildCardIdentifier);

        try {
            SimpleTaskRetryLooper looper = new SimpleTaskRetryLooper(ForwardingRulesManagerImpl.STARTUP_LOOP_TICK,
                    ForwardingRulesManagerImpl.STARTUP_LOOP_MAX_RETRIES);

            listenerRegistration = looper.loopUntilNoException(
                () -> dataBroker.registerDataTreeChangeListener(treeId, DeviceMastershipManager.this));
        } catch (Exception e) {
            LOG.warn("Data listener registration failed: {}", e.getMessage());
            LOG.debug("Data listener registration failed ", e);
            throw new IllegalStateException("Node listener registration failed!", e);
        }
    }

    @Override
    public void onBecomeOwner(@NonNull final DeviceInfo deviceInfo) {
        LOG.debug("Mastership role notification received for device : {}", deviceInfo.getDatapathId());
        DeviceMastership membership = deviceMasterships.computeIfAbsent(deviceInfo.getNodeId(),
            device -> new DeviceMastership(deviceInfo.getNodeId()));
        membership.reconcile();
        membership.registerReconciliationRpc(rpcProviderService, reconcliationService);
    }

    @Override
    public void onLoseOwnership(@NonNull final DeviceInfo deviceInfo) {
        final DeviceMastership mastership = deviceMasterships.remove(deviceInfo.getNodeId());
        if (mastership != null) {
            mastership.deregisterReconciliationRpc();
            mastership.close();
            LOG.debug("Unregistered deviceMastership for device : {}", deviceInfo.getNodeId());
        }
    }
}
