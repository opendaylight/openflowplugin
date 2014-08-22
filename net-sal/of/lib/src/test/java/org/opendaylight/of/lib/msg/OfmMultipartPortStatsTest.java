/*
 * (c) Copyright 2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.of.lib.msg;

import org.junit.Test;
import org.opendaylight.of.lib.IncompleteStructureException;
import org.opendaylight.of.lib.MessageParseException;
import org.opendaylight.of.lib.OfPacketReader;
import org.opendaylight.of.lib.ProtocolVersion;
import org.opendaylight.of.lib.mp.*;
import org.opendaylight.util.net.BigPortNumber;

import java.util.Iterator;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.opendaylight.of.lib.ProtocolVersion.V_1_0;
import static org.opendaylight.of.lib.ProtocolVersion.V_1_3;
import static org.opendaylight.of.lib.mp.MultipartType.PORT_STATS;
import static org.opendaylight.of.lib.msg.MessageType.MULTIPART_REPLY;
import static org.opendaylight.of.lib.msg.MessageType.MULTIPART_REQUEST;
import static org.opendaylight.util.junit.TestTools.*;

/**
 * Unit tests for OfmMultipartRequest and OfmMultipartReply messages
 * of type MultipartType.PORT_STATS.
 *
 * @author Pramod Shanbhag
 * @author Simon Hunt
 */
public class OfmMultipartPortStatsTest extends OfmMultipartTest {

    // Test files...
    private static final String TF_PORT_STATS_REQ_10 = "v10/mpRequestPortStats";
    private static final String TF_PORT_STATS_REQ_13 = "v13/mpRequestPortStats";
    private static final String TF_PORT_STATS_REP_13 = "v13/mpReplyPortStats";
    private static final String TF_PORT_STATS_REP_13_TWICE =
            "v13/mpReplyPortStatsTwice";
    private static final String TF_PORT_STATS_REP_10 = "v10/mpReplyPortStats";
    private static final String TF_PORT_STATS_REP_10_TWICE =
            "v10/mpReplyPortStatsTwice";

    private static final BigPortNumber EXP_PORT = bpn(20);
    private static final long EXP_RX_PKTS = 4660;
    private static final long EXP_TX_PKTS = 4662;
    private static final long EXP_RX_BYTS = 22136;
    private static final long EXP_TX_BYTS = 22138;
    private static final long EXP_RX_DROPD = 20;
    private static final long EXP_TX_DROPD = 24;
    private static final long EXP_RX_ERR = 12;
    private static final long EXP_TX_ERR = 14;
    private static final long EXP_RX_FR_ERR = 4;
    private static final long EXP_RX_OVR_ERR = 4;
    private static final long EXP_RX_CRC_ERR = 4;
    private static final long EXP_COL = 8;
    private static final long EXP_DUR = 50;
    private static final long EXP_DUR_NS = 12;

    private static final BigPortNumber EXP_PORT_1 = bpn(22);
    private static final long EXP_TX_DROPD_1 = 13;
    private static final long EXP_RX_ERR_1 = 22;
    private static final long EXP_RX_CRC_ERR_1 = 14;

    // ========================================================= PARSING ====

    @Test
    public void mpRequestPortStats10() {
        print(EOL + "mpRequestPortStats10()");
        mpRequestPortStats(V_1_0, 20, TF_PORT_STATS_REQ_10);
    }

    @Test
    public void mpRequestPortStats13() {
        print(EOL + "mpRequestPortStats13()");
        mpRequestPortStats(V_1_3, 24, TF_PORT_STATS_REQ_13);
    }

    private void mpRequestPortStats(ProtocolVersion pv, int expLen,
                                    String testFile) {
        OfmMultipartRequest msg = (OfmMultipartRequest)
                verifyMsgHeader(testFile, pv, MULTIPART_REQUEST, expLen);
        MBodyPortStatsRequest body = (MBodyPortStatsRequest)
                verifyMpHeader(msg, PORT_STATS);
        assertEquals(AM_NEQ, EXP_PORT, body.getPort());
    }

    @Test
    public void mpReplyPortStats10() throws MessageParseException {
        print(EOL + "mpReplyPortStats10()");
        OfPacketReader pkt = getOfmTestReader(TF_PORT_STATS_REP_10);
        OpenflowMessage m = MessageFactory.parseMessage(pkt);
        print(FMT_START_TARGET, pkt.startIndex(), pkt.targetIndex());
        validateReplyPortStats10(m);
    }


    @Test
    public void mpReplyPortStats10Twice() throws MessageParseException {
        print(EOL + "mpReplyPortStats10Twice()");
        OfPacketReader pkt = getOfmTestReader(TF_PORT_STATS_REP_10_TWICE);

        OpenflowMessage m = MessageFactory.parseMessage(pkt);
        print(FMT_START_TARGET, pkt.startIndex(), pkt.targetIndex());
        validateReplyPortStats10(m);

        m = MessageFactory.parseMessage(pkt);
        print(FMT_START_TARGET, pkt.startIndex(), pkt.targetIndex());
        validateReplyPortStats10(m);
    }

    private void validateReplyPortStats10(OpenflowMessage m) {
        print(m.toDebugString());
        assertEquals(AM_NEQ, V_1_0, m.getVersion());
        assertEquals(AM_NEQ, MULTIPART_REPLY, m.getType());
        assertEquals(AM_UXS, 220, m.length());
        validateReplyPortStats((OfmMultipartReply) m);
    }

    @Test
    public void mpReplyPortStats13() throws MessageParseException {
        print(EOL + "mpReplyPortStats13()");
        OfPacketReader pkt = getOfmTestReader(TF_PORT_STATS_REP_13);
        OpenflowMessage m = MessageFactory.parseMessage(pkt);
        print(FMT_START_TARGET, pkt.startIndex(), pkt.targetIndex());
        validateReplyPortStats13(m);
    }

    @Test
    public void mpReplyPortStats13Twice() throws MessageParseException {
        print(EOL + "mpReplyPortStats13Twice()");
        OfPacketReader pkt = getOfmTestReader(TF_PORT_STATS_REP_13_TWICE);

        OpenflowMessage m = MessageFactory.parseMessage(pkt);
        print(FMT_START_TARGET, pkt.startIndex(), pkt.targetIndex());
        validateReplyPortStats13(m);

        m = MessageFactory.parseMessage(pkt);
        print(FMT_START_TARGET, pkt.startIndex(), pkt.targetIndex());
        validateReplyPortStats13(m);
    }

    private void validateReplyPortStats13(OpenflowMessage m) {
        print(m.toDebugString());
        assertEquals(AM_NEQ, V_1_3, m.getVersion());
        assertEquals(AM_NEQ, MULTIPART_REPLY, m.getType());
        assertEquals(AM_UXS, 240, m.length());
        validateReplyPortStats((OfmMultipartReply) m);
    }


    private void validateReplyPortStats(OfmMultipartReply msg) {
        final ProtocolVersion pv = msg.getVersion();
        MBodyPortStats.Array body = (MBodyPortStats.Array)
                verifyMpHeader(msg, PORT_STATS);

        List<MBodyPortStats> stats = body.getList();
        assertEquals(AM_UXS, 2, stats.size());

        Iterator<MBodyPortStats> it = stats.iterator();
        //validate data from the first port stat
        MBodyPortStats ps = it.next();
        verifyGroupStats(ps, pv, EXP_PORT, EXP_RX_PKTS, EXP_TX_PKTS,
                         EXP_RX_BYTS, EXP_TX_BYTS, EXP_RX_DROPD,
                         EXP_TX_DROPD, EXP_RX_ERR, EXP_TX_ERR,
                         EXP_RX_FR_ERR, EXP_RX_OVR_ERR, EXP_RX_CRC_ERR,
                         EXP_COL, EXP_DUR, EXP_DUR_NS);

        //validate data from the second port stat
        ps = it.next();
        verifyGroupStats(ps, pv, EXP_PORT_1, EXP_RX_PKTS, EXP_TX_PKTS,
                         EXP_RX_BYTS, EXP_TX_BYTS, EXP_RX_DROPD,
                         EXP_TX_DROPD_1, EXP_RX_ERR_1, EXP_TX_ERR,
                         EXP_RX_FR_ERR, EXP_RX_OVR_ERR, EXP_RX_CRC_ERR_1,
                         EXP_COL, EXP_DUR, EXP_DUR_NS);
    }

    private void verifyGroupStats(MBodyPortStats ps, ProtocolVersion pv,
                                  BigPortNumber port, long rxPackets,
                                  long txPackets, long rxBytes,
                                  long txBytes, long rxDropped,
                                  long txDropped, long rxErrors,
                                  long txErrors, long rxFrameErr,
                                  long rxOverErr, long rxCrcErr,
                                  long collisions, long durationSec,
                                  long durationNsec) {
        assertEquals(AM_NEQ, port, ps.getPort());
        assertEquals(AM_NEQ, rxPackets, ps.getRxPackets());
        assertEquals(AM_NEQ, txPackets, ps.getTxPackets());
        assertEquals(AM_NEQ, rxBytes, ps.getRxBytes());
        assertEquals(AM_NEQ, txBytes, ps.getTxBytes());
        assertEquals(AM_NEQ, rxDropped, ps.getRxDropped());
        assertEquals(AM_NEQ, txDropped, ps.getTxDropped());
        assertEquals(AM_NEQ, rxErrors, ps.getRxErrors());
        assertEquals(AM_NEQ, txErrors, ps.getTxErrors());
        assertEquals(AM_NEQ, rxFrameErr, ps.getRxFrameErr());
        assertEquals(AM_NEQ, rxOverErr, ps.getRxOverErr());
        assertEquals(AM_NEQ, rxCrcErr, ps.getRxCRCErr());
        assertEquals(AM_NEQ, collisions, ps.getCollisions());

        if (pv.gt(V_1_0)) {
            assertEquals(AM_NEQ, durationSec, ps.getDurationSec());
            assertEquals(AM_NEQ, durationNsec, ps.getDurationNsec());
        }
    }

    // ============================================= CREATING / ENCODING ====
    @Test
    public void encodeMpRequestPortStats13() {
        print(EOL + "encodeMpRequestGroup13()");
        encodeMpRequestPortStats(V_1_3, TF_PORT_STATS_REQ_13);
    }

    @Test
    public void encodeMpRequestPortStats13WithMpType() {
        print(EOL + "encodeMpRequestPortStats13WithMpType()");
        encodeMpRequestPortStatsWithMpType(V_1_3, TF_PORT_STATS_REQ_13);
    }

    @Test
    public void encodeMpRequestPortStats10() {
        print(EOL + "encodeMpRequestGroup10()");
        encodeMpRequestPortStats(V_1_0, TF_PORT_STATS_REQ_10);
    }

    @Test
    public void encodeMpRequestPortStats10WithMpType() {
        print(EOL + "encodeMpRequestPortStats10WithMpType()");
        encodeMpRequestPortStatsWithMpType(V_1_0, TF_PORT_STATS_REQ_10);
    }

    private void encodeMpRequestPortStats(ProtocolVersion pv, String testFile) {

        OfmMutableMultipartRequest req = (OfmMutableMultipartRequest)
                MessageFactory.create(pv, MULTIPART_REQUEST);
        req.clearXid();

        MBodyMutablePortStatsRequest body = (MBodyMutablePortStatsRequest)
                MpBodyFactory.createRequestBody(pv, PORT_STATS);
        body.port(EXP_PORT);
        req.body((MultipartBody) body.toImmutable());
        // now encode and verify
        encodeAndVerifyMessage(req.toImmutable(), testFile);
    }

    private void encodeMpRequestPortStatsWithMpType(ProtocolVersion pv,
                                                    String testFile) {
        OfmMutableMultipartRequest req = (OfmMutableMultipartRequest)
                MessageFactory.create(pv, MULTIPART_REQUEST, PORT_STATS);
        req.clearXid();
        MBodyMutablePortStatsRequest body =
                (MBodyMutablePortStatsRequest) req.getBody();
        body.port(EXP_PORT);
        req.body((MultipartBody) body.toImmutable());
        // now encode and verify
        encodeAndVerifyMessage(req.toImmutable(), testFile);
    }

    @Test
    public void encodeMpReplyPortStats13() throws IncompleteStructureException {
        print(EOL + "encodeMpReplyPortStats13()");
        encodeMpReplyPortStats(V_1_3, TF_PORT_STATS_REP_13);
    }

    @Test
    public void encodeMpReplyPortStats10() throws IncompleteStructureException {
        print(EOL + "encodeMpReplyPortStats10()");
        encodeMpReplyPortStats(V_1_0, TF_PORT_STATS_REP_10);
    }

    @Test
    public void encodeMpReplyPortStats13WithMpType()
            throws IncompleteStructureException {
        print(EOL + "encodeMpReplyPortStats13WithMpType()");
        encodeMpReplyPortStatsWithMpType(V_1_3, TF_PORT_STATS_REP_13);
    }

    @Test
    public void encodeMpReplyPortStats10WithMpType()
            throws IncompleteStructureException {
        print(EOL + "encodeMpReplyPortStats10WithMpType()");
        encodeMpReplyPortStatsWithMpType(V_1_0, TF_PORT_STATS_REP_10);
    }

    private void encodeMpReplyPortStats(ProtocolVersion pv, String testFile)
            throws IncompleteStructureException {
        OfmMutableMultipartReply rep = (OfmMutableMultipartReply)
                MessageFactory.create(pv, MULTIPART_REPLY);
        rep.clearXid();

        MBodyPortStats.MutableArray array = (MBodyPortStats.MutableArray)
                MpBodyFactory.createReplyBody(pv, PORT_STATS);

        fillOutArray(array, pv);
        rep.body((MultipartBody) array.toImmutable());
        // now encode and verify
        encodeAndVerifyMessage(rep.toImmutable(), testFile);
    }

    private void encodeMpReplyPortStatsWithMpType(ProtocolVersion pv,
                                                  String testFile)
            throws IncompleteStructureException {
        print(EOL + "encodeMpReplyPortStats13WithMpType()");
        OfmMutableMultipartReply rep = (OfmMutableMultipartReply)
                MessageFactory.create(pv, MULTIPART_REPLY, PORT_STATS);
        rep.clearXid();
        MBodyPortStats.MutableArray array =
                (MBodyPortStats.MutableArray) rep.getBody();
        fillOutArray(array, pv);
        encodeAndVerifyMessage(rep.toImmutable(), testFile);
    }

    private void fillOutArray(MBodyPortStats.MutableArray array,
                              ProtocolVersion pv)
                                  throws IncompleteStructureException {
        // create the first group stats object
        MBodyMutablePortStats mps = (MBodyMutablePortStats)
                MpBodyFactory.createReplyBodyElement(pv, PORT_STATS);
        mps.port(EXP_PORT).rxPackets(EXP_RX_PKTS).txPackets(EXP_TX_PKTS)
            .rxBytes(EXP_RX_BYTS).txBytes(EXP_TX_BYTS).rxDropped(EXP_RX_DROPD)
            .txDropped(EXP_TX_DROPD).rxErrors(EXP_RX_ERR).txErrors(EXP_TX_ERR)
            .rxFrameErr(EXP_RX_FR_ERR).rxOverErr(EXP_RX_OVR_ERR)
            .rxCrcErr(EXP_RX_CRC_ERR).collisions(EXP_COL);

        if (pv == V_1_3)
            mps.duration(EXP_DUR, EXP_DUR_NS);

        array.addPortStats((MBodyPortStats) mps.toImmutable());

        // create the second group stats object
        mps = (MBodyMutablePortStats)
                MpBodyFactory.createReplyBodyElement(pv, PORT_STATS);
        mps.port(EXP_PORT_1).rxPackets(EXP_RX_PKTS).txPackets(EXP_TX_PKTS)
            .rxBytes(EXP_RX_BYTS).txBytes(EXP_TX_BYTS).rxDropped(EXP_RX_DROPD)
            .txDropped(EXP_TX_DROPD_1).rxErrors(EXP_RX_ERR_1)
            .txErrors(EXP_TX_ERR).rxFrameErr(EXP_RX_FR_ERR)
            .rxOverErr(EXP_RX_OVR_ERR).rxCrcErr(EXP_RX_CRC_ERR_1)
            .collisions(EXP_COL);

        if (pv == V_1_3)
            mps.duration(EXP_DUR, EXP_DUR_NS);

        array.addPortStats((MBodyPortStats) mps.toImmutable());
    }
}
