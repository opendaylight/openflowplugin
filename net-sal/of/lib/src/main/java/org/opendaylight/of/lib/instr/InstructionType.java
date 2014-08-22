/*
 * (c) Copyright 2012 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */


package org.opendaylight.of.lib.instr;

import org.opendaylight.of.lib.DecodeException;
import org.opendaylight.of.lib.OfpCodeBasedEnum;
import org.opendaylight.of.lib.ProtocolVersion;
import org.opendaylight.of.lib.VersionMismatchException;

import static org.opendaylight.of.lib.ProtocolVersion.V_1_1;
import static org.opendaylight.of.lib.ProtocolVersion.V_1_3;

/**
 * Denotes the different Flow instruction types.
 *
 * @author Simon Hunt
 */
public enum InstructionType implements OfpCodeBasedEnum {
    /** Set up the next table in the lookup pipeline; Since 1.1. */
    GOTO_TABLE(1, 8, V_1_1),
    /** Set up the metadata field for use later in the pipeline; Since 1.1. */
    WRITE_METADATA(2, 24, V_1_1),
    /** Write the action(s) onto the datapath action set; Since 1.1. */
    WRITE_ACTIONS(3, 8, V_1_1),
    /** Apply the action(s) immediately; Since 1.1. */
    APPLY_ACTIONS(4, 8, V_1_1),
    /** Clear all actions from the datapath action set; Since 1.1. */
    CLEAR_ACTIONS(5, 8, V_1_1),
    /** Apply meter (rate limiter); Since 1.3. */
    METER(6, 8, V_1_3),
    /** Experimenter instruction; Since 1.1. */
    EXPERIMENTER(0xffff, 8, V_1_1),
    ;

    private final int code;
    private final int minLen;
    private final ProtocolVersion minVer;

    InstructionType(int code, int minLen, ProtocolVersion minVer) {
        this.code = code;
        this.minLen = minLen;
        this.minVer = minVer;
    }

    @Override
    public int getCode(ProtocolVersion pv) {
        return code;
    }

    /**
     * Returns the minimum valid length of the instruction structure for the
     * type.
     * 
     * @return the minimum valid length
     */
    public int minValidLength() {
        return minLen;
    }

    /** Decodes the given code for the given protocol version, and returns
     * the appropriate type constant. Returns null if the code does not
     * correspond to any type.
     *
     * @param code the encoded type
     * @param pv the protocol version
     * @return the corresponding type constant
     * @throws DecodeException if the code is unrecognized
     */
    static InstructionType decode(int code, ProtocolVersion pv)
            throws DecodeException {
        InstructionType type = null;
        for (InstructionType t: values())
            if (t.code == code) {
                type = t;
                break;
            }
        if (type == null)
            throw new DecodeException("InstructionType: code unknown: " + code);

        // version constraints
        if (pv.lt(type.minVer))
            throw new VersionMismatchException(pv + " bad InstructionType: " +
                                                type);
        return type;
    }
}
