/*
 * (c) Copyright 2012 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.of.lib.instr;

import org.opendaylight.of.lib.ProtocolVersion;
import org.opendaylight.of.lib.dt.GroupId;

/**
 * Flow action {@code GROUP}.
 *
 * @author Simon Hunt
 */
public class ActGroup extends ActionU32 {
    /**
     * Constructs an action.
     *
     * @param pv the protocol version
     * @param header the action header
     */
    ActGroup(ProtocolVersion pv, Header header) {
        super(pv, header);
    }

    @Override
    String valueLabel() {
        return "grpId";
    }

    /** Returns the group id.
     *
     * @return the group id
     */
    public GroupId getGroupId() {
        return GroupId.valueOf(value);
    }
}
