/*
 * (c) Copyright 2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */


package org.opendaylight.of.lib.err;

import org.opendaylight.of.lib.DecodeException;
import org.opendaylight.of.lib.OfpCodeBasedEnum;
import org.opendaylight.of.lib.ProtocolVersion;
import org.opendaylight.of.lib.VersionMismatchException;

import static org.opendaylight.of.lib.CommonUtils.verMinSince;
import static org.opendaylight.of.lib.ProtocolVersion.*;

/**
 * Designates the type of error; Since 1.0.
 *
 * @author Simon Hunt
 */
public enum ErrorType implements OfpCodeBasedEnum {
    /** Hello protocol failed; Since 1.0. */
    HELLO_FAILED(V_1_0, 0),
    /** Request was not understood; Since 1.0. */
    BAD_REQUEST(V_1_0, 1),
    /** Error in action description; Since 1.0. */
    BAD_ACTION(V_1_0, 2),
    /** Error in instruction list; Since 1.1. */
    BAD_INSTRUCTION(V_1_1, 3),
    /** Error in match; Since 1.1. */
    BAD_MATCH(V_1_1, 4),
    /** Problem modifying flow entry; Since 1.0. */
    FLOW_MOD_FAILED(V_1_0, 5, 3),
    /** Problem modifying group entry; Since 1.1. */
    GROUP_MOD_FAILED(V_1_1, 6),
    /** Port mod request failed; Since 1.0. */
    PORT_MOD_FAILED(V_1_0, 7, 4),
    /** Table mod request failed; Since 1.1. */
    TABLE_MOD_FAILED(V_1_1, 8),
    /** Queue operation failed; Since 1.0. */
    QUEUE_OP_FAILED(V_1_0, 9, 5),
    /** Switch config request failed; Since 1.1. */
    SWITCH_CONFIG_FAILED(V_1_1, 10),
    /** Controller Role request failed; Since 1.2. */
    ROLE_REQUEST_FAILED(V_1_2, 11),
    /** Error in meter; Since 1.3. */
    METER_MOD_FAILED(V_1_3, 12),
    /** Setting table features failed; Since 1.3. */
    TABLE_FEATURES_FAILED(V_1_3, 13),
    /** Experimenter error messages; Since 1.2. */
    EXPERIMENTER(V_1_2, 0xffff),
    ;

    private final int code;
    private final int code0;
    private final ProtocolVersion since;

    ErrorType(ProtocolVersion since, int code) {
        this.since = since;
        this.code = code;
        this.code0 = since.gt(V_1_0) ? -1 : code;
    }

    ErrorType(ProtocolVersion since, int code, int code0) {
        this.since = since;
        this.code = code;
        this.code0 = code0;
    }


    @Override
    public int getCode(ProtocolVersion pv) {
        return pv == V_1_0 ? code0 : code;
    }

    /** Decodes the error type code and returns the corresponding
     * constant. If the code is not recognized, an exception is thrown.
     *
     * @param code the encoded type
     * @param pv the protocol version
     * @return the error type
     * @throws DecodeException if the code is not recognized
     * @throws VersionMismatchException if the error type is not
     *          supported in the given version
     */
    public static ErrorType decode(int code, ProtocolVersion pv)
        throws DecodeException {
        ErrorType type = null;
        for (ErrorType t: values()) {
            int val = t.getCode(pv);
            if (val != -1 && val == code) {
                type = t;
                break;
            }
        }
        if (type == null)
            throw new DecodeException("ErrorType[" + pv + "]: unknown code: "
                    + code);
        verMinSince(pv, type.since, type.name());
        return type;
    }

}
