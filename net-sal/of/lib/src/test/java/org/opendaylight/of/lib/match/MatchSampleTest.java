/*
 * (c) Copyright 2012-2014 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.of.lib.match;

import org.junit.Test;
import org.opendaylight.of.lib.*;
import org.opendaylight.of.lib.msg.OfmTest;
import org.opendaylight.util.net.*;

import java.util.Iterator;

import static org.junit.Assert.*;
import static org.opendaylight.of.lib.ProtocolVersion.*;
import static org.opendaylight.of.lib.match.FieldFactory.createBasicField;
import static org.opendaylight.of.lib.match.OxmBasicFieldType.*;
import static org.opendaylight.util.junit.TestTools.*;

/**
 * Unit tests for parsing, creating and encoding sample Match structures.
 *
 * @author Simon Hunt
 */
public class MatchSampleTest extends OfmTest {
    private static final String E_PARSE_FAIL = "failed to parse match struct: ";
    private static final String E_VALIDATE_FAIL = "failed to validate match: ";

    private static final String FILE_PREFIX = "match/";

    private static final String EVERYTHING_13 = "v13/matchEverything";
    private static final String SAMPLE_1_13 = "v13/matchSample1";
    private static final String SAMPLE_1_10 = "v10/matchSample1";
    private static final String SAMPLE_2_10 = "v10/matchSample2";

    private OfPacketReader getPkt(String basename) {
        return getPacketReader(FILE_PREFIX + basename + HEX);
    }

    private byte[] getExpBytes(String filename) {
        return getExpByteArray("../" + FILE_PREFIX + filename);
    }

    // === Expected values....

    private static final OxmBasicFieldType EXP_MF_T1 = ETH_SRC;
    private static final MacAddress EXP_MF_V1 = mac("11:22:33:44:55:66");
    private static final MacAddress EXP_MF_M1 = mac("ff:ff:ff:00:00:00");

    private static final OxmBasicFieldType EXP_MF_T2 = ETH_TYPE;
    private static final EthernetType EXP_MF_V2 = EthernetType.IPv4;

    private static final OxmBasicFieldType EXP_MF_T3 = IP_PROTO;
    private static final IpProtocol EXP_MF_V3 = IpProtocol.TCP;

    private static final OxmBasicFieldType EXP_MF_T4 = TCP_DST;
    private static final PortNumber EXP_MF_V4 = PortNumber.valueOf(25); // SMTP

    // ========================================================= PARSING ====

    @Test
    public void matchEverything13() {
        print(EOL + "matchEverything13()");
        OfPacketReader pkt = getPkt(EVERYTHING_13);
        try {
            Match m = MatchFactory.parseMatch(pkt, V_1_3);
            print(m.toDebugString());

            // see matchEverything.hex for expected values
            assertEquals(AM_NEQ, MatchType.OXM, m.getMatchType());
            assertEquals(AM_NEQ, 4, m.header.length); // no getter!
            assertEquals(AM_NEQ, 0, m.getMatchFields().size());

        } catch (MessageParseException mpe) {
            print(mpe);
            fail(E_PARSE_FAIL + mpe);
        }
        checkEOBuffer(pkt);
    }

    @Test
    public void matchOne13() {
        print(EOL + "matchOne13()");
        OfPacketReader pkt = getPkt(SAMPLE_1_13);
        try {
            Match m = MatchFactory.parseMatch(pkt, V_1_3);
            print(m.toDebugString());

            // see v13/matchSample1.hex for expected values
            assertEquals(AM_NEQ, MatchType.OXM, m.getMatchType());
            assertEquals(AM_NEQ, 37, m.header.length); // no getter!
            assertEquals(AM_NEQ, 4, m.getMatchFields().size());
            Iterator<MatchField> iter = m.getMatchFields().iterator();

            verifyMatchField(iter.next(), EXP_MF_T1, EXP_MF_V1, EXP_MF_M1);
            verifyMatchField(iter.next(), EXP_MF_T2, EXP_MF_V2);
            verifyMatchField(iter.next(), EXP_MF_T3, EXP_MF_V3);
            verifyMatchField(iter.next(), EXP_MF_T4, EXP_MF_V4);

        } catch (MessageParseException mpe) {
            print(mpe);
            fail(E_PARSE_FAIL + mpe);
        }
        checkEOBuffer(pkt);
    }

    @Test
    public void validateOne13() {
        print(EOL + "validateOne13()");
        OfPacketReader pkt = getPkt(SAMPLE_1_13);
        Match m = null;
        try {
            m = MatchFactory.parseMatch(pkt, V_1_3);
            print(m.toDebugString());
        } catch (MessageParseException mpe) {
            print(mpe);
            fail(E_PARSE_FAIL + mpe);
        }
        checkEOBuffer(pkt);

        try {
            MatchFactory.validatePreRequisites(m);
        } catch (Exception e) {
            print(e);
            fail(E_VALIDATE_FAIL);
        }
    }

    private static final BigPortNumber PORT_NUMBER = BigPortNumber.valueOf(14);
    private static final MacAddress MAC_1 = mac("00001e:111111");
    private static final MacAddress MAC_2 = mac("00001e:222222");
    private static final IpAddress IP_1 = ip("15.255.76.3");
    private static final IpAddress IP_M_1 = ip("255.255.255.0");
    private static final IpAddress IP_2 = ip("15.255.77.1");
    private static final IpAddress IP_M_2 = ip("255.240.0.0");
    private static final PortNumber P_SRC = pn(25);
    private static final PortNumber P_DST = pn(32001);

    @Test
    public void matchOne10() {
        print(EOL + "matchOne10()");
        OfPacketReader pkt = getPkt(SAMPLE_1_10);
        try {
            Match m = MatchFactory.parseMatch(pkt, V_1_0);
            print(m.toDebugString());

            // see v10/matchSample1.hex for expected values
            // NOTE that match fields will be returned in the order defined
            //  in OxmBasicFieldType enum...
            Iterator<MatchField> iter = m.getMatchFields().iterator();
            verifyMatchField(iter.next(), IN_PORT, PORT_NUMBER);
            verifyMatchField(iter.next(), ETH_DST, MAC_2);
            verifyMatchField(iter.next(), ETH_SRC, MAC_1);
            verifyMatchField(iter.next(), ETH_TYPE, EthernetType.IPv4);
            verifyMatchField(iter.next(), IP_DSCP, 5);
            verifyMatchField(iter.next(), IP_PROTO, IpProtocol.TCP);
            verifyMatchField(iter.next(), IPV4_SRC, IP_1, IP_M_1);
            verifyMatchField(iter.next(), IPV4_DST, IP_2, IP_M_2);
            verifyMatchField(iter.next(), TCP_SRC, P_SRC);
            verifyMatchField(iter.next(), TCP_DST, P_DST);
            assertFalse(AM_HUH, iter.hasNext());

        } catch (MessageParseException mpe) {
            print(mpe);
            fail(E_PARSE_FAIL + mpe);
        }
        checkEOBuffer(pkt);
    }

    @Test
    public void matchTwo10() {
        print(EOL + "matchTwo10()");
        OfPacketReader pkt = getPkt(SAMPLE_2_10);
        try {
            Match m = MatchFactory.parseMatch(pkt, V_1_0);
            print(m.toDebugString());

            // see v10/matchSample2.hex for expected values
            assertEquals(AM_UXS, 0, m.getMatchFields().size());

        } catch (MessageParseException mpe) {
            print(mpe);
            fail(E_PARSE_FAIL + mpe);
        }
        checkEOBuffer(pkt);
    }

    // ============================================= CREATING / ENCODING ====

    private void encodeAndVerifyMatch(String label, Match m, String fname) {
        print(m.toDebugString());
        byte[] expData = getExpBytes(fname);
        OfPacketWriter pkt = new OfPacketWriter(expData.length);
        MatchFactory.encodeMatch(m, pkt);
        byte[] encoded = pkt.array();
        debugPrint(label, expData, encoded);
        assertArrayEquals(AM_NEQ, expData, encoded);
    }

    @Test
    public void encodeMatchEverything13() {
        print(EOL + "encodeMatchEverything13()");
        MutableMatch mm = MatchFactory.createMatch(V_1_3);
        // since there are no actual match fields for an everything match,
        //   we should be done.
        encodeAndVerifyMatch("encodeMatchEverything",
                (Match) mm.toImmutable(), EVERYTHING_13);
    }

    @Test
    public void encodeSampleOne13() {
        print(EOL + "encodeSampleOne13()");
        MutableMatch mm = MatchFactory.createMatch(V_1_3)
            .addField(createBasicField(V_1_3, EXP_MF_T1, EXP_MF_V1, EXP_MF_M1))
            .addField(createBasicField(V_1_3, EXP_MF_T2, EXP_MF_V2))
            .addField(createBasicField(V_1_3, EXP_MF_T3, EXP_MF_V3))
            .addField(createBasicField(V_1_3, EXP_MF_T4, EXP_MF_V4));

        encodeAndVerifyMatch("encodeSampleOne13",
                (Match) mm.toImmutable(), SAMPLE_1_13);
    }

    @Test(expected = VersionNotSupportedException.class)
    public void encodeMatch12NotSupported() {
        MatchFactory.createMatch(V_1_2);
    }

    @Test(expected = VersionNotSupportedException.class)
    public void encodeMatch11NotSupported() {
        MatchFactory.createMatch(V_1_1);
    }

    @Test
    public void encodeSampleOne10() {
        print(EOL + "encodeSampleOne10()");
        MutableMatch mm = MatchFactory.createMatch(V_1_0)
            .addField(createBasicField(V_1_0, IN_PORT, PORT_NUMBER))
            .addField(createBasicField(V_1_0, ETH_SRC, MAC_1))
            .addField(createBasicField(V_1_0, ETH_DST, MAC_2))
            .addField(createBasicField(V_1_0, ETH_TYPE, EthernetType.IPv4))
            .addField(createBasicField(V_1_0, IP_DSCP, 5))
            .addField(createBasicField(V_1_0, IP_PROTO, IpProtocol.TCP))
            .addField(createBasicField(V_1_0, IPV4_SRC, IP_1, IP_M_1))
            .addField(createBasicField(V_1_0, IPV4_DST, IP_2, IP_M_2))
            .addField(createBasicField(V_1_0, TCP_SRC, P_SRC))
            .addField(createBasicField(V_1_0, TCP_DST, P_DST));

        encodeAndVerifyMatch("encodeSampleOne10",
                (Match) mm.toImmutable(), SAMPLE_1_10);
    }

    @Test
    public void encodeSampleTwo10() {
        print(EOL + "encodeSampleTwo10()");
        MutableMatch mm = MatchFactory.createMatch(V_1_0);

        encodeAndVerifyMatch("encodeSampleTwo10",
                (Match) mm.toImmutable(), SAMPLE_2_10);
    }

    @Test
    public void sampleForRavi() {
        print(EOL + "sampleForRavi()");
        final ProtocolVersion pv = V_1_0;
        MutableMatch mm = MatchFactory.createMatch(pv)
            .addField(createBasicField(pv, IN_PORT, bpn(20)))
            .addField(createBasicField(pv, ETH_SRC, mac("a2:c0:98:8e:ec:4a")))
            .addField(createBasicField(pv, ETH_DST, mac("a2:c0:98:8e:ec:4b")))
            .addField(createBasicField(pv, VLAN_VID, VlanId.valueOf(42)))
            .addField(createBasicField(pv, VLAN_PCP, 7))
            .addField(createBasicField(pv, ETH_TYPE, EthernetType.IPv4))
            .addField(createBasicField(pv, IP_DSCP, 2)) // ToS
            .addField(createBasicField(pv, IP_PROTO, IpProtocol.TCP))
            .addField(createBasicField(pv, IPV4_SRC, ip("15.255.127.35")))
            .addField(createBasicField(pv, IPV4_DST, ip("15.255.127.42")))
            .addField(createBasicField(pv, TCP_SRC, pn(24)))
            .addField(createBasicField(pv, TCP_DST, pn(26)));
        Match m = (Match) mm.toImmutable();
        print(m.toDebugString());
        assertEquals(AM_UXS, 12, m.getMatchFields().size());

        /*  (old JSON format)
        "in_port"     : "20",
        "dl_src"      : "a2:c0:98:8e:ec:4a",
        "dl_dst"      : "a2:c0:98:8e:ec:4b",
        "dl_vlan"     : "42",
        "dl_vlanpcp"  : "7",
        "dl_type"     : "IPv4",
        "nw_tos"      : "2",
        "nw_proto"    : "tcp",
        "nw_src"      : "15.255.127.35",
        "nw_dst"      : "15.255.127.42",
        "tp_src"      : "24",
        "tp_dst"      : "26"
        */
    }
}
