/*
 * Copyright (c) 2016 Ericsson India Global Services Pvt Ltd. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.applications.southboundmanager.openflowservice.transactions.impl;


import org.opendaylight.controller.sal.binding.api.NotificationService;
import org.opendaylight.openflowplugin.applications.southboundmanager.openflowservice.api.ErrorCallable;
import org.opendaylight.openflowplugin.applications.southboundmanager.openflowservice.api.OpenflowErrorCause;
import org.opendaylight.openflowplugin.applications.southboundmanager.openflowservice.transactions.api.TransactionTracker;
import org.opendaylight.openflowplugin.applications.southboundmanager.openflowservice.transactions.api.TransactionTrackerFactory;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.transaction.rev150304.TransactionId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yang.gen.v1.urn.opendaylight.node.error.service.rev140410.*;
import org.opendaylight.yangtools.concepts.ListenerRegistration;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

public class TransactionTrackerFactoryImpl implements TransactionTrackerFactory {

    private static final Logger s_logger = LoggerFactory.getLogger(TransactionTrackerFactoryImpl.class);
    private ConcurrentHashMap<NodeId,TransactionTrackerImpl> m_mapTransactionTracker;
    private ListenerRegistration nodeErrorListenerRegistration;
    private AtomicLong bundleIdGenerator = new AtomicLong(0);

    public TransactionTrackerFactoryImpl(){
        m_mapTransactionTracker = new ConcurrentHashMap<>();
    }

    @Override
    public TransactionTracker getCacheEntry(NodeId nodeId) {
        return m_mapTransactionTracker.get(nodeId);
    }

    @Override
    public void addCacheEntry(NodeId nodeId, boolean isOwner, boolean hasOwner) {

        s_logger.debug("TransactionTracker being created for nodeId: {}", nodeId);
        TransactionTrackerImpl transactionTracker = TransactionTrackerImpl.
                createInstance(nodeId, bundleIdGenerator);
        transactionTracker.setIsOwner(isOwner);
        transactionTracker.setHasOwner(hasOwner);
        m_mapTransactionTracker.put(nodeId, transactionTracker);
    }

    @Override
    public void removeCacheEntry(NodeId nodeId) {
        s_logger.debug("TransactionTracker being removed for nodeId: {}", nodeId);
        TransactionTracker txTracker = m_mapTransactionTracker.remove(nodeId);
        if (txTracker != null) {
            txTracker.invalidateAll();
        }
    }

    @Override
    public void registerNotificationListener(NotificationService notificationService) {
        nodeErrorListenerRegistration = notificationService.
                registerNotificationListener(new NodeErrorNotificationListener());
    }

    @Override
    public void deregisterNotificationListener() {
        nodeErrorListenerRegistration.close();
    }

    private void handleNodeError(NodeId nodeId, TransactionId transactionId) {
        s_logger.error("Node Error occurred on node: {}", nodeId.getValue());
        TransactionTrackerImpl txTracker = m_mapTransactionTracker.get(nodeId);
        if (txTracker != null) {
            ErrorCallable errorCallable = txTracker.getTransactionEntry(transactionId);
            if (errorCallable != null) {
                // RPC succeeded
                txTracker.removeTransactionEntry(transactionId);
                errorCallable.setIsCalled(true);
                errorCallable.setCause(OpenflowErrorCause.OF_ERROR);
                try {
                    errorCallable.call();
                } catch (Exception ex) {
                    s_logger.error("Exception caught while executing error callable. ex: {}", ex.getStackTrace());
                }
            } else {
                // RPC hasn't succeeded or error occurred after cache entry expiry timeout.
                txTracker.addTransactionEntry(transactionId, new ErrorCallable() {
                    @Override
                    public Object call() throws Exception {
                        // This is a stub implementation and is not suppose to be used.
                        return null;
                    }
                });
            }
        }
    }

    private class NodeErrorNotificationListener implements NodeErrorListener {

        private NodeId getNodeId(InstanceIdentifier<Node> nodeInstanceIdentifier){
            return nodeInstanceIdentifier.firstKeyOf(Node.class).getId();
        }

        @Override
        public void onExperimenterErrorNotification(ExperimenterErrorNotification notification) {
            s_logger.error("onExperimenterErrorNotification: notification: {}", notification);
            InstanceIdentifier<Node> nodeIId = (InstanceIdentifier<Node>)notification.getNode().getValue();
            handleNodeError(getNodeId(nodeIId), notification.getTransactionId());
        }

        @Override
        public void onHelloFailedErrorNotification(HelloFailedErrorNotification notification) {
            s_logger.error("onHelloFailedErrorNotification: notification: {}", notification);
        }

        @Override
        public void onBadInstructionErrorNotification(BadInstructionErrorNotification notification) {
            s_logger.error("onBadInstructionErrorNotification: notification: {}", notification);
            InstanceIdentifier<Node> nodeIId = (InstanceIdentifier<Node>)notification.getNode().getValue();
            handleNodeError(getNodeId(nodeIId), notification.getTransactionId());
        }

        @Override
        public void onMeterModErrorNotification(MeterModErrorNotification notification) {
            s_logger.error("onMeterModErrorNotification: notification: {}", notification);
            InstanceIdentifier<Node> nodeIId = (InstanceIdentifier<Node>)notification.getNode().getValue();
            handleNodeError(getNodeId(nodeIId), notification.getTransactionId());
        }

        @Override
        public void onBadMatchErrorNotification(BadMatchErrorNotification notification) {
            s_logger.error("onBadMatchErrorNotification: notification: {}", notification);
            InstanceIdentifier<Node> nodeIId = (InstanceIdentifier<Node>)notification.getNode().getValue();
            handleNodeError(getNodeId(nodeIId), notification.getTransactionId());
        }

        @Override
        public void onTableFeaturesErrorNotification(TableFeaturesErrorNotification notification) {
            s_logger.error("onTableFeaturesErrorNotification: notification: {}", notification);
            InstanceIdentifier<Node> nodeIId = (InstanceIdentifier<Node>)notification.getNode().getValue();
            handleNodeError(getNodeId(nodeIId), notification.getTransactionId());
        }

        @Override
        public void onBadRequestErrorNotification(BadRequestErrorNotification notification) {
            s_logger.error("onBadRequestErrorNotification: notification: {}", notification);
            InstanceIdentifier<Node> nodeIId = (InstanceIdentifier<Node>)notification.getNode().getValue();
            handleNodeError(getNodeId(nodeIId), notification.getTransactionId());
        }

        @Override
        public void onQueueOpErrorNotification(QueueOpErrorNotification notification) {
            s_logger.error("onQueueOpErrorNotification: notification: {}", notification);
            InstanceIdentifier<Node> nodeIId = (InstanceIdentifier<Node>)notification.getNode().getValue();
            handleNodeError(getNodeId(nodeIId), notification.getTransactionId());
        }

        @Override
        public void onSwitchConfigErrorNotification(SwitchConfigErrorNotification notification) {
            s_logger.error("onSwitchConfigErrorNotification: notification: {}", notification);
        }

        @Override
        public void onTableModErrorNotification(TableModErrorNotification notification) {
            s_logger.error("onTableModErrorNotification: notification: {}", notification);
            InstanceIdentifier<Node> nodeIId = (InstanceIdentifier<Node>)notification.getNode().getValue();
            handleNodeError(getNodeId(nodeIId), notification.getTransactionId());
        }

        @Override
        public void onPortModErrorNotification(PortModErrorNotification notification) {
            s_logger.error("onPortModErrorNotification: notification: {}", notification);
            InstanceIdentifier<Node> nodeIId = (InstanceIdentifier<Node>)notification.getNode().getValue();
            handleNodeError(getNodeId(nodeIId), notification.getTransactionId());
        }

        @Override
        public void onGroupModErrorNotification(GroupModErrorNotification notification) {
            s_logger.error("onGroupModErrorNotification: notification: {}", notification);
            InstanceIdentifier<Node> nodeIId = (InstanceIdentifier<Node>)notification.getNode().getValue();
            handleNodeError(getNodeId(nodeIId), notification.getTransactionId());
        }

        @Override
        public void onRoleRequestErrorNotification(RoleRequestErrorNotification notification) {
            s_logger.error("onRoleRequestErrorNotification: notification: {}", notification);
        }

        @Override
        public void onFlowModErrorNotification(FlowModErrorNotification notification) {
            s_logger.error("onFlowModErrorNotification: notification: {}", notification);
            InstanceIdentifier<Node> nodeIId = (InstanceIdentifier<Node>)notification.getNode().getValue();
            handleNodeError(getNodeId(nodeIId), notification.getTransactionId());
        }

        @Override
        public void onBadActionErrorNotification(BadActionErrorNotification notification) {
            s_logger.error("onBadActionErrorNotification: notification: {}", notification);
            InstanceIdentifier<Node> nodeIId = (InstanceIdentifier<Node>)notification.getNode().getValue();
            handleNodeError(getNodeId(nodeIId), notification.getTransactionId());
        }
    }
}
