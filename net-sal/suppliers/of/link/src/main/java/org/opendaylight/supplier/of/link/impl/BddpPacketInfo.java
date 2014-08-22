/*
 * (c) Copyright 2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.supplier.of.link.impl;

import org.opendaylight.of.lib.dt.DataPathId;
import org.opendaylight.of.lib.msg.OfmPacketIn;
import org.opendaylight.util.net.BigPortNumber;
import org.opendaylight.util.packet.*;

/**
 * Provides methods for parsing data from BDDP packets received by the
 * controller from the data plane.
 * 
 * @author Shaun Wackerly
 * @author Ryan Tidwell
 */
public class BddpPacketInfo {

    // Port on the source DPID from which this packet was sent
    private final BigPortNumber sourcePort;

    // Port on the destination DPID where this packet was received
    private final BigPortNumber destPort;

    // Source DPID that sent this packet
    private final DataPathId sourceDpid;

    // Destination DPID that received this packet
    private final DataPathId destDpid;

    // ID of the controller that originated this packet
    private final Long controllerId;

    // Indicates whether the packet was received on link-local MAC
    private final boolean isLinkLocal;

    /**
     * Parses BDDP information from the given BDDP packet received by the
     * controller.
     * 
     * @param dpid the datapath ID which received the packet
     * @param ofm the received openflow message
     * @param pkt the received packet
     * @throws IllegalArgumentException if the given packet is not a valid
     *         BDDP packet initiated by a controller
     */
    public BddpPacketInfo(DataPathId dpid, OfmPacketIn ofm, Packet pkt) {
        Bddp bddp = (Bddp) pkt.innermost(ProtocolId.BDDP);
        if (bddp == null)
            throw new IllegalArgumentException("Packet does not contain BDDP");

        Ethernet eth = (Ethernet) pkt.innermost(ProtocolId.ETHERNET);
        if (eth == null)
            throw new IllegalArgumentException("Packet does not contain Ethernet");
        isLinkLocal = eth.dstAddr().isLinkLocal();

        // Store the source port
        LldpTlv srcPortId = bddp.portId();
        if (null == srcPortId)
            throw new IllegalArgumentException("Packet has no port ID");
        sourcePort = BigPortNumber.valueOf(srcPortId.name());

        // Store the datapath ID that sent this packet
        sourceDpid = parseSourceDpid(bddp);
        if (null == sourceDpid)
            throw new IllegalArgumentException("Source DPID not found");

        // Store the controller ID that sent this packet
        LldpTlv optionalTlv = bddp.options().get(LldpTlv.Type.SYS_DESC);
        if (optionalTlv == null)
            throw new IllegalArgumentException("Packet has no source controller ID");
        controllerId = Long.parseLong(optionalTlv.name());

        // Store the datapath ID and port that received this packet
        destPort = ofm.getInPort();
        destDpid = dpid;
    }

    /**
     * Parse the source datapath ID from the given BDDP packet.
     * 
     * @param bddp BDDP packet
     * @return the source datapath ID, or null if not found
     */
    private DataPathId parseSourceDpid(Bddp bddp) {
        // Verify if dpidTlv is present in the private options.
        for (LldpTlv tlv : bddp.privateOptions()) {
            DataPathId dpid = BddpPacketBuilder.getDpidFromTlv(tlv);
            if (dpid != null)
                return dpid;
        }

        return null;
    }

    /**
     * Get the port from which this packet was sent.
     * 
     * @return the source port
     */
    public BigPortNumber getSourcePort() {
        return sourcePort;
    }

    /**
     * Get the port where this packet was received.
     * 
     * @return the destination port
     */
    public BigPortNumber getDestinationPort() {
        return destPort;
    }

    /**
     * Get the source datapath ID that sent this packet.
     * 
     * @return the source datapath ID
     */
    public DataPathId getSourceDpid() {
        return sourceDpid;
    }

    /**
     * Get the destination DPID that received this packet.
     * 
     * @return the destination datapath ID
     */
    public DataPathId getDestinationDpid() {
        return destDpid;
    }

    /**
     * Get the ID of the controller that originated this packet.
     * 
     * @return the controller ID
     */
    public Long getControllerId() {
        return controllerId;
    }

    /**
     * Test whether the packet was received on a link-local address
     * 
     * @return true if received on link-local address, false otherwise
     */
    public boolean isLinkLocal() {
        return isLinkLocal;
    }

    @Override
    public String toString() {
        return "BddpPacketInfo{" +
                "sourceDpid=" + sourceDpid +
                ", sourcePort=" + sourcePort +
                ", destDpid=" + destDpid +
                ", destPort=" + destPort +
                ", controllerId=" + controllerId +
                ", isLinkLocal=" + isLinkLocal +
                '}';
    }

}
