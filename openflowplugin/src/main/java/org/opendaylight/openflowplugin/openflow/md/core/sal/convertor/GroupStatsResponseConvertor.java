/*
 * Copyright (c) 2013, 2015 IBM Corporation and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.openflow.md.core.sal.convertor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.opendaylight.openflowplugin.api.openflow.md.util.OpenflowVersion;
import org.opendaylight.openflowplugin.extension.api.path.ActionPath;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.action.data.ActionResponseConvertorData;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev100924.Counter32;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev100924.Counter64;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.list.Action;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.list.ActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.list.ActionKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.BucketId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.GroupId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.GroupTypes;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.group.buckets.Bucket;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.group.buckets.BucketBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.group.buckets.BucketKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.group.desc.stats.reply.GroupDescStats;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.group.desc.stats.reply.GroupDescStatsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.group.desc.stats.reply.GroupDescStatsKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.group.statistics.Buckets;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.group.statistics.BucketsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.group.statistics.DurationBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.group.statistics.buckets.BucketCounter;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.group.statistics.buckets.BucketCounterBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.group.statistics.buckets.BucketCounterKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.group.statistics.reply.GroupStats;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.group.statistics.reply.GroupStatsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.group.statistics.reply.GroupStatsKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.buckets.grouping.BucketsList;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.group._case.multipart.reply.group.group.stats.BucketStats;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.group.desc._case.multipart.reply.group.desc.GroupDesc;

/**
 * Class is an utility class for converting group related statistics messages coming from switch to MD-SAL
 * messages.
 * @author avishnoi@in.ibm.com
 *
 */
public class GroupStatsResponseConvertor {

    public List<GroupStats> toSALGroupStatsList(
            List<org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply
            .multipart.reply.body.multipart.reply.group._case.multipart.reply.group.GroupStats> allGroupStats){
        List<GroupStats> convertedSALGroups = new ArrayList<>();
        for(org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply
                .multipart.reply.body.multipart.reply.group._case.multipart.reply.group.GroupStats group: allGroupStats){
            convertedSALGroups.add(toSALGroupStats(group));
        }
        return convertedSALGroups;

    }
    /**
     * Method convert GroupStats message from library to MD SAL defined GroupStats
     * @param groupStats GroupStats from library
     * @return GroupStats -- GroupStats defined in MD-SAL
     */
    public GroupStats toSALGroupStats(
            org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.
            multipart.reply.body.multipart.reply.group._case.multipart.reply.group.GroupStats groupStats){

        GroupStatsBuilder salGroupStats = new GroupStatsBuilder();

        salGroupStats.setBuckets(toSALBuckets(groupStats.getBucketStats()));
        salGroupStats.setByteCount(new Counter64(groupStats.getByteCount()));

        DurationBuilder time = new DurationBuilder();
        time.setSecond(new Counter32(groupStats.getDurationSec()));
        time.setNanosecond(new Counter32(groupStats.getDurationNsec()));

        salGroupStats.setDuration(time.build());
        salGroupStats.setGroupId(new GroupId(groupStats.getGroupId().getValue()));
        salGroupStats.setPacketCount(new Counter64(groupStats.getPacketCount()));
        salGroupStats.setRefCount(new Counter32(groupStats.getRefCount()));
        salGroupStats.setKey(new GroupStatsKey(salGroupStats.getGroupId()));

        return salGroupStats.build();
    }

    public Buckets toSALBuckets(
            List<BucketStats> bucketStats ){

        BucketsBuilder salBuckets  = new BucketsBuilder();

        List<BucketCounter> allBucketStats = new ArrayList<>();
        int bucketKey = 0;
        for(BucketStats bucketStat : bucketStats){
            BucketCounterBuilder bucketCounter = new BucketCounterBuilder();
            bucketCounter.setByteCount(new Counter64(bucketStat.getByteCount()));
            bucketCounter.setPacketCount(new Counter64(bucketStat.getPacketCount()));
            BucketId bucketId = new BucketId((long)bucketKey);
            bucketCounter.setKey(new BucketCounterKey(bucketId));
            bucketCounter.setBucketId(bucketId);
            bucketKey++;
            allBucketStats.add(bucketCounter.build());
        }
        salBuckets.setBucketCounter(allBucketStats);
        return salBuckets.build();
    }


    public List<GroupDescStats> toSALGroupDescStatsList(
            List<GroupDesc> allGroupDescStats, OpenflowVersion ofVersion){

        List<GroupDescStats> convertedSALGroupsDesc = new ArrayList<>();
        for(GroupDesc groupDesc: allGroupDescStats){
            convertedSALGroupsDesc.add(toSALGroupDescStats(groupDesc, ofVersion));
        }
        return convertedSALGroupsDesc;

    }
    /**
     * Method convert GroupStats message from library to MD SAL defined GroupStats
     * @param groupDesc GroupStats from library
     * @param ofVersion current ofp version
     * @return GroupStats -- GroupStats defined in MD-SAL
     */
    public GroupDescStats toSALGroupDescStats(GroupDesc groupDesc, OpenflowVersion ofVersion){

        GroupDescStatsBuilder salGroupDescStats = new GroupDescStatsBuilder();

        salGroupDescStats.setBuckets(toSALBucketsDesc(groupDesc.getBucketsList(), ofVersion));
        salGroupDescStats.setGroupId(new GroupId(groupDesc.getGroupId().getValue()));
        salGroupDescStats.setGroupType(GroupTypes.forValue(groupDesc.getType().getIntValue()));
        salGroupDescStats.setKey(new GroupDescStatsKey(salGroupDescStats.getGroupId()));

        return salGroupDescStats.build();
    }

    public  org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.group.Buckets toSALBucketsDesc(
            List<BucketsList> bucketDescStats, OpenflowVersion ofVersion ){
        final ActionResponseConvertorData data = new ActionResponseConvertorData(ofVersion.getVersion());
        data.setActionPath(ActionPath.GROUPDESCSTATSUPDATED_GROUPDESCSTATS_BUCKETS_BUCKET_ACTION);

        org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.group.BucketsBuilder salBucketsDesc  =
                new org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.group.BucketsBuilder();
        List<Bucket> allBuckets = new ArrayList<>();
        int bucketKey = 0;
        for(BucketsList bucketDetails : bucketDescStats){
            BucketBuilder bucketDesc = new BucketBuilder();
            final Optional<List<org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.Action>> convertedSalActions =
                    ConvertorManager.getInstance().convert(bucketDetails.getAction(), data);

            if (convertedSalActions.isPresent()) {
                List<Action> actions = new ArrayList<>();
                int actionKey = 0;
                for (org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.Action action : convertedSalActions.get()) {
                    ActionBuilder wrappedAction = new ActionBuilder();
                    wrappedAction.setAction(action);
                    wrappedAction.setKey(new ActionKey(actionKey));
                    wrappedAction.setOrder(actionKey);
                    actions.add(wrappedAction.build());
                    actionKey++;
                }

                bucketDesc.setAction(actions);
            } else {
                bucketDesc.setAction(Collections.emptyList());
            }

            bucketDesc.setWeight(bucketDetails.getWeight());
            bucketDesc.setWatchPort(bucketDetails.getWatchPort().getValue());
            bucketDesc.setWatchGroup(bucketDetails.getWatchGroup());
            BucketId bucketId = new BucketId((long)bucketKey);
            bucketDesc.setBucketId(bucketId);
            bucketDesc.setKey(new BucketKey(bucketId));
            bucketKey++;
            allBuckets.add(bucketDesc.build());
        }
        salBucketsDesc.setBucket(allBuckets);
        return salBucketsDesc.build();
    }

}
