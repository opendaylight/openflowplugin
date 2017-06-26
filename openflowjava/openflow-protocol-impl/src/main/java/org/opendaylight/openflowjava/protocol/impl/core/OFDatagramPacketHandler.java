/*
 * Copyright (c) 2014 Pantheon Technologies s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowjava.protocol.impl.core;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.socket.DatagramPacket;
import io.netty.handler.codec.MessageToMessageDecoder;

import java.util.List;

import org.opendaylight.openflowjava.protocol.api.connection.SwitchConnectionHandler;
import org.opendaylight.openflowjava.protocol.api.util.EncodeConstants;
import org.opendaylight.openflowjava.protocol.impl.core.connection.ConnectionAdapterFactory;
import org.opendaylight.openflowjava.protocol.impl.core.connection.ConnectionAdapterFactoryImpl;
import org.opendaylight.openflowjava.protocol.impl.core.connection.ConnectionFacade;
import org.opendaylight.openflowjava.protocol.impl.core.connection.MessageConsumer;
import org.opendaylight.openflowjava.util.ByteBufUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author michal.polkorab
 *
 */
public class OFDatagramPacketHandler extends MessageToMessageDecoder<DatagramPacket> {

    private static final Logger LOG = LoggerFactory.getLogger(OFDatagramPacketHandler.class);

    /** Length of OpenFlow 1.3 header */
    public static final byte LENGTH_OF_HEADER = 8;
    private static final byte LENGTH_INDEX_IN_HEADER = 2;
    private ConnectionAdapterFactory adapterFactory = new ConnectionAdapterFactoryImpl();
    private SwitchConnectionHandler connectionHandler;

    /**
     * Default constructor
     * @param sch the switchConnectionHandler that decides
     * what to do with incomming message / channel
     */
    public OFDatagramPacketHandler(SwitchConnectionHandler sch) {
        this.connectionHandler = sch;
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        LOG.warn("Unexpected exception from downstream.", cause);
        LOG.warn("Closing connection.");
        ctx.close();
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, DatagramPacket msg,
            List<Object> out) throws Exception {
        LOG.debug("OFDatagramPacketFramer");
        MessageConsumer consumer = UdpConnectionMap.getMessageConsumer(msg.sender());
        if (consumer == null) {
            ConnectionFacade connectionFacade =
                    adapterFactory.createConnectionFacade(ctx.channel(), msg.sender(), false);
            connectionHandler.onSwitchConnected(connectionFacade);
            connectionFacade.checkListeners();
            UdpConnectionMap.addConnection(msg.sender(), connectionFacade);
        }
        ByteBuf bb = msg.content();
        int readableBytes = bb.readableBytes();
        if (readableBytes < LENGTH_OF_HEADER) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("skipping bytebuf - too few bytes for header: {} < {}", readableBytes, LENGTH_OF_HEADER);
                LOG.debug("bb: {}", ByteBufUtils.byteBufToHexString(bb));
            }
            return;
        }

        int length = bb.getUnsignedShort(bb.readerIndex() + LENGTH_INDEX_IN_HEADER);
        LOG.debug("length of actual message: {}", length);

        if (readableBytes < length) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("skipping bytebuf - too few bytes for msg: {} < {}", readableBytes, length);
                LOG.debug("bytebuffer: {}", ByteBufUtils.byteBufToHexString(bb));
            }
            return;
        }
        LOG.debug("OF Protocol message received, type:{}", bb.getByte(bb.readerIndex() + 1));


        byte version = bb.readByte();
        if ((version == EncodeConstants.OF13_VERSION_ID) || (version == EncodeConstants.OF10_VERSION_ID)) {
            LOG.debug("detected version: {}", version);
            ByteBuf messageBuffer = bb.slice();
            out.add(new VersionMessageUdpWrapper(version, messageBuffer, msg.sender()));
            messageBuffer.retain();
        } else {
            LOG.warn("detected version: {} - currently not supported", version);
        }
        bb.skipBytes(bb.readableBytes());
    }
}
