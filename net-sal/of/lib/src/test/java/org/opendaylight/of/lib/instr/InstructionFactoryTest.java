/*
 * (c) Copyright 2012 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.of.lib.instr;

import org.junit.Test;
import org.opendaylight.of.lib.MessageParseException;
import org.opendaylight.of.lib.OfPacketReader;
import org.opendaylight.of.lib.OfPacketWriter;
import org.opendaylight.of.lib.ProtocolVersion;
import org.opendaylight.of.lib.dt.MeterId;
import org.opendaylight.of.lib.dt.TableId;
import org.opendaylight.of.lib.match.*;
import org.opendaylight.of.lib.msg.OfmTest;
import org.opendaylight.util.StringUtils;
import org.opendaylight.util.net.IpAddress;
import org.opendaylight.util.net.MacAddress;

import java.util.*;

import static org.junit.Assert.*;
import static org.opendaylight.of.lib.ExperimenterId.HP;
import static org.opendaylight.of.lib.ProtocolVersion.V_1_3;
import static org.opendaylight.of.lib.instr.InstructionFactory.*;
import static org.opendaylight.of.lib.instr.InstructionType.*;
import static org.opendaylight.util.junit.TestTools.*;

/**
 * Unit tests for parsing instructions with InstructionFactory.
 *
 * @author Simon Hunt
 */
public class InstructionFactoryTest extends OfmTest {
    private static final String E_PARSE_FAIL = "failed to parse instruction";
    private static final String FILE_PREFIX = "instr/v13/instr";

    private OfPacketReader pkt;
    private Instruction ins;
    private InstrMutableAction mutIns;
    private Action act;
    private MFieldBasic mf;


    private OfPacketReader getPkt(InstructionType type) {
        return getPkt(type, "");
    }

    private OfPacketReader getPkt(InstructionType type, String suffix) {
        String basename = StringUtils.toCamelCase(FILE_PREFIX, type);
        return getPacketReader(basename + suffix + HEX);
    }

    private void verify(Instruction ins, InstructionType expType, int expLen) {
        assertEquals(AM_NEQ, expType, ins.getInstructionType());
        assertEquals(AM_NEQ, expLen, ins.header.length); // no getter!
    }


    @Test
    public void gotoTable() {
        print(EOL + "gotoTable()");
        pkt = getPkt(GOTO_TABLE);
        try {
            ins = parseInstruction(pkt, V_1_3);
            print(ins.toDebugString());

            // see instrGotoTable.hex for expected values
            verify(ins, GOTO_TABLE, 8);

            assertTrue(AM_WRCL, ins instanceof InstrGotoTable);
            InstrGotoTable ii = (InstrGotoTable) ins;
            assertEquals(AM_NEQ, EXP_TABLE_ID, ii.getTableId());

        } catch (MessageParseException e) {
            print(e);
            fail(E_PARSE_FAIL);
        }
        checkEOBuffer(pkt);
    }

    private static final TableId EXP_TABLE_ID = TableId.valueOf(3);

    @Test
    public void writeMetadata() {
        print(EOL + "writeMetadata()");
        pkt = getPkt(WRITE_METADATA);
        try {
            ins = parseInstruction(pkt, V_1_3);
            print(ins.toDebugString());

            // see instrWriteMetadata.hex for expected values
            verify(ins, WRITE_METADATA, 24);

            assertTrue(AM_WRCL, ins instanceof InstrWriteMetadata);
            InstrWriteMetadata ii = (InstrWriteMetadata) ins;
            assertEquals(AM_NEQ, EXP_META_DATA, ii.getMetadata());
            assertEquals(AM_NEQ, EXP_META_MASK, ii.getMask());

        } catch (MessageParseException e) {
            print(e);
            fail(E_PARSE_FAIL);
        }
        checkEOBuffer(pkt);
    }

    private static final long EXP_META_DATA = 0x1234L;
    private static final long EXP_META_MASK = 0xffffL;

    private static final MacAddress MAC = mac("00001e:453411");
    private static final IpAddress IPv4 = ip("15.254.17.1");
    private static final IpAddress IPv6 = ip("fedc::8765:4321");

    @Test
    public void writeActions() {
        print(EOL + "writeActions()");
        pkt = getPkt(WRITE_ACTIONS);
        try {
            ins = parseInstruction(pkt, V_1_3);
            print(ins.toDebugString());

            // see instrWriteActions.hex for expected values
            verify(ins, WRITE_ACTIONS, 56);

            assertTrue(AM_WRCL, ins instanceof InstrWriteActions);
            InstrWriteActions ii = (InstrWriteActions) ins;
            Set<Action> acts = ii.getActionSet();
            assertEquals(AM_UXS, 3, acts.size());

            // assert information about the actions
            // (we are using a tree-set, so we know the order returned)
            Iterator<Action> actIter = acts.iterator();

            // First action should be DEC_NW_TTL
            Action a = actIter.next();
            assertTrue(AM_WRCL, a instanceof ActDecMplsTtl);

            // Second action should be SET_FIELD.ETH_DST
            a = actIter.next();
            assertTrue(AM_WRCL, a instanceof ActSetField);
            ActSetField asf = (ActSetField) a;
            MatchField mf = asf.getField();
            assertFalse(AM_HUH, mf.hasMask()); // mask should never be set
            assertTrue(AM_WRCL, mf instanceof MfbEthDst);
            MfbEthDst ethDst = (MfbEthDst) mf;
            assertEquals(AM_NEQ, MAC, ethDst.getMacAddress());

            // Third action should be SET_FIELD.IPV6_DST
            a = actIter.next();
            assertTrue(AM_WRCL, a instanceof ActSetField);
            asf = (ActSetField) a;
            mf = asf.getField();
            assertFalse(AM_HUH, mf.hasMask()); // mask should never be set
            assertTrue(AM_WRCL, mf instanceof MfbIpv6Dst);
            MfbIpv6Dst ipv6Dst = (MfbIpv6Dst) mf;
            assertEquals(AM_NEQ, IPv6, ipv6Dst.getIpAddress());

            assertFalse(AM_HUH, actIter.hasNext());

        } catch (MessageParseException e) {
            print(e);
            fail(E_PARSE_FAIL);
        }
        checkEOBuffer(pkt);
    }

    @Test
    public void applyActions() {
        print(EOL + "applyActions()");
        pkt = getPkt(APPLY_ACTIONS);
        try {
            ins = parseInstruction(pkt, V_1_3);
            print(ins.toDebugString());

            // see instrApplyActions.hex for expected values
            verify(ins, APPLY_ACTIONS, 48);

            assertTrue(AM_WRCL, ins instanceof InstrApplyActions);
            InstrApplyActions ii = (InstrApplyActions) ins;
            List<Action> acts = ii.getActionList();
            assertEquals(AM_UXS, 3, acts.size());

            // assert information about the actions
            Iterator<Action> actIter = acts.iterator();

            // First action should be DEC_NW_TTL
            Action a = actIter.next();
            assertTrue(AM_WRCL, a instanceof ActDecNwTtl);

            // Second action should be SET_FIELD.ETH_DST
            a = actIter.next();
            assertTrue(AM_WRCL, a instanceof ActSetField);
            ActSetField asf = (ActSetField) a;
            MatchField mf = asf.getField();
            assertFalse(AM_HUH, mf.hasMask()); // mask should never be set
            assertTrue(AM_WRCL, mf instanceof MfbEthDst);
            MfbEthDst ethDst = (MfbEthDst) mf;
            assertEquals(AM_NEQ, MAC, ethDst.getMacAddress());

            // Third action should be SET_FIELD.IPV4_DST
            a = actIter.next();
            assertTrue(AM_WRCL, a instanceof ActSetField);
            asf = (ActSetField) a;
            mf = asf.getField();
            assertFalse(AM_HUH, mf.hasMask()); // mask should never be set
            assertTrue(AM_WRCL, mf instanceof MfbIpv4Dst);
            MfbIpv4Dst ipv4Dst = (MfbIpv4Dst) mf;
            assertEquals(AM_NEQ, IPv4, ipv4Dst.getIpAddress());

            assertFalse(AM_HUH, actIter.hasNext());

        } catch (MessageParseException e) {
            print(e);
            fail(E_PARSE_FAIL);
        }
        checkEOBuffer(pkt);
    }

    @Test
    public void clearActions() {
        print(EOL + "clearActions()");
        pkt = getPkt(CLEAR_ACTIONS);
        try {
            ins = parseInstruction(pkt, V_1_3);
            print(ins.toDebugString());

            // see instrClearActions.hex for expected values
            verify(ins, CLEAR_ACTIONS, 8);

            assertTrue(AM_WRCL, ins instanceof InstrClearActions);

        } catch (MessageParseException e) {
            print(e);
            fail(E_PARSE_FAIL);
        }
        checkEOBuffer(pkt);
    }

    @Test
    public void meter() {
        print(EOL + "meter()");
        pkt = getPkt(METER);
        try {
            ins = parseInstruction(pkt, V_1_3);
            print(ins.toDebugString());

            // see instrMeter.hex for expected values
            verify(ins, METER, 8);

            assertTrue(AM_WRCL, ins instanceof InstrMeter);
            InstrMeter ii = (InstrMeter) ins;
            assertEquals(AM_NEQ, EXP_METER_ID, ii.getMeterId());

        } catch (MessageParseException e) {
            print(e);
            fail(E_PARSE_FAIL);
        }
    }

    private static final MeterId EXP_METER_ID = MeterId.valueOf(192);

    private static final byte[] EXP_EXP_DATA = {8, 7, 6, 5, 4, 3, 2, 1};

    @Test
    public void experimenter() {
        print(EOL + "experimenter()");
        pkt = getPkt(EXPERIMENTER);
        try {
            ins = parseInstruction(pkt, V_1_3);
            print(ins.toDebugString());

            // see instrExperimenter.hex for expected values
            verify(ins, EXPERIMENTER, 16);

            assertTrue(AM_WRCL, ins instanceof InstrExperimenter);
            InstrExperimenter ii = (InstrExperimenter) ins;
            assertArrayEquals(AM_NEQ, EXP_EXP_DATA, ii.getData());

        } catch (MessageParseException e) {
            print(e);
            fail(E_PARSE_FAIL);
        }
    }

    @Test
    public void experimenterBadLen() {
        print(EOL + "experimenterBadLen()");
        pkt = getPkt(EXPERIMENTER, "BadLen");
        try {
            ins = parseInstruction(pkt, V_1_3);
            print(ins.toDebugString());
            fail(AM_NOEX);

        } catch (MessageParseException e) {
            print(e);
        } catch (Exception e) {
            print(e);
            fail(AM_UNEX);
        }
    }


    // =====================================================================
    //  === TEST WRITE operations

    // ====== Supporting methods

    private byte[] getExpBytes(InstructionType type, String suffix) {
        String basename = StringUtils.toCamelCase(FILE_PREFIX, type);
        return getExpByteArray("../" + basename + suffix);
    }

    private byte[] getExpBytes(InstructionType type) {
        return getExpBytes(type, "");
    }

    private void run13BarBase(Instruction ins, String label, byte[] expData) {
        print(EOL + label + "()");
        print(ins);
        // encode the field into a buffer the same size
        OfPacketWriter pkt = new OfPacketWriter(expData.length);
        InstructionFactory.encodeInstruction(ins, pkt);
        // check that all is as expected
        verifyEncodement(label, expData, pkt);
    }

    private void run13Bar(Instruction ins, InstructionType type) {
        String label = StringUtils.toCamelCase("encode", type);
        byte[] expData = getExpBytes(type);
        run13BarBase(ins, label, expData);
    }

    // ====== Create / Encode Test methods

    @Test
    public void encodeGotoTable() {
        ins = createInstruction(V_1_3, GOTO_TABLE, EXP_TABLE_ID);
        run13Bar(ins, GOTO_TABLE);
    }

    @Test
    public void encodeWriteMetadata() {
        ins = createInstruction(V_1_3, WRITE_METADATA,
                EXP_META_DATA, EXP_META_MASK);
        run13Bar(ins, WRITE_METADATA);
    }

    @Test
    public void encodeWriteActions() {
        final ProtocolVersion pv = V_1_3;

        mutIns = createMutableInstruction(pv, WRITE_ACTIONS);

        act = ActionFactory.createAction(pv, ActionType.DEC_MPLS_TTL);
        mutIns.addAction(act);

        mf = FieldFactory.createBasicField(pv, OxmBasicFieldType.ETH_DST, MAC);
        act = ActionFactory.createAction(pv, ActionType.SET_FIELD, mf);
        mutIns.addAction(act);

        mf = FieldFactory.createBasicField(pv, OxmBasicFieldType.IPV6_DST, IPv6);
        act = ActionFactory.createAction(pv, ActionType.SET_FIELD, mf);
        mutIns.addAction(act);

        // now cast in stone
        ins = (Instruction) mutIns.toImmutable();
        // encode and verify...
        run13Bar(ins, WRITE_ACTIONS);
    }

    @Test
    public void encodeApplyActions() {
        final ProtocolVersion pv = V_1_3;

        mutIns = createMutableInstruction(pv, APPLY_ACTIONS);

        act = ActionFactory.createAction(pv, ActionType.DEC_NW_TTL);
        mutIns.addAction(act);

        mf = FieldFactory.createBasicField(pv, OxmBasicFieldType.ETH_DST, MAC);
        act = ActionFactory.createAction(pv, ActionType.SET_FIELD, mf);
        mutIns.addAction(act);

        mf = FieldFactory.createBasicField(pv, OxmBasicFieldType.IPV4_DST, IPv4);
        act = ActionFactory.createAction(pv, ActionType.SET_FIELD, mf);
        mutIns.addAction(act);

        // now cast in stone
        ins = (Instruction) mutIns.toImmutable();
        // encode and verify...
        run13Bar(ins, APPLY_ACTIONS);
    }

    @Test
    public void encodeClearActions() {
        final ProtocolVersion pv = V_1_3;
        ins = createInstruction(pv, CLEAR_ACTIONS);
        run13Bar(ins, CLEAR_ACTIONS);
    }

    @Test
    public void encodeMeter() {
        final ProtocolVersion pv = V_1_3;
        ins = createInstruction(pv, METER, EXP_METER_ID);
        run13Bar(ins, METER);
    }

    @Test
    public void encodeExperimenter() {
        final ProtocolVersion pv = V_1_3;
        ins = createInstruction(pv, EXPERIMENTER, HP, EXP_EXP_DATA);
        run13Bar(ins, EXPERIMENTER);
    }

    // ===

    @Test
    public void createHeaders() {
        print(EOL + "createHeaders()");
        final ProtocolVersion pv = V_1_3;
        Set<InstructionType> types = new HashSet<InstructionType>(
                Arrays.asList(APPLY_ACTIONS, WRITE_ACTIONS, GOTO_TABLE)
        );

        List<Instruction> ins = createInstructionHeaders(pv, types);
        for (Instruction i: ins)
            print(i.toDebugString());
        assertEquals(AM_UXS, 3, ins.size());
        for (Instruction i: ins) {
            assertTrue(AM_WRCL, InstrHeader.class.isInstance(i));
            assertEquals(AM_NEQ, 4, i.getTotalLength());
        }
    }
}
