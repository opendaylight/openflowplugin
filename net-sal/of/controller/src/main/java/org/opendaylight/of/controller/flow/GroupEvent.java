/*
 * (c) Copyright 2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */


package org.opendaylight.of.controller.flow;

import org.opendaylight.of.lib.dt.DataPathId;
import org.opendaylight.of.lib.msg.OfmGroupMod;
import org.opendaylight.util.event.TypedEvent;

/**
 * Encapsulates a group-related event.
 *
 * @author Simon Hunt
 */
public interface GroupEvent extends TypedEvent<GroupEventType> {

    /**
     * Returns the associated datapath ID.
     *
     * @return the datapath ID
     */
    DataPathId dpid();

    /**
     * Returns the <em>GroupMod</em> message that was pushed (or attempted).
     *
     * @return the pushed <em>GroupMod</em>
     */
    OfmGroupMod groupMod();
}
