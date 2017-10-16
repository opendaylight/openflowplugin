/**
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.connection;

import java.util.concurrent.ExecutorService;
import org.opendaylight.openflowplugin.api.openflow.connection.HandshakeContext;
import org.opendaylight.openflowplugin.api.openflow.md.core.HandshakeManager;

public class HandshakeContextImpl implements HandshakeContext {
    private ExecutorService handshakePool;
    private HandshakeManager handshakeManager;

    /**
     * Constructor.
     *
     * @param handshakePool - pool
     * @param handshakeManager - manager
     */
    public HandshakeContextImpl(ExecutorService handshakePool, HandshakeManager handshakeManager) {
        this.handshakePool = handshakePool;
        this.handshakeManager = handshakeManager;
    }

    @Override
    public HandshakeManager getHandshakeManager() {
        return handshakeManager;
    }

    @Override
    public ExecutorService getHandshakePool() {
        return handshakePool;
    }

    @Override
    public void close() {
    }
}
