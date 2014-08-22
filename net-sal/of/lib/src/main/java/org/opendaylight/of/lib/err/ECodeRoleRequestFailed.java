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
import static org.opendaylight.of.lib.ProtocolVersion.V_1_2;

/**
 * Designates the error codes associated with the
 * {@link ErrorType#ROLE_REQUEST_FAILED} error type.
 *
 * @author Pramod Shanbhag
 */
public enum ECodeRoleRequestFailed implements ErrorCode {
    /** Stale Message: old generation ID; Since 1.2. */
    STALE(V_1_2, 0),
    /** Controller role change unsupported; Since 1.2. */
    UNSUP(V_1_2, 1),
    /** Invalid role; Since 1.2. */
    BAD_ROLE(V_1_2, 2)
    ;

    private final ProtocolVersion since;
    private final int code;

    ECodeRoleRequestFailed(ProtocolVersion since, int code) {
        this.since = since;
        this.code = code;
    }

    @Override
    public ErrorType parentType() {
        return ErrorType.ROLE_REQUEST_FAILED;
    }

    @Override
    public int getCode(ProtocolVersion pv) {
        return code;
    }

    /** Decodes the given error code and returns the corresponding
     * constant. If the code is not recognised, an exception is thrown.
     *
     * @param code the error code
     * @param pv the protocol version
     * @return the role request error code
     * @throws DecodeException if the code is not recognized
     * @throws VersionMismatchException if the code is not supported
     *         in the given version
     */
    static ECodeRoleRequestFailed decode(int code, ProtocolVersion pv)
            throws DecodeException {
        ECodeRoleRequestFailed errCode = null;
        for (ECodeRoleRequestFailed e: values())
            if (e.code == code) {
                errCode = e;
                break;
            }
        if (errCode == null)
            throw new DecodeException("ECodeRoleRequestFailed: " +
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
    public static void validate(ECodeRoleRequestFailed code, ProtocolVersion pv) {
        verMinSince(pv, code.since, code.name());
    }

}
