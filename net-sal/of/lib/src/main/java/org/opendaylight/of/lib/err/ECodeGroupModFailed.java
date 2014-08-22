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
 * {@link ErrorType#GROUP_MOD_FAILED} error type.
 *
 * @author Pramod Shanbhag
 */
public enum ECodeGroupModFailed implements ErrorCode {
    /** Group not added because a group ADD attempted to replace
     * an already-present group; Since 1.1.
     */
    GROUP_EXISTS(V_1_1, 0),
    /** Group not added because the group specified is invalid; Since 1.1. */
    INVALID_GROUP(V_1_1, 1),
    /** Switch does not support unequal load sharing with select groups;
     * Since 1.1.
     */
    WEIGHT_UNSUPPORTED(V_1_1, 2),
    /** The group table is full; Since 1.1. */
    OUT_OF_GROUPS(V_1_1, 3),
    /** The maximum number of action buckets for a group has been exceeded;
     * Since 1.1.
     */
    OUT_OF_BUCKETS(V_1_1, 4),
    /** Switch does not support groups that forward to groups; Since 1.1. */
    CHAINING_UNSUPPORTED(V_1_1, 5),
    /** This group cannot watch the Watch-Port or Watch-Group specified;
     * Since 1.1.
     */
    WATCH_UNSUPPORTED(V_1_1, 6),
    /** Group entry would cause a loop; Since 1.1. */
    LOOP(V_1_1, 7),
    /** Group not modified because a group MODIFY attempted to modify
     * a non-existent group; Since 1.1.
     */
    UNKNOWN_GROUP(V_1_1, 8),
     /** Group not deleted because another group is forwarding to it;
     * Since 1.2.
     */
    CHAINED_GROUP(V_1_2, 9),
    /** Unsupported or unknown group type; Since 1.2. */
    BAD_TYPE(V_1_2, 10),
    /** Unsupported or unknown command; Since 1.2. */
    BAD_COMMAND(V_1_2, 11),
    /** Error in bucket; Since 1.2. */
    BAD_BUCKET(V_1_2, 12),
    /** Error in Watch-Port or Watch-Group; Since 1.2. */
    BAD_WATCH(V_1_2, 13),
    /** Permissions error; Since 1.2. */
    EPERM(V_1_2, 14)
    ;

    private final ProtocolVersion since;
    private final int code;

    ECodeGroupModFailed(ProtocolVersion since, int code) {
        this.since = since;
        this.code = code;
    }

    @Override
    public ErrorType parentType() {
        return ErrorType.GROUP_MOD_FAILED;
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
     * @return the group mod failed error code
     * @throws DecodeException if the code is not recognized
     * @throws VersionMismatchException if the code is not supported
     *         in the given version
     */
    static ECodeGroupModFailed decode(int code, ProtocolVersion pv)
            throws DecodeException {
        ECodeGroupModFailed errCode = null;
        for (ECodeGroupModFailed e: values())
            if (e.code == code) {
                errCode = e;
                break;
            }
        if (errCode == null)
            throw new DecodeException("ECodeGroupModFailed: " +
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
    public static void validate(ECodeGroupModFailed code, ProtocolVersion pv) {
        verMinSince(pv, code.since, code.name());
    }

}
