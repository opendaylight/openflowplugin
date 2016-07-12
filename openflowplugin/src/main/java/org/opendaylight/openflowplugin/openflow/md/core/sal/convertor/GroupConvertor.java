/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.openflow.md.core.sal.convertor;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import org.opendaylight.openflowjava.protocol.api.util.BinContent;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.action.data.ActionConvertorData;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.common.Convertor;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.data.VersionDatapathIdConvertorData;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.service.rev130918.AddGroupInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.service.rev130918.RemoveGroupInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.service.rev130918.group.update.UpdatedGroup;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.Group;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.GroupTypes;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.group.Buckets;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.group.buckets.Bucket;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.actions.grouping.Action;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.GroupId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.GroupModCommand;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.GroupType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.PortNumber;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.GroupModInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.buckets.grouping.BucketsList;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.buckets.grouping.BucketsListBuilder;
import org.opendaylight.yangtools.yang.binding.DataContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Decodes the SAL - Group Mod Message and encodes into a OF
 * Library for the OFPT_GROUP_MOD Message. Input:SAL Layer Group command data.
 *
 * Example usage:
 * <pre>
 * {@code
 * VersionDatapathIdConvertorData data = new VersionDatapathIdConvertorData(version);
 * data.setDatapathId(datapathId);
 * Optional<GroupModInputBuilder> ofGroup = convertorManager.convert(salGroup, data);
 * }
 * </pre>
 */
public class GroupConvertor extends Convertor<Group, GroupModInputBuilder, VersionDatapathIdConvertorData> {
    private static final List<Class<? extends DataContainer>> TYPES = Arrays.asList(Group.class, AddGroupInput.class, RemoveGroupInput.class, UpdatedGroup.class);
    /**
     * Create default empty group mod input builder
     * Use this method, if result from convertor is empty.
     *
     * @param version Openflow version
     * @return default empty group mod input builder
     */
    public static GroupModInputBuilder defaultResult(short version) {
        return new GroupModInputBuilder()
                .setVersion(version);
    }

    private static final Logger LOG = LoggerFactory.getLogger(GroupConvertor.class);
    private static final Integer DEFAULT_WEIGHT = 0;
    private static final Long OFPP_ANY = Long.parseLong("ffffffff", 16);
    private static final Long DEFAULT_WATCH_PORT = OFPP_ANY;
    private static final Long OFPG_ANY = Long.parseLong("ffffffff", 16);
    private static final Long DEFAULT_WATCH_GROUP = OFPG_ANY;
    private static final Comparator<Bucket> COMPARATOR = (bucket1, bucket2) -> {
        if (bucket1.getBucketId() == null || bucket2.getBucketId() == null) return 0;
        return bucket1.getBucketId().getValue().compareTo(bucket2.getBucketId().getValue());
    };

    private List<BucketsList> salToOFBucketList(Buckets buckets, short version, int groupType, BigInteger datapathid) {
        final List<BucketsList> bucketLists = new ArrayList<>();
        final ActionConvertorData data = new ActionConvertorData(version);
        data.setDatapathId(datapathid);

        for (org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.group.buckets.Bucket groupBucket : buckets
                .getBucket()) {
            BucketsListBuilder bucketBuilder = new BucketsListBuilder();

            salToOFBucketListWeight(groupBucket, bucketBuilder, groupType);
            salToOFBucketListWatchGroup(groupBucket, bucketBuilder, groupType);
            salToOFBucketListWatchPort(groupBucket, bucketBuilder, groupType);

            Optional<List<Action>> bucketActionList = getConvertorExecutor().convert(
                    groupBucket.getAction(), data);

            bucketBuilder.setAction(bucketActionList.orElse(Collections.emptyList()));
            BucketsList bucket = bucketBuilder.build();
            bucketLists.add(bucket);
        }

        return bucketLists;

    }

    private static void salToOFBucketListWatchPort(Bucket groupBucket, BucketsListBuilder bucketBuilder, int groupType) {
        if (null != groupBucket.getWatchPort()) {
            bucketBuilder.setWatchPort(new PortNumber(groupBucket.getWatchPort()));
        } else {
            bucketBuilder.setWatchPort(new PortNumber(BinContent.intToUnsignedLong(DEFAULT_WATCH_PORT.intValue())));
            if (groupType == GroupType.OFPGTFF.getIntValue()) {
                LOG.error("WatchPort required for this OFPGT_FF");
            }
        }
    }

    private static void salToOFBucketListWatchGroup(Bucket groupBucket, BucketsListBuilder bucketBuilder, int groupType) {
        if (null != groupBucket.getWatchGroup()) {
            bucketBuilder.setWatchGroup(groupBucket.getWatchGroup());
        } else {
            bucketBuilder.setWatchGroup(BinContent.intToUnsignedLong(DEFAULT_WATCH_GROUP.intValue()));
            if (groupType == GroupType.OFPGTFF.getIntValue()) {
                LOG.error("WatchGroup required for this OFPGT_FF");
            }
        }
    }

    private static void salToOFBucketListWeight(Bucket groupBucket, BucketsListBuilder bucketBuilder, int groupType) {
        if (null != groupBucket.getWeight()) {
            bucketBuilder.setWeight(groupBucket.getWeight());
        } else {
            bucketBuilder.setWeight(DEFAULT_WEIGHT);
            if (groupType == GroupType.OFPGTSELECT.getIntValue()) {
                LOG.error("Weight value required for this OFPGT_SELECT");
            }
        }
    }

    @Override
    public Collection<Class<? extends DataContainer>> getTypes() {
        return  TYPES;
    }

    @Override
    public GroupModInputBuilder convert(Group source, VersionDatapathIdConvertorData data) {
        GroupModInputBuilder groupModInputBuilder = new GroupModInputBuilder();
        if (source instanceof AddGroupInput) {
            groupModInputBuilder.setCommand(GroupModCommand.OFPGCADD);
        } else if (source instanceof RemoveGroupInput) {
            groupModInputBuilder.setCommand(GroupModCommand.OFPGCDELETE);
        } else if (source instanceof UpdatedGroup) {
            groupModInputBuilder.setCommand(GroupModCommand.OFPGCMODIFY);
        }

        if (GroupTypes.GroupAll.equals(source.getGroupType())) {
            groupModInputBuilder.setType(GroupType.OFPGTALL);
        }

        if (GroupTypes.GroupSelect.equals(source.getGroupType())) {
            groupModInputBuilder.setType(GroupType.OFPGTSELECT);
        }

        if (GroupTypes.GroupIndirect.equals(source.getGroupType())) {
            groupModInputBuilder.setType(GroupType.OFPGTINDIRECT);
        }

        if (GroupTypes.GroupFf.equals(source.getGroupType())) {
            groupModInputBuilder.setType(GroupType.OFPGTFF);
        }

        groupModInputBuilder.setGroupId(new GroupId(source.getGroupId().getValue()));

        // Only if the bucket is configured for the group then add it
        // During group deletion do not push the buckets
        if (groupModInputBuilder.getCommand() != GroupModCommand.OFPGCDELETE) {
            if ((source.getBuckets() != null) && (source.getBuckets().getBucket().size() != 0)) {

                Collections.sort(source.getBuckets().getBucket(), COMPARATOR);

                List<BucketsList> bucketLists = salToOFBucketList(source.getBuckets(), data.getVersion(), source.getGroupType().getIntValue(), data.getDatapathId());
                groupModInputBuilder.setBucketsList(bucketLists);
            }
        }

        groupModInputBuilder.setVersion(data.getVersion());
        return groupModInputBuilder;
    }
}