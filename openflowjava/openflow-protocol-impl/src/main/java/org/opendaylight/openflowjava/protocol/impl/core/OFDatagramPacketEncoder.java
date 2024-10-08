/*
 * Copyright (c) 2014 Pantheon Technologies s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowjava.protocol.impl.core;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.socket.DatagramPacket;
import io.netty.handler.codec.MessageToMessageEncoder;
import io.netty.util.concurrent.Future;
import java.util.List;
import org.opendaylight.openflowjava.protocol.impl.core.connection.UdpMessageListenerWrapper;
import org.opendaylight.openflowjava.protocol.impl.serialization.SerializationFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Encoder for datagram packets.
 *
 * @author michal.polkorab
 */
public class OFDatagramPacketEncoder extends MessageToMessageEncoder<UdpMessageListenerWrapper> {
    private static final Logger LOG = LoggerFactory.getLogger(OFDatagramPacketEncoder.class);

    private SerializationFactory serializationFactory = null;

    @Override
    @SuppressWarnings("checkstyle:IllegalCatch")
    protected void encode(final ChannelHandlerContext ctx,
            final UdpMessageListenerWrapper wrapper, final List<Object> out) throws Exception {
        LOG.trace("Encoding");
        try {
            ByteBuf buffer = PooledByteBufAllocator.DEFAULT.buffer();
            serializationFactory.messageToBuffer(wrapper.getMsg().getVersion(), buffer, wrapper.getMsg());
            out.add(new DatagramPacket(buffer, wrapper.getAddress()));
        } catch (RuntimeException e) {
            LOG.warn("Message serialization failed: {}", e.getMessage());
            Future<Void> newFailedFuture = ctx.newFailedFuture(e);
            wrapper.getListener().operationComplete(newFailedFuture);
            return;
        }
    }

    public void setSerializationFactory(final SerializationFactory serializationFactory) {
        this.serializationFactory = serializationFactory;
    }
}
