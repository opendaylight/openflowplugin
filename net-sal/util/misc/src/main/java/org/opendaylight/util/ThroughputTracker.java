/*
 * (c) Copyright 2007 Hewlett-Packard Co., All Rights Reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.util;

/**
 * Utility class to accrue count of arbitrary entities and measure throughput.
 * Once frozen the tracker time will stop ticking and no more entities can be
 * added to it.
 * 
 * @author Thomas Vachuska
 */
public class ThroughputTracker {

    private long total = 0L;
    private long start = System.currentTimeMillis();
    private long end = 0L;
    
    /**
     * Create a new tracker in a reset state.
     */
    public ThroughputTracker() {
    }
    
    /**
     * Synthesize a throughput tracker from existing data.
     * 
     * @param start start time-stamp
     * @param total total number of items to start with
     * @param frozen true if the tracker should be immediately frozen
     */
    public ThroughputTracker(long start, long total, boolean frozen) {
        if (frozen)
            freeze();
        this.start = start;
        this.total = total;
    }

    /**
     * Reset the counts and restart the timer associated with the tracker.
     */
    public synchronized void reset() {
        end = total = 0L;
        start = System.currentTimeMillis();
    }
    
    /**
     * Freeze the current throughput tracker in the current state. No more
     * bytes can be added to it, and no more time will elapse.  The tracker
     * may not be reset to start tracking throughput again.
     */
    public synchronized void freeze() {
        end = System.currentTimeMillis();
    }
    
    /**
     * Add the specified number of entities to the tracker.  No-op if the 
     * tracker has been frozen.
     * 
     * @param count number of entities to consider as transfered
     */
    public synchronized void add(long count) {
        if (end == 0L)
            total += count;
    }
    
    /**
     * Get the number of entities transferred per second.
     * 
     * @return entities per second throughput
     */
    public synchronized double throughput() {
        return total / duration();
    }
    
    /**
     * Get the number of entities accumulated by the tracker.
     * 
     * @return number of entities previously added to the tracker via add method
     */
    public synchronized long total() {
        return total;
    }
    
    /**
     * Number of seconds elapsed since the tracker was last reset. 
     * 
     * @return fractional number of seconds since the last reset
     */
    public synchronized double duration() {
        long duration = (end == 0L ? System.currentTimeMillis() : end) - start;
        //  Protect against 0 return by artificially setting duration to 1ms
        if (duration == 0)
            duration = 1;
        return duration / 1000.0;
    }
    
}
