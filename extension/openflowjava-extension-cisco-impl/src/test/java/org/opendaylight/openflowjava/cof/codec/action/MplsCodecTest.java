/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowjava.cof.codec.action;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.UnpooledByteBufAllocator;
import junit.framework.TestCase;
import org.junit.Assert;
import org.junit.Test;
import org.opendaylight.openflowjava.util.ByteBufUtils;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.actions.grouping.Action;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.actions.grouping.ActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.cof.action.rev141008.action.container.action.choice.OfjCofActionMplsLsp;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.cof.action.rev141008.action.container.action.choice.OfjCofActionMplsLspBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.cof.action.rev141008.ofj.cof.action.mpls.lsp.grouping.ActionMplsLsp;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.cof.action.rev141008.ofj.cof.action.mpls.lsp.grouping.ActionMplsLspBuilder;

public class MplsCodecTest extends TestCase {

    private static final String mplsBytes = "ff ff 00 0e 00 00 00 0c 00 05 4d 50 4c 53";
    private static final String mplsName = "MPLS";

    /**
     * Test method for {@link org.opendaylight.openflowjava.cof.codec.action.MplsCodec#deserialize(io.netty.buffer.ByteBuf)}
     */
    @Test
    public void testDeserialize() throws Exception {
        ByteBuf buffer = ByteBufUtils.hexStringToByteBuf(mplsBytes);
        Action action = MplsCodec.getInstance().deserialize(buffer);

        ActionMplsLsp actionMplsLsp = ((OfjCofActionMplsLsp) action.getActionChoice()).getActionMplsLsp();
        Assert.assertNotNull(actionMplsLsp);
        Assert.assertEquals(mplsName, new String(actionMplsLsp.getName()));
    }

    /**
     * Test method for {@link org.opendaylight.openflowjava.cof.codec.action.MplsCodec#serialize(org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.actions.grouping.Action, io.netty.buffer.ByteBuf)}
     *
     * @throws Exception
     */
    @Test
    public void testSerialize() throws Exception {
        ByteBuf buffer = UnpooledByteBufAllocator.DEFAULT.buffer();

        ActionMplsLspBuilder actionMplsLspBuilder = new ActionMplsLspBuilder();
        actionMplsLspBuilder.setName(mplsName.getBytes());

        OfjCofActionMplsLspBuilder cofActionBld = new OfjCofActionMplsLspBuilder().setActionMplsLsp(actionMplsLspBuilder.build());
        ActionBuilder inputBld = new ActionBuilder().setActionChoice(cofActionBld.build());
        MplsCodec.getInstance().serialize(inputBld.build(), buffer);

        Assert.assertEquals(mplsBytes, ByteBufUtils.byteBufToHexString(buffer));
    }
}