/*
 * (c) Copyright 2012 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */


package org.opendaylight.of.lib.msg;

import org.opendaylight.of.lib.DecodeException;
import org.opendaylight.of.lib.ProtocolVersion;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

/**
 * Denotes OpenFlow message types. Each constant is documented with when it
 * was introduced into the protocol.
 *
 * @author Simon Hunt
 */
public enum MessageType {
    /* Implementation note: -1 represents Not-Applicable for a version.
     * I would have declared a constant NA = -1, except that we can't
     * forward reference, and we can't declare it here.
     */
    /** Hello; Since 1.0. */
    HELLO(0),
    /** Error; Since 1.0. */
    ERROR(1),
    /** Echo Request; Since 1.0. */
    ECHO_REQUEST(2),
    /** Echo Reply; Since 1.0. */
    ECHO_REPLY(3),
    /** Experimenter; Since 1.0 (known then as Vendor, but renamed to
     * Experimenter in 1.1). */
    EXPERIMENTER(4),
    /** Features Request; Since 1.0. */
    FEATURES_REQUEST(5),
    /** Features Reply; Since 1.0. */
    FEATURES_REPLY(6),
    /** Get Config Request; Since 1.0. */
    GET_CONFIG_REQUEST(7),
    /** Get Config Reply; Since 1.0. */
    GET_CONFIG_REPLY(8),
    /** Set Config; Since 1.0. */
    SET_CONFIG(9),
    /** Packet In; Since 1.0. */
    PACKET_IN(10),
    /** Flow Removed; Since 1.0. */
    FLOW_REMOVED(11),
    /** Port Status; Since 1.0. */
    PORT_STATUS(12),
    /** Packet Out; Since 1.0. */
    PACKET_OUT(13),
    /** Flow Mod; Since 1.0. */
    FLOW_MOD(14),
    /** Group Mod; Since 1.1. */
    GROUP_MOD(-1, 15, 15, 15),
    /** Port Mod; Since 1.0. */
    PORT_MOD(15, 16, 16, 16),
    /** Table Mod; Since 1.1. */
    TABLE_MOD(-1, 17, 17, 17),

    // Implementation Note: Stats-Req/Rep folded into MP-Req/Rep.
    /** Multipart Request; Since 1.3 (but see {@link OfmMultipartRequest}). */
    MULTIPART_REQUEST(16, 18, 18, 18),
    /** Multipart Reply; Since 1.3 (but see {@link OfmMultipartReply}). */
    MULTIPART_REPLY(17, 19, 19, 19),

    /** Barrier Request; Since 1.0. */
    BARRIER_REQUEST(18, 20, 20, 20),
    /** Barrier Reply; Since 1.0. */
    BARRIER_REPLY(19, 21, 21, 21),
    /** Get Config Request; Since 1.0. */
    QUEUE_GET_CONFIG_REQUEST(20, 22, 22, 22),
    /** Get Config Reply; Since 1.0. */
    QUEUE_GET_CONFIG_REPLY(21, 23, 23, 23),
    /** Role Request; Since 1.2. */
    ROLE_REQUEST(-1, -1, 24, 24),
    /** Role Reply; Since 1.2. */
    ROLE_REPLY(-1, -1, 25, 25),
    /** Get Async Request; Since 1.3. */
    GET_ASYNC_REQUEST(-1, -1, -1, 26),
    /** Get Async Reply; Since 1.3. */
    GET_ASYNC_REPLY(-1, -1, -1, 27),
    /** Set Async; Since 1.3. */
    SET_ASYNC(-1, -1, -1, 28),
    /** Meter Mod; Since 1.3. */
    METER_MOD(-1, -1, -1, 29),
    ;

    /** The code (u8) when the same across all versions of the protocol. */
    private final int code;
    /** The code (u8) when it differs across versions of the protocol. */
    private EnumMap<ProtocolVersion, Integer> alt;

    /** Constructor for types that have the same code across all versions.
     *
     * @param code the code
     */
    private MessageType(int code) {
        this.code = code;
    }

    /** Constructor for types that have different encodings in different
     * versions of the protocol.
     *
     * @param v1_0 the code for version 1.0
     * @param v1_1 the code for version 1.1
     * @param v1_2 the code for version 1.2
     * @param v1_3 the code for version 1.3
     */
    private MessageType(int v1_0, int v1_1, int v1_2, int v1_3) {
        code = 0;
        alt = new EnumMap<ProtocolVersion, Integer>(ProtocolVersion.class);
        alt.put(ProtocolVersion.V_1_0, v1_0);
        alt.put(ProtocolVersion.V_1_1, v1_1);
        alt.put(ProtocolVersion.V_1_2, v1_2);
        alt.put(ProtocolVersion.V_1_3, v1_3);
    }


    /** Returns the code for this message type, under the given protocol.
     *
     * @param pv the protocol version
     * @return the message type code
     */
    public int getCode(ProtocolVersion pv) {
        return alt != null ? alt.get(pv) : code;
    }

    /* Secret Decoder Ring! */
    private static final Map<Integer, Alternates> DECODE =
            new HashMap<Integer, Alternates>();

    /* Generate integer to type (by version) lookup table. */
    static {
        for (MessageType mt: values()) {
            if (mt.alt != null) {
                // code maps to different types depending on version
                for (ProtocolVersion v: ProtocolVersion.values()) {
                    int code = mt.alt.get(v);
                    if (code != -1) {
                        // add this type to the alternates for the code
                        Alternates a = DECODE.get(code);
                        if (a == null) {
                            a = new Alternates();
                            DECODE.put(code, a);
                        }
                        a.alt.put(v, mt);
                    }
                }
            } else {
                // code is same type across all versions
                DECODE.put(mt.code, new Alternates(mt));
            }
        }
    }

    /** Encapsulates the alternate types for a given coded value. */
    private static class Alternates {
        private EnumMap<ProtocolVersion, MessageType> alt;
        private MessageType type;

        private Alternates() {
            alt = new EnumMap<ProtocolVersion, MessageType>(ProtocolVersion.class);
        }

        private Alternates(MessageType type) {
            this.type = type;
        }

        private MessageType getType(ProtocolVersion v) {
            return (alt != null) ? alt.get(v) : type;
        }
    }


    /** Returns the message type constant corresponding to the
     * encoded message type and the given protocol version.
     *
     *
     * @param typeCode the message type code
     * @param v the protocol version
     * @return the corresponding message type constant
     * @throws DecodeException if the argument combination is invalid
     */
    static MessageType decode(int typeCode, ProtocolVersion v)
            throws DecodeException {
        MessageType type = null;
        Alternates a = DECODE.get(typeCode);
        if (a != null)
            type = a.getType(v);
        if (type == null)
            throw new DecodeException("Unknown " + v + " type code: " + typeCode);
        return type;
    }
}
