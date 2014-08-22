/*
 * (c) Copyright 2007 Hewlett-Packard Co., All Rights Reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.util;

/**
 * Auxiliary to create a read-only view of a throughput tracker.
 * 
 * @author Thomas Vachuska
 */
public class ReadOnlyThroughputTracker extends ThroughputTracker {
    
    private ThroughputTracker tt;

    /**
     * Create a read-only throughput tracker backed by the another throughput
     * tracker.
     * 
     * @param tt backing tracker to be made read-only
     */
    public ReadOnlyThroughputTracker(ThroughputTracker tt) {
        this.tt = tt;
    }

    /**
     * Disabled reset operation.
     * 
     * @throws UnsupportedOperationException if attempted on read-only tracker
     */
    @Override
    public synchronized void reset() {
        throw new UnsupportedOperationException("Unable to add to read-only tracker");
    }

    /**
     * Disabled freeze operation.
     * 
     * @throws UnsupportedOperationException if attempted on read-only tracker
     */
    @Override
    public synchronized void freeze() {
        throw new UnsupportedOperationException("Unable to add to read-only tracker");
    }

    /**
     * Disabled add operation.
     * 
     * @throws UnsupportedOperationException if attempted on read-only tracker
     */
    @Override
    public synchronized void add(long count) {
        throw new UnsupportedOperationException("Unable to add to read-only tracker");
    }

    @Override
    public synchronized double throughput() {
        return tt.throughput();
    }

    @Override
    public synchronized long total() {
        return tt.total();
    }

    @Override
    public synchronized double duration() {
        return tt.duration();
    }

}
