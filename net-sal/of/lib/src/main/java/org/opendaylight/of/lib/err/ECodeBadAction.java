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
 * {@link ErrorType#BAD_ACTION} error type.
 *
 * @author Pramod Shanbhag
 */
public enum ECodeBadAction implements ErrorCode {
    /** Unknown action type; Since 1.0. */
    BAD_TYPE(V_1_0, 0),
    /** Length problem in actions; Since 1.0. */
    BAD_LEN(V_1_0, 1),
    /** Unknown experimenter id specified; Since 1.0.
     * (Renamed from {@code BAD_VENDOR} at 1.1.)
     */
    BAD_EXPERIMENTER(V_1_0, 2),
    /** Unknown action for experimenter id; Since 1.0.
     * (Renamed from {@code BAD_VENDOR_TYPE} at 1.1.)
     * (Renamed from {@code BAD_EXPERIMENTER_TYPE} at 1.2)
     */
    BAD_EXP_TYPE(V_1_0, 3),
    /** Problem validating output port; Since 1.0. */
    BAD_OUT_PORT(V_1_0, 4),
    /** Bad action argument; Since 1.0. */
    BAD_ARGUMENT(V_1_0, 5),
    /** Permissions error; Since 1.0. */
    EPERM(V_1_0, 6),
    /** Can’t handle this many actions; Since 1.0. */
    TOO_MANY(V_1_0, 7),
    /** Problem validating output queue; Since 1.0. */
    BAD_QUEUE(V_1_0, 8),
    /** Invalid group id in forward action; Since 1.1. */
    BAD_OUT_GROUP(V_1_1, 9),
    /** Action can’t apply for this match, or Set-Field missing
     * prerequisite; Since 1.1.
     */
    MATCH_INCONSISTENT(V_1_1, 10),
    /** Action order is unsupported for the action list in an
        Apply-Actions instruction; Since 1.1.
     */
    UNSUPPORTED_ORDER(V_1_1, 11),
    /** Action uses an unsupported tag or encapsulation; Since 1.1. */
    BAD_TAG(V_1_1, 12),
    /** Unsupported type in Set-Field action; Since 1.2. */
    BAD_SET_TYPE(V_1_2, 13),
    /** Length problem in Set-Field action; Since 1.2. */
    BAD_SET_LEN(V_1_2, 14),
    /** Bad argument in Set-Field action; Since 1.2. */
    BAD_SET_ARGUMENT(V_1_2, 15)
    ;

    private final ProtocolVersion since;
    private final int code;

    ECodeBadAction (ProtocolVersion since, int code) {
        this.since = since;
        this.code = code;
    }

    @Override
    public ErrorType parentType() {
        return ErrorType.BAD_ACTION;
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
     * @return the bad action error code
     * @throws DecodeException if the code is not recognized
     * @throws VersionMismatchException if the code is not supported
     *         in the given version
     */
    static ECodeBadAction decode(int code, ProtocolVersion pv)
            throws DecodeException {
        ECodeBadAction errCode = null;
        for (ECodeBadAction e: values())
            if (e.code == code) {
                errCode = e;
                break;
            }
        if (errCode == null)
            throw new DecodeException("ECodeBadAction: " +
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
    public static void validate(ECodeBadAction code, ProtocolVersion pv) {
        verMinSince(pv, code.since, code.name());
    }

}
