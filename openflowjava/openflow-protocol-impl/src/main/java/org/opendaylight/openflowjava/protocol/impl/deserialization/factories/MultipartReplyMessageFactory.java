/*
 * Copyright (c) 2013 Pantheon Technologies s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowjava.protocol.impl.deserialization.factories;

import io.netty.buffer.ByteBuf;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import org.opendaylight.openflowjava.protocol.api.extensibility.DeserializerRegistry;
import org.opendaylight.openflowjava.protocol.api.extensibility.DeserializerRegistryInjector;
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
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.ActionType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.ExperimenterId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.FlowModFlags;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.GroupCapabilities;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.GroupId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.GroupType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.GroupTypes;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.MeterBandType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.MeterBandTypeBitmap;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.MeterFlags;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.MeterId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.MultipartRequestFlags;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.MultipartType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.PortConfig;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.PortFeatures;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.PortNumber;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.PortState;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.TableConfig;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.TableFeaturesPropType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entries.grouping.MatchEntry;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.grouping.Match;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.MultipartReplyMessage;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.MultipartReplyMessageBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.buckets.grouping.BucketsList;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.buckets.grouping.BucketsListBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.experimenter.core.ExperimenterDataOfChoice;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.meter.band.header.meter.band.MeterBandDropCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.meter.band.header.meter.band.MeterBandDscpRemarkCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.meter.band.header.meter.band.MeterBandExperimenterCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.meter.band.header.meter.band.meter.band.drop._case.MeterBandDropBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.meter.band.header.meter.band.meter.band.dscp.remark._case.MeterBandDscpRemarkBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.MultipartReplyAggregateCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.MultipartReplyAggregateCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.MultipartReplyDescCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.MultipartReplyDescCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.MultipartReplyExperimenterCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.MultipartReplyExperimenterCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.MultipartReplyFlowCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.MultipartReplyFlowCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.MultipartReplyGroupCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.MultipartReplyGroupCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.MultipartReplyGroupDescCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.MultipartReplyGroupDescCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.MultipartReplyGroupFeaturesCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.MultipartReplyGroupFeaturesCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.MultipartReplyMeterCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.MultipartReplyMeterCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.MultipartReplyMeterConfigCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.MultipartReplyMeterConfigCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.MultipartReplyMeterFeaturesCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.MultipartReplyMeterFeaturesCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.MultipartReplyPortDescCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.MultipartReplyPortDescCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.MultipartReplyPortStatsCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.MultipartReplyPortStatsCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.MultipartReplyQueueCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.MultipartReplyQueueCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.MultipartReplyTableCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.MultipartReplyTableCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.MultipartReplyTableFeaturesCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.MultipartReplyTableFeaturesCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.aggregate._case.MultipartReplyAggregateBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.desc._case.MultipartReplyDescBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.experimenter._case.MultipartReplyExperimenterBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.flow._case.MultipartReplyFlowBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.flow._case.multipart.reply.flow.FlowStats;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.flow._case.multipart.reply.flow.FlowStatsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.group._case.MultipartReplyGroupBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.group._case.multipart.reply.group.GroupStats;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.group._case.multipart.reply.group.GroupStatsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.group._case.multipart.reply.group.group.stats.BucketStats;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.group._case.multipart.reply.group.group.stats.BucketStatsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.group.desc._case.MultipartReplyGroupDescBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.group.desc._case.multipart.reply.group.desc.GroupDesc;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.group.desc._case.multipart.reply.group.desc.GroupDescBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.group.features._case.MultipartReplyGroupFeaturesBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.meter._case.MultipartReplyMeterBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.meter._case.multipart.reply.meter.MeterStats;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.meter._case.multipart.reply.meter.MeterStatsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.meter._case.multipart.reply.meter.meter.stats.MeterBandStats;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.meter._case.multipart.reply.meter.meter.stats.MeterBandStatsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.meter.config._case.MultipartReplyMeterConfigBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.meter.config._case.multipart.reply.meter.config.MeterConfig;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.meter.config._case.multipart.reply.meter.config.MeterConfigBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.meter.config._case.multipart.reply.meter.config.meter.config.Bands;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.meter.config._case.multipart.reply.meter.config.meter.config.BandsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.meter.features._case.MultipartReplyMeterFeaturesBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.port.desc._case.MultipartReplyPortDescBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.port.desc._case.multipart.reply.port.desc.Ports;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.port.desc._case.multipart.reply.port.desc.PortsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.port.stats._case.MultipartReplyPortStatsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.port.stats._case.multipart.reply.port.stats.PortStats;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.port.stats._case.multipart.reply.port.stats.PortStatsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.queue._case.MultipartReplyQueueBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.queue._case.multipart.reply.queue.QueueStats;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.queue._case.multipart.reply.queue.QueueStatsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.table._case.MultipartReplyTableBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.table._case.multipart.reply.table.TableStats;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.table._case.multipart.reply.table.TableStatsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.table.features._case.MultipartReplyTableFeaturesBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.table.features._case.multipart.reply.table.features.TableFeatures;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.table.features._case.multipart.reply.table.features.TableFeaturesBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.table.features.properties.grouping.TableFeatureProperties;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.table.features.properties.grouping.TableFeaturePropertiesBuilder;
import org.opendaylight.yangtools.yang.common.Uint32;

/**
 * Translates MultipartReply messages.
 *
 * @author timotej.kubas
 * @author michal.polkorab
 */
public class MultipartReplyMessageFactory implements OFDeserializer<MultipartReplyMessage>,
        DeserializerRegistryInjector {

    private static final byte PADDING_IN_MULTIPART_REPLY_HEADER = 4;
    private static final int DESC_STR_LEN = 256;
    private static final int SERIAL_NUM_LEN = 32;
    private static final byte PADDING_IN_FLOW_STATS_HEADER_01 = 1;
    private static final byte PADDING_IN_FLOW_STATS_HEADER_02 = 4;
    private static final byte PADDING_IN_AGGREGATE_HEADER = 4;
    private static final byte PADDING_IN_TABLE_HEADER = 3;
    private static final byte PADDING_IN_MULTIPART_REPLY_TABLE_FEATURES = 5;
    private static final byte MAX_TABLE_NAME_LENGTH = 32;
    private static final byte MULTIPART_REPLY_TABLE_FEATURES_STRUCTURE_LENGTH = 64;
    private static final byte COMMON_PROPERTY_LENGTH = 4;
    private static final byte PADDING_IN_PORT_STATS_HEADER = 4;
    private static final byte PADDING_IN_GROUP_HEADER_01 = 2;
    private static final byte PADDING_IN_GROUP_HEADER_02 = 4;
    private static final byte BUCKET_COUNTER_LENGTH = 16;
    private static final byte GROUP_BODY_LENGTH = 40;
    private static final byte PADDING_IN_METER_FEATURES_HEADER = 2;
    private static final byte PADDING_IN_METER_STATS_HEADER = 6;
    private static final byte METER_BAND_STATS_LENGTH = 16;
    private static final byte METER_BODY_LENGTH = 40;
    private static final byte METER_CONFIG_LENGTH = 8;
    private static final byte PADDING_IN_METER_BAND_DROP_HEADER = 4;
    private static final byte PADDING_IN_METER_BAND_DSCP_HEADER = 3;
    private static final byte PADDING_IN_PORT_DESC_HEADER_01 = 4;
    private static final byte PADDING_IN_PORT_DESC_HEADER_02 = 2;
    private static final int GROUP_TYPES = 4;
    private static final byte PADDING_IN_GROUP_DESC_HEADER = 1;
    private static final byte PADDING_IN_BUCKETS_HEADER = 4;
    private static final byte GROUP_DESC_HEADER_LENGTH = 8;
    private static final byte BUCKETS_HEADER_LENGTH = 16;
    private DeserializerRegistry registry;

    @Override
    public MultipartReplyMessage deserialize(final ByteBuf rawMessage) {
        MultipartReplyMessageBuilder builder = new MultipartReplyMessageBuilder();
        builder.setVersion((short) EncodeConstants.OF13_VERSION_ID);
        builder.setXid(rawMessage.readUnsignedInt());
        int type = rawMessage.readUnsignedShort();
        builder.setType(MultipartType.forValue(type));
        builder.setFlags(new MultipartRequestFlags((rawMessage.readUnsignedShort() & 0x01) != 0));
        rawMessage.skipBytes(PADDING_IN_MULTIPART_REPLY_HEADER);

        switch (MultipartType.forValue(type)) {
            case OFPMPDESC:
                builder.setMultipartReplyBody(setDesc(rawMessage));
                break;
            case OFPMPFLOW:
                builder.setMultipartReplyBody(setFlow(rawMessage));
                break;
            case OFPMPAGGREGATE:
                builder.setMultipartReplyBody(setAggregate(rawMessage));
                break;
            case OFPMPTABLE:
                builder.setMultipartReplyBody(setTable(rawMessage));
                break;
            case OFPMPPORTSTATS:
                builder.setMultipartReplyBody(setPortStats(rawMessage));
                break;
            case OFPMPQUEUE:
                builder.setMultipartReplyBody(setQueue(rawMessage));
                break;
            case OFPMPGROUP:
                builder.setMultipartReplyBody(setGroup(rawMessage));
                break;
            case OFPMPGROUPDESC:
                builder.setMultipartReplyBody(setGroupDesc(rawMessage));
                break;
            case OFPMPGROUPFEATURES:
                builder.setMultipartReplyBody(setGroupFeatures(rawMessage));
                break;
            case OFPMPMETER:
                builder.setMultipartReplyBody(setMeter(rawMessage));
                break;
            case OFPMPMETERCONFIG:
                builder.setMultipartReplyBody(setMeterConfig(rawMessage));
                break;
            case OFPMPMETERFEATURES:
                builder.setMultipartReplyBody(setMeterFeatures(rawMessage));
                break;
            case OFPMPTABLEFEATURES:
                builder.setMultipartReplyBody(setTableFeatures(rawMessage));
                break;
            case OFPMPPORTDESC:
                builder.setMultipartReplyBody(setPortDesc(rawMessage));
                break;
            case OFPMPEXPERIMENTER:
                builder.setMultipartReplyBody(setExperimenter(rawMessage));
                break;
            default:
                break;
        }

        return builder.build();
    }

    private static MultipartReplyDescCase setDesc(final ByteBuf input) {
        final MultipartReplyDescCaseBuilder caseBuilder = new MultipartReplyDescCaseBuilder();
        MultipartReplyDescBuilder descBuilder = new MultipartReplyDescBuilder();
        byte[] mfrDescBytes = new byte[DESC_STR_LEN];
        input.readBytes(mfrDescBytes);
        String mfrDesc = new String(mfrDescBytes, StandardCharsets.UTF_8);
        descBuilder.setMfrDesc(mfrDesc.trim());
        byte[] hwDescBytes = new byte[DESC_STR_LEN];
        input.readBytes(hwDescBytes);
        String hwDesc = new String(hwDescBytes, StandardCharsets.UTF_8);
        descBuilder.setHwDesc(hwDesc.trim());
        byte[] swDescBytes = new byte[DESC_STR_LEN];
        input.readBytes(swDescBytes);
        String swDesc = new String(swDescBytes, StandardCharsets.UTF_8);
        descBuilder.setSwDesc(swDesc.trim());
        byte[] serialNumBytes = new byte[SERIAL_NUM_LEN];
        input.readBytes(serialNumBytes);
        String serialNum = new String(serialNumBytes, StandardCharsets.UTF_8);
        descBuilder.setSerialNum(serialNum.trim());
        byte[] dpDescBytes = new byte[DESC_STR_LEN];
        input.readBytes(dpDescBytes);
        String dpDesc = new String(dpDescBytes, StandardCharsets.UTF_8);
        descBuilder.setDpDesc(dpDesc.trim());
        caseBuilder.setMultipartReplyDesc(descBuilder.build());
        return caseBuilder.build();
    }

    private MultipartReplyFlowCase setFlow(final ByteBuf input) {
        MultipartReplyFlowCaseBuilder caseBuilder = new MultipartReplyFlowCaseBuilder();
        MultipartReplyFlowBuilder flowBuilder = new MultipartReplyFlowBuilder();
        List<FlowStats> flowStatsList = new ArrayList<>();
        while (input.readableBytes() > 0) {
            FlowStatsBuilder flowStatsBuilder = new FlowStatsBuilder();
            int flowRecordLength = input.readUnsignedShort();
            ByteBuf subInput = input.readSlice(flowRecordLength - EncodeConstants.SIZE_OF_SHORT_IN_BYTES);
            flowStatsBuilder.setTableId(subInput.readUnsignedByte());
            subInput.skipBytes(PADDING_IN_FLOW_STATS_HEADER_01);
            flowStatsBuilder.setDurationSec(subInput.readUnsignedInt());
            flowStatsBuilder.setDurationNsec(subInput.readUnsignedInt());
            flowStatsBuilder.setPriority(subInput.readUnsignedShort());
            flowStatsBuilder.setIdleTimeout(subInput.readUnsignedShort());
            flowStatsBuilder.setHardTimeout(subInput.readUnsignedShort());
            flowStatsBuilder.setFlags(createFlowModFlagsFromBitmap(subInput.readUnsignedShort()));
            subInput.skipBytes(PADDING_IN_FLOW_STATS_HEADER_02);
            byte[] cookie = new byte[EncodeConstants.SIZE_OF_LONG_IN_BYTES];
            subInput.readBytes(cookie);
            flowStatsBuilder.setCookie(new BigInteger(1, cookie));
            byte[] packetCount = new byte[EncodeConstants.SIZE_OF_LONG_IN_BYTES];
            subInput.readBytes(packetCount);
            flowStatsBuilder.setPacketCount(new BigInteger(1, packetCount));
            byte[] byteCount = new byte[EncodeConstants.SIZE_OF_LONG_IN_BYTES];
            subInput.readBytes(byteCount);
            flowStatsBuilder.setByteCount(new BigInteger(1, byteCount));
            OFDeserializer<Match> matchDeserializer = registry.getDeserializer(new MessageCodeKey(
                    EncodeConstants.OF13_VERSION_ID, EncodeConstants.EMPTY_VALUE, Match.class));
            flowStatsBuilder.setMatch(matchDeserializer.deserialize(subInput));
            CodeKeyMaker keyMaker = CodeKeyMakerFactory
                    .createInstructionsKeyMaker(EncodeConstants.OF13_VERSION_ID);
            List<Instruction> instructions = ListDeserializer.deserializeList(
                    EncodeConstants.OF13_VERSION_ID, subInput.readableBytes(), subInput, keyMaker, registry);
            flowStatsBuilder.setInstruction(instructions);
            flowStatsList.add(flowStatsBuilder.build());
        }
        flowBuilder.setFlowStats(flowStatsList);
        caseBuilder.setMultipartReplyFlow(flowBuilder.build());
        return caseBuilder.build();
    }

    @SuppressWarnings("checkstyle:AbbreviationAsWordInName")
    private static FlowModFlags createFlowModFlagsFromBitmap(final int input) {
        final Boolean fmfSENDFLOWREM = (input & 1 << 0) != 0;
        final Boolean fmfCHECKOVERLAP = (input & 1 << 1) != 0;
        final Boolean fmfRESETCOUNTS = (input & 1 << 2) != 0;
        final Boolean fmfNOPKTCOUNTS = (input & 1 << 3) != 0;
        final Boolean fmfNOBYTCOUNTS = (input & 1 << 4) != 0;
        return new FlowModFlags(fmfCHECKOVERLAP, fmfNOBYTCOUNTS, fmfNOPKTCOUNTS, fmfRESETCOUNTS, fmfSENDFLOWREM);
    }

    private static MultipartReplyAggregateCase setAggregate(final ByteBuf input) {
        final MultipartReplyAggregateCaseBuilder caseBuilder = new MultipartReplyAggregateCaseBuilder();
        MultipartReplyAggregateBuilder builder = new MultipartReplyAggregateBuilder();
        byte[] packetCount = new byte[EncodeConstants.SIZE_OF_LONG_IN_BYTES];
        input.readBytes(packetCount);
        builder.setPacketCount(new BigInteger(1, packetCount));
        byte[] byteCount = new byte[EncodeConstants.SIZE_OF_LONG_IN_BYTES];
        input.readBytes(byteCount);
        builder.setByteCount(new BigInteger(1, byteCount));
        builder.setFlowCount(input.readUnsignedInt());
        input.skipBytes(PADDING_IN_AGGREGATE_HEADER);
        caseBuilder.setMultipartReplyAggregate(builder.build());
        return caseBuilder.build();
    }

    private static MultipartReplyTableCase setTable(final ByteBuf input) {
        MultipartReplyTableCaseBuilder caseBuilder = new MultipartReplyTableCaseBuilder();
        MultipartReplyTableBuilder builder = new MultipartReplyTableBuilder();
        List<TableStats> tableStatsList = new ArrayList<>();
        while (input.readableBytes() > 0) {
            TableStatsBuilder tableStatsBuilder = new TableStatsBuilder();
            tableStatsBuilder.setTableId(input.readUnsignedByte());
            input.skipBytes(PADDING_IN_TABLE_HEADER);
            tableStatsBuilder.setActiveCount(input.readUnsignedInt());
            byte[] lookupCount = new byte[EncodeConstants.SIZE_OF_LONG_IN_BYTES];
            input.readBytes(lookupCount);
            tableStatsBuilder.setLookupCount(new BigInteger(1, lookupCount));
            byte[] matchedCount = new byte[EncodeConstants.SIZE_OF_LONG_IN_BYTES];
            input.readBytes(matchedCount);
            tableStatsBuilder.setMatchedCount(new BigInteger(1, matchedCount));
            tableStatsList.add(tableStatsBuilder.build());
        }
        builder.setTableStats(tableStatsList);
        caseBuilder.setMultipartReplyTable(builder.build());
        return caseBuilder.build();
    }

    private MultipartReplyTableFeaturesCase setTableFeatures(final ByteBuf input) {
        MultipartReplyTableFeaturesCaseBuilder caseBuilder = new MultipartReplyTableFeaturesCaseBuilder();
        MultipartReplyTableFeaturesBuilder builder = new MultipartReplyTableFeaturesBuilder();
        List<TableFeatures> features = new ArrayList<>();
        while (input.readableBytes() > 0) {
            TableFeaturesBuilder featuresBuilder = new TableFeaturesBuilder();
            final int length = input.readUnsignedShort();
            featuresBuilder.setTableId(input.readUnsignedByte());
            input.skipBytes(PADDING_IN_MULTIPART_REPLY_TABLE_FEATURES);
            featuresBuilder.setName(ByteBufUtils.decodeNullTerminatedString(input, MAX_TABLE_NAME_LENGTH));
            byte[] metadataMatch = new byte[EncodeConstants.SIZE_OF_LONG_IN_BYTES];
            input.readBytes(metadataMatch);
            featuresBuilder.setMetadataMatch(metadataMatch);
            byte[] metadataWrite = new byte[EncodeConstants.SIZE_OF_LONG_IN_BYTES];
            input.readBytes(metadataWrite);
            featuresBuilder.setMetadataWrite(metadataWrite);
            featuresBuilder.setConfig(createTableConfig(input.readUnsignedInt()));
            featuresBuilder.setMaxEntries(input.readUnsignedInt());
            featuresBuilder.setTableFeatureProperties(createTableFeaturesProperties(input,
                    length - MULTIPART_REPLY_TABLE_FEATURES_STRUCTURE_LENGTH));
            features.add(featuresBuilder.build());
        }
        builder.setTableFeatures(features);
        caseBuilder.setMultipartReplyTableFeatures(builder.build());
        return caseBuilder.build();
    }

    private static TableConfig createTableConfig(final long input) {
        boolean deprecated = (input & 3) != 0;
        return new TableConfig(deprecated);
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
                    nextTableIdsBuilder.setTableId(input.readUnsignedByte());
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
                OFDeserializer<TableFeatureProperties> propDeserializer = registry.getDeserializer(
                        ExperimenterDeserializerKeyFactory.createMultipartReplyTFDeserializerKey(
                                EncodeConstants.OF13_VERSION_ID, expId));
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

    private static MultipartReplyPortStatsCase setPortStats(final ByteBuf input) {
        MultipartReplyPortStatsCaseBuilder caseBuilder = new MultipartReplyPortStatsCaseBuilder();
        MultipartReplyPortStatsBuilder builder = new MultipartReplyPortStatsBuilder();
        List<PortStats> portStatsList = new ArrayList<>();
        while (input.readableBytes() > 0) {
            PortStatsBuilder portStatsBuilder = new PortStatsBuilder();
            portStatsBuilder.setPortNo(input.readUnsignedInt());
            input.skipBytes(PADDING_IN_PORT_STATS_HEADER);
            byte[] rxPackets = new byte[EncodeConstants.SIZE_OF_LONG_IN_BYTES];
            input.readBytes(rxPackets);
            portStatsBuilder.setRxPackets(new BigInteger(1, rxPackets));
            byte[] txPackets = new byte[EncodeConstants.SIZE_OF_LONG_IN_BYTES];
            input.readBytes(txPackets);
            portStatsBuilder.setTxPackets(new BigInteger(1, txPackets));
            byte[] rxBytes = new byte[EncodeConstants.SIZE_OF_LONG_IN_BYTES];
            input.readBytes(rxBytes);
            portStatsBuilder.setRxBytes(new BigInteger(1, rxBytes));
            byte[] txBytes = new byte[EncodeConstants.SIZE_OF_LONG_IN_BYTES];
            input.readBytes(txBytes);
            portStatsBuilder.setTxBytes(new BigInteger(1, txBytes));
            byte[] rxDropped = new byte[EncodeConstants.SIZE_OF_LONG_IN_BYTES];
            input.readBytes(rxDropped);
            portStatsBuilder.setRxDropped(new BigInteger(1, rxDropped));
            byte[] txDropped = new byte[EncodeConstants.SIZE_OF_LONG_IN_BYTES];
            input.readBytes(txDropped);
            portStatsBuilder.setTxDropped(new BigInteger(1, txDropped));
            byte[] rxErrors = new byte[EncodeConstants.SIZE_OF_LONG_IN_BYTES];
            input.readBytes(rxErrors);
            portStatsBuilder.setRxErrors(new BigInteger(1, rxErrors));
            byte[] txErrors = new byte[EncodeConstants.SIZE_OF_LONG_IN_BYTES];
            input.readBytes(txErrors);
            portStatsBuilder.setTxErrors(new BigInteger(1, txErrors));
            byte[] rxFrameErr = new byte[EncodeConstants.SIZE_OF_LONG_IN_BYTES];
            input.readBytes(rxFrameErr);
            portStatsBuilder.setRxFrameErr(new BigInteger(1, rxFrameErr));
            byte[] rxOverErr = new byte[EncodeConstants.SIZE_OF_LONG_IN_BYTES];
            input.readBytes(rxOverErr);
            portStatsBuilder.setRxOverErr(new BigInteger(1, rxOverErr));
            byte[] rxCrcErr = new byte[EncodeConstants.SIZE_OF_LONG_IN_BYTES];
            input.readBytes(rxCrcErr);
            portStatsBuilder.setRxCrcErr(new BigInteger(1, rxCrcErr));
            byte[] collisions = new byte[EncodeConstants.SIZE_OF_LONG_IN_BYTES];
            input.readBytes(collisions);
            portStatsBuilder.setCollisions(new BigInteger(1, collisions));
            portStatsBuilder.setDurationSec(input.readUnsignedInt());
            portStatsBuilder.setDurationNsec(input.readUnsignedInt());
            portStatsList.add(portStatsBuilder.build());
        }
        builder.setPortStats(portStatsList);
        caseBuilder.setMultipartReplyPortStats(builder.build());
        return caseBuilder.build();
    }

    private static MultipartReplyQueueCase setQueue(final ByteBuf input) {
        MultipartReplyQueueCaseBuilder caseBuilder = new MultipartReplyQueueCaseBuilder();
        MultipartReplyQueueBuilder builder = new MultipartReplyQueueBuilder();
        List<QueueStats> queueStatsList = new ArrayList<>();
        while (input.readableBytes() > 0) {
            QueueStatsBuilder queueStatsBuilder = new QueueStatsBuilder();
            queueStatsBuilder.setPortNo(input.readUnsignedInt());
            queueStatsBuilder.setQueueId(input.readUnsignedInt());
            byte[] txBytes = new byte[EncodeConstants.SIZE_OF_LONG_IN_BYTES];
            input.readBytes(txBytes);
            queueStatsBuilder.setTxBytes(new BigInteger(1, txBytes));
            byte[] txPackets = new byte[EncodeConstants.SIZE_OF_LONG_IN_BYTES];
            input.readBytes(txPackets);
            queueStatsBuilder.setTxPackets(new BigInteger(1, txPackets));
            byte[] txErrors = new byte[EncodeConstants.SIZE_OF_LONG_IN_BYTES];
            input.readBytes(txErrors);
            queueStatsBuilder.setTxErrors(new BigInteger(1, txErrors));
            queueStatsBuilder.setDurationSec(input.readUnsignedInt());
            queueStatsBuilder.setDurationNsec(input.readUnsignedInt());
            queueStatsList.add(queueStatsBuilder.build());
        }
        builder.setQueueStats(queueStatsList);
        caseBuilder.setMultipartReplyQueue(builder.build());
        return caseBuilder.build();
    }

    private static MultipartReplyGroupCase setGroup(final ByteBuf input) {
        MultipartReplyGroupCaseBuilder caseBuilder = new MultipartReplyGroupCaseBuilder();
        MultipartReplyGroupBuilder builder = new MultipartReplyGroupBuilder();
        List<GroupStats> groupStatsList = new ArrayList<>();
        while (input.readableBytes() > 0) {
            GroupStatsBuilder groupStatsBuilder = new GroupStatsBuilder();
            final int bodyLength = input.readUnsignedShort();
            input.skipBytes(PADDING_IN_GROUP_HEADER_01);
            groupStatsBuilder.setGroupId(new GroupId(input.readUnsignedInt()));
            groupStatsBuilder.setRefCount(input.readUnsignedInt());
            input.skipBytes(PADDING_IN_GROUP_HEADER_02);
            byte[] packetCount = new byte[EncodeConstants.SIZE_OF_LONG_IN_BYTES];
            input.readBytes(packetCount);
            groupStatsBuilder.setPacketCount(new BigInteger(1, packetCount));
            byte[] byteCount = new byte[EncodeConstants.SIZE_OF_LONG_IN_BYTES];
            input.readBytes(byteCount);
            groupStatsBuilder.setByteCount(new BigInteger(1, byteCount));
            groupStatsBuilder.setDurationSec(input.readUnsignedInt());
            groupStatsBuilder.setDurationNsec(input.readUnsignedInt());
            int actualLength = GROUP_BODY_LENGTH;
            List<BucketStats> bucketStatsList = new ArrayList<>();
            while (actualLength < bodyLength) {
                BucketStatsBuilder bucketStatsBuilder = new BucketStatsBuilder();
                byte[] packetCountBucket = new byte[EncodeConstants.SIZE_OF_LONG_IN_BYTES];
                input.readBytes(packetCountBucket);
                bucketStatsBuilder.setPacketCount(new BigInteger(1, packetCountBucket));
                byte[] byteCountBucket = new byte[EncodeConstants.SIZE_OF_LONG_IN_BYTES];
                input.readBytes(byteCountBucket);
                bucketStatsBuilder.setByteCount(new BigInteger(1, byteCountBucket));
                bucketStatsList.add(bucketStatsBuilder.build());
                actualLength += BUCKET_COUNTER_LENGTH;
            }
            groupStatsBuilder.setBucketStats(bucketStatsList);
            groupStatsList.add(groupStatsBuilder.build());
        }
        builder.setGroupStats(groupStatsList);
        caseBuilder.setMultipartReplyGroup(builder.build());
        return caseBuilder.build();
    }

    private static MultipartReplyMeterFeaturesCase setMeterFeatures(final ByteBuf input) {
        final MultipartReplyMeterFeaturesCaseBuilder caseBuilder = new MultipartReplyMeterFeaturesCaseBuilder();
        MultipartReplyMeterFeaturesBuilder builder = new MultipartReplyMeterFeaturesBuilder();
        builder.setMaxMeter(input.readUnsignedInt());
        builder.setBandTypes(createMeterBandsBitmap(input.readUnsignedInt()));
        builder.setCapabilities(createMeterFlags(input.readUnsignedInt()));
        builder.setMaxBands(input.readUnsignedByte());
        builder.setMaxColor(input.readUnsignedByte());
        input.skipBytes(PADDING_IN_METER_FEATURES_HEADER);
        caseBuilder.setMultipartReplyMeterFeatures(builder.build());
        return caseBuilder.build();
    }

    @SuppressWarnings("checkstyle:AbbreviationAsWordInName")
    private static MeterFlags createMeterFlags(final long input) {
        final Boolean mfKBPS = (input & 1 << 0) != 0;
        final Boolean mfPKTPS = (input & 1 << 1) != 0;
        final Boolean mfBURST = (input & 1 << 2) != 0;
        final Boolean mfSTATS = (input & 1 << 3) != 0;
        return new MeterFlags(mfBURST, mfKBPS, mfPKTPS, mfSTATS);
    }

    @SuppressWarnings("checkstyle:AbbreviationAsWordInName")
    private static MeterBandTypeBitmap createMeterBandsBitmap(final long input) {
        final Boolean mbtDROP = (input & 1 << 1) != 0;
        final Boolean mbtDSCPREMARK = (input & 1 << 2) != 0;
        return new MeterBandTypeBitmap(mbtDROP, mbtDSCPREMARK);
    }

    private static MultipartReplyMeterCase setMeter(final ByteBuf input) {
        MultipartReplyMeterCaseBuilder caseBuilder = new MultipartReplyMeterCaseBuilder();
        MultipartReplyMeterBuilder builder = new MultipartReplyMeterBuilder();
        List<MeterStats> meterStatsList = new ArrayList<>();
        while (input.readableBytes() > 0) {
            MeterStatsBuilder meterStatsBuilder = new MeterStatsBuilder();
            meterStatsBuilder.setMeterId(new MeterId(input.readUnsignedInt()));
            final int meterStatsBodyLength = input.readUnsignedShort();
            input.skipBytes(PADDING_IN_METER_STATS_HEADER);
            meterStatsBuilder.setFlowCount(input.readUnsignedInt());
            byte[] packetInCount = new byte[EncodeConstants.SIZE_OF_LONG_IN_BYTES];
            input.readBytes(packetInCount);
            meterStatsBuilder.setPacketInCount(new BigInteger(1, packetInCount));
            byte[] byteInCount = new byte[EncodeConstants.SIZE_OF_LONG_IN_BYTES];
            input.readBytes(byteInCount);
            meterStatsBuilder.setByteInCount(new BigInteger(1, byteInCount));
            meterStatsBuilder.setDurationSec(input.readUnsignedInt());
            meterStatsBuilder.setDurationNsec(input.readUnsignedInt());
            int actualLength = METER_BODY_LENGTH;
            List<MeterBandStats> meterBandStatsList = new ArrayList<>();
            while (actualLength < meterStatsBodyLength) {
                MeterBandStatsBuilder meterBandStatsBuilder = new MeterBandStatsBuilder();
                byte[] packetBandCount = new byte[EncodeConstants.SIZE_OF_LONG_IN_BYTES];
                input.readBytes(packetBandCount);
                meterBandStatsBuilder.setPacketBandCount(new BigInteger(1, packetBandCount));
                byte[] byteBandCount = new byte[EncodeConstants.SIZE_OF_LONG_IN_BYTES];
                input.readBytes(byteBandCount);
                meterBandStatsBuilder.setByteBandCount(new BigInteger(1, byteBandCount));
                meterBandStatsList.add(meterBandStatsBuilder.build());
                actualLength += METER_BAND_STATS_LENGTH;
            }
            meterStatsBuilder.setMeterBandStats(meterBandStatsList);
            meterStatsList.add(meterStatsBuilder.build());
        }
        builder.setMeterStats(meterStatsList);
        caseBuilder.setMultipartReplyMeter(builder.build());
        return caseBuilder.build();
    }

    private MultipartReplyMeterConfigCase setMeterConfig(final ByteBuf input) {
        MultipartReplyMeterConfigCaseBuilder caseBuilder = new MultipartReplyMeterConfigCaseBuilder();
        MultipartReplyMeterConfigBuilder builder = new MultipartReplyMeterConfigBuilder();
        List<MeterConfig> meterConfigList = new ArrayList<>();
        while (input.readableBytes() > 0) {
            MeterConfigBuilder meterConfigBuilder = new MeterConfigBuilder();
            int meterConfigBodyLength = input.readUnsignedShort();
            meterConfigBuilder.setFlags(createMeterFlags(input.readUnsignedShort()));
            meterConfigBuilder.setMeterId(new MeterId(input.readUnsignedInt()));
            int actualLength = METER_CONFIG_LENGTH;
            List<Bands> bandsList = new ArrayList<>();
            while (actualLength < meterConfigBodyLength) {
                int bandStartIndex = input.readerIndex();
                BandsBuilder bandsBuilder = new BandsBuilder();
                int bandType = input.readUnsignedShort();
                switch (bandType) {
                    case 1:
                        final MeterBandDropCaseBuilder bandDropCaseBuilder = new MeterBandDropCaseBuilder();
                        MeterBandDropBuilder bandDropBuilder = new MeterBandDropBuilder();
                        bandDropBuilder.setType(MeterBandType.forValue(bandType));
                        actualLength += input.readUnsignedShort();
                        bandDropBuilder.setRate(input.readUnsignedInt());
                        bandDropBuilder.setBurstSize(input.readUnsignedInt());
                        input.skipBytes(PADDING_IN_METER_BAND_DROP_HEADER);
                        bandDropCaseBuilder.setMeterBandDrop(bandDropBuilder.build());
                        bandsBuilder.setMeterBand(bandDropCaseBuilder.build());
                        break;
                    case 2:
                        final MeterBandDscpRemarkCaseBuilder bandDscpRemarkCaseBuilder =
                            new MeterBandDscpRemarkCaseBuilder();
                        MeterBandDscpRemarkBuilder bandDscpRemarkBuilder = new MeterBandDscpRemarkBuilder();
                        bandDscpRemarkBuilder.setType(MeterBandType.forValue(bandType));
                        actualLength += input.readUnsignedShort();
                        bandDscpRemarkBuilder.setRate(input.readUnsignedInt());
                        bandDscpRemarkBuilder.setBurstSize(input.readUnsignedInt());
                        bandDscpRemarkBuilder.setPrecLevel(input.readUnsignedByte());
                        input.skipBytes(PADDING_IN_METER_BAND_DSCP_HEADER);
                        bandDscpRemarkCaseBuilder.setMeterBandDscpRemark(bandDscpRemarkBuilder.build());
                        bandsBuilder.setMeterBand(bandDscpRemarkCaseBuilder.build());
                        break;
                    case 0xFFFF:
                        actualLength += input.readUnsignedShort();
                        final long expId = input.getUnsignedInt(input.readerIndex()
                                + 2 * EncodeConstants.SIZE_OF_INT_IN_BYTES);
                        input.readerIndex(bandStartIndex);
                        OFDeserializer<MeterBandExperimenterCase> deserializer = registry.getDeserializer(
                                ExperimenterDeserializerKeyFactory.createMeterBandDeserializerKey(
                                        EncodeConstants.OF13_VERSION_ID, expId));
                        bandsBuilder.setMeterBand(deserializer.deserialize(input));
                        break;
                    default:
                        break;
                }
                bandsList.add(bandsBuilder.build());
            }
            meterConfigBuilder.setBands(bandsList);
            meterConfigList.add(meterConfigBuilder.build());
        }
        builder.setMeterConfig(meterConfigList);
        caseBuilder.setMultipartReplyMeterConfig(builder.build());
        return caseBuilder.build();
    }

    private MultipartReplyExperimenterCase setExperimenter(final ByteBuf input) {
        final long expId = input.readUnsignedInt();
        final long expType = input.readUnsignedInt();

        final OFDeserializer<ExperimenterDataOfChoice> deserializer = registry.getDeserializer(
                ExperimenterDeserializerKeyFactory.createMultipartReplyMessageDeserializerKey(
                        EncodeConstants.OF13_VERSION_ID, expId, expType));

        final MultipartReplyExperimenterBuilder mpExperimenterBld = new MultipartReplyExperimenterBuilder()
                .setExperimenter(new ExperimenterId(expId))
                .setExpType(expType)
                .setExperimenterDataOfChoice(deserializer.deserialize(input));
        final MultipartReplyExperimenterCaseBuilder mpReplyExperimenterCaseBld =
                new MultipartReplyExperimenterCaseBuilder().setMultipartReplyExperimenter(mpExperimenterBld.build());
        return mpReplyExperimenterCaseBld.build();
    }

    private static MultipartReplyPortDescCase setPortDesc(final ByteBuf input) {
        MultipartReplyPortDescCaseBuilder caseBuilder = new MultipartReplyPortDescCaseBuilder();
        MultipartReplyPortDescBuilder builder = new MultipartReplyPortDescBuilder();
        List<Ports> portsList = new ArrayList<>();
        while (input.readableBytes() > 0) {
            PortsBuilder portsBuilder = new PortsBuilder();
            portsBuilder.setPortNo(input.readUnsignedInt());
            input.skipBytes(PADDING_IN_PORT_DESC_HEADER_01);
            portsBuilder.setHwAddr(ByteBufUtils.readIetfMacAddress(input));
            input.skipBytes(PADDING_IN_PORT_DESC_HEADER_02);
            portsBuilder.setName(ByteBufUtils.decodeNullTerminatedString(input, EncodeConstants.MAX_PORT_NAME_LENGTH));
            portsBuilder.setConfig(createPortConfig(input.readUnsignedInt()));
            portsBuilder.setState(createPortState(input.readUnsignedInt()));
            portsBuilder.setCurrentFeatures(createPortFeatures(input.readUnsignedInt()));
            portsBuilder.setAdvertisedFeatures(createPortFeatures(input.readUnsignedInt()));
            portsBuilder.setSupportedFeatures(createPortFeatures(input.readUnsignedInt()));
            portsBuilder.setPeerFeatures(createPortFeatures(input.readUnsignedInt()));
            portsBuilder.setCurrSpeed(input.readUnsignedInt());
            portsBuilder.setMaxSpeed(input.readUnsignedInt());
            portsList.add(portsBuilder.build());
        }
        builder.setPorts(portsList);
        caseBuilder.setMultipartReplyPortDesc(builder.build());
        return caseBuilder.build();
    }

    private static PortConfig createPortConfig(final long input) {
        final Boolean pcPortDown = (input & 1 << 0) != 0;
        final Boolean pcNRecv = (input & 1 << 2) != 0;
        final Boolean pcNFwd = (input & 1 << 5) != 0;
        final Boolean pcNPacketIn = (input & 1 << 6) != 0;
        return new PortConfig(pcNFwd, pcNPacketIn, pcNRecv, pcPortDown);
    }

    private static PortState createPortState(final long input) {
        final Boolean psLinkDown = (input & 1 << 0) != 0;
        final Boolean psBlocked = (input & 1 << 1) != 0;
        final Boolean psLive = (input & 1 << 2) != 0;
        return new PortState(psBlocked, psLinkDown, psLive);
    }

    private static PortFeatures createPortFeatures(final long input) {
        final Boolean pf10mbHd = (input & 1 << 0) != 0;
        final Boolean pf10mbFd = (input & 1 << 1) != 0;
        final Boolean pf100mbHd = (input & 1 << 2) != 0;
        final Boolean pf100mbFd = (input & 1 << 3) != 0;
        final Boolean pf1gbHd = (input & 1 << 4) != 0;
        final Boolean pf1gbFd = (input & 1 << 5) != 0;
        final Boolean pf10gbFd = (input & 1 << 6) != 0;
        final Boolean pf40gbFd = (input & 1 << 7) != 0;
        final Boolean pf100gbFd = (input & 1 << 8) != 0;
        final Boolean pf1tbFd = (input & 1 << 9) != 0;
        final Boolean pfOther = (input & 1 << 10) != 0;
        final Boolean pfCopper = (input & 1 << 11) != 0;
        final Boolean pfFiber = (input & 1 << 12) != 0;
        final Boolean pfAutoneg = (input & 1 << 13) != 0;
        final Boolean pfPause = (input & 1 << 14) != 0;
        final Boolean pfPauseAsym = (input & 1 << 15) != 0;
        return new PortFeatures(pf100gbFd, pf100mbFd, pf100mbHd, pf10gbFd, pf10mbFd, pf10mbHd, pf1gbFd,
                pf1gbHd, pf1tbFd, pf40gbFd, pfAutoneg, pfCopper, pfFiber, pfOther, pfPause, pfPauseAsym);
    }

    private static MultipartReplyGroupFeaturesCase setGroupFeatures(final ByteBuf rawMessage) {
        final MultipartReplyGroupFeaturesCaseBuilder caseBuilder = new MultipartReplyGroupFeaturesCaseBuilder();
        MultipartReplyGroupFeaturesBuilder featuresBuilder = new MultipartReplyGroupFeaturesBuilder();
        featuresBuilder.setTypes(createGroupType(rawMessage.readUnsignedInt()));
        featuresBuilder.setCapabilities(createCapabilities(rawMessage.readUnsignedInt()));
        List<Uint32> maxGroupsList = new ArrayList<>();
        for (int i = 0; i < GROUP_TYPES; i++) {
            maxGroupsList.add(Uint32.valueOf(rawMessage.readUnsignedInt()));
        }
        featuresBuilder.setMaxGroups(maxGroupsList);
        List<ActionType> actionBitmaps = new ArrayList<>();
        for (int i = 0; i < GROUP_TYPES; i++) {
            actionBitmaps.add(createActionBitmap(rawMessage.readUnsignedInt()));
        }
        featuresBuilder.setActionsBitmap(actionBitmaps);
        caseBuilder.setMultipartReplyGroupFeatures(featuresBuilder.build());
        return caseBuilder.build();
    }

    @SuppressWarnings("checkstyle:AbbreviationAsWordInName")
    private static ActionType createActionBitmap(final long input) {
        final Boolean atOutput = (input & 1 << 0) != 0;
        final Boolean atCopyTTLout = (input & 1 << 11) != 0;
        final Boolean atCopyTTLin = (input & 1 << 12) != 0;
        final Boolean atSetMplsTTL = (input & 1 << 15) != 0;
        final Boolean atDecMplsTTL = (input & 1 << 16) != 0;
        final Boolean atPushVLAN = (input & 1 << 17) != 0;
        final Boolean atPopVLAN = (input & 1 << 18) != 0;
        final Boolean atPushMPLS = (input & 1 << 19) != 0;
        final Boolean atPopMPLS = (input & 1 << 20) != 0;
        final Boolean atSetQueue = (input & 1 << 21) != 0;
        final Boolean atGroup = (input & 1 << 22) != 0;
        final Boolean atSetNWTTL = (input & 1 << 23) != 0;
        final Boolean atDecNWTTL = (input & 1 << 24) != 0;
        final Boolean atSetField = (input & 1 << 25) != 0;
        final Boolean atPushPBB = (input & 1 << 26) != 0;
        final Boolean atPopPBB = (input & 1 << 27) != 0;
        final Boolean atExperimenter = false;
        return new ActionType(atCopyTTLin, atCopyTTLout, atDecMplsTTL,
                atDecNWTTL, atExperimenter, atGroup, atOutput, atPopMPLS,
                atPopPBB, atPopVLAN, atPushMPLS, atPushPBB, atPushVLAN,
                atSetField, atSetMplsTTL, atSetNWTTL, atSetQueue);
    }

    private static GroupCapabilities createCapabilities(final long input) {
        final Boolean gcSelectWeight = (input & 1 << 0) != 0;
        final Boolean gcSelectLiveness = (input & 1 << 1) != 0;
        final Boolean gcChaining = (input & 1 << 2) != 0;
        final Boolean gcChainingChecks = (input & 1 << 3) != 0;
        return new GroupCapabilities(gcChaining, gcChainingChecks, gcSelectLiveness, gcSelectWeight);
    }

    private static GroupTypes createGroupType(final long input) {
        final Boolean gtAll = (input & 1 << 0) != 0;
        final Boolean gtSelect = (input & 1 << 1) != 0;
        final Boolean gtIndirect = (input & 1 << 2) != 0;
        final Boolean gtFF = (input & 1 << 3) != 0;
        return new GroupTypes(gtAll, gtFF, gtIndirect, gtSelect);
    }

    private MultipartReplyGroupDescCase setGroupDesc(final ByteBuf input) {
        MultipartReplyGroupDescCaseBuilder caseBuilder = new MultipartReplyGroupDescCaseBuilder();
        MultipartReplyGroupDescBuilder builder = new MultipartReplyGroupDescBuilder();
        List<GroupDesc> groupDescsList = new ArrayList<>();
        while (input.readableBytes() > 0) {
            GroupDescBuilder groupDescBuilder = new GroupDescBuilder();
            final int bodyLength = input.readUnsignedShort();
            groupDescBuilder.setType(GroupType.forValue(input.readUnsignedByte()));
            input.skipBytes(PADDING_IN_GROUP_DESC_HEADER);
            groupDescBuilder.setGroupId(new GroupId(input.readUnsignedInt()));
            int actualLength = GROUP_DESC_HEADER_LENGTH;
            List<BucketsList> bucketsList = new ArrayList<>();
            while (actualLength < bodyLength) {
                BucketsListBuilder bucketsBuilder = new BucketsListBuilder();
                final int bucketsLength = input.readUnsignedShort();
                bucketsBuilder.setWeight(input.readUnsignedShort());
                bucketsBuilder.setWatchPort(new PortNumber(input.readUnsignedInt()));
                bucketsBuilder.setWatchGroup(input.readUnsignedInt());
                input.skipBytes(PADDING_IN_BUCKETS_HEADER);
                CodeKeyMaker keyMaker = CodeKeyMakerFactory.createActionsKeyMaker(EncodeConstants.OF13_VERSION_ID);
                List<Action> actions = ListDeserializer.deserializeList(EncodeConstants.OF13_VERSION_ID,
                        bucketsLength - BUCKETS_HEADER_LENGTH, input, keyMaker, registry);
                bucketsBuilder.setAction(actions);
                bucketsList.add(bucketsBuilder.build());
                actualLength += bucketsLength;
            }
            groupDescBuilder.setBucketsList(bucketsList);
            groupDescsList.add(groupDescBuilder.build());
        }
        builder.setGroupDesc(groupDescsList);
        caseBuilder.setMultipartReplyGroupDesc(builder.build());
        return caseBuilder.build();
    }

    @Override
    public void injectDeserializerRegistry(
            final DeserializerRegistry deserializerRegistry) {
        registry = deserializerRegistry;
    }
}
