package org.opendaylight.openflowplugin.openflow.md.core.sal.convertor;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.CopyTtlIn;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.CopyTtlInBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.GroupAction;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.GroupActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.SetMplsTtlAction;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.SetMplsTtlActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.list.ActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.service.rev130918.AddGroupInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.GroupId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.group.Buckets;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.group.BucketsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.group.buckets.Bucket;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.group.buckets.BucketBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.augments.rev131002.GroupIdAction;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev130731.actions.ActionsList;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.GroupModCommand;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.GroupType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.GroupModInput;

public class GroupConvertorTest {

    @Test
    public void testGroupModConvertorwithallParameters() {

        AddGroupInputBuilder addGroupBuilder = new AddGroupInputBuilder();

        addGroupBuilder.setGroupId(new GroupId(10L));

        addGroupBuilder
                .setGroupType(org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.GroupTypes.GroupType.GroupAll);
        List<Bucket> bucketList = new ArrayList<Bucket>();
        List<org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.list.Action> actionsList = new ArrayList<org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.list.Action>();
        List<org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.list.Action> actionsList1 = new ArrayList<org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.list.Action>();

        // Action1
        GroupActionBuilder groupActionBuilder = new GroupActionBuilder();
        groupActionBuilder.setGroup("005");
        GroupAction groupIdaction = groupActionBuilder.build();
        ActionBuilder actionsB = new ActionBuilder();
        actionsB.setAction(groupIdaction);

        // Action2:
        GroupActionBuilder groupActionBuilder1 = new GroupActionBuilder();
        groupActionBuilder1.setGroup("006");
        GroupAction groupIdaction1 = groupActionBuilder.build();
        ActionBuilder actionsB1 = new ActionBuilder();
        actionsB1.setAction(groupIdaction1);

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
        actionsB2.setAction(copyTtl);

        // Action2:
        SetMplsTtlActionBuilder setMplsTtlActionBuilder = new SetMplsTtlActionBuilder();
        setMplsTtlActionBuilder.setMplsTtl((short)0X1);
        SetMplsTtlAction setMAction = setMplsTtlActionBuilder.build();
        ActionBuilder actionsB3 = new ActionBuilder();

        actionsB3.setAction(setMAction);


        actionsList1.add(actionsB2.build());
        actionsList1.add(actionsB3.build());

        bucketB1.setAction(actionsList);

        Bucket bucket1 = bucketB1.build(); // second bucket

        bucketList.add(bucket1);

        bucketsB.setBucket(bucketList);// List of bucket added to the Buckets
        Buckets buckets = bucketsB.build();

        addGroupBuilder.setBuckets(buckets);

        GroupModInput outAddGroupInput = GroupConvertor.toGroupModInput(addGroupBuilder.build());

        Assert.assertEquals(GroupModCommand.OFPGCADD, outAddGroupInput.getCommand());
        Assert.assertEquals(GroupType.OFPGTALL, outAddGroupInput.getType());

        Assert.assertEquals(10L, (long) outAddGroupInput.getGroupId());
        Assert.assertEquals(10, (int) outAddGroupInput.getBucketsList().get(0).getWeight());
        Assert.assertEquals(20L, (long) outAddGroupInput.getBucketsList().get(0).getWatchPort().getValue());
        Assert.assertEquals((Long) 22L, outAddGroupInput.getBucketsList().get(0).getWatchGroup());

        List<ActionsList> outActionList = outAddGroupInput.getBucketsList().get(0).getActionsList();
        for (int outItem = 0; outItem < outActionList.size(); outItem++) {
            org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev130731.actions.actions.list.Action action = outActionList
                    .get(outItem).getAction();
            if (action instanceof GroupIdAction) {
                Assert.assertEquals((Long) 5L, ((GroupIdAction) action).getGroupId());

            }
            // TODO:setMplsTTL :OF layer doesnt have get();
        }

        Assert.assertEquals((Integer) 50, outAddGroupInput.getBucketsList().get(1).getWeight());
        Assert.assertEquals((long) 60, (long) outAddGroupInput.getBucketsList().get(1).getWatchPort().getValue());
        Assert.assertEquals((Long) 70L, outAddGroupInput.getBucketsList().get(1).getWatchGroup());
        List<ActionsList> outActionList1 = outAddGroupInput.getBucketsList().get(1).getActionsList();
        for (int outItem = 0; outItem < outActionList1.size(); outItem++) {
            org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev130731.actions.actions.list.Action action = outActionList1
                    .get(outItem).getAction();
            if (action instanceof GroupIdAction) {

                Assert.assertEquals((Long) 6L, ((GroupIdAction) action).getGroupId());


            }
            // TODO:setMplsTTL :OF layer doesnt have get();
        }

    }

}
