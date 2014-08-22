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
 * {@link ErrorType#BAD_REQUEST} error type.
 *
 * @author Simon Hunt
 */
public enum ECodeBadRequest implements ErrorCode {
    /** OpenFlow Protocol Header - version not supported; Since 1.0. */
    BAD_VERSION(V_1_0, 0),
    /** OpenFlow Protocol Header - type not supported; Since 1.0. */
    BAD_TYPE(V_1_0, 1),
    /** OpenFlow Protocol Stats Request - type not supported; Since 1.0. */
    BAD_STAT(V_1_0, 2),
    /** Experimenter is not supported; Since 1.0.
     * (Renamed from {@code BAD_VENDOR} at 1.1.)
     */
    BAD_EXPERIMENTER(V_1_0, 3),
    /** Experimenter type not supported; Since 1.0.
     * (Renamed from {@code BAD_SUBTYPE} at 1.2.)
     */
    BAD_EXP_TYPE(V_1_0, 4),
    /** Permissions error; Since 1.0. */
    EPERM(V_1_0, 5),
    /** Wrong request length for type; Since 1.0. */
    BAD_LEN(V_1_0, 6),
    /** Specified buffer has already been used; Since 1.0. */
    BUFFER_EMPTY(V_1_0, 7),
    /** Specified buffer does not exist; Since 1.0. */
    BUFFER_UNKNOWN(V_1_0, 8),
    /** Specified table-id invalid or does not exist; Since 1.1. */
    BAD_TABLE_ID(V_1_1, 9),
    /** Denied because controller is slave; Since 1.2. */
    IS_SLAVE(V_1_2, 10),
    /** Invalid port; Since 1.2. */
    BAD_PORT(V_1_2, 11),
    /** Invalid packet in packet-out; Since 1.2. */
    BAD_PACKET(V_1_2, 12),
    /** Multipart-Request overflowed the assigned buffer; Since 1.3. */
    MP_BUFFER_OVERFLOW(V_1_3, 13),
    ;

    private final ProtocolVersion since;
    private final int code;

    ECodeBadRequest(ProtocolVersion since, int code) {
        this.since = since;
        this.code = code;
    }

    @Override
    public ErrorType parentType() {
        return ErrorType.BAD_REQUEST;
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
     * @return the bad request error code
     * @throws DecodeException if the code is not recognized
     * @throws VersionMismatchException if the code is not supported
     *         in the given version
     */
    static ECodeBadRequest decode(int code, ProtocolVersion pv)
            throws DecodeException {
        ECodeBadRequest errCode = null;
        for (ECodeBadRequest e: values())
            if (e.code == code) {
                errCode = e;
                break;
            }
        if (errCode == null)
            throw new DecodeException("ECodeBadRequest: " +
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
    public static void validate(ECodeBadRequest code, ProtocolVersion pv) {
        verMinSince(pv, code.since, code.name());
    }
}
