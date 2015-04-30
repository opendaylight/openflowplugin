/*
 * Copyright (c) 2014 Pacnet and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.applications.lldpspeaker;

import java.math.BigInteger;
import java.util.Collections;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.opendaylight.controller.liblldp.EtherTypes;
import org.opendaylight.controller.liblldp.Ethernet;
import org.opendaylight.controller.liblldp.HexEncode;
import org.opendaylight.controller.liblldp.LLDP;
import org.opendaylight.controller.liblldp.LLDPTLV;
import org.opendaylight.controller.liblldp.PacketException;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev100924.MacAddress;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeConnectorId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility class for dealing with LLDP packets.
 */
public final class LLDPUtil {
    private static final Logger LOG = LoggerFactory.getLogger(LLDPUtil.class);
    private static final String OF_URI_PREFIX = "openflow:";

    static byte[] buildLldpFrame(NodeId nodeId,
            NodeConnectorId nodeConnectorId, MacAddress src, Long outPortNo,
            MacAddress destinationAddress) {
        // Create LLDP TTL TLV
        byte[] ttl = new byte[] { (byte) 0x13, (byte) 0x37 };
        LLDPTLV ttlTlv = new LLDPTLV();
        ttlTlv.setType(LLDPTLV.TLVType.TTL.getValue())
                .setLength((short) ttl.length).setValue(ttl);

        // Create LLDP ChassisID TLV
        BigInteger dataPathId = dataPathIdFromNodeId(nodeId);
        byte[] cidValue = LLDPTLV
                .createChassisIDTLVValue(colonize(bigIntegerToPaddedHex(dataPathId)));
        LLDPTLV chassisIdTlv = new LLDPTLV();
        chassisIdTlv.setType(LLDPTLV.TLVType.ChassisID.getValue());
        chassisIdTlv.setType(LLDPTLV.TLVType.ChassisID.getValue())
                .setLength((short) cidValue.length).setValue(cidValue);

        // Create LLDP SystemName TLV
        byte[] snValue = LLDPTLV.createSystemNameTLVValue(nodeId.getValue());
        LLDPTLV systemNameTlv = new LLDPTLV();
        systemNameTlv.setType(LLDPTLV.TLVType.SystemName.getValue());
        systemNameTlv.setType(LLDPTLV.TLVType.SystemName.getValue())
                .setLength((short) snValue.length).setValue(snValue);

        // Create LLDP PortID TL
        String hexString = Long.toHexString(outPortNo);
        byte[] pidValue = LLDPTLV.createPortIDTLVValue(hexString);
        LLDPTLV portIdTlv = new LLDPTLV();
        portIdTlv.setType(LLDPTLV.TLVType.PortID.getValue())
                .setLength((short) pidValue.length).setValue(pidValue);
        portIdTlv.setType(LLDPTLV.TLVType.PortID.getValue());

        // Create LLDP Custom TLV
        byte[] customValue = LLDPTLV.createCustomTLVValue(nodeConnectorId
                .getValue());
        LLDPTLV customTlv = new LLDPTLV();
        customTlv.setType(LLDPTLV.TLVType.Custom.getValue())
                .setLength((short) customValue.length).setValue(customValue);

        // Create LLDP Custom Option list
        List<LLDPTLV> customList = Collections.singletonList(customTlv);

        // Create discovery pkt
        LLDP discoveryPkt = new LLDP();
        discoveryPkt.setChassisId(chassisIdTlv).setPortId(portIdTlv)
                .setTtl(ttlTlv).setSystemNameId(systemNameTlv)
                .setOptionalTLVList(customList);

        // Create ethernet pkt
        byte[] sourceMac = HexEncode.bytesFromHexString(src.getValue());
        Ethernet ethPkt = new Ethernet();
        ethPkt.setSourceMACAddress(sourceMac)
                .setEtherType(EtherTypes.LLDP.shortValue())
                .setPayload(discoveryPkt);
        if (destinationAddress == null) {
            ethPkt.setDestinationMACAddress(LLDP.LLDPMulticastMac);
        } else {
            ethPkt.setDestinationMACAddress(HexEncode
                    .bytesFromHexString(destinationAddress.getValue()));
        }

        try {
            return ethPkt.serialize();
        } catch (PacketException e) {
            LOG.warn("Error creating LLDP packet: {}", e.getMessage());
            LOG.debug("Error creating LLDP packet.. ", e);
        }
        return null;
    }

    private static String colonize(String orig) {
        return orig.replaceAll("(?<=..)(..)", ":$1");
    }

    private static BigInteger dataPathIdFromNodeId(NodeId nodeId) {
        String dpids = nodeId.getValue().replace(OF_URI_PREFIX, "");
        return new BigInteger(dpids);
    }

    private static String bigIntegerToPaddedHex(BigInteger dataPathId) {
        return StringUtils.leftPad(dataPathId.toString(16), 16, "0");
    }

    static byte[] buildLldpFrame(NodeId nodeId,
            NodeConnectorId nodeConnectorId, MacAddress srcMacAddress,
            Long outputPortNo) {
        return buildLldpFrame(nodeId, nodeConnectorId, srcMacAddress,
                outputPortNo, null);
    }
}
