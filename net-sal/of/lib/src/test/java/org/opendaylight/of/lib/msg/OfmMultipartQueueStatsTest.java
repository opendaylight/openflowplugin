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
import org.opendaylight.of.lib.dt.QueueId;
import org.opendaylight.of.lib.mp.*;
import org.opendaylight.util.net.BigPortNumber;

import java.util.Iterator;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.opendaylight.of.lib.ProtocolVersion.V_1_0;
import static org.opendaylight.of.lib.ProtocolVersion.V_1_3;
import static org.opendaylight.of.lib.mp.MultipartType.QUEUE;
import static org.opendaylight.of.lib.msg.MessageType.MULTIPART_REPLY;
import static org.opendaylight.of.lib.msg.MessageType.MULTIPART_REQUEST;
import static org.opendaylight.util.junit.TestTools.*;

/**
 * Unit tests for OfmMultipartRequest and OfmMultipartReply messages
 * of type MultipartType.QUEUE.
 *
 * @author Shruthy Mohanram
 * @author Simon Hunt
 */
public class OfmMultipartQueueStatsTest extends OfmMultipartTest {

    // test files
    private static final String TF_QSTATS_REQ_13 = "v13/mpRequestQueue";
    private static final String TF_QSTATS_REP_13 = "v13/mpReplyQueue";
    private static final String TF_QSTATS_REP_13_TWICE = "v13/mpReplyQueueTwice";
    private static final String TF_QSTATS_REQ_10 = "v10/mpRequestQueue";
    private static final String TF_QSTATS_REP_10 = "v10/mpReplyQueue";
    private static final String TF_QSTATS_REP_10_TWICE = "v10/mpReplyQueueTwice";

    private static final BigPortNumber EXP_PORT = bpn(30);
    private static final BigPortNumber EXP_PORT_150 = bpn(150);
    private static final BigPortNumber EXP_PORT_151 = bpn(151);
    private static final BigPortNumber EXP_PORT_152 = bpn(152);
    private static final QueueId EXP_QUEUE_ID_2 = qid(0x02);
    private static final QueueId EXP_QUEUE_ID_3 = qid(0x03);
    private static final QueueId EXP_QUEUE_ID_4 = qid(0x04);
    private static final Long EXP_TX_BYTES_60 = 60l;
    private static final Long EXP_TX_BYTES_61 = 61l;
    private static final Long EXP_TX_BYTES_62 = 62l;
    private static final Long EXP_TX_PKTS_20 = 20l;
    private static final Long EXP_TX_PKTS_21 = 21l;
    private static final Long EXP_TX_PKTS_22 = 22l;
    private static final Long EXP_TX_ERRORS_5 = 5l;
    private static final Integer EXP_SECS_748 = 748;
    private static final Integer EXP_SECS_750 = 750;
    private static final Integer EXP_SECS_752 = 752;
    private static final Integer EXP_NSECS_32 = 32;
    private static final Integer EXP_NSECS_34 = 34;
    private static final Integer EXP_NSECS_36 = 36;


    // ========================================================= PARSING ====

    @Test
    public void mpRequestQueue10() {
        print(EOL + "mpRequestQueue10()");
        OfmMultipartRequest msg = (OfmMultipartRequest)
                verifyMsgHeader(TF_QSTATS_REQ_10, V_1_0, MULTIPART_REQUEST, 20);
        MBodyQueueStatsRequest body = (MBodyQueueStatsRequest)
                verifyMpHeader(msg, QUEUE);

        assertEquals(AM_NEQ, EXP_PORT, body.getPort());
        assertEquals(AM_NEQ, EXP_QUEUE_ID_2, body.getQueueId());
    }

    @Test
    public void mpReplyQueue10() throws MessageParseException {
        print(EOL + "mpReplyQueue10()");
        OfPacketReader pkt = getOfmTestReader(TF_QSTATS_REP_10);
        OpenflowMessage m = MessageFactory.parseMessage(pkt);
        print(FMT_START_TARGET, pkt.startIndex(), pkt.targetIndex());
        validateReplyQueue10(m);
    }

    @Test
    public void mpReplyQueue10Twice() throws MessageParseException {
        print(EOL + "mpReplyQueue10Twice()");
        OfPacketReader pkt = getOfmTestReader(TF_QSTATS_REP_10_TWICE);

        OpenflowMessage m = MessageFactory.parseMessage(pkt);
        print(FMT_START_TARGET, pkt.startIndex(), pkt.targetIndex());
        validateReplyQueue10(m);

        m = MessageFactory.parseMessage(pkt);
        print(FMT_START_TARGET, pkt.startIndex(), pkt.targetIndex());
        validateReplyQueue10(m);
    }

    private void validateReplyQueue10(OpenflowMessage m) {
        print(m.toDebugString());

        assertEquals(AM_NEQ, V_1_0, m.getVersion());
        assertEquals(AM_NEQ, MULTIPART_REPLY, m.getType());
        assertEquals(AM_UXS, 108, m.length());

        OfmMultipartReply msg = (OfmMultipartReply) m;
        MBodyQueueStats.Array body = (MBodyQueueStats.Array)
                verifyMpHeader(msg, QUEUE);

        List<MBodyQueueStats> stats = body.getList();
        assertEquals(AM_UXS, 3, stats.size());

        Iterator<MBodyQueueStats> qsIt = stats.iterator();

        // validate data from first queue stats...
        MBodyQueueStats qs = qsIt.next();
        verifyQueueStats(qs, V_1_0, EXP_PORT_150, EXP_QUEUE_ID_2,
                EXP_TX_BYTES_60, EXP_TX_PKTS_20, EXP_TX_ERRORS_5, 0, 0);

        // validate data from second queue stats...
        qs = qsIt.next();
        verifyQueueStats(qs, V_1_0, EXP_PORT_151, EXP_QUEUE_ID_3,
                EXP_TX_BYTES_61, EXP_TX_PKTS_21, EXP_TX_ERRORS_5, 0, 0);

        // validate data from third queue stats....
        qs = qsIt.next();
        verifyQueueStats(qs, V_1_0, EXP_PORT_152, EXP_QUEUE_ID_4,
                EXP_TX_BYTES_62, EXP_TX_PKTS_22, EXP_TX_ERRORS_5, 0, 0);
    }

    @Test
    public void mpRequestQueue13() {
        print(EOL + "mpRequestQueue13()");
        OfmMultipartRequest msg = (OfmMultipartRequest)
                verifyMsgHeader(TF_QSTATS_REQ_13, V_1_3, MULTIPART_REQUEST, 24);
        MBodyQueueStatsRequest body = (MBodyQueueStatsRequest)
                verifyMpHeader(msg, QUEUE);

        assertEquals(AM_NEQ, EXP_PORT, body.getPort());
        assertEquals(AM_NEQ, EXP_QUEUE_ID_2, body.getQueueId());
    }

    @Test
    public void mpReplyQueue13() throws MessageParseException {
        print(EOL + "mpReplyQueue13()");
        OfPacketReader pkt = getOfmTestReader(TF_QSTATS_REP_13);
        OpenflowMessage m = MessageFactory.parseMessage(pkt);
        print(FMT_START_TARGET, pkt.startIndex(), pkt.targetIndex());
        validateReplyQueue13(m);
    }

    @Test
    public void mpReplyQueue13Twice() throws MessageParseException {
        print(EOL + "mpReplyQueue13Twice()");
        OfPacketReader pkt = getOfmTestReader(TF_QSTATS_REP_13_TWICE);

        OpenflowMessage m = MessageFactory.parseMessage(pkt);
        print(FMT_START_TARGET, pkt.startIndex(), pkt.targetIndex());
        validateReplyQueue13(m);

        m = MessageFactory.parseMessage(pkt);
        print(FMT_START_TARGET, pkt.startIndex(), pkt.targetIndex());
        validateReplyQueue13(m);
    }

    private void validateReplyQueue13(OpenflowMessage m) {
        print(m.toDebugString());

        assertEquals(AM_NEQ, V_1_3, m.getVersion());
        assertEquals(AM_NEQ, MULTIPART_REPLY, m.getType());
        assertEquals(AM_UXS, 136, m.length());


        OfmMultipartReply msg = (OfmMultipartReply) m;
        MBodyQueueStats.Array body = (MBodyQueueStats.Array)
                verifyMpHeader(msg, QUEUE);

        List<MBodyQueueStats> stats = body.getList();
        assertEquals(AM_UXS, 3, stats.size());

        Iterator<MBodyQueueStats> qsIt = stats.iterator();

        // validate data from first queue stats...
        MBodyQueueStats qs = qsIt.next();
        verifyQueueStats(qs, V_1_3, EXP_PORT_150, EXP_QUEUE_ID_2,
                EXP_TX_BYTES_60, EXP_TX_PKTS_20, EXP_TX_ERRORS_5,
                EXP_SECS_748, EXP_NSECS_32);

        // validate data from second queue stats...
        qs = qsIt.next();
        verifyQueueStats(qs, V_1_3, EXP_PORT_151, EXP_QUEUE_ID_3,
                EXP_TX_BYTES_61, EXP_TX_PKTS_21, EXP_TX_ERRORS_5,
                EXP_SECS_750, EXP_NSECS_34);

        // validate data from third queue stats....
        qs = qsIt.next();
        verifyQueueStats(qs, V_1_3, EXP_PORT_152, EXP_QUEUE_ID_4,
                EXP_TX_BYTES_62, EXP_TX_PKTS_22, EXP_TX_ERRORS_5,
                EXP_SECS_752, EXP_NSECS_36);
    }

    private void verifyQueueStats(MBodyQueueStats qs, ProtocolVersion pv,
                                  BigPortNumber portNum, QueueId queueId,
                                  long txBytes, long txPkts, long txErrs,
                                  int sec, int nsec) {
        assertEquals(AM_NEQ, portNum, qs.getPort());
        assertEquals(AM_NEQ, queueId, qs.getQueueId());
        assertEquals(AM_NEQ, txBytes, qs.getTxBytes());
        assertEquals(AM_NEQ, txPkts,  qs.getTxPackets());
        assertEquals(AM_NEQ, txErrs,  qs.getTxErrors());
        if (pv == V_1_3) {
            assertEquals(AM_NEQ, sec, qs.getDurationSec());
            assertEquals(AM_NEQ, nsec, qs.getDurationNsec());
        }
    }

 // ============================================= CREATING / ENCODING ====

    @Test
    public void encodeMpRequestQueue10() {
        print(EOL + "encodeMpRequestQueue10()");
        final ProtocolVersion pv = V_1_0;
        OfmMutableMultipartRequest req = (OfmMutableMultipartRequest)
              MessageFactory.create(pv, MULTIPART_REQUEST);
        req.clearXid();

        MBodyMutableQueueStatsRequest body = (MBodyMutableQueueStatsRequest)
                MpBodyFactory.createRequestBody(pv, QUEUE);
        body.port(EXP_PORT).queueId(EXP_QUEUE_ID_2);
        req.body((MultipartBody) body.toImmutable());
        // now encode and verify
        encodeAndVerifyMessage(req.toImmutable(), TF_QSTATS_REQ_10);
    }

    @Test
    public void encodeMpRequestQueue10WithMpType() {
        print(EOL + "encodeMpRequestQueue10WithMpType()");
        final ProtocolVersion pv = V_1_0;
        OfmMutableMultipartRequest req = (OfmMutableMultipartRequest)
              MessageFactory.create(pv, MULTIPART_REQUEST, QUEUE);
        req.clearXid();
        MBodyMutableQueueStatsRequest body =
                (MBodyMutableQueueStatsRequest) req.getBody();
        body.port(EXP_PORT).queueId(EXP_QUEUE_ID_2);
        encodeAndVerifyMessage(req.toImmutable(), TF_QSTATS_REQ_10);
    }

    @Test(expected = VersionMismatchException.class)
    public void mismatchedBodyVersion() {
        MBodyMutableQueueStatsRequest body = (MBodyMutableQueueStatsRequest)
              MpBodyFactory.createRequestBody(V_1_3, QUEUE);
        body.port(EXP_PORT).queueId(EXP_QUEUE_ID_2);

        OfmMutableMultipartRequest req =  (OfmMutableMultipartRequest)
              MessageFactory.create(V_1_0, MULTIPART_REQUEST);
        req.body((MultipartBody) body.toImmutable());
    }

    @Test
    public void encodeMpRequestQueue13() {
        print(EOL + "encodeMpRequestQueue13()");
        final ProtocolVersion pv = V_1_3;
        OfmMutableMultipartRequest req = (OfmMutableMultipartRequest)
              MessageFactory.create(pv, MULTIPART_REQUEST);
        req.clearXid();

        MBodyMutableQueueStatsRequest body = (MBodyMutableQueueStatsRequest)
              MpBodyFactory.createRequestBody(pv, QUEUE);
        body.port(EXP_PORT).queueId(EXP_QUEUE_ID_2);

        req.body((MultipartBody) body.toImmutable());
        // now encode and verify
        encodeAndVerifyMessage(req.toImmutable(), TF_QSTATS_REQ_13);
    }

    @Test
    public void encodeMpRequestQueue13WithMpType() {
        print(EOL + "encodeMpRequestQueue13WithMpType()");
        final ProtocolVersion pv = V_1_3;
        OfmMutableMultipartRequest req = (OfmMutableMultipartRequest)
              MessageFactory.create(pv, MULTIPART_REQUEST, QUEUE);
        req.clearXid();
        MBodyMutableQueueStatsRequest body =
                (MBodyMutableQueueStatsRequest) req.getBody();
        body.port(EXP_PORT).queueId(EXP_QUEUE_ID_2);
        encodeAndVerifyMessage(req.toImmutable(), TF_QSTATS_REQ_13);
    }

    @Test
    public void encodeMpReplyQueue10() throws IncompleteStructureException {
        print(EOL + "encodeMpReplyQueue10()");
        final ProtocolVersion pv = V_1_0;
        OfmMutableMultipartReply rep = (OfmMutableMultipartReply)
              MessageFactory.create(pv, MULTIPART_REPLY);
        rep.clearXid();

        MBodyQueueStats.MutableArray array = (MBodyQueueStats.MutableArray)
              MpBodyFactory.createReplyBody(pv, QUEUE);

        fillOutArray10(array);

        // FINALLY ++++++++++
        rep.body((MultipartBody) array.toImmutable());
        // now encode and verify
        encodeAndVerifyMessage(rep.toImmutable(), TF_QSTATS_REP_10);
    }

    @Test
    public void encodeMpReplyQueue10WithMpType()
            throws IncompleteStructureException {
        print(EOL + "encodeMpReplyQueue10WithMpType()");
        final ProtocolVersion pv = V_1_0;
        OfmMutableMultipartReply rep = (OfmMutableMultipartReply)
              MessageFactory.create(pv, MULTIPART_REPLY, QUEUE);
        rep.clearXid();
        MBodyQueueStats.MutableArray array = (MBodyQueueStats.MutableArray) rep.getBody();
        fillOutArray10(array);
        encodeAndVerifyMessage(rep.toImmutable(), TF_QSTATS_REP_10);
    }

    private void fillOutArray10(MBodyQueueStats.MutableArray array)
            throws IncompleteStructureException {
        final ProtocolVersion pv = V_1_0;

        // ==== create the first queue stats object ====
        MBodyMutableQueueStats qfs = (MBodyMutableQueueStats)
                MpBodyFactory.createReplyBodyElement(pv, QUEUE);
        qfs.port(EXP_PORT_150).queueId(EXP_QUEUE_ID_2)
                .txBytes(EXP_TX_BYTES_60).txPackets(EXP_TX_PKTS_20)
                .txErrors(EXP_TX_ERRORS_5);
        array.addQueueStats((MBodyQueueStats) qfs.toImmutable());

        // ==== create the second queue stats object ====
        qfs = (MBodyMutableQueueStats)
                MpBodyFactory.createReplyBodyElement(pv, QUEUE);
        qfs.port(EXP_PORT_151).queueId(EXP_QUEUE_ID_3)
                .txBytes(EXP_TX_BYTES_61).txPackets(EXP_TX_PKTS_21)
                .txErrors(EXP_TX_ERRORS_5);
        array.addQueueStats((MBodyQueueStats) qfs.toImmutable());

        // ==== create the third queue stats object ====
        qfs = (MBodyMutableQueueStats)
                MpBodyFactory.createReplyBodyElement(pv, QUEUE);
        qfs.port(EXP_PORT_152).queueId(EXP_QUEUE_ID_4)
                .txBytes(EXP_TX_BYTES_62).txPackets(EXP_TX_PKTS_22)
                .txErrors(EXP_TX_ERRORS_5);
        array.addQueueStats((MBodyQueueStats) qfs.toImmutable());
    }

    @Test
    public void encodeMpReplyQueue13() throws IncompleteStructureException {
        print(EOL + "encodeMpReplyFlow13()");
        final ProtocolVersion pv = V_1_3;
        OfmMutableMultipartReply rep = (OfmMutableMultipartReply)
              MessageFactory.create(pv, MULTIPART_REPLY);
        rep.clearXid();

        MBodyQueueStats.MutableArray array = (MBodyQueueStats.MutableArray)
              MpBodyFactory.createReplyBody(pv, QUEUE);

        fillOutArray13(array);

        // FINALLY ++++++++++
        rep.body((MultipartBody) array.toImmutable());
        // now encode and verify
        encodeAndVerifyMessage(rep.toImmutable(), TF_QSTATS_REP_13);
    }

    @Test
    public void encodeMpReplyQueue13WithMpType()
            throws IncompleteStructureException {
        print(EOL + "encodeMpReplyFlow13WithMpType()");
        final ProtocolVersion pv = V_1_3;
        OfmMutableMultipartReply rep = (OfmMutableMultipartReply)
              MessageFactory.create(pv, MULTIPART_REPLY, QUEUE);
        rep.clearXid();
        MBodyQueueStats.MutableArray array =
                (MBodyQueueStats.MutableArray) rep.getBody();
        fillOutArray13(array);
        encodeAndVerifyMessage(rep.toImmutable(), TF_QSTATS_REP_13);
    }

    private void fillOutArray13(MBodyQueueStats.MutableArray array)
            throws IncompleteStructureException {
        final ProtocolVersion pv = V_1_3;

        // ==== create the first queue stats object ====
        MBodyMutableQueueStats qfs = (MBodyMutableQueueStats)
                MpBodyFactory.createReplyBodyElement(pv, QUEUE);
        qfs.port(EXP_PORT_150).queueId(EXP_QUEUE_ID_2)
                .txBytes(EXP_TX_BYTES_60).txPackets(EXP_TX_PKTS_20)
                .txErrors(EXP_TX_ERRORS_5)
                .duration(EXP_SECS_748, EXP_NSECS_32);
        array.addQueueStats((MBodyQueueStats) qfs.toImmutable());

        // ==== create the second queue stats object ====
        qfs = (MBodyMutableQueueStats)
                MpBodyFactory.createReplyBodyElement(pv, QUEUE);
        qfs.port(EXP_PORT_151).queueId(EXP_QUEUE_ID_3)
                .txBytes(EXP_TX_BYTES_61).txPackets(EXP_TX_PKTS_21)
                .txErrors(EXP_TX_ERRORS_5)
                .duration(EXP_SECS_750, EXP_NSECS_34);
        array.addQueueStats((MBodyQueueStats) qfs.toImmutable());

        // ==== create the third queue stats object ====
        qfs = (MBodyMutableQueueStats)
                MpBodyFactory.createReplyBodyElement(pv, QUEUE);
        qfs.port(EXP_PORT_152).queueId(EXP_QUEUE_ID_4)
                .txBytes(EXP_TX_BYTES_62).txPackets(EXP_TX_PKTS_22)
                .txErrors(EXP_TX_ERRORS_5)
                .duration(EXP_SECS_752, EXP_NSECS_36);
        array.addQueueStats((MBodyQueueStats) qfs.toImmutable());
    }
}
