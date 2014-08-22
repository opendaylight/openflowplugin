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

import java.util.HashSet;
import java.util.ResourceBundle;
import java.util.Set;

/**
 * Designates switch configuration flags.
 *
 * @author Simon Hunt
 */
public enum ConfigFlag implements OfpBitmapEnum {
    /** No special handling of fragments; Since 1.0. */
    FRAG_NORMAL(0), // zero
    /** Drop fragments; Since 1.0. */
    FRAG_DROP(0x1), // 1 << 0
    /** Reassemble fragments; Since 1.0.
     * Only if {@link Capability#IP_REASM} flag is set.
     */
    FRAG_REASM(0x2), // 1 << 1
    /** Send packets with invalid TTL to the controller; Since 1.1;
     * Removed at 1.3.
     */
    INV_TTL_TO_CTRLR(0x4), // 1 << 2
    ;

    private static final ResourceBundle RES = ResourceUtils.getBundledResource(
            ConfigFlag.class, "configFlag");

    static final String E_MUTEX = RES.getString("e_mutex");

    private static final int NA_BIT = -1;

    private final int bit;

    ConfigFlag(int bit) {
        this.bit = bit;
    }

    // masks (u16) for valid bit positions for each version
    private static final int[] MASKS = {
            0x3,     // 1.0
            0x7,     // 1.1
            0x7,     // 1.2
            0x3,     // 1.3
    };

    @Override
    public int getBit(ProtocolVersion pv) {
        return (((bit & MASKS[pv.ordinal()]) == 0) && this != FRAG_NORMAL)
                ? NA_BIT : bit;
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
    private static final OfpBitmapCodec<ConfigFlag> CODEC =
            new OfpBitmapCodec<ConfigFlag>(MASKS, values());


    private static void checkMutexFlags(Set<ConfigFlag> flags) {
        // special handling of "FRAG_*" values
        Set<ConfigFlag> mutex = new HashSet<ConfigFlag>(flags);
        // remove flags not involved in mutex
        mutex.remove(INV_TTL_TO_CTRLR);
        if (mutex.isEmpty()) {
            mutex.add(FRAG_NORMAL);
            flags.add(FRAG_NORMAL);
        }
        if (mutex.size() != 1)
            throw new IllegalStateException(E_MUTEX + mutex);
    }

    /**
     * Decodes a configuration flag bitmap to generate the corresponding
     * set of configuration flags.
     *
     * @param bitmap the bitmapped configuration flags
     * @param pv the protocol version
     * @return the set of corresponding flags
     * @throws VersionMismatchException if unexpected flag bits found for
     *          the specified version
     */
    static Set<ConfigFlag> decodeBitmap(int bitmap, ProtocolVersion pv) {
        Set<ConfigFlag> result = CODEC.decode(bitmap, pv);
        // special handling of "FRAG_*" values
        checkMutexFlags(result);
        return result;
    }

    /**
     * Encodes a set of configuration flags as a bitmap. The {@code flags}
     * parameter may be null, in which case a bitmap of 0 (zero) is returned.
     *
     * @param flags the flags to encode
     * @param pv the protocol version
     * @return the flags encoded as a bitmap
     * @throws NullPointerException if protocol version is null
     * @throws VersionMismatchException if a flag is present that is not
     *          supported in the given protocol version
     */
    static int encodeBitmap(Set<ConfigFlag> flags, ProtocolVersion pv) {
        if (flags != null) {
            checkMutexFlags(flags);
            // NOTE: special handling for FRAG_NORMAL, which is encoded as zero
            // we have to remove it, since the standard codec expects a 1 bit.
            flags.remove(FRAG_NORMAL);
        }
        return CODEC.encode(flags, pv);
    }
}