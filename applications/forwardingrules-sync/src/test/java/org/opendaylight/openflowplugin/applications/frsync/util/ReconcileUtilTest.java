/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.applications.frsync.util;

import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.SettableFuture;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.GroupActionCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.GroupActionCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.group.action._case.GroupAction;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.group.action._case.GroupActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.list.Action;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.list.ActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.transaction.rev150304.FlowCapableTransactionService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.transaction.rev150304.SendBarrierInput;
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
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.opendaylight.yangtools.yang.common.RpcResultBuilder;

/**
 * Test for {@link ReconcileUtil}.
 */
@RunWith(MockitoJUnitRunner.class)
public class ReconcileUtilTest {

    private static final NodeId NODE_ID = new NodeId("unit-node-id");
    private InstanceIdentifier<Node> NODE_IDENT = InstanceIdentifier.create(Nodes.class)
            .child(Node.class, new NodeKey(NODE_ID));

    @Rule
    public ExpectedException thrown = ExpectedException.none();
    @Mock
    private FlowCapableTransactionService flowCapableService;
    @Captor
    private ArgumentCaptor<SendBarrierInput> barrierInputCaptor;

    @Test
    public void testChainBarrierFlush() throws Exception {
        SettableFuture<RpcResult<Void>> testRabbit = SettableFuture.create();
        final ListenableFuture<RpcResult<Void>> vehicle =
                Futures.transform(testRabbit, ReconcileUtil.chainBarrierFlush(NODE_IDENT, flowCapableService));
        Mockito.when(flowCapableService.sendBarrier(barrierInputCaptor.capture()))
                .thenReturn(RpcResultBuilder.<Void>success().buildFuture());

        Mockito.verify(flowCapableService, Mockito.never()).sendBarrier(Matchers.<SendBarrierInput>any());
        Assert.assertFalse(vehicle.isDone());

        testRabbit.set(RpcResultBuilder.<Void>success().build());
        Mockito.verify(flowCapableService).sendBarrier(Matchers.<SendBarrierInput>any());
        Assert.assertTrue(vehicle.isDone());
        Assert.assertTrue(vehicle.get().isSuccessful());
    }

    @Test
    public void testCreateRpcResultCondenser() throws Exception {

    }

    /**
     * add one missing group
     *
     * @throws Exception
     */
    @Test
    public void testResolveAndDivideGroups1() throws Exception {
        final Map<Long, Group> installedGroups = new HashMap<>();
        installedGroups.put(1L, createGroup(1L));
        installedGroups.put(2L, createGroup(2L));
        installedGroups.put(3L, createGroup(3L));

        final List<Group> pendingGroups = new ArrayList<>();
        pendingGroups.add(createGroup(2L));
        pendingGroups.add(createGroup(3L));
        pendingGroups.add(createGroup(4L));

        final List<ItemSyncBox<Group>> plan = ReconcileUtil.resolveAndDivideGroups(
                NODE_ID, installedGroups, pendingGroups);

        Assert.assertEquals(1, plan.size());

        Assert.assertEquals(1, plan.get(0).getItemsToAdd().size());
        Assert.assertEquals(4L, plan.get(0).getItemsToAdd().iterator().next().getKey().getGroupId().getValue().longValue());
        Assert.assertEquals(0, plan.get(0).getItemsToUpdate().size());
    }

    /**
     * add 3 groups with dependencies - 3 steps involved
     *
     * @throws Exception
     */
    @Test
    public void testResolveAndDivideGroups2() throws Exception {
        final Map<Long, Group> installedGroups = new HashMap<>();
        installedGroups.put(1L, createGroup(1L));

        final List<Group> pendingGroups = new ArrayList<>();
        pendingGroups.add(createGroup(2L));
        pendingGroups.add(createGroupWithPreconditions(3L, 2L, 4L));
        pendingGroups.add(createGroupWithPreconditions(4L, 2L));

        final List<ItemSyncBox<Group>> plan = ReconcileUtil.resolveAndDivideGroups(
                NODE_ID, installedGroups, pendingGroups);

        Assert.assertEquals(3, plan.size());

        Assert.assertEquals(1, plan.get(0).getItemsToAdd().size());
        Assert.assertEquals(2L, plan.get(0).getItemsToAdd().iterator().next().getKey().getGroupId().getValue().longValue());
        Assert.assertEquals(0, plan.get(0).getItemsToUpdate().size());

        Assert.assertEquals(1, plan.get(1).getItemsToAdd().size());
        Assert.assertEquals(4L, plan.get(1).getItemsToAdd().iterator().next().getKey().getGroupId().getValue().longValue());
        Assert.assertEquals(0, plan.get(1).getItemsToUpdate().size());

        Assert.assertEquals(1, plan.get(2).getItemsToAdd().size());
        Assert.assertEquals(3L, plan.get(2).getItemsToAdd().iterator().next().getKey().getGroupId().getValue().longValue());
        Assert.assertEquals(0, plan.get(2).getItemsToUpdate().size());
    }

    /**
     * no actions taken - installed and pending groups are the same
     *
     * @throws Exception
     */
    @Test
    public void testResolveAndDivideGroups3() throws Exception {
        final Map<Long, Group> installedGroups = new HashMap<>();
        installedGroups.put(1L, createGroup(1L));
        installedGroups.put(2L, createGroupWithPreconditions(2L, 1L));

        final List<Group> pendingGroups = new ArrayList<>();
        pendingGroups.add(createGroup(1L));
        pendingGroups.add(createGroupWithPreconditions(2L, 1L));

        final List<ItemSyncBox<Group>> plan = ReconcileUtil.resolveAndDivideGroups(
                NODE_ID, installedGroups, pendingGroups);

        Assert.assertEquals(0, plan.size());
    }

    /**
     * update 1 group
     *
     * @throws Exception
     */
    @Test
    public void testResolveAndDivideGroups4() throws Exception {
        final Map<Long, Group> installedGroups = new HashMap<>();
        installedGroups.put(1L, createGroup(1L));
        installedGroups.put(2L, createGroup(2L));

        final List<Group> pendingGroups = new ArrayList<>();
        pendingGroups.add(createGroupWithPreconditions(1L, 2L));
        pendingGroups.add(createGroup(2L));

        final List<ItemSyncBox<Group>> plan = ReconcileUtil.resolveAndDivideGroups(
                NODE_ID, installedGroups, pendingGroups);

        Assert.assertEquals(1, plan.size());
        Assert.assertEquals(0, plan.get(0).getItemsToAdd().size());
        Assert.assertEquals(1, plan.get(0).getItemsToUpdate().size());
        final ItemSyncBox.ItemUpdateTuple<Group> firstItemUpdateTuple = plan.get(0).getItemsToUpdate().iterator().next();
        Assert.assertEquals(1L, firstItemUpdateTuple.getOriginal().getGroupId().getValue().longValue());
        Assert.assertEquals(1L, firstItemUpdateTuple.getUpdated().getGroupId().getValue().longValue());
    }

    /**
     * no action taken - update 1 group will be ignored
     *
     * @throws Exception
     */
    @Test
    public void testResolveAndDivideGroups5() throws Exception {
        final Map<Long, Group> installedGroups = new HashMap<>();
        installedGroups.put(1L, createGroup(1L));
        installedGroups.put(2L, createGroup(2L));

        final List<Group> pendingGroups = new ArrayList<>();
        pendingGroups.add(createGroupWithPreconditions(1L, 2L));
        pendingGroups.add(createGroup(2L));

        final List<ItemSyncBox<Group>> plan = ReconcileUtil.resolveAndDivideGroups(
                NODE_ID, installedGroups, pendingGroups, false);

        Assert.assertEquals(0, plan.size());
    }

    /**
     * should add 1 group but preconditions are not met
     *
     * @throws Exception
     */
    @Test
    public void testResolveAndDivideGroups_negative1() throws Exception {
        final Map<Long, Group> installedGroups = new HashMap<>();
        installedGroups.put(1L, createGroup(1L));
        installedGroups.put(2L, createGroup(2L));

        final List<Group> pendingGroups = new ArrayList<>();
        pendingGroups.add(createGroupWithPreconditions(3L, 4L));

        thrown.expect(IllegalStateException.class);
        final List<ItemSyncBox<Group>> plan = ReconcileUtil.resolveAndDivideGroups(
                NODE_ID, installedGroups, pendingGroups);
    }

    /**
     * should update 1 group but preconditions are not met
     *
     * @throws Exception
     */
    @Test
    public void testResolveAndDivideGroups_negative2() throws Exception {
        final Map<Long, Group> installedGroups = new HashMap<>();
        installedGroups.put(1L, createGroup(1L));
        installedGroups.put(2L, createGroup(2L));

        final List<Group> pendingGroups = new ArrayList<>();
        pendingGroups.add(createGroupWithPreconditions(1L, 3L));

        thrown.expect(IllegalStateException.class);
        final List<ItemSyncBox<Group>> plan = ReconcileUtil.resolveAndDivideGroups(
                NODE_ID, installedGroups, pendingGroups);
    }

    @Test
    public void testCheckGroupPrecondition() throws Exception {
        final Set<Long> installedGroups = new HashSet<>(Arrays.asList(new Long[]{1L, 2L}));

        final Group pendingGroup1 = createGroupWithPreconditions(3L, 2L, 4L);
        Assert.assertFalse(ReconcileUtil.checkGroupPrecondition(installedGroups, pendingGroup1));

        final Group pendingGroup2 = createGroupWithPreconditions(1L, 2L);
        Assert.assertTrue(ReconcileUtil.checkGroupPrecondition(installedGroups, pendingGroup2));

        final Group pendingGroup3 = createGroupWithPreconditions(1L);
        Assert.assertTrue(ReconcileUtil.checkGroupPrecondition(installedGroups, pendingGroup3));
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

    private Group createGroup(final long groupIdValue) {
        final Buckets buckets = new BucketsBuilder()
                .setBucket(Collections.<Bucket>emptyList())
                .build();
        return new GroupBuilder()
                .setGroupId(new GroupId(groupIdValue))
                .setBuckets(buckets)
                .build();
    }
}