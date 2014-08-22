/*
 * (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */


package org.opendaylight.of.controller.pkt.impl;

import org.junit.Before;
import org.junit.Test;
import org.opendaylight.of.controller.pkt.SequencedPacketListenerRole;
import org.opendaylight.of.controller.pkt.SequencedPacketAdapter;
import org.opendaylight.of.controller.pkt.SequencedPacketListener;

import static org.junit.Assert.assertEquals;
import static org.opendaylight.util.junit.TestTools.*;

/**
 * Unit tests for {@link SplMetricSnapshot}.
 *
 * @author Simon Hunt
 */
public class SplMetricSnapshotTest {

    private static final SequencedPacketListener SPL = new TestSpl();
    private static final SequencedPacketListenerRole ROLE = SequencedPacketListenerRole.OBSERVER;
    private static final int ALT = 100;

    private static final double MILLION = 1000000.0;
    private static final double EPSILON = 0.001;
    private static final double ZERO = 0.0;
    private static final double UNDEF = -1.0;
    private static final String NA = "n/a";

    private SplMetricData data;
    private SplMetricSnapshot snap;

    private static class TestSpl extends SequencedPacketAdapter { }

    @Before
    public void setUp() {
        data = new SplMetricData(SPL, ROLE, ALT);
    }

    private void verifyMath(long count, long duration, double avNs, String ms) {
        double avMs = avNs < ZERO ? UNDEF : avNs / MILLION;
        assertEquals(AM_NEQ, count, snap.sampleCount());
        assertEquals(AM_NEQ, duration, snap.totalDuration());
        assertEquals(AM_NEQ, avNs, snap.averageDurationNanos(), EPSILON);
        assertEquals(AM_NEQ, avMs, snap.averageDurationMs(), EPSILON);
        assertEquals(AM_NEQ, ms, snap.averageMs());
    }

    @Test
    public void basic() {
        print(EOL + "basic()");
        snap = new SplMetricSnapshot(data);
        print(snap);
        assertEquals(AM_NEQ, SPL.getClass(), snap.splClass());
        assertEquals(AM_NEQ, ROLE, snap.role());
        assertEquals(AM_NEQ, ALT, snap.altitude());
        verifyMath(0, 0, UNDEF, NA);
    }

    @Test
    public void addStuff() {
        print(EOL + "addStuff()");
        data.addSample(15); // add 15 nanos
        snap = new SplMetricSnapshot(data);
        print(snap);
        verifyMath(1, 15, 15.0, "0.000015");
    }

    @Test
    public void addMoreStuff() {
        print(EOL + "addMoreStuff()");
        data.addSample(15);     // add 15 nanos
        data.addSample(100);    // and another 100 nanos
        data.addSample(2);      // and a final 2 nanos
        snap = new SplMetricSnapshot(data);
        print(snap);
        verifyMath(3, 117, 39.0, "0.000039");
    }
}
