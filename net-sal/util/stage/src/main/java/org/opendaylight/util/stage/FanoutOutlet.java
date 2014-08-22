/*
 * (c) Copyright 2010 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.util.stage;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Simple aggregating outlet that delegates the item given via the
 * {@code accept} method to the outlets that have been aggregated via the
 * {@link FanoutOutlet#add add} method.
 * 
 * @author Thomas Vachuska
 * @param <T> type of item accepted/taken by the outlet
 */
public class FanoutOutlet<T> implements Outlet<T> {
    
    private final Set<Outlet<T>> outlets = new HashSet<Outlet<T>>();
    
    /**
     * Add the specified outlet to the aggregate outlet.
     * 
     * @param outlet outlet to be aggregated
     * @return true if the aggregation did not already contain the given outlet
     */
    public boolean add(Outlet<T> outlet) {
        synchronized(outlets) {
            return outlets.add(outlet);
        }
    }

    /**
     * Remove the specified outlet from the aggregate outlet.
     * 
     * @param outlet outlet to be removed from the aggregation
     * @return true if the aggregation contained the given outlet
     */
    public boolean remove(Outlet<T> outlet) {
        synchronized(outlets) {
            return outlets.remove(outlet);
        }
    }
    
    /**
     * Get the set of aggregated outlets.
     * 
     * @return unmodifiable set of the aggregated outlets
     */
    public Set<Outlet<T>> getOutlets() {
        synchronized(outlets) {
            return Collections.unmodifiableSet(outlets);
        }
    }
    
    /**
     * Get the size of the aggregation.
     * 
     * @return number of aggregated outlets
     */
    public int size() {
        return outlets.size();
    }

    /**
     * Delegates the accept invocation to all of the aggregated outlets. Each
     * outlet is guaranteed to have a chance to accept the item.
     * 
     * @return true if at least one outlet accepted the item; false otherwise
     */
    @Override
    public boolean accept(T item) {
        synchronized(outlets) {
            boolean accepted = false;
            for (Outlet<T> outlet : outlets)
                accepted |= outlet.accept(item);
            return accepted;
        }
    }

}
