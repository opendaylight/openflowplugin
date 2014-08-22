/*
 * (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */



package org.opendaylight.of.controller;

import org.opendaylight.of.controller.pipeline.PipelineDefinition;
import org.opendaylight.of.lib.dt.DataPathInfo;
import org.opendaylight.of.lib.msg.OfmFlowMod;

import java.util.List;

/**
 * An API for the processing of flow mods. The implementer is responsible for
 * providing the appropriate default flow mods, or adjusting a given flow mod,
 * as is most appropriate for the specified datapath ID. It is assumed that
 * device specific code will be used behind the scenes.
 *
 * @author Simon Hunt
 * @author Thomas Vachuska
 */
public interface FlowModAdvisor {

    /**
     * Returns the default set of flow mods to be installed on the
     * specified datapath. The "default" flows are those to be installed
     * when the datapath first connects to the controller.
     *
     * @param dpi                target datapath information
     * @param contributedFlows   list of flows from initial flow contributors
     * @param pipelineDefinition device table pipeline definition
     * @param isHybrid           indicates if controller is running in hybrid mode
     * @return the default flow mods
     */
    List<OfmFlowMod> getDefaultFlowMods(DataPathInfo dpi,
                                        List<OfmFlowMod> contributedFlows,
                                        PipelineDefinition pipelineDefinition,
                                        boolean isHybrid);

    /**
     * Adjusts the specified flowmod for the specified datapath, as might be
     * necessary based on the nuances of the device, returning one or more
     * flowmods as a result. Note that more than one flowmod might be returned
     * if, for example, the requested action needs to be "split" across tables
     * because of the limitations of the switch implementation.
     *
     * @param dpi target datapath information
     * @param fm  the flowmod to adjust
     * @return one or more adjusted flowmods
     */
    List<OfmFlowMod> adjustFlowMod(DataPathInfo dpi, OfmFlowMod fm);

}
