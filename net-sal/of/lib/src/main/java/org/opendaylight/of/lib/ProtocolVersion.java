/*
 * (c) Copyright 2012 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */


package org.opendaylight.of.lib;

import org.opendaylight.util.ByteUtils;

import java.util.Set;

/**
 * Denotes the published OpenFlow Protocol versions.
 *
 * @author Simon Hunt
 */
public enum ProtocolVersion {
    /** Version 1.0.0 (December 31, 2009) */
    V_1_0((byte) 0x1, "1.0.0"),
    /** Version 1.1.0 (February 28, 2011) */
    V_1_1((byte) 0x2, "1.1.0"),
    /** Version 1.2 (December 5, 2011) */
    V_1_2((byte) 0x3, "1.2"),   // NOTE: yes, it really is "1.2" not "1.2.0"
    /** Version 1.3.0 (June 25, 2012); Version 1.3.1 (September 6, 2012) */
    V_1_3((byte) 0x4, "1.3.0"),
    ;
    // TODO update V_1_3 string to 1.3.1, once we support the HELLO TLV msg

    // NOTE: 1.1.0 OpenFlow Switch Specification (2/28/11) states that
    //  experimental versions have the highest bit set, with the
    //  lower bits specifying the revision number. But we aren't
    //  (currently) planning on supporting experimental versions.

    private final byte ver;
    private final String str;

    ProtocolVersion(byte v, String s) {
        ver = v;
        str = s;
    }

    /** Returns the encoded byte value for this protocol version.
     *
     * @return the encoded byte value
     */
    public byte code() {
        return ver;
    }

    /** Returns a "friendly" text representation of the protocol version,
     * as stated on the cover page of the related specification. For example,
     * the {@link #V_1_3} constant will return "1.3.0".
     *
     * @return a friendly text representation of the version
     */
    public String toDisplayString() {
        return str;
    }

    /** Returns true if this protocol version is earlier than (less than)
     * the specified version.
     *
     * @param v the version to test against
     * @return true, if this version is earlier than the given version
     */
    public boolean lt(ProtocolVersion v) {
        return compareTo(v) < 0;
    }

    /** Returns true if this protocol version is earlier than (less than)
     * or equal to the specified version.
     *
     * @param v the version to test against
     * @return true, if this version is earlier than or the same as the
     *          given version
     */
    public boolean le(ProtocolVersion v) {
        return compareTo(v) <= 0;
    }

    /** Returns true if this protocol version is later than (greater than)
     *  the specified version.
     *
     * @param v the version to test against
     * @return true, if this version is greater than the given version
     */
    public boolean gt(ProtocolVersion v) {
        return compareTo(v) > 0;
    }

    /** Returns true if this protocol version is later than (greater than)
     * or equal to the specified version.
     *
     * @param v the version to test against
     * @return true, if this version is later than or the same as the
     *          given version
     */
    public boolean ge(ProtocolVersion v) {
        return compareTo(v) >= 0;
    }


    /** Returns the latest protocol version; currently {@link #V_1_3}.
     *
     * @return the latest protocol version
     */
    public static ProtocolVersion latest() {
        return V_1_3;
    }

    /** Decodes the version byte value and returns the version
     * constant associated with that value. If the byte value
     * is not recognized, a {@link DecodeException} will be thrown.
     *
     * @param v the encoded byte value
     * @return the corresponding version constant
     * @throws DecodeException if protocol version cannot be decoded
     */
    public static ProtocolVersion decode(byte v) throws DecodeException {
        ProtocolVersion version = null;
        for (ProtocolVersion pv: values())
            if (pv.ver == v) {
                version = pv;
                break;
            }
        if (version == null)
            throw new DecodeException("Unknown OpenFlow Protocol " +
                    "version code: 0x" + ByteUtils.byteToHex(v));
        return version;
    }

    /** Returns the constant that has the "friendly" text representation
     * specified, or null if no match is found.
     *
     * @param s the string representation
     * @return the corresponding version constant, or null for no match
     */
    public static ProtocolVersion fromString(String s) {
        ProtocolVersion version = null;
        for (ProtocolVersion pv: values())
            if (pv.str.equals(s)) {
                version = pv;
                break;
            }
        return version;
    }

    /** Returns the highest protocol version from the given set.
     * If the argument is null, or the set is empty, {@code null} will be
     * returned.
     *
     * @param versions the set of versions to examine
     * @return the highest version from the set
     */
    public static ProtocolVersion max(Set<ProtocolVersion> versions) {
        if (versions == null || versions.size() == 0)
            return null;
        ProtocolVersion max = null;
        for (ProtocolVersion pv: versions)
            if (max == null || max.lt(pv))
                max = pv;
        return max;
    }
}
