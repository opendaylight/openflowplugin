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
import org.opendaylight.of.lib.instr.ActionFactory;
import org.opendaylight.of.lib.mp.MBodyGroupDescStats;
import org.opendaylight.of.lib.mp.MBodyMutableGroupDescStats;
import org.opendaylight.of.lib.mp.MpBodyFactory;
import org.opendaylight.of.lib.msg.Bucket;
import org.opendaylight.of.lib.msg.BucketFactory;
import org.opendaylight.of.lib.msg.GroupType;
import org.opendaylight.of.lib.msg.MutableBucket;
import org.opendaylight.util.json.JsonFactory;
import org.opendaylight.util.net.BigPortNumber;
import org.opendaylight.util.net.IpAddress;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.opendaylight.of.lib.ProtocolVersion.V_1_3;
import static org.opendaylight.of.lib.instr.ActionType.DEC_NW_TTL;
import static org.opendaylight.of.lib.instr.ActionType.OUTPUT;
import static org.opendaylight.of.lib.match.OxmBasicFieldType.IPV4_DST;
import static org.opendaylight.of.lib.mp.MultipartType.GROUP_DESC;
import static org.opendaylight.util.StringUtils.normalizeEOL;
import static org.opendaylight.util.json.JsonValidator.validate;
import static org.opendaylight.util.junit.TestTools.AM_NEQ;
import static org.opendaylight.util.junit.TestTools.AM_UXS;

/**
 * Unit tests for {@link MBodyGroupDescStatsCodec}.
 * @author Prashant Nayak
 *
 */
public class MBodyGroupDescStatsCodecTest extends AbstractCodecTest {

    private static final JsonFactory factory = OfJsonFactory.instance();
    private static final MBodyGroupDescStatsCodec codec = 
          (MBodyGroupDescStatsCodec) factory.codec(MBodyGroupDescStats.class);
    
    private static final String JSON_GROUP_DESC_STAT = "v13/mbodyGroupDescStat";
    private static final String JSON_GROUP_DESC_STATS = 
            "v13/mbodyGroupDescStats";
    
    private static final GroupType EXP_GRP_TYPE = GroupType.ALL;
    private static final GroupId EXP_ID = GroupId.valueOf(1);
    private static final int EXP_NUM_BUCKET = 2;
    
    private static final int EXP_B1_WEIGHT = 1;
    private static final GroupId EXP_B1_W_GROUP = GroupId.valueOf(20);
    private static final BigPortNumber EXP_B1_W_PORT = 
            BigPortNumber.valueOf(99);
    private static final int EXP_B1_NUM_ACTIONS = 3;

    private static final int EXP_B2_WEIGHT = 1;
    private static final GroupId EXP_B2_W_GROUP = GroupId.valueOf(19);
    private static final BigPortNumber EXP_B2_W_PORT = 
            BigPortNumber.valueOf(25);
    private static final BigPortNumber EXP_PORT = BigPortNumber.valueOf(1987);
    private static final IpAddress EXP_IP_ADDRESS =
                                IpAddress.valueOf("15.255.124.141");
    
    private static final GroupType EXP_GRP_1_TYPE = GroupType.FF;
    private static final GroupId EXP_GRP_1_ID = GroupId.valueOf(2);
    
    private static final int EXP_GRP_1_B1_WEIGHT = 3;
    private static final GroupId EXP_GRP_1_B1_W_GROUP = GroupId.valueOf(10);
    private static final BigPortNumber EXP_GRP_1_B1_W_PORT = 
            BigPortNumber.valueOf(90);

    @Test
    public void encode() {
        MBodyMutableGroupDescStats mgs = (MBodyMutableGroupDescStats)
                MpBodyFactory.createReplyBodyElement(V_1_3, GROUP_DESC);
        
        mgs.groupId(EXP_ID);
        mgs.groupType(EXP_GRP_TYPE);
        
        // create bucket list
        List<Bucket> bkts = new ArrayList<Bucket>();
        
        // add the first bucket
        MutableBucket bucket = BucketFactory.createMutableBucket(V_1_3);
        bucket.weight(EXP_B1_WEIGHT).watchGroup(EXP_B1_W_GROUP)
              .watchPort(EXP_B1_W_PORT)
              .addAction(ActionFactory.createAction(V_1_3, OUTPUT, EXP_PORT))
              .addAction(ActionFactory.createAction(V_1_3, DEC_NW_TTL))
              .addAction(ActionFactory.createActionSetField(V_1_3, IPV4_DST,
                                                             EXP_IP_ADDRESS));
        bkts.add(bucket);
        
        //add the second bucket
        MutableBucket bucket1 = BucketFactory.createMutableBucket(V_1_3);
        bucket1.weight(EXP_B2_WEIGHT).watchGroup(EXP_B2_W_GROUP)
               .watchPort(EXP_B2_W_PORT)
               .addAction(ActionFactory.createAction(V_1_3, OUTPUT, EXP_PORT))
               .addAction(ActionFactory.createAction(V_1_3, DEC_NW_TTL))
               .addAction(ActionFactory.createActionSetField(V_1_3, IPV4_DST,
                                                       EXP_IP_ADDRESS)); 
        bkts.add(bucket1);
        mgs.buckets(bkts);

        String exp = getJsonContents(JSON_GROUP_DESC_STAT);
        String actual = codec.encode((MBodyGroupDescStats) 
                                     mgs.toImmutable(), true);

        assertEquals(AM_NEQ, normalizeEOL(exp), normalizeEOL(actual)); 
        validate(actual, MBodyGroupDescStatsCodec.ROOT);
    }
    
    @Test
    public void decode() {
        String actual = getJsonContents(JSON_GROUP_DESC_STAT);
        MBodyGroupDescStats mBodyGroupDescStats = codec.decode(actual);
        
        assertEquals(AM_NEQ, EXP_ID, mBodyGroupDescStats.getGroupId());
        assertEquals(AM_NEQ, EXP_GRP_TYPE, mBodyGroupDescStats.getType());
        assertEquals(AM_NEQ, EXP_NUM_BUCKET, 
                     mBodyGroupDescStats.getBuckets().size());
        
        List<Bucket> buckets = mBodyGroupDescStats.getBuckets();
        Iterator<Bucket> bIter = buckets.iterator();
        
        // Bucket 0
        Bucket bkt = bIter.next();
        verifyBucketHeader(bkt, EXP_B1_WEIGHT,
                           EXP_B1_W_PORT, EXP_B1_W_GROUP, EXP_B1_NUM_ACTIONS);
        
        ActionCodecTestUtils.verifyGroupRandomActions(bkt.getActions());
        
        // Bucket 1
        bkt = bIter.next();
        verifyBucketHeader(bkt, EXP_B1_WEIGHT,
                           EXP_B2_W_PORT, EXP_B2_W_GROUP, EXP_B1_NUM_ACTIONS);
        
        ActionCodecTestUtils.verifyGroupRandomActions(bkt.getActions());
    }
    
    private void verifyBucketHeader(Bucket bkt, int expWgt, 
                                    BigPortNumber expPort, GroupId expGroup,
                                    int expNumAct) {
        assertEquals(AM_NEQ, expWgt, bkt.getWeight());
        assertEquals(AM_NEQ, expPort, bkt.getWatchPort());
        assertEquals(AM_NEQ, expGroup, bkt.getWatchGroup());
        assertEquals(AM_UXS, expNumAct, bkt.getActions().size());
    }
    
    @Test
    public void encodeList_v13() {
        List<MBodyGroupDescStats> groupDescStatList = new 
                ArrayList<MBodyGroupDescStats>();
        
        // ==== create the first group desc stats object ====
        MBodyMutableGroupDescStats mgs = (MBodyMutableGroupDescStats)
                MpBodyFactory.createReplyBodyElement(V_1_3, GROUP_DESC);
        
        mgs.groupId(EXP_ID);
        mgs.groupType(EXP_GRP_TYPE);
        
        // create bucket list
        List<Bucket> bkts = new ArrayList<Bucket>();
        
        // add the first bucket
        MutableBucket bucket = BucketFactory.createMutableBucket(V_1_3);
        bucket.weight(EXP_B1_WEIGHT).watchGroup(EXP_B1_W_GROUP)
              .watchPort(EXP_B1_W_PORT)
              .addAction(ActionFactory.createAction(V_1_3, OUTPUT, EXP_PORT))
              .addAction(ActionFactory.createAction(V_1_3, DEC_NW_TTL))
              .addAction(ActionFactory.createActionSetField(V_1_3, IPV4_DST,
                                                             EXP_IP_ADDRESS));
        bkts.add(bucket);
        
        //add the second bucket
        MutableBucket bucket1 = BucketFactory.createMutableBucket(V_1_3);
        bucket1.weight(EXP_B2_WEIGHT).watchGroup(EXP_B2_W_GROUP)
               .watchPort(EXP_B2_W_PORT)
               .addAction(ActionFactory.createAction(V_1_3, OUTPUT, EXP_PORT))
               .addAction(ActionFactory.createAction(V_1_3, DEC_NW_TTL))
               .addAction(ActionFactory.createActionSetField(V_1_3, IPV4_DST,
                                                       EXP_IP_ADDRESS)); 
        bkts.add(bucket1);
        mgs.buckets(bkts);
        groupDescStatList.add((MBodyGroupDescStats) mgs.toImmutable());
        
        // ==== create the first group desc stats object ====
        mgs = (MBodyMutableGroupDescStats)
                MpBodyFactory.createReplyBodyElement(V_1_3, GROUP_DESC);
        
        mgs.groupId(EXP_GRP_1_ID);
        mgs.groupType(EXP_GRP_1_TYPE);
        
        // create bucket list
        List<Bucket> bkts1 = new ArrayList<Bucket>();
        
        // add the first bucket
        MutableBucket bucket3 = BucketFactory.createMutableBucket(V_1_3);
        bucket3.weight(EXP_GRP_1_B1_WEIGHT).watchGroup(EXP_GRP_1_B1_W_GROUP)
              .watchPort(EXP_GRP_1_B1_W_PORT)
              .addAction(ActionFactory.createAction(V_1_3, OUTPUT, 
                                                    EXP_PORT))
              .addAction(ActionFactory.createAction(V_1_3, DEC_NW_TTL))
              .addAction(ActionFactory.createActionSetField(V_1_3, IPV4_DST,
                                                         EXP_IP_ADDRESS));
        bkts1.add(bucket3);
        
        //add the second bucket
        MutableBucket bucket4 = BucketFactory.createMutableBucket(V_1_3);
        bucket4.weight(EXP_B2_WEIGHT).watchGroup(EXP_B2_W_GROUP)
               .watchPort(EXP_B2_W_PORT)
               .addAction(ActionFactory.createAction(V_1_3, OUTPUT, EXP_PORT))
               .addAction(ActionFactory.createAction(V_1_3, DEC_NW_TTL))
               .addAction(ActionFactory.createActionSetField(V_1_3, IPV4_DST,
                                                       EXP_IP_ADDRESS));
        bkts1.add(bucket4);
        mgs.buckets(bkts1);
        groupDescStatList.add((MBodyGroupDescStats) mgs.toImmutable());
        
        String exp = getJsonContents(JSON_GROUP_DESC_STATS);
        String actual = codec.encodeList(groupDescStatList, true);

        assertEquals(AM_NEQ, normalizeEOL(exp), normalizeEOL(actual));
        validate(actual, MBodyGroupDescStatsCodec.ROOTS);
        
    }
    
    @Test
    public void decodeList_v13() {
        String actual = getJsonContents(JSON_GROUP_DESC_STATS);

        List<MBodyGroupDescStats> mBodyGroupDescStatsList = 
                codec.decodeList(actual);
        assertEquals(AM_NEQ, 2, mBodyGroupDescStatsList.size());

        MBodyGroupDescStats mBodyGroupDescStats = 
                mBodyGroupDescStatsList.get(0);
        
        assertEquals(AM_NEQ, EXP_ID, mBodyGroupDescStats.getGroupId());
        assertEquals(AM_NEQ, EXP_GRP_TYPE, mBodyGroupDescStats.getType());
        assertEquals(AM_NEQ, EXP_NUM_BUCKET, 
                     mBodyGroupDescStats.getBuckets().size());
        
        List<Bucket> buckets = mBodyGroupDescStats.getBuckets();
        Iterator<Bucket> bIter = buckets.iterator();
        
        // Bucket 0
        Bucket bkt = bIter.next();
        verifyBucketHeader(bkt, EXP_B1_WEIGHT,
                           EXP_B1_W_PORT, EXP_B1_W_GROUP, EXP_B1_NUM_ACTIONS);
       
        ActionCodecTestUtils.verifyGroupRandomActions(bkt.getActions());
        
        // Bucket 1
        bkt = bIter.next();
        verifyBucketHeader(bkt, EXP_B1_WEIGHT,
                           EXP_B2_W_PORT, EXP_B2_W_GROUP, EXP_B1_NUM_ACTIONS);
        
        ActionCodecTestUtils.verifyGroupRandomActions(bkt.getActions());
        
        mBodyGroupDescStats = mBodyGroupDescStatsList.get(1);
        
        assertEquals(AM_NEQ, EXP_GRP_1_ID, mBodyGroupDescStats.getGroupId());
        assertEquals(AM_NEQ, EXP_GRP_1_TYPE, mBodyGroupDescStats.getType());
        assertEquals(AM_NEQ, EXP_NUM_BUCKET, 
                     mBodyGroupDescStats.getBuckets().size());
        
        buckets = mBodyGroupDescStats.getBuckets();
        bIter = buckets.iterator();
        
        // Bucket 0
        bkt = bIter.next();
        verifyBucketHeader(bkt, EXP_GRP_1_B1_WEIGHT,
                          EXP_GRP_1_B1_W_PORT, EXP_GRP_1_B1_W_GROUP,
                          EXP_B1_NUM_ACTIONS);
        
        ActionCodecTestUtils.verifyGroupRandomActions(bkt.getActions());
        
     // Bucket 1
        bkt = bIter.next();
        verifyBucketHeader(bkt, EXP_B1_WEIGHT,
                          EXP_B2_W_PORT, EXP_B2_W_GROUP, EXP_B1_NUM_ACTIONS);
        
        ActionCodecTestUtils.verifyGroupRandomActions(bkt.getActions());
    }

}
