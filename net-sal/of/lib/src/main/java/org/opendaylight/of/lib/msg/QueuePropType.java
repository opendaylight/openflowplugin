/*
 * (c) Copyright 2012 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */


package org.opendaylight.of.lib.msg;

import org.opendaylight.of.lib.DecodeException;
import org.opendaylight.of.lib.OfpCodeBasedEnum;
import org.opendaylight.of.lib.ProtocolVersion;
import org.opendaylight.of.lib.VersionMismatchException;
import org.opendaylight.util.ResourceUtils;

import java.util.ResourceBundle;

/**
 * Denotes different queue properties.
 *
 * @author Simon Hunt
 */
public enum QueuePropType implements OfpCodeBasedEnum {
    /** Minimum datarate guaranteed; Since 1.0. */
    MIN_RATE(1, ProtocolVersion.V_1_0),
    /** Maximum datarate; Since 1.2. */
    MAX_RATE(2, ProtocolVersion.V_1_2),
    /** Experimenter defined property; Since 1.2. */
    EXPERIMENTER(0xffff, ProtocolVersion.V_1_2),
    ;

    private final int code;
    private final ProtocolVersion minVer;
    private final boolean deprecated;

    QueuePropType(int code, ProtocolVersion minVer) {
        this.code = code;
        this.minVer = minVer;
        this.deprecated = false;
    }

    QueuePropType(int code, ProtocolVersion minVer, boolean deprecated) {
        this.code = code;
        this.minVer = minVer;
        this.deprecated = deprecated;
    }

    @Override
    public int getCode(ProtocolVersion pv) {
        return code;
    }

    /** Returns the queue property type constant corresponding to the
     * given encoded value.
     *
     * @param code the code
     * @param pv the protocol version
     * @return the corresponding constant
     * @throws DecodeException if the code is unknown
     * @throws VersionMismatchException if the code is not supported
     *          in the given version
     */
    static QueuePropType decode(int code, ProtocolVersion pv)
            throws DecodeException {
        QueuePropType qp = null;
        for (QueuePropType p: values())
            if (p.getCode(pv) == code) {
                qp = p;
                break;
            }
        if (qp == null)
            throw new DecodeException("QueuePropType: unknown code: " + code);

        // version constraints
        if (pv.lt(qp.minVer) || (qp.deprecated && pv.ge(ProtocolVersion.V_1_2)))
            throw new VersionMismatchException(pv + E_BAD_PROP + qp);
        return qp;
    }

    private static final ResourceBundle RES = ResourceUtils.getBundledResource(
            QueuePropType.class, "queuePropType");

    private static final String E_BAD_PROP = RES.getString("e_bad_prop");
}