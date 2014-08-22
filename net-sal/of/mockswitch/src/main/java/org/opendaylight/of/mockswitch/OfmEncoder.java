/*
 * (c) Copyright 2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.of.mockswitch;

import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.handler.codec.oneone.OneToOneEncoder;
import org.opendaylight.of.lib.msg.MessageFactory;
import org.opendaylight.of.lib.msg.OpenflowMessage;

/**
 * Part of a {@code ChannelPipeline} - encodes an {@link OpenflowMessage}
 * into a byte-array-backed {@code ChannelBuffer}.
 *
 * @author Scott Simes
 * @author Simon Hunt
 */
public class OfmEncoder extends OneToOneEncoder {
    @Override
    protected Object encode(ChannelHandlerContext ctx,
                            Channel channel, Object ofm) throws Exception {
        // We have to assume that the input is OpenflowMessage.
        OpenflowMessage msg = (OpenflowMessage) ofm;
        byte[] encoded = MessageFactory.encodeMessage(msg);
        return ChannelBuffers.wrappedBuffer(encoded);
    }
}
