/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.applications.frm.impl.reconciliate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.junit.Assert;
import org.junit.Test;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.GroupActionCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.GroupActionCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.group.action._case.GroupAction;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.group.action._case.GroupActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.list.Action;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.list.ActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.GroupId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.group.Buckets;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.group.BucketsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.group.buckets.Bucket;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.group.buckets.BucketBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.groups.Group;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.groups.GroupBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;

/**
 * Test for {@link FlowCapableNodeReconciliatorStrictConfigLimitedImpl}.
 */
public class FlowCapableNodeReconciliatorStrictConfigLimitedImplTest {

    private NodeId nodeId = new NodeId("unit-nodeId");

    @Test
    public void testResolveAndDivideGroups() throws Exception {
        final Set<Long> installedGroups = new HashSet<>(Arrays.asList(new Long[]{1L, 2L, 3L}));
        final List<Group> pendingGroups = new ArrayList<>();
        pendingGroups.add(createGroup(2L));
        pendingGroups.add(createGroup(3L));
        pendingGroups.add(createGroup(4L));

        final List<Set<Group>> plan = FlowCapableNodeReconciliatorStrictConfigLimitedImpl.resolveAndDivideGroups(
                nodeId, installedGroups, pendingGroups);

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
                nodeId, installedGroups, pendingGroups);

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
                nodeId, installedGroups, pendingGroups);

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
}