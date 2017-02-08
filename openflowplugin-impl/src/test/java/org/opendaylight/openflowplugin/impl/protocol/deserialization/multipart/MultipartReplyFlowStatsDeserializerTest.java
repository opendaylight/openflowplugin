/**
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.impl.protocol.deserialization.multipart;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.UnpooledByteBufAllocator;
import org.junit.Test;
import org.opendaylight.openflowjava.protocol.api.util.EncodeConstants;
import org.opendaylight.openflowjava.protocol.api.util.OxmMatchConstants;
import org.opendaylight.openflowjava.protocol.impl.util.ActionConstants;
import org.opendaylight.openflowjava.protocol.impl.util.InstructionConstants;
import org.opendaylight.openflowjava.util.ByteBufUtils;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.PopPbbActionCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.PopVlanActionCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.statistics.rev130819.flow.and.statistics.map.list.FlowAndStatisticsMapList;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.statistics.rev130819.multipart.reply.multipart.reply.body.MultipartReplyFlowStats;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.FlowModFlags;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.Instruction;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.instruction.ApplyActionsCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.instruction.WriteActionsCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.MultipartType;

public class MultipartReplyFlowStatsDeserializerTest extends AbstractMultipartDeserializerTest{

    private static final byte PADDING_IN_FLOW_STATS_HEADER_01 = 1;
    private static final byte PADDING_IN_FLOW_STATS_HEADER_02 = 4;

    private static  final short ITEM_LENGTH = 96;
    private static  final byte TABLE_ID = 1;
    private static  final int SECOND = 1;
    private static  final int NANOSECOND = 2;
    private static  final short PRIORITY = 2;
    private static  final short IDLE_TIMEOUT = 3;
    private static  final short HARD_TIMEOUT = 4;
    private static final boolean SEND_FLOWREM = true;
    private static final boolean RESET_COUNTS = false;
    private static final boolean NO_PKTCOUNTS = true;
    private static final boolean NO_BYTCOUNTS = true;
    private static final boolean CHECK_OVERLAP = false;
    private static final FlowModFlags FLAGS = new FlowModFlags(
            CHECK_OVERLAP, NO_BYTCOUNTS, NO_PKTCOUNTS, RESET_COUNTS, SEND_FLOWREM);
    private static  final long COOKIE = 2;
    private static  final long PACKET_COUNT = 4;
    private static  final long BYTE_COUNT = 5;

    private static final int OXM_MATCH_TYPE_CODE = 1;
    private static final int MPLS_LABEL = 135;

    @Test
    public void testDeserialize() throws Exception {

        ByteBuf buffer = UnpooledByteBufAllocator.DEFAULT.buffer();

        buffer.writeShort(ITEM_LENGTH);
        buffer.writeByte(TABLE_ID);
        buffer.writeZero(PADDING_IN_FLOW_STATS_HEADER_01);
        buffer.writeInt(SECOND);
        buffer.writeInt(NANOSECOND);
        buffer.writeShort(PRIORITY);
        buffer.writeShort(IDLE_TIMEOUT);
        buffer.writeShort(HARD_TIMEOUT);
        buffer.writeShort(ByteBufUtils.fillBitMask(0,
                FLAGS.isSENDFLOWREM(),
                FLAGS.isCHECKOVERLAP(),
                FLAGS.isRESETCOUNTS(),
                FLAGS.isNOPKTCOUNTS(),
                FLAGS.isNOBYTCOUNTS()));
        buffer.writeZero(PADDING_IN_FLOW_STATS_HEADER_02);
        buffer.writeLong(COOKIE);
        buffer.writeLong(PACKET_COUNT);
        buffer.writeLong(BYTE_COUNT);

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

        // Instruction POP PBB header
        int instructionStartIndex = buffer.writerIndex();
        buffer.writeShort(InstructionConstants.APPLY_ACTIONS_TYPE);
        int instructionLengthIndex = buffer.writerIndex();
        buffer.writeShort(EncodeConstants.EMPTY_LENGTH);
        buffer.writeZero(InstructionConstants.PADDING_IN_ACTIONS_INSTRUCTION);

        // POP PBB action
        buffer.writeShort(ActionConstants.POP_PBB_CODE);
        buffer.writeShort(ActionConstants.GENERAL_ACTION_LENGTH);
        buffer.writeZero(ActionConstants.PADDING_IN_ACTION_HEADER);

        // Count total length of instruction
        buffer.setShort(instructionLengthIndex, buffer.writerIndex() - instructionStartIndex);

        // Instruction POP Vlan header
        instructionStartIndex = buffer.writerIndex();
        buffer.writeShort(InstructionConstants.WRITE_ACTIONS_TYPE);
        instructionLengthIndex = buffer.writerIndex();
        buffer.writeShort(EncodeConstants.EMPTY_LENGTH);
        buffer.writeZero(InstructionConstants.PADDING_IN_ACTIONS_INSTRUCTION);

        // POP Vlan action
        buffer.writeShort(ActionConstants.POP_VLAN_CODE);
        buffer.writeShort(ActionConstants.GENERAL_ACTION_LENGTH);
        buffer.writeZero(ActionConstants.PADDING_IN_ACTION_HEADER);

        // Count total length of instruction
        buffer.setShort(instructionLengthIndex, buffer.writerIndex() - instructionStartIndex);

        final MultipartReplyFlowStats reply = (MultipartReplyFlowStats) deserializeMultipart(buffer);
        final FlowAndStatisticsMapList flowAndStatisticsMapList = reply.getFlowAndStatisticsMapList().get(0);
        assertEquals(TABLE_ID, flowAndStatisticsMapList.getTableId().shortValue());
        assertEquals(SECOND, flowAndStatisticsMapList.getDuration().getSecond().getValue().intValue());
        assertEquals(NANOSECOND, flowAndStatisticsMapList.getDuration().getNanosecond().getValue().intValue());
        assertEquals(PRIORITY, flowAndStatisticsMapList.getPriority().intValue());
        assertEquals(IDLE_TIMEOUT, flowAndStatisticsMapList.getIdleTimeout().intValue());
        assertEquals(HARD_TIMEOUT, flowAndStatisticsMapList.getHardTimeout().intValue());
        assertTrue(flowAndStatisticsMapList.getFlags().equals(FLAGS));
        assertEquals(COOKIE, flowAndStatisticsMapList.getCookie().getValue().longValue());
        assertEquals(BYTE_COUNT, flowAndStatisticsMapList.getByteCount().getValue().longValue());
        assertEquals(PACKET_COUNT, flowAndStatisticsMapList.getPacketCount().getValue().longValue());

        assertEquals(2, flowAndStatisticsMapList.getInstructions().getInstruction().size());

        final Instruction instruction = flowAndStatisticsMapList.getInstructions().getInstruction().get(0).getInstruction();
        assertEquals(ApplyActionsCase.class, instruction.getImplementedInterface());

        final ApplyActionsCase applyActions = ApplyActionsCase.class.cast(instruction);
        assertEquals(1, applyActions.getApplyActions().getAction().size());
        assertEquals(PopPbbActionCase.class, applyActions.getApplyActions().getAction().get(0)
                .getAction().getImplementedInterface());

        final Instruction instruction1 = flowAndStatisticsMapList.getInstructions().getInstruction().get(1).getInstruction();
        assertEquals(WriteActionsCase.class, instruction1.getImplementedInterface());

        final WriteActionsCase writeActions = WriteActionsCase.class.cast(instruction1);
        assertEquals(1, writeActions.getWriteActions().getAction().size());
        assertEquals(PopVlanActionCase.class, writeActions.getWriteActions().getAction().get(0)
                .getAction().getImplementedInterface());
    }

    @Override
    protected int getType() {
        return MultipartType.OFPMPFLOW.getIntValue();
    }
}
