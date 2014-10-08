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
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.Ipv4Address;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.Ipv6Address;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev100924.MacAddress;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev130731.actions.grouping.Action;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev130731.actions.grouping.ActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.cof.action.rev141008.CofAtOutputNhAddressExtraType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.cof.action.rev141008.CofAtOutputNhAddressType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.cof.action.rev141008.NhPortNumber;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.cof.action.rev141008.OfjAugCofAction;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.cof.action.rev141008.OfjAugCofActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.cof.action.rev141008.ofj.cof.action.output.nh.grouping.ActionOutputNh;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.cof.action.rev141008.ofj.cof.action.output.nh.grouping.ActionOutputNh.Address;
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
        
        Address address = new Address(new Ipv4Address("10.1.2.3"));
        NhPortNumber addressExtra = new NhPortNumber(0xeffffffeL);
        ActionOutputNhBuilder nhBld = new ActionOutputNhBuilder();
        nhBld.setAddressType(CofAtOutputNhAddressType.IPV4)
            .setAddressExtraType(CofAtOutputNhAddressExtraType.PORT)
            .setAddress(address)
            .setAddressExtra(addressExtra);
        
        OfjAugCofActionBuilder cofActionBld = new OfjAugCofActionBuilder().setActionOutputNh(nhBld.build());
        ActionBuilder inputBld = new ActionBuilder().addAugmentation(OfjAugCofAction.class, cofActionBld.build());
        NextHopCodec.getInstance().serialize(inputBld.build(), buffer);
        
        String expected = "ff ff 00 14 00 00 00 0c 00 2b 02 01 ef ff ff fe 0a 01 02 03";
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
        
        Address address = new Address(new Ipv6Address("0102:0304:0506:0708:090a:0b0c:0d0e:0f10"));
        NhPortNumber addressExtra = new NhPortNumber(0xeffffffeL);
        ActionOutputNhBuilder nhBld = new ActionOutputNhBuilder();
        nhBld.setAddressType(CofAtOutputNhAddressType.IPV6)
            .setAddressExtraType(CofAtOutputNhAddressExtraType.PORT)
            .setAddress(address)
            .setAddressExtra(addressExtra);
        
        OfjAugCofActionBuilder cofActionBld = new OfjAugCofActionBuilder().setActionOutputNh(nhBld.build());
        ActionBuilder inputBld = new ActionBuilder().addAugmentation(OfjAugCofAction.class, cofActionBld.build());
        NextHopCodec.getInstance().serialize(inputBld.build(), buffer);
        
        String expected = "ff ff 00 20 00 00 00 0c 00 2b 03 01 ef ff ff fe 01 02 03 04 05 06 07 08 09 0a 0b 0c 0d 0e 0f 10";
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
        
        Address address = new Address(new MacAddress("01:02:03:04:05:06"));
        NhPortNumber addressExtra = new NhPortNumber(0xeffffffeL);
        ActionOutputNhBuilder nhBld = new ActionOutputNhBuilder();
        nhBld.setAddressType(CofAtOutputNhAddressType.MAC48)
            .setAddressExtraType(CofAtOutputNhAddressExtraType.PORT)
            .setAddress(address)
            .setAddressExtra(addressExtra);
        
        OfjAugCofActionBuilder cofActionBld = new OfjAugCofActionBuilder().setActionOutputNh(nhBld.build());
        ActionBuilder inputBld = new ActionBuilder().addAugmentation(OfjAugCofAction.class, cofActionBld.build());
        NextHopCodec.getInstance().serialize(inputBld.build(), buffer);
        
        String expected = "ff ff 00 16 00 00 00 0c 00 2b 04 01 ef ff ff fe 01 02 03 04 05 06";
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
        
        Address address = new Address(new Ipv4Address("10.1.2.3"));
        NhPortNumber addressExtra = new NhPortNumber(0xeffffffeL);
        ActionOutputNhBuilder nhBld = new ActionOutputNhBuilder();
        nhBld.setAddressType(CofAtOutputNhAddressType.P2P)
            .setAddressExtraType(CofAtOutputNhAddressExtraType.PORT)
            .setAddress(address)
            .setAddressExtra(addressExtra);
        
        OfjAugCofActionBuilder cofActionBld = new OfjAugCofActionBuilder().setActionOutputNh(nhBld.build());
        ActionBuilder inputBld = new ActionBuilder().addAugmentation(OfjAugCofAction.class, cofActionBld.build());
        NextHopCodec.getInstance().serialize(inputBld.build(), buffer);
        
        String expected = "ff ff 00 10 00 00 00 0c 00 2b 01 01 ef ff ff fe";
        Assert.assertEquals(expected , ByteBufUtils.byteBufToHexString(buffer));
    }

    /**
     * Test method for {@link org.opendaylight.openflowjava.cof.codec.action.NextHopCodec#deserialize(io.netty.buffer.ByteBuf)}.
     * expect  port+ipv4
     */
    @Test
    public void testDeserialize1() {
        String input = "ff ff 00 14 00 00 00 0c 00 2b 02 01 ef ff ff fe 0a 01 02 03";
        ByteBuf buffer = ByteBufUtils.hexStringToByteBuf(input);
        Action action = NextHopCodec.getInstance().deserialize(buffer);
        
        OfjAugCofAction aug = action.getAugmentation(OfjAugCofAction.class);
        Assert.assertNotNull(aug);
        
        ActionOutputNh nextHopAction = aug.getActionOutputNh();
        Assert.assertNotNull(nextHopAction);
        Assert.assertEquals(CofAtOutputNhAddressType.IPV4, nextHopAction.getAddressType());
        
        Assert.assertEquals(0xeffffffeL, nextHopAction.getAddressExtra().getValue().longValue());
        Assert.assertEquals("10.1.2.3", nextHopAction.getAddress().getIpv4Address().getValue());
    }

    /**
     * Test method for {@link org.opendaylight.openflowjava.cof.codec.action.NextHopCodec#deserialize(io.netty.buffer.ByteBuf)}.
     * expect  port+ipv6
     */
    @Test
    public void testDeserialize2() {
        String input = "ff ff 00 14 00 00 00 0c 00 2b 03 01 ef ff ff fe 01 02 03 04 05 06 07 08 09 0a 0b 0c 0d 0e 0f 10";
        ByteBuf buffer = ByteBufUtils.hexStringToByteBuf(input);
        Action action = NextHopCodec.getInstance().deserialize(buffer);
        
        OfjAugCofAction aug = action.getAugmentation(OfjAugCofAction.class);
        Assert.assertNotNull(aug);
        
        ActionOutputNh nextHopAction = aug.getActionOutputNh();
        Assert.assertNotNull(nextHopAction);
        Assert.assertEquals(CofAtOutputNhAddressType.IPV6, nextHopAction.getAddressType());
        
        Assert.assertEquals(0xeffffffeL, nextHopAction.getAddressExtra().getValue().longValue());
        Assert.assertEquals("0102:0304:0506:0708:090A:0B0C:0D0E:0F10", nextHopAction.getAddress().getIpv6Address().getValue());
    }
    
    /**
     * Test method for {@link org.opendaylight.openflowjava.cof.codec.action.NextHopCodec#deserialize(io.netty.buffer.ByteBuf)}.
     * expect  port+mac48
     */
    @Test
    public void testDeserialize3() {
        String input = "ff ff 00 0c 00 00 00 0c 00 2b 04 01 ef ff ff fe 01 02 03 04 05 06";
        ByteBuf buffer = ByteBufUtils.hexStringToByteBuf(input);
        Action action = NextHopCodec.getInstance().deserialize(buffer);
        
        OfjAugCofAction aug = action.getAugmentation(OfjAugCofAction.class);
        Assert.assertNotNull(aug);
        
        ActionOutputNh nextHopAction = aug.getActionOutputNh();
        Assert.assertNotNull(nextHopAction);
        Assert.assertEquals(CofAtOutputNhAddressType.MAC48, nextHopAction.getAddressType());
        
        Assert.assertEquals(0xeffffffeL, nextHopAction.getAddressExtra().getValue().longValue());
        Assert.assertEquals("01:02:03:04:05:06", nextHopAction.getAddress().getMacAddress().getValue());
    }
    
    /**
     * Test method for {@link org.opendaylight.openflowjava.cof.codec.action.NextHopCodec#deserialize(io.netty.buffer.ByteBuf)}.
     * expect  port+P2P
     */
    @Test
    public void testDeserialize4() {
        String input = "ff ff 00 0c 00 00 00 0c 00 2b 01 01 ef ff ff fe";
        ByteBuf buffer = ByteBufUtils.hexStringToByteBuf(input);
        Action action = NextHopCodec.getInstance().deserialize(buffer);
        
        OfjAugCofAction aug = action.getAugmentation(OfjAugCofAction.class);
        Assert.assertNotNull(aug);
        
        ActionOutputNh nextHopAction = aug.getActionOutputNh();
        Assert.assertNotNull(nextHopAction);
        Assert.assertEquals(CofAtOutputNhAddressType.P2P, nextHopAction.getAddressType());
        
        Assert.assertEquals(0xeffffffeL, nextHopAction.getAddressExtra().getValue().longValue());
        Assert.assertNull(nextHopAction.getAddress());
    }
}
