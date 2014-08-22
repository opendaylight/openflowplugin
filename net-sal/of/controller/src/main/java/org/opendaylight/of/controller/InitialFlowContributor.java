/*
 * (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */



package org.opendaylight.of.controller;

import org.opendaylight.of.lib.dt.DataPathInfo;
import org.opendaylight.of.lib.msg.OfmFlowMod;

import java.util.List;

/**
 * An API to allow components to contribute to the flow mod messages that will
 * be pushed to newly connected datapaths. The callback is invoked each time
 * a datapath connects to the controller.
 * <p>
 * Note that flow classes must be pre-registered for the flows that will
 * be provided by the contributor.
 *
 * @see org.opendaylight.of.controller.ControllerService#registerFlowClass
 *
 * @author Simon Hunt
 * @author Thomas Vachuska
 */
public interface InitialFlowContributor {

    /**
     * Callback invoked when the controller is about to push initial flows
     * down to a newly connected datapath. The returned list of flow mods
     * will be installed on the new datapath.
     *
     * @param info the info for the newly connected datapath
     * @param isHybrid true if the controller is in hybrid mode
     * @return the list of flow mods to be installed
     */
    List<OfmFlowMod> provideInitialFlows(DataPathInfo info, boolean isHybrid);
}
