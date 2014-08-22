/*
 * (c) Copyright 2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */


package org.opendaylight.of.lib.instr;

import org.opendaylight.of.lib.DecodeException;
import org.opendaylight.of.lib.OfpCodeBasedEnum;
import org.opendaylight.of.lib.ProtocolVersion;
import org.opendaylight.of.lib.VersionMismatchException;

import static org.opendaylight.of.lib.ProtocolVersion.V_1_0;
import static org.opendaylight.of.lib.ProtocolVersion.V_1_1;

/**
 * Denotes the different actions that can be associated with
 * flow entries or packets (in 1.0 and 1.1).
 *
 * @author Simon Hunt
 * @see ActionType
 */
enum OldActionType implements OfpCodeBasedEnum {
    OUTPUT(0),
    SET_VLAN_VID(1),
    SET_VLAN_PCP(2),
    STRIP_VLAN(3, -1),
    SET_DL_SRC(4, 3),
    SET_DL_DST(5, 4),
    SET_NW_SRC(6, 5),
    SET_NW_DST(7, 6),
    SET_NW_TOS(8, 7),
    SET_NW_ECN(-1, 8),
    SET_TP_SRC(9),
    SET_TP_DST(10),
    COPY_TTL_OUT(-1, 11),
    COPY_TTL_IN(-1, 12),
    SET_MPLS_LABEL(-1, 13),
    SET_MPLS_TC(-1, 14),
    SET_MPLS_TTL(-1, 15),
    DEC_MPLS_TTL(-1, 16),
    PUSH_VLAN(-1, 17),
    POP_VLAN(-1, 18),
    PUSH_MPLS(-1, 19),
    POP_MPLS(-1, 20),
    // named ENQUEUE in 1.0...
    SET_QUEUE(11, 21),
    GROUP(-1, 22),
    SET_NW_TTL(-1, 23),
    DEC_NW_TTL(-1, 24),
    EXPERIMENTER(0xffff),
    ;

    private final int code10;
    private final int code11;

    OldActionType(int code) {
        this.code10 = code;
        this.code11 = code;
    }

    OldActionType(int code10, int code11) {
        this.code10 = code10;
        this.code11 = code11;
    }

    @Override
    public int getCode(ProtocolVersion pv) {
        return pv == V_1_0 ? code10 : code11;
    }

    /** Decodes the action type code and returns the corresponding
     * constant. If the code is not recognized, an exception is thrown.
     *
     * @param code the encoded action type
     * @param pv the protocol version
     * @return the action type
     * @throws DecodeException if the code is unrecognized
     * @throws VersionMismatchException if pv &gt; 1.1
     */
    static OldActionType decode(int code, ProtocolVersion pv)
            throws DecodeException {
        if (pv.gt(V_1_1))
            throw new VersionMismatchException(pv + ": OldActionType:" +
                                                    " 1.0 and 1.1 only");
        OldActionType type = null;
        for (OldActionType t: values()) {
            if (code != -1 && t.getCode(pv) == code) {
                type = t;
                break;
            }
        }
        if (type == null)
            throw new DecodeException(pv + ": OldActionType: " +
                                        "unknown code: " + code);
        return type;
    }
}
