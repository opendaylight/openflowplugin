/*
 * (c) Copyright 2012,2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.of.lib.match;

import org.opendaylight.of.lib.ProtocolVersion;
import org.opendaylight.util.net.MacAddress;

/**
 * Abstract OXM Basic match field superclass for MAC address payloads.
 *
 * @author Simon Hunt
 */
public abstract class MFieldBasicMac extends MFieldBasic {
    MacAddress mac;
    MacAddress mask; // may be null

    /**
     * Constructor invoked by FieldFactory.
     *
     * @param pv the protocol version
     * @param header the match field header
     */
    MFieldBasicMac(ProtocolVersion pv, Header header) {
        super(pv, header);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(super.toString());
        int len = sb.length();
        sb.replace(len-1, len, ",mac=").append(mac);
        if (mask != null)
            sb.append(",mask=").append(mask);
        sb.append("}");
        return sb.toString();
    }

    /** Returns the MAC address.
     *
     * @return the MAC address
     */
    public MacAddress getMacAddress() {
        return mac;
    }

    /** Returns the MAC address mask.
     *
     * @return the mask
     */
    public MacAddress getMask() {
        return mask;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        MFieldBasicMac that = (MFieldBasicMac) o;
        return header.equals(that.header) && mac.equals(that.mac) &&
                !(mask != null ? !mask.equals(that.mask) : that.mask != null);
    }

    @Override
    public int hashCode() {
        int result = header.hashCode();
        result = 31 * result + mac.hashCode();
        result = 31 * result + (mask != null ? mask.hashCode() : 0);
        return result;
    }
}
