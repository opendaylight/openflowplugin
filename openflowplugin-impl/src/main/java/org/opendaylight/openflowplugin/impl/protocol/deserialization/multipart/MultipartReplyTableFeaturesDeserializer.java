/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.protocol.deserialization.multipart;

import static org.opendaylight.yangtools.yang.common.netty.ByteBufUtils.readUint32;
import static org.opendaylight.yangtools.yang.common.netty.ByteBufUtils.readUint64;
import static org.opendaylight.yangtools.yang.common.netty.ByteBufUtils.readUint8;

import io.netty.buffer.ByteBuf;
import java.util.ArrayList;
import java.util.List;
import org.opendaylight.openflowjava.protocol.api.extensibility.DeserializerRegistry;
import org.opendaylight.openflowjava.protocol.api.extensibility.DeserializerRegistryInjector;
import org.opendaylight.openflowjava.protocol.api.extensibility.OFDeserializer;
import org.opendaylight.openflowjava.protocol.api.util.EncodeConstants;
import org.opendaylight.openflowjava.util.ByteBufUtils;
import org.opendaylight.openflowjava.util.ExperimenterDeserializerKeyFactory;
import org.opendaylight.openflowplugin.extension.api.path.ActionPath;
import org.opendaylight.openflowplugin.impl.protocol.deserialization.util.ActionUtil;
import org.opendaylight.openflowplugin.impl.protocol.deserialization.util.InstructionUtil;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.list.Action;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.list.ActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.list.ActionKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.list.InstructionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.list.InstructionKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.multipart.types.rev170112.multipart.reply.MultipartReplyBody;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.TableFeaturesPropType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.TableConfig;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.multipart.reply.multipart.reply.body.MultipartReplyTableFeaturesBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.set.field.match.SetFieldMatch;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table.feature.prop.type.table.feature.prop.type.ApplyActionsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table.feature.prop.type.table.feature.prop.type.ApplyActionsMissBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table.feature.prop.type.table.feature.prop.type.ApplySetfieldBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table.feature.prop.type.table.feature.prop.type.ApplySetfieldMissBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table.feature.prop.type.table.feature.prop.type.InstructionsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table.feature.prop.type.table.feature.prop.type.InstructionsMissBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table.feature.prop.type.table.feature.prop.type.MatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table.feature.prop.type.table.feature.prop.type.NextTableBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table.feature.prop.type.table.feature.prop.type.NextTableMissBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table.feature.prop.type.table.feature.prop.type.WildcardsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table.feature.prop.type.table.feature.prop.type.WriteActionsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table.feature.prop.type.table.feature.prop.type.WriteActionsMissBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table.feature.prop.type.table.feature.prop.type.WriteSetfieldBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table.feature.prop.type.table.feature.prop.type.WriteSetfieldMissBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table.feature.prop.type.table.feature.prop.type.match.MatchSetfieldBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table.feature.prop.type.table.feature.prop.type.next.table.TablesBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table.feature.prop.type.table.feature.prop.type.next.table.miss.TablesMissBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table.feature.prop.type.table.feature.prop.type.wildcards.WildcardSetfieldBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table.features.TableFeatures;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table.features.TableFeaturesBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table.features.TableFeaturesKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table.features.table.features.TableProperties;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table.features.table.features.TablePropertiesBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table.features.table.features.table.properties.TableFeatureProperties;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table.features.table.features.table.properties.TableFeaturePropertiesBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table.features.table.features.table.properties.TableFeaturePropertiesKey;
import org.opendaylight.yangtools.yang.common.Uint8;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MultipartReplyTableFeaturesDeserializer implements OFDeserializer<MultipartReplyBody>,
        DeserializerRegistryInjector {
    private static final Logger LOG = LoggerFactory.getLogger(MultipartReplyTableFeaturesDeserializer.class);
    private static final byte PADDING_IN_MULTIPART_REPLY_TABLE_FEATURES = 5;
    private static final byte MAX_TABLE_NAME_LENGTH = 32;
    private static final byte MULTIPART_REPLY_TABLE_FEATURES_STRUCTURE_LENGTH = 64;
    private static final byte COMMON_PROPERTY_LENGTH = 4;
    private static final TableFeaturesMatchFieldDeserializer MATCH_FIELD_DESERIALIZER =
            new TableFeaturesMatchFieldDeserializer();

    private DeserializerRegistry registry;

    @Override
    public MultipartReplyBody deserialize(final ByteBuf message) {
        final MultipartReplyTableFeaturesBuilder builder = new MultipartReplyTableFeaturesBuilder();
        final List<TableFeatures> items = new ArrayList<>();

        while (message.readableBytes() > 0) {
            final int itemLength = message.readUnsignedShort();
            final TableFeaturesBuilder itemBuilder = new TableFeaturesBuilder()
                    .setTableId(readUint8(message));

            message.skipBytes(PADDING_IN_MULTIPART_REPLY_TABLE_FEATURES);

            items.add(itemBuilder
                    .withKey(new TableFeaturesKey(itemBuilder.getTableId()))
                    .setName(ByteBufUtils.decodeNullTerminatedString(message, MAX_TABLE_NAME_LENGTH))
                    .setMetadataMatch(readUint64(message))
                    .setMetadataWrite(readUint64(message))
                    .setConfig(readTableConfig(message))
                    .setMaxEntries(readUint32(message))
                    .setTableProperties(readTableProperties(message,
                            itemLength - MULTIPART_REPLY_TABLE_FEATURES_STRUCTURE_LENGTH))
                    .build());
        }

        return builder
                .setTableFeatures(items)
                .build();
    }

    private static TableConfig readTableConfig(final ByteBuf message) {
        final long input = message.readUnsignedInt();
        final boolean deprecated = (input & 3) != 0;

        return new TableConfig(deprecated);
    }

    private TableProperties readTableProperties(final ByteBuf message, final int length) {
        final List<TableFeatureProperties> items = new ArrayList<>();
        int tableFeaturesLength = length;
        int order = 0;
        while (tableFeaturesLength > 0) {
            final int propStartIndex = message.readerIndex();
            final TableFeaturesPropType propType = TableFeaturesPropType.forValue(message.readUnsignedShort());
            int propertyLength = message.readUnsignedShort();
            final int paddingRemainder = propertyLength % EncodeConstants.PADDING;
            tableFeaturesLength -= propertyLength;
            final int commonPropertyLength = propertyLength - COMMON_PROPERTY_LENGTH;
            final TableFeaturePropertiesBuilder propBuilder = new TableFeaturePropertiesBuilder()
                .setOrder(order)
                .withKey(new TableFeaturePropertiesKey(order));

            switch (propType) {
                case OFPTFPTINSTRUCTIONS:
                    propBuilder.setTableFeaturePropType(new InstructionsBuilder()
                            .setInstructions(new org.opendaylight.yang.gen.v1.urn.opendaylight.table.types
                                .rev131026.table.feature.prop.type.table.feature.prop.type.instructions
                                .InstructionsBuilder()
                                .setInstruction(readInstructions(message, commonPropertyLength))
                                .build())
                            .build());
                    break;
                case OFPTFPTINSTRUCTIONSMISS:
                    propBuilder.setTableFeaturePropType(new InstructionsMissBuilder()
                            .setInstructionsMiss(new org.opendaylight.yang.gen.v1.urn.opendaylight.table.types
                                .rev131026.table.feature.prop.type.table.feature.prop.type.instructions.miss
                                .InstructionsMissBuilder()
                                .setInstruction(readInstructions(message, commonPropertyLength))
                                .build())
                            .build());
                    break;
                case OFPTFPTNEXTTABLES:
                    propBuilder.setTableFeaturePropType(new NextTableBuilder()
                            .setTables(new TablesBuilder()
                                .setTableIds(readNextTableIds(message, commonPropertyLength))
                                .build())
                            .build());
                    break;
                case OFPTFPTNEXTTABLESMISS:
                    propBuilder.setTableFeaturePropType(new NextTableMissBuilder()
                            .setTablesMiss(new TablesMissBuilder()
                                .setTableIds(readNextTableIds(message, commonPropertyLength))
                                .build())
                            .build());
                    break;
                case OFPTFPTWRITEACTIONS:
                    propBuilder.setTableFeaturePropType(new WriteActionsBuilder()
                            .setWriteActions(new org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026
                                .table.feature.prop.type.table.feature.prop.type.write.actions.WriteActionsBuilder()
                                .setAction(readActions(message, commonPropertyLength))
                                .build())
                            .build());
                    break;
                case OFPTFPTWRITEACTIONSMISS:
                    propBuilder.setTableFeaturePropType(new WriteActionsMissBuilder()
                            .setWriteActionsMiss(new org.opendaylight.yang.gen.v1.urn.opendaylight.table.types
                                .rev131026.table.feature.prop.type.table.feature.prop.type.write.actions.miss
                                .WriteActionsMissBuilder()
                                .setAction(readActions(message, commonPropertyLength))
                                .build())
                            .build());
                    break;
                case OFPTFPTAPPLYACTIONS:
                    propBuilder.setTableFeaturePropType(new ApplyActionsBuilder()
                            .setApplyActions(new org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026
                                .table.feature.prop.type.table.feature.prop.type.apply.actions.ApplyActionsBuilder()
                                .setAction(readActions(message, commonPropertyLength))
                                .build())
                            .build());
                    break;
                case OFPTFPTAPPLYACTIONSMISS:
                    propBuilder.setTableFeaturePropType(new ApplyActionsMissBuilder()
                            .setApplyActionsMiss(new org.opendaylight.yang.gen.v1.urn.opendaylight.table.types
                                .rev131026.table.feature.prop.type.table.feature.prop.type.apply.actions.miss
                                .ApplyActionsMissBuilder()
                                .setAction(readActions(message, commonPropertyLength))
                                .build())
                            .build());
                    break;
                case OFPTFPTMATCH:
                    propBuilder.setTableFeaturePropType(new MatchBuilder()
                            .setMatchSetfield(new MatchSetfieldBuilder()
                                .setSetFieldMatch(readMatchFields(message, commonPropertyLength))
                                .build())
                            .build());
                    break;
                case OFPTFPTWILDCARDS:
                    propBuilder.setTableFeaturePropType(new WildcardsBuilder()
                            .setWildcardSetfield(new WildcardSetfieldBuilder()
                                .setSetFieldMatch(readMatchFields(message, commonPropertyLength))
                                .build())
                            .build());
                    break;
                case OFPTFPTWRITESETFIELD:
                    propBuilder.setTableFeaturePropType(new WriteSetfieldBuilder()
                            .setWriteSetfield(new org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026
                                .table.feature.prop.type.table.feature.prop.type.write.setfield.WriteSetfieldBuilder()
                                .setSetFieldMatch(readMatchFields(message, commonPropertyLength))
                                .build())
                            .build());
                    break;
                case OFPTFPTWRITESETFIELDMISS:
                    propBuilder.setTableFeaturePropType(new WriteSetfieldMissBuilder()
                            .setWriteSetfieldMiss(new org.opendaylight.yang.gen.v1.urn.opendaylight.table.types
                                .rev131026.table.feature.prop.type.table.feature.prop.type.write.setfield.miss
                                .WriteSetfieldMissBuilder()
                                .setSetFieldMatch(readMatchFields(message, commonPropertyLength))
                                .build())
                            .build());
                    break;
                case OFPTFPTAPPLYSETFIELD:
                    propBuilder.setTableFeaturePropType(new ApplySetfieldBuilder()
                            .setApplySetfield(new org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026
                                .table.feature.prop.type.table.feature.prop.type.apply.setfield.ApplySetfieldBuilder()
                                .setSetFieldMatch(readMatchFields(message, commonPropertyLength))
                                .build())
                            .build());
                    break;
                case OFPTFPTAPPLYSETFIELDMISS:
                    propBuilder.setTableFeaturePropType(new ApplySetfieldMissBuilder()
                            .setApplySetfieldMiss(new org.opendaylight.yang.gen.v1.urn.opendaylight.table.types
                                .rev131026.table.feature.prop.type.table.feature.prop.type.apply.setfield.miss
                                .ApplySetfieldMissBuilder()
                                .setSetFieldMatch(readMatchFields(message, commonPropertyLength))
                                .build())
                            .build());
                    break;
                case OFPTFPTEXPERIMENTER:
                case OFPTFPTEXPERIMENTERMISS:
                    final long expId = message.readUnsignedInt();
                    message.readerIndex(propStartIndex);

                    final OFDeserializer<TableFeatureProperties> propDeserializer = registry
                        .getDeserializer(ExperimenterDeserializerKeyFactory
                                .createMultipartReplyTFDeserializerKey(EncodeConstants.OF13_VERSION_ID, expId));

                    // TODO: Finish experimenter table features (currently using OFJava deserialization only to skip
                    // bytes)
                    propDeserializer.deserialize(message);
                    LOG.debug("Table feature property type {} is not handled yet.", propType);
                    break;
                default:
                    // no operation
            }


            if (paddingRemainder != 0) {
                message.skipBytes(EncodeConstants.PADDING - paddingRemainder);
                tableFeaturesLength -= EncodeConstants.PADDING - paddingRemainder;
            }

            items.add(propBuilder.build());
            order++;
        }


        return new TablePropertiesBuilder()
            .setTableFeatureProperties(items)
            .build();
    }

    private static List<SetFieldMatch> readMatchFields(final ByteBuf message, final int length) {
        final List<SetFieldMatch> matchFields = new ArrayList<>();

        final int startIndex = message.readerIndex();

        while (message.readerIndex() - startIndex < length) {
            MATCH_FIELD_DESERIALIZER
                    .deserialize(message)
                    .map(matchFields::add)
                    .orElseGet(() -> {
                        message.skipBytes(2 * Short.BYTES);
                        return Boolean.FALSE;
                    });
        }

        return matchFields;
    }

    private static List<Uint8> readNextTableIds(final ByteBuf message, final int length) {
        final List<Uint8> tableIds = new ArrayList<>();
        int nextTableLength = length;

        while (nextTableLength > 0) {
            tableIds.add(readUint8(message));
            nextTableLength -= 1;
        }

        return tableIds;
    }

    private List<org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.list
            .Instruction> readInstructions(final ByteBuf message, final int length) {

        final List<org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.list
                .Instruction> instructions = new ArrayList<>();
        final int startIndex = message.readerIndex();
        int offset = 0;

        while (message.readerIndex() - startIndex < length) {
            try {
                instructions.add(new InstructionBuilder()
                        .withKey(new InstructionKey(offset))
                        .setOrder(offset)
                        .setInstruction(InstructionUtil
                                .readInstructionHeader(EncodeConstants.OF13_VERSION_ID, message, registry))
                        .build());

                offset++;
            } catch (ClassCastException | IllegalStateException e) {
                message.skipBytes(2 * Short.BYTES);
            }
        }

        return instructions;
    }

    @SuppressWarnings("checkstyle:LineLength")
    private List<Action> readActions(final ByteBuf message, final int length) {
        final List<Action> actions = new ArrayList<>();
        final int startIndex = message.readerIndex();
        int offset = 0;

        while (message.readerIndex() - startIndex < length) {
            try {
                actions.add(new ActionBuilder()
                        .withKey(new ActionKey(offset))
                        .setOrder(offset)
                        .setAction(ActionUtil.readActionHeader(EncodeConstants.OF13_VERSION_ID, message, registry,
                                ActionPath.FLOWS_STATISTICS_UPDATE_APPLY_ACTIONS))
                        .build());

                offset++;
            } catch (ClassCastException | IllegalStateException e) {
                message.skipBytes(2 * Short.BYTES);
            }
        }

        return actions;
    }

    @Override
    public void injectDeserializerRegistry(final DeserializerRegistry deserializerRegistry) {
        registry = deserializerRegistry;
    }

}
