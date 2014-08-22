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
import org.opendaylight.of.lib.dt.TableId;
import org.opendaylight.of.lib.instr.ActExperimenter;
import org.opendaylight.of.lib.instr.ActionType;
import org.opendaylight.of.lib.instr.InstrExperimenter;
import org.opendaylight.of.lib.instr.InstructionType;
import org.opendaylight.of.lib.match.MFieldExperimenter;
import org.opendaylight.of.lib.match.OxmBasicFieldType;

import java.util.*;

import static org.junit.Assert.*;
import static org.opendaylight.of.lib.ExperimenterId.BUDAPEST_U;
import static org.opendaylight.of.lib.instr.ActionFactory.createAction;
import static org.opendaylight.of.lib.instr.InstructionFactory.createInstruction;
import static org.opendaylight.of.lib.instr.InstructionType.*;
import static org.opendaylight.of.lib.match.FieldFactory.createExperimenterField;
import static org.opendaylight.of.lib.match.OxmBasicFieldType.*;
import static org.opendaylight.util.junit.TestTools.*;

/**
 * Unit tests for {@link TableFeatureFactory}.
 *
 * @author Simon Hunt
 */
public class TableFeatureFactoryTest extends OfmTest {

    private static final String TFEAT_INSTR = "tabFeatPropInstr";
    private static final String TFEAT_INSTR_MISS = "tabFeatPropInstrMiss";
    private static final String TFEAT_NEXT_TAB = "tabFeatPropNextTables";
    private static final String TFEAT_NEXT_TAB_MISS = "tabFeatPropNextTablesMiss";
    private static final String TFEAT_WRITE_ACTIONS = "tabFeatPropWriteActions";
    private static final String TFEAT_WRITE_ACTIONS_MISS = "tabFeatPropWriteActionsMiss";
    private static final String TFEAT_APPLY_ACTIONS = "tabFeatPropApplyActions";
    private static final String TFEAT_APPLY_ACTIONS_MISS = "tabFeatPropApplyActionsMiss";
    private static final String TFEAT_APPLY_ACTIONS_MISS_FAIL = "tabFeatPropApplyActionsMissFail";
    private static final String TFEAT_OXM_MATCH = "tabFeatPropOxmMatch";
    private static final String TFEAT_OXM_WILDCARDS = "tabFeatPropOxmWildcards";
    private static final String TFEAT_OXM_WRITE_SETFIELD = "tabFeatPropOxmWriteSetfield";
    private static final String TFEAT_OXM_WRITE_SETFIELD_MISS = "tabFeatPropOxmWriteSetfieldMiss";
    private static final String TFEAT_OXM_APPLY_SETFIELD = "tabFeatPropOxmApplySetfield";
    private static final String TFEAT_OXM_APPLY_SETFIELD_MISS = "tabFeatPropOxmApplySetfieldMiss";
    private static final String TFEAT_EXPER = "tabFeatPropExper";
    private static final String TFEAT_EXPER_MISS = "tabFeatPropExperMiss";

    private static final String E_PARSE_FAIL = "Failed to parse table property.";

    private static final Set<InstructionType> EXP_TYPES =
            new TreeSet<InstructionType>(
                    Arrays.asList(GOTO_TABLE, WRITE_METADATA, WRITE_ACTIONS,
                            APPLY_ACTIONS, CLEAR_ACTIONS, METER)
            );
    private static final Set<InstructionType> EXP_MISS_TYPES =
            new TreeSet<InstructionType>(
                    Arrays.asList(GOTO_TABLE, WRITE_METADATA, WRITE_ACTIONS,
                            APPLY_ACTIONS, CLEAR_ACTIONS)
            );

    private static final int B = 256;
    private static final byte[] EXP_EXPER_DATA = {
            0x0f, 0x00, 0x0b, 0xa2-B, 0xc0-B, 0x01, 0xc0-B, 0x01,
    };

    private static final ProtocolVersion PV = ProtocolVersion.V_1_3;

    // ====================================================================
    // === Parsing

    private OfPacketReader getPkt(String filename) {
        return getMsgPkt("struct/" + filename);
    }

    @Test
    public void tabFeatPropInstr() {
        print(EOL + "tabFeatPropInstr()");
        OfPacketReader pkt = getPkt(TFEAT_INSTR);
        try {
            TableFeaturePropInstr prop = (TableFeaturePropInstr)
                    TableFeatureFactory.parseProp(pkt, PV);
            print(prop);

            // See tabFeatPropInstr.hex for expected values
            assertEquals(AM_NEQ, EXP_TYPES, prop.getSupportedInstructions());
            List<InstrExperimenter> list = prop.getSupportedExperInstructions();
            assertEquals(AM_UXS, 1, list.size());
            InstrExperimenter expr = list.get(0);
            assertArrayEquals(AM_NEQ, EXP_EXPER_DATA, expr.getData());

        } catch (MessageParseException e) {
            print(e);
            fail(E_PARSE_FAIL);
        }
        checkEOBuffer(pkt);
    }

    @Test
    public void tabFeatPropInstrMiss() {
        print(EOL + "tabFeatPropInstrMiss()");
        OfPacketReader pkt = getPkt(TFEAT_INSTR_MISS);
        try {
            TableFeaturePropInstr prop = (TableFeaturePropInstr)
                    TableFeatureFactory.parseProp(pkt, PV);
            print(prop);

            // See tabFeatPropInstrMiss.hex for expected values
            assertEquals(AM_NEQ, EXP_MISS_TYPES, prop.getSupportedInstructions());
            assertEquals(AM_UXS, 0, prop.getSupportedExperInstructions().size());

        } catch (MessageParseException e) {
            print(e);
            fail(E_PARSE_FAIL);
        }
        checkEOBuffer(pkt);
    }

    @Test
    public void tabFeatPropNextTables() {
        print(EOL + "tabFeatPropNextTables()");
        OfPacketReader pkt = getPkt(TFEAT_NEXT_TAB);
        try {
            TableFeaturePropNextTable prop = (TableFeaturePropNextTable)
                    TableFeatureFactory.parseProp(pkt, PV);
            print(prop);

            // See tabFeatPropNextTables.hex for expected values
            Set<TableId> nt = prop.getNextTables();
            assertEquals(AM_UXS, 3, nt.size());
            assertTrue(AM_HUH, nt.contains(tid(2)));
            assertTrue(AM_HUH, nt.contains(tid(3)));
            assertTrue(AM_HUH, nt.contains(tid(5)));

        } catch (MessageParseException e) {
            print(e);
            fail(E_PARSE_FAIL);
        }
        checkEOBuffer(pkt);
    }

    @Test
    public void tabFeatPropNextTablesMiss() {
        print(EOL + "tabFeatPropNextTablesMiss()");
        OfPacketReader pkt = getPkt(TFEAT_NEXT_TAB_MISS);
        try {
            TableFeaturePropNextTable prop = (TableFeaturePropNextTable)
                    TableFeatureFactory.parseProp(pkt, PV);
            print(prop);

            // See tabFeatPropNextTablesMiss.hex for expected values
            Set<TableId> nt = prop.getNextTables();
            assertEquals(AM_UXS, 1, nt.size());
            assertTrue(AM_HUH, nt.contains(tid(9)));

        } catch (MessageParseException e) {
            print(e);
            fail(E_PARSE_FAIL);
        }
        checkEOBuffer(pkt);
    }

    @Test
    public void tabFeatPropWriteActions() {
        print(EOL + "tabFeatPropWriteActions()");
        OfPacketReader pkt = getPkt(TFEAT_WRITE_ACTIONS);
        try {
            TableFeaturePropAction prop = (TableFeaturePropAction)
                    TableFeatureFactory.parseProp(pkt, PV);
            print(prop);

            // See tabFeatPropWriteActions.hex for expected values
            Set<ActionType> at = prop.getSupportedActions();
            verifyFlags(at, ActionType.OUTPUT, ActionType.COPY_TTL_OUT,
                    ActionType.COPY_TTL_IN);
            assertEquals(AM_UXS, 0, prop.getSupportedExperActions().size());

        } catch (MessageParseException e) {
            print(e);
            fail(E_PARSE_FAIL);
        }
        checkEOBuffer(pkt);
    }

    @Test
    public void tabFeatPropWriteActionsMiss() {
        print(EOL + "tabFeatPropWriteActionsMiss()");
        OfPacketReader pkt = getPkt(TFEAT_WRITE_ACTIONS_MISS);
        try {
            TableFeaturePropAction prop = (TableFeaturePropAction)
                    TableFeatureFactory.parseProp(pkt, PV);
            print(prop);

            // See tabFeatPropWriteActionsMiss.hex for expected values
            Set<ActionType> at = prop.getSupportedActions();
            verifyFlags(at, ActionType.SET_MPLS_TTL, ActionType.DEC_MPLS_TTL);
            assertEquals(AM_UXS, 0, prop.getSupportedExperActions().size());

        } catch (MessageParseException e) {
            print(e);
            fail(E_PARSE_FAIL);
        }
        checkEOBuffer(pkt);
    }


    @Test
    public void tabFeatPropApplyActions() {
        print(EOL + "tabFeatPropApplyActions()");
        OfPacketReader pkt = getPkt(TFEAT_APPLY_ACTIONS);
        try {
            TableFeaturePropAction prop = (TableFeaturePropAction)
                    TableFeatureFactory.parseProp(pkt, PV);
            print(prop);

            // See tabFeatPropApplyActions.hex for expected values
            Set<ActionType> at = prop.getSupportedActions();
            verifyFlags(at, ActionType.PUSH_VLAN, ActionType.POP_VLAN);
            assertEquals(AM_UXS, 0, prop.getSupportedExperActions().size());

        } catch (MessageParseException e) {
            print(e);
            fail(E_PARSE_FAIL);
        }
        checkEOBuffer(pkt);
    }

    @Test
    public void tabFeatPropApplyActionsMissFail() {
        print(EOL + "tabFeatPropApplyActionsMissFail()");
        OfPacketReader pkt = getPkt(TFEAT_APPLY_ACTIONS_MISS_FAIL);
        try {
            TableFeaturePropAction prop = (TableFeaturePropAction)
                    TableFeatureFactory.parseProp(pkt, PV);
            print(prop);
            fail("Expected IllegalArgumentException for data length not % 8");
        } catch (MessageParseException e) {
            print(FMT_EX, e);
        }
    }

    @Test
    public void tabFeatPropApplyActionsMiss() {
        print(EOL + "tabFeatPropApplyActionsMiss()");
        OfPacketReader pkt = getPkt(TFEAT_APPLY_ACTIONS_MISS);
        try {
            TableFeaturePropAction prop = (TableFeaturePropAction)
                    TableFeatureFactory.parseProp(pkt, PV);
            print(prop);

            // See tabFeatPropApplyActionsMiss.hex for expected values
            Set<ActionType> at = prop.getSupportedActions();
            verifyFlags(at, ActionType.SET_QUEUE, ActionType.GROUP);
            List<ActExperimenter> aelist = prop.getSupportedExperActions();
            assertEquals(AM_UXS, 1, aelist.size());
            ActExperimenter ae = aelist.get(0);
            assertEquals(AM_NEQ, ExperimenterId.HP.encodedId(), ae.getId());
            assertArrayEquals(AM_NEQ, EXP_EXPER_DATA, ae.getData());

        } catch (MessageParseException e) {
            print(e);
            fail(E_PARSE_FAIL);
        }
        checkEOBuffer(pkt);
    }

    @Test
    public void tabFeatPropOxmMatch() {
        print(EOL + "tabFeatPropOxmMatch()");
        OfPacketReader pkt = getPkt(TFEAT_OXM_MATCH);
        try {
            TableFeaturePropOxm prop = (TableFeaturePropOxm)
                    TableFeatureFactory.parseProp(pkt, PV);
            print(prop);

            // See tabFeatPropOxmMatch.hex for expected values
            Set<OxmBasicFieldType> fts = prop.getSupportedFieldTypes();
            verifyFlags(fts, IN_PORT, IPV4_SRC, IPV4_DST, ETH_TYPE);
            assertFalse(AM_HUH, prop.hasMaskBitSet(IN_PORT));
            assertTrue(AM_HUH, prop.hasMaskBitSet(IPV4_SRC));
            assertTrue(AM_HUH, prop.hasMaskBitSet(IPV4_DST));
            assertFalse(AM_HUH, prop.hasMaskBitSet(ETH_TYPE));
            List<MFieldExperimenter> elist = prop.getSupportedExperFields();
            assertEquals(AM_UXS, 1, elist.size());
            MFieldExperimenter e = elist.get(0);
            assertEquals(AM_NEQ, 23, e.getRawFieldType());
            assertNull(AM_HUH, e.getPayload());
            assertEquals(AM_NEQ, ExperimenterId.HP.encodedId(), e.getId());

        } catch (MessageParseException e) {
            print(e);
            fail(E_PARSE_FAIL);
        }
        checkEOBuffer(pkt);
    }

    @Test
    public void tabFeatPropOxmWildcards() {
        print(EOL + "tabFeatPropOxmWildcards()");
        OfPacketReader pkt = getPkt(TFEAT_OXM_WILDCARDS);
        try {
            TableFeaturePropOxm prop = (TableFeaturePropOxm)
                    TableFeatureFactory.parseProp(pkt, PV);
            print(prop);

            // See tabFeatPropOxmWildcards.hex for expected values
            Set<OxmBasicFieldType> fts = prop.getSupportedFieldTypes();
            verifyFlags(fts,
                    ETH_SRC, ETH_DST
            );
            assertTrue(AM_HUH, prop.hasMaskBitSet(ETH_SRC));
            assertTrue(AM_HUH, prop.hasMaskBitSet(ETH_DST));
            assertEquals(AM_UXS, 0, prop.getSupportedExperFields().size());

        } catch (MessageParseException e) {
            print(e);
            fail(E_PARSE_FAIL);
        }
        checkEOBuffer(pkt);
    }

    @Test
    public void tabFeatPropOxmWriteSetfield() {
        print(EOL + "tabFeatPropOxmWriteSetfield()");
        OfPacketReader pkt = getPkt(TFEAT_OXM_WRITE_SETFIELD);
        try {
            TableFeaturePropOxm prop = (TableFeaturePropOxm)
                    TableFeatureFactory.parseProp(pkt, PV);
            print(prop);

            // See tabFeatPropOxmWriteSetfield.hex for expected values
            Set<OxmBasicFieldType> fts = prop.getSupportedFieldTypes();
            verifyFlags(fts, ETH_SRC, ETH_DST);
            assertFalse(AM_HUH, prop.hasMaskBitSet(ETH_SRC));
            assertFalse(AM_HUH, prop.hasMaskBitSet(ETH_DST));
            assertEquals(AM_UXS, 0, prop.getSupportedExperFields().size());

        } catch (MessageParseException e) {
            print(e);
            fail(E_PARSE_FAIL);
        }
        checkEOBuffer(pkt);
    }

    @Test
    public void tabFeatPropOxmWriteSetfieldMiss() {
        print(EOL + "tabFeatPropOxmWriteSetfieldMiss()");
        OfPacketReader pkt = getPkt(TFEAT_OXM_WRITE_SETFIELD_MISS);
        try {
            TableFeaturePropOxm prop = (TableFeaturePropOxm)
                    TableFeatureFactory.parseProp(pkt, PV);
            print(prop);

            // See tabFeatPropOxmWriteSetfieldMiss.hex for expected values
            Set<OxmBasicFieldType> fts = prop.getSupportedFieldTypes();
            verifyFlags(fts, ETH_SRC, ETH_DST);
            assertFalse(AM_HUH, prop.hasMaskBitSet(ETH_SRC));
            assertFalse(AM_HUH, prop.hasMaskBitSet(ETH_DST));
            assertEquals(AM_UXS, 0, prop.getSupportedExperFields().size());

        } catch (MessageParseException e) {
            print(e);
            fail(E_PARSE_FAIL);
        }
        checkEOBuffer(pkt);
    }

    @Test
    public void tabFeatPropOxmApplySetfield() {
        print(EOL + "tabFeatPropOxmApplySetfield()");
        OfPacketReader pkt = getPkt(TFEAT_OXM_APPLY_SETFIELD);
        try {
            TableFeaturePropOxm prop = (TableFeaturePropOxm)
                    TableFeatureFactory.parseProp(pkt, PV);
            print(prop);

            // See tabFeatPropOxmApplySetfield.hex for expected values
            Set<OxmBasicFieldType> fts = prop.getSupportedFieldTypes();
            verifyFlags(fts, IPV4_SRC, IPV4_DST);
            assertFalse(AM_HUH, prop.hasMaskBitSet(IPV4_SRC));
            assertFalse(AM_HUH, prop.hasMaskBitSet(IPV4_DST));
            assertEquals(AM_UXS, 0, prop.getSupportedExperFields().size());

        } catch (MessageParseException e) {
            print(e);
            fail(E_PARSE_FAIL);
        }
        checkEOBuffer(pkt);
    }

    @Test
    public void tabFeatPropOxmApplySetfieldMiss() {
        print(EOL + "tabFeatPropOxmApplySetfieldMiss()");
        OfPacketReader pkt = getPkt(TFEAT_OXM_APPLY_SETFIELD_MISS);
        try {
            TableFeaturePropOxm prop = (TableFeaturePropOxm)
                    TableFeatureFactory.parseProp(pkt, PV);
            print(prop);

            // See tabFeatPropOxmApplySetfieldMiss.hex for expected values
            Set<OxmBasicFieldType> fts = prop.getSupportedFieldTypes();
            verifyFlags(fts, IPV6_SRC, IPV6_DST);
            assertFalse(AM_HUH, prop.hasMaskBitSet(IPV6_SRC));
            assertFalse(AM_HUH, prop.hasMaskBitSet(IPV6_DST));
            assertEquals(AM_UXS, 0, prop.getSupportedExperFields().size());

        } catch (MessageParseException e) {
            print(e);
            fail(E_PARSE_FAIL);
        }
        checkEOBuffer(pkt);
    }

    private static final byte[] HP_EXPER_DATA = {2, 2, 4, 4, 6, 6, 8, 8};
    private static final byte[] BS_EXPER_DATA = {2, 2, 4, 4, 6, 6};

    @Test
    public void tabFeatPropExper() {
        print(EOL + "tabFeatPropExper()");
        OfPacketReader pkt = getPkt(TFEAT_EXPER);
        try {
            TableFeaturePropExper prop = (TableFeaturePropExper)
                    TableFeatureFactory.parseProp(pkt, PV);
            print(prop);

            // See tabFeatPropExper.hex for expected values
            assertEquals(AM_NEQ, ExperimenterId.HP, prop.getExpId());
            assertEquals(AM_NEQ, 42, prop.getExpType());
            assertArrayEquals(AM_NEQ, HP_EXPER_DATA, prop.getData());

        } catch (MessageParseException e) {
            print(e);
            fail(E_PARSE_FAIL);
        }
        checkEOBuffer(pkt);
    }

    @Test
    public void tabFeatPropExperMiss() {
        print(EOL + "tabFeatPropExperMiss()");
        OfPacketReader pkt = getPkt(TFEAT_EXPER_MISS);
        try {
            TableFeaturePropExper prop = (TableFeaturePropExper)
                    TableFeatureFactory.parseProp(pkt, PV);
            print(prop);

            // See tabFeatPropExperMiss.hex for expected values
            assertEquals(AM_NEQ, ExperimenterId.BIG_SWITCH, prop.getExpId());
            assertEquals(AM_NEQ, EXP_BS_FT, prop.getExpType());
            assertArrayEquals(AM_NEQ, BS_EXPER_DATA, prop.getData());

        } catch (MessageParseException e) {
            print(e);
            fail(E_PARSE_FAIL);
        }
        checkEOBuffer(pkt);
    }


    // ====================================================================
    // === Creating

    private static final InstrExperimenter IEXP = (InstrExperimenter)
            createInstruction(PV, EXPERIMENTER, ExperimenterId.HP,
                    EXP_EXPER_DATA);
    private static final List<InstrExperimenter> IEXP_LIST =
            new ArrayList<InstrExperimenter>(Arrays.asList(IEXP));

    private static final ActExperimenter AEXP = (ActExperimenter)
            createAction(PV, ActionType.EXPERIMENTER, ExperimenterId.HP,
                    EXP_EXPER_DATA);
    private static final List<ActExperimenter> AEXP_LIST =
            new ArrayList<ActExperimenter>(Arrays.asList(AEXP));

    @Test
    public void createInstructionsProp() {
        print(EOL + "createInstructionsProp()");
        Set<InstructionType> ins = new HashSet<InstructionType>(
                Arrays.asList(GOTO_TABLE, WRITE_ACTIONS)
        );
        TableFeaturePropInstr p = TableFeatureFactory.createInstrProp(PV,
                TableFeaturePropType.INSTRUCTIONS, ins);
        print(p);
        Set<InstructionType> supported = p.getSupportedInstructions();
        verifyFlags(supported, GOTO_TABLE, WRITE_ACTIONS);
        assertNull(AM_HUH, p.getSupportedExperInstructions());
        // length in bytes should be 4 for header, 4 each for types = 12
        assertEquals(AM_NEQ, 12, p.getTotalLength());
    }

    @Test
    public void createInstructionsPropWithExperimenter() {
        print(EOL + "createInstructionsPropWithExperimenter()");
        Set<InstructionType> ins = new HashSet<InstructionType>(
                Arrays.asList(APPLY_ACTIONS)
        );

        TableFeaturePropInstr p = TableFeatureFactory.createInstrProp(PV,
                TableFeaturePropType.INSTRUCTIONS, ins, IEXP_LIST);
        print(p);
        verifyFlags(p.getSupportedInstructions(), APPLY_ACTIONS);
        List<InstrExperimenter> ie = p.getSupportedExperInstructions();
        assertEquals(AM_UXS, 1, ie.size());
        assertEquals(AM_NEQ, IEXP, ie.get(0));
        // length in bytes should be 4 for header, 4 for type, 16 for exp = 24
        assertEquals(AM_NEQ, 24, p.getTotalLength());
    }

    @Test
    public void createNextTablesProp() {
        print(EOL + "createNextTablesProp()");
        Set<TableId> tids = new HashSet<TableId>(
                Arrays.asList(tid(2), tid(3), tid(5))
        );
        TableFeaturePropNextTable p =
                TableFeatureFactory.createNextTablesProp(PV,
                        TableFeaturePropType.NEXT_TABLES, tids);
        print(p);
        Set<TableId> ti = p.getNextTables();
        assertEquals(AM_UXS, 3, ti.size());
        assertTrue(AM_HUH, ti.contains(tid(2)));
        assertTrue(AM_HUH, ti.contains(tid(3)));
        assertTrue(AM_HUH, ti.contains(tid(5)));
        // length in bytes: header: 4, +1 byte for each table id: 3 ... total 7
        assertEquals(AM_NEQ, 7, p.getTotalLength());
    }

    @Test
    public void createActionsProp() {
        print(EOL + "createActionsProp()");
        Set<ActionType> acts = new HashSet<ActionType>(
                Arrays.asList(ActionType.OUTPUT, ActionType.COPY_TTL_OUT,
                        ActionType.COPY_TTL_IN)
        );
        TableFeaturePropAction p = TableFeatureFactory.createActionProp(PV,
                TableFeaturePropType.WRITE_ACTIONS, acts);
        print(p);
        Set<ActionType> a = p.getSupportedActions();
        verifyFlags(a, ActionType.OUTPUT, ActionType.COPY_TTL_OUT,
                ActionType.COPY_TTL_IN);
        assertNull(AM_HUH, p.getSupportedExperActions());
        // header:4, +4 bytes for each action type: 12 ... total 16
        assertEquals(AM_NEQ, 16, p.getTotalLength());
    }

    @Test
    public void createActionsPropWithExperimenter() {
        print(EOL + "createActionsPropWithExperimenter()");
        Set<ActionType> acts = new HashSet<ActionType>(
                Arrays.asList(ActionType.SET_QUEUE, ActionType.GROUP)
        );
        TableFeaturePropAction p = TableFeatureFactory.createActionProp(PV,
                TableFeaturePropType.WRITE_ACTIONS, acts, AEXP_LIST);
        print(p);
        Set<ActionType> a = p.getSupportedActions();
        verifyFlags(a, ActionType.SET_QUEUE, ActionType.GROUP);
        List<ActExperimenter> aexp = p.getSupportedExperActions();
        assertEquals(AM_UXS, 1, aexp.size());
        ActExperimenter ae = aexp.get(0);
        assertEquals(AM_NEQ, ExperimenterId.HP, ae.getExpId());
        assertArrayEquals(AM_NEQ, EXP_EXPER_DATA, ae.getData());
        // header:4, +4 bytes for each action type: 8,
        //   16 for exper ... total 28
        assertEquals(AM_NEQ, 28, p.getTotalLength());
    }

    private static final int EXP_FT = 42;
    private static final int EXP_BS_FT = 7;
    private static final byte[] EXP_DATA = {4, 5, 6, 7};

    @Test
    public void createOxmProp() {
        print(EOL + "createOxmProp()");
        // first create the basic field map:
        Map<OxmBasicFieldType, Boolean> fields =
                new HashMap<OxmBasicFieldType, Boolean>();
        fields.put(ETH_SRC, false);
        fields.put(ETH_DST, false);
        fields.put(IPV4_SRC, true);
        fields.put(IPV4_DST, true);
        List<MFieldExperimenter> expFields = new ArrayList<MFieldExperimenter>();
        expFields.add(createExperimenterField(PV, EXP_FT, BUDAPEST_U, EXP_DATA));

        TableFeaturePropOxm p = TableFeatureFactory.createOxmProp(PV,
                TableFeaturePropType.MATCH, fields, expFields);
        print(p.toDebugString(0));

        Set<OxmBasicFieldType> suppFts = p.getSupportedFieldTypes();
        assertEquals(AM_NEQ, TableFeaturePropType.MATCH, p.getType());
        assertEquals(AM_UXS, 4, suppFts.size());
        verifyFlags(suppFts, ETH_SRC, ETH_DST, IPV4_SRC, IPV4_DST);
        assertFalse(p.hasMaskBitSet(ETH_SRC));
        assertFalse(p.hasMaskBitSet(ETH_DST));
        assertTrue(p.hasMaskBitSet(IPV4_SRC));
        assertTrue(p.hasMaskBitSet(IPV4_DST));

        List<MFieldExperimenter> ef = p.getSupportedExperFields();
        assertEquals(AM_UXS, 1, ef.size());
        MFieldExperimenter exp = ef.get(0);
        assertEquals(AM_NEQ, EXP_FT, exp.getRawFieldType());
        assertEquals(AM_NEQ, BUDAPEST_U, exp.getExpId());
        assertArrayEquals(AM_NEQ, EXP_DATA, exp.getPayload());

        // header:4, +4 bytes for each basic field header: 20,
        //   12 for exper field (4+4+4) ... total 32
        assertEquals(AM_NEQ, 32, p.getTotalLength());
    }

    private static final long EXP_DEF_TYPE = 98765;
    // apparently, data doesn't have to be multiple of 8 , because we
    // add padding.. (see spec)
    private static final byte[] EXP_PROP_DATA = {9,8,7,6,5};

    @Test
    public void createExperProp() {
        print(EOL + "createExperProp()");
        TableFeaturePropExper p = TableFeatureFactory.createExperProp(PV,
                TableFeaturePropType.EXPERIMENTER,
                ExperimenterId.HP, EXP_DEF_TYPE, EXP_PROP_DATA);
        print(p.toDebugString(0));

        assertEquals(AM_NEQ, ExperimenterId.HP, p.getExpId());
        assertEquals(AM_NEQ, EXP_DEF_TYPE, p.getExpType());
        assertArrayEquals(AM_NEQ, EXP_PROP_DATA, p.getData());
    }


    // ====================================================================
    // === Encoding

    private byte[] getExpBytes(String fname) {
        return getExpByteArray("struct/" + fname);
    }

    private void verifyPropEncoding(TableFeatureProp prop, String fname) {
        byte[] expData = getExpBytes(fname);
        // encode the property into a buffer the same size as the expected data
        OfPacketWriter pkt = new OfPacketWriter(expData.length);
        TableFeatureFactory.encodeProp(PV, prop, pkt);
        // check that all is as expected
        verifyEncodement(fname, expData, pkt);
    }

    @Test
    public void encodeTabFeatPropInstr() {
        print(EOL + "encodeTabFeatPropInstr()");
        Set<InstructionType> ins = new HashSet<InstructionType>(
                Arrays.asList(GOTO_TABLE, WRITE_METADATA, WRITE_ACTIONS,
                        APPLY_ACTIONS, CLEAR_ACTIONS, METER)
        );

        TableFeaturePropInstr prop = TableFeatureFactory.createInstrProp(PV,
                TableFeaturePropType.INSTRUCTIONS, ins, IEXP_LIST);
        print(prop);

        verifyPropEncoding(prop, TFEAT_INSTR);
    }

    @Test
    public void encodeTabFeatPropInstrMiss() {
        print(EOL + "encodeTabFeatPropInstrMiss()");
        Set<InstructionType> ins = new HashSet<InstructionType>(
                Arrays.asList(GOTO_TABLE, WRITE_METADATA, WRITE_ACTIONS,
                        APPLY_ACTIONS, CLEAR_ACTIONS)
        );

        TableFeaturePropInstr prop = TableFeatureFactory.createInstrProp(PV,
                TableFeaturePropType.INSTRUCTIONS_MISS, ins);
        print(prop);

        verifyPropEncoding(prop, TFEAT_INSTR_MISS);
    }

    @Test
    public void encodeTabFeatPropNextTables() {
        print(EOL + "encodeTabFeatPropNextTables()");
        Set<TableId> tableIds = new HashSet<TableId>(
                Arrays.asList(tid(2), tid(3), tid(5))
        );
        TableFeaturePropNextTable prop =
                TableFeatureFactory.createNextTablesProp(PV,
                        TableFeaturePropType.NEXT_TABLES, tableIds);
        print(prop);
        verifyPropEncoding(prop, TFEAT_NEXT_TAB);
    }

    @Test
    public void encodeTabFeatPropNextTablesMiss() {
        print(EOL + "encodeTabFeatPropNextTablesMiss()");
        Set<TableId> tableIds = new HashSet<TableId>(Arrays.asList(tid(9)));
        TableFeaturePropNextTable prop =
                TableFeatureFactory.createNextTablesProp(PV,
                        TableFeaturePropType.NEXT_TABLES_MISS, tableIds);
        print(prop);
        verifyPropEncoding(prop, TFEAT_NEXT_TAB_MISS);
    }

    @Test
    public void encodeTabFeatPropWriteActions() {
        print(EOL + "encodeTabFeatPropWriteActions()");
        Set<ActionType> actTypes = new HashSet<ActionType>(Arrays.asList(
                ActionType.OUTPUT,
                ActionType.COPY_TTL_OUT,
                ActionType.COPY_TTL_IN
        ));
        TableFeaturePropAction prop = TableFeatureFactory.createActionProp(PV,
                TableFeaturePropType.WRITE_ACTIONS, actTypes);
        print(prop);
        verifyPropEncoding(prop, TFEAT_WRITE_ACTIONS);
    }

    @Test
    public void encodeTabFeatPropWriteActionsMiss() {
        print(EOL + "encodeTabFeatPropWriteActionsMiss()");
        Set<ActionType> actTypes = new HashSet<ActionType>(Arrays.asList(
                ActionType.SET_MPLS_TTL,
                ActionType.DEC_MPLS_TTL
        ));
        TableFeaturePropAction prop = TableFeatureFactory.createActionProp(PV,
                TableFeaturePropType.WRITE_ACTIONS_MISS, actTypes);
        print(prop);
        verifyPropEncoding(prop, TFEAT_WRITE_ACTIONS_MISS);
    }

    @Test
    public void encodeTabFeatPropApplyActions() {
        print(EOL + "encodeTabFeatPropApplyActions()");
        Set<ActionType> actTypes = new HashSet<ActionType>(Arrays.asList(
                ActionType.PUSH_VLAN,
                ActionType.POP_VLAN
        ));
        TableFeaturePropAction prop = TableFeatureFactory.createActionProp(PV,
                TableFeaturePropType.APPLY_ACTIONS, actTypes);
        print(prop);
        verifyPropEncoding(prop, TFEAT_APPLY_ACTIONS);
    }

    @Test
    public void encodeTabFeatPropApplyActionsMiss() {
        print(EOL + "encodeTabFeatPropApplyActionsMiss()");
        Set<ActionType> actTypes = new HashSet<ActionType>(Arrays.asList(
                ActionType.SET_QUEUE,
                ActionType.GROUP
        ));
        List<ActExperimenter> expActs = new ArrayList<ActExperimenter>();
        expActs.add((ActExperimenter) createAction(PV, ActionType.EXPERIMENTER,
                ExperimenterId.HP, EXP_EXPER_DATA));

        TableFeaturePropAction prop = TableFeatureFactory.createActionProp(PV,
                TableFeaturePropType.APPLY_ACTIONS_MISS, actTypes, expActs);
        print(prop);
        verifyPropEncoding(prop, TFEAT_APPLY_ACTIONS_MISS);
    }

    @Test
    public void encodeTabFeatPropOxmMatch() {
        print(EOL + "encodeTabFeatPropOxmMatch()");
        Map<OxmBasicFieldType, Boolean> fields =
                new TreeMap<OxmBasicFieldType, Boolean>();
        // Doesn't matter the order we add, because it's a tree map
        fields.put(ETH_TYPE, false);
        fields.put(IPV4_DST, true);
        fields.put(IPV4_SRC, true);
        fields.put(IN_PORT, false);

        List<MFieldExperimenter> expFields = new ArrayList<MFieldExperimenter>();
        expFields.add(createExperimenterField(PV, 23, ExperimenterId.HP, null));

        TableFeaturePropOxm prop = TableFeatureFactory.createOxmProp(PV,
                TableFeaturePropType.MATCH, fields, expFields);
        print(prop.toDebugString(0));
        verifyPropEncoding(prop, TFEAT_OXM_MATCH);
    }

    @Test
    public void encodeTabFeatPropOxmWildcards() {
        print(EOL + "encodeTabFeatPropOxmWildcards()");
        Map<OxmBasicFieldType, Boolean> fields =
                new TreeMap<OxmBasicFieldType, Boolean>();
        fields.put(ETH_SRC, true);
        fields.put(ETH_DST, true);

        TableFeaturePropOxm prop = TableFeatureFactory.createOxmProp(PV,
                TableFeaturePropType.WILDCARDS, fields);
        print(prop.toDebugString(0));
        verifyPropEncoding(prop, TFEAT_OXM_WILDCARDS);
    }

    @Test
    public void encodeTabFeatPropWriteSetfield() {
        print(EOL + "encodeTabFeatPropWriteSetfield()");
        Map<OxmBasicFieldType, Boolean> fields =
                new TreeMap<OxmBasicFieldType, Boolean>();
        fields.put(ETH_SRC, false);
        fields.put(ETH_DST, false);

        TableFeaturePropOxm prop = TableFeatureFactory.createOxmProp(PV,
                TableFeaturePropType.WRITE_SETFIELD, fields);
        print(prop.toDebugString(0));
        verifyPropEncoding(prop, TFEAT_OXM_WRITE_SETFIELD);
    }

    @Test
    public void encodeTabFeatPropWriteSetfieldMiss() {
        print(EOL + "encodeTabFeatPropWriteSetfieldMiss()");
        Map<OxmBasicFieldType, Boolean> fields =
                new TreeMap<OxmBasicFieldType, Boolean>();
        fields.put(ETH_SRC, false);
        fields.put(ETH_DST, false);

        TableFeaturePropOxm prop = TableFeatureFactory.createOxmProp(PV,
                TableFeaturePropType.WRITE_SETFIELD_MISS, fields);
        print(prop.toDebugString(0));
        verifyPropEncoding(prop, TFEAT_OXM_WRITE_SETFIELD_MISS);
    }

    @Test
    public void encodeTabFeatPropApplySetfield() {
        print(EOL + "encodeTabFeatPropApplySetfield()");
        Map<OxmBasicFieldType, Boolean> fields =
                new TreeMap<OxmBasicFieldType, Boolean>();
        fields.put(IPV4_SRC, false);
        fields.put(IPV4_DST, false);

        TableFeaturePropOxm prop = TableFeatureFactory.createOxmProp(PV,
                TableFeaturePropType.APPLY_SETFIELD, fields);
        print(prop.toDebugString(0));
        verifyPropEncoding(prop, TFEAT_OXM_APPLY_SETFIELD);
    }

    @Test
    public void encodeTabFeatPropApplySetfieldMiss() {
        print(EOL + "encodeTabFeatPropApplySetfieldMiss()");
        Map<OxmBasicFieldType, Boolean> fields =
                new TreeMap<OxmBasicFieldType, Boolean>();
        fields.put(IPV6_SRC, false);
        fields.put(IPV6_DST, false);

        TableFeaturePropOxm prop = TableFeatureFactory.createOxmProp(PV,
                TableFeaturePropType.APPLY_SETFIELD_MISS, fields);
        print(prop.toDebugString(0));
        verifyPropEncoding(prop, TFEAT_OXM_APPLY_SETFIELD_MISS);
    }

    @Test
    public void encodeTabFeatPropExper() {
        print(EOL + "encodeTabFeatPropExper()");

        TableFeaturePropExper prop = TableFeatureFactory.createExperProp(PV,
                TableFeaturePropType.EXPERIMENTER, ExperimenterId.HP, EXP_FT,
                HP_EXPER_DATA);
        print(prop.toDebugString(0));
        verifyPropEncoding(prop, TFEAT_EXPER);
    }

    @Test
    public void encodeTabFeatPropExperMiss() {
        print(EOL + "encodeTabFeatPropExperMiss()");

        TableFeaturePropExper prop = TableFeatureFactory.createExperProp(PV,
                TableFeaturePropType.EXPERIMENTER_MISS, ExperimenterId.BIG_SWITCH,
                EXP_BS_FT, BS_EXPER_DATA);
        print(prop.toDebugString(0));
        verifyPropEncoding(prop, TFEAT_EXPER_MISS);
    }

}
