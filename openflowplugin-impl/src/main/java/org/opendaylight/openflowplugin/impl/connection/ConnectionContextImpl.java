/**
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.impl.connection;

import java.math.BigInteger;
import java.net.InetSocketAddress;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import io.netty.util.Timeout;
import io.netty.util.Timer;
import io.netty.util.TimerTask;
import io.netty.util.concurrent.SingleThreadEventExecutor;
import org.opendaylight.openflowjava.protocol.api.connection.ConnectionAdapter;
import org.opendaylight.openflowjava.protocol.api.connection.OutboundQueue;
import org.opendaylight.openflowjava.protocol.api.connection.OutboundQueueHandlerRegistration;
import org.opendaylight.openflowplugin.api.openflow.connection.ConnectionContext;
import org.opendaylight.openflowplugin.api.openflow.connection.HandshakeContext;
import org.opendaylight.openflowplugin.api.openflow.connection.OutboundQueueProvider;
import org.opendaylight.openflowplugin.api.openflow.device.handlers.DeviceDisconnectedHandler;
import org.opendaylight.openflowplugin.impl.LifecycleConductor;
import org.opendaylight.openflowplugin.impl.statistics.ofpspecific.SessionStatistics;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.FeaturesReply;
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
        LOG.debug("Actively closing connection: {}, datapathId:{}.",
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
            LOG.debug("Waiting 1s for unregistering outbound queue.");
            future.get(1, TimeUnit.SECONDS);
            LOG.info("Unregistering outbound queue successful.");
        } catch (InterruptedException e) {
            LOG.warn("Unregistering outbound queue was interrupted for node {}", nodeId);
        } catch (ExecutionException e) {
            LOG.warn("Unregistering outbound queue throws exception for node {}", nodeId, e);
        } catch (TimeoutException e) {
            LOG.warn("Unregistering outbound queue took longer than 1 seconds for node {}", nodeId);
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
                LOG.info("handshake context closing failed: ", e);
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
    public void setHandshakeContext(HandshakeContext handshakeContext) {
        this.handshakeContext = handshakeContext;
    }
}
