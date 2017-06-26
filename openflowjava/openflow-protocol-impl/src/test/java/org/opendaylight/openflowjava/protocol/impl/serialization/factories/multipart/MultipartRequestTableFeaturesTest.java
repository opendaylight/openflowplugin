/*
 * Copyright (c) 2013 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowjava.protocol.impl.serialization.factories.multipart;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.UnpooledByteBufAllocator;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.opendaylight.openflowjava.protocol.api.extensibility.OFSerializer;
import org.opendaylight.openflowjava.protocol.api.extensibility.SerializerRegistry;
import org.opendaylight.openflowjava.protocol.api.keys.MessageTypeKey;
import org.opendaylight.openflowjava.protocol.api.util.EncodeConstants;
import org.opendaylight.openflowjava.protocol.impl.serialization.SerializerRegistryImpl;
import org.opendaylight.openflowjava.protocol.impl.serialization.factories.MultipartRequestInputFactory;
import org.opendaylight.openflowjava.protocol.impl.serialization.factories.MultipartRequestInputFactoryTest;
import org.opendaylight.openflowjava.protocol.impl.util.BufferHelper;
import org.opendaylight.openflowjava.util.ByteBufUtils;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.augments.rev150225.ActionRelatedTableFeatureProperty;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.augments.rev150225.ActionRelatedTableFeaturePropertyBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.augments.rev150225.ExperimenterIdTableFeatureProperty;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.augments.rev150225.ExperimenterIdTableFeaturePropertyBuilder;
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
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.instruction.rev130731.instruction.grouping.instruction.choice.ClearActionsCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.instruction.rev130731.instruction.grouping.instruction.choice.GotoTableCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.instruction.rev130731.instruction.grouping.instruction.choice.MeterCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.instruction.rev130731.instruction.grouping.instruction.choice.WriteActionsCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.instruction.rev130731.instruction.grouping.instruction.choice.WriteMetadataCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.instruction.rev130731.instructions.grouping.Instruction;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.instruction.rev130731.instructions.grouping.InstructionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.ExperimenterId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.MultipartRequestFlags;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.MultipartType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.TableConfig;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.TableFeaturesPropType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.InPhyPort;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.InPort;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.IpEcn;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.IpProto;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.OpenflowBasicClass;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entries.grouping.MatchEntry;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entries.grouping.MatchEntryBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.MultipartRequestInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.MultipartRequestInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.MultipartRequestTableFeaturesCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.multipart.request.table.features._case.MultipartRequestTableFeaturesBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.multipart.request.table.features._case.multipart.request.table.features.TableFeatures;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.multipart.request.table.features._case.multipart.request.table.features.TableFeaturesBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.table.features.properties.grouping.TableFeatureProperties;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.table.features.properties.grouping.TableFeaturePropertiesBuilder;

/**
 * @author michal.polkorab
 *
 */
@RunWith(MockitoJUnitRunner.class)
public class MultipartRequestTableFeaturesTest {

    private static final byte PADDING_IN_MULTIPART_REQUEST_MESSAGE =
            MultipartRequestInputFactoryTest.PADDING_IN_MULTIPART_REQUEST_MESSAGE;
    private SerializerRegistry registry;
    private OFSerializer<MultipartRequestInput> multipartFactory;

    @Mock SerializerRegistry mockRegistry;
    @Mock OFSerializer<TableFeatureProperties> serializer;

    /**
     * Initializes serializer registry and stores correct factory in field
     */
    @Before
    public void startUp() {
        Mockito.when(mockRegistry.getSerializer((MessageTypeKey<?>)Matchers.any()))
        .thenReturn(serializer);
        registry = new SerializerRegistryImpl();
        registry.init();
        multipartFactory = registry.getSerializer(
                new MessageTypeKey<>(EncodeConstants.OF13_VERSION_ID, MultipartRequestInput.class));
    }

    /**
     * @throws Exception
     * Testing of {@link MultipartRequestInputFactory} for correct translation from POJO
     */
    @Test
    public void testMultipartRequestTableFeaturesMessageFactory() throws Exception {
        MultipartRequestInputBuilder builder = new MultipartRequestInputBuilder();
        BufferHelper.setupHeader(builder, EncodeConstants.OF13_VERSION_ID);
        builder.setType(MultipartType.forValue(12));
        builder.setFlags(new MultipartRequestFlags(true));
        MultipartRequestTableFeaturesCaseBuilder caseBuilder = new MultipartRequestTableFeaturesCaseBuilder();
        MultipartRequestTableFeaturesBuilder featuresBuilder = new MultipartRequestTableFeaturesBuilder();
        List<TableFeatures> tableFeaturesList = new ArrayList<>();
        TableFeaturesBuilder tableFeaturesBuilder = new TableFeaturesBuilder();
        tableFeaturesBuilder.setTableId((short) 8);
        tableFeaturesBuilder.setName("AAAABBBBCCCCDDDDEEEEFFFFGGGG");
        tableFeaturesBuilder.setMetadataMatch(new BigInteger(new byte[] {0x00, 0x01, 0x02, 0x03, 0x01, 0x04, 0x08, 0x01}));
        tableFeaturesBuilder.setMetadataWrite(new BigInteger(new byte[] {0x00, 0x07, 0x01, 0x05, 0x01, 0x00, 0x03, 0x01}));
        tableFeaturesBuilder.setConfig(new TableConfig(true));
        tableFeaturesBuilder.setMaxEntries(65L);
        List<TableFeatureProperties> properties = new ArrayList<>();
        TableFeaturePropertiesBuilder propBuilder = new TableFeaturePropertiesBuilder();
        propBuilder.setType(TableFeaturesPropType.OFPTFPTNEXTTABLES);
        NextTableRelatedTableFeaturePropertyBuilder nextPropBuilder =
                new NextTableRelatedTableFeaturePropertyBuilder();
        List<NextTableIds> nextIds = new ArrayList<>();
        nextIds.add(new NextTableIdsBuilder().setTableId((short) 1).build());
        nextIds.add(new NextTableIdsBuilder().setTableId((short) 2).build());
        nextPropBuilder.setNextTableIds(nextIds);
        propBuilder.addAugmentation(NextTableRelatedTableFeatureProperty.class, nextPropBuilder.build());
        properties.add(propBuilder.build());
        propBuilder = new TableFeaturePropertiesBuilder();
        propBuilder.setType(TableFeaturesPropType.OFPTFPTNEXTTABLESMISS);
        nextPropBuilder = new NextTableRelatedTableFeaturePropertyBuilder();
        nextIds = new ArrayList<>();
        nextPropBuilder.setNextTableIds(nextIds);
        propBuilder.addAugmentation(NextTableRelatedTableFeatureProperty.class, nextPropBuilder.build());
        properties.add(propBuilder.build());
        propBuilder = new TableFeaturePropertiesBuilder();
        propBuilder.setType(TableFeaturesPropType.OFPTFPTINSTRUCTIONS);
        InstructionRelatedTableFeaturePropertyBuilder insPropBuilder =
                new InstructionRelatedTableFeaturePropertyBuilder();
        List<Instruction> insIds = new ArrayList<>();
        InstructionBuilder insBuilder = new InstructionBuilder();
        insBuilder.setInstructionChoice(new WriteActionsCaseBuilder().build());
        insIds.add(insBuilder.build());
        insBuilder = new InstructionBuilder();
        insBuilder.setInstructionChoice(new GotoTableCaseBuilder().build());
        insIds.add(insBuilder.build());
        insPropBuilder.setInstruction(insIds);
        propBuilder.addAugmentation(InstructionRelatedTableFeatureProperty.class, insPropBuilder.build());
        properties.add(propBuilder.build());
        propBuilder = new TableFeaturePropertiesBuilder();
        propBuilder.setType(TableFeaturesPropType.OFPTFPTINSTRUCTIONSMISS);
        insPropBuilder = new InstructionRelatedTableFeaturePropertyBuilder();
        insIds = new ArrayList<>();
        insBuilder = new InstructionBuilder();
        insBuilder.setInstructionChoice(new WriteMetadataCaseBuilder().build());
        insIds.add(insBuilder.build());
        insBuilder = new InstructionBuilder();
        insBuilder.setInstructionChoice(new ApplyActionsCaseBuilder().build());
        insIds.add(insBuilder.build());
        insBuilder = new InstructionBuilder();
        insBuilder.setInstructionChoice(new MeterCaseBuilder().build());
        insIds.add(insBuilder.build());
        insBuilder = new InstructionBuilder();
        insBuilder.setInstructionChoice(new ClearActionsCaseBuilder().build());
        insIds.add(insBuilder.build());
        insBuilder = new InstructionBuilder();
        insBuilder.setInstructionChoice(new GotoTableCaseBuilder().build());
        insIds.add(insBuilder.build());
        insPropBuilder.setInstruction(insIds);
        propBuilder.addAugmentation(InstructionRelatedTableFeatureProperty.class, insPropBuilder.build());
        properties.add(propBuilder.build());
        tableFeaturesBuilder.setTableFeatureProperties(properties);
        tableFeaturesList.add(tableFeaturesBuilder.build());
        tableFeaturesBuilder = new TableFeaturesBuilder();
        tableFeaturesBuilder.setTableId((short) 8);
        tableFeaturesBuilder.setName("AAAABBBBCCCCDDDDEEEEFFFFGGGG");
        tableFeaturesBuilder.setMetadataMatch(new BigInteger(new byte[] {0x00, 0x01, 0x02, 0x03, 0x01, 0x04, 0x08, 0x01}));
        tableFeaturesBuilder.setMetadataWrite(new BigInteger(new byte[] {0x00, 0x07, 0x01, 0x05, 0x01, 0x00, 0x03, 0x01}));
        tableFeaturesBuilder.setConfig(new TableConfig(true));
        tableFeaturesBuilder.setMaxEntries(67L);
        properties = new ArrayList<>();
        propBuilder = new TableFeaturePropertiesBuilder();
        propBuilder.setType(TableFeaturesPropType.OFPTFPTWRITEACTIONS);
        ActionRelatedTableFeaturePropertyBuilder actBuilder = new ActionRelatedTableFeaturePropertyBuilder();
        List<Action> actions = new ArrayList<>();
        ActionBuilder actionBuilder = new ActionBuilder();
        actionBuilder.setActionChoice(new OutputActionCaseBuilder().build());
        actions.add(actionBuilder.build());
        actBuilder.setAction(actions);
        propBuilder.addAugmentation(ActionRelatedTableFeatureProperty.class, actBuilder.build());
        properties.add(propBuilder.build());
        propBuilder = new TableFeaturePropertiesBuilder();
        propBuilder.setType(TableFeaturesPropType.OFPTFPTWRITEACTIONSMISS);
        actBuilder = new ActionRelatedTableFeaturePropertyBuilder();
        actions = new ArrayList<>();
        actBuilder.setAction(actions);
        propBuilder.addAugmentation(ActionRelatedTableFeatureProperty.class, actBuilder.build());
        properties.add(propBuilder.build());
        propBuilder = new TableFeaturePropertiesBuilder();
        propBuilder.setType(TableFeaturesPropType.OFPTFPTAPPLYACTIONS);
        actBuilder = new ActionRelatedTableFeaturePropertyBuilder();
        actions = new ArrayList<>();
        actBuilder.setAction(actions);
        propBuilder.addAugmentation(ActionRelatedTableFeatureProperty.class, actBuilder.build());
        properties.add(propBuilder.build());
        propBuilder = new TableFeaturePropertiesBuilder();
        propBuilder.setType(TableFeaturesPropType.OFPTFPTAPPLYACTIONSMISS);
        actBuilder = new ActionRelatedTableFeaturePropertyBuilder();
        actions = new ArrayList<>();
        actBuilder.setAction(actions);
        propBuilder.addAugmentation(ActionRelatedTableFeatureProperty.class, actBuilder.build());
        properties.add(propBuilder.build());
        propBuilder = new TableFeaturePropertiesBuilder();
        propBuilder.setType(TableFeaturesPropType.OFPTFPTMATCH);
        OxmRelatedTableFeaturePropertyBuilder oxmBuilder = new OxmRelatedTableFeaturePropertyBuilder();
        List<MatchEntry> entries = new ArrayList<>();
        MatchEntryBuilder entriesBuilder = new MatchEntryBuilder();
        entriesBuilder.setOxmClass(OpenflowBasicClass.class);
        entriesBuilder.setOxmMatchField(InPhyPort.class);
        entriesBuilder.setHasMask(false);
        entries.add(entriesBuilder.build());
        entriesBuilder = new MatchEntryBuilder();
        entriesBuilder.setOxmClass(OpenflowBasicClass.class);
        entriesBuilder.setOxmMatchField(InPort.class);
        entriesBuilder.setHasMask(false);
        entries.add(entriesBuilder.build());
        oxmBuilder.setMatchEntry(entries);
        propBuilder.addAugmentation(OxmRelatedTableFeatureProperty.class, oxmBuilder.build());
        properties.add(propBuilder.build());
        propBuilder = new TableFeaturePropertiesBuilder();
        propBuilder.setType(TableFeaturesPropType.OFPTFPTWILDCARDS);
        oxmBuilder = new OxmRelatedTableFeaturePropertyBuilder();
        entries = new ArrayList<>();
        oxmBuilder.setMatchEntry(entries);
        propBuilder.addAugmentation(OxmRelatedTableFeatureProperty.class, oxmBuilder.build());
        properties.add(propBuilder.build());
        propBuilder = new TableFeaturePropertiesBuilder();
        propBuilder.setType(TableFeaturesPropType.OFPTFPTWRITESETFIELD);
        oxmBuilder = new OxmRelatedTableFeaturePropertyBuilder();
        entries = new ArrayList<>();
        oxmBuilder.setMatchEntry(entries);
        propBuilder.addAugmentation(OxmRelatedTableFeatureProperty.class, oxmBuilder.build());
        properties.add(propBuilder.build());
        propBuilder = new TableFeaturePropertiesBuilder();
        propBuilder.setType(TableFeaturesPropType.OFPTFPTWRITESETFIELDMISS);
        oxmBuilder = new OxmRelatedTableFeaturePropertyBuilder();
        entries = new ArrayList<>();
        oxmBuilder.setMatchEntry(entries);
        propBuilder.addAugmentation(OxmRelatedTableFeatureProperty.class, oxmBuilder.build());
        properties.add(propBuilder.build());
        propBuilder = new TableFeaturePropertiesBuilder();
        propBuilder.setType(TableFeaturesPropType.OFPTFPTAPPLYSETFIELD);
        oxmBuilder = new OxmRelatedTableFeaturePropertyBuilder();
        entries = new ArrayList<>();
        entriesBuilder = new MatchEntryBuilder();
        entriesBuilder.setOxmClass(OpenflowBasicClass.class);
        entriesBuilder.setOxmMatchField(IpProto.class);
        entriesBuilder.setHasMask(false);
        entries.add(entriesBuilder.build());
        entriesBuilder = new MatchEntryBuilder();
        entriesBuilder.setOxmClass(OpenflowBasicClass.class);
        entriesBuilder.setOxmMatchField(IpEcn.class);
        entriesBuilder.setHasMask(false);
        entries.add(entriesBuilder.build());
        oxmBuilder.setMatchEntry(entries);
        propBuilder.addAugmentation(OxmRelatedTableFeatureProperty.class, oxmBuilder.build());
        properties.add(propBuilder.build());
        propBuilder = new TableFeaturePropertiesBuilder();
        propBuilder.setType(TableFeaturesPropType.OFPTFPTAPPLYSETFIELDMISS);
        oxmBuilder = new OxmRelatedTableFeaturePropertyBuilder();
        entries = new ArrayList<>();
        oxmBuilder.setMatchEntry(entries);
        propBuilder.addAugmentation(OxmRelatedTableFeatureProperty.class, oxmBuilder.build());
        properties.add(propBuilder.build());
        tableFeaturesBuilder.setTableFeatureProperties(properties);
        tableFeaturesList.add(tableFeaturesBuilder.build());
        featuresBuilder.setTableFeatures(tableFeaturesList);
        caseBuilder.setMultipartRequestTableFeatures(featuresBuilder.build());
        builder.setMultipartRequestBody(caseBuilder.build());
        MultipartRequestInput message = builder.build();

        ByteBuf out = UnpooledByteBufAllocator.DEFAULT.buffer();
        multipartFactory.serialize(message, out);

        BufferHelper.checkHeaderV13(out, (byte) 18, 296);
        Assert.assertEquals("Wrong type", 12, out.readUnsignedShort());
        Assert.assertEquals("Wrong flags", 1, out.readUnsignedShort());
        out.skipBytes(PADDING_IN_MULTIPART_REQUEST_MESSAGE);
        Assert.assertEquals("Wrong length", 120, out.readUnsignedShort());
        Assert.assertEquals("Wrong registry-id", 8, out.readUnsignedByte());
        out.skipBytes(5);
        Assert.assertEquals("Wrong name", "AAAABBBBCCCCDDDDEEEEFFFFGGGG",
                ByteBufUtils.decodeNullTerminatedString(out, 32));
        byte[] metadataMatch = new byte[EncodeConstants.SIZE_OF_LONG_IN_BYTES];
        out.readBytes(metadataMatch);
        Assert.assertArrayEquals("Wrong metadata-match",
                new byte[] {0x00, 0x01, 0x02, 0x03, 0x01, 0x04, 0x08, 0x01}, metadataMatch);
        byte[] metadataWrite = new byte[EncodeConstants.SIZE_OF_LONG_IN_BYTES];
        out.readBytes(metadataWrite);
        Assert.assertArrayEquals("Wrong metadata-write",
                new byte[] {0x00, 0x07, 0x01, 0x05, 0x01, 0x00, 0x03, 0x01}, metadataWrite);
        Assert.assertEquals("Wrong config", 8, out.readUnsignedInt());
        Assert.assertEquals("Wrong max-entries", 65, out.readUnsignedInt());
        Assert.assertEquals("Wrong property type", 2, out.readUnsignedShort());
        Assert.assertEquals("Wrong property length", 6, out.readUnsignedShort());
        Assert.assertEquals("Wrong next-registry-id", 1, out.readUnsignedByte());
        Assert.assertEquals("Wrong next-registry-id", 2, out.readUnsignedByte());
        out.skipBytes(2);
        Assert.assertEquals("Wrong property type", 3, out.readUnsignedShort());
        Assert.assertEquals("Wrong property length", 4, out.readUnsignedShort());
        out.skipBytes(4);
        Assert.assertEquals("Wrong property type", 0, out.readUnsignedShort());
        Assert.assertEquals("Wrong property length", 12, out.readUnsignedShort());
        Assert.assertEquals("Wrong instruction type", 3, out.readUnsignedShort());
        Assert.assertEquals("Wrong instruction length", 4, out.readUnsignedShort());
        Assert.assertEquals("Wrong instruction type", 1, out.readUnsignedShort());
        Assert.assertEquals("Wrong instruction length", 4, out.readUnsignedShort());
        out.skipBytes(4);
        Assert.assertEquals("Wrong property type", 1, out.readUnsignedShort());
        Assert.assertEquals("Wrong property length", 24, out.readUnsignedShort());
        Assert.assertEquals("Wrong instruction type", 2, out.readUnsignedShort());
        Assert.assertEquals("Wrong instruction length", 4, out.readUnsignedShort());
        Assert.assertEquals("Wrong instruction type", 4, out.readUnsignedShort());
        Assert.assertEquals("Wrong instruction length", 4, out.readUnsignedShort());
        Assert.assertEquals("Wrong instruction type", 6, out.readUnsignedShort());
        Assert.assertEquals("Wrong instruction length", 4, out.readUnsignedShort());
        Assert.assertEquals("Wrong instruction type", 5, out.readUnsignedShort());
        Assert.assertEquals("Wrong instruction length", 4, out.readUnsignedShort());
        Assert.assertEquals("Wrong instruction type", 1, out.readUnsignedShort());
        Assert.assertEquals("Wrong instruction length", 4, out.readUnsignedShort());
        Assert.assertEquals("Wrong length", 160, out.readUnsignedShort());
        Assert.assertEquals("Wrong registry-id", 8, out.readUnsignedByte());
        out.skipBytes(5);
        Assert.assertEquals("Wrong name", "AAAABBBBCCCCDDDDEEEEFFFFGGGG",
                ByteBufUtils.decodeNullTerminatedString(out, 32));
        metadataMatch = new byte[EncodeConstants.SIZE_OF_LONG_IN_BYTES];
        out.readBytes(metadataMatch);
        Assert.assertArrayEquals("Wrong metadata-match",
                new byte[] {0x00, 0x01, 0x02, 0x03, 0x01, 0x04, 0x08, 0x01}, metadataMatch);
        metadataWrite = new byte[EncodeConstants.SIZE_OF_LONG_IN_BYTES];
        out.readBytes(metadataWrite);
        Assert.assertArrayEquals("Wrong metadata-write",
                new byte[] {0x00, 0x07, 0x01, 0x05, 0x01, 0x00, 0x03, 0x01}, metadataWrite);
        Assert.assertEquals("Wrong config", 8, out.readUnsignedInt());
        Assert.assertEquals("Wrong max-entries", 67, out.readUnsignedInt());
        Assert.assertEquals("Wrong property type", 4, out.readUnsignedShort());
        Assert.assertEquals("Wrong property length", 8, out.readUnsignedShort());
        Assert.assertEquals("Wrong action type", 0, out.readUnsignedShort());
        Assert.assertEquals("Wrong action length", 4, out.readUnsignedShort());
        Assert.assertEquals("Wrong property type", 5, out.readUnsignedShort());
        Assert.assertEquals("Wrong property length", 4, out.readUnsignedShort());
        out.skipBytes(4);
        Assert.assertEquals("Wrong property type", 6, out.readUnsignedShort());
        Assert.assertEquals("Wrong property length", 4, out.readUnsignedShort());
        out.skipBytes(4);
        Assert.assertEquals("Wrong property type", 7, out.readUnsignedShort());
        Assert.assertEquals("Wrong property length", 4, out.readUnsignedShort());
        out.skipBytes(4);
        Assert.assertEquals("Wrong property type", 8, out.readUnsignedShort());
        Assert.assertEquals("Wrong property length", 12, out.readUnsignedShort());
        Assert.assertEquals("Wrong match class", 0x8000, out.readUnsignedShort());
        Assert.assertEquals("Wrong match field&mask", 2, out.readUnsignedByte());
        Assert.assertEquals("Wrong match length", 4, out.readUnsignedByte());
        Assert.assertEquals("Wrong match class", 0x8000, out.readUnsignedShort());
        Assert.assertEquals("Wrong match field&mask", 0, out.readUnsignedByte());
        Assert.assertEquals("Wrong match length", 4, out.readUnsignedByte());
        out.skipBytes(4);
        Assert.assertEquals("Wrong property type", 10, out.readUnsignedShort());
        Assert.assertEquals("Wrong property length", 4, out.readUnsignedShort());
        out.skipBytes(4);
        Assert.assertEquals("Wrong property type", 12, out.readUnsignedShort());
        Assert.assertEquals("Wrong property length", 4, out.readUnsignedShort());
        out.skipBytes(4);
        Assert.assertEquals("Wrong property type", 13, out.readUnsignedShort());
        Assert.assertEquals("Wrong property length", 4, out.readUnsignedShort());
        out.skipBytes(4);
        Assert.assertEquals("Wrong property type", 14, out.readUnsignedShort());
        Assert.assertEquals("Wrong property length", 12, out.readUnsignedShort());
        Assert.assertEquals("Wrong match class", 0x8000, out.readUnsignedShort());
        Assert.assertEquals("Wrong match field&mask", 20, out.readUnsignedByte());
        Assert.assertEquals("Wrong match length", 1, out.readUnsignedByte());
        Assert.assertEquals("Wrong match class", 0x8000, out.readUnsignedShort());
        Assert.assertEquals("Wrong match field&mask", 18, out.readUnsignedByte());
        Assert.assertEquals("Wrong match length", 1, out.readUnsignedByte());
        out.skipBytes(4);
        Assert.assertEquals("Wrong property type", 15, out.readUnsignedShort());
        Assert.assertEquals("Wrong property length", 4, out.readUnsignedShort());
        out.skipBytes(4);
        Assert.assertTrue("Unread data", out.readableBytes() == 0);
    }

    /**
     * @throws Exception
     * Testing of {@link MultipartRequestInputFactory} for correct translation from POJO
     */
    @Test
    public void testMultipartRequestTableFeaturesExperimenter() throws Exception {
        MultipartRequestInputFactory factory = new MultipartRequestInputFactory();
        factory.injectSerializerRegistry(mockRegistry);
        MultipartRequestInputBuilder builder = new MultipartRequestInputBuilder();
        BufferHelper.setupHeader(builder, EncodeConstants.OF13_VERSION_ID);
        builder.setType(MultipartType.forValue(12));
        builder.setFlags(new MultipartRequestFlags(true));
        MultipartRequestTableFeaturesCaseBuilder caseBuilder = new MultipartRequestTableFeaturesCaseBuilder();
        MultipartRequestTableFeaturesBuilder featuresBuilder = new MultipartRequestTableFeaturesBuilder();
        List<TableFeatures> tableFeaturesList = new ArrayList<>();
        TableFeaturesBuilder tableFeaturesBuilder = new TableFeaturesBuilder();
        tableFeaturesBuilder.setTableId((short) 8);
        tableFeaturesBuilder.setName("AAAABBBBCCCCDDDDEEEEFFFFGGGG");
        tableFeaturesBuilder.setMetadataMatch(new BigInteger(new byte[] {0x00, 0x01, 0x02, 0x03, 0x01, 0x04, 0x08, 0x01}));
        tableFeaturesBuilder.setMetadataWrite(new BigInteger(new byte[] {0x00, 0x07, 0x01, 0x05, 0x01, 0x00, 0x03, 0x01}));
        tableFeaturesBuilder.setConfig(new TableConfig(true));
        tableFeaturesBuilder.setMaxEntries(65L);
        List<TableFeatureProperties> properties = new ArrayList<>();
        TableFeaturePropertiesBuilder propBuilder = new TableFeaturePropertiesBuilder();
        propBuilder.setType(TableFeaturesPropType.OFPTFPTEXPERIMENTER);
        ExperimenterIdTableFeaturePropertyBuilder expBuilder = new ExperimenterIdTableFeaturePropertyBuilder();
        expBuilder.setExperimenter(new ExperimenterId(42L));
        propBuilder.addAugmentation(ExperimenterIdTableFeatureProperty.class, expBuilder.build());
        properties.add(propBuilder.build());
        propBuilder = new TableFeaturePropertiesBuilder();
        propBuilder.setType(TableFeaturesPropType.OFPTFPTEXPERIMENTERMISS);
        expBuilder = new ExperimenterIdTableFeaturePropertyBuilder();
        expBuilder.setExperimenter(new ExperimenterId(43L));
        propBuilder.addAugmentation(ExperimenterIdTableFeatureProperty.class, expBuilder.build());
        properties.add(propBuilder.build());
        tableFeaturesBuilder.setTableFeatureProperties(properties);
        tableFeaturesList.add(tableFeaturesBuilder.build());
        featuresBuilder.setTableFeatures(tableFeaturesList);
        caseBuilder.setMultipartRequestTableFeatures(featuresBuilder.build());
        builder.setMultipartRequestBody(caseBuilder.build());
        MultipartRequestInput message = builder.build();

        ByteBuf out = UnpooledByteBufAllocator.DEFAULT.buffer();
        factory.serialize(message, out);

        BufferHelper.checkHeaderV13(out, (byte) 18, 80);
        Assert.assertEquals("Wrong type", 12, out.readUnsignedShort());
        Assert.assertEquals("Wrong flags", 1, out.readUnsignedShort());
        out.skipBytes(PADDING_IN_MULTIPART_REQUEST_MESSAGE);
        Assert.assertEquals("Wrong length", 64, out.readUnsignedShort());
        Assert.assertEquals("Wrong registry-id", 8, out.readUnsignedByte());
        out.skipBytes(5);
        Assert.assertEquals("Wrong name", "AAAABBBBCCCCDDDDEEEEFFFFGGGG",
                ByteBufUtils.decodeNullTerminatedString(out, 32));
        byte[] metadataMatch = new byte[EncodeConstants.SIZE_OF_LONG_IN_BYTES];
        out.readBytes(metadataMatch);
        Assert.assertArrayEquals("Wrong metadata-match",
                new byte[] {0x00, 0x01, 0x02, 0x03, 0x01, 0x04, 0x08, 0x01}, metadataMatch);
        byte[] metadataWrite = new byte[EncodeConstants.SIZE_OF_LONG_IN_BYTES];
        out.readBytes(metadataWrite);
        Assert.assertArrayEquals("Wrong metadata-write",
                new byte[] {0x00, 0x07, 0x01, 0x05, 0x01, 0x00, 0x03, 0x01}, metadataWrite);
        Assert.assertEquals("Wrong config", 8, out.readUnsignedInt());
        Assert.assertEquals("Wrong max-entries", 65, out.readUnsignedInt());
        Mockito.verify(serializer, Mockito.times(2)).serialize(Matchers.any(TableFeatureProperties.class),
                Matchers.any(ByteBuf.class));
    }
}