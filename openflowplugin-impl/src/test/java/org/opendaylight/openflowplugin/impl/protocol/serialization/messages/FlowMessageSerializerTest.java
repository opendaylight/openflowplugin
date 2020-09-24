/*
 * Copyright (c) 2016 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.protocol.serialization.messages;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.UnpooledByteBufAllocator;
import java.util.Arrays;
import java.util.Collections;
import org.junit.Test;
import org.opendaylight.openflowjava.protocol.api.keys.MessageTypeKey;
import org.opendaylight.openflowjava.protocol.api.util.EncodeConstants;
import org.opendaylight.openflowjava.protocol.api.util.OxmMatchConstants;
import org.opendaylight.openflowjava.protocol.impl.util.ActionConstants;
import org.opendaylight.openflowjava.protocol.impl.util.InstructionConstants;
import org.opendaylight.openflowjava.util.ByteBufUtils;
import org.opendaylight.openflowplugin.impl.protocol.serialization.AbstractSerializerTest;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.PortNumber;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.SetTpDstActionCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.SetTpSrcActionCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.SetVlanIdActionCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.set.tp.dst.action._case.SetTpDstActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.set.tp.src.action._case.SetTpSrcActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.set.vlan.id.action._case.SetVlanIdActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.list.ActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.list.ActionKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.FlowCookie;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.FlowMessage;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.FlowMessageBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.FlowModFlags;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.Instructions;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.InstructionsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.Match;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.MatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.instruction.ApplyActionsCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.instruction.apply.actions._case.ApplyActionsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.list.InstructionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.list.InstructionKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.l2.types.rev130827.VlanId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.IpMatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.FlowModCommand;
import org.opendaylight.yangtools.yang.common.Uint16;
import org.opendaylight.yangtools.yang.common.Uint32;
import org.opendaylight.yangtools.yang.common.Uint64;
import org.opendaylight.yangtools.yang.common.Uint8;

public class FlowMessageSerializerTest extends AbstractSerializerTest {
    private static final byte PADDING_IN_FLOW_MOD_MESSAGE = 2;

    private static final Uint32 XID = Uint32.valueOf(42);
    private static final Uint8 VERSION = EncodeConstants.OF_VERSION_1_3;
    private static final Uint8 TABLE_ID = Uint8.TWO;
    private static final Boolean STRICT = true;
    private static final Uint16 PRIORITY = Uint16.TEN;
    private static final FlowModCommand COMMAND = FlowModCommand.OFPFCADD;
    private static final Boolean BARRIER = false;
    private static final Uint32 BUFFER_ID = Uint32.valueOf(12);
    private static final String CONTAINER_NAME = "openflow:1";
    private static final FlowCookie COOKIE = new FlowCookie(Uint64.ONE);
    private static final FlowCookie COOKIE_MASK = new FlowCookie(Uint64.ZERO);
    private static final String FLOW_NAME = "flowflow";
    private static final Uint16 HARD_TIMEOUT = Uint16.TEN;
    private static final Uint16 IDLE_TIMEOUT = Uint16.valueOf(5);
    private static final Boolean INSTALL_HW = true;
    private static final Uint32 OUT_GROUP = Uint32.ONE;
    private static final Uint64 OUT_PORT = Uint64.TEN;


    private static final Boolean IS_CHECKOVERLAP = true;
    private static final Boolean IS_NOBYTCOUNTS = false;
    private static final Boolean IS_NOPKTCOUNTS = false;
    private static final Boolean IS_RESETCOUNTS = true;
    private static final Boolean IS_SENDFLOWREM = false;
    private static final FlowModFlags FLAGS = new FlowModFlags(
            IS_CHECKOVERLAP,
            IS_NOBYTCOUNTS,
            IS_NOPKTCOUNTS,
            IS_RESETCOUNTS,
            IS_SENDFLOWREM);

    private static final Uint16 VLAN_ID = Uint16.ONE;
    private static final Uint8 IP_PROTOCOL = Uint8.valueOf(6); // TCP

    private static final Uint16 TP_SRC_PORT = Uint16.valueOf(22);
    private static final Uint16 TP_DST_PORT = Uint16.valueOf(23);
    private static final Instructions INSTRUCTIONS = new InstructionsBuilder()
            .setInstruction(Arrays.asList(
                    new InstructionBuilder()
                            .setOrder(0)
                            .withKey(new InstructionKey(0))
                            .setInstruction(new ApplyActionsCaseBuilder()
                                    .setApplyActions(new ApplyActionsBuilder()
                                            .setAction(Collections.singletonList(new ActionBuilder()
                                                    .setOrder(0)
                                                    .withKey(new ActionKey(0))
                                                    .setAction(new SetVlanIdActionCaseBuilder()
                                                            .setSetVlanIdAction(new SetVlanIdActionBuilder()
                                                                    .setVlanId(new VlanId(VLAN_ID))
                                                                    .build())
                                                            .build())
                                                    .build()))
                                            .build())
                                    .build())
                            .build(),
                    new InstructionBuilder()
                            .setOrder(2)
                            .withKey(new InstructionKey(2))
                            .setInstruction(new ApplyActionsCaseBuilder()
                                    .setApplyActions(new ApplyActionsBuilder()
                                            .setAction(Collections.singletonList(new ActionBuilder()
                                                    .setOrder(1)
                                                    .withKey(new ActionKey(1))
                                                    .setAction(new SetTpDstActionCaseBuilder()
                                                            .setSetTpDstAction(new SetTpDstActionBuilder()
                                                                    .setIpProtocol(IP_PROTOCOL)
                                                                    .setPort(new PortNumber(TP_DST_PORT))
                                                                    .build())
                                                            .build())
                                                    .build()))
                                            .build())
                                    .build())
                            .build(),
                    new InstructionBuilder()
                            .setOrder(1)
                            .withKey(new InstructionKey(1))
                            .setInstruction(new ApplyActionsCaseBuilder()
                                    .setApplyActions(new ApplyActionsBuilder()
                                            .setAction(Collections.singletonList(new ActionBuilder()
                                                    .setOrder(2)
                                                    .withKey(new ActionKey(2))
                                                    .setAction(new SetTpSrcActionCaseBuilder()
                                                            .setSetTpSrcAction(new SetTpSrcActionBuilder()
                                                                    .setIpProtocol(IP_PROTOCOL)
                                                                    .setPort(new PortNumber(TP_SRC_PORT))
                                                                    .build())
                                                            .build())
                                                    .build()))
                                            .build())
                                    .build())
                            .build()))
            .build();

    private static final Uint8 IP_PROTOCOL_MATCH = Uint8.valueOf(17);
    private static final Match MATCH = new MatchBuilder()
            .setIpMatch(new IpMatchBuilder()
                    .setIpProtocol(IP_PROTOCOL_MATCH)
                    .build())
            .build();

    private static final FlowMessage MESSAGE = new FlowMessageBuilder()
            .setXid(XID)
            .setVersion(VERSION)
            .setTableId(TABLE_ID)
            .setStrict(STRICT)
            .setPriority(PRIORITY)
            .setCommand(COMMAND)
            .setBarrier(BARRIER)
            .setBufferId(BUFFER_ID)
            .setContainerName(CONTAINER_NAME)
            .setCookie(COOKIE)
            .setCookieMask(COOKIE_MASK)
            .setFlags(FLAGS)
            .setFlowName(FLOW_NAME)
            .setHardTimeout(HARD_TIMEOUT)
            .setIdleTimeout(IDLE_TIMEOUT)
            .setInstallHw(INSTALL_HW)
            .setOutGroup(OUT_GROUP)
            .setOutPort(OUT_PORT)
            .setInstructions(INSTRUCTIONS)
            .setMatch(MATCH)
            .build();

    private FlowMessageSerializer serializer;

    @Override
    protected void init() {
        serializer = getRegistry()
                .getSerializer(new MessageTypeKey<>(EncodeConstants.OF13_VERSION_ID, FlowMessage.class));
    }

    @Test
    public void testSerialize() {
        final ByteBuf out = UnpooledByteBufAllocator.DEFAULT.buffer();
        serializer.serialize(MESSAGE, out);

        // Our message was split to 2 flows because it contained set_vlan_id action
        testVlanFalse(out);
        testVlanTrue(out);
        assertEquals(out.readableBytes(), 0);
    }

    private void testVlanFalse(final ByteBuf out) {
        // Header
        assertEquals(out.readByte(), VERSION.shortValue());
        assertEquals(out.readByte(), serializer.getMessageType());
        assertEquals(out.readUnsignedShort(), 144);
        assertEquals(out.readInt(), XID.intValue());

        // Body
        assertEquals(out.readLong(), COOKIE.getValue().longValue());
        assertEquals(out.readLong(), COOKIE_MASK.getValue().longValue());
        assertEquals(out.readUnsignedByte(), TABLE_ID.shortValue());
        assertEquals(out.readUnsignedByte(), COMMAND.getIntValue());
        assertEquals(out.readUnsignedShort(), IDLE_TIMEOUT.intValue());
        assertEquals(out.readUnsignedShort(), HARD_TIMEOUT.intValue());
        assertEquals(out.readUnsignedShort(), PRIORITY.intValue());
        assertEquals(out.readUnsignedInt(), BUFFER_ID.longValue());
        assertEquals(out.readUnsignedInt(), OUT_PORT.longValue());
        assertEquals(out.readUnsignedInt(), OUT_GROUP.longValue());
        assertEquals(out.readUnsignedShort(), ByteBufUtils.fillBitMask(0,
                IS_SENDFLOWREM,
                IS_CHECKOVERLAP,
                IS_RESETCOUNTS,
                IS_NOPKTCOUNTS,
                IS_NOBYTCOUNTS));
        out.skipBytes(PADDING_IN_FLOW_MOD_MESSAGE);

        // Body match
        int matchLength = 15;
        assertEquals(out.readShort(), 1); // OXM match type
        assertEquals(out.readUnsignedShort(), matchLength); // OXM match length

         // Vlan false match
        assertEquals(out.readUnsignedShort(), OxmMatchConstants.OPENFLOW_BASIC_CLASS);
        assertEquals(out.readUnsignedByte(), OxmMatchConstants.VLAN_VID << 1);
        assertEquals(out.readUnsignedByte(), Short.BYTES);
        assertEquals(out.readUnsignedShort(), 0);

        // Ip proto match
        assertEquals(out.readUnsignedShort(), OxmMatchConstants.OPENFLOW_BASIC_CLASS);
        assertEquals(out.readUnsignedByte(), OxmMatchConstants.IP_PROTO << 1);
        assertEquals(out.readUnsignedByte(), Byte.BYTES);
        assertEquals(out.readUnsignedByte(), IP_PROTOCOL_MATCH.shortValue());

        int paddingRemainder = matchLength % EncodeConstants.PADDING;
        if (paddingRemainder != 0) {
            out.skipBytes(EncodeConstants.PADDING - paddingRemainder);
        }

        // Apply actions instruction
        int applyActionsLength = 32;
        assertEquals(out.readUnsignedShort(), InstructionConstants.APPLY_ACTIONS_TYPE);
        assertEquals(out.readUnsignedShort(), applyActionsLength); // length of actions
        out.skipBytes(InstructionConstants.PADDING_IN_ACTIONS_INSTRUCTION);

        // Push vlan action (was injected because we had setvlanid action)
        assertEquals(out.readUnsignedShort(), ActionConstants.PUSH_VLAN_CODE);
        assertEquals(out.readUnsignedShort(), ActionConstants.GENERAL_ACTION_LENGTH);
        assertEquals(out.readUnsignedShort(), 0x8100);
        out.skipBytes(ActionConstants.ETHERTYPE_ACTION_PADDING);

        // Set vlan id action
        int setVlanIdLength = 16;
        final int setVlanStartIndex = out.readerIndex();
        assertEquals(out.readUnsignedShort(), ActionConstants.SET_FIELD_CODE);
        assertEquals(out.readUnsignedShort(), setVlanIdLength);
        assertEquals(out.readUnsignedShort(), OxmMatchConstants.OPENFLOW_BASIC_CLASS);
        assertEquals(out.readUnsignedByte(), OxmMatchConstants.VLAN_VID << 1);
        assertEquals(out.readUnsignedByte(), Short.BYTES);
        assertEquals(out.readUnsignedShort(), VLAN_ID.toJava() | 1 << 12);

        paddingRemainder = (out.readerIndex() - setVlanStartIndex) % EncodeConstants.PADDING;
        if (paddingRemainder != 0) {
            out.skipBytes(EncodeConstants.PADDING - paddingRemainder);
        }

        // Apply actions instruction 2
        int applyActionsLength2 = 24;
        assertEquals(out.readUnsignedShort(), InstructionConstants.APPLY_ACTIONS_TYPE);
        assertEquals(out.readUnsignedShort(), applyActionsLength2); // length of actions
        out.skipBytes(InstructionConstants.PADDING_IN_ACTIONS_INSTRUCTION);

        // Set tp src action
        int setTpSrcLength = 16;
        final int setTpSrcStartIndex = out.readerIndex();
        assertEquals(out.readUnsignedShort(), ActionConstants.SET_FIELD_CODE);
        assertEquals(out.readUnsignedShort(), setTpSrcLength);
        assertEquals(out.readUnsignedShort(), OxmMatchConstants.OPENFLOW_BASIC_CLASS);
        assertEquals(out.readUnsignedByte(), OxmMatchConstants.UDP_SRC << 1);
        assertEquals(out.readUnsignedByte(), Short.BYTES);
        assertEquals(out.readUnsignedShort(), TP_SRC_PORT.intValue());

        paddingRemainder = (out.readerIndex() - setTpSrcStartIndex) % EncodeConstants.PADDING;
        if (paddingRemainder != 0) {
            out.skipBytes(EncodeConstants.PADDING - paddingRemainder);
        }

        // Apply actions instruction 3
        int applyActionsLength3 = 24;
        assertEquals(out.readUnsignedShort(), InstructionConstants.APPLY_ACTIONS_TYPE);
        assertEquals(out.readUnsignedShort(), applyActionsLength3); // length of actions
        out.skipBytes(InstructionConstants.PADDING_IN_ACTIONS_INSTRUCTION);

        // Set tp dst action
        int setTpDstLength = 16;
        final int setTpDstStartIndex = out.readerIndex();
        assertEquals(out.readUnsignedShort(), ActionConstants.SET_FIELD_CODE);
        assertEquals(out.readUnsignedShort(), setTpDstLength);
        assertEquals(out.readUnsignedShort(), OxmMatchConstants.OPENFLOW_BASIC_CLASS);
        assertEquals(out.readUnsignedByte(), OxmMatchConstants.UDP_DST << 1);
        assertEquals(out.readUnsignedByte(), Short.BYTES);
        assertEquals(out.readUnsignedShort(), TP_DST_PORT.intValue());

        paddingRemainder = (out.readerIndex() - setTpDstStartIndex) % EncodeConstants.PADDING;
        if (paddingRemainder != 0) {
            out.skipBytes(EncodeConstants.PADDING - paddingRemainder);
        }
    }

    private void testVlanTrue(final ByteBuf out) {
        // VLAN_TRUE flow

        // Header
        assertEquals(out.readByte(), VERSION.shortValue());
        assertEquals(out.readByte(), serializer.getMessageType());
        assertEquals(out.readUnsignedShort(), 144);
        assertEquals(out.readInt(), XID.intValue());

        // Body
        assertEquals(out.readLong(), COOKIE.getValue().longValue());
        assertEquals(out.readLong(), COOKIE_MASK.getValue().longValue());
        assertEquals(out.readUnsignedByte(), TABLE_ID.shortValue());
        assertEquals(out.readUnsignedByte(), COMMAND.getIntValue());
        assertEquals(out.readUnsignedShort(), IDLE_TIMEOUT.intValue());
        assertEquals(out.readUnsignedShort(), HARD_TIMEOUT.intValue());
        assertEquals(out.readUnsignedShort(), PRIORITY.intValue());
        assertEquals(out.readUnsignedInt(), BUFFER_ID.longValue());
        assertEquals(out.readUnsignedInt(), OUT_PORT.longValue());
        assertEquals(out.readUnsignedInt(), OUT_GROUP.longValue());
        assertEquals(out.readUnsignedShort(), ByteBufUtils.fillBitMask(0,
                IS_SENDFLOWREM,
                IS_CHECKOVERLAP,
                IS_RESETCOUNTS,
                IS_NOPKTCOUNTS,
                IS_NOBYTCOUNTS));
        out.skipBytes(PADDING_IN_FLOW_MOD_MESSAGE);

        // Body match
        int matchLength = 17;
        assertEquals(out.readShort(), 1); // OXM match type
        assertEquals(out.readUnsignedShort(), matchLength); // OXM match length

         // Vlan false match
        assertEquals(out.readUnsignedShort(), OxmMatchConstants.OPENFLOW_BASIC_CLASS);
        assertEquals(out.readUnsignedByte(), OxmMatchConstants.VLAN_VID << 1 | 1);
        assertEquals(out.readUnsignedByte(), Short.BYTES * 2);
        assertEquals(out.readUnsignedShort(), 1 << 12);
        byte[] vlanMask = new byte[2];
        out.readBytes(vlanMask);
        assertArrayEquals(vlanMask, new byte[] { 16, 0 });

        // Ip proto match
        assertEquals(out.readUnsignedShort(), OxmMatchConstants.OPENFLOW_BASIC_CLASS);
        assertEquals(out.readUnsignedByte(), OxmMatchConstants.IP_PROTO << 1);
        assertEquals(out.readUnsignedByte(), Byte.BYTES);
        assertEquals(out.readUnsignedByte(), IP_PROTOCOL_MATCH.shortValue());

        int paddingRemainder = matchLength % EncodeConstants.PADDING;
        if (paddingRemainder != 0) {
            out.skipBytes(EncodeConstants.PADDING - paddingRemainder);
        }

        // Apply actions instruction
        int applyActionsLength = 24;
        assertEquals(out.readUnsignedShort(), InstructionConstants.APPLY_ACTIONS_TYPE);
        assertEquals(out.readUnsignedShort(), applyActionsLength); // length of actions
        out.skipBytes(InstructionConstants.PADDING_IN_ACTIONS_INSTRUCTION);

        // Set vlan id action
        int setVlanIdLength = 16;
        final int setVlanStartIndex = out.readerIndex();
        assertEquals(out.readUnsignedShort(), ActionConstants.SET_FIELD_CODE);
        assertEquals(out.readUnsignedShort(), setVlanIdLength);
        assertEquals(out.readUnsignedShort(), OxmMatchConstants.OPENFLOW_BASIC_CLASS);
        assertEquals(out.readUnsignedByte(), OxmMatchConstants.VLAN_VID << 1);
        assertEquals(out.readUnsignedByte(), Short.BYTES);
        assertEquals(out.readUnsignedShort(), VLAN_ID.toJava() | 1 << 12);

        paddingRemainder = (out.readerIndex() - setVlanStartIndex) % EncodeConstants.PADDING;
        if (paddingRemainder != 0) {
            out.skipBytes(EncodeConstants.PADDING - paddingRemainder);
        }

        // Apply actions instruction 2
        int applyActionsLength2 = 24;
        assertEquals(out.readUnsignedShort(), InstructionConstants.APPLY_ACTIONS_TYPE);
        assertEquals(out.readUnsignedShort(), applyActionsLength2); // length of actions
        out.skipBytes(InstructionConstants.PADDING_IN_ACTIONS_INSTRUCTION);

        // Set tp src action
        int setTpSrcLength = 16;
        final int setTpSrcStartIndex = out.readerIndex();
        assertEquals(out.readUnsignedShort(), ActionConstants.SET_FIELD_CODE);
        assertEquals(out.readUnsignedShort(), setTpSrcLength);
        assertEquals(out.readUnsignedShort(), OxmMatchConstants.OPENFLOW_BASIC_CLASS);
        assertEquals(out.readUnsignedByte(), OxmMatchConstants.UDP_SRC << 1);
        assertEquals(out.readUnsignedByte(), Short.BYTES);
        assertEquals(out.readUnsignedShort(), TP_SRC_PORT.intValue());

        paddingRemainder = (out.readerIndex() - setTpSrcStartIndex) % EncodeConstants.PADDING;
        if (paddingRemainder != 0) {
            out.skipBytes(EncodeConstants.PADDING - paddingRemainder);
        }

        // Apply actions instruction 3
        int applyActionsLength3 = 24;
        assertEquals(out.readUnsignedShort(), InstructionConstants.APPLY_ACTIONS_TYPE);
        assertEquals(out.readUnsignedShort(), applyActionsLength3); // length of actions
        out.skipBytes(InstructionConstants.PADDING_IN_ACTIONS_INSTRUCTION);

        // Set tp dst action
        int setTpDstLength = 16;
        final int setTpDstStartIndex = out.readerIndex();
        assertEquals(out.readUnsignedShort(), ActionConstants.SET_FIELD_CODE);
        assertEquals(out.readUnsignedShort(), setTpDstLength);
        assertEquals(out.readUnsignedShort(), OxmMatchConstants.OPENFLOW_BASIC_CLASS);
        assertEquals(out.readUnsignedByte(), OxmMatchConstants.UDP_DST << 1);
        assertEquals(out.readUnsignedByte(), Short.BYTES);
        assertEquals(out.readUnsignedShort(), TP_DST_PORT.intValue());

        paddingRemainder = (out.readerIndex() - setTpDstStartIndex) % EncodeConstants.PADDING;
        if (paddingRemainder != 0) {
            out.skipBytes(EncodeConstants.PADDING - paddingRemainder);
        }
    }

}