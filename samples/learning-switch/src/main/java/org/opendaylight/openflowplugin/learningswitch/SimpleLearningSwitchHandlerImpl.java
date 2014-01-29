/**
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.learningswitch;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev100924.MacAddress;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.Table;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.TableKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.table.Flow;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.table.FlowBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.table.FlowKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeConnectorId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeConnectorRef;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeRef;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.Nodes;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.node.NodeConnector;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.node.NodeConnectorKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.NodeKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.packet.service.rev130709.PacketProcessingService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.packet.service.rev130709.PacketReceived;
import org.opendaylight.yang.gen.v1.urn.opendaylight.packet.service.rev130709.TransmitPacketInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.packet.service.rev130709.TransmitPacketInputBuilder;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 */
public class SimpleLearningSwitchHandlerImpl implements SimpleLearningSwitchHandler {
    
    private static final Logger LOG = LoggerFactory
            .getLogger(SimpleLearningSwitchHandler.class);
    
    private DataChangeListenerRegistrationPublisher registrationPublisher;
    private OFDataStoreAccessor dataStoreAccessor;
    private PacketProcessingService packetProcessingService;

    private boolean iAmLearning = false;

    private NodeId nodeId;
    private AtomicLong flowIdInc = new AtomicLong();
    private InstanceIdentifier<Node> nodePath;
    private InstanceIdentifier<Table> tablePath;

    @Override
    public synchronized void onSwitchAppeared(InstanceIdentifier<Table> appearedTablePath) {
        if (iAmLearning) {
            LOG.debug("already learning a node, skipping {}", nodeId.getValue());
            return;
        }
        
        LOG.debug("expected table acquired, learning ..");
       
        // disable listening - simple learning handles only one node (switch)
        try {
            LOG.debug("closing dataChangeListenerRegistration");
            registrationPublisher.getDataChangeListenerRegistration().close();
        } catch (Exception e) {
            LOG.error("closing registration upon flowCapable node update listener failed: " + e.getMessage(), e);
        }
        iAmLearning  = true;
        tablePath = appearedTablePath;
        nodePath = tablePath.firstIdentifierOf(Node.class);
        nodeId = nodePath.firstKeyOf(Node.class, NodeKey.class).getId();
        
        // start forwarding all packages to controller
        FlowId flowId = new FlowId(String.valueOf(flowIdInc.getAndIncrement()));
        InstanceIdentifier<Flow> flowPath = assemleFlowPath(flowId, tablePath);

        // create flow in table with id = 0, priority = 4 (other params are defaulted in OFDataStoreUtil)
        FlowBuilder allToCtrlFlow = OFDataStoreUtil.createFwdAllToControllerFlow(
                appearedTablePath.firstKeyOf(Table.class, TableKey.class).getId(), 4, flowId);

        LOG.debug("writing packetForwardToController flow");
        dataStoreAccessor.writeFlowToConfig(flowPath, allToCtrlFlow.build());        
    }

    /**
     * @param flowId
     * @param tablePathArg 
     * @return
     */
    private static InstanceIdentifier<Flow> assemleFlowPath(FlowId flowId, InstanceIdentifier<Table> tablePathArg) {
        FlowKey flowKey = new FlowKey(flowId);
        InstanceIdentifier<Flow> flowPath = InstanceIdentifier.builder(tablePathArg)
                .child(Flow.class, flowKey )
                .toInstance();
        return flowPath;
    }
    
    @Override
    public void setRegistrationPublisher(
            DataChangeListenerRegistrationPublisher registrationPublisher) {
        this.registrationPublisher = registrationPublisher;
    }
    
    @Override
    public void setDataStoreAccessor(OFDataStoreAccessor dataStoreAccessor) {
        this.dataStoreAccessor = dataStoreAccessor;
    }
    
    @Override
    public void setPacketProcessingService(
            PacketProcessingService packetProcessingService) {
        this.packetProcessingService = packetProcessingService;
    }
    
    @Override
    public void onPacketReceived(PacketReceived notification) {
        if (!iAmLearning) {
            // ignoring packets - this should not happen
            return;
        }
        
        LOG.debug("Received packet via match: {}", notification.getMatch());
        
        // detect and compare node - we support one switch
        if (!nodePath.contains(notification.getIngress().getValue())) {
            return;
        }
        
        // read src MAC and dst MAC
        byte[] dstMacRaw = extractDstMac(notification.getPayload());
        byte[] srcMacRaw = extractSrcMac(notification.getPayload());
        
        MacAddress dstMac = rawMacToMac(dstMacRaw);
        MacAddress srcMac = rawMacToMac(srcMacRaw);
        
        LOG.debug("Received packet from MAC match: {}", srcMac);
        LOG.debug("Received packet to   MAC match: {}", dstMac);
        
        // add src MAC to port map
        Map<MacAddress, NodeConnectorRef> mac2portMapping = new HashMap<>();
        mac2portMapping.put(srcMac, notification.getIngress());
        
        // if dst MAC mapped: 
        NodeConnectorRef dstNodeConnectorRef = mac2portMapping.get(dstMac);
        if (dstNodeConnectorRef != null) {
            // add flow
            addBridgeFlow(srcMac, dstMac, dstNodeConnectorRef);
            addBridgeFlow(dstMac, srcMac, notification.getIngress());
        }
        // flood
        flood(notification.getPayload(), notification.getIngress());
    }

    /**
     * @param srcMac
     * @param dstMac
     * @param dstNodeConnectorRef
     */
    private void addBridgeFlow(MacAddress srcMac, MacAddress dstMac,
            NodeConnectorRef dstNodeConnectorRef) {
        FlowId flowId = new FlowId(String.valueOf(flowIdInc.getAndIncrement()));
        
        // create flow in table with id = 0, priority = 2 (other params are defaulted in OFDataStoreUtil)
        InstanceIdentifier<Flow> flowPath = assemleFlowPath(flowId, tablePath);
        FlowBuilder srcToDstFlow = OFDataStoreUtil.createDirectMacToMacFlow((short) 0, 2,
                srcMac, dstMac, dstNodeConnectorRef);

        dataStoreAccessor.writeFlowToConfig(flowPath, srcToDstFlow.build());
    }

    /**
     * @param payload
     * @return
     */
    private static byte[] extractDstMac(byte[] payload) {
        return Arrays.copyOfRange(payload, 6, 12);
    }
    
    /**
     * @param payload
     * @return
     */
    private static byte[] extractSrcMac(byte[] payload) {
        return Arrays.copyOfRange(payload, 0, 6);
    }
    
    private static MacAddress rawMacToMac(byte[] rawMac) {
        MacAddress mac = null;
        if (rawMac != null && rawMac.length == 6) {
            StringBuffer sb = new StringBuffer();
            for (byte octet : rawMac) {
                sb.append(String.format(":%02X", octet));
            }
            mac = new MacAddress(sb.substring(1));
        }
        return mac;
    }
    
    private void flood(byte[] payload, NodeConnectorRef ingress) {
        NodeKey nodeKey = new NodeKey(nodeId);
        
        TransmitPacketInput input = buildPacketOut(payload, ingress, "0xfffffffb", nodeKey);
        packetProcessingService.transmitPacket(input);
    }
    
    private static TransmitPacketInput buildPacketOut(byte[] payload, NodeConnectorRef ingress,
            String outputPort, NodeKey nodekey) {
        InstanceIdentifier<Node> nodePath = InstanceIdentifier.builder(Nodes.class)
                .child(Node.class, nodekey).toInstance();
        
        NodeConnectorRef egressConfRef = new NodeConnectorRef(
                createNodeConnRef(nodePath, nodekey, outputPort));
        
        TransmitPacketInputBuilder tPackBuilder = new TransmitPacketInputBuilder();
        tPackBuilder.setPayload(payload);
        tPackBuilder.setNode(new NodeRef(nodePath));
        tPackBuilder.setCookie(null);
        tPackBuilder.setEgress(egressConfRef);
        tPackBuilder.setIngress(ingress);
        return tPackBuilder.build();
    }

    private static NodeConnectorRef createNodeConnRef(InstanceIdentifier<Node> nodeInstId, 
            NodeKey nodeKey, String port) {
        StringBuilder sBuild = new StringBuilder(nodeKey.getId().getValue()).append(":").append(port);
        NodeConnectorKey nConKey = new NodeConnectorKey(new NodeConnectorId(sBuild.toString()));
        InstanceIdentifier<NodeConnector> portPath = InstanceIdentifier.builder(nodeInstId)
                .child(NodeConnector.class, nConKey).toInstance();
        return new NodeConnectorRef(portPath);
    }
}
