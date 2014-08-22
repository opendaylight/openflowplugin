/*
 * (c) Copyright 2010 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.util;

import static org.opendaylight.util.junit.TestTools.delay;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Date;

import org.junit.Before;
import org.junit.Test;

/**
 * Test suite for the Clock facade utility.
 * 
 * @author Thomas Vachuska
 */
public class ClockTest {
    
    private static final long HOURS = 3600 * 1000L;
    private static final int DELAY = 100;
    
    @Before
    public void setUp() {
        Clock.resetSourceUnderPenaltyOfDeath();
    }

    @Test
    public void latchRealClock() {
        Clock.assertRealTimeSource();
        Clock.assertRealTimeSource();
        try {
            Clock.assertTestTimeSource();
            fail("failed to latch real time source");
        } catch (IllegalStateException e) {
        }
        
        assertFalse("warping not supported", Clock.source().supportsWarping());

        try {
            Clock.source().setCurrentTimeMillis(12345);
            fail("failed to ignore setting time warp");
        } catch (UnsupportedOperationException e) {
        }

        try {
            Clock.source().setTimeOffset(12345);
            fail("failed to ignore setting time warp offset");
        } catch (UnsupportedOperationException e) {
        }
    }

    @Test
    public void latchTestClock() {
        Clock.assertTestTimeSource();
        Clock.assertTestTimeSource();
        try {
            Clock.assertRealTimeSource();
            fail("failed to latch test time source");
        } catch (IllegalStateException e) {
        }

        assertTrue("warping supported", Clock.source().supportsWarping());
    }
    
    @Test
    public void testRealClockTime() {
        Clock.assertRealTimeSource();
        long st = System.currentTimeMillis();
        long ct = Clock.currentTimeMillis();
        
        assertTrue("system clock " + st + " and facade clock " + ct + 
                   " should be near identical",
                   st <= ct && ct <= st + (DELAY * 10));
    }

    @Test
    public void testRealClockDate() {
        Clock.assertRealTimeSource();
        Date sd = new Date();
        Date cd = Clock.date();
        
        assertTrue("system date " + sd + " and facade date " + cd + 
                   " should be near identical",
                   sd.getTime() <= cd.getTime() && 
                   cd.getTime() <= sd.getTime() + (DELAY * 10));
    }

    @Test
    public void negativeTimeOffset() {
        Clock.assertTestTimeSource();
        
        // Set the clock to be 10 hours behind the real one
        Clock.source().setTimeOffset(-10 * HOURS);
        
        // And verify that the fake now falls before real five hours ago
        long fakeNow = Clock.currentTimeMillis();
        long fiveHoursAgo = System.currentTimeMillis() - 5 * HOURS;
        assertTrue("fake now " + fakeNow + 
                   " should be before real past " + fiveHoursAgo, 
                   fakeNow < fiveHoursAgo);
    }
    
    @Test
    public void fakeNowInPast() {
        Clock.assertTestTimeSource();
        
        // Set the clock current time to be relative to 10 hours ago 
        long tenHoursAgo = System.currentTimeMillis() - 10 * HOURS;
        Clock.source().setCurrentTimeMillis(tenHoursAgo);
        
        // And verify that the fake now falls before real five hours ago
        long fiveHoursAgo = System.currentTimeMillis() - 5 * HOURS;
        long fakeNow = Clock.currentTimeMillis();
        assertTrue("fake now " + fakeNow + 
                   " should be before real future " + fiveHoursAgo, 
                   fakeNow < fiveHoursAgo);
        
        // Delay a tiny bit and make sure that the time is properly relative
        // to the fake now, but still in the past.
        delay(DELAY);
        long t = Clock.currentTimeMillis();
        assertTrue("time " + t + " should be just after fake now " + fakeNow,
                   fakeNow < t && t < fakeNow + (DELAY * 10));
        assertTrue("time " + t + 
                   " should still be before real past " + fiveHoursAgo, 
                   t < fiveHoursAgo);
    }
    
    
    @Test
    public void positiveTimeOffset() {
        Clock.assertTestTimeSource();

        // Set the clock to be 10 hours ahead of the real one
        Clock.source().setTimeOffset(+10 * HOURS);
        
        // And verify that the fake now falls after real five hours later
        long fakeNow = Clock.currentTimeMillis();
        long fiveHoursAfter = System.currentTimeMillis() + 5 * HOURS;
        assertTrue("fake now " + fakeNow + 
                   " should be after real future " + fiveHoursAfter, 
                   fakeNow > fiveHoursAfter);
    }

    @Test
    public void fakeNowInFuture() {
        Clock.assertTestTimeSource();

        // Set the clock current time to be relative to 10 hours later
        long tenHoursAfter = System.currentTimeMillis() + 10 * HOURS;
        Clock.source().setCurrentTimeMillis(tenHoursAfter);
        
        long fiveHoursAfter = System.currentTimeMillis() + 5 * HOURS;
        long fakeNow = Clock.currentTimeMillis();
        assertTrue("fake now " + fakeNow + 
                   " should be after real future " + fiveHoursAfter, 
                   fakeNow > fiveHoursAfter);

        // Delay a tiny bit and make sure that the time is properly relative
        // to the fake now, but still in the future.
        delay(DELAY);
        long t = Clock.currentTimeMillis();
        assertTrue("time " + t + " should be just after fake now " + fakeNow,
                   fakeNow < t && t < fakeNow + (DELAY * 10));
        assertTrue("time " + t + 
                   " should still be after real future " + fiveHoursAfter, 
                   t > fiveHoursAfter);
    }

}
