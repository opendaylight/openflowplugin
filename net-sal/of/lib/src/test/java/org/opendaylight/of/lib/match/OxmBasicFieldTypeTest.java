/*
 * (c) Copyright 2012 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.of.lib.match;

import junit.framework.Assert;
import org.junit.Test;
import org.opendaylight.of.lib.AbstractTest;
import org.opendaylight.of.lib.DecodeException;
import org.opendaylight.of.lib.ProtocolVersion;
import org.opendaylight.of.lib.VersionMismatchException;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.opendaylight.of.lib.ProtocolVersion.*;
import static org.opendaylight.of.lib.match.OxmBasicFieldType.*;
import static org.opendaylight.util.junit.TestTools.*;

/**
 * Unit tests for OxmBasicField.
 *
 * @author Simon Hunt
 */
public class OxmBasicFieldTypeTest extends AbstractTest {

    @Test
    public void basic() {
        print(EOL + "basic()");
        for (OxmBasicFieldType field: OxmBasicFieldType.values())
            print(field);
        Assert.assertEquals(AM_UXCC, 40, OxmBasicFieldType.values().length);
    }

    private static final int[] UNKNOWN = { -1, 40, 2000, 0xffff };

    @Test
    public void unknownCodes() {
        print(EOL + "unknownCodes()");
        for (ProtocolVersion pv: PV_0123) {
            print(pv);
            for (int code: UNKNOWN) {
                try {
                    OxmBasicFieldType.decode(code, pv);
                    fail(AM_NOEX);
                } catch (DecodeException e) {
                    print(FMT_EX, e);
                }
            }
        }
    }

    private void verifyCode(int code, ProtocolVersion pv,
                            OxmBasicFieldType exp) {
        try {
            OxmBasicFieldType field = OxmBasicFieldType.decode(code, pv);
            print(FMT_PV_CODE_ENUM, pv, code, field);
            if (exp == null)
                fail(AM_NOEX);
            assertEquals(AM_NEQ, exp, field);
        } catch (VersionMismatchException vme) {
            print(FMT_EX + vme);
            if (exp != null)
                fail(AM_UNEX_MISMATCH);
        } catch (DecodeException e) {
            if (exp == null)
                print(FMT_EX, e);
            else
                fail(AM_WREX);

        }
    }

    private static final int CODE_MIN = 0;
    private static final int CODE_MAX = 39;

    @Test
    public void codesV0() {
        print(EOL + "codesV0()");
        for (int code = CODE_MIN; code <= CODE_MAX; code++)
            verifyCode(code, V_1_0, null);
    }

    @Test
    public void codesV1() {
        print(EOL + "codesV1()");
        for (int code = CODE_MIN; code <= CODE_MAX; code++)
            verifyCode(code, V_1_1, null);
    }

    @Test
    public void codesV2() {
        print(EOL + "codesV2()");
        verifyCode(0, V_1_2, IN_PORT);
        verifyCode(1, V_1_2, IN_PHY_PORT);
        verifyCode(2, V_1_2, METADATA);
        verifyCode(3, V_1_2, ETH_DST);
        verifyCode(4, V_1_2, ETH_SRC);
        verifyCode(5, V_1_2, ETH_TYPE);
        verifyCode(6, V_1_2, VLAN_VID);
        verifyCode(7, V_1_2, VLAN_PCP);
        verifyCode(8, V_1_2, IP_DSCP);
        verifyCode(9, V_1_2, IP_ECN);
        verifyCode(10, V_1_2, IP_PROTO);
        verifyCode(11, V_1_2, IPV4_SRC);
        verifyCode(12, V_1_2, IPV4_DST);
        verifyCode(13, V_1_2, TCP_SRC);
        verifyCode(14, V_1_2, TCP_DST);
        verifyCode(15, V_1_2, UDP_SRC);
        verifyCode(16, V_1_2, UDP_DST);
        verifyCode(17, V_1_2, SCTP_SRC);
        verifyCode(18, V_1_2, SCTP_DST);
        verifyCode(19, V_1_2, ICMPV4_TYPE);
        verifyCode(20, V_1_2, ICMPV4_CODE);
        verifyCode(21, V_1_2, ARP_OP);
        verifyCode(22, V_1_2, ARP_SPA);
        verifyCode(23, V_1_2, ARP_TPA);
        verifyCode(24, V_1_2, ARP_SHA);
        verifyCode(25, V_1_2, ARP_THA);
        verifyCode(26, V_1_2, IPV6_SRC);
        verifyCode(27, V_1_2, IPV6_DST);
        verifyCode(28, V_1_2, IPV6_FLABEL);
        verifyCode(29, V_1_2, ICMPV6_TYPE);
        verifyCode(30, V_1_2, ICMPV6_CODE);
        verifyCode(31, V_1_2, IPV6_ND_TARGET);
        verifyCode(32, V_1_2, IPV6_ND_SLL);
        verifyCode(33, V_1_2, IPV6_ND_TLL);
        verifyCode(34, V_1_2, MPLS_LABEL);
        verifyCode(35, V_1_2, MPLS_TC);
        verifyCode(36, V_1_2, null);
        verifyCode(37, V_1_2, null);
        verifyCode(38, V_1_2, null);
        verifyCode(39, V_1_2, null);
    }

    @Test
    public void codesV3() {
        print(EOL + "codesV3()");
        verifyCode(0, V_1_3, IN_PORT);
        verifyCode(1, V_1_3, IN_PHY_PORT);
        verifyCode(2, V_1_3, METADATA);
        verifyCode(3, V_1_3, ETH_DST);
        verifyCode(4, V_1_3, ETH_SRC);
        verifyCode(5, V_1_3, ETH_TYPE);
        verifyCode(6, V_1_3, VLAN_VID);
        verifyCode(7, V_1_3, VLAN_PCP);
        verifyCode(8, V_1_3, IP_DSCP);
        verifyCode(9, V_1_3, IP_ECN);
        verifyCode(10, V_1_3, IP_PROTO);
        verifyCode(11, V_1_3, IPV4_SRC);
        verifyCode(12, V_1_3, IPV4_DST);
        verifyCode(13, V_1_3, TCP_SRC);
        verifyCode(14, V_1_3, TCP_DST);
        verifyCode(15, V_1_3, UDP_SRC);
        verifyCode(16, V_1_3, UDP_DST);
        verifyCode(17, V_1_3, SCTP_SRC);
        verifyCode(18, V_1_3, SCTP_DST);
        verifyCode(19, V_1_3, ICMPV4_TYPE);
        verifyCode(20, V_1_3, ICMPV4_CODE);
        verifyCode(21, V_1_3, ARP_OP);
        verifyCode(22, V_1_3, ARP_SPA);
        verifyCode(23, V_1_3, ARP_TPA);
        verifyCode(24, V_1_3, ARP_SHA);
        verifyCode(25, V_1_3, ARP_THA);
        verifyCode(26, V_1_3, IPV6_SRC);
        verifyCode(27, V_1_3, IPV6_DST);
        verifyCode(28, V_1_3, IPV6_FLABEL);
        verifyCode(29, V_1_3, ICMPV6_TYPE);
        verifyCode(30, V_1_3, ICMPV6_CODE);
        verifyCode(31, V_1_3, IPV6_ND_TARGET);
        verifyCode(32, V_1_3, IPV6_ND_SLL);
        verifyCode(33, V_1_3, IPV6_ND_TLL);
        verifyCode(34, V_1_3, MPLS_LABEL);
        verifyCode(35, V_1_3, MPLS_TC);
        verifyCode(36, V_1_3, MPLS_BOS);
        verifyCode(37, V_1_3, PBB_ISID);
        verifyCode(38, V_1_3, TUNNEL_ID);
        verifyCode(39, V_1_3, IPV6_EXTHDR);
    }

    private static final OxmBasicFieldType[] REQUIRED = {
            IN_PORT, ETH_DST, ETH_SRC, ETH_TYPE, IP_PROTO,
            IPV4_SRC, IPV4_DST, IPV6_SRC, IPV6_DST,
            TCP_SRC, TCP_DST, UDP_SRC, UDP_DST,
    };
    private static final Set<OxmBasicFieldType> REQUIRED_SET =
            new HashSet<OxmBasicFieldType>(Arrays.asList(REQUIRED));

    @Test
    public void required() {
        print(EOL + "required()");
        for (OxmBasicFieldType field: OxmBasicFieldType.values()) {
            print(field + (field.isRequired() ? " (Required)" : ""));
            assertEquals("required mismatch", REQUIRED_SET.contains(field),
                    field.isRequired());
        }
    }

    private static final int[] PAYLOADS = {
            4,  // IN_PORT : port number
            4,  // IN_PHY_PORT : port number
            8,  // METADATA : u64
            6,  // ETH_DST : MAC address
            6,  // ETH_SRC : MAC address
            2,  // ETH_TYPE : u16
            2,  // VLAN_ID : u12 + 1    (u16)
            1,  // VLAN_PCP : u3        (u8)
            1,  // IP_DSCP : u6         (u8)
            1,  // IP_ECN : u2          (u8)
            1,  // IP_PROTO : u8
            4,  // IPV4_SRC : IPv4 address
            4,  // IPV4_DST : IPv4 address
            2,  // TCP_SRC : port number
            2,  // TCP_DST : port number
            2,  // UDP_SRC : port number
            2,  // UDP_DST : port number
            2,  // SCTP_SRC : port number
            2,  // SCTP_DST : port number
            1,  // ICMPV4_TYPE : u8
            1,  // ICMPV4_CODE : u8
            2,  // ARP_OP : u16
            4,  // ARP_SPA : IPv4 address
            4,  // ARP_TPA : IPv4 address
            6,  // ARP_SHA : MAC address
            6,  // ARP_THA : MAC address
            16, // IPV6_SRC : IPv6 address
            16, // IPV6_DST : IPv6 address
            3,  // IPV6_FLABEL : u20    (u24)   // FIXME: 3 or 4?
            1,  // ICMPV6_TYPE : u8
            1,  // ICMPV6_CODE : u8
            16, // IPV6_ND_TARGET : IPv6 address
            6,  // IPV6_ND_SLL : MAC address
            6,  // IPV6_ND_TLL : MAC address
            3,  // MPLS_LABEL : u20     (u32)   // FIXME: 3 or 4?
            1,  // MPLS_TC : u3         (u8)
            1,  // MPLS_BOS : u1        (u8)
            3,  // PBB_ISID : u24               // FIXME: 3 or 4?
            8,  // TUNNEL_ID : u64
            2,  // IPV6_EXTHDR : u9     (u16)
    };

    @Test
    public void payloadLengths() throws DecodeException {
        print(EOL + "payloadLengths()");
        // sanity - check we hit all the constants...
        Set<OxmBasicFieldType> seen = new HashSet<OxmBasicFieldType>();
        for (int code = CODE_MIN; code <= CODE_MAX; code++) {
            int expPayload = PAYLOADS[code];
            OxmBasicFieldType field = OxmBasicFieldType.decode(code, V_1_3);
            seen.add(field);
            print("{} -> {}", field, field.expectedLength(false));
            assertEquals(AM_NEQ, expPayload, field.expectedLength(false));
            assertEquals(AM_NEQ, expPayload*2, field.expectedLength(true));
        }
        assertEquals(AM_UXCC, OxmBasicFieldType.values().length, seen.size());
    }

    private static final OxmBasicFieldType[] MASKABLE = {
            METADATA, ETH_DST, ETH_SRC, VLAN_VID, IPV4_SRC, IPV4_DST,
            ARP_SPA, ARP_TPA, ARP_SHA, ARP_THA, IPV6_SRC, IPV6_DST,
            IPV6_FLABEL, PBB_ISID, TUNNEL_ID, IPV6_EXTHDR,
    };
    private static final Set<OxmBasicFieldType> MASKABLE_SET =
            new HashSet<OxmBasicFieldType>(Arrays.asList(MASKABLE));

    @Test
    public void maskable() {
        print(EOL + "maskable()");
        for (OxmBasicFieldType ft: OxmBasicFieldType.values()) {
            boolean m = ft.isMaskable();
            print("{} maskable -> {}", ft, m);
            boolean expMaskable = MASKABLE_SET.contains(ft);
            assertEquals(AM_NEQ, expMaskable, m);
        }
    }

}
