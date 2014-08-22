/*
 * (c) Copyright 2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.of.lib.err;

import org.opendaylight.of.lib.DecodeException;
import org.opendaylight.of.lib.ProtocolVersion;
import org.opendaylight.of.lib.VersionMismatchException;

import static org.opendaylight.of.lib.CommonUtils.verMinSince;
import static org.opendaylight.of.lib.ProtocolVersion.V_1_1;
import static org.opendaylight.of.lib.ProtocolVersion.V_1_2;

/**
 * Designates the error codes associated with the
 * {@link ErrorType#BAD_INSTRUCTION} error type.
 *
 * @author Pramod Shanbhag
 */
public enum ECodeBadInstruction implements ErrorCode {
    /** Unknown instruction; Since 1.1. */
    UNKNOWN_INST(V_1_1, 0, 0),
    /** Switch or table does not support the instruction; Since 1.1. */
    UNSUP_INST(V_1_1, 1, 1),
    /** Invalid table ID specified; Since 1.1. */
    BAD_TABLE_ID(V_1_1, 2, 2),
    /** Metadata value unsupported by datapath; Since 1.1. */
    UNSUP_METADATA(V_1_1, 3, 3),
    /** Metadata mask value unsupported by datapath; Since 1.1. */
    UNSUP_METADATA_MASK(V_1_1, 4, 4),
    /** Unknown experimenter ID specified; Since 1.2. */
    BAD_EXPERIMENTER(V_1_2, 5, -1),
    /** Unknown instruction for experimenter ID; Since 1.1.
     * (Renamed from {@code UNSUP_EXP_INST} at 1.2.)
     */
    BAD_EXP_TYPE(V_1_1, 6, 5),
    /** Length problem in instructions; Since 1.2. */
    BAD_LEN(V_1_2, 7, -1),
    /** Permissions error; Since 1.2. */
    EPERM(V_1_2, 8, -1)
    ;

    private final ProtocolVersion since;
    private final int code;
    private final int code1;

    ECodeBadInstruction(ProtocolVersion since, int code, int code1) {
        // code1 represents the error code for 1.1.
        // code represents the error code for 1.2 and 1.3
        this.since = since;
        this.code = code;
        this.code1 = code1;
    }

    @Override
    public ErrorType parentType() {
        return ErrorType.BAD_INSTRUCTION;
    }

    @Override
    public int getCode(ProtocolVersion pv) {
        return pv == V_1_1 ? code1 : code;
    }

    /** Decodes the given error code and returns the corresponding
     * constant. If the code is not recognised, an exception is thrown.
     *
     * @param code the error code
     * @param pv the protocol version
     * @return the bad instruction error code
     * @throws DecodeException if the code is not recognized
     * @throws VersionMismatchException if the code is not supported
     *         in the given version
     */
    static ECodeBadInstruction decode(int code, ProtocolVersion pv)
            throws DecodeException {
        ECodeBadInstruction errCode = null;
        for (ECodeBadInstruction e: values()) {
            int internalCode = e.getCode(pv);
            if (internalCode != -1 && internalCode == code) {
                errCode = e;
                break;
            }
        }
        if (errCode == null)
            throw new DecodeException("ECodeBadInstruction: " +
                "unknown code: " + code);
        verMinSince(pv, errCode.since, errCode.name());
        return errCode;
    }

    /** Validates the given error code against the specified protocol version,
     * silently returning if all is well, throwing an exception otherwise.
     *
     * @param code the code
     * @param pv the protocol version
     * @throws VersionMismatchException if the code is not defined in the
     *          given protocol version
     */
    public static void validate(ECodeBadInstruction code, ProtocolVersion pv) {
        verMinSince(pv, code.since, code.name());
    }

}
