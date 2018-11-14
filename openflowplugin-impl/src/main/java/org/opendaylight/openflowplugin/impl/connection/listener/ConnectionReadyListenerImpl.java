/**
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.connection.listener;

import java.util.concurrent.Future;
import org.opendaylight.openflowjava.protocol.api.connection.ConnectionReadyListener;
import org.opendaylight.openflowplugin.api.openflow.connection.ConnectionContext;
import org.opendaylight.openflowplugin.api.openflow.connection.HandshakeContext;
import org.opendaylight.openflowplugin.impl.connection.HandshakeStepWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Oneshot listener - once connection is ready, initiate handshake (if not already started by device).
 */
public class ConnectionReadyListenerImpl implements ConnectionReadyListener {

    private static final Logger LOG = LoggerFactory.getLogger(ConnectionReadyListenerImpl.class);

    private final ConnectionContext connectionContext;
    private final HandshakeContext handshakeContext;

    /**
     * Constructor.
     *
     * @param connectionContext - connection context
     * @param handshakeContext - handshake context
     */
    public ConnectionReadyListenerImpl(ConnectionContext connectionContext, HandshakeContext handshakeContext) {
        this.connectionContext = connectionContext;
        this.handshakeContext = handshakeContext;
    }

    @Override
    @SuppressWarnings("checkstyle:IllegalCatch")
    public void onConnectionReady() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("device is connected and ready-to-use (pipeline prepared): {}",
                    connectionContext.getConnectionAdapter().getRemoteAddress());
        }

        if (connectionContext.getConnectionState() == null) {
            synchronized (connectionContext) {
                if (connectionContext.getConnectionState() == null) {
                    connectionContext.changeStateToHandshaking();
                    HandshakeStepWrapper handshakeStepWrapper = new HandshakeStepWrapper(
                            null, handshakeContext.getHandshakeManager(), connectionContext.getConnectionAdapter());
                    final Future<?> handshakeResult = handshakeContext.getHandshakePool().submit(handshakeStepWrapper);

                    try {
                        // As we run not in netty thread,
                        // need to remain in sync lock until initial handshake step processed.
                        handshakeResult.get();
                    } catch (Exception e) {
                        LOG.error("failed to process onConnectionReady event on device {}",
                                connectionContext.getConnectionAdapter().getRemoteAddress(),
                                e);
                        connectionContext.closeConnection(false);
                        handshakeContext.close();
                    }
                } else {
                    LOG.debug("already touched by hello message from device {} after second check",
                            connectionContext.getConnectionAdapter().getRemoteAddress());
                }
            }
        } else {
            LOG.debug("already touched by hello message from device {} after first check",
                    connectionContext.getConnectionAdapter().getRemoteAddress());
        }
    }

}
