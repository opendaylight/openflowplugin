/*
 * Copyright (c) 2016, 2017 Pantheon Technologies s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.applications.frm.impl;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.MoreExecutors;
import com.google.common.util.concurrent.SettableFuture;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;
import org.checkerframework.checker.lock.qual.Holding;
import org.opendaylight.mdsal.binding.api.DataBroker;
import org.opendaylight.mdsal.binding.api.DataObjectDeleted;
import org.opendaylight.mdsal.binding.api.DataObjectModified;
import org.opendaylight.mdsal.binding.api.DataObjectWritten;
import org.opendaylight.mdsal.binding.api.DataTreeChangeListener;
import org.opendaylight.mdsal.binding.api.DataTreeModification;
import org.opendaylight.mdsal.binding.api.RpcProviderService;
import org.opendaylight.mdsal.common.api.LogicalDatastoreType;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceInfo;
import org.opendaylight.openflowplugin.api.openflow.mastership.MastershipChangeService;
import org.opendaylight.openflowplugin.api.openflow.mastership.MastershipChangeServiceManager;
import org.opendaylight.openflowplugin.applications.frm.FlowNodeReconciliation;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNode;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.Nodes;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.NodeBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.NodeKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.openflowplugin.app.frm.reconciliation.service.rev180227.ReconcileNodeInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.openflowplugin.app.frm.reconciliation.service.rev180227.ReconcileNodeOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.openflowplugin.app.frm.reconciliation.service.rev180227.ReconcileNodeOutputBuilder;
import org.opendaylight.yangtools.binding.DataObjectIdentifier;
import org.opendaylight.yangtools.binding.DataObjectReference;
import org.opendaylight.yangtools.concepts.Registration;
import org.opendaylight.yangtools.yang.common.ErrorType;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.opendaylight.yangtools.yang.common.RpcResultBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Manager for clustering service registrations of {@link DeviceMastership}.
 */
public class DeviceMastershipManager implements DataTreeChangeListener<FlowCapableNode>, AutoCloseable,
        MastershipChangeService {
    private static final Logger LOG = LoggerFactory.getLogger(DeviceMastershipManager.class);
    private static final DataObjectReference<FlowCapableNode> II_TO_FLOW_CAPABLE_NODE =
        DataObjectReference.builder(Nodes.class).child(Node.class).augmentation(FlowCapableNode.class).build();

    private final ConcurrentHashMap<NodeId, DeviceMastership> deviceMasterships = new ConcurrentHashMap<>();
    private final Set<DataObjectIdentifier<FlowCapableNode>> activeNodes = ConcurrentHashMap.newKeySet();
    private final ReentrantLock lock = new ReentrantLock();
    private final FlowNodeReconciliation reconcliationAgent;
    private final RpcProviderService rpcProviderService;

    private Registration listenerRegistration;
    private Registration mastershipChangeServiceRegistration;

    @SuppressFBWarnings(value = "MC_OVERRIDABLE_METHOD_CALL_IN_CONSTRUCTOR", justification = "Non-final for mocking")
    public DeviceMastershipManager(final FlowNodeReconciliation reconcliationAgent,
                                   final DataBroker dataBroker,
                                   final MastershipChangeServiceManager mastershipChangeServiceManager,
                                   final RpcProviderService rpcProviderService) {
        this.reconcliationAgent = reconcliationAgent;
        this.rpcProviderService = rpcProviderService;
        listenerRegistration = dataBroker.registerTreeChangeListener(LogicalDatastoreType.OPERATIONAL,
            DataObjectReference.builder(Nodes.class).child(Node.class).augmentation(FlowCapableNode.class).build(),
            this);
        mastershipChangeServiceRegistration = mastershipChangeServiceManager.register(this);
    }

    public boolean isDeviceMastered(final NodeId nodeId) {
        return deviceMasterships.get(nodeId) != null && deviceMasterships.get(nodeId).isDeviceMastered();
    }

    public boolean isNodeActive(final NodeId nodeId) {
        return activeNodes.contains(DataObjectIdentifier.builder(Nodes.class)
            .child(Node.class, new NodeKey(nodeId))
            .augmentation(FlowCapableNode.class)
            .build());
    }

    @VisibleForTesting
    ConcurrentHashMap<NodeId, DeviceMastership> getDeviceMasterships() {
        return deviceMasterships;
    }

    @Override
    public void onDataTreeChanged(final List<DataTreeModification<FlowCapableNode>> changes) {
        for (DataTreeModification<FlowCapableNode> change : changes) {
            final var key = change.path();
            final var nodeIdent = key.trimTo(FlowCapableNode.class);

            switch (change.getRootNode()) {
                case DataObjectDeleted<FlowCapableNode> deleted -> remove(key, deleted.dataBefore(), nodeIdent);
                case DataObjectModified<FlowCapableNode> modified -> {
                    // NO-OP since we do not need to reconcile on Node-updated
                }
                case DataObjectWritten<FlowCapableNode> written -> {
                    if (written.dataBefore() == null) {
                        add(key, written.dataAfter(), nodeIdent);
                    }
                }
            }
        }
    }

    public void remove(final DataObjectIdentifier<FlowCapableNode> identifier, final FlowCapableNode del,
            final DataObjectIdentifier<FlowCapableNode> nodeIdent) {
        if (compareInstanceIdentifierTail(identifier, II_TO_FLOW_CAPABLE_NODE)) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Node removed: {}", nodeIdent.getFirstKeyOf(Node.class).getId().getValue());
            }

            if (activeNodes.remove(nodeIdent)) {
                lock.lock();
                try {
                    reconcliationAgent.flowNodeDisconnected(nodeIdent);
                    setNodeOperationalStatus(nodeIdent, false);
                } finally {
                    lock.unlock();
                }
            }
        }
    }

    public void add(final DataObjectIdentifier<FlowCapableNode> identifier, final FlowCapableNode add,
            final DataObjectIdentifier<FlowCapableNode> nodeIdent) {
        if (compareInstanceIdentifierTail(identifier, II_TO_FLOW_CAPABLE_NODE)) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Node added: {}", nodeIdent.getFirstKeyOf(Node.class).getId().getValue());
            }

            if (activeNodes.add(nodeIdent)) {
                lock.lock();
                try {
                    setNodeOperationalStatus(nodeIdent, true);
                } finally {
                    lock.unlock();
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

    private static boolean compareInstanceIdentifierTail(final DataObjectIdentifier<?> identifier1,
            final DataObjectReference<?> identifier2) {
        return identifier1.lastStep().equals(identifier2.lastStep());
    }

    @Holding("lockObj")
    private void setNodeOperationalStatus(final DataObjectIdentifier<FlowCapableNode> nodeIid, final boolean status) {
        final var nodeId = nodeIid.getFirstKeyOf(Node.class).getId();
        if (nodeId == null) {
            return;
        }
        final var mastership = deviceMasterships.get(nodeId);
        if (mastership != null) {
            mastership.setDeviceOperationalStatus(status);
            LOG.debug("Operational status of device {} is set to {}", nodeId, status);
        }
    }

    @Override
    public void onBecomeOwner(final DeviceInfo deviceInfo) {
        LOG.debug("Mastership role notification received for device : {}", deviceInfo.getDatapathId());
        final var membership = deviceMasterships.computeIfAbsent(deviceInfo.getNodeId(),
            device -> new DeviceMastership(deviceInfo.getNodeId()));
        membership.reconcile();
        membership.registerReconcileNode(rpcProviderService, this::reconcileNode);
    }

    private ListenableFuture<RpcResult<ReconcileNodeOutput>> reconcileNode(final ReconcileNodeInput input) {
        final var nodeId = input.requireNodeId();
        LOG.debug("Triggering reconciliation for node: {}", nodeId);

        final var nodeDpn = new NodeBuilder().setId(new NodeId("openflow:" + nodeId)).build();
        final var connectedNode = DataObjectIdentifier.builder(Nodes.class)
                .child(Node.class, nodeDpn.key()).augmentation(FlowCapableNode.class)
                .build();
        final var rpcResult = SettableFuture.<RpcResult<ReconcileNodeOutput>>create();
        Futures.addCallback(reconcliationAgent.reconcileConfiguration(connectedNode), new FutureCallback<>() {
            @Override
            public void onSuccess(final Boolean result) {
                rpcResult.set(result
                    ? RpcResultBuilder.success(new ReconcileNodeOutputBuilder().setResult(result).build()).build()
                        : RpcResultBuilder.<ReconcileNodeOutput>failed()
                            .withError(ErrorType.APPLICATION, "Error while triggering reconciliation")
                            .build());
            }

            @Override
            public void onFailure(final Throwable error) {
                LOG.error("initReconciliation failed", error);
                rpcResult.set(RpcResultBuilder.<ReconcileNodeOutput>failed()
                        .withError(ErrorType.RPC, "Error while calling RPC").build());
            }
        }, MoreExecutors.directExecutor());

        LOG.debug("Completing reconciliation for node: {}", nodeId);
        return rpcResult;
    }

    @Override
    public void onLoseOwnership(final DeviceInfo deviceInfo) {
        final var mastership = deviceMasterships.remove(deviceInfo.getNodeId());
        if (mastership != null) {
            mastership.deregisterReconcileNode();
            mastership.close();
            LOG.debug("Unregistered deviceMastership for device : {}", deviceInfo.getNodeId());
        }
    }
}
