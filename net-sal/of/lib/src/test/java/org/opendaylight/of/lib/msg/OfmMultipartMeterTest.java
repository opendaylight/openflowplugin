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
import org.opendaylight.of.lib.dt.MeterId;
import org.opendaylight.of.lib.mp.*;

import java.util.Iterator;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.opendaylight.of.lib.ProtocolVersion.*;
import static org.opendaylight.of.lib.mp.MBodyMeterStats.MeterBandStats;
import static org.opendaylight.of.lib.mp.MultipartType.METER;
import static org.opendaylight.of.lib.msg.MessageType.MULTIPART_REPLY;
import static org.opendaylight.of.lib.msg.MessageType.MULTIPART_REQUEST;
import static org.opendaylight.util.junit.TestTools.*;

/**
 * Unit tests for the OfmMultipartRequest and OfmMultipartReply messages of
 * type MultipartType.METER.
 *
 * @author Scott Simes
 * @author Simon Hunt
 */
public class OfmMultipartMeterTest extends OfmMultipartTest {

    // test files
    private static final String TF_REQ_13 = "v13/mpRequestMeter";
    private static final String TF_REPLY_13 = "v13/mpReplyMeter";
    private static final String TF_REPLY_13_TWICE = "v13/mpReplyMeterTwice";

    // expected values
    private static final MeterId EXP_METER_ID = MeterId.SLOWPATH;

    // meter stat 0
    private static final MeterId EXP_MI_0 = EXP_METER_ID;
    private static final long EXP_FC_0 = 100l;
    private static final long EXP_PIC_0 = 1000l;
    private static final long EXP_BIC_0 = 10000l;
    private static final long EXP_DUR_0 = 100000l;
    private static final long EXP_DUR_NS_0 = 1000000l;

    // meter stat 1
    private static final MeterId EXP_MI_1 = MeterId.valueOf(42l);
    private static final long EXP_FC_1 = 200l;
    private static final long EXP_PIC_1 = 2000l;
    private static final long EXP_BIC_1 = 20000l;
    private static final long EXP_DUR_1 = 200000l;
    private static final long EXP_DUR_NS_1 = 2000000l;

    // meter band stats
    private static final long EXP_MBS_A_PKT = 42l;
    private static final long EXP_MBS_A_BYTE = 7l;

    private static final long EXP_MBS_B_PKT = 9876543210l;
    private static final long EXP_MBS_B_BYTE = 1234567890l;

    private static final long EXP_MBS_C_PKT = 2420242l;
    private static final long EXP_MBS_C_BYTE = 4420442l;

    private static final long EXP_MBS_D_PKT = 112233445566778899l;
    private static final long EXP_MBS_D_BYTE = 998877665544332211l;

    private static final long EXP_MBS_E_PKT = 111122223333l;
    private static final long EXP_MBS_E_BYTE = 444455556666l;

    // provide asserts for a given meter stats body element
    private void checkMeterStats(MBodyMeterStats mstat, MeterId expMeterId,
                                 long expFlowCnt, long expPktInCnt,
                                 long expByteInCnt, long expDurSec,
                                 long expDurNsec) {
        assertEquals(AM_NEQ, expMeterId, mstat.getMeterId());
        assertEquals(AM_NEQ, expFlowCnt, mstat.getFlowCount());
        assertEquals(AM_NEQ, expPktInCnt, mstat.getPktInCount());
        assertEquals(AM_NEQ, expByteInCnt, mstat.getByteInCount());
        assertEquals(AM_NEQ, expDurSec, mstat.getDurationSec());
        assertEquals(AM_NEQ, expDurNsec, mstat.getDurationNSec());
    }

    // provides asserts for a given meter band stats element
    private void checkMeterBandStats(MeterBandStats mbs, long expPkt,
                                     long expByte) {
        assertEquals(AM_NEQ, expPkt, mbs.getPacketBandCount());
        assertEquals(AM_NEQ, expByte, mbs.getByteBandCount());
    }

    //===================================================== PARSING ==========

    @Test
    public void mpRequestMeter13() {
        print(EOL + "mpRequestMeter13()");
        OfmMultipartRequest msg = (OfmMultipartRequest)
                verifyMsgHeader(TF_REQ_13, V_1_3, MULTIPART_REQUEST, 24);
        MBodyMeterStatsRequest body = (MBodyMeterStatsRequest)
                verifyMpHeader(msg, METER);
        assertEquals(AM_NEQ, EXP_METER_ID, body.getMeterId());
    }

    @Test
    public void mpReplyMeter13() throws MessageParseException {
        print(EOL + "mpReplyMeter13()");
        OfPacketReader pkt = getOfmTestReader(TF_REPLY_13);
        OpenflowMessage m = MessageFactory.parseMessage(pkt);
        print(FMT_START_TARGET, pkt.startIndex(), pkt.targetIndex());
        validateReplyMeter13(m);
    }

    @Test
    public void mpReplyMeter13Twice() throws MessageParseException {
        print(EOL + "mpReplyMeter13Twice()");
        OfPacketReader pkt = getOfmTestReader(TF_REPLY_13_TWICE);

        OpenflowMessage m = MessageFactory.parseMessage(pkt);
        print(FMT_START_TARGET, pkt.startIndex(), pkt.targetIndex());
        validateReplyMeter13(m);

        m = MessageFactory.parseMessage(pkt);
        print(FMT_START_TARGET, pkt.startIndex(), pkt.targetIndex());
        validateReplyMeter13(m);
    }

    private void validateReplyMeter13(OpenflowMessage m) {
        print(m.toDebugString());

        assertEquals(AM_NEQ, V_1_3, m.getVersion());
        assertEquals(AM_NEQ, MULTIPART_REPLY, m.getType());
        assertEquals(AM_UXS, 176, m.length());

        OfmMultipartReply reply = (OfmMultipartReply) m;

        MBodyMeterStats.Array body =
                (MBodyMeterStats.Array) verifyMpHeader(reply, METER);

        List<MBodyMeterStats> stats = body.getList();
        assertEquals(AM_UXS, 2, stats.size());

        Iterator<MBodyMeterStats> iterator = stats.iterator();
        MBodyMeterStats meterStats = iterator.next();
        checkMeterStats(meterStats, EXP_MI_0, EXP_FC_0, EXP_PIC_0, EXP_BIC_0,
                EXP_DUR_0, EXP_DUR_NS_0);

        List<MeterBandStats> mbsList = meterStats.getBandStats();
        assertEquals(AM_UXS, 2, mbsList.size());
        Iterator<MeterBandStats> mbsIterator = mbsList.iterator();
        checkMeterBandStats(mbsIterator.next(), EXP_MBS_A_PKT, EXP_MBS_A_BYTE);
        checkMeterBandStats(mbsIterator.next(), EXP_MBS_B_PKT, EXP_MBS_B_BYTE);

        meterStats = iterator.next();
        checkMeterStats(meterStats, EXP_MI_1, EXP_FC_1, EXP_PIC_1, EXP_BIC_1,
                EXP_DUR_1, EXP_DUR_NS_1);

        mbsList = meterStats.getBandStats();
        assertEquals(AM_UXS, 3, mbsList.size());
        mbsIterator = mbsList.iterator();
        checkMeterBandStats(mbsIterator.next(), EXP_MBS_C_PKT, EXP_MBS_C_BYTE);
        checkMeterBandStats(mbsIterator.next(), EXP_MBS_D_PKT, EXP_MBS_D_BYTE);
        checkMeterBandStats(mbsIterator.next(), EXP_MBS_E_PKT, EXP_MBS_E_BYTE);
    }

    // NOTE: Meter Stats not supported in 1.0, 1.1, 1.2

    // ============================================ Creating / Encoding  ====

    @Test
    public void encodeMpRequest13() {
        print(EOL + "encodeMpRequest13()");
        OfmMutableMultipartRequest req = (OfmMutableMultipartRequest)
                MessageFactory.create(V_1_3, MULTIPART_REQUEST);
        req.clearXid();

        MBodyMutableMeterStatsRequest body = (MBodyMutableMeterStatsRequest)
                MpBodyFactory.createRequestBody(V_1_3, METER);
        body.meterId(EXP_METER_ID);
        req.body((MultipartBody) body.toImmutable());

        // encode and verify
        encodeAndVerifyMessage(req.toImmutable(), TF_REQ_13);
    }

    @Test
    public void encodeMpRequest13withMpType() {
        print(EOL + "encodeMpRequest13withMpType()");
        OfmMutableMultipartRequest req = (OfmMutableMultipartRequest)
                MessageFactory.create(V_1_3, MULTIPART_REQUEST, METER);
        req.clearXid();

        MBodyMutableMeterStatsRequest body =
                (MBodyMutableMeterStatsRequest) req.getBody();
        body.meterId(EXP_METER_ID);

        req.body((MultipartBody) body.toImmutable());
        // encode and verify
        encodeAndVerifyMessage(req.toImmutable(), TF_REQ_13);
    }

    @Test
    public void encodeMpReplyMeter13()
            throws IncompleteStructureException {
        print(EOL + "encodeMpReplyMeter13()");
        OfmMutableMultipartReply reply = (OfmMutableMultipartReply)
                MessageFactory.create(V_1_3, MULTIPART_REPLY);
        reply.clearXid();

        MBodyMeterStats.MutableArray array = (MBodyMeterStats.MutableArray)
                MpBodyFactory.createReplyBody(V_1_3, METER);

        fillMeterStatsArray(array);
        reply.body((MultipartBody) array.toImmutable());
        encodeAndVerifyMessage(reply.toImmutable(), TF_REPLY_13);
    }

    @Test
    public void encodeMpReplyMeter13withMpType()
            throws IncompleteStructureException {
        print(EOL + "encodeMpReplyMeter13withMpType()");

        OfmMutableMultipartReply reply = (OfmMutableMultipartReply)
                MessageFactory.create(V_1_3, MULTIPART_REPLY, METER);
        reply.clearXid();

        MBodyMeterStats.MutableArray array =
                (MBodyMeterStats.MutableArray) reply.getBody();
        fillMeterStatsArray(array);
        encodeAndVerifyMessage(reply.toImmutable(), TF_REPLY_13);
    }

    // NOTE: Meter Stats not supported in 1.0, 1.1, 1.2

    @Test(expected = VersionMismatchException.class)
    public void mismatchVersion10() {
        MessageFactory.create(V_1_0, MULTIPART_REPLY, METER);
    }

    @Test(expected = VersionNotSupportedException.class)
    public void mismatchVersion11() {
        MessageFactory.create(V_1_1, MULTIPART_REPLY, METER);
    }

    @Test(expected = VersionNotSupportedException.class)
    public void mismatchVersion12() {
        MessageFactory.create(V_1_2, MULTIPART_REPLY, METER);
    }

    // populates the contents of the meter stats object
    private void fillMeterStatsArray(MBodyMeterStats.MutableArray array)
            throws IncompleteStructureException {

        MBodyMutableMeterStats meterStats = (MBodyMutableMeterStats)
                MpBodyFactory.createReplyBodyElement(array.getVersion(), METER);

        meterStats.meterId(EXP_MI_0).flowCount(EXP_FC_0)
                .packetInCount(EXP_PIC_0).byteInCount(EXP_BIC_0)
                .duration(EXP_DUR_0, EXP_DUR_NS_0);
        meterStats.addMeterBandStat(new MeterBandStats(EXP_MBS_A_PKT,
                EXP_MBS_A_BYTE));
        meterStats.addMeterBandStat(new MeterBandStats(EXP_MBS_B_PKT,
                EXP_MBS_B_BYTE));
        array.addMeterStats((MBodyMeterStats)meterStats.toImmutable());

        meterStats = (MBodyMutableMeterStats)
                MpBodyFactory.createReplyBodyElement(array.getVersion(), METER);

        meterStats.meterId(EXP_MI_1).flowCount(EXP_FC_1)
                .packetInCount(EXP_PIC_1).byteInCount(EXP_BIC_1)
                .duration(EXP_DUR_1, EXP_DUR_NS_1);
        meterStats.addMeterBandStat(new MeterBandStats(EXP_MBS_C_PKT,
                EXP_MBS_C_BYTE));
        meterStats.addMeterBandStat(new MeterBandStats(EXP_MBS_D_PKT,
                EXP_MBS_D_BYTE));
        meterStats.addMeterBandStat(new MeterBandStats(EXP_MBS_E_PKT,
                EXP_MBS_E_BYTE));
        array.addMeterStats((MBodyMeterStats)meterStats.toImmutable());
    }
}
