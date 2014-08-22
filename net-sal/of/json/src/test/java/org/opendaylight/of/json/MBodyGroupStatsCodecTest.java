/*
 * (c) Copyright 2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.of.json;

import org.junit.Test;
import org.opendaylight.of.lib.dt.GroupId;
import org.opendaylight.of.lib.mp.MBodyGroupStats;
import org.opendaylight.of.lib.mp.MBodyGroupStats.BucketCounter;
import org.opendaylight.of.lib.mp.MBodyMutableGroupStats;
import org.opendaylight.of.lib.mp.MpBodyFactory;
import org.opendaylight.util.json.JsonFactory;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.opendaylight.of.lib.ProtocolVersion.V_1_3;
import static org.opendaylight.of.lib.mp.MultipartType.GROUP;
import static org.opendaylight.util.StringUtils.normalizeEOL;
import static org.opendaylight.util.json.JsonValidator.validate;
import static org.opendaylight.util.junit.TestTools.AM_NEQ;

/**
 * Unit tests for {@link MBodyGroupStatsCodec}.
 * 
 * @author Prashant Nayak
 */
public class MBodyGroupStatsCodecTest extends AbstractCodecTest {

    private static final JsonFactory factory = OfJsonFactory.instance();
    private static final MBodyGroupStatsCodec codec = (MBodyGroupStatsCodec) factory
        .codec(MBodyGroupStats.class);

    private static final String JSON_GROUP_STAT = "v13/mbodyGroupStat";
    private static final String JSON_GROUP_STATS = "v13/mbodyGroupStats";

    private static final GroupId EXP_GRP_ID_1 = GroupId.valueOf(1);
    private static final GroupId EXP_GRP_ID_2 = GroupId.valueOf(2);;

    private static final long EXP_GRP_RF_COUNT = 8;
    private static final long EXP_GRP_PKTS = 4660;
    private static final long EXP_GRP_BYTS = 13398;
    private static final long EXP_GRP_DUR = 430;
    private static final long EXP_GRP_DUR_N = 25;
    private static final long EXP_BKT_PKTS = 4660;
    private static final long EXP_BKT_BYTS = 13398;

    private static final long EXP_GRP_RF_COUNT_2 = 9;
    private static final long EXP_GRP_PKTS_2 = 22136;
    private static final long EXP_GRP_BYTS_2 = 30864;
    private static final long EXP_GRP_DUR_2 = 560;
    private static final long EXP_GRP_DUR_N_2 = 26;
    private static final long EXP_BKT_PKTS_2 = 22136;
    private static final long EXP_BKT_BYTS_2 = 30864;

    @Test
    public void decode() {
        String actual = getJsonContents(JSON_GROUP_STAT);
        MBodyGroupStats mBodyGroupStats = codec.decode(actual);

        assertEquals(AM_NEQ, EXP_GRP_ID_1, mBodyGroupStats.getGroupId());
        assertEquals(AM_NEQ, EXP_GRP_RF_COUNT, mBodyGroupStats.getRefCount());
        assertEquals(AM_NEQ, EXP_GRP_PKTS, mBodyGroupStats.getPacketCount());
        assertEquals(AM_NEQ, EXP_GRP_BYTS, mBodyGroupStats.getByteCount());
        assertEquals(AM_NEQ, EXP_GRP_DUR, mBodyGroupStats.getDurationSec());
        assertEquals(AM_NEQ, EXP_GRP_DUR_N, mBodyGroupStats.getDurationNsec());

        List<BucketCounter> bktStats = mBodyGroupStats.getBucketStats();
        Iterator<BucketCounter> bIter = bktStats.iterator();

        // Bucket 0
        BucketCounter bkt = bIter.next();
        assertEquals(AM_NEQ, EXP_GRP_PKTS, bkt.getPacketCount());
        assertEquals(AM_NEQ, EXP_GRP_BYTS, bkt.getByteCount());

        // Bucket 1
        bkt = bIter.next();
        assertEquals(AM_NEQ, EXP_GRP_PKTS, bkt.getPacketCount());
        assertEquals(AM_NEQ, EXP_GRP_BYTS, bkt.getByteCount());

    }

    @Test
    public void decodeList() {

        String actual = getJsonContents(JSON_GROUP_STATS);

        List<MBodyGroupStats> mBodyGroupStatsList = codec.decodeList(actual);
        assertEquals(AM_NEQ, 2, mBodyGroupStatsList.size());

        MBodyGroupStats mBodyGroupStats = mBodyGroupStatsList.get(0);

        assertEquals(AM_NEQ, EXP_GRP_ID_1, mBodyGroupStats.getGroupId());
        assertEquals(AM_NEQ, EXP_GRP_RF_COUNT, mBodyGroupStats.getRefCount());
        assertEquals(AM_NEQ, EXP_GRP_PKTS, mBodyGroupStats.getPacketCount());
        assertEquals(AM_NEQ, EXP_GRP_BYTS, mBodyGroupStats.getByteCount());
        assertEquals(AM_NEQ, EXP_GRP_DUR, mBodyGroupStats.getDurationSec());
        assertEquals(AM_NEQ, EXP_GRP_DUR_N, mBodyGroupStats.getDurationNsec());

        List<BucketCounter> bktStats = mBodyGroupStats.getBucketStats();
        Iterator<BucketCounter> bIter = bktStats.iterator();

        // Bucket 0
        BucketCounter bkt = bIter.next();
        assertEquals(AM_NEQ, EXP_GRP_PKTS, bkt.getPacketCount());
        assertEquals(AM_NEQ, EXP_GRP_BYTS, bkt.getByteCount());

        mBodyGroupStats = mBodyGroupStatsList.get(1);

        assertEquals(AM_NEQ, EXP_GRP_ID_2, mBodyGroupStats.getGroupId());
        assertEquals(AM_NEQ, EXP_GRP_RF_COUNT_2, mBodyGroupStats.getRefCount());
        assertEquals(AM_NEQ, EXP_GRP_PKTS_2, mBodyGroupStats.getPacketCount());
        assertEquals(AM_NEQ, EXP_GRP_BYTS_2, mBodyGroupStats.getByteCount());
        assertEquals(AM_NEQ, EXP_GRP_DUR_2, mBodyGroupStats.getDurationSec());
        assertEquals(AM_NEQ, EXP_GRP_DUR_N_2, mBodyGroupStats.getDurationNsec());

        bktStats = mBodyGroupStats.getBucketStats();
        bIter = bktStats.iterator();

        // Bucket 0
        bkt = bIter.next();
        assertEquals(AM_NEQ, EXP_GRP_PKTS, bkt.getPacketCount());
        assertEquals(AM_NEQ, EXP_GRP_BYTS, bkt.getByteCount());

        // Bucket 1
        bkt = bIter.next();
        assertEquals(AM_NEQ, EXP_GRP_PKTS_2, bkt.getPacketCount());
        assertEquals(AM_NEQ, EXP_GRP_BYTS_2, bkt.getByteCount());

    }

    @Test
    public void encodeList() {

        List<MBodyGroupStats> groupStatList = new ArrayList<MBodyGroupStats>();
        
        // create the first group stats object
        MBodyMutableGroupStats mgs = (MBodyMutableGroupStats) MpBodyFactory
            .createReplyBodyElement(V_1_3, GROUP);

        mgs.groupId(EXP_GRP_ID_1).refCount(EXP_GRP_RF_COUNT)
            .packetCount(EXP_GRP_PKTS).byteCount(EXP_GRP_BYTS)
            .duration(EXP_GRP_DUR, EXP_GRP_DUR_N);
        mgs.addBucketStats(EXP_BKT_PKTS, EXP_BKT_BYTS);

        groupStatList.add((MBodyGroupStats) mgs.toImmutable());

        // create the second group stats object
        mgs = (MBodyMutableGroupStats) MpBodyFactory
            .createReplyBodyElement(V_1_3, GROUP);

        mgs.groupId(EXP_GRP_ID_2).refCount(EXP_GRP_RF_COUNT_2)
            .packetCount(EXP_GRP_PKTS_2).byteCount(EXP_GRP_BYTS_2)
            .duration(EXP_GRP_DUR_2, EXP_GRP_DUR_N_2);
        mgs.addBucketStats(EXP_BKT_PKTS, EXP_BKT_BYTS);
        mgs.addBucketStats(EXP_BKT_PKTS_2, EXP_BKT_BYTS_2);

        groupStatList.add((MBodyGroupStats) mgs.toImmutable());

        String exp = getJsonContents(JSON_GROUP_STATS);
        String actual = codec.encodeList(groupStatList, true);

        assertEquals(AM_NEQ, normalizeEOL(exp), normalizeEOL(actual));
        validate(actual, MBodyGroupStatsCodec.ROOTS);

    }

}
