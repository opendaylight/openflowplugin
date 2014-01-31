/**
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.learningswitch;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev100924.MacAddress;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.Table;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.TableKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.table.Flow;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.table.FlowBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeConnectorRef;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.node.NodeConnector;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.node.NodeConnectorKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.NodeKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.packet.service.rev130709.PacketProcessingListener;
import org.opendaylight.yang.gen.v1.urn.opendaylight.packet.service.rev130709.PacketProcessingService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.packet.service.rev130709.PacketReceived;
import org.opendaylight.yang.gen.v1.urn.opendaylight.packet.service.rev130709.TransmitPacketInput;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 */
public class LearningSwitchHandlerSimpleImpl implements LearningSwitchHandler, PacketProcessingListener {
    
    private static final Logger LOG = LoggerFactory
            .getLogger(LearningSwitchHandler.class);

    private static final byte[] ETH_TYPE_IPV4 = new byte[] {0x08, 0x00};
    
    private DataChangeListenerRegistrationPublisher registrationPublisher;
    private OFDataStoreAccessor dataStoreAccessor;
    private PacketProcessingService packetProcessingService;

    private boolean iAmLearning = false;

    private NodeId nodeId;
    private AtomicLong flowIdInc = new AtomicLong();
    private InstanceIdentifier<Node> nodePath;
    private InstanceIdentifier<Table> tablePath;

    private Map<MacAddress, NodeConnectorRef> mac2portMapping;
    private Set<String> coveredMacPaths;

    @Override
    public synchronized void onSwitchAppeared(InstanceIdentifier<Table> appearedTablePath) {
        if (iAmLearning) {
            LOG.debug("already learning a node, skipping {}", nodeId.getValue());
            return;
        }
        
        LOG.debug("expected table acquired, learning ..");
       
        // disable listening - simple learning handles only one node (switch)
        if (registrationPublisher != null) {
            try {
                LOG.debug("closing dataChangeListenerRegistration");
                registrationPublisher.getDataChangeListenerRegistration().close();
            } catch (Exception e) {
                LOG.error("closing registration upon flowCapable node update listener failed: " + e.getMessage(), e);
            }
        }
        
        iAmLearning  = true;
        tablePath = appearedTablePath;
        nodePath = tablePath.firstIdentifierOf(Node.class);
        nodeId = nodePath.firstKeyOf(Node.class, NodeKey.class).getId();
        mac2portMapping = new HashMap<>();
        coveredMacPaths = new HashSet<>();
        
        // start forwarding all packages to controller
        FlowId flowId = new FlowId(String.valueOf(flowIdInc.getAndIncrement()));
        InstanceIdentifier<Flow> flowPath = OFFlowUtil.assemleFlowPath(flowId, tablePath);

        // create flow in table with id = 0, priority = 4 (other params are defaulted in OFDataStoreUtil)
        FlowBuilder allToCtrlFlow = OFFlowUtil.createFwdAllToControllerFlow(
                appearedTablePath.firstKeyOf(Table.class, TableKey.class).getId(), 0, flowId);

        LOG.debug("writing packetForwardToController flow");
        dataStoreAccessor.writeFlowToConfig(flowPath, allToCtrlFlow.build());        
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
        byte[] dstMacRaw = OFFlowUtil.extractDstMac(notification.getPayload());
        byte[] srcMacRaw = OFFlowUtil.extractSrcMac(notification.getPayload());
        byte[] etherType = OFFlowUtil.extractEtherType(notification.getPayload());
        
        MacAddress dstMac = OFFlowUtil.rawMacToMac(dstMacRaw);
        MacAddress srcMac = OFFlowUtil.rawMacToMac(srcMacRaw);
        
        NodeConnectorKey ingressKey = OFFlowUtil.distillKey(notification.getIngress());
        if (LOG.isDebugEnabled()) {
            LOG.debug("Received packet from MAC match: {}, ingress: {}", srcMac,
                    ingressKey.getId());
            LOG.debug("Received packet to   MAC match: {}", dstMac);
            LOG.debug("Ethertype: {}", Integer.toHexString(0x0000ffff & ByteBuffer.wrap(etherType).getShort()));
        }
        
        // learn by IPv4 traffic only
        if (Arrays.equals(ETH_TYPE_IPV4, etherType)) {
            NodeConnectorRef previousPort = mac2portMapping.put(srcMac, notification.getIngress());
            if (previousPort != null && !notification.getIngress().equals(previousPort)) {
                LOG.debug("mac2port mapping changed by mac {}: {} -> {}",
                        srcMac, OFFlowUtil.distillKey(previousPort).getId(), ingressKey.getId());
            }
            // if dst MAC mapped: 
            NodeConnectorRef dstNodeConnectorRef = mac2portMapping.get(dstMac);
            if (dstNodeConnectorRef != null) {
                synchronized (coveredMacPaths) {
                    if (!dstNodeConnectorRef.equals(notification.getIngress())) {
                        // add flow
                        addBridgeFlow(srcMac, dstMac, dstNodeConnectorRef);
                        addBridgeFlow(dstMac, srcMac, notification.getIngress());
                    } else {
                        LOG.debug("useless rule ignoring - both MACs are behind the same port");
                    }
                }
                LOG.debug("packetIn-directing.. to {}", 
                        OFFlowUtil.distillKey(dstNodeConnectorRef).getId());
                sendThroughPort(notification.getPayload(), notification.getIngress(), dstNodeConnectorRef);
            } else {
                // flood
                LOG.debug("packetIn-still flooding.. ");
                flood(notification.getPayload(), notification.getIngress());
            }
        } else {
            //non IPv4 package
            flood(notification.getPayload(), notification.getIngress());
        }

    }

    /**
     * @param srcMac
     * @param dstMac
     * @param dstNodeConnectorRef
     */
    private void addBridgeFlow(MacAddress srcMac, MacAddress dstMac,
            NodeConnectorRef dstNodeConnectorRef) {
        synchronized (coveredMacPaths) {
            String macPath = srcMac.toString() + dstMac.toString();
            if (!coveredMacPaths.contains(macPath)) {
                if (LOG.isDebugEnabled()) {
                    NodeConnectorKey nodeConnectorKey = dstNodeConnectorRef.getValue()
                            .firstKeyOf(NodeConnector.class, NodeConnectorKey.class);
                    LOG.debug("covering mac path: {} by [{}]", macPath, nodeConnectorKey.getId());
                }
                
                coveredMacPaths.add(macPath);
                FlowId flowId = new FlowId(String.valueOf(flowIdInc.getAndIncrement()));

                // create flow in table with id = 0, priority = 0 (other params are defaulted in OFDataStoreUtil)
                InstanceIdentifier<Flow> flowPath = OFFlowUtil.assemleFlowPath(flowId, tablePath);
                FlowBuilder srcToDstFlow = OFFlowUtil.createDirectMacToMacFlow((short) 0, 512,
                        srcMac, dstMac, dstNodeConnectorRef);

                dataStoreAccessor.writeFlowToConfig(flowPath, srcToDstFlow.build());
            }
        }
    }

    private void flood(byte[] payload, NodeConnectorRef ingress) {
        NodeKey nodeKey = new NodeKey(nodeId);
        NodeConnectorRef egressConfRef = new NodeConnectorRef(
                OFFlowUtil.createNodeConnRef(nodePath, nodeKey, "0xfffffffb"));
        
        sendThroughPort(payload, ingress, egressConfRef);
    }
    
    private void sendThroughPort(byte[] payload, NodeConnectorRef ingress, NodeConnectorRef egress) {
        NodeKey nodeKey = new NodeKey(nodeId);
        TransmitPacketInput input = OFFlowUtil.buildPacketOut(payload, ingress, egress, nodeKey);
        packetProcessingService.transmitPacket(input);
    }
}
