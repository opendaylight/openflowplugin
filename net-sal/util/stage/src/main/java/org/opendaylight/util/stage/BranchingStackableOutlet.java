/*
 * (c) Copyright 2010 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.util.stage;

/**
 * Abstraction of a discarding stackable outlet capable of branching items to
 * a side outlet that are of a type {@code B} which is distinct from the input
 * {@code T} or output {@code P} types. The branch may be connected such that
 * it feeds items back into a process flow upstream, or such that it creates a
 * new downstream flow.
 * 
 * @author Simon Hunt
 * 
 * @param <T> type of item accepted/taken by the outlet
 * @param <P> type of item produced by the outlet
 * @param <B> type of alternate item produced by the outlet when branching
 */
public interface BranchingStackableOutlet<T, P, B> extends DiscardingStackableOutlet<T, P> {

    /**
     * Sets the specified outlet as a branch outlet to which branched items
     * are to be forwarded.
     *
     * @param branchOutlet new branch outlet
     */
    public void setBranchOutlet(Outlet<B> branchOutlet);

    /**
     * Gets the current branch outlet.
     *
     * @return current branch outlet
     */
    public Outlet<B> getBranchOutlet();


}
