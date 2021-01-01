/*
 * Copyright (c) 2014 Pacnet and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.applications.lldpspeaker;

import static org.opendaylight.openflowplugin.applications.topology.lldp.utils.LLDPDiscoveryUtils.getValueForLLDPPacketIntegrityEnsuring;

import com.google.common.base.Strings;
import java.math.BigInteger;
import java.util.regex.Pattern;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.openflowplugin.libraries.liblldp.EtherTypes;
import org.opendaylight.openflowplugin.libraries.liblldp.Ethernet;
import org.opendaylight.openflowplugin.libraries.liblldp.HexEncode;
import org.opendaylight.openflowplugin.libraries.liblldp.LLDP;
import org.opendaylight.openflowplugin.libraries.liblldp.LLDPTLV;
import org.opendaylight.openflowplugin.libraries.liblldp.PacketException;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev130715.MacAddress;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeConnectorId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;
import org.opendaylight.yangtools.yang.common.Uint32;

/**
 * Utility class for dealing with LLDP packets.
 */
public final class LLDPUtil {
    private static final Pattern COLONIZE_REGEX = Pattern.compile("(?<=..)(..)");
    private static final String OF_URI_PREFIX = "openflow:";

    private LLDPUtil() {
        // Hidden on purpose
    }

    static byte @NonNull [] buildLldpFrame(final NodeId nodeId, final NodeConnectorId nodeConnectorId,
            final MacAddress src, final Uint32 outPortNo, final MacAddress destinationAddress) throws PacketException {
        // Create discovery pkt
        LLDP discoveryPkt = new LLDP();

        // Create LLDP ChassisID TLV
        BigInteger dataPathId = dataPathIdFromNodeId(nodeId);
        byte[] cidValue = LLDPTLV.createChassisIDTLVValue(colonize(bigIntegerToPaddedHex(dataPathId)));
        LLDPTLV chassisIdTlv = new LLDPTLV();
        chassisIdTlv.setType(LLDPTLV.TLVType.ChassisID.getValue());
        chassisIdTlv.setType(LLDPTLV.TLVType.ChassisID.getValue()).setLength((short) cidValue.length)
                .setValue(cidValue);
        discoveryPkt.setChassisId(chassisIdTlv);

        // Create LLDP PortID TL
        String hexString = Long.toHexString(outPortNo.toJava());
        byte[] pidValue = LLDPTLV.createPortIDTLVValue(hexString);
        LLDPTLV portIdTlv = new LLDPTLV();
        portIdTlv.setType(LLDPTLV.TLVType.PortID.getValue()).setLength((short) pidValue.length).setValue(pidValue);
        portIdTlv.setType(LLDPTLV.TLVType.PortID.getValue());
        discoveryPkt.setPortId(portIdTlv);

        // Create LLDP TTL TLV
        byte[] ttl = new byte[]{(byte) 0x13, (byte) 0x37};
        LLDPTLV ttlTlv = new LLDPTLV();
        ttlTlv.setType(LLDPTLV.TLVType.TTL.getValue()).setLength((short) ttl.length).setValue(ttl);
        discoveryPkt.setTtl(ttlTlv);

        // Create LLDP SystemName TLV
        byte[] snValue = LLDPTLV.createSystemNameTLVValue(nodeId.getValue());
        LLDPTLV systemNameTlv = new LLDPTLV();
        systemNameTlv.setType(LLDPTLV.TLVType.SystemName.getValue());
        systemNameTlv.setType(LLDPTLV.TLVType.SystemName.getValue()).setLength((short) snValue.length)
                .setValue(snValue);
        discoveryPkt.setSystemNameId(systemNameTlv);

        // Create LLDP Custom TLV
        byte[] customValue = LLDPTLV.createCustomTLVValue(nodeConnectorId.getValue());
        LLDPTLV customTlv = new LLDPTLV();
        customTlv.setType(LLDPTLV.TLVType.Custom.getValue()).setLength((short) customValue.length)
                .setValue(customValue);
        discoveryPkt.addCustomTLV(customTlv);

        //Create LLDP CustomSec TLV
        byte[] pureValue = getValueForLLDPPacketIntegrityEnsuring(nodeConnectorId);
        byte[] customSecValue = LLDPTLV.createSecSubTypeCustomTLVValue(pureValue);
        LLDPTLV customSecTlv = new LLDPTLV();
        customSecTlv.setType(LLDPTLV.TLVType.Custom.getValue()).setLength((short) customSecValue.length)
            .setValue(customSecValue);
        discoveryPkt.addCustomTLV(customSecTlv);

        // Create ethernet pkt
        byte[] sourceMac = HexEncode.bytesFromHexString(src.getValue());
        Ethernet ethPkt = new Ethernet();
        ethPkt.setSourceMACAddress(sourceMac).setEtherType(EtherTypes.LLDP.shortValue()).setPayload(discoveryPkt);
        if (destinationAddress == null) {
            ethPkt.setDestinationMACAddress(LLDP.LLDP_MULTICAST_MAC);
        } else {
            ethPkt.setDestinationMACAddress(HexEncode.bytesFromHexString(destinationAddress.getValue()));
        }

        return ethPkt.serialize();
    }

    static byte @NonNull[] buildLldpFrame(final NodeId nodeId, final NodeConnectorId nodeConnectorId,
            final MacAddress srcMacAddress, final Uint32 outputPortNo) throws PacketException {
        return buildLldpFrame(nodeId, nodeConnectorId, srcMacAddress, outputPortNo, null);
    }

    private static String colonize(final String orig) {
        return COLONIZE_REGEX.matcher(orig).replaceAll(":$1");
    }

    private static BigInteger dataPathIdFromNodeId(final NodeId nodeId) {
        String dpids = nodeId.getValue().replace(OF_URI_PREFIX, "");
        return new BigInteger(dpids);
    }

    private static String bigIntegerToPaddedHex(final BigInteger dataPathId) {
        return Strings.padStart(dataPathId.toString(16), 16, '0');
    }
}
