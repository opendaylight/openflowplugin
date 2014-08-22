/*
 * (c) Copyright 2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.of.mockswitch;

import org.jboss.netty.channel.ChannelHandlerContext;
import org.opendaylight.of.lib.ProtocolVersion;

/**
 * Memo passed between OfConnection fixture and
 *  MockOpenflowSwitchTest.TestMessageHandler fixture.
 *
 * @author Simon Hunt
 */
class ConnMemo {
    final MockOpenflowSwitchTest.TestMessageHandler handler;
    final ChannelHandlerContext ctx;

    ConnMemo(MockOpenflowSwitchTest.TestMessageHandler handler,
             ChannelHandlerContext ctx) {
        this.handler = handler;
        this.ctx = ctx;
    }

    /** Invoked once the negotiated protocol version has been determined.
     *
     * @param pv the negotiated protocol version
     */
    void versionNegotiatedAs(ProtocolVersion pv) {
        handler.versionNegotiatedAs(pv, this);
    }

    /** Invoked once the features-reply has been received and processed. */
    void handshakeComplete() {
        handler.handshakeComplete();
    }
}
