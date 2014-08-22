/*
 * (c) Copyright 2012 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */


package org.opendaylight.of.lib.msg;

import org.opendaylight.of.lib.OfpBitmapCodec;
import org.opendaylight.of.lib.OfpBitmapEnum;
import org.opendaylight.of.lib.ProtocolVersion;
import org.opendaylight.of.lib.VersionMismatchException;
import org.opendaylight.util.StringUtils;

import java.util.Set;

/**
 * Designates multipart request flags; Since 1.3.
 *
 * @author Simon Hunt
 */
public enum MultipartRequestFlag implements OfpBitmapEnum {
    /** More requests to follow; Since 1.3. */
    REQUEST_MORE(0x1),
    ;

    private static final int NA_BIT = -1;

    private final int bit;

    MultipartRequestFlag(int bit) {
        this.bit = bit;
    }

    private static final int[] MASKS = {
            0,      // not supported in 1.0
            0,      // not supported in 1.1
            0,      // not supported in 1.2
            0x1,    // 1.3
    };

    @Override
    public int getBit(ProtocolVersion pv) {
        return pv.lt(ProtocolVersion.V_1_3) ? NA_BIT : bit;
    }

    @Override
    public String toString() {
        return StringUtils.toCamelCase(name());
    }

    @Override
    public String toDisplayString() {
        // TODO: use lion bundle
        return toString();
    }


    // our secret decoder ring
    private static final OfpBitmapCodec<MultipartRequestFlag> CODEC =
            new OfpBitmapCodec<MultipartRequestFlag>(MASKS, values());


    /** Decodes the multipart request flags and returns the set of
     * corresponding constants.
     *
     * @param bitmap the bitmap to decode
     * @param pv the protocol version
     * @return the set of flags
     * @throws VersionMismatchException if a bit position is not supported
     *          in the given version
     */
    static Set<MultipartRequestFlag> decodeBitmap(int bitmap,
                                                  ProtocolVersion pv) {
        return CODEC.decode(bitmap, pv);
    }

    /** Encodes a set of multipart message request flags as a bitmap.
     * The {@code flags} parameter may be null, in which case a bitmap
     * of 0 (zero) is returned.
     *
     * @param flags the flags to encode
     * @param pv the protocol version
     * @return the flags encoded as a bitmap
     * @throws NullPointerException if protocol version is null
     * @throws VersionMismatchException if a flag is present that is not
     *          supported in the given protocol version
     */
    static int encodeBitmap(Set<MultipartRequestFlag> flags,
                            ProtocolVersion pv) {
        return CODEC.encode(flags, pv);
    }

}
