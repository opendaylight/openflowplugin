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

import java.util.ResourceBundle;
import java.util.Set;

/**
 * Indicates behavior of a flow table for unmatched packets; Since 1.1;
 * Removed at 1.3.
 *
 * @author Simon Hunt
 */
public enum TableConfig implements OfpBitmapEnum {
    /** Unmatched packets are sent to the controller;
     * Since 1.1; Removed at 1.3.
     */
    TABLE_MISS_CONTROLLER(0), // zero
    /** Unmatched packets are directed to the next table in the pipeline;
     * Since 1.1; Removed at 1.3.
     * If the miss occurred in the last table of the pipeline, the unmatched
     * packet is sent to the controller.
     */
    TABLE_MISS_CONTINUE(0x1), // 1 << 0
    /** Drops unmatched packets; Since 1.1; Removed at 1.3. */
    TABLE_MISS_DROP(0x2), // 1 << 1
    ;

    private static final ResourceBundle RES = ResourceUtils.getBundledResource(
            TableConfig.class, "tableConfig");

    static final String E_MUTEX = RES.getString("e_mutex");

    private static final int NA_BIT = -1;

    private final int bit;

    TableConfig(int bit) {
        this.bit = bit;
    }

    // masks (u32) for valid bit positions for each version
    private static final int[] MASKS = {
            0x0,    // 1.0 (not supported)
            0x3,    // 1.1
            0x3,    // 1.2
            0x0,    // 1.3 (reserved for future use)
    };

    @Override
    public int getBit(ProtocolVersion pv) {
        return (pv == ProtocolVersion.V_1_1 || pv == ProtocolVersion.V_1_2) ? bit : NA_BIT;
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
    private static final OfpBitmapCodec<TableConfig> CODEC =
            new OfpBitmapCodec<TableConfig>(MASKS, values());

    /** Decodes a table configuration flag bitmap to generate the
     * corresponding set of flags.
     *
     * @param bitmap the bitmapped configuration flags
     * @param pv the protocol version
     * @return the set of corresponding flags
     * @throws VersionMismatchException if unexpected flag bits found for
     *          the specified version
     */
    static Set<TableConfig> decodeBitmap(int bitmap, ProtocolVersion pv) {
        Set<TableConfig> result = CODEC.decode(bitmap, pv);
        if (pv == ProtocolVersion.V_1_3)
            return null; // reserved but unused in 1.3
                         // no need to store an empty set

        // special handling of default value (versions 1.1, 1.2)
        checkMutexFlags(result);
        return result;
    }

    /** Encodes a set of port table flags as a bitmap. The {@code flags}
     * parameter may be null, in which case a bitmask of 0 (zero) is returned.
     *
     * @param flags the flags to encode
     * @param pv the protocol version
     * @return the flags encoded as a bitmap
     * @throws NullPointerException if protocol version is null
     * @throws VersionMismatchException if a flag is present that is not
     *          supported in the given protocol version
     */
    static int encodeBitmap(Set<TableConfig> flags, ProtocolVersion pv) {
        if (pv.lt(ProtocolVersion.V_1_3))
            checkMutexFlags(flags);
        // have to remove TABLE_MISS_CONTROLLER (value 0x0) for default
        // codec to work, since it expects a 1 bit for each flag
        if (flags != null)
            flags.remove(TABLE_MISS_CONTROLLER);
        return CODEC.encode(flags, pv);
    }

    // does flag validation
    private static void checkMutexFlags(Set<TableConfig> flags) {
        if (flags != null) {
            if (flags.isEmpty())
                flags.add(TABLE_MISS_CONTROLLER);
            if (flags.size() != 1)
                throw new IllegalStateException(E_MUTEX + flags);
        }
    }
}