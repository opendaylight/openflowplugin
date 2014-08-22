/*
 * (c) Copyright 2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.of.mockswitch;

import org.junit.Test;
import org.opendaylight.of.lib.msg.Capability;
import org.opendaylight.util.net.IpAddress;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.opendaylight.of.lib.msg.Capability.*;
import static org.opendaylight.util.junit.TestTools.*;

/**
 * Unit tests for CfgBase.
 *
 * @author Simon Hunt
 */
public class CfgBaseTest {

    private static final IpAddress IP = IpAddress.LOOPBACK_IPv4;
    private static final int PORT = 6633;
    private static final int NBUFF = 256;
    private static final int NTAB = 12;
    private static final Set<Capability> CAPS =
            new HashSet<Capability>(Arrays.asList(
                    FLOW_STATS, TABLE_STATS, PORT_STATS, IP_REASM, QUEUE_STATS
            ));

    @Test(expected = NullPointerException.class)
    public void nullIp() {
        new CfgBase().setControllerAddress(null);
    }

    @Test(expected = NullPointerException.class)
    public void nullCaps() {
        new CfgBase().setCapabilities(null);
    }

    @Test
    public void basic() {
        print(EOL + "basic()");
        CfgBase base = new CfgBase();
        base.setControllerAddress(IP);
        base.setOpenflowPort(PORT);
        base.setBufferCount(NBUFF);
        base.setTableCount(NTAB);
        base.setCapabilities(CAPS);
        assertEquals(AM_NEQ, IP, base.getControllerAddress());
        assertEquals(AM_NEQ, PORT, base.getOpenflowPort());
        assertEquals(AM_NEQ, NBUFF, base.getBufferCount());
        assertEquals(AM_NEQ, NTAB, base.getTableCount());
        assertEquals(AM_NEQ, CAPS, base.getCapabilities());
    }

}
