/*
 * Copyright (c) 2014 Pantheon Technologies s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowjava.protocol.impl.core;

import static org.mockito.Mockito.when;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.socket.DatagramPacket;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.opendaylight.openflowjava.protocol.api.connection.SwitchConnectionHandler;
import org.opendaylight.openflowjava.protocol.api.util.EncodeConstants;
import org.opendaylight.openflowjava.protocol.impl.core.connection.MessageConsumer;
import org.opendaylight.openflowjava.util.ByteBufUtils;

/**
 * @author madamjak
 *
 */
public class OFDatagramPacketHandlerTest {
    @Mock ChannelHandlerContext ctxMock;
    @Mock SwitchConnectionHandler switchConnHandler;
    @Mock MessageConsumer consumerMock;
    @Mock Channel channelMock;

    @Before
    public void startUp(){
        MockitoAnnotations.initMocks(this);
        when(ctxMock.channel()).thenReturn(channelMock);
    }

    /**
     * Test {@link OFDatagramPacketHandler}
     */
    @Test
    public void test(){
        OFDatagramPacketHandler handler = new OFDatagramPacketHandler(switchConnHandler);
        byte version = EncodeConstants.OF13_VERSION_ID;
        ByteBuf messageBuffer = ByteBufUtils.hexStringToByteBuf("04 02 00 08 01 02 03 04");
        InetSocketAddress recipientISA = InetSocketAddress.createUnresolved("localhost", 9876);
        InetSocketAddress senderISA = InetSocketAddress.createUnresolved("192.168.15.24", 21021);
        DatagramPacket datagramPacket = new DatagramPacket(messageBuffer, recipientISA, senderISA);
        UdpConnectionMap.addConnection(datagramPacket.sender(), consumerMock);
        List<Object> outList = new ArrayList<>();
        try {
            handler.decode(ctxMock, datagramPacket, outList);
        } catch (Exception e) {
            Assert.fail("Wrong - Unexcepted exception occurred");
        }
        VersionMessageUdpWrapper versionUdpWrapper = (VersionMessageUdpWrapper) outList.get(0);
        Assert.assertEquals("Wrong - incorrect version has been decoded",version, versionUdpWrapper.getVersion());
        Assert.assertEquals("Wrong - sender addresses are different", senderISA, versionUdpWrapper.getAddress());
        messageBuffer.readerIndex(1);
        Assert.assertEquals("Wrong - undecoded part of input ByteBuff is differnt to output",0, messageBuffer.compareTo(versionUdpWrapper.getMessageBuffer()));
    }
}
