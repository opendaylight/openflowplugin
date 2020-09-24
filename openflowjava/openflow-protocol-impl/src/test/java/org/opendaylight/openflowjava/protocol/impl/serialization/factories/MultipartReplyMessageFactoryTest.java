/*
 * Copyright (c) 2015 NetIDE Consortium and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowjava.protocol.impl.serialization.factories;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.UnpooledByteBufAllocator;
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
import org.opendaylight.openflowjava.util.ByteBufUtils;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev130715.MacAddress;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.augments.rev150225.ActionRelatedTableFeaturePropertyBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.augments.rev150225.InstructionRelatedTableFeaturePropertyBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.augments.rev150225.NextTableRelatedTableFeaturePropertyBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.augments.rev150225.OxmRelatedTableFeaturePropertyBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.augments.rev150225.table.features.properties.container.table.feature.properties.NextTableIdsBuilder;
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
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.ActionType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.EtherType;
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
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.InPhyPort;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.InPort;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.IpEcn;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.IpProto;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.OpenflowBasicClass;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.OxmMatchType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entries.grouping.MatchEntry;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entries.grouping.MatchEntryBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.InPhyPortCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.IpEcnCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.in.phy.port._case.InPhyPortBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.ip.ecn._case.IpEcnBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.grouping.MatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.MultipartReplyMessage;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.MultipartReplyMessageBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.buckets.grouping.BucketsList;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.buckets.grouping.BucketsListBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.meter.band.header.meter.band.MeterBandDropCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.meter.band.header.meter.band.MeterBandDscpRemarkCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.meter.band.header.meter.band.meter.band.drop._case.MeterBandDropBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.meter.band.header.meter.band.meter.band.dscp.remark._case.MeterBandDscpRemarkBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.MultipartReplyBody;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.MultipartReplyAggregateCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.MultipartReplyAggregateCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.MultipartReplyDescCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.MultipartReplyDescCaseBuilder;
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
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.MultipartReplyTableFeaturesCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.aggregate._case.MultipartReplyAggregate;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.aggregate._case.MultipartReplyAggregateBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.desc._case.MultipartReplyDescBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.flow._case.MultipartReplyFlow;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.flow._case.MultipartReplyFlowBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.flow._case.multipart.reply.flow.FlowStats;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.flow._case.multipart.reply.flow.FlowStatsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.group._case.MultipartReplyGroup;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.group._case.MultipartReplyGroupBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.group._case.multipart.reply.group.GroupStats;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.group._case.multipart.reply.group.GroupStatsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.group._case.multipart.reply.group.group.stats.BucketStats;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.group._case.multipart.reply.group.group.stats.BucketStatsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.group.desc._case.MultipartReplyGroupDesc;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.group.desc._case.MultipartReplyGroupDescBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.group.desc._case.multipart.reply.group.desc.GroupDesc;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.group.desc._case.multipart.reply.group.desc.GroupDescBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.group.features._case.MultipartReplyGroupFeatures;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.group.features._case.MultipartReplyGroupFeaturesBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.meter._case.MultipartReplyMeter;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.meter._case.MultipartReplyMeterBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.meter._case.multipart.reply.meter.MeterStats;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.meter._case.multipart.reply.meter.MeterStatsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.meter._case.multipart.reply.meter.meter.stats.MeterBandStats;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.meter._case.multipart.reply.meter.meter.stats.MeterBandStatsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.meter.config._case.MultipartReplyMeterConfig;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.meter.config._case.MultipartReplyMeterConfigBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.meter.config._case.multipart.reply.meter.config.MeterConfig;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.meter.config._case.multipart.reply.meter.config.MeterConfigBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.meter.config._case.multipart.reply.meter.config.meter.config.Bands;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.meter.config._case.multipart.reply.meter.config.meter.config.BandsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.meter.features._case.MultipartReplyMeterFeatures;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.meter.features._case.MultipartReplyMeterFeaturesBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.port.desc._case.MultipartReplyPortDesc;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.port.desc._case.MultipartReplyPortDescBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.port.desc._case.multipart.reply.port.desc.Ports;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.port.desc._case.multipart.reply.port.desc.PortsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.port.stats._case.MultipartReplyPortStats;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.port.stats._case.MultipartReplyPortStatsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.port.stats._case.multipart.reply.port.stats.PortStats;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.port.stats._case.multipart.reply.port.stats.PortStatsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.queue._case.MultipartReplyQueue;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.queue._case.MultipartReplyQueueBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.queue._case.multipart.reply.queue.QueueStats;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.queue._case.multipart.reply.queue.QueueStatsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.table._case.MultipartReplyTable;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.table._case.MultipartReplyTableBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.table._case.multipart.reply.table.TableStats;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.table._case.multipart.reply.table.TableStatsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.table.features._case.MultipartReplyTableFeaturesBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.table.features._case.multipart.reply.table.features.TableFeatures;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.table.features._case.multipart.reply.table.features.TableFeaturesBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.table.features.properties.grouping.TableFeatureProperties;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.table.features.properties.grouping.TableFeaturePropertiesBuilder;
import org.opendaylight.yangtools.yang.common.Uint16;
import org.opendaylight.yangtools.yang.common.Uint32;
import org.opendaylight.yangtools.yang.common.Uint64;
import org.opendaylight.yangtools.yang.common.Uint8;

/**
 * Unit tests for MultipartReplyMessageFactory.
 *
 * @author giuseppex.petralia@intel.com
 */
public class MultipartReplyMessageFactoryTest {
    private static final byte MESSAGE_TYPE = 19;
    private static final byte PADDING = 4;

    private OFSerializer<MultipartReplyMessage> factory;

    @Before
    public void startUp() {
        SerializerRegistry registry = new SerializerRegistryImpl();
        registry.init();
        factory = registry
                .getSerializer(new MessageTypeKey<>(EncodeConstants.OF13_VERSION_ID, MultipartReplyMessage.class));
    }

    @Test
    public void testMultipartRequestTableFeaturesMessageFactory() throws Exception {
        MultipartReplyMessageBuilder builder = new MultipartReplyMessageBuilder();
        BufferHelper.setupHeader(builder, EncodeConstants.OF13_VERSION_ID);
        builder.setFlags(new MultipartRequestFlags(true));
        builder.setType(MultipartType.forValue(12));
        final MultipartReplyTableFeaturesCaseBuilder caseBuilder = new MultipartReplyTableFeaturesCaseBuilder();
        final MultipartReplyTableFeaturesBuilder featuresBuilder = new MultipartReplyTableFeaturesBuilder();
        final List<TableFeatures> tableFeaturesList = new ArrayList<>();
        TableFeaturesBuilder tableFeaturesBuilder = new TableFeaturesBuilder();
        tableFeaturesBuilder.setTableId(Uint8.valueOf(8));
        tableFeaturesBuilder.setName("AAAABBBBCCCCDDDDEEEEFFFFGGGG");
        tableFeaturesBuilder.setMetadataMatch(new byte[] { 0x00, 0x01, 0x02, 0x03, 0x01, 0x04, 0x08, 0x01 });
        tableFeaturesBuilder.setMetadataWrite(new byte[] { 0x00, 0x07, 0x01, 0x05, 0x01, 0x00, 0x03, 0x01 });
        tableFeaturesBuilder.setConfig(new TableConfig(true));
        tableFeaturesBuilder.setMaxEntries(Uint32.valueOf(65));
        TableFeaturePropertiesBuilder propBuilder = new TableFeaturePropertiesBuilder();
        propBuilder.setType(TableFeaturesPropType.OFPTFPTNEXTTABLES);
        propBuilder.addAugmentation(new NextTableRelatedTableFeaturePropertyBuilder()
            .setNextTableIds(List.of(
                new NextTableIdsBuilder().setTableId(Uint8.ONE).build(),
                new NextTableIdsBuilder().setTableId(Uint8.TWO).build()))
            .build());
        List<TableFeatureProperties> properties = new ArrayList<>();
        properties.add(propBuilder.build());
        propBuilder = new TableFeaturePropertiesBuilder();
        propBuilder.setType(TableFeaturesPropType.OFPTFPTNEXTTABLESMISS);
        propBuilder.addAugmentation(new NextTableRelatedTableFeaturePropertyBuilder().build());
        properties.add(propBuilder.build());
        propBuilder = new TableFeaturePropertiesBuilder();
        propBuilder.setType(TableFeaturesPropType.OFPTFPTINSTRUCTIONS);
        List<Instruction> insIds = new ArrayList<>();
        InstructionBuilder insBuilder = new InstructionBuilder();
        insBuilder.setInstructionChoice(new WriteActionsCaseBuilder().build());
        insIds.add(insBuilder.build());
        insBuilder = new InstructionBuilder();
        insBuilder.setInstructionChoice(new GotoTableCaseBuilder().build());
        insIds.add(insBuilder.build());
        propBuilder.addAugmentation(new InstructionRelatedTableFeaturePropertyBuilder().setInstruction(insIds).build());
        properties.add(propBuilder.build());
        propBuilder = new TableFeaturePropertiesBuilder();
        propBuilder.setType(TableFeaturesPropType.OFPTFPTINSTRUCTIONSMISS);
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
        propBuilder.addAugmentation(new InstructionRelatedTableFeaturePropertyBuilder().setInstruction(insIds).build());
        properties.add(propBuilder.build());
        tableFeaturesBuilder.setTableFeatureProperties(properties);
        tableFeaturesList.add(tableFeaturesBuilder.build());
        tableFeaturesBuilder = new TableFeaturesBuilder();
        tableFeaturesBuilder.setTableId(Uint8.valueOf(8));
        tableFeaturesBuilder.setName("AAAABBBBCCCCDDDDEEEEFFFFGGGG");
        byte[] metadataMatch = new byte[] { 0x00, 0x01, 0x02, 0x03, 0x01, 0x04, 0x08, 0x01 };
        tableFeaturesBuilder.setMetadataMatch(metadataMatch);
        byte[] metadataWrite = new byte[] { 0x00, 0x07, 0x01, 0x05, 0x01, 0x00, 0x03, 0x01 };
        tableFeaturesBuilder.setMetadataWrite(metadataWrite);
        tableFeaturesBuilder.setConfig(new TableConfig(true));
        tableFeaturesBuilder.setMaxEntries(Uint32.valueOf(67));
        properties = new ArrayList<>();
        propBuilder = new TableFeaturePropertiesBuilder();
        propBuilder.setType(TableFeaturesPropType.OFPTFPTWRITEACTIONS);
        List<Action> actions = new ArrayList<>();
        ActionBuilder actionBuilder = new ActionBuilder();
        actionBuilder.setActionChoice(new OutputActionCaseBuilder().build());
        actions.add(actionBuilder.build());
        propBuilder.addAugmentation(new ActionRelatedTableFeaturePropertyBuilder().setAction(actions).build());
        properties.add(propBuilder.build());
        propBuilder = new TableFeaturePropertiesBuilder();
        propBuilder.setType(TableFeaturesPropType.OFPTFPTWRITEACTIONSMISS);
        propBuilder.addAugmentation(new ActionRelatedTableFeaturePropertyBuilder().build());
        properties.add(propBuilder.build());
        propBuilder = new TableFeaturePropertiesBuilder();
        propBuilder.setType(TableFeaturesPropType.OFPTFPTAPPLYACTIONS);
        propBuilder.addAugmentation(new ActionRelatedTableFeaturePropertyBuilder().build());
        properties.add(propBuilder.build());
        propBuilder = new TableFeaturePropertiesBuilder();
        propBuilder.setType(TableFeaturesPropType.OFPTFPTAPPLYACTIONSMISS);
        propBuilder.addAugmentation(new ActionRelatedTableFeaturePropertyBuilder().build());
        properties.add(propBuilder.build());
        propBuilder = new TableFeaturePropertiesBuilder();
        propBuilder.setType(TableFeaturesPropType.OFPTFPTMATCH);
        MatchEntryBuilder entriesBuilder = new MatchEntryBuilder();
        entriesBuilder.setOxmClass(OpenflowBasicClass.class);
        entriesBuilder.setOxmMatchField(InPhyPort.class);
        entriesBuilder.setHasMask(false);
        List<MatchEntry> entries = new ArrayList<>();
        entries.add(entriesBuilder.build());
        entriesBuilder = new MatchEntryBuilder();
        entriesBuilder.setOxmClass(OpenflowBasicClass.class);
        entriesBuilder.setOxmMatchField(InPort.class);
        entriesBuilder.setHasMask(false);
        entries.add(entriesBuilder.build());
        propBuilder.addAugmentation(new OxmRelatedTableFeaturePropertyBuilder().setMatchEntry(entries).build());
        properties.add(propBuilder.build());
        propBuilder = new TableFeaturePropertiesBuilder();
        propBuilder.setType(TableFeaturesPropType.OFPTFPTWILDCARDS);
        propBuilder.addAugmentation(new OxmRelatedTableFeaturePropertyBuilder().build());
        properties.add(propBuilder.build());
        propBuilder = new TableFeaturePropertiesBuilder();
        propBuilder.setType(TableFeaturesPropType.OFPTFPTWRITESETFIELD);
        propBuilder.addAugmentation(new OxmRelatedTableFeaturePropertyBuilder().build());
        properties.add(propBuilder.build());
        propBuilder = new TableFeaturePropertiesBuilder();
        propBuilder.setType(TableFeaturesPropType.OFPTFPTWRITESETFIELDMISS);
        propBuilder.addAugmentation(new OxmRelatedTableFeaturePropertyBuilder().build());
        properties.add(propBuilder.build());
        propBuilder = new TableFeaturePropertiesBuilder();
        propBuilder.setType(TableFeaturesPropType.OFPTFPTAPPLYSETFIELD);
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
        propBuilder.addAugmentation(new OxmRelatedTableFeaturePropertyBuilder().setMatchEntry(entries).build());
        properties.add(propBuilder.build());
        propBuilder = new TableFeaturePropertiesBuilder();
        propBuilder.setType(TableFeaturesPropType.OFPTFPTAPPLYSETFIELDMISS);
        propBuilder.addAugmentation(new OxmRelatedTableFeaturePropertyBuilder().build());
        properties.add(propBuilder.build());
        tableFeaturesBuilder.setTableFeatureProperties(properties);
        tableFeaturesList.add(tableFeaturesBuilder.build());
        featuresBuilder.setTableFeatures(tableFeaturesList);
        caseBuilder.setMultipartReplyTableFeatures(featuresBuilder.build());
        builder.setMultipartReplyBody(caseBuilder.build());
        MultipartReplyMessage message = builder.build();
        ByteBuf serializedBuffer = UnpooledByteBufAllocator.DEFAULT.buffer();
        factory.serialize(message, serializedBuffer);
        BufferHelper.checkHeaderV13(serializedBuffer, MESSAGE_TYPE, 520);
        Assert.assertEquals("Wrong type", MultipartType.OFPMPTABLEFEATURES.getIntValue(), serializedBuffer.readShort());
        Assert.assertEquals("Wrong flags", message.getFlags(),
                createMultipartRequestFlags(serializedBuffer.readShort()));
        serializedBuffer.skipBytes(PADDING);

        Assert.assertEquals("Wrong length", 232, serializedBuffer.readUnsignedShort());
        Assert.assertEquals("Wrong registry-id", 8, serializedBuffer.readUnsignedByte());
        serializedBuffer.skipBytes(5);
        Assert.assertEquals("Wrong name", "AAAABBBBCCCCDDDDEEEEFFFFGGGG",
                ByteBufUtils.decodeNullTerminatedString(serializedBuffer, 32));
        byte[] metadataMatchOutput = new byte[metadataMatch.length];
        serializedBuffer.readBytes(metadataMatchOutput);
        Assert.assertArrayEquals("Wrong metadata-match", new byte[] { 0x00, 0x01, 0x02, 0x03, 0x01, 0x04, 0x08, 0x01 },
                metadataMatchOutput);
        serializedBuffer.skipBytes(64 - metadataMatch.length);
        byte[] metadataWriteOutput = new byte[metadataWrite.length];
        serializedBuffer.readBytes(metadataWriteOutput);
        Assert.assertArrayEquals("Wrong metadata-write", new byte[] { 0x00, 0x07, 0x01, 0x05, 0x01, 0x00, 0x03, 0x01 },
                metadataWriteOutput);
        serializedBuffer.skipBytes(64 - metadataWrite.length);
        Assert.assertEquals("Wrong config", 1, serializedBuffer.readUnsignedInt());
        Assert.assertEquals("Wrong max-entries", 65, serializedBuffer.readUnsignedInt());
        Assert.assertEquals("Wrong property type", 2, serializedBuffer.readUnsignedShort());
        Assert.assertEquals("Wrong property length", 6, serializedBuffer.readUnsignedShort());
        Assert.assertEquals("Wrong next-registry-id", 1, serializedBuffer.readUnsignedByte());
        Assert.assertEquals("Wrong next-registry-id", 2, serializedBuffer.readUnsignedByte());
        serializedBuffer.skipBytes(2);
        Assert.assertEquals("Wrong property type", 3, serializedBuffer.readUnsignedShort());
        Assert.assertEquals("Wrong property length", 4, serializedBuffer.readUnsignedShort());
        serializedBuffer.skipBytes(4);
        Assert.assertEquals("Wrong property type", 0, serializedBuffer.readUnsignedShort());
        Assert.assertEquals("Wrong property length", 12, serializedBuffer.readUnsignedShort());
        Assert.assertEquals("Wrong instruction type", 3, serializedBuffer.readUnsignedShort());
        Assert.assertEquals("Wrong instruction length", 4, serializedBuffer.readUnsignedShort());
        Assert.assertEquals("Wrong instruction type", 1, serializedBuffer.readUnsignedShort());
        Assert.assertEquals("Wrong instruction length", 4, serializedBuffer.readUnsignedShort());
        serializedBuffer.skipBytes(4);
        Assert.assertEquals("Wrong property type", 1, serializedBuffer.readUnsignedShort());
        Assert.assertEquals("Wrong property length", 24, serializedBuffer.readUnsignedShort());
        Assert.assertEquals("Wrong instruction type", 2, serializedBuffer.readUnsignedShort());
        Assert.assertEquals("Wrong instruction length", 4, serializedBuffer.readUnsignedShort());
        Assert.assertEquals("Wrong instruction type", 4, serializedBuffer.readUnsignedShort());
        Assert.assertEquals("Wrong instruction length", 4, serializedBuffer.readUnsignedShort());
        Assert.assertEquals("Wrong instruction type", 6, serializedBuffer.readUnsignedShort());
        Assert.assertEquals("Wrong instruction length", 4, serializedBuffer.readUnsignedShort());
        Assert.assertEquals("Wrong instruction type", 5, serializedBuffer.readUnsignedShort());
        Assert.assertEquals("Wrong instruction length", 4, serializedBuffer.readUnsignedShort());
        Assert.assertEquals("Wrong instruction type", 1, serializedBuffer.readUnsignedShort());
        Assert.assertEquals("Wrong instruction length", 4, serializedBuffer.readUnsignedShort());
        Assert.assertEquals("Wrong length", 272, serializedBuffer.readUnsignedShort());
        Assert.assertEquals("Wrong registry-id", 8, serializedBuffer.readUnsignedByte());
        serializedBuffer.skipBytes(5);
        Assert.assertEquals("Wrong name", "AAAABBBBCCCCDDDDEEEEFFFFGGGG",
                ByteBufUtils.decodeNullTerminatedString(serializedBuffer, 32));
        metadataMatchOutput = new byte[metadataMatch.length];
        serializedBuffer.readBytes(metadataMatchOutput);
        serializedBuffer.skipBytes(64 - metadataMatch.length);
        Assert.assertArrayEquals("Wrong metadata-match", new byte[] { 0x00, 0x01, 0x02, 0x03, 0x01, 0x04, 0x08, 0x01 },
                metadataMatchOutput);
        metadataWriteOutput = new byte[metadataWrite.length];
        serializedBuffer.readBytes(metadataWriteOutput);
        serializedBuffer.skipBytes(64 - metadataWrite.length);
        Assert.assertArrayEquals("Wrong metadata-write", new byte[] { 0x00, 0x07, 0x01, 0x05, 0x01, 0x00, 0x03, 0x01 },
                metadataWriteOutput);
        Assert.assertEquals("Wrong config", 1, serializedBuffer.readUnsignedInt());
        Assert.assertEquals("Wrong max-entries", 67, serializedBuffer.readUnsignedInt());
        Assert.assertEquals("Wrong property type", 4, serializedBuffer.readUnsignedShort());
        Assert.assertEquals("Wrong property length", 8, serializedBuffer.readUnsignedShort());
        Assert.assertEquals("Wrong action type", 0, serializedBuffer.readUnsignedShort());
        Assert.assertEquals("Wrong action length", 4, serializedBuffer.readUnsignedShort());
        Assert.assertEquals("Wrong property type", 5, serializedBuffer.readUnsignedShort());
        Assert.assertEquals("Wrong property length", 4, serializedBuffer.readUnsignedShort());
        serializedBuffer.skipBytes(4);
        Assert.assertEquals("Wrong property type", 6, serializedBuffer.readUnsignedShort());
        Assert.assertEquals("Wrong property length", 4, serializedBuffer.readUnsignedShort());
        serializedBuffer.skipBytes(4);
        Assert.assertEquals("Wrong property type", 7, serializedBuffer.readUnsignedShort());
        Assert.assertEquals("Wrong property length", 4, serializedBuffer.readUnsignedShort());
        serializedBuffer.skipBytes(4);
        Assert.assertEquals("Wrong property type", 8, serializedBuffer.readUnsignedShort());
        Assert.assertEquals("Wrong property length", 12, serializedBuffer.readUnsignedShort());
        Assert.assertEquals("Wrong match class", 0x8000, serializedBuffer.readUnsignedShort());
        Assert.assertEquals("Wrong match field&mask", 2, serializedBuffer.readUnsignedByte());
        Assert.assertEquals("Wrong match length", 4, serializedBuffer.readUnsignedByte());
        Assert.assertEquals("Wrong match class", 0x8000, serializedBuffer.readUnsignedShort());
        Assert.assertEquals("Wrong match field&mask", 0, serializedBuffer.readUnsignedByte());
        Assert.assertEquals("Wrong match length", 4, serializedBuffer.readUnsignedByte());
        serializedBuffer.skipBytes(4);
        Assert.assertEquals("Wrong property type", 10, serializedBuffer.readUnsignedShort());
        Assert.assertEquals("Wrong property length", 4, serializedBuffer.readUnsignedShort());
        serializedBuffer.skipBytes(4);
        Assert.assertEquals("Wrong property type", 12, serializedBuffer.readUnsignedShort());
        Assert.assertEquals("Wrong property length", 4, serializedBuffer.readUnsignedShort());
        serializedBuffer.skipBytes(4);
        Assert.assertEquals("Wrong property type", 13, serializedBuffer.readUnsignedShort());
        Assert.assertEquals("Wrong property length", 4, serializedBuffer.readUnsignedShort());
        serializedBuffer.skipBytes(4);
        Assert.assertEquals("Wrong property type", 14, serializedBuffer.readUnsignedShort());
        Assert.assertEquals("Wrong property length", 12, serializedBuffer.readUnsignedShort());
        Assert.assertEquals("Wrong match class", 0x8000, serializedBuffer.readUnsignedShort());
        Assert.assertEquals("Wrong match field&mask", 20, serializedBuffer.readUnsignedByte());
        Assert.assertEquals("Wrong match length", 1, serializedBuffer.readUnsignedByte());
        Assert.assertEquals("Wrong match class", 0x8000, serializedBuffer.readUnsignedShort());
        Assert.assertEquals("Wrong match field&mask", 18, serializedBuffer.readUnsignedByte());
        Assert.assertEquals("Wrong match length", 1, serializedBuffer.readUnsignedByte());
        serializedBuffer.skipBytes(4);
        Assert.assertEquals("Wrong property type", 15, serializedBuffer.readUnsignedShort());
        Assert.assertEquals("Wrong property length", 4, serializedBuffer.readUnsignedShort());
        serializedBuffer.skipBytes(4);
        Assert.assertTrue("Unread data", serializedBuffer.readableBytes() == 0);
    }

    @Test
    public void testPortDescSerialize() throws Exception {
        MultipartReplyMessageBuilder builder = new MultipartReplyMessageBuilder();
        BufferHelper.setupHeader(builder, EncodeConstants.OF13_VERSION_ID);
        builder.setFlags(new MultipartRequestFlags(true));
        builder.setType(MultipartType.forValue(13));
        MultipartReplyPortDescCaseBuilder portDescCase = new MultipartReplyPortDescCaseBuilder();
        MultipartReplyPortDescBuilder portDesc = new MultipartReplyPortDescBuilder();
        portDesc.setPorts(createPortList());
        portDescCase.setMultipartReplyPortDesc(portDesc.build());
        builder.setMultipartReplyBody(portDescCase.build());
        MultipartReplyMessage message = builder.build();
        ByteBuf serializedBuffer = UnpooledByteBufAllocator.DEFAULT.buffer();
        factory.serialize(message, serializedBuffer);
        BufferHelper.checkHeaderV13(serializedBuffer, MESSAGE_TYPE, 80);
        Assert.assertEquals("Wrong type", MultipartType.OFPMPPORTDESC.getIntValue(), serializedBuffer.readShort());
        Assert.assertEquals("Wrong flags", message.getFlags(),
                createMultipartRequestFlags(serializedBuffer.readShort()));
        serializedBuffer.skipBytes(PADDING);
        MultipartReplyPortDescCase body = (MultipartReplyPortDescCase) message.getMultipartReplyBody();
        MultipartReplyPortDesc messageOutput = body.getMultipartReplyPortDesc();
        Ports port = messageOutput.getPorts().get(0);
        Assert.assertEquals("Wrong PortNo", port.getPortNo().intValue(), serializedBuffer.readUnsignedInt());
        serializedBuffer.skipBytes(4);
        byte[] address = new byte[6];
        serializedBuffer.readBytes(address);
        Assert.assertEquals("Wrong MacAddress", port.getHwAddr().getValue().toLowerCase(),
                new MacAddress(ByteBufUtils.macAddressToString(address)).getValue().toLowerCase());
        serializedBuffer.skipBytes(2);
        byte[] name = new byte[16];
        serializedBuffer.readBytes(name);
        Assert.assertEquals("Wrong name", port.getName(), new String(name).trim());
        Assert.assertEquals("Wrong config", port.getConfig(), createPortConfig(serializedBuffer.readInt()));
        Assert.assertEquals("Wrong state", port.getState(), createPortState(serializedBuffer.readInt()));
        Assert.assertEquals("Wrong current", port.getCurrentFeatures(), createPortFeatures(serializedBuffer.readInt()));
        Assert.assertEquals("Wrong advertised", port.getAdvertisedFeatures(),
                createPortFeatures(serializedBuffer.readInt()));
        Assert.assertEquals("Wrong supported", port.getSupportedFeatures(),
                createPortFeatures(serializedBuffer.readInt()));
        Assert.assertEquals("Wrong peer", port.getPeerFeatures(), createPortFeatures(serializedBuffer.readInt()));
        Assert.assertEquals("Wrong Current speed", port.getCurrSpeed().longValue(), serializedBuffer.readInt());
        Assert.assertEquals("Wrong Max speed", port.getMaxSpeed().longValue(), serializedBuffer.readInt());
    }

    @Test
    public void testMeterFeaturesSerialize() throws Exception {
        MultipartReplyMessageBuilder builder = new MultipartReplyMessageBuilder();
        BufferHelper.setupHeader(builder, EncodeConstants.OF13_VERSION_ID);
        builder.setFlags(new MultipartRequestFlags(true));
        builder.setType(MultipartType.forValue(11));
        final MultipartReplyMeterFeaturesCaseBuilder meterFeaturesCase = new MultipartReplyMeterFeaturesCaseBuilder();
        MultipartReplyMeterFeaturesBuilder meterFeatures = new MultipartReplyMeterFeaturesBuilder();
        meterFeatures.setMaxMeter(1L);
        meterFeatures.setBandTypes(new MeterBandTypeBitmap(true, false));
        meterFeatures.setCapabilities(new MeterFlags(true, false, true, false));
        meterFeatures.setMaxBands(Uint8.ONE);
        meterFeatures.setMaxColor(Uint8.ONE);
        meterFeaturesCase.setMultipartReplyMeterFeatures(meterFeatures.build());
        builder.setMultipartReplyBody(meterFeaturesCase.build());
        MultipartReplyMessage message = builder.build();
        ByteBuf serializedBuffer = UnpooledByteBufAllocator.DEFAULT.buffer();
        factory.serialize(message, serializedBuffer);
        BufferHelper.checkHeaderV13(serializedBuffer, MESSAGE_TYPE, 30);
        Assert.assertEquals("Wrong type", MultipartType.OFPMPMETERFEATURES.getIntValue(), serializedBuffer.readShort());
        Assert.assertEquals("Wrong flags", message.getFlags(),
                createMultipartRequestFlags(serializedBuffer.readShort()));
        serializedBuffer.skipBytes(PADDING);
        MultipartReplyMeterFeaturesCase body = (MultipartReplyMeterFeaturesCase) message.getMultipartReplyBody();
        MultipartReplyMeterFeatures messageOutput = body.getMultipartReplyMeterFeatures();
        Assert.assertEquals("Wrong max meter", messageOutput.getMaxMeter().intValue(), serializedBuffer.readInt());
        Assert.assertEquals("Wrong band type", messageOutput.getBandTypes(),
                createMeterBandTypeBitmap(serializedBuffer.readInt()));
        Assert.assertEquals("Wrong capabilities", messageOutput.getCapabilities(),
                createMeterFlags(serializedBuffer.readShort()));
        Assert.assertEquals("Wrong max bands", messageOutput.getMaxBands().shortValue(),
                serializedBuffer.readUnsignedByte());
        Assert.assertEquals("Wrong max color", messageOutput.getMaxColor().shortValue(),
                serializedBuffer.readUnsignedByte());
        serializedBuffer.skipBytes(2);
    }

    @Test
    public void testMeterConfigSerialize() throws Exception {
        MultipartReplyMessageBuilder builder = new MultipartReplyMessageBuilder();
        BufferHelper.setupHeader(builder, EncodeConstants.OF13_VERSION_ID);
        builder.setFlags(new MultipartRequestFlags(true));
        builder.setType(MultipartType.forValue(10));
        MultipartReplyMeterConfigCaseBuilder meterConfigCase = new MultipartReplyMeterConfigCaseBuilder();
        MultipartReplyMeterConfigBuilder meterConfigBuilder = new MultipartReplyMeterConfigBuilder();
        meterConfigBuilder.setMeterConfig(createMeterConfig());
        meterConfigCase.setMultipartReplyMeterConfig(meterConfigBuilder.build());
        builder.setMultipartReplyBody(meterConfigCase.build());
        MultipartReplyMessage message = builder.build();
        ByteBuf serializedBuffer = UnpooledByteBufAllocator.DEFAULT.buffer();
        factory.serialize(message, serializedBuffer);
        BufferHelper.checkHeaderV13(serializedBuffer, MESSAGE_TYPE, 48);
        Assert.assertEquals("Wrong type", MultipartType.OFPMPMETERCONFIG.getIntValue(), serializedBuffer.readShort());
        Assert.assertEquals("Wrong flags", message.getFlags(),
                createMultipartRequestFlags(serializedBuffer.readShort()));
        serializedBuffer.skipBytes(PADDING);
        MultipartReplyMeterConfigCase body = (MultipartReplyMeterConfigCase) message.getMultipartReplyBody();
        MultipartReplyMeterConfig messageOutput = body.getMultipartReplyMeterConfig();
        MeterConfig meterConfig = messageOutput.getMeterConfig().get(0);
        Assert.assertEquals("Wrong len", 32, serializedBuffer.readShort());
        Assert.assertEquals("Wrong flags", meterConfig.getFlags(), createMeterFlags(serializedBuffer.readShort()));
        Assert.assertEquals("Wrong meterId", meterConfig.getMeterId().getValue().intValue(),
                serializedBuffer.readInt());
        Assert.assertEquals("Wrong bands", meterConfig.getBands(), decodeBandsList(serializedBuffer));
    }

    @Test
    public void testMeterSerialize() throws Exception {
        MultipartReplyMessageBuilder builder = new MultipartReplyMessageBuilder();
        BufferHelper.setupHeader(builder, EncodeConstants.OF13_VERSION_ID);
        builder.setFlags(new MultipartRequestFlags(true));
        builder.setType(MultipartType.forValue(9));
        MultipartReplyMeterCaseBuilder meterCase = new MultipartReplyMeterCaseBuilder();
        MultipartReplyMeterBuilder meter = new MultipartReplyMeterBuilder();
        meter.setMeterStats(createMeterStats());
        meterCase.setMultipartReplyMeter(meter.build());
        builder.setMultipartReplyBody(meterCase.build());
        MultipartReplyMessage message = builder.build();
        ByteBuf serializedBuffer = UnpooledByteBufAllocator.DEFAULT.buffer();
        factory.serialize(message, serializedBuffer);
        BufferHelper.checkHeaderV13(serializedBuffer, MESSAGE_TYPE, 74);
        Assert.assertEquals("Wrong type", MultipartType.OFPMPMETER.getIntValue(), serializedBuffer.readShort());
        Assert.assertEquals("Wrong flags", message.getFlags(),
                createMultipartRequestFlags(serializedBuffer.readShort()));
        serializedBuffer.skipBytes(PADDING);
        MultipartReplyMeterCase body = (MultipartReplyMeterCase) message.getMultipartReplyBody();
        MultipartReplyMeter messageOutput = body.getMultipartReplyMeter();
        MeterStats meterStats = messageOutput.getMeterStats().get(0);
        Assert.assertEquals("Wrong meterId", meterStats.getMeterId().getValue().intValue(), serializedBuffer.readInt());
        Assert.assertEquals("Wrong len", 58, serializedBuffer.readInt());
        serializedBuffer.skipBytes(6);
        Assert.assertEquals("Wrong flow count", meterStats.getFlowCount().intValue(), serializedBuffer.readInt());
        Assert.assertEquals("Wrong packet in count", meterStats.getPacketInCount().longValue(),
                serializedBuffer.readLong());
        Assert.assertEquals("Wrong byte in count", meterStats.getByteInCount().longValue(),
                serializedBuffer.readLong());
        Assert.assertEquals("Wrong duration sec", meterStats.getDurationSec().intValue(), serializedBuffer.readInt());
        Assert.assertEquals("Wrong duration nsec", meterStats.getDurationNsec().intValue(), serializedBuffer.readInt());
        MeterBandStats meterBandStats = meterStats.getMeterBandStats().get(0);
        Assert.assertEquals("Wrong packet in count", meterBandStats.getPacketBandCount().longValue(),
                serializedBuffer.readLong());
        Assert.assertEquals("Wrong byte in count", meterBandStats.getByteBandCount().longValue(),
                serializedBuffer.readLong());
    }

    @Test
    public void testGroupFeaturesSerialize() throws Exception {
        MultipartReplyMessageBuilder builder = new MultipartReplyMessageBuilder();
        BufferHelper.setupHeader(builder, EncodeConstants.OF13_VERSION_ID);
        builder.setFlags(new MultipartRequestFlags(true));
        builder.setType(MultipartType.forValue(8));
        final MultipartReplyGroupFeaturesCaseBuilder featureCase = new MultipartReplyGroupFeaturesCaseBuilder();
        MultipartReplyGroupFeaturesBuilder feature = new MultipartReplyGroupFeaturesBuilder();
        feature.setTypes(new GroupTypes(true, false, true, false));
        feature.setCapabilities(new GroupCapabilities(true, false, true, true));
        List<Uint32> maxGroups = new ArrayList<>();
        maxGroups.add(Uint32.valueOf(1));
        maxGroups.add(Uint32.valueOf(2));
        maxGroups.add(Uint32.valueOf(3));
        maxGroups.add(Uint32.valueOf(4));
        feature.setMaxGroups(maxGroups);
        feature.setActionsBitmap(createActionType());
        featureCase.setMultipartReplyGroupFeatures(feature.build());
        builder.setMultipartReplyBody(featureCase.build());
        MultipartReplyMessage message = builder.build();
        ByteBuf serializedBuffer = UnpooledByteBufAllocator.DEFAULT.buffer();
        factory.serialize(message, serializedBuffer);
        BufferHelper.checkHeaderV13(serializedBuffer, MESSAGE_TYPE, 56);
        Assert.assertEquals("Wrong type", MultipartType.OFPMPGROUPFEATURES.getIntValue(), serializedBuffer.readShort());
        Assert.assertEquals("Wrong flags", message.getFlags(),
                createMultipartRequestFlags(serializedBuffer.readShort()));
        serializedBuffer.skipBytes(PADDING);
        MultipartReplyGroupFeaturesCase body = (MultipartReplyGroupFeaturesCase) message.getMultipartReplyBody();
        MultipartReplyGroupFeatures messageOutput = body.getMultipartReplyGroupFeatures();
        Assert.assertEquals("Wrong type", messageOutput.getTypes(), createGroupTypes(serializedBuffer.readInt()));
        Assert.assertEquals("Wrong capabilities", messageOutput.getCapabilities(),
                createGroupCapabilities(serializedBuffer.readInt()));
        Assert.assertEquals("Wrong max groups", messageOutput.getMaxGroups().get(0).intValue(),
                serializedBuffer.readInt());
        Assert.assertEquals("Wrong max groups", messageOutput.getMaxGroups().get(1).intValue(),
                serializedBuffer.readInt());
        Assert.assertEquals("Wrong max groups", messageOutput.getMaxGroups().get(2).intValue(),
                serializedBuffer.readInt());
        Assert.assertEquals("Wrong max groups", messageOutput.getMaxGroups().get(3).intValue(),
                serializedBuffer.readInt());
        Assert.assertEquals("Wrong actions", messageOutput.getActionsBitmap().get(0),
                createActionType(serializedBuffer.readInt()));
        Assert.assertEquals("Wrong actions", messageOutput.getActionsBitmap().get(1),
                createActionType(serializedBuffer.readInt()));
        Assert.assertEquals("Wrong actions", messageOutput.getActionsBitmap().get(2),
                createActionType(serializedBuffer.readInt()));
        Assert.assertEquals("Wrong actions", messageOutput.getActionsBitmap().get(3),
                createActionType(serializedBuffer.readInt()));
    }

    @Test
    public void testGroupDescSerialize() throws Exception {
        MultipartReplyMessageBuilder builder = new MultipartReplyMessageBuilder();
        BufferHelper.setupHeader(builder, EncodeConstants.OF13_VERSION_ID);
        builder.setFlags(new MultipartRequestFlags(true));
        builder.setType(MultipartType.forValue(7));
        MultipartReplyGroupDescCaseBuilder groupCase = new MultipartReplyGroupDescCaseBuilder();
        MultipartReplyGroupDescBuilder group = new MultipartReplyGroupDescBuilder();
        group.setGroupDesc(createGroupDesc());
        groupCase.setMultipartReplyGroupDesc(group.build());
        builder.setMultipartReplyBody(groupCase.build());
        MultipartReplyMessage message = builder.build();
        ByteBuf serializedBuffer = UnpooledByteBufAllocator.DEFAULT.buffer();
        factory.serialize(message, serializedBuffer);
        BufferHelper.checkHeaderV13(serializedBuffer, MESSAGE_TYPE, 64);
        Assert.assertEquals("Wrong type", MultipartType.OFPMPGROUPDESC.getIntValue(), serializedBuffer.readShort());
        Assert.assertEquals("Wrong flags", message.getFlags(),
                createMultipartRequestFlags(serializedBuffer.readShort()));
        serializedBuffer.skipBytes(PADDING);
        MultipartReplyGroupDescCase body = (MultipartReplyGroupDescCase) message.getMultipartReplyBody();
        MultipartReplyGroupDesc messageOutput = body.getMultipartReplyGroupDesc();
        GroupDesc groupDesc = messageOutput.getGroupDesc().get(0);
        Assert.assertEquals("Wrong length", 48, serializedBuffer.readShort());
        Assert.assertEquals("Wrong type", groupDesc.getType().getIntValue(), serializedBuffer.readUnsignedByte());
        serializedBuffer.skipBytes(1);
        Assert.assertEquals("Wrong group id", groupDesc.getGroupId().getValue().intValue(), serializedBuffer.readInt());
        BucketsList bucketList = groupDesc.getBucketsList().get(0);
        Assert.assertEquals("Wrong length", 40, serializedBuffer.readShort());
        Assert.assertEquals("Wrong weight", bucketList.getWeight().intValue(), serializedBuffer.readShort());
        Assert.assertEquals("Wrong watch port", bucketList.getWatchPort().getValue().intValue(),
                serializedBuffer.readInt());
        Assert.assertEquals("Wrong watch group", bucketList.getWatchGroup().intValue(), serializedBuffer.readInt());
        serializedBuffer.skipBytes(4);

        Assert.assertEquals("Wrong action type", 0, serializedBuffer.readUnsignedShort());
        Assert.assertEquals("Wrong action length", 16, serializedBuffer.readUnsignedShort());
        Assert.assertEquals("Wrong action type", 45, serializedBuffer.readUnsignedInt());
        Assert.assertEquals("Wrong action type", 55, serializedBuffer.readUnsignedShort());
        serializedBuffer.skipBytes(6);
        Assert.assertEquals("Wrong action type", 23, serializedBuffer.readUnsignedShort());
        Assert.assertEquals("Wrong action length", 8, serializedBuffer.readUnsignedShort());
        Assert.assertEquals("Wrong action type", 64, serializedBuffer.readUnsignedByte());
        serializedBuffer.skipBytes(3);
        Assert.assertTrue("Not all data were read", serializedBuffer.readableBytes() == 0);
    }

    @Test
    public void testGroupSerialize() throws Exception {
        MultipartReplyMessageBuilder builder = new MultipartReplyMessageBuilder();
        BufferHelper.setupHeader(builder, EncodeConstants.OF13_VERSION_ID);
        builder.setFlags(new MultipartRequestFlags(true));
        builder.setType(MultipartType.forValue(6));
        MultipartReplyGroupCaseBuilder groupCase = new MultipartReplyGroupCaseBuilder();
        MultipartReplyGroupBuilder group = new MultipartReplyGroupBuilder();
        group.setGroupStats(createGroupStats());
        groupCase.setMultipartReplyGroup(group.build());
        builder.setMultipartReplyBody(groupCase.build());
        MultipartReplyMessage message = builder.build();
        ByteBuf serializedBuffer = UnpooledByteBufAllocator.DEFAULT.buffer();
        factory.serialize(message, serializedBuffer);
        BufferHelper.checkHeaderV13(serializedBuffer, MESSAGE_TYPE, 72);
        Assert.assertEquals("Wrong type", MultipartType.OFPMPGROUP.getIntValue(), serializedBuffer.readShort());
        Assert.assertEquals("Wrong flags", message.getFlags(),
                createMultipartRequestFlags(serializedBuffer.readShort()));
        serializedBuffer.skipBytes(PADDING);
        MultipartReplyGroupCase body = (MultipartReplyGroupCase) message.getMultipartReplyBody();
        MultipartReplyGroup messageOutput = body.getMultipartReplyGroup();
        GroupStats groupStats = messageOutput.getGroupStats().get(0);
        Assert.assertEquals("Wrong length", 56, serializedBuffer.readShort());
        serializedBuffer.skipBytes(2);
        Assert.assertEquals("Wrong group id", groupStats.getGroupId().getValue().intValue(),
                serializedBuffer.readInt());
        Assert.assertEquals("Wrong ref count", groupStats.getRefCount().intValue(), serializedBuffer.readInt());
        serializedBuffer.skipBytes(4);
        Assert.assertEquals("Wrong Packet count", groupStats.getPacketCount().longValue(), serializedBuffer.readLong());
        Assert.assertEquals("Wrong Byte count", groupStats.getByteCount().longValue(), serializedBuffer.readLong());
        Assert.assertEquals("Wrong duration sec", groupStats.getDurationSec().intValue(), serializedBuffer.readInt());
        Assert.assertEquals("Wrong duration nsec", groupStats.getDurationNsec().intValue(), serializedBuffer.readInt());
        BucketStats bucketStats = groupStats.getBucketStats().get(0);
        Assert.assertEquals("Wrong Packet count", bucketStats.getPacketCount().longValue(),
                serializedBuffer.readLong());
        Assert.assertEquals("Wrong Byte count", bucketStats.getByteCount().longValue(), serializedBuffer.readLong());
    }

    @Test
    public void testQueueSerialize() throws Exception {
        MultipartReplyMessageBuilder builder = new MultipartReplyMessageBuilder();
        BufferHelper.setupHeader(builder, EncodeConstants.OF13_VERSION_ID);
        builder.setFlags(new MultipartRequestFlags(true));
        builder.setType(MultipartType.forValue(5));
        MultipartReplyQueueCaseBuilder queueCase = new MultipartReplyQueueCaseBuilder();
        MultipartReplyQueueBuilder queue = new MultipartReplyQueueBuilder();
        queue.setQueueStats(createQueueStats());
        queueCase.setMultipartReplyQueue(queue.build());
        builder.setMultipartReplyBody(queueCase.build());
        MultipartReplyMessage message = builder.build();
        ByteBuf serializedBuffer = UnpooledByteBufAllocator.DEFAULT.buffer();
        factory.serialize(message, serializedBuffer);
        BufferHelper.checkHeaderV13(serializedBuffer, MESSAGE_TYPE, 56);
        Assert.assertEquals("Wrong type", MultipartType.OFPMPQUEUE.getIntValue(), serializedBuffer.readShort());
        Assert.assertEquals("Wrong flags", message.getFlags(),
                createMultipartRequestFlags(serializedBuffer.readShort()));
        serializedBuffer.skipBytes(PADDING);
        MultipartReplyQueueCase body = (MultipartReplyQueueCase) message.getMultipartReplyBody();
        MultipartReplyQueue messageOutput = body.getMultipartReplyQueue();
        QueueStats queueStats = messageOutput.getQueueStats().get(0);
        Assert.assertEquals("Wrong PortNo", queueStats.getPortNo().intValue(), serializedBuffer.readUnsignedInt());
        Assert.assertEquals("Wrong queue id", queueStats.getQueueId().intValue(), serializedBuffer.readUnsignedInt());
        Assert.assertEquals("Wrong tx bytes", queueStats.getTxBytes().longValue(), serializedBuffer.readLong());
        Assert.assertEquals("Wrong tx packets", queueStats.getTxPackets().longValue(), serializedBuffer.readLong());
        Assert.assertEquals("Wrong tx errors", queueStats.getTxErrors().longValue(), serializedBuffer.readLong());
        Assert.assertEquals("Wrong duration sec", queueStats.getDurationSec().intValue(), serializedBuffer.readInt());
        Assert.assertEquals("Wrong duration nsec", queueStats.getDurationNsec().intValue(), serializedBuffer.readInt());
    }

    @Test
    public void testPortStatsSerialize() throws Exception {
        MultipartReplyMessageBuilder builder = new MultipartReplyMessageBuilder();
        BufferHelper.setupHeader(builder, EncodeConstants.OF13_VERSION_ID);
        builder.setFlags(new MultipartRequestFlags(true));
        builder.setType(MultipartType.forValue(4));
        MultipartReplyPortStatsCaseBuilder portStatsCase = new MultipartReplyPortStatsCaseBuilder();
        MultipartReplyPortStatsBuilder portStats = new MultipartReplyPortStatsBuilder();
        portStats.setPortStats(createPortStats());
        portStatsCase.setMultipartReplyPortStats(portStats.build());
        builder.setMultipartReplyBody(portStatsCase.build());
        MultipartReplyMessage message = builder.build();
        ByteBuf serializedBuffer = UnpooledByteBufAllocator.DEFAULT.buffer();
        factory.serialize(message, serializedBuffer);
        BufferHelper.checkHeaderV13(serializedBuffer, MESSAGE_TYPE, 128);
        Assert.assertEquals("Wrong type", MultipartType.OFPMPPORTSTATS.getIntValue(), serializedBuffer.readShort());
        Assert.assertEquals("Wrong flags", message.getFlags(),
                createMultipartRequestFlags(serializedBuffer.readShort()));
        serializedBuffer.skipBytes(PADDING);
        MultipartReplyPortStatsCase body = (MultipartReplyPortStatsCase) message.getMultipartReplyBody();
        MultipartReplyPortStats messageOutput = body.getMultipartReplyPortStats();
        PortStats portStatsOutput = messageOutput.getPortStats().get(0);
        Assert.assertEquals("Wrong port no", portStatsOutput.getPortNo().intValue(), serializedBuffer.readInt());
        serializedBuffer.skipBytes(4);
        Assert.assertEquals("Wrong rx packets", portStatsOutput.getRxPackets().longValue(),
                serializedBuffer.readLong());
        Assert.assertEquals("Wrong tx packets", portStatsOutput.getTxPackets().longValue(),
                serializedBuffer.readLong());
        Assert.assertEquals("Wrong rx bytes", portStatsOutput.getRxBytes().longValue(), serializedBuffer.readLong());
        Assert.assertEquals("Wrong tx bytes", portStatsOutput.getTxBytes().longValue(), serializedBuffer.readLong());
        Assert.assertEquals("Wrong rx dropped", portStatsOutput.getRxDropped().longValue(),
                serializedBuffer.readLong());
        Assert.assertEquals("Wrong tx dropped", portStatsOutput.getTxDropped().longValue(),
                serializedBuffer.readLong());
        Assert.assertEquals("Wrong rx errors", portStatsOutput.getRxErrors().longValue(), serializedBuffer.readLong());
        Assert.assertEquals("Wrong tx errors", portStatsOutput.getTxErrors().longValue(), serializedBuffer.readLong());
        Assert.assertEquals("Wrong rx frame err", portStatsOutput.getRxFrameErr().longValue(),
                serializedBuffer.readLong());
        Assert.assertEquals("Wrong rx over err", portStatsOutput.getRxOverErr().longValue(),
                serializedBuffer.readLong());
        Assert.assertEquals("Wrong rx crc err", portStatsOutput.getRxCrcErr().longValue(), serializedBuffer.readLong());
        Assert.assertEquals("Wrong collisions", portStatsOutput.getCollisions().longValue(),
                serializedBuffer.readLong());
        Assert.assertEquals("Wrong duration sec", portStatsOutput.getDurationSec().intValue(),
                serializedBuffer.readInt());
        Assert.assertEquals("Wrong duration nsec", portStatsOutput.getDurationNsec().intValue(),
                serializedBuffer.readInt());
    }

    @Test
    public void testTableSerialize() throws Exception {
        MultipartReplyMessageBuilder builder = new MultipartReplyMessageBuilder();
        BufferHelper.setupHeader(builder, EncodeConstants.OF13_VERSION_ID);
        builder.setFlags(new MultipartRequestFlags(true));
        builder.setType(MultipartType.forValue(3));
        MultipartReplyTableCaseBuilder tableCase = new MultipartReplyTableCaseBuilder();
        MultipartReplyTableBuilder table = new MultipartReplyTableBuilder();
        table.setTableStats(createTableStats());
        tableCase.setMultipartReplyTable(table.build());
        builder.setMultipartReplyBody(tableCase.build());
        MultipartReplyMessage message = builder.build();
        ByteBuf serializedBuffer = UnpooledByteBufAllocator.DEFAULT.buffer();
        factory.serialize(message, serializedBuffer);
        BufferHelper.checkHeaderV13(serializedBuffer, MESSAGE_TYPE, 40);
        Assert.assertEquals("Wrong type", MultipartType.OFPMPTABLE.getIntValue(), serializedBuffer.readShort());
        Assert.assertEquals("Wrong flags", message.getFlags(),
                createMultipartRequestFlags(serializedBuffer.readShort()));
        serializedBuffer.skipBytes(PADDING);
        MultipartReplyTableCase body = (MultipartReplyTableCase) message.getMultipartReplyBody();
        MultipartReplyTable messageOutput = body.getMultipartReplyTable();
        TableStats tableStats = messageOutput.getTableStats().get(0);
        Assert.assertEquals("Wrong tableId", tableStats.getTableId().shortValue(), serializedBuffer.readUnsignedByte());
        serializedBuffer.skipBytes(3);
        Assert.assertEquals("Wrong active count", tableStats.getActiveCount().longValue(), serializedBuffer.readInt());
        Assert.assertEquals("Wrong lookup count", tableStats.getLookupCount().longValue(), serializedBuffer.readLong());
        Assert.assertEquals("Wrong matched count", tableStats.getMatchedCount().longValue(),
                serializedBuffer.readLong());
    }

    @Test
    public void testAggregateSerialize() throws Exception {
        MultipartReplyMessageBuilder builder = new MultipartReplyMessageBuilder();
        BufferHelper.setupHeader(builder, EncodeConstants.OF13_VERSION_ID);
        builder.setFlags(new MultipartRequestFlags(true));
        builder.setType(MultipartType.forValue(2));
        final MultipartReplyAggregateCaseBuilder aggregateCase = new MultipartReplyAggregateCaseBuilder();
        MultipartReplyAggregateBuilder aggregate = new MultipartReplyAggregateBuilder();
        aggregate.setPacketCount(Uint64.ONE);
        aggregate.setByteCount(Uint64.ONE);
        aggregate.setFlowCount(Uint32.ONE);
        aggregateCase.setMultipartReplyAggregate(aggregate.build());
        builder.setMultipartReplyBody(aggregateCase.build());
        MultipartReplyMessage message = builder.build();
        ByteBuf serializedBuffer = UnpooledByteBufAllocator.DEFAULT.buffer();
        factory.serialize(message, serializedBuffer);
        BufferHelper.checkHeaderV13(serializedBuffer, MESSAGE_TYPE, 40);
        Assert.assertEquals("Wrong type", MultipartType.OFPMPAGGREGATE.getIntValue(), serializedBuffer.readShort());
        Assert.assertEquals("Wrong flags", message.getFlags(),
                createMultipartRequestFlags(serializedBuffer.readShort()));
        serializedBuffer.skipBytes(PADDING);
        MultipartReplyAggregateCase body = (MultipartReplyAggregateCase) message.getMultipartReplyBody();
        MultipartReplyAggregate messageOutput = body.getMultipartReplyAggregate();
        Assert.assertEquals("Wrong Packet count", messageOutput.getPacketCount().longValue(),
                serializedBuffer.readLong());
        Assert.assertEquals("Wrong Byte count", messageOutput.getByteCount().longValue(), serializedBuffer.readLong());
        Assert.assertEquals("Wrong Flow count", messageOutput.getFlowCount().longValue(), serializedBuffer.readInt());
        serializedBuffer.skipBytes(4);
    }

    @Test
    public void testFlowSerialize() throws Exception {
        MultipartReplyMessageBuilder builder = new MultipartReplyMessageBuilder();
        BufferHelper.setupHeader(builder, EncodeConstants.OF13_VERSION_ID);
        builder.setFlags(new MultipartRequestFlags(true));
        builder.setType(MultipartType.forValue(1));
        MultipartReplyFlowCaseBuilder flowCase = new MultipartReplyFlowCaseBuilder();
        MultipartReplyFlowBuilder flow = new MultipartReplyFlowBuilder();
        flow.setFlowStats(createFlowStats());
        flowCase.setMultipartReplyFlow(flow.build());
        builder.setMultipartReplyBody(flowCase.build());
        MultipartReplyMessage message = builder.build();
        ByteBuf serializedBuffer = UnpooledByteBufAllocator.DEFAULT.buffer();
        factory.serialize(message, serializedBuffer);
        BufferHelper.checkHeaderV13(serializedBuffer, MESSAGE_TYPE, 192);
        Assert.assertEquals("Wrong type", MultipartType.OFPMPFLOW.getIntValue(), serializedBuffer.readShort());
        Assert.assertEquals("Wrong flags", message.getFlags(),
                createMultipartRequestFlags(serializedBuffer.readShort()));
        serializedBuffer.skipBytes(PADDING);
        testFlowBody(message.getMultipartReplyBody(), serializedBuffer);
    }

    @Test
    public void testDescSerialize() throws Exception {
        MultipartReplyMessageBuilder builder = new MultipartReplyMessageBuilder();
        BufferHelper.setupHeader(builder, EncodeConstants.OF13_VERSION_ID);
        builder.setFlags(new MultipartRequestFlags(true));
        builder.setType(MultipartType.forValue(0));
        final MultipartReplyDescCaseBuilder descCase = new MultipartReplyDescCaseBuilder();
        MultipartReplyDescBuilder desc = new MultipartReplyDescBuilder();
        desc.setMfrDesc("Test");
        desc.setHwDesc("Test");
        desc.setSwDesc("Test");
        desc.setSerialNum("12345");
        desc.setDpDesc("Test");
        descCase.setMultipartReplyDesc(desc.build());
        builder.setMultipartReplyBody(descCase.build());
        MultipartReplyMessage message = builder.build();
        ByteBuf serializedBuffer = UnpooledByteBufAllocator.DEFAULT.buffer();
        factory.serialize(message, serializedBuffer);
        BufferHelper.checkHeaderV13(serializedBuffer, MESSAGE_TYPE, 1072);
        Assert.assertEquals("Wrong type", MultipartType.OFPMPDESC.getIntValue(), serializedBuffer.readShort());
        Assert.assertEquals("Wrong flags", message.getFlags(),
                createMultipartRequestFlags(serializedBuffer.readShort()));
        serializedBuffer.skipBytes(PADDING);
        Assert.assertEquals("Wrong desc body", message.getMultipartReplyBody(), decodeDescBody(serializedBuffer));
    }

    private static void testFlowBody(MultipartReplyBody body, ByteBuf output) {
        MultipartReplyFlowCase flowCase = (MultipartReplyFlowCase) body;
        MultipartReplyFlow flow = flowCase.getMultipartReplyFlow();
        FlowStats flowStats = flow.getFlowStats().get(0);
        Assert.assertEquals("Wrong length", 176, output.readShort());
        Assert.assertEquals("Wrong Table ID", flowStats.getTableId().intValue(), output.readUnsignedByte());
        output.skipBytes(1);
        Assert.assertEquals("Wrong duration sec", flowStats.getDurationSec().intValue(), output.readInt());
        Assert.assertEquals("Wrong duration nsec", flowStats.getDurationNsec().intValue(), output.readInt());
        Assert.assertEquals("Wrong priority", flowStats.getPriority().intValue(), output.readShort());
        Assert.assertEquals("Wrong idle timeout", flowStats.getIdleTimeout().intValue(), output.readShort());
        Assert.assertEquals("Wrong hard timeout", flowStats.getHardTimeout().intValue(), output.readShort());
        output.skipBytes(6);
        Assert.assertEquals("Wrong cookie", flowStats.getCookie().longValue(), output.readLong());
        Assert.assertEquals("Wrong Packet count", flowStats.getPacketCount().longValue(), output.readLong());
        Assert.assertEquals("Wrong Byte count", flowStats.getByteCount().longValue(), output.readLong());
        Assert.assertEquals("Wrong match type", 1, output.readUnsignedShort());
        output.skipBytes(Short.BYTES);
        Assert.assertEquals("Wrong oxm class", 0x8000, output.readUnsignedShort());
        short fieldAndMask = output.readUnsignedByte();
        Assert.assertEquals("Wrong oxm hasMask", 0, fieldAndMask & 1);
        Assert.assertEquals("Wrong oxm field", 1, fieldAndMask >> 1);
        output.skipBytes(Byte.BYTES);
        Assert.assertEquals("Wrong oxm value", 42, output.readUnsignedInt());
        Assert.assertEquals("Wrong oxm class", 0x8000, output.readUnsignedShort());
        fieldAndMask = output.readUnsignedByte();
        Assert.assertEquals("Wrong oxm hasMask", 0, fieldAndMask & 1);
        Assert.assertEquals("Wrong oxm field", 9, fieldAndMask >> 1);
        output.skipBytes(Byte.BYTES);
        Assert.assertEquals("Wrong oxm value", 4, output.readUnsignedByte());
        output.skipBytes(7);
        Assert.assertEquals("Wrong instruction type", 1, output.readUnsignedShort());
        Assert.assertEquals("Wrong instruction length", 8, output.readUnsignedShort());
        Assert.assertEquals("Wrong instruction table-id", 5, output.readUnsignedByte());
        output.skipBytes(3);
        Assert.assertEquals("Wrong instruction type", 2, output.readUnsignedShort());
        Assert.assertEquals("Wrong instruction length", 24, output.readUnsignedShort());
        output.skipBytes(4);
        byte[] actual = new byte[8];
        output.readBytes(actual);
        Assert.assertEquals("Wrong instruction metadata", "00 01 02 03 04 05 06 07",
                ByteBufUtils.bytesToHexString(actual));
        actual = new byte[8];
        output.readBytes(actual);
        Assert.assertEquals("Wrong instruction metadata-mask", "07 06 05 04 03 02 01 00",
                ByteBufUtils.bytesToHexString(actual));
        Assert.assertEquals("Wrong instruction type", 5, output.readUnsignedShort());
        Assert.assertEquals("Wrong instruction length", 8, output.readUnsignedShort());
        output.skipBytes(4);
        Assert.assertEquals("Wrong instruction type", 6, output.readUnsignedShort());
        Assert.assertEquals("Wrong instruction length", 8, output.readUnsignedShort());
        Assert.assertEquals("Wrong instruction meter-id", 42, output.readUnsignedInt());
        Assert.assertEquals("Wrong instruction type", 3, output.readUnsignedShort());
        Assert.assertEquals("Wrong instruction length", 32, output.readUnsignedShort());
        output.skipBytes(4);
        Assert.assertEquals("Wrong action type", 0, output.readUnsignedShort());
        Assert.assertEquals("Wrong action length", 16, output.readUnsignedShort());
        Assert.assertEquals("Wrong action type", 45, output.readUnsignedInt());
        Assert.assertEquals("Wrong action type", 55, output.readUnsignedShort());
        output.skipBytes(6);
        Assert.assertEquals("Wrong action type", 23, output.readUnsignedShort());
        Assert.assertEquals("Wrong action length", 8, output.readUnsignedShort());
        Assert.assertEquals("Wrong action type", 64, output.readUnsignedByte());
        output.skipBytes(3);
        Assert.assertEquals("Wrong instruction type", 4, output.readUnsignedShort());
        Assert.assertEquals("Wrong instruction length", 24, output.readUnsignedShort());
        output.skipBytes(4);
        Assert.assertEquals("Wrong action type", 17, output.readUnsignedShort());
        Assert.assertEquals("Wrong action length", 8, output.readUnsignedShort());
        Assert.assertEquals("Wrong action ethertype", 14, output.readUnsignedShort());
        output.skipBytes(2);
        Assert.assertEquals("Wrong action type", 27, output.readUnsignedShort());
        Assert.assertEquals("Wrong action length", 8, output.readUnsignedShort());
        output.skipBytes(4);
        Assert.assertTrue("Not all data were read", output.readableBytes() == 0);
    }

    private static List<Ports> createPortList() {
        PortsBuilder builder = new PortsBuilder();
        builder.setPortNo(Uint32.ONE);
        builder.setHwAddr(new MacAddress("94:de:80:a6:61:40"));
        builder.setName("Port name");
        builder.setConfig(new PortConfig(true, false, true, false));
        builder.setState(new PortState(true, false, true));
        builder.setCurrentFeatures(new PortFeatures(true, false, true, false, true, false, true, false, true, false,
                true, false, true, false, true, false));
        builder.setAdvertisedFeatures(new PortFeatures(true, false, true, false, true, false, true, false, true, false,
                true, false, true, false, true, false));
        builder.setSupportedFeatures(new PortFeatures(true, false, true, false, true, false, true, false, true, false,
                true, false, true, false, true, false));
        builder.setPeerFeatures(new PortFeatures(true, false, true, false, true, false, true, false, true, false, true,
                false, true, false, true, false));
        builder.setCurrSpeed(Uint32.valueOf(1234));
        builder.setMaxSpeed(Uint32.valueOf(1234));
        List<Ports> list = new ArrayList<>();
        list.add(builder.build());
        return list;
    }

    private static PortConfig createPortConfig(long input) {
        final Boolean _portDown = (input & 1 << 0) > 0;
        final Boolean _noRecv = (input & 1 << 2) > 0;
        final Boolean _noFwd = (input & 1 << 5) > 0;
        final Boolean _noPacketIn = (input & 1 << 6) > 0;
        return new PortConfig(_noFwd, _noPacketIn, _noRecv, _portDown);
    }

    private static PortFeatures createPortFeatures(long input) {
        final Boolean _10mbHd = (input & 1 << 0) > 0;
        final Boolean _10mbFd = (input & 1 << 1) > 0;
        final Boolean _100mbHd = (input & 1 << 2) > 0;
        final Boolean _100mbFd = (input & 1 << 3) > 0;
        final Boolean _1gbHd = (input & 1 << 4) > 0;
        final Boolean _1gbFd = (input & 1 << 5) > 0;
        final Boolean _10gbFd = (input & 1 << 6) > 0;
        final Boolean _40gbFd = (input & 1 << 7) > 0;
        final Boolean _100gbFd = (input & 1 << 8) > 0;
        final Boolean _1tbFd = (input & 1 << 9) > 0;
        final Boolean _other = (input & 1 << 10) > 0;
        final Boolean _copper = (input & 1 << 11) > 0;
        final Boolean _fiber = (input & 1 << 12) > 0;
        final Boolean _autoneg = (input & 1 << 13) > 0;
        final Boolean _pause = (input & 1 << 14) > 0;
        final Boolean _pauseAsym = (input & 1 << 15) > 0;
        return new PortFeatures(_100gbFd, _100mbFd, _100mbHd, _10gbFd, _10mbFd, _10mbHd, _1gbFd, _1gbHd, _1tbFd,
                _40gbFd, _autoneg, _copper, _fiber, _other, _pause, _pauseAsym);
    }

    private static PortState createPortState(long input) {
        final Boolean one = (input & 1 << 0) > 0;
        final Boolean two = (input & 1 << 1) > 0;
        final Boolean three = (input & 1 << 2) > 0;
        return new PortState(two, one, three);
    }

    private static List<Bands> decodeBandsList(ByteBuf input) {
        final List<Bands> bandsList = new ArrayList<>();
        final BandsBuilder bandsBuilder = new BandsBuilder();
        final MeterBandDropCaseBuilder dropCaseBuilder = new MeterBandDropCaseBuilder();
        MeterBandDropBuilder dropBand = new MeterBandDropBuilder();
        dropBand.setType(MeterBandType.forValue(input.readUnsignedShort()));
        input.skipBytes(Short.SIZE / Byte.SIZE);
        dropBand.setRate(input.readUnsignedInt());
        dropBand.setBurstSize(input.readUnsignedInt());
        dropCaseBuilder.setMeterBandDrop(dropBand.build());
        bandsList.add(bandsBuilder.setMeterBand(dropCaseBuilder.build()).build());
        final MeterBandDscpRemarkCaseBuilder dscpCaseBuilder = new MeterBandDscpRemarkCaseBuilder();
        MeterBandDscpRemarkBuilder dscpRemarkBand = new MeterBandDscpRemarkBuilder();
        dscpRemarkBand.setType(MeterBandType.forValue(input.readUnsignedShort()));
        input.skipBytes(Short.SIZE / Byte.SIZE);
        dscpRemarkBand.setRate(input.readUnsignedInt());
        dscpRemarkBand.setBurstSize(input.readUnsignedInt());
        dscpRemarkBand.setPrecLevel((short) 3);
        dscpCaseBuilder.setMeterBandDscpRemark(dscpRemarkBand.build());
        bandsList.add(bandsBuilder.setMeterBand(dscpCaseBuilder.build()).build());
        return bandsList;
    }

    private static List<MeterConfig> createMeterConfig() {
        MeterConfigBuilder builder = new MeterConfigBuilder();
        builder.setFlags(new MeterFlags(true, false, true, false));
        builder.setMeterId(new MeterId(Uint32.ONE));
        builder.setBands(createBandsList());
        List<MeterConfig> list = new ArrayList<>();
        list.add(builder.build());
        return list;
    }

    private static MeterBandTypeBitmap createMeterBandTypeBitmap(int input) {
        final Boolean one = (input & 1 << 0) > 0;
        final Boolean two = (input & 1 << 1) > 0;
        return new MeterBandTypeBitmap(one, two);
    }

    private static List<Bands> createBandsList() {
        final List<Bands> bandsList = new ArrayList<>();
        final BandsBuilder bandsBuilder = new BandsBuilder();
        final MeterBandDropCaseBuilder dropCaseBuilder = new MeterBandDropCaseBuilder();
        MeterBandDropBuilder dropBand = new MeterBandDropBuilder();
        dropBand.setType(MeterBandType.OFPMBTDROP);
        dropBand.setRate(Uint32.ONE);
        dropBand.setBurstSize(Uint32.TWO);
        dropCaseBuilder.setMeterBandDrop(dropBand.build());
        bandsList.add(bandsBuilder.setMeterBand(dropCaseBuilder.build()).build());
        final MeterBandDscpRemarkCaseBuilder dscpCaseBuilder = new MeterBandDscpRemarkCaseBuilder();
        MeterBandDscpRemarkBuilder dscpRemarkBand = new MeterBandDscpRemarkBuilder();
        dscpRemarkBand.setType(MeterBandType.OFPMBTDSCPREMARK);
        dscpRemarkBand.setRate(Uint32.ONE);
        dscpRemarkBand.setBurstSize(Uint32.ONE);
        dscpRemarkBand.setPrecLevel((short) 3);
        dscpCaseBuilder.setMeterBandDscpRemark(dscpRemarkBand.build());
        bandsList.add(bandsBuilder.setMeterBand(dscpCaseBuilder.build()).build());
        return bandsList;
    }

    private static MeterFlags createMeterFlags(int input) {
        final Boolean one = (input & 1 << 0) > 0;
        final Boolean two = (input & 1 << 1) > 0;
        final Boolean three = (input & 1 << 2) > 0;
        final Boolean four = (input & 1 << 3) > 0;
        return new MeterFlags(three, one, two, four);
    }

    private static List<MeterStats> createMeterStats() {
        MeterStatsBuilder builder = new MeterStatsBuilder();
        builder.setMeterId(new MeterId(Uint32.ONE));
        builder.setFlowCount(Uint32.ONE);
        builder.setPacketInCount(Uint64.ONE);
        builder.setByteInCount(Uint64.ONE);
        builder.setDurationSec(Uint32.ONE);
        builder.setDurationNsec(Uint32.ONE);
        builder.setMeterBandStats(createMeterBandStats());
        List<MeterStats> list = new ArrayList<>();
        list.add(builder.build());
        return list;
    }

    private static List<MeterBandStats> createMeterBandStats() {
        MeterBandStatsBuilder builder = new MeterBandStatsBuilder();
        builder.setPacketBandCount(Uint64.ONE);
        builder.setByteBandCount(Uint64.ONE);
        List<MeterBandStats> list = new ArrayList<>();
        list.add(builder.build());
        return list;
    }

    private static ActionType createActionType(int input) {
        final Boolean one = (input & 1 << 0) > 0;
        final Boolean two = (input & 1 << 1) > 0;
        final Boolean three = (input & 1 << 2) > 0;
        final Boolean four = (input & 1 << 3) > 0;
        final Boolean five = (input & 1 << 4) > 0;
        final Boolean six = (input & 1 << 5) > 0;
        final Boolean seven = (input & 1 << 6) > 0;
        final Boolean eight = (input & 1 << 7) > 0;
        final Boolean nine = (input & 1 << 8) > 0;
        final Boolean ten = (input & 1 << 9) > 0;
        final Boolean eleven = (input & 1 << 10) > 0;
        final Boolean twelve = (input & 1 << 11) > 0;
        final Boolean thirteen = (input & 1 << 12) > 0;
        final Boolean fourteen = (input & 1 << 13) > 0;
        final Boolean fifthteen = (input & 1 << 14) > 0;
        final Boolean sixteen = (input & 1 << 15) > 0;
        final Boolean seventeen = (input & 1 << 16) > 0;
        return new ActionType(three, two, five, thirteen, seventeen, eleven, one, nine, sixteen, seven, eight,
                fifthteen, six, fourteen, four, twelve, ten);
    }

    private static List<ActionType> createActionType() {
        ActionType actionType1 = new ActionType(true, false, true, false, true, false, true, false, true, false, true,
                false, true, false, true, false, true);
        ActionType actionType2 = new ActionType(true, false, false, false, true, false, true, false, true, false, true,
                false, true, false, true, true, true);
        ActionType actionType3 = new ActionType(true, false, true, false, true, false, true, false, true, false, true,
                false, true, false, true, false, true);
        ActionType actionType4 = new ActionType(true, false, true, false, true, false, true, false, true, false, true,
                false, true, false, true, false, true);
        List<ActionType> list = new ArrayList<>();
        list.add(actionType1);
        list.add(actionType2);
        list.add(actionType3);
        list.add(actionType4);
        return list;

    }

    private static GroupCapabilities createGroupCapabilities(int input) {
        final Boolean one = (input & 1 << 0) > 0;
        final Boolean two = (input & 1 << 1) > 0;
        final Boolean three = (input & 1 << 2) > 0;
        final Boolean four = (input & 1 << 3) > 0;
        return new GroupCapabilities(three, four, two, one);
    }

    private static GroupTypes createGroupTypes(int input) {
        final Boolean one = (input & 1 << 0) > 0;
        final Boolean two = (input & 1 << 1) > 0;
        final Boolean three = (input & 1 << 2) > 0;
        final Boolean four = (input & 1 << 3) > 0;
        return new GroupTypes(one, four, three, two);
    }

    private static List<GroupDesc> createGroupDesc() {
        GroupDescBuilder builder = new GroupDescBuilder();
        builder.setType(GroupType.forValue(1));
        builder.setGroupId(new GroupId(Uint32.ONE));
        builder.setBucketsList(createBucketsList());
        List<GroupDesc> list = new ArrayList<>();
        list.add(builder.build());
        return list;
    }

    private static List<GroupStats> createGroupStats() {
        GroupStatsBuilder builder = new GroupStatsBuilder();
        builder.setGroupId(new GroupId(Uint32.ONE));
        builder.setRefCount(Uint32.ONE);
        builder.setPacketCount(Uint64.ONE);
        builder.setByteCount(Uint64.ONE);
        builder.setDurationSec(Uint32.ONE);
        builder.setDurationNsec(Uint32.ONE);
        builder.setBucketStats(createBucketStats());
        List<GroupStats> list = new ArrayList<>();
        list.add(builder.build());
        return list;
    }

    private static List<BucketsList> createBucketsList() {
        BucketsListBuilder builder = new BucketsListBuilder();
        builder.setWeight(Uint16.ONE);
        builder.setWatchPort(new PortNumber(Uint32.ONE));
        builder.setWatchGroup(Uint32.ONE);
        builder.setAction(createActionList());
        List<BucketsList> list = new ArrayList<>();
        list.add(builder.build());
        return list;
    }

    private static List<Action> createActionList() {
        final List<Action> actions = new ArrayList<>();
        OutputActionCaseBuilder caseBuilder = new OutputActionCaseBuilder();
        OutputActionBuilder outputBuilder = new OutputActionBuilder();
        outputBuilder.setPort(new PortNumber(Uint32.valueOf(45)));
        outputBuilder.setMaxLength(55);
        caseBuilder.setOutputAction(outputBuilder.build());
        ActionBuilder actionBuilder = new ActionBuilder();
        actionBuilder.setActionChoice(caseBuilder.build());
        actions.add(actionBuilder.build());
        actionBuilder = new ActionBuilder();
        SetNwTtlCaseBuilder ttlCaseBuilder = new SetNwTtlCaseBuilder();
        SetNwTtlActionBuilder ttlActionBuilder = new SetNwTtlActionBuilder();
        ttlActionBuilder.setNwTtl((short) 64);
        ttlCaseBuilder.setSetNwTtlAction(ttlActionBuilder.build());
        actionBuilder.setActionChoice(ttlCaseBuilder.build());
        actions.add(actionBuilder.build());
        return actions;
    }

    private static List<BucketStats> createBucketStats() {
        BucketStatsBuilder builder = new BucketStatsBuilder();
        builder.setPacketCount(Uint64.ONE);
        builder.setByteCount(Uint64.ONE);
        List<BucketStats> list = new ArrayList<>();
        list.add(builder.build());
        return list;
    }

    private static List<QueueStats> createQueueStats() {
        QueueStatsBuilder builder = new QueueStatsBuilder();
        builder.setPortNo(Uint32.ONE);
        builder.setQueueId(Uint32.ONE);
        builder.setTxBytes(Uint64.ONE);
        builder.setTxPackets(Uint64.ONE);
        builder.setTxErrors(Uint64.ONE);
        builder.setDurationSec(Uint32.ONE);
        builder.setDurationNsec(Uint32.ONE);
        List<QueueStats> list = new ArrayList<>();
        list.add(builder.build());
        return list;
    }

    private static List<PortStats> createPortStats() {
        PortStatsBuilder builder = new PortStatsBuilder();
        builder.setPortNo(Uint32.ONE);
        builder.setRxPackets(Uint64.ONE);
        builder.setTxPackets(Uint64.ONE);
        builder.setRxBytes(Uint64.ONE);
        builder.setTxBytes(Uint64.ONE);
        builder.setRxDropped(Uint64.ONE);
        builder.setTxDropped(Uint64.ONE);
        builder.setRxErrors(Uint64.ONE);
        builder.setTxErrors(Uint64.ONE);
        builder.setRxFrameErr(Uint64.ONE);
        builder.setRxOverErr(Uint64.ONE);
        builder.setRxCrcErr(Uint64.ONE);
        builder.setCollisions(Uint64.ONE);
        builder.setDurationSec(Uint32.ONE);
        builder.setDurationNsec(Uint32.ONE);
        List<PortStats> list = new ArrayList<>();
        list.add(builder.build());
        return list;
    }

    private static List<TableStats> createTableStats() {
        TableStatsBuilder builder = new TableStatsBuilder();
        builder.setTableId((short) 1);
        builder.setActiveCount(Uint32.ONE);
        builder.setLookupCount(Uint64.ONE);
        builder.setMatchedCount(Uint64.ONE);
        List<TableStats> list = new ArrayList<>();
        list.add(builder.build());
        return list;
    }

    private static List<FlowStats> createFlowStats() {
        FlowStatsBuilder builder = new FlowStatsBuilder();
        builder.setTableId(Uint8.ONE);
        builder.setDurationSec(Uint32.ONE);
        builder.setDurationNsec(Uint32.ONE);
        builder.setPriority(Uint16.ONE);
        builder.setIdleTimeout(Uint16.ONE);
        builder.setHardTimeout(Uint16.ONE);
        builder.setCookie(Uint64.valueOf(1234));
        builder.setPacketCount(Uint64.valueOf(1234));
        builder.setByteCount(Uint64.valueOf(1234));
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
        // Goto_table instruction
        InstructionBuilder builderInstruction = new InstructionBuilder();
        GotoTableCaseBuilder gotoCaseBuilder = new GotoTableCaseBuilder();
        GotoTableBuilder instructionBuilder = new GotoTableBuilder();
        instructionBuilder.setTableId(Uint8.valueOf(5));
        gotoCaseBuilder.setGotoTable(instructionBuilder.build());
        builderInstruction.setInstructionChoice(gotoCaseBuilder.build());
        instructions.add(builderInstruction.build());
        // Write_metadata instruction
        builderInstruction = new InstructionBuilder();
        WriteMetadataCaseBuilder metadataCaseBuilder = new WriteMetadataCaseBuilder();
        WriteMetadataBuilder metadataBuilder = new WriteMetadataBuilder();
        metadataBuilder.setMetadata(ByteBufUtils.hexStringToBytes("00 01 02 03 04 05 06 07"));
        metadataBuilder.setMetadataMask(ByteBufUtils.hexStringToBytes("07 06 05 04 03 02 01 00"));
        metadataCaseBuilder.setWriteMetadata(metadataBuilder.build());
        builderInstruction.setInstructionChoice(metadataCaseBuilder.build());
        instructions.add(builderInstruction.build());
        // Clear_actions instruction
        builderInstruction = new InstructionBuilder();
        builderInstruction.setInstructionChoice(new ClearActionsCaseBuilder().build());
        instructions.add(builderInstruction.build());
        // Meter instruction
        builderInstruction = new InstructionBuilder();
        MeterCaseBuilder meterCaseBuilder = new MeterCaseBuilder();
        MeterBuilder meterBuilder = new MeterBuilder();
        meterBuilder.setMeterId(Uint32.valueOf(42));
        meterCaseBuilder.setMeter(meterBuilder.build());
        builderInstruction.setInstructionChoice(meterCaseBuilder.build());
        instructions.add(builderInstruction.build());
        // Write_actions instruction
        builderInstruction = new InstructionBuilder();
        final WriteActionsCaseBuilder writeActionsCaseBuilder = new WriteActionsCaseBuilder();
        final WriteActionsBuilder writeActionsBuilder = new WriteActionsBuilder();
        OutputActionCaseBuilder caseBuilder = new OutputActionCaseBuilder();
        OutputActionBuilder outputBuilder = new OutputActionBuilder();
        outputBuilder.setPort(new PortNumber(Uint32.valueOf(45)));
        outputBuilder.setMaxLength(55);
        caseBuilder.setOutputAction(outputBuilder.build());
        ActionBuilder actionBuilder = new ActionBuilder();
        actionBuilder.setActionChoice(caseBuilder.build());
        List<Action> actions = new ArrayList<>();
        actions.add(actionBuilder.build());
        actionBuilder = new ActionBuilder();
        SetNwTtlCaseBuilder ttlCaseBuilder = new SetNwTtlCaseBuilder();
        SetNwTtlActionBuilder ttlActionBuilder = new SetNwTtlActionBuilder();
        ttlActionBuilder.setNwTtl((short) 64);
        ttlCaseBuilder.setSetNwTtlAction(ttlActionBuilder.build());
        actionBuilder.setActionChoice(ttlCaseBuilder.build());
        actions.add(actionBuilder.build());
        writeActionsBuilder.setAction(actions);
        writeActionsCaseBuilder.setWriteActions(writeActionsBuilder.build());
        builderInstruction.setInstructionChoice(writeActionsCaseBuilder.build());
        instructions.add(builderInstruction.build());
        // Apply_actions instruction
        builderInstruction = new InstructionBuilder();
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
        builderInstruction.setInstructionChoice(applyActionsCaseBuilder.build());
        instructions.add(builderInstruction.build());
        builder.setInstruction(instructions);
        List<FlowStats> list = new ArrayList<>();
        list.add(builder.build());
        return list;
    }

    private static MultipartRequestFlags createMultipartRequestFlags(int input) {
        final Boolean one = (input & 1 << 0) > 0;
        return new MultipartRequestFlags(one);
    }

    private static MultipartReplyDescCase decodeDescBody(ByteBuf output) {
        final MultipartReplyDescCaseBuilder descCase = new MultipartReplyDescCaseBuilder();
        MultipartReplyDescBuilder desc = new MultipartReplyDescBuilder();
        byte[] mfrDesc = new byte[256];
        output.readBytes(mfrDesc);
        desc.setMfrDesc(new String(mfrDesc).trim());
        byte[] hwDesc = new byte[256];
        output.readBytes(hwDesc);
        desc.setHwDesc(new String(hwDesc).trim());
        byte[] swDesc = new byte[256];
        output.readBytes(swDesc);
        desc.setSwDesc(new String(swDesc).trim());
        byte[] serialNumber = new byte[32];
        output.readBytes(serialNumber);
        desc.setSerialNum(new String(serialNumber).trim());
        byte[] dpDesc = new byte[256];
        output.readBytes(dpDesc);
        desc.setDpDesc(new String(dpDesc).trim());
        descCase.setMultipartReplyDesc(desc.build());
        return descCase.build();
    }
}
