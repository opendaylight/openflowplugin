/*
 * Copyright (c) 2016 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.protocol.deserialization.instruction;

import static org.junit.Assert.assertEquals;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.UnpooledByteBufAllocator;
import org.junit.Test;
import org.opendaylight.openflowjava.protocol.api.extensibility.OFDeserializer;
import org.opendaylight.openflowjava.protocol.api.util.EncodeConstants;
import org.opendaylight.openflowjava.protocol.impl.util.ActionConstants;
import org.opendaylight.openflowjava.protocol.impl.util.InstructionConstants;
import org.opendaylight.openflowplugin.extension.api.path.ActionPath;
import org.opendaylight.openflowplugin.impl.protocol.deserialization.key.MessageCodeActionExperimenterKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.PopPbbActionCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.Instruction;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.instruction.ApplyActionsCase;

public class ApplyActionsInstructionDeserializerTest extends AbstractInstructionDeserializerTest {

    private OFDeserializer<Instruction> deserializer;

    @Override
    protected void init() {
        deserializer = getRegistry().getDeserializer(
                new MessageCodeActionExperimenterKey(EncodeConstants.OF_VERSION_1_3, getType(), Instruction.class,
                        ActionPath.INVENTORY_FLOWNODE_TABLE_APPLY_ACTIONS,
                        null));
    }

    @Test
    public void testDeserialize() {
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
        assertEquals(ApplyActionsCase.class, instruction.implementedInterface());
        final ApplyActionsCase actionCase = (ApplyActionsCase) instruction;
        assertEquals(1, actionCase.getApplyActions().getAction().size());
        assertEquals(PopPbbActionCase.class, actionCase.getApplyActions().nonnullAction().values().iterator().next()
                .getAction().implementedInterface());
        assertEquals(0, in.readableBytes());
    }

    @Override
    protected short getType() {
        return InstructionConstants.APPLY_ACTIONS_TYPE;
    }

    @Override
    protected short getLength() {
        return EncodeConstants.EMPTY_LENGTH;
    }
}
