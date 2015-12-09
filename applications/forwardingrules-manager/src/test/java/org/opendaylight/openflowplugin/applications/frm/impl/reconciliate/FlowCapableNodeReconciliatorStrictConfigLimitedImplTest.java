/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.applications.frm.impl.reconciliate;

import com.google.common.util.concurrent.ListenableFuture;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.openflowplugin.applications.frm.ForwardingRulesCommiter;
import org.opendaylight.openflowplugin.applications.frm.ForwardingRulesManager;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.GroupActionCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.GroupActionCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.group.action._case.GroupAction;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.group.action._case.GroupActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.list.Action;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.list.ActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNode;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNodeBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.meters.Meter;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.meters.MeterBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.Table;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.TableBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.table.Flow;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.table.FlowBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.AddFlowOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.AddFlowOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.RemoveFlowOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.RemoveFlowOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.UpdateFlowOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.transaction.rev150304.FlowCapableTransactionService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.transaction.rev150304.SendBarrierInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.service.rev130918.AddGroupOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.service.rev130918.AddGroupOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.service.rev130918.RemoveGroupOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.service.rev130918.RemoveGroupOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.service.rev130918.UpdateGroupOutput;
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
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.service.rev130918.AddMeterOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.service.rev130918.AddMeterOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.service.rev130918.RemoveMeterOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.service.rev130918.RemoveMeterOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.service.rev130918.UpdateMeterOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.MeterId;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.opendaylight.yangtools.yang.common.RpcResultBuilder;

/**
 * Test for {@link FlowCapableNodeReconciliatorStrictConfigLimitedImpl}.
 */
@RunWith(MockitoJUnitRunner.class)
public class FlowCapableNodeReconciliatorStrictConfigLimitedImplTest {

    private static final NodeId NODE_ID = new NodeId("unit-nodeId");
    private static final InstanceIdentifier<FlowCapableNode> NODE_IDENT = InstanceIdentifier.create(Nodes.class)
            .child(Node.class, new NodeKey(NODE_ID)).augmentation(FlowCapableNode.class);

    private FlowCapableNodeReconciliatorStrictConfigLimitedImpl reconciliator;
    @Mock
    private ForwardingRulesManager frm;
    @Mock
    private DataBroker db;
    @Mock
    private ForwardingRulesCommiter<Group, AddGroupOutput, RemoveGroupOutput, UpdateGroupOutput> groupCommitter;
    @Mock
    private ForwardingRulesCommiter<Flow, AddFlowOutput, RemoveFlowOutput, UpdateFlowOutput> flowCommitter;
    @Mock
    private ForwardingRulesCommiter<Meter, AddMeterOutput, RemoveMeterOutput, UpdateMeterOutput> meterCommitter;
    @Mock
    private FlowCapableTransactionService flowCapableTxService;

    @Captor
    private ArgumentCaptor<Group> groupCaptor;
    @Captor
    private ArgumentCaptor<Flow> flowCaptor;
    @Captor
    private ArgumentCaptor<Meter> meterCaptor;

    @Before

    public void setUp() throws Exception {
        Mockito.when(flowCapableTxService.sendBarrier(Matchers.<SendBarrierInput>any()))
                .thenReturn(RpcResultBuilder.success((Void) null).buildFuture());
        Mockito.when(frm.getFlowCapableTransactionService()).thenReturn(flowCapableTxService);
        reconciliator = new FlowCapableNodeReconciliatorStrictConfigLimitedImpl(frm, db);
    }

    @Test
    public void testResolveAndDivideGroups() throws Exception {
        final Set<Long> installedGroups = new HashSet<>(Arrays.asList(new Long[]{1L, 2L, 3L}));
        final List<Group> pendingGroups = new ArrayList<>();
        pendingGroups.add(createGroup(2L));
        pendingGroups.add(createGroup(3L));
        pendingGroups.add(createGroup(4L));

        final List<Set<Group>> plan = FlowCapableNodeReconciliatorStrictConfigLimitedImpl.resolveAndDivideGroups(
                NODE_ID, installedGroups, pendingGroups);

        Assert.assertEquals(1, plan.size());
        Assert.assertEquals(1, plan.get(0).size());
        Assert.assertEquals(4L, plan.get(0).iterator().next().getKey().getGroupId().getValue().longValue());
    }

    @Test
    public void testResolveAndDivideGroups2() throws Exception {
        final Set<Long> installedGroups = new HashSet<>(Arrays.asList(new Long[]{1L}));
        final List<Group> pendingGroups = new ArrayList<>();
        pendingGroups.add(createGroup(2L));
        pendingGroups.add(createGroupWithPreconditions(3L, 2L, 4L));
        pendingGroups.add(createGroupWithPreconditions(4L, 2L));

        final List<Set<Group>> plan = FlowCapableNodeReconciliatorStrictConfigLimitedImpl.resolveAndDivideGroups(
                NODE_ID, installedGroups, pendingGroups);

        Assert.assertEquals(3, plan.size());
        Assert.assertEquals(1, plan.get(0).size());
        Assert.assertEquals(2L, plan.get(0).iterator().next().getKey().getGroupId().getValue().longValue());
        Assert.assertEquals(1, plan.get(1).size());
        Assert.assertEquals(4L, plan.get(1).iterator().next().getKey().getGroupId().getValue().longValue());
        Assert.assertEquals(1, plan.get(2).size());
        Assert.assertEquals(3L, plan.get(2).iterator().next().getKey().getGroupId().getValue().longValue());
    }

    @Test
    public void testResolveAndDivideGroups3() throws Exception {
        final Set<Long> installedGroups = new HashSet<>(Arrays.asList(new Long[]{1L, 2L}));
        final List<Group> pendingGroups = new ArrayList<>();
        pendingGroups.add(createGroup(2L));
        pendingGroups.add(createGroupWithPreconditions(3L, 2L, 4L));
        pendingGroups.add(createGroupWithPreconditions(4L, 2L));

        final List<Set<Group>> plan = FlowCapableNodeReconciliatorStrictConfigLimitedImpl.resolveAndDivideGroups(
                NODE_ID, installedGroups, pendingGroups);

        Assert.assertEquals(2, plan.size());
        Assert.assertEquals(1, plan.get(0).size());
        Assert.assertEquals(4L, plan.get(0).iterator().next().getKey().getGroupId().getValue().longValue());
        Assert.assertEquals(1, plan.get(1).size());
        Assert.assertEquals(3L, plan.get(1).iterator().next().getKey().getGroupId().getValue().longValue());
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

    private Flow createFlow(final String flowIdValue) {
        return new FlowBuilder()
                .setId(new FlowId(flowIdValue))
                .build();
    }

    private Meter createMeter(final Long meterIdValue) {
        return new MeterBuilder()
                .setMeterId(new MeterId(meterIdValue))
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
    public void testCheckGroupPrecondition() throws Exception {
        final Set<Long> installedGroups = new HashSet<>(Arrays.asList(new Long[]{1L, 2L}));

        final Group pendingGroup1 = createGroupWithPreconditions(3L, 2L, 4L);
        Assert.assertFalse(FlowCapableNodeReconciliatorStrictConfigLimitedImpl.checkGroupPrecondition(installedGroups, pendingGroup1));

        final Group pendingGroup2 = createGroupWithPreconditions(1L, 2L);
        Assert.assertTrue(FlowCapableNodeReconciliatorStrictConfigLimitedImpl.checkGroupPrecondition(installedGroups, pendingGroup2));

        final Group pendingGroup3 = createGroupWithPreconditions(1L);
        Assert.assertTrue(FlowCapableNodeReconciliatorStrictConfigLimitedImpl.checkGroupPrecondition(installedGroups, pendingGroup3));
    }

    @Test
    public void testRemoveRedundantFlows() throws Exception {
        Mockito.when(flowCommitter.remove(Matchers.<InstanceIdentifier<Flow>>any(), flowCaptor.capture(),
                Matchers.same(NODE_IDENT)))
                .thenReturn(RpcResultBuilder.success(new RemoveFlowOutputBuilder().build()).buildFuture());

        final Table tableCfg = new TableBuilder()
                .setId((short) 0)
                .setFlow(Arrays.asList(
                        createFlow("f1"), createFlow("f2")))
                .build();
        final FlowCapableNode config = new FlowCapableNodeBuilder()
                .setTable(Collections.singletonList(tableCfg))
                .build();

        final Table tableOpe = new TableBuilder()
                .setId((short) 0)
                .setFlow(Arrays.asList(
                        createFlow("f1"), createFlow("f2"), createFlow("f3"), createFlow("f4")))
                .build();
        final FlowCapableNode operational = new FlowCapableNodeBuilder()
                .setTable(Collections.singletonList(tableOpe))
                .build();

        final ListenableFuture<RpcResult<Void>> result = reconciliator.removeRedundantFlows(
                NODE_IDENT, flowCommitter, config, operational);

        Assert.assertTrue(result.isDone());
        Assert.assertTrue(result.get().isSuccessful());

        final List<Flow> flowCaptorAllValues = flowCaptor.getAllValues();
        Assert.assertEquals(2, flowCaptorAllValues.size());
        Assert.assertEquals("f3", flowCaptorAllValues.get(0).getId().getValue());
        Assert.assertEquals("f4", flowCaptorAllValues.get(1).getId().getValue());

        Mockito.verify(flowCapableTxService, Mockito.times(1)).sendBarrier(Matchers.<SendBarrierInput>any());
    }

    @Test
    public void testAddMissingFlows() throws Exception {
        Mockito.when(flowCommitter.add(Matchers.<InstanceIdentifier<Flow>>any(), flowCaptor.capture(),
                Matchers.same(NODE_IDENT)))
                .thenReturn(RpcResultBuilder.success(new AddFlowOutputBuilder().build()).buildFuture());

        final Table tableCfg = new TableBuilder()
                .setId((short) 0)
                .setFlow(Arrays.asList(
                        createFlow("f1"), createFlow("f2"), createFlow("f3"), createFlow("f4")))
                .build();
        final FlowCapableNode config = new FlowCapableNodeBuilder()
                .setTable(Collections.singletonList(tableCfg))
                .build();

        final Table tableOpe = new TableBuilder()
                .setId((short) 0)
                .setFlow(Arrays.asList(
                        createFlow("f1"), createFlow("f2")))
                .build();
        final FlowCapableNode operational = new FlowCapableNodeBuilder()
                .setTable(Collections.singletonList(tableOpe))
                .build();

        final ListenableFuture<RpcResult<Void>> result = reconciliator.addMissingFlows(
                NODE_IDENT, flowCommitter, config, operational);

        Assert.assertTrue(result.isDone());
        Assert.assertTrue(result.get().isSuccessful());

        final List<Flow> flowCaptorAllValues = flowCaptor.getAllValues();
        Assert.assertEquals(2, flowCaptorAllValues.size());
        Assert.assertEquals("f3", flowCaptorAllValues.get(0).getId().getValue());
        Assert.assertEquals("f4", flowCaptorAllValues.get(1).getId().getValue());

        Mockito.verify(flowCapableTxService, Mockito.times(1)).sendBarrier(Matchers.<SendBarrierInput>any());
    }

    @Test
    public void testAddMissingMeters() throws Exception {
        Mockito.when(meterCommitter.add(Matchers.<InstanceIdentifier<Meter>>any(), meterCaptor.capture(),
                Matchers.same(NODE_IDENT)))
                .thenReturn(RpcResultBuilder.success(new AddMeterOutputBuilder().build()).buildFuture());

        final FlowCapableNode config = new FlowCapableNodeBuilder()
                .setMeter(Arrays.asList(
                        createMeter(1L), createMeter(2L), createMeter(3L), createMeter(4L)
                ))
                .build();

        final FlowCapableNode operational = new FlowCapableNodeBuilder()
                .setMeter(Arrays.asList(
                        createMeter(1L), createMeter(3L)
                ))
                .build();

        final ListenableFuture<RpcResult<Void>> result = reconciliator.addMissingMeters(
                NODE_IDENT, meterCommitter, config, operational);

        Assert.assertTrue(result.isDone());
        Assert.assertTrue(result.get().isSuccessful());

        final List<Meter> metercaptorAllValues = meterCaptor.getAllValues();
        Assert.assertEquals(2, metercaptorAllValues.size());
        Assert.assertEquals(2L, metercaptorAllValues.get(0).getMeterId().getValue().longValue());
        Assert.assertEquals(4L, metercaptorAllValues.get(1).getMeterId().getValue().longValue());

        Mockito.verify(flowCapableTxService, Mockito.times(1)).sendBarrier(Matchers.<SendBarrierInput>any());
    }

    @Test
    public void testRemoveRedundantMeters() throws Exception {
        Mockito.when(meterCommitter.remove(Matchers.<InstanceIdentifier<Meter>>any(), meterCaptor.capture(),
                Matchers.same(NODE_IDENT)))
                .thenReturn(RpcResultBuilder.success(new RemoveMeterOutputBuilder().build()).buildFuture());

        final FlowCapableNode config = new FlowCapableNodeBuilder()
                .setMeter(Arrays.asList(
                        createMeter(1L), createMeter(3L)
                ))
                .build();

        final FlowCapableNode operational = new FlowCapableNodeBuilder()
                .setMeter(Arrays.asList(
                        createMeter(1L), createMeter(2L), createMeter(3L), createMeter(4L)
                ))
                .build();

        final ListenableFuture<RpcResult<Void>> result = reconciliator.removeRedundantMeters(
                NODE_IDENT, meterCommitter, config, operational);

        Assert.assertTrue(result.isDone());
        Assert.assertTrue(result.get().isSuccessful());

        final List<Meter> metercaptorAllValues = meterCaptor.getAllValues();
        Assert.assertEquals(2, metercaptorAllValues.size());
        Assert.assertEquals(2L, metercaptorAllValues.get(0).getMeterId().getValue().longValue());
        Assert.assertEquals(4L, metercaptorAllValues.get(1).getMeterId().getValue().longValue());

        Mockito.verify(flowCapableTxService, Mockito.times(1)).sendBarrier(Matchers.<SendBarrierInput>any());
    }

    @Test
    public void testAddMissingGroups() throws Exception {
        Mockito.when(groupCommitter.add(Matchers.<InstanceIdentifier<Group>>any(), groupCaptor.capture(),
                Matchers.same(NODE_IDENT)))
                .thenReturn(RpcResultBuilder.success(new AddGroupOutputBuilder().build()).buildFuture());

        final FlowCapableNode config = new FlowCapableNodeBuilder()
                .setGroup(Arrays.asList(
                        createGroup(1L), createGroup(2L),
                        createGroupWithPreconditions(3L, 2L),
                        createGroupWithPreconditions(4L, 2L),
                        createGroupWithPreconditions(5L, 3L, 4L)))
                .build();
        final FlowCapableNode operational = new FlowCapableNodeBuilder()
                .setGroup(Collections.singletonList(createGroup(1L)))
                .build();

        final ListenableFuture<RpcResult<Void>> result = reconciliator.addMissingGroups(
                NODE_IDENT, groupCommitter, config, operational);

        Assert.assertTrue(result.isDone());
        Assert.assertTrue(result.get().isSuccessful());

        final List<Group> groupCaptorAllValues = groupCaptor.getAllValues();
        Assert.assertEquals(4, groupCaptorAllValues.size());
        Assert.assertEquals(2L, groupCaptorAllValues.get(0).getGroupId().getValue().longValue());
        Assert.assertEquals(3L, groupCaptorAllValues.get(1).getGroupId().getValue().longValue());
        Assert.assertEquals(4L, groupCaptorAllValues.get(2).getGroupId().getValue().longValue());
        Assert.assertEquals(5L, groupCaptorAllValues.get(3).getGroupId().getValue().longValue());

        Mockito.verify(flowCapableTxService, Mockito.times(3)).sendBarrier(Matchers.<SendBarrierInput>any());
    }

    @Test
    public void testRemoveRedundantGroups() throws Exception {
        Mockito.when(groupCommitter.remove(Matchers.<InstanceIdentifier<Group>>any(), groupCaptor.capture(),
                Matchers.same(NODE_IDENT)))
                .thenReturn(RpcResultBuilder.success(new RemoveGroupOutputBuilder().build()).buildFuture());

        final FlowCapableNode operational = new FlowCapableNodeBuilder()
                .setGroup(new ArrayList<>(Arrays.asList(
                        createGroup(1L), createGroup(2L),
                        createGroupWithPreconditions(3L, 2L),
                        createGroupWithPreconditions(4L, 2L),
                        createGroupWithPreconditions(5L, 3L, 4L))))
                .build();
        final FlowCapableNode config = new FlowCapableNodeBuilder()
                .setGroup(Collections.singletonList(createGroup(1L)))
                .build();

        final ListenableFuture<RpcResult<Void>> result = reconciliator.removeRedundantGroups(
                NODE_IDENT, groupCommitter, config, operational);

        Assert.assertTrue(result.isDone());
        Assert.assertTrue(result.get().isSuccessful());

        final List<Group> groupCaptorAllValues = groupCaptor.getAllValues();
        Assert.assertEquals(4, groupCaptorAllValues.size());
        Assert.assertEquals(5L, groupCaptorAllValues.get(0).getGroupId().getValue().longValue());
        Assert.assertEquals(3L, groupCaptorAllValues.get(1).getGroupId().getValue().longValue());
        Assert.assertEquals(4L, groupCaptorAllValues.get(2).getGroupId().getValue().longValue());
        Assert.assertEquals(2L, groupCaptorAllValues.get(3).getGroupId().getValue().longValue());

        Mockito.verify(flowCapableTxService, Mockito.times(3)).sendBarrier(Matchers.<SendBarrierInput>any());
    }

}