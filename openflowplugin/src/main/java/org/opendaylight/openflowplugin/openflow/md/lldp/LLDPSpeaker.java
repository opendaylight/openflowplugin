/**
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.openflow.md.lldp;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang3.StringUtils;
import org.opendaylight.controller.sal.packet.Ethernet;
import org.opendaylight.controller.sal.packet.LLDP;
import org.opendaylight.controller.sal.packet.LLDPTLV;
import org.opendaylight.controller.sal.packet.PacketException;
import org.opendaylight.controller.sal.utils.EtherTypes;
import org.opendaylight.controller.sal.utils.HexEncode;
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
	
	private  final Map<InstanceIdentifier<NodeConnector>,TransmitPacketInput> nodeConnectorMap = new ConcurrentHashMap<InstanceIdentifier<NodeConnector>,TransmitPacketInput>();
	private  final Map<InstanceIdentifier<Node>,ModelDrivenSwitch> nodeMap = new ConcurrentHashMap<InstanceIdentifier<Node>,ModelDrivenSwitch>();
	private static final LLDPSpeaker instance = new LLDPSpeaker();
	private Timer timer = new Timer();
	private static final int DELAY = 0;
	private static final int PERIOD = 1000*5;
	
	private LLDPSpeaker() {
	    timer.schedule(new LLDPSpeakerTask(), DELAY, PERIOD);
	}
	
	public static LLDPSpeaker getInstance() {
	    return instance;
	}
	
	public  void addModelDrivenSwitch(InstanceIdentifier<Node> nodeInstanceId, ModelDrivenSwitch sw) {
		nodeMap.put(nodeInstanceId,sw);		
	}
	
	public void removeModelDrivenSwitch(InstanceIdentifier<Node> nodeInstanceId) {
	    nodeMap.remove(nodeInstanceId);
	    for (InstanceIdentifier<NodeConnector> nodeConnectorInstanceId : nodeConnectorMap.keySet()) {
	        if(nodeInstanceId.equals(nodeConnectorInstanceId.firstIdentifierOf(Node.class))) {
	            nodeConnectorMap.remove(nodeConnectorInstanceId);
	        }
	    }
	}

	public  void addNodeConnector(InstanceIdentifier<NodeConnector> nodeConnectorInstanceId, NodeConnector nodeConnector) {
    	InstanceIdentifier<Node> nodeInstanceId = nodeConnectorInstanceId.firstIdentifierOf(Node.class);
    	NodeKey nodeKey = InstanceIdentifier.keyOf(nodeInstanceId);
    	NodeId nodeId = nodeKey.getId();
    	NodeConnectorId nodeConnectorId = nodeConnector.getId();
    	FlowCapableNodeConnector flowConnector = nodeConnector.<FlowCapableNodeConnector>getAugmentation(FlowCapableNodeConnector.class);
    	TransmitPacketInputBuilder tpib = new TransmitPacketInputBuilder();
    	tpib.setEgress(new NodeConnectorRef(nodeConnectorInstanceId));
    	tpib.setNode(new NodeRef(nodeInstanceId));
    	tpib.setPayload(lldpDataFrom(nodeInstanceId,nodeConnectorInstanceId,flowConnector.getHardwareAddress()));
    	nodeConnectorMap.put(nodeConnectorInstanceId, tpib.build());
        ModelDrivenSwitch md = nodeMap.get(nodeInstanceId);
        md.transmitPacket(nodeConnectorMap.get(nodeConnectorInstanceId));
	}

	public  void removeNodeConnector(
			InstanceIdentifier<NodeConnector> nodeConnectorInstanceId,
			NodeConnector nodeConnector) {
		nodeConnectorMap.remove(nodeConnectorInstanceId);		
	}
	
	private  byte[] lldpDataFrom(InstanceIdentifier<Node> nodeInstanceId,InstanceIdentifier<NodeConnector> nodeConnectorInstanceId,MacAddress src) {
		
	    NodeId nodeId = InstanceIdentifier.keyOf(nodeInstanceId).getId();
	    NodeConnectorId nodeConnectorId = InstanceIdentifier.keyOf(nodeConnectorInstanceId).getId();
		// Create LLDP TTL TLV
        byte[] ttl = new byte[] { (byte) 0, (byte) 120 };
        LLDPTLV ttlTlv = new LLDPTLV();
        ttlTlv.setType(LLDPTLV.TLVType.TTL.getValue()).setLength((short) ttl.length).setValue(ttl);
		
        // Create LLDP ChassisID TLV
        byte[] cidValue = LLDPTLV.createChassisIDTLVValue(colonize(StringUtils.leftPad(Long.toHexString(InventoryDataServiceUtil.dataPathIdFromNodeId(nodeId)),16,"0")));
        LLDPTLV chassisIdTlv = new LLDPTLV();
        chassisIdTlv.setType(LLDPTLV.TLVType.ChassisID.getValue());
        chassisIdTlv.setType(LLDPTLV.TLVType.ChassisID.getValue()).setLength((short) cidValue.length)
                .setValue(cidValue);

        // Create LLDP SystemName TLV
        byte[] snValue = LLDPTLV.createSystemNameTLVValue(nodeId.getValue());
        LLDPTLV systemNameTlv = new LLDPTLV();
        systemNameTlv.setType(LLDPTLV.TLVType.SystemName.getValue());
        systemNameTlv.setType(LLDPTLV.TLVType.SystemName.getValue()).setLength((short) snValue.length)
                .setValue(snValue);

        // Create LLDP PortID TL
        Long portNo = InventoryDataServiceUtil.portNumberfromNodeConnectorId(nodeConnectorId);
        String hexString = Long.toHexString(portNo);
        byte[] pidValue = LLDPTLV.createPortIDTLVValue(hexString);
        LLDPTLV portIdTlv = new LLDPTLV();
        portIdTlv.setType(LLDPTLV.TLVType.PortID.getValue()).setLength((short) pidValue.length).setValue(pidValue);
        portIdTlv.setType(LLDPTLV.TLVType.PortID.getValue());

        // Create LLDP Custom TLV
        byte[] customValue = LLDPTLV.createCustomTLVValue(nodeConnectorId.getValue());
        LLDPTLV customTlv = new LLDPTLV();
        customTlv.setType(LLDPTLV.TLVType.Custom.getValue()).setLength((short) customValue.length)
                .setValue(customValue);

        // Create LLDP Custom Option list
        List<LLDPTLV> customList = new ArrayList<LLDPTLV>();
        customList.add(customTlv);

        // Create discovery pkt
        LLDP discoveryPkt = new LLDP();
        discoveryPkt.setChassisId(chassisIdTlv).setPortId(portIdTlv).setTtl(ttlTlv).setSystemNameId(systemNameTlv)
                .setOptionalTLVList(customList);

        // Create ethernet pkt
        byte[] sourceMac = HexEncode.bytesFromHexString(src.getValue());
        Ethernet ethPkt = new Ethernet();
        ethPkt.setSourceMACAddress(sourceMac).setDestinationMACAddress(LLDP.LLDPMulticastMac)
                .setEtherType(EtherTypes.LLDP.shortValue()).setPayload(discoveryPkt);

        try {
            byte[] data = ethPkt.serialize();
            return data;
        } catch (PacketException e) {
            LOG.error("Error creating LLDP packet",e);
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
