/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.openflow.md.core.sal.convertor;

import com.google.common.collect.ImmutableMap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.common.Convertor;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.data.VersionConvertorData;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev130715.Counter32;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev130715.Counter64;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.BucketId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.GroupId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.group.statistics.Buckets;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.group.statistics.BucketsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.group.statistics.DurationBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.group.statistics.buckets.BucketCounter;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.group.statistics.buckets.BucketCounterBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.group.statistics.buckets.BucketCounterKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.group.statistics.reply.GroupStats;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.group.statistics.reply.GroupStatsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.group.statistics.reply.GroupStatsKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.group._case.multipart.reply.group.group.stats.BucketStats;
import org.opendaylight.yangtools.yang.common.Uint32;

/**
 * Converts group related statistics messages coming from switch to MD-SAL messages.
 *
 * <p>
 * Example usage:
 * <pre>
 * {@code
 * VersionConvertorData data = new VersionConvertorData(version);
 * Optional<List<GroupStats>> salGroupStats = convertorManager.convert(ofGroupStats, data);
 * }
 * </pre>
 */
public class GroupStatsResponseConvertor extends Convertor<
        List<org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart
            .reply.body.multipart.reply.group._case.multipart.reply.group.GroupStats>,
        List<GroupStats>,
        VersionConvertorData> {

    private static final Set<Class<?>> TYPES = Collections.singleton(org.opendaylight.yang.gen.v1.urn.opendaylight
            .openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.group._case.multipart
                .reply.group.GroupStats.class);

    private static Buckets toSALBuckets(List<BucketStats> bucketStats) {
        final ImmutableMap.Builder<BucketCounterKey, BucketCounter> allBucketStats =
                ImmutableMap.builderWithExpectedSize(bucketStats.size());

        int bucketKey = 0;
        for (BucketStats bucketStat : bucketStats) {
            final BucketCounterKey key = new BucketCounterKey(new BucketId(Uint32.valueOf(bucketKey++)));
            allBucketStats.put(key, new BucketCounterBuilder()
                .withKey(key)
                .setByteCount(new Counter64(bucketStat.getByteCount()))
                .setPacketCount(new Counter64(bucketStat.getPacketCount()))
                .build());
        }

        return new BucketsBuilder().setBucketCounter(allBucketStats.build()).build();
    }

    @Override
    public Collection<Class<?>> getTypes() {
        return TYPES;
    }

    @Override
    public List<GroupStats> convert(List<org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731
            .multipart.reply.multipart.reply.body.multipart.reply.group._case.multipart.reply.group.GroupStats> source,
            VersionConvertorData data) {
        List<GroupStats> convertedSALGroups = new ArrayList<>();

        for (org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply
                .body.multipart.reply.group._case.multipart.reply.group.GroupStats groupStats : source) {
            convertedSALGroups.add(new GroupStatsBuilder()
                .withKey(new GroupStatsKey(new GroupId(groupStats.getGroupId().getValue())))
                .setBuckets(toSALBuckets(groupStats.nonnullBucketStats()))
                .setByteCount(new Counter64(groupStats.getByteCount()))
                .setDuration(new DurationBuilder()
                    .setSecond(new Counter32(groupStats.getDurationSec()))
                    .setNanosecond(new Counter32(groupStats.getDurationNsec()))
                    .build())
                .setPacketCount(new Counter64(groupStats.getPacketCount()))
                .setRefCount(new Counter32(groupStats.getRefCount()))
                .build());
        }

        return convertedSALGroups;
    }
}
