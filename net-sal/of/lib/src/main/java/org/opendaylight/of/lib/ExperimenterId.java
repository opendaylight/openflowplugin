/*
 * (c) Copyright 2012 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */


package org.opendaylight.of.lib;

import java.util.HashMap;
import java.util.Map;

/**
 * Denotes the documented experimenter IDs of the ONF members.
 * <p>
 *     See the <a href="https://www.opennetworking.org/wiki/display/PUBLIC/ONF+Registry">ONF Registry</a>.
 * </p>
 *
 * @author Simon Hunt
 */
public enum ExperimenterId {
    // == IEEE OUI-based ==

    /** Nicira Networks. */
    NICIRA(0x00002320),
    /** Big Switch Networks. */
    BIG_SWITCH(0x005c16c7),
    /** Vello Systems. */
    VELLO(0x00b0d2f5),
    /** Hewlett-Packard. */
    HP(0x00002481),
    /** Hewlett-Packard Labs. */
    HP_LABS(0x000004ea),

    // == Non-OUI ==
    /** Budapest University of Technology and Economics. */
    BUDAPEST_U(0xff000001),
    ;

    private final int encodedId;

    ExperimenterId(int encoded) {
        encodedId = encoded;
    }

    /** Returns the ID encoded as an int.
     *
     * @return the encoded ID
     */
    public int encodedId() {
        return encodedId;
    }

    /** Returns the experimenter ID for the given coded value; or null if
     * not defined.
     *
     * @param code the encoded ID
     * @return the corresponding experimenter ID (or null)
     */
    public static ExperimenterId decode(int code) {
        return KNOWN.get(code);
    }

    /** Returns an experimenter id as a hex-encoded string. If it is a
     * known id, the experimenter name is included in the string.
     *
     * @param code the encoded ID
     * @return the ID in string format
     */
    public static String idToString(int code) {
        StringBuilder sb = new StringBuilder("0x");
        sb.append(Integer.toHexString(code));
        ExperimenterId e = decode(code);
        if (e != null)
            sb.append("(").append(e).append(")");
        return sb.toString();
    }

    // ======================================================================
    private static final Map<Integer, ExperimenterId> KNOWN =
            new HashMap<Integer, ExperimenterId>(values().length);
    static {
        for (ExperimenterId e: values())
            KNOWN.put(e.encodedId, e);
    }
}
