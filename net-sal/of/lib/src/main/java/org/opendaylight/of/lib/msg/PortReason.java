/*
 * (c) Copyright 2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.of.lib.msg;

import org.opendaylight.of.lib.*;

import java.util.Set;

/**
 * Designates the reason for port status asynchronous message; Since 1.0.
 *
 * @author Radhika Hegde
 * @author Scott Simes
 */
public enum PortReason implements OfpCodeBasedEnum {
    /** Indicates port added; Since 1.0. */
    ADD(0),
    /** Indicates port removed; Since 1.0. */
    DELETE(1),
    /** Indicates any port attribute modified; Since 1.0. */
    MODIFY(2),
    ;

    private final int code;

    PortReason(int code) {
        this.code = code;
    }

    @Override
    public int getCode(ProtocolVersion pv) {
        return code;
    }

    private static final int[] MASKS = {
            0x7,    // 1.0
            0x7,    // 1.1
            0x7,    // 1.2
            0x7,    // 1.3
    };

    // our secret decoder ring for code to bitmap manipulations
    private static final OfpCodeBasedCodec<PortReason> CODEC =
            new OfpCodeBasedCodec<PortReason>(MASKS, values());

    /** Decodes the port status reason code and returns the corresponding
     * constant. If the code is not recognized, an exception is thrown.
     *
     * @param code the encoded reason
     * @param pv the protocol version
     * @return the corresponding reason
     * @throws DecodeException if the code is not recognized
     */
    static PortReason decode(int code, ProtocolVersion pv)
            throws DecodeException {
        // No version constraints, currently: same across v1.0 - v1.3
        PortReason reason = null;
        for (PortReason p: values())
            if (p.code == code) {
                reason = p;
                break;
            }
        if (reason == null)
            throw new DecodeException("PortReason: unknown code: " + code);
        return reason;
    }

    /**
     * Decodes the port reason flags, based on the code associated with the
     * flags, and returns the set of corresponding constants.
     *
     * @param bitmap the bitmap to decode
     * @param pv the protocol version
     * @return the set of flags
     * @throws NullPointerException if protocol version is null
     * @throws VersionMismatchException if a bit position is not supported
     *          in the given version
     */
    static Set<PortReason> decodeFlags(int bitmap, ProtocolVersion pv) {
        return CODEC.decode(bitmap, pv);
    }

    /**
     * Encodes a set of port reason flags as a bitmap. The {@code flags}
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
    static int encodeFlags(Set<PortReason> flags, ProtocolVersion pv) {
        return CODEC.encode(flags, pv);
    }
}
