/*
 * Copyright (c) 2014 Pantheon Technologies s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.openflow.md.core.sal.convertor;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.opendaylight.openflowplugin.api.OFConstants;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.data.VersionConvertorData;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.group.statistics.buckets.BucketCounter;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.GroupId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.group._case.multipart.reply.group.GroupStats;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.group._case.multipart.reply.group.GroupStatsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.group._case.multipart.reply.group.group.stats.BucketStats;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.group._case.multipart.reply.group.group.stats.BucketStatsBuilder;
import org.opendaylight.yangtools.yang.common.Uint32;
import org.opendaylight.yangtools.yang.common.Uint64;

/**
 * Unit tests for GroupStats conversion.
 *
 * @author michal.polkorab
 */
public class GroupStatsResponseConvertorTest {
    private ConvertorManager convertorManager;

    @Before
    public void setUp() {
        convertorManager = ConvertorManagerFactory.createDefaultManager();
    }

    /**
     * Test empty GroupStats conversion.
     */
    @Test
    public void testEmptyGroupStats() {
        List<GroupStats> groupStats = new ArrayList<>();

        Optional<List<org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.group.statistics.reply
                .GroupStats>> salGroupStats = convertorManager.convert(groupStats,
                        new VersionConvertorData(OFConstants.OFP_VERSION_1_3));

        Assert.assertFalse("Group stats response should be not present", salGroupStats.isPresent());
    }

    /**
     * Test single GroupStat conversion.
     */
    @Test
    public void testSingleGroupStat() {
        GroupStatsBuilder statsBuilder = new GroupStatsBuilder();
        statsBuilder.setByteCount(Uint64.valueOf(12345));
        statsBuilder.setDurationNsec(Uint32.valueOf(1000000));
        statsBuilder.setDurationSec(Uint32.valueOf(5000));
        statsBuilder.setGroupId(new GroupId(Uint32.valueOf(42)));
        statsBuilder.setPacketCount(Uint64.valueOf(54321));
        statsBuilder.setRefCount(Uint32.valueOf(24));
        statsBuilder.setBucketStats(new ArrayList<BucketStats>());
        List<GroupStats> groupStats = new ArrayList<>();
        groupStats.add(statsBuilder.build());

        Optional<List<org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.group.statistics.reply
                .GroupStats>> salGroupStatsOptional = convertorManager.convert(groupStats,
                        new VersionConvertorData(OFConstants.OFP_VERSION_1_3));
        Assert.assertTrue("Group stats response convertor not found", salGroupStatsOptional.isPresent());
        List<org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.group.statistics.reply
                .GroupStats> salGroupStats = salGroupStatsOptional.get();

        Assert.assertEquals("Wrong group stats size", 1, salGroupStats.size());
        org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.group.statistics.reply
                .GroupStats stat = salGroupStats.get(0);
        Assert.assertEquals("Wrong group-id", 42, stat.getGroupId().getValue().intValue());
        Assert.assertEquals("Wrong ref-count", 24, stat.getRefCount().getValue().intValue());
        Assert.assertEquals("Wrong packet count", 54321, stat.getPacketCount().getValue().intValue());
        Assert.assertEquals("Wrong byte count", 12345, stat.getByteCount().getValue().intValue());
        Assert.assertEquals("Wrong duration sec", 5000, stat.getDuration().getSecond().getValue().intValue());
        Assert.assertEquals("Wrong duration n sec", 1000000, stat.getDuration().getNanosecond().getValue().intValue());
        Assert.assertEquals("Wrong bucket stats", 0, stat.getBuckets().nonnullBucketCounter().size());
    }

    /**
     * Test two GroupStats conversion.
     */
    @Test
    public void testTwoGroupStats() {
        GroupStatsBuilder statsBuilder = new GroupStatsBuilder();
        statsBuilder.setByteCount(Uint64.valueOf(12345));
        statsBuilder.setDurationNsec(Uint32.valueOf(1000000));
        statsBuilder.setDurationSec(Uint32.valueOf(5000));
        statsBuilder.setGroupId(new GroupId(Uint32.valueOf(42)));
        statsBuilder.setPacketCount(Uint64.valueOf(54321));
        statsBuilder.setRefCount(Uint32.valueOf(24));
        statsBuilder.setBucketStats(new ArrayList<BucketStats>());

        List<GroupStats> groupStats = new ArrayList<>();
        groupStats.add(statsBuilder.build());
        statsBuilder = new GroupStatsBuilder();
        statsBuilder.setByteCount(Uint64.ONE);
        statsBuilder.setDurationNsec(Uint32.valueOf(2));
        statsBuilder.setDurationSec(Uint32.valueOf(3));
        statsBuilder.setGroupId(new GroupId(Uint32.valueOf(4)));
        statsBuilder.setPacketCount(Uint64.valueOf(5));
        statsBuilder.setRefCount(Uint32.valueOf(6));
        statsBuilder.setBucketStats(new ArrayList<>());
        groupStats.add(statsBuilder.build());

        Optional<List<org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.group.statistics.reply
                .GroupStats>> salGroupStatsOptional = convertorManager.convert(groupStats,
                        new VersionConvertorData(OFConstants.OFP_VERSION_1_3));
        Assert.assertTrue("Group stats response convertor not found", salGroupStatsOptional.isPresent());
        List<org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.group.statistics.reply
                .GroupStats> salGroupStats = salGroupStatsOptional.get();

        Assert.assertEquals("Wrong group stats size", 2, salGroupStats.size());
        org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.group.statistics.reply
                .GroupStats stat = salGroupStats.get(0);
        Assert.assertEquals("Wrong group-id", 42, stat.getGroupId().getValue().intValue());
        Assert.assertEquals("Wrong key", 42, stat.key().getGroupId().getValue().intValue());
        Assert.assertEquals("Wrong ref-count", 24, stat.getRefCount().getValue().intValue());
        Assert.assertEquals("Wrong packet count", 54321, stat.getPacketCount().getValue().intValue());
        Assert.assertEquals("Wrong byte count", 12345, stat.getByteCount().getValue().intValue());
        Assert.assertEquals("Wrong duration sec", 5000, stat.getDuration().getSecond().getValue().intValue());
        Assert.assertEquals("Wrong duration n sec", 1000000, stat.getDuration().getNanosecond().getValue().intValue());
        Assert.assertEquals("Wrong bucket stats", 0, stat.getBuckets().nonnullBucketCounter().size());
        stat = salGroupStats.get(1);
        Assert.assertEquals("Wrong group-id", 4, stat.getGroupId().getValue().intValue());
        Assert.assertEquals("Wrong key", 4, stat.key().getGroupId().getValue().intValue());
        Assert.assertEquals("Wrong ref-count", 6, stat.getRefCount().getValue().intValue());
        Assert.assertEquals("Wrong packet count", 5, stat.getPacketCount().getValue().intValue());
        Assert.assertEquals("Wrong byte count", 1, stat.getByteCount().getValue().intValue());
        Assert.assertEquals("Wrong duration sec", 3, stat.getDuration().getSecond().getValue().intValue());
        Assert.assertEquals("Wrong duration n sec", 2, stat.getDuration().getNanosecond().getValue().intValue());
        Assert.assertEquals("Wrong bucket stats", 0, stat.getBuckets().nonnullBucketCounter().size());
    }

    /**
     * Test GroupStats with buckets conversion.
     */
    @Test
    public void testGroupStatsWithBuckets() {
        GroupStatsBuilder statsBuilder = new GroupStatsBuilder();
        statsBuilder.setByteCount(Uint64.valueOf(12345));
        statsBuilder.setDurationNsec(Uint32.valueOf(1000000));
        statsBuilder.setDurationSec(Uint32.valueOf(5000));
        statsBuilder.setGroupId(new GroupId(Uint32.valueOf(42)));
        statsBuilder.setPacketCount(Uint64.valueOf(54321));
        statsBuilder.setRefCount(Uint32.valueOf(24));
        List<BucketStats> bucketStats = new ArrayList<>();
        BucketStatsBuilder bucketStatsBuilder = new BucketStatsBuilder();
        bucketStatsBuilder.setByteCount(Uint64.valueOf(987));
        bucketStatsBuilder.setPacketCount(Uint64.valueOf(654));
        bucketStats.add(bucketStatsBuilder.build());
        bucketStatsBuilder = new BucketStatsBuilder();
        bucketStatsBuilder.setByteCount(Uint64.valueOf(123));
        bucketStatsBuilder.setPacketCount(Uint64.valueOf(456));
        bucketStats.add(bucketStatsBuilder.build());
        statsBuilder.setBucketStats(bucketStats);

        List<GroupStats> groupStats = new ArrayList<>();
        groupStats.add(statsBuilder.build());

        Optional<List<org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.group.statistics.reply
                .GroupStats>> salGroupStatsOptional = convertorManager.convert(groupStats,
                        new VersionConvertorData(OFConstants.OFP_VERSION_1_3));
        Assert.assertTrue("Group stats response convertor not found", salGroupStatsOptional.isPresent());
        List<org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.group.statistics.reply
                .GroupStats> salGroupStats = salGroupStatsOptional.get();

        Assert.assertEquals("Wrong group stats size", 1, salGroupStats.size());
        org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.group.statistics.reply
                .GroupStats stat = salGroupStats.get(0);
        Assert.assertEquals("Wrong group-id", 42, stat.getGroupId().getValue().intValue());
        Assert.assertEquals("Wrong ref-count", 24, stat.getRefCount().getValue().intValue());
        Assert.assertEquals("Wrong packet count", 54321, stat.getPacketCount().getValue().intValue());
        Assert.assertEquals("Wrong byte count", 12345, stat.getByteCount().getValue().intValue());
        Assert.assertEquals("Wrong duration sec", 5000, stat.getDuration().getSecond().getValue().intValue());
        Assert.assertEquals("Wrong duration n sec", 1000000, stat.getDuration().getNanosecond().getValue().intValue());
        Assert.assertEquals("Wrong bucket stats", 2, stat.getBuckets().getBucketCounter().size());
        Iterator<BucketCounter> it = stat.getBuckets().nonnullBucketCounter().values().iterator();
        BucketCounter counter = it.next();
        Assert.assertEquals("Wrong bucket-id", 0, counter.getBucketId().getValue().intValue());
        Assert.assertEquals("Wrong bucket packet count", 654, counter.getPacketCount().getValue().intValue());
        Assert.assertEquals("Wrong bucket byte count", 987, counter.getByteCount().getValue().intValue());
        counter = it.next();
        Assert.assertEquals("Wrong bucket-id", 1, counter.getBucketId().getValue().intValue());
        Assert.assertEquals("Wrong bucket packet count", 456, counter.getPacketCount().getValue().intValue());
        Assert.assertEquals("Wrong bucket byte count", 123, counter.getByteCount().getValue().intValue());
    }
}
