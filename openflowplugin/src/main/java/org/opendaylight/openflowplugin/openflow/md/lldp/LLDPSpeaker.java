/**
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.openflow.md.lldp;

import static org.opendaylight.controller.liblldp.LLDPTLV.CUSTOM_TLV_SUB_TYPE_CUSTOM_SEC;
import static org.opendaylight.md.controller.topology.lldp.utils.LLDPDiscoveryUtils.getValueForLLDPPacketIntegrityEnsuring;

import java.security.NoSuchAlgorithmException;

import java.util.ArrayList;
import java.math.BigInteger;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;
import org.opendaylight.controller.liblldp.EtherTypes;
import org.opendaylight.controller.liblldp.Ethernet;
import org.opendaylight.controller.liblldp.HexEncode;
import org.opendaylight.controller.liblldp.LLDP;
import org.opendaylight.controller.liblldp.LLDPTLV;
import org.opendaylight.controller.liblldp.PacketException;
import org.opendaylight.openflowplugin.api.openflow.md.util.OpenflowVersion;
import org.opendaylight.openflowplugin.openflow.md.ModelDrivenSwitch;
import org.opendaylight.openflowplugin.openflow.md.util.InventoryDataServiceUtil;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev100924.MacAddress;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNodeConnector;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeConnectorId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeConnectorRef;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeRef;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.node.NodeConnector;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.NodeKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.packet.service.rev130709.TransmitPacketInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.packet.service.rev130709.TransmitPacketInputBuilder;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class LLDPSpeaker {
    private static Logger LOG = LoggerFactory.getLogger(LLDPSpeaker.class);

    private final Map<InstanceIdentifier<NodeConnector>, TransmitPacketInput> nodeConnectorMap = new ConcurrentHashMap<InstanceIdentifier<NodeConnector>, TransmitPacketInput>();
    private final Map<InstanceIdentifier<Node>, ModelDrivenSwitch> nodeMap = new ConcurrentHashMap<InstanceIdentifier<Node>, ModelDrivenSwitch>();
    private static final LLDPSpeaker instance = new LLDPSpeaker();
    private Timer timer = new Timer();
    private static final int DELAY = 0;
    private static final int PERIOD = 1000 * 5;

    private LLDPSpeaker() {
        timer.schedule(new LLDPSpeakerTask(), DELAY, PERIOD);
    }

    public static LLDPSpeaker getInstance() {
        return instance;
    }

    public void addModelDrivenSwitch(InstanceIdentifier<Node> nodeInstanceId, ModelDrivenSwitch sw) {
        nodeMap.put(nodeInstanceId, sw);
    }

    public void removeModelDrivenSwitch(InstanceIdentifier<Node> nodeInstanceId) {
        nodeMap.remove(nodeInstanceId);
        for (InstanceIdentifier<NodeConnector> nodeConnectorInstanceId : nodeConnectorMap.keySet()) {
            if (nodeInstanceId.equals(nodeConnectorInstanceId.firstIdentifierOf(Node.class))) {
                nodeConnectorMap.remove(nodeConnectorInstanceId);
            }
        }
    }

    public void addNodeConnector(InstanceIdentifier<NodeConnector> nodeConnectorInstanceId, NodeConnector nodeConnector) {
        InstanceIdentifier<Node> nodeInstanceId = nodeConnectorInstanceId.firstIdentifierOf(Node.class);
        ModelDrivenSwitch md = nodeMap.get(nodeInstanceId);

        NodeKey nodeKey = InstanceIdentifier.keyOf(nodeInstanceId);
        NodeId nodeId = nodeKey.getId();
        NodeConnectorId nodeConnectorId = nodeConnector.getId();
        FlowCapableNodeConnector flowConnector = nodeConnector.<FlowCapableNodeConnector>getAugmentation(FlowCapableNodeConnector.class);
        TransmitPacketInputBuilder tpib = new TransmitPacketInputBuilder();
        tpib.setEgress(new NodeConnectorRef(nodeConnectorInstanceId));
        tpib.setNode(new NodeRef(nodeInstanceId));
        if(nodeInstanceId == null) {
            LOG.warn("addNodeConnector(): nodeInstanceId should not be null nodeConnectorInstanceId {} nodeConnector {}",nodeConnectorInstanceId,nodeConnector);
        } else if (nodeConnectorInstanceId == null) {
            LOG.warn("addNodeConnector(): nodeConnectorInstanceId should not be null nodeConnectorInstanceId {} nodeConnector {}",nodeConnectorInstanceId,nodeConnector);
        } else if (flowConnector == null) {
            LOG.warn("addNodeConnector(): flowConnector should not be null nodeConnectorInstanceId {} nodeConnector {}",nodeConnectorInstanceId,nodeConnector);
        } else if (md == null) {
            LOG.debug("addNodeConnector(): md is null, this usually means your switch disconnected while you had unprocessed NodeConnectorUpdated messages in queue nodeConnectorInstanceId {} nodeConnector {}",nodeConnectorInstanceId,nodeConnector);
        } else if(md.getSessionContext() == null) {
            LOG.warn("addNodeConnector(): md.getSessionContext() should not be null nodeConnectorInstanceId {} nodeConnector {}",nodeConnectorInstanceId,nodeConnector);
        } else if (md.getSessionContext().getPrimaryConductor() == null) {
            LOG.warn("addNodeConnector(): md.getSessionContext().getPrimaryConductor() should not be null nodeConnectorInstanceId {} nodeConnector {}",nodeConnectorInstanceId,nodeConnector);
        } else {
            tpib.setPayload(lldpDataFrom(nodeInstanceId,nodeConnectorInstanceId,flowConnector.getHardwareAddress(),
                    md.getSessionContext().getPrimaryConductor().getVersion()));
            nodeConnectorMap.put(nodeConnectorInstanceId, tpib.build());

            md.transmitPacket(nodeConnectorMap.get(nodeConnectorInstanceId));
        }
    }

    public void removeNodeConnector(
            InstanceIdentifier<NodeConnector> nodeConnectorInstanceId,
            NodeConnector nodeConnector) {
        nodeConnectorMap.remove(nodeConnectorInstanceId);
    }

    private byte[] lldpDataFrom(InstanceIdentifier<Node> nodeInstanceId, InstanceIdentifier<NodeConnector> nodeConnectorInstanceId, MacAddress src,
                                Short version) {

        NodeId nodeId = InstanceIdentifier.keyOf(nodeInstanceId).getId();
        NodeConnectorId nodeConnectorId = InstanceIdentifier.keyOf(nodeConnectorInstanceId).getId();
        // Create discovery pkt
        LLDP discoveryPkt = new LLDP();

        // Create LLDP TTL TLV
        byte[] ttl = new byte[]{(byte) 0, (byte) 120};
        LLDPTLV ttlTlv = new LLDPTLV();
        ttlTlv.setType(LLDPTLV.TLVType.TTL.getValue()).setLength((short) ttl.length).setValue(ttl);
        discoveryPkt.setTtl(ttlTlv);

        // Create LLDP ChassisID TLV
        BigInteger dataPathId = InventoryDataServiceUtil.dataPathIdFromNodeId(nodeId);
        byte[] cidValue = LLDPTLV.createChassisIDTLVValue(
                colonize(InventoryDataServiceUtil.bigIntegerToPaddedHex(dataPathId)));
        LLDPTLV chassisIdTlv = new LLDPTLV();
        chassisIdTlv.setType(LLDPTLV.TLVType.ChassisID.getValue());
        chassisIdTlv.setType(LLDPTLV.TLVType.ChassisID.getValue()).setLength((short) cidValue.length)
                .setValue(cidValue);
        discoveryPkt.setChassisId(chassisIdTlv);

        // Create LLDP SystemName TLV
        byte[] snValue = LLDPTLV.createSystemNameTLVValue(nodeId.getValue());
        LLDPTLV systemNameTlv = new LLDPTLV();
        systemNameTlv.setType(LLDPTLV.TLVType.SystemName.getValue());
        systemNameTlv.setType(LLDPTLV.TLVType.SystemName.getValue()).setLength((short) snValue.length)
                .setValue(snValue);
        discoveryPkt.setSystemNameId(systemNameTlv);

        // Create LLDP PortID TL
        Long portNo = InventoryDataServiceUtil.portNumberfromNodeConnectorId(OpenflowVersion.get(version), nodeConnectorId);

        String hexString = Long.toHexString(portNo);
        byte[] pidValue = LLDPTLV.createPortIDTLVValue(hexString);
        LLDPTLV portIdTlv = new LLDPTLV();
        portIdTlv.setType(LLDPTLV.TLVType.PortID.getValue()).setLength((short) pidValue.length).setValue(pidValue);
        portIdTlv.setType(LLDPTLV.TLVType.PortID.getValue());
        discoveryPkt.setPortId(portIdTlv);

        // Create LLDP Custom TLV
        byte[] customValue = LLDPTLV.createCustomTLVValue(nodeConnectorId.getValue());
        LLDPTLV customTlv = new LLDPTLV();
        customTlv.setType(LLDPTLV.TLVType.Custom.getValue()).setLength((short) customValue.length)
                .setValue(customValue);
        discoveryPkt.addCustomTLV(customTlv);

        //Create LLDP CustomSec TLV
        byte[] pureValue = new byte[1];
        try {
            pureValue = getValueForLLDPPacketIntegrityEnsuring(nodeConnectorId);
        } catch (NoSuchAlgorithmException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
        byte[] customSecValue = LLDPTLV.createCustomTLVValue(CUSTOM_TLV_SUB_TYPE_CUSTOM_SEC, pureValue);
        LLDPTLV customSecTlv = new LLDPTLV();
        customSecTlv.setType(LLDPTLV.TLVType.Custom.getValue()).setLength((short)customSecValue.length)
                .setValue(customSecValue);
        discoveryPkt.addCustomTLV(customSecTlv);

        // Create ethernet pkt
        byte[] sourceMac = HexEncode.bytesFromHexString(src.getValue());
        Ethernet ethPkt = new Ethernet();
        ethPkt.setSourceMACAddress(sourceMac).setDestinationMACAddress(LLDP.LLDPMulticastMac)
                .setEtherType(EtherTypes.LLDP.shortValue()).setPayload(discoveryPkt);

        try {
            byte[] data = ethPkt.serialize();
            return data;
        } catch (PacketException e) {
            LOG.error("Error creating LLDP packet", e);
        }
        return null;
    }

    private class LLDPSpeakerTask extends TimerTask {

        @Override
        public void run() {
            for (InstanceIdentifier<NodeConnector> nodeConnectorInstanceId : nodeConnectorMap.keySet()) {
                InstanceIdentifier<Node> nodeInstanceId = nodeConnectorInstanceId.firstIdentifierOf(Node.class);
                ModelDrivenSwitch md = nodeMap.get(nodeInstanceId);
                md.transmitPacket(nodeConnectorMap.get(nodeConnectorInstanceId));
            }

        }

    }

    private String colonize(String orig) {
        return orig.replaceAll("(?<=..)(..)", ":$1");
    }
}
