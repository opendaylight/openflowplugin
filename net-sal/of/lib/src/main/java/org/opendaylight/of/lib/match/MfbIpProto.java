/*
 * (c) Copyright 2012 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.of.lib.match;

import org.opendaylight.of.lib.ProtocolVersion;
import org.opendaylight.util.net.IpProtocol;

/**
 * OXM Basic match field for {@code IP_PROTO}.
 *
 * @author Simon Hunt
 */
public class MfbIpProto extends MFieldBasic {
    IpProtocol ipp;

    /**
     * Constructor invoked by FieldFactory.
     *
     * @param pv the protocol version
     * @param header the match field header
     */
    MfbIpProto(ProtocolVersion pv, Header header) {
        super(pv, header);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(super.toString());
        int len = sb.length();
        sb.replace(len-1, len, ",ipp=").append(ipp).append("}");
        return sb.toString();
    }

    /** Returns the IP Protocol.
     *
     * @return the protocol
     */
    public IpProtocol getIpProtocol() {
        return ipp;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        MfbIpProto that = (MfbIpProto) o;
        return header.equals(that.header) && ipp.equals(that.ipp);
    }

    @Override
    public int hashCode() {
        int result = header.hashCode();
        result = 31 * result + ipp.hashCode();
        return result;
    }
}
