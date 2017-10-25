/*
 * Copyright (c) 2013 Pantheon Technologies s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowjava.protocol.impl.core;

import static org.junit.Assert.assertEquals;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import java.util.ArrayList;
import java.util.List;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.opendaylight.openflowjava.util.ByteBufUtils;

/**
 * Test for {@link org.opendaylight.openflowjava.protocol.impl.core.OFVersionDetector}.
 */
@RunWith(MockitoJUnitRunner.class)
public class OFVersionDetectorTest {

    @Mock
    ChannelHandlerContext channelHandlerContext;

    private OFVersionDetector detector;
    private List<Object> list = new ArrayList<>();

    @Before
    public void setUp() {
        list.clear();
        detector = new OFVersionDetector();
    }

    @Test
    public void testDecode13ProtocolMessage() {
        detector.decode(channelHandlerContext, ByteBufUtils.hexStringToByteBuf("04 00 00 08 00 00 00 01"), list);
        Assert.assertEquals(7, ((VersionMessageWrapper) list.get(0)).getMessageBuffer().readableBytes());
    }

    @Test
    public void testDecode10ProtocolMessage() {
        detector.decode(channelHandlerContext, ByteBufUtils.hexStringToByteBuf("01 00 00 08 00 00 00 01"), list);
        Assert.assertEquals(7, ((VersionMessageWrapper) list.get(0)).getMessageBuffer().readableBytes());
    }

    @Test
    public void testDecodeEmptyProtocolMessage() {
        ByteBuf byteBuffer = ByteBufUtils.hexStringToByteBuf("01 00 00 08 00 00 00 01").skipBytes(8);
        detector.decode(channelHandlerContext, byteBuffer, list);
        assertEquals(0, byteBuffer.refCnt());
    }

    @Test
    public void testDecodeNotSupportedVersionProtocolMessage() {
        detector.decode(channelHandlerContext, ByteBufUtils.hexStringToByteBuf("02 01 00 08 00 00 00 01"), list);
        Assert.assertEquals("List is not empty", 0, list.size());
    }

    @Test
    public void testDecodeHelloProtocolMessage() {
        detector.decode(channelHandlerContext, ByteBufUtils.hexStringToByteBuf("05 00 00 08 00 00 00 01"), list);
        Assert.assertEquals(7, ((VersionMessageWrapper) list.get(0)).getMessageBuffer().readableBytes());
    }
}
