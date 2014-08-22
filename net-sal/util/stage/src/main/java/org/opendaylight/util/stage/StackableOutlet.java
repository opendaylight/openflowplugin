/*
 * (c) Copyright 2010 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.util.stage;

/**
 * Abstraction of an outlet fitted with a downstream outlet. Such outlets are
 * capable of being assembled (or stacked) into various sequences.
 * <p>
 * The outlet takes (accepts) items of type {@code T} and produces items of
 * type {@code P}, which it forwards on its downstream outlet.
 * 
 * @author Thomas Vachuska
 * @author Simon Hunt
 * 
 * @param <T> type of item accepted/taken by the outlet
 * @param <P> type of item produced by the outlet onto the downstream outlet
 */
public interface StackableOutlet<T, P> extends Outlet<T> {

    /**
     * Sets the specified outlet as a downstream outlet to which entities
     * processed by this outlet will be forwarded.
     * 
     * @param outlet new downstream outlet
     */
    public void setOutlet(Outlet<P> outlet);

    /**
     * Gets the current downstream outlet.
     * 
     * @return current downstream outlet
     */
    public Outlet<P> getOutlet();


    /** This predicate returns true if there is no downstream outlet; that is, if
     * {@link #getOutlet} returns null.
     *
     * @return true if there is no downstream outlet; false otherwise
     */
    public boolean isTerminal();

}
