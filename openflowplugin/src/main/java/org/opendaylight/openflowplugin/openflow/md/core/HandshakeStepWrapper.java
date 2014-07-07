/**
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.openflow.md.core;

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

    private final HelloMessage helloMessage;
    private final HandshakeManager handshakeManager;


    /**
     * @param helloMessage
     * @param handshakeManager
     */
    public HandshakeStepWrapper(HelloMessage helloMessage,
            HandshakeManager handshakeManager ) {
        this.helloMessage = helloMessage;
        this.handshakeManager = handshakeManager;
    }

    @Override
	public void run() {
		if (handshakeManager.getConnectionAdapter().isAlive()) {
			if (helloMessage == null) {
				handshakeManager.startHandshake();

			} else {
				handshakeManager.setReceivedHello(helloMessage);
				handshakeManager.continueHandshake();
			}
		} else {
			LOG.debug(
					"Connection has broken ... handshake aborting for remote={}",
					handshakeManager.getConnectionAdapter().remoteAddress());
		}
	}

}
