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

/**
 * Designates the error codes associated with the
 * {@link ErrorType#HELLO_FAILED} error type.
 *
 * @author Simon Hunt
 */
public enum ECodeHelloFailed implements ErrorCode {
    /** No compatible version; Since 1.0. */
    INCOMPATIBLE(0),
    /** Permissions error; Since 1.0. */
    EPERM(1),
    ;

    private final int code;

    ECodeHelloFailed(int code) {
        this.code = code;
    }

    @Override
    public ErrorType parentType() {
        return ErrorType.HELLO_FAILED;
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
     * @return the hello failed error code
     * @throws DecodeException if the code is not recognized
     * @throws VersionMismatchException if the code is not supported
     *         in the given version
     */
    static ECodeHelloFailed decode(int code, ProtocolVersion pv)
        throws DecodeException {
        ECodeHelloFailed errCode = null;
        for (ECodeHelloFailed e: values())
            if (e.code == code) {
                errCode = e;
                break;
            }
        if (errCode == null)
            throw new DecodeException("ECodeHelloFailed: " +
                                        "unknown code: " + code);
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
    public static void validate(ECodeHelloFailed code, ProtocolVersion pv) {
        // all codes are valid for all protocol versions
    }
}
