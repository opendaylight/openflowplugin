/*
 * Copyright (c) 2014 Pantheon Technologies s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowjava.protocol.impl.core;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import java.net.InetSocketAddress;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.opendaylight.openflowjava.protocol.api.util.EncodeConstants;
import org.opendaylight.openflowjava.protocol.impl.deserialization.DeserializationFactory;

/**
 * Unit tests for OFDatagramPacketDecoder.
 *
 * @author madamjak
 */
@RunWith(MockitoJUnitRunner.class)
public class OFDatagramPacketDecoderTest {
    @Mock DeserializationFactory deserializationFactory;
    @Mock ChannelHandlerContext ctx;
    @Mock ByteBuf messageBufferMock;

    private VersionMessageUdpWrapper msgWrapper;

    @Test
    public void test() {
        OFDatagramPacketDecoder decoder = new OFDatagramPacketDecoder();
        decoder.setDeserializationFactory(deserializationFactory);
        msgWrapper = new VersionMessageUdpWrapper(EncodeConstants.OF_VERSION_1_3, messageBufferMock,
                new InetSocketAddress("10.0.0.1", 6653));

        decoder.channelRead0(ctx, msgWrapper);
        verify(messageBufferMock, times(1)).release();
    }
}
