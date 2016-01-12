/**
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.openflow.md.core;

import org.opendaylight.openflowjava.protocol.api.connection.ConnectionAdapter;
import org.opendaylight.openflowplugin.api.openflow.md.core.HandshakeManager;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.HelloMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author mirehak
 *
 */
public class HandshakeStepWrapper implements Runnable {

    private static final Logger LOG = LoggerFactory
            .getLogger(HandshakeStepWrapper.class);

    private HelloMessage helloMessage;
    private HandshakeManager handshakeManager;
    private ConnectionAdapter connectionAdapter;



    /**
     * @param helloMessage initial hello message
     * @param handshakeManager connection handshake manager
     * @param connectionAdapter connection adaptor fro switch
     */
    public HandshakeStepWrapper(HelloMessage helloMessage,
            HandshakeManager handshakeManager, ConnectionAdapter connectionAdapter) {
        this.helloMessage = helloMessage;
        this.handshakeManager = handshakeManager;
        this.connectionAdapter = connectionAdapter;
    }

    @Override
    public void run() {
        if (connectionAdapter.isAlive()) {
            handshakeManager.shake(helloMessage);
        } else {
            LOG.debug("connection is down - skipping handshake step");
        }
    }

}
