/*
 * (c) Copyright 2010 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.util.stage;

import java.util.Set;

import org.opendaylight.util.ThroughputTracker;


/**
 * Abstraction of a sequence of {@link ProcessStage process stages},
 * interconnected to form a process flow, which as a whole can be viewed as a
 * single process stage. The stages are connected indirectly via
 * {@link MeteringFanoutOutlet fan-out outlets}, thus allowing multiple stages to
 * execute in parallel and allowing stages to join or leave the process flow
 * in flight in a dynamic and extensible-at-runtime fashion.
 * 
 * @author Thomas Vachuska
 */
public interface FanoutProcessFlow extends ProcessFlow {
    
    /**
     * Gets the set of outlets that are currently part of the flow at the
     * given stage.
     * 
     * @param <T> type of items consumed by the process stage outlet
     * @param stageClass class of the process stage for which the set
     *        of process stage instances is to be returned
     * @return unmodifiable set of process state outlets currently servicing
     *         the given process flow stage; null if there is no stage
     *         corresponding to the given class
     */
    public <T> Set<Outlet<T>> getOutlets(Class<? extends ProcessStageOutlet<T, ?>> stageClass);

    /**
     * Gets the throughput tracker associated with the metering fan-out of the
     * given process stage.
     * 
     * @param stageClass class of the process stage
     * @return item throughput tracker associated with this process stage
     *         fan-out
     */
    public ThroughputTracker getStageTracker(Class<? extends ProcessStageOutlet<?, ?>> stageClass);

}
