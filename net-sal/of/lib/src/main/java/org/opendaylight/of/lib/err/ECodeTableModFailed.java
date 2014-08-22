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
 * {@link ErrorType#TABLE_MOD_FAILED} error type.
 *
 * @author Pramod Shanbhag
 */
public enum ECodeTableModFailed implements ErrorCode {
    /** Specified table does not exist; Since 1.1. */
    BAD_TABLE(V_1_1, 0),
    /** Specified configuration is invalid; Since 1.1. */
    BAD_CONFIG(V_1_1, 1),
    /** Permissions error; Since 1.2. */
    EPERM(V_1_2, 2)
    ;

    private final ProtocolVersion since;
    private final int code;

    ECodeTableModFailed(ProtocolVersion since, int code) {
        this.since = since;
        this.code = code;
    }

    @Override
    public ErrorType parentType() {
        return ErrorType.TABLE_MOD_FAILED;
    }

    @Override
    public int getCode(ProtocolVersion pv) {
        return code;
    }

    /** Decodes the given error code and returns the corresponding
     * constant. If the code is not recognized, an exception is thrown.
     *
     * @param code the error code
     * @param pv the protocol version
     * @return the table mod failed error code
     * @throws DecodeException if the code is not recognized
     * @throws VersionMismatchException if the code is not supported
     *         in the given version
     */
    static ECodeTableModFailed decode(int code, ProtocolVersion pv)
            throws DecodeException {
        ECodeTableModFailed errCode = null;
        for (ECodeTableModFailed e: values())
            if (e.code == code) {
                errCode = e;
                break;
            }
        if (errCode == null)
            throw new DecodeException("ECodeTableModFailed: " +
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
    public static void validate(ECodeTableModFailed code, ProtocolVersion pv) {
        verMinSince(pv, code.since, code.name());
    }

}
