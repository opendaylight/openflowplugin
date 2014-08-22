/*
 * (c) Copyright 2009,2010 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.util.net;

import org.junit.Test;

import static org.opendaylight.util.junit.TestTools.*;
import static org.junit.Assert.*;

/**
 * This class implements JUnit tests for {@link IpProtocol}.
 *
 * @author Simon Hunt
 */
public class IpProtocolTest {

    private IpProtocol ipp;

    @Test
    public void tcp() {
        print(EOL + "tcp()");
        ipp = IpProtocol.valueOf("tcp");
        print(ipp);
        assertSame(AM_NSR, IpProtocol.TCP, ipp);
        ipp = IpProtocol.valueOf(6);
        print(ipp);
        assertSame(AM_NSR, IpProtocol.TCP, ipp);
    }

    @Test
    public void udp() {
        print(EOL + "udp()");
        ipp = IpProtocol.valueOf("udp");
        print(ipp);
        assertSame(AM_NSR, IpProtocol.UDP, ipp);
        ipp = IpProtocol.valueOf(17);
        print(ipp);
        assertSame(AM_NSR, IpProtocol.UDP, ipp);
    }

    @Test
    public void sctp() {
        print(EOL + "sctp()");
        ipp = IpProtocol.valueOf("sctp");
        print(ipp);
        assertSame(AM_NSR, IpProtocol.SCTP, ipp);
        ipp = IpProtocol.valueOf(132);
        print(ipp);
        assertSame(AM_NSR, IpProtocol.SCTP, ipp);
    }

    @Test
    public void icmp() {
        print(EOL + "icmp()");
        ipp = IpProtocol.valueOf("icmp");
        print(ipp);
        assertSame(AM_NSR, IpProtocol.ICMP, ipp);
        ipp = IpProtocol.valueOf(1);
        print(ipp);
        assertSame(AM_NSR, IpProtocol.ICMP, ipp);
    }

    @Test
    public void icmpv6() {
        print(EOL + "icmpv6()");
        ipp = IpProtocol.valueOf("IPv6-ICMP");
        print(ipp);
        assertSame(AM_NSR, IpProtocol.ICMPv6, ipp);
        ipp = IpProtocol.valueOf("icmpv6");
        print(ipp);
        assertSame(AM_NSR, IpProtocol.ICMPv6, ipp);
        ipp = IpProtocol.valueOf("ICMPv6");
        print(ipp);
        assertSame(AM_NSR, IpProtocol.ICMPv6, ipp);
        ipp = IpProtocol.valueOf(58);
        print(ipp);
        assertSame(AM_NSR, IpProtocol.ICMPv6, ipp);
    }


    private static final int[] UNK_THRESH = {
            0, 140, 253, 256
    };

    @Test
    public void unknownRanges() {
        print(EOL + "unknownRanges()");
        boolean expUnknown = false;
        for (int i=0; i<UNK_THRESH.length-1; i++) {
            int start = UNK_THRESH[i];
            int end = UNK_THRESH[i+1];
            for (int code=start; code<end; code++) {
                ipp = IpProtocol.valueOf(code);
                print(ipp);
                assertEquals("unknown mismatch", expUnknown, ipp.isUnknown());
            }
            expUnknown = !expUnknown;
        }
    }

    private static final String[] TCP_STRINGS = {
            "tcp",
            "Tcp",
            "TCP",
            "TCP : Transmission Control Protocol",
            "tcp : transmission control protocol",
    };

    @Test
    public void valueOfStringIgnoringCase() {
        print(EOL + "valueOfStringIgnoringCase()");
        for (String s: TCP_STRINGS) {
            IpProtocol ipp = IpProtocol.valueOf(s);
            print(s + " --> " + ipp);
            assertSame(AM_NSR, IpProtocol.TCP, ipp);
        }
    }

    // see ipProtocol.properties...
    private static final String IGMP_SHORT = "IGMP";
    private static final String IGMP_LONG =
            "IGMP : Internet Group Management Protocol";
    private static final int IGMP_NUM = 2;
    private static final String IGMP_STR = IGMP_NUM + "(" + IGMP_SHORT + ")";

    @Test
    public void igmp() {
        print(EOL + "igmp()");
        IpProtocol ipp = IpProtocol.valueOf("igmp");
        assertEquals(AM_NEQ, IGMP_STR, ipp.toString());
        assertEquals(AM_NEQ, IGMP_NUM, ipp.getNumber());
        assertEquals(AM_NEQ, IGMP_SHORT, ipp.getShortName());
        assertEquals(AM_NEQ, IGMP_LONG, ipp.getName());
        assertFalse(AM_NEQ, ipp.isUnknown());
    }


    //=== Check for Exceptions ===

    @Test (expected=NullPointerException.class)
    public void valueOfStringNull() {
        IpProtocol.valueOf(null);
    }

    @Test (expected=IllegalArgumentException.class)
    public void valueOfStringEmptyString() {
        IpProtocol.valueOf("");
    }

    @Test (expected=IllegalArgumentException.class)
    public void valueOfStringXyyzy() {
        IpProtocol.valueOf("xyyzy");
    }

    @Test (expected=IllegalArgumentException.class)
    public void valueOfStringUnknown() {
        IpProtocol.valueOf("(Unknown)");
    }

    @Test (expected=IllegalArgumentException.class)
    public void valueOfIntTooSmall() {
        IpProtocol.valueOf(-1);
    }

    @Test (expected=IllegalArgumentException.class)
    public void valueOfIntTooBig() {
        IpProtocol.valueOf(256);
    }

    @Test (expected=IllegalArgumentException.class)
    public void valueOfIntTooSmallMin() {
        IpProtocol.valueOf(Integer.MIN_VALUE);
    }

    @Test (expected=IllegalArgumentException.class)
    public void valueOfIntTooBigMax() {
        IpProtocol.valueOf(Integer.MAX_VALUE);
    }

    private static final String XNET_STR = "15(XNET)";

    @Test
    public void toStringTest() {
        print(EOL + "toStringTest()");
        IpProtocol ipp = IpProtocol.valueOf(15);
        print(ipp);
        assertEquals(AM_HUH, XNET_STR, ipp.toString());
    }

    @Test
    public void equality() {
        assertFalse(AM_HUH, IpProtocol.TCP.equals(null));
        assertFalse(AM_HUH, IpProtocol.TCP.equals(Integer.MAX_VALUE));
        assertFalse(AM_HUH, IpProtocol.TCP.equals(IpProtocol.UDP));
    }
}
