/*
 * Copyright (c) 2014 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.net.facet;

import org.opendaylight.util.driver.Facet;
import org.opendaylight.of.controller.pipeline.PipelineDefinition;
import org.opendaylight.of.lib.ProtocolVersion;
import org.opendaylight.of.lib.msg.OfmFlowMod;
import org.opendaylight.util.driver.HandlerFacet;

import java.util.Set;

/**
 * A generic {@link Facet} that provides adjustment of flow mods based on
 * specific device information.
 *
 * @author Julie Britt
 */
public interface FlowModAdjuster extends Facet {

    /**
     * Sets the Pipeline Definition from which the facet will retrieve
     * information about the table properties on the device for which the flow
     * mods are being generated.
     *
     * @param tableProps specifying capabilities of the device
     * @param pv         indicates the Openflow version running on the device
     * @param isHybrid   indicates whether the controller is in hybrid mode
     */
    public void setTableProperties(PipelineDefinition tableProps,
                                   ProtocolVersion pv, boolean isHybrid);

    /**
     * Adjusts the given FlowMod based on the table properties of this device.
     * This entails adjusting the FlowMod as per the capabilities of the device.
     * Modifications will be documented in the Flare SDK documentation. This
     * method returns the proper FlowMod or FlowMods that should be sent.
     * Decision rests with the caller to actually send the adjusted flows. In
     * general, the code will determine if the flow can be put in any table as
     * is. If so, it will put the flow into that table. If that table is a
     * software table, it will additionally put a goto flow in a hardware table
     * that modifies the match statements to be more generic and points to the
     * software table for just that match. If no table can be identified to
     * support the flow as is, modifications will need to be made to the flow on
     * a device-specific basis.
     *
     * @param flow the FlowMod object to be sent
     * @return Set of flow mods that should be sent to the device to accomplish
     * the intended flow mod action(s) within the parameters of the
     * device's supported tables
     * @throws FlowUnsupportedException if it is an unsupported flow
     */
    public Set<OfmFlowMod> adjustFlowMod(OfmFlowMod flow);

    /**
     * Create a set of default flows for a particular device. These flows will
     * be created with inherent knowledge about what the device supports and
     * some information about its current configuration.
     *
     * @return Set of flows that need to be pushed to the device upon
     * registration with the controller
     * @throws FlowUnsupportedException if default flow cannot be handled
     */
    public Set<OfmFlowMod> generateDefaultFlows();

}
