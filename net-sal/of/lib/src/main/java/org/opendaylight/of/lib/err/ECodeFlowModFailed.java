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
import org.opendaylight.util.ResourceUtils;

import java.util.ResourceBundle;

import static org.opendaylight.of.lib.CommonUtils.verMinSince;
import static org.opendaylight.of.lib.ProtocolVersion.*;

/**
 * Designates the error codes associated with the
 * {@link ErrorType#FLOW_MOD_FAILED} error type.
 *
 * @author Pramod Shanbhag
 */
public enum ECodeFlowModFailed implements ErrorCode {
    /** Unspecified error; Since 1.1. */
    UNKNOWN(V_1_1, 0, -1),
    /** Flow not added because table was full; Since 1.0. */
    TABLE_FULL(V_1_0, 1, 0),
    /** Table does not exist; Since 1.1. */
    BAD_TABLE_ID(V_1_1, 2, -1),
    /** Attempted to add overlapping flow with Check-Overlap flag set;
     * Since 1.0.
     */
    OVERLAP(V_1_0, 3, 1),
    /** Permissions error; Since 1.0. */
    EPERM(V_1_0, 4, 2),
    /** Flow was not added because of unsupported idle or hard timeout;
     * Since 1.0.
     */
    BAD_TIMEOUT(V_1_0, 5, 3),
    /** Unsupported action list; Since 1.0; Removed at 1.1.
     * The actions cannot be processed in the order specified.
     */
    UNSUPPORTED_ACTION_LIST(V_1_0, -1, 5),
    /** Unsupported or unknown command; Since 1.0. */
    BAD_COMMAND(V_1_0, 6, 4),
    /** Unsupported or unknown flags; Since 1.2. */
    BAD_FLAGS(V_1_2, 7, -1)
    ;

    private final ProtocolVersion since;
    private final int code;
    private final int code0;

    ECodeFlowModFailed(ProtocolVersion since, int code, int code0) {
        this.since = since;
        this.code = code;
        this.code0 = code0;
    }

    @Override
    public ErrorType parentType() {
        return ErrorType.FLOW_MOD_FAILED;
    }

    @Override
    public int getCode(ProtocolVersion pv) {
        return pv == V_1_0 ? code0 : code;
    }

    /** Decodes the given error code and returns the corresponding
     * constant. If the code is not recognised, an exception is thrown.
     *
     * @param code the error code
     * @param pv the protocol version
     * @return the flow mod failed error code
     * @throws DecodeException if the code is not recognized
     * @throws VersionMismatchException if the code is not supported
     *         in the given version
     */
    static ECodeFlowModFailed decode(int code, ProtocolVersion pv)
            throws DecodeException {
        ECodeFlowModFailed errCode = null;
        for (ECodeFlowModFailed e: values()) {
            int internalCode = e.getCode(pv);
            if (internalCode != -1 && internalCode == code) {
                errCode = e;
                break;
            }
        }
        if (errCode == null)
            throw new DecodeException("ECodeFlowModFailed: " +
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
    public static void validate(ECodeFlowModFailed code, ProtocolVersion pv) {
        verMinSince(pv, code.since, code.name());
        // have to special case unsupported action list (removed at 1.1)
        if (code == UNSUPPORTED_ACTION_LIST && pv.gt(V_1_0))
            throw new VersionMismatchException(E_UAL_10_ONLY);
    }

    private static final ResourceBundle RES = ResourceUtils.getBundledResource(
            ECodeFlowModFailed.class, "eCodeFlowModFailed");

    private static final String E_UAL_10_ONLY = RES.getString("e_ual_10_only");
}