/*
 * (c) Copyright 2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.of.controller.impl;

import org.opendaylight.of.controller.DataPathEvent;
import org.opendaylight.of.controller.OpenflowEventType;
import org.opendaylight.of.lib.ProtocolVersion;
import org.opendaylight.of.lib.dt.DataPathId;
import org.opendaylight.util.net.IpAddress;

/**
 * A datapath event.
 *
 * @author Simon Hunt
 */
class DataPathEvt extends OpenflowEvt implements DataPathEvent {

    static final String PV_LABEL = ",pv=";
    static final String DPID_LABEL = ",dpid=";
    static final String IP_LABEL = ",ip=";

    private final DataPathId dpid;
    private final ProtocolVersion pv;
    private final IpAddress ip;

    /** Constructs the datapath event.
     *
     * @param type the event type
     * @param dpid the ID of the associated datapath
     * @param pv the protocol version negotiated with the datapath
     * @param ip the IP address of the datapath
     */
    DataPathEvt(OpenflowEventType type, DataPathId dpid, ProtocolVersion pv,
                IpAddress ip) {
        super(type);
        this.dpid = dpid;
        this.pv = pv;
        this.ip = ip;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(super.toString());
        int n = sb.length();
        sb.replace(n-1, n, PV_LABEL).append(pv)
                .append(DPID_LABEL).append(dpid)
                .append(IP_LABEL).append(ip)
                .append("}");
        return sb.toString();
    }

    @Override
    public DataPathId dpid() {
        return dpid;
    }

    @Override
    public ProtocolVersion negotiated() {
        return pv;
    }

    @Override
    public IpAddress ip() {
        return ip;
    }

}
