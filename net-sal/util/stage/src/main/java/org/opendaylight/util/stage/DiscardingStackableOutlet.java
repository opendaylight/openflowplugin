/*
 * (c) Copyright 2010 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.util.stage;

/**
 * Abstraction of a stackable outlet capable of discarding accepted items to a
 * side outlet, in addition to being able to feed a downstream outlet with the
 * processed items.
 * 
 * @author Thomas Vachuska
 * 
 * @param <T> type of item accepted/taken by the outlet
 * @param <P> type of item produced by the outlet
 */
public interface DiscardingStackableOutlet<T, P> extends StackableOutlet<T, P> {

    /**
     * Sets the specified outlet as a discard outlet to which discarded items
     * are to be forwarded.
     * 
     * @param discardOutlet new discard outlet
     */
    public void setDiscardOutlet(Outlet<T> discardOutlet);

    /**
     * Gets the current discard outlet.
     * 
     * @return current discard outlet
     */
    public Outlet<T> getDiscardOutlet();

}
