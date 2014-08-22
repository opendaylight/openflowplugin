/*
 * (c) Copyright 2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.of.lib.msg;

import org.junit.Test;
import org.opendaylight.of.lib.*;
import org.opendaylight.of.lib.dt.GroupId;
import org.opendaylight.of.lib.mp.*;
import org.opendaylight.of.lib.mp.MBodyGroupStats.BucketCounter;

import java.util.Iterator;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.opendaylight.of.lib.ProtocolVersion.V_1_0;
import static org.opendaylight.of.lib.ProtocolVersion.V_1_3;
import static org.opendaylight.of.lib.mp.MultipartType.GROUP;
import static org.opendaylight.of.lib.msg.MessageType.MULTIPART_REPLY;
import static org.opendaylight.of.lib.msg.MessageType.MULTIPART_REQUEST;
import static org.opendaylight.util.junit.TestTools.*;

/**
 * Unit tests for OfmMultipartRequest and OfmMultipartReply messages
 * of type MultipartType.GROUP.
 *
 * @author Pramod Shanbhag
 * @author Simon Hunt
 */
public class OfmMultipartGroupTest extends OfmMultipartTest {

    // Test files...
    private static final String TF_GROUP_REQ_13 = "v13/mpRequestGroup";
    private static final String TF_GROUP_REP_13 = "v13/mpReplyGroup";
    private static final String TF_GROUP_REP_13_TWICE = "v13/mpReplyGroupTwice";

    private static final int EXP_GRP_LENGTH = 72;
    private static final GroupId EXP_GRP_ID = GroupId.ALL;
    private static final GroupId EXP_GRP_ID_1 = gid(0x10101032);
    private static final GroupId EXP_GRP_ID_2 = gid(0x10101033);

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

    // ========================================================= PARSING ====

    @Test
    public void mpRequestGroup13() {
        print(EOL + "mpRequestGroup13()");
        OfmMultipartRequest msg = (OfmMultipartRequest)
                verifyMsgHeader(TF_GROUP_REQ_13, V_1_3, MULTIPART_REQUEST, 24);
        MBodyGroupStatsRequest body =
                (MBodyGroupStatsRequest) verifyMpHeader(msg, GROUP);
        assertEquals(AM_NEQ, EXP_GRP_ID, body.getGroupId());
    }

    @Test
    public void mpReplyGroup13() throws MessageParseException {
        print(EOL + "mpReplyGroup13()");
        OfPacketReader pkt = getOfmTestReader(TF_GROUP_REP_13);
        OpenflowMessage m = MessageFactory.parseMessage(pkt);
        print(FMT_START_TARGET, pkt.startIndex(), pkt.targetIndex());
        validateReplyGroup13(m);
    }

    @Test
    public void mpReplyGroup13Twice() throws MessageParseException {
        print(EOL + "mpReplyGroup13Twice()");
        OfPacketReader pkt = getOfmTestReader(TF_GROUP_REP_13_TWICE);

        OpenflowMessage m = MessageFactory.parseMessage(pkt);
        print(FMT_START_TARGET, pkt.startIndex(), pkt.targetIndex());
        validateReplyGroup13(m);

        m = MessageFactory.parseMessage(pkt);
        print(FMT_START_TARGET, pkt.startIndex(), pkt.targetIndex());
        validateReplyGroup13(m);
    }

    private void validateReplyGroup13(OpenflowMessage m) {
        print(m.toDebugString());

        assertEquals(AM_NEQ, V_1_3, m.getVersion());
        assertEquals(AM_NEQ, MULTIPART_REPLY, m.getType());
        assertEquals(AM_UXS, 160, m.length());

        OfmMultipartReply msg = (OfmMultipartReply) m;
        MBodyGroupStats.Array body =
                (MBodyGroupStats.Array) verifyMpHeader(msg, GROUP);

        List<MBodyGroupStats> stats = body.getList();
        assertEquals(AM_UXS, 2, stats.size());

        Iterator<MBodyGroupStats> gsIt = stats.iterator();
        //validate data from the first group stat
        MBodyGroupStats gs = gsIt.next();
        verifyGroupStatsFixed(gs, EXP_GRP_LENGTH, EXP_GRP_ID_1,
                EXP_GRP_RF_COUNT, EXP_GRP_PKTS, EXP_GRP_BYTS, EXP_GRP_DUR,
                EXP_GRP_DUR_N);
        List<BucketCounter> bucketStats = gs.getBucketStats();
        assertEquals(AM_UXS, 2, bucketStats.size());

        Iterator<BucketCounter> bcIt = bucketStats.iterator();
        verifyBucketCounter(bcIt.next(), EXP_BKT_PKTS, EXP_BKT_BYTS);
        verifyBucketCounter(bcIt.next(), EXP_BKT_PKTS, EXP_BKT_BYTS);

        //validate data from the second group stat
        gs = gsIt.next();
        verifyGroupStatsFixed(gs,EXP_GRP_LENGTH, EXP_GRP_ID_2,
                EXP_GRP_RF_COUNT_2, EXP_GRP_PKTS_2, EXP_GRP_BYTS_2,
                EXP_GRP_DUR_2, EXP_GRP_DUR_N_2);
        bucketStats = gs.getBucketStats();
        assertEquals(AM_UXS, 2, bucketStats.size());

        bcIt = bucketStats.iterator();
        verifyBucketCounter(bcIt.next(), EXP_BKT_PKTS_2, EXP_BKT_BYTS_2);
        verifyBucketCounter(bcIt.next(), EXP_BKT_PKTS_2, EXP_BKT_BYTS_2);
    }

    private void verifyGroupStatsFixed(MBodyGroupStats gs, int length,
                                       GroupId groupId, long refCount,
                                       long packetCount, long byteCount,
                                       long durationSec, long durationNsec) {
        assertEquals(AM_NEQ, length, gs.getTotalLength());
        assertEquals(AM_NEQ, groupId, gs.getGroupId());
        assertEquals(AM_NEQ, refCount, gs.getRefCount());
        assertEquals(AM_NEQ, packetCount, gs.getPacketCount());
        assertEquals(AM_NEQ, byteCount, gs.getByteCount());
        assertEquals(AM_NEQ, durationSec, gs.getDurationSec());
        assertEquals(AM_NEQ, durationNsec, gs.getDurationNsec());
    }

    private void verifyBucketCounter(BucketCounter bc, long packetCount,
                                     long byteCount) {
        assertEquals(AM_NEQ, packetCount, bc.getPacketCount());
        assertEquals(AM_NEQ, byteCount, bc.getByteCount());
    }

    // ============================================= CREATING / ENCODING ====

    @Test
    public void defaultGroupIdIsAll() {
        print(EOL + "defaultGroupIdIsAll()");
        OfmMutableMultipartRequest req = (OfmMutableMultipartRequest)
                MessageFactory.create(V_1_3, MULTIPART_REQUEST, GROUP);
        MBodyMutableGroupStatsRequest body =
                (MBodyMutableGroupStatsRequest) req.getBody();
        print(body.toDebugString());
        assertEquals(AM_NEQ, GroupId.ALL, body.getGroupId());
    }

    @Test
    public void encodeMpRequestGroup13() {
        print(EOL + "encodeMpRequestGroup13()");
        final ProtocolVersion pv = V_1_3;
        OfmMutableMultipartRequest req = (OfmMutableMultipartRequest)
                MessageFactory.create(pv, MULTIPART_REQUEST);
        req.clearXid();
        MBodyMutableGroupStatsRequest body = (MBodyMutableGroupStatsRequest)
                MpBodyFactory.createRequestBody(pv, GROUP);
        body.groupId(EXP_GRP_ID);
        req.body((MultipartBody) body.toImmutable());
        // now encode and verify
        encodeAndVerifyMessage(req.toImmutable(), TF_GROUP_REQ_13);
    }

    @Test
    public void encodeMpRequestGroup13WithMpType() {
        print(EOL + "encodeMpRequestGroup13WithMpType()");
        final ProtocolVersion pv = V_1_3;
        OfmMutableMultipartRequest req = (OfmMutableMultipartRequest)
                MessageFactory.create(pv, MULTIPART_REQUEST, GROUP);
        req.clearXid();
        MBodyMutableGroupStatsRequest body =
                (MBodyMutableGroupStatsRequest) req.getBody();
        body.groupId(EXP_GRP_ID);
        req.body((MultipartBody) body.toImmutable());
        // now encode and verify
        encodeAndVerifyMessage(req.toImmutable(), TF_GROUP_REQ_13);
    }

    @Test(expected = VersionMismatchException.class)
    public void cantCreate10MpGroupRequest() {
        MessageFactory.create(V_1_0, MULTIPART_REQUEST, GROUP);
    }

    @Test(expected = VersionMismatchException.class)
    public void cantCreate10GroupRequestBody() {
        MpBodyFactory.createRequestBody(V_1_0, GROUP);
    }

    @Test
    public void encodeMpReplyGroup13() throws IncompleteStructureException {
        print(EOL + "encodeMpReplyGroup13()");
        final ProtocolVersion pv = V_1_3;
        OfmMutableMultipartReply rep = (OfmMutableMultipartReply)
                MessageFactory.create(pv, MULTIPART_REPLY);
        rep.clearXid();

        MBodyGroupStats.MutableArray array = (MBodyGroupStats.MutableArray)
                MpBodyFactory.createReplyBody(pv, GROUP);

        fillOutArray13(array);

        rep.body((MultipartBody) array.toImmutable());
        // now encode and verify
        encodeAndVerifyMessage(rep.toImmutable(), TF_GROUP_REP_13);
    }

    private void fillOutArray13(MBodyGroupStats.MutableArray array)
            throws IncompleteStructureException {
        final ProtocolVersion pv = V_1_3;

        // create the first group stats object
        MBodyMutableGroupStats mgs = (MBodyMutableGroupStats)
                MpBodyFactory.createReplyBodyElement(pv, GROUP);

        mgs.groupId(EXP_GRP_ID_1).refCount(EXP_GRP_RF_COUNT)
                .packetCount(EXP_GRP_PKTS).byteCount(EXP_GRP_BYTS)
                .duration(EXP_GRP_DUR, EXP_GRP_DUR_N);
        mgs.addBucketStats(EXP_BKT_PKTS, EXP_BKT_BYTS);
        mgs.addBucketStats(EXP_BKT_PKTS, EXP_BKT_BYTS);

        array.addGroupStats((MBodyGroupStats) mgs.toImmutable());

        // create the second group stats object
        mgs = (MBodyMutableGroupStats)
                MpBodyFactory.createReplyBodyElement(pv, GROUP);

        mgs.groupId(EXP_GRP_ID_2).refCount(EXP_GRP_RF_COUNT_2)
                .packetCount(EXP_GRP_PKTS_2).byteCount(EXP_GRP_BYTS_2)
                .duration(EXP_GRP_DUR_2, EXP_GRP_DUR_N_2);
        mgs.addBucketStats(EXP_BKT_PKTS_2, EXP_BKT_BYTS_2);
        mgs.addBucketStats(EXP_BKT_PKTS_2, EXP_BKT_BYTS_2);

        array.addGroupStats((MBodyGroupStats) mgs.toImmutable());
    }

    @Test(expected = VersionMismatchException.class)
    public void encodeMpReplyGroup10() {
        MpBodyFactory.createReplyBody(V_1_0, GROUP);
    }

}
