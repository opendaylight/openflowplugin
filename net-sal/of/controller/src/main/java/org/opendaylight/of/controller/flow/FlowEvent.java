/*
 * (c) Copyright 2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */


package org.opendaylight.of.controller.flow;

import org.opendaylight.of.lib.dt.DataPathId;
import org.opendaylight.of.lib.msg.OfmFlowMod;
import org.opendaylight.of.lib.msg.OfmFlowRemoved;
import org.opendaylight.util.event.TypedEvent;

/**
 * Encapsulates a flow-related event.
 *
 * @author Simon Hunt
 */
public interface FlowEvent extends TypedEvent<FlowEventType> {

    /**
     * Returns the associated datapath ID.
     *
     * @return the datapath ID
     */
    DataPathId dpid();

    /**
     * Returns the <em>FlowMod</em> message that was pushed (or attempted).
     * If the event type is not {@code FLOW_MOD_PUSHED} or
     * {@code FLOW_MOD_PUSH_FAILED}, this method returns null.
     *
     * @return the pushed <em>FlowMod</em>
     */
    OfmFlowMod flowMod();

    /**
     * Returns the <em>FlowRemoved</em> message that was received from the
     * datapath.
     * If the event type is not {@code FLOW_REMOVED}, this method returns null.
     *
     * @return the <em>FlowRemoved</em> message
     */
    OfmFlowRemoved flowRemoved();
}
