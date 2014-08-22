/*
 * (c) Copyright 2012-2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */


package org.opendaylight.of.lib.msg;

import org.opendaylight.of.lib.*;

import java.util.Set;

import static org.opendaylight.of.lib.CommonUtils.verMin13;

/**
 * Designates meter band types; Since 1.3.
 *
 * @author Simon Hunt
 * @author Scott Simes
 */
public enum MeterBandType implements OfpCodeBasedEnum {
    /** Drop packet; Since 1.3. */
    DROP(1),
    /** Remark DSCP in the IP header; Since 1.3. */
    DSCP_REMARK(2),
    /** Experimenter meter band; Since 1.3. */
    EXPERIMENTER(0xffff),
    ;

    private final int code;

    MeterBandType(int code) {
        this.code = code;
    }

    /** Returns the internal code for the given band type.
     *
     * @return the internal code
     */
    @Override
    public int getCode(ProtocolVersion pv) {
        return code;
    }

    /*
     * Implementation note: The spec defines the bitmask used in the
     * Meter Features reply (supported band types) as bit positions
     * corresponding to 1 shifted left by the code value of each supported
     * band type. Thus, for 1.3, with types DROP(1) and DSCP_REMARK(2), the
     * appropriate mask is:
     *   1 << 1 = 0x2
     *   1 << 2 = 0x4
     *   mask   = 0x6
     */
    private static final int[] MASKS = {
            0,      // not defined in 1.0
            0,      // not defined in 1.1
            0,      // not defined in 1.2
            0x6,    // 1.3
    };

    private static final MeterBandType[] VALID_FOR_BITMASK = {
            DROP, DSCP_REMARK
    };

    // our secret decoder ring for code to bitmap manipulations
    private static final OfpCodeBasedCodec<MeterBandType> CODEC =
            new OfpCodeBasedCodec<MeterBandType>(MASKS, VALID_FOR_BITMASK);

    /** Decodes the meter band type code and returns the corresponding
     * constant. If the code is not recognized, an exception is thrown.
     *
     * @param code the encoded band type
     * @param pv the protocol version
     * @return the meter band type
     * @throws DecodeException if the code is not recognized
     * @throws VersionMismatchException if the meter band type is not
     *             supported in the given version
     */
    static MeterBandType decode(int code, ProtocolVersion pv)
            throws DecodeException {
        verMin13(pv);
        MeterBandType type = null;
        for (MeterBandType t: values())
            if (t.code == code) {
                type = t;
                break;
            }
        if (type == null)
            throw new DecodeException("MeterBandType: unknown code: " + code);

        // No further version constraints.
        return type;
    }

    /**
     * Decodes the meter band type flags, based on the code associated with the
     * flags, and returns the set of corresponding constants.
     * <p>
     * Note that the {@link MeterBandType#EXPERIMENTER} can not be represented
     * as a possible flag in an integer bit mask.
     *
     * @param bitmap the bitmap to decode
     * @param pv the protocol version
     * @return the set of flags
     * @throws NullPointerException if protocol version is null
     * @throws VersionMismatchException if a bit position is not supported
     *          in the given version
     */
    public static Set<MeterBandType> decodeFlags(int bitmap, ProtocolVersion pv) {
        return CODEC.decode(bitmap, pv);
    }

    /**
     * Encodes a set of meter band type flags as a bitmap.  The {@code flags}
     * parameter may be empty, in which case a bitmap of 0 (zero) is returned.
     * <p>
     * Note that the {@link MeterBandType#EXPERIMENTER} can not be represented
     * as a possible flag in an integer bit mask.
     *
     * @param flags the flags to encode
     * @param pv the protocol version
     * @return the flags encoded as a bitmap
     * @throws NullPointerException if either parameter is null
     * @throws IllegalArgumentException if code associated with enumeration
     *         constant is outside the valid range (0 - 31)
     * @throws VersionMismatchException if a flag is present that is not
     *          supported in the given protocol version
     */
    public static int encodeFlags(Set<MeterBandType> flags, ProtocolVersion pv) {
        return CODEC.encode(flags, pv);
    }
}
