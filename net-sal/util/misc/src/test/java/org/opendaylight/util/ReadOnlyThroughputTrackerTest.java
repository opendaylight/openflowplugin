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
public class ReadOnlyThroughputTrackerTest {

    @Test
    public void testBasics() {
        ThroughputTracker tt = new ThroughputTracker();
        ThroughputTracker rott = new ReadOnlyThroughputTracker(tt);
        assertEquals("incorrect number of bytes", 0L, rott.total());
        assertEquals("incorrect throughput", 0.0, rott.throughput(), 0.0001);
        tt.add(1234567890L);
        assertEquals("incorrect number of bytes", 1234567890L, rott.total());
        assertTrue("incorrect throughput", 1234567890.0 < rott.throughput());
        delay(1500);
        tt.add(1L);
        assertEquals("incorrect number of bytes", 1234567891L, rott.total());
        assertTrue("incorrect throughput", 1234567891.0 > rott.throughput());
        tt.reset();
        assertEquals("incorrect number of bytes", 0L, rott.total());
        assertEquals("incorrect throughput", 0.0, rott.throughput(), 0.0001);
    }
    
    @Test(expected=UnsupportedOperationException.class)
    public void testReset() {
        new ReadOnlyThroughputTracker(new ThroughputTracker()).reset();
    }

    @Test(expected=UnsupportedOperationException.class)
    public void testFreeze() {
        new ReadOnlyThroughputTracker(new ThroughputTracker()).freeze();
    }

    @Test(expected=UnsupportedOperationException.class)
    public void testAdd() {
        new ReadOnlyThroughputTracker(new ThroughputTracker()).add(10);
    }
}
