/*
 * (c) Copyright 2012-2014 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.of.lib.msg;

import org.junit.Test;
import org.opendaylight.of.lib.*;
import org.opendaylight.of.lib.dt.GroupId;
import org.opendaylight.of.lib.dt.MeterId;
import org.opendaylight.of.lib.dt.TableId;
import org.opendaylight.of.lib.instr.*;
import org.opendaylight.of.lib.match.Match;
import org.opendaylight.of.lib.match.MatchFactory;
import org.opendaylight.of.lib.match.MatchField;
import org.opendaylight.of.lib.match.MutableMatch;
import org.opendaylight.of.lib.mp.*;
import org.opendaylight.util.net.*;

import java.util.*;

import static org.junit.Assert.*;
import static org.opendaylight.of.lib.ProtocolVersion.V_1_0;
import static org.opendaylight.of.lib.ProtocolVersion.V_1_3;
import static org.opendaylight.of.lib.instr.ActionFactory.createAction;
import static org.opendaylight.of.lib.instr.ActionFactory.createActionSetField;
import static org.opendaylight.of.lib.instr.InstructionFactory.createInstruction;
import static org.opendaylight.of.lib.instr.InstructionFactory.createMutableInstruction;
import static org.opendaylight.of.lib.match.FieldFactory.createBasicField;
import static org.opendaylight.of.lib.match.OxmBasicFieldType.*;
import static org.opendaylight.of.lib.mp.MultipartType.FLOW;
import static org.opendaylight.of.lib.msg.FlowModFlag.*;
import static org.opendaylight.of.lib.msg.MessageType.MULTIPART_REPLY;
import static org.opendaylight.of.lib.msg.MessageType.MULTIPART_REQUEST;
import static org.opendaylight.util.junit.TestTools.*;

/**
 * Unit tests for OfmMultipartRequest and OfmMultipartReply messages
 * of type MultipartType.FLOW.
 *
 * @author Simon Hunt
 */
public class OfmMultipartFlowTest extends OfmMultipartTest {

    // Test files...
    private static final String TF_FLOW_REQ_13 = "v13/mpRequestFlow";
    private static final String TF_FLOW_REP_13 = "v13/mpReplyFlow";
    private static final String TF_FLOW_REP_13_TWICE = "v13/mpReplyFlowTwice";
    private static final String TF_FLOW_REQ_10 = "v10/mpRequestFlow";
    private static final String TF_FLOW_REP_10 = "v10/mpReplyFlow";
    private static final String TF_FLOW_REP_10_TWICE = "v10/mpReplyFlowTwice";

    private static final TableId EXP_TID = TableId.ALL;
    private static final BigPortNumber EXP_OPORT = bpn(12);
    private static final GroupId EXP_OGROUP = GroupId.ANY;
    private static final long EXP_COOK = 0x123;
    private static final long EXP_MASK = 0xfff;


    private static final MacAddress EXP_MAC_SRC = mac("112233:445566");
    private static final MacAddress EXP_MAC_SRC_MASK = mac("ffffff:000000");
    private static final PortNumber EXP_PORT = pn(25);

    private static final MacAddress EXP_MAC_DST = mac("00001e:453411");
    private static final IpAddress EXP_IP_DST = ip("15.254.17.1");


    // ========================================================= PARSING ====

    @Test
    public void mpRequestFlow13() {
        print(EOL + "mpRequestFlow13()");
        OfmMultipartRequest msg =
                (OfmMultipartRequest) verifyMsgHeader(TF_FLOW_REQ_13,
                        V_1_3, MULTIPART_REQUEST, 72);
        MBodyFlowStatsRequest body =
                (MBodyFlowStatsRequest) verifyMpHeader(msg, FLOW);

        assertEquals(AM_NEQ, EXP_TID, body.getTableId());
        assertEquals(AM_NEQ, EXP_OPORT, body.getOutPort());
        assertEquals(AM_NEQ, EXP_OGROUP, body.getOutGroup());
        assertEquals(AM_NEQ, EXP_COOK, body.getCookie());
        assertEquals(AM_NEQ, EXP_MASK, body.getCookieMask());

        Iterator<MatchField> mfi = body.getMatch().getMatchFields().iterator();
        verifyMatchField(mfi.next(), ETH_SRC, EXP_MAC_SRC, EXP_MAC_SRC_MASK);
        assertFalse(AM_HUH, mfi.hasNext());
    }

    @Test
    public void mpRequestFlow10() {
        print(EOL + "mpRequestFlow10()");
        OfmMultipartRequest msg =
                (OfmMultipartRequest) verifyMsgHeader(TF_FLOW_REQ_10,
                        V_1_0, MULTIPART_REQUEST, 56);
        MBodyFlowStatsRequest body =
                (MBodyFlowStatsRequest) verifyMpHeader(msg, FLOW);

        assertEquals(AM_NEQ, EXP_TID, body.getTableId());
        assertEquals(AM_NEQ, EXP_OPORT, body.getOutPort());
        assertEquals(AM_NEQ, null, body.getOutGroup());
        assertEquals(AM_NEQ, 0, body.getCookie());
        assertEquals(AM_NEQ, 0, body.getCookieMask());

        Iterator<MatchField> mfi = body.getMatch().getMatchFields().iterator();
        verifyMatchField(mfi.next(), ETH_SRC, EXP_MAC_SRC);
        assertFalse(AM_HUH, mfi.hasNext());
    }

    @Test
    public void mpReplyFlow13() throws MessageParseException {
        print(EOL + "mpReplyFlow13()");
        OfPacketReader pkt = getOfmTestReader(TF_FLOW_REP_13);
        OpenflowMessage m = MessageFactory.parseMessage(pkt);
        print(FMT_START_TARGET, pkt.startIndex(), pkt.targetIndex());
        validateReplyFlow13(m);
    }

    @Test
    public void mpReplyFlow13Twice() throws MessageParseException {
        print(EOL + "mpReplyFlow13Twice()");
        OfPacketReader pkt = getOfmTestReader(TF_FLOW_REP_13_TWICE);

        OpenflowMessage m = MessageFactory.parseMessage(pkt);
        print(FMT_START_TARGET, pkt.startIndex(), pkt.targetIndex());
        validateReplyFlow13(m);

        m = MessageFactory.parseMessage(pkt);
        print(FMT_START_TARGET, pkt.startIndex(), pkt.targetIndex());
        validateReplyFlow13(m);
    }

    private void validateReplyFlow13(OpenflowMessage m) {
        print(m.toDebugString());

        assertEquals(AM_NEQ, V_1_3, m.getVersion());
        assertEquals(AM_NEQ, MULTIPART_REPLY, m.getType());
        assertEquals(AM_NEQ, 352, m.length());

        OfmMultipartReply msg = (OfmMultipartReply) m;
        MBodyFlowStats.Array body =
                (MBodyFlowStats.Array) verifyMpHeader(msg, FLOW);

        List<MBodyFlowStats> stats = body.getList();
        assertEquals(AM_UXS, 3, stats.size());

        Iterator<MBodyFlowStats> fsIt = stats.iterator();

        // validate data from first flow...
        MBodyFlowStats fs = fsIt.next();
        verifyFlowStatsFixedOne(fs, 0, 300, 50000, 4, 60, 300);
        verifyFlags(fs.getFlags(), SEND_FLOW_REM, NO_PACKET_COUNTS,
                NO_BYTE_COUNTS);
        verifyFlowStatsFixedTwo(fs, 0x1234, 2342, 13423234);

        Iterator<MatchField> mfi = fs.getMatch().getMatchFields().iterator();
        verifyMatchField(mfi.next(), ETH_SRC, EXP_MAC_SRC, EXP_MAC_SRC_MASK);
        verifyMatchField(mfi.next(), ETH_TYPE, EthernetType.IPv4);
        verifyMatchField(mfi.next(), IP_PROTO, IpProtocol.TCP);
        verifyMatchField(mfi.next(), TCP_DST, EXP_PORT);
        assertFalse(AM_HUH, mfi.hasNext());

        assertNull(AM_HUH, fs.getActions()); // only applies to 1.0
        Iterator<Instruction> ii = fs.getInstructions().iterator();
        Instruction ins = ii.next();
        verifyInstrActions(ins, InstructionType.CLEAR_ACTIONS, 0);
        ins = ii.next();
        verifyInstrActions(ins, InstructionType.APPLY_ACTIONS, 3);
        Iterator<Action> ai = ((InstrApplyActions)ins).getActionList().iterator();
        verifyAction(ai.next(), ActionType.DEC_NW_TTL);
        verifyActionSetField(ai.next(), ETH_DST, EXP_MAC_DST);
        verifyActionSetField(ai.next(), IPV4_DST, EXP_IP_DST);
        assertFalse(AM_HUH, ai.hasNext());
        assertFalse(AM_HUH, ii.hasNext());

        // validate data from second flow...
        fs = fsIt.next();
        verifyFlowStatsFixedOne(fs, 1, 740, 3, 1, 60, 300);
        verifyFlags(fs.getFlags(), SEND_FLOW_REM, NO_BYTE_COUNTS);
        verifyFlowStatsFixedTwo(fs, 0x1234, 256, 37000);
        mfi = fs.getMatch().getMatchFields().iterator();
        verifyMatchField(mfi.next(), ETH_SRC, EXP_MAC_SRC, EXP_MAC_SRC_MASK);
        verifyMatchField(mfi.next(), ETH_TYPE, EthernetType.IPv4);
        assertFalse(AM_HUH, mfi.hasNext());

        assertNull(AM_HUH, fs.getActions()); // only applies to 1.0
        ii = fs.getInstructions().iterator();
        ins = ii.next();
        verifyInstrGoTab(ins, 3);
        assertFalse(AM_HUH, ii.hasNext());

        // validate data from third (and final) flow...
        fs = fsIt.next();
        verifyFlowStatsFixedOne(fs, 1, 748, 32, 7, 60, 300);
        verifyFlags(fs.getFlags(), SEND_FLOW_REM);
        verifyFlowStatsFixedTwo(fs, 0x1234, 2560, 37004);
        mfi = fs.getMatch().getMatchFields().iterator();
        verifyMatchField(mfi.next(), ETH_SRC, EXP_MAC_SRC, EXP_MAC_SRC_MASK);
        verifyMatchField(mfi.next(), ETH_TYPE, EthernetType.IPv4);
        assertFalse(AM_HUH, mfi.hasNext());

        assertNull(AM_HUH, fs.getActions()); // only applies to 1.0
        ii = fs.getInstructions().iterator();
        ins = ii.next();
        verifyInstrMeter(ins, 0xc0);
        ins = ii.next();
        assertTrue(AM_WRCL, InstrExperimenter.class.isInstance(ins));
        InstrExperimenter ie = (InstrExperimenter) ins;
        assertArrayEquals(AM_NEQ, EXP_EXP_DATA, ie.getData());
        assertFalse(AM_HUH, ii.hasNext());
    }

    private static final byte[] EXP_EXP_DATA = new byte[] {
            0x48, 0x6f, 0x6c, 0x61, 0x21, 0, 0, 0
    };

    private void verifyFlowStatsFixedTwo(MBodyFlowStats fs, long cook,
                                         long pkts, long bytes) {
        assertEquals(AM_NEQ, cook, fs.getCookie());
        assertEquals(AM_NEQ, pkts, fs.getPacketCount());
        assertEquals(AM_NEQ, bytes, fs.getByteCount());
    }

    private void verifyFlowStatsFixedOne(MBodyFlowStats fs, int tableId,
                                         long sec, long nsec, int pri,
                                         int idle, int hard) {
        assertEquals(AM_NEQ, tid(tableId), fs.getTableId());
        assertEquals(AM_NEQ, sec, fs.getDurationSec());
        assertEquals(AM_NEQ, nsec, fs.getDurationNsec());
        assertEquals(AM_NEQ, pri, fs.getPriority());
        assertEquals(AM_NEQ, idle, fs.getIdleTimeout());
        assertEquals(AM_NEQ, hard, fs.getHardTimeout());
    }

    private void validateReplyFlow10(OpenflowMessage m) {
        print(m.toDebugString());

        assertEquals(AM_NEQ, V_1_0, m.getVersion());
        assertEquals(AM_NEQ, MULTIPART_REPLY, m.getType());
        assertEquals(AM_UXS, 340, m.length());

        OfmMultipartReply msg = (OfmMultipartReply) m;

        MBodyFlowStats.Array body =
                (MBodyFlowStats.Array) verifyMpHeader(msg, FLOW);

        List<MBodyFlowStats> stats = body.getList();
        assertEquals(AM_UXS, 3, stats.size());

        Iterator<MBodyFlowStats> fsIt = stats.iterator();

        // validate data from first flow...
        MBodyFlowStats fs = fsIt.next();
        verifyFlowStatsFixedOne(fs, 0, 300, 50000, 4, 60, 300);
        assertNull(AM_HUH, fs.getFlags());
        verifyFlowStatsFixedTwo(fs, 0x1234, 2342, 13423234);

        Iterator<MatchField> mfi = fs.getMatch().getMatchFields().iterator();
        verifyMatchField(mfi.next(), ETH_SRC, EXP_MAC_SRC);
        assertFalse(AM_HUH, mfi.hasNext());

        assertNull(AM_HUH, fs.getInstructions()); // doesn't apply to 1.0
        Iterator<Action> ai = fs.getActions().iterator();
        verifyActionSetField(ai.next(), VLAN_VID, VlanId.valueOf(42));
        verifyActionSetField(ai.next(), VLAN_PCP, 7);
        verifyActionSetField(ai.next(), IPV4_DST, ip("15.255.127.35"));

        // validate data from second flow...
        fs = fsIt.next();
        verifyFlowStatsFixedOne(fs, 1, 740, 3, 1, 60, 300);
        assertNull(AM_HUH, fs.getFlags());
        verifyFlowStatsFixedTwo(fs, 0x1234, 256, 37000);

        mfi = fs.getMatch().getMatchFields().iterator();
        verifyMatchField(mfi.next(), ETH_TYPE, EthernetType.IPv4);
        verifyMatchField(mfi.next(), IPV4_SRC, ip("15.255.1.1"), ip("255.0.0.0"));
        assertFalse(AM_HUH, mfi.hasNext());

        assertNull(AM_HUH, fs.getInstructions()); // doesn't apply to 1.0
        ai = fs.getActions().iterator();
        verifyActionSetField(ai.next(), IPV4_DST, ip("15.255.127.35"));
        verifyAction(ai.next(), ActionType.OUTPUT, Port.CONTROLLER);

        // validate data from third flow...
        fs = fsIt.next();
        verifyFlowStatsFixedOne(fs, 1, 748, 32, 7, 60, 300);
        assertNull(AM_HUH, fs.getFlags());
        verifyFlowStatsFixedTwo(fs, 0x1234, 2560, 37004);

        mfi = fs.getMatch().getMatchFields().iterator();
        assertFalse(AM_HUH, mfi.hasNext());

        assertNull(AM_HUH, fs.getInstructions()); // doesn't apply to 1.0
        ai = fs.getActions().iterator();
//        verifyAction(ai.next(), ActionType.OUTPUT, Port.CONTROLLER);
        // NOTE: verifyAction can't extract max-len param
        ActOutput ao = (ActOutput) ai.next();
        assertEquals(AM_NEQ, Port.CONTROLLER, ao.getPort());
        assertEquals(AM_NEQ, 128, ao.getMaxLen());
        verifyAction(ai.next(), ActionType.EXPERIMENTER, ExperimenterId.NICIRA);
    }

    @Test
    public void mpReplyFlow10() throws MessageParseException {
        print(EOL + "mpReplyFlow10()");
        OfPacketReader pkt = getOfmTestReader(TF_FLOW_REP_10);
        OpenflowMessage m = MessageFactory.parseMessage(pkt);
        print(FMT_START_TARGET, pkt.startIndex(), pkt.targetIndex());
        validateReplyFlow10(m);
    }

    @Test
    public void mpReplyFlow10Twice() throws MessageParseException {
        print(EOL + "mpReplyFlow10Twice()");
        OfPacketReader pkt = getOfmTestReader(TF_FLOW_REP_10_TWICE);

        OpenflowMessage m = MessageFactory.parseMessage(pkt);
        print(FMT_START_TARGET, pkt.startIndex(), pkt.targetIndex());
        validateReplyFlow10(m);

        m = MessageFactory.parseMessage(pkt);
        print(FMT_START_TARGET, pkt.startIndex(), pkt.targetIndex());
        validateReplyFlow10(m);
    }


    // ============================================= CREATING / ENCODING ====

    @Test
    public void encodeMpRequestFlow13() {
        print(EOL + "encodeMpRequestFlow13()");
        final ProtocolVersion pv = V_1_3;
        OfmMutableMultipartRequest req = (OfmMutableMultipartRequest)
                MessageFactory.create(pv, MULTIPART_REQUEST);
        req.clearXid();

        MBodyMutableFlowStatsRequest body = (MBodyMutableFlowStatsRequest)
                MpBodyFactory.createRequestBody(pv, FLOW);
        body.outPort(EXP_OPORT).cookie(0x123).cookieMask(0xfff);

        MutableMatch mm = MatchFactory.createMatch(pv);
        mm.addField(createBasicField(pv, ETH_SRC, EXP_MAC_SRC, EXP_MAC_SRC_MASK));
        body.match((Match) mm.toImmutable());

        req.body((MultipartBody) body.toImmutable());
        // now encode and verify
        encodeAndVerifyMessage(req.toImmutable(), TF_FLOW_REQ_13);
    }

    @Test
    public void encodeMpRequestFlow13WithMpType() {
        print(EOL + "encodeMpRequestFlow13WithMpType()");
        final ProtocolVersion pv = V_1_3;
        OfmMutableMultipartRequest req = (OfmMutableMultipartRequest)
                MessageFactory.create(pv, MULTIPART_REQUEST, FLOW);
        req.clearXid();
        MBodyMutableFlowStatsRequest body =
                (MBodyMutableFlowStatsRequest) req.getBody();
        body.outPort(EXP_OPORT).cookie(0x123).cookieMask(0xfff);
        MutableMatch mm = MatchFactory.createMatch(pv);
        mm.addField(createBasicField(pv, ETH_SRC, EXP_MAC_SRC, EXP_MAC_SRC_MASK));
        body.match((Match) mm.toImmutable());
        encodeAndVerifyMessage(req.toImmutable(), TF_FLOW_REQ_13);
    }

    @Test
    public void encodeMpRequestFlow10() {
        print(EOL + "encodeMpRequestFlow10()");
        final ProtocolVersion pv = V_1_0;
        OfmMutableMultipartRequest req = (OfmMutableMultipartRequest)
                MessageFactory.create(pv, MULTIPART_REQUEST);
        req.clearXid();

        MBodyMutableFlowStatsRequest body = (MBodyMutableFlowStatsRequest)
                MpBodyFactory.createRequestBody(pv, FLOW);
        MutableMatch mm = MatchFactory.createMatch(pv);
        mm.addField(createBasicField(pv, ETH_SRC, EXP_MAC_SRC));
        body.match((Match) mm.toImmutable()).outPort(EXP_OPORT);
        req.body((MultipartBody) body.toImmutable());
        // now encode and verify
        encodeAndVerifyMessage(req.toImmutable(), TF_FLOW_REQ_10);
    }

    @Test
    public void encodeMpRequestFlow10WithMpType() {
        print(EOL + "encodeMpRequestFlow10WithMpType()");
        final ProtocolVersion pv = V_1_0;
        OfmMutableMultipartRequest req = (OfmMutableMultipartRequest)
                MessageFactory.create(pv, MULTIPART_REQUEST, FLOW);
        req.clearXid();
        MBodyMutableFlowStatsRequest body =
                (MBodyMutableFlowStatsRequest) req.getBody();
        MutableMatch mm = MatchFactory.createMatch(pv);
        mm.addField(createBasicField(pv, ETH_SRC, EXP_MAC_SRC));
        body.match((Match) mm.toImmutable()).outPort(EXP_OPORT);
        encodeAndVerifyMessage(req.toImmutable(), TF_FLOW_REQ_10);
    }

    @Test(expected = VersionMismatchException.class)
    public void mismatchedMatchVersion() {
        MBodyMutableFlowStatsRequest body = (MBodyMutableFlowStatsRequest)
                MpBodyFactory.createRequestBody(V_1_0, FLOW);
        MutableMatch mm = MatchFactory.createMatch(V_1_3);
        body.match((Match) mm.toImmutable());
    }

    @Test(expected = VersionMismatchException.class)
    public void mismatchedBodyVersion() {
        MBodyMutableFlowStatsRequest body = (MBodyMutableFlowStatsRequest)
                MpBodyFactory.createRequestBody(V_1_3, FLOW);
        MutableMatch mm = MatchFactory.createMatch(V_1_3);
        body.match((Match) mm.toImmutable());

        OfmMutableMultipartRequest req =  (OfmMutableMultipartRequest)
                MessageFactory.create(V_1_0, MULTIPART_REQUEST);
        req.body((MultipartBody) body.toImmutable());
    }

    private static final Set<FlowModFlag> FLAGS_FLOW_1 =
            EnumSet.of(SEND_FLOW_REM, NO_PACKET_COUNTS, NO_BYTE_COUNTS);

    private static final Set<FlowModFlag> FLAGS_FLOW_2 =
            EnumSet.of(SEND_FLOW_REM, NO_BYTE_COUNTS);

    private static final Set<FlowModFlag> FLAGS_FLOW_3 =
            EnumSet.of(SEND_FLOW_REM);

    @Test
    public void encodeMpReplyFlow13() throws IncompleteStructureException {
        print(EOL + "encodeMpReplyFlow13()");
        final ProtocolVersion pv = V_1_3;
        OfmMutableMultipartReply rep = (OfmMutableMultipartReply)
                MessageFactory.create(pv, MULTIPART_REPLY);
        rep.clearXid();

        MBodyFlowStats.MutableArray array = (MBodyFlowStats.MutableArray)
                MpBodyFactory.createReplyBody(pv, FLOW);

        fillOutArray13(array);

        // FINALLY ++++++++++
        rep.body((MultipartBody) array.toImmutable());
        // now encode and verify
        encodeAndVerifyMessage(rep.toImmutable(), TF_FLOW_REP_13);
    }

    @Test
    public void encodeMpReplyFlow13WithMpType()
            throws IncompleteStructureException {
        print(EOL + "encodeMpReplyFlow13WithMpType()");
        final ProtocolVersion pv = V_1_3;
        OfmMutableMultipartReply rep = (OfmMutableMultipartReply)
                MessageFactory.create(pv, MULTIPART_REPLY, FLOW);
        rep.clearXid();
        MBodyFlowStats.MutableArray array =
                (MBodyFlowStats.MutableArray) rep.getBody();
        fillOutArray13(array);
        encodeAndVerifyMessage(rep.toImmutable(), TF_FLOW_REP_13);
    }

    private void fillOutArray13(MBodyFlowStats.MutableArray array)
            throws IncompleteStructureException {
        final ProtocolVersion pv = V_1_3;

        // ==== create the first flow stats object ====
        MBodyMutableFlowStats mfs = (MBodyMutableFlowStats)
                MpBodyFactory.createReplyBodyElement(pv, FLOW);

        mfs.tableId(tid(0)).duration(300, 50000).priority(4)
                .idleTimeout(60).hardTimeout(300).flags(FLAGS_FLOW_1)
                .cookie(0x1234).packetCount(2342).byteCount(13423234);

        // create the match object
        MutableMatch mm = MatchFactory.createMatch(pv)
                .addField(createBasicField(pv, ETH_SRC, EXP_MAC_SRC, EXP_MAC_SRC_MASK))
                .addField(createBasicField(pv, ETH_TYPE, EthernetType.IPv4))
                .addField(createBasicField(pv, IP_PROTO, IpProtocol.TCP))
                .addField(createBasicField(pv, TCP_DST, EXP_PORT));
        mfs.match((Match) mm.toImmutable());

        // create the list of instructions
        List<Instruction> insList = new ArrayList<>();
        insList.add(createInstruction(pv, InstructionType.CLEAR_ACTIONS));
        InstrMutableAction apply =
                createMutableInstruction(pv, InstructionType.APPLY_ACTIONS)
                        .addAction(createAction(pv, ActionType.DEC_NW_TTL))
                        .addAction(createActionSetField(pv, ETH_DST, EXP_MAC_DST))
                        .addAction(createActionSetField(pv, IPV4_DST, EXP_IP_DST));
        insList.add((Instruction) apply.toImmutable());
        mfs.instructions(insList);

        array.addFlowStats((MBodyFlowStats) mfs.toImmutable());


        // ==== create the second flow stats object ====
        mfs = (MBodyMutableFlowStats)
                MpBodyFactory.createReplyBodyElement(pv, FLOW);

        mfs.tableId(tid(1)).duration(740, 3).priority(1)
                .idleTimeout(60).hardTimeout(300).flags(FLAGS_FLOW_2)
                .cookie(0x1234).packetCount(256).byteCount(37000);

        // create the match object
        mm = MatchFactory.createMatch(pv)
                .addField(createBasicField(pv, ETH_SRC, EXP_MAC_SRC, EXP_MAC_SRC_MASK))
                .addField(createBasicField(pv, ETH_TYPE, EthernetType.IPv4));
        mfs.match((Match) mm.toImmutable());

        // create the list of instructions
        insList = new ArrayList<>();
        insList.add(createInstruction(pv, InstructionType.GOTO_TABLE,
                tid(3)));
        mfs.instructions(insList);

        array.addFlowStats((MBodyFlowStats) mfs.toImmutable());


        // ==== create the third flow stats object ====
        mfs = (MBodyMutableFlowStats)
                MpBodyFactory.createReplyBodyElement(pv, FLOW);

        mfs.tableId(tid(1)).duration(748, 32).priority(7)
                .idleTimeout(60).hardTimeout(300).flags(FLAGS_FLOW_3)
                .cookie(0x1234).packetCount(2560).byteCount(37004);

        // create the match object
        mm = MatchFactory.createMatch(pv)
                .addField(createBasicField(pv, ETH_SRC, EXP_MAC_SRC, EXP_MAC_SRC_MASK))
                .addField(createBasicField(pv, ETH_TYPE, EthernetType.IPv4));
        mfs.match((Match) mm.toImmutable());

        // create the list of instructions
        insList = new ArrayList<>();
        insList.add(createInstruction(pv, InstructionType.METER,
                MeterId.valueOf(192)));
        insList.add(createInstruction(pv, InstructionType.EXPERIMENTER,
                ExperimenterId.HP, HOLA_DATA));
        mfs.instructions(insList);

        array.addFlowStats((MBodyFlowStats) mfs.toImmutable());

    }

    private static final byte[] HOLA_DATA = {
            0x48, 0x6f, 0x6c, 0x61, 0x21, 0x00, 0x00, 0x00
    };


    @Test
    public void encodeMpReplyFlow10() throws IncompleteStructureException {
        print(EOL + "encodeMpReplyFlow10()");
        final ProtocolVersion pv = V_1_0;
        OfmMutableMultipartReply rep = (OfmMutableMultipartReply)
                MessageFactory.create(pv, MULTIPART_REPLY);
        rep.clearXid();

        MBodyFlowStats.MutableArray array = (MBodyFlowStats.MutableArray)
                MpBodyFactory.createReplyBody(pv, FLOW);

        fillOutArray10(array);

        // FINALLY ++++++++++
        rep.body((MultipartBody) array.toImmutable());
        // now encode and verify
        encodeAndVerifyMessage(rep.toImmutable(), TF_FLOW_REP_10);
    }

    @Test
    public void encodeMpReplyFlow10WithMpType()
            throws IncompleteStructureException {
        print(EOL + "encodeMpReplyFlow10WithMpType()");
        final ProtocolVersion pv = V_1_0;
        OfmMutableMultipartReply rep = (OfmMutableMultipartReply)
                MessageFactory.create(pv, MULTIPART_REPLY, FLOW);
        rep.clearXid();
        MBodyFlowStats.MutableArray array =
                (MBodyFlowStats.MutableArray) rep.getBody();
        fillOutArray10(array);
        encodeAndVerifyMessage(rep.toImmutable(), TF_FLOW_REP_10);
    }

    private void fillOutArray10(MBodyFlowStats.MutableArray array)
            throws IncompleteStructureException {
        final ProtocolVersion pv = V_1_0;

        // ==== create the first flow stats object ====
        MBodyMutableFlowStats mfs = (MBodyMutableFlowStats)
                MpBodyFactory.createReplyBodyElement(pv, FLOW);
        mfs.tableId(tid(0));

        // create the match object
        MutableMatch mm = MatchFactory.createMatch(pv);
        mm.addField(createBasicField(pv, ETH_SRC, EXP_MAC_SRC));
        mfs.match((Match) mm.toImmutable()).duration(300, 50000).priority(4)
                .idleTimeout(60).hardTimeout(300).cookie(0x1234)
                .packetCount(2342).byteCount(13423234);

        // create the list of actions
        List<Action> actList = new ArrayList<>();
        actList.add(createActionSetField(pv, VLAN_VID, VlanId.valueOf(42)));
        actList.add(createActionSetField(pv, VLAN_PCP, 7));
        actList.add(createActionSetField(pv, IPV4_DST, ip("15.255.127.35")));
        mfs.actions(actList);

        array.addFlowStats((MBodyFlowStats) mfs.toImmutable());

        // ==== create the second flow stats object ====
        mfs = (MBodyMutableFlowStats)
                MpBodyFactory.createReplyBodyElement(pv, FLOW);
        mfs.tableId(tid(1));

        // create the match object
        mm = MatchFactory.createMatch(pv)
                .addField(createBasicField(pv, ETH_TYPE, EthernetType.IPv4))
                .addField(createBasicField(pv, IPV4_SRC, ip("15.255.1.1"),
                                                         ip("255.0.0.0")));
        mfs.match((Match) mm.toImmutable()).duration(740, 3).priority(1)
                .idleTimeout(60).hardTimeout(300).cookie(0x1234)
                .packetCount(256).byteCount(37000);

        // create the list of actions
        actList = new ArrayList<>();
        actList.add(createActionSetField(pv, IPV4_DST,
                IpAddress.valueOf("15.255.127.35")));
        actList.add(createAction(pv, ActionType.OUTPUT,
                Port.CONTROLLER, ActOutput.CONTROLLER_NO_BUFFER));
        mfs.actions(actList);

        array.addFlowStats((MBodyFlowStats) mfs.toImmutable());

        // ==== create the third flow stats object ====
        mfs = (MBodyMutableFlowStats)
                MpBodyFactory.createReplyBodyElement(pv, FLOW);
        mfs.tableId(tid(1));

        // create the match object
        mm = MatchFactory.createMatch(pv);
        mfs.match((Match) mm.toImmutable()).duration(748, 32).priority(7)
                .idleTimeout(60).hardTimeout(300).cookie(0x1234)
                .packetCount(2560).byteCount(37004);

        // create the list of actions
        actList = new ArrayList<>();
        actList.add(createAction(pv, ActionType.OUTPUT,
                Port.CONTROLLER, 128));
        actList.add(createAction(pv, ActionType.EXPERIMENTER,
                ExperimenterId.NICIRA, NICIRA_DATA));
        mfs.actions(actList);

        array.addFlowStats((MBodyFlowStats) mfs.toImmutable());

    }

    private static final byte[] NICIRA_DATA = {
            1, 2, 3, 4, 5, 6, 7, 8,
    };
}
