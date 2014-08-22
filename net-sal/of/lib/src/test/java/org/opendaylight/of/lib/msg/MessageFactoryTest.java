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
import org.opendaylight.of.lib.dt.BufferId;
import org.opendaylight.of.lib.instr.*;
import org.opendaylight.of.lib.match.Match;
import org.opendaylight.of.lib.mp.MultipartType;

import static org.junit.Assert.*;
import static org.opendaylight.of.lib.ProtocolVersion.V_1_0;
import static org.opendaylight.of.lib.ProtocolVersion.V_1_3;
import static org.opendaylight.of.lib.instr.ActionFactory.createAction;
import static org.opendaylight.of.lib.instr.InstructionFactory.createMutableInstruction;
import static org.opendaylight.of.lib.match.MatchFactory.createMatch;
import static org.opendaylight.of.lib.mp.MultipartType.*;
import static org.opendaylight.of.lib.msg.MessageFactory.create;
import static org.opendaylight.of.lib.msg.MessageType.*;
import static org.opendaylight.of.lib.msg.MessageType.EXPERIMENTER;
import static org.opendaylight.util.junit.TestTools.*;

/**
 * A miscellany of tests for message factory.
 *
 * @author Simon Hunt
 */
public class MessageFactoryTest extends AbstractTest {

    private static final String MIXTURE = "msg/mixture.hex";

    private static final int OLD_PRIORITY = 1234;
    private static final int NEW_PRIORITY = 4321;

    @Test(expected = VersionMismatchException.class)
    public void mpMeterConfig10() {
        create(V_1_0, MULTIPART_REQUEST, METER_CONFIG);
    }

    @Test(expected = VersionMismatchException.class)
    public void meterMod10() {
        create(V_1_0, METER_MOD);
    }

    @Test
    public void calculateXidRollover() {
        print(EOL + "calculateXidRollover");
        // suppose the library is asked to create messages at the rate of
        // one per millisecond, continuously. How long before the assigned
        // XIDs wrap into negative values? Remember XID is u32...
        long u32 = ((long)Math.pow(2, 32));
        assertEquals(AM_NEQ, 0xffffffffL, u32-1);
        long xids = MessageFactory.LAST_XID - MessageFactory.BASE_XID - 1;
        print("2^32 = {}", u32);
        print("XIDs = {}", xids);

        xids /= 1000;

        long secs = xids % 60;
        xids /= secs;

        long mins = xids % 60;
        xids /= 60;

        long hours = xids % 24;
        xids /= 24;

        long days = xids % 365;
        long weeks = days / 7;
        days = days % 7;
        long years = xids / 365;

        print("At the continuous production rate of 1/ms...");
        print("Time to XID rollover: {} years {} weeks {} days {} hours {} mins",
                years, weeks, days, hours, mins);
        assertTrue("We should worry...", weeks > 5);
    }

    private static final String BARRIER_REPLY = "msg/v13/barrierReply.hex";

    @Test
    public void patchingXid() throws MessageParseException {
        print(EOL + "patchingXid()");
        // first, here is an example of parsing a straight "canned" message
        OfPacketReader pkt = getPacketReader(BARRIER_REPLY);
        OpenflowMessage msg = MessageFactory.parseMessage(pkt);
        print(msg.toDebugString());
        assertEquals(AM_NEQ, 0, msg.getXid());

        // now, do the same, but patch the XID to match a "request"
        OpenflowMessage request = create(V_1_3, BARRIER_REQUEST).toImmutable();
        pkt = getPacketReader(BARRIER_REPLY);
        msg = MessageFactory.parseMessage(pkt, request);
        print(msg.toDebugString());
        assertEquals(AM_NEQ, request.getXid(), msg.getXid());
        assertTrue(AM_HUH, msg.getXid() != 0);

        // finally, check for version mismatch exception
        request = create(V_1_0, BARRIER_REQUEST).toImmutable();
        pkt = getPacketReader(BARRIER_REPLY);
        try {
            MessageFactory.parseMessage(pkt, request);
            fail(AM_NOEX);
        } catch (VersionMismatchException e) {
            print(FMT_EX, e);
        } catch (Exception e) {
            print(e);
            fail(AM_WREX);
        }
    }

    @Test
    public void copyingXid() {
        print(EOL + "copyingXid()");
        OpenflowMessage msg = MessageFactory.create(V_1_3, HELLO).toImmutable();
        print(msg);
        long xid = msg.getXid();
        assertTrue(AM_HUH, xid != 0);
        MutableMessage mm = MessageFactory.create(V_1_3, HELLO);
        print(mm);
        assertTrue(AM_HUH, mm.getXid() != xid);
        MessageFactory.copyXid(msg, mm);
        print(mm);
        assertTrue(AM_HUH, mm.getXid() == xid);
    }

    @Test
    public void parsingMessageFromMultiBuffer() {
        print(EOL + "parsingMessageFromMultiBuffer()");
        OfPacketReader pkt = getPacketReader(MIXTURE);
        int totalBytes = pkt.readableBytes();
        print("Readable bytes = {}", totalBytes);
        mixtureCheck(pkt, HELLO);
        mixtureCheck(pkt, FEATURES_REQUEST);
        mixtureCheck(pkt, FEATURES_REPLY);
        mixtureCheck(pkt, MULTIPART_REQUEST, PORT_DESC);
        mixtureCheck(pkt, MULTIPART_REPLY, PORT_DESC);
        mixtureCheck(pkt, MULTIPART_REQUEST, DESC);
        mixtureCheck(pkt, MULTIPART_REPLY, DESC);
        mixtureCheck(pkt, FLOW_MOD);
        mixtureCheck(pkt, MULTIPART_REPLY, TABLE_FEATURES);
        mixtureCheck(pkt, PACKET_IN);
        mixtureCheck(pkt, PACKET_OUT);
        mixtureCheck(pkt, EXPERIMENTER);
        assertEquals("Didn't consume buffer", 0, pkt.readableBytes());
    }

    @Test
    public void flowModPriorityPatching() {
        OfmMutableFlowMod mfm = (OfmMutableFlowMod)
                create(V_1_0, FLOW_MOD, FlowModCommand.ADD);
        mfm.bufferId(BufferId.NO_BUFFER).priority(OLD_PRIORITY)
                .match((Match) createMatch(V_1_0).toImmutable());

        OfmFlowMod orig = (OfmFlowMod) mfm.toImmutable();

        assertEquals("original priority incorrect", OLD_PRIORITY, orig.getPriority());
        OfmFlowMod patched = MessageFactory.patchFlowModPriority(orig, NEW_PRIORITY);
        assertEquals("new priority incorrect", NEW_PRIORITY, patched.getPriority());
        assertSame("same instance expected", orig, patched);
    }

    private void mixtureCheck(OfPacketReader pkt, MessageType mt) {
        mixtureCheck(pkt, mt, null);
    }

    private void mixtureCheck(OfPacketReader pkt, MessageType mt, MultipartType mpt) {
        try {
            OpenflowMessage m = MessageFactory.parseMessage(pkt);
            print(m);
            assertEquals(AM_NEQ, mt, m.getType());
            if (mt == MULTIPART_REQUEST) {
                OfmMultipartRequest mp = (OfmMultipartRequest) m;
                assertEquals(AM_NEQ, mpt, mp.getMultipartType());
            } else if (mt == MULTIPART_REPLY) {
                OfmMultipartReply mp = (OfmMultipartReply) m;
                assertEquals(AM_NEQ, mpt, mp.getMultipartType());
            }
        } catch (MessageParseException e) {
            print(e);
            fail(AM_UNEX);
        }
    }

    // unit tests for the copying of flow mods

    private OfmFlowMod createFlowMod(ProtocolVersion pv) {
        OfmMutableFlowMod mfm = (OfmMutableFlowMod)
                create(pv, FLOW_MOD, FlowModCommand.ADD);
        mfm.match((Match) createMatch(pv).toImmutable());
        Action outAct = createAction(pv, ActionType.OUTPUT, bpn(23));
        if (pv == V_1_0) {
            mfm.addAction(outAct);
        } else {
            InstrMutableAction ins =
                    createMutableInstruction(pv, InstructionType.WRITE_ACTIONS);
            ins.addAction(outAct);
            mfm.addInstruction((Instruction) ins.toImmutable());
        }
        return (OfmFlowMod) mfm.toImmutable();
    }

    private static final ProtocolVersion[] PVS = {V_1_0, V_1_3};

    @Test
    public void flowModImmutableCopy() {
        print(EOL + "flowModImmutableCopy()");
        for (ProtocolVersion pv : PVS) {
            print("{} version {}...", EOL, pv);
            OfmFlowMod fm = createFlowMod(pv);
            print(fm.toDebugString());
            long origXid = fm.getXid();
            OpenflowMessage.Header origHeader = fm.header;

            OfmFlowMod copy = (OfmFlowMod) MessageFactory.copy(fm);
            print("{}===COPY==={}{}", EOL, EOL, copy.toDebugString());
            assertFalse(AM_WRCL, copy instanceof OfmMutableFlowMod);
            assertEquals(AM_NEQ, fm.length(), copy.length());
            assertEquals(AM_NEQ, fm.getVersion(), copy.getVersion());
            assertEquals(AM_NEQ, fm.getType(), copy.getType());
            assertNotSame("Same header instance!", origHeader, copy.header);
            assertEquals("Orig XID changed!", origXid, fm.getXid());
            assertTrue("XIDs same!", copy.getXid() > fm.getXid());
        }
    }

    @Test
    public void flowModImmutableExactCopy() {
        print(EOL + "flowModImmutableExactCopy()");
        for (ProtocolVersion pv : PVS) {
            print("{} version {}...", EOL, pv);
            OfmFlowMod fm = createFlowMod(pv);
            print(fm.toDebugString());
            long origXid = fm.getXid();
            OpenflowMessage.Header origHeader = fm.header;

            OfmFlowMod copy = (OfmFlowMod) MessageFactory.exactCopy(fm);
            print("{}===COPY==={}{}", EOL, EOL, copy.toDebugString());
            assertFalse(AM_WRCL, copy instanceof OfmMutableFlowMod);
            assertEquals(AM_NEQ, fm.length(), copy.length());
            assertEquals(AM_NEQ, fm.getVersion(), copy.getVersion());
            assertEquals(AM_NEQ, fm.getType(), copy.getType());
            assertSame("NOT Same header instance!", origHeader, copy.header);
            assertEquals("Orig XID changed!", origXid, fm.getXid());
            assertEquals("XIDs differ!", copy.getXid(), fm.getXid());
        }
    }

    @Test
    public void flowModMutableCopy() {
        print(EOL + "flowModMutableCopy()");
        for (ProtocolVersion pv : PVS) {
            print("{} version {}...", EOL, pv);
            OfmFlowMod fm = createFlowMod(pv);
            print(fm.toDebugString());
            long origXid = fm.getXid();
            OpenflowMessage.Header origHeader = fm.header;

            OfmFlowMod copy = (OfmFlowMod) MessageFactory.mutableCopy(fm);
            print("{}===COPY==={}{}", EOL, EOL, copy.toDebugString());
            assertTrue(AM_WRCL, copy instanceof OfmMutableFlowMod);
            assertEquals(AM_NEQ, fm.length(), copy.length());
            assertEquals(AM_NEQ, fm.getVersion(), copy.getVersion());
            assertEquals(AM_NEQ, fm.getType(), copy.getType());
            assertNotSame("Same header instance!", origHeader, copy.header);
            assertEquals("Orig XID changed!", origXid, fm.getXid());
            assertTrue("XIDs same!", copy.getXid() > fm.getXid());
        }
    }

    @Test
    public void flowModMutableExactCopy() {
        print(EOL + "flowModMutableExactCopy()");
        for (ProtocolVersion pv : PVS) {
            print("{} version {}...", EOL, pv);
            OfmFlowMod fm = createFlowMod(pv);
            print(fm.toDebugString());
            long origXid = fm.getXid();
            OpenflowMessage.Header origHeader = fm.header;

            OfmFlowMod copy = (OfmFlowMod) MessageFactory.exactMutableCopy(fm);
            print("{}===COPY==={}{}", EOL, EOL, copy.toDebugString());
            assertTrue(AM_WRCL, copy instanceof OfmMutableFlowMod);
            assertEquals(AM_NEQ, fm.length(), copy.length());
            assertEquals(AM_NEQ, fm.getVersion(), copy.getVersion());
            assertEquals(AM_NEQ, fm.getType(), copy.getType());
            assertNotSame("Same header instance!", origHeader, copy.header);
            assertEquals("Orig XID changed!", origXid, fm.getXid());
            assertEquals("XIDs differ!", copy.getXid(), fm.getXid());
        }
    }

}
