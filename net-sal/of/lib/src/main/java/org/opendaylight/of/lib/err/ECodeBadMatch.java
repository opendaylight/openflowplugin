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
 * {@link ErrorType#BAD_MATCH} error type.
 *
 * @author Pramod Shanbhag
 */
public enum ECodeBadMatch implements ErrorCode {
    /** Unsupported match type specified by the match; Since 1.1. */
    BAD_TYPE(V_1_1, 0),
    /** Length problem in match; Since 1.1. */
    BAD_LEN(V_1_1, 1),
    /** Match uses an unsupported tag or encapsulation; Since 1.1. */
    BAD_TAG(V_1_1, 2),
    /** Unsupported datalink address mask; Since 1.1.
     * The switch does not support arbitrary datalink address masks.
     */
    BAD_DL_ADDR_MASK(V_1_1, 3),
    /** Unsupported network address mask; Since 1.1.
     * The switch does not support arbitrary network address masks.
     */
    BAD_NW_ADDR_MASK(V_1_1, 4),
    /** Unsupported combination of fields masked or omitted in the match;
     * Since 1.1.
     */
    BAD_WILDCARDS(V_1_1, 5),
    /** Unsupported field type in the match; Since 1.1. */
    BAD_FIELD(V_1_1, 6),
    /** Unsupported value in a match field; Since 1.1. */
    BAD_VALUE(V_1_1, 7),
    /** Unsupported mask specified in the match; Since 1.2.
     * The field is not datalink address or network address.
     */
    BAD_MASK(V_1_2, 8),
    /** A prerequisite was not met; Since 1.2. */
    BAD_PREREQ(V_1_2, 9),
    /** A field type was duplicated; Since 1.2. */
    DUP_FIELD(V_1_2, 10),
    /** Permissions error; Since 1.2. */
    EPERM(V_1_2, 11)
    ;

    private final ProtocolVersion since;
    private final int code;

    ECodeBadMatch(ProtocolVersion since, int code) {
        this.since = since;
        this.code = code;
    }

    @Override
    public ErrorType parentType() {
        return ErrorType.BAD_MATCH;
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
     * @return the bad match error code
     * @throws DecodeException if the code is not recognized
     * @throws VersionMismatchException if the code is not supported
     *         in the given version
     */
    static ECodeBadMatch decode(int code, ProtocolVersion pv)
            throws DecodeException {
        ECodeBadMatch errCode = null;
        for (ECodeBadMatch e: values())
            if (e.code == code) {
                errCode = e;
                break;
            }
        if (errCode == null)
            throw new DecodeException("ECodeBadMatch: unknown code: " + code);
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
    public static void validate(ECodeBadMatch code, ProtocolVersion pv) {
        verMinSince(pv, code.since, code.name());
    }

}
