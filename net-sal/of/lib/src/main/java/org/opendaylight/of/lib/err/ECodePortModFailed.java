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
import static org.opendaylight.of.lib.ProtocolVersion.*;

/**
 * Designates the error codes associated with the
 * {@link ErrorType#PORT_MOD_FAILED} error type.
 *
 * @author Pramod Shanbhag
 */
public enum ECodePortModFailed implements ErrorCode {
    /** Specified port number does not exist; Since 1.0. */
    BAD_PORT(V_1_0, 0),
    /** Specified hardware address does not match the port number; Since 1.0. */
    BAD_HW_ADDR(V_1_0, 1),
    /** Specified configuration is invalid; Since 1.1. */
    BAD_CONFIG(V_1_1, 2),
    /** Specified advertise is invalid; Since 1.1. */
    BAD_ADVERTISE(V_1_1, 3),
    /** Permissions error; Since 1.2. */
    EPERM(V_1_2, 4)
    ;

    private final ProtocolVersion since;
    private final int code;

    ECodePortModFailed(ProtocolVersion since, int code) {
        this.since = since;
        this.code = code;
    }

    @Override
    public ErrorType parentType() {
        return ErrorType.PORT_MOD_FAILED;
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
     * @return the port mod failed error code
     * @throws DecodeException if the code is not recognized
     * @throws VersionMismatchException if the code is not supported
     *         in the given version
     */
    static ECodePortModFailed decode(int code, ProtocolVersion pv)
            throws DecodeException {
        ECodePortModFailed errCode = null;
        for (ECodePortModFailed e: values())
            if (e.code == code) {
                errCode = e;
                break;
            }
        if (errCode == null)
            throw new DecodeException("ECodePortModFailed: " +
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
    public static void validate(ECodePortModFailed code, ProtocolVersion pv) {
        verMinSince(pv, code.since, code.name());
    }

}
