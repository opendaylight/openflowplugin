/**
 * Copyright (c) 2016 Cisco Systems, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.applications.frsync.impl.strategy;

import com.google.common.collect.Lists;
import com.google.common.util.concurrent.ListenableFuture;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.Captor;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.openflowplugin.applications.frsync.impl.DSInputFactory;
import org.opendaylight.openflowplugin.applications.frsync.util.ItemSyncBox;
import org.opendaylight.openflowplugin.applications.frsync.util.SyncCrudCounters;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNode;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNodeBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.meters.Meter;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.TableBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.TableKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.table.Flow;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.AddFlowOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.RemoveFlowOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.UpdateFlowOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.transaction.rev150304.FlowCapableTransactionService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.transaction.rev150304.SendBarrierInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.transaction.rev150304.SendBarrierOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.service.rev130918.AddGroupOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.service.rev130918.RemoveGroupOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.service.rev130918.UpdateGroupOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.groups.Group;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.Nodes;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.NodeKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.service.rev130918.AddMeterOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.service.rev130918.RemoveMeterOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.service.rev130918.UpdateMeterOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.service.rev131026.UpdateTableOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table.features.TableFeatures;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table.features.TableFeaturesBuilder;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.opendaylight.yangtools.yang.common.RpcResultBuilder;

/**
 * Test for {@link SyncPlanPushStrategyIncrementalImpl}.
 */
@RunWith(MockitoJUnitRunner.class)
public class SyncPlanPushStrategyIncrementalImplTest {

    private static final NodeId NODE_ID = new NodeId("unit-nodeId");
    private static final InstanceIdentifier<FlowCapableNode> NODE_IDENT = InstanceIdentifier.create(Nodes.class)
            .child(Node.class, new NodeKey(NODE_ID)).augmentation(FlowCapableNode.class);

    private SyncPlanPushStrategyIncrementalImpl syncPlanPushStrategy;

    @Mock
    private DataBroker db;
    @Mock
    private GroupForwarder groupCommitter;
    @Mock
    private FlowForwarder flowCommitter;
    @Mock
    private MeterForwarder meterCommitter;
    @Mock
    private TableForwarder tableCommitter;
    @Mock
    private FlowCapableTransactionService flowCapableTxService;

    @Captor
    private ArgumentCaptor<Group> groupCaptor;
    @Captor
    private ArgumentCaptor<Group> groupUpdateCaptor;
    @Captor
    private ArgumentCaptor<Flow> flowCaptor;
    @Captor
    private ArgumentCaptor<Flow> flowUpdateCaptor;
    @Captor
    private ArgumentCaptor<Meter> meterCaptor;
    @Captor
    private ArgumentCaptor<Meter> meterUpdateCaptor;
    @Captor
    private ArgumentCaptor<TableFeatures> tableFeaturesCaptor;

    private SyncCrudCounters counters;

    private final List<ItemSyncBox<Group>> groupsToAddOrUpdate;
    private final List<ItemSyncBox<Group>> groupsToRemove;
    private final ItemSyncBox<Meter> metersToAddOrUpdate;
    private final ItemSyncBox<Meter> metersToRemove;
    private final Map<TableKey, ItemSyncBox<Flow>> flowsToAddOrUpdate;
    private final Map<TableKey, ItemSyncBox<Flow>> flowsToRemove;

    public SyncPlanPushStrategyIncrementalImplTest() {
        groupsToAddOrUpdate = Lists.newArrayList(DiffInputFactory.createGroupSyncBox(1, 2, 3),
                DiffInputFactory.createGroupSyncBoxWithUpdates(4, 5, 6));
        groupsToRemove = Lists.newArrayList(DiffInputFactory.createGroupSyncBox(1, 2, 3),
                DiffInputFactory.createGroupSyncBox(4, 5, 6));

        metersToAddOrUpdate = DiffInputFactory.createMeterSyncBoxWithUpdates(1, 2, 3);
        metersToRemove = DiffInputFactory.createMeterSyncBox(1, 2, 3);

        flowsToAddOrUpdate = new HashMap<>();
        flowsToAddOrUpdate.put(new TableKey((short) 0), DiffInputFactory.createFlowSyncBox("1", "2", "3"));
        flowsToAddOrUpdate.put(new TableKey((short) 1), DiffInputFactory.createFlowSyncBoxWithUpdates("4", "5", "6"));
        flowsToRemove = new HashMap<>();
        flowsToRemove.put(new TableKey((short) 0), DiffInputFactory.createFlowSyncBox("1", "2", "3"));
        flowsToRemove.put(new TableKey((short) 1), DiffInputFactory.createFlowSyncBox("4", "5", "6"));
    }

    @Test
    public void testExecuteSyncStrategy() throws Exception {
        final SynchronizationDiffInput diffInput = new SynchronizationDiffInput(NODE_IDENT,
                groupsToAddOrUpdate, metersToAddOrUpdate, flowsToAddOrUpdate, flowsToRemove,
                metersToRemove, groupsToRemove);

        final SyncCrudCounters syncCounters = new SyncCrudCounters();
        final ListenableFuture<RpcResult<Void>> rpcResult = syncPlanPushStrategy.executeSyncStrategy(
                RpcResultBuilder.<Void>success().buildFuture(), diffInput, syncCounters);

        Mockito.verify(groupCommitter, Mockito.times(6)).add(ArgumentMatchers.<InstanceIdentifier<Group>>any(),
                ArgumentMatchers.<Group>any(), ArgumentMatchers.<InstanceIdentifier<FlowCapableNode>>any());
        Mockito.verify(groupCommitter, Mockito.times(3)).update(ArgumentMatchers.<InstanceIdentifier<Group>>any(),
                ArgumentMatchers.<Group>any(), ArgumentMatchers.<Group>any(),
                ArgumentMatchers.<InstanceIdentifier<FlowCapableNode>>any());
        Mockito.verify(groupCommitter, Mockito.times(6)).remove(ArgumentMatchers.<InstanceIdentifier<Group>>any(),
                ArgumentMatchers.<Group>any(), ArgumentMatchers.<InstanceIdentifier<FlowCapableNode>>any());
        Mockito.verify(flowCommitter, Mockito.times(6)).add(ArgumentMatchers.<InstanceIdentifier<Flow>>any(),
                ArgumentMatchers.<Flow>any(), ArgumentMatchers.<InstanceIdentifier<FlowCapableNode>>any());
        Mockito.verify(flowCommitter, Mockito.times(3)).update(ArgumentMatchers.<InstanceIdentifier<Flow>>any(),
                ArgumentMatchers.<Flow>any(), ArgumentMatchers.<Flow>any(),
                ArgumentMatchers.<InstanceIdentifier<FlowCapableNode>>any());
        Mockito.verify(flowCommitter, Mockito.times(6)).remove(ArgumentMatchers.<InstanceIdentifier<Flow>>any(),
                ArgumentMatchers.<Flow>any(), ArgumentMatchers.<InstanceIdentifier<FlowCapableNode>>any());
        Mockito.verify(meterCommitter, Mockito.times(3)).add(ArgumentMatchers.<InstanceIdentifier<Meter>>any(),
                ArgumentMatchers.<Meter>any(), ArgumentMatchers.<InstanceIdentifier<FlowCapableNode>>any());
        Mockito.verify(meterCommitter, Mockito.times(3)).update(ArgumentMatchers.<InstanceIdentifier<Meter>>any(),
                ArgumentMatchers.<Meter>any(), ArgumentMatchers.<Meter>any(),
                ArgumentMatchers.<InstanceIdentifier<FlowCapableNode>>any());
        Mockito.verify(meterCommitter, Mockito.times(3)).remove(ArgumentMatchers.<InstanceIdentifier<Meter>>any(),
                ArgumentMatchers.<Meter>any(), ArgumentMatchers.<InstanceIdentifier<FlowCapableNode>>any());

        Assert.assertTrue(rpcResult.isDone());
        Assert.assertTrue(rpcResult.get().isSuccessful());

        Assert.assertEquals(6, syncCounters.getFlowCrudCounts().getAdded());
        Assert.assertEquals(3, syncCounters.getFlowCrudCounts().getUpdated());
        Assert.assertEquals(6, syncCounters.getFlowCrudCounts().getRemoved());

        Assert.assertEquals(6, syncCounters.getGroupCrudCounts().getAdded());
        Assert.assertEquals(3, syncCounters.getGroupCrudCounts().getUpdated());
        Assert.assertEquals(6, syncCounters.getGroupCrudCounts().getRemoved());

        Assert.assertEquals(3, syncCounters.getMeterCrudCounts().getAdded());
        Assert.assertEquals(3, syncCounters.getMeterCrudCounts().getUpdated());
        Assert.assertEquals(3, syncCounters.getMeterCrudCounts().getRemoved());
    }

    @Before
    public void setUp() throws Exception {
        Mockito.when(flowCapableTxService.sendBarrier(ArgumentMatchers.<SendBarrierInput>any()))
                .thenReturn(RpcResultBuilder.success((SendBarrierOutput) null).buildFuture());

        Mockito.doAnswer(createSalServiceFutureAnswer()).when(groupCommitter).add(
                ArgumentMatchers.<InstanceIdentifier<Group>>any(), ArgumentMatchers.<Group>any(),
                ArgumentMatchers.<InstanceIdentifier<FlowCapableNode>>any());
        Mockito.doAnswer(createSalServiceFutureAnswer()).when(groupCommitter).update(
                ArgumentMatchers.<InstanceIdentifier<Group>>any(), ArgumentMatchers.<Group>any(),
                ArgumentMatchers.<Group>any(),
                ArgumentMatchers.<InstanceIdentifier<FlowCapableNode>>any());
        Mockito.doAnswer(createSalServiceFutureAnswer()).when(groupCommitter).remove(
                ArgumentMatchers.<InstanceIdentifier<Group>>any(), ArgumentMatchers.<Group>any(),
                ArgumentMatchers.<InstanceIdentifier<FlowCapableNode>>any());

        Mockito.doAnswer(createSalServiceFutureAnswer()).when(flowCommitter).add(
                ArgumentMatchers.<InstanceIdentifier<Flow>>any(), ArgumentMatchers.<Flow>any(),
                ArgumentMatchers.<InstanceIdentifier<FlowCapableNode>>any());
        Mockito.doAnswer(createSalServiceFutureAnswer()).when(flowCommitter).update(
                ArgumentMatchers.<InstanceIdentifier<Flow>>any(), ArgumentMatchers.<Flow>any(),
                ArgumentMatchers.<Flow>any(), ArgumentMatchers.<InstanceIdentifier<FlowCapableNode>>any());
        Mockito.doAnswer(createSalServiceFutureAnswer()).when(flowCommitter).remove(
                ArgumentMatchers.<InstanceIdentifier<Flow>>any(), ArgumentMatchers.<Flow>any(),
                ArgumentMatchers.<InstanceIdentifier<FlowCapableNode>>any());

        Mockito.doAnswer(createSalServiceFutureAnswer()).when(meterCommitter).add(
                ArgumentMatchers.<InstanceIdentifier<Meter>>any(), ArgumentMatchers.<Meter>any(),
                ArgumentMatchers.<InstanceIdentifier<FlowCapableNode>>any());
        Mockito.doAnswer(createSalServiceFutureAnswer()).when(meterCommitter).update(
                ArgumentMatchers.<InstanceIdentifier<Meter>>any(), ArgumentMatchers.<Meter>any(),
                ArgumentMatchers.<Meter>any(), ArgumentMatchers.<InstanceIdentifier<FlowCapableNode>>any());
        Mockito.doAnswer(createSalServiceFutureAnswer()).when(meterCommitter).remove(
                ArgumentMatchers.<InstanceIdentifier<Meter>>any(), ArgumentMatchers.<Meter>any(),
                ArgumentMatchers.<InstanceIdentifier<FlowCapableNode>>any());

        Mockito.doAnswer(createSalServiceFutureAnswer()).when(tableCommitter).update(
                ArgumentMatchers.<InstanceIdentifier<TableFeatures>>any(), ArgumentMatchers.<TableFeatures>any(),
                ArgumentMatchers.<TableFeatures>any(), ArgumentMatchers.<InstanceIdentifier<FlowCapableNode>>any());

        syncPlanPushStrategy = new SyncPlanPushStrategyIncrementalImpl()
                .setMeterForwarder(meterCommitter)
                .setTableForwarder(tableCommitter)
                .setGroupForwarder(groupCommitter)
                .setFlowForwarder(flowCommitter)
                .setTransactionService(flowCapableTxService);

        counters = new SyncCrudCounters();
    }

    private <O> Answer<Future<RpcResult<O>>> createSalServiceFutureAnswer() {
        return invocation -> RpcResultBuilder.<O>success().buildFuture();
    }

    @Test
    public void testAddMissingFlows() throws Exception {
        Mockito.when(flowCommitter.add(ArgumentMatchers.<InstanceIdentifier<Flow>>any(), flowCaptor.capture(),
                ArgumentMatchers.same(NODE_IDENT)))
                .thenReturn(RpcResultBuilder.success(new AddFlowOutputBuilder().build()).buildFuture());

        final ItemSyncBox<Flow> flowBox = new ItemSyncBox<>();
        flowBox.getItemsToPush().add(DSInputFactory.createFlow("f3", 3));
        flowBox.getItemsToPush().add(DSInputFactory.createFlow("f4", 4));

        final Map<TableKey, ItemSyncBox<Flow>> flowBoxMap = new LinkedHashMap<>();
        flowBoxMap.put(new TableKey((short) 0), flowBox);

        final ListenableFuture<RpcResult<Void>> result = syncPlanPushStrategy.addMissingFlows(
                NODE_ID, NODE_IDENT, flowBoxMap, counters);

        Assert.assertTrue(result.isDone());
        Assert.assertTrue(result.get().isSuccessful());

        final List<Flow> flowCaptorAllValues = flowCaptor.getAllValues();
        Assert.assertEquals(2, flowCaptorAllValues.size());
        Assert.assertEquals("f3", flowCaptorAllValues.get(0).getId().getValue());
        Assert.assertEquals("f4", flowCaptorAllValues.get(1).getId().getValue());

        final InOrder inOrderFlow = Mockito.inOrder(flowCapableTxService, flowCommitter);
        inOrderFlow.verify(flowCommitter, Mockito.times(2)).add(ArgumentMatchers.<InstanceIdentifier<Flow>>any(),
                ArgumentMatchers.<Flow>any(), ArgumentMatchers.eq(NODE_IDENT));
        //TODO: uncomment when enabled in impl
//        inOrderFlow.verify(flowCapableTxService).sendBarrier(Matchers.<SendBarrierInput>any());
        inOrderFlow.verifyNoMoreInteractions();
    }

    @Test
    public void testRemoveRedundantFlows() throws Exception {
        Mockito.when(flowCommitter.remove(ArgumentMatchers.<InstanceIdentifier<Flow>>any(), flowCaptor.capture(),
                ArgumentMatchers.same(NODE_IDENT)))
                .thenReturn(RpcResultBuilder.success(new RemoveFlowOutputBuilder().build()).buildFuture());

        final ItemSyncBox<Flow> flowBox = new ItemSyncBox<>();
        flowBox.getItemsToPush().add(DSInputFactory.createFlow("f3", 3));
        flowBox.getItemsToPush().add(DSInputFactory.createFlow("f4", 4));

        final Map<TableKey, ItemSyncBox<Flow>> flowBoxMap = new LinkedHashMap<>();
        flowBoxMap.put(new TableKey((short) 0), flowBox);

        final ListenableFuture<RpcResult<Void>> result = syncPlanPushStrategy.removeRedundantFlows(
                NODE_ID, NODE_IDENT, flowBoxMap, counters);

        Assert.assertTrue(result.isDone());
        Assert.assertTrue(result.get().isSuccessful());

        final List<Flow> flowCaptorAllValues = flowCaptor.getAllValues();
        Assert.assertEquals(2, flowCaptorAllValues.size());
        Assert.assertEquals("f3", flowCaptorAllValues.get(0).getId().getValue());
        Assert.assertEquals("f4", flowCaptorAllValues.get(1).getId().getValue());

        final InOrder inOrderFlow = Mockito.inOrder(flowCapableTxService, flowCommitter);
        inOrderFlow.verify(flowCommitter, Mockito.times(2)).remove(ArgumentMatchers.<InstanceIdentifier<Flow>>any(),
                ArgumentMatchers.<Flow>any(), ArgumentMatchers.eq(NODE_IDENT));
        inOrderFlow.verify(flowCapableTxService).sendBarrier(ArgumentMatchers.<SendBarrierInput>any());
        inOrderFlow.verifyNoMoreInteractions();
    }


    @Test
    public void testAddMissingFlows_withUpdate() throws Exception {
        Mockito.when(flowCommitter.add(ArgumentMatchers.<InstanceIdentifier<Flow>>any(), flowCaptor.capture(),
                ArgumentMatchers.same(NODE_IDENT)))
                .thenReturn(RpcResultBuilder.success(new AddFlowOutputBuilder().build()).buildFuture());

        Mockito.when(flowCommitter.update(ArgumentMatchers.<InstanceIdentifier<Flow>>any(),
                flowUpdateCaptor.capture(), flowUpdateCaptor.capture(),
                ArgumentMatchers.same(NODE_IDENT)))
                .thenReturn(RpcResultBuilder.success(new UpdateFlowOutputBuilder().build()).buildFuture());

        final ItemSyncBox<Flow> flowBox = new ItemSyncBox<>();
        flowBox.getItemsToPush().add(DSInputFactory.createFlow("f3", 3));
        flowBox.getItemsToPush().add(DSInputFactory.createFlow("f4", 4));
        flowBox.getItemsToUpdate().add(new ItemSyncBox.ItemUpdateTuple<>(
                DSInputFactory.createFlow("f1", 1), DSInputFactory.createFlowWithInstruction("f1", 1)));

        final Map<TableKey, ItemSyncBox<Flow>> flowBoxMap = new LinkedHashMap<>();
        flowBoxMap.put(new TableKey((short) 0), flowBox);


        //TODO: replace null
        final ListenableFuture<RpcResult<Void>> result = syncPlanPushStrategy.addMissingFlows(
                NODE_ID, NODE_IDENT, flowBoxMap, counters);

        Assert.assertTrue(result.isDone());
        Assert.assertTrue(result.get().isSuccessful());

        final List<Flow> flowCaptorAllValues = flowCaptor.getAllValues();
        Assert.assertEquals(2, flowCaptorAllValues.size());
        Assert.assertEquals("f3", flowCaptorAllValues.get(0).getId().getValue());
        Assert.assertEquals("f4", flowCaptorAllValues.get(1).getId().getValue());

        final List<Flow> flowUpdateCaptorAllValues = flowUpdateCaptor.getAllValues();
        Assert.assertEquals(2, flowUpdateCaptorAllValues.size());
        Assert.assertEquals("f1", flowUpdateCaptorAllValues.get(0).getId().getValue());
        Assert.assertEquals("f1", flowUpdateCaptorAllValues.get(1).getId().getValue());

        final InOrder inOrderFlow = Mockito.inOrder(flowCapableTxService, flowCommitter);
        // add f3, f4
        inOrderFlow.verify(flowCommitter, Mockito.times(2)).add(ArgumentMatchers.<InstanceIdentifier<Flow>>any(),
                ArgumentMatchers.<Flow>any(), ArgumentMatchers.eq(NODE_IDENT));
        // update f1
        inOrderFlow.verify(flowCommitter).update(ArgumentMatchers.<InstanceIdentifier<Flow>>any(),
                ArgumentMatchers.<Flow>any(), ArgumentMatchers.<Flow>any(), ArgumentMatchers.eq(NODE_IDENT));
        //TODO: uncomment when enabled in impl
//        inOrderFlow.verify(flowCapableTxService).sendBarrier(Matchers.<SendBarrierInput>any());

        inOrderFlow.verifyNoMoreInteractions();
    }

    @Test
    public void testAddMissingMeters() throws Exception {
        Mockito.when(meterCommitter.add(ArgumentMatchers.<InstanceIdentifier<Meter>>any(), meterCaptor.capture(),
                ArgumentMatchers.same(NODE_IDENT)))
                .thenReturn(RpcResultBuilder.success(new AddMeterOutputBuilder().build()).buildFuture());

        final ItemSyncBox<Meter> meterSyncBox = new ItemSyncBox<>();
        meterSyncBox.getItemsToPush().add(DSInputFactory.createMeter(2L));
        meterSyncBox.getItemsToPush().add(DSInputFactory.createMeter(4L));

        final ListenableFuture<RpcResult<Void>> result = syncPlanPushStrategy.addMissingMeters(
                NODE_ID, NODE_IDENT, meterSyncBox, counters);

        Assert.assertTrue(result.isDone());
        Assert.assertTrue(result.get().isSuccessful());

        final List<Meter> metercaptorAllValues = meterCaptor.getAllValues();
        Assert.assertEquals(2, metercaptorAllValues.size());
        Assert.assertEquals(2L, metercaptorAllValues.get(0).getMeterId().getValue().longValue());
        Assert.assertEquals(4L, metercaptorAllValues.get(1).getMeterId().getValue().longValue());

        final InOrder inOrderMeter = Mockito.inOrder(flowCapableTxService, meterCommitter);
        inOrderMeter.verify(meterCommitter, Mockito.times(2)).add(ArgumentMatchers.<InstanceIdentifier<Meter>>any(),
                ArgumentMatchers.<Meter>any(), ArgumentMatchers.eq(NODE_IDENT));
        //TODO: uncomment when enabled in impl
//        inOrderMeter.verify(flowCapableTxService).sendBarrier(Matchers.<SendBarrierInput>any());
        inOrderMeter.verifyNoMoreInteractions();
    }

    @Test
    public void testAddMissingMeters_withUpdate() throws Exception {
        Mockito.when(meterCommitter.add(ArgumentMatchers.<InstanceIdentifier<Meter>>any(), meterCaptor.capture(),
                ArgumentMatchers.same(NODE_IDENT)))
                .thenReturn(RpcResultBuilder.success(new AddMeterOutputBuilder().build()).buildFuture());

        Mockito.when(meterCommitter.update(ArgumentMatchers.<InstanceIdentifier<Meter>>any(),
                meterUpdateCaptor.capture(), meterUpdateCaptor.capture(), ArgumentMatchers.same(NODE_IDENT)))
                .thenReturn(RpcResultBuilder.success(new UpdateMeterOutputBuilder().build()).buildFuture());

        final ItemSyncBox<Meter> meterSyncBox = new ItemSyncBox<>();
        meterSyncBox.getItemsToPush().add(DSInputFactory.createMeter(2L));
        meterSyncBox.getItemsToPush().add(DSInputFactory.createMeter(4L));
        meterSyncBox.getItemsToUpdate().add(new ItemSyncBox.ItemUpdateTuple<>(
                DSInputFactory.createMeter(1L), DSInputFactory.createMeterWithBody(1L)));

        final ListenableFuture<RpcResult<Void>> result = syncPlanPushStrategy.addMissingMeters(
                NODE_ID, NODE_IDENT, meterSyncBox, counters);

        Assert.assertTrue(result.isDone());
        Assert.assertTrue(result.get().isSuccessful());

        final List<Meter> meterCaptorAllValues = meterCaptor.getAllValues();
        Assert.assertEquals(2, meterCaptorAllValues.size());
        Assert.assertEquals(2L, meterCaptorAllValues.get(0).getMeterId().getValue().longValue());
        Assert.assertEquals(4L, meterCaptorAllValues.get(1).getMeterId().getValue().longValue());


        final List<Meter> meterUpdateCaptorAllValues = meterUpdateCaptor.getAllValues();
        Assert.assertEquals(2, meterUpdateCaptorAllValues.size());
        Assert.assertEquals(1L, meterUpdateCaptorAllValues.get(0).getMeterId().getValue().longValue());
        Assert.assertEquals(1L, meterUpdateCaptorAllValues.get(1).getMeterId().getValue().longValue());

        final InOrder inOrderMeters = Mockito.inOrder(flowCapableTxService, meterCommitter);
        inOrderMeters.verify(meterCommitter, Mockito.times(2)).add(ArgumentMatchers.<InstanceIdentifier<Meter>>any(),
                ArgumentMatchers.<Meter>any(), ArgumentMatchers.eq(NODE_IDENT));
        inOrderMeters.verify(meterCommitter).update(ArgumentMatchers.<InstanceIdentifier<Meter>>any(),
                ArgumentMatchers.<Meter>any(), ArgumentMatchers.<Meter>any(), ArgumentMatchers.eq(NODE_IDENT));
        //TODO: uncomment when enabled in impl
//        inOrderMeters.verify(flowCapableTxService).sendBarrier(Matchers.<SendBarrierInput>any());

        inOrderMeters.verifyNoMoreInteractions();
    }

    @Test
    public void testRemoveRedundantMeters() throws Exception {
        Mockito.when(meterCommitter.remove(ArgumentMatchers.<InstanceIdentifier<Meter>>any(), meterCaptor.capture(),
                ArgumentMatchers.same(NODE_IDENT)))
                .thenReturn(RpcResultBuilder.success(new RemoveMeterOutputBuilder().build()).buildFuture());

        final ItemSyncBox<Meter> meterSyncBox = new ItemSyncBox<>();
        meterSyncBox.getItemsToPush().add(DSInputFactory.createMeter(2L));
        meterSyncBox.getItemsToPush().add(DSInputFactory.createMeter(4L));
        meterSyncBox.getItemsToUpdate().add(new ItemSyncBox.ItemUpdateTuple<>(
                DSInputFactory.createMeter(1L), DSInputFactory.createMeterWithBody(1L)));

        final ListenableFuture<RpcResult<Void>> result = syncPlanPushStrategy.removeRedundantMeters(
                NODE_ID, NODE_IDENT, meterSyncBox, counters);

        Assert.assertTrue(result.isDone());
        Assert.assertTrue(result.get().isSuccessful());

        final List<Meter> metercaptorAllValues = meterCaptor.getAllValues();
        Assert.assertEquals(2, metercaptorAllValues.size());
        Assert.assertEquals(2L, metercaptorAllValues.get(0).getMeterId().getValue().longValue());
        Assert.assertEquals(4L, metercaptorAllValues.get(1).getMeterId().getValue().longValue());

        final InOrder inOrderMeter = Mockito.inOrder(flowCapableTxService, meterCommitter);
        inOrderMeter.verify(meterCommitter, Mockito.times(2)).remove(ArgumentMatchers.<InstanceIdentifier<Meter>>any(),
                ArgumentMatchers.<Meter>any(), ArgumentMatchers.eq(NODE_IDENT));
        //TODO: uncomment when enabled in impl
//        inOrderMeter.verify(flowCapableTxService).sendBarrier(Matchers.<SendBarrierInput>any());
        inOrderMeter.verifyNoMoreInteractions();
    }

    @Test
    public void testAddMissingGroups() throws Exception {
        Mockito.when(groupCommitter.add(ArgumentMatchers.<InstanceIdentifier<Group>>any(), groupCaptor.capture(),
                ArgumentMatchers.same(NODE_IDENT)))
                .thenReturn(RpcResultBuilder.success(new AddGroupOutputBuilder().build()).buildFuture());

        ItemSyncBox<Group> groupBox1 = new ItemSyncBox<>();
        groupBox1.getItemsToPush().add(DSInputFactory.createGroup(2L));

        ItemSyncBox<Group> groupBox2 = new ItemSyncBox<>();
        groupBox2.getItemsToPush().add(DSInputFactory.createGroupWithPreconditions(3L, 2L));
        groupBox2.getItemsToPush().add(DSInputFactory.createGroupWithPreconditions(4L, 2L));

        ItemSyncBox<Group> groupBox3 = new ItemSyncBox<>();
        groupBox3.getItemsToPush().add(DSInputFactory.createGroupWithPreconditions(5L, 3L, 4L));

        final List<ItemSyncBox<Group>> groupBoxLot = Lists.newArrayList(groupBox1, groupBox2, groupBox3);

        final ListenableFuture<RpcResult<Void>> result = syncPlanPushStrategy.addMissingGroups(
                NODE_ID, NODE_IDENT, groupBoxLot, counters);

        Assert.assertTrue(result.isDone());
        Assert.assertTrue(result.get().isSuccessful());

        final List<Group> groupCaptorAllValues = groupCaptor.getAllValues();
        Assert.assertEquals(4, groupCaptorAllValues.size());
        Assert.assertEquals(2L, groupCaptorAllValues.get(0).getGroupId().getValue().longValue());
        Assert.assertEquals(3L, groupCaptorAllValues.get(1).getGroupId().getValue().longValue());
        Assert.assertEquals(4L, groupCaptorAllValues.get(2).getGroupId().getValue().longValue());
        Assert.assertEquals(5L, groupCaptorAllValues.get(3).getGroupId().getValue().longValue());

        final InOrder inOrderGroups = Mockito.inOrder(flowCapableTxService, groupCommitter);
        // add 2
        inOrderGroups.verify(groupCommitter).add(ArgumentMatchers.<InstanceIdentifier<Group>>any(),
                ArgumentMatchers.<Group>any(), ArgumentMatchers.eq(NODE_IDENT));
        inOrderGroups.verify(flowCapableTxService).sendBarrier(ArgumentMatchers.<SendBarrierInput>any());
        // add 3, 4
        inOrderGroups.verify(groupCommitter, Mockito.times(2)).add(ArgumentMatchers.<InstanceIdentifier<Group>>any(),
                ArgumentMatchers.<Group>any(), ArgumentMatchers.eq(NODE_IDENT));
        inOrderGroups.verify(flowCapableTxService).sendBarrier(ArgumentMatchers.<SendBarrierInput>any());
        // add 5
        inOrderGroups.verify(groupCommitter).add(ArgumentMatchers.<InstanceIdentifier<Group>>any(),
                ArgumentMatchers.<Group>any(), ArgumentMatchers.eq(NODE_IDENT));
        inOrderGroups.verify(flowCapableTxService).sendBarrier(ArgumentMatchers.<SendBarrierInput>any());

        inOrderGroups.verifyNoMoreInteractions();
    }

    @Test
    public void testAddMissingGroups_withUpdate() throws Exception {
        Mockito.when(groupCommitter.add(ArgumentMatchers.<InstanceIdentifier<Group>>any(), groupCaptor.capture(),
                ArgumentMatchers.same(NODE_IDENT)))
                .thenReturn(RpcResultBuilder.success(new AddGroupOutputBuilder().build()).buildFuture());

        Mockito.when(groupCommitter.update(ArgumentMatchers.<InstanceIdentifier<Group>>any(),
                groupUpdateCaptor.capture(), groupUpdateCaptor.capture(),
                ArgumentMatchers.same(NODE_IDENT)))
                .thenReturn(RpcResultBuilder.success(new UpdateGroupOutputBuilder().build()).buildFuture());

        ItemSyncBox<Group> groupBox1 = new ItemSyncBox<>();
        groupBox1.getItemsToPush().add(DSInputFactory.createGroup(2L));
        groupBox1.getItemsToUpdate().add(new ItemSyncBox.ItemUpdateTuple<>(
                DSInputFactory.createGroup(1L), DSInputFactory.createGroupWithAction(1L)));

        ItemSyncBox<Group> groupBox2 = new ItemSyncBox<>();
        groupBox2.getItemsToPush().add(DSInputFactory.createGroupWithPreconditions(3L, 2L));
        groupBox2.getItemsToPush().add(DSInputFactory.createGroupWithPreconditions(4L, 2L));

        ItemSyncBox<Group> groupBox3 = new ItemSyncBox<>();
        groupBox3.getItemsToPush().add(DSInputFactory.createGroupWithPreconditions(5L, 3L, 4L));

        final List<ItemSyncBox<Group>> groupBoxLot = Lists.newArrayList(groupBox1, groupBox2, groupBox3);
        final ListenableFuture<RpcResult<Void>> result = syncPlanPushStrategy.addMissingGroups(
                NODE_ID, NODE_IDENT, groupBoxLot, counters);

        Assert.assertTrue(result.isDone());
        Assert.assertTrue(result.get().isSuccessful());

        final List<Group> groupCaptorAllValues = groupCaptor.getAllValues();
        Assert.assertEquals(4, groupCaptorAllValues.size());
        Assert.assertEquals(2L, groupCaptorAllValues.get(0).getGroupId().getValue().longValue());
        Assert.assertEquals(3L, groupCaptorAllValues.get(1).getGroupId().getValue().longValue());
        Assert.assertEquals(4L, groupCaptorAllValues.get(2).getGroupId().getValue().longValue());
        Assert.assertEquals(5L, groupCaptorAllValues.get(3).getGroupId().getValue().longValue());

        final List<Group> groupUpdateCaptorAllValues = groupUpdateCaptor.getAllValues();
        Assert.assertEquals(2, groupUpdateCaptorAllValues.size());
        Assert.assertEquals(1L, groupUpdateCaptorAllValues.get(0).getGroupId().getValue().longValue());
        Assert.assertEquals(1L, groupUpdateCaptorAllValues.get(1).getGroupId().getValue().longValue());

        final InOrder inOrderGroups = Mockito.inOrder(flowCapableTxService, groupCommitter);

        // add 2, update 1
        inOrderGroups.verify(groupCommitter).add(ArgumentMatchers.<InstanceIdentifier<Group>>any(),
                ArgumentMatchers.<Group>any(), ArgumentMatchers.eq(NODE_IDENT));
        inOrderGroups.verify(groupCommitter).update(ArgumentMatchers.<InstanceIdentifier<Group>>any(),
                ArgumentMatchers.<Group>any(), ArgumentMatchers.<Group>any(), ArgumentMatchers.eq(NODE_IDENT));
        inOrderGroups.verify(flowCapableTxService).sendBarrier(ArgumentMatchers.<SendBarrierInput>any());

        // add 3, 4
        inOrderGroups.verify(groupCommitter, Mockito.times(2)).add(ArgumentMatchers.<InstanceIdentifier<Group>>any(),
                ArgumentMatchers.<Group>any(), ArgumentMatchers.eq(NODE_IDENT));
        inOrderGroups.verify(flowCapableTxService).sendBarrier(ArgumentMatchers.<SendBarrierInput>any());
        // add 5
        inOrderGroups.verify(groupCommitter).add(ArgumentMatchers.<InstanceIdentifier<Group>>any(),
                ArgumentMatchers.<Group>any(), ArgumentMatchers.eq(NODE_IDENT));
        inOrderGroups.verify(flowCapableTxService).sendBarrier(ArgumentMatchers.<SendBarrierInput>any());

        inOrderGroups.verifyNoMoreInteractions();
    }

    @Test
    public void testRemoveRedundantGroups() throws Exception {
        Mockito.when(groupCommitter.remove(ArgumentMatchers.<InstanceIdentifier<Group>>any(), groupCaptor.capture(),
                ArgumentMatchers.same(NODE_IDENT)))
                .thenReturn(RpcResultBuilder.success(new RemoveGroupOutputBuilder().build()).buildFuture());

        ItemSyncBox<Group> groupBox1 = new ItemSyncBox<>();
        groupBox1.getItemsToPush().add(DSInputFactory.createGroup(2L));
        groupBox1.getItemsToUpdate().add(new ItemSyncBox.ItemUpdateTuple<>(
                DSInputFactory.createGroup(1L), DSInputFactory.createGroupWithAction(1L)));

        ItemSyncBox<Group> groupBox2 = new ItemSyncBox<>();
        groupBox2.getItemsToPush().add(DSInputFactory.createGroupWithPreconditions(3L, 2L));
        groupBox2.getItemsToPush().add(DSInputFactory.createGroupWithPreconditions(4L, 2L));

        ItemSyncBox<Group> groupBox3 = new ItemSyncBox<>();
        groupBox3.getItemsToPush().add(DSInputFactory.createGroupWithPreconditions(5L, 3L, 4L));

        final List<ItemSyncBox<Group>> groupBoxLot = Lists.newArrayList(groupBox1, groupBox2, groupBox3);
        final ListenableFuture<RpcResult<Void>> result = syncPlanPushStrategy.removeRedundantGroups(
                NODE_ID, NODE_IDENT, groupBoxLot, counters);

        Assert.assertTrue(result.isDone());
        Assert.assertTrue(result.get().isSuccessful());

        final List<Group> groupCaptorAllValues = groupCaptor.getAllValues();
        Assert.assertEquals(4, groupCaptorAllValues.size());
        Assert.assertEquals(5L, groupCaptorAllValues.get(0).getGroupId().getValue().longValue());
        Assert.assertEquals(3L, groupCaptorAllValues.get(1).getGroupId().getValue().longValue());
        Assert.assertEquals(4L, groupCaptorAllValues.get(2).getGroupId().getValue().longValue());
        Assert.assertEquals(2L, groupCaptorAllValues.get(3).getGroupId().getValue().longValue());

        final InOrder inOrderGroup = Mockito.inOrder(flowCapableTxService, groupCommitter);
        // remove 5
        inOrderGroup.verify(groupCommitter).remove(ArgumentMatchers.<InstanceIdentifier<Group>>any(),
                ArgumentMatchers.<Group>any(), ArgumentMatchers.eq(NODE_IDENT));
        inOrderGroup.verify(flowCapableTxService).sendBarrier(ArgumentMatchers.<SendBarrierInput>any());
        // remove 3, 4
        inOrderGroup.verify(groupCommitter, Mockito.times(2)).remove(ArgumentMatchers.<InstanceIdentifier<Group>>any(),
                ArgumentMatchers.<Group>any(), ArgumentMatchers.eq(NODE_IDENT));
        inOrderGroup.verify(flowCapableTxService).sendBarrier(ArgumentMatchers.<SendBarrierInput>any());
        // remove 2
        inOrderGroup.verify(groupCommitter).remove(ArgumentMatchers.<InstanceIdentifier<Group>>any(),
                ArgumentMatchers.<Group>any(), ArgumentMatchers.eq(NODE_IDENT));
        inOrderGroup.verify(flowCapableTxService).sendBarrier(ArgumentMatchers.<SendBarrierInput>any());

        inOrderGroup.verifyNoMoreInteractions();
    }

    @Test
    public void testUpdateTableFeatures() throws Exception {
        Mockito.lenient().when(tableCommitter.update(ArgumentMatchers.<InstanceIdentifier<TableFeatures>>any(),
                ArgumentMatchers.isNull(TableFeatures.class), tableFeaturesCaptor.capture(),
                ArgumentMatchers.same(NODE_IDENT)))
                .thenReturn(RpcResultBuilder.success(new UpdateTableOutputBuilder().build()).buildFuture());

        final FlowCapableNode operational = new FlowCapableNodeBuilder()
                .setTable(Collections.singletonList(new TableBuilder()
                        .setId((short) 1)
                        .build()))
                .setTableFeatures(Collections.singletonList(new TableFeaturesBuilder()
                        .setName("test table features")
                        .setTableId((short) 1)
                        .build()))
                .build();

        final ListenableFuture<RpcResult<Void>> result = syncPlanPushStrategy.updateTableFeatures(
                NODE_IDENT, operational);

        Assert.assertTrue(result.isDone());
        Assert.assertTrue(result.get().isSuccessful());

        final List<TableFeatures> groupCaptorAllValues = tableFeaturesCaptor.getAllValues();
        //TODO: uncomment when enabled in impl
//        Assert.assertEquals(1, groupCaptorAllValues.size());
//        Assert.assertEquals("test table features", groupCaptorAllValues.get(0).getName());
//        Assert.assertEquals(1, groupCaptorAllValues.get(0).getTableId().intValue());

        Mockito.verify(flowCapableTxService).sendBarrier(ArgumentMatchers.<SendBarrierInput>any());
    }
}
