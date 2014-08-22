/*
 * (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.net.host;

import org.opendaylight.net.model.DefaultHost;
import org.opendaylight.net.model.Host;
import org.opendaylight.net.model.HostId;
import org.opendaylight.net.model.SegmentId;
import org.opendaylight.util.net.IpAddress;
import org.opendaylight.util.net.VlanId;
import org.junit.Test;

import static junit.framework.Assert.*;

/**
 * Unit tests for {@link DefaultHostEvent}.
 *
 * @author Shaun Wackerly
 */
public class DefaultHostEventTest {

    private DefaultHostEvent dne;
    private final HostId id1 = HostId.valueOf(IpAddress.BROADCAST_IPv4,
                                              SegmentId.valueOf(VlanId.NONE));
    private final HostId id2 = HostId.valueOf(IpAddress.LOOPBACK_IPv4,
                                              SegmentId.valueOf(VlanId.NONE));
    private final Host n1 = new DefaultHost(id1);
    private final Host n2 = new DefaultHost(id2);
    
    @Test
    public void allTypeCombinations() {
        for (HostEvent.Type t : HostEvent.Type.values()) {
            dne = new DefaultHostEvent(t, n1);
            long ts = System.currentTimeMillis();

            assertEquals(t, dne.type());
            assertSame(n1, dne.subject());

            assertTrue(ts >= dne.ts());
            assertTrue(ts-100 < dne.ts());
        }
    }
    
    @Test (expected = NullPointerException.class)
    public void nullType() {
        dne = new DefaultHostEvent(null, n1);
    }

    @Test (expected = NullPointerException.class)
    public void nullNode() {
        dne = new DefaultHostEvent(HostEvent.Type.HOST_ADDED, null);
    }

}
