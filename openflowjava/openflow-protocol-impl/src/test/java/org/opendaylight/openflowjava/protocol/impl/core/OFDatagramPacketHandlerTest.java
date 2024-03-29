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
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.opendaylight.openflowjava.protocol.api.connection.SwitchConnectionHandler;
import org.opendaylight.openflowjava.protocol.api.util.EncodeConstants;
import org.opendaylight.openflowjava.protocol.impl.core.connection.MessageConsumer;
import org.opendaylight.openflowjava.util.ByteBufUtils;

/**
 * Unit tests for OFDatagramPacketHandler.
 *
 * @author madamjak
 */
@RunWith(MockitoJUnitRunner.class)
public class OFDatagramPacketHandlerTest {

    private static final int CHANNEL_OUTBOUND_QUEUE_SIZE = 1024;
    @Mock ChannelHandlerContext ctxMock;
    @Mock SwitchConnectionHandler switchConnHandler;
    @Mock MessageConsumer consumerMock;

    /**
     * Test {@link OFDatagramPacketHandler}.
     */
    @Test
    public void test() {
        OFDatagramPacketHandler handler = new OFDatagramPacketHandler(switchConnHandler, CHANNEL_OUTBOUND_QUEUE_SIZE);
        ByteBuf messageBuffer = ByteBufUtils.hexStringToByteBuf("04 02 00 08 01 02 03 04");
        InetSocketAddress recipientISA = InetSocketAddress.createUnresolved("localhost", 9876);
        InetSocketAddress senderISA = InetSocketAddress.createUnresolved("192.168.15.24", 21021);
        DatagramPacket datagramPacket = new DatagramPacket(messageBuffer, recipientISA, senderISA);
        UdpConnectionMap.addConnection(datagramPacket.sender(), consumerMock);
        List<Object> outList = new ArrayList<>();

        handler.decode(ctxMock, datagramPacket, outList);

        VersionMessageUdpWrapper versionUdpWrapper = (VersionMessageUdpWrapper) outList.get(0);
        Assert.assertEquals("Wrong - incorrect version has been decoded", EncodeConstants.OF_VERSION_1_3,
            versionUdpWrapper.getVersion());
        Assert.assertEquals("Wrong - sender addresses are different", senderISA, versionUdpWrapper.getAddress());
        messageBuffer.readerIndex(1);
        Assert.assertEquals("Wrong - undecoded part of input ByteBuff is differnt to output", 0,
                messageBuffer.compareTo(versionUdpWrapper.getMessageBuffer()));
    }
}
