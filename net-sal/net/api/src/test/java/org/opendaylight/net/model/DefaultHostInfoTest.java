/*
 * (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.net.model;

import org.opendaylight.util.net.IpAddress;
import org.opendaylight.util.net.MacAddress;
import org.junit.Test;

import static org.opendaylight.util.net.BigPortNumber.bpn;
import static junit.framework.Assert.assertNull;
import static org.junit.Assert.assertEquals;

/**
 * Unit tests for {@link DefaultHostInfo}.
 *
 * @author Shaun Wackerly
 */
public class DefaultHostInfoTest {

    private static final HostId NID = HostId.valueOf(IpAddress.LOOPBACK_IPv4, SegmentId.UNKNOWN);

    private static final DeviceId DID = DeviceId.valueOf("foo");
    private static final InterfaceId DIDIF = InterfaceId.valueOf(bpn(1));

    private static final Interface INTF = new DefaultInterface(InterfaceId.valueOf(bpn(0)),
                                                               new DefaultInterfaceInfo(NID, bpn(0)));
    private static final MacAddress MAC = MacAddress.BROADCAST;
    private static final HostLocation LOC = new DefaultHostLocation(DID, DIDIF);

    @Test
    public void basic() {
        HostInfo dni = new DefaultHostInfo(INTF, MAC, LOC);
        assertEquals("incorrect interface", INTF, dni.netInterface());
        assertEquals("incorrect mac", MAC, dni.mac());
        assertEquals("incorrect location", LOC, dni.location());
    }

    @Test
    public void empty() {
        HostInfo dni = new DefaultHostInfo(null, null, null);
        assertNull(dni.netInterface());
        assertNull(dni.mac());
        assertNull(dni.location());
    }


}
