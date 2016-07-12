/*
 * Copyright (c) 2014 Pantheon Technologies s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.openflow.md.core.sal.convertor;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import java.util.Optional;
import org.junit.Assert;
import org.junit.Test;
import org.opendaylight.openflowplugin.api.OFConstants;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.data.VersionConvertorData;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.group.statistics.buckets.BucketCounter;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.GroupId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.group._case.multipart.reply.group.GroupStats;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.group._case.multipart.reply.group.GroupStatsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.group._case.multipart.reply.group.group.stats.BucketStats;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.group._case.multipart.reply.group.group.stats.BucketStatsBuilder;

/**
 * @author michal.polkorab
 *
 */
public class GroupStatsResponseConvertorTest extends ConvertorManagerInitialization {

    /**
     * Test empty GroupStats conversion
     */
    @Test
    public void testEmptyGroupStats() {
        List<GroupStats> groupStats = new ArrayList<>();

        Optional<List<org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.group.statistics.reply
                .GroupStats>> salGroupStats = getConvertorManager().convert(groupStats, new VersionConvertorData(OFConstants.OFP_VERSION_1_3));

        Assert.assertFalse("Group stats response should be not present", salGroupStats.isPresent());
    }

    /**
     * Test single GroupStat conversion
     */
    @Test
    public void testSingleGroupStat() {
        List<GroupStats> groupStats = new ArrayList<>();
        GroupStatsBuilder statsBuilder = new GroupStatsBuilder();
        statsBuilder.setByteCount(new BigInteger("12345"));
        statsBuilder.setDurationNsec(1000000L);
        statsBuilder.setDurationSec(5000L);
        statsBuilder.setGroupId(new GroupId(42L));
        statsBuilder.setPacketCount(new BigInteger("54321"));
        statsBuilder.setRefCount(24L);
        statsBuilder.setBucketStats(new ArrayList<BucketStats>());
        groupStats.add(statsBuilder.build());

        Optional<List<org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.group.statistics.reply
                .GroupStats>> salGroupStatsOptional = getConvertorManager().convert(groupStats, new VersionConvertorData(OFConstants.OFP_VERSION_1_3));
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
        Assert.assertEquals("Wrong bucket stats", 0, stat.getBuckets().getBucketCounter().size());
    }

    /**
     * Test two GroupStats conversion
     */
    @Test
    public void testTwoGroupStats() {
        List<GroupStats> groupStats = new ArrayList<>();
        GroupStatsBuilder statsBuilder = new GroupStatsBuilder();
        statsBuilder.setByteCount(new BigInteger("12345"));
        statsBuilder.setDurationNsec(1000000L);
        statsBuilder.setDurationSec(5000L);
        statsBuilder.setGroupId(new GroupId(42L));
        statsBuilder.setPacketCount(new BigInteger("54321"));
        statsBuilder.setRefCount(24L);
        statsBuilder.setBucketStats(new ArrayList<BucketStats>());
        groupStats.add(statsBuilder.build());
        statsBuilder = new GroupStatsBuilder();
        statsBuilder.setByteCount(new BigInteger("1"));
        statsBuilder.setDurationNsec(2L);
        statsBuilder.setDurationSec(3L);
        statsBuilder.setGroupId(new GroupId(4L));
        statsBuilder.setPacketCount(new BigInteger("5"));
        statsBuilder.setRefCount(6L);
        statsBuilder.setBucketStats(new ArrayList<BucketStats>());
        groupStats.add(statsBuilder.build());

        Optional<List<org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.group.statistics.reply
                .GroupStats>> salGroupStatsOptional = getConvertorManager().convert(groupStats, new VersionConvertorData(OFConstants.OFP_VERSION_1_3));
        Assert.assertTrue("Group stats response convertor not found", salGroupStatsOptional.isPresent());
        List<org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.group.statistics.reply
                .GroupStats> salGroupStats = salGroupStatsOptional.get();

        Assert.assertEquals("Wrong group stats size", 2, salGroupStats.size());
        org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.group.statistics.reply
                .GroupStats stat = salGroupStats.get(0);
        Assert.assertEquals("Wrong group-id", 42, stat.getGroupId().getValue().intValue());
        Assert.assertEquals("Wrong key", 42, stat.getKey().getGroupId().getValue().intValue());
        Assert.assertEquals("Wrong ref-count", 24, stat.getRefCount().getValue().intValue());
        Assert.assertEquals("Wrong packet count", 54321, stat.getPacketCount().getValue().intValue());
        Assert.assertEquals("Wrong byte count", 12345, stat.getByteCount().getValue().intValue());
        Assert.assertEquals("Wrong duration sec", 5000, stat.getDuration().getSecond().getValue().intValue());
        Assert.assertEquals("Wrong duration n sec", 1000000, stat.getDuration().getNanosecond().getValue().intValue());
        Assert.assertEquals("Wrong bucket stats", 0, stat.getBuckets().getBucketCounter().size());
        stat = salGroupStats.get(1);
        Assert.assertEquals("Wrong group-id", 4, stat.getGroupId().getValue().intValue());
        Assert.assertEquals("Wrong key", 4, stat.getKey().getGroupId().getValue().intValue());
        Assert.assertEquals("Wrong ref-count", 6, stat.getRefCount().getValue().intValue());
        Assert.assertEquals("Wrong packet count", 5, stat.getPacketCount().getValue().intValue());
        Assert.assertEquals("Wrong byte count", 1, stat.getByteCount().getValue().intValue());
        Assert.assertEquals("Wrong duration sec", 3, stat.getDuration().getSecond().getValue().intValue());
        Assert.assertEquals("Wrong duration n sec", 2, stat.getDuration().getNanosecond().getValue().intValue());
        Assert.assertEquals("Wrong bucket stats", 0, stat.getBuckets().getBucketCounter().size());
    }

    /**
     * Test GroupStats with buckets conversion
     */
    @Test
    public void testGroupStatsWithBuckets() {
        List<GroupStats> groupStats = new ArrayList<>();
        GroupStatsBuilder statsBuilder = new GroupStatsBuilder();
        statsBuilder.setByteCount(new BigInteger("12345"));
        statsBuilder.setDurationNsec(1000000L);
        statsBuilder.setDurationSec(5000L);
        statsBuilder.setGroupId(new GroupId(42L));
        statsBuilder.setPacketCount(new BigInteger("54321"));
        statsBuilder.setRefCount(24L);
        List<BucketStats> bucketStats = new ArrayList<>();
        BucketStatsBuilder bucketStatsBuilder = new BucketStatsBuilder();
        bucketStatsBuilder.setByteCount(new BigInteger("987"));
        bucketStatsBuilder.setPacketCount(new BigInteger("654"));
        bucketStats.add(bucketStatsBuilder.build());
        bucketStatsBuilder = new BucketStatsBuilder();
        bucketStatsBuilder.setByteCount(new BigInteger("123"));
        bucketStatsBuilder.setPacketCount(new BigInteger("456"));
        bucketStats.add(bucketStatsBuilder.build());
        statsBuilder.setBucketStats(bucketStats);
        groupStats.add(statsBuilder.build());

        Optional<List<org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.group.statistics.reply
                .GroupStats>> salGroupStatsOptional = getConvertorManager().convert(groupStats, new VersionConvertorData(OFConstants.OFP_VERSION_1_3));
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
        List<BucketCounter> list = stat.getBuckets().getBucketCounter();
        Assert.assertEquals("Wrong bucket-id", 0, list.get(0).getBucketId().getValue().intValue());
        Assert.assertEquals("Wrong bucket packet count", 654, list.get(0).getPacketCount().getValue().intValue());
        Assert.assertEquals("Wrong bucket byte count", 987, list.get(0).getByteCount().getValue().intValue());
        Assert.assertEquals("Wrong bucket-id", 1, list.get(1).getBucketId().getValue().intValue());
        Assert.assertEquals("Wrong bucket packet count", 456, list.get(1).getPacketCount().getValue().intValue());
        Assert.assertEquals("Wrong bucket byte count", 123, list.get(1).getByteCount().getValue().intValue());
    }
}
