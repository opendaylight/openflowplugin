/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.impl.statistics;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.common.base.Optional;
import com.google.common.collect.Lists;
import com.google.common.util.concurrent.CheckedFuture;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import java.math.BigInteger;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.opendaylight.controller.md.sal.binding.api.ReadOnlyTransaction;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.controller.md.sal.common.api.data.ReadFailedException;
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
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.statistics.rev131111.nodes.node.meter.MeterStatistics;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.FlowModFlags;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.GroupId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.GroupType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.MeterFlags;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.MeterId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.MultipartRequestFlags;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.MultipartType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.PortNumber;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entries.grouping.MatchEntry;
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
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.meter.config._case.multipart.reply.meter.config.meter.config.Bands;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.port.stats._case.MultipartReplyPortStatsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.port.stats._case.multipart.reply.port.stats.PortStatsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.queue._case.MultipartReplyQueueBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.queue._case.multipart.reply.queue.QueueStatsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.table._case.MultipartReplyTableBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.table._case.multipart.reply.table.TableStatsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.port.statistics.rev131214.FlowCapableNodeConnectorStatisticsData;
import org.opendaylight.yang.gen.v1.urn.opendaylight.port.statistics.rev131214.flow.capable.node.connector.statistics.FlowCapableNodeConnectorStatistics;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.binding.KeyedInstanceIdentifier;
import org.opendaylight.yangtools.yang.common.RpcResultBuilder;


@RunWith(MockitoJUnitRunner.class)
public class StatisticsGatheringUtilsTest {

    static final String DUMMY_NODE_ID_VALUE = "1";
    static final NodeId DUMMY_NODE_ID = new NodeId(DUMMY_NODE_ID_VALUE);

    private final KeyedInstanceIdentifier<Node, NodeKey> dummyNodePath = InstanceIdentifier.create(Nodes.class)
            .child(Node.class, new NodeKey(DUMMY_NODE_ID));
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
    private FlowId flowId;
    @Mock
    private GetFeaturesOutput features;
    @Mock
    private ReadOnlyTransaction readTx;
    @Mock
    private ConnectionContext connectionAdapter;
    @Mock
    private StatisticsGatherer<MultipartReply> statisticsService;
    @Mock
    private DeviceInfo deviceInfo;

    private MultipartWriterProvider provider;

    @Before
    public void setUp() throws Exception {
        when(deviceContext.getDeviceState()).thenReturn(deviceState);
        when(deviceContext.getDeviceInfo()).thenReturn(deviceInfo);
        when(deviceContext.getDeviceFlowRegistry()).thenReturn(deviceFlowRegistry);
        when(deviceContext.getDeviceGroupRegistry()).thenReturn(deviceGroupRegistry);
        when(deviceContext.getDeviceMeterRegistry()).thenReturn(deviceMeterRegistry);
        when(deviceFlowRegistry.retrieveDescriptor(Matchers.any(FlowRegistryKey.class))).thenReturn(flowDescriptor);
        when(deviceContext.getReadTransaction()).thenReturn(readTx);
        when(deviceContext.getReadTransaction()).thenReturn(readTx);
        when(deviceContext.getPrimaryConnectionContext()).thenReturn(connectionAdapter);
        when(deviceContext.isTransactionsEnabled()).thenReturn(Boolean.TRUE);
        when(connectionAdapter.getNodeId()).thenReturn(DUMMY_NODE_ID);
        when(connectionAdapter.getFeatures()).thenReturn(features);
        when(features.getDatapathId()).thenReturn(BigInteger.ONE);
        when(features.getVersion()).thenReturn(OFConstants.OFP_VERSION_1_3);
        when(deviceInfo.getVersion()).thenReturn(OFConstants.OFP_VERSION_1_3);
        when(deviceInfo.getDatapathId()).thenReturn(BigInteger.ONE);
        when(deviceInfo.getNodeInstanceIdentifier()).thenReturn(dummyNodePath);
        when(deviceInfo.getNodeId()).thenReturn(DUMMY_NODE_ID);
        provider = MultipartWriterProviderFactory.createDefaultProvider(deviceContext);
    }

    @Test
    public void testWriteFlowStatistics() throws Exception {
        final ArgumentCaptor<LogicalDatastoreType> dataStoreType = ArgumentCaptor.forClass(LogicalDatastoreType.class);
        final ArgumentCaptor<InstanceIdentifier> flowPath = ArgumentCaptor.forClass(InstanceIdentifier.class);
        final ArgumentCaptor<Flow> flow = ArgumentCaptor.forClass(Flow.class);

        provider.lookup(MultipartType.OFPMPFLOW).get().write(prepareFlowStatisticsData().iterator().next(), false);

        Mockito.verify(deviceContext).writeToTransaction(
                dataStoreType.capture(), flowPath.capture(), flow.capture());
        Assert.assertEquals(LogicalDatastoreType.OPERATIONAL, dataStoreType.getValue());
        final InstanceIdentifier<FlowCapableNode> flowCapableNodePath = flowPath.getValue();
        Assert.assertEquals(DUMMY_NODE_ID, flowCapableNodePath.firstKeyOf(Node.class, NodeKey.class).getId());
        Assert.assertEquals(42, flow.getValue().getTableId().intValue());
    }

    private Iterable<FlowsStatisticsUpdate> prepareFlowStatisticsData() {
        final FlowAndStatisticsMapListBuilder flowAndStatsMapListBld = new FlowAndStatisticsMapListBuilder();
        flowAndStatsMapListBld.setTableId((short) 42);
        flowAndStatsMapListBld.setMatch(new MatchBuilder().build());

        final FlowsStatisticsUpdateBuilder flowStatsUpdateBld1 = new FlowsStatisticsUpdateBuilder();
        flowStatsUpdateBld1.setFlowAndStatisticsMapList(Lists.newArrayList(flowAndStatsMapListBld.build()));

        return Lists.newArrayList(flowStatsUpdateBld1.build());
    }


    @Test
    public void testGatherStatistics_group() throws Exception {
        final MultipartType type = MultipartType.OFPMPGROUP;
        final long groupIdValue = 19L;

        final GroupStatsBuilder groupStatsBld = new GroupStatsBuilder()
                .setBucketStats(Lists.newArrayList(createBucketStat(21L, 42L)))
                .setByteCount(BigInteger.valueOf(84L))
                .setPacketCount(BigInteger.valueOf(63L))
                .setDurationSec(11L)
                .setDurationNsec(12L)
                .setRefCount(13L)
                .setGroupId(new GroupId(groupIdValue));
        final MultipartReplyGroupBuilder mpReplyGroupBld = new MultipartReplyGroupBuilder();
        mpReplyGroupBld.setGroupStats(Lists.newArrayList(groupStatsBld.build()));
        final MultipartReplyGroupCaseBuilder mpReplyGroupCaseBld = new MultipartReplyGroupCaseBuilder();
        mpReplyGroupCaseBld.setMultipartReplyGroup(mpReplyGroupBld.build());

        final MultipartReply groupStatsUpdated = assembleMPReplyMessage(type, mpReplyGroupCaseBld.build());
        final List<MultipartReply> statsData = Collections.singletonList(groupStatsUpdated);

        fireAndCheck(type, statsData);

        final InstanceIdentifier<GroupStatistics> groupPath = dummyNodePath.augmentation(FlowCapableNode.class)
                .child(Group.class, new GroupKey(new org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.GroupId(groupIdValue)))
                .augmentation(NodeGroupStatistics.class)
                .child(GroupStatistics.class);
        verify(deviceContext).writeToTransaction(Matchers.eq(LogicalDatastoreType.OPERATIONAL),
                Matchers.eq(groupPath), Matchers.any(GroupStatistics.class));
    }

    @Test
    public void testGatherStatistics_groupDesc() throws Exception {
        final MultipartType type = MultipartType.OFPMPGROUPDESC;
        final long groupIdValue = 27L;

        final BucketsListBuilder bucketsListBld = new BucketsListBuilder()
                .setWatchPort(new PortNumber(5L));
        final GroupDescBuilder groupStatsBld = new GroupDescBuilder()
                .setBucketsList(Lists.newArrayList(bucketsListBld.build()))
                .setGroupId(new GroupId(groupIdValue))
                .setType(GroupType.OFPGTALL);
        final MultipartReplyGroupDescBuilder mpReplyGroupBld = new MultipartReplyGroupDescBuilder();
        mpReplyGroupBld.setGroupDesc(Lists.newArrayList(groupStatsBld.build()));
        final MultipartReplyGroupDescCaseBuilder mpReplyGroupCaseBld = new MultipartReplyGroupDescCaseBuilder();
        mpReplyGroupCaseBld.setMultipartReplyGroupDesc(mpReplyGroupBld.build());

        final MultipartReply groupStatsUpdated = assembleMPReplyMessage(type, mpReplyGroupCaseBld.build());
        final List<MultipartReply> statsData = Collections.singletonList(groupStatsUpdated);

        fireAndCheck(type, statsData);

        final org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.GroupId storedGroupId = new org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.GroupId(groupIdValue);
        final KeyedInstanceIdentifier<Group, GroupKey> groupPath = dummyNodePath.augmentation(FlowCapableNode.class).child(Group.class, new GroupKey(storedGroupId));

        verify(deviceContext, Mockito.never()).addDeleteToTxChain(Matchers.eq(LogicalDatastoreType.OPERATIONAL), Matchers.<InstanceIdentifier<?>> any());
        verify(deviceGroupRegistry).store(storedGroupId);
        verify(deviceContext).writeToTransaction(
                Matchers.eq(LogicalDatastoreType.OPERATIONAL), Matchers.eq(groupPath), Matchers.any(Group.class));
    }

    @Test
    public void testGatherStatistics_meter() throws Exception {
        final MultipartType type = MultipartType.OFPMPMETER;
        final long meterIdValue = 19L;

        final MeterBandStatsBuilder meterBandStatsBld = new MeterBandStatsBuilder()
                .setByteBandCount(BigInteger.valueOf(91L))
                .setPacketBandCount(BigInteger.valueOf(92L));
        final MeterStatsBuilder meterStatsBld = new MeterStatsBuilder()
                .setMeterId(new MeterId(meterIdValue))
                .setByteInCount(BigInteger.valueOf(111L))
                .setDurationSec(112L)
                .setDurationNsec(113L)
                .setFlowCount(114L)
                .setPacketInCount(BigInteger.valueOf(115L))
                .setMeterBandStats(Lists.newArrayList(meterBandStatsBld.build()));
        final MultipartReplyMeterBuilder mpReplyMeterBld = new MultipartReplyMeterBuilder();
        mpReplyMeterBld.setMeterStats(Lists.newArrayList(meterStatsBld.build()));
        final MultipartReplyMeterCaseBuilder mpReplyMeterCaseBld = new MultipartReplyMeterCaseBuilder();
        mpReplyMeterCaseBld.setMultipartReplyMeter(mpReplyMeterBld.build());

        final MultipartReply meterStatsUpdated = assembleMPReplyMessage(type, mpReplyMeterCaseBld.build());
        final List<MultipartReply> statsData = Collections.singletonList(meterStatsUpdated);

        fireAndCheck(type, statsData);

        final InstanceIdentifier<MeterStatistics> meterPath = dummyNodePath.augmentation(FlowCapableNode.class)
                .child(Meter.class, new MeterKey(new org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.MeterId(meterIdValue)))
                .augmentation(NodeMeterStatistics.class)
                .child(MeterStatistics.class);
        verify(deviceContext).writeToTransaction(Matchers.eq(LogicalDatastoreType.OPERATIONAL),
                Matchers.eq(meterPath), Matchers.any(MeterStatistics.class));
    }

    @Test
    public void testGatherStatistics_nodeConnector() throws Exception {
        final MultipartType type = MultipartType.OFPMPPORTSTATS;

        final PortStatsBuilder portStatsBld = new PortStatsBuilder()
                .setPortNo(11L);
        final MultipartReplyPortStatsBuilder mpReplyMeterBld = new MultipartReplyPortStatsBuilder();
        mpReplyMeterBld.setPortStats(Lists.newArrayList(portStatsBld.build()));
        final MultipartReplyPortStatsCaseBuilder mpReplyMeterCaseBld = new MultipartReplyPortStatsCaseBuilder();
        mpReplyMeterCaseBld.setMultipartReplyPortStats(mpReplyMeterBld.build());

        final MultipartReply meterStatsUpdated = assembleMPReplyMessage(type, mpReplyMeterCaseBld.build());
        final List<MultipartReply> statsData = Collections.singletonList(meterStatsUpdated);

        fireAndCheck(type, statsData);

        final InstanceIdentifier<FlowCapableNodeConnectorStatistics> portPath = dummyNodePath
                .child(NodeConnector.class, new NodeConnectorKey(new NodeConnectorId("openflow:" + DUMMY_NODE_ID_VALUE + ":11")))
                .augmentation(FlowCapableNodeConnectorStatisticsData.class)
                .child(FlowCapableNodeConnectorStatistics.class);
        verify(deviceContext).writeToTransaction(
                Matchers.eq(LogicalDatastoreType.OPERATIONAL),
                Matchers.eq(portPath),
                Matchers.any(FlowCapableNodeConnectorStatistics.class));
    }

    @Test
    public void testGatherStatistics_table() throws Exception {
        final MultipartType type = MultipartType.OFPMPTABLE;

        final TableStatsBuilder tableStatsBld = new TableStatsBuilder()
                .setActiveCount(33L)
                .setLookupCount(BigInteger.valueOf(34L))
                .setMatchedCount(BigInteger.valueOf(35L))
                .setTableId((short) 0);
        final MultipartReplyTableBuilder mpReplyTableBld = new MultipartReplyTableBuilder();
        mpReplyTableBld.setTableStats(Lists.newArrayList(tableStatsBld.build()));
        final MultipartReplyTableCaseBuilder mpReplyTableCaseBld = new MultipartReplyTableCaseBuilder();
        mpReplyTableCaseBld.setMultipartReplyTable(mpReplyTableBld.build());

        final MultipartReply meterStatsUpdated = assembleMPReplyMessage(type, mpReplyTableCaseBld.build());
        final List<MultipartReply> statsData = Collections.singletonList(meterStatsUpdated);

        fireAndCheck(type, statsData);

        final InstanceIdentifier<FlowTableStatistics> tablePath = dummyNodePath
                .augmentation(FlowCapableNode.class)
                .child(Table.class, new TableKey((short) 0))
                .augmentation(FlowTableStatisticsData.class)
                .child(FlowTableStatistics.class);
        verify(deviceContext).writeToTransaction(
                Matchers.eq(LogicalDatastoreType.OPERATIONAL),
                Matchers.eq(tablePath),
                Matchers.any(FlowTableStatistics.class));
    }

    @Test
    public void testGatherStatistics_queue() throws Exception {
        final MultipartType type = MultipartType.OFPMPQUEUE;

        final long queueIdValue = 4L;
        final QueueStatsBuilder queueStatsBld = new QueueStatsBuilder()
                .setPortNo(11L)
                .setTxBytes(BigInteger.valueOf(44L))
                .setTxErrors(BigInteger.valueOf(45L))
                .setTxPackets(BigInteger.valueOf(46L))
                .setDurationSec(47L)
                .setDurationNsec(48L)
                .setQueueId(queueIdValue);

        final MultipartReplyQueueBuilder mpReplyQueueBld = new MultipartReplyQueueBuilder();
        mpReplyQueueBld.setQueueStats(Lists.newArrayList(queueStatsBld.build()));
        final MultipartReplyQueueCaseBuilder mpReplyQueueCaseBld = new MultipartReplyQueueCaseBuilder();
        mpReplyQueueCaseBld.setMultipartReplyQueue(mpReplyQueueBld.build());

        final MultipartReply meterStatsUpdated = assembleMPReplyMessage(type, mpReplyQueueCaseBld.build());
        final List<MultipartReply> statsData = Collections.singletonList(meterStatsUpdated);

        fireAndCheck(type, statsData);

        final KeyedInstanceIdentifier<Queue, QueueKey> queuePath = dummyNodePath
                .child(NodeConnector.class, new NodeConnectorKey(new NodeConnectorId("openflow:" + DUMMY_NODE_ID_VALUE + ":11")))
                .augmentation(FlowCapableNodeConnector.class)
                .child(Queue.class, new QueueKey(new QueueId(queueIdValue)));
        verify(deviceContext).writeToTransaction(
                Matchers.eq(LogicalDatastoreType.OPERATIONAL),
                Matchers.eq(queuePath),
                Matchers.any(Queue.class));
    }

    @Test
    public void testGatherStatistics_flow() throws Exception {
        final short tableId = 0;
        final MultipartType type = MultipartType.OFPMPFLOW;

        final InstanceIdentifier<FlowCapableNode> nodePath = deviceInfo.getNodeInstanceIdentifier().augmentation(FlowCapableNode.class);
        final TableBuilder tableDataBld = new TableBuilder();
        tableDataBld.setId(tableId);
        final FlowCapableNodeBuilder flowNodeBuilder = new FlowCapableNodeBuilder();
        flowNodeBuilder.setTable(Collections.singletonList(tableDataBld.build()));
        final Optional<FlowCapableNode> flowNodeOpt = Optional.of(flowNodeBuilder.build());
        final CheckedFuture<Optional<FlowCapableNode>, ReadFailedException> flowNodeFuture = Futures.immediateCheckedFuture(flowNodeOpt);
        when(readTx.read(LogicalDatastoreType.OPERATIONAL, nodePath)).thenReturn(flowNodeFuture);
        when(flowDescriptor.getFlowId()).thenReturn(flowId);

        final org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.grouping.MatchBuilder matchBld =
                new org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.grouping.MatchBuilder()
                        .setMatchEntry(Collections.<MatchEntry>emptyList());
        final FlowStatsBuilder flowStatsBld = new FlowStatsBuilder()
                .setByteCount(BigInteger.valueOf(55L))
                .setPacketCount(BigInteger.valueOf(56L))
                .setDurationSec(57L)
                .setDurationNsec(58L)
                .setTableId((short) 0)
                .setMatch(matchBld.build())
                .setFlags(new FlowModFlags(true, false, false, false, true));

        final MultipartReplyFlowBuilder mpReplyFlowBld = new MultipartReplyFlowBuilder();
        mpReplyFlowBld.setFlowStats(Lists.newArrayList(flowStatsBld.build()));
        final MultipartReplyFlowCaseBuilder mpReplyFlowCaseBld = new MultipartReplyFlowCaseBuilder();
        mpReplyFlowCaseBld.setMultipartReplyFlow(mpReplyFlowBld.build());

        final MultipartReply flowStatsUpdated = assembleMPReplyMessage(type, mpReplyFlowCaseBld.build());
        final List<MultipartReply> statsData = Collections.singletonList(flowStatsUpdated);
        fireAndCheck(type, statsData);

        final FlowBuilder flowBld = new FlowBuilder()
                .setTableId((short) 0)
                .setMatch(new MatchBuilder().build());
        final KeyedInstanceIdentifier<Table, TableKey> tablePath = dummyNodePath.augmentation(FlowCapableNode.class)
                .child(Table.class, new TableKey((short) 0));
        final KeyedInstanceIdentifier<Flow, FlowKey> flowPath =  tablePath.child(Flow.class, new FlowKey(flowId));

        verify(deviceContext, Mockito.never()).addDeleteToTxChain(Matchers.eq(LogicalDatastoreType.OPERATIONAL), Matchers.<InstanceIdentifier<?>>any());

        final InOrder inOrder = Mockito.inOrder(deviceContext);
        inOrder.verify(deviceContext).writeToTransaction(Matchers.eq(LogicalDatastoreType.OPERATIONAL),Matchers.any(), Matchers.any());
    }

    @Test
    public void testGatherStatistics_meterConfig() throws Exception {
        final MultipartType type = MultipartType.OFPMPMETERCONFIG;
        final Long meterIdValue = 55L;

        final MeterConfigBuilder meterConfigBld = new MeterConfigBuilder()
                .setMeterId(new MeterId(meterIdValue))
                .setFlags(new MeterFlags(false, true, false, true))
                .setBands(Collections.<Bands>emptyList());

        final MultipartReplyMeterConfigBuilder mpReplyMeterConfigBld = new MultipartReplyMeterConfigBuilder();
        mpReplyMeterConfigBld.setMeterConfig(Lists.newArrayList(meterConfigBld.build()));
        final MultipartReplyMeterConfigCaseBuilder mpReplyMeterConfigCaseBld = new MultipartReplyMeterConfigCaseBuilder();
        mpReplyMeterConfigCaseBld.setMultipartReplyMeterConfig(mpReplyMeterConfigBld.build());

        final MultipartReply meterConfigUpdated = assembleMPReplyMessage(type, mpReplyMeterConfigCaseBld.build());
        final List<MultipartReply> statsData = Collections.singletonList(meterConfigUpdated);

        fireAndCheck(type, statsData);

        final org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.MeterId meterId =
                new org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.MeterId(meterIdValue);
        final KeyedInstanceIdentifier<Meter, MeterKey> meterPath = dummyNodePath.augmentation(FlowCapableNode.class)
                .child(Meter.class, new MeterKey(meterId));
        verify(deviceContext, Mockito.never()).addDeleteToTxChain(Matchers.eq(LogicalDatastoreType.OPERATIONAL), Matchers.<InstanceIdentifier<?>>any());
        verify(deviceMeterRegistry).store(meterId);
        verify(deviceContext).writeToTransaction(Matchers.eq(LogicalDatastoreType.OPERATIONAL), Matchers.eq(meterPath), Matchers.any(Meter.class));
    }

    private void fireAndCheck(final MultipartType type, final List<MultipartReply> statsData) throws InterruptedException, ExecutionException, TimeoutException {
        when(statisticsService.getStatisticsOfType(Matchers.any(EventIdentifier.class), Matchers.eq(type)))
                .thenReturn(Futures.immediateFuture(RpcResultBuilder.success(statsData).build()));

        final ListenableFuture<Boolean> gatherStatisticsResult = StatisticsGatheringUtils.gatherStatistics(
            statisticsService,
            deviceInfo,
            type,
            deviceContext,
            deviceContext,
            ConvertorManagerFactory.createDefaultManager(),
            provider);

        Assert.assertTrue(gatherStatisticsResult.get(1, TimeUnit.SECONDS));
        verify(deviceContext).submitTransaction();
    }

    private static MultipartReplyMessage assembleMPReplyMessage(final MultipartType type, final MultipartReplyBody mpReplyGroupCaseBld) {
        return new MultipartReplyMessageBuilder()
                .setMultipartReplyBody(mpReplyGroupCaseBld)
                .setType(type)
                .setFlags(new MultipartRequestFlags(false))
                .setXid(42L)
                .build();
    }

    private static BucketStats createBucketStat(final long byteCount, final long packetCount) {
        return new BucketStatsBuilder().setByteCount(BigInteger.valueOf(byteCount)).setPacketCount(BigInteger.valueOf(packetCount)).build();
    }

    @Test
    public void testDeleteAllKnownFlows() throws Exception {
        final short tableId = 0;
        final InstanceIdentifier<FlowCapableNode> nodePath = deviceInfo.getNodeInstanceIdentifier().augmentation(FlowCapableNode.class);
        final TableBuilder tableDataBld = new TableBuilder();
        tableDataBld.setId(tableId);
        final FlowCapableNodeBuilder flowNodeBuilder = new FlowCapableNodeBuilder();
        flowNodeBuilder.setTable(Collections.singletonList(tableDataBld.build()));
        final Optional<FlowCapableNode> flowNodeOpt = Optional.of(flowNodeBuilder.build());
        final CheckedFuture<Optional<FlowCapableNode>, ReadFailedException> flowNodeFuture = Futures.immediateCheckedFuture(flowNodeOpt);
        when(readTx.read(LogicalDatastoreType.OPERATIONAL, nodePath)).thenReturn(flowNodeFuture);
        StatisticsGatheringUtils.deleteAllKnownFlows(deviceContext, deviceInfo.getNodeInstanceIdentifier()
            .augmentation(FlowCapableNode.class), deviceFlowRegistry);

        verify(deviceContext).isTransactionsEnabled();
        verify(deviceContext).getReadTransaction();
        verify(deviceContext).writeToTransaction(Mockito.eq(LogicalDatastoreType.OPERATIONAL), Mockito.any(), Mockito.any());
    }
}
