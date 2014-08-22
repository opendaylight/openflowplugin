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

import static org.opendaylight.of.lib.CommonUtils.verMin12;

/**
 * Designates the reason for the packet-in message; Since 1.0.
 *
 * @author Simon Hunt
 * @author Scott Simes
 */
public enum PacketInReason implements OfpCodeBasedEnum {
    /** No matching flow (table-miss flow entry); Since 1.0. */
    NO_MATCH(0),
    /** Action explicitly output to controller; Since 1.0. */
    ACTION(1),
    /** Packet has invalid TTL; Since 1.2. */
    INVALID_TTL(2),
    ;

    private final int code;

    PacketInReason(int code) {
        this.code = code;
    }

    @Override
    public int getCode(ProtocolVersion pv) {
        return code;
    }

    private static final int[] MASKS = {
            0x3,    // 1.0
            0x3,    // 1.1
            0x7,    // 1.2
            0x7,    // 1.3
    };

    // our secret decoder ring for code to bitmap manipulations
    private static final OfpCodeBasedCodec<PacketInReason> CODEC =
            new OfpCodeBasedCodec<PacketInReason>(MASKS, values());

    /** Decodes the packet in reason code and returns the corresponding
     * constant. If the code is not recognized, an exception is thrown.
     *
     * @param code the encoded reason
     * @param pv the protocol version
     * @return the packet in reason
     * @throws DecodeException if the code is not recognized
     * @throws VersionMismatchException if the reason type is not
     *          supported in the given version
     */
    static PacketInReason decode(int code, ProtocolVersion pv)
            throws DecodeException {
        PacketInReason reason = null;
        for (PacketInReason p: values())
            if (p.code == code) {
                reason = p;
                break;
            }
        if (reason == null)
            throw new DecodeException("PacketInReason: unknown code: " + code);
        if (reason == INVALID_TTL)
            verMin12(pv, "INVALID_TTL");

        return reason;
    }

    /**
     * Decodes the packet in reason flags, based on the code associated with the
     * flags, and returns the set of corresponding constants.
     *
     * @param bitmap the bitmap to decode
     * @param pv the protocol version
     * @return the set of flags
     * @throws NullPointerException if protocol version is null
     * @throws VersionMismatchException if a bit position is not supported
     *          in the given version
     */
    static Set<PacketInReason> decodeFlags(int bitmap, ProtocolVersion pv) {
        return CODEC.decode(bitmap, pv);
    }

    /**
     * Encodes a set of packet in reason flags as a bitmap. The {@code flags}
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
    static int encodeFlags(Set<PacketInReason> flags, ProtocolVersion pv) {
        return CODEC.encode(flags, pv);
    }
}
