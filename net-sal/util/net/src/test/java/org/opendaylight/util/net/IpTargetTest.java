/*
 * (C) Copyright 2013,2014 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.util.net;

import static org.opendaylight.util.junit.TestTools.AM_HUH;
import static org.opendaylight.util.junit.TestTools.AM_NEQ;
import static org.opendaylight.util.junit.TestTools.verifyEqual;
import static org.opendaylight.util.junit.TestTools.verifyNotEqual;
import static org.opendaylight.util.net.IpAddress.ip;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import org.junit.Test;

/**
 * Unit tests for {@link IpTarget}.
 * 
 * @author Fabiel Zuniga
 * @author Simon Hunt
 */
public class IpTargetTest {

    private static final IpAddress IP = ip("15.1.2.3");
    private static final IpAddress ALT_IP = IpAddress.LOOPBACK_IPv4;
    private static final PortNumber PORT = PortNumber.valueOf(1);
    private static final PortNumber ALT_PORT = PortNumber.valueOf(2);


    @Test(expected = NullPointerException.class)
    public void nullIp() {
        new IpTarget(null, PORT);
    }

    @Test(expected = NullPointerException.class)
    public void nullPort() {
        new IpTarget(IP, null);
    }

    @Test
    public void construct() {
        IpTarget ipTarget = new IpTarget(ALT_IP, PORT);
        assertEquals(AM_NEQ, ALT_IP, ipTarget.address());
        assertEquals(AM_NEQ, PORT, ipTarget.port());
    }

    @Test
    public void equalsAndHashCode() {
        IpTarget base = new IpTarget(IP, PORT);
        IpTarget copy = new IpTarget(IP, PORT);
        IpTarget diff1 = new IpTarget(ALT_IP, PORT);
        IpTarget diff2 = new IpTarget(IP, ALT_PORT);
        IpTarget diff3 = new IpTarget(ALT_IP, ALT_PORT);

        verifyEqual(base, copy);
        verifyNotEqual(base, diff1);
        verifyNotEqual(base, diff2);
        verifyNotEqual(base, diff3);
    }

    @Test
    public void testToString() {
        IpTarget ipTarget = new IpTarget(ALT_IP, PORT);
        assertFalse(AM_HUH, ipTarget.toString().isEmpty());
    }
}
