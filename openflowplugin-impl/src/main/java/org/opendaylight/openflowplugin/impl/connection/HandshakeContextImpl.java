/**
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.connection;

import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import org.opendaylight.openflowplugin.api.openflow.connection.HandshakeContext;
import org.opendaylight.openflowplugin.api.openflow.md.core.HandshakeManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 */
public class HandshakeContextImpl implements HandshakeContext {

    private static final Logger LOG = LoggerFactory.getLogger(HandshakeContextImpl.class);

    private ThreadPoolExecutor handshakePool;
    private HandshakeManager handshakeManager;

    /**
     * @param handshakePool
     * @param handshakeManager
     */
    public HandshakeContextImpl(ThreadPoolExecutor handshakePool, HandshakeManager handshakeManager) {
        this.handshakePool = handshakePool;
        this.handshakeManager = handshakeManager;
    }

    @Override
    public HandshakeManager getHandshakeManager() {
        return handshakeManager;
    }

    @Override
    public ThreadPoolExecutor getHandshakePool() {
        return handshakePool;
    }

    @Override
    public void close() throws Exception {
        shutdownPoolPolitely();
    }

    private void shutdownPoolPolitely() {
        LOG.debug("terminating handshake pool");
        handshakePool.shutdown();
        try {
            handshakePool.awaitTermination(1, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            LOG.info("Error while awaiting termination on pool. Will use shutdownNow method.");
        } finally {
            handshakePool.purge();
            if (! handshakePool.isTerminated()) {
                handshakePool.shutdownNow();
            }
            LOG.debug("pool is terminated: {}", handshakePool.isTerminated());
        }
    }
}
