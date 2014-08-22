/*
 * (c) Copyright 2012 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.of.lib.instr;

import org.opendaylight.of.lib.ProtocolVersion;

/**
 * Flow action {@code POP_VLAN}.
 *
 * @author Simon Hunt
 */
public class ActPopVlan extends Action {
    /**
     * Constructs an action.
     *
     * @param pv the protocol version
     * @param header the action header
     */
    ActPopVlan(ProtocolVersion pv, Header header) {
        super(pv, header);
    }
}
