/*
 * Copyright (c) 2014 Pantheon Technologies s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.openflow.md.core.sal.convertor;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.opendaylight.openflowplugin.api.openflow.md.util.OpenflowVersion;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.GroupTypes;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.group.buckets.Bucket;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.group.desc.stats.reply.GroupDescStats;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.actions.grouping.Action;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.actions.grouping.ActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.GroupId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.GroupType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.PortNumber;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.buckets.grouping.BucketsList;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.buckets.grouping.BucketsListBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.group.desc._case.multipart.reply.group.desc.GroupDesc;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.group.desc._case.multipart.reply.group.desc.GroupDescBuilder;

/**
 * @author michal.polkorab
 *
 */
public class GroupStatsResponseConvertorTest2 {

    GroupStatsResponseConvertor convertor =  new GroupStatsResponseConvertor();

    /**
     * Test empty GroupDescStats conversion
     */
    @Test
    public void test() {
        List<GroupDesc> groupDescStats = new ArrayList<>();

        List<GroupDescStats> statsList = convertor.toSALGroupDescStatsList(groupDescStats, OpenflowVersion.OF13);

        Assert.assertEquals("Wrong groupDesc stats size", 0, statsList.size());
    }

    /**
     * Test single GroupDescStat conversion without buckets
     */
    @Test
    public void testSingleGroupDescStat() {
        List<GroupDesc> groupDescStats = new ArrayList<>();
        GroupDescBuilder builder = new GroupDescBuilder();
        builder.setType(GroupType.OFPGTALL);
        builder.setGroupId(new GroupId(42L));
        builder.setBucketsList(new ArrayList<BucketsList>());
        groupDescStats.add(builder.build());

        List<GroupDescStats> statsList = convertor.toSALGroupDescStatsList(groupDescStats, OpenflowVersion.OF13);

        Assert.assertEquals("Wrong groupDesc stats size", 1, statsList.size());
        GroupDescStats stat = statsList.get(0);
        Assert.assertEquals("Wrong type", GroupTypes.GroupAll, stat.getGroupType());
        Assert.assertEquals("Wrong group-id", 42, stat.getGroupId().getValue().intValue());
        Assert.assertEquals("Wrong key", 42, stat.getKey().getGroupId().getValue().intValue());
        Assert.assertEquals("Wrong buckets size", 0, stat.getBuckets().getBucket().size());
    }

    /**
     * Test single GroupDescStats conversion
     */
    @Test
    public void testGroupDescStats() {
        List<GroupDesc> groupDescStats = new ArrayList<>();
        GroupDescBuilder builder = new GroupDescBuilder();
        builder.setType(GroupType.OFPGTFF);
        builder.setGroupId(new GroupId(42L));
        List<BucketsList> bucketsList = new ArrayList<>();
        BucketsListBuilder bucketsBuilder = new BucketsListBuilder();
        bucketsBuilder.setWeight(16);
        bucketsBuilder.setWatchPort(new PortNumber(84L));
        bucketsBuilder.setWatchGroup(168L);
        List<Action> actions = new ArrayList<>();
        ActionBuilder actionBuilder = new ActionBuilder();
        actions.add(actionBuilder.build());
        bucketsBuilder.setAction(actions);
        bucketsList.add(bucketsBuilder.build());
        builder.setBucketsList(bucketsList);
        groupDescStats.add(builder.build());
        builder = new GroupDescBuilder();
        builder.setType(GroupType.OFPGTINDIRECT);
        builder.setGroupId(new GroupId(50L));
        bucketsList = new ArrayList<>();
        bucketsBuilder = new BucketsListBuilder();
        bucketsBuilder.setWeight(100);
        bucketsBuilder.setWatchPort(new PortNumber(200L));
        bucketsBuilder.setWatchGroup(400L);
        actions = new ArrayList<>();
        actionBuilder = new ActionBuilder();
        actions.add(actionBuilder.build());
        actionBuilder = new ActionBuilder();
        actions.add(actionBuilder.build());
        actionBuilder = new ActionBuilder();
        actions.add(actionBuilder.build());
        bucketsBuilder.setAction(actions);
        bucketsList.add(bucketsBuilder.build());
        bucketsBuilder = new BucketsListBuilder();
        bucketsBuilder.setWeight(5);
        bucketsBuilder.setWatchPort(new PortNumber(10L));
        bucketsBuilder.setWatchGroup(15L);
        actions = new ArrayList<>();
        bucketsBuilder.setAction(actions);
        bucketsList.add(bucketsBuilder.build());
        builder.setBucketsList(bucketsList);
        groupDescStats.add(builder.build());

        List<GroupDescStats> statsList = convertor.toSALGroupDescStatsList(groupDescStats, OpenflowVersion.OF13);

        Assert.assertEquals("Wrong groupDesc stats size", 2, statsList.size());
        GroupDescStats stat = statsList.get(0);
        Assert.assertEquals("Wrong type", GroupTypes.GroupFf, stat.getGroupType());
        Assert.assertEquals("Wrong group-id", 42, stat.getGroupId().getValue().intValue());
        Assert.assertEquals("Wrong key", 42, stat.getKey().getGroupId().getValue().intValue());
        Assert.assertEquals("Wrong buckets size", 1, stat.getBuckets().getBucket().size());
        Bucket bucket = stat.getBuckets().getBucket().get(0);
        Assert.assertEquals("Wrong type", 0, bucket.getKey().getBucketId().getValue().intValue());
        Assert.assertEquals("Wrong type", 0, bucket.getBucketId().getValue().intValue());
        Assert.assertEquals("Wrong type", 16, bucket.getWeight().intValue());
        Assert.assertEquals("Wrong type", 168, bucket.getWatchGroup().intValue());
        Assert.assertEquals("Wrong type", 84, bucket.getWatchPort().intValue());
        Assert.assertEquals("Wrong type", 1, bucket.getAction().size());
        org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.list
        .Action action = bucket.getAction().get(0);
        Assert.assertEquals("Wrong type", 0, action.getOrder().intValue());
        Assert.assertEquals("Wrong type", "org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112"
                + ".action.action.CopyTtlInCase", action.getAction().getImplementedInterface().getName());

        stat = statsList.get(1);
        Assert.assertEquals("Wrong type", GroupTypes.GroupIndirect, stat.getGroupType());
        Assert.assertEquals("Wrong group-id", 50, stat.getGroupId().getValue().intValue());
        Assert.assertEquals("Wrong key", 50, stat.getKey().getGroupId().getValue().intValue());
        Assert.assertEquals("Wrong buckets size", 2, stat.getBuckets().getBucket().size());
        bucket = stat.getBuckets().getBucket().get(0);
        Assert.assertEquals("Wrong type", 0, bucket.getKey().getBucketId().getValue().intValue());
        Assert.assertEquals("Wrong type", 0, bucket.getBucketId().getValue().intValue());
        Assert.assertEquals("Wrong type", 100, bucket.getWeight().intValue());
        Assert.assertEquals("Wrong type", 400, bucket.getWatchGroup().intValue());
        Assert.assertEquals("Wrong type", 200, bucket.getWatchPort().intValue());
        Assert.assertEquals("Wrong type", 3, bucket.getAction().size());
        action = bucket.getAction().get(0);
        Assert.assertEquals("Wrong type", 0, action.getOrder().intValue());
        Assert.assertEquals("Wrong type", "org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112"
                + ".action.action.CopyTtlOutCase", action.getAction().getImplementedInterface().getName());
        action = bucket.getAction().get(1);
        Assert.assertEquals("Wrong type", 1, action.getOrder().intValue());
        Assert.assertEquals("Wrong type", "org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112"
                + ".action.action.DecNwTtlCase", action.getAction().getImplementedInterface().getName());
        action = bucket.getAction().get(2);
        Assert.assertEquals("Wrong type", 2, action.getOrder().intValue());
        Assert.assertEquals("Wrong type", "org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112"
                + ".action.action.PopPbbActionCase", action.getAction().getImplementedInterface().getName());
        bucket = stat.getBuckets().getBucket().get(1);
        Assert.assertEquals("Wrong type", 1, bucket.getKey().getBucketId().getValue().intValue());
        Assert.assertEquals("Wrong type", 1, bucket.getBucketId().getValue().intValue());
        Assert.assertEquals("Wrong type", 5, bucket.getWeight().intValue());
        Assert.assertEquals("Wrong type", 15, bucket.getWatchGroup().intValue());
        Assert.assertEquals("Wrong type", 10, bucket.getWatchPort().intValue());
        Assert.assertEquals("Wrong type", 0, bucket.getAction().size());
    }
}