/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.statistics;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.MoreExecutors;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.opendaylight.mdsal.binding.api.ReadTransaction;
import org.opendaylight.mdsal.common.api.LogicalDatastoreType;
import org.opendaylight.openflowplugin.api.OFConstants;
import org.opendaylight.openflowplugin.api.openflow.connection.ConnectionContext;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceContext;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceInfo;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceState;
import org.opendaylight.openflowplugin.api.openflow.registry.flow.DeviceFlowRegistry;
import org.opendaylight.openflowplugin.api.openflow.registry.flow.FlowDescriptor;
import org.opendaylight.openflowplugin.api.openflow.registry.flow.FlowRegistryKey;
import org.opendaylight.openflowplugin.api.openflow.registry.group.DeviceGroupRegistry;
import org.opendaylight.openflowplugin.api.openflow.registry.meter.DeviceMeterRegistry;
import org.opendaylight.openflowplugin.api.openflow.statistics.ofpspecific.EventIdentifier;
import org.opendaylight.openflowplugin.api.openflow.statistics.ofpspecific.StatisticsGatherer;
import org.opendaylight.openflowplugin.impl.datastore.MultipartWriterProvider;
import org.opendaylight.openflowplugin.impl.datastore.MultipartWriterProviderFactory;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.ConvertorManagerFactory;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNode;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNodeBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNodeConnector;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.meters.Meter;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.meters.MeterKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.Table;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.TableBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.TableKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.table.Flow;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.table.FlowBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.table.FlowKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.statistics.rev130819.FlowsStatisticsUpdate;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.statistics.rev130819.FlowsStatisticsUpdateBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.statistics.rev130819.flow.and.statistics.map.list.FlowAndStatisticsMapListBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.table.statistics.rev131215.FlowTableStatisticsData;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.table.statistics.rev131215.flow.table.statistics.FlowTableStatistics;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.port.rev130925.queues.Queue;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.port.rev130925.queues.QueueKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.queue.rev130925.QueueId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.MatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.statistics.rev131111.NodeGroupStatistics;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.statistics.rev131111.group.statistics.GroupStatistics;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.groups.Group;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.groups.GroupKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeConnectorId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.Nodes;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.node.NodeConnector;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.node.NodeConnectorKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.NodeKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.statistics.rev131111.NodeMeterStatistics;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.statistics.rev131111.node.meter.statistics.MeterStatistics;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.FlowModFlags;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.GroupId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.GroupType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.MeterFlags;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.MeterId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.MultipartRequestFlags;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.MultipartType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.PortNumber;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.GetFeaturesOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.MultipartReply;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.MultipartReplyMessage;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.MultipartReplyMessageBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.buckets.grouping.BucketsListBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.MultipartReplyBody;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.MultipartReplyFlowCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.MultipartReplyGroupCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.MultipartReplyGroupDescCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.MultipartReplyMeterCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.MultipartReplyMeterConfigCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.MultipartReplyPortStatsCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.MultipartReplyQueueCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.MultipartReplyTableCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.flow._case.MultipartReplyFlowBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.flow._case.multipart.reply.flow.FlowStatsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.group._case.MultipartReplyGroupBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.group._case.multipart.reply.group.GroupStatsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.group._case.multipart.reply.group.group.stats.BucketStats;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.group._case.multipart.reply.group.group.stats.BucketStatsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.group.desc._case.MultipartReplyGroupDescBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.group.desc._case.multipart.reply.group.desc.GroupDescBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.meter._case.MultipartReplyMeterBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.meter._case.multipart.reply.meter.MeterStatsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.meter._case.multipart.reply.meter.meter.stats.MeterBandStatsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.meter.config._case.MultipartReplyMeterConfigBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.meter.config._case.multipart.reply.meter.config.MeterConfigBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.port.stats._case.MultipartReplyPortStatsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.port.stats._case.multipart.reply.port.stats.PortStatsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.queue._case.MultipartReplyQueueBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.queue._case.multipart.reply.queue.QueueStatsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.table._case.MultipartReplyTableBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.table._case.multipart.reply.table.TableStatsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.port.statistics.rev131214.FlowCapableNodeConnectorStatisticsData;
import org.opendaylight.yang.gen.v1.urn.opendaylight.port.statistics.rev131214.flow.capable.node.connector.statistics.FlowCapableNodeConnectorStatistics;
import org.opendaylight.yangtools.binding.DataObjectIdentifier;
import org.opendaylight.yangtools.binding.util.BindingMap;
import org.opendaylight.yangtools.util.concurrent.FluentFutures;
import org.opendaylight.yangtools.yang.common.RpcResultBuilder;
import org.opendaylight.yangtools.yang.common.Uint32;
import org.opendaylight.yangtools.yang.common.Uint64;
import org.opendaylight.yangtools.yang.common.Uint8;

@RunWith(MockitoJUnitRunner.class)
public class StatisticsGatheringUtilsTest {

    static final String DUMMY_NODE_ID_VALUE = "1";
    static final NodeId DUMMY_NODE_ID = new NodeId(DUMMY_NODE_ID_VALUE);

    private final DataObjectIdentifier.WithKey<Node, NodeKey> dummyNodePath = DataObjectIdentifier.builder(Nodes.class)
            .child(Node.class, new NodeKey(DUMMY_NODE_ID))
            .build();
    @Mock
    private DeviceContext deviceContext;
    @Mock
    private DeviceState deviceState;
    @Mock
    private DeviceFlowRegistry deviceFlowRegistry;
    @Mock
    private DeviceGroupRegistry deviceGroupRegistry;
    @Mock
    private DeviceMeterRegistry deviceMeterRegistry;
    @Mock
    private FlowDescriptor flowDescriptor;
    @Mock
    private FlowRegistryKey flowRegistryKey;
    @Mock
    private FlowId flowId;
    @Mock
    private GetFeaturesOutput features;
    @Mock
    private ReadTransaction readTx;
    @Mock
    private ConnectionContext connectionAdapter;
    @Mock
    private StatisticsGatherer<MultipartReply> statisticsService;
    @Mock
    private DeviceInfo deviceInfo;

    private MultipartWriterProvider provider;

    @Before
    public void setUp() {
        when(deviceContext.getDeviceInfo()).thenReturn(deviceInfo);
        when(deviceContext.getDeviceFlowRegistry()).thenReturn(deviceFlowRegistry);
        when(deviceContext.getDeviceGroupRegistry()).thenReturn(deviceGroupRegistry);
        when(deviceContext.getDeviceMeterRegistry()).thenReturn(deviceMeterRegistry);
        when(deviceFlowRegistry.createKey(any())).thenReturn(flowRegistryKey);
        when(deviceFlowRegistry.retrieveDescriptor(any())).thenReturn(flowDescriptor);
        when(flowDescriptor.getFlowId()).thenReturn(new FlowId("MOCK_FLOW"));
        when(deviceContext.getReadTransaction()).thenReturn(readTx);
        when(deviceContext.getReadTransaction()).thenReturn(readTx);
        when(deviceContext.getPrimaryConnectionContext()).thenReturn(connectionAdapter);
        when(deviceContext.isTransactionsEnabled()).thenReturn(Boolean.TRUE);
        when(connectionAdapter.getFeatures()).thenReturn(features);
        when(features.getDatapathId()).thenReturn(Uint64.ONE);
        when(features.getVersion()).thenReturn(OFConstants.OFP_VERSION_1_3);
        when(deviceInfo.getVersion()).thenReturn(OFConstants.OFP_VERSION_1_3);
        when(deviceInfo.getDatapathId()).thenReturn(Uint64.ONE);
        when(deviceInfo.getNodeInstanceIdentifier()).thenReturn(dummyNodePath);
        when(deviceInfo.getNodeId()).thenReturn(DUMMY_NODE_ID);
        provider = MultipartWriterProviderFactory.createDefaultProvider(deviceContext);
    }

    @Test
    public void testWriteFlowStatistics() {
        final ArgumentCaptor<LogicalDatastoreType> dataStoreType = ArgumentCaptor.forClass(LogicalDatastoreType.class);
        final ArgumentCaptor<DataObjectIdentifier> flowPath = ArgumentCaptor.forClass(DataObjectIdentifier.class);
        final ArgumentCaptor<Flow> flow = ArgumentCaptor.forClass(Flow.class);

        provider.lookup(MultipartType.OFPMPFLOW).orElseThrow().write(prepareFlowStatisticsData().iterator().next(),
            false);

        Mockito.verify(deviceContext).writeToTransaction(
                dataStoreType.capture(), flowPath.capture(), flow.capture());
        Assert.assertEquals(LogicalDatastoreType.OPERATIONAL, dataStoreType.getValue());
        final DataObjectIdentifier<FlowCapableNode> flowCapableNodePath = flowPath.getValue();
        Assert.assertEquals(DUMMY_NODE_ID, flowCapableNodePath.toLegacy().firstKeyOf(Node.class).getId());
        Assert.assertEquals(42, flow.getValue().getTableId().intValue());
    }

    private static List<FlowsStatisticsUpdate> prepareFlowStatisticsData() {
        final FlowAndStatisticsMapListBuilder flowAndStatsMapListBld = new FlowAndStatisticsMapListBuilder();
        flowAndStatsMapListBld.setTableId(Uint8.valueOf(42));
        flowAndStatsMapListBld.setMatch(new MatchBuilder().build());

        final FlowsStatisticsUpdateBuilder flowStatsUpdateBld1 = new FlowsStatisticsUpdateBuilder();
        flowStatsUpdateBld1.setFlowAndStatisticsMapList(List.of(flowAndStatsMapListBld.build()));

        return List.of(flowStatsUpdateBld1.build());
    }

    @Test
    public void testGatherStatistics_group() throws Exception {
        final MultipartType type = MultipartType.OFPMPGROUP;
        final Uint32 groupIdValue = Uint32.valueOf(19);

        final GroupStatsBuilder groupStatsBld = new GroupStatsBuilder()
                .setBucketStats(List.of(createBucketStat(21L, 42L)))
                .setByteCount(Uint64.valueOf(84))
                .setPacketCount(Uint64.valueOf(63))
                .setDurationSec(Uint32.valueOf(11))
                .setDurationNsec(Uint32.valueOf(12))
                .setRefCount(Uint32.valueOf(13))
                .setGroupId(new GroupId(groupIdValue));
        final MultipartReplyGroupBuilder mpReplyGroupBld = new MultipartReplyGroupBuilder();
        mpReplyGroupBld.setGroupStats(List.of(groupStatsBld.build()));
        final MultipartReplyGroupCaseBuilder mpReplyGroupCaseBld = new MultipartReplyGroupCaseBuilder();
        mpReplyGroupCaseBld.setMultipartReplyGroup(mpReplyGroupBld.build());

        final MultipartReply groupStatsUpdated = assembleMPReplyMessage(type, mpReplyGroupCaseBld.build());
        final List<MultipartReply> statsData = List.of(groupStatsUpdated);

        fireAndCheck(type, statsData);

        final var groupPath = dummyNodePath.toBuilder()
                .augmentation(FlowCapableNode.class)
                .child(Group.class, new GroupKey(new org.opendaylight.yang.gen.v1.urn
                        .opendaylight.group.types.rev131018.GroupId(groupIdValue)))
                .augmentation(NodeGroupStatistics.class)
                .child(GroupStatistics.class)
                .build();
        verify(deviceContext).writeToTransaction(eq(LogicalDatastoreType.OPERATIONAL),
                eq(groupPath), any(GroupStatistics.class));
    }

    @Test
    public void testGatherStatistics_groupDesc() throws Exception {
        final MultipartType type = MultipartType.OFPMPGROUPDESC;
        final Uint32 groupIdValue = Uint32.valueOf(27);

        final BucketsListBuilder bucketsListBld = new BucketsListBuilder()
                .setWatchPort(new PortNumber(Uint32.valueOf(5)));
        final GroupDescBuilder groupStatsBld = new GroupDescBuilder()
                .setBucketsList(List.of(bucketsListBld.build()))
                .setGroupId(new GroupId(groupIdValue))
                .setType(GroupType.OFPGTALL);
        final MultipartReplyGroupDescBuilder mpReplyGroupBld = new MultipartReplyGroupDescBuilder();
        mpReplyGroupBld.setGroupDesc(List.of(groupStatsBld.build()));
        final MultipartReplyGroupDescCaseBuilder mpReplyGroupCaseBld = new MultipartReplyGroupDescCaseBuilder();
        mpReplyGroupCaseBld.setMultipartReplyGroupDesc(mpReplyGroupBld.build());

        final MultipartReply groupStatsUpdated = assembleMPReplyMessage(type, mpReplyGroupCaseBld.build());
        final List<MultipartReply> statsData = List.of(groupStatsUpdated);

        fireAndCheck(type, statsData);

        final org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.GroupId storedGroupId =
                new org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.GroupId(groupIdValue);
        final var groupPath = dummyNodePath.toBuilder()
            .augmentation(FlowCapableNode.class)
            .child(Group.class, new GroupKey(storedGroupId))
            .build();

        verify(deviceContext, Mockito.never()).addDeleteToTxChain(eq(LogicalDatastoreType.OPERATIONAL),
                ArgumentMatchers.any());
        verify(deviceGroupRegistry).store(storedGroupId);
        verify(deviceContext).writeToTransaction(eq(LogicalDatastoreType.OPERATIONAL),
                                                 eq(groupPath), any(Group.class));
    }

    @Test
    public void testGatherStatistics_meter() throws Exception {
        final MultipartType type = MultipartType.OFPMPMETER;
        final Uint32 meterIdValue = Uint32.valueOf(19);

        final MeterBandStatsBuilder meterBandStatsBld = new MeterBandStatsBuilder()
                .setByteBandCount(Uint64.valueOf(91))
                .setPacketBandCount(Uint64.valueOf(92));
        final MeterStatsBuilder meterStatsBld = new MeterStatsBuilder()
                .setMeterId(new MeterId(meterIdValue))
                .setByteInCount(Uint64.valueOf(111))
                .setDurationSec(Uint32.valueOf(112))
                .setDurationNsec(Uint32.valueOf(113))
                .setFlowCount(Uint32.valueOf(114))
                .setPacketInCount(Uint64.valueOf(115))
                .setMeterBandStats(List.of(meterBandStatsBld.build()));
        final MultipartReplyMeterBuilder mpReplyMeterBld = new MultipartReplyMeterBuilder();
        mpReplyMeterBld.setMeterStats(List.of(meterStatsBld.build()));
        final MultipartReplyMeterCaseBuilder mpReplyMeterCaseBld = new MultipartReplyMeterCaseBuilder();
        mpReplyMeterCaseBld.setMultipartReplyMeter(mpReplyMeterBld.build());

        final MultipartReply meterStatsUpdated = assembleMPReplyMessage(type, mpReplyMeterCaseBld.build());
        final List<MultipartReply> statsData = List.of(meterStatsUpdated);

        fireAndCheck(type, statsData);

        final var meterPath = dummyNodePath.toBuilder()
                .augmentation(FlowCapableNode.class)
                .child(Meter.class, new MeterKey(new org.opendaylight.yang.gen.v1.urn
                        .opendaylight.meter.types.rev130918.MeterId(meterIdValue)))
                .augmentation(NodeMeterStatistics.class)
                .child(MeterStatistics.class)
                .build();
        verify(deviceContext).writeToTransaction(eq(LogicalDatastoreType.OPERATIONAL),
                eq(meterPath), any(MeterStatistics.class));
    }

    @Test
    public void testGatherStatistics_nodeConnector() throws Exception {
        final MultipartType type = MultipartType.OFPMPPORTSTATS;

        final PortStatsBuilder portStatsBld = new PortStatsBuilder()
                .setPortNo(Uint32.valueOf(11));
        final MultipartReplyPortStatsBuilder mpReplyMeterBld = new MultipartReplyPortStatsBuilder();
        mpReplyMeterBld.setPortStats(List.of(portStatsBld.build()));
        final MultipartReplyPortStatsCaseBuilder mpReplyMeterCaseBld = new MultipartReplyPortStatsCaseBuilder();
        mpReplyMeterCaseBld.setMultipartReplyPortStats(mpReplyMeterBld.build());

        final MultipartReply meterStatsUpdated = assembleMPReplyMessage(type, mpReplyMeterCaseBld.build());
        final List<MultipartReply> statsData = List.of(meterStatsUpdated);

        fireAndCheck(type, statsData);

        final var portPath = dummyNodePath.toBuilder()
                .child(NodeConnector.class,
                        new NodeConnectorKey(new NodeConnectorId("openflow:" + DUMMY_NODE_ID_VALUE + ":11")))
                .augmentation(FlowCapableNodeConnectorStatisticsData.class)
                .child(FlowCapableNodeConnectorStatistics.class)
                .build();
        verify(deviceContext).writeToTransaction(
                eq(LogicalDatastoreType.OPERATIONAL),
                eq(portPath),
                any(FlowCapableNodeConnectorStatistics.class));
    }

    @Test
    public void testGatherStatistics_table() throws Exception {
        final MultipartType type = MultipartType.OFPMPTABLE;

        final TableStatsBuilder tableStatsBld = new TableStatsBuilder()
                .setActiveCount(Uint32.valueOf(33))
                .setLookupCount(Uint64.valueOf(34))
                .setMatchedCount(Uint64.valueOf(35))
                .setTableId(Uint8.ZERO);
        final MultipartReplyTableBuilder mpReplyTableBld = new MultipartReplyTableBuilder();
        mpReplyTableBld.setTableStats(List.of(tableStatsBld.build()));
        final MultipartReplyTableCaseBuilder mpReplyTableCaseBld = new MultipartReplyTableCaseBuilder();
        mpReplyTableCaseBld.setMultipartReplyTable(mpReplyTableBld.build());

        final MultipartReply meterStatsUpdated = assembleMPReplyMessage(type, mpReplyTableCaseBld.build());
        final List<MultipartReply> statsData = List.of(meterStatsUpdated);

        fireAndCheck(type, statsData);

        final var tablePath = dummyNodePath.toBuilder()
                .augmentation(FlowCapableNode.class)
                .child(Table.class, new TableKey(Uint8.ZERO))
                .augmentation(FlowTableStatisticsData.class)
                .child(FlowTableStatistics.class)
                .build();
        verify(deviceContext).writeToTransaction(
                eq(LogicalDatastoreType.OPERATIONAL),
                eq(tablePath),
                any(FlowTableStatistics.class));
    }

    @Test
    public void testGatherStatistics_queue() throws Exception {
        final MultipartType type = MultipartType.OFPMPQUEUE;

        final Uint32 queueIdValue = Uint32.valueOf(4);
        final QueueStatsBuilder queueStatsBld = new QueueStatsBuilder()
                .setPortNo(Uint32.valueOf(11))
                .setTxBytes(Uint64.valueOf(44))
                .setTxErrors(Uint64.valueOf(45))
                .setTxPackets(Uint64.valueOf(46))
                .setDurationSec(Uint32.valueOf(47))
                .setDurationNsec(Uint32.valueOf(48))
                .setQueueId(queueIdValue);

        final MultipartReplyQueueBuilder mpReplyQueueBld = new MultipartReplyQueueBuilder();
        mpReplyQueueBld.setQueueStats(List.of(queueStatsBld.build()));
        final MultipartReplyQueueCaseBuilder mpReplyQueueCaseBld = new MultipartReplyQueueCaseBuilder();
        mpReplyQueueCaseBld.setMultipartReplyQueue(mpReplyQueueBld.build());

        final MultipartReply meterStatsUpdated = assembleMPReplyMessage(type, mpReplyQueueCaseBld.build());
        final List<MultipartReply> statsData = List.of(meterStatsUpdated);

        fireAndCheck(type, statsData);

        final var queuePath = dummyNodePath.toBuilder()
                .child(NodeConnector.class,
                        new NodeConnectorKey(new NodeConnectorId("openflow:" + DUMMY_NODE_ID_VALUE + ":11")))
                .augmentation(FlowCapableNodeConnector.class)
                .child(Queue.class, new QueueKey(new QueueId(queueIdValue)))
                .build();
        verify(deviceContext).writeToTransaction(
                eq(LogicalDatastoreType.OPERATIONAL),
                eq(queuePath),
                any(Queue.class));
    }

    @Test
    public void testGatherStatistics_flow() throws Exception {
        final MultipartType type = MultipartType.OFPMPFLOW;

        final var nodePath = deviceInfo.getNodeInstanceIdentifier().toBuilder()
            .augmentation(FlowCapableNode.class)
            .build();
        final TableBuilder tableDataBld = new TableBuilder();
        tableDataBld.setId(Uint8.ZERO);
        final FlowCapableNodeBuilder flowNodeBuilder = new FlowCapableNodeBuilder();
        flowNodeBuilder.setTable(BindingMap.of(tableDataBld.build()));
        final Optional<FlowCapableNode> flowNodeOpt = Optional.of(flowNodeBuilder.build());
        doReturn(FluentFutures.immediateFluentFuture(flowNodeOpt)).when(readTx)
            .read(LogicalDatastoreType.OPERATIONAL, nodePath);
        when(flowDescriptor.getFlowId()).thenReturn(flowId);

        final org.opendaylight.yang.gen.v1.urn
                .opendaylight.openflow.oxm.rev150225.match.grouping.MatchBuilder matchBld =
                new org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.grouping.MatchBuilder()
                        .setMatchEntry(List.of());
        final FlowStatsBuilder flowStatsBld = new FlowStatsBuilder()
                .setByteCount(Uint64.valueOf(55))
                .setPacketCount(Uint64.valueOf(56))
                .setDurationSec(Uint32.valueOf(57))
                .setDurationNsec(Uint32.valueOf(58L))
                .setTableId(Uint8.ZERO)
                .setMatch(matchBld.build())
                .setFlags(new FlowModFlags(true, false, false, false, true));

        final MultipartReplyFlowBuilder mpReplyFlowBld = new MultipartReplyFlowBuilder();
        mpReplyFlowBld.setFlowStats(List.of(flowStatsBld.build()));
        final MultipartReplyFlowCaseBuilder mpReplyFlowCaseBld = new MultipartReplyFlowCaseBuilder();
        mpReplyFlowCaseBld.setMultipartReplyFlow(mpReplyFlowBld.build());

        final MultipartReply flowStatsUpdated = assembleMPReplyMessage(type, mpReplyFlowCaseBld.build());
        final List<MultipartReply> statsData = List.of(flowStatsUpdated);
        fireAndCheck(type, statsData);

        final FlowBuilder flowBld = new FlowBuilder()
                .setTableId(Uint8.ZERO)
                .setMatch(new MatchBuilder().build());
        final var tablePath = dummyNodePath.toBuilder()
                .augmentation(FlowCapableNode.class)
                .child(Table.class, new TableKey(Uint8.ZERO))
                .build();
        final var flowPath =  tablePath.toBuilder().child(Flow.class, new FlowKey(flowId)).build();

        verify(deviceContext, Mockito.never()).addDeleteToTxChain(eq(LogicalDatastoreType.OPERATIONAL),
                ArgumentMatchers.any());

        final InOrder inOrder = Mockito.inOrder(deviceContext);
        inOrder.verify(deviceContext).writeToTransaction(eq(LogicalDatastoreType.OPERATIONAL),
                                                         any(),
                                                         any());
    }

    @Test
    public void testGatherStatistics_meterConfig() throws Exception {
        final MultipartType type = MultipartType.OFPMPMETERCONFIG;
        final Uint32 meterIdValue = Uint32.valueOf(55);

        final MeterConfigBuilder meterConfigBld = new MeterConfigBuilder()
                .setMeterId(new MeterId(meterIdValue))
                .setFlags(new MeterFlags(false, true, false, true))
                .setBands(List.of());

        final MultipartReplyMeterConfigBuilder mpReplyMeterConfigBld = new MultipartReplyMeterConfigBuilder();
        mpReplyMeterConfigBld.setMeterConfig(List.of(meterConfigBld.build()));
        final MultipartReplyMeterConfigCaseBuilder mpReplyMeterConfigCaseBld =
                new MultipartReplyMeterConfigCaseBuilder();
        mpReplyMeterConfigCaseBld.setMultipartReplyMeterConfig(mpReplyMeterConfigBld.build());

        final MultipartReply meterConfigUpdated = assembleMPReplyMessage(type, mpReplyMeterConfigCaseBld.build());
        final List<MultipartReply> statsData = List.of(meterConfigUpdated);

        fireAndCheck(type, statsData);

        final org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.MeterId meterId =
                new org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.MeterId(meterIdValue);
        final var meterPath = dummyNodePath.toBuilder()
                .augmentation(FlowCapableNode.class)
                .child(Meter.class, new MeterKey(meterId))
                .build();
        verify(deviceContext, Mockito.never()).addDeleteToTxChain(eq(LogicalDatastoreType.OPERATIONAL),
                ArgumentMatchers.any());
        verify(deviceMeterRegistry).store(meterId);
        verify(deviceContext).writeToTransaction(eq(LogicalDatastoreType.OPERATIONAL),
                                                 eq(meterPath), any(Meter.class));
    }

    private void fireAndCheck(final MultipartType type, final List<MultipartReply> statsData)
            throws InterruptedException, ExecutionException, TimeoutException {
        when(statisticsService.getStatisticsOfType(any(EventIdentifier.class),eq(type)))
                .thenReturn(Futures.immediateFuture(RpcResultBuilder.success(statsData).build()));

        final ListenableFuture<Boolean> gatherStatisticsResult = StatisticsGatheringUtils.gatherStatistics(
            statisticsService,
            deviceInfo,
            type,
            deviceContext,
            deviceContext,
            ConvertorManagerFactory.createDefaultManager(),
            provider,
            MoreExecutors.directExecutor());

        Assert.assertTrue(gatherStatisticsResult.get(1, TimeUnit.SECONDS));
        verify(deviceContext).submitTransaction();
    }

    private static MultipartReplyMessage assembleMPReplyMessage(final MultipartType type,
                                                                final MultipartReplyBody mpReplyGroupCaseBld) {
        return new MultipartReplyMessageBuilder()
                .setMultipartReplyBody(mpReplyGroupCaseBld)
                .setType(type)
                .setFlags(new MultipartRequestFlags(false))
                .setXid(Uint32.valueOf(42))
                .build();
    }

    private static BucketStats createBucketStat(final long byteCount, final long packetCount) {
        return new BucketStatsBuilder()
                .setByteCount(Uint64.valueOf(byteCount)).setPacketCount(Uint64.valueOf(packetCount)).build();
    }

    @Test
    public void testDeleteAllKnownFlows() {
        final var nodePath = deviceInfo.getNodeInstanceIdentifier().toBuilder()
            .augmentation(FlowCapableNode.class)
            .build();
        final TableBuilder tableDataBld = new TableBuilder();
        tableDataBld.setId(Uint8.ZERO);
        final FlowCapableNodeBuilder flowNodeBuilder = new FlowCapableNodeBuilder();
        flowNodeBuilder.setTable(BindingMap.of(tableDataBld.build()));
        final Optional<FlowCapableNode> flowNodeOpt = Optional.of(flowNodeBuilder.build());
        doReturn(FluentFutures.immediateFluentFuture(flowNodeOpt)).when(readTx)
            .read(LogicalDatastoreType.OPERATIONAL, nodePath);
        StatisticsGatheringUtils.deleteAllKnownFlows(deviceContext, deviceInfo.getNodeInstanceIdentifier()
            .toBuilder()
            .augmentation(FlowCapableNode.class)
            .build(), deviceFlowRegistry);

        verify(deviceContext).isTransactionsEnabled();
        verify(deviceContext).getReadTransaction();
        verify(deviceContext).writeToTransaction(eq(LogicalDatastoreType.OPERATIONAL), any(), any());
    }
}
