/*
 * (c) Copyright 2012,2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */


package org.opendaylight.of.lib.msg;

import org.opendaylight.of.lib.OfpBitmapCodec;
import org.opendaylight.of.lib.OfpBitmapEnum;
import org.opendaylight.of.lib.ProtocolVersion;
import org.opendaylight.of.lib.VersionMismatchException;
import org.opendaylight.util.StringUtils;

import java.util.Set;

/**
 * Designates the capabilities of a group; Since 1.2.
 *
 * @author Simon Hunt
 */
public enum GroupCapability implements OfpBitmapEnum {
    /** Support weight for select groups; Since 1.2. */
    SELECT_WEIGHT(0x1), // 1 << 0
    /** Support liveness for select groups; Since 1.2. */
    SELECT_LIVENESS(0x2), // 1 << 1
    /** Support chaining groups; Since 1.2. */
    CHAINING(0x4), // 1 << 2
    /** Check chaining for loops and delete; Since 1.2. */
    CHAINING_CHECKS(0x8), // 1 <, 3
    ;

    private static final int NA_BIT = -1;

    private final int bit;

    GroupCapability(int bit) {
        this.bit = bit;
    }

    private static final int[] MASKS = {
            0,      // not supported in 1.0
            0,      // not supported in 1.1
            0xf,    // 1.2
            0xf,    // 1.3
    };

    @Override
    public int getBit(ProtocolVersion pv) {
        return pv.lt(ProtocolVersion.V_1_2) ? NA_BIT : bit;
    }

    @Override
    public String toString() {
        return StringUtils.toCamelCase(name());
    }

    @Override
    public String toDisplayString() {
        // TODO: use lion bundle
        return toString();
    }

    // our secret decoder ring
    private static final OfpBitmapCodec<GroupCapability> CODEC =
            new OfpBitmapCodec<GroupCapability>(MASKS, values());

    /** Decodes the group capabilities flags and returns the set of
     * corresponding constants.
     *
     * @param bitmap the bitmap to decode
     * @param pv the protocol version
     * @return the set of flags
     * @throws VersionMismatchException if a bit position is not supported
     *          in the given version
     */
    public static Set<GroupCapability> decodeBitmap(int bitmap, ProtocolVersion pv) {
        return CODEC.decode(bitmap, pv);
    }

    /** Encodes a set of group capabilities flags as a bitmap. The {@code flags}
     * parameter may be null, in which case a bitmap of 0 (zero) is returned.
     *
     * @param flags the flags to encode
     * @param pv the protocol version
     * @return the flags encoded as a bitmap
     * @throws NullPointerException if protocol version is null
     * @throws VersionMismatchException if a flag is present that is not
     *          supported in the given protocol version
     */
    public static int encodeBitmap(Set<GroupCapability> flags, ProtocolVersion pv) {
        return CODEC.encode(flags, pv);
    }
}
