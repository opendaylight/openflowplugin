/*
 * Copyright (c) 2014 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.util.nbio.demo;

import org.opendaylight.util.nbio.IOLoop;
import org.opendaylight.util.nbio.MessageBuffer;

import javax.net.ssl.SSLContext;
import java.io.IOException;
import java.nio.channels.ByteChannel;
import java.util.List;

/**
 * IO Loop to drive the transfer of messages.
 *
 * @author Simon Hunt
 */
public class DemoIOLoop extends IOLoop<Msg, MsgBuffer> {

    public DemoIOLoop() throws IOException { }

    @Override
    protected MsgBuffer createBuffer(ByteChannel ch, SSLContext sslContext) {
        // note: for the purposes of this demo we are not implementing TLS/SSL
        return new MsgBuffer(ch, this);
    }

    @Override
    protected void processMessages(MessageBuffer<Msg> b, List<Msg> messages) {
        // Let's increment counters in the messages, then send them back.
        incrementCounters(messages);
        try {
            b.queue(messages);
        } catch (IOException e) {
            System.err.println("Unable to queue outbound messages: " + e);
        }
    }

    private void incrementCounters(List<Msg> messages) {
        for (Msg m: messages)
            m.incrementCounter();
    }
}
