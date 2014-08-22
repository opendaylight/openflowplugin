/*
 * (c) Copyright 2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.supplier.of.link.impl;

import org.opendaylight.of.lib.ProtocolVersion;
import org.opendaylight.of.lib.dt.BufferId;
import org.opendaylight.of.lib.dt.DataPathId;
import org.opendaylight.of.lib.dt.DataPathInfo;
import org.opendaylight.of.lib.instr.ActOutput;
import org.opendaylight.of.lib.instr.ActionFactory;
import org.opendaylight.of.lib.instr.ActionType;
import org.opendaylight.of.lib.msg.MessageFactory;
import org.opendaylight.of.lib.msg.MessageType;
import org.opendaylight.of.lib.msg.OfmMutablePacketOut;
import org.opendaylight.of.lib.msg.Port;
import org.opendaylight.util.ByteUtils;
import org.opendaylight.util.net.BigPortNumber;
import org.opendaylight.util.net.EthernetType;
import org.opendaylight.util.net.MacAddress;
import org.opendaylight.util.packet.*;
import org.opendaylight.util.packet.LldpTlv.PortIdSubType;

import static org.opendaylight.of.lib.dt.DataPathId.valueOf;

/**
 * Builds BDDP packet payload and related OpenFlow messages.
 *
 * @author Shaun Wackerly
 */
public class BddpPacketBuilder {

    // FIXME Nearly all of the 'static final' values specified below are
    // magic numbers, meaning that they appear in the code without explanation
    // as to the rationale (guesstimate? theoretical calculation? defined
    // by standard?). The values should either be explained or based on some
    // other common definition which explains them.

    // Prime value for creating controllerTlv.
    private static final int CONTROLLER_TLV_PRIME = 7867;

    // Constants representing first 4 bytes of dpidTlv.
    private static final byte DPID_TLV0 = 0x0;
    private static final byte DPID_TLV1 = 0x26;
    private static final byte DPID_TLV2 = (byte) 0xe1;

    // Constant representing the ttlTlv.
    private static final byte TTL_TLV3 = 0x78;

    private static final byte DPID_TLV_LENGTH = 8;
    private static final byte DPID_TLV_START_INDEX = 4;

    /**
     * Generate a PACKET_OUT message containing the discovery payload tailored
     * to the given DataPathInfo and Port
     *
     * @param dpInfo       DataPathInfo for the target datapath
     * @param portNumber   target port
     * @param portMac      port mac
     * @param destMac      destination mac
     * @param controllerId numerical form of controller id
     */
    public static OfmMutablePacketOut buildPacketOut(DataPathInfo dpInfo,
                                                     BigPortNumber portNumber,
                                                     MacAddress portMac,
                                                     MacAddress destMac, long controllerId) {
        Bddp bddp = buildPayload(dpInfo.dpid(), portNumber, controllerId);
        Packet packet = buildBddpPacket(portMac, destMac, bddp);
        return buildPacketOut(packet, dpInfo.negotiated(), portNumber);
    }

    /**
     * Builds an Ethernet BDDP packet containing the discovery payload.
     *
     * @param portMac port mac
     * @param destMac destination mac
     * @param bddp    BDDP payload
     */
    static Packet buildBddpPacket(MacAddress portMac, MacAddress destMac, Bddp bddp) {
        Ethernet eth = new Ethernet.Builder()
                .dstAddr(destMac).srcAddr(portMac).type(EthernetType.BDDP).build();
        return new Packet(eth, bddp);
    }

    /**
     * Builds the mutable packet-out with the required action for BDDP.
     *
     * @param packet the packet to be sent
     * @param pv     the published protocol version
     * @param bpn    the switch port number
     * @return the mutable packet-out
     */
    private static OfmMutablePacketOut buildPacketOut(Packet packet, ProtocolVersion pv, BigPortNumber bpn) {
        byte[] data = Codec.encode(packet);

        OfmMutablePacketOut po = (OfmMutablePacketOut) MessageFactory.create(pv, MessageType.PACKET_OUT);
        po.bufferId(BufferId.NO_BUFFER);

        // OpenFlow 1.0 spec says that if the input port is not available, we
        // should use Port.NONE. All others use Port.CONTROLLER.
        po.inPort(pv == ProtocolVersion.V_1_0 ? Port.NONE : Port.CONTROLLER);
        po.addAction(ActionFactory.createAction(pv, ActionType.OUTPUT, bpn, ActOutput.CONTROLLER_NO_BUFFER));
        po.data(data);
        return po;
    }

    /**
     * Builds a BDDP packet payload with the required TLVs set.
     *
     * @param dpid         the target datapath
     * @param portNumber   the switch port number
     * @param controllerId numerical form of controller id
     * @return the lldp packet
     */
    static Bddp buildPayload(DataPathId dpid, BigPortNumber portNumber, long controllerId) {
        LldpTlv dpidTlv = makeTlvFromDpid(dpid);

        LldpTlv chassisId = LldpTlv.chassisIdMacAddr(dpid.getMacAddress());
        LldpTlv portId = LldpTlv.portIdName(PortIdSubType.LOCAL, portNumber.toString());
        LldpTlv ttl = LldpTlv.ttl(TTL_TLV3);
        LldpTlv[] ctlrLldpTlv = new LldpTlv[]{new LldpTlv.Builder(LldpTlv.Type.SYS_DESC)
                .name(Long.valueOf(controllerId).toString()).build()};
        LldpTlv[] dpIdLldpTlv = new LldpTlv[]{dpidTlv};

        // Build the BDDP frame with the mandatory tlv's-chassis, port and ttl
        Lldp lldp = new Lldp.Builder().chassisId(chassisId).portId(portId).ttl(ttl)
                // Add the optional controller tlv which uniquely
                // identifies the controller which sent the packet.
                .options(ctlrLldpTlv)
                        // Add the private optional organizational tlv which contains
                        // the given datapathid.
                .privateOptions(dpIdLldpTlv).build();

        return new Bddp(lldp);
    }


    /**
     * Generate a datapath ID Tlv used for creating a BDDP packet.
     *
     * @param dpId the target datapath
     * @return the DpId Tlv.
     */
    private static LldpTlv makeTlvFromDpid(DataPathId dpId) {
        byte[] dpidTLVValue = new byte[]{DPID_TLV0, DPID_TLV1, DPID_TLV2, 0, 0, 0, 0, 0, 0, 0, 0, 0};
        System.arraycopy(dpId.toByteArray(), 0, dpidTLVValue, DPID_TLV_START_INDEX, DPID_TLV_LENGTH);
        return new LldpTlv.PrivateBuilder().bytes(dpidTLVValue).build();
    }

    /**
     * Determines if the given set of bytes represent a DPID value.
     *
     * @param bytes the given set of bytes
     * @return whether or not the bytes represent a DPID value
     */
    private static boolean isDpidTlv(byte[] bytes) {
        return (bytes[0] == DPID_TLV0 && bytes[1] == DPID_TLV1 && bytes[2] == DPID_TLV2 && bytes[3] == 0);
    }

    /**
     * Gets a datapath ID from the given TLV, if applicable.
     *
     * @param tlv the given TLV
     * @return the contained datapath ID, or null if not found
     */
    static DataPathId getDpidFromTlv(LldpTlv tlv) {
        byte[] bytes = tlv.bytes();
        return !isDpidTlv(bytes) ? null : valueOf(ByteUtils.getLong(bytes, DPID_TLV_START_INDEX));
    }

}