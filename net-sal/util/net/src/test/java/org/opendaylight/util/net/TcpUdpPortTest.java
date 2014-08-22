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
import static org.opendaylight.util.net.IpProtocol.TCP;
import static org.opendaylight.util.net.IpProtocol.UDP;
import static org.junit.Assert.*;

/**
 * This class implements unit tests for {@link TcpUdpPort}.
 *
 * @author Simon Hunt
 */
public class TcpUdpPortTest {

    @Test
    public void basicPort5() {
        // see tcpUdpPorts.properties
        final String P5_SHORT = "rje";
        final String P5_LONG  = "rje : Remote Job Entry";

        TcpUdpPort p = TcpUdpPort.valueOf(5, TCP);
        assertSame(AM_NSR, TCP, p.getProtocol());
        assertEquals(AM_HUH, 5, p.getNumber());
        assertEquals(AM_HUH, P5_SHORT, p.getShortName());
        assertEquals(AM_HUH, P5_LONG, p.getName());
        assertEquals(AM_HUH, "5/tcp", p.toString());

        // UDP port has same string values
        p = TcpUdpPort.valueOf(5, UDP);
        assertSame(AM_NSR, UDP, p.getProtocol());
        assertEquals(AM_HUH, 5, p.getNumber());
        assertEquals(AM_HUH, P5_SHORT, p.getShortName());
        assertEquals(AM_HUH, P5_LONG, p.getName());
        assertEquals(AM_HUH, "5/udp", p.toString());
    }


    //=== Boundary Tests for valueOf(int, IpProtocol)

    @Test (expected = NullPointerException.class)
    public void nullProtocol() {
        TcpUdpPort.valueOf(0, null);
    }

    @Test
    public void nonTcpUdpProtocols() {
        for (int i=0; i<256; i++) {
            IpProtocol ipp = IpProtocol.valueOf(i);
            if (ipp != TCP && ipp != UDP) {
                try {
                    TcpUdpPort.valueOf(0, ipp);
                    fail("non-TCP/UDP port: " + ipp);
                } catch (IllegalArgumentException e) {
                    // perfect
                } catch (Exception e) {
                    fail("Unexpected Exception: " + e);
                }
            }
        }
    }

    @Test (expected = IllegalArgumentException.class)
    public void portTooBig() {
        TcpUdpPort.valueOf(65536, TCP);
    }

    @Test (expected = IllegalArgumentException.class)
    public void portTooSmall() {
        TcpUdpPort.valueOf(-2, TCP);
    }

    @Test (expected = IllegalArgumentException.class)
    public void portTooBigMax() {
        TcpUdpPort.valueOf(Integer.MAX_VALUE, TCP);
    }

    @Test (expected = IllegalArgumentException.class)
    public void portTooSmallMin() {
        TcpUdpPort.valueOf(Integer.MIN_VALUE, TCP);
    }

    @Test
    public void undeterminedPort() {
        TcpUdpPort un = TcpUdpPort.UNDETERMINED_TCP;
        assertTrue(AM_HUH, un.isUndetermined());
        assertEquals(AM_HUH, -1, un.getNumber());
        assertEquals(AM_HUH, TCP, un.getProtocol());

        un = TcpUdpPort.UNDETERMINED_UDP;
        assertTrue(AM_HUH, un.isUndetermined());
        assertEquals(AM_HUH, -1, un.getNumber());
        assertEquals(AM_HUH, UDP, un.getProtocol());
    }


    @Test
    public void valueOfString() {
        TcpUdpPort p = TcpUdpPort.valueOf("23/tcp");
        assertEquals(AM_HUH, 23, p.getNumber());
        assertEquals(AM_HUH, TCP, p.getProtocol());
        assertEquals(AM_HUH, "telnet", p.getShortName());
        assertEquals(AM_HUH, "telnet : Telnet", p.getName());
        assertEquals(AM_HUH, "23/tcp", p.toString());

        p = TcpUdpPort.valueOf("21/udp");
        assertEquals(AM_HUH, 21, p.getNumber());
        assertEquals(AM_HUH, UDP, p.getProtocol());
        assertEquals(AM_HUH, "ftp", p.getShortName());
        assertEquals(AM_HUH, "ftp : File Transfer [Control]", p.getName());
        assertEquals(AM_HUH, "21/udp", p.toString());
    }

    @Test (expected = NullPointerException.class)
    public void valueOfStringNull() {
        TcpUdpPort.valueOf(null);
    }

    @Test (expected = IllegalArgumentException.class)
    public void valueOfStringBad1() {
        TcpUdpPort.valueOf("123");
    }

    @Test (expected = IllegalArgumentException.class)
    public void valueOfStringBad2() {
        TcpUdpPort.valueOf("tcp");
    }

    @Test (expected = IllegalArgumentException.class)
    public void valueOfStringBad3() {
        TcpUdpPort.valueOf("1/tcpx");
    }

    @Test (expected = IllegalArgumentException.class)
    public void valueOfStringBad4() {
        TcpUdpPort.valueOf("1tcp");
    }

    @Test (expected = IllegalArgumentException.class)
    public void valueOfStringBad5() {
        TcpUdpPort.valueOf("-2345/udp");
    }

    @Test (expected = IllegalArgumentException.class)
    public void valueOfStringBad6() {
        TcpUdpPort.valueOf("65536/udp");
    }


    @Test
    public void uniqueRef() {
        TcpUdpPort p1 = TcpUdpPort.valueOf(39, TCP);
        TcpUdpPort p2 = TcpUdpPort.valueOf("39/tcp");
        TcpUdpPort p3 = TcpUdpPort.valueOf("39/TCP");
        assertSame(AM_NSR, p1, p2);
        assertSame(AM_NSR, p1, p3);
    }


    @Test
    public void unknownPortName() {
        TcpUdpPort port = TcpUdpPort.valueOf(19999, TCP);
        String name = port.getName();
        print(port);
        print(name);
        assertEquals(AM_HUH, "Unknown Port", name);
    }

    private static final TcpUdpPort TCP_0004 = TcpUdpPort.valueOf(4, TCP);
    private static final TcpUdpPort UDP_0123 = TcpUdpPort.valueOf(123, TCP);
    private static final TcpUdpPort TCP_1234 = TcpUdpPort.valueOf(1234, TCP);
    private static final TcpUdpPort UDP_2468 = TcpUdpPort.valueOf(2468, UDP);
    private static final TcpUdpPort TCP_2468 = TcpUdpPort.valueOf(2468, TCP);
    private static final TcpUdpPort TCP_33333 = TcpUdpPort.valueOf(33333, TCP);

    private static final TcpUdpPort[] SORTED = {
            TCP_0004,
            UDP_0123,
            TCP_1234,
            TCP_2468,
            UDP_2468,
            TCP_33333,
    };

    private static final TcpUdpPort[] UNSORTED = {
            UDP_0123,
            TCP_33333,
            TCP_0004,
            TCP_1234,
            TCP_2468,
            UDP_2468,
    };

    @Test
    public void equality() {
        assertTrue(AM_HUH, TCP_0004.equals(TCP_0004));
        assertFalse(AM_HUH, TCP_0004.equals(null));
        assertFalse(AM_HUH, TCP_0004.equals(TCP_1234));
        assertFalse(AM_HUH, TCP_1234.equals(TCP_0004));
    }

    @Test
    public void comparisons() {
        for (int i=0; i<SORTED.length-1; i++) {
            TcpUdpPort a = SORTED[i];
            assertTrue(AM_HUH, a.compareTo(a) == 0);
            for (int j=i+1; j<SORTED.length; j++) {
                TcpUdpPort b = SORTED[j];
                print(a + " <=> " + b);
                assertTrue(AM_HUH, a.compareTo(b) < 0);
                assertTrue(AM_HUH, b.compareTo(a) > 0);
            }
        }
    }

    @Test
    public void sorting() {
        assertFalse(AM_HUH, Arrays.equals(SORTED, UNSORTED));
        TcpUdpPort[] unsorted = UNSORTED.clone();
        Arrays.sort(unsorted);
        assertTrue(AM_HUH, Arrays.equals(SORTED, unsorted));
    }

    @Test
    public void convenienceMethods() {
        print(EOL + "convenienceMethods()");
        TcpUdpPort port = TcpUdpPort.tcpPort(1234);
        print(port);
        assertSame(AM_NSR, TCP_1234, port);
        port = TcpUdpPort.udpPort(2468);
        print(port);
        assertSame(AM_NSR, UDP_2468, port);
    }
}
