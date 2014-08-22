/*
 * (c) Copyright 2012 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.of.lib.instr;

import org.opendaylight.of.lib.ExperimenterId;
import org.opendaylight.of.lib.ProtocolVersion;
import org.opendaylight.util.ByteUtils;

/**
 * Flow Instruction {@code EXPERIMENTER}.
 *
 * @author Simon Hunt
 */
public class InstrExperimenter extends Instruction {
    /** Experimenter id (encoded). */
    int id;

    /** Experimenter defined data. */
    byte[] data;

    /**
     * Constructs an instruction.
     *
     * @param pv the protocol version
     * @param header the instruction header
     */
    InstrExperimenter(ProtocolVersion pv, Header header) {
        super(pv, header);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(super.toString());
        int len = sb.length();
        sb.replace(len-1, len, ",expId=")
                .append(ExperimenterId.idToString(id))
                .append(",data=").append(ByteUtils.toHexArrayString(data))
                .append("}");
        return sb.toString();
    }

    /** Returns the experimenter ID encoded as an int.
     *
     * @return the experimenter ID (encoded)
     */
    public int getId() {
        return id;
    }

    /** Returns the experimenter ID (if we know it); null otherwise.
     *
     * @return the experimenter ID
     */
    public ExperimenterId getExpId() {
        return ExperimenterId.decode(id);
    }

    /** Returns a copy of the experimenter-defined data.
     *
     * @return a copy of the data
     */
    public byte[] getData() {
        return data == null ? null : data.clone();
    }
}
