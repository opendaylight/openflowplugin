/**
 * Copyright (c) 2014 Ericsson India Global Services Pvt Ltd. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributor: usha.m.s@ericsson.com
 */
package org.opendaylight.openflowplugin.openflow.md.core.sal.convertor;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.CopyTtlInCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.GroupActionCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.SetMplsTtlActionCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.copy.ttl.in._case.CopyTtlIn;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.copy.ttl.in._case.CopyTtlInBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.group.action._case.GroupAction;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.group.action._case.GroupActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.set.mpls.ttl.action._case.SetMplsTtlAction;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.set.mpls.ttl.action._case.SetMplsTtlActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.list.ActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.service.rev130918.AddGroupInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.GroupId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.GroupTypes;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.group.Buckets;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.group.BucketsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.group.buckets.Bucket;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.group.buckets.BucketBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.augments.rev131002.GroupIdAction;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev130731.actions.grouping.Action;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.GroupModCommand;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.GroupType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.GroupModInputBuilder;

public class GroupConvertorTest {

    /**
     * test of {@link GroupConvertor#toGroupModInput(org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.Group, short)}
     */
    @Test
    public void testGroupModConvertorwithallParameters() {

        AddGroupInputBuilder addGroupBuilder = new AddGroupInputBuilder();

        addGroupBuilder.setGroupId(new GroupId(10L));

        addGroupBuilder.setGroupType(GroupTypes.GroupAll);
        List<Bucket> bucketList = new ArrayList<Bucket>();
        List<org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.list.Action> actionsList = new ArrayList<org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.list.Action>();
        List<org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.list.Action> actionsList1 = new ArrayList<org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.list.Action>();

        // Action1
        GroupActionBuilder groupActionBuilder = new GroupActionBuilder();
        groupActionBuilder.setGroup("005");
        GroupAction groupIdaction = groupActionBuilder.build();
        ActionBuilder actionsB = new ActionBuilder();
        actionsB.setAction(new GroupActionCaseBuilder().setGroupAction(groupIdaction).build());

        // Action2:
        GroupActionBuilder groupActionBuilder1 = new GroupActionBuilder();
        groupActionBuilder1.setGroup("006");
        GroupAction groupIdaction1 = groupActionBuilder.build();
        ActionBuilder actionsB1 = new ActionBuilder();
        actionsB1.setAction(new GroupActionCaseBuilder().setGroupAction(groupIdaction1).build());

        actionsList.add(actionsB.build());
        actionsList.add(actionsB1.build());


        BucketsBuilder bucketsB = new BucketsBuilder();

        BucketBuilder bucketB = new BucketBuilder();
        bucketB.setWeight(10);
        bucketB.setWatchPort(20L);
        bucketB.setWatchGroup(22L);

        bucketB.setAction(actionsList);
        Bucket bucket = bucketB.build();

        bucketList.add(bucket); // List of bucket

        BucketBuilder bucketB1 = new BucketBuilder();
        bucketB1.setWeight(50);
        bucketB1.setWatchPort(60L);
        bucketB1.setWatchGroup(70L);

        // Action1
        CopyTtlInBuilder copyTtlB = new CopyTtlInBuilder();
        CopyTtlIn copyTtl = copyTtlB.build();
        ActionBuilder actionsB2 = new ActionBuilder();
        actionsB2.setAction(new CopyTtlInCaseBuilder().setCopyTtlIn(copyTtl).build());

        // Action2:
        SetMplsTtlActionBuilder setMplsTtlActionBuilder = new SetMplsTtlActionBuilder();
        setMplsTtlActionBuilder.setMplsTtl((short)0X1);
        SetMplsTtlAction setMAction = setMplsTtlActionBuilder.build();
        ActionBuilder actionsB3 = new ActionBuilder();

        actionsB3.setAction(new SetMplsTtlActionCaseBuilder().setSetMplsTtlAction(setMAction).build());


        actionsList1.add(actionsB2.build());
        actionsList1.add(actionsB3.build());

        bucketB1.setAction(actionsList);

        Bucket bucket1 = bucketB1.build(); // second bucket

        bucketList.add(bucket1);

        bucketsB.setBucket(bucketList);// List of bucket added to the Buckets
        Buckets buckets = bucketsB.build();

        addGroupBuilder.setBuckets(buckets);

        GroupModInputBuilder outAddGroupInput = GroupConvertor.toGroupModInput(addGroupBuilder.build(), (short) 0X4,BigInteger.valueOf(1));

        Assert.assertEquals(GroupModCommand.OFPGCADD, outAddGroupInput.getCommand());
        Assert.assertEquals(GroupType.OFPGTALL, outAddGroupInput.getType());

        Assert.assertEquals(10L, (long) outAddGroupInput.getGroupId().getValue());
        Assert.assertEquals(10, (int) outAddGroupInput.getBucketsList().get(0).getWeight());
        Assert.assertEquals(20L, (long) outAddGroupInput.getBucketsList().get(0).getWatchPort().getValue());
        Assert.assertEquals((Long) 22L, outAddGroupInput.getBucketsList().get(0).getWatchGroup());

        List<Action> outActionList = outAddGroupInput.getBucketsList().get(0).getAction();
        for (int outItem = 0; outItem < outActionList.size(); outItem++) {
           Action action = outActionList
                    .get(outItem);
            if (action instanceof GroupIdAction) {
                Assert.assertEquals((Long) 5L, ((GroupIdAction) action).getGroupId());

            }
            // TODO:setMplsTTL :OF layer doesnt have get();
        }

        Assert.assertEquals((Integer) 50, outAddGroupInput.getBucketsList().get(1).getWeight());
        Assert.assertEquals((long) 60, (long) outAddGroupInput.getBucketsList().get(1).getWatchPort().getValue());
        Assert.assertEquals((Long) 70L, outAddGroupInput.getBucketsList().get(1).getWatchGroup());
        List<Action> outActionList1 = outAddGroupInput.getBucketsList().get(1).getAction();
        for (int outItem = 0; outItem < outActionList1.size(); outItem++) {
            Action action = outActionList1
                    .get(outItem);
            if (action instanceof GroupIdAction) {

                Assert.assertEquals((Long) 6L, ((GroupIdAction) action).getGroupId());


            }
            // TODO:setMplsTTL :OF layer doesnt have get();
        }

    }

    /**
     * test of {@link GroupConvertor#toGroupModInput(org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.Group, short)}
     */
    @Test
    public void testGroupModConvertorNoBucket() {
        AddGroupInputBuilder addGroupBuilder = new AddGroupInputBuilder();

        addGroupBuilder.setGroupId(new GroupId(10L));

        addGroupBuilder.setGroupType(GroupTypes.GroupAll);

        GroupModInputBuilder outAddGroupInput = GroupConvertor.toGroupModInput(addGroupBuilder.build(), (short) 0X4,BigInteger.valueOf(1));

        Assert.assertEquals(GroupModCommand.OFPGCADD, outAddGroupInput.getCommand());
        Assert.assertEquals(GroupType.OFPGTALL, outAddGroupInput.getType());
    }

    /**
     * test of {@link GroupConvertor#toGroupModInput(org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.Group, short)}
     */
    @Test
    public void testGroupModConvertorBucketwithNOWieghtValuesForGroupTypeFastFailure() {

        AddGroupInputBuilder addGroupBuilder = new AddGroupInputBuilder();

        addGroupBuilder.setGroupId(new GroupId(10L));

        addGroupBuilder.setGroupType(GroupTypes.GroupFf);
        List<Bucket> bucketList = new ArrayList<Bucket>();
        List<org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.list.Action> actionsList = new ArrayList<org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.list.Action>();
        List<org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.list.Action> actionsList1 = new ArrayList<org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.list.Action>();

        // Action1: 005
        actionsList.add(assembleActionBuilder("005").build());
        // Action2: 006
        actionsList.add(assembleActionBuilder("006").build());
        // .. and mr.Bond is not coming today

        BucketsBuilder bucketsB = new BucketsBuilder();

        BucketBuilder bucketB = new BucketBuilder();

        bucketB.setAction(actionsList);
        Bucket bucket = bucketB.build();

        bucketList.add(bucket); // List of bucket
        

        
        BucketBuilder bucketB1 = new BucketBuilder();

        // Action1
        actionsList1.add(assembleCopyTtlInBuilder().build());
        // Action2:
        actionsList1.add(assembleSetMplsTtlActionBuilder().build());

        bucketB1.setAction(actionsList1);

        Bucket bucket1 = bucketB1.build(); // second bucket

        bucketList.add(bucket1);

        bucketsB.setBucket(bucketList);// List of bucket added to the Buckets
        Buckets buckets = bucketsB.build();

        addGroupBuilder.setBuckets(buckets);

        GroupModInputBuilder outAddGroupInput = GroupConvertor.toGroupModInput(addGroupBuilder.build(), (short) 0X4,BigInteger.valueOf(1));

        Assert.assertEquals(GroupModCommand.OFPGCADD, outAddGroupInput.getCommand());
        Assert.assertEquals(GroupType.OFPGTFF, outAddGroupInput.getType());

        Assert.assertEquals(10L, outAddGroupInput.getGroupId().getValue().longValue());

        List<Action> outActionList = outAddGroupInput.getBucketsList().get(0).getAction();
        for (int outItem = 0; outItem < outActionList.size(); outItem++) {
            Action action = outActionList
                    .get(outItem);
            if (action instanceof GroupIdAction) {
                Assert.assertEquals((Long) 5L, ((GroupIdAction) action).getGroupId());
            }
        }

        List<Action> outActionList1 = outAddGroupInput.getBucketsList().get(1).getAction();
        for (int outItem = 0; outItem < outActionList1.size(); outItem++) {
            Action action = outActionList1
                    .get(outItem);
            if (action instanceof GroupIdAction) {
                Assert.assertEquals((Long) 6L, ((GroupIdAction) action).getGroupId());
            }
        }
    }

    /**
     * @return
     */
    private static ActionBuilder assembleSetMplsTtlActionBuilder() {
        SetMplsTtlActionBuilder setMplsTtlActionBuilder = new SetMplsTtlActionBuilder();
        setMplsTtlActionBuilder.setMplsTtl((short) 0X1);
        SetMplsTtlActionCaseBuilder setMplsTtlActionCaseBuilder = new SetMplsTtlActionCaseBuilder();
        setMplsTtlActionCaseBuilder.setSetMplsTtlAction(setMplsTtlActionBuilder.build());
        ActionBuilder actionsB3 = new ActionBuilder();
        actionsB3.setAction(setMplsTtlActionCaseBuilder.build());
        return actionsB3;
    }

    /**
     * @return
     */
    private static ActionBuilder assembleCopyTtlInBuilder() {
        CopyTtlInBuilder copyTtlB = new CopyTtlInBuilder();
        CopyTtlInCaseBuilder copyTtlInCaseBuilder = new CopyTtlInCaseBuilder();
        copyTtlInCaseBuilder.setCopyTtlIn(copyTtlB.build());
        ActionBuilder actionsB2 = new ActionBuilder();
        actionsB2.setAction(copyTtlInCaseBuilder.build());
        return actionsB2;
    }

    /**
     * @param groupName name of group
     * @return 
     * 
     */
    private static ActionBuilder assembleActionBuilder(String groupName) {
        GroupActionBuilder groupActionBuilder = new GroupActionBuilder();
        groupActionBuilder.setGroup(groupName);
        GroupActionCaseBuilder groupActionCaseBuilder = new GroupActionCaseBuilder();
        groupActionCaseBuilder.setGroupAction(groupActionBuilder.build());
        ActionBuilder actionsBld = new ActionBuilder();
        actionsBld.setAction(groupActionCaseBuilder.build());
        return actionsBld;
    }

    /**
     * test of {@link GroupConvertor#toGroupModInput(org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.Group, short)}
     */
    @Test
    public void testGroupModConvertorBucketwithNOWieghtValuesForGroupTypeAll() {

        AddGroupInputBuilder addGroupBuilder = new AddGroupInputBuilder();

        addGroupBuilder.setGroupId(new GroupId(10L));

        addGroupBuilder.setGroupType(GroupTypes.GroupAll);
        List<Bucket> bucketList = new ArrayList<Bucket>();
        List<org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.list.Action> actionsList = new ArrayList<org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.list.Action>();
        List<org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.list.Action> actionsList1 = new ArrayList<org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.list.Action>();

        // Action1
        actionsList.add(assembleActionBuilder("005").build());
        // Action2:
        actionsList.add(assembleActionBuilder("006").build());

        BucketsBuilder bucketsB = new BucketsBuilder();

        BucketBuilder bucketB = new BucketBuilder();

        bucketB.setAction(actionsList);
        Bucket bucket = bucketB.build();

        bucketList.add(bucket); // List of bucket

        BucketBuilder bucketB1 = new BucketBuilder();

        // Action1
        actionsList1.add(assembleCopyTtlInBuilder().build());
        // Action2:
        actionsList1.add(assembleSetMplsTtlActionBuilder().build());

        bucketB1.setAction(actionsList);

        Bucket bucket1 = bucketB1.build(); // second bucket

        bucketList.add(bucket1);

        bucketsB.setBucket(bucketList);// List of bucket added to the Buckets
        Buckets buckets = bucketsB.build();

        addGroupBuilder.setBuckets(buckets);

        GroupModInputBuilder outAddGroupInput = GroupConvertor.toGroupModInput(addGroupBuilder.build(), (short) 0X4,BigInteger.valueOf(1));

        Assert.assertEquals(GroupModCommand.OFPGCADD, outAddGroupInput.getCommand());
        Assert.assertEquals(GroupType.OFPGTALL, outAddGroupInput.getType());

        Assert.assertEquals(10L, outAddGroupInput.getGroupId().getValue().longValue());

        List<Action> outActionList = outAddGroupInput.getBucketsList().get(0).getAction();
        for (int outItem = 0; outItem < outActionList.size(); outItem++) {
            Action action = outActionList
                    .get(outItem);
            if (action instanceof GroupIdAction) {
                Assert.assertEquals((Long) 5L, ((GroupIdAction) action).getGroupId());

            }

        }

        List<Action> outActionList1 = outAddGroupInput.getBucketsList().get(1).getAction();
        for (int outItem = 0; outItem < outActionList1.size(); outItem++) {
            Action action = outActionList1
                    .get(outItem);
            if (action instanceof GroupIdAction) {

                Assert.assertEquals((Long) 6L, ((GroupIdAction) action).getGroupId());

            }

        }

    }
}
