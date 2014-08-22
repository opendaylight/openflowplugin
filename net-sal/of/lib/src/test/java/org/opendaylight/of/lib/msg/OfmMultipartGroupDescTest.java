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
import org.opendaylight.of.lib.instr.Action;
import org.opendaylight.of.lib.instr.ActionType;
import org.opendaylight.of.lib.mp.MBodyGroupDescStats;
import org.opendaylight.of.lib.mp.MBodyMutableGroupDescStats;
import org.opendaylight.of.lib.mp.MpBodyFactory;
import org.opendaylight.of.lib.mp.MultipartBody;
import org.opendaylight.util.net.BigPortNumber;
import org.opendaylight.util.net.IpAddress;
import org.opendaylight.util.net.MacAddress;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.opendaylight.of.lib.ProtocolVersion.V_1_0;
import static org.opendaylight.of.lib.ProtocolVersion.V_1_3;
import static org.opendaylight.of.lib.instr.ActionFactory.createAction;
import static org.opendaylight.of.lib.instr.ActionFactory.createActionSetField;
import static org.opendaylight.of.lib.match.OxmBasicFieldType.ETH_DST;
import static org.opendaylight.of.lib.match.OxmBasicFieldType.IPV4_DST;
import static org.opendaylight.of.lib.mp.MultipartType.GROUP_DESC;
import static org.opendaylight.of.lib.msg.BucketFactory.createMutableBucket;
import static org.opendaylight.of.lib.msg.MessageType.MULTIPART_REPLY;
import static org.opendaylight.of.lib.msg.MessageType.MULTIPART_REQUEST;
import static org.opendaylight.util.junit.TestTools.*;

/**
 * Unit tests for the OfmMultipartRequest and OfmMultipartReply messages of
 * type MultipartType.GROUP_DESC.
 *
 * @author Prashant Nayak
 * @author Simon Hunt
 */
public class OfmMultipartGroupDescTest extends OfmMultipartTest {

    // Test files...
    private static final String TF_REQ_GD_13 = "v13/mpRequestGroupDesc";
    private static final String TF_REP_GD_13 = "v13/mpReplyGroupDesc";
    private static final String TF_REP_GD_13_TWICE = "v13/mpReplyGroupDescTwice";

    private static final GroupType EXP_GRP_TYPE = GroupType.ALL;
    private static final GroupId EXP_ID = gid(2012);
    private static final int EXP_GROUP_DESC_REP_MSG_LEN = 80;

    private static final int EXP_NUM_BUCKETS = 2;

    private static final BigPortNumber EXP_W_PORT = bpn(99);
    private static final GroupId EXP_W_GROUP = gid(2013);

    private static final int EXP_B1_LEN = 24;
    private static final int EXP_B1_WEIGHT = 8;
    private static final int EXP_B1_NUM_ACTIONS = 1;

    private static final int EXP_B2_LEN = 48;
    private static final int EXP_B2_WEIGHT = 1;
    private static final int EXP_B2_NUM_ACTIONS = 2;

    private static final MacAddress MAC = mac("00001e:453411");
    private static final IpAddress IP = ip("15.254.17.1");

    // ========================================================= PARSING ====

    private void verifyBucketHeader(Bucket bkt, int expLen, int expWgt,
                                    BigPortNumber expPort, GroupId expGroup,
                                    int expNumAct) {
        assertEquals(AM_NEQ, expLen, bkt.length);   // no getter
        assertEquals(AM_NEQ, expWgt, bkt.getWeight());
        assertEquals(AM_NEQ, expPort, bkt.getWatchPort());
        assertEquals(AM_NEQ, expGroup, bkt.getWatchGroup());
        assertEquals(AM_UXS, expNumAct, bkt.getActions().size());
    }

    @Test
    public void mpRequestGroupDesc13() {
        print(EOL + "mpRequestGroupDesc13()");
        OfmMultipartRequest msg = (OfmMultipartRequest)
                verifyMsgHeader(TF_REQ_GD_13, V_1_3, MULTIPART_REQUEST, 16);
        verifyMpHeader(msg, GROUP_DESC);
    }

    @Test
    public void mpReplyGroupDesc13() throws MessageParseException {
        print(EOL + "mpReplyGroupDesc13()");
        OfPacketReader pkt = getOfmTestReader(TF_REP_GD_13);
        OpenflowMessage m = MessageFactory.parseMessage(pkt);
        print(FMT_START_TARGET, pkt.startIndex(), pkt.targetIndex());
        validateReplyGroupDesc13(m);
    }

    @Test
    public void mpReplyGroupDesc13Twice() throws MessageParseException {
        print(EOL + "mpReplyGroupDesc13Twice()");
        OfPacketReader pkt = getOfmTestReader(TF_REP_GD_13_TWICE);

        OpenflowMessage m = MessageFactory.parseMessage(pkt);
        print(FMT_START_TARGET, pkt.startIndex(), pkt.targetIndex());
        validateReplyGroupDesc13(m);

        m = MessageFactory.parseMessage(pkt);
        print(FMT_START_TARGET, pkt.startIndex(), pkt.targetIndex());
        validateReplyGroupDesc13(m);
    }

    private void validateReplyGroupDesc13(OpenflowMessage m) {
        print(m.toDebugString());

        assertEquals(AM_NEQ, V_1_3, m.getVersion());
        assertEquals(AM_NEQ, MULTIPART_REPLY, m.getType());
        assertEquals(AM_UXS, 96, m.length());

        OfmMultipartReply msg = (OfmMultipartReply) m;

        MBodyGroupDescStats.Array stats =
                (MBodyGroupDescStats.Array) verifyMpHeader(msg, GROUP_DESC);

        List<MBodyGroupDescStats> groupDescStats = stats.getList();
        Iterator<MBodyGroupDescStats> groupDes = groupDescStats.iterator();
        MBodyGroupDescStats body = groupDes.next();

        assertEquals(AM_NEQ, EXP_GRP_TYPE, body.getType());
        assertEquals(AM_NEQ, EXP_ID, body.getGroupId());
        assertEquals(AM_NEQ, EXP_GROUP_DESC_REP_MSG_LEN, body.getTotalLength());
        assertEquals(AM_NEQ, EXP_ID, body.getGroupId());
        List<Bucket> buckets = body.getBuckets();
        assertEquals(AM_NEQ, EXP_NUM_BUCKETS, buckets.size());

        Iterator<Bucket> bIter = buckets.iterator();

        // Bucket 0
        Bucket bkt = bIter.next();
        verifyBucketHeader(bkt, EXP_B1_LEN, EXP_B1_WEIGHT,
                EXP_W_PORT, EXP_W_GROUP, EXP_B1_NUM_ACTIONS);
        Iterator<Action> aIter = bkt.getActions().iterator();
        verifyAction(aIter.next(), ActionType.DEC_NW_TTL);
        assertFalse(AM_HUH, aIter.hasNext());

        // Bucket 1
        bkt = bIter.next();
        verifyBucketHeader(bkt, EXP_B2_LEN, EXP_B2_WEIGHT,
                EXP_W_PORT, EXP_W_GROUP, EXP_B2_NUM_ACTIONS);
        aIter = bkt.getActions().iterator();
        verifyActionSetField(aIter.next(), ETH_DST, MAC);
        verifyActionSetField(aIter.next(), IPV4_DST, IP);
        assertFalse(AM_HUH, aIter.hasNext());
    }

    // ============================================= CREATING / ENCODING ====

    @Test
    public void encodeMpRequestGroupDesc13() {
        print(EOL + "encodeMpRequestGroupDesc13()");
        OfmMutableMultipartRequest req = (OfmMutableMultipartRequest)
                MessageFactory.create(V_1_3, MULTIPART_REQUEST, GROUP_DESC);
        req.clearXid();
        // now encode and verify
        encodeAndVerifyMessage(req.toImmutable(), TF_REQ_GD_13);
    }

    @Test
    public void encodeMpReplyGroupDesc13() throws IncompleteStructureException {
        print(EOL + "encodeMpReplyGroupDesc13()");
        OfmMutableMultipartReply rep = (OfmMutableMultipartReply)
                MessageFactory.create(V_1_3, MULTIPART_REPLY, GROUP_DESC);
        rep.clearXid();
        MBodyGroupDescStats.MutableArray array =
                (MBodyGroupDescStats.MutableArray) rep.getBody();
        fillOutArray(array);
        rep.body((MultipartBody) array.toImmutable());
        // now encode and verify
        encodeAndVerifyMessage(rep.toImmutable(), TF_REP_GD_13);
    }

    private void fillOutArray(MBodyGroupDescStats.MutableArray array)
            throws IncompleteStructureException {
        final ProtocolVersion pv = V_1_3;

        // create the group description stats object
        MBodyMutableGroupDescStats groupDesc = (MBodyMutableGroupDescStats)
                MpBodyFactory.createReplyBodyElement(pv, GROUP_DESC);

        groupDesc.groupId(EXP_ID).groupType(EXP_GRP_TYPE);

        // create bucket list
        List<Bucket> bkts = new ArrayList<Bucket>();

        // add the first bucket
        MutableBucket bkt1 = createMutableBucket(pv);
        bkt1.weight(EXP_B1_WEIGHT).watchPort(EXP_W_PORT).watchGroup(EXP_W_GROUP)
            .addAction(createAction(pv, ActionType.DEC_NW_TTL));
        bkts.add(bkt1);

        // add the second bucket
        MutableBucket bkt2 = createMutableBucket(pv);
        bkt2.weight(EXP_B2_WEIGHT).watchPort(EXP_W_PORT).watchGroup(EXP_W_GROUP)
            .addAction(createActionSetField(pv, ETH_DST, MAC))
            .addAction(createActionSetField(pv, IPV4_DST, IP));
        bkts.add(bkt2);

        // add bucket list to the group description stats object
        groupDesc.buckets(bkts);

        array.addGroupDesc((MBodyGroupDescStats)groupDesc.toImmutable());
    }

    @Test(expected = VersionMismatchException.class)
    public void encodeMpRequestGroupDescStats10() {
        MessageFactory.create(V_1_0, MULTIPART_REQUEST, GROUP_DESC);
    }

    @Test(expected = VersionMismatchException.class)
    public void encodeMpReplyGroupDescStats10() {
        MessageFactory.create(V_1_0, MULTIPART_REPLY, GROUP_DESC);
    }
}
