/*
 * Copyright (c) 2014 Pantheon Technologies s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */


package org.opendaylight.openflowjava.protocol.impl.clients;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.socket.DatagramPacket;
import io.netty.handler.codec.MessageToMessageDecoder;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class for decoding incoming udp messages into message frames.
 *
 * @author michal.polkorab
 */
public class UdpSimpleClientFramer extends MessageToMessageDecoder<DatagramPacket> {

    /** Length of OpenFlow 1.3 header */
    public static final byte LENGTH_OF_HEADER = 8;
    private static final byte LENGTH_INDEX_IN_HEADER = 2;
    private static final Logger LOG = LoggerFactory.getLogger(UdpSimpleClientFramer.class);

    /**
     * Constructor of class.
     */
    public UdpSimpleClientFramer() {
        LOG.trace("Creating OFFrameDecoder");
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        LOG.warn("Unexpected exception from downstream.", cause);
        ctx.close();
    }

    @Override
    protected void decode(ChannelHandlerContext chc, DatagramPacket msg, List<Object> list) throws Exception {
        ByteBuf bb = msg.content();
        if (bb.readableBytes() < LENGTH_OF_HEADER) {
            LOG.debug("skipping bb - too few data for header: {}", bb.readableBytes());
            return;
        }

        int length = bb.getUnsignedShort(bb.readerIndex() + LENGTH_INDEX_IN_HEADER);
        if (bb.readableBytes() < length) {
            LOG.debug("skipping bb - too few data for msg: {} < {}", bb.readableBytes(), length);
            return;
        }
        LOG.debug("OF Protocol message received, type:{}", bb.getByte(bb.readerIndex() + 1));

        ByteBuf messageBuffer = bb.slice(bb.readerIndex(), length);
        list.add(messageBuffer);
        messageBuffer.retain();
        bb.skipBytes(length);
    }
}
