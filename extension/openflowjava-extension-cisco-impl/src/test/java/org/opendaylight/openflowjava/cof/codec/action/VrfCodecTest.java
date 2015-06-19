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
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.actions.grouping.Action;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.actions.grouping.ActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.cof.action.rev141008.action.container.action.choice.OfjCofActionVrf;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.cof.action.rev141008.action.container.action.choice.OfjCofActionVrfBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.cof.action.rev141008.ofj.cof.action.vrf.grouping.ActionVrf;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.cof.action.rev141008.ofj.cof.action.vrf.grouping.ActionVrfBuilder;

/**
 *
 */
public class VrfCodecTest {

    /**
     * Test method for {@link org.opendaylight.openflowjava.cof.codec.action.VrfCodec#serialize(org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.actions.grouping.Action, io.netty.buffer.ByteBuf)}.
     * <p/>
     * use vpnId
     */
    @Test
    public void testSerialize1() {
        ByteBuf buffer = UnpooledByteBufAllocator.DEFAULT.buffer();

        byte[] vrfExtra = new byte[]{0, 1, 2, 3, 4, 5, 6};
        ActionVrfBuilder vrfBld = new ActionVrfBuilder();
        vrfBld.setVpnType(1).setVrfExtra(vrfExtra);

        OfjCofActionVrfBuilder cofActionBld = new OfjCofActionVrfBuilder().setActionVrf(vrfBld.build());
        ActionBuilder inputBld = new ActionBuilder().setActionChoice(cofActionBld.build());

        VrfCodec.getInstance().serialize(inputBld.build(), buffer);

        String expected = "ff ff 00 18 00 00 00 0c 00 04 01 00 01 02 03 04 05 06 00 00 00 00 00 00";
        Assert.assertEquals(expected, ByteBufUtils.byteBufToHexString(buffer));
    }

    /**
     * Test method for {@link org.opendaylight.openflowjava.cof.codec.action.VrfCodec#serialize(org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.actions.grouping.Action, io.netty.buffer.ByteBuf)}.
     * <p/>
     * use vpnName
     */
    @Test
    public void testSerialize2() {
        ByteBuf buffer = UnpooledByteBufAllocator.DEFAULT.buffer();

        byte[] vrfExtra = "h2g2".getBytes();
        ActionVrfBuilder vrfBld = new ActionVrfBuilder();
        vrfBld.setVpnType(2).setVrfExtra(vrfExtra);

        OfjCofActionVrfBuilder cofActionBld = new OfjCofActionVrfBuilder().setActionVrf(vrfBld.build());
        ActionBuilder inputBld = new ActionBuilder().setActionChoice(cofActionBld.build());
        VrfCodec.getInstance().serialize(inputBld.build(), buffer);

        String expected = "ff ff 00 10 00 00 00 0c 00 04 02 68 32 67 32 00";
        Assert.assertEquals(expected, ByteBufUtils.byteBufToHexString(buffer));
    }

    /**
     * Test method for {@link org.opendaylight.openflowjava.cof.codec.action.VrfCodec#deserialize(io.netty.buffer.ByteBuf)}.
     * <p/>
     * expect vpnId
     */
    @Test
    public void testDeserialize1() {
        String input = "ff ff 00 18 00 00 00 0c 00 04 01 00 01 02 03 04 05 06 00 00 00 00 00 00";
        ByteBuf buffer = ByteBufUtils.hexStringToByteBuf(input);
        Action action = VrfCodec.getInstance().deserialize(buffer);

        ActionVrf vrfAction = ((OfjCofActionVrf) action.getActionChoice()).getActionVrf();
        Assert.assertNotNull(vrfAction);
        Assert.assertEquals(1, vrfAction.getVpnType().intValue());

        byte[] vpnIdRaw = new byte[]{0, 1, 2, 3, 4, 5, 6};
        Assert.assertArrayEquals(vpnIdRaw, vrfAction.getVrfExtra());
        Assert.assertEquals((input.length() + 1) / 3, buffer.readerIndex());
    }

    /**
     * Test method for {@link org.opendaylight.openflowjava.cof.codec.action.VrfCodec#deserialize(io.netty.buffer.ByteBuf)}.
     * <p/>
     * expect vpnName
     */
    @Test
    public void testDeserialize2() {
        String input = "ff ff 00 10 00 00 00 0c 00 04 02 68 32 67 32 00";
        ByteBuf buffer = ByteBufUtils.hexStringToByteBuf(input);
        Action action = VrfCodec.getInstance().deserialize(buffer);

        ActionVrf vrfAction = ((OfjCofActionVrf) action.getActionChoice()).getActionVrf();
        Assert.assertNotNull(vrfAction);
        Assert.assertEquals(2, vrfAction.getVpnType().intValue());

        String vpnName = "h2g2";
        Assert.assertEquals(vpnName, new String(vrfAction.getVrfExtra()));
        Assert.assertEquals((input.length() + 1) / 3, buffer.readerIndex());
    }

}
