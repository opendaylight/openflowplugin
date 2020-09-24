/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.statistics;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.google.common.collect.Lists;
import java.util.Collections;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;
import org.opendaylight.openflowplugin.api.OFConstants;
import org.opendaylight.openflowplugin.api.openflow.connection.ConnectionContext;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceContext;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceInfo;
import org.opendaylight.openflowplugin.impl.common.MultipartReplyTranslatorUtil;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.ConvertorManager;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.ConvertorManagerFactory;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.statistics.rev130819.FlowAndStatisticsMapList;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.GroupDescStatsReply;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.GroupStatisticsReply;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.GroupTypes;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.group.desc.stats.reply.GroupDescStats;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.group.desc.stats.reply.GroupDescStatsKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.statistics.types.rev130925.AggregateFlowStatistics;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.GroupId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.GroupType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.MultipartRequestFlags;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.MultipartType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.FeaturesReply;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.MultipartReplyMessage;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.MultipartReplyBody;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.MultipartReplyAggregateCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.MultipartReplyFlowCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.MultipartReplyGroupCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.MultipartReplyGroupDescCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.MultipartReplyPortStatsCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.aggregate._case.MultipartReplyAggregateBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.flow._case.MultipartReplyFlowBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.group._case.MultipartReplyGroupBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.group._case.multipart.reply.group.GroupStatsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.group.desc._case.MultipartReplyGroupDescBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.group.desc._case.multipart.reply.group.desc.GroupDescBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.port.stats._case.MultipartReplyPortStatsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.port.stats._case.multipart.reply.port.stats.PortStatsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.port.statistics.rev131214.node.connector.statistics.and.port.number.map.NodeConnectorStatisticsAndPortNumberMap;
import org.opendaylight.yangtools.yang.binding.DataContainer;
import org.opendaylight.yangtools.yang.common.Uint32;
import org.opendaylight.yangtools.yang.common.Uint64;
import org.opendaylight.yangtools.yang.common.Uint8;

public class MultipartReplyTranslatorTest {

    private static final Uint64 DUMMY_DATAPATH_ID = Uint64.valueOf(21);
    private static final Uint32 DUMMY_XID = Uint32.ONE;
    private static final Uint64 DUMMY_BYTE_COUNT = Uint64.valueOf(31);
    private static final Uint64 DUMMY_PACKET_COUNT = Uint64.valueOf(41);
    private static final Uint32 DUMMY_FLOW_COUNT = Uint32.valueOf(51);
    private static final Uint32 DUMMY_PORT_NO = Uint32.valueOf(154);
    private static final Uint64 DUMMY_RX_BYTES = Uint64.valueOf(155);
    private static final Uint64 DUMMY_TX_BYTES = Uint64.valueOf(165);
    private static final Uint64 DUMMY_RX_PACKETS = Uint64.valueOf(175);
    private static final Uint64 DUMMY_TX_PACKETS = Uint64.valueOf(185);
    private static final Uint64 DUMMY_COLLISIONS = Uint64.valueOf(195);
    private static final Uint64 DUMMY_RX_CRC_ERR = Uint64.valueOf(205);
    private static final Uint64 DUMMY_RX_DROPPED = Uint64.valueOf(215);
    private static final Uint64 DUMMY_RX_ERRORS = Uint64.valueOf(225);
    private static final Uint64 DUMMY_RX_FRAME_ERR = Uint64.valueOf(235);
    private static final Uint64 DUMMY_OVER_ERR = Uint64.valueOf(245);
    private static final Uint64 DUMMY_TX_DROPPED = Uint64.valueOf(255);
    private static final Uint64 DUMMY_TX_ERRORS = Uint64.valueOf(265);
    private static final Uint32 DUMMY_DURATION_SEC = Uint32.valueOf(3453);
    private static final Uint32 DUMMY_DURATION_NSEC = Uint32.valueOf(3343);
    private static final GroupId DUMMY_GROUP_ID = new GroupId(Uint32.valueOf(55));
    private static final Uint32 DUMMY_REF_COUNT = Uint32.valueOf(1234);
    private static final GroupTypes DUMMY_GROUPS_TYPE = GroupTypes.GroupAll;
    private static final GroupType DUMMY_GROUP_TYPE = GroupType.OFPGTALL;
    private static final ConvertorManager CONVERTOR_MANAGER = ConvertorManagerFactory.createDefaultManager();

    @Before
    public void setUp() {
    }

    @Test
    public void testTranslateFlow() {
        DeviceContext mockedDeviceContext = mock(DeviceContext.class);

        MultipartReplyMessage multipartReplyMessage =
                prepareMocks(mockedDeviceContext, prepareMultipartReplyFlow(), MultipartType.OFPMPFLOW);

        DataContainer result = MultipartReplyTranslatorUtil.translate(
            multipartReplyMessage,
            mockedDeviceContext.getDeviceInfo(),
                CONVERTOR_MANAGER,
            mockedDeviceContext.oook()).get();

        DataContainer dataObject = validateOutput(result);
        assertTrue(dataObject instanceof FlowAndStatisticsMapList);
    }

    @Test
    public void testTranslateAggregate() {
        DeviceContext mockedDeviceContext = mock(DeviceContext.class);

        MultipartReplyMessage multipartReplyMessage =
                prepareMocks(mockedDeviceContext, prepareMultipartReplyAggregate(), MultipartType.OFPMPAGGREGATE);

        DataContainer result = MultipartReplyTranslatorUtil.translate(
            multipartReplyMessage,
            mockedDeviceContext.getDeviceInfo(),
                CONVERTOR_MANAGER,
            mockedDeviceContext.oook()).get();

        DataContainer dataObject = validateOutput(result);
        assertTrue(dataObject instanceof AggregateFlowStatistics);
        AggregateFlowStatistics message = (AggregateFlowStatistics)dataObject;
        assertEquals(DUMMY_BYTE_COUNT, message.getByteCount().getValue());
        assertEquals(DUMMY_PACKET_COUNT, message.getPacketCount().getValue());
        assertEquals(DUMMY_FLOW_COUNT, message.getFlowCount().getValue());
    }

    @Test
    public void testTranslatePortStats() {
        DeviceContext mockedDeviceContext = mock(DeviceContext.class);

        MultipartReplyMessage multipartReplyMessage =
                prepareMocks(mockedDeviceContext, prepareMultipartReplyPortStats(), MultipartType.OFPMPPORTSTATS);

        DataContainer result = MultipartReplyTranslatorUtil.translate(
            multipartReplyMessage,
            mockedDeviceContext.getDeviceInfo(),
                CONVERTOR_MANAGER,
            mockedDeviceContext.oook()).get();

        DataContainer dataObject = validateOutput(result);
        assertTrue(dataObject instanceof org.opendaylight.yang.gen.v1.urn
                .opendaylight.port.statistics.rev131214.NodeConnectorStatisticsAndPortNumberMap);
        org.opendaylight.yang.gen.v1.urn.opendaylight.port.statistics.rev131214.NodeConnectorStatisticsAndPortNumberMap
                nodeConnectorStatisticsUpdate = (org.opendaylight.yang.gen.v1.urn
                .opendaylight.port.statistics.rev131214.NodeConnectorStatisticsAndPortNumberMap) dataObject;
        var nodeConnectorStatisticsAndPortNumberMaps =
                nodeConnectorStatisticsUpdate.nonnullNodeConnectorStatisticsAndPortNumberMap();
        assertEquals(1, nodeConnectorStatisticsAndPortNumberMaps.size());
        NodeConnectorStatisticsAndPortNumberMap nodeConnectorStatisticsAndPortNumberMap =
                nodeConnectorStatisticsAndPortNumberMaps.values().iterator().next();
        assertEquals("openflow:" + DUMMY_DATAPATH_ID + ":" + DUMMY_PORT_NO,
                nodeConnectorStatisticsAndPortNumberMap.getNodeConnectorId().getValue());
        assertEquals(DUMMY_RX_BYTES, nodeConnectorStatisticsAndPortNumberMap.getBytes().getReceived());
        assertEquals(DUMMY_TX_BYTES, nodeConnectorStatisticsAndPortNumberMap.getBytes().getTransmitted());
        assertEquals(DUMMY_RX_PACKETS, nodeConnectorStatisticsAndPortNumberMap.getPackets().getReceived());
        assertEquals(DUMMY_TX_PACKETS, nodeConnectorStatisticsAndPortNumberMap.getPackets().getTransmitted());
        assertEquals(DUMMY_COLLISIONS, nodeConnectorStatisticsAndPortNumberMap.getCollisionCount());
        assertEquals(DUMMY_RX_CRC_ERR, nodeConnectorStatisticsAndPortNumberMap.getReceiveCrcError());
        assertEquals(DUMMY_RX_DROPPED, nodeConnectorStatisticsAndPortNumberMap.getReceiveDrops());
        assertEquals(DUMMY_RX_ERRORS, nodeConnectorStatisticsAndPortNumberMap.getReceiveErrors());
        assertEquals(DUMMY_RX_FRAME_ERR, nodeConnectorStatisticsAndPortNumberMap.getReceiveFrameError());
        assertEquals(DUMMY_OVER_ERR, nodeConnectorStatisticsAndPortNumberMap.getReceiveOverRunError());
        assertEquals(DUMMY_TX_DROPPED, nodeConnectorStatisticsAndPortNumberMap.getTransmitDrops());
        assertEquals(DUMMY_TX_ERRORS, nodeConnectorStatisticsAndPortNumberMap.getTransmitErrors());
    }

    @Test
    public void testTranslateGroup() {
        DeviceContext mockedDeviceContext = mock(DeviceContext.class);

        MultipartReplyMessage multipartReplyMessage =
                prepareMocks(mockedDeviceContext, prepareMultipartReplyGroup(), MultipartType.OFPMPGROUP);

        DataContainer result = MultipartReplyTranslatorUtil.translate(
            multipartReplyMessage,
            mockedDeviceContext.getDeviceInfo(),
                CONVERTOR_MANAGER,
            mockedDeviceContext.oook()).get();

        DataContainer dataObject = validateOutput(result);
        assertTrue(dataObject instanceof GroupStatisticsReply);
        GroupStatisticsReply groupStatisticsUpdate = (GroupStatisticsReply)dataObject;
        var groupStats = groupStatisticsUpdate.nonnullGroupStats();
        assertEquals(1, groupStats.size());
        var groupStat = groupStats.values().iterator().next();

        assertEquals(DUMMY_BYTE_COUNT, groupStat.getByteCount().getValue());
        assertEquals(DUMMY_DURATION_SEC, groupStat.getDuration().getSecond().getValue());
        assertEquals(DUMMY_DURATION_NSEC, groupStat.getDuration().getNanosecond().getValue());
        assertEquals(DUMMY_GROUP_ID.getValue(), groupStat.getGroupId().getValue());
        assertEquals(DUMMY_PACKET_COUNT, groupStat.getPacketCount().getValue());
        assertEquals(DUMMY_REF_COUNT, groupStat.getRefCount().getValue());
    }

    @Test
    public void testTranslateGroupDesc() {
        DeviceContext mockedDeviceContext = mock(DeviceContext.class);

        MultipartReplyMessage multipartReplyMessage =
                prepareMocks(mockedDeviceContext, prepareMultipartReplyGroupDesc(), MultipartType.OFPMPGROUPDESC);

        DataContainer result = MultipartReplyTranslatorUtil.translate(
            multipartReplyMessage,
            mockedDeviceContext.getDeviceInfo(),
                CONVERTOR_MANAGER,
            mockedDeviceContext.oook()).get();

        DataContainer dataObject = validateOutput(result);
        assertTrue(dataObject instanceof GroupDescStatsReply);
        GroupDescStatsReply groupStatistics = (GroupDescStatsReply) dataObject;
        Map<GroupDescStatsKey, GroupDescStats> groupDescStats = groupStatistics.nonnullGroupDescStats();
        assertEquals(1, groupDescStats.size());
        GroupDescStats groupDescStat = groupDescStats.values().iterator().next();
        assertEquals(DUMMY_GROUP_ID.getValue(),groupDescStat.getGroupId().getValue());
        assertEquals(DUMMY_GROUPS_TYPE,groupDescStat.getGroupType());
    }

    private static MultipartReplyBody prepareMultipartReplyGroupDesc() {
        GroupDescBuilder groupDescBuilder = new GroupDescBuilder();
        groupDescBuilder.setGroupId(DUMMY_GROUP_ID);
        groupDescBuilder.setBucketsList(Collections.emptyList());
        groupDescBuilder.setType(DUMMY_GROUP_TYPE);
        MultipartReplyGroupDescBuilder multipartReplyGroupDescBuilder = new MultipartReplyGroupDescBuilder();
        multipartReplyGroupDescBuilder.setGroupDesc(Lists.newArrayList(groupDescBuilder.build()));
        MultipartReplyGroupDescCaseBuilder multipartReplyGroupDescCaseBuilder =
                new MultipartReplyGroupDescCaseBuilder();
        multipartReplyGroupDescCaseBuilder.setMultipartReplyGroupDesc(multipartReplyGroupDescBuilder.build());
        return multipartReplyGroupDescCaseBuilder.build();
    }

    private static MultipartReplyBody prepareMultipartReplyGroup() {
        GroupStatsBuilder groupStatsBuilder = new GroupStatsBuilder();
        groupStatsBuilder.setByteCount(DUMMY_BYTE_COUNT);
        groupStatsBuilder.setBucketStats(Collections.emptyList());
        groupStatsBuilder.setDurationSec(DUMMY_DURATION_SEC);
        groupStatsBuilder.setDurationNsec(DUMMY_DURATION_NSEC);
        groupStatsBuilder.setGroupId(DUMMY_GROUP_ID);
        groupStatsBuilder.setPacketCount(DUMMY_PACKET_COUNT);
        groupStatsBuilder.setRefCount(DUMMY_REF_COUNT);
        MultipartReplyGroupBuilder multipartReplyGroupBuilder = new MultipartReplyGroupBuilder();
        multipartReplyGroupBuilder.setGroupStats(Lists.newArrayList(groupStatsBuilder.build()));
        MultipartReplyGroupCaseBuilder multipartReplyGroupCaseBuilder = new MultipartReplyGroupCaseBuilder();
        multipartReplyGroupCaseBuilder.setMultipartReplyGroup(multipartReplyGroupBuilder.build());
        return multipartReplyGroupCaseBuilder.build();
    }

    private static MultipartReplyBody prepareMultipartReplyPortStats() {
        PortStatsBuilder dummyPortStatBuilder = new PortStatsBuilder();
        dummyPortStatBuilder.setPortNo(DUMMY_PORT_NO);
        dummyPortStatBuilder.setRxBytes(DUMMY_RX_BYTES);
        dummyPortStatBuilder.setTxBytes(DUMMY_TX_BYTES);
        dummyPortStatBuilder.setRxPackets(DUMMY_RX_PACKETS);
        dummyPortStatBuilder.setTxPackets(DUMMY_TX_PACKETS);
        dummyPortStatBuilder.setCollisions(DUMMY_COLLISIONS);
        dummyPortStatBuilder.setRxCrcErr(DUMMY_RX_CRC_ERR);
        dummyPortStatBuilder.setRxDropped(DUMMY_RX_DROPPED);
        dummyPortStatBuilder.setRxErrors(DUMMY_RX_ERRORS);
        dummyPortStatBuilder.setRxFrameErr(DUMMY_RX_FRAME_ERR);
        dummyPortStatBuilder.setRxOverErr(DUMMY_OVER_ERR);
        dummyPortStatBuilder.setTxDropped(DUMMY_TX_DROPPED);
        dummyPortStatBuilder.setTxErrors(DUMMY_TX_ERRORS);
        MultipartReplyPortStatsBuilder multipartReplyPortStatsBuilder = new MultipartReplyPortStatsBuilder();
        multipartReplyPortStatsBuilder.setPortStats(Lists.newArrayList(dummyPortStatBuilder.build()));
        MultipartReplyPortStatsCaseBuilder multipartReplyPortStatsCaseBuilder =
                new MultipartReplyPortStatsCaseBuilder();
        multipartReplyPortStatsCaseBuilder.setMultipartReplyPortStats(multipartReplyPortStatsBuilder.build());
        return multipartReplyPortStatsCaseBuilder.build();
    }


    private static MultipartReplyBody prepareMultipartReplyAggregate() {
        MultipartReplyAggregateBuilder multipartReplyAggregateBuilder = new MultipartReplyAggregateBuilder();
        multipartReplyAggregateBuilder.setByteCount(DUMMY_BYTE_COUNT);
        multipartReplyAggregateBuilder.setPacketCount(DUMMY_PACKET_COUNT);
        multipartReplyAggregateBuilder.setFlowCount(DUMMY_FLOW_COUNT);
        MultipartReplyAggregateCaseBuilder multipartReplyAggregateCaseBuilder =
                new MultipartReplyAggregateCaseBuilder();
        multipartReplyAggregateCaseBuilder.setMultipartReplyAggregate(multipartReplyAggregateBuilder.build());
        return multipartReplyAggregateCaseBuilder.build();
    }

    private static MultipartReplyBody prepareMultipartReplyFlow() {
        MultipartReplyFlowCaseBuilder multipartReplyFlowCaseBuilder = new MultipartReplyFlowCaseBuilder();
        MultipartReplyFlowBuilder multipartReplyFlowBuilder = new MultipartReplyFlowBuilder();
        multipartReplyFlowBuilder.setFlowStats(Collections.emptyList());
        multipartReplyFlowCaseBuilder.setMultipartReplyFlow(multipartReplyFlowBuilder.build());
        return multipartReplyFlowCaseBuilder.build();
    }

    private static MultipartReplyMessage prepareMocks(final DeviceContext mockedDeviceContext,
                                               final MultipartReplyBody multipartReplyBody,
                                               final MultipartType multipartType) {
        ConnectionContext mockedConnectionContext = mock(ConnectionContext.class);
        FeaturesReply mockedFeaturesReply = mock(FeaturesReply.class);
        when(mockedFeaturesReply.getVersion()).thenReturn(Uint8.valueOf(OFConstants.OFP_VERSION_1_3));
        when(mockedFeaturesReply.getDatapathId()).thenReturn(DUMMY_DATAPATH_ID);

        DeviceInfo deviceInfo = mock(DeviceInfo.class);
        when(deviceInfo.getVersion()).thenReturn(OFConstants.OFP_VERSION_1_3);
        when(deviceInfo.getDatapathId()).thenReturn(DUMMY_DATAPATH_ID);
        when(mockedDeviceContext.getDeviceInfo()).thenReturn(deviceInfo);

        when(mockedConnectionContext.getFeatures()).thenReturn(mockedFeaturesReply);
        when(mockedDeviceContext.getPrimaryConnectionContext()).thenReturn(mockedConnectionContext);

        MultipartReplyMessage multipartReplyMessage = mock(MultipartReplyMessage.class);
        when(multipartReplyMessage.getType()).thenReturn(multipartType);
        when(multipartReplyMessage.getFlags()).thenReturn(new MultipartRequestFlags(true));
        when(multipartReplyMessage.getXid()).thenReturn(DUMMY_XID);
        when(multipartReplyMessage.getMultipartReplyBody()).thenReturn(multipartReplyBody);
        return multipartReplyMessage;
    }

    private static DataContainer validateOutput(final DataContainer dataObject) {
        return dataObject;
    }
}
