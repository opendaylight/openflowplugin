/*
 * (c) Copyright 2012 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.of.lib.match;

import org.opendaylight.of.lib.ProtocolVersion;
import org.opendaylight.util.net.ICMPv6Type;

/**
 * OXM Basic match field for {@code ICMPV6_TYPE}.
 *
 * @author Simon Hunt
 */
public class MfbIcmpv6Type extends MFieldBasic {
    ICMPv6Type type;

    /**
     * Constructor invoked by FieldFactory.
     *
     * @param pv the protocol version
     * @param header the match field header
     */
    MfbIcmpv6Type(ProtocolVersion pv, Header header) {
        super(pv, header);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(super.toString());
        int len = sb.length();
        sb.replace(len-1, len, ",type=").append(type).append("}");
        return sb.toString();
    }

    /** Returns the ICMPv6 type.
     *
     * @return the ICMPv6 type
     */
    public ICMPv6Type getICMPv6Type() {
        return type;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        MfbIcmpv6Type that = (MfbIcmpv6Type) o;
        return header.equals(that.header) && type.equals(that.type);
    }

    @Override
    public int hashCode() {
        int result = header.hashCode();
        result = 31 * result + type.hashCode();
        return result;
    }
}
