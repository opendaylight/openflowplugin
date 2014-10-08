package org.opendaylight.openflowplugin.extension.vendor.cisco.convertor.action;
/**
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */


import org.junit.Assert;
import org.junit.Test;
import org.opendaylight.openflowplugin.extension.api.path.ActionPath;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.Ipv4Address;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.Ipv6Address;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev100924.MacAddress;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev130731.actions.grouping.Action;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev130731.actions.grouping.ActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.cof.action.rev141008.OfjAugCofAction;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.cof.action.rev141008.OfjAugCofActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.cof.action.rev141008.ofj.cof.action.output.nh.grouping.ActionOutputNh;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.cof.action.rev141008.ofj.cof.action.output.nh.grouping.ActionOutputNhBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.cisco.action.rev141010.group.desc.stats.updated.group.desc.stats.buckets.bucket.action.action.CofActionNextHopNotifGroupDescStatsUpdatedCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.cof.hi.action.rev141010.CofActionNextHopGrouping;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.cof.hi.action.rev141010.CofAtOutputNhAddressExtraType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.cof.hi.action.rev141010.CofAtOutputNhAddressType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.cof.hi.action.rev141010.NhPortNumber;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.cof.hi.action.rev141010.cof.action.next.hop.grouping.ActionOutputNhHi;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.cof.hi.action.rev141010.cof.action.next.hop.grouping.ActionOutputNhHi.AddressNh;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.cof.hi.action.rev141010.cof.action.next.hop.grouping.ActionOutputNhHiBuilder;

/**
 * 
 */
public class NextHopCodecTest {
    
    /** default port number */
    private static final long DEFAULT_PORT = 0xeffffffeL;
    /** default action path suitable for tests */
    private static final ActionPath DEFAULT_ACTION_PATH = 
            ActionPath.GROUPDESCSTATSUPDATED_GROUPDESCSTATS_BUCKETS_BUCKET_ACTION;
    /** singleton converter */
    private static final NextHopConvertor NH_CONVERTOR = new NextHopConvertor();

    /**
     * Test method for {@link NextHopConvertor#convert(Action, ActionPath)}.
     * expect  port+ipv4
     */
    @Test
    public void testConvert1() {
        byte[] ipv4Raw = new byte[]{ 10, 1, 2, 3 };
        ActionOutputNhBuilder nhBld = new ActionOutputNhBuilder();
        nhBld.setAddressType(2).setAddressExtraType(1)
        .setAddressExtra(DEFAULT_PORT).setAddress(ipv4Raw);
        
        OfjAugCofActionBuilder cofActionBld = new OfjAugCofActionBuilder().setActionOutputNh(nhBld.build());
        ActionBuilder inputBld = new ActionBuilder().addAugmentation(OfjAugCofAction.class, cofActionBld.build());
        
        org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.Action actionHi = 
                NH_CONVERTOR.convert(inputBld.build(), DEFAULT_ACTION_PATH);
        
        Assert.assertTrue("converted action is of incorrect type: "+actionHi.getClass(), 
                actionHi instanceof CofActionNextHopGrouping);
        ActionOutputNhHi actionOutNhHi = ((CofActionNextHopGrouping) actionHi).getActionOutputNhHi();
        Assert.assertNotNull(actionOutNhHi);
        Assert.assertEquals(CofAtOutputNhAddressType.IPV4, actionOutNhHi.getAddressType());
        Assert.assertEquals(CofAtOutputNhAddressExtraType.PORT, actionOutNhHi.getAddressExtraType());
        Ipv4Address ipv4Address = actionOutNhHi.getAddressNh().getIpv4Address();
        Assert.assertNotNull("ipv4 not present", ipv4Address);
        Assert.assertEquals("10.1.2.3", ipv4Address.getValue());
        Assert.assertEquals(0xeffffffeL, actionOutNhHi.getAddressExtra().getValue().longValue());
    }
    
    /**
     * Test method for {@link NextHopConvertor#convert(Action, ActionPath)}.
     * expect  port+ipv6
     */
    @Test
    public void testConvert2() {
        byte[] ipv6Raw = new byte[]{ 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16 };
        ActionOutputNhBuilder nhBld = new ActionOutputNhBuilder();
        nhBld.setAddressType(3).setAddressExtraType(1)
        .setAddressExtra(DEFAULT_PORT).setAddress(ipv6Raw);
        
        OfjAugCofActionBuilder cofActionBld = new OfjAugCofActionBuilder().setActionOutputNh(nhBld.build());
        ActionBuilder inputBld = new ActionBuilder().addAugmentation(OfjAugCofAction.class, cofActionBld.build());
        
        org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.Action actionHi = 
                NH_CONVERTOR.convert(inputBld.build(), DEFAULT_ACTION_PATH);
        
        Assert.assertTrue("converted action is of incorrect type: "+actionHi.getClass(), 
                actionHi instanceof CofActionNextHopGrouping);
        ActionOutputNhHi actionOutNhHi = ((CofActionNextHopGrouping) actionHi).getActionOutputNhHi();
        Assert.assertNotNull(actionOutNhHi);
        Assert.assertEquals(CofAtOutputNhAddressType.IPV6, actionOutNhHi.getAddressType());
        Assert.assertEquals(CofAtOutputNhAddressExtraType.PORT, actionOutNhHi.getAddressExtraType());
        Ipv6Address ipv6Address = actionOutNhHi.getAddressNh().getIpv6Address();
        Assert.assertNotNull("ipv6 not present", ipv6Address);
        Assert.assertEquals("0102:0304:0506:0708:090A:0B0C:0D0E:0F10", ipv6Address.getValue());
        Assert.assertEquals(0xeffffffeL, actionOutNhHi.getAddressExtra().getValue().longValue());
    }
    
    /**
     * Test method for {@link NextHopConvertor#convert(Action, ActionPath)}.
     * expect port+mac48
     */
    @Test
    public void testConvert3() {
        byte[] mac48Raw = new byte[]{ 1, 2, 3, 4, 5, 6 };
        ActionOutputNhBuilder nhBld = new ActionOutputNhBuilder();
        nhBld.setAddressType(4).setAddressExtraType(1)
        .setAddressExtra(DEFAULT_PORT).setAddress(mac48Raw);
        
        OfjAugCofActionBuilder cofActionBld = new OfjAugCofActionBuilder().setActionOutputNh(nhBld.build());
        ActionBuilder inputBld = new ActionBuilder().addAugmentation(OfjAugCofAction.class, cofActionBld.build());
        
        org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.Action actionHi = 
                NH_CONVERTOR.convert(inputBld.build(), DEFAULT_ACTION_PATH);
        
        Assert.assertTrue("converted action is of incorrect type: "+actionHi.getClass(), 
                actionHi instanceof CofActionNextHopGrouping);
        ActionOutputNhHi actionOutNhHi = ((CofActionNextHopGrouping) actionHi).getActionOutputNhHi();
        Assert.assertNotNull(actionOutNhHi);
        Assert.assertEquals(CofAtOutputNhAddressType.MAC48, actionOutNhHi.getAddressType());
        Assert.assertEquals(CofAtOutputNhAddressExtraType.PORT, actionOutNhHi.getAddressExtraType());
        MacAddress mac48 = actionOutNhHi.getAddressNh().getMacAddress();
        Assert.assertNotNull("mac not present", mac48);
        Assert.assertEquals("01:02:03:04:05:06", mac48.getValue());
        Assert.assertEquals(0xeffffffeL, actionOutNhHi.getAddressExtra().getValue().longValue());
    }
    
    /**
     * Test method for {@link NextHopConvertor#convert(Action, ActionPath)}.
     * expect port+P2P
     */
    @Test
    public void testConvert4() {
        ActionOutputNhBuilder nhBld = new ActionOutputNhBuilder();
        nhBld.setAddressType(1).setAddressExtraType(1)
        .setAddressExtra(DEFAULT_PORT);
        
        OfjAugCofActionBuilder cofActionBld = new OfjAugCofActionBuilder().setActionOutputNh(nhBld.build());
        ActionBuilder inputBld = new ActionBuilder().addAugmentation(OfjAugCofAction.class, cofActionBld.build());
        
        org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.Action actionHi = 
                NH_CONVERTOR.convert(inputBld.build(), DEFAULT_ACTION_PATH);
        
        Assert.assertTrue("converted action is of incorrect type: "+actionHi.getClass(), 
                actionHi instanceof CofActionNextHopGrouping);
        ActionOutputNhHi actionOutNhHi = ((CofActionNextHopGrouping) actionHi).getActionOutputNhHi();
        Assert.assertNotNull(actionOutNhHi);
        Assert.assertEquals(CofAtOutputNhAddressType.P2P, actionOutNhHi.getAddressType());
        Assert.assertEquals(CofAtOutputNhAddressExtraType.PORT, actionOutNhHi.getAddressExtraType());
        Assert.assertNull(actionOutNhHi.getAddressNh());
        Assert.assertEquals(0xeffffffeL, actionOutNhHi.getAddressExtra().getValue().longValue());
    }
    
    /**
     * Test method for {@link NextHopConvertor#convert(org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.Action)}.
     * 
     * use port+ivp4
     */
    @Test
    public void testConvertBack1() {
        ActionOutputNhHiBuilder actionNhHiBld = new ActionOutputNhHiBuilder();
        actionNhHiBld.setAddressType(CofAtOutputNhAddressType.IPV4);
        actionNhHiBld.setAddressExtraType(CofAtOutputNhAddressExtraType.PORT);
        actionNhHiBld.setAddressNh(new AddressNh(new Ipv4Address("10.1.2.3")));
        actionNhHiBld.setAddressExtra(new NhPortNumber(DEFAULT_PORT));
        
        CofActionNextHopNotifGroupDescStatsUpdatedCaseBuilder inputBld = 
                new CofActionNextHopNotifGroupDescStatsUpdatedCaseBuilder(); 
        inputBld.setActionOutputNhHi(actionNhHiBld.build());
        Action action = NH_CONVERTOR.convert(inputBld.build());
        
        OfjAugCofAction cofActionNhAug = action.getAugmentation(OfjAugCofAction.class);
        Assert.assertNotNull(cofActionNhAug);
        ActionOutputNh actionNh = cofActionNhAug.getActionOutputNh();
        Assert.assertNotNull(actionNh);
        Assert.assertEquals(2, actionNh.getAddressType().intValue());
        Assert.assertEquals(1, actionNh.getAddressExtraType().intValue());
        Assert.assertEquals(DEFAULT_PORT, actionNh.getAddressExtra().longValue());
        byte[] expected = new byte[]{ 10, 1, 2, 3 };
        Assert.assertArrayEquals(expected  , actionNh.getAddress());
    }
    
    /**
     * Test method for {@link NextHopConvertor#convert(org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.Action)}.
     * 
     * use port+ivp6
     */
    @Test
    public void testConvertBack2() {
        ActionOutputNhHiBuilder actionNhHiBld = new ActionOutputNhHiBuilder();
        actionNhHiBld.setAddressType(CofAtOutputNhAddressType.IPV6);
        actionNhHiBld.setAddressExtraType(CofAtOutputNhAddressExtraType.PORT);
        actionNhHiBld.setAddressNh(new AddressNh(new Ipv6Address(
                "0102:0304:0506:0708:090A:0B0C:0D0E:0F10")));
        actionNhHiBld.setAddressExtra(new NhPortNumber(DEFAULT_PORT));
        
        CofActionNextHopNotifGroupDescStatsUpdatedCaseBuilder inputBld = 
                new CofActionNextHopNotifGroupDescStatsUpdatedCaseBuilder(); 
        inputBld.setActionOutputNhHi(actionNhHiBld.build());
        Action action = NH_CONVERTOR.convert(inputBld.build());
        
        OfjAugCofAction cofActionNhAug = action.getAugmentation(OfjAugCofAction.class);
        Assert.assertNotNull(cofActionNhAug);
        ActionOutputNh actionNh = cofActionNhAug.getActionOutputNh();
        Assert.assertNotNull(actionNh);
        Assert.assertEquals(3, actionNh.getAddressType().intValue());
        Assert.assertEquals(1, actionNh.getAddressExtraType().intValue());
        Assert.assertEquals(DEFAULT_PORT, actionNh.getAddressExtra().longValue());
        byte[] expected = new byte[]{ 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16 };
        Assert.assertArrayEquals(expected  , actionNh.getAddress());
    }
    
    /**
     * Test method for {@link NextHopConvertor#convert(org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.Action)}.
     * 
     * use port+mac48
     */
    @Test
    public void testConvertBack3() {
        ActionOutputNhHiBuilder actionNhHiBld = new ActionOutputNhHiBuilder();
        actionNhHiBld.setAddressType(CofAtOutputNhAddressType.MAC48);
        actionNhHiBld.setAddressExtraType(CofAtOutputNhAddressExtraType.PORT);
        actionNhHiBld.setAddressNh(new AddressNh(new MacAddress(
                "01:02:03:04:05:06")));
        actionNhHiBld.setAddressExtra(new NhPortNumber(DEFAULT_PORT));
        
        CofActionNextHopNotifGroupDescStatsUpdatedCaseBuilder inputBld = 
                new CofActionNextHopNotifGroupDescStatsUpdatedCaseBuilder(); 
        inputBld.setActionOutputNhHi(actionNhHiBld.build());
        Action action = NH_CONVERTOR.convert(inputBld.build());
        
        OfjAugCofAction cofActionNhAug = action.getAugmentation(OfjAugCofAction.class);
        Assert.assertNotNull(cofActionNhAug);
        ActionOutputNh actionNh = cofActionNhAug.getActionOutputNh();
        Assert.assertNotNull(actionNh);
        Assert.assertEquals(4, actionNh.getAddressType().intValue());
        Assert.assertEquals(1, actionNh.getAddressExtraType().intValue());
        Assert.assertEquals(DEFAULT_PORT, actionNh.getAddressExtra().longValue());
        byte[] expected = new byte[]{ 1, 2, 3, 4, 5, 6 };
        Assert.assertArrayEquals(expected  , actionNh.getAddress());
    }
    
    /**
     * Test method for {@link NextHopConvertor#convert(org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.Action)}.
     * 
     * use use port (p2p)
     */
    @Test
    public void testConvertBack4() {
        ActionOutputNhHiBuilder actionNhHiBld = new ActionOutputNhHiBuilder();
        actionNhHiBld.setAddressType(CofAtOutputNhAddressType.P2P);
        actionNhHiBld.setAddressExtraType(CofAtOutputNhAddressExtraType.PORT);
        actionNhHiBld.setAddressNh(new AddressNh(new MacAddress(
                "01:02:03:04:05:06")));
        actionNhHiBld.setAddressExtra(new NhPortNumber(DEFAULT_PORT));
        
        CofActionNextHopNotifGroupDescStatsUpdatedCaseBuilder inputBld = 
                new CofActionNextHopNotifGroupDescStatsUpdatedCaseBuilder(); 
        inputBld.setActionOutputNhHi(actionNhHiBld.build());
        Action action = NH_CONVERTOR.convert(inputBld.build());
        
        OfjAugCofAction cofActionNhAug = action.getAugmentation(OfjAugCofAction.class);
        Assert.assertNotNull(cofActionNhAug);
        ActionOutputNh actionNh = cofActionNhAug.getActionOutputNh();
        Assert.assertNotNull(actionNh);
        Assert.assertEquals(1, actionNh.getAddressType().intValue());
        Assert.assertEquals(1, actionNh.getAddressExtraType().intValue());
        Assert.assertEquals(DEFAULT_PORT, actionNh.getAddressExtra().longValue());
        Assert.assertNull(actionNh.getAddress());
    }
}
