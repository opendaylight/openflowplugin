/*
 * (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */



package org.opendaylight.of.controller;

import org.opendaylight.of.lib.mp.MBodyFlowStats;

import java.util.List;

/**
 * Placeholder for aggregation of meta data with {@link MBodyFlowStats}
 * elements for the purposes of sending the data
 * to the UI in a single transaction.
 *
 * @author Simon Hunt
 */
public interface MetaFlow {
    /**
     * Returns the list of flow statistics elements.
     *
     * @return the flow statistics
     */
    List<MBodyFlowStats> flowStats();
}
