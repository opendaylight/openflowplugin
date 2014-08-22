/*
 * (c) Copyright 2012,2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.of.lib.msg;

import org.opendaylight.of.lib.ProtocolVersion;

/**
 * Represents a {@link MeterBand} of type
 * {@link MeterBandType#DSCP_REMARK DSCP_REMARK}.
 *
 * @author Simon Hunt
 */
public class MeterBandDscpRemark extends MeterBand {

    /** Number of precedence level to subtract. */
    int precLevel;

    /**
     * Constructor invoked by MeterFactory.
     *
     * @param pv the protocol version
     * @param header the meter band header
     */
    MeterBandDscpRemark(ProtocolVersion pv, Header header) {
        super(pv, header);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(super.toString());
        int len = sb.length();
        sb.replace(len-1, len, ",pLev=");
        sb.append(precLevel).append("}");
        return sb.toString();
    }


    /** Returns the number of precedence levels to subtract.
     *
     * @return the number of precedence levels
     */
    public int getPrecLevel() {
        return precLevel;
    }
}
