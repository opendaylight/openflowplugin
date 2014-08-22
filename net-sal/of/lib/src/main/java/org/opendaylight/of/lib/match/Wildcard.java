/*
 * (c) Copyright 2013 Hewlett-Packard Development Company, L.P.
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
import org.opendaylight.util.ByteUtils;
import org.opendaylight.util.StringUtils;
import org.opendaylight.util.net.IpAddress;
import org.opendaylight.util.net.MacAddress;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static org.opendaylight.of.lib.CommonUtils.E_DEPRECATED;
import static org.opendaylight.of.lib.ProtocolVersion.V_1_0;
import static org.opendaylight.of.lib.ProtocolVersion.V_1_1;

/**
 * Denotes the flags in the Match Wildcards field. Package-private
 * implementation to facilitate backward compatibility with 1.0 and 1.1 flow
 * matching.
 *
 * @author Simon Hunt
 */
enum Wildcard implements OfpBitmapEnum {
    IN_PORT(0x1, 0x1),
    DL_VLAN(0x2, 0x2),
    DL_SRC(0x4, -1),
    DL_DST(0x8, -1),
    DL_TYPE(0x10, 0x8),
    DL_VLAN_PCP(0x100000, 0x4),
    NW_TOS(0x200000, 0x10),
    NW_PROTO(0x20, 0x20),
    TP_SRC(0x40, 0x40),
    TP_DST(0x80, 0x80),
    MPLS_LABEL(-1, 0x100),
    MPLS_TC(-1, 0x200),
    // special values to represent the NW SRC/DST 6-bit fields
    NW_SRC(-1, -1),
    NW_DST(-1, -1),
    ;

    private final int bit10;
    private final int bit11;

    Wildcard(int bit10, int bit11) {
        this.bit10 = bit10;
        this.bit11 = bit11;
    }

    private static final Wildcard[] PURE_BITS = {
            IN_PORT, DL_VLAN, DL_SRC, DL_DST, DL_TYPE, DL_VLAN_PCP, NW_TOS,
            NW_PROTO, TP_SRC, TP_DST, MPLS_LABEL, MPLS_TC,
    };

    private static final Wildcard[] ALL_WILD_10 = {
            IN_PORT, DL_VLAN, DL_SRC, DL_DST, DL_TYPE, DL_VLAN_PCP, NW_TOS,
            NW_PROTO, TP_SRC, TP_DST,
    };

    private static final Wildcard[] ALL_WILD_11 = {
            IN_PORT, DL_VLAN, DL_TYPE, DL_VLAN_PCP, NW_TOS, NW_PROTO, TP_SRC,
            TP_DST, MPLS_LABEL, MPLS_TC,
    };

    // masks (u32) for valid bit positions for each version
    private static final int[] MASKS = {
            0x3000ff, // 1.0
            0x3ff, // 1.1
            0, // Deprecated in 1.2
            0, // Deprecated in 1.3
    };

    @Override
    public int getBit(ProtocolVersion pv) {
        return pv == V_1_0 ? bit10 : bit11;
    }

    @Override
    public String toString() {
        return StringUtils.toCamelCase(name());
    }

    @Override
    public String toDisplayString() {
        return toString();
    }

    // our secret decoder ring
    private static final OfpBitmapCodec<Wildcard> CODEC =
            new OfpBitmapCodec<Wildcard>(MASKS, PURE_BITS);

    /** The IP address mask representing all wild bits. */
    static final IpAddress ALL_WILD_IP_MASK = IpAddress.valueOf("0.0.0.0");

    /** The MAC address mask representing all wild bits. */
    static final MacAddress ALL_WILD_MAC_MASK =
            MacAddress.valueOf("000000:000000");

    /** The metadata mask representing all wild bits. */
    static final long ALL_WILD_METADATA_MASK = 0;

    private static final int ALL_BITS = 0xffffffff;
    static final int NW_SRC_SHIFT = 8;
    static final int NW_DST_SHIFT = 14;
    private static final int BIT_FIELD_MASK = 0x3f;
    private static final int ALL_WILD = 32;

    /**
     * Returns the wildcard flags from the given bitmap. Takes into account
     * the additional non-pure-bit data in the NW SRC/DST fields for 1.0.
     *
     * @param bitmap the encoded bitmap
     * @param pv the protocol version
     * @return the set of decoded flags
     */
    static Set<Wildcard> decodeBitmap(int bitmap, ProtocolVersion pv) {
        return CODEC.decode(bitmap, pv);
    }

    /**
     * Returns the wildcard flags from the given bitmap. Takes into account
     * the additional non-pure-bit data in the NW SRC/DST fields for 1.0.
     *
     * @param bitmap the encoded bitmap
     * @param pv the protocol version
     * @return the set of decoded flags
     */
    static Set<Wildcard> decodeV10Bitmap(int bitmap, ProtocolVersion pv) {
        // mask out non-pure bit fields before passing to codec
        return CODEC.decode(bitmap & MASKS[0], pv);
    }

    /**
     * Returns the specified set of flags as an encoded value.
     *
     * @param flags the flags to encode
     * @param pv the protocol version
     * @return the encoded flags
     */
    static int encodeBitmap(Set<Wildcard> flags, ProtocolVersion pv) {
        return CODEC.encode(flags, pv);
    }

    /**
     * Encodes and returns the specified set of flags, along with the NW_SRC
     * and NW_DST masks.
     *
     * @param flags the flags to encode
     * @param nwSrcMask the NW_SRC mask
     * @param nwDstMask the NW_DST mask
     * @param pv the protocol version
     * @return the encoded wildcards
     */
    static int encodeV10Bitmap(Set<Wildcard> flags, IpAddress nwSrcMask,
                               IpAddress nwDstMask, ProtocolVersion pv) {
        if (pv != V_1_0)
            throw new VersionMismatchException(E_DEPRECATED + V_1_1);

        int pure = encodeBitmap(flags, pv);
        int result = encodeNetmask(NW_SRC, nwSrcMask, pure, pv);
        return encodeNetmask(NW_DST, nwDstMask, result, pv);
    }

    /**
     * Returns the equivalent IP address as a mask for the specified number of
     * wild bits.
     *
     * @param n the number of wild bits
     * @return the equivalent IP mask
     */
    static IpAddress getIpMaskForWildBits(int n) {
        if (n < 0)
            throw new IllegalArgumentException("negative: " + n);
        if (n >= ALL_WILD)
            return ALL_WILD_IP_MASK;

        byte[] asBytes = new byte[4];
        ByteUtils.setInteger(asBytes, 0, ALL_BITS << n);
        return IpAddress.valueOf(asBytes);
    }

    /**
     * Decodes the specified netmask value from its bit-field in the given
     * bitmap. Extracts the bits from the field, and converts the value to the
     * equivalent subnet mask (expressed as an IP address).
     *
     * @param which which value (NW_SRC or NW_DST)
     * @param bitmap the bitmap to extract from
     * @param pv the protocol version
     * @return the netmask
     * @throws VersionMismatchException if not v1.0
     * @throws IllegalArgumentException if which is not appropriate
     */
    static IpAddress decodeNetmask(Wildcard which, int bitmap,
                                   ProtocolVersion pv) {
        if (pv != V_1_0)
            throw new VersionMismatchException(E_DEPRECATED + V_1_1);
        int shift = which == NW_SRC ? NW_SRC_SHIFT : NW_DST_SHIFT;
        int dontCareBits = (bitmap >>> shift) & BIT_FIELD_MASK;
        return getIpMaskForWildBits(dontCareBits);
    }

    /*
     * Wild bits are 0 bits (so we can AND)
     *  value = 0 means no bits are wild        : mask = ff ff ff ff
     *  value = 1 means LSB is wild             : mask = ff ff ff fe
     *  :
     *  value = 8 means 8 LSBits are wild       : mask = ff ff ff 00
     *  :
     *  value = 31 means all but MSB are wild   : mask = 80 00 00 00
     *  value = 32...63 means all bits are wild : mask = 00 00 00 00
     */

    private static final Integer[] VALID_MASKS = {
            0xffffffff, 0xfffffffe, 0xfffffffc, 0xfffffff8,
            0xfffffff0, 0xffffffe0, 0xffffffc0, 0xffffff80,
            0xffffff00, 0xfffffe00, 0xfffffc00, 0xfffff800,
            0xfffff000, 0xffffe000, 0xffffc000, 0xffff8000,
            0xffff0000, 0xfffe0000, 0xfffc0000, 0xfff80000,
            0xfff00000, 0xffe00000, 0xffc00000, 0xff800000,
            0xff000000, 0xfe000000, 0xfc000000, 0xf8000000,
            0xf0000000, 0xe0000000, 0xc0000000, 0x80000000,
            0x00000000,
    };

    /**
     * Encodes the specified netmask value into its bit-field in the given
     * bitmap.
     *
     * @param which which value (NW_SRC or NW_DST)
     * @param mask the netmask
     * @param bitmap the bitmap to insert into
     * @param pv the protocol version
     * @return the modified bitmap
     * @throws VersionMismatchException if not v1.0
     * @throws IllegalArgumentException if which is not appropriate or if mask
     *         does not express a valid subnet mask
     */
    static int encodeNetmask(Wildcard which, IpAddress mask, int bitmap,
                             ProtocolVersion pv) {
        if (pv != V_1_0)
            throw new VersionMismatchException(E_DEPRECATED + V_1_1);

        int idx = 0;
        int nMasks = VALID_MASKS.length;
        int maxIdx = nMasks - 1;
        // Starts with exact match for don't care bits
        int dontCareBitsValue = 0;

        if (mask != null) {
            int maskAsInt = ByteUtils.getInteger(mask.toByteArray(), 0);
            while (VALID_MASKS[idx] != maskAsInt && idx < maxIdx)
                idx++;
            if (idx == maxIdx && VALID_MASKS[idx] != maskAsInt)
                throw new IllegalArgumentException("Invalid mask value: " + mask);
            dontCareBitsValue = idx;
        }

        int shift = which == NW_SRC ? NW_SRC_SHIFT : NW_DST_SHIFT;
        return (dontCareBitsValue << shift) | bitmap;
    }

    /**
     * Creates and returns a set of wildcard flags representing all wild bits
     * set for the specified protocol version.
     *
     * @param pv the protocol version
     * @return a set of wildcard flags representing "all" wild
     */
    static Set<Wildcard> allWild(ProtocolVersion pv) {
        Wildcard[] allWild = pv == V_1_0 ? ALL_WILD_10 : ALL_WILD_11;
        return new HashSet<Wildcard>(Arrays.asList(allWild));
    }
}
