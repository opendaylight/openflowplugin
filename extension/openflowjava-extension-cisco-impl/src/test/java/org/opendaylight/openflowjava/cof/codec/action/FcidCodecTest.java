/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
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
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.cof.action.rev141008.action.container.action.choice.OfjCofActionFcid;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.cof.action.rev141008.action.container.action.choice.OfjCofActionFcidBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.cof.action.rev141008.ofj.cof.action.fcid.grouping.ActionFcid;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.cof.action.rev141008.ofj.cof.action.fcid.grouping.ActionFcidBuilder;

public class FcidCodecTest {
    private static final String fcidBytes = "ff ff 00 10 00 00 00 0c 00 03 2a 00 00 00 00 00";

    /**
     * Test method for {@link org.opendaylight.openflowjava.cof.codec.action.FcidCodec#deserialize(io.netty.buffer.ByteBuf)}
     */
    @Test
    public void testDeserialize() throws Exception {
        ByteBuf buffer = ByteBufUtils.hexStringToByteBuf(fcidBytes);
        Action action = FcidCodec.getInstance().deserialize(buffer);

        ActionFcid actionFcid = ((OfjCofActionFcid) action.getActionChoice()).getActionFcid();
        Assert.assertNotNull(actionFcid);
        Assert.assertEquals(42, actionFcid.getFcid().shortValue());
    }

    /**
     * Test method for {@link org.opendaylight.openflowjava.cof.codec.action.FcidCodec#serialize(org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.actions.grouping.Action, io.netty.buffer.ByteBuf)}
     *
     * @throws Exception
     */
    @Test
    public void testSerialize() throws Exception {
        ByteBuf buffer = UnpooledByteBufAllocator.DEFAULT.buffer();

        ActionFcidBuilder fcidBuilder = new ActionFcidBuilder();
        fcidBuilder.setFcid((short) 42);

        OfjCofActionFcidBuilder cofActionBld = new OfjCofActionFcidBuilder().setActionFcid(fcidBuilder.build());
        ActionBuilder inputBld = new ActionBuilder().setActionChoice(cofActionBld.build());
        FcidCodec.getInstance().serialize(inputBld.build(), buffer);

        Assert.assertEquals(fcidBytes, ByteBufUtils.byteBufToHexString(buffer));
    }
}