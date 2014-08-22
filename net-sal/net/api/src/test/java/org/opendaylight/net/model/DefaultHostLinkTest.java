/*
 * (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.net.model;

import org.opendaylight.net.model.Link.Type;
import org.opendaylight.net.supplier.SupplierId;
import org.opendaylight.util.net.MacAddress;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.opendaylight.util.net.BigPortNumber.bpn;
import static org.opendaylight.util.net.IpAddress.ip;
import static junit.framework.Assert.assertEquals;

/**
 * Tests of the default node link implementation.
 *
 * @author Thomas Vachuska
 */
public class DefaultHostLinkTest {

    private static final InterfaceId IF0 = InterfaceId.valueOf(bpn(0));
    private static final InterfaceId IF1 = InterfaceId.valueOf(bpn(1));

    private static final HostId NID = HostId.valueOf(ip("12.34.56.78"), SegmentId.UNKNOWN);
    private static final DeviceId DID = DeviceId.valueOf("12:34:56:78:90:ab:cd:ef");

    private static final Interface IF =
            new DefaultInterface(IF0, new DefaultInterfaceInfo(NID, bpn(0)));

    private static final HostLocation LOC = new DefaultHostLocation(DID, IF1);
    private static final SupplierId SID = new SupplierId("foobar");
    private static final MacAddress MAC = MacAddress.mac("12:34:56:78:90:ab");

    @Test
    public void basics() {
        DefaultHostLink link =
                new DefaultHostLink(new DefaultConnectionPoint(NID, IF.id()), LOC, true);
        assertEquals("invalid node id", NID, link.nodeId());
        assertEquals("invalid type", Type.NODE, link.type());
        assertEquals("invalid cp", LOC, link.connectionPoint());
        assertTrue("wrong direction", link.src().elementId() instanceof HostId);
    }

    @Test
    public void fromHost() {
        DefaultHostLink link =
                new DefaultHostLink(new DefaultHost(SID, NID, IF, MAC, LOC), false);
        assertEquals("invalid node id", NID, link.nodeId());
        assertEquals("invalid type", Type.NODE, link.type());
        assertEquals("invalid cp", LOC, link.connectionPoint());
        assertFalse("wrong direction", link.src().elementId() instanceof HostId);
    }

}
