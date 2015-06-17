/**
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowjava.cof.codec;


import io.netty.buffer.ByteBuf;

import org.junit.Assert;
import org.junit.Test;
import org.opendaylight.openflowjava.util.ByteBufUtils;

/**
 * test of {@link CodecUtil}
 */
public class CodecUtilTest {

    /**
     * Test method for {@link org.opendaylight.openflowjava.cof.codec.CodecUtil#computePadding8(int)}.
     */
    @Test
    public void testComputePadding8() {
        Assert.assertEquals(0, CodecUtil.computePadding8(0));
        Assert.assertEquals(7, CodecUtil.computePadding8(1));
        Assert.assertEquals(6, CodecUtil.computePadding8(2));
        Assert.assertEquals(5, CodecUtil.computePadding8(3));
        Assert.assertEquals(4, CodecUtil.computePadding8(4));
        Assert.assertEquals(3, CodecUtil.computePadding8(5));
        Assert.assertEquals(2, CodecUtil.computePadding8(6));
        Assert.assertEquals(1, CodecUtil.computePadding8(7));
        Assert.assertEquals(0, CodecUtil.computePadding8(8));
        Assert.assertEquals(7, CodecUtil.computePadding8(9));
    }
    
    /**
     * Test method for {@link org.opendaylight.openflowjava.cof.codec.CodecUtil#computePadding8(int)}.
     */
    @Test
    public void testGetCofActionLength() {
        ByteBuf buffer = ByteBufUtils.hexStringToByteBuf("2a a2 01 02 03");
        Assert.assertEquals(0x0102, CodecUtil.getCofActionLength(buffer, 0));
    }
    
    /**
     * Test method for {@link org.opendaylight.openflowjava.cof.codec.CodecUtil#finishElementReading(ByteBuf, int, int)}.
     */
    @Test
    public void testFinishElementReading() {
        ByteBuf buffer = ByteBufUtils.hexStringToByteBuf("2a a2 01 02 03");
        CodecUtil.finishElementReading(buffer, 0, 2);
        Assert.assertEquals(2, buffer.readerIndex());
        
        CodecUtil.finishElementReading(buffer, 0, 4);
        Assert.assertEquals(4, buffer.readerIndex());
        
        CodecUtil.finishElementReading(buffer, 0, 5);
        Assert.assertEquals(5, buffer.readerIndex());
        
        try {
            CodecUtil.finishElementReading(buffer, 0, 4);
            Assert.fail("out of range exceptino expected");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }
    
    /**
     * Test method for {@link org.opendaylight.openflowjava.cof.codec.CodecUtil#stripTrailingZeroes(byte[])}.
     */
    @Test
    public void testStripTrailingZeroes() {
        byte[][] sources = new byte[][] {
                { 1, 2, 3, 4 },
                { 1, 2, 3, 4, 0 },
                { 1, 2, 3, 4, 0, 0, 0 },
                { 1, 2, 3, 4, 0, 0, 0, 5 },
                { 1, 2, 3, 4, 0, 0, 0, 5, 0 },
                {}
        };
        byte[][] results = new byte[][] {
                { 1, 2, 3, 4 },
                { 1, 2, 3, 4 },
                { 1, 2, 3, 4 },
                { 1, 2, 3, 4, 0, 0, 0, 5 },
                { 1, 2, 3, 4, 0, 0, 0, 5 },
                {}
        };
        
        for (int i = 0; i < sources.length; i++) {
            Assert.assertArrayEquals(results[i], CodecUtil.stripTrailingZeroes(sources[i]));
        }
    }
    
}
