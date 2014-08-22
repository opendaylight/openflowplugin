/*
 * (c) Copyright 2012,2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.of.lib.match;

import org.opendaylight.of.lib.ProtocolVersion;
import org.opendaylight.of.lib.msg.Port;
import org.opendaylight.util.net.PortNumber;

/**
 * Abstract OXM Basic match field superclass for {@code TCP_SRC/DST} etc.
 *
 * @author Simon Hunt
 */
public abstract class MFieldBasicPort extends MFieldBasic {
    PortNumber port;

    /**
     * Constructor invoked by FieldFactory.
     *
     * @param pv the protocol version
     * @param header the match field header
     */
    MFieldBasicPort(ProtocolVersion pv, Header header) {
        super(pv, header);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(super.toString());
        int len = sb.length();
        sb.replace(len-1, len, ",port=").append(Port.portNumberToString(port))
                .append("}");
        return sb.toString();
    }

    /** Returns the port number.
     *
     * @return the port number
     */
    public PortNumber getPort() {
        return port;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        MFieldBasicPort that = (MFieldBasicPort) o;
        return header.equals(that.header) && port.equals(that.port);
    }

    @Override
    public int hashCode() {
        int result = header.hashCode();
        result = 31 * result + port.hashCode();
        return result;
    }
}
