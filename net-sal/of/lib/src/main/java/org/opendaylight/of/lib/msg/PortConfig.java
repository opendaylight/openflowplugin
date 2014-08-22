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
import org.opendaylight.util.ResourceUtils;
import org.opendaylight.util.StringUtils;

import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.Set;

/**
 * Indicates a port's behavior. Used in {@link Port} to describe the
 * current configuration. Used in {@link OfmPortMod} messages to configure
 * the port's behavior.
 *
 * @author Simon Hunt
 */
public enum PortConfig implements OfpBitmapEnum {
    /** Port is administratively down; Since 1.0. */
    PORT_DOWN(0x1, false),     // 1 << 0
    /** Disable 802.1D spanning tree on port; Since 1.0; Removed at 1.1. */
    NO_STP(0x2, true),        // 1 << 1
    /** Drop all packets received by port; Since 1.0. <p>
     * In 1.0 the meaning of this value is:
     * Drop all packets <em>except 802.1D spanning tree packets.</em>
     */
    NO_RECV(0x4, false),       // 1 << 2
    /** Drop Received 802.1D STP packets; Since 1.0; Removed at 1.1. */
    NO_RECV_STP(0x8, true),   // 1 << 3
    /** Do not include this port when flooding; Since 1.0; Removed at 1.1. */
    NO_FLOOD(0x10, true),     // 1 << 4
    /** Drop packets forwarded to port; Since 1.0. */
    NO_FWD(0x20, false),       // 1 << 5
    /** Do not send packet-in messages for port; Since 1.0. */
    NO_PACKET_IN(0x40, false), // 1 << 6
    ;

    /** Localization resource for toDisplayString(). */
    private static final ResourceBundle lion =
            ResourceUtils.getBundledResource(PortConfig.class);

    private static final int NA_BIT = -1;

    private final int bit;
    private final boolean v10Only;

    PortConfig(int bit, boolean v10Only) {
        this.bit = bit;
        this.v10Only = v10Only;
    }

    // masks (u32) for valid bit positions for each version
    private static final int[] MASKS = {
            0x7f,     // 1.0
            0x65,     // 1.1
            0x65,     // 1.2
            0x65,     // 1.3
    };

    @Override
    public int getBit(ProtocolVersion pv) {
        return pv.gt(ProtocolVersion.V_1_0) && v10Only ? NA_BIT : bit;
    }

    @Override
    public String toString() {
        return StringUtils.toCamelCase(name());
    }

    /** Returns a string representation of this constant suitable for
     * display. For example, {@code NO_PACKET_IN.toDisplayString()} returns:
     * <pre>
     *   "No Packet-In"
     * </pre>
     * @return the display string
     */
    @Override
    public String toDisplayString() {
        String cc = toString();
        String display;
        try {
            display = lion.getString(cc);
        } catch (MissingResourceException e) {
            // should never happen, right?!
            display = "?" + cc + "?";
        }
        return display;
    }

    // our secret decoder ring
    private static final OfpBitmapCodec<PortConfig> CODEC =
            new OfpBitmapCodec<PortConfig>(MASKS, values());

    /** Decodes a port config bitmap to generate the corresponding set of
     * config flags.
     *
     * @param bitmap the bitmapped value
     * @param pv the protocol version
     * @return the set of corresponding config flags
     * @throws VersionMismatchException if unexpected bits found for the version
     */
    static Set<PortConfig> decodeBitmap(int bitmap, ProtocolVersion pv) {
        return CODEC.decode(bitmap, pv);
    }

    /** Encodes a set of port config flags as a bitmap. The {@code flags}
     * parameter may be null, in which case a bitmap of 0 (zero) is returned.
     *
     * @param flags the flags to encode
     * @param pv the protocol version
     * @return the flags encoded as a bitmap
     * @throws NullPointerException if protocol version is null
     * @throws VersionMismatchException if a flag is present that is not
     *          supported in the given protocol version
     */
    static int encodeBitmap(Set<PortConfig> flags, ProtocolVersion pv) {
        return CODEC.encode(flags, pv);
    }

}
