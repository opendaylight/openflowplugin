/*
 * Copyright (c) 2014 Pantheon Technologies s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowjava.protocol.impl.deserialization.instruction;

import io.netty.buffer.ByteBuf;

import org.junit.Assert;
import org.junit.Test;
import org.opendaylight.openflowjava.util.ByteBufUtils;
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
public class AbstractInstructionDeserializerTest {

    /**
     * Tests {@link AbstractInstructionDeserializer#deserializeHeader(ByteBuf)} with different
     * instruction types
     */
    @Test
    public void test() {
        ByteBuf buffer = ByteBufUtils.hexStringToByteBuf("00 01 00 04");
        Instruction instruction = new GoToTableInstructionDeserializer().deserializeHeader(buffer);
        Assert.assertTrue("Wrong type", instruction.getInstructionChoice() instanceof GotoTableCase);

        buffer = ByteBufUtils.hexStringToByteBuf("00 02 00 04");
        instruction = new WriteMetadataInstructionDeserializer().deserializeHeader(buffer);
        Assert.assertTrue("Wrong type", instruction.getInstructionChoice() instanceof WriteMetadataCase);

        buffer = ByteBufUtils.hexStringToByteBuf("00 03 00 04");
        instruction = new WriteActionsInstructionDeserializer().deserializeHeader(buffer);
        Assert.assertTrue("Wrong type", instruction.getInstructionChoice() instanceof WriteActionsCase);

        buffer = ByteBufUtils.hexStringToByteBuf("00 04 00 04");
        instruction = new ApplyActionsInstructionDeserializer().deserializeHeader(buffer);
        Assert.assertTrue("Wrong type", instruction.getInstructionChoice() instanceof ApplyActionsCase);

        buffer = ByteBufUtils.hexStringToByteBuf("00 05 00 04");
        instruction = new ClearActionsInstructionDeserializer().deserializeHeader(buffer);
        Assert.assertTrue("Wrong type", instruction.getInstructionChoice() instanceof ClearActionsCase);

        buffer = ByteBufUtils.hexStringToByteBuf("00 06 00 04");
        instruction = new MeterInstructionDeserializer().deserializeHeader(buffer);
        Assert.assertTrue("Wrong type", instruction.getInstructionChoice() instanceof MeterCase);
    }
}