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
 * Designates port features.
 *
 * @author Simon Hunt
 */
public enum PortFeature implements OfpBitmapEnum {
    /** 10 Mb half-duplex rate support; Since 1.0. */
    RATE_10MB_HD(0x1),
    /** 10 Mb full-duplex rate support; Since 1.0. */
    RATE_10MB_FD(0x2),
    /** 100 Mb half-duplex rate support; Since 1.0. */
    RATE_100MB_HD(0x4),
    /** 100 Mb full-duplex rate support; Since 1.0. */
    RATE_100MB_FD(0x8),
    /** 1 Gb half-duplex rate support; Since 1.0. */
    RATE_1GB_HD(0x10),
    /** 1 Gb full-duplex rate support; Since 1.0. */
    RATE_1GB_FD(0x20),
    /** 10 Gb full-duplex rate support; Since 1.0. */
    RATE_10GB_FD(0x40),
    /** 40 Gb full-duplex rate support; Since 1.1. */
    RATE_40GB_FD(-1, 0x80),
    /** 100 Gb full-duplex rate support; Since 1.1. */
    RATE_100GB_FD(-1, 0x100),
    /** 1 Tb full-duplex rate support; Since 1.1. */
    RATE_1TB_FD(-1, 0x200),
    /** Other rate, not in the list; Since 1.1. */
    RATE_OTHER(-1, 0x400),
    /** Copper medium; Since 1.0. */
    COPPER(0x80, 0x800),
    /** Fiber medium; Since 1.0. */
    FIBER(0x100, 0x1000),
    /** Auto-negotiation; Since 1.0. */
    AUTONEG(0x200, 0x2000),
    /** Pause; Since 1.0. */
    PAUSE(0x400, 0x4000),
    /** Asymmetric pause; Since 1.0. */
    PAUSE_ASYM(0x800, 0x8000),
    ;

    /** Localization resource for toDisplayString(). */
    private static final ResourceBundle lion =
            ResourceUtils.getBundledResource(PortFeature.class);

    private final int v10Code;
    private final int v123Code;

    PortFeature(int code) {
        v10Code = code;
        v123Code = code;
    }

    PortFeature(int v10Code, int v123Code) {
        this.v10Code = v10Code;
        this.v123Code = v123Code;
    }

    // masks (u32) for valid bit positions for each version
    private static final int[] MASKS = {
            0x0fff,     // 1.0
            0xffff,     // 1.1
            0xffff,     // 1.2
            0xffff,     // 1.3
    };

    @Override
    public int getBit(ProtocolVersion pv) {
        return pv == ProtocolVersion.V_1_0 ? v10Code : v123Code;
    }

    @Override
    public String toString() {
        return StringUtils.toCamelCase(name());
    }

    /** Returns a string representation of this constant suitable for
     * display. For example, {@code PAUSE_ASYM.toDisplayString()} returns:
     * <pre>
     *   "Asymmetric pause"
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
    private static final OfpBitmapCodec<PortFeature> CODEC =
            new OfpBitmapCodec<PortFeature>(MASKS, values());

    /** Decodes a port feature bitmap to generate the corresponding set of
     * feature flags. If the bitmap is all zeros, null is returned to
     * indicate that the features are unsupported/unavailable.
     *
     * @param bitmap the bitmapped features
     * @param pv the protocol version
     * @return the set of corresponding feature flags, or null if
     *          unsupported/unavailable
     * @throws VersionMismatchException if unexpected flag bits found for
     *          the specified version
     */
    static Set<PortFeature> decodeBitmap(int bitmap, ProtocolVersion pv) {
        return bitmap == 0 ? null : CODEC.decode(bitmap, pv);
    }

    /** Encodes a set of port feature flags as a bitmap. The {@code flags}
     * parameter may be null, in which case a bitmask of 0 (zero) is returned.
     *
     * @param flags the flags to encode
     * @param pv the protocol version
     * @return the flags encoded as a bitmap
     * @throws NullPointerException if protocol version is null
     * @throws VersionMismatchException if a flag is present that is not
     *          supported in the given protocol version
     */
    static int encodeBitmap(Set<PortFeature> flags, ProtocolVersion pv) {
        return CODEC.encode(flags, pv);
    }
}
