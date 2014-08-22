/*
 * (c) Copyright 2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.of.controller.flow.impl;

import org.opendaylight.of.controller.flow.MeterEvent;
import org.opendaylight.of.controller.flow.MeterEventType;
import org.opendaylight.of.lib.dt.DataPathId;
import org.opendaylight.of.lib.msg.OfmMeterMod;
import org.opendaylight.util.event.AbstractTypedEvent;

/**
 * Implementation of MeterEvent.
 *
 * @author Simon Hunt
 */
class MeterEvt extends AbstractTypedEvent<MeterEventType> implements MeterEvent {

    static final String DPID_LABEL = ",dpid=";
    static final String MM_LABEL = ",mm=";

    private final DataPathId dpid;
    private final OfmMeterMod mm;

    /**
     * Constructs a meter event of the given type, for the given datapath and
     * <em>MeterMod</em> message.
     *
     * @param type the type of event
     * @param dpid the target datapath
     * @param mm the <em>MeterMod</em> message that was pushed
     */
    MeterEvt(MeterEventType type, DataPathId dpid, OfmMeterMod mm) {
        super(type);
        this.dpid = dpid;
        this.mm = mm;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(super.toString());
        int n = sb.length();
        sb.replace(n - 1, n, DPID_LABEL).append(dpid)
                .append(MM_LABEL).append(mm).append("}");
        return sb.toString();
    }

    @Override
    public DataPathId dpid() {
        return dpid;
    }

    @Override
    public OfmMeterMod meterMod() {
        return mm;
    }
}
