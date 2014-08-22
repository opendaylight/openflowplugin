/*
 * (c) Copyright 2012 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */


package org.opendaylight.of.lib.msg;

import org.opendaylight.of.lib.*;

import java.util.Set;

import static org.opendaylight.of.lib.CommonUtils.verMin11;

/**
 * Designates group types; Since 1.1.
 *
 * @author Simon Hunt
 * @author Scott Simes
 */
public enum GroupType implements OfpCodeBasedEnum {
    /** All (multicast/broadcast) group; Since 1.1. */
    ALL(0),
    /** Select group; Since 1.1. */
    SELECT(1),
    /** Indirect group; Since 1.1. */
    INDIRECT(2),
    /** Fast failover group; Since 1.1. */
    FF(3),
    ;

    private final int code;

    GroupType(int code) {
        this.code = code;
    }

    @Override
    public int getCode(ProtocolVersion pv) {
        return code;
    }

    private static final int[] MASKS = {
            0,      // not supported in 1.0
            0xf,    // 1.1
            0xf,    // 1.2
            0xf,    // 1.3
    };

    // our secret decoder ring for code to bitmap manipulations
    private static final OfpCodeBasedCodec<GroupType> CODEC =
            new OfpCodeBasedCodec<GroupType>(MASKS, values());

    /** Decodes the group type code and returns the corresponding
     * constant. If the code is not recognized, an exception is thrown.
     *
     * @param code the encoded type
     * @param pv the protocol version
     * @return the group type
     * @throws DecodeException if the code is not recognized
     * @throws VersionMismatchException if the group type is not
     *             supported in the given version
     */
    public static GroupType decode(int code, ProtocolVersion pv)
            throws DecodeException {
        verMin11(pv);
        GroupType type = null;
        for (GroupType t: values())
            if (t.code == code) {
                type = t;
                break;
            }
        if (type == null)
            throw new DecodeException("GroupType: unknown code: " + code);

        // No further version constraints, currently: same across v1.1 - v1.3
        return type;
    }

    /**
     * Decodes the group type flags, based on the code associated with the
     * flags, and returns the set of corresponding constants.
     *
     * @param bitmap the bitmap to decode
     * @param pv the protocol version
     * @return the set of flags
     * @throws NullPointerException if protocol version is null
     * @throws VersionMismatchException if a bit position is not supported
     *          in the given version
     */
    public static Set<GroupType> decodeFlags(int bitmap, ProtocolVersion pv) {
        return CODEC.decode(bitmap, pv);
    }

    /**
     * Encodes a set of group type flags as a bitmap. The {@code flags}
     * parameter may be empty, in which case a bitmap of 0 (zero) is returned.
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
    public static int encodeFlags(Set<GroupType> flags, ProtocolVersion pv) {
        return CODEC.encode(flags, pv);
    }
}
