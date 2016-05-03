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
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InOrder;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.openflowplugin.applications.frsync.impl.FlowForwarder;
import org.opendaylight.openflowplugin.applications.frsync.impl.GroupForwarder;
import org.opendaylight.openflowplugin.applications.frsync.impl.MeterForwarder;
import org.opendaylight.openflowplugin.applications.frsync.impl.TableForwarder;
import org.opendaylight.openflowplugin.applications.frsync.util.ItemSyncBox;
import org.opendaylight.openflowplugin.applications.frsync.util.SyncCrudCounters;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.Uri;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.GroupActionCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.GroupActionCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.OutputActionCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.group.action._case.GroupAction;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.group.action._case.GroupActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.output.action._case.OutputActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.list.Action;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.list.ActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNode;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNodeBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.meters.Meter;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.meters.MeterBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.TableBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.TableKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.table.Flow;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.table.FlowBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.AddFlowOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.RemoveFlowOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.UpdateFlowOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.transaction.rev150304.FlowCapableTransactionService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.transaction.rev150304.SendBarrierInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.InstructionsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.MatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.instruction.ApplyActionsCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.instruction.apply.actions._case.ApplyActionsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.list.InstructionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.service.rev130918.AddGroupOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.service.rev130918.RemoveGroupOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.service.rev130918.UpdateGroupOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.GroupId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.group.Buckets;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.group.BucketsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.group.buckets.Bucket;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.group.buckets.BucketBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.groups.Group;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.groups.GroupBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.Nodes;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.NodeKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.service.rev130918.AddMeterOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.service.rev130918.RemoveMeterOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.service.rev130918.UpdateMeterOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.BandId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.MeterId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.band.type.band.type.DropBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.meter.MeterBandHeadersBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.meter.meter.band.headers.MeterBandHeaderBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.service.rev131026.UpdateTableOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table.features.TableFeatures;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table.features.TableFeaturesBuilder;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.opendaylight.yangtools.yang.common.RpcResultBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;

/**
 * Test for {@link SyncPlanPushStrategyIncrementalImpl}.
 */
@RunWith(MockitoJUnitRunner.class)
public class SyncPlanPushStrategyIncrementalImplTest {

    private static final Logger LOG = LoggerFactory.getLogger(SyncPlanPushStrategyIncrementalImplTest.class);

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

    @Test
    public void testExecuteSyncStrategy() throws Exception {

    }

    @Before
    public void setUp() throws Exception {
        Mockito.when(flowCapableTxService.sendBarrier(Matchers.<SendBarrierInput>any()))
                .thenReturn(RpcResultBuilder.success((Void) null).buildFuture());

        Mockito.doAnswer(createSalServiceFutureAnswer()).when(groupCommitter).add(
                Matchers.<InstanceIdentifier<Group>>any(), Matchers.<Group>any(),
                Matchers.<InstanceIdentifier<FlowCapableNode>>any());
        Mockito.doAnswer(createSalServiceFutureAnswer()).when(groupCommitter).update(
                Matchers.<InstanceIdentifier<Group>>any(), Matchers.<Group>any(), Matchers.<Group>any(),
                Matchers.<InstanceIdentifier<FlowCapableNode>>any());
        Mockito.doAnswer(createSalServiceFutureAnswer()).when(groupCommitter).remove(
                Matchers.<InstanceIdentifier<Group>>any(), Matchers.<Group>any(),
                Matchers.<InstanceIdentifier<FlowCapableNode>>any());

        Mockito.doAnswer(createSalServiceFutureAnswer()).when(flowCommitter).add(
                Matchers.<InstanceIdentifier<Flow>>any(), Matchers.<Flow>any(),
                Matchers.<InstanceIdentifier<FlowCapableNode>>any());
        Mockito.doAnswer(createSalServiceFutureAnswer()).when(flowCommitter).update(
                Matchers.<InstanceIdentifier<Flow>>any(), Matchers.<Flow>any(), Matchers.<Flow>any(),
                Matchers.<InstanceIdentifier<FlowCapableNode>>any());
        Mockito.doAnswer(createSalServiceFutureAnswer()).when(flowCommitter).remove(
                Matchers.<InstanceIdentifier<Flow>>any(), Matchers.<Flow>any(),
                Matchers.<InstanceIdentifier<FlowCapableNode>>any());

        Mockito.doAnswer(createSalServiceFutureAnswer()).when(meterCommitter).add(
                Matchers.<InstanceIdentifier<Meter>>any(), Matchers.<Meter>any(),
                Matchers.<InstanceIdentifier<FlowCapableNode>>any());
        Mockito.doAnswer(createSalServiceFutureAnswer()).when(meterCommitter).update(
                Matchers.<InstanceIdentifier<Meter>>any(), Matchers.<Meter>any(), Matchers.<Meter>any(),
                Matchers.<InstanceIdentifier<FlowCapableNode>>any());
        Mockito.doAnswer(createSalServiceFutureAnswer()).when(meterCommitter).remove(
                Matchers.<InstanceIdentifier<Meter>>any(), Matchers.<Meter>any(),
                Matchers.<InstanceIdentifier<FlowCapableNode>>any());

        Mockito.doAnswer(createSalServiceFutureAnswer()).when(tableCommitter).update(
                Matchers.<InstanceIdentifier<TableFeatures>>any(), Matchers.<TableFeatures>any(), Matchers.<TableFeatures>any(),
                Matchers.<InstanceIdentifier<FlowCapableNode>>any());

        syncPlanPushStrategy = new SyncPlanPushStrategyIncrementalImpl()
                .setMeterForwarder(meterCommitter)
                .setTableForwarder(tableCommitter)
                .setGroupForwarder(groupCommitter)
                .setFlowForwarder(flowCommitter)
                .setTransactionService(flowCapableTxService);

        counters = new SyncCrudCounters();
    }

    private <O> Answer<Future<RpcResult<O>>> createSalServiceFutureAnswer() {
        return new Answer<Future<RpcResult<O>>>() {
            @Override
            public Future<RpcResult<O>> answer(final InvocationOnMock invocation) throws Throwable {
                return RpcResultBuilder.<O>success().buildFuture();
            }
        };
    }

    private Group createGroup(final long groupIdValue) {
        final Buckets buckets = new BucketsBuilder()
                .setBucket(Collections.<Bucket>emptyList())
                .build();
        return new GroupBuilder()
                .setGroupId(new GroupId(groupIdValue))
                .setBuckets(buckets)
                .build();
    }

    private Group createGroupWithAction(final long groupIdValue) {
        final Buckets buckets = new BucketsBuilder()
                .setBucket(Collections.singletonList(new BucketBuilder()
                        .setAction(Collections.singletonList(new ActionBuilder()
                                .setAction(new OutputActionCaseBuilder()
                                        .setOutputAction(new OutputActionBuilder()
                                                .setOutputNodeConnector(new Uri("ut-port-1"))
                                                .build())
                                        .build())
                                .build()))
                        .build()))
                .build();
        return new GroupBuilder()
                .setGroupId(new GroupId(groupIdValue))
                .setBuckets(buckets)
                .build();
    }

    private Flow createFlow(final String flowIdValue, final int priority) {
        return new FlowBuilder()
                .setId(new FlowId(flowIdValue))
                .setPriority(priority)
                .setTableId((short) 42)
                .setMatch(new MatchBuilder().build())
                .build();
    }

    private Flow createFlowWithInstruction(final String flowIdValue, final int priority) {
        return new FlowBuilder()
                .setId(new FlowId(flowIdValue))
                .setPriority(priority)
                .setTableId((short) 42)
                .setMatch(new MatchBuilder().build())
                .setInstructions(new InstructionsBuilder()
                        .setInstruction(Collections.singletonList(new InstructionBuilder()
                                .setInstruction(new ApplyActionsCaseBuilder()
                                        .setApplyActions(new ApplyActionsBuilder()
                                                .setAction(Collections.singletonList(new ActionBuilder()
                                                        .setAction(new OutputActionCaseBuilder()
                                                                .setOutputAction(new OutputActionBuilder()
                                                                        .setOutputNodeConnector(new Uri("ut-port-1"))
                                                                        .build())
                                                                .build())
                                                        .build()))
                                                .build())
                                        .build())
                                .build()))
                        .build())
                .build();
    }

    private Meter createMeter(final Long meterIdValue) {
        return new MeterBuilder()
                .setMeterId(new MeterId(meterIdValue))
                .build();
    }

    private Meter createMeterWithBody(final Long meterIdValue) {
        return new MeterBuilder()
                .setMeterId(new MeterId(meterIdValue))
                .setMeterBandHeaders(new MeterBandHeadersBuilder()
                        .setMeterBandHeader(Collections.singletonList(new MeterBandHeaderBuilder()
                                .setBandId(new BandId(42L))
                                .setBandType(new DropBuilder()
                                        .setDropRate(43L)
                                        .build())
                                .build()))
                        .build())
                .build();
    }

    private Group createGroupWithPreconditions(final long groupIdValue, final long... requiredId) {
        final List<Action> actionBag = new ArrayList<>();
        for (long groupIdPrecondition : requiredId) {
            final GroupAction groupAction = new GroupActionBuilder()
                    .setGroupId(groupIdPrecondition)
                    .build();
            final GroupActionCase groupActionCase = new GroupActionCaseBuilder()
                    .setGroupAction(groupAction)
                    .build();
            final Action action = new ActionBuilder()
                    .setAction(groupActionCase)
                    .build();
            actionBag.add(action);
        }

        final Bucket bucket = new BucketBuilder()
                .setAction(actionBag)
                .build();
        final Buckets buckets = new BucketsBuilder()
                .setBucket(Collections.singletonList(bucket))
                .build();

        return new GroupBuilder()
                .setGroupId(new GroupId(groupIdValue))
                .setBuckets(buckets)
                .build();
    }

    @Test
    public void testAddMissingFlows() throws Exception {
        Mockito.when(flowCommitter.add(Matchers.<InstanceIdentifier<Flow>>any(), flowCaptor.capture(),
                Matchers.same(NODE_IDENT)))
                .thenReturn(RpcResultBuilder.success(new AddFlowOutputBuilder().build()).buildFuture());

        final ItemSyncBox<Flow> flowBox = new ItemSyncBox<>();
        flowBox.getItemsToPush().add(createFlow("f3", 3));
        flowBox.getItemsToPush().add(createFlow("f4", 4));

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
        inOrderFlow.verify(flowCommitter, Mockito.times(2)).add(Matchers.<InstanceIdentifier<Flow>>any(),
                Matchers.<Flow>any(), Matchers.eq(NODE_IDENT));
        //TODO: uncomment when enabled in impl
//        inOrderFlow.verify(flowCapableTxService).sendBarrier(Matchers.<SendBarrierInput>any());
        inOrderFlow.verifyNoMoreInteractions();
    }

    @Test
    public void testRemoveRedundantFlows() throws Exception {
        Mockito.when(flowCommitter.remove(Matchers.<InstanceIdentifier<Flow>>any(), flowCaptor.capture(),
                Matchers.same(NODE_IDENT)))
                .thenReturn(RpcResultBuilder.success(new RemoveFlowOutputBuilder().build()).buildFuture());

        final ItemSyncBox<Flow> flowBox = new ItemSyncBox<>();
        flowBox.getItemsToPush().add(createFlow("f3", 3));
        flowBox.getItemsToPush().add(createFlow("f4", 4));

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
        inOrderFlow.verify(flowCommitter, Mockito.times(2)).remove(Matchers.<InstanceIdentifier<Flow>>any(),
                Matchers.<Flow>any(), Matchers.eq(NODE_IDENT));
        inOrderFlow.verify(flowCapableTxService).sendBarrier(Matchers.<SendBarrierInput>any());
        inOrderFlow.verifyNoMoreInteractions();
    }


    @Test
    public void testAddMissingFlows_withUpdate() throws Exception {
        Mockito.when(flowCommitter.add(Matchers.<InstanceIdentifier<Flow>>any(), flowCaptor.capture(),
                Matchers.same(NODE_IDENT)))
                .thenReturn(RpcResultBuilder.success(new AddFlowOutputBuilder().build()).buildFuture());

        Mockito.when(flowCommitter.update(Matchers.<InstanceIdentifier<Flow>>any(),
                flowUpdateCaptor.capture(), flowUpdateCaptor.capture(),
                Matchers.same(NODE_IDENT)))
                .thenReturn(RpcResultBuilder.success(new UpdateFlowOutputBuilder().build()).buildFuture());

        final ItemSyncBox<Flow> flowBox = new ItemSyncBox<>();
        flowBox.getItemsToPush().add(createFlow("f3", 3));
        flowBox.getItemsToPush().add(createFlow("f4", 4));
        flowBox.getItemsToUpdate().add(new ItemSyncBox.ItemUpdateTuple<>(
                createFlow("f1", 1), createFlowWithInstruction("f1", 1)));

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
        inOrderFlow.verify(flowCommitter, Mockito.times(2)).add(Matchers.<InstanceIdentifier<Flow>>any(),
                Matchers.<Flow>any(), Matchers.eq(NODE_IDENT));
        // update f1
        inOrderFlow.verify(flowCommitter).update(Matchers.<InstanceIdentifier<Flow>>any(),
                Matchers.<Flow>any(), Matchers.<Flow>any(), Matchers.eq(NODE_IDENT));
        //TODO: uncomment when enabled in impl
//        inOrderFlow.verify(flowCapableTxService).sendBarrier(Matchers.<SendBarrierInput>any());

        inOrderFlow.verifyNoMoreInteractions();
    }

    @Test
    public void testAddMissingMeters() throws Exception {
        Mockito.when(meterCommitter.add(Matchers.<InstanceIdentifier<Meter>>any(), meterCaptor.capture(),
                Matchers.same(NODE_IDENT)))
                .thenReturn(RpcResultBuilder.success(new AddMeterOutputBuilder().build()).buildFuture());

        final ItemSyncBox<Meter> meterSyncBox = new ItemSyncBox<>();
        meterSyncBox.getItemsToPush().add(createMeter(2L));
        meterSyncBox.getItemsToPush().add(createMeter(4L));

        final ListenableFuture<RpcResult<Void>> result = syncPlanPushStrategy.addMissingMeters(
                NODE_ID, NODE_IDENT, meterSyncBox, counters);

        Assert.assertTrue(result.isDone());
        Assert.assertTrue(result.get().isSuccessful());

        final List<Meter> metercaptorAllValues = meterCaptor.getAllValues();
        Assert.assertEquals(2, metercaptorAllValues.size());
        Assert.assertEquals(2L, metercaptorAllValues.get(0).getMeterId().getValue().longValue());
        Assert.assertEquals(4L, metercaptorAllValues.get(1).getMeterId().getValue().longValue());

        final InOrder inOrderMeter = Mockito.inOrder(flowCapableTxService, meterCommitter);
        inOrderMeter.verify(meterCommitter, Mockito.times(2)).add(Matchers.<InstanceIdentifier<Meter>>any(),
                Matchers.<Meter>any(), Matchers.eq(NODE_IDENT));
        //TODO: uncomment when enabled in impl
//        inOrderMeter.verify(flowCapableTxService).sendBarrier(Matchers.<SendBarrierInput>any());
        inOrderMeter.verifyNoMoreInteractions();
    }

    @Test
    public void testAddMissingMeters_withUpdate() throws Exception {
        Mockito.when(meterCommitter.add(Matchers.<InstanceIdentifier<Meter>>any(), meterCaptor.capture(),
                Matchers.same(NODE_IDENT)))
                .thenReturn(RpcResultBuilder.success(new AddMeterOutputBuilder().build()).buildFuture());

        Mockito.when(meterCommitter.update(Matchers.<InstanceIdentifier<Meter>>any(),
                meterUpdateCaptor.capture(), meterUpdateCaptor.capture(), Matchers.same(NODE_IDENT)))
                .thenReturn(RpcResultBuilder.success(new UpdateMeterOutputBuilder().build()).buildFuture());

        final ItemSyncBox<Meter> meterSyncBox = new ItemSyncBox<>();
        meterSyncBox.getItemsToPush().add(createMeter(2L));
        meterSyncBox.getItemsToPush().add(createMeter(4L));
        meterSyncBox.getItemsToUpdate().add(new ItemSyncBox.ItemUpdateTuple<>(
                createMeter(1L), createMeterWithBody(1L)));

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
        inOrderMeters.verify(meterCommitter, Mockito.times(2)).add(Matchers.<InstanceIdentifier<Meter>>any(),
                Matchers.<Meter>any(), Matchers.eq(NODE_IDENT));
        inOrderMeters.verify(meterCommitter).update(Matchers.<InstanceIdentifier<Meter>>any(),
                Matchers.<Meter>any(), Matchers.<Meter>any(), Matchers.eq(NODE_IDENT));
        //TODO: uncomment when enabled in impl
//        inOrderMeters.verify(flowCapableTxService).sendBarrier(Matchers.<SendBarrierInput>any());

        inOrderMeters.verifyNoMoreInteractions();
    }

    @Test
    public void testRemoveRedundantMeters() throws Exception {
        Mockito.when(meterCommitter.remove(Matchers.<InstanceIdentifier<Meter>>any(), meterCaptor.capture(),
                Matchers.same(NODE_IDENT)))
                .thenReturn(RpcResultBuilder.success(new RemoveMeterOutputBuilder().build()).buildFuture());

        final ItemSyncBox<Meter> meterSyncBox = new ItemSyncBox<>();
        meterSyncBox.getItemsToPush().add(createMeter(2L));
        meterSyncBox.getItemsToPush().add(createMeter(4L));
        meterSyncBox.getItemsToUpdate().add(new ItemSyncBox.ItemUpdateTuple<>(
                createMeter(1L), createMeterWithBody(1L)));

        final ListenableFuture<RpcResult<Void>> result = syncPlanPushStrategy.removeRedundantMeters(
                NODE_ID, NODE_IDENT, meterSyncBox, counters);

        Assert.assertTrue(result.isDone());
        Assert.assertTrue(result.get().isSuccessful());

        final List<Meter> metercaptorAllValues = meterCaptor.getAllValues();
        Assert.assertEquals(2, metercaptorAllValues.size());
        Assert.assertEquals(2L, metercaptorAllValues.get(0).getMeterId().getValue().longValue());
        Assert.assertEquals(4L, metercaptorAllValues.get(1).getMeterId().getValue().longValue());

        final InOrder inOrderMeter = Mockito.inOrder(flowCapableTxService, meterCommitter);
        inOrderMeter.verify(meterCommitter, Mockito.times(2)).remove(Matchers.<InstanceIdentifier<Meter>>any(),
                Matchers.<Meter>any(), Matchers.eq(NODE_IDENT));
        //TODO: uncomment when enabled in impl
//        inOrderMeter.verify(flowCapableTxService).sendBarrier(Matchers.<SendBarrierInput>any());
        inOrderMeter.verifyNoMoreInteractions();
    }

    @Test
    public void testAddMissingGroups() throws Exception {
        Mockito.when(groupCommitter.add(Matchers.<InstanceIdentifier<Group>>any(), groupCaptor.capture(),
                Matchers.same(NODE_IDENT)))
                .thenReturn(RpcResultBuilder.success(new AddGroupOutputBuilder().build()).buildFuture());

        ItemSyncBox<Group> groupBox1 = new ItemSyncBox<>();
        groupBox1.getItemsToPush().add(createGroup(2L));

        ItemSyncBox<Group> groupBox2 = new ItemSyncBox<>();
        groupBox2.getItemsToPush().add(createGroupWithPreconditions(3L, 2L));
        groupBox2.getItemsToPush().add(createGroupWithPreconditions(4L, 2L));

        ItemSyncBox<Group> groupBox3 = new ItemSyncBox<>();
        groupBox3.getItemsToPush().add(createGroupWithPreconditions(5L, 3L, 4L));

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
        inOrderGroups.verify(groupCommitter).add(Matchers.<InstanceIdentifier<Group>>any(),
                Matchers.<Group>any(), Matchers.eq(NODE_IDENT));
        inOrderGroups.verify(flowCapableTxService).sendBarrier(Matchers.<SendBarrierInput>any());
        // add 3, 4
        inOrderGroups.verify(groupCommitter, Mockito.times(2)).add(Matchers.<InstanceIdentifier<Group>>any(),
                Matchers.<Group>any(), Matchers.eq(NODE_IDENT));
        inOrderGroups.verify(flowCapableTxService).sendBarrier(Matchers.<SendBarrierInput>any());
        // add 5
        inOrderGroups.verify(groupCommitter).add(Matchers.<InstanceIdentifier<Group>>any(),
                Matchers.<Group>any(), Matchers.eq(NODE_IDENT));
        inOrderGroups.verify(flowCapableTxService).sendBarrier(Matchers.<SendBarrierInput>any());

        inOrderGroups.verifyNoMoreInteractions();
    }

    @Test
    public void testAddMissingGroups_withUpdate() throws Exception {
        Mockito.when(groupCommitter.add(Matchers.<InstanceIdentifier<Group>>any(), groupCaptor.capture(),
                Matchers.same(NODE_IDENT)))
                .thenReturn(RpcResultBuilder.success(new AddGroupOutputBuilder().build()).buildFuture());

        Mockito.when(groupCommitter.update(Matchers.<InstanceIdentifier<Group>>any(),
                groupUpdateCaptor.capture(), groupUpdateCaptor.capture(),
                Matchers.same(NODE_IDENT)))
                .thenReturn(RpcResultBuilder.success(new UpdateGroupOutputBuilder().build()).buildFuture());

        ItemSyncBox<Group> groupBox1 = new ItemSyncBox<>();
        groupBox1.getItemsToPush().add(createGroup(2L));
        groupBox1.getItemsToUpdate().add(new ItemSyncBox.ItemUpdateTuple<>(
                createGroup(1L), createGroupWithAction(1L)));

        ItemSyncBox<Group> groupBox2 = new ItemSyncBox<>();
        groupBox2.getItemsToPush().add(createGroupWithPreconditions(3L, 2L));
        groupBox2.getItemsToPush().add(createGroupWithPreconditions(4L, 2L));

        ItemSyncBox<Group> groupBox3 = new ItemSyncBox<>();
        groupBox3.getItemsToPush().add(createGroupWithPreconditions(5L, 3L, 4L));

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
        inOrderGroups.verify(groupCommitter).add(Matchers.<InstanceIdentifier<Group>>any(),
                Matchers.<Group>any(), Matchers.eq(NODE_IDENT));
        inOrderGroups.verify(groupCommitter).update(Matchers.<InstanceIdentifier<Group>>any(),
                Matchers.<Group>any(), Matchers.<Group>any(), Matchers.eq(NODE_IDENT));
        inOrderGroups.verify(flowCapableTxService).sendBarrier(Matchers.<SendBarrierInput>any());

        // add 3, 4
        inOrderGroups.verify(groupCommitter, Mockito.times(2)).add(Matchers.<InstanceIdentifier<Group>>any(),
                Matchers.<Group>any(), Matchers.eq(NODE_IDENT));
        inOrderGroups.verify(flowCapableTxService).sendBarrier(Matchers.<SendBarrierInput>any());
        // add 5
        inOrderGroups.verify(groupCommitter).add(Matchers.<InstanceIdentifier<Group>>any(),
                Matchers.<Group>any(), Matchers.eq(NODE_IDENT));
        inOrderGroups.verify(flowCapableTxService).sendBarrier(Matchers.<SendBarrierInput>any());

        inOrderGroups.verifyNoMoreInteractions();
    }

    @Test
    public void testRemoveRedundantGroups() throws Exception {
        Mockito.when(groupCommitter.remove(Matchers.<InstanceIdentifier<Group>>any(), groupCaptor.capture(),
                Matchers.same(NODE_IDENT)))
                .thenReturn(RpcResultBuilder.success(new RemoveGroupOutputBuilder().build()).buildFuture());

        ItemSyncBox<Group> groupBox1 = new ItemSyncBox<>();
        groupBox1.getItemsToPush().add(createGroup(2L));
        groupBox1.getItemsToUpdate().add(new ItemSyncBox.ItemUpdateTuple<>(
                createGroup(1L), createGroupWithAction(1L)));

        ItemSyncBox<Group> groupBox2 = new ItemSyncBox<>();
        groupBox2.getItemsToPush().add(createGroupWithPreconditions(3L, 2L));
        groupBox2.getItemsToPush().add(createGroupWithPreconditions(4L, 2L));

        ItemSyncBox<Group> groupBox3 = new ItemSyncBox<>();
        groupBox3.getItemsToPush().add(createGroupWithPreconditions(5L, 3L, 4L));

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
        inOrderGroup.verify(groupCommitter).remove(Matchers.<InstanceIdentifier<Group>>any(),
                Matchers.<Group>any(), Matchers.eq(NODE_IDENT));
        inOrderGroup.verify(flowCapableTxService).sendBarrier(Matchers.<SendBarrierInput>any());
        // remove 3, 4
        inOrderGroup.verify(groupCommitter, Mockito.times(2)).remove(Matchers.<InstanceIdentifier<Group>>any(),
                Matchers.<Group>any(), Matchers.eq(NODE_IDENT));
        inOrderGroup.verify(flowCapableTxService).sendBarrier(Matchers.<SendBarrierInput>any());
        // remove 2
        inOrderGroup.verify(groupCommitter).remove(Matchers.<InstanceIdentifier<Group>>any(),
                Matchers.<Group>any(), Matchers.eq(NODE_IDENT));
        inOrderGroup.verify(flowCapableTxService).sendBarrier(Matchers.<SendBarrierInput>any());

        inOrderGroup.verifyNoMoreInteractions();
    }

    @Test
    public void testUpdateTableFeatures() throws Exception {
        Mockito.when(tableCommitter.update(Matchers.<InstanceIdentifier<TableFeatures>>any(),
                Matchers.isNull(TableFeatures.class), tableFeaturesCaptor.capture(),
                Matchers.same(NODE_IDENT)))
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

        Mockito.verify(flowCapableTxService).sendBarrier(Matchers.<SendBarrierInput>any());
    }
}