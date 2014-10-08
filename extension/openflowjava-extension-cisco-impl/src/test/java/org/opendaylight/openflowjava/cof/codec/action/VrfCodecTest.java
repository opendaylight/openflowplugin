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
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.cof.action.rev141008.CofAtVrfType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.cof.action.rev141008.OfjAugCofAction;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.cof.action.rev141008.OfjAugCofActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.cof.action.rev141008.VrfExtra;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.cof.action.rev141008.VrfName;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.cof.action.rev141008.VrfVpnId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.cof.action.rev141008.ofj.cof.action.vrf.grouping.ActionVrf;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.cof.action.rev141008.ofj.cof.action.vrf.grouping.ActionVrfBuilder;

/**
 * 
 */
public class VrfCodecTest {

    /**
     * Test method for {@link org.opendaylight.openflowjava.cof.codec.action.VrfCodec#serialize(org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev130731.actions.grouping.Action, io.netty.buffer.ByteBuf)}.
     * 
     * use vpnId
     */
    @Test
    public void testSerialize1() {
        ByteBuf buffer = UnpooledByteBufAllocator.DEFAULT.buffer();
        
        byte[] vpnIdRaw = new byte[]{0, 1, 2, 3, 4, 5, 6};
        VrfExtra vrfExtra = new VrfExtra(new VrfVpnId(vpnIdRaw));
        ActionVrfBuilder vrfBld = new ActionVrfBuilder();
        vrfBld.setVpnType(CofAtVrfType.VPNID).setVrfExtra(vrfExtra);
        
        OfjAugCofActionBuilder cofActionBld = new OfjAugCofActionBuilder().setActionVrf(vrfBld.build());
        ActionBuilder inputBld = new ActionBuilder().addAugmentation(OfjAugCofAction.class, cofActionBld.build());
        VrfCodec.getInstance().serialize(inputBld.build(), buffer);
        
        String expected = "ff ff 00 12 00 00 00 0c 00 2a 01 00 01 02 03 04 05 06";
        Assert.assertEquals(expected , ByteBufUtils.byteBufToHexString(buffer));
    }
    
    /**
     * Test method for {@link org.opendaylight.openflowjava.cof.codec.action.VrfCodec#serialize(org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev130731.actions.grouping.Action, io.netty.buffer.ByteBuf)}.
     * 
     * use vpnName
     */
    @Test
    public void testSerialize2() {
        ByteBuf buffer = UnpooledByteBufAllocator.DEFAULT.buffer();
        
        VrfExtra vrfExtra = new VrfExtra(new VrfName("h2g2"));
        ActionVrfBuilder vrfBld = new ActionVrfBuilder();
        vrfBld.setVpnType(CofAtVrfType.NAME).setVrfExtra(vrfExtra);
        
        OfjAugCofActionBuilder cofActionBld = new OfjAugCofActionBuilder().setActionVrf(vrfBld.build());
        ActionBuilder inputBld = new ActionBuilder().addAugmentation(OfjAugCofAction.class, cofActionBld.build());
        VrfCodec.getInstance().serialize(inputBld.build(), buffer);
        
        String expected = "ff ff 00 0f 00 00 00 0c 00 2a 02 68 32 67 32";
        Assert.assertEquals(expected , ByteBufUtils.byteBufToHexString(buffer));
    }

    /**
     * Test method for {@link org.opendaylight.openflowjava.cof.codec.action.VrfCodec#deserialize(io.netty.buffer.ByteBuf)}.
     * 
     * expect vpnId
     */
    @Test
    public void testDeserialize1() {
        String input = "ff ff 00 12 00 00 00 0c 00 2a 01 00 01 02 03 04 05 06";
        ByteBuf buffer = ByteBufUtils.hexStringToByteBuf(input);
        Action action = VrfCodec.getInstance().deserialize(buffer);
        
        OfjAugCofAction aug = action.getAugmentation(OfjAugCofAction.class);
        Assert.assertNotNull(aug);
        
        ActionVrf vrfAction = aug.getActionVrf();
        Assert.assertNotNull(vrfAction);
        Assert.assertEquals(CofAtVrfType.VPNID, vrfAction.getVpnType());
        
        byte[] vpnIdRaw = new byte[]{0, 1, 2, 3, 4, 5, 6};
        Assert.assertArrayEquals(vpnIdRaw , vrfAction.getVrfExtra().getVrfVpnId().getValue());
    }
    
    /**
     * Test method for {@link org.opendaylight.openflowjava.cof.codec.action.VrfCodec#deserialize(io.netty.buffer.ByteBuf)}.
     * 
     * expect vpnName
     */
    @Test
    public void testDeserialize2() {
        String input = "ff ff 00 0f 00 00 00 0c 00 2a 02 68 32 67 32";
        ByteBuf buffer = ByteBufUtils.hexStringToByteBuf(input);
        Action action = VrfCodec.getInstance().deserialize(buffer);
        
        OfjAugCofAction aug = action.getAugmentation(OfjAugCofAction.class);
        Assert.assertNotNull(aug);
        
        ActionVrf vrfAction = aug.getActionVrf();
        Assert.assertNotNull(vrfAction);
        Assert.assertEquals(CofAtVrfType.NAME, vrfAction.getVpnType());
        
        String vpnName = "h2g2";
        Assert.assertEquals(vpnName , vrfAction.getVrfExtra().getVrfName().getValue());
    }

}
