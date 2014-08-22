/*
 * (c) Copyright 2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.of.controller.flow.impl;

import org.opendaylight.of.controller.flow.FlowEvent;
import org.opendaylight.of.controller.flow.FlowEventType;
import org.opendaylight.of.lib.dt.DataPathId;
import org.opendaylight.of.lib.msg.OfmFlowMod;
import org.opendaylight.of.lib.msg.OfmFlowRemoved;
import org.opendaylight.util.event.AbstractTypedEvent;

/**
 * Implementation of FlowEvent.
 *
 * @author Simon Hunt
 */
class FlowEvt extends AbstractTypedEvent<FlowEventType> implements FlowEvent {

    static final String DPID_LABEL = ",dpid=";
    static final String FM_LABEL = ",fm=";
    static final String FR_LABEL = ",fr=";

    private final DataPathId dpid;
    private final OfmFlowMod fm;
    private final OfmFlowRemoved fr;

    /**
     * Constructs a flow event of the given type, for the given datapath and
     * <em>FlowMod</em> message.
     *
     * @param type the type of event
     * @param dpid the target datapath
     * @param fm the <em>FlowMod</em> message that was pushed
     */
    FlowEvt(FlowEventType type, DataPathId dpid, OfmFlowMod fm) {
        super(type);
        this.dpid = dpid;
        this.fm = fm;
        this.fr = null;
    }

    /**
     * Constructs a flow event of the given type, for the given datapath and
     * <em>FlowRemoved</em> message.
     *
     * @param type the type of event
     * @param dpid the source datapath
     * @param fr the <em>FlowRemoved</em> message that was received
     */
    FlowEvt(FlowEventType type, DataPathId dpid, OfmFlowRemoved fr) {
        super(type);
        this.dpid = dpid;
        this.fm = null;
        this.fr = fr;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(super.toString());
        int n = sb.length();
        sb.replace(n - 1, n, DPID_LABEL).append(dpid);
        if (fm != null)
            sb.append(FM_LABEL).append(fm);
        if (fr != null)
            sb.append(FR_LABEL).append(fr);
        sb.append("}");
        return sb.toString();
    }

    @Override
    public DataPathId dpid() {
        return dpid;
    }

    @Override
    public OfmFlowMod flowMod() {
        return fm;
    }

    @Override
    public OfmFlowRemoved flowRemoved() {
        return fr;
    }
}
