/*
 * (c) Copyright 2013 Hewlett-Packard Development Company, L.P.
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
 * Designates supported action flags for Features-Reply; Since 1.0;
 * Removed at 1.1.
 * <p>
 * The "supported actions" bitmap in the 1.0 Features-Reply was dropped
 * in 1.1, that is, the field was renamed "Reserved" and described as
 * "Reserved for future use". These flags only apply to v1.0.
 *
 * @author Scott Simes
 * @author Simon Hunt
 */
public enum SupportedAction implements OfpBitmapEnum {
    /* IMPLEMENTATION NOTE:
     *  compare to ofp_action_type enum values from 1.0 spec (p.21-22)
     */

    /** Output to switch port. */
    OUTPUT(0x1), // 1 << 0

    /** Set the 802.1q VLAN id. */
    SET_VLAN_VID(0x2), // 1 << 1

    /** Set the 802.1q priority. */
    SET_VLAN_PCP(0x4), // 1 << 2

    /** Strip the 802.1q header. */
    STRIP_VLAN(0x8), // 1 << 3

    /** Set the Ethernet (Data-Link) source address. */
    SET_DL_SRC(0x10), // 1 << 4

    /** Set the Ethernet (Data-Link) destination address. */
    SET_DL_DST(0x20), // 1 << 5

    /** Set the IP (Network) source address. */
    SET_NW_SRC(0x40), // 1 << 6

    /** Set the IP (Network) destination address. */
    SET_NW_DST(0x80), // 1 << 7

    /** Set the IP (Network) ToS (DSCP field, 6 bits). */
    SET_NW_TOS(0x100), // 1 << 8

    /** Set the TCP/UDP source port. */
    SET_TP_SRC(0x200), // 1 << 9

    /** Set the TCP/UDP destination port. */
    SET_TP_DST(0x400), // 1 << 10

    /** Output to queue. */
    ENQUEUE(0x800), // 1 << 11
    ;

    private final int bit;

    SupportedAction (int bit) {
        this.bit = bit;
    }

    // masks (u32) for valid bit positions for each version
    private static final int[] MASKS = {
            0xfff,     // 1.0
            0x000,     // not supported in 1.1
            0x000,     // not supported in 1.2
            0x000,     // not supported in 1.3
    };

    @Override
    public int getBit(ProtocolVersion pv) {
        return pv == ProtocolVersion.V_1_0 ? bit : -1;
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
    private static final OfpBitmapCodec<SupportedAction> CODEC =
            new OfpBitmapCodec<SupportedAction>(MASKS, values());

    /** Decodes a Supported Actions bitmap to generate the corresponding
     * set of supported action flags.
     *
     * @param bitmap the bitmapped supported actions
     * @param pv the protocol version
     * @return the set of corresponding supported action flags
     * @throws VersionMismatchException if unexpected flags found for
     *          the specified version
     */
    static Set<SupportedAction> decodeBitmap(int bitmap, ProtocolVersion pv) {
        return CODEC.decode(bitmap, pv);
    }

    /** Encodes a set of suppported action flags as a bitmap.
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
    static int encodeBitmap(Set<SupportedAction> flags, ProtocolVersion pv) {
        return CODEC.encode(flags, pv);
    }
}
