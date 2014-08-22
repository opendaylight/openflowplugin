/*
 * (c) Copyright 2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.of.controller.flow.impl;

import org.opendaylight.of.controller.flow.GroupEvent;
import org.opendaylight.of.controller.flow.GroupEventType;
import org.opendaylight.of.lib.dt.DataPathId;
import org.opendaylight.of.lib.msg.OfmGroupMod;
import org.opendaylight.util.event.AbstractTypedEvent;

/**
 * Implementation of GroupEvent.
 *
 * @author Simon Hunt
 */
class GroupEvt extends AbstractTypedEvent<GroupEventType> implements GroupEvent {

    static final String DPID_LABEL = ",dpid=";
    static final String GM_LABEL = ",gm=";

    private final DataPathId dpid;
    private final OfmGroupMod gm;

    /**
     * Constructs a group event of the given type, for the given datapath and
     * <em>GroupMod</em> message.
     *
     * @param type the type of event
     * @param dpid the target datapath
     * @param gm the <em>GroupMod</em> message that was pushed
     */
    GroupEvt(GroupEventType type, DataPathId dpid, OfmGroupMod gm) {
        super(type);
        this.dpid = dpid;
        this.gm = gm;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(super.toString());
        int n = sb.length();
        sb.replace(n - 1, n, DPID_LABEL).append(dpid)
                .append(GM_LABEL).append(gm).append("}");
        return sb.toString();
    }

    @Override
    public DataPathId dpid() {
        return dpid;
    }

    @Override
    public OfmGroupMod groupMod() {
        return gm;
    }
}
