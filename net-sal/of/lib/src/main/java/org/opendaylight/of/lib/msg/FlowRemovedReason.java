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

import static org.opendaylight.of.lib.CommonUtils.verMin11;

/**
 * Designates the reason a flow was removed; Since 1.0.
 *
 * @author Sudheer Duggisetty
 * @author Scott Simes
 */
public enum FlowRemovedReason implements OfpCodeBasedEnum {
    /** Flow idle time exceeded the idle timeout; Since 1.0. */
    IDLE_TIMEOUT(0),
    /** Time exceeded the hard timeout; Since 1.0. */
    HARD_TIMEOUT(1),
    /** Evicted by a DELETE flow mod; Since 1.0. */
    DELETE(2),
    /** Group was removed; Since 1.1. */
    GROUP_DELETE(3)
    ;

    private final int code;

    FlowRemovedReason(int code) {
        this.code = code;
    }

    @Override
    public int getCode(ProtocolVersion pv) {
        return code;
    }

    private static final int[] MASKS = {
            0x7,    // 1.0
            0xf,    // 1.1
            0xf,    // 1.2
            0xf,    // 1.3
    };

    // our secret decoder ring for code to bitmap manipulations
    private static final OfpCodeBasedCodec<FlowRemovedReason> CODEC =
            new OfpCodeBasedCodec<FlowRemovedReason>(MASKS, values());

    /** Decodes the flow removed reason code and returns the corresponding
     * constant. If the code is not recognized, an exception is thrown.
     *
     * @param code the encoded reason
     * @param pv the protocol version
     * @return the flow removed reason
     * @throws DecodeException if the code is not recognized
     * @throws VersionMismatchException if the reason type is not
     *          supported in the given version
     */
    static FlowRemovedReason decode(int code, ProtocolVersion pv)
        throws DecodeException {

        FlowRemovedReason reason = null;

        for (FlowRemovedReason fr: values())
            if (fr.code == code) {
                reason = fr;
                break;
            }

        if (reason == null)
            throw new DecodeException("FlowRemovedReason: unknown code: " +
                    code);

        if (reason == GROUP_DELETE)
            verMin11(pv, "GROUP_DELETE");

        return reason;
    }

    /**
     * Decodes the flow removed reason flags, based on the code associated with
     * the flags, and returns the set of corresponding constants.
     *
     * @param bitmap the bitmap to decode
     * @param pv the protocol version
     * @return the set of flags
     * @throws NullPointerException if protocol version is null
     * @throws VersionMismatchException if a bit position is not supported
     *          in the given version
     */
    static Set<FlowRemovedReason> decodeFlags(int bitmap, ProtocolVersion pv) {
        return CODEC.decode(bitmap, pv);
    }

    /**
     * Encodes a set of flow removed reason flags as a bitmap. The {@code flags}
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
    static int encodeFlags(Set<FlowRemovedReason> flags, ProtocolVersion pv) {
        return CODEC.encode(flags, pv);
    }
}
