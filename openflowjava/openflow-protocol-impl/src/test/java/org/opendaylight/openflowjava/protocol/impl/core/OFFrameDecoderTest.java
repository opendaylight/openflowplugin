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
import org.mockito.MockitoAnnotations;
import org.mockito.runners.MockitoJUnitRunner;
import org.opendaylight.openflowjava.protocol.impl.core.connection.ConnectionFacade;
import org.opendaylight.openflowjava.util.ByteBufUtils;

/**
 * Testing class of {@link OFFrameDecoder}
 *
 * @author michal.polkorab
 */
@RunWith(MockitoJUnitRunner.class)
public class OFFrameDecoderTest {

    @Mock
    ChannelHandlerContext channelHandlerContext;

    @Mock
    ConnectionFacade connectionFacade;
    private OFFrameDecoder decoder;
    private List<Object> list = new ArrayList<>();

    /**
     * Sets up tests
     */
    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        decoder = new OFFrameDecoder(connectionFacade, false);
        list.clear();

    }

    /**
     * Test of decoding
     * {@link OFFrameDecoder#decode(io.netty.channel.ChannelHandlerContext, io.netty.buffer.ByteBuf, java.util.List)}
     */
    @Test
    public void testDecode8BMessage() {
        try {
            decoder.decode(channelHandlerContext,
                    ByteBufUtils.hexStringToByteBuf("04 00 00 08 00 00 00 01"),
                    list);
        } catch (Exception e) {
            Assert.fail();
        }

        assertEquals(8, ((ByteBuf) list.get(0)).readableBytes());
    }

    /**
     * Test of decoding
     * {@link OFFrameDecoder#decode(io.netty.channel.ChannelHandlerContext, io.netty.buffer.ByteBuf, java.util.List)}
     */
    @Test
    public void testDecode16BMessage() {
        ByteBuf byteBuffer = ByteBufUtils
                .hexStringToByteBuf("04 00 00 10 00 00 00 00 00 00 00 00 00 00 00 42");
        try {
            decoder.decode(channelHandlerContext, byteBuffer, list);
        } catch (Exception e) {
            Assert.fail();
        }

        assertEquals(16, ((ByteBuf) list.get(0)).readableBytes());
        assertEquals(0, byteBuffer.readableBytes());
    }

    /**
     * Test of decoding
     * {@link OFFrameDecoder#decode(io.netty.channel.ChannelHandlerContext, io.netty.buffer.ByteBuf, java.util.List)}
     */
    @Test
    public void testDecode5BIncompleteMessage() {
        ByteBuf byteBuffer = ByteBufUtils.hexStringToByteBuf("04 00 00 08 00");
        try {
            decoder.decode(channelHandlerContext, byteBuffer, list);
        } catch (Exception e) {
            Assert.fail();
        }

        Assert.assertEquals("List is not empty", 0, list.size());
        assertEquals(5, byteBuffer.readableBytes());
    }

    /**
     * Test of decoding
     * {@link OFFrameDecoder#decode(io.netty.channel.ChannelHandlerContext, io.netty.buffer.ByteBuf, java.util.List)}
     */
    @Test
    public void testDecode16BIncompleteMessage() {
        ByteBuf byteBuffer = ByteBufUtils
                .hexStringToByteBuf("04 00 00 11 00 00 00 00 00 00 00 00 00 00 00 42");
        try {
            decoder.decode(channelHandlerContext, byteBuffer, list);
        } catch (Exception e) {
            Assert.fail();
        }

        Assert.assertEquals("List is not empty", 0, list.size());
        assertEquals(16, byteBuffer.readableBytes());
    }

    /**
     * Test of decoding
     * {@link OFFrameDecoder#decode(io.netty.channel.ChannelHandlerContext, io.netty.buffer.ByteBuf, java.util.List)}
     */
    @Test
    public void testDecodeCompleteAndPartialMessage() {
        ByteBuf byteBuffer = ByteBufUtils
                .hexStringToByteBuf("04 00 00 08 00 00 00 01 04 00 00 08 00");
        try {
            decoder.decode(channelHandlerContext, byteBuffer, list);
        } catch (Exception e) {
            Assert.fail();
        }

        Assert.assertEquals(8, ((ByteBuf) list.get(0)).readableBytes());
        Assert.assertEquals(1, list.size());
        assertEquals(5, byteBuffer.readableBytes());

    }

    @Test
    public void testExceptionCaught() throws Exception {
        decoder.exceptionCaught(channelHandlerContext, new Throwable());
    }

    /**
     * Test of decoding
     * {@link OFFrameDecoder#decode(io.netty.channel.ChannelHandlerContext, io.netty.buffer.ByteBuf, java.util.List)}
     */
    @Test
    public void testDecode8BMessageWithTls() {
        decoder = new OFFrameDecoder(connectionFacade, true);
        try {
            decoder.decode(channelHandlerContext,
                    ByteBufUtils.hexStringToByteBuf("04 00 00 08 00 00 00 01"),
                    list);
        } catch (Exception e) {
            Assert.fail();
        }

        assertEquals(8, ((ByteBuf) list.get(0)).readableBytes());
    }
}
