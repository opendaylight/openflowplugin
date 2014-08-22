/*
 * (c) Copyright 2012 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */


package org.opendaylight.of.lib.match;

import org.opendaylight.of.lib.DecodeException;
import org.opendaylight.of.lib.OfpCodeBasedEnum;
import org.opendaylight.of.lib.ProtocolVersion;
import org.opendaylight.of.lib.VersionMismatchException;

/**
 * Denotes the different match types.
 *
 * @author Simon Hunt
 */
public enum MatchType implements OfpCodeBasedEnum {
    /** Standard match; Since 1.0; Deprecated.
     * <p>
     * Although the "STANDARD" match was not explicitly defined until the
     * 1.1 specification, it is implied in the 1.0 specification.
     * Therefore we have encoded it here as "since 1.0".
     *
     */
    STANDARD(0, ProtocolVersion.V_1_0),
    /** OpenFlow Extensible Match; Since 1.2. */
    OXM(1, ProtocolVersion.V_1_2),
    ;

    private final int code;
    private final ProtocolVersion minVer;

    MatchType(int code, ProtocolVersion minVer) {
        this.code = code;
        this.minVer = minVer;
    }

    @Override
    public int getCode(ProtocolVersion pv) {
        return code;
    }

    /** Decodes the match type value and returns the corresponding
     * constant. If the code is not recognized, an exception is thrown.
     *
     * @param code the encoded type
     * @param pv the protocol version
     * @return the type
     * @throws DecodeException if the code is unrecognized
     * @throws VersionMismatchException if the code is not supported in the
     *          given version
     */
    static MatchType decode(int code, ProtocolVersion pv)
            throws DecodeException {
        MatchType type = null;
        for (MatchType t: values())
            if (t.code == code) {
                type = t;
                break;
            }
        if (type == null)
            throw new DecodeException("MatchType: unknown code: " + code);

        // version constraints
        if (pv.lt(type.minVer))
            throw new VersionMismatchException(pv + " bad MatchType: " + type);
        return type;
    }

}
