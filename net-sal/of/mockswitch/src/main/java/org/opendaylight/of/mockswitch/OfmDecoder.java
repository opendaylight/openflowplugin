/*
 * (c) Copyright 2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.of.mockswitch;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.handler.codec.frame.FrameDecoder;
import org.opendaylight.of.lib.OfPacketReader;
import org.opendaylight.of.lib.msg.MessageFactory;
import org.opendaylight.of.lib.msg.OpenflowMessage;

/**
 * Part of a {@code ChannelPipeline} - decodes a byte stream as
 * an {@link OpenflowMessage}.
 *
 * @author Scott Simes
 * @author Simon Hunt
 */
public class OfmDecoder extends FrameDecoder {

    private static final int LENGTH_MASK = 0xffff;

    @Override
    protected Object decode(ChannelHandlerContext ctx, Channel channel,
                            ChannelBuffer buffer) throws Exception {
        // need to ensure we have a complete message in the buffer first...
        //  +-------+-------+-------+-------+---
        //  | P.Ver | Type  |     Length    | ...
        //  +-------+-------+-------+-------+---
        if (buffer.readableBytes() < 4) {
            /* The length field was not received yet - return null.
             * This method will be invoked again when more packets are
             * received and appended to the buffer.
             */
            return null;
        }

        // enough bytes in the buffer to read the length field...
        buffer.markReaderIndex();
        int length = buffer.readInt() & LENGTH_MASK;
        buffer.resetReaderIndex();

        if (buffer.readableBytes() < length) {
            /* Still don't have a complete message - return null.
             * This method will be invoked again when more packets are
             * received and appended to the buffer.
             */
            return null;
        }

        // enough bytes in the buffer to parse a complete message...
        ChannelBuffer frame = buffer.readBytes(length);
        OfPacketReader pkt = new OfPacketReader(frame.array());

        // convert the frame into a OpenFlow message
        return MessageFactory.parseMessage(pkt);
    }
}
