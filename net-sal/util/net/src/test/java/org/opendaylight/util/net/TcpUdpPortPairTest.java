/*
 * (c) Copyright 2009,2010 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */


package org.opendaylight.util.net;

import org.junit.Test;

import java.util.Arrays;

import static org.opendaylight.util.junit.TestTools.*;
import static org.junit.Assert.*;

/**
 * This class defines unit tests for {@link TcpUdpPortPair}.
 *
 * @author Simon Hunt
 */
public class TcpUdpPortPairTest {

    private static final IpAddress SRC_IP = IpAddress.valueOf("15.37.29.13");
    private static final IpAddress SRC_6_IP = IpAddress.valueOf("ffa::23:1");
    private static final TcpUdpPort SRC_TCP = TcpUdpPort.valueOf("23/tcp");
    private static final TcpUdpPort SRC_UDP = TcpUdpPort.valueOf("23/udp");

    private static final IpAddress DST_IP = IpAddress.valueOf("15.1.200.1");
    private static final IpAddress DST_6_IP = IpAddress.valueOf("ffa::23:7801");
    private static final TcpUdpPort DST_TCP = TcpUdpPort.valueOf("4000/tcp");
    private static final TcpUdpPort DST_UDP = TcpUdpPort.valueOf("4000/udp");


    @Test(expected = NullPointerException.class)
    public void valueOfNullOne() {
        TcpUdpPortPair.valueOf(null, SRC_TCP, DST_IP, DST_TCP);
    }

    @Test (expected = NullPointerException.class)
    public void valueOfNullTwo() {
        TcpUdpPortPair.valueOf(SRC_IP, null, DST_IP, DST_TCP);
    }

    @Test (expected = NullPointerException.class)
    public void valueOfNullThree() {
        TcpUdpPortPair.valueOf(SRC_IP, SRC_TCP, null, DST_TCP);
    }

    @Test (expected = NullPointerException.class)
    public void valueOfNullFour() {
        TcpUdpPortPair.valueOf(SRC_IP, SRC_TCP, DST_IP, null);
    }

    @Test (expected = IllegalArgumentException.class)
    public void portMismatchOne() {
        TcpUdpPortPair.valueOf(SRC_IP, SRC_TCP, DST_IP, DST_UDP);
    }

    @Test (expected = IllegalArgumentException.class)
    public void portMismatchTwo() {
        TcpUdpPortPair.valueOf(SRC_IP, SRC_UDP, DST_IP, DST_TCP);
    }


    @Test
    public void basicEquivalenceOne() {
        TcpUdpPortPair p1 = TcpUdpPortPair.valueOf(SRC_IP, SRC_TCP, DST_IP, DST_TCP);
        TcpUdpPortPair p2 = TcpUdpPortPair.valueOf(SRC_IP, SRC_TCP, DST_IP, DST_TCP);
        print(EOL + "basicEquivalenceOne():");
        print("  " + p1);
        print("  " + p1.toShortString());
        assertSame(AM_NSR, p1, p2);
    }

    @Test
    public void basicEquivalenceTwo() {
        TcpUdpPortPair p1 = TcpUdpPortPair.valueOf(SRC_6_IP, SRC_TCP, DST_6_IP, DST_TCP);
        TcpUdpPortPair p2 = TcpUdpPortPair.valueOf(SRC_6_IP, SRC_TCP, DST_6_IP, DST_TCP);
        print(EOL + "basicEquivalenceTwo():");
        print("  " + p1);
        print("  " + p1.toShortString());
        assertSame(AM_NSR, p1, p2);
    }

    @Test
    public void getters() {
        TcpUdpPortPair pair = TcpUdpPortPair.valueOf(SRC_6_IP, SRC_TCP, DST_6_IP, DST_TCP);
        assertEquals(AM_HUH, SRC_6_IP, pair.getSourceIp());
        assertEquals(AM_HUH, SRC_TCP, pair.getSourcePort());
        assertEquals(AM_HUH, DST_6_IP, pair.getDestinationIp());
        assertEquals(AM_HUH, DST_TCP, pair.getDestinationPort());
    }

    @Test
    public void equality() {
        TcpUdpPortPair pair = TcpUdpPortPair.valueOf(SRC_6_IP, SRC_TCP, DST_6_IP, DST_TCP);
        TcpUdpPortPair two = TcpUdpPortPair.valueOf(SRC_IP, SRC_UDP, DST_IP, DST_UDP);

        assertTrue(AM_HUH, pair.equals(pair));
        assertFalse(AM_HUH, pair.equals(null));
        assertFalse(AM_HUH, pair.equals(two));
        assertFalse(AM_HUH, two.equals(pair));
    }


    private static final IpAddress IP_1 = IpAddress.valueOf("12.34.56.78");
    private static final IpAddress IP_2 = IpAddress.valueOf("23.55.56.78");
    private static final IpAddress IP_3 = IpAddress.valueOf("34.55.56.78");
    private static final TcpUdpPort TCP_1 = TcpUdpPort.valueOf(23, IpProtocol.TCP);
    private static final TcpUdpPort TCP_2 = TcpUdpPort.valueOf(55, IpProtocol.TCP);
    private static final TcpUdpPort UDP_1 = TcpUdpPort.valueOf(23, IpProtocol.UDP);
    private static final TcpUdpPort UDP_2 = TcpUdpPort.valueOf(55, IpProtocol.UDP);

    private static final TcpUdpPortPair PP_1T1_2T1 = TcpUdpPortPair.valueOf(IP_1, TCP_1, IP_2, TCP_1);
    private static final TcpUdpPortPair PP_1T2_2T2 = TcpUdpPortPair.valueOf(IP_1, TCP_2, IP_2, TCP_2);
    private static final TcpUdpPortPair PP_2T1_3T1 = TcpUdpPortPair.valueOf(IP_2, TCP_1, IP_3, TCP_1);
    private static final TcpUdpPortPair PP_2T2_3T2 = TcpUdpPortPair.valueOf(IP_2, TCP_2, IP_3, TCP_2);
    private static final TcpUdpPortPair PP_1U1_2U1 = TcpUdpPortPair.valueOf(IP_1, UDP_1, IP_2, UDP_1);
    private static final TcpUdpPortPair PP_1U2_2U2 = TcpUdpPortPair.valueOf(IP_1, UDP_2, IP_2, UDP_2);
    private static final TcpUdpPortPair PP_2U1_3U1 = TcpUdpPortPair.valueOf(IP_2, UDP_1, IP_3, UDP_1);
    private static final TcpUdpPortPair PP_2U2_3U2 = TcpUdpPortPair.valueOf(IP_2, UDP_2, IP_3, UDP_2);


    private static final TcpUdpPortPair[] SORTED = {
            PP_1T1_2T1,
            PP_1U1_2U1,
            PP_1T2_2T2,
            PP_1U2_2U2,
            PP_2T1_3T1,
            PP_2U1_3U1,
            PP_2T2_3T2,
            PP_2U2_3U2,
    };

    private static final TcpUdpPortPair[] UNSORTED = {
            PP_1T2_2T2,
            PP_2T1_3T1,
            PP_1U2_2U2,
            PP_2T2_3T2,
            PP_1T1_2T1,
            PP_1U1_2U1,
            PP_2U2_3U2,
            PP_2U1_3U1,
    };

    @Test
    public void comparison() {
        for (int i=0; i<SORTED.length-1; i++) {
            TcpUdpPortPair a = SORTED[i];
            assertTrue(AM_HUH, a.compareTo(a) == 0);
            for (int j=i+1; j<SORTED.length; j++) {
                TcpUdpPortPair b = SORTED[j];
                print("("+i+","+j+") " + a + " <=> " + b);
                assertTrue(AM_A_NLT_B, a.compareTo(b) < 0);
                assertTrue(AM_B_NGT_A, b.compareTo(a) > 0);
            }
        }
    }

    @Test
    public void sorting() {
        assertFalse(AM_HUH, Arrays.equals(SORTED, UNSORTED));
        TcpUdpPortPair[] unsorted = UNSORTED.clone();
        Arrays.sort(unsorted);
        assertTrue(AM_HUH, Arrays.equals(SORTED, unsorted));
    }

}
