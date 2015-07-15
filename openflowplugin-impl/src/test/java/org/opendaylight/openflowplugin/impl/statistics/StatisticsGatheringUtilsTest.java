/*
 *
 *  * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *  *
 *  * This program and the accompanying materials are made available under the
 *  * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 *  * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 *
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
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.opendaylight.controller.md.sal.binding.api.ReadTransaction;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.controller.md.sal.common.api.data.ReadFailedException;
import org.opendaylight.openflowplugin.api.OFConstants;
import org.opendaylight.openflowplugin.api.openflow.connection.ConnectionContext;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceContext;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceState;
import org.opendaylight.openflowplugin.api.openflow.registry.flow.DeviceFlowRegistry;
import org.opendaylight.openflowplugin.api.openflow.registry.group.DeviceGroupRegistry;
import org.opendaylight.openflowplugin.api.openflow.statistics.ofpspecific.EventIdentifier;
import org.opendaylight.openflowplugin.api.openflow.statistics.ofpspecific.StatisticsGatherer;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNode;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.Table;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.TableBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.TableKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.table.Flow;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.statistics.rev130819.FlowsStatisticsUpdate;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.statistics.rev130819.FlowsStatisticsUpdateBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.statistics.rev130819.flow.and.statistics.map.list.FlowAndStatisticsMapListBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.MatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.groups.Group;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.groups.GroupKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.Nodes;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.NodeKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.GroupId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.GroupType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.MultipartRequestFlags;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.MultipartType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.PortNumber;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.GetFeaturesOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.MultipartReply;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.MultipartReplyMessage;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.MultipartReplyMessageBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.buckets.grouping.BucketsListBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.MultipartReplyBody;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.MultipartReplyGroupCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.MultipartReplyGroupDescCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.group._case.MultipartReplyGroupBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.group._case.multipart.reply.group.GroupStatsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.group._case.multipart.reply.group.group.stats.BucketStats;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.group._case.multipart.reply.group.group.stats.BucketStatsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.group.desc._case.MultipartReplyGroupDescBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.group.desc._case.multipart.reply.group.desc.GroupDescBuilder;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.binding.KeyedInstanceIdentifier;
import org.opendaylight.yangtools.yang.common.RpcResultBuilder;


@RunWith(MockitoJUnitRunner.class)
public class StatisticsGatheringUtilsTest {

    public static final String DUMMY_NODE_ID_VALUE = "dummyNodeId";
    public static final NodeId DUMMY_NODE_ID = new NodeId(DUMMY_NODE_ID_VALUE);

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
    private GetFeaturesOutput features;
    @Mock
    private ReadTransaction readTx;
    @Mock
    private ConnectionContext connectionAdapter;
    @Mock
    private StatisticsGatherer statisticsService;

    @Before
    public void setUp() throws Exception {
        when(deviceContext.getDeviceState()).thenReturn(deviceState);
        when(deviceContext.getDeviceState()).thenReturn(deviceState);
        when(deviceContext.getDeviceFlowRegistry()).thenReturn(deviceFlowRegistry);
        when(deviceContext.getDeviceGroupRegistry()).thenReturn(deviceGroupRegistry);
        when(deviceContext.getReadTransaction()).thenReturn(readTx);
        when(deviceContext.getPrimaryConnectionContext()).thenReturn(connectionAdapter);
        when(connectionAdapter.getNodeId()).thenReturn(DUMMY_NODE_ID);
        when(deviceState.getFeatures()).thenReturn(features);
        when(connectionAdapter.getFeatures()).thenReturn(features);

        when(features.getVersion()).thenReturn(OFConstants.OFP_VERSION_1_3);
        when(features.getDatapathId()).thenReturn(BigInteger.ONE);

        when(deviceState.getNodeInstanceIdentifier()).thenReturn(dummyNodePath);
    }

    @After
    public void tearDown() throws Exception {

    }

    @Test
    public void testWriteFlowStatistics() {
        ArgumentCaptor<LogicalDatastoreType> dataStoreType = ArgumentCaptor.forClass(LogicalDatastoreType.class);
        ArgumentCaptor<InstanceIdentifier> flowPath = ArgumentCaptor.forClass(InstanceIdentifier.class);
        ArgumentCaptor<Flow> flow = ArgumentCaptor.forClass(Flow.class);

        StatisticsGatheringUtils.writeFlowStatistics(prepareFlowStatisticsData(), deviceContext);

        Mockito.verify(deviceContext).writeToTransaction(
                dataStoreType.capture(), flowPath.capture(), flow.capture());
        Assert.assertEquals(LogicalDatastoreType.OPERATIONAL, dataStoreType.getValue());
        InstanceIdentifier<FlowCapableNode> flowCapableNodePath = flowPath.getValue();
        Assert.assertEquals(DUMMY_NODE_ID, flowCapableNodePath.firstKeyOf(Node.class, NodeKey.class).getId());
        Assert.assertEquals(42, flow.getValue().getTableId().intValue());
    }

    private Iterable<FlowsStatisticsUpdate> prepareFlowStatisticsData() {
        FlowAndStatisticsMapListBuilder flowAndStatsMapListBld = new FlowAndStatisticsMapListBuilder();
        flowAndStatsMapListBld.setTableId((short) 42);
        flowAndStatsMapListBld.setMatch(new MatchBuilder().build());

        FlowsStatisticsUpdateBuilder flowStatsUpdateBld1 = new FlowsStatisticsUpdateBuilder();
        flowStatsUpdateBld1.setFlowAndStatisticsMapList(Lists.newArrayList(flowAndStatsMapListBld.build()));

        return Lists.newArrayList(flowStatsUpdateBld1.build());
    }


    @Test
    public void testGatherStatistics_group() throws Exception {
        MultipartType type = MultipartType.OFPMPGROUP;

        GroupStatsBuilder groupStatsBld = new GroupStatsBuilder()
                .setBucketStats(Lists.newArrayList(createBucketStat(21L, 42L)))
                .setByteCount(BigInteger.valueOf(84L))
                .setPacketCount(BigInteger.valueOf(63L))
                .setDurationSec(11L)
                .setDurationNsec(12L)
                .setRefCount(13L)
                .setGroupId(new GroupId(19L));
        MultipartReplyGroupBuilder mpReplyGroupBld = new MultipartReplyGroupBuilder();
        mpReplyGroupBld.setGroupStats(Lists.newArrayList(groupStatsBld.build()));
        MultipartReplyGroupCaseBuilder mpReplyGroupCaseBld = new MultipartReplyGroupCaseBuilder();
        mpReplyGroupCaseBld.setMultipartReplyGroup(mpReplyGroupBld.build());

        MultipartReply groupStatsUpdated = assembleMPReplyMessage(type, mpReplyGroupCaseBld.build());
        List<MultipartReply> statsData = Collections.singletonList(groupStatsUpdated);
        fireAndCheck(type, statsData);
    }

    @Test
    public void testGatherStatistics_groupDesc() throws Exception {
        MultipartType type = MultipartType.OFPMPGROUPDESC;
        final long groupIdValue = 27L;

        BucketsListBuilder bucketsListBld = new BucketsListBuilder()
                .setWatchPort(new PortNumber(5L));
        GroupDescBuilder groupStatsBld = new GroupDescBuilder()
                .setBucketsList(Lists.newArrayList(bucketsListBld.build()))
                .setGroupId(new GroupId(groupIdValue))
                .setType(GroupType.OFPGTALL);
        MultipartReplyGroupDescBuilder mpReplyGroupBld = new MultipartReplyGroupDescBuilder();
        mpReplyGroupBld.setGroupDesc(Lists.newArrayList(groupStatsBld.build()));
        MultipartReplyGroupDescCaseBuilder mpReplyGroupCaseBld = new MultipartReplyGroupDescCaseBuilder();
        mpReplyGroupCaseBld.setMultipartReplyGroupDesc(mpReplyGroupBld.build());

        MultipartReply groupStatsUpdated = assembleMPReplyMessage(type, mpReplyGroupCaseBld.build());
        List<MultipartReply> statsData = Collections.singletonList(groupStatsUpdated);
        fireAndCheck(type, statsData);

        org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.GroupId storedGroupId = new org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.GroupId(groupIdValue);
        KeyedInstanceIdentifier<Group, GroupKey> groupPath = dummyNodePath.augmentation(FlowCapableNode.class).child(Group.class, new GroupKey(storedGroupId));

        verify(deviceContext, Mockito.never()).addDeleteToTxChain(Matchers.eq(LogicalDatastoreType.OPERATIONAL), Matchers.any(InstanceIdentifier.class));
        verify(deviceGroupRegistry).removeMarked();
        verify(deviceGroupRegistry).store(storedGroupId);
        verify(deviceContext).writeToTransaction(Matchers.eq(LogicalDatastoreType.OPERATIONAL), Matchers.eq(groupPath), Matchers.any(Group.class));
        verify(deviceContext).submitTransaction();
    }

    private void fireAndCheck(MultipartType type, List<MultipartReply> statsData) throws InterruptedException, ExecutionException, TimeoutException {
        when(statisticsService.getStatisticsOfType(Matchers.any(EventIdentifier.class), Matchers.eq(type)))
                .thenReturn(Futures.immediateFuture(RpcResultBuilder.success(statsData).build()));

        ListenableFuture<Boolean> gatherStatisticsResult = StatisticsGatheringUtils.gatherStatistics(statisticsService, deviceContext, type);
        Assert.assertTrue(gatherStatisticsResult.get(1, TimeUnit.SECONDS).booleanValue());
    }

    private static MultipartReplyMessage assembleMPReplyMessage(MultipartType type, MultipartReplyBody mpReplyGroupCaseBld) {
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
    public void testDeleteAllKnownFlowsNotSync() throws Exception {
        when(deviceState.deviceSynchronized()).thenReturn(false);
        StatisticsGatheringUtils.deleteAllKnownFlows(deviceContext);
        Mockito.verifyNoMoreInteractions(deviceFlowRegistry);
    }

    @Test
    public void testDeleteAllKnownFlows() throws Exception {
        when(deviceState.deviceSynchronized()).thenReturn(true);
        when(features.getTables()).thenReturn((short) 1);
        KeyedInstanceIdentifier<Table, TableKey> tablePath = deviceState.getNodeInstanceIdentifier()
                .augmentation(FlowCapableNode.class)
                .child(Table.class, new TableKey((short) 0));

        TableBuilder tableDataBld = new TableBuilder();
        Optional<Table> tableDataOpt = Optional.of(tableDataBld.build());
        CheckedFuture<Optional<Table>, ReadFailedException> tableDataFuture = Futures.immediateCheckedFuture(tableDataOpt);
        when(readTx.read(LogicalDatastoreType.OPERATIONAL, tablePath)).thenReturn(tableDataFuture);
        StatisticsGatheringUtils.deleteAllKnownFlows(deviceContext);


        verify(deviceContext).writeToTransaction(
                LogicalDatastoreType.OPERATIONAL,
                tablePath,
                tableDataBld.setFlow(Collections.<Flow>emptyList()).build());
    }
}
