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
 * Simple implementation of {@link MetaFlow}.
 *
 * @author Simon Hunt
 */
public class MetaFlowData implements MetaFlow {

    private final List<MBodyFlowStats> flowStats;

    public MetaFlowData(List<MBodyFlowStats> fs) {
        flowStats = fs;
    }

    @Override
    public List<MBodyFlowStats> flowStats() {
        return flowStats;
    }
}
