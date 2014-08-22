/*
 * (c) Copyright 2007 Hewlett-Packard Co., All Rights Reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.util;

import static org.opendaylight.util.junit.TestTools.delay;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/**
 * Suite of tests for the throughput tracking abstraction.
 * 
 * @author Thomas Vachuska
 */
public class ThroughputTrackerTest {

    @Test
    public void basics() {
        ThroughputTracker tt = new ThroughputTracker();
        assertEquals("incorrect number of bytes", 0L, tt.total());
        assertEquals("incorrect throughput", 0.0, tt.throughput(), 0.0001);
        tt.add(1234567890L);
        assertEquals("incorrect number of bytes", 1234567890L, tt.total());
        assertTrue("incorrect throughput", 1234567890.0 < tt.throughput());
        delay(1500);
        tt.add(1L);
        assertEquals("incorrect number of bytes", 1234567891L, tt.total());
        assertTrue("incorrect throughput", 1234567891.0 > tt.throughput());
        tt.reset();
        assertEquals("incorrect number of bytes", 0L, tt.total());
        assertEquals("incorrect throughput", 0.0, tt.throughput(), 0.0001);
    }
    
    @Test
    public void freeze() {
        ThroughputTracker tt = new ThroughputTracker();
        tt.add(123L);
        assertEquals("incorrect number of bytes", 123L, tt.total());
        delay(1000);
        tt.freeze();
        tt.add(123L);
        assertEquals("incorrect number of bytes", 123L, tt.total());

        double d = tt.duration();
        double t = tt.throughput();
        assertEquals("incorrect duration", d, tt.duration(), 0.0001);
        assertEquals("incorrect throughput", t, tt.throughput(), 0.0001);
        assertEquals("incorrect number of bytes", 123L, tt.total());
    }
    
    @Test
    public void reset() {
        ThroughputTracker tt = new ThroughputTracker();
        tt.add(123L);
        assertEquals("incorrect number of bytes", 123L, tt.total());

        double d = tt.duration();
        double t = tt.throughput();
        assertEquals("incorrect duration", d, tt.duration(), 0.0001);
        assertEquals("incorrect throughput", t, tt.throughput(), 0.0001);
        assertEquals("incorrect number of bytes", 123L, tt.total());
        
        tt.reset();
        assertEquals("incorrect throughput", 0.0, tt.throughput(), 0.0001);
        assertEquals("incorrect number of bytes", 0, tt.total());
    }

    @Test
    public void syntheticTracker() {
        long then = System.currentTimeMillis() - 1000;
        ThroughputTracker tt = new ThroughputTracker(then, 1000, true);
        assertEquals("incorrect duration", 1, tt.duration(), 0.1);
        assertEquals("incorrect throughput", 1000, tt.throughput(), 1.0);
    }
}
