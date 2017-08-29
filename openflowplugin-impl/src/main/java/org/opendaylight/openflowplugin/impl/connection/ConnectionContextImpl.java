/**
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.impl.connection;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
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
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.PortStatusMessage;
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
    private List<PortStatusMessage> portStatusMessages = new ArrayList<>();

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
    public void setFeatures(final FeaturesReply featuresReply) {
        this.featuresReply = featuresReply;
    }

    @Override
    public void closeConnection(final boolean propagate) {
        disconnectDevice(propagate, true);
    }

    private void closeHandshakeContext() {
        LOG.debug("Trying closing handshake context for node {}", getSafeNodeIdForLOG());
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
        disconnectDevice(true, false);
    }

    private void disconnectDevice(final boolean propagate,
                                  final boolean forced) {
        final String device = Objects.nonNull(nodeId) ? nodeId.getValue() : getConnectionAdapter().getRemoteAddress().toString();
        final short auxiliaryId = Optional
                .ofNullable(getFeatures())
                .flatMap(features -> Optional
                        .ofNullable(features.getAuxiliaryId()))
                .orElse((short) 0);

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
        if (Objects.nonNull(deviceDisconnectedHandler)) {
            final BigInteger datapathId = featuresReply != null ? featuresReply.getDatapathId() : BigInteger.ZERO;
            if (LOG.isDebugEnabled()) {
                LOG.debug("Propagating connection closed event: {}, datapathId:{}.",
                        connectionAdapter.getRemoteAddress(), datapathId);
            }
            deviceDisconnectedHandler.onDeviceDisconnected(this);
        }
    }

    /**
     * This method returns safe nodeId for logging
     * @return string value od nodeId or string "null"
     */
    @Override
    public String getSafeNodeIdForLOG() {
        return Objects.nonNull(nodeId) ? nodeId.getValue() : "null";
    }

    @Override
    public void setOutboundQueueHandleRegistration(OutboundQueueHandlerRegistration<OutboundQueueProvider> outboundQueueHandlerRegistration) {
        this.outboundQueueHandlerRegistration = outboundQueueHandlerRegistration;
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
    public void setHandshakeContext(HandshakeContext handshakeContext) {
        this.handshakeContext = handshakeContext;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        ConnectionContextImpl that = (ConnectionContextImpl) o;

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
        public short getVersion() {
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
        public String toString() {
            return Objects.isNull(nodeId) ? "null" : getNodeId().getValue();
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
