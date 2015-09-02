/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.flowprogrammer.flowconflictdetection;

import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.PortNumber;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.Layer4Match;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.layer._4.match.TcpMatch;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.layer._4.match.UdpMatch;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.layer._4.match.SctpMatch;

import java.lang.Integer;

/**
 * This class converts Layer4Match to simple data type. 
 *
 * <p>
 *
 * @author Yi Yang (yi.y.yang@intel.com)
 *
 * <p>
 * @since 2015-08-25
 */

public class L4Data {
    private Integer srcPort;
    private Integer dstPort;

    public L4Data(Integer srcPort, Integer dstPort) {
        this.srcPort = srcPort;
        this.dstPort = dstPort;
    }

    public static L4Data toL4Data(TcpMatch match) {
        Integer srcPort = Integer.valueOf(-1);
        Integer dstPort = Integer.valueOf(-1);
        PortNumber port = match.getTcpSourcePort();
        if (port != null) {
            srcPort = port.getValue();
        }
        port = match.getTcpDestinationPort();
        if (port != null) {
            dstPort = port.getValue();
        }
        return new L4Data(srcPort, dstPort);
    }

    public static L4Data toL4Data(Layer4Match l4Match) {
        Integer srcPort = Integer.valueOf(-1);
        Integer dstPort = Integer.valueOf(-1);
        PortNumber port = null;
        if (l4Match instanceof TcpMatch) {
            TcpMatch tcpMatch = (TcpMatch)l4Match;
            port = tcpMatch.getTcpSourcePort();
        }
        else if (l4Match instanceof UdpMatch) {
            UdpMatch udpMatch = (UdpMatch)l4Match;
            port = udpMatch.getUdpSourcePort();
        }
        else if (l4Match instanceof SctpMatch) {
            SctpMatch sctpMatch = (SctpMatch)l4Match;
            port = sctpMatch.getSctpSourcePort();
        }
        if (port != null) {
            srcPort = port.getValue();
        }
        if (l4Match instanceof TcpMatch) {
            TcpMatch tcpMatch = (TcpMatch)l4Match;
            port = tcpMatch.getTcpDestinationPort();
        }
        else if (l4Match instanceof UdpMatch) {
            UdpMatch udpMatch = (UdpMatch)l4Match;
            port = udpMatch.getUdpDestinationPort();
        }
        else if (l4Match instanceof SctpMatch) {
            SctpMatch sctpMatch = (SctpMatch)l4Match;
            port = sctpMatch.getSctpDestinationPort();
        }
        if (port != null) {
            dstPort = port.getValue();
        }
        return new L4Data(srcPort, dstPort);
    }

    public boolean isSame(Layer4Match l4Match) {
        L4Data l4Data = null;
        if (l4Match instanceof TcpMatch) {
            l4Data = toL4Data((TcpMatch)l4Match);
        }
        else if (l4Match instanceof UdpMatch) {
            l4Data = toL4Data((UdpMatch)l4Match);
        }
        else if (l4Match instanceof SctpMatch) {
            l4Data = toL4Data((SctpMatch)l4Match);
        }
        if ((l4Data.srcPort != this.srcPort)
           && (l4Data.srcPort != Integer.valueOf(-1))
           && (this.srcPort != Integer.valueOf(-1))) {
            return false;
        }
        if ((l4Data.dstPort != this.dstPort)
           && (l4Data.dstPort != Integer.valueOf(-1))
           && (this.dstPort != Integer.valueOf(-1))) {
            return false;
        }
        return true;
    }
}
