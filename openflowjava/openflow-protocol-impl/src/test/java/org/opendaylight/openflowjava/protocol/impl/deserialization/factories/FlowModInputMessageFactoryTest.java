/*
 * Copyright (c) 2015 NetIDE Consortium and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowjava.protocol.impl.deserialization.factories;

import io.netty.buffer.ByteBuf;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.opendaylight.openflowjava.protocol.api.extensibility.DeserializerRegistry;
import org.opendaylight.openflowjava.protocol.api.extensibility.OFDeserializer;
import org.opendaylight.openflowjava.protocol.api.keys.MessageCodeKey;
import org.opendaylight.openflowjava.protocol.api.util.EncodeConstants;
import org.opendaylight.openflowjava.protocol.impl.deserialization.DeserializerRegistryImpl;
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
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.grouping.Match;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.grouping.MatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.FlowModInput;

/**
 * @author giuseppex.petralia@intel.com
 *
 */
public class FlowModInputMessageFactoryTest {
    private OFDeserializer<FlowModInput> flowFactory;

    /**
     * Initializes deserializer registry and lookups correct deserializer
     */
    @Before
    public void startUp() {
        DeserializerRegistry registry = new DeserializerRegistryImpl();
        registry.init();
        flowFactory = registry
                .getDeserializer(new MessageCodeKey(EncodeConstants.OF13_VERSION_ID, 14, FlowModInput.class));
    }

    @Test
    public void test() throws Exception {
        ByteBuf bb = BufferHelper
                .buildBuffer("ff 01 04 01 06 00 07 01 ff 05 00 00 09 30 00 30 41 02 00 0c 00 00 00 7e 00 "
                        + "00 00 02 00 00 11 46 00 00 00 62 00 0b 00 00 00 01 00 11 80 00 02 04 00 00 00 2a 80 00 12 01 04 00 "
                        + "00 00 00 00 00 00 00 01 00 08 2b 00 00 00 00 02 00 18 00 00 00 00 ff 01 04 01 06 00 07 01 ff 05 00 00 "
                        + "09 30 00 30 00 04 00 18 00 00 00 00 00 00 00 10 00 00 00 2a 00 34 00 00 00 00 00 00");
        FlowModInput deserializedMessage = BufferHelper.deserialize(flowFactory, bb);
        BufferHelper.checkHeaderV13(deserializedMessage);
        byte[] cookie = new byte[] { (byte) 0xFF, 0x01, 0x04, 0x01, 0x06, 0x00, 0x07, 0x01 };
        Assert.assertEquals("Wrong cookie", new BigInteger(1, cookie), deserializedMessage.getCookie());
        byte[] cookieMask = new byte[] { (byte) 0xFF, 0x05, 0x00, 0x00, 0x09, 0x30, 0x00, 0x30 };
        Assert.assertEquals("Wrong cookie mask", new BigInteger(1, cookieMask), deserializedMessage.getCookieMask());
        Assert.assertEquals("Wrong table id", new TableId(65L), deserializedMessage.getTableId());
        Assert.assertEquals("Wrong command", FlowModCommand.forValue(2), deserializedMessage.getCommand());
        Assert.assertEquals("Wrong idle timeout", 12, deserializedMessage.getIdleTimeout().intValue());
        Assert.assertEquals("Wrong hard timeout", 0, deserializedMessage.getHardTimeout().intValue());
        Assert.assertEquals("Wrong priority", 126, deserializedMessage.getPriority().intValue());
        Assert.assertEquals("Wrong buffer id ", 2L, deserializedMessage.getBufferId().longValue());
        Assert.assertEquals("Wrong out port", new PortNumber(4422L), deserializedMessage.getOutPort());
        Assert.assertEquals("Wrong out group", 98L, deserializedMessage.getOutGroup().longValue());
        Assert.assertEquals("Wrong flags", new FlowModFlags(true, false, true, false, true),
                deserializedMessage.getFlags());
        Assert.assertEquals("Wrong match", createMatch(), deserializedMessage.getMatch());
        Assert.assertEquals("Wrong instructions", createInstructions(), deserializedMessage.getInstruction());

    }

    private List<Instruction> createInstructions() {
        List<Instruction> instructions = new ArrayList<>();
        InstructionBuilder insBuilder = new InstructionBuilder();
        GotoTableCaseBuilder goToCaseBuilder = new GotoTableCaseBuilder();
        GotoTableBuilder instructionBuilder = new GotoTableBuilder();
        instructionBuilder.setTableId((short) 43);
        goToCaseBuilder.setGotoTable(instructionBuilder.build());
        insBuilder.setInstructionChoice(goToCaseBuilder.build());
        instructions.add(insBuilder.build());
        WriteMetadataCaseBuilder metadataCaseBuilder = new WriteMetadataCaseBuilder();
        WriteMetadataBuilder metadataBuilder = new WriteMetadataBuilder();
        byte[] metadata = new byte[] { (byte) 0xFF, 0x01, 0x04, 0x01, 0x06, 0x00, 0x07, 0x01 };
        metadataBuilder.setMetadata(metadata);
        byte[] metadataMask = new byte[] { (byte) 0xFF, 0x05, 0x00, 0x00, 0x09, 0x30, 0x00, 0x30 };
        metadataBuilder.setMetadataMask(metadataMask);
        metadataCaseBuilder.setWriteMetadata(metadataBuilder.build());
        insBuilder.setInstructionChoice(metadataCaseBuilder.build());
        instructions.add(insBuilder.build());
        insBuilder = new InstructionBuilder();
        ApplyActionsCaseBuilder applyActionsCaseBuilder = new ApplyActionsCaseBuilder();
        ApplyActionsBuilder actionsBuilder = new ApplyActionsBuilder();
        List<Action> actions = new ArrayList<>();
        ActionBuilder actionBuilder = new ActionBuilder();
        OutputActionCaseBuilder caseBuilder = new OutputActionCaseBuilder();
        OutputActionBuilder outputBuilder = new OutputActionBuilder();
        outputBuilder.setPort(new PortNumber(42L));
        outputBuilder.setMaxLength(52);
        caseBuilder.setOutputAction(outputBuilder.build());
        actionBuilder.setActionChoice(caseBuilder.build());
        actions.add(actionBuilder.build());
        actionsBuilder.setAction(actions);
        applyActionsCaseBuilder.setApplyActions(actionsBuilder.build());
        insBuilder.setInstructionChoice(applyActionsCaseBuilder.build());
        instructions.add(insBuilder.build());
        return instructions;
    }

    private Match createMatch() {
        MatchBuilder matchBuilder = new MatchBuilder();
        matchBuilder.setType(OxmMatchType.class);
        List<MatchEntry> entries = new ArrayList<>();
        MatchEntryBuilder entriesBuilder = new MatchEntryBuilder();
        entriesBuilder.setOxmClass(OpenflowBasicClass.class);
        entriesBuilder.setOxmMatchField(InPhyPort.class);
        entriesBuilder.setHasMask(false);
        InPhyPortCaseBuilder inPhyPortCaseBuilder = new InPhyPortCaseBuilder();
        InPhyPortBuilder inPhyPortBuilder = new InPhyPortBuilder();
        inPhyPortBuilder.setPortNumber(new PortNumber(42L));
        inPhyPortCaseBuilder.setInPhyPort(inPhyPortBuilder.build());
        entriesBuilder.setMatchEntryValue(inPhyPortCaseBuilder.build());
        entries.add(entriesBuilder.build());
        entriesBuilder.setOxmClass(OpenflowBasicClass.class);
        entriesBuilder.setOxmMatchField(IpEcn.class);
        entriesBuilder.setHasMask(false);
        IpEcnCaseBuilder ipEcnCaseBuilder = new IpEcnCaseBuilder();
        IpEcnBuilder ipEcnBuilder = new IpEcnBuilder();
        ipEcnBuilder.setEcn((short) 4);
        ipEcnCaseBuilder.setIpEcn(ipEcnBuilder.build());
        entriesBuilder.setMatchEntryValue(ipEcnCaseBuilder.build());
        entries.add(entriesBuilder.build());
        matchBuilder.setMatchEntry(entries);
        return matchBuilder.build();
    }

}
