/*
 * Copyright (c) 2016 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.impl.protocol.deserialization.messages;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.opendaylight.openflowjava.protocol.api.util.EncodeConstants;
import org.opendaylight.openflowjava.protocol.api.util.OxmMatchConstants;
import org.opendaylight.openflowjava.protocol.impl.util.ActionConstants;
import org.opendaylight.openflowjava.protocol.impl.util.InstructionConstants;
import org.opendaylight.openflowjava.util.ByteBufUtils;
import org.opendaylight.openflowplugin.impl.protocol.deserialization.AbstractDeserializerTest;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.PopPbbActionCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.FlowMessage;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.FlowModFlags;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.Instruction;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.instruction.ApplyActionsCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.FlowModCommand;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.UnpooledByteBufAllocator;

public class FlowMessageDeserializerTest extends AbstractDeserializerTest {

    private static final byte PADDING = 2;

    private static final int TYPE = 14;
    private static final int XID = 42;
    private static final FlowModCommand COMMAND = FlowModCommand.OFPFCADD;
    private static final int BUFFER_ID = 26;
    private static final int OUT_PORT = 22;
    private static final int OUT_GROUP = 25;

    private static final boolean SEND_FLOWREM = true;
    private static final boolean RESET_COUNTS = false;
    private static final boolean NO_PKTCOUNTS = true;
    private static final boolean NO_BYTCOUNTS = true;
    private static final boolean CHECK_OVERLAP = false;

    private static final FlowModFlags FLAGS = new FlowModFlags(
            CHECK_OVERLAP, NO_BYTCOUNTS, NO_PKTCOUNTS, RESET_COUNTS, SEND_FLOWREM);
    private static final long COOKIE = 12;
    private static final long COOKIE_MASK = 14;
    private static final int TABLE_ID = 2;
    private static final int IDLE_TIMEOUT = 20;
    private static final int HARD_TIMEOUT = 35;
    private static final int PRIORITY = 100;
    private static final int OXM_MATCH_TYPE_CODE = 1;
    private static final int MPLS_LABEL = 135;

    private ByteBuf buffer;

    @Override
    protected void init() {
        buffer = UnpooledByteBufAllocator.DEFAULT.buffer();
    }

    @Test
    public void deserialize() throws Exception {
        // Flow header
        buffer.writeByte(TYPE);
        buffer.writeShort(EncodeConstants.EMPTY_LENGTH);
        buffer.writeInt(XID);
        buffer.writeLong(COOKIE);
        buffer.writeLong(COOKIE_MASK);
        buffer.writeByte(TABLE_ID);
        buffer.writeByte(COMMAND.getIntValue());
        buffer.writeShort(IDLE_TIMEOUT);
        buffer.writeShort(HARD_TIMEOUT);
        buffer.writeShort(PRIORITY);
        buffer.writeInt(BUFFER_ID);
        buffer.writeInt(OUT_PORT);
        buffer.writeInt(OUT_GROUP);
        buffer.writeShort(ByteBufUtils.fillBitMask(0,
                    FLAGS.isSENDFLOWREM(),
                    FLAGS.isCHECKOVERLAP(),
                    FLAGS.isRESETCOUNTS(),
                    FLAGS.isNOPKTCOUNTS(),
                    FLAGS.isNOBYTCOUNTS()));
        buffer.writeZero(PADDING);

        // Match header
        int matchStartIndex = buffer.writerIndex();
        buffer.writeShort(OXM_MATCH_TYPE_CODE);
        int matchLengthIndex = buffer.writerIndex();
        buffer.writeShort(EncodeConstants.EMPTY_LENGTH);

        // MplsLabel match
        buffer.writeShort(OxmMatchConstants.OPENFLOW_BASIC_CLASS);
        buffer.writeByte(OxmMatchConstants.MPLS_LABEL << 1);
        buffer.writeByte(EncodeConstants.SIZE_OF_INT_IN_BYTES);
        buffer.writeInt(MPLS_LABEL);

        // Match footer
        int matchLength = buffer.writerIndex() - matchStartIndex;
        buffer.setShort(matchLengthIndex, matchLength);
        int paddingRemainder = matchLength % EncodeConstants.PADDING;
        if (paddingRemainder != 0) {
            buffer.writeZero(EncodeConstants.PADDING - paddingRemainder);
        }

        // Instructions header
        int instructionStartIndex = buffer.writerIndex();
        buffer.writeShort(InstructionConstants.APPLY_ACTIONS_TYPE);
        int instructionLengthIndex = buffer.writerIndex();
        buffer.writeShort(EncodeConstants.EMPTY_LENGTH);
        buffer.writeZero(InstructionConstants.PADDING_IN_ACTIONS_INSTRUCTION);

        // POP PBB action
        buffer.writeShort(ActionConstants.POP_PBB_CODE);
        buffer.writeShort(ActionConstants.GENERAL_ACTION_LENGTH);
        buffer.writeZero(ActionConstants.PADDING_IN_ACTION_HEADER);

        // Count total length of instructions
        buffer.setShort(instructionLengthIndex, buffer.writerIndex() - instructionStartIndex);

        // Deserialize and check everything
        final FlowMessage message = (FlowMessage) getFactory()
            .deserialize(buffer, EncodeConstants.OF13_VERSION_ID);

        assertEquals(XID, message.getXid().intValue());
        assertEquals(COMMAND.getIntValue(), message.getCommand().getIntValue());
        assertEquals(MPLS_LABEL, message.getMatch().getProtocolMatchFields().getMplsLabel().intValue());
        assertEquals(1, message.getInstructions().getInstruction().size());

        final Instruction instruction = message.getInstructions().getInstruction().get(0).getInstruction();
        assertEquals(ApplyActionsCase.class, instruction.getImplementedInterface());

        final ApplyActionsCase applyActions = ApplyActionsCase.class.cast(instruction);
        assertEquals(1, applyActions.getApplyActions().getAction().size());
        assertEquals(PopPbbActionCase.class, applyActions.getApplyActions().getAction().get(0)
                .getAction().getImplementedInterface());
    }

}
