/**
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.impl.connection;

import java.net.InetAddress;
import java.util.concurrent.ThreadPoolExecutor;
import org.opendaylight.openflowjava.protocol.api.connection.ConnectionAdapter;
import org.opendaylight.openflowjava.protocol.api.connection.ConnectionReadyListener;
import org.opendaylight.openflowplugin.api.openflow.connection.ConnectionContext;
import org.opendaylight.openflowplugin.api.openflow.connection.ConnectionManager;
import org.opendaylight.openflowplugin.api.openflow.connection.HandshakeContext;
import org.opendaylight.openflowplugin.api.openflow.device.handlers.DeviceConnectedHandler;
import org.opendaylight.openflowplugin.api.openflow.md.core.ConnectionConductor;
import org.opendaylight.openflowplugin.api.openflow.md.core.HandshakeListener;
import org.opendaylight.openflowplugin.api.openflow.md.core.HandshakeManager;
import org.opendaylight.openflowplugin.impl.connection.listener.ConnectionReadyListenerImpl;
import org.opendaylight.openflowplugin.impl.connection.listener.HandshakeListenerImpl;
import org.opendaylight.openflowplugin.impl.connection.listener.OpenflowProtocolListenerInitialImpl;
import org.opendaylight.openflowplugin.impl.connection.listener.SystemNotificationsListenerImpl;
import org.opendaylight.openflowplugin.openflow.md.core.ErrorHandlerSimpleImpl;
import org.opendaylight.openflowplugin.openflow.md.core.HandshakeManagerImpl;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.OpenflowProtocolListener;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.system.rev130927.SystemNotificationsListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 */
public class ConnectionManagerImpl implements ConnectionManager {

    private static final Logger LOG = LoggerFactory.getLogger(ConnectionManagerImpl.class);
    private static final int HELLO_LIMIT = 20;
    private final boolean bitmapNegotiationEnabled = true;
    private DeviceConnectedHandler deviceConnectedHandler;
    private final long echoReplyTimeout;
    private final ThreadPoolExecutor threadPool;

    public ConnectionManagerImpl(long echoReplyTimeout, final ThreadPoolExecutor threadPool) {
        this.echoReplyTimeout = echoReplyTimeout;
        this.threadPool = threadPool;
    }

    @Override
    public void onSwitchConnected(final ConnectionAdapter connectionAdapter) {
        LOG.trace("prepare connection context");
        final ConnectionContext connectionContext = new ConnectionContextImpl(connectionAdapter);

        HandshakeListener handshakeListener = new HandshakeListenerImpl(connectionContext, deviceConnectedHandler);
        final HandshakeManager handshakeManager = createHandshakeManager(connectionAdapter, handshakeListener);

        LOG.trace("prepare handshake context");
        HandshakeContext handshakeContext = new HandshakeContextImpl(threadPool, handshakeManager);
        handshakeListener.setHandshakeContext(handshakeContext);
        connectionContext.setHandshakeContext(handshakeContext);

        LOG.trace("prepare connection listeners");
        final ConnectionReadyListener connectionReadyListener = new ConnectionReadyListenerImpl(
                connectionContext, handshakeContext);
        connectionAdapter.setConnectionReadyListener(connectionReadyListener);

        final OpenflowProtocolListener ofMessageListener =
                new OpenflowProtocolListenerInitialImpl(connectionContext, handshakeContext);
        connectionAdapter.setMessageListener(ofMessageListener);

        final SystemNotificationsListener systemListener = new SystemNotificationsListenerImpl(connectionContext, echoReplyTimeout);
        connectionAdapter.setSystemListener(systemListener);

        LOG.trace("connection ballet finished");
    }

    /**
     * @param connectionAdapter
     * @param handshakeListener
     * @return
     */
    private HandshakeManager createHandshakeManager(final ConnectionAdapter connectionAdapter,
                                                    final HandshakeListener handshakeListener) {
        HandshakeManagerImpl handshakeManager = new HandshakeManagerImpl(connectionAdapter,
                ConnectionConductor.versionOrder.get(0),
                ConnectionConductor.versionOrder);
        handshakeManager.setUseVersionBitmap(isBitmapNegotiationEnabled());
        handshakeManager.setHandshakeListener(handshakeListener);
        handshakeManager.setErrorHandler(new ErrorHandlerSimpleImpl());

        return handshakeManager;
    }

    /**
     * @return parameter dedicated to hello message content
     */
    public boolean isBitmapNegotiationEnabled() {
        return bitmapNegotiationEnabled;
    }

    @Override
    public boolean accept(final InetAddress switchAddress) {
        // TODO add connection accept logic based on address
        return true;
    }

    @Override
    public void setDeviceConnectedHandler(final DeviceConnectedHandler deviceConnectedHandler) {
        this.deviceConnectedHandler = deviceConnectedHandler;
    }
}
