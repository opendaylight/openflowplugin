/*
 * (c) Copyright 2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */


package org.opendaylight.of.controller.flow;

import org.opendaylight.of.lib.dt.DataPathId;
import org.opendaylight.of.lib.msg.OfmMeterMod;
import org.opendaylight.util.event.TypedEvent;

/**
 * Encapsulates a meter-related event.
 *
 * @author Simon Hunt
 */
public interface MeterEvent extends TypedEvent<MeterEventType> {

    /**
     * Returns the associated datapath ID.
     *
     * @return the datapath ID
     */
    DataPathId dpid();

    /**
     * Returns the <em>MeterMod</em> message that was pushed (or attempted).
     *
     * @return the pushed <em>MeterMod</em>
     */
    OfmMeterMod meterMod();

    // TODO: support for custom meters in 1.0 using experimenter messages
    /*
     * IMPLEMENTATION NOTE:
     * We should be wrapping the Experimenter message in an abstraction that
     * provides meter-related semantics, rather than expecting the consumer
     * to parse the raw byte array data.
     */
}
