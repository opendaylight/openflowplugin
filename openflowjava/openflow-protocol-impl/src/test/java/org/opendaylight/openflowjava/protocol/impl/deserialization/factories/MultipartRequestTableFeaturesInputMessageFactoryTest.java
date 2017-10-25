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
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.augments.rev150225.ActionRelatedTableFeatureProperty;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.augments.rev150225.ActionRelatedTableFeaturePropertyBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.augments.rev150225.InstructionRelatedTableFeatureProperty;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.augments.rev150225.InstructionRelatedTableFeaturePropertyBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.augments.rev150225.NextTableRelatedTableFeatureProperty;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.augments.rev150225.NextTableRelatedTableFeaturePropertyBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.augments.rev150225.OxmRelatedTableFeatureProperty;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.augments.rev150225.OxmRelatedTableFeaturePropertyBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.augments.rev150225.table.features.properties.container.table.feature.properties.NextTableIds;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.augments.rev150225.table.features.properties.container.table.feature.properties.NextTableIdsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.OutputActionCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.actions.grouping.Action;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.actions.grouping.ActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.instruction.rev130731.instruction.grouping.instruction.choice.ApplyActionsCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.instruction.rev130731.instruction.grouping.instruction.choice.GotoTableCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.instruction.rev130731.instruction.grouping.instruction.choice.WriteMetadataCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.instruction.rev130731.instructions.grouping.Instruction;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.instruction.rev130731.instructions.grouping.InstructionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.MultipartRequestFlags;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.MultipartType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.TableConfig;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.TableFeaturesPropType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.InPhyPort;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.OpenflowBasicClass;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entries.grouping.MatchEntry;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entries.grouping.MatchEntryBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.MultipartRequestInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.MultipartRequestTableFeaturesCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.MultipartRequestTableFeaturesCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.multipart.request.table.features._case.MultipartRequestTableFeaturesBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.multipart.request.table.features._case.multipart.request.table.features.TableFeatures;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.multipart.request.table.features._case.multipart.request.table.features.TableFeaturesBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.table.features.properties.grouping.TableFeatureProperties;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.table.features.properties.grouping.TableFeaturePropertiesBuilder;

/**
 * @author giuseppex.petralia@intel.com
 *
 */
public class MultipartRequestTableFeaturesInputMessageFactoryTest {
    private OFDeserializer<MultipartRequestInput> factory;

    @Before
    public void startUp() {
        DeserializerRegistry desRegistry = new DeserializerRegistryImpl();
        desRegistry.init();
        factory = desRegistry
                .getDeserializer(new MessageCodeKey(EncodeConstants.OF13_VERSION_ID, 18, MultipartRequestInput.class));
    }

    @Test
    public void test() {
        ByteBuf bb = BufferHelper.buildBuffer("00 0c 00 01 00 00 00 00 00 68 01 00 00 00 00 00 4e 61 6d "
                + "65 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 "
                + "00 00 00 00 00 00 00 01 00 00 00 00 00 00 00 01 00 00 00 00 00 00 00 01 00 00 00 10 00 01 "
                + "00 04 00 02 00 04 00 04 00 04 00 02 00 05 01 00 00 00 00 04 00 08 00 00 00 04 00 08 00 08 80 00 02 04 ");
        MultipartRequestInput deserializedMessage = BufferHelper.deserialize(factory, bb);
        BufferHelper.checkHeaderV13(deserializedMessage);
        Assert.assertEquals("Wrong type", MultipartType.forValue(12), deserializedMessage.getType());
        Assert.assertEquals("Wrong flags", new MultipartRequestFlags(true), deserializedMessage.getFlags());
        Assert.assertEquals("Wrong body", createTableFeatures(), deserializedMessage.getMultipartRequestBody());
    }

    public MultipartRequestTableFeaturesCase createTableFeatures() {
        MultipartRequestTableFeaturesCaseBuilder caseBuilder = new MultipartRequestTableFeaturesCaseBuilder();
        MultipartRequestTableFeaturesBuilder builder = new MultipartRequestTableFeaturesBuilder();
        builder.setTableFeatures(createTableFeaturesList());
        caseBuilder.setMultipartRequestTableFeatures(builder.build());
        return caseBuilder.build();

    }

    public List<TableFeatures> createTableFeaturesList() {
        List<TableFeatures> list = new ArrayList<>();
        TableFeaturesBuilder builder = new TableFeaturesBuilder();
        builder.setTableId((short) 1);
        builder.setName("Name");
        builder.setMetadataWrite(new BigInteger("1"));
        builder.setMetadataMatch(new BigInteger("1"));
        builder.setMaxEntries(1L);
        builder.setConfig(new TableConfig(false));
        builder.setTableFeatureProperties(createTableFeatureProperties());
        list.add(builder.build());
        return list;
    }

    public List<TableFeatureProperties> createTableFeatureProperties() {
        List<TableFeatureProperties> list = new ArrayList<>();
        TableFeaturePropertiesBuilder builder = new TableFeaturePropertiesBuilder();
        builder.setType(TableFeaturesPropType.forValue(0));
        InstructionRelatedTableFeaturePropertyBuilder insBuilder = new InstructionRelatedTableFeaturePropertyBuilder();
        insBuilder.setInstruction(createInstructions());
        builder.addAugmentation(InstructionRelatedTableFeatureProperty.class, insBuilder.build());
        list.add(builder.build());

        builder = new TableFeaturePropertiesBuilder();
        builder.setType(TableFeaturesPropType.forValue(2));
        NextTableRelatedTableFeaturePropertyBuilder nextBuilder = new NextTableRelatedTableFeaturePropertyBuilder();
        nextBuilder.setNextTableIds(createNextTableIds());
        builder.addAugmentation(NextTableRelatedTableFeatureProperty.class, nextBuilder.build());
        list.add(builder.build());

        builder = new TableFeaturePropertiesBuilder();
        builder.setType(TableFeaturesPropType.forValue(4));
        ActionRelatedTableFeaturePropertyBuilder actionBuilder = new ActionRelatedTableFeaturePropertyBuilder();
        actionBuilder.setAction(createAction());
        builder.addAugmentation(ActionRelatedTableFeatureProperty.class, actionBuilder.build());
        list.add(builder.build());

        builder = new TableFeaturePropertiesBuilder();
        builder.setType(TableFeaturesPropType.forValue(8));
        OxmRelatedTableFeaturePropertyBuilder oxmBuilder = new OxmRelatedTableFeaturePropertyBuilder();
        oxmBuilder.setMatchEntry(createMatchEntries());
        builder.addAugmentation(OxmRelatedTableFeatureProperty.class, oxmBuilder.build());
        list.add(builder.build());

        return list;
    }

    public List<MatchEntry> createMatchEntries() {
        List<MatchEntry> entries = new ArrayList<>();
        MatchEntryBuilder entriesBuilder = new MatchEntryBuilder();
        entriesBuilder.setOxmClass(OpenflowBasicClass.class);
        entriesBuilder.setOxmMatchField(InPhyPort.class);
        entriesBuilder.setHasMask(false);
        entries.add(entriesBuilder.build());
        return entries;
    }

    public List<Action> createAction() {
        List<Action> actions = new ArrayList<>();
        ActionBuilder actionBuilder = new ActionBuilder();
        OutputActionCaseBuilder caseBuilder = new OutputActionCaseBuilder();
        actionBuilder.setActionChoice(caseBuilder.build());
        actions.add(actionBuilder.build());
        return actions;
    }

    public List<NextTableIds> createNextTableIds() {
        List<NextTableIds> list = new ArrayList<>();
        NextTableIdsBuilder builder = new NextTableIdsBuilder();
        builder.setTableId((short) 1);
        list.add(builder.build());
        return list;
    }

    public List<Instruction> createInstructions() {
        List<Instruction> instructions = new ArrayList<>();
        InstructionBuilder insBuilder = new InstructionBuilder();
        GotoTableCaseBuilder goToCaseBuilder = new GotoTableCaseBuilder();
        insBuilder.setInstructionChoice(goToCaseBuilder.build());
        instructions.add(insBuilder.build());
        WriteMetadataCaseBuilder metadataCaseBuilder = new WriteMetadataCaseBuilder();
        insBuilder.setInstructionChoice(metadataCaseBuilder.build());
        instructions.add(insBuilder.build());
        insBuilder = new InstructionBuilder();
        ApplyActionsCaseBuilder applyActionsCaseBuilder = new ApplyActionsCaseBuilder();
        insBuilder.setInstructionChoice(applyActionsCaseBuilder.build());
        instructions.add(insBuilder.build());
        return instructions;
    }
}
