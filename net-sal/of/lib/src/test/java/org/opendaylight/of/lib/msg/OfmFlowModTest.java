/*
 * (c) Copyright 2012-2014 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.of.lib.msg;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.opendaylight.of.lib.IncompleteMessageException;
import org.opendaylight.of.lib.InvalidMutableException;
import org.opendaylight.of.lib.ProtocolVersion;
import org.opendaylight.of.lib.VersionMismatchException;
import org.opendaylight.of.lib.dt.BufferId;
import org.opendaylight.of.lib.dt.GroupId;
import org.opendaylight.of.lib.dt.TableId;
import org.opendaylight.of.lib.instr.*;
import org.opendaylight.of.lib.match.*;
import org.opendaylight.util.junit.SlowestTests;
import org.opendaylight.util.net.*;

import java.util.EnumSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.*;
import static org.opendaylight.of.lib.ProtocolVersion.V_1_0;
import static org.opendaylight.of.lib.ProtocolVersion.V_1_3;
import static org.opendaylight.of.lib.instr.ActionFactory.createAction;
import static org.opendaylight.of.lib.instr.ActionFactory.createActionSetField;
import static org.opendaylight.of.lib.instr.ActionType.DEC_NW_TTL;
import static org.opendaylight.of.lib.instr.ActionType.SET_FIELD;
import static org.opendaylight.of.lib.instr.InstructionFactory.createInstruction;
import static org.opendaylight.of.lib.instr.InstructionFactory.createMutableInstruction;
import static org.opendaylight.of.lib.instr.InstructionType.APPLY_ACTIONS;
import static org.opendaylight.of.lib.instr.InstructionType.WRITE_METADATA;
import static org.opendaylight.of.lib.match.FieldFactory.createBasicField;
import static org.opendaylight.of.lib.match.MatchFactory.createMatch;
import static org.opendaylight.of.lib.match.OxmBasicFieldType.*;
import static org.opendaylight.of.lib.msg.FlowModFlag.*;
import static org.opendaylight.of.lib.msg.MessageType.FLOW_MOD;
import static org.opendaylight.util.junit.TestTools.*;

/**
 * Unit test for {@link OfmFlowMod} and {@link OfmMutableFlowMod} classes.
 *
 * @author Simon Hunt
 */
@Category(SlowestTests.class)
public class OfmFlowModTest extends OfmTest {

    // Test files...
    private static final String TF_FM_13 = "v13/flowMod";
    private static final String TF_FM_10 = "v10/flowMod";


    private void verifyFlowModHeader(OfmFlowMod msg, long expCook, long expMask,
                                     TableId expTid, FlowModCommand expCmd,
                                     int expIdle, int expHard, int expPri,
                                     BufferId expBuf, BigPortNumber expPort,
                                     GroupId expGrp) {
        assertEquals(AM_NEQ, expCook, msg.getCookie());
        assertEquals(AM_NEQ, expMask, msg.getCookieMask());
        assertEquals(AM_NEQ, expTid, msg.getTableId());
        assertEquals(AM_NEQ, expCmd, msg.getCommand());
        assertEquals(AM_NEQ, expIdle, msg.getIdleTimeout());
        assertEquals(AM_NEQ, expHard, msg.getHardTimeout());
        assertEquals(AM_NEQ, expPri, msg.getPriority());
        assertEquals(AM_NEQ, expBuf, msg.getBufferId());
        assertEquals(AM_NEQ, expPort, msg.getOutPort());
        assertEquals(AM_NEQ, expGrp, msg.getOutGroup());
    }

    // === Expected values....

    private static final long EXP_COOK = 0x1234;
    private static final long EXP_COOK_MASK = 0xffff;
    private static final TableId EXP_TID = tid(3);
    private static final FlowModCommand EXP_CMD = FlowModCommand.ADD;
    private static final int EXP_IDLE = 30;
    private static final int EXP_HARD = 300;
    private static final int EXP_PRI = 64;
    private static final BufferId EXP_BID = bid(7);
    private static final BigPortNumber EXP_OPORT = Port.ANY;
    private static final GroupId EXP_OGROUP = GroupId.ANY;
    private static final Set<FlowModFlag> EXP_FLAGS =
            EnumSet.of(SEND_FLOW_REM, CHECK_OVERLAP, NO_BYTE_COUNTS);
    private static final Set<FlowModFlag> EXP_FLAGS_10 =
            EnumSet.of(SEND_FLOW_REM, CHECK_OVERLAP);

    private static final OxmBasicFieldType EXP_MF_T1 = ETH_SRC;
    private static final OxmBasicFieldType EXP_MF_T2 = ETH_TYPE;
    private static final OxmBasicFieldType EXP_MF_T3 = IP_PROTO;
    private static final OxmBasicFieldType EXP_MF_T4 = TCP_DST;
    private static final OxmBasicFieldType[] EXP_MF = {
            EXP_MF_T1, EXP_MF_T2, EXP_MF_T3, EXP_MF_T4,
    };

    private static final MacAddress EXP_MF_V1 = mac("112233:445566");
    private static final EthernetType EXP_MF_V2 = EthernetType.IPv4;
    private static final IpProtocol EXP_MF_V3 = IpProtocol.TCP;
    private static final PortNumber EXP_MF_V4 = pn(25);
    private static final Object[] EXP_MF_VAL = {
            EXP_MF_V1, EXP_MF_V2, EXP_MF_V3, EXP_MF_V4,
    };

    private static final MacAddress EXP_MF_M1 = mac("ffffff:000000");
    private static final Object[] EXP_MF_MASK = {
            EXP_MF_M1, null, null, null,
    };

    private static final long EXP_META = 0xabcd0000L;
    private static final long EXP_META_MASK = 0xffff0000L;

    private static final ActionType EXP_ACT_1 = DEC_NW_TTL;
    private static final ActionType EXP_ACT_2 = SET_FIELD;
    private static final ActionType EXP_ACT_3 = SET_FIELD;

    private static final OxmBasicFieldType EXP_SFMF_T2 = ETH_DST;
    private static final OxmBasicFieldType EXP_SFMF_T3 = IPV4_DST;

    private static final MacAddress EXP_SFMF_V2 = mac("00001e:453411");
    private static final IpAddress EXP_SFMF_V3 = ip("15.254.17.1");


    private MutableMessage mm;

    // ========================================================= PARSING ====
    private FlowModFlag[] flagsAsArray(Set<FlowModFlag> flags) {
        return flags.toArray(new FlowModFlag[flags.size()]);
    }

    @Test
    public void flowMod13() {
        print(EOL + "flowMod13()");
        OfmFlowMod msg = (OfmFlowMod) verifyMsgHeader(TF_FM_13, V_1_3,
                FLOW_MOD, 160);

        verifyFlowModHeader(msg, EXP_COOK, EXP_COOK_MASK, EXP_TID, EXP_CMD,
                EXP_IDLE, EXP_HARD, EXP_PRI, EXP_BID, EXP_OPORT, EXP_OGROUP);

        verifyFlags(msg.getFlags(), flagsAsArray(EXP_FLAGS));

        Iterator<MatchField> mfi = msg.getMatch().getMatchFields().iterator();
        int i = 0;
        while(mfi.hasNext()) {
            verifyMatchField(mfi.next(),
                    EXP_MF[i], EXP_MF_VAL[i], EXP_MF_MASK[i]);
            i++;
        }

        List<Action> actions = msg.getActions(); // should be null

        Iterator<Instruction> ii = msg.getInstructions().iterator();
        Instruction ins = ii.next();
        verifyInstrWrMeta(ins, EXP_META, EXP_META_MASK);

        ins = ii.next();
        verifyInstrActions(ins, InstructionType.APPLY_ACTIONS, 3);
        InstrApplyActions iaa = (InstrApplyActions) ins;
        Iterator<Action> aIter = iaa.getActionList().iterator();
        verifyAction(aIter.next(), EXP_ACT_1);
        verifyActionSetField(aIter.next(), EXP_SFMF_T2, EXP_SFMF_V2);
        verifyActionSetField(aIter.next(), EXP_SFMF_T3, EXP_SFMF_V3);
        assertFalse(AM_HUH, aIter.hasNext());

        assertFalse(AM_HUH, ii.hasNext());

        assertNull(AM_HUH, actions);
    }

    @Test
    public void flowMod10() {
        print(EOL + "flowMod10()");
        OfmFlowMod msg = (OfmFlowMod) verifyMsgHeader(TF_FM_10, V_1_0,
                FLOW_MOD, 96);
        verifyFlowModHeader(msg, EXP_COOK, 0, null, FlowModCommand.ADD,
                EXP_IDLE, EXP_HARD, EXP_PRI, EXP_BID, EXP_OPORT, null);
        verifyFlags(msg.getFlags(), flagsAsArray(EXP_FLAGS_10));

        Iterator<MatchField> mIter = msg.getMatch().getMatchFields().iterator();
        verifyMatchField(mIter.next(), ETH_SRC, EXP_MF_V1);
        verifyMatchField(mIter.next(), ETH_TYPE, EthernetType.IPv4);
        verifyMatchField(mIter.next(), IP_PROTO, IpProtocol.TCP);
        verifyMatchField(mIter.next(), TCP_DST, pn(25));
        assertFalse(mIter.hasNext());

        Iterator<Action> aIter = msg.getActions().iterator();
        verifyActionSetField(aIter.next(), VLAN_VID, VlanId.valueOf(42));
        verifyActionSetField(aIter.next(), VLAN_PCP, 7);
        verifyActionSetField(aIter.next(), IPV4_DST, ip("15.255.127.35"));
        assertFalse(aIter.hasNext());
    }

    // ============================================= CREATING / ENCODING ====

    @Test
    public void encodeFlowMod13() {
        print(EOL + "encodeFlowMod13()");
        final ProtocolVersion pv = V_1_3;

        mm = MessageFactory.create(pv, FLOW_MOD);
        mm.clearXid();
        verifyMutableHeader(mm, pv, FLOW_MOD, 0);

        // set the "fixed" fields
        OfmMutableFlowMod mod = (OfmMutableFlowMod) mm;
        mod.cookie(EXP_COOK).cookieMask(EXP_COOK_MASK).tableId(EXP_TID)
                .command(EXP_CMD).idleTimeout(EXP_IDLE).hardTimeout(EXP_HARD)
                .priority(EXP_PRI).bufferId(EXP_BID)
                .outPort(EXP_OPORT).outGroup(EXP_OGROUP)
                .flowModFlags(EXP_FLAGS);

        // assemble a match definition
        MutableMatch match = createMatch(pv)
                .addField(createBasicField(pv, EXP_MF_T1, EXP_MF_V1, EXP_MF_M1))
                .addField(createBasicField(pv, EXP_MF_T2, EXP_MF_V2))
                .addField(createBasicField(pv, EXP_MF_T3, EXP_MF_V3))
                .addField(createBasicField(pv, EXP_MF_T4, EXP_MF_V4));
        mod.match((Match) match.toImmutable());

        // assemble a list of instructions
        Instruction ins;
        MFieldBasic mfb;
        /* IMPLEMENTATION NOTE:
         *   We are breaking these statements up into constituent
         *   components intentionally, to illustrate the actual
         *   relationship of calls (set-field action / match field to set).
         *   The coding can be greatly simplified, as illustrated in the
         *   next unit test.
         */

        // first instruction is write metadata
        ins = createInstruction(pv, WRITE_METADATA, EXP_META, EXP_META_MASK);
        mod.addInstruction(ins);

        // second instruction is apply actions
        InstrMutableAction mutIns = createMutableInstruction(pv, APPLY_ACTIONS);
        // first action DEC_NW_TTL
        mutIns.addAction(createAction(pv, EXP_ACT_1));
        // second action SET_FIELD.ETH_DST
        mfb = createBasicField(pv, EXP_SFMF_T2, EXP_SFMF_V2);
        mutIns.addAction(createAction(pv, EXP_ACT_2, mfb));
        // third action SET_FIELD.IPV4_DST
        mfb = createBasicField(pv, EXP_SFMF_T3, EXP_SFMF_V3);
        mutIns.addAction(createAction(pv, EXP_ACT_3, mfb));
        // add the instruction to the flow mod
        mod.addInstruction((Instruction) mutIns.toImmutable());

        encodeAndVerifyMessage(mm.toImmutable(), TF_FM_13);
    }

    @Test
    public void encodeFlowModMagicNumbers() {
        print(EOL + "encodeFlowModMagicNumbers()");
        OfmMutableFlowMod mod = (OfmMutableFlowMod)
                MessageFactory.create(V_1_3, FLOW_MOD);
        mod.clearXid();
        // NOTE: Intentionally not using the EXP_* constants, so it is
        //       clearer to see how easy it is to construct one of these..
        //       (though we are not promoting the use of MAGIC Numbers!)
        mod.cookie(0x1234).cookieMask(0xffff).tableId(tid(3))
                .command(FlowModCommand.ADD).idleTimeout(30).hardTimeout(300)
                .priority(64).bufferId(bid(7));
        // let out-port and out-group both default to "ANY"
        mod.flowModFlags(EnumSet.of(SEND_FLOW_REM, CHECK_OVERLAP, NO_BYTE_COUNTS));

        // Define our match
        MacAddress ETH_SRC_MAC = mac("112233:445566");
        MacAddress ETH_SRC_MASK = mac("ffffff:000000");
        PortNumber SMTP_PORT = pn(25);

        // NOTE: static import of FieldFactory.createBasicField
        //                    and OxmBasicFieldType.*
        MutableMatch match = createMatch(V_1_3)
            .addField(createBasicField(V_1_3, ETH_SRC, ETH_SRC_MAC, ETH_SRC_MASK))
            .addField(createBasicField(V_1_3, ETH_TYPE, EthernetType.IPv4))
            .addField(createBasicField(V_1_3, IP_PROTO, IpProtocol.TCP))
            .addField(createBasicField(V_1_3, TCP_DST, SMTP_PORT));
        mod.match((Match) match.toImmutable());

        // Now define the list of (2) Instructions
        // NOTE: static import of InstructionFactory.createInstruction
        //                    and InstructionFactory.createMutableInstruction
        //                    and InstructionType.*

        // First instruction: write-metadata
        mod.addInstruction(createInstruction(V_1_3,
                WRITE_METADATA, 0xabcd0000L, 0xffff0000L));

        // Second instruction: apply-actions
        // NOTE: static import of ActionType.*
        MacAddress SET_ETH_DST_MAC = mac("00001e:453411");
        IpAddress SET_IPV4_DEST_IP = ip("15.254.17.1");

        InstrMutableAction apply = createMutableInstruction(V_1_3, APPLY_ACTIONS)
            .addAction(createAction(V_1_3, DEC_NW_TTL))
            .addAction(createActionSetField(V_1_3, ETH_DST, SET_ETH_DST_MAC))
            .addAction(createActionSetField(V_1_3, IPV4_DST, SET_IPV4_DEST_IP));
        mod.addInstruction((Instruction) apply.toImmutable());

        // finally encode and verify
        encodeAndVerifyMessage(mod.toImmutable(), TF_FM_13);
    }

    @Test
    public void encodeFlowMod10() {
        print(EOL + "encodeFlowMod10()");
        final ProtocolVersion pv = V_1_0;

        mm = MessageFactory.create(pv, FLOW_MOD);
        mm.clearXid();
        verifyMutableHeader(mm, pv, FLOW_MOD, 0);

        // set the "fixed" fields
        OfmMutableFlowMod mod = (OfmMutableFlowMod) mm;
        mod.cookie(EXP_COOK).command(EXP_CMD).idleTimeout(EXP_IDLE)
                .hardTimeout(EXP_HARD).priority(EXP_PRI).bufferId(EXP_BID)
                .outPort(EXP_OPORT).flowModFlags(EXP_FLAGS_10);

        // assemble a match definition
        MutableMatch match = createMatch(pv)
            .addField(createBasicField(pv, ETH_SRC, EXP_MF_V1))
            .addField(createBasicField(pv, ETH_TYPE, EthernetType.IPv4))
            .addField(createBasicField(pv, IP_PROTO, IpProtocol.TCP))
            .addField(createBasicField(pv, TCP_DST, pn(25)));
        mod.match((Match) match.toImmutable());

        // assemble a list of actions
        mod.addAction(createActionSetField(pv, VLAN_VID, VlanId.valueOf(42)))
            .addAction(createActionSetField(pv, VLAN_PCP, 7))
            .addAction(createActionSetField(pv, IPV4_DST, ip("15.255.127.35")));

        // now encode and verify
        encodeAndVerifyMessage(mod.toImmutable(), TF_FM_10);
    }

    @Test
    public void createWithCommand() {
        print(EOL + "createWithCommand()");
        OfmMutableFlowMod m = (OfmMutableFlowMod) MessageFactory.create(V_1_3,
                FLOW_MOD, FlowModCommand.MODIFY_STRICT);
        m.clearXid();
        verifyMutableHeader(m, V_1_3, FLOW_MOD, 0);
        assertEquals(AM_NEQ, FlowModCommand.MODIFY_STRICT, m.getCommand());
    }

    @Test
    public void precision() {
        double d = 1315097.317463547;
        String s = String.format("%.1f", d);
        print("Result is {}", s);
    }

    private static final int ENCODE_ITERATIONS = 10000000;

    @Test @Ignore("only do this if you have the time to wait")
    public void flowModEncodeSpeedTest() {
        print(EOL + "flowModEncodeSpeedTest()");
        print("Encoding a flowmod message {} times...", ENCODE_ITERATIONS);
        OpenflowMessage msg = createFlowMod13();
        long start = 0;
        long finish = 0;
        try {
            start = System.currentTimeMillis();
            for (int i=0; i<ENCODE_ITERATIONS; i++)
                MessageFactory.encodeMessage(msg);
            finish = System.currentTimeMillis();
        } catch (Exception e) {
            fail(AM_UNEX);
        }
        double durationSeconds = ((double)finish - (double)start) / 1000.0;
        double perSec = (double)ENCODE_ITERATIONS / durationSeconds;
        String perSecStr = String.format("%.1f", perSec);
        print("Test duration = {} seconds", durationSeconds);
        print("Estimated flowmod encode speed is {} / second", perSecStr);
    }

    private OpenflowMessage createFlowMod13() {
        OfmMutableFlowMod mod = (OfmMutableFlowMod)
                MessageFactory.create(V_1_3, FLOW_MOD, FlowModCommand.ADD);
        mod.tableId(tid(3)).bufferId(bid(7));
        mod.flowModFlags(EnumSet.of(SEND_FLOW_REM, CHECK_OVERLAP, NO_BYTE_COUNTS));

        MacAddress ETH_SRC_MAC = mac("112233:445566");
        MacAddress ETH_SRC_MASK = mac("ffffff:000000");
        PortNumber SMTP_PORT = pn(25);

        MutableMatch match = createMatch(V_1_3)
                .addField(createBasicField(V_1_3, ETH_SRC, ETH_SRC_MAC, ETH_SRC_MASK))
                .addField(createBasicField(V_1_3, ETH_TYPE, EthernetType.IPv4))
                .addField(createBasicField(V_1_3, IP_PROTO, IpProtocol.TCP))
                .addField(createBasicField(V_1_3, TCP_DST, SMTP_PORT));
        mod.match((Match) match.toImmutable());

        mod.addInstruction(createInstruction(V_1_3,
                WRITE_METADATA, 0xabcd0000L, 0xffff0000L));

        MacAddress SET_ETH_DST_MAC = mac("00001e:453411");
        IpAddress SET_IPV4_DEST_IP = ip("15.254.17.1");

        InstrMutableAction apply = createMutableInstruction(V_1_3, APPLY_ACTIONS)
                .addAction(createAction(V_1_3, DEC_NW_TTL))
                .addAction(createActionSetField(V_1_3, ETH_DST, SET_ETH_DST_MAC))
                .addAction(createActionSetField(V_1_3, IPV4_DST, SET_IPV4_DEST_IP));
        mod.addInstruction((Instruction) apply.toImmutable());
        return mod.toImmutable();
    }



    // ========================================================= COPYING ====

    @Test
    public void copy13() {
        print(EOL + "copy13()");
        OfmFlowMod msg = (OfmFlowMod) verifyMsgHeader(TF_FM_13, V_1_3,
                FLOW_MOD, 160);
        OpenflowMessage copy = MessageFactory.exactCopy(msg);
        assertEquals(AM_NEQ, OfmFlowMod.class, copy.getClass());
        assertNotSame(AM_HUH, msg, copy);
        encodeAndVerifyMessage(copy, TF_FM_13);
    }

    @Test
    public void copy13Mutable() {
        print(EOL + "copy13Mutable()");
        OfmFlowMod msg = (OfmFlowMod) verifyMsgHeader(TF_FM_13, V_1_3,
                FLOW_MOD, 160);
        MutableMessage copy = MessageFactory.exactMutableCopy(msg);
        assertEquals(AM_NEQ, OfmMutableFlowMod.class, copy.getClass());
        assertNotSame(AM_HUH, msg, copy);
        encodeAndVerifyMessage(copy.toImmutable(), TF_FM_13);
    }

    @Test
    public void copy10() {
        print(EOL + "copy10()");
        OfmFlowMod msg = (OfmFlowMod) verifyMsgHeader(TF_FM_10, V_1_0,
                FLOW_MOD, 96);
        OpenflowMessage copy = MessageFactory.exactCopy(msg);
        assertEquals(AM_NEQ, OfmFlowMod.class, copy.getClass());
        assertNotSame(AM_HUH, msg, copy);
        encodeAndVerifyMessage(copy, TF_FM_10);
    }

    @Test
    public void copy10Mutable() {
        print(EOL + "copy10Mutable()");
        OfmFlowMod msg = (OfmFlowMod) verifyMsgHeader(TF_FM_10, V_1_0,
                FLOW_MOD, 96);
        MutableMessage copy = MessageFactory.exactMutableCopy(msg);
        assertEquals(AM_NEQ, OfmMutableFlowMod.class, copy.getClass());
        assertNotSame(AM_HUH, msg, copy);
        encodeAndVerifyMessage(copy.toImmutable(), TF_FM_10);
    }

    //=======================================================================
    //=== Tests from reported defects : write a test to confirm the issue,
    //===  then fix the code, and prove that the issue has been fixed.

    private static final Set<FlowModFlag> FLAGS_13 =
            EnumSet.of(SEND_FLOW_REM, CHECK_OVERLAP, NO_BYTE_COUNTS);

    @Test
    public void npeFrom13FlowMod () {
        ProtocolVersion pv = V_1_3;
        BigPortNumber inPort = bpn(2);
        BigPortNumber outPort = bpn(1);

        OfmMutableFlowMod flowMod = (OfmMutableFlowMod)
                MessageFactory.create(pv, MessageType.FLOW_MOD);
        flowMod.clearXid();

        flowMod.cookie(0x1234).cookieMask(0xffff)
                .command(FlowModCommand.ADD).idleTimeout(32)
                .hardTimeout(200).priority(64)
                .bufferId(bid(7)).outPort(Port.ANY).outGroup(GroupId.ANY)
                .flowModFlags(FLAGS_13);

        MutableMatch match = createMatch(pv)
                .addField(createBasicField(pv, IN_PORT, inPort));
        flowMod.match((Match) match.toImmutable());

        InstrMutableAction mutIns = createMutableInstruction(pv, APPLY_ACTIONS);
        mutIns.addAction(createAction(pv, ActionType.OUTPUT, outPort));
        flowMod.addInstruction((Instruction) mutIns.toImmutable());
        OfmFlowMod m = (OfmFlowMod)flowMod.toImmutable();
        print(m.toDebugString());

        try {
            m.validate();
            fail(AM_NOEX);
        } catch (IncompleteMessageException e) {
            print(FMT_EX, e);
        }
    }

    private OfmMutableFlowMod emptyFlowMod(ProtocolVersion pv) {
        OfmMutableFlowMod m =  (OfmMutableFlowMod)
                MessageFactory.create(pv, FLOW_MOD);
        m.clearXid();
        return m;
    }

    private Match makeMatch(ProtocolVersion pv) {
        return (Match) createMatch(pv).toImmutable();
    }

    @Test
    public void validateNoCommand() {
        print(EOL + "validateNoCommand()");
        OpenflowMessage m = emptyFlowMod(V_1_3)
//                .command(FlowModCommand.ADD)
                .bufferId(bid(2))
                .match(makeMatch(V_1_3))
                .tableId(tid(1))
                .toImmutable();
        try {
            m.validate();
            fail(AM_NOEX);
        } catch (IncompleteMessageException e) {
            print(FMT_EX, e);
        }

        m = emptyFlowMod(V_1_0)
//                .command(FlowModCommand.ADD)
                .bufferId(bid(2))
                .match(makeMatch(V_1_0))
                .toImmutable();
        try {
            m.validate();
            fail(AM_NOEX);
        } catch (IncompleteMessageException e) {
            print(FMT_EX, e);
        }
    }

    @Test
    public void validateNoBufferId() {
        print(EOL + "validateNoBufferId()");
        OpenflowMessage m = emptyFlowMod(V_1_3)
                .command(FlowModCommand.ADD)
                .match(makeMatch(V_1_3))
                .tableId(tid(1))
                .toImmutable();
        OfmFlowMod fm = (OfmFlowMod) m;
        try {
            m.validate();
            assertEquals(AM_NEQ, BufferId.NO_BUFFER, fm.getBufferId());
        } catch (IncompleteMessageException e) {
            fail(AM_UNEX);
        }

        m = emptyFlowMod(V_1_0)
                .command(FlowModCommand.ADD)
                .match(makeMatch(V_1_0))
                .toImmutable();
        try {
            m.validate();
            assertEquals(AM_NEQ, BufferId.NO_BUFFER, fm.getBufferId());
        } catch (IncompleteMessageException e) {
            fail(AM_UNEX);
        }
    }

    @Test
    public void validateNoMatch() {
        print(EOL + "validateNoMatch()");
        OpenflowMessage m = emptyFlowMod(V_1_3)
                .command(FlowModCommand.ADD)
                .bufferId(bid(2))
//                .match(makeMatch(V_1_3))
                .tableId(tid(1))
                .toImmutable();
        try {
            m.validate();
            fail(AM_NOEX);
        } catch (IncompleteMessageException e) {
            print(FMT_EX, e);
        }

        // for 1.0, the table ID can (should?) be null
        m = emptyFlowMod(V_1_0)
                .command(FlowModCommand.ADD)
                .bufferId(bid(2))
//                .match(makeMatch(V_1_0))
                .toImmutable();
        try {
            m.validate();
            fail(AM_UNEX);
        } catch (IncompleteMessageException e) {
            print(FMT_EX, e);
        }
    }

    @Test
    public void validateNoTableId() {
        print(EOL + "validateNoTableId()");
        OpenflowMessage m = emptyFlowMod(V_1_3)
                .command(FlowModCommand.ADD)
                .bufferId(bid(2))
                .match(makeMatch(V_1_3))
//                .tableId(tid(1))
                .toImmutable();
        try {
            m.validate();
            fail(AM_NOEX);
        } catch (IncompleteMessageException e) {
            print(FMT_EX, e);
        }

        // for 1.0, the table ID can (should?) be null
        m = emptyFlowMod(V_1_0)
                .command(FlowModCommand.ADD)
                .bufferId(bid(2))
                .match(makeMatch(V_1_0))
                .toImmutable();
        try {
            m.validate();
            print(m);
        } catch (IncompleteMessageException e) {
            fail(AM_UNEX);
            print(e);
        }
    }

    @Test
    public void validatePriorityPatching() {
        print(EOL + "validatePriorityPatching()");
        OfmFlowMod msg = (OfmFlowMod) verifyMsgHeader(TF_FM_10, V_1_0,
                                                      FLOW_MOD, 96);
        assertEquals("incorrect original priority", 64, msg.getPriority());
        OfmFlowMod patched = msg.patchPriority(4321);
        assertEquals("incorrect original priority", 4321, msg.getPriority());
        assertSame("should be the same instance", msg, patched);
    }

    // === Slightly newer functionality...
    private static final String FMT_MSG_LEN = "Length of message = {} [{}]";
    private static final BigPortNumber PORT_23 = bpn(23);
    private static final IpAddress IP_123 = ip("15.255.255.123");
    private static final MacAddress MAC_123 = mac("00001e:000123");

    @Test
    public void clearActions10() {
        print(EOL + "clearActions10()");
        final ProtocolVersion pv = V_1_0;

        mm = MessageFactory.create(pv, FLOW_MOD);
        OfmMutableFlowMod mod = (OfmMutableFlowMod) mm;
        print(mod.toDebugString());

        int emptyLength = mod.length();
        print(FMT_MSG_LEN, emptyLength, "no actions");

        Action act = createAction(pv, ActionType.OUTPUT, PORT_23);
        mod.addAction(act);
        int oneActionLength = mod.length();
        print(FMT_MSG_LEN, oneActionLength, "one action");

        Action act2 = createActionSetField(pv, IPV4_SRC, IP_123);
        mod.addAction(act2);
        int twoActionsLength = mod.length();
        print(FMT_MSG_LEN, twoActionsLength, "two actions");
        print(mod.toDebugString());

        mod.clearActions();
        print(".. clearing actions ..");
        print(mod.toDebugString());
        assertEquals(AM_UXS, emptyLength, mod.length());

        // idempotent::
        mod.clearActions();
        print(".. clearing actions (again)..");
        print(mod.toDebugString());
        assertEquals(AM_UXS, emptyLength, mod.length());
    }

    @Test(expected = InvalidMutableException.class)
    public void clearActions10Immutable() {
        final ProtocolVersion pv = V_1_0;
        mm = MessageFactory.create(pv, FLOW_MOD);
        OfmMutableFlowMod mod = (OfmMutableFlowMod) mm;
        mod.toImmutable();
        mod.clearActions();
    }

    @Test(expected = VersionMismatchException.class)
    public void clearActions13() {
        final ProtocolVersion pv = V_1_3;
        mm = MessageFactory.create(pv, FLOW_MOD);
        OfmMutableFlowMod mod = (OfmMutableFlowMod) mm;
        mod.clearActions();
    }

    @Test
    public void clearInstructions13() {
        print(EOL + "clearInstructions13()");
        final ProtocolVersion pv = V_1_3;

        mm = MessageFactory.create(pv, FLOW_MOD);
        OfmMutableFlowMod mod = (OfmMutableFlowMod) mm;
        print(mod.toDebugString());

        int emptyLength = mod.length();
        print(FMT_MSG_LEN, emptyLength, "no instructions");

        // First instruction: write-metadata
        mod.addInstruction(createInstruction(V_1_3,
                WRITE_METADATA, 0xabcd0000L, 0xffff0000L));
        int oneInsLength = mod.length();
        print(FMT_MSG_LEN, oneInsLength, "one instruction");

        // Second instruction: apply-actions
        // NOTE: static import of ActionType.*
        InstrMutableAction apply = createMutableInstruction(V_1_3, APPLY_ACTIONS)
                .addAction(createAction(V_1_3, DEC_NW_TTL))
                .addAction(createActionSetField(V_1_3, ETH_DST, MAC_123))
                .addAction(createActionSetField(V_1_3, IPV4_DST, IP_123));
        mod.addInstruction((Instruction) apply.toImmutable());
        int twoInsLength = mod.length();
        print(FMT_MSG_LEN, twoInsLength, "two instructions");
        print(mod.toDebugString());

        mod.clearInstructions();
        print(".. clearing instructions ..");
        print(mod.toDebugString());
        assertEquals(AM_UXS, emptyLength, mod.length());

        // idempotent::
        mod.clearInstructions();
        print(".. clearing instructions (again)..");
        print(mod.toDebugString());
        assertEquals(AM_UXS, emptyLength, mod.length());
    }

    @Test(expected = InvalidMutableException.class)
    public void clearInstructions13Immutable() {
        final ProtocolVersion pv = V_1_3;
        mm = MessageFactory.create(pv, FLOW_MOD);
        OfmMutableFlowMod mod = (OfmMutableFlowMod) mm;
        mod.toImmutable();
        mod.clearInstructions();
    }

    @Test(expected = VersionMismatchException.class)
    public void clearInstructions10() {
        final ProtocolVersion pv = V_1_0;
        mm = MessageFactory.create(pv, FLOW_MOD);
        OfmMutableFlowMod mod = (OfmMutableFlowMod) mm;
        mod.clearInstructions();
    }

    @Test
    public void overwritingMatch10() {
        print(EOL + "overwritingMatch()");
        final ProtocolVersion pv = V_1_0;

        mm = MessageFactory.create(pv, FLOW_MOD);
        OfmMutableFlowMod mod = (OfmMutableFlowMod) mm;
        print(mod.toDebugString());
        int initLength = mod.length();
        print(FMT_MSG_LEN, initLength, "initial length");
        // match starts as null - we'll need to set one
        assertNull(AM_HUH, mod.getMatch());

        MutableMatch match = MatchFactory.createMatch(pv);
        match.addField(createBasicField(pv, ETH_TYPE, EthernetType.IPv4));
        match.addField(createBasicField(pv, IPV4_DST, IP_123));
        mod.match((Match) match.toImmutable());
        print(mod.toDebugString());
        int afterMatchLength = mod.length();
        print(FMT_MSG_LEN, afterMatchLength, "after match length");
        // adding a match should make no difference to the length
        // since, in 1.0, the match is a fixed length structure and already
        // accounted for.
        assertEquals(AM_NEQ, initLength, afterMatchLength);

        // verify we have the specified match:
        Match m = mod.getMatch();
        assertEquals(AM_UXS, 2, m.getMatchFields().size());

        // replace match with a new one..
        match = MatchFactory.createMatch(pv);
        // no fields - match anything
        mod.match((Match) match.toImmutable());
        print(mod.toDebugString());
        int afterMatchAnyLength = mod.length();
        print(FMT_MSG_LEN, afterMatchAnyLength, "after match-any length");
        // adding a match should make no difference to the length
        // since, in 1.0, the match is a fixed length structure and already
        // accounted for.
        assertEquals(AM_NEQ, initLength, afterMatchAnyLength);

        // verify we have the alternate match:
        m = mod.getMatch();
        assertEquals(AM_UXS, 0, m.getMatchFields().size());
    }

    @Test
    public void overwritingMatch13() {
        print(EOL + "overwritingMatch13()");
        final ProtocolVersion pv = V_1_3;

        mm = MessageFactory.create(pv, FLOW_MOD);
        OfmMutableFlowMod mod = (OfmMutableFlowMod) mm;
        print(mod.toDebugString());
        int initLength = mod.length();
        print(FMT_MSG_LEN, initLength, "initial length");
        // match starts as null - we'll need to set one
        assertNull(AM_HUH, mod.getMatch());

        MutableMatch match = MatchFactory.createMatch(pv);
        match.addField(createBasicField(pv, ETH_TYPE, EthernetType.IPv4));
        match.addField(createBasicField(pv, IPV4_DST, IP_123));
        mod.match((Match) match.toImmutable());
        print(mod.toDebugString());
        int afterMatchLength = mod.length();
        print(FMT_MSG_LEN, afterMatchLength, "after match length");
        // adding a match will increase the length of the message
        assertTrue(AM_HUH, afterMatchLength > initLength);

        // verify we have the specified match:
        Match m = mod.getMatch();
        assertEquals(AM_UXS, 2, m.getMatchFields().size());

        // replace match with a new one..
        match = MatchFactory.createMatch(pv);
        // no fields - match anything
        mod.match((Match) match.toImmutable());
        print(mod.toDebugString());
        int afterMatchAnyLength = mod.length();
        print(FMT_MSG_LEN, afterMatchAnyLength, "after match-any length");
        // adding this match will replace the previous, but still be a
        //  longer message length than no match..
        assertTrue(AM_HUH, afterMatchAnyLength < afterMatchLength);
        assertTrue(AM_HUH, afterMatchAnyLength > initLength);

        // verify we have the alternate match:
        m = mod.getMatch();
        assertEquals(AM_UXS, 0, m.getMatchFields().size());
    }
}
