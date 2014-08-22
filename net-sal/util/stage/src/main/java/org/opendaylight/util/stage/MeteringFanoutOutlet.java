/*
 * (c) Copyright 2010 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.util.stage;

import org.opendaylight.util.ThroughputTracker;

/**
 * Simple aggregating outlet that meters the incoming items and delegates them
 * via the {@code accept} method to the outlets that have been aggregated via
 * the {@link MeteringFanoutOutlet#add add} method.
 * 
 * @author Thomas Vachuska
 * 
 * @param <T> type of item accepted/taken by the fanout outlet
 */
public class MeteringFanoutOutlet<T> extends FanoutOutlet<T> {
    
    private ThroughputTracker tracker = new ThroughputTracker();
    
    /**
     * Gets the item throughput tracker.
     * 
     * @return item throughput tracker
     */
    public ThroughputTracker getTracker() {
        return tracker;
    }
    
    /**
     * {@inheritDoc}
     * <p>
     * Takes tally the incoming item to track throughput. 
     */
    @Override
    public boolean accept(T item) {
        tracker.add(+1);
        return super.accept(item);
    }

}
