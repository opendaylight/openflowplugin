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
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev130731.actions.grouping.Action;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev130731.actions.grouping.ActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.cof.action.rev141008.OfjAugCofAction;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.cof.action.rev141008.OfjAugCofActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.cof.action.rev141008.ofj.cof.action.vrf.grouping.ActionVrf;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.cof.action.rev141008.ofj.cof.action.vrf.grouping.ActionVrfBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.cof.action.rev141010.CofActionVrfGrouping;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.cof.action.rev141010.CofAtVrfType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.cof.action.rev141010.VrfExtra;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.cof.action.rev141010.VrfName;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.cof.action.rev141010.VrfVpnId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.cof.action.rev141010.cof.action.vrf.grouping.ActionVrfHi;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.cof.action.rev141010.cof.action.vrf.grouping.ActionVrfHiBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.cisco.action.rev141010.group.desc.stats.updated.group.desc.stats.buckets.bucket.action.action.CofActionVrfNotifGroupDescStatsUpdatedCaseBuilder;

/**
 * 
 */
public class VrfCodecTest {

    /** default action path suitable for tests */
    private static final ActionPath DEFAULT_ACTION_PATH = 
            ActionPath.GROUPDESCSTATSUPDATED_GROUPDESCSTATS_BUCKETS_BUCKET_ACTION;
    /** singleton converter */
    private static final VrfConvertor VRF_CONVERTOR = new VrfConvertor();

    /**
     * Test method for {@link VrfConvertor#convert(Action, org.opendaylight.openflowplugin.extension.api.path.ActionPath)}.
     * 
     * use vpnId
     */
    @Test
    public void testConvert1() {
        byte[] vpnIdRaw = new byte[]{0, 1, 2, 3, 4, 5, 6};
        ActionVrfBuilder vrfBld = new ActionVrfBuilder();
        vrfBld.setVpnType(1).setVrfExtra(vpnIdRaw);
        
        OfjAugCofActionBuilder cofActionBld = new OfjAugCofActionBuilder().setActionVrf(vrfBld.build());
        ActionBuilder inputBld = new ActionBuilder().addAugmentation(OfjAugCofAction.class, cofActionBld.build());
        org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.Action actionHi = 
                VRF_CONVERTOR.convert(inputBld.build(), DEFAULT_ACTION_PATH);
        
        Assert.assertTrue("converted action is of incorrect type: "+actionHi.getClass(), 
                actionHi instanceof CofActionVrfGrouping);
        ActionVrfHi actionVrfHi = ((CofActionVrfGrouping) actionHi).getActionVrfHi();
        Assert.assertNotNull(actionVrfHi);
        Assert.assertEquals(CofAtVrfType.VPNID, actionVrfHi.getVpnType());
        Assert.assertArrayEquals(vpnIdRaw, actionVrfHi.getVrfExtra().getVrfVpnId().getValue());
    }
    
    /**
     * Test method for {@link VrfConvertor#convert(Action, org.opendaylight.openflowplugin.extension.api.path.ActionPath)}.
     * 
     * use vpnName
     */
    @Test
    public void testConvert2() {
        String vpnName = "h2g2";
        ActionVrfBuilder vrfBld = new ActionVrfBuilder();
        vrfBld.setVpnType(2).setVrfExtra(vpnName.getBytes());
        
        OfjAugCofActionBuilder cofActionBld = new OfjAugCofActionBuilder().setActionVrf(vrfBld.build());
        ActionBuilder inputBld = new ActionBuilder().addAugmentation(OfjAugCofAction.class, cofActionBld.build());
        org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.Action actionHi = 
                VRF_CONVERTOR.convert(inputBld.build(), DEFAULT_ACTION_PATH);
        
        Assert.assertTrue("converted action is of incorrect type: "+actionHi.getClass(), 
                actionHi instanceof CofActionVrfGrouping);
        ActionVrfHi actionVrfHi = ((CofActionVrfGrouping) actionHi).getActionVrfHi();
        Assert.assertNotNull(actionVrfHi);
        Assert.assertEquals(CofAtVrfType.NAME, actionVrfHi.getVpnType());
        Assert.assertEquals(vpnName, new String(actionVrfHi.getVrfExtra().getVrfName().getValue()));
    }

    /**
     * Test method for {@link VrfConvertor#convert(org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.Action)}.
     * 
     * expect vpnId
     */
    @Test
    public void testConvertBack1() {
        byte[] vpnIdRaw = new byte[]{0, 1, 2, 3, 4, 5, 6};
        ActionVrfHiBuilder vrfHiBld = new ActionVrfHiBuilder();
        vrfHiBld.setVpnType(CofAtVrfType.VPNID).setVrfExtra(new VrfExtra(new VrfVpnId(vpnIdRaw)));
        
        CofActionVrfNotifGroupDescStatsUpdatedCaseBuilder cofActionBld = new CofActionVrfNotifGroupDescStatsUpdatedCaseBuilder();
        cofActionBld.setActionVrfHi(vrfHiBld.build());
        
        Action action = VRF_CONVERTOR.convert(cofActionBld.build());
        
        OfjAugCofAction cofActionVrfAug = action.getAugmentation(OfjAugCofAction.class);
        Assert.assertNotNull(cofActionVrfAug);
        ActionVrf actionVrf = cofActionVrfAug.getActionVrf();
        Assert.assertNotNull(actionVrf);
        Assert.assertEquals(1, actionVrf.getVpnType().intValue());
        Assert.assertArrayEquals(vpnIdRaw, actionVrf.getVrfExtra());
    }
    
    /**
     * Test method for {@link VrfConvertor#convert(org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.Action)}.
     * 
     * expect vpnName
     */
    @Test
    public void testConvertBack2() {
        String vpnName= "h2g2";
        ActionVrfHiBuilder vrfHiBld = new ActionVrfHiBuilder();
        vrfHiBld.setVpnType(CofAtVrfType.NAME).setVrfExtra(new VrfExtra(new VrfName(vpnName)));
        
        CofActionVrfNotifGroupDescStatsUpdatedCaseBuilder cofActionBld = new CofActionVrfNotifGroupDescStatsUpdatedCaseBuilder();
        cofActionBld.setActionVrfHi(vrfHiBld.build());
        
        Action action = VRF_CONVERTOR.convert(cofActionBld.build());
        
        OfjAugCofAction cofActionVrfAug = action.getAugmentation(OfjAugCofAction.class);
        Assert.assertNotNull(cofActionVrfAug);
        ActionVrf actionVrf = cofActionVrfAug.getActionVrf();
        Assert.assertNotNull(actionVrf);
        Assert.assertEquals(2, actionVrf.getVpnType().intValue());
        Assert.assertEquals(vpnName, new String(actionVrf.getVrfExtra()));
    }

}
