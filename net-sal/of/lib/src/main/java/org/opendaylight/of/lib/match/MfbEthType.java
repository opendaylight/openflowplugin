/*
 * (c) Copyright 2012 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.of.lib.match;

import org.opendaylight.of.lib.ProtocolVersion;
import org.opendaylight.util.net.EthernetType;

/**
 * OXM Basic match field for {@code ETH_TYPE}.
 *
 * @author Simon Hunt
 */
public class MfbEthType extends MFieldBasic {
    EthernetType ethType;

    /**
     * Constructor invoked by FieldFactory.
     *
     * @param pv the protocol version
     * @param header the match field header
     */
    MfbEthType(ProtocolVersion pv, Header header) {
        super(pv, header);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(super.toString());
        int len = sb.length();
        sb.replace(len-1, len, ",type=").append(ethType).append("}");
        return sb.toString();
    }

    /** Returns the Ethernet type.
     *
     * @return the Ethernet type
     */
    public EthernetType getEthernetType() {
        return ethType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        MfbEthType that = (MfbEthType) o;
        return header.equals(that.header) && ethType.equals(that.ethType);
    }

    @Override
    public int hashCode() {
        int result = header.hashCode();
        result = 31 * result + ethType.hashCode();
        return result;
    }
}
