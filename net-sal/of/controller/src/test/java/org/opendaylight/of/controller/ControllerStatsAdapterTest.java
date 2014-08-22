/*
 * (c) Copyright 2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.of.controller;

import org.junit.Test;
import org.opendaylight.of.controller.impl.AbstractTest;

import static org.junit.Assert.assertEquals;
import static org.opendaylight.util.junit.TestTools.AM_NEQ;

/**
 * Unit tests for ControllerStatsAdapter.
 *
 * @author Simon Hunt
 */
public class ControllerStatsAdapterTest extends AbstractTest {

    private static final long DURATION = 100;
    private static final long PKT_INS = 35;
    private static final long PKT_OUTS = 46;

    private static class MyAdapter extends ControllerStatsAdapter {
        @Override public long duration() { return DURATION; }
        @Override public long packetInCount() { return PKT_INS; }
        @Override public long packetOutCount() { return PKT_OUTS; }
    }

    @Test
    public void basic() {
        ControllerStats stats = new ControllerStatsAdapter();
        assertEquals(AM_NEQ, 0, stats.duration());
        assertEquals(AM_NEQ, 0, stats.packetInCount());
        assertEquals(AM_NEQ, 0, stats.packetInBytes());
        assertEquals(AM_NEQ, 0, stats.packetOutCount());
        assertEquals(AM_NEQ, 0, stats.packetOutBytes());
        assertEquals(AM_NEQ, 0, stats.packetDropCount());
        assertEquals(AM_NEQ, 0, stats.packetDropBytes());
        assertEquals(AM_NEQ, 0, stats.msgRxCount());
        assertEquals(AM_NEQ, 0, stats.msgTxCount());
    }

    @Test
    public void overrideSomething() {
        ControllerStats stats = new MyAdapter();
        assertEquals(AM_NEQ, DURATION, stats.duration());
        assertEquals(AM_NEQ, PKT_INS, stats.packetInCount());
        assertEquals(AM_NEQ, PKT_OUTS, stats.packetOutCount());
        assertEquals(AM_NEQ, 0, stats.packetDropCount());
        assertEquals(AM_NEQ, 0, stats.msgRxCount());
        assertEquals(AM_NEQ, 0, stats.msgTxCount());
    }
}
