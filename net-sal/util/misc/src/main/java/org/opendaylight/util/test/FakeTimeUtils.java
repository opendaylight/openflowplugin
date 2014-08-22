/*
 * (c) Copyright 2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.util.test;

import org.opendaylight.util.TimeUtils;

import java.util.Calendar;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Implements a fake {@link TimeUtils}, suitable for unit tests to carefully
 * control the time returned by {@link TimeUtils#currentTimeMillis()}.
 * <p>
 * The fake {@code TimeUtils} starts its clock at midnight January 1st, 2013,
 * and thereafter every call to {@code currentTimeMillis()} either:
 * <ul>
 *     <li>automatically advances the time by 10 milliseconds, or</li>
 *     <li>does not advance the time</li>
 * </ul>
 * In the latter case, it is up to the consumer to invoke {@link #advanceMs}
 * to change the time.
 * <p>
 * A production "event" class may look something like this (javadocs omitted
 * for conciseness):
 * <pre>
 * public class FooEvent {
 *     static TimeUtils TIME = TimeUtils.getInstance();
 *     ...
 *
 *     private final long ts = TIME.currentTimeMillis();
 *
 *     public FooEvent(...) {
 *         ...
 *     }
 *
 *     public long ts() {
 *         return ts;
 *     }
 *
 *     &#64;Override
 *     public String toString() {
 *         return "{" + TIME.hhmmssnnn(ts) + ... + "}";
 *     }
 * }
 * </pre>
 *
 * Notice how the {@code TimeUtils} instance is stored in a static,
 * package-private field. Under production conditions, the {@code ts}
 * instance field is set to system time during construction.
 * <p>
 * A unit test for the class might look something like this, using the
 * default behavior of {@link FakeTimeUtils} whereby the clock is advanced
 * 10ms after every call to {@code currentTimeMillis()}:
 * <pre>
 * public class FooEventTest {
 *     private FakeTimeUtils fake;
 *
 *     &#64;Before
 *     public void setUp() {
 *         fake = FakeTimeUtils.getInstance();
 *         FooEvent.TIME = fake.timeUtils();
 *     }
 *
 *     &#64;Test
 *     public void basic() {
 *         long startTs = fake.getCurrent();
 *         FooEvent e1 = new FooEvent();
 *         FooEvent e2 = new FooEvent();
 *         FooEvent e3 = new FooEvent();
 *         assertEquals(AM_NEQ, startTs, e1.ts());
 *         assertEquals(AM_NEQ, startTs + 10, e2.ts());
 *         assertEquals(AM_NEQ, startTs + 20, e3.ts());
 *     }
 * }
 * </pre>
 *
 * Alternatively, a unit test might wish to exert exact control over the
 * time values returned:
 * <pre>
 * public class FooEventTest {
 *     private static final long FIVE_K = 5000;
 *
 *     private FakeTimeUtils fake;
 *
 *     &#64;Before
 *     public void setUp() {
 *         fake = FakeTimeUtils.getInstance(Advance.MANUAL);
 *         FooEvent.TIME = fake.timeUtils();
 *     }
 *
 *     &#64;Test
 *     public void basic() {
 *         long startTs = fake.getCurrent();
 *         FooEvent e1 = new FooEvent();
 *         FooEvent e2 = new FooEvent();
 *         FooEvent e3 = new FooEvent();
 *         assertEquals(AM_NEQ, startTs, e1.ts());
 *         assertEquals(AM_NEQ, startTs, e2.ts());
 *         assertEquals(AM_NEQ, startTs, e3.ts());
 *
 *         fake.advanceMs(FIVE_K);
 *         FooEvent e4 = new FooEvent();
 *         assertEquals(AM_NEQ, startTs + FIVE_K, e4.ts());
 *     }
 * }
 * </pre>
 *
 *
 * @author Simon Hunt
 */

public final class FakeTimeUtils {

    private static final String E_NULL_PARAM = "Parameter cannot be null";

    /** Increment clock by 10ms per call. */
    public static final int TICK_SIZE = 10;

    /** Blank final for clock start value. */
    private static final long CLOCK_START;

    static {
        Calendar cal = Calendar.getInstance();
        cal.set(2013, Calendar.JANUARY, 1, 0, 0, 0);
        cal.set(Calendar.MILLISECOND, 0);
        CLOCK_START = cal.getTime().getTime();
    }

    private final AtomicLong nextTick = new AtomicLong(CLOCK_START);
    private final MyNow now = new MyNow();
    private final Advance advance;
    private TimeUtils timeUtils;

    /** Constructs a fake time-utils with the given clock advancement behavior.
     *
     * @param advance how to advance the clock
     */
    private FakeTimeUtils(Advance advance) {
        this.advance = advance;
    }

    /** Returns the patched time-utils instance to attach to the class
     * under test.
     *
     * @return the patched time-utils
     */
    public TimeUtils timeUtils() {
        return timeUtils;
    }

    /** Advances the clock by the specified number of milliseconds.
     *
     * @param ms the number of milliseconds to advance
     */
    public void advanceMs(long ms) {
        nextTick.addAndGet(ms);
    }

    /** Returns the current value of the clock, i.e.&nbsp;the value that
     * will be returned from the next invocation of {@code currentTimeMillis()}
     * on the {@link TimeUtils} instance.
     *
     * @return the current value of the clock
     */
    public long getCurrent() {
        return nextTick.get();
    }

    // =======

    /** Returns a fake TimeUtils instance with the behavior of advancing the
     * clock by 10ms after every call to {@link TimeUtils#currentTimeMillis}.
     *
     * @return the fake TimeUtils
     */
    public static FakeTimeUtils getInstance() {
        return getInstance(Advance.AUTO_10);
    }

    /** Returns a fake TimeUtils instance with the specified behavior.
     *
     * @param advance the required clock advancement behavior
     * @return fake TimeUtils
     */
    public static FakeTimeUtils getInstance(Advance advance) {
        if (advance == null)
            throw new NullPointerException(E_NULL_PARAM);
        FakeTimeUtils fake = new FakeTimeUtils(advance);
        fake.timeUtils = TimeUtils.getInstance(fake.now);
        return fake;
    }

    // =======

    /** Denotes how time will advance when
     * {@link TimeUtils#currentTimeMillis} is invoked.
     */
    public static enum Advance {
        /** Automatically advance the time by 10 milliseconds. */
        AUTO_10,
        /** Do not advance the time. It is expected that the consumer of
         * the class will advance the time manually with calls to
         * {@link FakeTimeUtils#advanceMs(long)}.
         */
        MANUAL
    }


    // inner class for the implementation of Now
    private class MyNow implements TimeUtils.Now {
        @Override
        public long currentTimeMillis() {
            return advance == Advance.AUTO_10
                ? FakeTimeUtils.this.nextTick.getAndAdd(TICK_SIZE)
                : FakeTimeUtils.this.nextTick.get();
        }
    }
}
