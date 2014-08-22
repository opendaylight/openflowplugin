/*
 * (c) Copyright 2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.of.lib.msg;

import org.opendaylight.of.lib.dt.BufferId;
import org.opendaylight.util.packet.*;

import java.nio.BufferUnderflowException;

import static org.opendaylight.of.lib.dt.BufferId.NO_BUFFER;

/**
 * Provides utilities for dealing with network packet data.
 *
 * @author Simon Hunt
 */
class PacketUtils {
    private static final String NO_DATA = "[no packet data]";
    private static final String DECODE_FAILED = "[cannot decode packet]";
    private static final String NOT_ETHERNET = "(not Ethernet)";
    private static final String OPEN = "[";
    private static final String CLOSE = "]";
    private static final String COMMA = ", ";
    private static final String DST = "dst=";
    private static final String SRC = "src=";

    /**
     * Creates a string representation of the given network packet data,
     * listing the protocol layers and (assuming it is an Ethernet packet)
     * the destination and source MAC addresses.
     * <p>
     * Buffer ID is provided as a hint to the code when presented with
     * partial packet data.
     *
     * @param data the network packet data
     * @param bufferId the buffer ID for the packet
     * @return a summary of the packet
     */
    static String packetSummary(byte[] data, BufferId bufferId) {
        if (data == null)
            return NO_DATA;

        Packet pkt = decodePacket(data, bufferId);
        if (pkt == null)
            return DECODE_FAILED;

        StringBuilder sb = new StringBuilder(OPEN);
        sb.append(pkt.protocolIds()).append(COMMA);
        if (pkt.has(ProtocolId.ETHERNET)) {
            Ethernet eth = pkt.get(ProtocolId.ETHERNET);
            sb.append(DST).append(eth.dstAddr()).append(COMMA)
                    .append(SRC).append(eth.srcAddr());
        } else {
            sb.append(NOT_ETHERNET);
        }
        sb.append(CLOSE);
        return sb.toString();
    }

    private static Packet decodePacket(byte[] data, BufferId bufferId) {
        Packet pkt = null;
        try {
            pkt = Codec.decodeEthernet(data);
        } catch (ProtocolException e) {
            if (!NO_BUFFER.equals(bufferId)
                    && e.packet() != null
                    && e.rootCause() instanceof BufferUnderflowException)
                pkt = e.packet();
        }
        return pkt;
    }
}
