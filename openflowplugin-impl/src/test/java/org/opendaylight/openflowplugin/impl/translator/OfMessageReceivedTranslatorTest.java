/*
 * Copyright (c) 2015 NetIDE Consortium and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.translator;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.openflowplugin.api.OFConstants;
import org.opendaylight.openflowplugin.api.openflow.connection.ConnectionContext;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceContext;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceState;
import org.opendaylight.openflowplugin.openflow.md.util.OpenflowPortsUtil;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.FlowModCommand;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.FlowModFlags;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.PortNumber;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.TableId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.FeaturesReply;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.FlowModInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.GetFeaturesOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.OfHeader;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.features.reply.PhyPort;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.binding.KeyedInstanceIdentifier;
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.Nodes;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.NodeKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.of.message.service.rev160511.OfMessageReceived;
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

@RunWith(MockitoJUnitRunner.class)
public class OfMessageReceivedTranslatorTest {


    @Mock
    ConnectionContext connectionContext;
    @Mock
    FeaturesReply featuresReply;
    @Mock
    GetFeaturesOutput getFeaturesOutput;
    @Mock
    DeviceState deviceState;
    @Mock
    DataBroker dataBroker;
    @Mock
    DeviceContext deviceContext;
    @Mock
    List<PhyPort> phyPorts;
    @Mock
    PhyPort phyPort;

    static final String PAYLOAD = "04 0e 00 80 01 02 03 04 ff 01 04 01 06 00 07 01 ff 05 00 00 09 30 00 "
            + "30 41 02 00 0c 00 00 00 7e 00 00 00 02 00 00 11 46 00 00 00 62 00 0b 00 00 00 01 "
            + "00 11 80 00 02 04 00 00 00 2a 80 00 12 01 04 00 00 00 00 00 00 00 00 01 00 08 2b 00 "
            + "00 00 00 02 00 18 00 00 00 00 ff 01 04 01 06 00 07 01 ff 05 00 00 09 30 00 30 00 04 00 "
            + "18 00 00 00 00 00 00 00 10 00 00 00 2a 00 34 00 00 00 00 00 00";

    protected static final String DUMMY_NODE_ID = "dummyNodeID";
    private static final KeyedInstanceIdentifier<Node, NodeKey> NODE_II
    = InstanceIdentifier.create(Nodes.class).child(Node.class, new NodeKey(new NodeId(DUMMY_NODE_ID)));

    public OfMessageReceivedTranslatorTest() {
        OpenflowPortsUtil.init();
    }

    @Before
    public void setUp() throws Exception {
        final List<PhyPort> phyPorts = Arrays.asList(phyPort);
        Mockito.when(deviceContext.getPrimaryConnectionContext()).thenReturn(connectionContext);
        Mockito.when(connectionContext.getFeatures()).thenReturn(featuresReply);
        Mockito.when(featuresReply.getDatapathId()).thenReturn(BigInteger.TEN);
        Mockito.when(deviceContext.getDeviceState()).thenReturn(deviceState);
        Mockito.when(deviceState.getVersion()).thenReturn(OFConstants.OFP_VERSION_1_3);
        Mockito.when(deviceState.getFeatures()).thenReturn(getFeaturesOutput);
        Mockito.when(deviceState.getNodeInstanceIdentifier()).thenReturn(NODE_II);
        Mockito.when(getFeaturesOutput.getDatapathId()).thenReturn(BigInteger.TEN);
        Mockito.when(getFeaturesOutput.getPhyPort()).thenReturn(phyPorts);
    }

    @Test
    public void testTranslate() throws Exception {
        final OfMessageReceivedTranslator translator = new OfMessageReceivedTranslator();
        OfHeader input = createOfHeaderInput();
        OfMessageReceived output = translator.translate(input, deviceState, null);
        Assert.assertArrayEquals("Wrong message in the translator", output.getMessage(), fromStringToByteArray(PAYLOAD));
    }

    private OfHeader createOfHeaderInput(){
        FlowModInputBuilder builder = new FlowModInputBuilder();
        builder.setVersion((short) 0x04);
        builder.setXid(0x01020304L);
        byte[] cookie = new byte[]{(byte) 0xFF, 0x01, 0x04, 0x01, 0x06, 0x00, 0x07, 0x01};
        builder.setCookie(new BigInteger(1, cookie));
        byte[] cookieMask = new byte[]{(byte) 0xFF, 0x05, 0x00, 0x00, 0x09, 0x30, 0x00, 0x30};
        builder.setCookieMask(new BigInteger(1, cookieMask));
        builder.setTableId(new TableId(65L));
        builder.setCommand(FlowModCommand.forValue(2));
        builder.setIdleTimeout(12);
        builder.setHardTimeout(0);
        builder.setPriority(126);
        builder.setBufferId(2L);
        builder.setOutPort(new PortNumber(4422L));
        builder.setOutGroup(98L);
        builder.setFlags(new FlowModFlags(true, false, true, false, true));
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
        builder.setMatch(matchBuilder.build());
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
        metadataBuilder.setMetadata(cookie);
        metadataBuilder.setMetadataMask(cookieMask);
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
        builder.setInstruction(instructions);
        FlowModInput message = builder.build();
        return (OfHeader) message;
    }

    private byte[] fromStringToByteArray(String input){
        final Splitter splitter =  Splitter.onPattern("\\s+").omitEmptyStrings();
        List<String> byteChips = Lists.newArrayList(splitter.split(input));
        byte[] result = new byte[byteChips.size()];
        int i = 0;
        for (String chip : byteChips) {
            result[i] = (byte) Short.parseShort(chip, 16);
            i++;
        }
        return result;
    }
}
