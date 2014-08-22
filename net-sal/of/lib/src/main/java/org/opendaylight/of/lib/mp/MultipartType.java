/*
 * (c) Copyright 2012 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */


package org.opendaylight.of.lib.mp;

import org.opendaylight.of.lib.DecodeException;
import org.opendaylight.of.lib.OfpCodeBasedEnum;
import org.opendaylight.of.lib.ProtocolVersion;
import org.opendaylight.of.lib.VersionMismatchException;
import org.opendaylight.of.lib.msg.OfmMultipartReply;
import org.opendaylight.of.lib.msg.OfmMultipartRequest;

import static org.opendaylight.of.lib.CommonUtils.verMinSince;
import static org.opendaylight.of.lib.ProtocolVersion.*;

/**
 * Designates multipart message types; Since 1.3 (but see note).
 * <p>
 * <em>Important Note:</em> Although MULTIPART-REQUEST/REPLY messages were
 * only added to the protocol at 1.3, they are semantically equivalent to the
 * (now deprecated) STATS-REQUEST/REPLY messages in 1.0, 1.1 and 1.2.
 * <p>
 * The documentation for each constant below indicates when the type was
 * added to the protocol irrespective of "stats" vs. "multipart".
 *
 * @see OfmMultipartRequest
 * @see OfmMultipartReply
 * @author Simon Hunt
 */
public enum MultipartType implements OfpCodeBasedEnum {
    /** Description of this openflow switch; Since 1.0. */
    DESC(0, V_1_0),
    /** Individual flow statistics; Since 1.0. */
    FLOW(1, V_1_0),
    /** Aggregate flow statistics; Since 1.0. */
    AGGREGATE(2, V_1_0),
    /** Flow table statistics; Since 1.0. */
    TABLE(3, V_1_0),
    /** Port statistics; Since 1.0.
     * (Called "PORT" in 1.0, 1.1, and 1.2) */
    PORT_STATS(4, V_1_0),
    /** Queue statistics for a port; Since 1.0.  */
    QUEUE(5, V_1_0),
    /** Group counter statistics; Since 1.1. */
    GROUP(6, V_1_1),
    /** Group description; Since 1.1. */
    GROUP_DESC(7, V_1_1),
    /** Group features; Since 1.2. */
    GROUP_FEATURES(8, V_1_2),
    /** Meter statistics; Since 1.3. */
    METER(9, V_1_3),
    /** Meter configuration; Since 1.3. */
    METER_CONFIG(10, V_1_3),
    /** Meter features; Since 1.3. */
    METER_FEATURES(11, V_1_3),
    /** Table features; Since 1.3. */
    TABLE_FEATURES(12, V_1_3),
    /** Port description; Since 1.3. */
    PORT_DESC(13, V_1_3),
    /** Experimenter extension; Since 1.0.
     * (Called "VENDOR" in 1.0).
     */
    EXPERIMENTER(0xffff, V_1_0),
    ;

    private final int code;
    private final ProtocolVersion since;

    MultipartType(int code, ProtocolVersion since) {
        this.code = code;
        this.since = since;
    }

    @Override
    public int getCode(ProtocolVersion pv) {
        verMinSince(pv, since, name());
        return code;
    }

    /** Decodes the multipart message type code and returns the corresponding
     * constant. If the code is not recognized, an exception is thrown.
     *
     * @param code the encoded multipart message type
     * @param pv the protocol version
     * @return the multipart type
     * @throws DecodeException if the code is not recognized
     * @throws VersionMismatchException if the multipart type is not
     *             supported in the given version
     */
    public static MultipartType decode(int code, ProtocolVersion pv)
            throws DecodeException {
        MultipartType type = null;
        for (MultipartType t : values())
            if (t.code == code) {
                type = t;
                break;
            }
        if (type == null)
            throw new DecodeException("MultipartType: unknown code: " + code);

        // version constraints
        verMinSince(pv, type.since, type.name());
        return type;
    }
}
