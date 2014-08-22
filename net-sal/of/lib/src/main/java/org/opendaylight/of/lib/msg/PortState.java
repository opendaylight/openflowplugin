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

import java.util.*;

import static org.opendaylight.of.lib.ProtocolVersion.V_1_0;

/**
 * Describes a port's internal state.
 * <p>
 * All port state flags are read-only and cannot be changed by the
 * controller. When the port flags are changed, the switch sends a
 * {@link MessageType#PORT_STATUS PORT_STATUS} message to notify the
 * controller of the change.
 * <p>
 * From version 1.1 onwards, the spanning tree state flags
 * ({@code STP_*} constants) were dropped.
 *
 * @author Simon Hunt
 */
public enum PortState implements OfpBitmapEnum {
    /** No physical link present; Since 1.0. */
    LINK_DOWN(0x1, false),         // 1 << 0
    /** Port is blocked; Since 1.1.
     * <p>
     * Indicates that a switch protocol outside of OpenFlow, such as
     * 802.1D Spanning Tree, is preventing the use of that port with
     * {@link Port#FLOOD}.
     */
    BLOCKED(0x2, false),           // 1 << 1
    /** Live for Fast Failover Group; Since 1.1. */
    LIVE(0x4, false),              // 1 << 2

    /** Not learning or relaying frames; Since 1.0; Removed at 1.1. */
    STP_LISTEN(0x000, true),      // 0 << 8
    /** Learning but not relaying frames; Since 1.0; Removed at 1.1. */
    STP_LEARN(0x100, true),       // 1 << 8
    /** Learning and relaying frames; Since 1.0; Removed at 1.1. */
    STP_FORWARD(0x200, true),     // 2 << 8
    /** Not part of spanning tree; Since 1.0; Removed at 1.1. */
    STP_BLOCK(0x300, true),       // 3 << 8
    ;

    private static final ResourceBundle RES =
            ResourceUtils.getBundledResource(PortState.class);

    private static final String E_MUTEX = RES.getString("e_mutex");

    private static final int NA_BIT = -1;

    private final int value;
    private final boolean v10Only;

    PortState(int value, boolean v10Only) {
        this.value = value;
        this.v10Only = v10Only;
    }


    /** STP field mask. */
    private static final int STP_MASK = 0x300; // 3 << 8
    private static final int STP_SHIFT = 8;

    /** Those constants that are encoded in the 2-bit STP field. */
    private static final PortState[] STP_FLAGS = {
            STP_LISTEN,     // 0b00
            STP_LEARN,      // 0b01
            STP_FORWARD,    // 0b10
            STP_BLOCK,      // 0b11
    };

    /** The constants that each represent a bit in the field. */
    private static final PortState[] BIT_SET = {
            LINK_DOWN,
            BLOCKED,
            LIVE,
    };

    // masks (u32) for valid bit positions for each version
    private static final int[] MASKS = {
            0x301,     // 1.0
            0x007,     // 1.1
            0x007,     // 1.2
            0x007,     // 1.3
    };

    @Override
    public int getBit(ProtocolVersion pv) {
        return ( ((MASKS[pv.ordinal()] & value) == 0 && this != STP_LISTEN) ||
                (v10Only && pv.gt(V_1_0)) ) ? NA_BIT : value;
    }

    @Override
    public String toString() {
        return StringUtils.toCamelCase(name());
    }

    /** Returns a string representation of this constant suitable for
     * display. For example, {@code STP_LISTEN.toDisplayString()} returns:
     * <pre>
     *   "STP Listen"
     * </pre>
     * @return the display string
     */
    @Override
    public String toDisplayString() {
        String cc = toString();
        String display;
        try {
            display = RES.getString(cc);
        } catch (MissingResourceException e) {
            // should never happen, right?!
            display = "?" + cc + "?";
        }
        return display;
    }

    // our secret decoder ring.
    private static final OfpBitmapCodec<PortState> CODEC =
            new OfpBitmapCodec<PortState>(MASKS, BIT_SET);


    /** Decodes a port state bitmap to generate the corresponding set
     * of state flags.
     *
     * @param bitmap the bitmapped value
     * @param pv the protocol version
     * @return the set of corresponding state flags
     * @throws VersionMismatchException if unexpected bits found for the version
     */
    static Set<PortState> decodeBitmap(int bitmap, ProtocolVersion pv) {
        Set<PortState> result = CODEC.decode(bitmap, pv);
        // decode the STP field
        if (pv == V_1_0) {
            int stp = (bitmap & STP_MASK) >> STP_SHIFT;
            result.add(STP_FLAGS[stp]);
        }
        return result;
    }

    /** Encodes a set of port state flags as a bitmap. The {@code flags}
     * parameter may be null, in which case a bitmask of 0 (zero) is returned.
     *
     * @param flags the flags to encode
     * @param pv the protocol version
     * @return the flags encoded as a bitmap
     * @throws NullPointerException if protocol version is null
     * @throws VersionMismatchException if a flag is present that is not
     *          supported in the given protocol version
     */
    static int encodeBitmap(Set<PortState> flags, ProtocolVersion pv) {
        if (flags == null)
            return 0;

        PortState stpFlag = null;
        if (pv == V_1_0)
            stpFlag = removeStpFlag(flags);

        int bitmap = CODEC.encode(flags, pv);

        if (stpFlag != null)
            bitmap |= stpFlag.getBit(pv);

        return bitmap;
    }

    // check for mutual exclusion of STP flags, and remove it if present
    private static PortState removeStpFlag(Set<PortState> flags) {
        if (flags == null)
            return null;

        Set<PortState> mutex = new HashSet<PortState>(flags);
        // remove flags not in the mutex set
        mutex.removeAll(Arrays.asList(BIT_SET));
        if (mutex.size() > 1)
            throw new IllegalStateException(E_MUTEX + mutex);
        PortState stpFlag = mutex.isEmpty() ? null : mutex.iterator().next();
        if (stpFlag != null)
            flags.remove(stpFlag);
        return stpFlag;
    }
}
