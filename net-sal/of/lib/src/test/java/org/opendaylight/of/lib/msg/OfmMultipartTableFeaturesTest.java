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
import org.opendaylight.of.lib.mp.MBodyMutableTableFeatures;
import org.opendaylight.of.lib.mp.MBodyTableFeatures;
import org.opendaylight.of.lib.mp.MpBodyFactory;
import org.opendaylight.of.lib.mp.MultipartBody;

import java.util.*;

import static org.junit.Assert.*;
import static org.opendaylight.of.lib.ExperimenterId.BIG_SWITCH;
import static org.opendaylight.of.lib.ExperimenterId.HP;
import static org.opendaylight.of.lib.ProtocolVersion.V_1_3;
import static org.opendaylight.of.lib.instr.ActionFactory.createAction;
import static org.opendaylight.of.lib.instr.InstructionFactory.createInstruction;
import static org.opendaylight.of.lib.instr.InstructionType.GOTO_TABLE;
import static org.opendaylight.of.lib.instr.InstructionType.WRITE_METADATA;
import static org.opendaylight.of.lib.match.FieldFactory.createExperimenterField;
import static org.opendaylight.of.lib.mp.MultipartType.TABLE_FEATURES;
import static org.opendaylight.of.lib.msg.MessageType.MULTIPART_REPLY;
import static org.opendaylight.of.lib.msg.MessageType.MULTIPART_REQUEST;
import static org.opendaylight.of.lib.msg.TableFeatureFactory.*;
import static org.opendaylight.of.lib.msg.TableFeaturePropType.*;
import static org.opendaylight.util.junit.TestTools.*;

/**
 * Unit tests for OfmMultipartRequest and OfmMultipartReply messages with the
 * type MultipartType.TABLE_FEATURES.
 *
 * @author Simon Hunt
 */
public class OfmMultipartTableFeaturesTest extends OfmMultipartTest {

    // test files
    private static final String TF_REQ_13_NOBODY =
            "v13/mpRequestTableFeaturesNoBody";
    private static final String TF_REQ_13_WBODY =
            "v13/mpRequestTableFeaturesWithBody";
    private static final String TF_REP_13 = "v13/mpReplyTableFeatures";
    private static final String TF_REP_13_TWICE = "v13/mpReplyTableFeaturesTwice";

    // some expected values
    private static final int B = 256;
    private static final byte[] EXP_EXPER_DATA_ONE = {
            0x0f, 0x00, 0x0b, 0xa2-B, 0xc0-B, 0x01, 0xc0-B, 0x01,
    };
    private static final byte[] EXP_EXPER_DATA = {
            0x0f, 0x00, 0x0b, 0xa2-B, 0xc0-B, 0x01, 0xc0-B, 0x01,
    };
    private static final long FULL_META = 0xffffffffffffffffL;
    private static final long HALF_META = 0xffffffff00000000L;


    // ========================================================= PARSING ====

    @Test
    public void mpRequestTableFeatures13NoBody() {
        print(EOL + "mpRequestTableFeatures13NoBody()");
        OfmMultipartRequest msg = (OfmMultipartRequest)
                verifyMsgHeader(TF_REQ_13_NOBODY, V_1_3, MULTIPART_REQUEST, 16);
        MultipartBody body = msg.getBody();
        assertTrue(AM_WRCL, body instanceof MBodyTableFeatures.Array);
        MBodyTableFeatures.Array array = (MBodyTableFeatures.Array) body;
        assertEquals(AM_UXS, 0, array.getList().size());
    }


    private void verifyTfFixed(MBodyTableFeatures tf, int expTid,
                               String expName, long expMetaMatch,
                               long expMetaWrite, long expMaxEnt) {
        assertEquals(AM_NEQ, tid(expTid), tf.getTableId());
        assertEquals(AM_NEQ, expName, tf.getName());
        assertEquals(AM_NEQ, expMetaMatch, tf.getMetadataMatch());
        assertEquals(AM_NEQ, expMetaWrite, tf.getMetadataWrite());
        assertEquals(AM_UXS, 0, tf.getConfig().size());
        assertEquals(AM_NEQ, expMaxEnt, tf.getMaxEntries());
    }

    @Test
    public void mpRequestTableFeatures13WithBody() {
        print(EOL + "mpRequestTableFeatures13WithBody()");
        OfmMultipartRequest msg = (OfmMultipartRequest)
                verifyMsgHeader(TF_REQ_13_WBODY, V_1_3, MULTIPART_REQUEST, 152);

        MBodyTableFeatures.Array body = (MBodyTableFeatures.Array)
                verifyMpHeader(msg, TABLE_FEATURES);
        List<MBodyTableFeatures> features = body.getList();
        assertEquals(AM_UXS, 1, features.size());

        //  see mpRequestTableFeaturesWithBody.hex for expected values
        Iterator<MBodyTableFeatures> iter = features.iterator();

        // assert the data in the first table features element
        MBodyTableFeatures tf = iter.next();
        verifyFirstTableFeatures(tf);
    }

    @Test
    public void mpReplyTableFeatures13() throws MessageParseException {
        print(EOL + "mpReplyTableFeatures13()");
        OfPacketReader pkt = getOfmTestReader(TF_REP_13);
        OpenflowMessage m = MessageFactory.parseMessage(pkt);
        print(FMT_START_TARGET, pkt.startIndex(), pkt.targetIndex());
        validateReplyTableFeatures13(m);
    }

    @Test
    public void mpReplyTableFeatures13Twice() throws MessageParseException {
        print(EOL + "mpReplyTableFeatures13Twice()");
        OfPacketReader pkt = getOfmTestReader(TF_REP_13_TWICE);

        OpenflowMessage m = MessageFactory.parseMessage(pkt);
        print(FMT_START_TARGET, pkt.startIndex(), pkt.targetIndex());
        validateReplyTableFeatures13(m);

        m = MessageFactory.parseMessage(pkt);
        print(FMT_START_TARGET, pkt.startIndex(), pkt.targetIndex());
        validateReplyTableFeatures13(m);
    }

    private void validateReplyTableFeatures13(OpenflowMessage m) {
        print(m.toDebugString());

        assertEquals(AM_NEQ, V_1_3, m.getVersion());
        assertEquals(AM_NEQ, MULTIPART_REPLY, m.getType());
        assertEquals(AM_UXS, 664, m.length());

        OfmMultipartReply msg = (OfmMultipartReply) m;

        MBodyTableFeatures.Array body = (MBodyTableFeatures.Array)
                verifyMpHeader(msg, TABLE_FEATURES);
        List<MBodyTableFeatures> features = body.getList();
        assertEquals(AM_UXS, 5, features.size());

        //  see mpRequestTableFeaturesWithBody.hex for expected values
        Iterator<MBodyTableFeatures> iter = features.iterator();
        verifyFirstTableFeatures(iter.next());
        verifySecondTableFeatures(iter.next());
        verifyThirdTableFeatures(iter.next());
        verifyFourthTableFeatures(iter.next());
        verifyLastTableFeatures(iter.next());
    }


    // private method to verify the "first table" test data
    private void verifyFirstTableFeatures(MBodyTableFeatures tf) {
        verifyTfFixed(tf, 0, "The First Table", FULL_META, HALF_META, 1024);

        // assert the data in the feature properties of the first table
        List<TableFeatureProp> propList = tf.getProps();
        assertEquals(AM_UXS, 2, propList.size());
        Iterator<TableFeatureProp> pIter = propList.iterator();

        // first property
        TableFeatureProp prop = pIter.next();
        assertEquals(AM_NEQ, INSTRUCTIONS, prop.getType());
        assertTrue(AM_HUH, TableFeaturePropInstr.class.isInstance(prop));
        TableFeaturePropInstr tfpi = (TableFeaturePropInstr) prop;
        verifyFlags(tfpi.getSupportedInstructions(),
                GOTO_TABLE,
                WRITE_METADATA,
                InstructionType.WRITE_ACTIONS,
                InstructionType.APPLY_ACTIONS,
                InstructionType.CLEAR_ACTIONS,
                InstructionType.METER);

        List<InstrExperimenter> supExper = tfpi.getSupportedExperInstructions();
        assertEquals(AM_UXS, 1, supExper.size());
        InstrExperimenter ie = supExper.get(0);
        assertArrayEquals(AM_NEQ, EXP_EXPER_DATA, ie.getData());

        // second property
        prop = pIter.next();
        assertEquals(AM_NEQ, INSTRUCTIONS_MISS, prop.getType());
        assertTrue(AM_HUH, TableFeaturePropInstr.class.isInstance(prop));
        tfpi = (TableFeaturePropInstr) prop;
        verifyFlags(tfpi.getSupportedInstructions(),
                GOTO_TABLE,
                WRITE_METADATA,
                InstructionType.WRITE_ACTIONS,
                InstructionType.APPLY_ACTIONS,
                InstructionType.CLEAR_ACTIONS);
        supExper = tfpi.getSupportedExperInstructions();
        assertEquals(AM_UXS, 0, supExper.size());
    }

    // private method to verify the "second table" test data
    private void verifySecondTableFeatures(MBodyTableFeatures tf) {
        verifyTfFixed(tf, 1, "The Second Table", HALF_META, FULL_META, 1025);

        // assert the data in the feature properties of the second table
        List<TableFeatureProp> propList = tf.getProps();
        assertEquals(AM_UXS, 2, propList.size());
        Iterator<TableFeatureProp> pIter = propList.iterator();

        // first property
        TableFeatureProp prop = pIter.next();
        assertEquals(AM_NEQ, NEXT_TABLES, prop.getType());
        assertTrue(AM_HUH, TableFeaturePropNextTable.class.isInstance(prop));
        TableFeaturePropNextTable tfpnt = (TableFeaturePropNextTable) prop;

        Set<TableId> ids = tfpnt.getNextTables();
        assertTrue(AM_HUH, ids.contains(tid(2)));
        assertTrue(AM_HUH, ids.contains(tid(3)));
        assertTrue(AM_HUH, ids.contains(tid(5)));
        assertEquals(AM_UXS, 3, ids.size());

        // second property
        prop = pIter.next();
        assertEquals(AM_NEQ, NEXT_TABLES_MISS, prop.getType());
        assertTrue(AM_HUH, TableFeaturePropNextTable.class.isInstance(prop));
        tfpnt = (TableFeaturePropNextTable) prop;

        ids = tfpnt.getNextTables();
        assertTrue(AM_HUH, ids.contains(tid(9)));
        assertEquals(AM_UXS, 1, ids.size());
    }

    private void verifyActionProp(TableFeatureProp prop,
                                  TableFeaturePropType expType,
                                  int expExperActCount, ActionType... expActs) {
        assertEquals(AM_NEQ, expType, prop.getType());
        assertTrue(AM_HUH, TableFeaturePropAction.class.isInstance(prop));
        TableFeaturePropAction tfpa = (TableFeaturePropAction) prop;

        Set<ActionType> suppActs = tfpa.getSupportedActions();
        verifyFlags(suppActs, expActs);
        assertEquals(AM_UXS, expExperActCount,
                tfpa.getSupportedExperActions().size());
    }

    // private method to verify the "second table" test data
    private void verifyThirdTableFeatures(MBodyTableFeatures tf) {
        verifyTfFixed(tf, 2, "The Third Table", HALF_META, HALF_META, 1026);

        // assert the data in the feature properties of the third table
        List<TableFeatureProp> propList = tf.getProps();
        assertEquals(AM_UXS, 4, propList.size());
        Iterator<TableFeatureProp> pIter = propList.iterator();

        // first property
        TableFeatureProp prop = pIter.next();
        verifyActionProp(prop, TableFeaturePropType.WRITE_ACTIONS, 0,
                ActionType.OUTPUT, ActionType.COPY_TTL_OUT,
                ActionType.COPY_TTL_IN);

        // second property
        prop = pIter.next();
        verifyActionProp(prop, WRITE_ACTIONS_MISS, 0, ActionType.SET_MPLS_TTL,
                ActionType.DEC_MPLS_TTL);

        // third property
        prop = pIter.next();
        verifyActionProp(prop, TableFeaturePropType.APPLY_ACTIONS, 0,
                ActionType.PUSH_VLAN, ActionType.POP_VLAN);

        // fourth property
        prop = pIter.next();
        verifyActionProp(prop, APPLY_ACTIONS_MISS, 1, ActionType.SET_QUEUE,
                ActionType.GROUP);
        TableFeaturePropAction tfpa = (TableFeaturePropAction) prop;
        ActExperimenter ae = tfpa.getSupportedExperActions().get(0);
        assertEquals(AM_NEQ, HP.encodedId(), ae.getId());
        assertArrayEquals(AM_NEQ, EXP_EXPER_DATA_ONE, ae.getData());
    }

    private void verifyFourthTableFeatures(MBodyTableFeatures tf) {
        verifyTfFixed(tf, 3, "The Fourth Table", FULL_META, FULL_META, 1027);

        // assert the data in the feature properties of the fourth table
        List<TableFeatureProp> propList = tf.getProps();
        assertEquals(AM_UXS, 6, propList.size());
        Iterator<TableFeatureProp> pIter = propList.iterator();

        // first property
        TableFeaturePropOxm prop = (TableFeaturePropOxm) pIter.next();
        verifyOxmProp(prop, MATCH, 4, 1);
        verifyOxmPropField(prop, OxmBasicFieldType.IN_PORT, false);
        verifyOxmPropField(prop, OxmBasicFieldType.IPV4_SRC, true);
        verifyOxmPropField(prop, OxmBasicFieldType.IPV4_DST, true);
        verifyOxmPropField(prop, OxmBasicFieldType.ETH_TYPE, false);
        verifyOxmPropExper(prop);

        // second property
        prop = (TableFeaturePropOxm) pIter.next();
        verifyOxmProp(prop, WILDCARDS, 2, 0);
        verifyOxmPropField(prop, OxmBasicFieldType.ETH_SRC, true);
        verifyOxmPropField(prop, OxmBasicFieldType.ETH_DST, true);

        // third property
        prop = (TableFeaturePropOxm) pIter.next();
        verifyOxmProp(prop, WRITE_SETFIELD, 2, 0);
        verifyOxmPropField(prop, OxmBasicFieldType.ETH_SRC, false);
        verifyOxmPropField(prop, OxmBasicFieldType.ETH_DST, false);

        // fourth property
        prop = (TableFeaturePropOxm) pIter.next();
        verifyOxmProp(prop, WRITE_SETFIELD_MISS, 2, 0);
        verifyOxmPropField(prop, OxmBasicFieldType.ETH_SRC, false);
        verifyOxmPropField(prop, OxmBasicFieldType.ETH_DST, false);

        // fifth property
        prop = (TableFeaturePropOxm) pIter.next();
        verifyOxmProp(prop, APPLY_SETFIELD, 2, 0);
        verifyOxmPropField(prop, OxmBasicFieldType.IPV4_SRC, false);
        verifyOxmPropField(prop, OxmBasicFieldType.IPV4_DST, false);

        // sixth property
        prop = (TableFeaturePropOxm) pIter.next();
        verifyOxmProp(prop, APPLY_SETFIELD_MISS, 2, 0);
        verifyOxmPropField(prop, OxmBasicFieldType.IPV6_SRC, false);
        verifyOxmPropField(prop, OxmBasicFieldType.IPV6_DST, false);

    }

    private void verifyOxmProp(TableFeaturePropOxm prop,
                               TableFeaturePropType type,
                               int expBasic, int expExper) {
        assertEquals(AM_NEQ, type, prop.getType());
        assertEquals(AM_UXS, expBasic, prop.getSupportedFieldTypes().size());
        assertEquals(AM_UXS, expExper, prop.getSupportedExperFields().size());
    }

    private void verifyOxmPropField(TableFeaturePropOxm prop,
                                    OxmBasicFieldType ft, boolean mask) {
        assertTrue("missing field", prop.getSupportedFieldTypes().contains(ft));
        assertEquals(AM_NEQ, mask, prop.hasMaskBitSet(ft));
    }

    private void verifyOxmPropExper(TableFeaturePropOxm prop) {
        // we know the list only contains one experimenter field...
        MFieldExperimenter exp = prop.getSupportedExperFields().get(0);
        assertEquals(AM_NEQ, 23, exp.getRawFieldType());
        assertEquals(AM_NEQ, HP, exp.getExpId());
    }


    private void verifyLastTableFeatures(MBodyTableFeatures tf) {
        verifyTfFixed(tf, 4, "The Last Table", FULL_META, FULL_META, 1028);

        // assert the data in the feature properties of the last table
        List<TableFeatureProp> propList = tf.getProps();
        assertEquals(AM_UXS, 2, propList.size());
        Iterator<TableFeatureProp> pIter = propList.iterator();

        // first property
        verifyExperProp(pIter.next(), TableFeaturePropType.EXPERIMENTER,
                HP, 42, HP_DATA);
        verifyExperProp(pIter.next(), EXPERIMENTER_MISS, BIG_SWITCH, 7, BS_DATA);
    }

    private static final byte[] HP_DATA = {2, 2, 4, 4, 6, 6, 8, 8};
    private static final byte[] BS_DATA = {2, 2, 4, 4, 6, 6};

    private void verifyExperProp(TableFeatureProp prop,
                                 TableFeaturePropType pType,
                                 ExperimenterId experId, int experType,
                                 byte[] expData) {
        TableFeaturePropExper pExper = (TableFeaturePropExper) prop;
        assertEquals(AM_NEQ, pType, pExper.getType());
        assertEquals(AM_NEQ, experId.encodedId(), pExper.getId());
        assertEquals(AM_NEQ, experType, pExper.getExpType());
        assertArrayEquals(AM_NEQ, expData, pExper.getData());
    }

    // NOTE: Table Features not supported in 1.0, 1.1, 1.2

    // ============================================= CREATING / ENCODING ====

    private static final byte[] EXPER_DATA =
            {0x0f, 0x00, 0x0b, 0xa2-B, 0xc0-B, 0x01, 0xc0-B, 0x01};


    @Test
    public void encodeMpRequestTableFeatures13NoBody() {
        print(EOL + "encodeMpRequestTableFeatures13NoBody()");
        OfmMutableMultipartRequest req = (OfmMutableMultipartRequest)
                MessageFactory.create(V_1_3, MULTIPART_REQUEST);
        req.clearXid();
        req.type(TABLE_FEATURES);
        encodeAndVerifyMessage(req.toImmutable(), TF_REQ_13_NOBODY);
    }

    @Test
    public void encodeMpRequestTableFeatures13WithBody()
            throws IncompleteStructureException {
        print(EOL + "encodeMpRequestTableFeatures13WithBody()");
        OfmMutableMultipartRequest req = (OfmMutableMultipartRequest)
                MessageFactory.create(V_1_3, MULTIPART_REQUEST, TABLE_FEATURES);
        req.clearXid();
        MBodyTableFeatures.MutableArray array =
                (MBodyTableFeatures.MutableArray) req.getBody();

        array.addTableFeatures(createFirstTable());

        encodeAndVerifyMessage(req.toImmutable(), TF_REQ_13_WBODY);
    }

    @Test
    public void encodeMpReplyTableFeatures13()
            throws IncompleteStructureException {
        print(EOL + "encodeMpReplyTableFeatures13()");
        OfmMutableMultipartReply rep = (OfmMutableMultipartReply)
                MessageFactory.create(V_1_3, MULTIPART_REPLY, TABLE_FEATURES);
        rep.clearXid();
        MBodyTableFeatures.MutableArray array =
                (MBodyTableFeatures.MutableArray) rep.getBody();

        array.addTableFeatures(createFirstTable());
        array.addTableFeatures(createSecondTable());
        array.addTableFeatures(createThirdTable());
        array.addTableFeatures(createFourthTable());
        array.addTableFeatures(createLastTable());

        encodeAndVerifyMessage(rep.toImmutable(), TF_REP_13);
    }

    private static final ProtocolVersion PV = V_1_3;

    // save some typing...
    private MBodyMutableTableFeatures mutTabFeat(int tid, String name,
                                                 long mMatch, long mWrite,
                                                 long max) {
        MBodyMutableTableFeatures tf = (MBodyMutableTableFeatures)
                MpBodyFactory.createReplyBodyElement(PV, TABLE_FEATURES);
        tf.tableId(tid(tid)).name(name).metadataMatch(mMatch)
                .metadataWrite(mWrite).maxEntries(max);
        return tf;
    }

    private MBodyTableFeatures createFirstTable() {
        MBodyMutableTableFeatures tf =
                mutTabFeat(0, "The First Table", FULL_META, HALF_META, 1024);

        // +++ INSTRUCTIONS property +++

        // the supported instructions...
        Set<InstructionType> suppInstr = new HashSet<InstructionType>(
                Arrays.asList(
                        InstructionType.GOTO_TABLE,
                        InstructionType.WRITE_METADATA,
                        InstructionType.WRITE_ACTIONS,
                        InstructionType.APPLY_ACTIONS,
                        InstructionType.CLEAR_ACTIONS,
                        InstructionType.METER
                )
        );

        // the supported experimenter instruction...
        InstrExperimenter ie = (InstrExperimenter) createInstruction(PV,
                InstructionType.EXPERIMENTER, HP, EXPER_DATA);
        List<InstrExperimenter> ieList = new ArrayList<InstrExperimenter>();
        ieList.add(ie);

        // stick 'em both in an Instructions prop and add them to the feature
        tf.addProp(createInstrProp(PV, INSTRUCTIONS, suppInstr, ieList));

        // +++ INSTRUCTIONS_MISS property +++

        // the supported instructions...
        suppInstr = new HashSet<InstructionType>(
                Arrays.asList(
                        InstructionType.GOTO_TABLE,
                        InstructionType.WRITE_METADATA,
                        InstructionType.WRITE_ACTIONS,
                        InstructionType.APPLY_ACTIONS,
                        InstructionType.CLEAR_ACTIONS
                )
        );

        // stick 'em in an Instructions-miss prop and add them to the feature
        tf.addProp(createInstrProp(PV, INSTRUCTIONS_MISS, suppInstr));

        // return the table features object...
        return (MBodyTableFeatures) tf.toImmutable();
    }

    private MBodyTableFeatures createSecondTable() {
        MBodyMutableTableFeatures tf =
                mutTabFeat(1, "The Second Table", HALF_META, FULL_META, 1025);

        // +++ NEXT_TABLES property +++
        Set<TableId> tables = new HashSet<TableId>(
                Arrays.asList(tid(2), tid(3), tid(5))
        );
        tf.addProp(createNextTablesProp(PV, NEXT_TABLES, tables));

        // +++ NEXT_TABLES_MISS property +++
        tables = new HashSet<TableId>(Arrays.asList(tid(9)));
        tf.addProp(createNextTablesProp(PV, NEXT_TABLES_MISS, tables));

        // return the table features object...
        return (MBodyTableFeatures) tf.toImmutable();
    }

    private MBodyTableFeatures createThirdTable() {
        MBodyMutableTableFeatures tf =
                mutTabFeat(2, "The Third Table", HALF_META, HALF_META, 1026);

        // +++ WRITE_ACTIONS property +++
        Set<ActionType> acts = new HashSet<ActionType>(
                Arrays.asList(
                        ActionType.OUTPUT,
                        ActionType.COPY_TTL_OUT,
                        ActionType.COPY_TTL_IN
                )
        );
        tf.addProp(createActionProp(PV, WRITE_ACTIONS, acts));

        // +++ WRITE_ACTIONS_MISS property +++
        acts = new HashSet<ActionType>(
                Arrays.asList(
                        ActionType.SET_MPLS_TTL,
                        ActionType.DEC_MPLS_TTL
                )
        );
        tf.addProp(createActionProp(PV, WRITE_ACTIONS_MISS, acts));

        // +++ APPLY_ACTIONS property +++
        acts = new HashSet<ActionType>(
                Arrays.asList(
                        ActionType.PUSH_VLAN,
                        ActionType.POP_VLAN
                )
        );
        tf.addProp(createActionProp(PV, APPLY_ACTIONS, acts));

        // +++ APPLY_ACTIONS_MISS property +++
        acts = new HashSet<ActionType>(
                Arrays.asList(
                        ActionType.SET_QUEUE,
                        ActionType.GROUP
                )
        );
        ActExperimenter ae = (ActExperimenter) createAction(PV,
                ActionType.EXPERIMENTER, HP, EXPER_DATA);
        tf.addProp(createActionProp(PV, APPLY_ACTIONS_MISS, acts,
                Arrays.asList(ae)));

        // return the table features object...
        return (MBodyTableFeatures) tf.toImmutable();
    }

    private MBodyTableFeatures createFourthTable() {
        MBodyMutableTableFeatures tf =
                mutTabFeat(3, "The Fourth Table", FULL_META, FULL_META, 1027);

        // +++ MATCH property +++
        Map<OxmBasicFieldType, Boolean> map =
                new HashMap<OxmBasicFieldType, Boolean>();
        map.put(OxmBasicFieldType.IN_PORT, false);
        map.put(OxmBasicFieldType.IPV4_SRC, true);
        map.put(OxmBasicFieldType.IPV4_DST, true);
        map.put(OxmBasicFieldType.ETH_TYPE, false);
        MFieldExperimenter emf = createExperimenterField(PV, 23, HP, null);
        tf.addProp(createOxmProp(PV, MATCH, map, Arrays.asList(emf)));

        // +++ WILDCARDS property +++
        map.clear();
        map.put(OxmBasicFieldType.ETH_SRC, true);
        map.put(OxmBasicFieldType.ETH_DST, true);
        tf.addProp(createOxmProp(PV, WILDCARDS, map));

        // +++ WRITE_SETFIELD property +++
        map.clear();
        map.put(OxmBasicFieldType.ETH_SRC, false);
        map.put(OxmBasicFieldType.ETH_DST, false);
        tf.addProp(createOxmProp(PV, WRITE_SETFIELD, map));

        // +++ WRITE_SETFIELD_MISS property +++
        // same fields for this one, we don't need to modify the map
//        map.clear();
//        map.put(OxmBasicFieldType.ETH_SRC, false);
//        map.put(OxmBasicFieldType.ETH_DST, false);
        tf.addProp(createOxmProp(PV, WRITE_SETFIELD_MISS, map));

        // +++ APPLY_SETFIELD property +++
        map.clear();
        map.put(OxmBasicFieldType.IPV4_SRC, false);
        map.put(OxmBasicFieldType.IPV4_DST, false);
        tf.addProp(createOxmProp(PV, APPLY_SETFIELD, map));

        // +++ APPLY_SETFIELD_MISS property +++
        map.clear();
        map.put(OxmBasicFieldType.IPV6_SRC, false);
        map.put(OxmBasicFieldType.IPV6_DST, false);
        tf.addProp(createOxmProp(PV, APPLY_SETFIELD_MISS, map));

        // return the table features object...
        return (MBodyTableFeatures) tf.toImmutable();
    }

    private MBodyTableFeatures createLastTable() {
        MBodyMutableTableFeatures tf =
                mutTabFeat(4, "The Last Table", FULL_META, FULL_META, 1028);

        tf.addProp(createExperProp(PV, EXPERIMENTER, HP, 42, HP_DATA));
        tf.addProp(createExperProp(PV, EXPERIMENTER_MISS, BIG_SWITCH, 7, BS_DATA));

        // return the table features object...
        return (MBodyTableFeatures) tf.toImmutable();
    }

}
