/*
 * (c) Copyright 2012-2014 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.of.lib.match;


import org.opendaylight.of.lib.ProtocolVersion;
import org.opendaylight.of.lib.dt.VId;
import org.opendaylight.util.net.VlanId;

/**
 * OXM Basic match field for {@code VLAN_VID}.
 *
 * @author Simon Hunt
 */
public class MfbVlanVid extends MFieldBasic {
    VId vid;

    /* IMPLEMENTATION NOTE:
     * Internal representation uses VId, because we need that extra bit
     * for the encoding/decoding (12 bits +1 for "present" - see the spec).
     * But we expose values as org.opendaylight.util.net.VlanId.
     */

    /**
     * Constructor invoked by FieldFactory.
     *
     * @param pv the protocol version
     * @param header the match field header
     */
    MfbVlanVid(ProtocolVersion pv, Header header) {
        super(pv, header);
    }

    /**
     * Returns the VLAN ID. Note that this may be one of the special values
     * {@link VlanId#NONE} or {@link VlanId#PRESENT}.
     *
     * @return the VLAN ID
     */
    public VlanId getVlanId() {
        return MatchFactory.equivVlanId(vid);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(super.toString());
        int len = sb.length();
        sb.replace(len-1, len, ",vlan-id=").append(vid).append("}");
        return sb.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        MfbVlanVid that = (MfbVlanVid) o;
        return  header.equals(that.header) && vid.equals(that.vid);
    }

    @Override
    public int hashCode() {
        int result = vid.hashCode();
        result = 31 * result + header.hashCode();
        return result;
    }
}
