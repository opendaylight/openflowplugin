/*
 * (c) Copyright 2012 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.of.lib.match;

import org.opendaylight.of.lib.ProtocolVersion;

import java.util.EnumSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 * OXM Basic match field for {@code IPV6_EXTHDR}.
 *
 * @author Simon Hunt
 */
public class MfbIpv6Exthdr extends MFieldBasic {
    int rawBits;
    int mask;

    /**
     * Constructor invoked by FieldFactory.
     *
     * @param pv the protocol version
     * @param header the match field header
     */
    MfbIpv6Exthdr(ProtocolVersion pv, Header header) {
        super(pv, header);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(super.toString());
        int len = sb.length();
        sb.replace(len-1, len, ",flags=").append(getFlags());
        sb.append("}");
        return sb.toString();
    }

    /**
     * Returns a map of flags indicating the extension header flags that should
     * be matched on. For each map entry, if the value is {@code true}, the
     * flag must be present; if the value is {@code false}, the flag must be
     * absent. Flags not included in the map are considered to be "don't care".
     *
     * @return the flag match definition
     */
    public Map<IPv6ExtHdr, Boolean> getFlags() {
        Set<IPv6ExtHdr> values = IPv6ExtHdr.decodeBitmap(rawBits, version);
        Set<IPv6ExtHdr> masked = (mask == 0)
                ? ALL_FLAGS : IPv6ExtHdr.decodeBitmap(mask, version);
        Map<IPv6ExtHdr, Boolean> map = new TreeMap<IPv6ExtHdr, Boolean>();
        for (IPv6ExtHdr eh: masked)
            map.put(eh, values.contains(eh));
        return map;
    }

    private static final Set<IPv6ExtHdr> ALL_FLAGS =
            EnumSet.allOf(IPv6ExtHdr.class);

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        MfbIpv6Exthdr that = (MfbIpv6Exthdr) o;
        return header.equals(that.header) && mask == that.mask &&
                rawBits == that.rawBits;
    }

    @Override
    public int hashCode() {
        int result = header.hashCode();
        result = 31 * result + rawBits;
        result = 31 * result + mask;
        return result;
    }
}
