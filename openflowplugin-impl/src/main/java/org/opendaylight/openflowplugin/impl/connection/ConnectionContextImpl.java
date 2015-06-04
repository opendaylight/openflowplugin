/**
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 * <p/>
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.connection;

import java.math.BigInteger;
import java.net.InetSocketAddress;
import org.opendaylight.openflowjava.protocol.api.connection.ConnectionAdapter;
import org.opendaylight.openflowjava.protocol.api.connection.OutboundQueue;
import org.opendaylight.openflowjava.protocol.api.connection.OutboundQueueHandlerRegistration;
import org.opendaylight.openflowplugin.api.openflow.connection.ConnectionContext;
import org.opendaylight.openflowplugin.api.openflow.connection.OutboundQueueProvider;
import org.opendaylight.openflowplugin.api.openflow.device.handlers.DeviceDisconnectedHandler;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.FeaturesReply;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 */
public class ConnectionContextImpl implements ConnectionContext {

    private final ConnectionAdapter connectionAdapter;
    private CONNECTION_STATE connectionState;
    private FeaturesReply featuresReply;
    private NodeId nodeId;
    private DeviceDisconnectedHandler deviceDisconnectedHandler;
    private static final Logger LOG = LoggerFactory.getLogger(ConnectionContextImpl.class);
    private OutboundQueueProvider outboundQueueProvider;
    private OutboundQueueHandlerRegistration<OutboundQueueProvider> outboundQueueHandlerRegistration;

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
    public void closeConnection(boolean propagate) {
        final BigInteger datapathId = featuresReply != null ? featuresReply.getDatapathId() : BigInteger.ZERO;
        LOG.debug("Actively closing connection: {}, datapathId:{}.",
                connectionAdapter.getRemoteAddress(), datapathId);
        connectionState = ConnectionContext.CONNECTION_STATE.RIP;

        unregisterOutboundQueue();
        if (getConnectionAdapter().isAlive()) {
            getConnectionAdapter().disconnect();
        }

        if (propagate) {
            propagateDeviceDisconnectedEvent();
        }
    }

    @Override
    public void onConnectionClosed() {
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
        if (outboundQueueHandlerRegistration != null) {
            outboundQueueHandlerRegistration.close();
            outboundQueueHandlerRegistration = null;
        }
    }

    @Override
    public void changeStateToHandshaking() {
        connectionState = CONNECTION_STATE.HANDSHAKING;
    }

    @Override
    public void changeStateToTimeouting() {
        connectionState = CONNECTION_STATE.TIMEOUTING;
    }

    @Override
    public void changeStateToWorking() {
        connectionState = CONNECTION_STATE.WORKING;
    }
}
