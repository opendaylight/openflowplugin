/*
 * (c) Copyright 2013,2014 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.of.lib.dt;

import org.junit.Test;
import org.opendaylight.of.lib.ProtocolVersion;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.opendaylight.of.lib.ProtocolVersion.V_1_3;
import static org.opendaylight.util.junit.TestTools.AM_HUH;
import static org.opendaylight.util.junit.TestTools.AM_NEQ;

/**
 * Unit tests for DataPathInfoAdapter.
 *
 * @author Simon Hunt
 */
public class DataPathInfoAdapterTest {

    private static final DataPathId DPID = DataPathId.valueOf("3/987654:fedcba");
    private static final ProtocolVersion NEG = V_1_3;

    private static class MyAdapter extends DataPathInfoAdapter {
        @Override public DataPathId dpid() { return DPID; }
        @Override public ProtocolVersion negotiated() { return NEG; }
    }

    @Test
    public void basic() {
        DataPathInfo dpi = new DataPathInfoAdapter();
        assertNull(AM_HUH, dpi.dpid());
        assertNull(AM_HUH, dpi.negotiated());
        assertEquals(AM_NEQ, 0, dpi.readyAt());
        assertEquals(AM_NEQ, 0, dpi.lastMessageAt());
        assertNull(AM_HUH, dpi.ports());
        assertEquals(AM_NEQ, 0, dpi.numBuffers());
        assertEquals(AM_NEQ, 0, dpi.numTables());
        assertNull(AM_HUH, dpi.capabilities());
        assertNull(AM_HUH, dpi.remoteAddress());
        assertNull(AM_HUH, dpi.remotePort());
        assertNull(AM_HUH, dpi.datapathDescription());
        assertNull(AM_HUH, dpi.manufacturerDescription());
        assertNull(AM_HUH, dpi.hardwareDescription());
        assertNull(AM_HUH, dpi.softwareDescription());
        assertNull(AM_HUH, dpi.serialNumber());
        assertNull(AM_HUH, dpi.deviceTypeName());
    }

    @Test
    public void overrideSomething() {
        DataPathInfo dpi = new MyAdapter();
        assertEquals(AM_NEQ, DPID, dpi.dpid());
        assertEquals(AM_NEQ, NEG, dpi.negotiated());
        assertEquals(AM_NEQ, 0, dpi.readyAt());
        assertNull(AM_HUH, dpi.ports());
        // etc...
    }
}
