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
 * Flow Instruction {@code WRITE_METADATA}.
 *
 * @author Simon Hunt
 */
public class InstrWriteMetadata extends Instruction {
    long metadata;
    long mask;

    /**
     * Constructs an instruction.
     *
     * @param pv the protocol version
     * @param header the instruction header
     */
    InstrWriteMetadata(ProtocolVersion pv, Header header) {
        super(pv, header);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(super.toString());
        int len = sb.length();
        sb.replace(len-1, len, ",meta=").append(hex(metadata))
                .append(",mask=").append(hex(mask)).append("}");
        return sb.toString();
    }

    /** Returns the metadata value.
     *
     * @return the metadata
     */
    public long getMetadata() {
        return metadata;
    }

    /** Returns the metadata mask value.
     *
     * @return the metadata mask
     */
    public long getMask() {
        return mask;
    }
}
