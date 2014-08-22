/*
 * (c) Copyright 2012 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.of.lib.instr;

import org.opendaylight.of.lib.ProtocolVersion;
import org.opendaylight.util.net.EthernetType;

/**
 * Abstract Flow "push" action with an EthernetType payload.
 *
 * @author Simon Hunt
 */
abstract class ActionEther extends Action {
    EthernetType ethType;

    /**
     * Constructs an action.
     *
     * @param pv the protocol version
     * @param header the action header
     */
    ActionEther(ProtocolVersion pv, Header header) {
        super(pv, header);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(super.toString());
        int len = sb.length();
        sb.replace(len-1, len, ",eth=").append(ethType).append("}");
        return sb.toString();
    }

    /** Returns the Ethernet type of this push action.
     *
     * @return the Ethernet type
     */
    public EthernetType getEthernetType() {
        return ethType;
    }

}
