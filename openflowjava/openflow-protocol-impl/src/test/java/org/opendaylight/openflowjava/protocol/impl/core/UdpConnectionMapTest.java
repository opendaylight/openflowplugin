/*
 * Copyright (c) 2014 Pantheon Technologies s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowjava.protocol.impl.core;

import io.netty.buffer.ByteBuf;
import io.netty.channel.socket.DatagramPacket;

import java.net.InetSocketAddress;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.opendaylight.openflowjava.protocol.impl.core.connection.MessageConsumer;

/**
 * @author madamjak
 *
 */
public class UdpConnectionMapTest {

    @Mock MessageConsumer consumerMock;
    @Mock  ByteBuf messageBuffer;

    @Before
    public void startUp(){
        MockitoAnnotations.initMocks(this);
    }

    /**
     * Test {@link UdpConnectionMap} - sender address is not null
     */
    @Test
    public void testWithSenderAddress(){
        InetSocketAddress recipientISA = InetSocketAddress.createUnresolved("localhost", 9876);
        InetSocketAddress senderISA = InetSocketAddress.createUnresolved("192.168.15.2", 21021);
        DatagramPacket datagramPacket = new DatagramPacket(messageBuffer, recipientISA, senderISA);
        UdpConnectionMap.addConnection(datagramPacket.sender(), consumerMock);
        Assert.assertEquals("Wrong - different object has been returned",
                consumerMock, UdpConnectionMap.getMessageConsumer(datagramPacket.sender()));
        UdpConnectionMap.removeConnection(datagramPacket.sender());
        Assert.assertNull("Wrong - object has been returned after remove key-value pair",
                UdpConnectionMap.getMessageConsumer(datagramPacket.sender()));
    }

    /**
     * Test {@link UdpConnectionMap} - sender address is null to add connection
     */
    @Test(expected = IllegalArgumentException.class)
    public void testWithoutSenderAddressOnAdd(){
        UdpConnectionMap.addConnection(null, consumerMock);
    }

    /**
     * Test {@link UdpConnectionMap} - sender address is not null to get message consumer
     */
    @Test(expected = IllegalArgumentException.class)
    public void testWithoutSenderAddressOnGet(){
        UdpConnectionMap.getMessageConsumer(null);
    }

    /**
     * Test {@link UdpConnectionMap} - sender address is not null to remove connection
     */
    @Test(expected = IllegalArgumentException.class)
    public void testWithoutSenderAddressOnRemove(){
        UdpConnectionMap.removeConnection(null);
    }
}
