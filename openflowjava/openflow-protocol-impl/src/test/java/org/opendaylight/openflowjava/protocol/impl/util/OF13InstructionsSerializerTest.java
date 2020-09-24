/*
 * Copyright (c) 2013 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowjava.protocol.impl.util;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.UnpooledByteBufAllocator;
import java.util.ArrayList;
import java.util.List;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.opendaylight.openflowjava.protocol.api.extensibility.SerializerRegistry;
import org.opendaylight.openflowjava.protocol.api.util.EncodeConstants;
import org.opendaylight.openflowjava.protocol.impl.serialization.SerializerRegistryImpl;
import org.opendaylight.openflowjava.util.ByteBufUtils;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.OutputActionCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.PopPbbCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.PushVlanCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.SetNwTtlCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.output.action._case.OutputActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.push.vlan._case.PushVlanActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.set.nw.ttl._case.SetNwTtlActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.actions.grouping.Action;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.actions.grouping.ActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.instruction.rev130731.instruction.grouping.instruction.choice.ApplyActionsCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.instruction.rev130731.instruction.grouping.instruction.choice.ClearActionsCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.instruction.rev130731.instruction.grouping.instruction.choice.GotoTableCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.instruction.rev130731.instruction.grouping.instruction.choice.MeterCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.instruction.rev130731.instruction.grouping.instruction.choice.WriteActionsCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.instruction.rev130731.instruction.grouping.instruction.choice.WriteMetadataCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.instruction.rev130731.instruction.grouping.instruction.choice._goto.table._case.GotoTableBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.instruction.rev130731.instruction.grouping.instruction.choice.apply.actions._case.ApplyActionsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.instruction.rev130731.instruction.grouping.instruction.choice.meter._case.MeterBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.instruction.rev130731.instruction.grouping.instruction.choice.write.actions._case.WriteActionsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.instruction.rev130731.instruction.grouping.instruction.choice.write.metadata._case.WriteMetadataBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.instruction.rev130731.instructions.grouping.Instruction;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.instruction.rev130731.instructions.grouping.InstructionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.EtherType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.PortNumber;
import org.opendaylight.yangtools.yang.common.Uint16;
import org.opendaylight.yangtools.yang.common.Uint32;
import org.opendaylight.yangtools.yang.common.Uint8;

/**
 * Unit tests for OF13InstructionsSerializer.
 *
 * @author michal.polkorab
 */
public class OF13InstructionsSerializerTest {

    private SerializerRegistry registry;

    /**
     * Initializes serializer table and stores correct factory in field.
     */
    @Before
    public void startUp() {
        registry = new SerializerRegistryImpl();
        registry.init();
    }

    /**
     * Testing instructions translation.
     */
    @Test
    public void test() {
        final List<Instruction> instructions = new ArrayList<>();
        // Goto_table instruction
        InstructionBuilder builder = new InstructionBuilder();
        GotoTableCaseBuilder gotoCaseBuilder = new GotoTableCaseBuilder();
        GotoTableBuilder instructionBuilder = new GotoTableBuilder();
        instructionBuilder.setTableId(Uint8.valueOf(5));
        gotoCaseBuilder.setGotoTable(instructionBuilder.build());
        builder.setInstructionChoice(gotoCaseBuilder.build());
        instructions.add(builder.build());
        // Write_metadata instruction
        builder = new InstructionBuilder();
        WriteMetadataCaseBuilder metadataCaseBuilder = new WriteMetadataCaseBuilder();
        WriteMetadataBuilder metadataBuilder = new WriteMetadataBuilder();
        metadataBuilder.setMetadata(ByteBufUtils.hexStringToBytes("00 01 02 03 04 05 06 07"));
        metadataBuilder.setMetadataMask(ByteBufUtils.hexStringToBytes("07 06 05 04 03 02 01 00"));
        metadataCaseBuilder.setWriteMetadata(metadataBuilder.build());
        builder.setInstructionChoice(metadataCaseBuilder.build());
        instructions.add(builder.build());
        // Clear_actions instruction
        builder = new InstructionBuilder();
        builder.setInstructionChoice(new ClearActionsCaseBuilder().build());
        instructions.add(builder.build());
        // Meter instruction
        builder = new InstructionBuilder();
        MeterCaseBuilder meterCaseBuilder = new MeterCaseBuilder();
        MeterBuilder meterBuilder = new MeterBuilder();
        meterBuilder.setMeterId(Uint32.valueOf(42));
        meterCaseBuilder.setMeter(meterBuilder.build());
        builder.setInstructionChoice(meterCaseBuilder.build());
        instructions.add(builder.build());
        // Write_actions instruction
        builder = new InstructionBuilder();
        final WriteActionsCaseBuilder writeActionsCaseBuilder = new WriteActionsCaseBuilder();
        final WriteActionsBuilder writeActionsBuilder = new WriteActionsBuilder();
        OutputActionCaseBuilder caseBuilder = new OutputActionCaseBuilder();
        OutputActionBuilder outputBuilder = new OutputActionBuilder();
        outputBuilder.setPort(new PortNumber(Uint32.valueOf(45)));
        outputBuilder.setMaxLength(Uint16.valueOf(55));
        caseBuilder.setOutputAction(outputBuilder.build());
        ActionBuilder actionBuilder = new ActionBuilder();
        actionBuilder.setActionChoice(caseBuilder.build());
        List<Action> actions = new ArrayList<>();
        actions.add(actionBuilder.build());
        actionBuilder = new ActionBuilder();
        SetNwTtlCaseBuilder ttlCaseBuilder = new SetNwTtlCaseBuilder();
        SetNwTtlActionBuilder ttlActionBuilder = new SetNwTtlActionBuilder();
        ttlActionBuilder.setNwTtl(Uint8.valueOf(64));
        ttlCaseBuilder.setSetNwTtlAction(ttlActionBuilder.build());
        actionBuilder.setActionChoice(ttlCaseBuilder.build());
        actions.add(actionBuilder.build());
        writeActionsBuilder.setAction(actions);
        writeActionsCaseBuilder.setWriteActions(writeActionsBuilder.build());
        builder.setInstructionChoice(writeActionsCaseBuilder.build());
        instructions.add(builder.build());
        // Apply_actions instruction
        builder = new InstructionBuilder();
        final ApplyActionsCaseBuilder applyActionsCaseBuilder = new ApplyActionsCaseBuilder();
        final ApplyActionsBuilder applyActionsBuilder = new ApplyActionsBuilder();
        actions = new ArrayList<>();
        actionBuilder = new ActionBuilder();
        PushVlanCaseBuilder vlanCaseBuilder = new PushVlanCaseBuilder();
        PushVlanActionBuilder vlanBuilder = new PushVlanActionBuilder();
        vlanBuilder.setEthertype(new EtherType(new EtherType(Uint16.valueOf(14))));
        vlanCaseBuilder.setPushVlanAction(vlanBuilder.build());
        actionBuilder.setActionChoice(vlanCaseBuilder.build());
        actions.add(actionBuilder.build());
        actionBuilder = new ActionBuilder();
        actionBuilder.setActionChoice(new PopPbbCaseBuilder().build());
        actions.add(actionBuilder.build());
        applyActionsBuilder.setAction(actions);
        applyActionsCaseBuilder.setApplyActions(applyActionsBuilder.build());
        builder.setInstructionChoice(applyActionsCaseBuilder.build());
        instructions.add(builder.build());

        ByteBuf out = UnpooledByteBufAllocator.DEFAULT.buffer();
        ListSerializer.serializeList(instructions, TypeKeyMakerFactory
                .createInstructionKeyMaker(EncodeConstants.OF13_VERSION_ID), registry, out);

        Assert.assertEquals("Wrong instruction type", 1, out.readUnsignedShort());
        Assert.assertEquals("Wrong instruction length", 8, out.readUnsignedShort());
        Assert.assertEquals("Wrong instruction table-id", 5, out.readUnsignedByte());
        out.skipBytes(3);
        Assert.assertEquals("Wrong instruction type", 2, out.readUnsignedShort());
        Assert.assertEquals("Wrong instruction length", 24, out.readUnsignedShort());
        out.skipBytes(4);
        byte[] actual = new byte[8];
        out.readBytes(actual);
        Assert.assertEquals("Wrong instruction metadata", "00 01 02 03 04 05 06 07",
                ByteBufUtils.bytesToHexString(actual));
        actual = new byte[8];
        out.readBytes(actual);
        Assert.assertEquals("Wrong instruction metadata-mask", "07 06 05 04 03 02 01 00",
                ByteBufUtils.bytesToHexString(actual));
        Assert.assertEquals("Wrong instruction type", 5, out.readUnsignedShort());
        Assert.assertEquals("Wrong instruction length", 8, out.readUnsignedShort());
        out.skipBytes(4);
        Assert.assertEquals("Wrong instruction type", 6, out.readUnsignedShort());
        Assert.assertEquals("Wrong instruction length", 8, out.readUnsignedShort());
        Assert.assertEquals("Wrong instruction meter-id", 42, out.readUnsignedInt());
        Assert.assertEquals("Wrong instruction type", 3, out.readUnsignedShort());
        Assert.assertEquals("Wrong instruction length", 32, out.readUnsignedShort());
        out.skipBytes(4);
        Assert.assertEquals("Wrong action type", 0, out.readUnsignedShort());
        Assert.assertEquals("Wrong action length", 16, out.readUnsignedShort());
        Assert.assertEquals("Wrong action type", 45, out.readUnsignedInt());
        Assert.assertEquals("Wrong action type", 55, out.readUnsignedShort());
        out.skipBytes(6);
        Assert.assertEquals("Wrong action type", 23, out.readUnsignedShort());
        Assert.assertEquals("Wrong action length", 8, out.readUnsignedShort());
        Assert.assertEquals("Wrong action type", 64, out.readUnsignedByte());
        out.skipBytes(3);
        Assert.assertEquals("Wrong instruction type", 4, out.readUnsignedShort());
        Assert.assertEquals("Wrong instruction length", 24, out.readUnsignedShort());
        out.skipBytes(4);
        Assert.assertEquals("Wrong action type", 17, out.readUnsignedShort());
        Assert.assertEquals("Wrong action length", 8, out.readUnsignedShort());
        Assert.assertEquals("Wrong action ethertype", 14, out.readUnsignedShort());
        out.skipBytes(2);
        Assert.assertEquals("Wrong action type", 27, out.readUnsignedShort());
        Assert.assertEquals("Wrong action length", 8, out.readUnsignedShort());
        out.skipBytes(4);
        Assert.assertTrue("Not all data were read", out.readableBytes() == 0);
    }

}
