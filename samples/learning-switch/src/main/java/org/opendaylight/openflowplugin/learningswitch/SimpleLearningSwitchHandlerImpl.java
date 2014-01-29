/**
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.learningswitch;

import java.util.List;

import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNode;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.Table;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.table.Flow;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.table.FlowBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.table.FlowKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yang.gen.v1.urn.opendaylight.packet.service.rev130709.PacketProcessingService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.packet.service.rev130709.PacketReceived;
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

    @Override
    public synchronized void onSwitchAppeared(FlowCapableNode fcNode, 
            InstanceIdentifier<Node> nodeInstanceId, NodeId nodeId) {
        if (iAmLearning) {
            LOG.debug("already learning a node, skipping {}", nodeId.getValue());
            return;
        }
        
        // check tables
        List<Table> tableBag = fcNode.getTable();
        Table firstTable = OFDataStoreUtil.findTable(tableBag, (short) 0);
        LOG.debug("table[0]: {}", firstTable);
       
        if (firstTable != null) {
            // disable listening - simple learning handles only one node (switch)
            try {
                registrationPublisher.getDataChangeListenerRegistration().close();
            } catch (Exception e) {
                LOG.error("closing registration upon flowCapable node update listener failed: " + e.getMessage(), e);
            }
            iAmLearning  = true;
            
            // start forwarding all packages to controller
            FlowId flowId = new FlowId("42");
            FlowKey flowKey = new FlowKey(flowId);
            InstanceIdentifier<Flow> flowPath = InstanceIdentifier.builder(nodeInstanceId)
                    .augmentation(FlowCapableNode.class)
                    .child(Table.class, firstTable.getKey())
                    .child(Flow.class, flowKey )
                    .toInstance();

            // create flow in table with id = 0, priority = 4 (other params are defaulted in OFDataStoreUtil)
            FlowBuilder allToCtrlFlow = OFDataStoreUtil.createFwdAllToControllerFlow(
                    firstTable.getId(), 4);

            dataStoreAccessor.writeFlowToConfig(flowPath, allToCtrlFlow.build());        
        }
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
        
        LOG.debug("Received packet of reason: {}", notification.getPacketInReason());
        
        //TODO: read src MAC and dst MAC
        //TODO: add src MAC to port map
        //TODO: if no flow:
        //TODO:   if dst MAC mapped: 
        //TODO:     add flow
        //TODO:     add remark on which MACs are covered by flow
        //TODO:   flood
        //TODO: else:
        //TODO:   exception - flow is not working
        
        
/*
        Packet formattedPak = this.dataPacketService.decodeDataPacket(inPkt);
        NodeConnector incoming_connector = inPkt.getIncomingNodeConnector();
        Node incoming_node = incoming_connector.getNode();

        if (formattedPak instanceof Ethernet) {
            byte[] srcMAC = ((Ethernet)formattedPak).getSourceMACAddress();
            byte[] dstMAC = ((Ethernet)formattedPak).getDestinationMACAddress();

            // Hub implementation
            if (function.equals("hub")) {
                floodPacket(inPkt);
                return PacketResult.CONSUME;
            }

            // Switch
            else {
                long srcMAC_val = BitBufferHelper.toNumber(srcMAC);
                long dstMAC_val = BitBufferHelper.toNumber(dstMAC);

                Match match = new Match();
                match.setField( new MatchField(MatchType.IN_PORT, incoming_connector) );
                match.setField( new MatchField(MatchType.DL_DST, dstMAC.clone()) );

                // Set up the mapping: switch -> src MAC address -> incoming port
                if (this.mac_to_port_per_switch.get(incoming_node) == null) {
                    this.mac_to_port_per_switch.put(incoming_node, new HashMap<Long, NodeConnector>());
                }
                this.mac_to_port_per_switch.get(incoming_node).put(srcMAC_val, incoming_connector);

                NodeConnector dst_connector = this.mac_to_port_per_switch.get(incoming_node).get(dstMAC_val);

                // Do I know the destination MAC?
                if (dst_connector != null) {

                    List<Action> actions = new ArrayList<Action>();
                    actions.add(new Output(dst_connector));

                    Flow f = new Flow(match, actions);

                    // Modify the flow on the network node
                    Status status = programmer.addFlow(incoming_node, f);
                    if (!status.isSuccess()) {
                        logger.warn(
                                "SDN Plugin failed to program the flow: {}. The failure is: {}",
                                f, status.getDescription());
                        return PacketResult.IGNORED;
                    }
                    logger.info("Installed flow {} in node {}",
                            f, incoming_node);
                }
                else 
                    floodPacket(inPkt);
            }
        }
        */
    }
    
    

}
