/*
 * (c) Copyright 2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */


package org.opendaylight.of.controller.pipeline.impl;

import org.opendaylight.of.controller.pipeline.PipelineReader;
import org.opendaylight.of.lib.dt.DataPathId;

/**
 * Extension of {@link PipelineReader} that includes the ability to remove
 * definitions.
 *
 * @author Simon Hunt
 */
public interface PipelineMgmt extends PipelineReader {
    /** Removes the pipeline definition for the given datapath.
     *
     * @param dpid the datapath
     */
    void removeDefinition(DataPathId dpid);
}
