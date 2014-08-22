/*
 * (c) Copyright 2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.util.test;

import org.opendaylight.util.TimeUtils;
import org.junit.Test;
import org.opendaylight.util.test.FakeTimeUtils;

import static org.opendaylight.util.junit.TestTools.AM_NEQ;
import static org.opendaylight.util.test.FakeTimeUtils.Advance;
import static org.junit.Assert.assertEquals;

/**
 * Unit tests for {@link org.opendaylight.util.test.FakeTimeUtils}.
 *
 * @author Simon Hunt
 */
public class FakeTimeUtilsTest {

    private static final String HMS_ZERO = "00:00:00.000";
    private static final String HMS_TEN = "00:00:00.010";
    private static final String HMS_TWENTY = "00:00:00.020";
    private static final String HMS_FIVE_K = "00:00:05.000";
    private static final long FIVE_K = 5000;

    FakeTimeUtils fake;
    TimeUtils time;

    @Test
    public void basic() {
        fake = FakeTimeUtils.getInstance();
        time = fake.timeUtils();

        long t0 = fake.getCurrent();
        long t1 = time.currentTimeMillis();
        long t2 = time.currentTimeMillis();
        long t3 = time.currentTimeMillis();
        long tfinal = fake.getCurrent();

        assertEquals(AM_NEQ, 0, t1 - t0);
        assertEquals(AM_NEQ, 10, t2 - t0);
        assertEquals(AM_NEQ, 20, t3 - t0);
        assertEquals(AM_NEQ, 10, tfinal - t3);

        assertEquals(AM_NEQ, HMS_ZERO, time.hhmmssnnn(t1));
        assertEquals(AM_NEQ, HMS_TEN, time.hhmmssnnn(t2));
        assertEquals(AM_NEQ, HMS_TWENTY, time.hhmmssnnn(t3));
    }

    @Test
    public void auto10() {
        fake = FakeTimeUtils.getInstance(Advance.AUTO_10);
        time = fake.timeUtils();

        long t1 = time.currentTimeMillis();
        long t2 = time.currentTimeMillis();
        long t3 = time.currentTimeMillis();

        assertEquals(AM_NEQ, HMS_ZERO, time.hhmmssnnn(t1));
        assertEquals(AM_NEQ, HMS_TEN, time.hhmmssnnn(t2));
        assertEquals(AM_NEQ, HMS_TWENTY, time.hhmmssnnn(t3));
    }

    @Test
    public void manual() {
        fake = FakeTimeUtils.getInstance(Advance.MANUAL);
        time = fake.timeUtils();

        long t1 = time.currentTimeMillis();
        long t2 = time.currentTimeMillis();
        long t3 = time.currentTimeMillis();

        assertEquals(AM_NEQ, HMS_ZERO, time.hhmmssnnn(t1));
        assertEquals(AM_NEQ, HMS_ZERO, time.hhmmssnnn(t2));
        assertEquals(AM_NEQ, HMS_ZERO, time.hhmmssnnn(t3));
    }

    @Test
    public void advancement() {
        fake = FakeTimeUtils.getInstance(Advance.MANUAL);
        time = fake.timeUtils();

        long t1 = time.currentTimeMillis();
        assertEquals(AM_NEQ, HMS_ZERO, time.hhmmssnnn(t1));
        fake.advanceMs(FIVE_K);
        long t2 = time.currentTimeMillis();
        assertEquals(AM_NEQ, HMS_FIVE_K, time.hhmmssnnn(t2));
    }
}
