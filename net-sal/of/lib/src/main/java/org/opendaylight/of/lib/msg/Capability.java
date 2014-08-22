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
 * Designates OpenFlow switch capabilities.
 *
 * @author Simon Hunt
 */
public enum Capability implements OfpBitmapEnum {
    /** Flow statistics; Since 1.0. */
    FLOW_STATS(0x1), // 1 << 0
    /** Table statistics; Since 1.0. */
    TABLE_STATS(0x2), // 1 << 1
    /** Port statistics; Since 1.0. */
    PORT_STATS(0x4), // 1 << 2
    /** 802.1d spanning tree; Since 1.0; Removed at 1.1. */
    STP(0x8, -1, -1, -1), // 1 << 3;
    /** Group statistics; Since 1.1. */
    GROUP_STATS(-1, 0x8, 0x8, 0x8), // 1 << 3;
    /** Reserved (must be zero); Since 1.0; Removed at 1.1. */
    RESERVED(0x10, -1, -1, -1), // 1 << 4;
    /** Can reassemble IP fragments; Since 1.0. */
    IP_REASM(0x20), // 1 << 5;
    /** Queue statistics; Since 1.0. */
    QUEUE_STATS(0x40), // 1 << 6;
    /** Match IP addresses in ARP packets; Since 1.0; Removed at 1.2. */
    ARP_MATCH_IP(0x80, 0x80, -1, -1), // 1 << 7;
    /** Switch will block looping ports; Since 1.2. */
    PORT_BLOCKED(-1, -1, 0x100, 0x100), // 1 << 8;
    ;

    private final int bit;
    private final int[] bits;

    Capability(int bit) {
        this.bit = bit;
        bits = null;
    }

    Capability(int v0, int v1, int v2, int v3) {
        bit = 0;
        bits = new int[] {v0, v1, v2, v3};

    }

    // masks (u32) for valid bit positions for each version
    private static final int[] MASKS = {
            0x0ff,     // 1.0
            0x0ef,     // 1.1
            0x16f,     // 1.2
            0x16f,     // 1.3
    };

    @Override
    public int getBit(ProtocolVersion pv) {
        return bits == null ? bit : bits[pv.ordinal()];
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
    private static final OfpBitmapCodec<Capability> CODEC =
            new OfpBitmapCodec<Capability>(MASKS, values());

    /** Decodes a switch capabilities bitmap to generate the corresponding
     * set of capabilities flags.
     *
     * @param bitmap the bitmapped capabilities
     * @param pv the protocol version
     * @return the set of corresponding capabilities flags
     * @throws VersionMismatchException if unexpected flags found for
     *          the specified version
     */
    static Set<Capability> decodeBitmap(int bitmap, ProtocolVersion pv) {
        return CODEC.decode(bitmap, pv);
    }

    /** Encodes a set of switch capabilities flags as a bitmap.
     * The {@code flags} parameter may be null, in which case a bitmap
     * of 0 (zero) is returned.
     *
     * @param flags the flags to encode
     * @param pv the protocol version
     * @return the flags encoded as a bitmap
     * @throws NullPointerException if protocol version is null
     * @throws VersionMismatchException if a flag is present that is not
     *          supported in the given protocol version
     */
    static int encodeBitmap(Set<Capability> flags, ProtocolVersion pv) {
        return CODEC.encode(flags, pv);
    }

}
