/*
 * (c) Copyright 2012 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.of.lib.instr;

import org.opendaylight.of.lib.*;

import java.util.Arrays;
import java.util.Set;
import java.util.TreeSet;

import static org.opendaylight.of.lib.ProtocolVersion.V_1_1;
import static org.opendaylight.of.lib.ProtocolVersion.V_1_2;

/**
 * Denotes the different actions that can be associated with
 * flow entries, groups or packets.
 *
 * @author Simon Hunt
 */
public enum ActionType implements OfpCodeBasedEnum {
    /* Commented out items are in protocols 1.0 & 1.1, but their
     * functionality was (for the most part) replaced by SET_FIELD
     * in 1.2 onwards.
     *
     * Documented here for completeness, so you can see why
     * the code numbers are the way they are.
     */

    /** Output to switch port; Since 1.0. */
    OUTPUT(0),
    //================ v1.0 == v1.1 ===
    // SET_VLAN_VID      1       1
    // SET_VLAN_PCP      2       2
    // STRIP_VLAN        3      n/a
    // SET_DL_SRC        4       3
    // SET_DL_DST        5       4
    // SET_NW_SRC        6       5
    // SET_NW_DST        7       6
    // SET_NW_TOS        8       7
    // SET_NW_ECN       n/a      8
    // SET_TP_SRC        9       9
    // SET_TP_DST       10      10
    // ENQUEUE          11      n/a
    //=================================
    /** Copy TTL "outwards" -- from next-to-outermost
     * to outermost; Since 1.1.
     */
    COPY_TTL_OUT(11),
    /** Copy TTL "inwards" -- from outermost to
     * next-to-outermost; Since 1.1.
     */
    COPY_TTL_IN(12),
    //================ v1.0 == v1.1 ===
    // SET_MPLS_LABEL   n/a     13
    // SET_MPLS_TC      n/a     14
    //=================================
    /** Set MPLS TTL; Since 1.1. */
    SET_MPLS_TTL(15),
    /** Decrement MPLS TTL; Since 1.1. */
    DEC_MPLS_TTL(16),
    /** Push a new VLAN tag; Since 1.1. */
    PUSH_VLAN(17),
    /** Pop the outer VLAN tag; Since 1.1. */
    POP_VLAN(18),
    /** Push a new MPLS tag; Since 1.1. */
    PUSH_MPLS(19),
    /** Pop the outer MPLS tag; Since 1.1. */
    POP_MPLS(20),
    /** Set queue id when outputting to a port; Since 1.1. */
    SET_QUEUE(21),
    /** Apply group; Since 1.1. */
    GROUP(22),
    /** Set IP TTL; Simce 1.1. */
    SET_NW_TTL(23),
    /** Decrement IP TTL; Since 1.1. */
    DEC_NW_TTL(24),
    /** Set a header field using OXM TLV format; Since 1.2. */
    SET_FIELD(25),
    /** Push a new PBB service tag (I-TAG); Since 1.3. */
    PUSH_PBB(26),
    /** Pop the outer PBB service tag (I-TAG); Since 1.3. */
    POP_PBB(27),
    //================ v1.0 == v1.1 ===
    // VENDOR         0xffff    n/a
    //=================================
    /** Experimenter action; Since 1.1. (Was {@code VENDOR} in 1.0.) */
    EXPERIMENTER(0xffff),
    ;

    private static final int MAX_CODE_12 = 25;

    private final int code;

    ActionType(int code) {
        this.code = code;
    }

    @Override
    public int getCode(ProtocolVersion pv) {
        return code;
    }

    private static final int[] MASKS = {
            0x0000001,  // 1.0
            0x1ff9801,  // 1.1
            0x3ff9801,  // 1.2
            0xfff9801,  // 1.3
    };

    // our secret decode ring for code to bitmap manipulations
    private static final OfpCodeBasedCodec<ActionType> CODEC;

    // need to exclude EXPERIMENTER from set of values for bitmap encoding
    static {
        Set<ActionType> valSet =
                new TreeSet<ActionType>(Arrays.asList(values()));
        valSet.remove(EXPERIMENTER);
        CODEC = new OfpCodeBasedCodec<ActionType>(MASKS,
                valSet.toArray(new ActionType[valSet.size()]));
    }

    /** Decodes the action type code and returns the corresponding
     * constant. If the code is not recognized, an exception is thrown.
     *
     * @param code the encoded action type
     * @param pv the protocol version
     * @return the action type
     * @throws DecodeException if the code is unrecognized
     * @throws VersionMismatchException if the action type is not supported
     *           in the given version
     */
    static ActionType decode(int code, ProtocolVersion pv)
            throws DecodeException {
        ActionType type = null;

        if (pv.le(V_1_1))
            throw new VersionMismatchException("Should be using OldActionType");

        for (ActionType t: values())
            if (t.code == code) {
                type = t;
                break;
            }

        // exit now if no match
        if (type == null)
            throw new DecodeException("ActionType: unknown code: " + code);

        if (type != EXPERIMENTER) {
            if (pv == V_1_2 && code > MAX_CODE_12)
                type = null;
        }
        return type;
    }

    /**
     * Decodes the action type flags, based on the code associated with the
     * flags, and returns the set of corresponding constants.
     *
     * @param bitmap the bitmap to decode
     * @param pv the protocol version
     * @return the set of flags
     * @throws NullPointerException if protocol version is null
     * @throws VersionMismatchException if a bit position is not supported
     *          in the given version
     */
    public static Set<ActionType> decodeFlags(int bitmap, ProtocolVersion pv) {
        return CODEC.decode(bitmap, pv);
    }

    /**
     * Encodes a set of action type flags as a bitmap. The {@code flags}
     * parameter may be empty, in which case a bitmap of 0 (zero) is returned.
     *
     * @param flags the flags to encode
     * @param pv the protocol version
     * @return the flags encoded as a bitmap
     * @throws NullPointerException if either parameter is null
     * @throws IllegalArgumentException if code associated with enumeration
     *         constant is outside the valid range (0 - 31)
     * @throws VersionMismatchException if a flag is present that is not
     *          supported in the given protocol version
     */
    public static int encodeFlags(Set<ActionType> flags, ProtocolVersion pv) {
        return CODEC.encode(flags, pv);
    }

}
