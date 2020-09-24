/*
 * Copyright (c) 2013 Pantheon Technologies s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowjava.protocol.impl.serialization.factories;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.UnpooledByteBufAllocator;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.opendaylight.openflowjava.protocol.api.extensibility.OFSerializer;
import org.opendaylight.openflowjava.protocol.api.extensibility.SerializerRegistry;
import org.opendaylight.openflowjava.protocol.api.keys.MessageTypeKey;
import org.opendaylight.openflowjava.protocol.api.util.EncodeConstants;
import org.opendaylight.openflowjava.protocol.impl.serialization.SerializerRegistryImpl;
import org.opendaylight.openflowjava.protocol.impl.util.BufferHelper;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.OutputActionCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.output.action._case.OutputActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.actions.grouping.Action;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.actions.grouping.ActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.instruction.rev130731.instruction.grouping.instruction.choice.ApplyActionsCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.instruction.rev130731.instruction.grouping.instruction.choice.GotoTableCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.instruction.rev130731.instruction.grouping.instruction.choice.WriteMetadataCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.instruction.rev130731.instruction.grouping.instruction.choice._goto.table._case.GotoTableBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.instruction.rev130731.instruction.grouping.instruction.choice.apply.actions._case.ApplyActionsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.instruction.rev130731.instruction.grouping.instruction.choice.write.metadata._case.WriteMetadataBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.instruction.rev130731.instructions.grouping.Instruction;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.instruction.rev130731.instructions.grouping.InstructionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.FlowModCommand;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.FlowModFlags;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.PortNumber;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.TableId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.InPhyPort;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.IpEcn;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.OpenflowBasicClass;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.OxmMatchType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entries.grouping.MatchEntry;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entries.grouping.MatchEntryBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.InPhyPortCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.IpEcnCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.in.phy.port._case.InPhyPortBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.ip.ecn._case.IpEcnBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.grouping.MatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.FlowModInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.FlowModInputBuilder;
import org.opendaylight.yangtools.yang.common.Uint16;
import org.opendaylight.yangtools.yang.common.Uint32;
import org.opendaylight.yangtools.yang.common.Uint64;
import org.opendaylight.yangtools.yang.common.Uint8;

/**
 * Unit tests for FlowModInputMessageFactory.
 *
 * @author timotej.kubas
 * @author michal.polkorab
 */
public class FlowModInputMessageFactoryTest {
    private static final byte PADDING_IN_FLOW_MOD_MESSAGE = 2;
    private SerializerRegistry registry;
    private OFSerializer<FlowModInput> flowModFactory;

    /**
     * Initializes serializer registry and stores correct factory in field.
     */
    @Before
    public void startUp() {
        registry = new SerializerRegistryImpl();
        registry.init();
        flowModFactory = registry.getSerializer(new MessageTypeKey<>(
                EncodeConstants.OF13_VERSION_ID, FlowModInput.class));
    }

    /**
     * Testing of {@link FlowModInputMessageFactory} for correct translation from POJO.
     */
    @SuppressWarnings("null")
    @Test
    public void testFlowModInputMessageFactory() throws Exception {
        FlowModInputBuilder builder = new FlowModInputBuilder();
        BufferHelper.setupHeader(builder, EncodeConstants.OF13_VERSION_ID);
        byte[] cookie = new byte[]{(byte) 0xFF, 0x01, 0x04, 0x01, 0x06, 0x00, 0x07, 0x01};
        builder.setCookie(Uint64.valueOf(new BigInteger(1, cookie)));
        byte[] cookieMask = new byte[]{(byte) 0xFF, 0x05, 0x00, 0x00, 0x09, 0x30, 0x00, 0x30};
        builder.setCookieMask(Uint64.valueOf(new BigInteger(1, cookieMask)));
        builder.setTableId(new TableId(Uint32.valueOf(65)));
        builder.setCommand(FlowModCommand.forValue(2));
        builder.setIdleTimeout(Uint16.valueOf(12));
        builder.setHardTimeout(Uint16.ZERO);
        builder.setPriority(Uint16.valueOf(126));
        builder.setBufferId(Uint32.TWO);
        builder.setOutPort(new PortNumber(Uint32.valueOf(4422)));
        builder.setOutGroup(Uint32.valueOf(98));
        builder.setFlags(new FlowModFlags(true, false, true, false, true));
        MatchBuilder matchBuilder = new MatchBuilder();
        matchBuilder.setType(OxmMatchType.class);
        final List<MatchEntry> entries = new ArrayList<>();
        MatchEntryBuilder entriesBuilder = new MatchEntryBuilder();
        entriesBuilder.setOxmClass(OpenflowBasicClass.class);
        entriesBuilder.setOxmMatchField(InPhyPort.class);
        entriesBuilder.setHasMask(false);
        InPhyPortCaseBuilder inPhyPortCaseBuilder = new InPhyPortCaseBuilder();
        InPhyPortBuilder inPhyPortBuilder = new InPhyPortBuilder();
        inPhyPortBuilder.setPortNumber(new PortNumber(Uint32.valueOf(42)));
        inPhyPortCaseBuilder.setInPhyPort(inPhyPortBuilder.build());
        entriesBuilder.setMatchEntryValue(inPhyPortCaseBuilder.build());
        entries.add(entriesBuilder.build());
        entriesBuilder.setOxmClass(OpenflowBasicClass.class);
        entriesBuilder.setOxmMatchField(IpEcn.class);
        entriesBuilder.setHasMask(false);
        IpEcnCaseBuilder ipEcnCaseBuilder = new IpEcnCaseBuilder();
        IpEcnBuilder ipEcnBuilder = new IpEcnBuilder();
        ipEcnBuilder.setEcn(Uint8.valueOf(4));
        ipEcnCaseBuilder.setIpEcn(ipEcnBuilder.build());
        entriesBuilder.setMatchEntryValue(ipEcnCaseBuilder.build());
        entries.add(entriesBuilder.build());
        matchBuilder.setMatchEntry(entries);
        builder.setMatch(matchBuilder.build());
        final List<Instruction> instructions = new ArrayList<>();
        InstructionBuilder insBuilder = new InstructionBuilder();
        GotoTableCaseBuilder goToCaseBuilder = new GotoTableCaseBuilder();
        GotoTableBuilder instructionBuilder = new GotoTableBuilder();
        instructionBuilder.setTableId(Uint8.valueOf(43));
        goToCaseBuilder.setGotoTable(instructionBuilder.build());
        insBuilder.setInstructionChoice(goToCaseBuilder.build());
        instructions.add(insBuilder.build());
        WriteMetadataCaseBuilder metadataCaseBuilder = new WriteMetadataCaseBuilder();
        WriteMetadataBuilder metadataBuilder = new WriteMetadataBuilder();
        metadataBuilder.setMetadata(cookie);
        metadataBuilder.setMetadataMask(cookieMask);
        metadataCaseBuilder.setWriteMetadata(metadataBuilder.build());
        insBuilder.setInstructionChoice(metadataCaseBuilder.build());
        instructions.add(insBuilder.build());
        insBuilder = new InstructionBuilder();
        final ApplyActionsCaseBuilder applyActionsCaseBuilder = new ApplyActionsCaseBuilder();
        final ApplyActionsBuilder actionsBuilder = new ApplyActionsBuilder();
        final List<Action> actions = new ArrayList<>();
        final ActionBuilder actionBuilder = new ActionBuilder();
        OutputActionCaseBuilder caseBuilder = new OutputActionCaseBuilder();
        OutputActionBuilder outputBuilder = new OutputActionBuilder();
        outputBuilder.setPort(new PortNumber(Uint32.valueOf(42)));
        outputBuilder.setMaxLength(Uint16.valueOf(52));
        caseBuilder.setOutputAction(outputBuilder.build());
        actionBuilder.setActionChoice(caseBuilder.build());
        actions.add(actionBuilder.build());
        actionsBuilder.setAction(actions);
        applyActionsCaseBuilder.setApplyActions(actionsBuilder.build());
        insBuilder.setInstructionChoice(applyActionsCaseBuilder.build());
        instructions.add(insBuilder.build());
        builder.setInstruction(instructions);
        final FlowModInput message = builder.build();

        ByteBuf out = UnpooledByteBufAllocator.DEFAULT.buffer();

        // simulate parent message
        out.writeInt(1);
        out.writeZero(2);
        out.writeShort(3);

        flowModFactory.serialize(message, out);

        // read parent message
        out.readInt();
        out.skipBytes(2);
        out.readShort();

        BufferHelper.checkHeaderV13(out,(byte) 14, 128);
        cookie = new byte[Long.BYTES];
        out.readBytes(cookie);
        Assert.assertEquals("Wrong cookie", message.getCookie(), Uint64.valueOf("FF01040106000701", 16));
        cookieMask = new byte[Long.BYTES];
        out.readBytes(cookieMask);
        Assert.assertEquals("Wrong cookieMask", message.getCookieMask(), Uint64.valueOf("FF05000009300030", 16));
        Assert.assertEquals("Wrong tableId", message.getTableId().getValue().intValue(), out.readUnsignedByte());
        Assert.assertEquals("Wrong command", message.getCommand().getIntValue(), out.readUnsignedByte());
        Assert.assertEquals("Wrong idleTimeOut", message.getIdleTimeout().intValue(), out.readShort());
        Assert.assertEquals("Wrong hardTimeOut", message.getHardTimeout().intValue(), out.readShort());
        Assert.assertEquals("Wrong priority", message.getPriority().intValue(), out.readUnsignedShort());
        Assert.assertEquals("Wrong bufferId", message.getBufferId().intValue(), out.readUnsignedInt());
        Assert.assertEquals("Wrong outPort", message.getOutPort().getValue().intValue(), out.readUnsignedInt());
        Assert.assertEquals("Wrong outGroup", message.getOutGroup().intValue(), out.readUnsignedInt());
        Assert.assertEquals("Wrong flags", message.getFlags(), createFlowModFlagsFromBitmap(out.readUnsignedShort()));
        out.skipBytes(PADDING_IN_FLOW_MOD_MESSAGE);
        Assert.assertEquals("Wrong match type", 1, out.readUnsignedShort());
        out.skipBytes(Short.BYTES);
        Assert.assertEquals("Wrong oxm class", 0x8000, out.readUnsignedShort());
        short fieldAndMask = out.readUnsignedByte();
        Assert.assertEquals("Wrong oxm hasMask", 0, fieldAndMask & 1);
        Assert.assertEquals("Wrong oxm field", 1, fieldAndMask >> 1);
        out.skipBytes(Byte.BYTES);
        Assert.assertEquals("Wrong oxm value", 42, out.readUnsignedInt());
        Assert.assertEquals("Wrong oxm class", 0x8000, out.readUnsignedShort());
        fieldAndMask = out.readUnsignedByte();
        Assert.assertEquals("Wrong oxm hasMask", 0, fieldAndMask & 1);
        Assert.assertEquals("Wrong oxm field", 9, fieldAndMask >> 1);
        out.skipBytes(Byte.BYTES);
        Assert.assertEquals("Wrong oxm value", 4, out.readUnsignedByte());
        out.skipBytes(7);
        Assert.assertEquals("Wrong instruction type", 1, out.readUnsignedShort());
        out.skipBytes(Short.BYTES);
        Assert.assertEquals("Wrong instruction value", 43, out.readUnsignedByte());
        out.skipBytes(3);
        Assert.assertEquals("Wrong instruction type", 2, out.readUnsignedShort());
        out.skipBytes(Short.BYTES);
        out.skipBytes(Integer.BYTES);
        byte[] cookieRead = new byte[Long.BYTES];
        out.readBytes(cookieRead);
        byte[] cookieMaskRead = new byte[Long.BYTES];
        out.readBytes(cookieMaskRead);
        Assert.assertArrayEquals("Wrong metadata", cookie, cookieRead);
        Assert.assertArrayEquals("Wrong metadata mask", cookieMask, cookieMaskRead);
        Assert.assertEquals("Wrong instruction type", 4, out.readUnsignedShort());
        Assert.assertEquals("Wrong instruction length", 24, out.readUnsignedShort());
        out.skipBytes(4);
        Assert.assertEquals("Wrong action type", 0, out.readUnsignedShort());
        Assert.assertEquals("Wrong action length", 16, out.readUnsignedShort());
        Assert.assertEquals("Wrong port", 42, out.readUnsignedInt());
        Assert.assertEquals("Wrong max-length", 52, out.readUnsignedShort());
        out.skipBytes(6);
        Assert.assertTrue("Unread data", out.readableBytes() == 0);
    }

    @SuppressWarnings("checkstyle:AbbreviationAsWordInName")
    private static FlowModFlags createFlowModFlagsFromBitmap(final int input) {
        final Boolean _oFPFFSENDFLOWREM = (input & 1 << 0) > 0;
        final Boolean _oFPFFCHECKOVERLAP = (input & 1 << 1) > 0;
        final Boolean _oFPFFRESETCOUNTS = (input & 1 << 2) > 0;
        final Boolean _oFPFFNOPKTCOUNTS = (input & 1 << 3) > 0;
        final Boolean _oFPFFNOBYTCOUNTS = (input & 1 << 4) > 0;
        return new FlowModFlags(_oFPFFCHECKOVERLAP, _oFPFFNOBYTCOUNTS, _oFPFFNOPKTCOUNTS, _oFPFFRESETCOUNTS,
                _oFPFFSENDFLOWREM);
    }

}
