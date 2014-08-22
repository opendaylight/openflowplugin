/*
 * (c) Copyright 2012-2014 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.of.lib.instr;

import org.junit.Test;
import org.opendaylight.of.lib.*;
import org.opendaylight.of.lib.dt.GroupId;
import org.opendaylight.of.lib.dt.QueueId;
import org.opendaylight.of.lib.match.FieldFactory;
import org.opendaylight.of.lib.match.MFieldBasic;
import org.opendaylight.of.lib.match.MfbVlanVid;
import org.opendaylight.of.lib.match.OxmBasicFieldType;
import org.opendaylight.of.lib.msg.OfmTest;
import org.opendaylight.of.lib.msg.Port;
import org.opendaylight.util.junit.TestTools;
import org.opendaylight.util.net.*;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.*;
import static org.opendaylight.of.lib.ExperimenterId.BIG_SWITCH;
import static org.opendaylight.of.lib.ExperimenterId.HP_LABS;
import static org.opendaylight.of.lib.ProtocolVersion.V_1_0;
import static org.opendaylight.of.lib.ProtocolVersion.V_1_3;
import static org.opendaylight.of.lib.instr.ActionFactory.createAction;
import static org.opendaylight.of.lib.instr.ActionType.*;
import static org.opendaylight.of.lib.match.OxmBasicFieldType.*;
import static org.opendaylight.util.StringUtils.EOL;
import static org.opendaylight.util.StringUtils.toCamelCase;
import static org.opendaylight.util.junit.TestTools.*;

/**
 * Unit tests for ActionFactory.
 *
 * @author Simon Hunt
 */
public class ActionFactoryTest extends OfmTest {
    private static final String E_PARSE_FAIL = "failed to parse action";
    private static final String[] FILE_PREFIX = {
            "instr/v10/act",
            "instr/v11/act",
            "instr/v12/act",
            "instr/v13/act",
    };

    private static final QueueId EXP_QUEUE = QueueId.valueOf(0x80000005L);
    private static final BigPortNumber EXP_PORT = bpn(42);
    private static final BigPortNumber PORT_7 = bpn(7);
    private static final PortNumber EXP_SM_PORT = pn(99);
    private static final GroupId EXP_GROUP = GroupId.valueOf(5);
    private static final VlanId EXP_VLAN_VID = VlanId.valueOf(42);
    private static final MacAddress MAC = mac("665544:332211");
    private static final IpAddress IPv4 = ip("15.254.17.1");
    private static final int EXP_TOS = 31;
    private static final int EXP_PRIORITY = 4;
    private static final int EXP_MPLS_TTL = 19;
    private static final int EXP_NW_TTL = 35;
    private static final byte[] BIG_SWITCH_DATA = {
            32, 33, 34, 35, 36, 37, 38, 39
    };
    private static final byte[] EXP_EXP_DATA = {
            1, 2, 3, 4, 5, 6, 7, 8,
    };

    private OfPacketReader pkt;
    private Action act;
    private MFieldBasic mf;

    private OfPacketReader getPkt(ProtocolVersion pv, Enum<?> actType,
                                String suffix) {
        String prefix = FILE_PREFIX[pv.ordinal()];
        String basename = toCamelCase(prefix, actType);
        return getPacketReader(basename + suffix + HEX);
    }

    private OfPacketReader getPkt(ProtocolVersion pv, ActionType actType) {
        return getPkt(pv, actType, "");
    }

    private OfPacketReader getPktLegacy(ProtocolVersion pv, OldActionType oat,
                                        String suffix) {
        return getPkt(pv, oat, suffix);
    }

    private void verifyActionHeader(Action act, ActionType expType, int expLen) {
        assertEquals(AM_NEQ, expType, act.getActionType());
        assertEquals(AM_NEQ, expLen, act.header.length); // no getter!
    }

    // ========================================================= PARSING ====

    /** Gets the hex file, parses it, and asserts stuff about the header, and
     * first embedded value. If there is more than one associated value,
     * it has to be asserted outside this method call. See
     * {@link ActionLookup#verifyAction(Action, ActionType, Object)}
     *
     * @param pkt the packet reader containing the file data
     * @param pv the protocol version
     * @param type the action type
     * @param expLen the expected structure length in bytes
     * @param expValue the expected value, if there is one
     * @return the action object
     */
    private Action parseActFile(OfPacketReader pkt, ProtocolVersion pv,
                                ActionType type, int expLen, Object expValue) {
        Action act = null;
        try {
            act = ActionFactory.parseAction(pkt, pv);
            print(act);
        } catch (MessageParseException e) {
            print(e);
            fail(E_PARSE_FAIL);
        }
        checkEOBuffer(pkt);
        verifyActionHeader(act, type, expLen);
        ActionLookup.verifyAction(act, type, expValue);
        return act;
    }

    private Action parseActionFile(ProtocolVersion pv, ActionType type,
                                   int expLen, Object expVal) {
        pkt = getPkt(pv, type);
        return parseActFile(pkt, pv, type, expLen, expVal);
    }

    private Action parseActionFile(ProtocolVersion pv, ActionType type,
                                 String suffix, int expLen, Object expVal) {
        pkt = getPkt(pv, type, suffix);
        return parseActFile(pkt, pv, type, expLen, expVal);
    }

    /** Gets the hex file, parses it, and asserts stuff about the header,
     * including that this is a SET_FIELD action, etc.
     *
     * @param pv the protocol version
     * @param suffix the hex file name suffix
     * @param expLen the expected action structure length
     * @param expFt the expected match field type
     * @param expFieldValue the expected field value
     * @return the action
     */
    private ActSetField parseSetFieldActionFile(ProtocolVersion pv,
                                                String suffix, int expLen,
                                                OxmBasicFieldType expFt,
                                                Object expFieldValue) {
        pkt = getPkt(pv, SET_FIELD, suffix);
        Action act = null;
        try {
            act = ActionFactory.parseAction(pkt, pv);
            print(act);
        } catch (MessageParseException e) {
            print(e);
            fail(E_PARSE_FAIL);
        }
        checkEOBuffer(pkt);
        verifyActionHeader(act, SET_FIELD, expLen);
        ActionLookup.verifyActionSetField(act, expFt, expFieldValue);
        return (ActSetField) act;
    }

    /** Gets the hex file, parses it, and asserts stuff about the header,
     * including that this is a SET_FIELD action, etc.
     *
     * @param pv the protocol version
     * @param oldActType the legacy action type
     * @param suffix suffix to hex file
     * @param expLen the expected action structure length
     * @param expFt the expected embedded field type
     * @param expFieldValue the expected field value
     * @return the action
     */
    private ActSetField parseSetFieldActionFile(ProtocolVersion pv,
                                                OldActionType oldActType,
                                                String suffix,
                                                int expLen,
                                                OxmBasicFieldType expFt,
                                                Object expFieldValue) {
        pkt = getPktLegacy(pv, oldActType, suffix);
        Action act = null;
        try {
            act = ActionFactory.parseAction(pkt, pv);
            print(act);
        } catch (MessageParseException e) {
            print(e);
            fail(E_PARSE_FAIL);
        }
        checkEOBuffer(pkt);
        verifyActionHeader(act, SET_FIELD, expLen);
        ActionLookup.verifyActionSetField(act, expFt, expFieldValue);
        return (ActSetField) act;
    }

    /** Gets the hex file, parses it, and asserts stuff about the header,
     * including that this is a SET_FIELD action, etc.
     *
     * @param pv the protocol version
     * @param oldActType the legacy action type
     * @param expLen the expected action structure length
     * @param expFt the expected embedded field type
     * @param expFieldValue the expected field value
     * @return the action
     */
    private ActSetField parseSetFieldActionFile(ProtocolVersion pv,
                                                OldActionType oldActType,
                                                int expLen,
                                                OxmBasicFieldType expFt,
                                                Object expFieldValue) {
        return parseSetFieldActionFile(pv, oldActType, "", expLen,
                expFt, expFieldValue);
    }

    @Test
    public void output13() {
        print(EOL + "output13()");
        // see v13/actOutput.hex for expected values
        ActOutput ai = (ActOutput)
                parseActionFile(V_1_3, OUTPUT, 16, Port.CONTROLLER);
        assertEquals(AM_NEQ, ActOutput.CONTROLLER_MAX, ai.getMaxLen());
    }

    @Test
    public void output10() {
        print(EOL + "output10");
        // see v10/actOutput.hex for expected values
        ActOutput ai = (ActOutput)
                parseActionFile(V_1_0, OUTPUT, 8, Port.CONTROLLER);
        assertEquals(AM_NEQ, ActOutput.CONTROLLER_MAX, ai.getMaxLen());
    }


    @Test
    public void group13() {
        print(EOL + "group13()");
        // see v13/actGroup.hex for expected values
        ActGroup ai = (ActGroup) parseActionFile(V_1_3, GROUP, 8, EXP_GROUP);
    }

    // NOTE: GROUP Action not applicable to 1.0

    @Test
    public void setQueue13() {
        print(EOL + "setQueue13()");
        // see v13/actSetQueue.hex for expected values
        ActSetQueue ai = (ActSetQueue)
                parseActionFile(V_1_3, SET_QUEUE, 8, EXP_QUEUE);
        assertNull(AM_HUH, ai.getPort());
    }

    @Test
    public void enqueue10() {
        print(EOL + "enqueue10()");
        // see v10/actSetQueue.hex for expected values
        ActSetQueue ai = (ActSetQueue)
                parseActionFile(V_1_0, SET_QUEUE, 16, EXP_QUEUE);
        assertEquals(AM_NEQ, EXP_PORT, ai.getPort());
    }

    // === some 1.0 set-field actions


    @Test
    public void setVlanVid10() {
        print(EOL + "setVlanVid10()");
        // see v10/actSetVlanVid.hex for expected values
        ActSetField ai = parseSetFieldActionFile(V_1_0,
                OldActionType.SET_VLAN_VID, 8, VLAN_VID, EXP_VLAN_VID);
        MfbVlanVid mfi = (MfbVlanVid) ai.getField();
        assertEquals(AM_NEQ, VlanId.valueOf(42), mfi.getVlanId());
    }

    @Test
    public void setVlanVidNone10() {
        print(EOL + "setVlanVidNone10()");
        // see v10/actSetVlanVidNone.hex for expected values
        ActSetField ai = parseSetFieldActionFile(V_1_0,
                OldActionType.SET_VLAN_VID, "None", 8, VLAN_VID, VlanId.NONE);
        MfbVlanVid mfi = (MfbVlanVid) ai.getField();
        assertEquals(AM_NEQ, VlanId.NONE, mfi.getVlanId());
    }


    @Test
    public void setVlanPcp10() {
        print(EOL + "setVlanPcp10()");
        // see v10/actSetVlanPcp.hex for expected values
        ActSetField ai = parseSetFieldActionFile(V_1_0,
                OldActionType.SET_VLAN_PCP, 8, VLAN_PCP, EXP_PRIORITY);
    }

    @Test
    public void stripVlan10() {
        print(EOL + "stripVlan10()");
        // see v10/actStripVlan.hex for expected values
        ActPopVlan ai = (ActPopVlan) parseActionFile(V_1_0, POP_VLAN, 8, null);
    }

    @Test
    public void setDlSrc10() {
        print(EOL + "setDlSrc10()");
        // see v10/actSetDlSrc.hex for expected values
        ActSetField ai = parseSetFieldActionFile(V_1_0,
                OldActionType.SET_DL_SRC, 16, ETH_SRC, MAC);
    }

    @Test
    public void setDlDst10() {
        print(EOL + "setDlDst10()");
        // see v10/actSetDlDst.hex for expected values
        ActSetField ai = parseSetFieldActionFile(V_1_0,
                OldActionType.SET_DL_DST, 16, ETH_DST, MAC);
    }

    @Test
    public void setNwSrc10() {
        print(EOL + "setNwSrc10()");
        // see v10/actSetNwSrc.hex for expected values
        ActSetField ai = parseSetFieldActionFile(V_1_0,
                OldActionType.SET_NW_SRC, 8, IPV4_SRC, IPv4);
    }

    @Test
    public void setNwDst10() {
        print(EOL + "setNwDst10()");
        // see v10/actSetNwDst.hex for expected values
        ActSetField ai = parseSetFieldActionFile(V_1_0,
                OldActionType.SET_NW_DST, 8, IPV4_DST, IPv4);
    }

    @Test
    public void setNwTos10() {
        print(EOL + "setNwTos10()");
        // see v10/actSetNwTos.hex for expected values
        // NOTE that IP ToS maps to IP_DSCP in v1.3-speak
        ActSetField ai = parseSetFieldActionFile(V_1_0,
                OldActionType.SET_NW_TOS, 8, IP_DSCP, EXP_TOS);
    }

    @Test
    public void setTpSrc10() {
        print(EOL + "setTpSrc10()");
        // see v10/actSetTpSrc.hex for expected values
        ActSetField ai = parseSetFieldActionFile(V_1_0,
                OldActionType.SET_TP_SRC, 8, TCP_SRC, EXP_SM_PORT);
    }

    @Test
    public void setTpDst10() {
        print(EOL + "setTpDst10()");
        // see v10/actSetTpDst.hex for expected values
        ActSetField ai = parseSetFieldActionFile(V_1_0,
                OldActionType.SET_TP_DST, 8, TCP_DST, EXP_SM_PORT);
    }

    @Test
    public void vendor10() {
        print(EOL + "vendor()");
        // see v10/.hex for expected values
        ActExperimenter ae = (ActExperimenter)
                parseActionFile(V_1_0, EXPERIMENTER, 16, ExperimenterId.HP_LABS);
        assertArrayEquals(AM_NEQ, EXP_EXP_DATA, ae.getData());
    }

    // ======= ==========================================

    @Test
    public void setMplsTtl13() {
        print(EOL + "setMplsTtl13()");
        // see actSetMplsTtl.hex for expected values
        ActSetMplsTtl ai = (ActSetMplsTtl)
                parseActionFile(V_1_3, SET_MPLS_TTL, 8, EXP_MPLS_TTL);
    }

    @Test
    public void decMplsTtl13() {
        print(EOL + "decMplsTtl13()");
        // see actDecMplsTtl.hex for expected values
        ActDecMplsTtl ai = (ActDecMplsTtl)
                parseActionFile(V_1_3, DEC_MPLS_TTL, 8, null);
    }

    @Test
    public void setNwTtl13() {
        print(EOL + "setNwTtl13()");
        // see actSetNwTtl.hex for expected values
        ActSetNwTtl ai = (ActSetNwTtl)
                parseActionFile(V_1_3, SET_NW_TTL, 8, EXP_NW_TTL);
    }

    @Test
    public void decNwTtl13() {
        print(EOL + "decNwTtl13()");
        // see actDecNwTtl.hex for expected values
        ActDecNwTtl ai = (ActDecNwTtl)
                parseActionFile(V_1_3, DEC_NW_TTL, 8, null);
    }

    @Test
    public void copyTtlOut13() {
        print(EOL + "copyTtlOut13()");
        // see actCopyTtlOut.hex for expected values
        ActCopyTtlOut ai = (ActCopyTtlOut)
                parseActionFile(V_1_3, COPY_TTL_OUT, 8, null);
    }

    @Test
    public void copyTtlIn13() {
        print(EOL + "copyTtlIn13()");
        // see actCopyTtlIn.hex for expected values
        ActCopyTtlIn ai = (ActCopyTtlIn)
                parseActionFile(V_1_3, COPY_TTL_IN, 8, null);
    }

    @Test
    public void pushVlan13() {
        print(EOL + "pushVlan13()");
        // see actPushVlanEtypeVlan.hex for expected values
        ActPushVlan ai = (ActPushVlan) parseActionFile(V_1_3, PUSH_VLAN,
                "EtypeVlan", 8, EthernetType.VLAN);
    }

    @Test
    public void pushMpls13() {
        print(EOL + "pushMpls13()");
        // see actPushMplsEtypeMplsu.hex for expected values
        ActPushMpls ai = (ActPushMpls) parseActionFile(V_1_3, PUSH_MPLS,
                "EtypeMplsu", 8, EthernetType.MPLS_U);
    }

    @Test
    public void pushPbb13() {
        print(EOL + "pushPbb13()");
        // see actPushPbb.hex for expected values
        ActPushPbb ai = (ActPushPbb) parseActionFile(V_1_3, PUSH_PBB,
                8, EthernetType.PBB);
    }

    @Test
    public void popVlan13() {
        print(EOL + "popVlan13()");
        // see actPopVlan.hex for expected values
        ActPopVlan ai = (ActPopVlan) parseActionFile(V_1_3, POP_VLAN, 8, null);
    }

    @Test
    public void popPbb13() {
        print(EOL + "popPbb13()");
        // see actPopPbb.hex for expected values
        ActPopPbb ai = (ActPopPbb) parseActionFile(V_1_3, POP_PBB, 8, null);
    }

    @Test
    public void popMpls13() {
        print(EOL + "popMpls13()");
        // see actPopMpls.hex for expected values
        ActPopMpls ai = (ActPopMpls)
                parseActionFile(V_1_3, POP_MPLS, 8, EthernetType.IPv6);
    }

    @Test
    public void experimenter13() {
        print(EOL + "experimenter13()");
        // see actExperimenter.hex for expected values
        ActExperimenter ai = (ActExperimenter)
                parseActionFile(V_1_3, EXPERIMENTER, 16, BIG_SWITCH);
        assertArrayEquals(AM_NEQ, BIG_SWITCH_DATA, ai.getData());
    }

    @Test
    public void setFieldEthDst13() {
        print(EOL + "setFieldEthDst13()");
        // see actSetFieldEthDst.hex for expected values
        ActSetField ai =
                parseSetFieldActionFile(V_1_3, "EthDst", 16, ETH_DST, MAC);
    }

    @Test
    public void setFieldIpv4Src13() {
        print(EOL + "setFieldIpv4Src13()");
        // see actSetFieldIpv4Src.hex for expected values
        ActSetField ai =
                parseSetFieldActionFile(V_1_3, "Ipv4Src", 16, IPV4_SRC, IPv4);
    }


    // ============================================= CREATING / ENCODING ====

    private byte[] getExpBytes(ProtocolVersion pv, ActionType type,
                               String suffix) {
        String prefix = FILE_PREFIX[pv.ordinal()];
        String basename = toCamelCase(prefix, type);
        return getExpByteArray("../" + basename + suffix);
    }

    private byte[] getExpBytes(ProtocolVersion pv, ActionType type) {
        return getExpBytes(pv, type, "");
    }

    private byte[] getExpBytes(ProtocolVersion pv, OldActionType type,
                               String suffix) {
        String prefix = FILE_PREFIX[pv.ordinal()];
        String basename = toCamelCase(prefix, type);
        return getExpByteArray("../" + basename + suffix);
    }

    private byte[] getExpBytes(ProtocolVersion pv, OldActionType type) {
        return getExpBytes(pv, type, "");
    }

    private void verifyActionEncodingBase(Action act, String label,
                                          byte[] expData) {
        print("{}{}()", EOL, label);
        print(act);
        // encode the field into a buffer the same size
        OfPacketWriter pkt = new OfPacketWriter(expData.length);
        ActionFactory.encodeAction(act, pkt);
        // check that all is as expected
        verifyEncodement(label, expData, pkt);
    }

    private void verifyActionEncoding(ProtocolVersion pv, Action act,
                                      ActionType type) {
        String label = pv + ": " + toCamelCase("encode", type);
        byte[] expData = getExpBytes(pv, type);
        verifyActionEncodingBase(act, label, expData);
    }

    private void verifyActionEncoding(ProtocolVersion pv, Action act,
                                      ActionType type, String suffix) {
        String label = pv + ": " + toCamelCase("encode", type) + suffix;
        byte[] expData = getExpBytes(pv, type, suffix);
        verifyActionEncodingBase(act, label, expData);
    }

    private void verifyActionEncoding(ProtocolVersion pv, Action act,
                                      OldActionType oat) {
        String label = pv + ": " + toCamelCase("encode", oat);
        byte[] expData = getExpBytes(pv, oat);
        verifyActionEncodingBase(act, label, expData);
    }

    @Test
    public void outputPort7() {
        act = createAction(V_1_3, OUTPUT, PORT_7);
        ActOutput ao = (ActOutput) act;
        assertEquals(AM_NEQ, 0, ao.getMaxLen());

        act = createAction(V_1_3, OUTPUT, PORT_7, 0);
        ao = (ActOutput) act;
        assertEquals(AM_NEQ, 0, ao.getMaxLen());

        act = createAction(V_1_3, OUTPUT, PORT_7,
                ActOutput.CONTROLLER_NO_BUFFER);
        ao = (ActOutput) act;
        assertEquals(AM_NEQ, ActOutput.CONTROLLER_NO_BUFFER, ao.getMaxLen());
    }

    @Test(expected = IllegalArgumentException.class)
    public void outputPort7PartialPacket() {
        act = createAction(V_1_3, OUTPUT, PORT_7, 128);
    }

    @Test(expected = IllegalArgumentException.class)
    public void outputPort7PartialPacketMax() {
        act = createAction(V_1_3, OUTPUT, PORT_7,
                ActOutput.CONTROLLER_MAX);
    }

    @Test
    public void encodeOutput13() {
        act = createAction(V_1_3, OUTPUT,
                Port.CONTROLLER, ActOutput.CONTROLLER_MAX);
        verifyActionEncoding(V_1_3, act, OUTPUT);
    }

    @Test
    public void encodeOutput10() {
        act = createAction(V_1_0, OUTPUT,
                Port.CONTROLLER, ActOutput.CONTROLLER_MAX);
        verifyActionEncoding(V_1_0, act, OUTPUT);
    }

    @Test
    public void encodeGroup13() {
        act = createAction(V_1_3, GROUP, EXP_GROUP);
        verifyActionEncoding(V_1_3, act, GROUP);
    }

    // IMPLEMENTATION NOTE: Group action not applicable to 1.0
    @Test(expected = VersionMismatchException.class)
    public void createGroup10() {
        createAction(V_1_0, GROUP, EXP_GROUP);
    }

    @Test
    public void encodeSetQueue13() {
        act = createAction(V_1_3, SET_QUEUE, EXP_QUEUE);
        verifyActionEncoding(V_1_3, act, SET_QUEUE);
    }

    @Test
    public void encodeSetQueue10() {
        act = createAction(V_1_0, SET_QUEUE, EXP_QUEUE, EXP_PORT);
        verifyActionEncoding(V_1_0, act, SET_QUEUE);
    }

    @Test
    public void encodeSetMplsTtl13() {
        act = createAction(V_1_3, SET_MPLS_TTL, EXP_MPLS_TTL);
        verifyActionEncoding(V_1_3, act, SET_MPLS_TTL);
    }

    // IMPLEMENTATION NOTE: Set MPLS TTL action not applicable to 1.0
    @Test(expected = VersionMismatchException.class)
    public void createSetMpleTtl10() {
        createAction(V_1_0, SET_MPLS_TTL, EXP_MPLS_TTL);
    }

    @Test
    public void encodeDecMplsTtl13() {
        act = createAction(V_1_3, DEC_MPLS_TTL);
        verifyActionEncoding(V_1_3, act, DEC_MPLS_TTL);
    }

    // IMPLEMENTATION NOTE: Decrement MPLS TTL action not applicable to 1.0
    @Test(expected = VersionMismatchException.class)
    public void createDecMplsTtl10() {
        createAction(V_1_0, DEC_MPLS_TTL);
    }

    @Test
    public void encodeSetNwTtl13() {
        act = createAction(V_1_3, SET_NW_TTL, EXP_NW_TTL);
        verifyActionEncoding(V_1_3, act, SET_NW_TTL);
    }

    // IMPLEMENTATION NOTE: Set Network TTL action not applicable to 1.0
    @Test(expected = VersionMismatchException.class)
    public void createSetNwTtl10() {
        createAction(V_1_0, SET_NW_TTL, EXP_NW_TTL);
    }

    @Test
    public void encodeDecNwTtl13() {
        act = createAction(V_1_3, DEC_NW_TTL);
        verifyActionEncoding(V_1_3, act, DEC_NW_TTL);
    }

    // IMPLEMENTATION NOTE: Decrement Network TTL action not applicable to 1.0
    @Test(expected = VersionMismatchException.class)
    public void createDecNwTtl10() {
        createAction(V_1_0, DEC_NW_TTL);
    }

    @Test
    public void encodeCopyTtlOut13() {
        act = createAction(V_1_3, COPY_TTL_OUT);
        verifyActionEncoding(V_1_3, act, COPY_TTL_OUT);
    }

    // IMPLEMENTATION NOTE: Copy TTL Out action not applicable to 1.0
    @Test(expected = VersionMismatchException.class)
    public void createCopyTtlOut10() {
        createAction(V_1_0, COPY_TTL_OUT);
    }

    @Test
    public void encodeCopyTtlIn13() {
        act = createAction(V_1_3, COPY_TTL_IN);
        verifyActionEncoding(V_1_3, act, COPY_TTL_IN);
    }

    // IMPLEMENTATION NOTE: Copy TTL In action not applicable to 1.0
    @Test(expected = VersionMismatchException.class)
    public void createCopyTtlIn10() {
        createAction(V_1_0, COPY_TTL_IN);
    }

    // NOTE: as per OF spec 1.3.1 (pg.23) valid EtherTypes for PUSH_VLAN
    //          are 0x8100(VLAN) and 0x88a8(PRV_BRDG)
    @Test
    public void encodePushVlan13EtypeVlan() {
        act = createAction(V_1_3, PUSH_VLAN, EthernetType.VLAN);
        verifyActionEncoding(V_1_3, act, PUSH_VLAN, "EtypeVlan");
    }

    @Test
    public void encodePushVlan13EtypePrvbrdg() {
        act = createAction(V_1_3, PUSH_VLAN, EthernetType.PRV_BRDG);
        verifyActionEncoding(V_1_3, act, PUSH_VLAN, "EtypePrvbrdg");
    }

    private void checkBadEthType(ActionType at, EthernetType et) {
        try {
            createAction(V_1_3, at, et);
            fail(AM_NOEX);
        } catch (IllegalArgumentException e) {
            print(FMT_EX, e);
        } catch (Exception e) {
            fail(AM_WREX);
        }
    }

    @Test
    public void pushVlan13BadEtypes() {
        checkBadEthType(PUSH_VLAN, EthernetType.IPv4);
        checkBadEthType(PUSH_VLAN, EthernetType.ARP);
        checkBadEthType(PUSH_VLAN, EthernetType.SNMP);
        checkBadEthType(PUSH_VLAN, EthernetType.IPv6);
        checkBadEthType(PUSH_VLAN, EthernetType.MPLS_U);
        checkBadEthType(PUSH_VLAN, EthernetType.MPLS_M);
        checkBadEthType(PUSH_VLAN, EthernetType.LLDP);
        checkBadEthType(PUSH_VLAN, EthernetType.PBB);
        checkBadEthType(PUSH_VLAN, EthernetType.BDDP);
        checkBadEthType(PUSH_VLAN, EthernetType.valueOf(0x1234));
    }

    // IMPLEMENTATION NOTE: Push VLAN action not applicable to 1.0
    @Test(expected = VersionMismatchException.class)
    public void createPushVlan10() {
        createAction(V_1_0, PUSH_VLAN, EthernetType.VLAN);
    }

    // NOTE: as per OF spec 1.3.1 (pg.23) valid EtherTypes for PUSH_MPLS
    //          are 0x8847(MPLS_U) and 0x8848(MPLS_M)
    @Test
    public void encodePushMpls13ETypeMplsu() {
        act = createAction(V_1_3, PUSH_MPLS, EthernetType.MPLS_U);
        verifyActionEncoding(V_1_3, act, PUSH_MPLS, "EtypeMplsu");
    }

    @Test
    public void encodePushMpls13ETypeMplsm() {
        act = createAction(V_1_3, PUSH_MPLS, EthernetType.MPLS_M);
        verifyActionEncoding(V_1_3, act, PUSH_MPLS, "EtypeMplsm");
    }

    @Test
    public void pushMpls13BadEtypes() {
        checkBadEthType(PUSH_MPLS, EthernetType.IPv4);
        checkBadEthType(PUSH_MPLS, EthernetType.ARP);
        checkBadEthType(PUSH_MPLS, EthernetType.VLAN);
        checkBadEthType(PUSH_MPLS, EthernetType.SNMP);
        checkBadEthType(PUSH_MPLS, EthernetType.IPv6);
        checkBadEthType(PUSH_MPLS, EthernetType.PRV_BRDG);
        checkBadEthType(PUSH_MPLS, EthernetType.LLDP);
        checkBadEthType(PUSH_MPLS, EthernetType.PBB);
        checkBadEthType(PUSH_MPLS, EthernetType.BDDP);
        checkBadEthType(PUSH_MPLS, EthernetType.valueOf(0x1234));
    }

    // IMPLEMENTATION NOTE: Push MPLS action not applicable to 1.0
    @Test(expected = VersionMismatchException.class)
    public void createPushMpls10() {
        createAction(V_1_0, PUSH_MPLS, EthernetType.MPLS_U);
    }

    // NOTE: as per OF spec 1.3.1 (pg.23) valid EtherType for PUSH_PBB
    //          is 0x88e7(PBB)
    @Test
    public void encodePushPbb13() {
        act = createAction(V_1_3, PUSH_PBB, EthernetType.PBB);
        verifyActionEncoding(V_1_3, act, PUSH_PBB);
    }

    @Test
    public void pushPbb13BadEtypes() {
        checkBadEthType(PUSH_PBB, EthernetType.IPv4);
        checkBadEthType(PUSH_PBB, EthernetType.ARP);
        checkBadEthType(PUSH_PBB, EthernetType.VLAN);
        checkBadEthType(PUSH_PBB, EthernetType.SNMP);
        checkBadEthType(PUSH_PBB, EthernetType.IPv6);
        checkBadEthType(PUSH_PBB, EthernetType.MPLS_U);
        checkBadEthType(PUSH_PBB, EthernetType.MPLS_M);
        checkBadEthType(PUSH_PBB, EthernetType.PRV_BRDG);
        checkBadEthType(PUSH_PBB, EthernetType.LLDP);
        checkBadEthType(PUSH_PBB, EthernetType.BDDP);
        checkBadEthType(PUSH_PBB, EthernetType.valueOf(0x1234));
    }

    // IMPLEMENTATION NOTE: Push PBB action not applicable to 1.0
    @Test(expected = VersionMismatchException.class)
    public void createPushPbb10() {
        createAction(V_1_0, PUSH_PBB, EthernetType.PBB);
    }

    @Test
    public void encodePopVlan13() {
        act = createAction(V_1_3, POP_VLAN);
        verifyActionEncoding(V_1_3, act, POP_VLAN);
    }

    @Test
    public void encodeStripVlan10() {
        // in v1.3 parlance: POP_VLAN
        act = createAction(V_1_0, POP_VLAN);
        verifyActionEncoding(V_1_0, act, POP_VLAN);
    }

    @Test
    public void encodePopPbb13() {
        act = createAction(V_1_3, POP_PBB);
        verifyActionEncoding(V_1_3, act, POP_PBB);
    }

    // IMPLEMENTATION NOTE: Pop PBB action not applicable to 1.0
    @Test(expected = VersionMismatchException.class)
    public void createPopPbb10() {
        createAction(V_1_0, POP_PBB);
    }

    // NOTE: as per OF spec 1.3.1 (pg.23) EtherType for POP_MPLS described as:
    //          "The Ethertype is used as the Ethertype for the resulting
    //          packet (Ethertype for the MPLS payload)."
    // TODO: exactly what are the restrictions on EthernetType?
    @Test
    public void encodePopMpls13() {
        act = createAction(V_1_3, POP_MPLS, EthernetType.IPv6);
        verifyActionEncoding(V_1_3, act, POP_MPLS);
    }

    // IMPLEMENTATION NOTE: Pop MPLS action not applicable to 1.0
    @Test(expected = VersionMismatchException.class)
    public void createPopMpls10() {
        createAction(V_1_0, POP_MPLS, EthernetType.IPv6);
    }

    @Test
    public void encodeExperimenter13() {
        act = createAction(V_1_3, EXPERIMENTER, BIG_SWITCH, BIG_SWITCH_DATA);
        verifyActionEncoding(V_1_3, act, EXPERIMENTER);
    }

    @Test
    public void encodeVendor10() {
        act = createAction(V_1_0, EXPERIMENTER, HP_LABS, EXP_EXP_DATA);
        verifyActionEncoding(V_1_0, act, EXPERIMENTER);
    }

    @Test
    public void encodeSetFieldEthDst13() {
        mf = FieldFactory.createBasicField(V_1_3, ETH_DST, MAC);
        act = createAction(V_1_3, SET_FIELD, mf);
        verifyActionEncoding(V_1_3, act, SET_FIELD, "EthDst");

        // alternately:
        act = ActionFactory.createActionSetField(V_1_3, ETH_DST, MAC);
        verifyActionEncoding(V_1_3, act, SET_FIELD, "EthDst");
    }

    @Test
    public void encodeSetFieldIpv4Src13() {
        mf = FieldFactory.createBasicField(V_1_3, IPV4_SRC, IPv4);
        act = createAction(V_1_3, SET_FIELD, mf);
        verifyActionEncoding(V_1_3, act, SET_FIELD, "Ipv4Src");

        // alternately:
        act = ActionFactory.createActionSetField(V_1_3, IPV4_SRC, IPv4);
        verifyActionEncoding(V_1_3, act, SET_FIELD, "Ipv4Src");
    }

    // === v1.0 set-field tests

    @Test
    public void encodeSetVlanId10() {
        act = ActionFactory.createActionSetField(V_1_0, VLAN_VID, EXP_VLAN_VID);
        verifyActionEncoding(V_1_0, act, OldActionType.SET_VLAN_VID);
    }

    @Test
    public void encodeSetVlanPcp10() {
        act = ActionFactory.createActionSetField(V_1_0, VLAN_PCP, EXP_PRIORITY);
        verifyActionEncoding(V_1_0, act, OldActionType.SET_VLAN_PCP);
    }

    @Test
    public void encodeSetDlSrc10() {
        act = ActionFactory.createActionSetField(V_1_0, ETH_SRC, MAC);
        verifyActionEncoding(V_1_0, act, OldActionType.SET_DL_SRC);
    }

    @Test
    public void encodeSetDlDst10() {
        act = ActionFactory.createActionSetField(V_1_0, ETH_DST, MAC);
        verifyActionEncoding(V_1_0, act, OldActionType.SET_DL_DST);
    }

    @Test
    public void encodeSetNwSrc10() {
        act = ActionFactory.createActionSetField(V_1_0, IPV4_SRC, IPv4);
        verifyActionEncoding(V_1_0, act, OldActionType.SET_NW_SRC);
    }

    @Test
    public void encodeSetNwDst10() {
        act = ActionFactory.createActionSetField(V_1_0, IPV4_DST, IPv4);
        verifyActionEncoding(V_1_0, act, OldActionType.SET_NW_DST);
    }

    @Test
    public void encodeSetNwTos10() {
        act = ActionFactory.createActionSetField(V_1_0, IP_DSCP, EXP_TOS);
        verifyActionEncoding(V_1_0, act, OldActionType.SET_NW_TOS);
    }

    @Test
    public void encodeSetTpSrc10() {
        // NOTE: TCP, UDP, SCTP should all map to the same thing
        act = ActionFactory.createActionSetField(V_1_0, TCP_SRC, EXP_SM_PORT);
        verifyActionEncoding(V_1_0, act, OldActionType.SET_TP_SRC);

        act = ActionFactory.createActionSetField(V_1_0, UDP_SRC, EXP_SM_PORT);
        verifyActionEncoding(V_1_0, act, OldActionType.SET_TP_SRC);

        act = ActionFactory.createActionSetField(V_1_0, SCTP_SRC, EXP_SM_PORT);
        verifyActionEncoding(V_1_0, act, OldActionType.SET_TP_SRC);
    }

    @Test
    public void encodeSetTpDst10() {
        // NOTE: TCP, UDP, SCTP should all map to the same thing
        act = ActionFactory.createActionSetField(V_1_0, TCP_DST, EXP_SM_PORT);
        verifyActionEncoding(V_1_0, act, OldActionType.SET_TP_DST);

        act = ActionFactory.createActionSetField(V_1_0, UDP_DST, EXP_SM_PORT);
        verifyActionEncoding(V_1_0, act, OldActionType.SET_TP_DST);

        act = ActionFactory.createActionSetField(V_1_0, SCTP_DST, EXP_SM_PORT);
        verifyActionEncoding(V_1_0, act, OldActionType.SET_TP_DST);
    }

    //======


    @Test
    public void createHeaders() {
        print(TestTools.EOL + "createHeaders()");
        final ProtocolVersion pv = V_1_3;
        Set<ActionType> types = EnumSet.of(OUTPUT, DEC_MPLS_TTL, SET_QUEUE);

        List<Action> acts = ActionFactory.createActionHeaders(pv, types);
        for (Action a: acts)
            print(a.toDebugString());
        assertEquals(AM_UXS, 3, acts.size());
        for (Action a: acts) {
            assertTrue(AM_WRCL, ActHeader.class.isInstance(a));
            assertEquals(AM_NEQ, 4, a.getTotalLength());
        }
    }
}
