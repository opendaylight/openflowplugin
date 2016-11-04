/**
 * Copyright (c) 2014 Ericsson India Global Services Pvt Ltd. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.openflow.md.core.sal.convertor;

import com.google.common.collect.ImmutableList;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.data.VersionDatapathIdConvertorData;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Uri;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.CopyTtlInCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.GroupActionCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.GroupActionCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.OutputActionCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.SetMplsTtlActionCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.copy.ttl.in._case.CopyTtlIn;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.copy.ttl.in._case.CopyTtlInBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.group.action._case.GroupAction;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.group.action._case.GroupActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.output.action._case.OutputActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.set.mpls.ttl.action._case.SetMplsTtlAction;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.set.mpls.ttl.action._case.SetMplsTtlActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.list.ActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.service.rev130918.AddGroupInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.service.rev130918.AddGroupInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.BucketId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.Group;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.GroupId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.GroupTypes;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.group.Buckets;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.group.BucketsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.group.buckets.Bucket;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.group.buckets.BucketBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.actions.grouping.Action;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.GroupModCommand;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.GroupType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.GroupModInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.buckets.grouping.BucketsList;

public class GroupConvertorTest {
    private ConvertorManager convertorManager;

    @Before
    public void setUp() {
        convertorManager = ConvertorManagerFactory.createDefaultManager();
    }

    /**
     * test of {@link GroupConvertor#convert(org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.Group, org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.data.VersionDatapathIdConvertorData)} }
     */
    @Test
    public void testGroupModConvertorwithallParameters() {

        final AddGroupInputBuilder addGroupBuilder = new AddGroupInputBuilder();

        addGroupBuilder.setGroupId(new GroupId(10L));

        addGroupBuilder.setGroupType(GroupTypes.GroupAll);
        final List<Bucket> bucketList = new ArrayList<Bucket>();
        final List<org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.list.Action> actionsList = new ArrayList<org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.list.Action>();
        final List<org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.list.Action> actionsList1 = new ArrayList<org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.list.Action>();

        int actionOrder = 0;

        // Action1
        final GroupActionBuilder groupActionBuilder = new GroupActionBuilder();
        groupActionBuilder.setGroup("005");
        final GroupAction groupIdaction = groupActionBuilder.build();
        final ActionBuilder actionsB = new ActionBuilder();
        actionsB.setOrder(actionOrder++).setAction(new GroupActionCaseBuilder().setGroupAction(groupIdaction).build());

        // Action2:
        final GroupActionBuilder groupActionBuilder1 = new GroupActionBuilder();
        groupActionBuilder1.setGroup("006");
        final GroupAction groupIdaction1 = groupActionBuilder.build();
        final ActionBuilder actionsB1 = new ActionBuilder();
        actionsB1.setOrder(actionOrder++).setAction(new GroupActionCaseBuilder().setGroupAction(groupIdaction1).build());

        actionsList.add(actionsB.build());
        actionsList.add(actionsB1.build());


        final BucketsBuilder bucketsB = new BucketsBuilder();

        final BucketBuilder bucketB = new BucketBuilder();
        bucketB.setWeight(10);
        bucketB.setWatchPort(20L);
        bucketB.setWatchGroup(22L);

        bucketB.setAction(actionsList);
        final Bucket bucket = bucketB.build();

        bucketList.add(bucket); // List of bucket

        final BucketBuilder bucketB1 = new BucketBuilder();
        bucketB1.setWeight(50);
        bucketB1.setWatchPort(60L);
        bucketB1.setWatchGroup(70L);

        // Action1
        final CopyTtlInBuilder copyTtlB = new CopyTtlInBuilder();
        final CopyTtlIn copyTtl = copyTtlB.build();
        final ActionBuilder actionsB2 = new ActionBuilder();
        actionsB2.setOrder(actionOrder++).setAction(new CopyTtlInCaseBuilder().setCopyTtlIn(copyTtl).build());

        // Action2:
        final SetMplsTtlActionBuilder setMplsTtlActionBuilder = new SetMplsTtlActionBuilder();
        setMplsTtlActionBuilder.setMplsTtl((short) 0X1);
        final SetMplsTtlAction setMAction = setMplsTtlActionBuilder.build();
        final ActionBuilder actionsB3 = new ActionBuilder();

        actionsB3.setOrder(actionOrder++).setAction(new SetMplsTtlActionCaseBuilder().setSetMplsTtlAction(setMAction).build());


        actionsList1.add(actionsB2.build());
        actionsList1.add(actionsB3.build());

        bucketB1.setAction(actionsList);

        final Bucket bucket1 = bucketB1.build(); // second bucket

        bucketList.add(bucket1);

        bucketsB.setBucket(bucketList);// List of bucket added to the Buckets
        final Buckets buckets = bucketsB.build();

        addGroupBuilder.setBuckets(buckets);

        VersionDatapathIdConvertorData data = new VersionDatapathIdConvertorData((short) 0X4);
        data.setDatapathId(BigInteger.valueOf(1));

        final GroupModInputBuilder outAddGroupInput = convert(addGroupBuilder.build(), data);

        Assert.assertEquals(GroupModCommand.OFPGCADD, outAddGroupInput.getCommand());
        Assert.assertEquals(GroupType.OFPGTALL, outAddGroupInput.getType());

        Assert.assertEquals(10L, (long) outAddGroupInput.getGroupId().getValue());
        Assert.assertEquals(10, (int) outAddGroupInput.getBucketsList().get(0).getWeight());
        Assert.assertEquals(20L, (long) outAddGroupInput.getBucketsList().get(0).getWatchPort().getValue());
        Assert.assertEquals((Long) 22L, outAddGroupInput.getBucketsList().get(0).getWatchGroup());

        final List<Action> outActionList = outAddGroupInput.getBucketsList().get(0).getAction();
        for (int outItem = 0; outItem < outActionList.size(); outItem++) {
            final Action action = outActionList
                    .get(outItem);
            if (action.getActionChoice() instanceof GroupActionCase) {
                Assert.assertEquals((Long) 5L, ((GroupActionCase) action.getActionChoice()).getGroupAction().getGroupId());

            }
            // TODO:setMplsTTL :OF layer doesnt have get();
        }

        Assert.assertEquals((Integer) 50, outAddGroupInput.getBucketsList().get(1).getWeight());
        Assert.assertEquals((long) 60, (long) outAddGroupInput.getBucketsList().get(1).getWatchPort().getValue());
        Assert.assertEquals((Long) 70L, outAddGroupInput.getBucketsList().get(1).getWatchGroup());
        final List<Action> outActionList1 = outAddGroupInput.getBucketsList().get(1).getAction();
        for (int outItem = 0; outItem < outActionList1.size(); outItem++) {
            final Action action = outActionList1
                    .get(outItem);
            if (action.getActionChoice() instanceof GroupActionCase) {

                Assert.assertEquals((Long) 6L, ((GroupActionCase) action.getActionChoice()).getGroupAction().getGroupId());


            }
            // TODO:setMplsTTL :OF layer doesnt have get();
        }

    }

    /**
     * test of {@link GroupConvertor#convert(org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.Group, org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.data.VersionDatapathIdConvertorData)} }
     */
    @Test
    public void testGroupModConvertorNoBucket() {
        final AddGroupInputBuilder addGroupBuilder = new AddGroupInputBuilder();

        addGroupBuilder.setGroupId(new GroupId(10L));

        addGroupBuilder.setGroupType(GroupTypes.GroupAll);

        VersionDatapathIdConvertorData data = new VersionDatapathIdConvertorData((short) 0X4);
        data.setDatapathId(BigInteger.valueOf(1));

        final GroupModInputBuilder outAddGroupInput = convert(addGroupBuilder.build(), data);
        Assert.assertEquals(GroupModCommand.OFPGCADD, outAddGroupInput.getCommand());
        Assert.assertEquals(GroupType.OFPGTALL, outAddGroupInput.getType());
    }

    /**
     * test of {@link GroupConvertor#convert(org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.Group, org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.data.VersionDatapathIdConvertorData)} }
     */
    @Test
    public void testGroupModConvertorBucketwithNOWieghtValuesForGroupTypeFastFailure() {

        int actionOrder = 0;

        final AddGroupInputBuilder addGroupBuilder = new AddGroupInputBuilder();

        addGroupBuilder.setGroupId(new GroupId(10L));

        addGroupBuilder.setGroupType(GroupTypes.GroupFf);
        final List<Bucket> bucketList = new ArrayList<Bucket>();
        final List<org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.list.Action> actionsList = new ArrayList<org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.list.Action>();
        final List<org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.list.Action> actionsList1 = new ArrayList<org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.list.Action>();

        // Action1: 005
        actionsList.add(assembleActionBuilder("005", actionOrder++).build());
        // Action2: 006
        actionsList.add(assembleActionBuilder("006", actionOrder++).build());
        // .. and mr.Bond is not coming today

        final BucketsBuilder bucketsB = new BucketsBuilder();

        final BucketBuilder bucketB = new BucketBuilder();

        bucketB.setAction(actionsList);
        final Bucket bucket = bucketB.build();

        bucketList.add(bucket); // List of bucket


        final BucketBuilder bucketB1 = new BucketBuilder();

        // Action1
        actionsList1.add(assembleCopyTtlInBuilder(actionOrder++).build());
        // Action2:
        actionsList1.add(assembleSetMplsTtlActionBuilder(actionOrder++).build());

        bucketB1.setAction(actionsList1);

        final Bucket bucket1 = bucketB1.build(); // second bucket

        bucketList.add(bucket1);

        bucketsB.setBucket(bucketList);// List of bucket added to the Buckets
        final Buckets buckets = bucketsB.build();

        addGroupBuilder.setBuckets(buckets);

        VersionDatapathIdConvertorData data = new VersionDatapathIdConvertorData((short) 0X4);
        data.setDatapathId(BigInteger.valueOf(1));

        final GroupModInputBuilder outAddGroupInput = convert(addGroupBuilder.build(), data);

        Assert.assertEquals(GroupModCommand.OFPGCADD, outAddGroupInput.getCommand());
        Assert.assertEquals(GroupType.OFPGTFF, outAddGroupInput.getType());

        Assert.assertEquals(10L, outAddGroupInput.getGroupId().getValue().longValue());

        final List<Action> outActionList = outAddGroupInput.getBucketsList().get(0).getAction();
        for (int outItem = 0; outItem < outActionList.size(); outItem++) {
            final Action action = outActionList
                    .get(outItem);
            if (action.getActionChoice() instanceof GroupActionCase) {
                Assert.assertEquals((Long) 5L, ((GroupActionCase) action.getActionChoice()).getGroupAction().getGroupId());
            }
        }

        final List<Action> outActionList1 = outAddGroupInput.getBucketsList().get(1).getAction();
        for (int outItem = 0; outItem < outActionList1.size(); outItem++) {
            final Action action = outActionList1
                    .get(outItem);
            if (action.getActionChoice() instanceof GroupActionCase) {
                Assert.assertEquals((Long) 6L, ((GroupActionCase) action.getActionChoice()).getGroupAction().getGroupId());
            }
        }
    }

    /**
     * test of {@link GroupConvertor#convert(org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.Group, org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.data.VersionDatapathIdConvertorData)} }
     */
    @Test
    public void testGroupModConvertSortedBuckets() {

        final int actionOrder = 0;

        final ArrayList<Bucket> bucket = new ArrayList<Bucket>();

        bucket.add(new BucketBuilder()
                .setBucketId(new BucketId((long) 4))
                .setWatchPort((long)2)
                .setWatchGroup((long) 1)
                .setAction(ImmutableList.of(new ActionBuilder()
                        .setOrder(0)
                        .setAction(new OutputActionCaseBuilder()
                                .setOutputAction(new OutputActionBuilder()
                                        .setOutputNodeConnector(new Uri("openflow:1:2"))
                                        .build())
                                .build())
                        .build()))
                .build());

        bucket.add(new BucketBuilder()
                .setBucketId(new BucketId((long) 3))
                .setWatchPort((long)6)
                .setWatchGroup((long) 1)
                .setAction(ImmutableList.of(new ActionBuilder()
                        .setOrder(0)
                        .setAction(new OutputActionCaseBuilder()
                                .setOutputAction(new OutputActionBuilder()
                                        .setOutputNodeConnector(new Uri("openflow:1:6"))
                                        .build())
                                .build())
                        .build()))
                .build());

        bucket.add(new BucketBuilder()
                .setBucketId(new BucketId((long) 2))
                .setWatchPort((long)5)
                .setWatchGroup((long) 1)
                .setAction(ImmutableList.of(new ActionBuilder()
                        .setOrder(0)
                        .setAction(new OutputActionCaseBuilder()
                                .setOutputAction(new OutputActionBuilder()
                                        .setOutputNodeConnector(new Uri("openflow:1:5"))
                                        .build())
                                .build())
                        .build()))
                .build());

        bucket.add(new BucketBuilder()
                .setBucketId(new BucketId((long) 1))
                .setWatchPort((long)4)
                .setWatchGroup((long) 1)
                .setAction(ImmutableList.of(new ActionBuilder()
                        .setOrder(0)
                        .setAction(new OutputActionCaseBuilder()
                                .setOutputAction(new OutputActionBuilder()
                                        .setOutputNodeConnector(new Uri("openflow:1:4"))
                                        .build())
                                .build())
                        .build()))
                .build());

        bucket.add(new BucketBuilder()
                .setBucketId(new BucketId((long) 0))
                .setWatchPort((long)3)
                .setWatchGroup((long) 1)
                .setAction(ImmutableList.of(new ActionBuilder()
                        .setOrder(0)
                        .setAction(new OutputActionCaseBuilder()
                                .setOutputAction(new OutputActionBuilder()
                                        .setOutputNodeConnector(new Uri("openflow:1:3"))
                                        .build())
                                .build())
                        .build()))
                .build());


        final AddGroupInput input = new AddGroupInputBuilder()
                .setGroupId(new GroupId((long) 1))
                .setGroupName("Foo")
                .setGroupType(GroupTypes.GroupFf)
                .setBuckets(new BucketsBuilder()
                        .setBucket(bucket)
                        .build())
                .build();

        VersionDatapathIdConvertorData data = new VersionDatapathIdConvertorData((short) 0X4);
        data.setDatapathId(BigInteger.valueOf(1));

        final GroupModInputBuilder outAddGroupInput = convert(input, data);

        final List<BucketsList> bucketList = outAddGroupInput.getBucketsList();
        Assert.assertEquals( Long.valueOf(1), bucketList.get(0).getWatchGroup());
        Assert.assertEquals( Long.valueOf(3), bucketList.get(0).getWatchPort().getValue());

        Assert.assertEquals( Long.valueOf(1), bucketList.get(1).getWatchGroup());
        Assert.assertEquals( Long.valueOf(4), bucketList.get(1).getWatchPort().getValue());

        Assert.assertEquals( Long.valueOf(1), bucketList.get(2).getWatchGroup());
        Assert.assertEquals( Long.valueOf(5), bucketList.get(2).getWatchPort().getValue());

        Assert.assertEquals( Long.valueOf(1), bucketList.get(3).getWatchGroup());
        Assert.assertEquals( Long.valueOf(6), bucketList.get(3).getWatchPort().getValue());

        Assert.assertEquals( Long.valueOf(1), bucketList.get(4).getWatchGroup());
        Assert.assertEquals( Long.valueOf(2), bucketList.get(4).getWatchPort().getValue());


    }

    /**
     * @return
     */
    private static ActionBuilder assembleSetMplsTtlActionBuilder(final int actionOrder) {
        final SetMplsTtlActionBuilder setMplsTtlActionBuilder = new SetMplsTtlActionBuilder();
        setMplsTtlActionBuilder.setMplsTtl((short) 0X1);
        final SetMplsTtlActionCaseBuilder setMplsTtlActionCaseBuilder = new SetMplsTtlActionCaseBuilder();
        setMplsTtlActionCaseBuilder.setSetMplsTtlAction(setMplsTtlActionBuilder.build());
        final ActionBuilder actionsB3 = new ActionBuilder();
        actionsB3.setOrder(actionOrder).setAction(setMplsTtlActionCaseBuilder.build());
        return actionsB3;
    }

    /**
     * @return
     */
    private static ActionBuilder assembleCopyTtlInBuilder(final int actionOrder) {
        final CopyTtlInBuilder copyTtlB = new CopyTtlInBuilder();
        final CopyTtlInCaseBuilder copyTtlInCaseBuilder = new CopyTtlInCaseBuilder();
        copyTtlInCaseBuilder.setCopyTtlIn(copyTtlB.build());
        final ActionBuilder actionsB2 = new ActionBuilder();
        actionsB2.setOrder(actionOrder).setAction(copyTtlInCaseBuilder.build());
        return actionsB2;
    }

    /**
     * @param groupName name of group
     * @return
     */
    private static ActionBuilder assembleActionBuilder(final String groupName, final int actionOrder) {
        final GroupActionBuilder groupActionBuilder = new GroupActionBuilder();
        groupActionBuilder.setGroup(groupName);
        final GroupActionCaseBuilder groupActionCaseBuilder = new GroupActionCaseBuilder();
        groupActionCaseBuilder.setGroupAction(groupActionBuilder.build());
        final ActionBuilder actionsBld = new ActionBuilder();
        actionsBld.setOrder(actionOrder).setAction(groupActionCaseBuilder.build());
        return actionsBld;
    }

    /**
     * test of {@link GroupConvertor#convert(org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.Group, org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.data.VersionDatapathIdConvertorData)} }
     */
    @Test
    public void testGroupModConvertorBucketwithNOWieghtValuesForGroupTypeAll() {

        int actionOrder = 0;

        final AddGroupInputBuilder addGroupBuilder = new AddGroupInputBuilder();

        addGroupBuilder.setGroupId(new GroupId(10L));

        addGroupBuilder.setGroupType(GroupTypes.GroupAll);
        final List<Bucket> bucketList = new ArrayList<Bucket>();
        final List<org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.list.Action> actionsList = new ArrayList<org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.list.Action>();
        final List<org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.list.Action> actionsList1 = new ArrayList<org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.list.Action>();

        // Action1
        actionsList.add(assembleActionBuilder("005", actionOrder++).build());
        // Action2:
        actionsList.add(assembleActionBuilder("006", actionOrder++).build());

        final BucketsBuilder bucketsB = new BucketsBuilder();

        final BucketBuilder bucketB = new BucketBuilder();

        bucketB.setAction(actionsList);
        final Bucket bucket = bucketB.build();

        bucketList.add(bucket); // List of bucket

        final BucketBuilder bucketB1 = new BucketBuilder();

        // Action1
        actionsList1.add(assembleCopyTtlInBuilder(actionOrder++).build());
        // Action2:
        actionsList1.add(assembleSetMplsTtlActionBuilder(actionOrder++).build());

        bucketB1.setAction(actionsList);

        final Bucket bucket1 = bucketB1.build(); // second bucket

        bucketList.add(bucket1);

        bucketsB.setBucket(bucketList);// List of bucket added to the Buckets
        final Buckets buckets = bucketsB.build();

        addGroupBuilder.setBuckets(buckets);

        VersionDatapathIdConvertorData data = new VersionDatapathIdConvertorData((short) 0X4);
        data.setDatapathId(BigInteger.valueOf(1));

        final GroupModInputBuilder outAddGroupInput = convert(addGroupBuilder.build(), data);

        Assert.assertEquals(GroupModCommand.OFPGCADD, outAddGroupInput.getCommand());
        Assert.assertEquals(GroupType.OFPGTALL, outAddGroupInput.getType());

        Assert.assertEquals(10L, outAddGroupInput.getGroupId().getValue().longValue());

        final List<Action> outActionList = outAddGroupInput.getBucketsList().get(0).getAction();
        for (int outItem = 0; outItem < outActionList.size(); outItem++) {
            final Action action = outActionList
                    .get(outItem);
            if (action.getActionChoice() instanceof GroupActionCase) {
                Assert.assertEquals((Long) 5L, ((GroupActionCase) action.getActionChoice()).getGroupAction().getGroupId());

            }

        }

        final List<Action> outActionList1 = outAddGroupInput.getBucketsList().get(1).getAction();
        for (int outItem = 0; outItem < outActionList1.size(); outItem++) {
            final Action action = outActionList1
                    .get(outItem);
            if (action.getActionChoice() instanceof GroupActionCase) {

                Assert.assertEquals((Long) 6L, ((GroupActionCase) action.getActionChoice()).getGroupAction().getGroupId());

            }

        }

    }

    private GroupModInputBuilder convert(Group group, VersionDatapathIdConvertorData data) {
        final Optional<GroupModInputBuilder> outAddGroupInputOptional = convertorManager.convert(group, data);
        Assert.assertTrue("Group convertor not found", outAddGroupInputOptional.isPresent());
        return outAddGroupInputOptional.get();
    }
}
