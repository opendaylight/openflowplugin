/*
 * Copyright (c) 2015 NetIDE Consortium and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowjava.protocol.impl.serialization.factories;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.UnpooledByteBufAllocator;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.opendaylight.openflowjava.protocol.api.extensibility.OFSerializer;
import org.opendaylight.openflowjava.protocol.api.extensibility.SerializerRegistry;
import org.opendaylight.openflowjava.protocol.api.extensibility.SerializerRegistryInjector;
import org.opendaylight.openflowjava.protocol.api.keys.MessageTypeKey;
import org.opendaylight.openflowjava.protocol.api.util.EncodeConstants;
import org.opendaylight.openflowjava.protocol.impl.util.ListSerializer;
import org.opendaylight.openflowjava.protocol.impl.util.TypeKeyMaker;
import org.opendaylight.openflowjava.protocol.impl.util.TypeKeyMakerFactory;
import org.opendaylight.openflowjava.util.ByteBufUtils;
import org.opendaylight.openflowjava.util.ExperimenterSerializerKeyFactory;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev130715.IetfYangUtil;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.augments.rev150225.ActionRelatedTableFeatureProperty;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.augments.rev150225.ExperimenterIdTableFeatureProperty;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.augments.rev150225.InstructionRelatedTableFeatureProperty;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.augments.rev150225.NextTableRelatedTableFeatureProperty;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.augments.rev150225.OxmRelatedTableFeatureProperty;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.augments.rev150225.table.features.properties.container.table.feature.properties.NextTableIds;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.actions.grouping.Action;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.instruction.rev130731.instructions.grouping.Instruction;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.ActionType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.GroupCapabilities;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.GroupTypes;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.MeterBandTypeBitmap;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.MeterFlags;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.MultipartRequestFlags;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.PortConfig;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.PortFeatures;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.PortState;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.TableConfig;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entries.grouping.MatchEntry;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.grouping.Match;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.MeterBandCommons;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.MultipartReplyMessage;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.buckets.grouping.BucketsList;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.meter.band.header.MeterBand;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.meter.band.header.meter.band.MeterBandDropCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.meter.band.header.meter.band.MeterBandDscpRemarkCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.meter.band.header.meter.band.MeterBandExperimenterCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.meter.band.header.meter.band.meter.band.drop._case.MeterBandDrop;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.meter.band.header.meter.band.meter.band.dscp.remark._case.MeterBandDscpRemark;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.meter.band.header.meter.band.meter.band.experimenter._case.MeterBandExperimenter;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.MultipartReplyBody;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.MultipartReplyAggregateCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.MultipartReplyDescCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.MultipartReplyFlowCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.MultipartReplyGroupCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.MultipartReplyGroupDescCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.MultipartReplyGroupFeaturesCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.MultipartReplyMeterCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.MultipartReplyMeterConfigCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.MultipartReplyMeterFeaturesCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.MultipartReplyPortDescCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.MultipartReplyPortStatsCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.MultipartReplyQueueCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.MultipartReplyTableCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.MultipartReplyTableFeaturesCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.aggregate._case.MultipartReplyAggregate;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.desc._case.MultipartReplyDesc;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.flow._case.MultipartReplyFlow;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.flow._case.multipart.reply.flow.FlowStats;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.group._case.MultipartReplyGroup;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.group._case.multipart.reply.group.GroupStats;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.group._case.multipart.reply.group.group.stats.BucketStats;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.group.desc._case.MultipartReplyGroupDesc;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.group.desc._case.multipart.reply.group.desc.GroupDesc;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.group.features._case.MultipartReplyGroupFeatures;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.meter._case.MultipartReplyMeter;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.meter._case.multipart.reply.meter.MeterStats;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.meter._case.multipart.reply.meter.meter.stats.MeterBandStats;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.meter.config._case.MultipartReplyMeterConfig;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.meter.config._case.multipart.reply.meter.config.MeterConfig;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.meter.config._case.multipart.reply.meter.config.meter.config.Bands;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.meter.features._case.MultipartReplyMeterFeatures;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.port.desc._case.MultipartReplyPortDesc;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.port.desc._case.multipart.reply.port.desc.Ports;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.port.stats._case.MultipartReplyPortStats;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.port.stats._case.multipart.reply.port.stats.PortStats;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.queue._case.MultipartReplyQueue;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.queue._case.multipart.reply.queue.QueueStats;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.table._case.MultipartReplyTable;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.table._case.multipart.reply.table.TableStats;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.table.features._case.MultipartReplyTableFeatures;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.table.features._case.multipart.reply.table.features.TableFeatures;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.table.features.properties.grouping.TableFeatureProperties;
import org.opendaylight.yangtools.yang.common.Uint32;

/**
 * Translates MultipartReply messages.
 *
 * @author giuseppex.petralia@intel.com
 */
@SuppressFBWarnings("UWF_FIELD_NOT_INITIALIZED_IN_CONSTRUCTOR") // FB doesn't recognize Objects.requireNonNull
public class MultipartReplyMessageFactory implements OFSerializer<MultipartReplyMessage>, SerializerRegistryInjector {

    private static final byte MESSAGE_TYPE = 19;
    private static final byte PADDING = 4;
    private static final byte PORT_DESC_PADDING_1 = 4;
    private static final byte PORT_DESC_PADDING_2 = 2;
    private static final int FLOW_STATS_LENGTH_INDEX = 0;
    private static final byte FLOW_STATS_PADDING_1 = 1;
    private static final byte FLOW_STATS_PADDING_2 = 6;
    private static final byte AGGREGATE_PADDING = 4;
    private static final byte TABLE_PADDING = 3;
    private static final byte PORT_STATS_PADDING = 4;
    private static final byte GROUP_STATS_PADDING_1 = 2;
    private static final byte GROUP_STATS_PADDING_2 = 4;
    private static final int GROUP_STATS_LENGTH_INDEX = 0;
    private static final int GROUP_DESC_LENGTH_INDEX = 0;
    private static final int BUCKET_LENGTH_INDEX = 0;
    private static final byte GROUP_DESC_PADDING = 1;
    private static final byte BUCKET_PADDING = 4;
    private static final int METER_LENGTH_INDEX = 4;
    private static final byte METER_PADDING = 6;
    private static final int METER_CONFIG_LENGTH_INDEX = 0;
    private static final short LENGTH_OF_METER_BANDS = 16;
    private static final byte METER_FEATURES_PADDING = 2;
    private static final int TABLE_FEATURES_LENGTH_INDEX = 0;
    private static final byte TABLE_FEATURES_PADDING = 5;
    private static final byte INSTRUCTIONS_CODE = 0;
    private static final byte INSTRUCTIONS_MISS_CODE = 1;
    private static final byte NEXT_TABLE_CODE = 2;
    private static final byte NEXT_TABLE_MISS_CODE = 3;
    private static final byte WRITE_ACTIONS_CODE = 4;
    private static final byte WRITE_ACTIONS_MISS_CODE = 5;
    private static final byte APPLY_ACTIONS_CODE = 6;
    private static final byte APPLY_ACTIONS_MISS_CODE = 7;
    private static final byte MATCH_CODE = 8;
    private static final byte WILDCARDS_CODE = 10;
    private static final byte WRITE_SETFIELD_CODE = 12;
    private static final byte WRITE_SETFIELD_MISS_CODE = 13;
    private static final byte APPLY_SETFIELD_CODE = 14;
    private static final byte APPLY_SETFIELD_MISS_CODE = 15;

    private SerializerRegistry registry;

    @Override
    public void injectSerializerRegistry(final SerializerRegistry serializerRegistry) {
        this.registry = serializerRegistry;
    }

    @Override
    public void serialize(final MultipartReplyMessage message, final ByteBuf outBuffer) {
        Objects.requireNonNull(registry);

        ByteBufUtils.writeOFHeader(MESSAGE_TYPE, message, outBuffer, EncodeConstants.EMPTY_LENGTH);
        outBuffer.writeShort(message.getType().getIntValue());
        writeFlags(message.getFlags(), outBuffer);
        outBuffer.writeZero(PADDING);
        switch (message.getType()) {
            case OFPMPDESC:
                serializeDescBody(message.getMultipartReplyBody(), outBuffer);
                break;
            case OFPMPFLOW:
                serializeFlowBody(message.getMultipartReplyBody(), outBuffer, message);
                break;
            case OFPMPAGGREGATE:
                serializeAggregateBody(message.getMultipartReplyBody(), outBuffer);
                break;
            case OFPMPTABLE:
                serializeTableBody(message.getMultipartReplyBody(), outBuffer);
                break;
            case OFPMPPORTSTATS:
                serializePortStatsBody(message.getMultipartReplyBody(), outBuffer);
                break;
            case OFPMPQUEUE:
                serializeQueueBody(message.getMultipartReplyBody(), outBuffer);
                break;
            case OFPMPGROUP:
                serializeGroupBody(message.getMultipartReplyBody(), outBuffer);
                break;
            case OFPMPGROUPDESC:
                serializeGroupDescBody(message.getMultipartReplyBody(), outBuffer, message);
                break;
            case OFPMPGROUPFEATURES:
                serializeGroupFeaturesBody(message.getMultipartReplyBody(), outBuffer);
                break;
            case OFPMPMETER:
                serializeMeterBody(message.getMultipartReplyBody(), outBuffer);
                break;
            case OFPMPMETERCONFIG:
                serializeMeterConfigBody(message.getMultipartReplyBody(), outBuffer);
                break;
            case OFPMPMETERFEATURES:
                serializeMeterFeaturesBody(message.getMultipartReplyBody(), outBuffer);
                break;
            case OFPMPTABLEFEATURES:
                serializeTableFeaturesBody(message.getMultipartReplyBody(), outBuffer);
                break;
            case OFPMPPORTDESC:
                serializePortDescBody(message.getMultipartReplyBody(), outBuffer);
                break;
            case OFPMPEXPERIMENTER:
                serializeExperimenterBody(message.getMultipartReplyBody(), outBuffer);
                break;
            default:
                break;
        }
        ByteBufUtils.updateOFHeaderLength(outBuffer);
    }

    private void serializeExperimenterBody(final MultipartReplyBody body, final ByteBuf outBuffer) {
//        MultipartReplyExperimenterCase experimenterCase = (MultipartReplyExperimenterCase) body;
//        MultipartReplyExperimenter experimenterBody = experimenterCase.getMultipartReplyExperimenter();
        // TODO: experimenterBody does not have get methods
    }

    private static void writeFlags(final MultipartRequestFlags flags, final ByteBuf outBuffer) {
        Map<Integer, Boolean> map = new HashMap<>();
        map.put(0, flags.getOFPMPFREQMORE());
        int bitmap = ByteBufUtils.fillBitMaskFromMap(map);
        outBuffer.writeShort(bitmap);
    }

    private void serializeTableFeaturesBody(final MultipartReplyBody body, final ByteBuf outBuffer) {
        MultipartReplyTableFeaturesCase tableFeaturesCase = (MultipartReplyTableFeaturesCase) body;
        MultipartReplyTableFeatures tableFeatures = tableFeaturesCase.getMultipartReplyTableFeatures();
        for (TableFeatures tableFeature : tableFeatures.getTableFeatures()) {
            ByteBuf tableFeatureBuff = UnpooledByteBufAllocator.DEFAULT.buffer();
            tableFeatureBuff.writeShort(EncodeConstants.EMPTY_LENGTH);
            tableFeatureBuff.writeByte(tableFeature.getTableId().toJava());
            tableFeatureBuff.writeZero(TABLE_FEATURES_PADDING);
            write32String(tableFeature.getName(), tableFeatureBuff);
            tableFeatureBuff.writeBytes(tableFeature.getMetadataMatch());
            tableFeatureBuff.writeZero(64 - tableFeature.getMetadataMatch().length);
            tableFeatureBuff.writeBytes(tableFeature.getMetadataWrite());
            tableFeatureBuff.writeZero(64 - tableFeature.getMetadataWrite().length);
            writeTableConfig(tableFeature.getConfig(), tableFeatureBuff);
            tableFeatureBuff.writeInt(tableFeature.getMaxEntries().intValue());
            for (TableFeatureProperties tableFeatureProp : tableFeature.getTableFeatureProperties()) {
                switch (tableFeatureProp.getType()) {
                    case OFPTFPTINSTRUCTIONS:
                        writeInstructionRelatedTableProperty(tableFeatureBuff, tableFeatureProp, INSTRUCTIONS_CODE);
                        break;
                    case OFPTFPTINSTRUCTIONSMISS:
                        writeInstructionRelatedTableProperty(tableFeatureBuff, tableFeatureProp,
                                INSTRUCTIONS_MISS_CODE);
                        break;
                    case OFPTFPTNEXTTABLES:
                        writeNextTableRelatedTableProperty(tableFeatureBuff, tableFeatureProp, NEXT_TABLE_CODE);
                        break;
                    case OFPTFPTNEXTTABLESMISS:
                        writeNextTableRelatedTableProperty(tableFeatureBuff, tableFeatureProp, NEXT_TABLE_MISS_CODE);
                        break;
                    case OFPTFPTWRITEACTIONS:
                        writeActionsRelatedTableProperty(tableFeatureBuff, tableFeatureProp, WRITE_ACTIONS_CODE);
                        break;
                    case OFPTFPTWRITEACTIONSMISS:
                        writeActionsRelatedTableProperty(tableFeatureBuff, tableFeatureProp, WRITE_ACTIONS_MISS_CODE);
                        break;
                    case OFPTFPTAPPLYACTIONS:
                        writeActionsRelatedTableProperty(tableFeatureBuff, tableFeatureProp, APPLY_ACTIONS_CODE);
                        break;
                    case OFPTFPTAPPLYACTIONSMISS:
                        writeActionsRelatedTableProperty(tableFeatureBuff, tableFeatureProp, APPLY_ACTIONS_MISS_CODE);
                        break;
                    case OFPTFPTMATCH:
                        writeOxmRelatedTableProperty(tableFeatureBuff, tableFeatureProp, MATCH_CODE);
                        break;
                    case OFPTFPTWILDCARDS:
                        writeOxmRelatedTableProperty(tableFeatureBuff, tableFeatureProp, WILDCARDS_CODE);
                        break;
                    case OFPTFPTWRITESETFIELD:
                        writeOxmRelatedTableProperty(tableFeatureBuff, tableFeatureProp, WRITE_SETFIELD_CODE);
                        break;
                    case OFPTFPTWRITESETFIELDMISS:
                        writeOxmRelatedTableProperty(tableFeatureBuff, tableFeatureProp, WRITE_SETFIELD_MISS_CODE);
                        break;
                    case OFPTFPTAPPLYSETFIELD:
                        writeOxmRelatedTableProperty(tableFeatureBuff, tableFeatureProp, APPLY_SETFIELD_CODE);
                        break;
                    case OFPTFPTAPPLYSETFIELDMISS:
                        writeOxmRelatedTableProperty(tableFeatureBuff, tableFeatureProp, APPLY_SETFIELD_MISS_CODE);
                        break;
                    case OFPTFPTEXPERIMENTER:
                    case OFPTFPTEXPERIMENTERMISS:
                        writeExperimenterRelatedTableProperty(tableFeatureBuff, tableFeatureProp);
                        break;
                    default:
                        break;
                }
            }
            tableFeatureBuff.setShort(TABLE_FEATURES_LENGTH_INDEX, tableFeatureBuff.readableBytes());
            outBuffer.writeBytes(tableFeatureBuff);
        }
    }

    private void writeExperimenterRelatedTableProperty(final ByteBuf output, final TableFeatureProperties property) {
        long expId = property.augmentation(ExperimenterIdTableFeatureProperty.class).getExperimenter().getValue()
            .toJava();
        OFSerializer<TableFeatureProperties> serializer = registry.getSerializer(ExperimenterSerializerKeyFactory
                .createMultipartRequestTFSerializerKey(EncodeConstants.OF13_VERSION_ID, expId));
        serializer.serialize(property, output);
    }

    private void writeOxmRelatedTableProperty(final ByteBuf output, final TableFeatureProperties property,
            final byte code) {
        final int startIndex = output.writerIndex();
        output.writeShort(code);
        int lengthIndex = output.writerIndex();
        output.writeShort(EncodeConstants.EMPTY_LENGTH);
        List<MatchEntry> entries = property.augmentation(OxmRelatedTableFeatureProperty.class).getMatchEntry();
        if (entries != null) {
            TypeKeyMaker<MatchEntry> keyMaker = TypeKeyMakerFactory
                    .createMatchEntriesKeyMaker(EncodeConstants.OF13_VERSION_ID);
            ListSerializer.serializeHeaderList(entries, keyMaker, registry, output);
        }
        int length = output.writerIndex() - startIndex;
        output.setShort(lengthIndex, length);
        output.writeZero(paddingNeeded(length));
    }

    private void writeActionsRelatedTableProperty(final ByteBuf output, final TableFeatureProperties property,
            final byte code) {
        final int startIndex = output.writerIndex();
        output.writeShort(code);
        int lengthIndex = output.writerIndex();
        output.writeShort(EncodeConstants.EMPTY_LENGTH);
        List<Action> actions = property.augmentation(ActionRelatedTableFeatureProperty.class).getAction();
        if (actions != null) {
            TypeKeyMaker<Action> keyMaker = TypeKeyMakerFactory.createActionKeyMaker(EncodeConstants.OF13_VERSION_ID);
            ListSerializer.serializeHeaderList(actions, keyMaker, registry, output);
        }
        int length = output.writerIndex() - startIndex;
        output.setShort(lengthIndex, length);
        output.writeZero(paddingNeeded(length));
    }

    private static void writeNextTableRelatedTableProperty(final ByteBuf output, final TableFeatureProperties property,
            final byte code) {
        final int startIndex = output.writerIndex();
        output.writeShort(code);
        int lengthIndex = output.writerIndex();
        output.writeShort(EncodeConstants.EMPTY_LENGTH);
        List<NextTableIds> nextTableIds = property.augmentation(NextTableRelatedTableFeatureProperty.class)
                .getNextTableIds();
        if (nextTableIds != null) {
            for (NextTableIds next : nextTableIds) {
                output.writeByte(next.getTableId().toJava());
            }
        }
        int length = output.writerIndex() - startIndex;
        output.setShort(lengthIndex, length);
        output.writeZero(paddingNeeded(length));
    }

    private void writeInstructionRelatedTableProperty(final ByteBuf output, final TableFeatureProperties property,
            final byte code) {
        final int startIndex = output.writerIndex();
        output.writeShort(code);
        int lengthIndex = output.writerIndex();
        output.writeShort(EncodeConstants.EMPTY_LENGTH);
        List<Instruction> instructions = property.augmentation(InstructionRelatedTableFeatureProperty.class)
                .getInstruction();
        if (instructions != null) {
            TypeKeyMaker<Instruction> keyMaker = TypeKeyMakerFactory
                    .createInstructionKeyMaker(EncodeConstants.OF13_VERSION_ID);
            ListSerializer.serializeHeaderList(instructions, keyMaker, registry, output);
        }
        int length = output.writerIndex() - startIndex;
        output.setShort(lengthIndex, length);
        output.writeZero(paddingNeeded(length));
    }

    private static int paddingNeeded(final int length) {
        int paddingRemainder = length % EncodeConstants.PADDING;
        int result = 0;
        if (paddingRemainder != 0) {
            result = EncodeConstants.PADDING - paddingRemainder;
        }
        return result;
    }

    private static void writeTableConfig(final TableConfig tableConfig, final ByteBuf outBuffer) {
        Map<Integer, Boolean> map = new HashMap<>();
        map.put(0, tableConfig.getOFPTCDEPRECATEDMASK());
        int bitmap = ByteBufUtils.fillBitMaskFromMap(map);
        outBuffer.writeInt(bitmap);
    }

    private static void serializeMeterFeaturesBody(final MultipartReplyBody body, final ByteBuf outBuffer) {
        MultipartReplyMeterFeaturesCase meterFeaturesCase = (MultipartReplyMeterFeaturesCase) body;
        MultipartReplyMeterFeatures meterFeatures = meterFeaturesCase.getMultipartReplyMeterFeatures();
        outBuffer.writeInt(meterFeatures.getMaxMeter().intValue());
        writeBandTypes(meterFeatures.getBandTypes(), outBuffer);
        writeMeterFlags(meterFeatures.getCapabilities(), outBuffer);
        outBuffer.writeByte(meterFeatures.getMaxBands().toJava());
        outBuffer.writeByte(meterFeatures.getMaxColor().toJava());
        outBuffer.writeZero(METER_FEATURES_PADDING);
    }

    private static void writeBandTypes(final MeterBandTypeBitmap bandTypes, final ByteBuf outBuffer) {
        Map<Integer, Boolean> map = new HashMap<>();
        map.put(0, bandTypes.getOFPMBTDROP());
        map.put(1, bandTypes.getOFPMBTDSCPREMARK());
        int bitmap = ByteBufUtils.fillBitMaskFromMap(map);
        outBuffer.writeInt(bitmap);
    }

    private static void serializeMeterConfigBody(final MultipartReplyBody body, final ByteBuf outBuffer) {
        MultipartReplyMeterConfigCase meterConfigCase = (MultipartReplyMeterConfigCase) body;
        MultipartReplyMeterConfig meter = meterConfigCase.getMultipartReplyMeterConfig();
        for (MeterConfig meterConfig : meter.getMeterConfig()) {
            ByteBuf meterConfigBuff = UnpooledByteBufAllocator.DEFAULT.buffer();
            meterConfigBuff.writeShort(EncodeConstants.EMPTY_LENGTH);
            writeMeterFlags(meterConfig.getFlags(), meterConfigBuff);
            meterConfigBuff.writeInt(meterConfig.getMeterId().getValue().intValue());
            for (Bands currentBand : meterConfig.getBands()) {
                MeterBand meterBand = currentBand.getMeterBand();
                if (meterBand instanceof MeterBandDropCase) {
                    MeterBandDropCase dropBandCase = (MeterBandDropCase) meterBand;
                    MeterBandDrop dropBand = dropBandCase.getMeterBandDrop();
                    writeBandCommonFields(dropBand, meterConfigBuff);
                } else if (meterBand instanceof MeterBandDscpRemarkCase) {
                    MeterBandDscpRemarkCase dscpRemarkBandCase = (MeterBandDscpRemarkCase) meterBand;
                    MeterBandDscpRemark dscpRemarkBand = dscpRemarkBandCase.getMeterBandDscpRemark();
                    writeBandCommonFields(dscpRemarkBand, meterConfigBuff);
                } else if (meterBand instanceof MeterBandExperimenterCase) {
                    MeterBandExperimenterCase experimenterBandCase = (MeterBandExperimenterCase) meterBand;
                    MeterBandExperimenter experimenterBand = experimenterBandCase.getMeterBandExperimenter();
                    writeBandCommonFields(experimenterBand, meterConfigBuff);
                }
            }
            meterConfigBuff.setShort(METER_CONFIG_LENGTH_INDEX, meterConfigBuff.readableBytes());
            outBuffer.writeBytes(meterConfigBuff);
        }
    }

    private static void writeBandCommonFields(final MeterBandCommons meterBand, final ByteBuf outBuffer) {
        outBuffer.writeShort(meterBand.getType().getIntValue());
        outBuffer.writeShort(LENGTH_OF_METER_BANDS);
        outBuffer.writeInt(meterBand.getRate().intValue());
        outBuffer.writeInt(meterBand.getBurstSize().intValue());
    }

    private static void writeMeterFlags(final MeterFlags flags, final ByteBuf outBuffer) {
        Map<Integer, Boolean> map = new HashMap<>();
        map.put(0, flags.getOFPMFKBPS());
        map.put(1, flags.getOFPMFPKTPS());
        map.put(2, flags.getOFPMFBURST());
        map.put(3, flags.getOFPMFSTATS());
        int bitmap = ByteBufUtils.fillBitMaskFromMap(map);
        outBuffer.writeShort(bitmap);
    }

    private static void serializeMeterBody(final MultipartReplyBody body, final ByteBuf outBuffer) {
        MultipartReplyMeterCase meterCase = (MultipartReplyMeterCase) body;
        MultipartReplyMeter meter = meterCase.getMultipartReplyMeter();
        for (MeterStats meterStats : meter.getMeterStats()) {
            ByteBuf meterStatsBuff = UnpooledByteBufAllocator.DEFAULT.buffer();
            meterStatsBuff.writeInt(meterStats.getMeterId().getValue().intValue());
            meterStatsBuff.writeInt(EncodeConstants.EMPTY_LENGTH);
            meterStatsBuff.writeZero(METER_PADDING);
            meterStatsBuff.writeInt(meterStats.getFlowCount().intValue());
            meterStatsBuff.writeLong(meterStats.getPacketInCount().longValue());
            meterStatsBuff.writeLong(meterStats.getByteInCount().longValue());
            meterStatsBuff.writeInt(meterStats.getDurationSec().intValue());
            meterStatsBuff.writeInt(meterStats.getDurationNsec().intValue());
            for (MeterBandStats meterBandStats : meterStats.getMeterBandStats()) {
                meterStatsBuff.writeLong(meterBandStats.getPacketBandCount().longValue());
                meterStatsBuff.writeLong(meterBandStats.getByteBandCount().longValue());
            }
            meterStatsBuff.setInt(METER_LENGTH_INDEX, meterStatsBuff.readableBytes());
            outBuffer.writeBytes(meterStatsBuff);
        }
    }

    private static void serializeGroupFeaturesBody(final MultipartReplyBody body, final ByteBuf outBuffer) {
        MultipartReplyGroupFeaturesCase groupFeaturesCase = (MultipartReplyGroupFeaturesCase) body;
        MultipartReplyGroupFeatures groupFeatures = groupFeaturesCase.getMultipartReplyGroupFeatures();
        writeGroupTypes(groupFeatures.getTypes(), outBuffer);
        writeGroupCapabilities(groupFeatures.getCapabilities(), outBuffer);
        for (Uint32 maxGroups : groupFeatures.getMaxGroups()) {
            outBuffer.writeInt(maxGroups.intValue());
        }
        for (ActionType action : groupFeatures.getActionsBitmap()) {
            writeActionType(action, outBuffer);
        }
    }

    private static void writeActionType(final ActionType action, final ByteBuf outBuffer) {
        Map<Integer, Boolean> map = new HashMap<>();
        map.put(0, action.getOFPATOUTPUT());
        map.put(1, action.getOFPATCOPYTTLOUT());
        map.put(2, action.getOFPATCOPYTTLIN());
        map.put(3, action.getOFPATSETMPLSTTL());
        map.put(4, action.getOFPATDECMPLSTTL());
        map.put(5, action.getOFPATPUSHVLAN());
        map.put(6, action.getOFPATPOPVLAN());
        map.put(7, action.getOFPATPUSHMPLS());
        map.put(8, action.getOFPATPOPMPLS());
        map.put(9, action.getOFPATSETQUEUE());
        map.put(10, action.getOFPATGROUP());
        map.put(11, action.getOFPATSETNWTTL());
        map.put(12, action.getOFPATDECNWTTL());
        map.put(13, action.getOFPATSETFIELD());
        map.put(14, action.getOFPATPUSHPBB());
        map.put(15, action.getOFPATPOPPBB());
        map.put(16, action.getOFPATEXPERIMENTER());
        int bitmap = ByteBufUtils.fillBitMaskFromMap(map);
        outBuffer.writeInt(bitmap);
    }

    private static void writeGroupCapabilities(final GroupCapabilities capabilities, final ByteBuf outBuffer) {
        Map<Integer, Boolean> map = new HashMap<>();
        map.put(0, capabilities.getOFPGFCSELECTWEIGHT());
        map.put(1, capabilities.getOFPGFCSELECTLIVENESS());
        map.put(2, capabilities.getOFPGFCCHAINING());
        map.put(3, capabilities.getOFPGFCCHAININGCHECKS());
        int bitmap = ByteBufUtils.fillBitMaskFromMap(map);
        outBuffer.writeInt(bitmap);
    }

    private static void writeGroupTypes(final GroupTypes types, final ByteBuf outBuffer) {
        Map<Integer, Boolean> map = new HashMap<>();
        map.put(0, types.getOFPGTALL());
        map.put(1, types.getOFPGTSELECT());
        map.put(2, types.getOFPGTINDIRECT());
        map.put(3, types.getOFPGTFF());
        int bitmap = ByteBufUtils.fillBitMaskFromMap(map);
        outBuffer.writeInt(bitmap);
    }

    private void serializeGroupDescBody(final MultipartReplyBody body, final ByteBuf outBuffer,
            final MultipartReplyMessage message) {
        MultipartReplyGroupDescCase groupDescCase = (MultipartReplyGroupDescCase) body;
        MultipartReplyGroupDesc group = groupDescCase.getMultipartReplyGroupDesc();
        for (GroupDesc groupDesc : group.getGroupDesc()) {
            ByteBuf groupDescBuff = UnpooledByteBufAllocator.DEFAULT.buffer();
            groupDescBuff.writeShort(EncodeConstants.EMPTY_LENGTH);
            groupDescBuff.writeByte(groupDesc.getType().getIntValue());
            groupDescBuff.writeZero(GROUP_DESC_PADDING);
            groupDescBuff.writeInt(groupDesc.getGroupId().getValue().intValue());
            for (BucketsList bucket : groupDesc.getBucketsList()) {
                ByteBuf bucketBuff = UnpooledByteBufAllocator.DEFAULT.buffer();
                bucketBuff.writeShort(EncodeConstants.EMPTY_LENGTH);
                bucketBuff.writeShort(bucket.getWeight().toJava());
                bucketBuff.writeInt(bucket.getWatchPort().getValue().intValue());
                bucketBuff.writeInt(bucket.getWatchGroup().intValue());
                bucketBuff.writeZero(BUCKET_PADDING);
                ListSerializer.serializeList(bucket.getAction(),
                        TypeKeyMakerFactory.createActionKeyMaker(message.getVersion().toJava()), registry, bucketBuff);
                bucketBuff.setShort(BUCKET_LENGTH_INDEX, bucketBuff.readableBytes());
                groupDescBuff.writeBytes(bucketBuff);
            }
            groupDescBuff.setShort(GROUP_DESC_LENGTH_INDEX, groupDescBuff.readableBytes());
            outBuffer.writeBytes(groupDescBuff);
        }
    }

    private static void serializeGroupBody(final MultipartReplyBody body, final ByteBuf outBuffer) {
        MultipartReplyGroupCase groupCase = (MultipartReplyGroupCase) body;
        MultipartReplyGroup group = groupCase.getMultipartReplyGroup();
        for (GroupStats groupStats : group.getGroupStats()) {
            ByteBuf groupStatsBuff = UnpooledByteBufAllocator.DEFAULT.buffer();
            groupStatsBuff.writeShort(EncodeConstants.EMPTY_LENGTH);
            groupStatsBuff.writeZero(GROUP_STATS_PADDING_1);
            groupStatsBuff.writeInt(groupStats.getGroupId().getValue().intValue());
            groupStatsBuff.writeInt(groupStats.getRefCount().intValue());
            groupStatsBuff.writeZero(GROUP_STATS_PADDING_2);
            groupStatsBuff.writeLong(groupStats.getPacketCount().longValue());
            groupStatsBuff.writeLong(groupStats.getByteCount().longValue());
            groupStatsBuff.writeInt(groupStats.getDurationSec().intValue());
            groupStatsBuff.writeInt(groupStats.getDurationNsec().intValue());
            for (BucketStats bucketStats : groupStats.getBucketStats()) {
                groupStatsBuff.writeLong(bucketStats.getPacketCount().longValue());
                groupStatsBuff.writeLong(bucketStats.getByteCount().longValue());
            }
            groupStatsBuff.setShort(GROUP_STATS_LENGTH_INDEX, groupStatsBuff.readableBytes());
            outBuffer.writeBytes(groupStatsBuff);
        }
    }

    private static void serializeQueueBody(final MultipartReplyBody body, final ByteBuf outBuffer) {
        MultipartReplyQueueCase queueCase = (MultipartReplyQueueCase) body;
        MultipartReplyQueue queue = queueCase.getMultipartReplyQueue();
        for (QueueStats queueStats : queue.getQueueStats()) {
            outBuffer.writeInt(queueStats.getPortNo().intValue());
            outBuffer.writeInt(queueStats.getQueueId().intValue());
            outBuffer.writeLong(queueStats.getTxBytes().longValue());
            outBuffer.writeLong(queueStats.getTxPackets().longValue());
            outBuffer.writeLong(queueStats.getTxErrors().longValue());
            outBuffer.writeInt(queueStats.getDurationSec().intValue());
            outBuffer.writeInt(queueStats.getDurationNsec().intValue());
        }
    }

    private static void serializePortStatsBody(final MultipartReplyBody body, final ByteBuf outBuffer) {
        MultipartReplyPortStatsCase portStatsCase = (MultipartReplyPortStatsCase) body;
        MultipartReplyPortStats portStats = portStatsCase.getMultipartReplyPortStats();
        for (PortStats portStat : portStats.getPortStats()) {
            outBuffer.writeInt(portStat.getPortNo().intValue());
            outBuffer.writeZero(PORT_STATS_PADDING);
            outBuffer.writeLong(portStat.getRxPackets().longValue());
            outBuffer.writeLong(portStat.getTxPackets().longValue());
            outBuffer.writeLong(portStat.getRxBytes().longValue());
            outBuffer.writeLong(portStat.getTxBytes().longValue());
            outBuffer.writeLong(portStat.getRxDropped().longValue());
            outBuffer.writeLong(portStat.getTxDropped().longValue());
            outBuffer.writeLong(portStat.getRxErrors().longValue());
            outBuffer.writeLong(portStat.getTxErrors().longValue());
            outBuffer.writeLong(portStat.getRxFrameErr().longValue());
            outBuffer.writeLong(portStat.getRxOverErr().longValue());
            outBuffer.writeLong(portStat.getRxCrcErr().longValue());
            outBuffer.writeLong(portStat.getCollisions().longValue());
            outBuffer.writeInt(portStat.getDurationSec().intValue());
            outBuffer.writeInt(portStat.getDurationNsec().intValue());
        }
    }

    private static void serializeTableBody(final MultipartReplyBody body, final ByteBuf outBuffer) {
        MultipartReplyTableCase tableCase = (MultipartReplyTableCase) body;
        MultipartReplyTable table = tableCase.getMultipartReplyTable();
        for (TableStats tableStats : table.getTableStats()) {
            outBuffer.writeByte(tableStats.getTableId().toJava());
            outBuffer.writeZero(TABLE_PADDING);
            outBuffer.writeInt(tableStats.getActiveCount().intValue());
            outBuffer.writeLong(tableStats.getLookupCount().longValue());
            outBuffer.writeLong(tableStats.getMatchedCount().longValue());
        }
    }

    private static void serializeAggregateBody(final MultipartReplyBody body, final ByteBuf outBuffer) {
        MultipartReplyAggregateCase aggregateCase = (MultipartReplyAggregateCase) body;
        MultipartReplyAggregate aggregate = aggregateCase.getMultipartReplyAggregate();
        outBuffer.writeLong(aggregate.getPacketCount().longValue());
        outBuffer.writeLong(aggregate.getByteCount().longValue());
        outBuffer.writeInt(aggregate.getFlowCount().intValue());
        outBuffer.writeZero(AGGREGATE_PADDING);
    }

    private void serializeFlowBody(final MultipartReplyBody body, final ByteBuf outBuffer,
            final MultipartReplyMessage message) {
        MultipartReplyFlowCase flowCase = (MultipartReplyFlowCase) body;
        MultipartReplyFlow flow = flowCase.getMultipartReplyFlow();
        for (FlowStats flowStats : flow.getFlowStats()) {
            ByteBuf flowStatsBuff = UnpooledByteBufAllocator.DEFAULT.buffer();
            flowStatsBuff.writeShort(EncodeConstants.EMPTY_LENGTH);
            flowStatsBuff.writeByte((byte)flowStats.getTableId().longValue());
            flowStatsBuff.writeZero(FLOW_STATS_PADDING_1);
            flowStatsBuff.writeInt(flowStats.getDurationSec().intValue());
            flowStatsBuff.writeInt(flowStats.getDurationNsec().intValue());
            flowStatsBuff.writeShort(flowStats.getPriority().toJava());
            flowStatsBuff.writeShort(flowStats.getIdleTimeout().toJava());
            flowStatsBuff.writeShort(flowStats.getHardTimeout().toJava());
            flowStatsBuff.writeZero(FLOW_STATS_PADDING_2);
            flowStatsBuff.writeLong(flowStats.getCookie().longValue());
            flowStatsBuff.writeLong(flowStats.getPacketCount().longValue());
            flowStatsBuff.writeLong(flowStats.getByteCount().longValue());
            OFSerializer<Match> matchSerializer = registry.getSerializer(
                    new MessageTypeKey<>(message.getVersion().toJava(), Match.class));
            matchSerializer.serialize(flowStats.getMatch(), flowStatsBuff);
            ListSerializer.serializeList(flowStats.getInstruction(),
                    TypeKeyMakerFactory.createInstructionKeyMaker(message.getVersion().toJava()), registry,
                    flowStatsBuff);

            flowStatsBuff.setShort(FLOW_STATS_LENGTH_INDEX, flowStatsBuff.readableBytes());
            outBuffer.writeBytes(flowStatsBuff);
        }
    }

    private static void serializeDescBody(final MultipartReplyBody body, final ByteBuf outBuffer) {
        MultipartReplyDescCase descCase = (MultipartReplyDescCase) body;
        MultipartReplyDesc desc = descCase.getMultipartReplyDesc();
        write256String(desc.getMfrDesc(), outBuffer);
        write256String(desc.getHwDesc(), outBuffer);
        write256String(desc.getSwDesc(), outBuffer);
        write32String(desc.getSerialNum(), outBuffer);
        write256String(desc.getDpDesc(), outBuffer);
    }

    private static void write256String(final String toWrite, final ByteBuf outBuffer) {
        byte[] nameBytes = toWrite.getBytes(StandardCharsets.UTF_8);
        if (nameBytes.length < 256) {
            byte[] nameBytesPadding = new byte[256];
            int index = 0;
            for (byte b : nameBytes) {
                nameBytesPadding[index] = b;
                index++;
            }
            for (; index < 256; index++) {
                nameBytesPadding[index] = 0x0;
            }
            outBuffer.writeBytes(nameBytesPadding);
        } else {
            outBuffer.writeBytes(nameBytes);
        }
    }

    private static void write32String(final String toWrite, final ByteBuf outBuffer) {
        byte[] nameBytes = toWrite.getBytes(StandardCharsets.UTF_8);
        if (nameBytes.length < 32) {
            byte[] nameBytesPadding = new byte[32];
            int index = 0;
            for (byte b : nameBytes) {
                nameBytesPadding[index] = b;
                index++;
            }
            for (; index < 32; index++) {
                nameBytesPadding[index] = 0x0;
            }
            outBuffer.writeBytes(nameBytesPadding);
        } else {
            outBuffer.writeBytes(nameBytes);
        }
    }

    private static void serializePortDescBody(final MultipartReplyBody body, final ByteBuf outBuffer) {
        MultipartReplyPortDescCase portCase = (MultipartReplyPortDescCase) body;
        MultipartReplyPortDesc portDesc = portCase.getMultipartReplyPortDesc();
        for (Ports port : portDesc.getPorts()) {
            outBuffer.writeInt(port.getPortNo().intValue()); // Assuming PortNo
                                                             // = PortId
            outBuffer.writeZero(PORT_DESC_PADDING_1);
            outBuffer.writeBytes(IetfYangUtil.INSTANCE.macAddressBytes(port.getHwAddr()));
            outBuffer.writeZero(PORT_DESC_PADDING_2);
            writeName(port.getName(), outBuffer);
            writePortConfig(port.getConfig(), outBuffer);
            writePortState(port.getState(), outBuffer);
            writePortFeatures(port.getCurrentFeatures(), outBuffer);
            writePortFeatures(port.getAdvertisedFeatures(), outBuffer);
            writePortFeatures(port.getSupportedFeatures(), outBuffer);
            writePortFeatures(port.getPeerFeatures(), outBuffer);
            outBuffer.writeInt(port.getCurrSpeed().intValue());
            outBuffer.writeInt(port.getMaxSpeed().intValue());
        }
    }

    private static void writeName(final String name, final ByteBuf outBuffer) {
        byte[] nameBytes = name.getBytes(StandardCharsets.UTF_8);
        if (nameBytes.length < 16) {
            byte[] nameBytesPadding = new byte[16];
            int index = 0;
            for (byte b : nameBytes) {
                nameBytesPadding[index] = b;
                index++;
            }
            for (; index < 16; index++) {
                nameBytesPadding[index] = 0x0;
            }
            outBuffer.writeBytes(nameBytesPadding);
        } else {
            outBuffer.writeBytes(nameBytes);
        }
    }

    private static void writePortConfig(final PortConfig config, final ByteBuf outBuffer) {
        Map<Integer, Boolean> map = new HashMap<>();
        map.put(0, config.getPortDown());
        map.put(2, config.getNoRecv());
        map.put(5, config.getNoFwd());
        map.put(6, config.getNoPacketIn());
        int bitmap = ByteBufUtils.fillBitMaskFromMap(map);
        outBuffer.writeInt(bitmap);
    }

    private static void writePortState(final PortState state, final ByteBuf outBuffer) {
        Map<Integer, Boolean> map = new HashMap<>();
        map.put(0, state.getLinkDown());
        map.put(1, state.getBlocked());
        map.put(2, state.getLive());
        int bitmap = ByteBufUtils.fillBitMaskFromMap(map);
        outBuffer.writeInt(bitmap);
    }

    private static void writePortFeatures(final PortFeatures features, final ByteBuf outBuffer) {
        Map<Integer, Boolean> map = new HashMap<>();
        map.put(0, features.get_10mbHd());
        map.put(1, features.get_10mbFd());
        map.put(2, features.get_100mbHd());
        map.put(3, features.get_100mbFd());
        map.put(4, features.get_1gbHd());
        map.put(5, features.get_1gbFd());
        map.put(6, features.get_10gbFd());
        map.put(7, features.get_40gbFd());
        map.put(8, features.get_100gbFd());
        map.put(9, features.get_1tbFd());
        map.put(10, features.getOther());
        map.put(11, features.getCopper());
        map.put(12, features.getFiber());
        map.put(13, features.getAutoneg());
        map.put(14, features.getPause());
        map.put(15, features.getPauseAsym());
        int bitmap = ByteBufUtils.fillBitMaskFromMap(map);
        outBuffer.writeInt(bitmap);
    }
}
