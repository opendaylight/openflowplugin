/*
 * (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */


package org.opendaylight.of.controller.impl;

import org.junit.Test;
import org.opendaylight.of.lib.OpenflowException;
import org.opendaylight.of.lib.dt.DataPathId;
import org.opendaylight.of.lib.dt.DataPathInfo;
import org.opendaylight.of.lib.dt.DataPathInfoAdapter;
import org.opendaylight.of.lib.mp.MBodyDesc;
import org.opendaylight.of.lib.mp.MultipartType;
import org.opendaylight.of.lib.msg.OfmError;
import org.opendaylight.of.lib.msg.OpenflowMessage;

import java.util.List;

import static org.junit.Assert.*;
import static org.opendaylight.of.lib.ProtocolVersion.V_1_3;
import static org.opendaylight.of.lib.dt.DataPathId.dpid;
import static org.opendaylight.of.lib.mp.MpBodyFactory.createReplyBody;
import static org.opendaylight.util.junit.TestTools.*;

/**
 * Unit test for {@link ListenerServiceAdapter}.
 *
 * @author Simon Hunt
 */
public class ListenerServiceAdapterTest {

    private static final MBodyDesc DESC =
            (MBodyDesc) createReplyBody(V_1_3, MultipartType.DESC);
    private static final DataPathInfo DPI = new DataPathInfoAdapter();
    private static final DataPathId DPID = dpid("1/112233445566");

    private static class FakeService extends ListenerServiceAdapter {
        @Override
        public DataPathInfo getDataPathInfo(DataPathId dpid) { return DPI; }
        @Override
        public MBodyDesc getCachedDeviceDesc(DataPathId dpid) { return DESC; }
    }

    private ListenerService ls;

    @Test
    public void basic() {
        ls = new ListenerServiceAdapter();
        // not a great test, per se, but invoke each method without error
        ls.addDataPathListener(null);
        ls.removeDataPathListener(null);
        ls.addMessageListener(null, null);
        ls.removeMessageListener(null);
        assertNull(AM_HUH, ls.getAllDataPathInfo());
        assertNull(AM_HUH, ls.getDataPathInfo(DPID));
        assertNull(AM_HUH, ls.versionOf(DPID));
        assertNull(AM_HUH, ls.getStats());
        assertNull(AM_HUH, ls.getPortStats(DPID));
        assertNull(AM_HUH, ls.getPortStats(DPID, null));
        assertNull(AM_HUH, ls.enablePort(DPID, null, false));
        try {
            ls.send(null, DPID, 0);
            ls.send((OpenflowMessage) null, DPID);
            ls.send((List<OpenflowMessage>) null, DPID);
            ls.sendFuture(null);
        } catch (OpenflowException e) {
            fail(AM_UNEX);
        }
        assertNull(AM_HUH, ls.findFuture(null, DPID));
        ls.failFuture(null, (Throwable) null);
        ls.failFuture(null, (OfmError) null);
        ls.successFuture(null, null);
        ls.cancelFuture(null);
        ls.countDrop(0);
        assertNull(AM_HUH, ls.getCachedTableFeatures(DPID));
        assertNull(AM_HUH, ls.getCachedDeviceDesc(DPID));
        assertTrue("Didn't get here?", true);
    }


    @Test
    public void subclass() {
        ls = new FakeService();
        assertEquals(AM_NEQ, DPI, ls.getDataPathInfo(DPID));
        assertEquals(AM_NEQ, DESC, ls.getCachedDeviceDesc(DPID));
    }
}
