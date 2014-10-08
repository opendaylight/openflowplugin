/**
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowjava.cof.codec.action;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.UnpooledByteBufAllocator;

import org.junit.Assert;
import org.junit.Test;
import org.opendaylight.openflowjava.util.ByteBufUtils;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev130731.actions.grouping.Action;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev130731.actions.grouping.ActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.cof.action.rev141008.OfjAugCofAction;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.cof.action.rev141008.OfjAugCofActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.cof.action.rev141008.ofj.cof.action.output.nh.grouping.ActionOutputNh;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.cof.action.rev141008.ofj.cof.action.output.nh.grouping.ActionOutputNhBuilder;

/**
 * 
 */
public class NextHopCodecTest {

    /**
     * Test method for {@link org.opendaylight.openflowjava.cof.codec.action.NextHopCodec#serialize(org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev130731.actions.grouping.Action, io.netty.buffer.ByteBuf)}.
     * 
     * use port+ivp4
     */
    @Test
    public void testSerialize1() {
        ByteBuf buffer = UnpooledByteBufAllocator.DEFAULT.buffer();
        
        byte[] address = new byte[] { 10, 1, 2, 3 };
        long addressExtra = 0xeffffffeL;
        ActionOutputNhBuilder nhBld = new ActionOutputNhBuilder();
        nhBld.setAddressType(2)
            .setAddressExtraType(1)
            .setAddress(address)
            .setAddressExtra(addressExtra);
        
        OfjAugCofActionBuilder cofActionBld = new OfjAugCofActionBuilder().setActionOutputNh(nhBld.build());
        ActionBuilder inputBld = new ActionBuilder().addAugmentation(OfjAugCofAction.class, cofActionBld.build());
        NextHopCodec.getInstance().serialize(inputBld.build(), buffer);
        
        String expected = "ff ff 00 18 00 00 00 0c 00 01 02 01 ef ff ff fe 00 00 00 00 0a 01 02 03";
        Assert.assertEquals(expected , ByteBufUtils.byteBufToHexString(buffer));
    }
    
    /**
     * Test method for {@link org.opendaylight.openflowjava.cof.codec.action.NextHopCodec#serialize(org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev130731.actions.grouping.Action, io.netty.buffer.ByteBuf)}.
     * 
     * use port+ivp6
     */
    @Test
    public void testSerialize2() {
        ByteBuf buffer = UnpooledByteBufAllocator.DEFAULT.buffer();
        
        byte[] address = new byte[] { 1, 2, 3, 4, 5, 6, 7, 8, 9, 0xa, 0xb, 0xc, 0xd, 0xe, 0xf, 0x10 };
        long addressExtra = 0xeffffffeL;
        ActionOutputNhBuilder nhBld = new ActionOutputNhBuilder();
        nhBld.setAddressType(3)
            .setAddressExtraType(1)
            .setAddress(address)
            .setAddressExtra(addressExtra);
        
        OfjAugCofActionBuilder cofActionBld = new OfjAugCofActionBuilder().setActionOutputNh(nhBld.build());
        ActionBuilder inputBld = new ActionBuilder().addAugmentation(OfjAugCofAction.class, cofActionBld.build());
        NextHopCodec.getInstance().serialize(inputBld.build(), buffer);
        
        String expected = "ff ff 00 28 00 00 00 0c 00 01 03 01 ef ff ff fe 00 00 00 00 01 02 03 04 05 06 07 08 09 0a 0b 0c 0d 0e 0f 10 00 00 00 00";
        Assert.assertEquals(expected , ByteBufUtils.byteBufToHexString(buffer));
    }
    
    /**
     * Test method for {@link org.opendaylight.openflowjava.cof.codec.action.NextHopCodec#serialize(org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev130731.actions.grouping.Action, io.netty.buffer.ByteBuf)}.
     * 
     * use port+mac48
     */
    @Test
    public void testSerialize3() {
        ByteBuf buffer = UnpooledByteBufAllocator.DEFAULT.buffer();
        
        byte[] address = new byte[] { 1, 2, 3, 4, 5, 6 };
        long addressExtra = 0xeffffffeL;
        ActionOutputNhBuilder nhBld = new ActionOutputNhBuilder();
        nhBld.setAddressType(4)
            .setAddressExtraType(1)
            .setAddress(address)
            .setAddressExtra(addressExtra);
        
        OfjAugCofActionBuilder cofActionBld = new OfjAugCofActionBuilder().setActionOutputNh(nhBld.build());
        ActionBuilder inputBld = new ActionBuilder().addAugmentation(OfjAugCofAction.class, cofActionBld.build());
        NextHopCodec.getInstance().serialize(inputBld.build(), buffer);
        
        String expected = "ff ff 00 20 00 00 00 0c 00 01 04 01 ef ff ff fe 00 00 00 00 01 02 03 04 05 06 00 00 00 00 00 00";
        Assert.assertEquals(expected , ByteBufUtils.byteBufToHexString(buffer));
    }
    
    /**
     * Test method for {@link org.opendaylight.openflowjava.cof.codec.action.NextHopCodec#serialize(org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev130731.actions.grouping.Action, io.netty.buffer.ByteBuf)}.
     * 
     * use port (p2p)
     */
    @Test
    public void testSerialize4() {
        ByteBuf buffer = UnpooledByteBufAllocator.DEFAULT.buffer();
        
        byte[] address = new byte[] { 10, 1, 2, 3 };
        long addressExtra = 0xeffffffeL;
        ActionOutputNhBuilder nhBld = new ActionOutputNhBuilder();
        nhBld.setAddressType(1)
            .setAddressExtraType(1)
            .setAddress(address)
            .setAddressExtra(addressExtra);
        
        OfjAugCofActionBuilder cofActionBld = new OfjAugCofActionBuilder().setActionOutputNh(nhBld.build());
        ActionBuilder inputBld = new ActionBuilder().addAugmentation(OfjAugCofAction.class, cofActionBld.build());
        NextHopCodec.getInstance().serialize(inputBld.build(), buffer);
        
        String expected = "ff ff 00 10 00 00 00 0c 00 01 01 01 ef ff ff fe";
        Assert.assertEquals(expected , ByteBufUtils.byteBufToHexString(buffer));
    }

    /**
     * Test method for {@link org.opendaylight.openflowjava.cof.codec.action.NextHopCodec#deserialize(io.netty.buffer.ByteBuf)}.
     * expect  port+ipv4
     */
    @Test
    public void testDeserialize1() {
        String input = "ff ff 00 18 00 00 00 0c 00 01 02 01 ef ff ff fe 00 00 00 00 0a 01 02 03";
        ByteBuf buffer = ByteBufUtils.hexStringToByteBuf(input);
        Action action = NextHopCodec.getInstance().deserialize(buffer);
        
        OfjAugCofAction aug = action.getAugmentation(OfjAugCofAction.class);
        Assert.assertNotNull(aug);
        
        ActionOutputNh nextHopAction = aug.getActionOutputNh();
        Assert.assertNotNull(nextHopAction);
        Assert.assertEquals(2, nextHopAction.getAddressType().intValue());
        
        Assert.assertEquals(0xeffffffeL, nextHopAction.getAddressExtra().longValue());
        Assert.assertArrayEquals(new byte[]{10, 1, 2, 3}, nextHopAction.getAddress());
        Assert.assertEquals((input.length() + 1)/3, buffer.readerIndex());
    }

    /**
     * Test method for {@link org.opendaylight.openflowjava.cof.codec.action.NextHopCodec#deserialize(io.netty.buffer.ByteBuf)}.
     * expect  port+ipv6
     */
    @Test
    public void testDeserialize2() {
        String input = "ff ff 00 24 00 00 00 0c 00 01 03 01 ef ff ff fe 00 00 00 00 01 02 03 04 05 06 07 08 09 0a 0b 0c 0d 0e 0f 10";
        ByteBuf buffer = ByteBufUtils.hexStringToByteBuf(input);
        Action action = NextHopCodec.getInstance().deserialize(buffer);
        
        OfjAugCofAction aug = action.getAugmentation(OfjAugCofAction.class);
        Assert.assertNotNull(aug);
        
        ActionOutputNh nextHopAction = aug.getActionOutputNh();
        Assert.assertNotNull(nextHopAction);
        Assert.assertEquals(3, nextHopAction.getAddressType().intValue());
        
        byte[] address = new byte[] { 1, 2, 3, 4, 5, 6, 7, 8, 9, 0xa, 0xb, 0xc, 0xd, 0xe, 0xf, 0x10 };
        Assert.assertEquals(0xeffffffeL, nextHopAction.getAddressExtra().longValue());
        Assert.assertArrayEquals(address, nextHopAction.getAddress());
        Assert.assertEquals((input.length() + 1)/3, buffer.readerIndex());
    }
    
    /**
     * Test method for {@link org.opendaylight.openflowjava.cof.codec.action.NextHopCodec#deserialize(io.netty.buffer.ByteBuf)}.
     * expect  port+mac48
     */
    @Test
    public void testDeserialize3() {
        String input = "ff ff 00 1a 00 00 00 0c 00 01 04 01 ef ff ff fe 00 00 00 00 01 02 03 04 05 06";
        ByteBuf buffer = ByteBufUtils.hexStringToByteBuf(input);
        Action action = NextHopCodec.getInstance().deserialize(buffer);
        
        OfjAugCofAction aug = action.getAugmentation(OfjAugCofAction.class);
        Assert.assertNotNull(aug);
        
        ActionOutputNh nextHopAction = aug.getActionOutputNh();
        Assert.assertNotNull(nextHopAction);
        Assert.assertEquals(4, nextHopAction.getAddressType().intValue());
        
        byte[] address = new byte[] { 1, 2, 3, 4, 5, 6 };
        Assert.assertEquals(0xeffffffeL, nextHopAction.getAddressExtra().longValue());
        Assert.assertArrayEquals(address, nextHopAction.getAddress());
        Assert.assertEquals((input.length() + 1)/3, buffer.readerIndex());
    }
    
    /**
     * Test method for {@link org.opendaylight.openflowjava.cof.codec.action.NextHopCodec#deserialize(io.netty.buffer.ByteBuf)}.
     * expect  port+P2P
     */
    @Test
    public void testDeserialize4() {
        String input = "ff ff 00 10 00 00 00 0c 00 01 01 01 ef ff ff fe";
        ByteBuf buffer = ByteBufUtils.hexStringToByteBuf(input);
        Action action = NextHopCodec.getInstance().deserialize(buffer);
        
        OfjAugCofAction aug = action.getAugmentation(OfjAugCofAction.class);
        Assert.assertNotNull(aug);
        
        ActionOutputNh nextHopAction = aug.getActionOutputNh();
        Assert.assertNotNull(nextHopAction);
        Assert.assertEquals(1, nextHopAction.getAddressType().intValue());
        
        Assert.assertEquals(0xeffffffeL, nextHopAction.getAddressExtra().longValue());
        Assert.assertNull(nextHopAction.getAddress());
        Assert.assertEquals((input.length() + 1)/3, buffer.readerIndex());
    }
}
