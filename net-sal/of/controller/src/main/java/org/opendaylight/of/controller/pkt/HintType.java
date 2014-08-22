/*
 * (c) Copyright 2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */


package org.opendaylight.of.controller.pkt;

import java.util.HashMap;
import java.util.Map;

/**
 * Designates the standard {@link Hint} types.
 *
 * @author Simon Hunt
 */
public enum HintType {
    /** The identity of the packet listener that handled
     * the <em>Packet-In</em> message.
     */
    HANDLER(0),
    /** This packet has been identified as a test packet. */
    TEST_PACKET(1),

    // TODO: add more standard types once we identify what they are.
    ;

    private final int encodedType;

    HintType(int id) {
        encodedType = id;
    }

    /** Returns the type encoded as an int.
     *
     * @return the encoded type
     */
    public int encodedType() {
        return encodedType;
    }

    /** Returns the hint type for the given coded value; or null if not
     * defined.
     *
     * @param encodedType the encoded type
     * @return the corresponding hint type (or null)
     */
    public static HintType decode(int encodedType) {
        return KNOWN.get(encodedType);
    }

    /** Returns a hint type as a string. If it is a standard type,
     * the type logical name is included in the string.
     *
     * @param code the encoded type
     * @return the type in string format
     */
    public static String typeToString(int code) {
        StringBuilder sb = new StringBuilder();
        sb.append(code);
        HintType h = decode(code);
        if (h != null)
            sb.append("(").append(h).append(")");
        return sb.toString();
    }

    // =====================================================================
    private static final Map<Integer, HintType> KNOWN =
            new HashMap<Integer, HintType>(values().length);
    static {
        for (HintType h: values())
            KNOWN.put(h.encodedType, h);
    }
}
