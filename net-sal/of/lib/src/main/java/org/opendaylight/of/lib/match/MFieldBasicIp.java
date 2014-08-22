/*
 * (c) Copyright 2012,2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.of.lib.match;

import org.opendaylight.of.lib.ProtocolVersion;
import org.opendaylight.util.net.IpAddress;

/**
 * Abstract OXM Basic match field superclass for IP address payloads.
 *
 * @author Simon Hunt
 */
public abstract class MFieldBasicIp extends MFieldBasic {
    IpAddress ip;
    IpAddress mask; // may be null

    /**
     * Constructor invoked by FieldFactory.
     *
     * @param pv the protocol version
     * @param header the match field header
     */
    MFieldBasicIp(ProtocolVersion pv, Header header) {
        super(pv, header);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(super.toString());
        int len = sb.length();
        sb.replace(len-1, len, ",ip=").append(ip);
        if (mask != null)
            sb.append(",mask=").append(mask);
        sb.append("}");
        return sb.toString();
    }

    /** Returns the IP address.
     *
     * @return the IP address
     */
    public IpAddress getIpAddress() {
        return ip;
    }

    /** Returns the IP address mask.
     *
     * @return the mask
     */
    public IpAddress getMask() {
        return mask;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        MFieldBasicIp that = (MFieldBasicIp) o;
        return header.equals(that.header) && ip.equals(that.ip) &&
                !(mask != null ? !mask.equals(that.mask) : that.mask != null);
    }

    @Override
    public int hashCode() {
        int result = header.hashCode();
        result = 31 * result + ip.hashCode();
        result = 31 * result + (mask != null ? mask.hashCode() : 0);
        return result;
    }
}
