/*
 * (c) Copyright 2012 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.of.lib.match;

import org.opendaylight.of.lib.ProtocolVersion;
import org.opendaylight.util.ByteUtils;

import java.util.Arrays;

/**
 * A minimal OpenFlow match field descriptor. Used when the OXM Class
 * is neither {@link OxmClass#OPENFLOW_BASIC} nor {@link OxmClass#EXPERIMENTER}.
 *
 * @author Simon Hunt
 */
public class MFieldMinimal extends MatchField {

    /** Payload. */
    byte[] payload;

    /**
     * Constructor invoked by FieldFactory.
     *
     * @param pv the protocol version
     * @param header the match field header
     */
    MFieldMinimal(ProtocolVersion pv, Header header) {
        super(pv, header);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(super.toString());
        int len = sb.length();
        sb.replace(len-1, len, ",data=");
        if (payload != null)
            sb.append(ByteUtils.toHexArrayString(payload));
        else
            sb.append("(none)");
        sb.append("}");
        return sb.toString();
    }

    /** Returns a copy of the payload.
     *
     * @return a copy of the payload
     */
    public byte[] getPayload() {
        return payload == null ? null : payload.clone();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        MFieldMinimal that = (MFieldMinimal) o;
        return header.equals(that.header) && Arrays.equals(payload, that.payload);
    }

    @Override
    public int hashCode() {
        int result = header.hashCode();
        result = 31 * result + (payload != null ? Arrays.hashCode(payload) : 0);
        return result;
    }
}
