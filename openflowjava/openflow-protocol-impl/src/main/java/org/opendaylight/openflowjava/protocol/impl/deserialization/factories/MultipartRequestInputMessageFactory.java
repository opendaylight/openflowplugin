/*
 * Copyright (c) 2015 NetIDE Consortium and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowjava.protocol.impl.deserialization.factories;

import static java.util.Objects.requireNonNull;
import static org.opendaylight.yangtools.yang.common.netty.ByteBufUtils.readUint32;
import static org.opendaylight.yangtools.yang.common.netty.ByteBufUtils.readUint64;
import static org.opendaylight.yangtools.yang.common.netty.ByteBufUtils.readUint8;

import io.netty.buffer.ByteBuf;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.opendaylight.openflowjava.protocol.api.extensibility.DeserializerRegistry;
import org.opendaylight.openflowjava.protocol.api.extensibility.OFDeserializer;
import org.opendaylight.openflowjava.protocol.api.keys.MessageCodeKey;
import org.opendaylight.openflowjava.protocol.api.util.EncodeConstants;
import org.opendaylight.openflowjava.protocol.impl.util.CodeKeyMaker;
import org.opendaylight.openflowjava.protocol.impl.util.CodeKeyMakerFactory;
import org.opendaylight.openflowjava.protocol.impl.util.ListDeserializer;
import org.opendaylight.openflowjava.util.ByteBufUtils;
import org.opendaylight.openflowjava.util.ExperimenterDeserializerKeyFactory;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.augments.rev150225.ActionRelatedTableFeaturePropertyBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.augments.rev150225.InstructionRelatedTableFeaturePropertyBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.augments.rev150225.NextTableRelatedTableFeaturePropertyBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.augments.rev150225.OxmRelatedTableFeaturePropertyBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.augments.rev150225.table.features.properties.container.table.feature.properties.NextTableIds;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.augments.rev150225.table.features.properties.container.table.feature.properties.NextTableIdsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.actions.grouping.Action;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.instruction.rev130731.instructions.grouping.Instruction;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.GroupId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.MeterId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.MultipartRequestFlags;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.MultipartType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.TableConfig;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.TableFeaturesPropType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entries.grouping.MatchEntry;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.grouping.Match;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.MultipartRequestInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.MultipartRequestInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.MultipartRequestAggregateCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.MultipartRequestAggregateCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.MultipartRequestDescCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.MultipartRequestDescCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.MultipartRequestExperimenterCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.MultipartRequestExperimenterCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.MultipartRequestFlowCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.MultipartRequestFlowCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.MultipartRequestGroupCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.MultipartRequestGroupCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.MultipartRequestGroupDescCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.MultipartRequestGroupDescCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.MultipartRequestGroupFeaturesCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.MultipartRequestGroupFeaturesCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.MultipartRequestMeterCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.MultipartRequestMeterCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.MultipartRequestMeterConfigCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.MultipartRequestMeterConfigCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.MultipartRequestMeterFeaturesCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.MultipartRequestMeterFeaturesCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.MultipartRequestPortDescCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.MultipartRequestPortDescCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.MultipartRequestPortStatsCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.MultipartRequestPortStatsCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.MultipartRequestQueueCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.MultipartRequestQueueCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.MultipartRequestTableCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.MultipartRequestTableCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.MultipartRequestTableFeaturesCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.MultipartRequestTableFeaturesCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.multipart.request.aggregate._case.MultipartRequestAggregateBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.multipart.request.desc._case.MultipartRequestDescBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.multipart.request.experimenter._case.MultipartRequestExperimenterBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.multipart.request.flow._case.MultipartRequestFlowBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.multipart.request.group._case.MultipartRequestGroupBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.multipart.request.group.desc._case.MultipartRequestGroupDescBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.multipart.request.group.features._case.MultipartRequestGroupFeaturesBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.multipart.request.meter._case.MultipartRequestMeterBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.multipart.request.meter.config._case.MultipartRequestMeterConfigBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.multipart.request.meter.features._case.MultipartRequestMeterFeaturesBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.multipart.request.port.desc._case.MultipartRequestPortDescBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.multipart.request.port.stats._case.MultipartRequestPortStatsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.multipart.request.queue._case.MultipartRequestQueueBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.multipart.request.table._case.MultipartRequestTableBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.multipart.request.table.features._case.MultipartRequestTableFeaturesBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.multipart.request.table.features._case.multipart.request.table.features.TableFeatures;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.multipart.request.table.features._case.multipart.request.table.features.TableFeaturesBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.table.features.properties.grouping.TableFeatureProperties;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.table.features.properties.grouping.TableFeaturePropertiesBuilder;
import org.opendaylight.yangtools.yang.common.Empty;

/**
 * Translates MultipartRequestInput messages.
 *
 * @author giuseppex.petralia@intel.com
 */
public class MultipartRequestInputMessageFactory implements OFDeserializer<MultipartRequestInput> {
    private static final byte PADDING = 4;
    private static final byte FLOW_PADDING_1 = 3;
    private static final byte FLOW_PADDING_2 = 4;
    private static final byte AGGREGATE_PADDING_1 = 3;
    private static final byte AGGREGATE_PADDING_2 = 4;
    private static final byte PADDING_IN_MULTIPART_REQUEST_TABLE_FEATURES = 5;
    private static final byte MAX_TABLE_NAME_LENGTH = 32;
    private static final byte MULTIPART_REQUEST_TABLE_FEATURES_STRUCTURE_LENGTH = 64;
    private static final byte COMMON_PROPERTY_LENGTH = 4;

    private final DeserializerRegistry registry;

    public MultipartRequestInputMessageFactory(final DeserializerRegistry registry) {
        this.registry = requireNonNull(registry);
    }

    @Override
    public MultipartRequestInput deserialize(final ByteBuf rawMessage) {
        Objects.requireNonNull(registry);

        MultipartRequestInputBuilder builder = new MultipartRequestInputBuilder()
                .setVersion(EncodeConstants.OF_VERSION_1_3)
                .setXid(readUint32(rawMessage));
        MultipartType type = getMultipartType(rawMessage.readUnsignedShort());
        builder.setType(type);
        builder.setFlags(getMultipartRequestFlags(rawMessage.readUnsignedShort()));
        rawMessage.skipBytes(PADDING);
        switch (type) {
            case OFPMPDESC:
                builder.setMultipartRequestBody(setDesc(rawMessage));
                break;
            case OFPMPFLOW:
                builder.setMultipartRequestBody(setFlow(rawMessage));
                break;
            case OFPMPAGGREGATE:
                builder.setMultipartRequestBody(setAggregate(rawMessage));
                break;
            case OFPMPTABLE:
                builder.setMultipartRequestBody(setTable(rawMessage));
                break;
            case OFPMPTABLEFEATURES:
                builder.setMultipartRequestBody(setTableFeatures(rawMessage));
                break;
            case OFPMPPORTSTATS:
                builder.setMultipartRequestBody(setPortStats(rawMessage));
                break;
            case OFPMPPORTDESC:
                builder.setMultipartRequestBody(setPortDesc(rawMessage));
                break;
            case OFPMPQUEUE:
                builder.setMultipartRequestBody(setQueue(rawMessage));
                break;
            case OFPMPGROUP:
                builder.setMultipartRequestBody(setGroup(rawMessage));
                break;
            case OFPMPGROUPDESC:
                builder.setMultipartRequestBody(setGroupDesc(rawMessage));
                break;
            case OFPMPGROUPFEATURES:
                builder.setMultipartRequestBody(setGroupFeatures(rawMessage));
                break;
            case OFPMPMETER:
                builder.setMultipartRequestBody(setMeter(rawMessage));
                break;
            case OFPMPMETERCONFIG:
                builder.setMultipartRequestBody(setMeterConfig(rawMessage));
                break;
            case OFPMPMETERFEATURES:
                builder.setMultipartRequestBody(setMeterFeatures(rawMessage));
                break;
            case OFPMPEXPERIMENTER:
                builder.setMultipartRequestBody(setExperimenter(rawMessage));
                break;
            default:
                break;
        }

        return builder.build();
    }

    private static MultipartType getMultipartType(final int input) {
        return MultipartType.forValue(input);
    }

    @SuppressWarnings("checkstyle:AbbreviationAsWordInName")
    private static MultipartRequestFlags getMultipartRequestFlags(final int input) {
        final Boolean _oFPMPFREQMORE = (input & 1 << 0) != 0;
        MultipartRequestFlags flag = new MultipartRequestFlags(_oFPMPFREQMORE);
        return flag;
    }

    private MultipartRequestTableFeaturesCase setTableFeatures(final ByteBuf input) {
        MultipartRequestTableFeaturesCaseBuilder caseBuilder = new MultipartRequestTableFeaturesCaseBuilder();
        MultipartRequestTableFeaturesBuilder tableFeaturesBuilder = new MultipartRequestTableFeaturesBuilder();
        List<TableFeatures> features = new ArrayList<>();
        while (input.readableBytes() > 0) {
            TableFeaturesBuilder featuresBuilder = new TableFeaturesBuilder();
            final int length = input.readUnsignedShort();
            featuresBuilder.setTableId(readUint8(input));
            input.skipBytes(PADDING_IN_MULTIPART_REQUEST_TABLE_FEATURES);
            featuresBuilder.setName(ByteBufUtils.decodeNullTerminatedString(input, MAX_TABLE_NAME_LENGTH));
            featuresBuilder.setMetadataMatch(readUint64(input));
            featuresBuilder.setMetadataWrite(readUint64(input));
            featuresBuilder.setConfig(createTableConfig(input.readUnsignedInt()));
            featuresBuilder.setMaxEntries(readUint32(input));
            featuresBuilder.setTableFeatureProperties(
                    createTableFeaturesProperties(input, length - MULTIPART_REQUEST_TABLE_FEATURES_STRUCTURE_LENGTH));
            features.add(featuresBuilder.build());
        }
        tableFeaturesBuilder.setTableFeatures(features);
        caseBuilder.setMultipartRequestTableFeatures(tableFeaturesBuilder.build());
        return caseBuilder.build();
    }

    private List<TableFeatureProperties> createTableFeaturesProperties(final ByteBuf input, final int length) {
        List<TableFeatureProperties> properties = new ArrayList<>();
        int tableFeaturesLength = length;
        while (tableFeaturesLength > 0) {
            int propStartIndex = input.readerIndex();
            TableFeaturePropertiesBuilder builder = new TableFeaturePropertiesBuilder();
            TableFeaturesPropType type = TableFeaturesPropType.forValue(input.readUnsignedShort());
            builder.setType(type);
            int propertyLength = input.readUnsignedShort();
            int paddingRemainder = propertyLength % EncodeConstants.PADDING;
            tableFeaturesLength -= propertyLength;
            if (type.equals(TableFeaturesPropType.OFPTFPTINSTRUCTIONS)
                    || type.equals(TableFeaturesPropType.OFPTFPTINSTRUCTIONSMISS)) {
                CodeKeyMaker keyMaker = CodeKeyMakerFactory.createInstructionsKeyMaker(EncodeConstants.OF13_VERSION_ID);
                List<Instruction> instructions = ListDeserializer.deserializeHeaders(EncodeConstants.OF13_VERSION_ID,
                        propertyLength - COMMON_PROPERTY_LENGTH, input, keyMaker, registry);
                builder.addAugmentation(new InstructionRelatedTableFeaturePropertyBuilder()
                    .setInstruction(instructions)
                    .build());
            } else if (type.equals(TableFeaturesPropType.OFPTFPTNEXTTABLES)
                    || type.equals(TableFeaturesPropType.OFPTFPTNEXTTABLESMISS)) {
                propertyLength -= COMMON_PROPERTY_LENGTH;
                List<NextTableIds> ids = new ArrayList<>();
                while (propertyLength > 0) {
                    NextTableIdsBuilder nextTableIdsBuilder = new NextTableIdsBuilder();
                    nextTableIdsBuilder.setTableId(readUint8(input));
                    ids.add(nextTableIdsBuilder.build());
                    propertyLength--;
                }
                builder.addAugmentation(new NextTableRelatedTableFeaturePropertyBuilder().setNextTableIds(ids).build());
            } else if (type.equals(TableFeaturesPropType.OFPTFPTWRITEACTIONS)
                    || type.equals(TableFeaturesPropType.OFPTFPTWRITEACTIONSMISS)
                    || type.equals(TableFeaturesPropType.OFPTFPTAPPLYACTIONS)
                    || type.equals(TableFeaturesPropType.OFPTFPTAPPLYACTIONSMISS)) {
                CodeKeyMaker keyMaker = CodeKeyMakerFactory.createActionsKeyMaker(EncodeConstants.OF13_VERSION_ID);
                List<Action> actions = ListDeserializer.deserializeHeaders(EncodeConstants.OF13_VERSION_ID,
                        propertyLength - COMMON_PROPERTY_LENGTH, input, keyMaker, registry);
                builder.addAugmentation(new ActionRelatedTableFeaturePropertyBuilder().setAction(actions).build());
            } else if (type.equals(TableFeaturesPropType.OFPTFPTMATCH)
                    || type.equals(TableFeaturesPropType.OFPTFPTWILDCARDS)
                    || type.equals(TableFeaturesPropType.OFPTFPTWRITESETFIELD)
                    || type.equals(TableFeaturesPropType.OFPTFPTWRITESETFIELDMISS)
                    || type.equals(TableFeaturesPropType.OFPTFPTAPPLYSETFIELD)
                    || type.equals(TableFeaturesPropType.OFPTFPTAPPLYSETFIELDMISS)) {
                CodeKeyMaker keyMaker = CodeKeyMakerFactory.createMatchEntriesKeyMaker(EncodeConstants.OF13_VERSION_ID);
                List<MatchEntry> entries = ListDeserializer.deserializeHeaders(EncodeConstants.OF13_VERSION_ID,
                        propertyLength - COMMON_PROPERTY_LENGTH, input, keyMaker, registry);
                builder.addAugmentation(new OxmRelatedTableFeaturePropertyBuilder().setMatchEntry(entries).build());
            } else if (type.equals(TableFeaturesPropType.OFPTFPTEXPERIMENTER)
                    || type.equals(TableFeaturesPropType.OFPTFPTEXPERIMENTERMISS)) {
                long expId = input.readUnsignedInt();
                input.readerIndex(propStartIndex);
                OFDeserializer<TableFeatureProperties> propDeserializer = registry
                        .getDeserializer(ExperimenterDeserializerKeyFactory
                                .createMultipartReplyTFDeserializerKey(EncodeConstants.OF13_VERSION_ID, expId));
                TableFeatureProperties expProp = propDeserializer.deserialize(input);
                properties.add(expProp);
                continue;
            }
            if (paddingRemainder != 0) {
                input.skipBytes(EncodeConstants.PADDING - paddingRemainder);
                tableFeaturesLength -= EncodeConstants.PADDING - paddingRemainder;
            }
            properties.add(builder.build());
        }
        return properties;
    }

    private static TableConfig createTableConfig(final long input) {
        boolean deprecated = (input & 3) != 0;
        return new TableConfig(deprecated);
    }

    private static MultipartRequestDescCase setDesc(final ByteBuf input) {
        MultipartRequestDescCaseBuilder caseBuilder = new MultipartRequestDescCaseBuilder();
        MultipartRequestDescBuilder descBuilder = new MultipartRequestDescBuilder();
        descBuilder.setEmpty(Empty.getInstance());
        caseBuilder.setMultipartRequestDesc(descBuilder.build());
        return caseBuilder.build();
    }

    private MultipartRequestFlowCase setFlow(final ByteBuf input) {
        final MultipartRequestFlowCaseBuilder caseBuilder = new MultipartRequestFlowCaseBuilder();
        MultipartRequestFlowBuilder flowBuilder = new MultipartRequestFlowBuilder();
        flowBuilder.setTableId(readUint8(input));
        input.skipBytes(FLOW_PADDING_1);
        flowBuilder.setOutPort(readUint32(input));
        flowBuilder.setOutGroup(readUint32(input));
        input.skipBytes(FLOW_PADDING_2);
        flowBuilder.setCookie(readUint64(input));
        flowBuilder.setCookieMask(readUint64(input));
        OFDeserializer<Match> matchDeserializer = registry.getDeserializer(
                new MessageCodeKey(EncodeConstants.OF13_VERSION_ID, EncodeConstants.EMPTY_VALUE, Match.class));
        flowBuilder.setMatch(matchDeserializer.deserialize(input));
        caseBuilder.setMultipartRequestFlow(flowBuilder.build());
        return caseBuilder.build();
    }

    private MultipartRequestAggregateCase setAggregate(final ByteBuf input) {
        final MultipartRequestAggregateCaseBuilder caseBuilder = new MultipartRequestAggregateCaseBuilder();
        MultipartRequestAggregateBuilder aggregateBuilder = new MultipartRequestAggregateBuilder();
        aggregateBuilder.setTableId(readUint8(input));
        input.skipBytes(AGGREGATE_PADDING_1);
        aggregateBuilder.setOutPort(readUint32(input));
        aggregateBuilder.setOutGroup(readUint32(input));
        input.skipBytes(AGGREGATE_PADDING_2);
        aggregateBuilder.setCookie(readUint64(input));
        aggregateBuilder.setCookieMask(readUint64(input));
        OFDeserializer<Match> matchDeserializer = registry.getDeserializer(
                new MessageCodeKey(EncodeConstants.OF13_VERSION_ID, EncodeConstants.EMPTY_VALUE, Match.class));
        aggregateBuilder.setMatch(matchDeserializer.deserialize(input));
        caseBuilder.setMultipartRequestAggregate(aggregateBuilder.build());
        return caseBuilder.build();
    }

    private static MultipartRequestPortDescCase setPortDesc(final ByteBuf input) {
        MultipartRequestPortDescCaseBuilder caseBuilder = new MultipartRequestPortDescCaseBuilder();
        MultipartRequestPortDescBuilder portBuilder = new MultipartRequestPortDescBuilder();
        portBuilder.setEmpty(Empty.getInstance());
        caseBuilder.setMultipartRequestPortDesc(portBuilder.build());
        return caseBuilder.build();
    }

    private static MultipartRequestPortStatsCase setPortStats(final ByteBuf input) {
        MultipartRequestPortStatsCaseBuilder caseBuilder = new MultipartRequestPortStatsCaseBuilder();
        MultipartRequestPortStatsBuilder portBuilder = new MultipartRequestPortStatsBuilder();
        portBuilder.setPortNo(readUint32(input));
        caseBuilder.setMultipartRequestPortStats(portBuilder.build());
        return caseBuilder.build();
    }

    private static MultipartRequestQueueCase setQueue(final ByteBuf input) {
        MultipartRequestQueueCaseBuilder caseBuilder = new MultipartRequestQueueCaseBuilder();
        MultipartRequestQueueBuilder queueBuilder = new MultipartRequestQueueBuilder();
        queueBuilder.setPortNo(readUint32(input));
        queueBuilder.setQueueId(readUint32(input));
        caseBuilder.setMultipartRequestQueue(queueBuilder.build());
        return caseBuilder.build();
    }

    private static MultipartRequestGroupCase setGroup(final ByteBuf input) {
        MultipartRequestGroupCaseBuilder caseBuilder = new MultipartRequestGroupCaseBuilder();
        MultipartRequestGroupBuilder groupBuilder = new MultipartRequestGroupBuilder();
        groupBuilder.setGroupId(new GroupId(readUint32(input)));
        caseBuilder.setMultipartRequestGroup(groupBuilder.build());
        return caseBuilder.build();
    }

    private static MultipartRequestGroupDescCase setGroupDesc(final ByteBuf input) {
        MultipartRequestGroupDescCaseBuilder caseBuilder = new MultipartRequestGroupDescCaseBuilder();
        MultipartRequestGroupDescBuilder groupBuilder = new MultipartRequestGroupDescBuilder();
        groupBuilder.setEmpty(Empty.getInstance());
        caseBuilder.setMultipartRequestGroupDesc(groupBuilder.build());
        return caseBuilder.build();
    }

    private static MultipartRequestGroupFeaturesCase setGroupFeatures(final ByteBuf input) {
        MultipartRequestGroupFeaturesCaseBuilder caseBuilder = new MultipartRequestGroupFeaturesCaseBuilder();
        MultipartRequestGroupFeaturesBuilder groupBuilder = new MultipartRequestGroupFeaturesBuilder();
        groupBuilder.setEmpty(Empty.getInstance());
        caseBuilder.setMultipartRequestGroupFeatures(groupBuilder.build());
        return caseBuilder.build();
    }

    private static MultipartRequestMeterCase setMeter(final ByteBuf input) {
        MultipartRequestMeterCaseBuilder caseBuilder = new MultipartRequestMeterCaseBuilder();
        MultipartRequestMeterBuilder meterBuilder = new MultipartRequestMeterBuilder();
        meterBuilder.setMeterId(new MeterId(readUint32(input)));
        caseBuilder.setMultipartRequestMeter(meterBuilder.build());
        return caseBuilder.build();
    }

    private static MultipartRequestMeterConfigCase setMeterConfig(final ByteBuf input) {
        MultipartRequestMeterConfigCaseBuilder caseBuilder = new MultipartRequestMeterConfigCaseBuilder();
        MultipartRequestMeterConfigBuilder meterBuilder = new MultipartRequestMeterConfigBuilder();
        meterBuilder.setMeterId(new MeterId(readUint32(input)));
        caseBuilder.setMultipartRequestMeterConfig(meterBuilder.build());
        return caseBuilder.build();
    }

    private static MultipartRequestMeterFeaturesCase setMeterFeatures(final ByteBuf input) {
        MultipartRequestMeterFeaturesCaseBuilder caseBuilder = new MultipartRequestMeterFeaturesCaseBuilder();
        MultipartRequestMeterFeaturesBuilder meterBuilder = new MultipartRequestMeterFeaturesBuilder();
        meterBuilder.setEmpty(Empty.getInstance());
        caseBuilder.setMultipartRequestMeterFeatures(meterBuilder.build());
        return caseBuilder.build();
    }

    private static MultipartRequestTableCase setTable(final ByteBuf input) {
        MultipartRequestTableCaseBuilder caseBuilder = new MultipartRequestTableCaseBuilder();
        MultipartRequestTableBuilder tableBuilder = new MultipartRequestTableBuilder();
        tableBuilder.setEmpty(Empty.getInstance());
        caseBuilder.setMultipartRequestTable(tableBuilder.build());
        return caseBuilder.build();
    }

    private static MultipartRequestExperimenterCase setExperimenter(final ByteBuf input) {
        MultipartRequestExperimenterCaseBuilder caseBuilder = new MultipartRequestExperimenterCaseBuilder();
        MultipartRequestExperimenterBuilder experimenterBuilder = new MultipartRequestExperimenterBuilder();
        caseBuilder.setMultipartRequestExperimenter(experimenterBuilder.build());
        return caseBuilder.build();
    }
}
