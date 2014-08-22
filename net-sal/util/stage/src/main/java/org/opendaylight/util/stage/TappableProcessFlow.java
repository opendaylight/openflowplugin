/*
 * (c) Copyright 2010 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.util.stage;

import java.util.Set;

/**
 * Abstraction of a sequence of {@link ProcessStage process stages},
 * interconnected to form a process flow, which as a whole can be viewed as a
 * single process stage. The stages are connected indirectly at their inlets
 * as well as at their outlets, via in {@link FanoutOutlet fan-out outlets},
 * thus allowing the output of each discrete stage to be tapped, allowing
 * multiple stages to execute in parallel and allowing stages to join or leave
 * the process flow in flight in a dynamic and extensible-at-runtime fashion.
 * 
 * @author Thomas Vachuska
 */
public interface TappableProcessFlow extends FanoutProcessFlow {
    
    /**
     * Gets the set of outlets currently tapping the the specified process
     * stage outlet.
     * 
     * @param <P> type of items produced by the process stage outlet
     * @param stageClass class of the process stage
     * @param tappedStageOutlet process stage instance whose taps are
     *        requested
     * @return unmodifiable set of outlets which are currently tapping the the
     *         given process flow stage
     */
    public <P> Set<Outlet<P>> getTaps(Class<? extends ProcessStageOutlet<?, P>> stageClass,
                                      ProcessStageOutlet<?, P> tappedStageOutlet);

    /**
     * Gets the number of of outlets currently tapping the the specified
     * process stage outlet.
     * 
     * @param <P> type of items produced by the process stage outlet
     * @param stageClass class of the process stage
     * @param tappedStageOutlet process stage instance whose taps are
     *        requested
     * @return number of outlets currently tapping the the given process flow
     *         stage
     */
    public <P> int getTapCount(Class<? extends ProcessStageOutlet<?, P>> stageClass,
                               ProcessStageOutlet<?, P> tappedStageOutlet);

    /**
     * Adds the given tap to the outlet of the specified process stage.
     * 
     * @param <P> type of items produced by the process stage being tapped
     * @param stageClass class of the process stage where the tapped stage
     *        outlet resides
     * @param tappedStageOutlet process stage instance whose outlet is to be
     *        tapped
     * @param tapOutlet tap outlet which should be added
     * @return true if the tap was added; false it the tap was already among
     *         the existing taps of this process flow stage outlet
     */
    public <P> boolean addTap(Class<? extends ProcessStageOutlet<?, P>> stageClass,
                              ProcessStageOutlet<?, P> tappedStageOutlet,
                              Outlet<P> tapOutlet);

    /**
     * Removes the given tap from the outlet of the specified process stage.
     * 
     * @param <P> type of items produced by the process stage being untapped
     * @param stageClass class of the process stage where the untapped stage
     *        outlet resides
     * @param tappedStageOutlet process stage instance whose outlet is to be
     *        untapped
     * @param tapOutlet tap outlet which should be removed
     * @return true if the tap was removed; false it the tap was not among the
     *         taps of this process flow stage outlet
     * @throws IllegalArgumentException if an attempt is made to remove a
     *         downstream process stage outlet as a tap
     */
    public <P> boolean removeTap(Class<? extends ProcessStageOutlet<?, P>> stageClass,
                                 ProcessStageOutlet<?, P> tappedStageOutlet,
                                 Outlet<P> tapOutlet);
    
}
