/*
 * (c) Copyright 2012 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.util.net;

import org.junit.Test;

import static org.opendaylight.util.junit.TestTools.*;
import static org.junit.Assert.assertSame;

/**
 * Unit tests for EthernetType.
 *
 * @author Simon Hunt
 */
public class EthernetTypeTest {

    private void verify(String tag, EthernetType expType) {
        EthernetType et = EthernetType.valueOf(tag);
        print(tag + " -> " + et);
        assertSame(AM_NSR, expType, et);
    }

    private void verify(int value, EthernetType expType) {
        EthernetType et = EthernetType.valueOf(value);
        print("0x" + Integer.toHexString(value) + " -> " + et);
        assertSame(AM_NSR, expType, et);
    }

    @Test
    public void ipv4() {
        print(EOL + "ipv4()");
        verify("IPv4", EthernetType.IPv4);
        verify(0x0800, EthernetType.IPv4);
    }

    @Test
    public void arp() {
        print(EOL + "arp()");
        verify("ARP", EthernetType.ARP);
        verify(0x0806, EthernetType.ARP);
    }

    @Test
    public void vlanTagged() {
        print(EOL + "vlanTagged()");
        verify("VLAN", EthernetType.VLAN);
        verify(0x8100, EthernetType.VLAN);
    }

    @Test
    public void ipv6() {
        print(EOL + "ipv6()");
        verify("IPv6", EthernetType.IPv6);
        verify(0x86dd, EthernetType.IPv6);
    }

    @Test
    public void mplsUnicast() {
        print(EOL + "mplsUnicast()");
        verify("MPLS_U", EthernetType.MPLS_U);
        verify(0x8847, EthernetType.MPLS_U);
    }

    @Test
    public void mplsMulticast() {
        print(EOL + "mplsMulticast()");
        verify("MPLS_M", EthernetType.MPLS_M);
        verify(0x8848, EthernetType.MPLS_M);
    }

    @Test
    public void providerBridging() {
        print(EOL + "providerBridging()");
        verify("PRV_BRDG", EthernetType.PRV_BRDG);
        verify(0x88a8, EthernetType.PRV_BRDG);
    }

    @Test
    public void lldp() {
        print(EOL + "lldp()");
        verify("LLDP", EthernetType.LLDP);
        verify(0x88cc, EthernetType.LLDP);
    }

    @Test
    public void pbb() {
        print(EOL + "pbb()");
        verify("PBB", EthernetType.PBB);
        verify(0x88e7, EthernetType.PBB);
    }

    @Test
    public void bddp() {
        print(EOL + "bddp()");
        verify("BDDP", EthernetType.BDDP);
        verify(0x8999, EthernetType.BDDP);
    }
    
    @Test(expected=IllegalArgumentException.class)
    public void outOfBounds() {
        EthernetType.valueOf(0x99999);
    }

    @Test(expected=IllegalArgumentException.class)
    public void negative() {
        EthernetType.valueOf(-1);
    }

    @Test(expected=NullPointerException.class)
    public void nullString() {
        EthernetType.valueOf(null);
    }

    @Test(expected=IllegalArgumentException.class)
    public void badString() {
        EthernetType.valueOf("foo");
    }
}
