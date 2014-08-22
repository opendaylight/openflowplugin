/*
 * (c) Copyright 2012 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */


package org.opendaylight.of.lib.match;

import org.opendaylight.of.lib.OfpBitmapCodec;
import org.opendaylight.of.lib.OfpBitmapEnum;
import org.opendaylight.of.lib.ProtocolVersion;
import org.opendaylight.of.lib.VersionMismatchException;
import org.opendaylight.util.StringUtils;

import java.util.Set;

import static org.opendaylight.of.lib.CommonUtils.verMin13;

/**
 * Denotes the flags indicating the presence of various IPv6
 * extension headers, in the pseudo field {@link OxmBasicFieldType#IPV6_EXTHDR}.
 * <p>
 * Since 1.3.
 *
 * @author Simon Hunt
 */
public enum IPv6ExtHdr implements OfpBitmapEnum {
    /** No Next Header header present; Since 1.3. */
    NO_NEXT(0x1),       // 1 << 0
    /** Encrypted Security Payload header present; Since 1.3. */
    ESP(0x2),           // 1 << 1
    /** Authentication header present; Since 1.3. */
    AUTH(0x4),          // 1 << 2
    /** One or two Destination options headers present; Since 1.3. */
    DEST(0x8),          // 1 << 3
    /** Fragmentation header present; Since 1.3. */
    FRAG(0x10),         // 1 << 4
    /** Router header present; Since 1.3. */
    ROUTER(0x20),       // 1 << 5
    /** Hop-by-Hop header present; Since 1.3. */
    HOP(0x40),          // 1 << 6
    /** Unexpected repeats encountered; Since 1.3. */
    UN_REP(0x80),       // 1 << 7
    /** Unexpected sequencing encountered; Since 1.3. */
    UN_SEQ(0x100),      // 1 << 8
    ;

    private static final int NA_BIT = -1;

    private int bit;

    IPv6ExtHdr(int bit) {
        this.bit = bit;
    }

    // masks (u9) for valid bit positions for each version
    private static final int[] MASKS = {
            0x0,     // 1.0 (not supported)
            0x0,     // 1.1 (not supported)
            0x0,     // 1.2 (not supported)
            0x1ff,   // 1.3
    };

    @Override
    public int getBit(ProtocolVersion pv) {
        return pv.lt(ProtocolVersion.V_1_3) ? NA_BIT : bit;
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
    private static final OfpBitmapCodec<IPv6ExtHdr> CODEC =
            new OfpBitmapCodec<IPv6ExtHdr>(MASKS, values());

    /** Decodes an IPv6 Extension Header match pseudo field, to generate
     * the corresponding set of flags.
     *
     * @param bitmap the bitmapped value
     * @param pv the protocol version
     * @return the set of corresponding flags
     * @throws VersionMismatchException if version is earlier than 1.3
     */
    static Set<IPv6ExtHdr> decodeBitmap(int bitmap, ProtocolVersion pv) {
        verMin13(pv);
        return CODEC.decode(bitmap, pv);
    }

    /** Encodes a set of IPv6 Extension Header flags as a bitmap.
     * The {@code flags} parameter may be null, in which case a bitmask
     * of 0 (zero) is returned.
     *
     * @param flags the flags to encode
     * @param pv the protocol version
     * @return the flags encoded as a bitmap
     * @throws NullPointerException if protocol version is null
     * @throws VersionMismatchException if version is earlier than 1.3
     */
    public static int encodeBitmap(Set<IPv6ExtHdr> flags, ProtocolVersion pv) {
        verMin13(pv);
        return CODEC.encode(flags, pv);
    }

}
