/*
 * Copyright (c) 2013 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowjava.protocol.impl.util;

import io.netty.buffer.ByteBuf;

import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.opendaylight.openflowjava.protocol.api.extensibility.DeserializerRegistry;
import org.opendaylight.openflowjava.protocol.api.util.EncodeConstants;
import org.opendaylight.openflowjava.protocol.impl.deserialization.DeserializerRegistryImpl;
import org.opendaylight.openflowjava.util.ByteBufUtils;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.GroupCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.OutputActionCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.SetMplsTtlCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.SetQueueCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.actions.grouping.Action;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.instruction.rev130731.instruction.grouping.instruction.choice.ApplyActionsCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.instruction.rev130731.instruction.grouping.instruction.choice.ClearActionsCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.instruction.rev130731.instruction.grouping.instruction.choice.GotoTableCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.instruction.rev130731.instruction.grouping.instruction.choice.MeterCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.instruction.rev130731.instruction.grouping.instruction.choice.WriteActionsCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.instruction.rev130731.instruction.grouping.instruction.choice.WriteMetadataCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.instruction.rev130731.instructions.grouping.Instruction;

/**
 * @author michal.polkorab
 *
 */
public class InstructionsDeserializerTest {


    private DeserializerRegistry registry;

    /**
     * Initializes deserializer registry and lookups correct deserializer
     */
    @Before
    public void startUp() {
        registry = new DeserializerRegistryImpl();
        registry.init();
    }

    /**
     * Testing instructions translation
     */
    @Test
    public void test() {
        ByteBuf message = BufferHelper.buildBuffer("00 01 00 08 0A 00 00 00 00 02 00 18 00 00 00 00 "
                + "00 00 00 00 00 00 00 20 00 00 00 00 00 00 00 30 00 05 00 08 00 00 00 00 00 06 00 08 "
                + "00 01 02 03 00 03 00 20 00 00 00 00 00 00 00 10 00 00 00 25 00 35 00 00 00 00 00 00 "
                + "00 16 00 08 00 00 00 50 00 04 00 18 00 00 00 00 00 15 00 08 00 00 00 25 00 0F 00 08 05 00 00 00");

        message.skipBytes(4); // skip XID

        CodeKeyMaker keyMaker = CodeKeyMakerFactory.createInstructionsKeyMaker(EncodeConstants.OF13_VERSION_ID);
        List<Instruction> instructions = ListDeserializer.deserializeList(EncodeConstants.OF13_VERSION_ID,
                message.readableBytes(), message, keyMaker, registry);
        Instruction i1 = instructions.get(0);
        Assert.assertTrue("Wrong type - i1", i1.getInstructionChoice() instanceof GotoTableCase);
        Assert.assertEquals("Wrong table-id - i1", 10, ((GotoTableCase) i1.getInstructionChoice())
                .getGotoTable().getTableId().intValue());
        Instruction i2 = instructions.get(1);
        Assert.assertTrue("Wrong type - i2", i2.getInstructionChoice() instanceof WriteMetadataCase);
        Assert.assertArrayEquals("Wrong metadata - i2", ByteBufUtils.hexStringToBytes("00 00 00 00 00 00 00 20"),
                ((WriteMetadataCase) i2.getInstructionChoice()).getWriteMetadata().getMetadata());
        Assert.assertArrayEquals("Wrong metadata-mask - i2", ByteBufUtils.hexStringToBytes("00 00 00 00 00 00 00 30"),
                ((WriteMetadataCase) i2.getInstructionChoice()).getWriteMetadata().getMetadataMask());
        Instruction i3 = instructions.get(2);
        Assert.assertTrue("Wrong type - i3", i3.getInstructionChoice() instanceof ClearActionsCase);
        Instruction i4 = instructions.get(3);
        Assert.assertTrue("Wrong type - i4", i4.getInstructionChoice() instanceof MeterCase);
        Assert.assertEquals("Wrong meterId - i4", 66051, ((MeterCase) i4.getInstructionChoice())
                .getMeter().getMeterId().intValue());
        Instruction i5 = instructions.get(4);
        Assert.assertTrue("Wrong type - i5", i5.getInstructionChoice() instanceof WriteActionsCase);
        Assert.assertEquals("Wrong instructions - i5", 2, ((WriteActionsCase) i5.getInstructionChoice())
                .getWriteActions().getAction().size());
        Action action1 = ((WriteActionsCase) i5.getInstructionChoice()).getWriteActions().getAction().get(0);
        Assert.assertTrue("Wrong action", action1.getActionChoice() instanceof OutputActionCase);
        Assert.assertEquals("Wrong action", 37, ((OutputActionCase) action1.getActionChoice()).getOutputAction()
                .getPort().getValue().intValue());
        Assert.assertEquals("Wrong action", 53, ((OutputActionCase) action1.getActionChoice()).getOutputAction()
                .getMaxLength().intValue());
        Action action2 = ((WriteActionsCase) i5.getInstructionChoice()).getWriteActions().getAction().get(1);
        Assert.assertTrue("Wrong action", action2.getActionChoice() instanceof GroupCase);
        Assert.assertEquals("Wrong action", 80, ((GroupCase) action2.getActionChoice()).getGroupAction().getGroupId().intValue());
        Instruction i6 = instructions.get(5);
        Assert.assertTrue("Wrong type - i6", i6.getInstructionChoice() instanceof ApplyActionsCase);
        Assert.assertEquals("Wrong instructions - i6", 2, ((ApplyActionsCase) i6.getInstructionChoice())
                .getApplyActions().getAction().size());
        action1 = ((ApplyActionsCase) i6.getInstructionChoice()).getApplyActions().getAction().get(0);
        Assert.assertTrue("Wrong action", action1.getActionChoice() instanceof SetQueueCase);
        Assert.assertEquals("Wrong action", 37, ((SetQueueCase) action1.getActionChoice()).getSetQueueAction()
                .getQueueId().intValue());
        action2 = ((ApplyActionsCase) i6.getInstructionChoice()).getApplyActions().getAction().get(1);
        Assert.assertTrue("Wrong action", action2.getActionChoice() instanceof SetMplsTtlCase);
        Assert.assertEquals("Wrong action", 5, ((SetMplsTtlCase) action2.getActionChoice()).getSetMplsTtlAction()
                .getMplsTtl().shortValue());
    }

}
