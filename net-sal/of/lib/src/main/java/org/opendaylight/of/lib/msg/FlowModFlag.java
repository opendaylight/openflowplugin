/*
 * (c) Copyright 2012 Hewlett-Packard Development Company, L.P.
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
 * Designates flow mod flags; Since 1.0.
 *
 * @author Simon Hunt
 */
public enum FlowModFlag implements OfpBitmapEnum {
    /** Send flow removed message when flow expires or is deleted; Since 1.0. */
    SEND_FLOW_REM(0x1), // 1 << 0
    /** Check for overlapping entries first; Since 1.0. */
    CHECK_OVERLAP(0x2), // 1 << 1
    /** Remark this is for emergency; Since 1.0; Removed at 1.1. */
    EMERG(0x4, -1), // 1 << 2 (v1.0 only)
    /** Reset flow packet and byte counts; Since 1.2. */
    RESET_COUNTS(-1, 0x4), // 1 << 2 (v1.1 and higher)
    /** Don't keep track of packet count; Since 1.3. */
    NO_PACKET_COUNTS(-1, 0x8), // 1 << 3
    /** Don't keep track of byte count; Since 1.3. */
    NO_BYTE_COUNTS(-1, 0x10), // 1 << 4
    ;

    private static final int NA_BIT = -1;

    private final int bitV10;
    private final int bitV123;

    FlowModFlag(int bit) {
        bitV10 = bit;
        bitV123 = bit;
    }

    FlowModFlag(int v10Bit, int v123Bit) {
        bitV10 = v10Bit;
        bitV123 = v123Bit;
    }

    // masks (u16) for valid bit positions for each version
    private static final int[] MASKS = {
            0x07,   // 1.0
            0x03,   // 1.1
            0x07,   // 1.2
            0x1f,   // 1.3
    };

    @Override
    public int getBit(ProtocolVersion pv) {
        int bit = pv == ProtocolVersion.V_1_0 ? bitV10 : bitV123;
        return ((bit & MASKS[pv.ordinal()]) == 0) ? NA_BIT : bit;
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
    private static final OfpBitmapCodec<FlowModFlag> CODEC =
            new OfpBitmapCodec<FlowModFlag>(MASKS, values());

    /** Decodes a flow mod flag bitmap to generate the corresponding set of
     * flags.
     *
     * @param bitmap the bitmapped flags
     * @param pv the protocol version
     * @return the set of corresponding flags
     * @throws VersionMismatchException if unexpected flag bits found for
     *          the specified version
     */
    public static Set<FlowModFlag> decodeBitmap(int bitmap, ProtocolVersion pv) {
        return CODEC.decode(bitmap, pv);
    }

    /** Encodes a set of flow mod flags as a bitmap. The {@code flags}
     * parameter may be null, in which case a bitmap of 0 (zero) is returned.
     *
     * @param flags the flags to encode
     * @param pv the protocol version
     * @return the flags encoded as a bitmap
     * @throws NullPointerException if protocol version is null
     * @throws VersionMismatchException if a flag is present that is not
     *          supported in the given protocol version
     */
    public static int encodeBitmap(Set<FlowModFlag> flags, ProtocolVersion pv) {
        return CODEC.encode(flags, pv);
    }

}
