/*
 * (c) Copyright 2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.of.controller.impl;

import org.opendaylight.util.nbio.IOLoop;
import org.opendaylight.util.nbio.MessageBuffer;
import org.opendaylight.of.lib.msg.OpenflowMessage;
import org.opendaylight.util.Log;
import org.opendaylight.util.ResourceUtils;
import org.slf4j.LoggerFactory;

import javax.net.ssl.SSLContext;
import java.io.IOException;
import java.nio.channels.ByteChannel;
import java.util.List;
import java.util.ResourceBundle;

/**
 * I/O loop for processing OpenFlow switch connections, initial hand-shake
 * comprising of protocol version negotiation, device features interrogation
 * and subsequent message passing.
 *
 * @author Thomas Vachuska
 * @author Simon Hunt
 */
public class OpenflowIOLoop extends IOLoop<OpenflowMessage, OpenflowMessageBuffer> {

    private static final int SELECT_TIMEOUT_MS = 50;

    private static final ResourceBundle RES = ResourceUtils.getBundledResource(
            OpenflowIOLoop.class, "openflowIOLoop");

    private static final String E_UNEXP_RUNTIME = RES
            .getString("e_unexp_runtime");

    private final OpenflowController controller;

    /**
     * Creates an I/O loop for processing OpenFlow messages.
     *
     * @param controller parent controller
     * @throws IOException if unable to open selector
     */
    OpenflowIOLoop(OpenflowController controller) throws IOException {
        super(SELECT_TIMEOUT_MS);
        this.controller = controller;
        this.log = LoggerFactory.getLogger(OpenflowController.class);
    }

    @Override
    protected OpenflowMessageBuffer createBuffer(ByteChannel ch,
                                                 SSLContext sslContext) {
        return new OpenflowMessageBuffer(ch, this, controller, sslContext);
    }

    @Override
    protected void processMessages(MessageBuffer<OpenflowMessage> buffer,
                                   List<OpenflowMessage> messages) {
        OpenflowMessageBuffer mb = (OpenflowMessageBuffer) buffer;
        for (OpenflowMessage message : messages)
            mb.processMessage(message);
    }

    @Override
    protected void handleMessageProcessingException(OpenflowMessageBuffer b,
                                                    RuntimeException e) {
        if (IllegalStateException.class.isInstance(e)
                && (e.getMessage().startsWith(DpInfo.E_DUP_DPID) || 
                        e.getMessage().startsWith(DpInfo.E_NOT_MAIN_CONN))) {
            b.discard();
            return;
        }
        // DO NOT throw an exception from here to avoid breaking the IOLoop!
        log.error(E_UNEXP_RUNTIME, Log.stackTraceSnippet(e));
    }
}