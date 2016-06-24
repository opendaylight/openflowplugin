/**
 * Copyright (c) 2014 Ericsson India Global Services Pvt Ltd. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.openflow.md.core.sal.convertor;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.opendaylight.openflowjava.protocol.api.util.BinContent;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.service.rev130918.AddGroupInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.service.rev130918.RemoveGroupInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.service.rev130918.group.update.UpdatedGroup;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This Utility class decodes the SAL - Group Mod Message and encodes into a OF
 * Library for the OFPT_GROUP_MOD Message. Input:SAL Layer Group command data.
 * Output:GroupModInput Message.
 *
 *
 */
public final class GroupConvertor {

    private static final Logger LOG = LoggerFactory.getLogger(GroupConvertor.class);

    private static final Integer DEFAULT_WEIGHT = 0;
    private static final Long OFPP_ANY = Long.parseLong("ffffffff", 16);
    private static final Long DEFAULT_WATCH_PORT = OFPP_ANY;
    private static final Long OFPG_ANY = Long.parseLong("ffffffff", 16);
    private static final Long DEFAULT_WATCH_GROUP = OFPG_ANY;
    private static final Comparator<Bucket> comparator = new Comparator<Bucket>(){
        @Override
        public int compare(Bucket bucket1,
                           Bucket bucket2) {
            if(bucket1.getBucketId() == null || bucket2.getBucketId() == null) return 0;
            return  bucket1.getBucketId().getValue().compareTo(bucket2.getBucketId().getValue());
        }
    };

    private GroupConvertor() {

    }

    public static GroupModInputBuilder toGroupModInput(

    org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.Group source, short version,BigInteger datapathid) {
        List<BucketsList> bucketLists = null;
        GroupModInputBuilder groupModInputBuilder = new GroupModInputBuilder();
        if (source instanceof AddGroupInput || source instanceof UpdatedGroup) {
            groupModInputBuilder.setCommand(GroupModCommand.OFPGCADD);
        } else if (source instanceof RemoveGroupInput) {
            groupModInputBuilder.setCommand(GroupModCommand.OFPGCDELETE);
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
        // During group deletion donot push the buckets
        if(groupModInputBuilder.getCommand() != GroupModCommand.OFPGCDELETE) {
            if ((source.getBuckets() != null) && (source.getBuckets().getBucket().size() != 0)) {

                Collections.sort(source.getBuckets().getBucket(), comparator);

                bucketLists = salToOFBucketList(source.getBuckets(), version, source.getGroupType().getIntValue(), datapathid);
                groupModInputBuilder.setBucketsList(bucketLists);
            }
        }
        groupModInputBuilder.setVersion(version);
        return groupModInputBuilder;

    }

    private static List<BucketsList> salToOFBucketList(Buckets buckets, short version, int groupType,BigInteger datapathid) {
        final List<BucketsList> bucketLists = new ArrayList<>();
        for (org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.group.buckets.Bucket groupBucket : buckets
                .getBucket()) {
            BucketsListBuilder bucketBuilder = new BucketsListBuilder();

            salToOFBucketListWeight(groupBucket, bucketBuilder, groupType);
            salToOFBucketListWatchGroup(groupBucket, bucketBuilder, groupType);
            salToOFBucketListWatchPort(groupBucket, bucketBuilder, groupType);

            List<Action> bucketActionList = ActionConvertor.getActions(groupBucket.getAction(), version, datapathid, null);
            bucketBuilder.setAction(bucketActionList);
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
            bucketBuilder.setWeight(groupBucket.getWeight().intValue());
        } else {
            bucketBuilder.setWeight(DEFAULT_WEIGHT);
            if (groupType == GroupType.OFPGTSELECT.getIntValue()) {
                LOG.error("Weight value required for this OFPGT_SELECT");
            }
        }
    }

}
