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
import org.opendaylight.of.lib.dt.TableId;
import org.opendaylight.of.lib.mp.MBodyMutableTableStats;
import org.opendaylight.of.lib.mp.MBodyTableStats;
import org.opendaylight.of.lib.mp.MpBodyFactory;
import org.opendaylight.of.lib.mp.MultipartBody;

import java.util.Iterator;
import java.util.List;

import static org.junit.Assert.*;
import static org.opendaylight.of.lib.ProtocolVersion.V_1_0;
import static org.opendaylight.of.lib.ProtocolVersion.V_1_3;
import static org.opendaylight.of.lib.mp.MultipartType.TABLE;
import static org.opendaylight.of.lib.msg.MessageType.MULTIPART_REPLY;
import static org.opendaylight.of.lib.msg.MessageType.MULTIPART_REQUEST;
import static org.opendaylight.util.junit.TestTools.*;

/**
 * Unit tests for OfmMultipartRequest and OfmMultipartReply messages
 * of type MultipartType.TABLE.
 *
 * @author Simon Hunt
 */
public class OfmMultipartTableTest extends OfmMultipartTest {

    // Test files...
    private static final String TF_TABLE_REQ_13 = "v13/mpRequestTable";
    private static final String TF_TABLE_REP_13 = "v13/mpReplyTable";
    private static final String TF_TABLE_REP_13_TWICE = "v13/mpReplyTableTwice";
    private static final String TF_TABLE_REQ_10 = "v10/mpRequestTable";
    private static final String TF_TABLE_REP_10 = "v10/mpReplyTable";
    private static final String TF_TABLE_REP_10_TWICE = "v10/mpReplyTableTwice";

    // ========================================================= PARSING ====

    @Test
    public void mpRequestTable13() {
        print(EOL + "mpRequestTable13()");
        OfmMultipartRequest msg = (OfmMultipartRequest)
                verifyMsgHeader(TF_TABLE_REQ_13, V_1_3, MULTIPART_REQUEST, 16);
        verifyMpHeader(msg, TABLE);
    }

    @Test
    public void mpRequestTable10() {
        print(EOL + "mpRequestTable10");
        OfmMultipartRequest msg = (OfmMultipartRequest)
                verifyMsgHeader(TF_TABLE_REQ_10, V_1_0, MULTIPART_REQUEST, 12);
        verifyMpHeader(msg, TABLE);
    }

    @Test
    public void mpReplyTable13() throws MessageParseException {
        print(EOL + "mpReplyTable13()");
        OfPacketReader pkt = getOfmTestReader(TF_TABLE_REP_13);
        OpenflowMessage m = MessageFactory.parseMessage(pkt);
        print(FMT_START_TARGET, pkt.startIndex(), pkt.targetIndex());
        validateReplyTable13(m);
    }

    @Test
    public void mpReplyTable13Twice() throws MessageParseException {
        print(EOL + "mpReplyTable13Twice()");
        OfPacketReader pkt = getOfmTestReader(TF_TABLE_REP_13_TWICE);

        OpenflowMessage m = MessageFactory.parseMessage(pkt);
        print(FMT_START_TARGET, pkt.startIndex(), pkt.targetIndex());
        validateReplyTable13(m);

        m = MessageFactory.parseMessage(pkt);
        print(FMT_START_TARGET, pkt.startIndex(), pkt.targetIndex());
        validateReplyTable13(m);
    }

    private void validateReplyTable13(OpenflowMessage m) {
        print(m.toDebugString());

        assertEquals(AM_NEQ, V_1_3, m.getVersion());
        assertEquals(AM_NEQ, MULTIPART_REPLY, m.getType());
        assertEquals(AM_NEQ, 88, m.length());

        OfmMultipartReply msg = (OfmMultipartReply) m;
        MBodyTableStats.Array body =
                (MBodyTableStats.Array) verifyMpHeader(msg, TABLE);
        List<MBodyTableStats> stats = body.getList();
        assertEquals(AM_UXS, 3, stats.size());

        Iterator<MBodyTableStats> iter = stats.iterator();
        check13TableStats(iter.next(), tid(0), 27, 65432, 21810);
        check13TableStats(iter.next(), tid(1), 29, 65428, 21817);
        check13TableStats(iter.next(), tid(2), 31, 65448, 21811);
        assertFalse(AM_UXS, iter.hasNext());
    }

    private void check13TableStats(MBodyTableStats ts, TableId tid,
                                   int expActive, int expLookup, int expHits) {
        assertEquals(AM_NEQ, tid, ts.getTableId());
        assertNull(AM_HUH, ts.getName());
        assertEquals(AM_NEQ, 0, ts.getMaxEntries());
        assertEquals(AM_NEQ, expActive, ts.getActiveCount());
        assertEquals(AM_NEQ, expLookup, ts.getLookupCount());
        assertEquals(AM_NEQ, expHits, ts.getMatchedCount());
    }

    private static final String EXP_TABLE_NAME = "The One and Only Table";

    @Test
    public void mpReplyTable10() throws MessageParseException {
        print(EOL + "mpReplyTable10()");
        OfPacketReader pkt = getOfmTestReader(TF_TABLE_REP_10);
        OpenflowMessage m = MessageFactory.parseMessage(pkt);
        print(FMT_START_TARGET, pkt.startIndex(), pkt.targetIndex());
        validateReplyTable10(m);
    }

    @Test
    public void mpReplyTable10Twice() throws MessageParseException {
        print(EOL + "mpReplyTable10Twice()");
        OfPacketReader pkt = getOfmTestReader(TF_TABLE_REP_10_TWICE);

        OpenflowMessage m = MessageFactory.parseMessage(pkt);
        print(FMT_START_TARGET, pkt.startIndex(), pkt.targetIndex());
        validateReplyTable10(m);

        m = MessageFactory.parseMessage(pkt);
        print(FMT_START_TARGET, pkt.startIndex(), pkt.targetIndex());
        validateReplyTable10(m);
    }

    private void validateReplyTable10(OpenflowMessage m) {
        print(m.toDebugString());

        assertEquals(AM_NEQ, V_1_0, m.getVersion());
        assertEquals(AM_NEQ, MULTIPART_REPLY, m.getType());
        assertEquals(AM_UXS, 76, m.length());

        OfmMultipartReply msg = (OfmMultipartReply) m;
        MBodyTableStats.Array body =
                (MBodyTableStats.Array) verifyMpHeader(msg, TABLE);
        List<MBodyTableStats> stats = body.getList();
        assertEquals(AM_UXS, 1, stats.size());
        MBodyTableStats ts = stats.get(0);
        assertEquals(AM_NEQ, tid(0), ts.getTableId());
        assertEquals(AM_NEQ, EXP_TABLE_NAME, ts.getName());
        // TODO : add wildcarding assertion
        assertEquals(AM_NEQ, 512, ts.getMaxEntries());
        assertEquals(AM_NEQ, 27, ts.getActiveCount());
        assertEquals(AM_NEQ, 65432, ts.getLookupCount());
        assertEquals(AM_NEQ, 21810, ts.getMatchedCount());
    }

    // ============================================= CREATING / ENCODING ====

    @Test
    public void encodeMpRequestTable13() {
        print(EOL + "encodeMpRequestTable13()");
        OfmMutableMultipartRequest req = (OfmMutableMultipartRequest)
                MessageFactory.create(V_1_3, MULTIPART_REQUEST);
        req.clearXid();
        req.type(TABLE);
        encodeAndVerifyMessage(req.toImmutable(), TF_TABLE_REQ_13);
    }

    @Test
    public void encodeMpRequestTable13WithType() {
        print(EOL + "encodeMpRequestTable13WithType()");
        OfmMutableMultipartRequest req = (OfmMutableMultipartRequest)
                MessageFactory.create(V_1_3, MULTIPART_REQUEST, TABLE);
        req.clearXid();
        encodeAndVerifyMessage(req.toImmutable(), TF_TABLE_REQ_13);
    }

    @Test
    public void encodeMpRequestTable10() {
        print(EOL + "encodeMpRequestTable10()");
        OfmMutableMultipartRequest req = (OfmMutableMultipartRequest)
                MessageFactory.create(V_1_0, MULTIPART_REQUEST);
        req.clearXid();
        req.type(TABLE);
        encodeAndVerifyMessage(req.toImmutable(), TF_TABLE_REQ_10);
    }

    @Test
    public void encodeMpRequestTable10WithType() {
        print(EOL + "encodeMpRequestTable10WithType()");
        OfmMutableMultipartRequest req = (OfmMutableMultipartRequest)
                MessageFactory.create(V_1_0, MULTIPART_REQUEST, TABLE);
        req.clearXid();
        encodeAndVerifyMessage(req.toImmutable(), TF_TABLE_REQ_10);
    }

    @Test
    public void encodeMpReplyTable13() throws IncompleteStructureException {
        print(EOL + "encodeMpReplyTable13()");
        final ProtocolVersion pv = V_1_3;
        OfmMutableMultipartReply rep = (OfmMutableMultipartReply)
                MessageFactory.create(pv, MULTIPART_REPLY);
        rep.clearXid();
        MBodyTableStats.MutableArray array = (MBodyTableStats.MutableArray)
                MpBodyFactory.createReplyBody(pv, TABLE);

        array.addTableStats(mts13(tid(0), 27, 65432, 21810));
        array.addTableStats(mts13(tid(1), 29, 65428, 21817));
        array.addTableStats(mts13(tid(2), 31, 65448, 21811));

        rep.body((MultipartBody) array.toImmutable());
        encodeAndVerifyMessage(rep.toImmutable(), TF_TABLE_REP_13);
    }

    @Test
    public void encodeMpReplyTable13WithType()
            throws IncompleteStructureException {
        print(EOL + "encodeMpReplyTable13WithType()");
        final ProtocolVersion pv = V_1_3;
        OfmMutableMultipartReply rep = (OfmMutableMultipartReply)
                MessageFactory.create(pv, MULTIPART_REPLY, TABLE);
        rep.clearXid();
        MBodyTableStats.MutableArray array =
                (MBodyTableStats.MutableArray) rep.getBody();

        array.addTableStats(mts13(tid(0), 27, 65432, 21810));
        array.addTableStats(mts13(tid(1), 29, 65428, 21817));
        array.addTableStats(mts13(tid(2), 31, 65448, 21811));

        rep.body((MultipartBody) array.toImmutable());
        encodeAndVerifyMessage(rep.toImmutable(), TF_TABLE_REP_13);
    }

    private MBodyTableStats mts13(TableId tid, long active,
                                  long lookup, long matched) {
        MBodyMutableTableStats mts = (MBodyMutableTableStats)
                MpBodyFactory.createReplyBodyElement(V_1_3, TABLE);
        mts.tableId(tid).activeCount(active)
                .lookupCount(lookup).matchedCount(matched);
        return (MBodyTableStats) mts.toImmutable();
    }

    @Test
    public void encodeMpReplyTable10() throws IncompleteStructureException {
        print(EOL + "encodeMpReplyTable10()");
        final ProtocolVersion pv = V_1_0;
        OfmMutableMultipartReply rep = (OfmMutableMultipartReply)
                MessageFactory.create(pv, MULTIPART_REPLY);
        rep.clearXid();
        MBodyTableStats.MutableArray array = (MBodyTableStats.MutableArray)
                MpBodyFactory.createReplyBody(pv, TABLE);
        array.addTableStats(mts10(tid(0), "The One and Only Table",
                512, 27, 65432, 21810));
    }

    @Test
    public void encodeMpReplyTable10WithType()
            throws IncompleteStructureException {
        print(EOL + "encodeMpReplyTable10WithType()");
        final ProtocolVersion pv = V_1_0;
        OfmMutableMultipartReply rep = (OfmMutableMultipartReply)
                MessageFactory.create(pv, MULTIPART_REPLY, TABLE);
        rep.clearXid();
        MBodyTableStats.MutableArray array = (MBodyTableStats.MutableArray)
                rep.getBody();
        array.addTableStats(mts10(tid(0), "The One and Only Table",
                512, 27, 65432, 21810));
    }

    private MBodyTableStats mts10(TableId tid, String name, long max,
                                  long active, long lookup, long matched) {
        MBodyMutableTableStats mts = (MBodyMutableTableStats)
                MpBodyFactory.createReplyBodyElement(V_1_0, TABLE);
        mts.tableId(tid).name(name).maxEntries(max).activeCount(active)
                .lookupCount(lookup).matchedCount(matched);
        return (MBodyTableStats) mts.toImmutable();
    }
}
