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
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.cof.action.rev141008.action.container.action.choice.OfjCofActionNetflow;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.cof.action.rev141008.action.container.action.choice.OfjCofActionNetflowBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.cof.action.rev141008.ofj.cof.action.netflow.grouping.ActionNetflow;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.cof.action.rev141008.ofj.cof.action.netflow.grouping.ActionNetflowBuilder;

public class NetflowCodecTest {

    private static final String netFlowBytes = "ff ff 00 10 00 00 00 0c 00 02 00 00 00 00 00 00";

    /**
     * Test method for {@link org.opendaylight.openflowjava.cof.codec.action.NetflowCodec#deserialize(io.netty.buffer.ByteBuf)}
     */
    @Test
    public void testDeserialize() throws Exception {
        ByteBuf buffer = ByteBufUtils.hexStringToByteBuf(netFlowBytes);
        Action action = NetflowCodec.getInstance().deserialize(buffer);

        ActionNetflow actionNetflow = ((OfjCofActionNetflow) action.getActionChoice()).getActionNetflow();
        Assert.assertNotNull(actionNetflow);
    }

    /**
     * Test method for {@link org.opendaylight.openflowjava.cof.codec.action.NetflowCodec#serialize(org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.actions.grouping.Action, io.netty.buffer.ByteBuf)}
     *
     * @throws Exception
     */
    @Test
    public void testSerialize() throws Exception {
        ByteBuf buffer = UnpooledByteBufAllocator.DEFAULT.buffer();

        ActionNetflowBuilder actionNetflowBuilder = new ActionNetflowBuilder();
        actionNetflowBuilder.setNetflow(true);

        OfjCofActionNetflowBuilder cofActionBld = new OfjCofActionNetflowBuilder().setActionNetflow(actionNetflowBuilder.build());
        ActionBuilder inputBld = new ActionBuilder().setActionChoice(cofActionBld.build());
        NetflowCodec.getInstance().serialize(inputBld.build(), buffer);

        Assert.assertEquals(netFlowBytes, ByteBufUtils.byteBufToHexString(buffer));
    }


}