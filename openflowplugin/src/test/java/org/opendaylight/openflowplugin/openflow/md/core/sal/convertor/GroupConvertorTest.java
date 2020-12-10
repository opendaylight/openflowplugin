/*
 * Copyright (c) 2014 Ericsson India Global Services Pvt Ltd. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.openflow.md.core.sal.convertor;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.google.common.collect.ImmutableList;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.Before;
import org.junit.Test;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.data.VersionDatapathIdConvertorData;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Uri;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.CopyTtlInCaseBuilder;
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
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.group.buckets.BucketKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.GroupCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.SetMplsTtlCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.actions.grouping.Action;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.GroupModCommand;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.GroupType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.GroupModInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.buckets.grouping.BucketsList;
import org.opendaylight.yangtools.yang.common.Uint16;
import org.opendaylight.yangtools.yang.common.Uint32;
import org.opendaylight.yangtools.yang.common.Uint64;
import org.opendaylight.yangtools.yang.common.Uint8;

public class GroupConvertorTest {
    private ConvertorManager convertorManager;

    @Before
    public void setUp() {
        convertorManager = ConvertorManagerFactory.createDefaultManager();
    }

    /**
     * test of {@link GroupConvertor#convert(Group, VersionDatapathIdConvertorData)} }.
     */
    @Test
    public void testGroupModConvertorwithallParameters() {

        final AddGroupInputBuilder addGroupBuilder = new AddGroupInputBuilder();

        addGroupBuilder.setGroupId(new GroupId(Uint32.TEN));

        addGroupBuilder.setGroupType(GroupTypes.GroupAll);
        final List<Bucket> bucketList = new ArrayList<>();
        final List<org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.list.Action>
            actionsList = new ArrayList<>();
        final List<org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.list.Action>
            actionsList1 = new ArrayList<>();

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
        actionsB1.setOrder(actionOrder++).setAction(new GroupActionCaseBuilder()
                .setGroupAction(groupIdaction1).build());

        actionsList.add(actionsB.build());
        actionsList.add(actionsB1.build());


        final BucketsBuilder bucketsB = new BucketsBuilder();

        final BucketBuilder bucketB = new BucketBuilder();
        bucketB.setWeight(Uint16.TEN);
        bucketB.setWatchPort(Uint32.valueOf(20));
        bucketB.setWatchGroup(Uint32.valueOf(22));
        bucketB.withKey(new BucketKey(new BucketId(Uint32.ZERO)));

        bucketB.setAction(actionsList);
        final Bucket bucket = bucketB.build();

        bucketList.add(bucket); // List of bucket

        final BucketBuilder bucketB1 = new BucketBuilder();
        bucketB1.setWeight(Uint16.valueOf(50));
        bucketB1.setWatchPort(Uint32.valueOf(60));
        bucketB1.setWatchGroup(Uint32.valueOf(70));
        bucketB1.withKey(new BucketKey(new BucketId(Uint32.ONE)));

        // Action1
        final CopyTtlInBuilder copyTtlB = new CopyTtlInBuilder();
        final CopyTtlIn copyTtl = copyTtlB.build();
        final ActionBuilder actionsB2 = new ActionBuilder();
        actionsB2.setOrder(actionOrder++).setAction(new CopyTtlInCaseBuilder().setCopyTtlIn(copyTtl).build());

        // Action2:
        final SetMplsTtlActionBuilder setMplsTtlActionBuilder = new SetMplsTtlActionBuilder();
        setMplsTtlActionBuilder.setMplsTtl(Uint8.ONE);
        final SetMplsTtlAction setMAction = setMplsTtlActionBuilder.build();
        final ActionBuilder actionsB3 = new ActionBuilder();

        actionsB3.setOrder(actionOrder++).setAction(new SetMplsTtlActionCaseBuilder()
                .setSetMplsTtlAction(setMAction).build());


        actionsList1.add(actionsB2.build());
        actionsList1.add(actionsB3.build());

        bucketB1.setAction(actionsList);

        final Bucket bucket1 = bucketB1.build(); // second bucket

        bucketList.add(bucket1);

        bucketsB.setBucket(bucketList);// List of bucket added to the Buckets
        final Buckets buckets = bucketsB.build();

        addGroupBuilder.setBuckets(buckets);

        VersionDatapathIdConvertorData data = new VersionDatapathIdConvertorData((short) 0X4);
        data.setDatapathId(Uint64.ONE);

        final GroupModInputBuilder outAddGroupInput = convert(addGroupBuilder.build(), data);

        assertEquals(GroupModCommand.OFPGCADD, outAddGroupInput.getCommand());
        assertEquals(GroupType.OFPGTALL, outAddGroupInput.getType());

        assertEquals(10L, outAddGroupInput.getGroupId().getValue().toJava());
        assertEquals(10, outAddGroupInput.getBucketsList().get(0).getWeight().toJava());
        assertEquals(20L, outAddGroupInput.getBucketsList().get(0).getWatchPort().getValue().toJava());
        assertEquals(22L, outAddGroupInput.getBucketsList().get(0).getWatchGroup().toJava());

        final List<Action> outActionList = outAddGroupInput.getBucketsList().get(0).getAction();
        assertEquals(ImmutableList.of(new org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action
            .rev150203.actions.grouping.ActionBuilder().setActionChoice(new GroupCaseBuilder().setGroupAction(
                new org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping
                .action.choice.group._case.GroupActionBuilder().setGroupId(Uint32.valueOf(5)).build())
                .build()).build(),
            new org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.actions.grouping
            .ActionBuilder().setActionChoice(new GroupCaseBuilder().setGroupAction(
                    new org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action
                    .grouping.action.choice.group._case.GroupActionBuilder().setGroupId(Uint32.valueOf(5))
                    .build()).build())
            .build()), outActionList);

        assertEquals(50, outAddGroupInput.getBucketsList().get(1).getWeight().toJava());
        assertEquals(60, outAddGroupInput.getBucketsList().get(1).getWatchPort().getValue().toJava());
        assertEquals(70L, outAddGroupInput.getBucketsList().get(1).getWatchGroup().toJava());

        final List<Action> outActionList1 = outAddGroupInput.getBucketsList().get(1).getAction();
        assertEquals(ImmutableList.of(new org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action
            .rev150203.actions.grouping.ActionBuilder().setActionChoice(new GroupCaseBuilder().setGroupAction(
                new org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping
                .action.choice.group._case.GroupActionBuilder().setGroupId(Uint32.valueOf(5)).build()).build())
            .build(),
            new org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.actions.grouping
            .ActionBuilder().setActionChoice(new GroupCaseBuilder().setGroupAction(
                    new org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action
                    .grouping.action.choice.group._case.GroupActionBuilder().setGroupId(Uint32.valueOf(5))
                    .build()).build())
            .build()), outActionList1);
    }

    /**
     * test of {@link GroupConvertor#convert(Group, VersionDatapathIdConvertorData)} }.
     */
    @Test
    public void testGroupModConvertorNoBucket() {
        final AddGroupInputBuilder addGroupBuilder = new AddGroupInputBuilder();

        addGroupBuilder.setGroupId(new GroupId(Uint32.TEN));

        addGroupBuilder.setGroupType(GroupTypes.GroupAll);

        VersionDatapathIdConvertorData data = new VersionDatapathIdConvertorData((short) 0X4);
        data.setDatapathId(Uint64.ONE);

        final GroupModInputBuilder outAddGroupInput = convert(addGroupBuilder.build(), data);
        assertEquals(GroupModCommand.OFPGCADD, outAddGroupInput.getCommand());
        assertEquals(GroupType.OFPGTALL, outAddGroupInput.getType());
    }

    /**
     * test of {@link GroupConvertor#convert(Group, VersionDatapathIdConvertorData)} }.
     */
    @Test
    public void testGroupModConvertorBucketwithNOWieghtValuesForGroupTypeFastFailure() {

        int actionOrder = 0;

        final AddGroupInputBuilder addGroupBuilder = new AddGroupInputBuilder();

        addGroupBuilder.setGroupId(new GroupId(Uint32.TEN));

        addGroupBuilder.setGroupType(GroupTypes.GroupFf);
        final List<Bucket> bucketList = new ArrayList<>();
        final List<org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.list.Action>
            actionsList = new ArrayList<>();
        final List<org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.list.Action>
            actionsList1 = new ArrayList<>();

        // Action1: 005
        actionsList.add(assembleActionBuilder("005", actionOrder++).build());
        // Action2: 006
        actionsList.add(assembleActionBuilder("006", actionOrder++).build());
        // .. and mr.Bond is not coming today

        final BucketsBuilder bucketsB = new BucketsBuilder();

        final BucketBuilder bucketB = new BucketBuilder();

        bucketB.setAction(actionsList).withKey(new BucketKey(new BucketId(Uint32.ZERO)));
        final Bucket bucket = bucketB.build();

        bucketList.add(bucket); // List of bucket


        final BucketBuilder bucketB1 = new BucketBuilder();

        // Action1
        actionsList1.add(assembleCopyTtlInBuilder(actionOrder++).build());
        // Action2:
        actionsList1.add(assembleSetMplsTtlActionBuilder(actionOrder++).build());

        bucketB1.setAction(actionsList1).withKey(new BucketKey(new BucketId(Uint32.ONE)));

        final Bucket bucket1 = bucketB1.build(); // second bucket

        bucketList.add(bucket1);

        bucketsB.setBucket(bucketList);// List of bucket added to the Buckets
        final Buckets buckets = bucketsB.build();

        addGroupBuilder.setBuckets(buckets);

        VersionDatapathIdConvertorData data = new VersionDatapathIdConvertorData((short) 0X4);
        data.setDatapathId(Uint64.ONE);

        final GroupModInputBuilder outAddGroupInput = convert(addGroupBuilder.build(), data);

        assertEquals(GroupModCommand.OFPGCADD, outAddGroupInput.getCommand());
        assertEquals(GroupType.OFPGTFF, outAddGroupInput.getType());

        assertEquals(10L, outAddGroupInput.getGroupId().getValue().longValue());

        final List<Action> outActionList = outAddGroupInput.getBucketsList().get(0).getAction();
        assertEquals(ImmutableList.of(new org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203
                    .actions.grouping.ActionBuilder().setActionChoice(new GroupCaseBuilder().setGroupAction(
                            new org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action
                            .grouping.action.choice.group._case.GroupActionBuilder().setGroupId(Uint32.valueOf(5L))
                            .build()).build())
                    .build(),
                    new org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.actions.grouping
                            .ActionBuilder().setActionChoice(new GroupCaseBuilder().setGroupAction(
                                    new org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203
                                    .action.grouping.action.choice.group._case.GroupActionBuilder()
                                    .setGroupId(Uint32.valueOf(6)).build()).build()).build()), outActionList);

        final List<Action> outActionList1 = outAddGroupInput.getBucketsList().get(1).getAction();
        assertEquals(ImmutableList.of(new org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203
                    .actions.grouping.ActionBuilder().setActionChoice(
                            new org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action
                            .grouping.action.choice.CopyTtlInCaseBuilder().build()).build(),
                    new org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.actions.grouping
                            .ActionBuilder().setActionChoice(new SetMplsTtlCaseBuilder().setSetMplsTtlAction(
                                    new org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203
                                            .action.grouping.action.choice.set.mpls.ttl._case.SetMplsTtlActionBuilder()
                                            .setMplsTtl(Uint8.ONE).build()).build()).build()), outActionList1);
    }

    /**
     * test of {@link GroupConvertor#convert(Group, VersionDatapathIdConvertorData)} }.
     */
    @Test
    public void testGroupModConvertSortedBuckets() {

        final int actionOrder = 0;

        final ArrayList<Bucket> bucket = new ArrayList<>();

        bucket.add(new BucketBuilder()
                .setBucketId(new BucketId(Uint32.valueOf(4)))
                .setWatchPort(Uint32.TWO)
                .setWatchGroup(Uint32.ONE)
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
                .setBucketId(new BucketId(Uint32.valueOf(3)))
                .setWatchPort(Uint32.valueOf(6))
                .setWatchGroup(Uint32.ONE)
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
                .setBucketId(new BucketId(Uint32.TWO))
                .setWatchPort(Uint32.valueOf(5))
                .setWatchGroup(Uint32.ONE)
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
                .setBucketId(new BucketId(Uint32.ONE))
                .setWatchPort(Uint32.valueOf(4))
                .setWatchGroup(Uint32.ONE)
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
                .setBucketId(new BucketId(Uint32.ZERO))
                .setWatchPort(Uint32.valueOf(3))
                .setWatchGroup(Uint32.ONE)
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
                .setGroupId(new GroupId(Uint32.ONE))
                .setGroupName("Foo")
                .setGroupType(GroupTypes.GroupFf)
                .setBuckets(new BucketsBuilder()
                        .setBucket(bucket)
                        .build())
                .build();

        VersionDatapathIdConvertorData data = new VersionDatapathIdConvertorData((short) 0X4);
        data.setDatapathId(Uint64.ONE);

        final GroupModInputBuilder outAddGroupInput = convert(input, data);

        final List<BucketsList> bucketList = outAddGroupInput.getBucketsList();
        assertEquals(Uint32.ONE, bucketList.get(0).getWatchGroup());
        assertEquals(Uint32.valueOf(3), bucketList.get(0).getWatchPort().getValue());

        assertEquals(Uint32.ONE, bucketList.get(1).getWatchGroup());
        assertEquals(Uint32.valueOf(4), bucketList.get(1).getWatchPort().getValue());

        assertEquals(Uint32.ONE, bucketList.get(2).getWatchGroup());
        assertEquals(Uint32.valueOf(5), bucketList.get(2).getWatchPort().getValue());

        assertEquals(Uint32.ONE, bucketList.get(3).getWatchGroup());
        assertEquals(Uint32.valueOf(6), bucketList.get(3).getWatchPort().getValue());

        assertEquals(Uint32.ONE, bucketList.get(4).getWatchGroup());
        assertEquals(Uint32.valueOf(2), bucketList.get(4).getWatchPort().getValue());

    }

    private static ActionBuilder assembleSetMplsTtlActionBuilder(final int actionOrder) {
        final SetMplsTtlActionBuilder setMplsTtlActionBuilder = new SetMplsTtlActionBuilder();
        setMplsTtlActionBuilder.setMplsTtl(Uint8.ONE);
        final SetMplsTtlActionCaseBuilder setMplsTtlActionCaseBuilder = new SetMplsTtlActionCaseBuilder();
        setMplsTtlActionCaseBuilder.setSetMplsTtlAction(setMplsTtlActionBuilder.build());
        final ActionBuilder actionsB3 = new ActionBuilder();
        actionsB3.setOrder(actionOrder).setAction(setMplsTtlActionCaseBuilder.build());
        return actionsB3;
    }

    private static ActionBuilder assembleCopyTtlInBuilder(final int actionOrder) {
        final CopyTtlInBuilder copyTtlB = new CopyTtlInBuilder();
        final CopyTtlInCaseBuilder copyTtlInCaseBuilder = new CopyTtlInCaseBuilder();
        copyTtlInCaseBuilder.setCopyTtlIn(copyTtlB.build());
        final ActionBuilder actionsB2 = new ActionBuilder();
        actionsB2.setOrder(actionOrder).setAction(copyTtlInCaseBuilder.build());
        return actionsB2;
    }

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
     * test of {@link GroupConvertor#convert(Group, VersionDatapathIdConvertorData)} }.
     */
    @Test
    public void testGroupModConvertorBucketwithNOWieghtValuesForGroupTypeAll() {

        int actionOrder = 0;

        final AddGroupInputBuilder addGroupBuilder = new AddGroupInputBuilder();

        addGroupBuilder.setGroupId(new GroupId(Uint32.TEN));

        addGroupBuilder.setGroupType(GroupTypes.GroupAll);
        final List<Bucket> bucketList = new ArrayList<>();
        final List<org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.list.Action>
            actionsList = new ArrayList<>();
        final List<org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.list.Action>
            actionsList1 = new ArrayList<>();

        // Action1
        actionsList.add(assembleActionBuilder("005", actionOrder++).build());
        // Action2:
        actionsList.add(assembleActionBuilder("006", actionOrder++).build());

        final BucketsBuilder bucketsB = new BucketsBuilder();

        final BucketBuilder bucketB = new BucketBuilder();

        bucketB.setAction(actionsList).withKey(new BucketKey(new BucketId(Uint32.ZERO)));
        final Bucket bucket = bucketB.build();

        bucketList.add(bucket); // List of bucket

        final BucketBuilder bucketB1 = new BucketBuilder();

        // Action1
        actionsList1.add(assembleCopyTtlInBuilder(actionOrder++).build());
        // Action2:
        actionsList1.add(assembleSetMplsTtlActionBuilder(actionOrder++).build());

        bucketB1.setAction(actionsList).withKey(new BucketKey(new BucketId(Uint32.ONE)));

        final Bucket bucket1 = bucketB1.build(); // second bucket

        bucketList.add(bucket1);

        bucketsB.setBucket(bucketList);// List of bucket added to the Buckets
        final Buckets buckets = bucketsB.build();

        addGroupBuilder.setBuckets(buckets);

        VersionDatapathIdConvertorData data = new VersionDatapathIdConvertorData((short) 0X4);
        data.setDatapathId(Uint64.ONE);

        final GroupModInputBuilder outAddGroupInput = convert(addGroupBuilder.build(), data);

        assertEquals(GroupModCommand.OFPGCADD, outAddGroupInput.getCommand());
        assertEquals(GroupType.OFPGTALL, outAddGroupInput.getType());

        assertEquals(10L, outAddGroupInput.getGroupId().getValue().longValue());

        final List<Action> outActionList = outAddGroupInput.getBucketsList().get(0).getAction();
        assertEquals(ImmutableList.of(new org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203
                    .actions.grouping.ActionBuilder().setActionChoice(new GroupCaseBuilder().setGroupAction(
                            new org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action
                                    .grouping.action.choice.group._case.GroupActionBuilder()
                                    .setGroupId(Uint32.valueOf(5)).build())
                        .build()).build(),
                    new org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.actions.grouping
                            .ActionBuilder().setActionChoice(new GroupCaseBuilder().setGroupAction(
                                    new org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203
                                            .action.grouping.action.choice.group._case.GroupActionBuilder()
                                            .setGroupId(Uint32.valueOf(6)).build()).build()).build()), outActionList);

        final List<Action> outActionList1 = outAddGroupInput.getBucketsList().get(1).getAction();
        assertEquals(ImmutableList.of(new org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203
                    .actions.grouping.ActionBuilder().setActionChoice(new GroupCaseBuilder().setGroupAction(
                            new org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action
                                    .grouping.action.choice.group._case.GroupActionBuilder()
                                        .setGroupId(Uint32.valueOf(5)).build())
                        .build()).build(),
                    new org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.actions.grouping
                            .ActionBuilder().setActionChoice(new GroupCaseBuilder().setGroupAction(
                                    new org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203
                                            .action.grouping.action.choice.group._case.GroupActionBuilder()
                                            .setGroupId(Uint32.valueOf(6)).build()).build()).build()), outActionList1);
    }

    private GroupModInputBuilder convert(final Group group, final VersionDatapathIdConvertorData data) {
        final Optional<GroupModInputBuilder> outAddGroupInputOptional = convertorManager.convert(group, data);
        assertTrue("Group convertor not found", outAddGroupInputOptional.isPresent());
        return outAddGroupInputOptional.get();
    }
}
