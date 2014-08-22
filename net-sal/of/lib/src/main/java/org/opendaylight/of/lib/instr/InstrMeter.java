/*
 * (c) Copyright 2012 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.of.lib.instr;

import org.opendaylight.of.lib.ProtocolVersion;
import org.opendaylight.of.lib.dt.MeterId;

/**
 * Flow Instruction {@code METER}.
 *
 * @author Simon Hunt
 */
public class InstrMeter extends Instruction {
    MeterId meterId;

    /**
     * Constructs an instruction.
     *
     * @param pv the protocol version
     * @param header the instruction header
     */
    InstrMeter(ProtocolVersion pv, Header header) {
        super(pv, header);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(super.toString());
        int len = sb.length();
        sb.replace(len-1, len, ",meterId=").append(meterId).append("}");
        return sb.toString();
    }

    /** Returns the meter id.
     *
     * @return the meter id
     */
    public MeterId getMeterId() {
        return meterId;
    }
}
