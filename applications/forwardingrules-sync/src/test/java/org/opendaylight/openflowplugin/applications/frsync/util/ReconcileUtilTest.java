/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.applications.frsync.util;

import com.google.common.base.Splitter;
import com.google.common.collect.Maps;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.MoreExecutors;
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
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.GroupActionCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.group.action._case.GroupActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.list.Action;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.list.ActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.list.ActionKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.transaction.rev150304.FlowCapableTransactionService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.transaction.rev150304.SendBarrierInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.transaction.rev150304.SendBarrierOutput;
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
import org.opendaylight.yangtools.yang.common.Uint32;

/**
 * Test for {@link ReconcileUtil}.
 */
@RunWith(MockitoJUnitRunner.class)
public class ReconcileUtilTest {

    private static final NodeId NODE_ID = new NodeId("unit-node-id");
    private static final InstanceIdentifier<Node> NODE_IDENT = InstanceIdentifier.create(Nodes.class)
            .child(Node.class, new NodeKey(NODE_ID));
    private static final Splitter COMMA_SPLITTER = Splitter.on(",");

    @Mock
    private FlowCapableTransactionService flowCapableService;
    @Captor
    private ArgumentCaptor<SendBarrierInput> barrierInputCaptor;

    @Test
    public void testChainBarrierFlush() throws Exception {
        SettableFuture<RpcResult<Void>> testRabbit = SettableFuture.create();
        final ListenableFuture<RpcResult<Void>> vehicle =
                Futures.transformAsync(testRabbit, ReconcileUtil.chainBarrierFlush(NODE_IDENT, flowCapableService),
                        MoreExecutors.directExecutor());
        Mockito.when(flowCapableService.sendBarrier(barrierInputCaptor.capture()))
                .thenReturn(RpcResultBuilder.<SendBarrierOutput>success().buildFuture());

        Mockito.verify(flowCapableService, Mockito.never()).sendBarrier(ArgumentMatchers.any());
        Assert.assertFalse(vehicle.isDone());

        testRabbit.set(RpcResultBuilder.<Void>success().build());
        Mockito.verify(flowCapableService).sendBarrier(ArgumentMatchers.any());
        Assert.assertTrue(vehicle.isDone());
        Assert.assertTrue(vehicle.get().isSuccessful());
    }

    /**
     * add one missing group.
     */
    @Test
    public void testResolveAndDivideGroupDiffs1() {
        final Map<Uint32, Group> installedGroups = createGroups(1, 2, 3);

        final List<Group> pendingGroups = new ArrayList<>();
        pendingGroups.add(createGroup(2L));
        pendingGroups.add(createGroup(3L));
        pendingGroups.add(createGroup(4L));

        final List<ItemSyncBox<Group>> plan = ReconcileUtil.resolveAndDivideGroupDiffs(
                NODE_ID, installedGroups, pendingGroups);

        Assert.assertEquals(1, plan.size());

        Assert.assertEquals(1, plan.get(0).getItemsToPush().size());
        Assert.assertEquals(4L, plan.get(0).getItemsToPush().iterator().next().key()
                .getGroupId().getValue().longValue());
        Assert.assertEquals(0, plan.get(0).getItemsToUpdate().size());
    }

    /**
     * add 3 groups with dependencies - 3 steps involved.
     */
    @Test
    public void testResolveAndDivideGroupDiffs2() {
        final Map<Uint32, Group> installedGroups = createGroups(1);

        final List<Group> pendingGroups = new ArrayList<>();
        pendingGroups.add(createGroup(2L));
        pendingGroups.add(createGroupWithPreconditions(3L, 2L, 4L));
        pendingGroups.add(createGroupWithPreconditions(4L, 2L));

        final List<ItemSyncBox<Group>> plan = ReconcileUtil.resolveAndDivideGroupDiffs(
                NODE_ID, installedGroups, pendingGroups);

        Assert.assertEquals(3, plan.size());

        Assert.assertEquals(1, plan.get(0).getItemsToPush().size());
        Assert.assertEquals(2L, plan.get(0).getItemsToPush().iterator().next().key()
                .getGroupId().getValue().longValue());
        Assert.assertEquals(0, plan.get(0).getItemsToUpdate().size());

        Assert.assertEquals(1, plan.get(1).getItemsToPush().size());
        Assert.assertEquals(4L, plan.get(1).getItemsToPush().iterator().next().key()
                .getGroupId().getValue().longValue());
        Assert.assertEquals(0, plan.get(1).getItemsToUpdate().size());

        Assert.assertEquals(1, plan.get(2).getItemsToPush().size());
        Assert.assertEquals(3L, plan.get(2).getItemsToPush().iterator().next().key()
                .getGroupId().getValue().longValue());
        Assert.assertEquals(0, plan.get(2).getItemsToUpdate().size());
    }

    /**
     * no actions taken - installed and pending groups are the same.
     */
    @Test
    public void testResolveAndDivideGroupDiffs3() {
        final Map<Uint32, Group> installedGroups = new HashMap<>();
        installedGroups.put(Uint32.ONE, createGroup(1L));
        installedGroups.put(Uint32.valueOf(2), createGroupWithPreconditions(2L, 1L));

        final List<Group> pendingGroups = new ArrayList<>();
        pendingGroups.add(createGroup(1L));
        pendingGroups.add(createGroupWithPreconditions(2L, 1L));

        final List<ItemSyncBox<Group>> plan = ReconcileUtil.resolveAndDivideGroupDiffs(
                NODE_ID, installedGroups, pendingGroups);

        Assert.assertEquals(0, plan.size());
    }

    /**
     * update 1 group.
     */
    @Test
    public void testResolveAndDivideGroupDiffs4() {
        final Map<Uint32, Group> installedGroups = createGroups(1, 2);

        final List<Group> pendingGroups = new ArrayList<>();
        pendingGroups.add(createGroupWithPreconditions(1L, 2L));
        pendingGroups.add(createGroup(2L));

        final List<ItemSyncBox<Group>> plan = ReconcileUtil.resolveAndDivideGroupDiffs(
                NODE_ID, installedGroups, pendingGroups);

        Assert.assertEquals(1, plan.size());
        Assert.assertEquals(0, plan.get(0).getItemsToPush().size());
        Assert.assertEquals(1, plan.get(0).getItemsToUpdate().size());
        final ItemSyncBox.ItemUpdateTuple<Group> firstItemUpdateTuple =
                plan.get(0).getItemsToUpdate().iterator().next();
        Assert.assertEquals(1L, firstItemUpdateTuple.getOriginal().getGroupId().getValue().longValue());
        Assert.assertEquals(1L, firstItemUpdateTuple.getUpdated().getGroupId().getValue().longValue());
    }

    /**
     * no action taken - update 1 group will be ignored.
     */
    @Test
    public void testResolveAndDivideGroupDiffs5() {
        final Map<Uint32, Group> installedGroups = createGroups(1, 2);

        final List<Group> pendingGroups = new ArrayList<>();
        pendingGroups.add(createGroupWithPreconditions(1L, 2L));
        pendingGroups.add(createGroup(2L));

        final List<ItemSyncBox<Group>> plan = ReconcileUtil.resolveAndDivideGroupDiffs(
                NODE_ID, installedGroups, pendingGroups, false);

        Assert.assertEquals(0, plan.size());
    }

    /**
     * should add 1 group but preconditions are not met.
     */
    @Test
    public void testResolveAndDivideGroupDiffs_negative1() {
        final Map<Uint32, Group> installedGroups = createGroups(1, 2);

        final List<Group> pendingGroups = new ArrayList<>();
        pendingGroups.add(createGroupWithPreconditions(3L, 4L));

        Assert.assertThrows(IllegalStateException.class,
            () -> ReconcileUtil.resolveAndDivideGroupDiffs(NODE_ID, installedGroups, pendingGroups));
    }

    /**
     * should update 1 group but preconditions are not met.
     */
    @Test
    public void testResolveAndDivideGroupDiffs_negative2() {
        final Map<Uint32, Group> installedGroups = createGroups(1, 2);

        final List<Group> pendingGroups = new ArrayList<>();
        pendingGroups.add(createGroupWithPreconditions(1L, 3L));

        Assert.assertThrows(IllegalStateException.class,
            () -> ReconcileUtil.resolveAndDivideGroupDiffs(NODE_ID, installedGroups, pendingGroups));
    }

    @Test
    public void testCheckGroupPrecondition() {
        final Set<Uint32> installedGroups = new HashSet<>(Arrays.asList(Uint32.ONE, Uint32.valueOf(2)));

        final Group pendingGroup1 = createGroupWithPreconditions(3L, 2L, 4L);
        Assert.assertFalse(ReconcileUtil.checkGroupPrecondition(installedGroups, pendingGroup1));

        final Group pendingGroup2 = createGroupWithPreconditions(1L, 2L);
        Assert.assertTrue(ReconcileUtil.checkGroupPrecondition(installedGroups, pendingGroup2));

        final Group pendingGroup3 = createGroupWithPreconditions(1L);
        Assert.assertTrue(ReconcileUtil.checkGroupPrecondition(installedGroups, pendingGroup3));
    }

    private static Group createGroupWithPreconditions(final long groupIdValue, final long... requiredId) {
        final List<Action> actionBag = new ArrayList<>();
        int key = 0;
        for (long groupIdPrecondition : requiredId) {
            actionBag.add(new ActionBuilder()
                .setAction(new GroupActionCaseBuilder()
                    .setGroupAction(new GroupActionBuilder()
                        .setGroupId(Uint32.valueOf(groupIdPrecondition))
                        .build())
                    .build())
                .withKey(new ActionKey(key++))
                .build());
        }

        final Bucket bucket = new BucketBuilder().setAction(actionBag).build();
        final Buckets buckets = new BucketsBuilder()
                .setBucket(Collections.singletonMap(bucket.key(), bucket))
                .build();

        return new GroupBuilder()
                .setGroupId(new GroupId(Uint32.valueOf(groupIdValue)))
                .setBuckets(buckets)
                .build();
    }

    private static Map<Uint32, Group> createGroups(long... groupIds) {
        final Map<Uint32, Group> ret = Maps.newHashMapWithExpectedSize(groupIds.length);
        for (long groupId : groupIds) {
            ret.put(Uint32.valueOf(groupId), createGroup(groupId));
        }
        return ret;
    }

    private static Group createGroup(final long groupIdValue) {
        return new GroupBuilder()
                .setGroupId(new GroupId(Uint32.valueOf(groupIdValue)))
                .setBuckets(new BucketsBuilder().build())
                .build();
    }

    /**
     * covers {@link ReconcileUtil#countTotalUpdated(Iterable)} too.
     */
    @Test
    public void testCountTotalAdds() {
        List<ItemSyncBox<String>> syncPlan = new ArrayList<>();
        ItemSyncBox<String> syncBox1 = createSyncBox("a,b", "x,y,z");
        syncPlan.add(syncBox1);
        syncPlan.add(syncBox1);
        Assert.assertEquals(4, ReconcileUtil.countTotalPushed(syncPlan));
        Assert.assertEquals(6, ReconcileUtil.countTotalUpdated(syncPlan));
    }

    private ItemSyncBox<String> createSyncBox(final String pushes, final String updates) {
        ItemSyncBox<String> syncBox1 = new ItemSyncBox<>();
        syncBox1.getItemsToPush().addAll(COMMA_SPLITTER.splitToList(pushes));
        for (String orig : COMMA_SPLITTER.splitToList(updates)) {
            syncBox1.getItemsToUpdate().add(new ItemSyncBox.ItemUpdateTuple<>(orig, orig + "_updated"));
        }
        return syncBox1;
    }
}
