/*
 * (c) Copyright 2012 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.of.lib.msg;

import org.junit.Test;
import org.opendaylight.of.lib.ProtocolVersion;
import org.opendaylight.of.lib.VersionMismatchException;
import org.opendaylight.of.lib.VersionNotSupportedException;
import org.opendaylight.of.lib.dt.DataPathId;
import org.opendaylight.util.net.BigPortNumber;
import org.opendaylight.util.net.MacAddress;

import java.util.*;

import static java.util.Arrays.asList;
import static org.junit.Assert.*;
import static org.opendaylight.of.lib.ProtocolVersion.*;
import static org.opendaylight.of.lib.msg.Capability.*;
import static org.opendaylight.of.lib.msg.MessageType.FEATURES_REPLY;
import static org.opendaylight.of.lib.msg.PortConfig.*;
import static org.opendaylight.of.lib.msg.PortFactory.LIB_PORT_V0;
import static org.opendaylight.of.lib.msg.PortFeature.*;
import static org.opendaylight.of.lib.msg.PortState.LINK_DOWN;
import static org.opendaylight.of.lib.msg.PortState.STP_LISTEN;
import static org.opendaylight.of.lib.msg.SupportedAction.*;
import static org.opendaylight.util.junit.TestTools.*;

/**
 * Unit test for the OfmFeaturesReply message.
 *
 * @author Simon Hunt
 */
public class OfmFeaturesReplyTest extends OfmTest {

    // test files
    private static final String TF_FREP_13 = "v13/featuresReply";
    private static final String TF_FREP_12 = "v12/featuresReply";
    private static final String TF_FREP_11 = "v11/featuresReply";
    private static final String TF_FREP_10 = "v10/featuresReply";

    private static final int MLEN_FIXED = MessageFactory.LIB_FEATURES_REPLY;
    private static final int MLEN_13 = MLEN_FIXED;
//    private static final int MLEN_12 = MLEN_FIXED + LIB_PORT_V123 * 2;
//    private static final int MLEN_11 = MLEN_FIXED + LIB_PORT_V123 * 2;
    private static final int MLEN_10 = MLEN_FIXED + LIB_PORT_V0 * 2;

    private static final DataPathId EXP_DPID =
            DataPathId.valueOf("45067/00:00:36:00:65:02");
    private static final int EXP_NBUFF = 256;
    private static final int EXP_NTAB = 20;
    private static final int EXP_AUX = 0;
    private static final Capability[] EXP_CAP_FLAGS_13 = {
            FLOW_STATS, TABLE_STATS, PORT_STATS,
            GROUP_STATS, IP_REASM, QUEUE_STATS, PORT_BLOCKED
    };
    private static final Set<Capability> EXP_CAP_SET_13 =
            new HashSet<Capability>(asList(EXP_CAP_FLAGS_13));

    private static final Capability[] EXP_CAP_FLAGS_10 = {
            FLOW_STATS, TABLE_STATS, PORT_STATS,
            STP, IP_REASM, QUEUE_STATS, ARP_MATCH_IP
    };
    private static final Set<Capability> EXP_CAP_SET_10 =
            new HashSet<Capability>(asList(EXP_CAP_FLAGS_10));

//    private static final Capabilities[] EXP_CAP_FLAGS_11 = {
//            FLOW_STATS, TABLE_STATS, PORT_STATS,
//            GROUP_STATS, IP_REASM, QUEUE_STATS, ARP_MATCH_IP
//    };
//    private static final Set<Capabilities> EXP_CAP_SET_11 =
//            new HashSet<Capabilities>(asList(EXP_CAP_FLAGS_11));
//
//    private static final Capabilities[] EXP_CAP_FLAGS_12 = {
//            FLOW_STATS, TABLE_STATS, PORT_STATS,
//            GROUP_STATS, IP_REASM, QUEUE_STATS, PORT_BLOCKED
//    };
//    private static final Set<Capabilities> EXP_CAP_SET_12 =
//            new HashSet<Capabilities>(asList(EXP_CAP_FLAGS_12));

    private static final SupportedAction[] EXP_SUP_ACT_10 = {
           OUTPUT, SET_VLAN_VID, STRIP_VLAN, SET_DL_DST, SET_NW_SRC,
           SET_NW_TOS, ENQUEUE,
    };
    private static final Set<SupportedAction> EXP_SUP_ACT_SET_10 =
            new HashSet<SupportedAction>(asList(EXP_SUP_ACT_10));

    private static final BigPortNumber EXP_PNUM_0 = bpn(1);
    private static final MacAddress EXP_MAC_0 = mac("114477:010101");
    private static final String EXP_NAME_0 = "One";

    private static final BigPortNumber EXP_PNUM_1 = bpn(2);
    private static final MacAddress EXP_MAC_1 = mac("114477:020202");
    private static final String EXP_NAME_1 = "Two";

    private static final PortState[] EXP_STATE = {LINK_DOWN, STP_LISTEN};
    private static final Set<PortState> EXP_STATE_SET =
            new TreeSet<PortState>(Arrays.asList(EXP_STATE));
    private static final PortConfig[] EXP_CFG = {NO_RECV, NO_FLOOD, NO_FWD};
    private static final Set<PortConfig> EXP_CFG_SET =
            new TreeSet<PortConfig>(Arrays.asList(EXP_CFG));
    private static final PortFeature[] EXP_CURR = {RATE_1GB_FD, FIBER, AUTONEG};
    private static final Set<PortFeature> EXP_CURR_SET =
            new TreeSet<PortFeature>(Arrays.asList(EXP_CURR));
    private static final PortFeature[] EXP_ADV = {RATE_1GB_FD, FIBER};
    private static final Set<PortFeature> EXP_ADV_SET =
            new TreeSet<PortFeature>(Arrays.asList(EXP_ADV));
    private static final PortFeature[] EXP_SUPP = {RATE_1GB_FD, FIBER, AUTONEG};
    private static final Set<PortFeature> EXP_SUPP_SET =
            new TreeSet<PortFeature>(Arrays.asList(EXP_SUPP));
    private static final PortFeature[] EXP_PEER = {RATE_100MB_FD};
    private static final Set<PortFeature> EXP_PEER_SET =
            new TreeSet<PortFeature>(Arrays.asList(EXP_PEER));
    private static final long EXP_123_CURR_SPEED = 1100000;
    private static final long EXP_123_MAX_SPEED = 3000000;


    private static final PortConfig[] EXP_CFG_11 = {NO_RECV, NO_FWD};
    private static final Set<PortConfig> EXP_CFG_SET_11_12 =
            new TreeSet<PortConfig>(Arrays.asList(EXP_CFG_11));
    private static final PortState[] EXP_STATE_11 = {LINK_DOWN};
    private static final Set<PortState> EXP_STATE_SET_11_12 =
            new TreeSet<PortState>(Arrays.asList(EXP_STATE_11));

    private MutableMessage mm;

    // ========================================================= PARSING ====


    @Test
    public void featuresReply13() {
        print(EOL + "featuresReply13()");
        OfmFeaturesReply msg =
                (OfmFeaturesReply) verifyMsgHeader(TF_FREP_13, V_1_3,
                        FEATURES_REPLY, MLEN_13);

        assertEquals(AM_NEQ, EXP_DPID, msg.getDpid());
        assertEquals(AM_NEQ, EXP_NBUFF, msg.getNumBuffers());
        assertEquals(AM_NEQ, EXP_NTAB, msg.getNumTables());
        assertEquals(AM_NEQ, EXP_AUX, msg.getAuxId());
        verifyFlags(msg.getCapabilities(), EXP_CAP_FLAGS_13);

        try {
            msg.getSupportedActions();
            fail(AM_NOEX);
        } catch (VersionMismatchException vme) {
            print(FMT_EX, vme);
        } catch (Exception e) {
            print(e);
            fail(AM_WREX);
        }

        try {
            msg.getPorts();
            fail(AM_NOEX);
        } catch (VersionMismatchException vme) {
            print(FMT_EX, vme);
        } catch (Exception e) {
            print(e);
            fail(AM_WREX);
        }
    }

    @Test
    public void featuresReply12() {
        print(EOL + "featuresReply12()");
        verifyNotSupported(TF_FREP_12);
//        OfmFeaturesReply msg =
//                (OfmFeaturesReply) verifyMsgHeader(TF_FREP_12, V_1_2,
//                        FEATURES_REPLY, MLEN_12);
//
//        assertEquals(AM_NEQ, EXP_DPID, msg.getDpid());
//        assertEquals(AM_NEQ, EXP_NBUFF, msg.getNumBuffers());
//        assertEquals(AM_NEQ, EXP_NTAB, msg.getNumTables());
//        assertEquals(AM_NEQ, 0, msg.getAuxId()); // n/a in 1.2
//        verifyFlags(msg.getCapabilities(), EXP_CAP_FLAGS_12);
//
//        try {
//            msg.getSupportedActions();
//            fail(AM_NOEX);
//        } catch (VersionMismatchException vme) {
//            print(FMT_EX, vme);
//        } catch (Exception e) {
//            print(e);
//            fail(AM_WREX);
//        }
//
//        Iterator<Port> pIt = msg.getPorts().iterator();
//        verifyPort(pIt.next(), EXP_PNUM_0, EXP_MAC_0, EXP_NAME_0,
//                EXP_CFG_SET_11_12, EXP_STATE_SET_11_12,
//                EXP_CURR_SET, EXP_ADV_SET, EXP_SUPP_SET, EXP_PEER_SET,
//                EXP_123_CURR_SPEED, EXP_123_MAX_SPEED);
//        verifyPort(pIt.next(), EXP_PNUM_1, EXP_MAC_1, EXP_NAME_1,
//                EXP_CFG_SET_11_12, EXP_STATE_SET_11_12,
//                EXP_CURR_SET, EXP_ADV_SET, EXP_SUPP_SET, EXP_PEER_SET,
//                EXP_123_CURR_SPEED, EXP_123_MAX_SPEED);
//        assertFalse(AM_HUH, pIt.hasNext());
    }


    @Test
    public void featuresReply11() {
        print(EOL + "featuresReply11()");
        verifyNotSupported(TF_FREP_11);
//        OfmFeaturesReply msg =
//                (OfmFeaturesReply) verifyMsgHeader(TF_FREP_11, V_1_1,
//                        FEATURES_REPLY, MLEN_11);
//
//        assertEquals(AM_NEQ, EXP_DPID, msg.getDpid());
//        assertEquals(AM_NEQ, EXP_NBUFF, msg.getNumBuffers());
//        assertEquals(AM_NEQ, EXP_NTAB, msg.getNumTables());
//        assertEquals(AM_NEQ, 0, msg.getAuxId()); // n/a in 1.1
//        verifyFlags(msg.getCapabilities(), EXP_CAP_FLAGS_11);
//
//        try {
//            msg.getSupportedActions();
//            fail(AM_NOEX);
//        } catch (VersionMismatchException vme) {
//            print(FMT_EX, vme);
//        } catch (Exception e) {
//            print(e);
//            fail(AM_WREX);
//        }
//
//        Iterator<Port> pIt = msg.getPorts().iterator();
//        verifyPort(pIt.next(), EXP_PNUM_0, EXP_MAC_0, EXP_NAME_0,
//                EXP_CFG_SET_11_12, EXP_STATE_SET_11_12,
//                EXP_CURR_SET, EXP_ADV_SET, EXP_SUPP_SET, EXP_PEER_SET,
//                EXP_123_CURR_SPEED, EXP_123_MAX_SPEED);
//        verifyPort(pIt.next(), EXP_PNUM_1, EXP_MAC_1, EXP_NAME_1,
//                EXP_CFG_SET_11_12, EXP_STATE_SET_11_12,
//                EXP_CURR_SET, EXP_ADV_SET, EXP_SUPP_SET, EXP_PEER_SET,
//                EXP_123_CURR_SPEED, EXP_123_MAX_SPEED);
//        assertFalse(AM_HUH, pIt.hasNext());
    }


    @Test
    public void featuresReply10() {
        print(EOL + "featuresReply10()");
        OfmFeaturesReply msg =
                (OfmFeaturesReply) verifyMsgHeader(TF_FREP_10, V_1_0,
                        FEATURES_REPLY, MLEN_10);

        assertEquals(AM_NEQ, EXP_DPID, msg.getDpid());
        assertEquals(AM_NEQ, EXP_NBUFF, msg.getNumBuffers());
        assertEquals(AM_NEQ, EXP_NTAB, msg.getNumTables());
        assertEquals(AM_NEQ, 0, msg.getAuxId()); // n/a in 1.0
        verifyFlags(msg.getCapabilities(), EXP_CAP_FLAGS_10);
        verifyFlags(msg.getSupportedActions(), EXP_SUP_ACT_10);

        Iterator<Port> pIt = msg.getPorts().iterator();
        verifyPort(pIt.next(), EXP_PNUM_0, EXP_MAC_0, EXP_NAME_0,
                EXP_CFG_SET, EXP_STATE_SET,
                EXP_CURR_SET, EXP_ADV_SET, EXP_SUPP_SET, EXP_PEER_SET);
        verifyPort(pIt.next(), EXP_PNUM_1, EXP_MAC_1, EXP_NAME_1,
                EXP_CFG_SET, EXP_STATE_SET,
                EXP_CURR_SET, EXP_ADV_SET, EXP_SUPP_SET, EXP_PEER_SET);
        assertFalse(AM_HUH, pIt.hasNext());
    }



    // ============================================= CREATING / ENCODING ====

    @Test
    public void encodeFeaturesReply13() {
        print(EOL + "encodeFeaturesReply13()");
        mm = MessageFactory.create(V_1_3, FEATURES_REPLY);
        mm.clearXid();
        verifyMutableHeader(mm, V_1_3, FEATURES_REPLY, 0);
        // assemble the pieces
        OfmMutableFeaturesReply rep = (OfmMutableFeaturesReply) mm;
        rep.dpid(EXP_DPID).numBuffers(EXP_NBUFF).numTables(EXP_NTAB)
                .auxId(EXP_AUX).capabilities(EXP_CAP_SET_13);

        // verify exceptions for unwarranted methods
        try {
            rep.supportedActions(null);
            fail(AM_NOEX);
        } catch (VersionMismatchException vme) {
            print(FMT_EX, vme);
        } catch (Exception e) {
            print(e);
            fail(AM_WREX);
        }
        try {
            rep.addPort(null);
            fail(AM_NOEX);
        } catch (VersionMismatchException vme) {
            print(FMT_EX, vme);
        } catch (Exception e) {
            print(e);
            fail(AM_WREX);
        }

        encodeAndVerifyMessage(mm.toImmutable(), TF_FREP_13);
    }

    @Test(expected = VersionNotSupportedException.class)
    public void encodeFeaturesReply12() {
        mm = MessageFactory.create(V_1_2, FEATURES_REPLY);
    }

    @Test(expected = VersionNotSupportedException.class)
    public void encodeFeaturesReply11() {
        mm = MessageFactory.create(V_1_1, FEATURES_REPLY);
    }

    @Test
    public void encodeFeaturesReply10() {
        print(EOL + "encodeFeaturesReply10()");
        final ProtocolVersion pv = V_1_0;
        mm = MessageFactory.create(pv, FEATURES_REPLY);
        mm.clearXid();
        verifyMutableHeader(mm, V_1_0, FEATURES_REPLY, 0);
        // assemble the pieces
        OfmMutableFeaturesReply rep = (OfmMutableFeaturesReply) mm;
        rep.dpid(EXP_DPID).numBuffers(EXP_NBUFF).numTables(EXP_NTAB)
                .capabilities(EXP_CAP_SET_10)
                .supportedActions(EXP_SUP_ACT_SET_10)
                .addPort(createPortOne(pv)).addPort(createPortTwo(pv));

        // verify exceptions for unwarranted methods
        try {
            rep.auxId(EXP_AUX);
        } catch (VersionMismatchException vme) {
            print(FMT_EX, vme);
        } catch (Exception e) {
            print(e);
            fail(AM_WREX);
        }

        encodeAndVerifyMessage(mm.toImmutable(), TF_FREP_10);
    }

    private Port createPortOne(ProtocolVersion pv) {
        Set<PortConfig> cfg = pv == V_1_0 ? EXP_CFG_SET : EXP_CFG_SET_11_12;
        Set<PortState> state = pv == V_1_0 ? EXP_STATE_SET : EXP_STATE_SET_11_12;
        return createPort(pv, EXP_PNUM_0, EXP_MAC_0, EXP_NAME_0, cfg, state,
                EXP_CURR_SET, EXP_ADV_SET, EXP_SUPP_SET, EXP_PEER_SET,
                EXP_123_CURR_SPEED, EXP_123_MAX_SPEED);
    }

    private Port createPortTwo(ProtocolVersion pv) {
        Set<PortConfig> cfg = pv == V_1_0 ? EXP_CFG_SET : EXP_CFG_SET_11_12;
        Set<PortState> state = pv == V_1_0 ? EXP_STATE_SET : EXP_STATE_SET_11_12;
        return createPort(pv, EXP_PNUM_1, EXP_MAC_1, EXP_NAME_1, cfg, state,
                EXP_CURR_SET, EXP_ADV_SET, EXP_SUPP_SET, EXP_PEER_SET,
                EXP_123_CURR_SPEED, EXP_123_MAX_SPEED);
    }

    private Port createPort(ProtocolVersion pv,
                            BigPortNumber pnum, MacAddress hw, String name,
                            Set<PortConfig> config, Set<PortState> state,
                            Set<PortFeature> curr, Set<PortFeature> adv,
                            Set<PortFeature> supp, Set<PortFeature> peer,
                            long cSpeed, long mSpeed) {
        MutablePort p = PortFactory.createPort(pv).portNumber(pnum)
                .hwAddress(hw).name(name).config(config).state(state)
                .current(curr).advertised(adv).supported(supp).peer(peer);
        if (pv.ge(V_1_1)) {
            p.currentSpeed(cSpeed).maxSpeed(mSpeed);
        }
        return (Port) p.toImmutable();
    }

}
