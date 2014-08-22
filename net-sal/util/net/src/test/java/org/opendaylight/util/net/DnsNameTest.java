/*
 * (c) Copyright 2009,2010 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.util.net;

import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;

import static org.opendaylight.util.junit.TestTools.*;
import static org.junit.Assert.*;

/**
 * This class defines unit tests for {@link DnsName}.
 *
 * @author Simon Hunt
 */
public class DnsNameTest {

    private static final String ROSE_DOMAIN = "rose.hp.com";

    private static final String A1Z1 = "a1z1.rose.hp.com";
    private static final String A1Z3 = "a1z3.rose.hp.com";
    private static final String A1Z15 = "a1z15.rose.hp.com";

    private static final String A3Z1 = "a3z1.rose.hp.com";
    private static final String A3Z3 = "a3z3.rose.hp.com";
    private static final String A3Z15 = "a3z15.rose.hp.com";

    private static final String A15Z1 = "a15z1.rose.hp.com";
    private static final String A15Z3 = "a15z3.rose.hp.com";
    private static final String A15Z15 = "a15z15.rose.hp.com";

    private DnsName a1z1;
    private DnsName a15z15;

    private DnsName[] unsorted, sorted;

    @Before
    public void initTest() {
        a1z1 = DnsName.valueOf(A1Z1);
        DnsName a1z3 = DnsName.valueOf(A1Z3);
        DnsName a1z15 = DnsName.valueOf(A1Z15);

        DnsName a3z1 = DnsName.valueOf(A3Z1);
        DnsName a3z3 = DnsName.valueOf(A3Z3);
        DnsName a3z15 = DnsName.valueOf(A3Z15);

        DnsName a15z1 = DnsName.valueOf(A15Z1);
        DnsName a15z3 = DnsName.valueOf(A15Z3);
        a15z15 = DnsName.valueOf(A15Z15);

        sorted = new DnsName[] {
                a1z1,
                a1z3,
                a1z15,
                a3z1,
                a3z3,
                a3z15,
                a15z1,
                a15z3,
                a15z15,
                DnsName.UNRESOLVABLE,
        };

        unsorted = new DnsName[] {
                a15z1,
                a1z15,
                a3z1,
                DnsName.UNRESOLVABLE,
                a3z3,
                a15z3,
                a1z1,
                a15z15,
                a3z15,
                a1z3,
        };
    }


    @Test (expected = NullPointerException.class)
    public void valueOfNull() {
        DnsName.valueOf(null);
    }

    @Test
    public void valueOfEmptyString() {
        DnsName d = DnsName.valueOf("");
        assertTrue(AM_HUH, d.isUnresolvable());
    }

    @Test
    public void pieces() {
        assertEquals(AM_HUH, "a1z1", a1z1.getHostName());
        assertEquals(AM_HUH, ROSE_DOMAIN, a1z1.getDomainName());
        assertEquals(AM_HUH, A15Z15, a15z15.toString());
    }


    @Test
    public void unresolvable() {
        // see dnsName.properties
        assertEquals(AM_NEQ, "Unresolvable DNS Name", DnsName.UNRESOLVABLE.toString());
        assertEquals(AM_NEQ, "Unknown Host", DnsName.UNRESOLVABLE.getHostName());
        assertEquals(AM_NEQ, "Unknown Domain", DnsName.UNRESOLVABLE.getDomainName());
        assertTrue(AM_HUH, DnsName.UNRESOLVABLE.isUnresolvable());
    }

    @Test
    public void unresolvable2() {
        DnsName az = DnsName.valueOf(A1Z1);
        verifyNotEqual(az, DnsName.UNRESOLVABLE);
        verifyEqual(DnsName.UNRESOLVABLE, DnsName.UNRESOLVABLE);
    }

    @Test
    public void comparing() {
        for (int i=0; i<sorted.length-1; i++) {
            DnsName a = sorted[i];
            assertTrue(AM_HUH, a.compareTo(a) == 0);
            for (int j=i+1; j<sorted.length; j++) {
                DnsName b = sorted[j];
                assertTrue(AM_A_NLT_B, a.compareTo(b) < 0);
                assertTrue(AM_B_NGT_A, b.compareTo(a) > 0);
            }
        }
    }

    @Test
    public void sorting() {
        assertFalse(AM_HUH, Arrays.equals(sorted, unsorted));
        Arrays.sort(unsorted);
        assertTrue(AM_HUH, Arrays.equals(sorted, unsorted));
        print(EOL + "sorting():");
        for (DnsName d: unsorted) {
            print("  " + d);
        }
    }
    
    @Test
    public void convenience() {
        assertEquals(AM_NEQ, DnsName.valueOf(A15Z15), DnsName.dns(A15Z15));
    }
}
