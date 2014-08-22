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
 * {@link ErrorType#QUEUE_OP_FAILED} error type.
 *
 * @author Pramod Shanbhag
 */
public enum ECodeQueueOpFailed implements ErrorCode {
    /** Invalid port or the port does not exist; Since 1.0. */
    BAD_PORT(0),
    /** Queue does not exist; Since 1.0. */
    BAD_QUEUE(1),
    /** Permissions error; Since 1.0. */
    EPERM(2)
    ;

    private final int code;

    ECodeQueueOpFailed(int code) {
        this.code = code;
    }

    @Override
    public ErrorType parentType() {
        return ErrorType.QUEUE_OP_FAILED;
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
     * @return the queue op failed error code
     * @throws DecodeException if the code is not recognized
     */
    static ECodeQueueOpFailed decode(int code, ProtocolVersion pv)
            throws DecodeException {
        ECodeQueueOpFailed errCode = null;
        for (ECodeQueueOpFailed e: values())
            if (e.code == code) {
                errCode = e;
                break;
            }
        if (errCode == null)
            throw new DecodeException("ECodeQueueOpFailed: " +
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
    public static void validate(ECodeQueueOpFailed code, ProtocolVersion pv) {
        // all codes are valid for all protocol versions
    }

}
