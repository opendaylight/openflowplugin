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
 * This class defines unit tests for {@link IpDnsPair}.
 *
 * @author Simon Hunt
 */
public class IpDnsPairTest {

    private static final String IP_1_STR = "15.3.200.1";
    private static final String DNS_1_STR = "palhgw9.cns.hp.com";
    private static final String IP_2_STR = "15.29.37.13";
    private static final String DNS_2_STR = "nmdev13.rose.hp.com";

    private static final IpAddress IP_1 = IpAddress.valueOf(IP_1_STR);
    private static final DnsName DNS_1 = DnsName.valueOf(DNS_1_STR);
    private static final IpAddress IP_2 = IpAddress.valueOf(IP_2_STR);
    private static final DnsName DNS_2 = DnsName.valueOf(DNS_2_STR);

    @Test(expected = NullPointerException.class)
    public void valueOfIpDnsNullOne() {
        IpDnsPair.valueOf((IpAddress)null, null);
    }
    @Test(expected = NullPointerException.class)
    public void valueOfIpDnsNullOneStr() {
        IpDnsPair.valueOf((String)null, null);
    }

    @Test (expected = NullPointerException.class)
    public void valueOfIpDnsNullTwo() {
        IpDnsPair.valueOf(IpAddress.LOOPBACK_IPv4, null);
    }

    @Test (expected = NullPointerException.class)
    public void valueOfIpDnsNullTwoStr() {
        IpDnsPair.valueOf(IpAddress.LOOPBACK_IPv4.toString(), null);
    }

    @Test (expected = NullPointerException.class)
    public void valueOfIpDnsNullThree() {
        IpDnsPair.valueOf(null, DNS_1);
    }

    @Test (expected = NullPointerException.class)
    public void valueOfIpDnsNullThreeStr() {
        IpDnsPair.valueOf(null, DNS_1_STR);
    }

    @Test (expected = NullPointerException.class)
    public void valueOfIpDnsNullFour() {
        IpDnsPair.valueOf((IpAddress)null);
    }

    @Test (expected = NullPointerException.class)
    public void valueOfIpDnsNullFourStr() {
        IpDnsPair.valueOf((String)null);
    }


    @Test
    public void fromStrings() {
        IpDnsPair pair1 = IpDnsPair.valueOf(IP_1_STR, DNS_1_STR);
        IpDnsPair pair2 = IpDnsPair.valueOf(IP_1, DNS_1);
        verifyEqual(pair1, pair2);
        assertEquals(AM_NSR, pair1, pair2);
    }

    @Test
    public void fromStringsUnres() {
        IpDnsPair pair1 = IpDnsPair.valueOf(IP_2_STR);
        IpDnsPair pair2 = IpDnsPair.valueOf(IP_2);
        verifyEqual(pair1, pair2);
        assertEquals(AM_NSR, pair1, pair2);
        assertTrue("not unresolvable", pair1.hasUnresolvableDnsName());
    }

    @Test
    public void notEqual() {
        IpDnsPair pair1 = IpDnsPair.valueOf(IP_1, DNS_1);
        IpDnsPair pair2 = IpDnsPair.valueOf(IP_2, DNS_2);
        verifyNotEqual(pair1, pair2);
    }

    @Test
    public void same() {
        IpDnsPair pair1 = IpDnsPair.valueOf(IP_1, DNS_1);
        IpDnsPair pair2 = IpDnsPair.valueOf(IP_1, DNS_1);
        verifyEqual(pair1, pair2);
        assertEquals(AM_NSR, pair1, pair2);
    }

    @Test
    public void unresolvableOne() {
        IpDnsPair pair = IpDnsPair.valueOf(IP_1, DNS_1);
        assertFalse(AM_HUH, pair.hasUnresolvableDnsName());
    }

    @Test
    public void unresolvableTwo() {
        IpDnsPair pair = IpDnsPair.valueOf(IP_1);
        assertTrue(AM_HUH, pair.hasUnresolvableDnsName());
    }

    @Test
    public void unresolvableThree() {
        IpDnsPair pair1 = IpDnsPair.valueOf(IP_1);
        IpDnsPair pair2 = IpDnsPair.valueOf(IP_1, DnsName.UNRESOLVABLE);
        assertEquals(AM_NSR, pair1, pair2);
    }

    @Test
    public void unresolvableFour() {
        IpDnsPair pair = IpDnsPair.valueOf(IP_1);
        DnsName dns = pair.getDns();
        assertTrue(AM_HUH, dns.isUnresolvable());
    }

    @Test
    public void checkBits() {
        IpDnsPair pair = IpDnsPair.valueOf(IP_1, DNS_1);
        assertEquals(AM_NSR, IP_1, pair.getIp());
        assertEquals(AM_NSR, DNS_1, pair.getDns());
    }

    @Test
    public void comparisons() {
        IpDnsPair a = IpDnsPair.valueOf(IP_1, DNS_1);
        IpDnsPair b = IpDnsPair.valueOf(IP_2, DNS_2);

        assertTrue(AM_HUH, a.compareTo(a) == 0);
        assertTrue(AM_HUH, b.compareTo(b) == 0);
        assertTrue(AM_A_NLT_B, a.compareTo(b) < 0);
        assertTrue(AM_B_NGT_A, b.compareTo(a) > 0);
    }
}
