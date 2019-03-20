/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.connection;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.opendaylight.mdsal.singleton.common.api.ServiceGroupIdentifier;
import org.opendaylight.openflowjava.protocol.api.connection.ConnectionAdapter;
import org.opendaylight.openflowjava.protocol.api.connection.OutboundQueue;
import org.opendaylight.openflowjava.protocol.api.connection.OutboundQueueHandlerRegistration;
import org.opendaylight.openflowplugin.api.openflow.connection.ConnectionContext;
import org.opendaylight.openflowplugin.api.openflow.connection.DeviceConnectionStatusProvider;
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
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.PortStatusMessage;
import org.opendaylight.yangtools.yang.binding.KeyedInstanceIdentifier;
import org.opendaylight.yangtools.yang.common.Uint64;
import org.opendaylight.yangtools.yang.common.Uint8;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
    private final List<PortStatusMessage> portStatusMessages = new ArrayList<>();
    private final DeviceConnectionStatusProvider deviceConnectionStatusProvider;

    /**
     * Constructor.
     *
     * @param connectionAdapter - connection adapter
     */
    public ConnectionContextImpl(final ConnectionAdapter connectionAdapter,
                                 final DeviceConnectionStatusProvider deviceConnectionStatusProvider) {
        this.connectionAdapter = connectionAdapter;
        this.deviceConnectionStatusProvider = deviceConnectionStatusProvider;
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
        ((DeviceInfoImpl)this.deviceInfo).setOutboundQueueProvider(this.outboundQueueProvider);
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
    public void setFeatures(final FeaturesReply newFeaturesReply) {
        this.featuresReply = newFeaturesReply;
    }

    @Override
    public void closeConnection(final boolean propagate) {
        disconnectDevice(propagate, true);
    }

    private void closeHandshakeContext() {
        LOG.debug("Trying closing handshake context for node {}", getSafeNodeIdForLOG());
        if (handshakeContext != null) {
            handshakeContext.close();
            handshakeContext = null;
        }
    }

    @Override
    public void onConnectionClosed() {
        disconnectDevice(true, false);
    }

    private void disconnectDevice(final boolean propagate,
                                  final boolean forced) {
        final String device = nodeId != null ? nodeId.getValue() : getConnectionAdapter().getRemoteAddress().toString();

        final Uint8 auxiliaryId;
        if (featuresReply != null) {
            final Uint8 id = featuresReply.getAuxiliaryId();
            auxiliaryId = id == null ? Uint8.ZERO : id;
        } else {
            auxiliaryId = Uint8.ZERO;
        }

        if (connectionState == CONNECTION_STATE.RIP) {
            LOG.debug("Connection for device {} with auxiliary ID {} is already {}, so skipping closing.",
                    device, auxiliaryId, getConnectionState());
            return;
        }

        connectionState = ConnectionContext.CONNECTION_STATE.RIP;

        SessionStatistics.countEvent(device, forced
                ? SessionStatistics.ConnectionStatus.CONNECTION_DISCONNECTED_BY_OFP
                : SessionStatistics.ConnectionStatus.CONNECTION_DISCONNECTED_BY_DEVICE);

        LOG.debug("{}: device={} | auxiliaryId={} | connectionState={}",
                forced ? "Actively closing connection" : "Disconnecting",
                device,
                auxiliaryId,
                getConnectionState());

        portStatusMessages.clear();
        unregisterOutboundQueue();
        closeHandshakeContext();

        if (forced && getConnectionAdapter().isAlive()) {
            getConnectionAdapter().disconnect();
        }

        if (propagate) {
            propagateDeviceDisconnectedEvent();
        }
    }

    private void propagateDeviceDisconnectedEvent() {
        if (deviceDisconnectedHandler != null) {
            final Uint64 datapathId = featuresReply != null ? featuresReply.getDatapathId() : Uint64.ZERO;
            if (LOG.isDebugEnabled()) {
                LOG.debug("Propagating connection closed event: {}, datapathId:{}.",
                        connectionAdapter.getRemoteAddress(), datapathId);
            }
            deviceDisconnectedHandler.onDeviceDisconnected(this);
        }
    }

    /**
     * Get safe nodeId for logging.
     *
     * @return string value od nodeId or string "null"
     */
    @Override
    public String getSafeNodeIdForLOG() {
        return nodeId == null ? "null" : nodeId.getValue();
    }

    @Override
    public void setOutboundQueueHandleRegistration(
            final OutboundQueueHandlerRegistration<OutboundQueueProvider> newRegistration) {
        this.outboundQueueHandlerRegistration = newRegistration;
    }

    private void unregisterOutboundQueue() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Trying unregister outbound queue handler registration for node {}", getSafeNodeIdForLOG());
        }
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
    public void handlePortStatusMessage(final PortStatusMessage portStatusMessage) {
        LOG.info("Received early port status message for node {} with reason {} and state {}",
                getSafeNodeIdForLOG(),
                portStatusMessage.getReason(),
                MoreObjects.firstNonNull(portStatusMessage.getState(), portStatusMessage.getStateV10()));

        LOG.debug("Early port status message body is {}", portStatusMessage);
        portStatusMessages.add(portStatusMessage);
    }

    @Override
    public List<PortStatusMessage> retrieveAndClearPortStatusMessages() {
        final List<PortStatusMessage> immutablePortStatusMessages = Collections.unmodifiableList(portStatusMessages);
        portStatusMessages.clear();
        return immutablePortStatusMessages;
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
    public void setHandshakeContext(final HandshakeContext handshakeContext) {
        this.handshakeContext = handshakeContext;
    }

    @Override
    public boolean equals(final Object object) {
        if (this == object) {
            return true;
        }

        if (object == null || getClass() != object.getClass()) {
            return false;
        }

        ConnectionContextImpl that = (ConnectionContextImpl) object;

        if (!connectionAdapter.equals(that.connectionAdapter)) {
            return false;
        }

        if (featuresReply != null ? !featuresReply.equals(that.featuresReply) : that.featuresReply != null) {
            return false;
        }

        return nodeId != null ? nodeId.equals(that.nodeId) : that.nodeId == null;

    }

    @Override
    public int hashCode() {
        int result = connectionAdapter.hashCode();
        result = 31 * result + (featuresReply != null ? featuresReply.hashCode() : 0);
        result = 31 * result + (nodeId != null ? nodeId.hashCode() : 0);
        return result;
    }

    private static class DeviceInfoImpl implements DeviceInfo {

        private final NodeId nodeId;
        private final KeyedInstanceIdentifier<Node, NodeKey> nodeII;
        private final Uint8 version;
        private final Uint64 datapathId;
        private final ServiceGroupIdentifier serviceGroupIdentifier;
        private OutboundQueue outboundQueueProvider;

        DeviceInfoImpl(
                final NodeId nodeId,
                final KeyedInstanceIdentifier<Node, NodeKey> nodeII,
                final Uint8 version,
                final Uint64 datapathId,
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
        public short getVersion() {
            return version.toJava();
        }

        @Override
        public Uint64 getDatapathId() {
            return datapathId;
        }

        @Override
        public ServiceGroupIdentifier getServiceIdentifier() {
            return this.serviceGroupIdentifier;
        }

        @Override
        public boolean equals(final Object object) {
            if (this == object) {
                return true;
            }

            if (object == null || getClass() != object.getClass()) {
                return false;
            }

            DeviceInfoImpl that = (DeviceInfoImpl) object;

            return  nodeId.equals(that.nodeId)
                    && nodeII.equals(that.nodeII)
                    && version.equals(that.version)
                    && datapathId.equals(that.datapathId);

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
        public String toString() {
            return nodeId == null ? "null" : getNodeId().getValue();
        }

        public void setOutboundQueueProvider(final OutboundQueue outboundQueueProvider) {
            this.outboundQueueProvider = outboundQueueProvider;
        }

        @Override
        public Long reserveXidForDeviceMessage() {
            return outboundQueueProvider.reserveEntry();
        }
    }
}
