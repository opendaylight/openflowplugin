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
import static org.opendaylight.of.lib.ProtocolVersion.V_1_3;

/**
 * Designates the error codes associated with the
 * {@link ErrorType#METER_MOD_FAILED} error type.
 *
 * @author Pramod Shanbhag
 */
public enum ECodeMeterModFailed implements ErrorCode {
    /** Unspecified error; Since 1.3. */
    UNKNOWN(V_1_3, 0),
    /** Meter not added because a meter ADD attempted to replace an
     * existing meter; Since 1.3.
     */
    METER_EXISTS(V_1_3, 1),
    /** Meter not added because the meter specified is invalid; Since 1.3. */
    INVALID_METER(V_1_3, 2),
    /** Meter not modified because a meter MODIFY attempted to modify
     * a non-existent meter; Since 1.3.
     */
    UNKNOWN_METER(V_1_3, 3),
    /** Unsupported or unknown command; Since 1.3. */
    BAD_COMMAND(V_1_3, 4),
    /** Flag configuration unsupported; Since 1.3. */
    BAD_FLAGS(V_1_3, 5),
    /** Rate unsupported; Since 1.3. */
    BAD_RATE(V_1_3, 6),
    /** Burst size unsupported; Since 1.3. */
    BAD_BURST(V_1_3, 7),
    /** Band unsupported; Since 1.3. */
    BAD_BAND(V_1_3, 8),
    /** Band value unsupported; Since 1.3. */
    BAD_BAND_VALUE(V_1_3, 9),
    /** No more meters available; Since 1.3. */
    OUT_OF_METERS(V_1_3, 10),
    /** The maximum number of properties for a meter has been exceeded;
     * Since 1.3.
     */
    OUT_OF_BANDS(V_1_3, 11),
    ;

    private final ProtocolVersion since;
    private final int code;

    ECodeMeterModFailed(ProtocolVersion since, int code) {
        this.since = since;
        this.code = code;
    }

    @Override
    public ErrorType parentType() {
        return ErrorType.METER_MOD_FAILED;
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
     * @return the meter mod failed error code
     * @throws DecodeException if the code is not recognized
     * @throws VersionMismatchException if the code is not supported
     *         in the given version
     */
    static ECodeMeterModFailed decode(int code, ProtocolVersion pv)
            throws DecodeException {
        ECodeMeterModFailed errCode = null;
        for (ECodeMeterModFailed e: values())
            if (e.code == code) {
                errCode = e;
                break;
            }
        if (errCode == null)
            throw new DecodeException("ECodeMeterModFailed: " +
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
    public static void validate(ECodeMeterModFailed code, ProtocolVersion pv) {
        verMinSince(pv, code.since, code.name());
    }

}
