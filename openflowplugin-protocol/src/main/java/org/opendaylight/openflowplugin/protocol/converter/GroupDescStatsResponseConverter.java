/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.protocol.converter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.opendaylight.openflowplugin.api.openflow.protocol.converter.Converter;
import org.opendaylight.openflowplugin.extension.api.path.ActionPath;
import org.opendaylight.openflowplugin.protocol.converter.data.ActionResponseConverterData;
import org.opendaylight.openflowplugin.protocol.converter.data.VersionConverterData;
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
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.buckets.grouping.BucketsList;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.group.desc._case.multipart.reply.group.desc.GroupDesc;

/**
 * Converts GroupDesc message from library to MD SAL defined GroupDescStats
 *
 * Example usage:
 * <pre>
 * {@code
 * VersionConverterData data = new VersionConverterData(version);
 * Optional<List<GroupDescStats>> salGroupStats = converterManager.convert(ofGroupStats, data);
 * }
 * </pre>
 */
public class GroupDescStatsResponseConverter extends Converter<List<GroupDesc>, List<GroupDescStats>, VersionConverterData> {

    private static final Set<Class<?>> TYPES = Collections.singleton(GroupDesc.class);

    private org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.group.Buckets toSALBucketsDesc(List<BucketsList> bucketDescStats, short version) {
        final ActionResponseConverterData data = new ActionResponseConverterData(version);
        data.setActionPath(ActionPath.GROUPDESCSTATSUPDATED_GROUPDESCSTATS_BUCKETS_BUCKET_ACTION);

        org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.group.BucketsBuilder salBucketsDesc =
                new org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.group.BucketsBuilder();
        List<Bucket> allBuckets = new ArrayList<>();
        int bucketKey = 0;

        for (BucketsList bucketDetails : bucketDescStats) {
            BucketBuilder bucketDesc = new BucketBuilder();
            final Optional<List<org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.Action>> convertedSalActions =
                    getConverterExecutor().convert(
                            bucketDetails.getAction(), data);


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
            BucketId bucketId = new BucketId((long) bucketKey);
            bucketDesc.setBucketId(bucketId);
            bucketDesc.setKey(new BucketKey(bucketId));
            bucketKey++;
            allBuckets.add(bucketDesc.build());
        }

        salBucketsDesc.setBucket(allBuckets);
        return salBucketsDesc.build();
    }

    @Override
    public Collection<Class<?>> getTypes() {
        return TYPES;
    }

    @Override
    public List<GroupDescStats> convert(List<GroupDesc> source, VersionConverterData data) {
        List<GroupDescStats> convertedSALGroupsDesc = new ArrayList<>();

        for (GroupDesc groupDesc : source) {
            GroupDescStatsBuilder salGroupDescStats = new GroupDescStatsBuilder();

            salGroupDescStats.setBuckets(toSALBucketsDesc(groupDesc.getBucketsList(), data.getVersion()));
            salGroupDescStats.setGroupId(new GroupId(groupDesc.getGroupId().getValue()));
            salGroupDescStats.setGroupType(GroupTypes.forValue(groupDesc.getType().getIntValue()));
            salGroupDescStats.setKey(new GroupDescStatsKey(salGroupDescStats.getGroupId()));

            convertedSALGroupsDesc.add(salGroupDescStats.build());
        }

        return convertedSALGroupsDesc;
    }
}