/**
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.impl.connection;

import com.google.common.base.Preconditions;
import java.math.BigInteger;
import java.net.InetSocketAddress;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.opendaylight.mdsal.singleton.common.api.ServiceGroupIdentifier;
import org.opendaylight.openflowjava.protocol.api.connection.ConnectionAdapter;
import org.opendaylight.openflowjava.protocol.api.connection.OutboundQueue;
import org.opendaylight.openflowjava.protocol.api.connection.OutboundQueueHandlerRegistration;
import org.opendaylight.openflowplugin.api.openflow.connection.ConnectionContext;
import org.opendaylight.openflowplugin.api.openflow.connection.HandshakeContext;
import org.opendaylight.openflowplugin.api.openflow.connection.OutboundQueueProvider;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceInfo;
import org.opendaylight.openflowplugin.api.openflow.device.handlers.DeviceDisconnectedHandler;
import org.opendaylight.openflowplugin.impl.statistics.ofpspecific.SessionStatistics;
import org.opendaylight.openflowplugin.impl.util.DeviceStateUtil;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.NodeKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.FeaturesReply;
import org.opendaylight.yangtools.yang.binding.KeyedInstanceIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 */
public class ConnectionContextImpl implements ConnectionContext {

    private final ConnectionAdapter connectionAdapter;
    private volatile CONNECTION_STATE connectionState;
    private FeaturesReply featuresReply;
    private NodeId nodeId;
    private DeviceDisconnectedHandler deviceDisconnectedHandler;
    private static final Logger LOG = LoggerFactory.getLogger(ConnectionContextImpl.class);
    private OutboundQueueProvider outboundQueueProvider;
    private OutboundQueueHandlerRegistration<OutboundQueueProvider> outboundQueueHandlerRegistration;
    private HandshakeContext handshakeContext;
    private DeviceInfo deviceInfo;

    /**
     * @param connectionAdapter
     */
    public ConnectionContextImpl(final ConnectionAdapter connectionAdapter) {
        this.connectionAdapter = connectionAdapter;
    }

    @Override
    public ConnectionAdapter getConnectionAdapter() {
        return connectionAdapter;
    }

    @Override
    public OutboundQueue getOutboundQueueProvider() {
        return this.outboundQueueProvider;
    }

    @Override
    public void setOutboundQueueProvider(final OutboundQueueProvider outboundQueueProvider) {
        this.outboundQueueProvider = outboundQueueProvider;
    }

    @Override
    public CONNECTION_STATE getConnectionState() {
        return connectionState;
    }

    @Override
    public NodeId getNodeId() {
        return nodeId;
    }

    @Override
    public void setNodeId(final NodeId nodeId) {
        this.nodeId = nodeId;
    }

    @Override
    public FeaturesReply getFeatures() {
        return featuresReply;
    }

    @Override
    public void setDeviceDisconnectedHandler(final DeviceDisconnectedHandler deviceDisconnectedHandler) {
        this.deviceDisconnectedHandler = deviceDisconnectedHandler;
    }

    @Override
    public void setFeatures(final FeaturesReply featuresReply) {
        this.featuresReply = featuresReply;
    }

    @Override
    public void closeConnection(final boolean propagate) {
        if (null == nodeId){
            SessionStatistics.countEvent(connectionAdapter.getRemoteAddress().toString(), SessionStatistics.ConnectionStatus.CONNECTION_DISCONNECTED_BY_OFP);
        } else {
            SessionStatistics.countEvent(nodeId.toString(), SessionStatistics.ConnectionStatus.CONNECTION_DISCONNECTED_BY_OFP);
        }
        final BigInteger datapathId = featuresReply != null ? featuresReply.getDatapathId() : BigInteger.ZERO;
        LOG.debug("Actively closing connection: {}, datapathId: {}",
                connectionAdapter.getRemoteAddress(), datapathId);
        connectionState = ConnectionContext.CONNECTION_STATE.RIP;

        Future<Void> future = Executors.newSingleThreadExecutor().submit(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                unregisterOutboundQueue();
                return null;
            }
        });
        try {
            future.get(1, TimeUnit.SECONDS);
            LOG.info("Unregister outbound queue successful.");
        } catch (InterruptedException | TimeoutException | ExecutionException e) {
            LOG.warn("Unregister outbound queue throws exception for node {} ", nodeId);
            LOG.trace("Unregister outbound queue throws exception for node {} ", nodeId, e);
        }

        closeHandshakeContext();

        if (getConnectionAdapter().isAlive()) {
            getConnectionAdapter().disconnect();
        }

        if (propagate) {
            LOG.debug("Propagating device disconnect for node {}", nodeId);
            propagateDeviceDisconnectedEvent();
        } else {
            LOG.debug("Close connection without propagating for node {}", nodeId);
        }
    }

    private void closeHandshakeContext() {
        LOG.debug("Trying closing handshake context for node {}", nodeId);
        if (handshakeContext != null) {
            try {
                handshakeContext.close();
            } catch (Exception e) {
                LOG.error("handshake context closing failed:{} ", e);
            } finally {
                handshakeContext = null;
            }
        }
    }

    @Override
    public void onConnectionClosed() {
        if (null == nodeId){
            SessionStatistics.countEvent(connectionAdapter.getRemoteAddress().toString(), SessionStatistics.ConnectionStatus.CONNECTION_DISCONNECTED_BY_DEVICE);
        } else {
            SessionStatistics.countEvent(nodeId.toString(), SessionStatistics.ConnectionStatus.CONNECTION_DISCONNECTED_BY_DEVICE);
        }
        connectionState = ConnectionContext.CONNECTION_STATE.RIP;

        final InetSocketAddress remoteAddress = connectionAdapter.getRemoteAddress();
        final Short auxiliaryId;
        if (null != getFeatures() && null != getFeatures().getAuxiliaryId()) {
            auxiliaryId = getFeatures().getAuxiliaryId();
        } else {
            auxiliaryId = 0;
        }

        LOG.debug("disconnecting: node={}|auxId={}|connection state = {}",
                remoteAddress,
                auxiliaryId,
                getConnectionState());

        unregisterOutboundQueue();
        closeHandshakeContext();
        propagateDeviceDisconnectedEvent();
    }

    private void propagateDeviceDisconnectedEvent() {
        if (null != deviceDisconnectedHandler) {
            final BigInteger datapathId = featuresReply != null ? featuresReply.getDatapathId() : BigInteger.ZERO;
            LOG.debug("Propagating connection closed event: {}, datapathId:{}.",
                    connectionAdapter.getRemoteAddress(), datapathId);
            deviceDisconnectedHandler.onDeviceDisconnected(this);
        }
    }

    @Override
    public void setOutboundQueueHandleRegistration(OutboundQueueHandlerRegistration<OutboundQueueProvider> outboundQueueHandlerRegistration) {
        this.outboundQueueHandlerRegistration = outboundQueueHandlerRegistration;
    }

    private void unregisterOutboundQueue() {
        LOG.debug("Trying unregister outbound queue handler registration for node {}", nodeId);
        if (outboundQueueHandlerRegistration != null) {
            outboundQueueHandlerRegistration.close();
            outboundQueueHandlerRegistration = null;
        }
    }

    @Override
    public synchronized void changeStateToHandshaking() {
        connectionState = CONNECTION_STATE.HANDSHAKING;
    }

    @Override
    public synchronized void changeStateToTimeouting() {
        connectionState = CONNECTION_STATE.TIMEOUTING;
    }

    @Override
    public synchronized void changeStateToWorking() {
        connectionState = CONNECTION_STATE.WORKING;
    }

    @Override
    public DeviceInfo getDeviceInfo() {
        return this.deviceInfo;
    }

    @Override
    public void handshakeSuccessful() {
        Preconditions.checkNotNull(nodeId, "Cannot create DeviceInfo if 'NodeId' is not set!");
        Preconditions.checkNotNull(featuresReply, "Cannot create DeviceInfo if 'features' is not set!");
        this.deviceInfo = new DeviceInfoImpl(
                nodeId,
                DeviceStateUtil.createNodeInstanceIdentifier(nodeId),
                featuresReply.getVersion(),
                featuresReply.getDatapathId(),
                outboundQueueProvider);
    }

    @Override
    public void setHandshakeContext(HandshakeContext handshakeContext) {
        this.handshakeContext = handshakeContext;
    }


    private class DeviceInfoImpl implements DeviceInfo {

        private final NodeId nodeId;
        private final KeyedInstanceIdentifier<Node, NodeKey> nodeII;
        private final Short version;
        private final BigInteger datapathId;
        private final ServiceGroupIdentifier serviceGroupIdentifier;
        private OutboundQueue outboundQueueProvider;

        DeviceInfoImpl(
                final NodeId nodeId,
                final KeyedInstanceIdentifier<Node, NodeKey> nodeII,
                final Short version,
                final BigInteger datapathId,
                final OutboundQueue outboundQueueProvider) {
            this.nodeId = nodeId;
            this.nodeII = nodeII;
            this.version = version;
            this.datapathId = datapathId;
            this.outboundQueueProvider = outboundQueueProvider;
            this.serviceGroupIdentifier = ServiceGroupIdentifier.create(this.nodeId.getValue());
        }

        @Override
        public NodeId getNodeId() {
            return nodeId;
        }

        @Override
        public KeyedInstanceIdentifier<Node, NodeKey> getNodeInstanceIdentifier() {
            return nodeII;
        }

        @Override
        public Short getVersion() {
            return version;
        }

        @Override
        public BigInteger getDatapathId() {
            return datapathId;
        }

        @Override
        public ServiceGroupIdentifier getServiceIdentifier() {
            return this.serviceGroupIdentifier;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }

            if (o == null || getClass() != o.getClass()) {
                return false;
            }

            DeviceInfoImpl that = (DeviceInfoImpl) o;

            return  (nodeId.equals(that.nodeId) &&
                    nodeII.equals(that.nodeII) &&
                    version.equals(that.version) &&
                    datapathId.equals(that.datapathId));

        }

        @Override
        public int hashCode() {
            int result = nodeId.hashCode();
            result = 31 * result + nodeII.hashCode();
            result = 31 * result + version.hashCode();
            result = 31 * result + datapathId.hashCode();
            return result;
        }

        @Override
        public Long reserveXidForDeviceMessage() {
            return outboundQueueProvider.reserveEntry();
        }
    }
}
