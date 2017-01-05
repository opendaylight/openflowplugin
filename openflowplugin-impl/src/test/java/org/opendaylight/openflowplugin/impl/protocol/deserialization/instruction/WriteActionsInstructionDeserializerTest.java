/*
 * Copyright (c) 2016 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.impl.protocol.deserialization.instruction;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.opendaylight.openflowjava.protocol.api.extensibility.OFDeserializer;
import org.opendaylight.openflowjava.protocol.api.util.EncodeConstants;
import org.opendaylight.openflowjava.protocol.impl.util.ActionConstants;
import org.opendaylight.openflowjava.protocol.impl.util.InstructionConstants;
import org.opendaylight.openflowplugin.extension.api.path.ActionPath;
import org.opendaylight.openflowplugin.impl.protocol.deserialization.key.MessageCodeActionExperimenterKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.PopPbbActionCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.Instruction;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.instruction.WriteActionsCase;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.UnpooledByteBufAllocator;

public class WriteActionsInstructionDeserializerTest extends AbstractInstructionDeserializerTest {

    private OFDeserializer<Instruction> deserializer;

    @Override
    protected void init() {
        deserializer = getRegistry().getDeserializer(
                new MessageCodeActionExperimenterKey(EncodeConstants.OF13_VERSION_ID, getType(), Instruction.class,
                    ActionPath.NODES_NODE_TABLE_FLOW_INSTRUCTIONS_INSTRUCTION_WRITEACTIONSCASE_WRITEACTIONS_ACTION_ACTION_EXTENSIONLIST_EXTENSION,
                    null));
    }

    @Test
    public void testDeserialize() throws Exception {
        final ByteBuf in = UnpooledByteBufAllocator.DEFAULT.buffer();

        // Header
        final int startIndex = in.writerIndex();
        in.writeShort(getType());
        final int index = in.writerIndex();
        in.writeShort(getLength());
        in.writeZero(InstructionConstants.PADDING_IN_ACTIONS_INSTRUCTION);

        // POP PBB action
        in.writeShort(ActionConstants.POP_PBB_CODE);
        in.writeShort(ActionConstants.GENERAL_ACTION_LENGTH);
        in.writeZero(ActionConstants.PADDING_IN_ACTION_HEADER);

        in.setShort(index, in.writerIndex() - startIndex);

        final Instruction instruction = deserializer.deserialize(in);
        assertEquals(WriteActionsCase.class, instruction.getImplementedInterface());
        final WriteActionsCase actionCase = WriteActionsCase.class.cast(instruction);
        assertEquals(1, actionCase.getWriteActions().getAction().size());
        assertEquals(PopPbbActionCase.class, actionCase.getWriteActions().getAction().get(0)
                .getAction().getImplementedInterface());
        assertEquals(0, in.readableBytes());
    }

    @Override
    protected short getType() {
        return InstructionConstants.WRITE_ACTIONS_TYPE;
    }

    @Override
    protected short getLength() {
        return EncodeConstants.EMPTY_LENGTH;
    }

}
