/*
 * (c) Copyright 2010 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.util.stage;

/**
 * Abstraction of a {@link org.opendaylight.util.stage.ProcessStage process stage}
 * component capable of being assembled into a {@link ProcessFlow process
 * flow} sequence with other such components.
 * <p>
 * The overall characteristics, inherited from the super-interfaces, are as
 * follows:
 * <ul>
 * <li>acts as an outlet for others</li>
 * <li>has a downstream outlet for forwarding items down the chain</li>
 * <li>has a side outlet for discarding items out of the chain</li>
 * <li>is active in that it can be stopped, started</li>
 * <li>and supports predicates for notions of being stopped, finished, idle
 * and empty</li>
 * </ul>
 * 
 * @author Thomas Vachuska
 * @param <T> type of items accepted by this stage outlet
 * @param <P> type of items produced by this stage outlet onto the downstream
 *        outlet
 */
public interface ProcessStageOutlet<T, P> extends
                            ProcessStage, DiscardingStackableOutlet<T, P> {

    /**
     * Return true if the outlet does not contain any pending requests. If an
     * outlet is not empty, the process stage must not be idle or finished.
     * Conversely, idle or finished process stage implies that the outlet is
     * empty.
     * 
     * @return true if the outlet is empty; false otherwise
     */
    public boolean isEmpty();

    /**
     * Get the number of pending items accepted by the outlet, but not yet
     * processed.
     * 
     * @return number of accepted items, not yet processed by the outlet
     */
    public int size();

}
