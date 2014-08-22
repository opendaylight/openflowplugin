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

import java.nio.ByteBuffer;
import java.nio.channels.ByteChannel;

/**
 * Message buffer for {@link Msg} messages.
 *
 * @author Simon Hunt
 */
public class MsgBuffer extends MessageBuffer<Msg> {
    private static final double GROWTH_FACTOR = 2.0;
    private final int msgLength = Msg.LENGTH;

    private int growthCount = 0;

    /**
     * Creates a new buffer for transferring messages.
     *
     * @param ch backing channel
     * @param loop driver loop
     */
    public MsgBuffer(ByteChannel ch, IOLoop<Msg, ?> loop) {
        super(ch, loop, null);
    }


    @Override
    protected Msg get(ByteBuffer rb) {
        // if the (read) byte buffer doesn't have sufficient bytes for
        // a complete message, bail...
        if (rb.remaining() < msgLength)
            return null;

        Msg msg = new Msg();
        rb.get(msg.data());
        return msg;
    }

    @Override
    protected void put(Msg message, ByteBuffer wb) {
        wb.put(message.data());
    }

    @Override
    protected double growthFactor() {
        growthCount++;
        return GROWTH_FACTOR;
    }

}
