/**
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.learningswitch;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.Future;

import org.opendaylight.controller.md.sal.common.api.TransactionStatus;
import org.opendaylight.controller.md.sal.common.api.data.DataChangeEvent;
import org.opendaylight.controller.sal.binding.api.BindingAwareBroker.ConsumerContext;
import org.opendaylight.controller.sal.binding.api.NotificationService;
import org.opendaylight.controller.sal.binding.api.data.DataBrokerService;
import org.opendaylight.controller.sal.binding.api.data.DataChangeListener;
import org.opendaylight.controller.sal.binding.api.data.DataModificationTransaction;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev100924.MacAddress;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.DropActionCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.drop.action._case.DropAction;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.drop.action._case.DropActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.list.Action;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.list.ActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNode;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.Table;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.TableKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.table.Flow;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.table.FlowBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.table.FlowKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.FlowModFlags;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.InstructionsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.MatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.instruction.ApplyActionsCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.instruction.apply.actions._case.ApplyActionsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.list.Instruction;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.list.InstructionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.Nodes;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.ethernet.match.fields.EthernetSourceBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.EthernetMatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.packet.service.rev130709.PacketProcessingListener;
import org.opendaylight.yang.gen.v1.urn.opendaylight.packet.service.rev130709.PacketProcessingService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.packet.service.rev130709.PacketReceived;
import org.opendaylight.yangtools.concepts.Registration;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.Identifier;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.binding.NotificationListener;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Listens to packetIn notification and 
 * <ul>
 * <li>in HUB mode simply floods all switch ports (except ingress port)</li>
 * <li>in LSWITCH mode collects source MAC address of packetIn and bind it with ingress port. 
 * If target MAC address is already bound then a flow is created (for direct communication between 
 * corresponding MACs)</li>
 * </ul>
 */
public class SimpleLearningSwitch implements PacketProcessingListener {
    
    protected static final Logger LOG = LoggerFactory
            .getLogger(SimpleLearningSwitch.class);

    private ConsumerContext context;
    private PacketProcessingService packetProcessingService;
    private DataBrokerService data;

    private Registration<NotificationListener> packetInRegistration; 
    
    /**
     * @param session
     */
    public void setContext(ConsumerContext session) {
        this.context = session;
    }

    /**
     * @param packetProcessingService the packetProcessingService to set
     */
    public void setPacketProcessingService(
            PacketProcessingService packetProcessingService) {
        this.packetProcessingService = packetProcessingService;
    }
    
    /**
     * @param data the data to set
     */
    public void setData(DataBrokerService data) {
        this.data = data;
    }

    /**
     * starting learning switch
     */
    public void start() {
        LOG.debug("start() -->");
        NotificationService notificationService = context.getSALService(NotificationService.class);
        packetInRegistration = notificationService.registerNotificationListener(this);
        
        //TODO: wait for node(openflow:1) 
        //TODO: then add flow (all packets direct to controller)

        DataChangeListener listener = new DataChangeListener() {
            
            @Override
            public void onDataChanged(
                    DataChangeEvent<InstanceIdentifier<?>, DataObject> change) {
                // TODO add flow
                Map<InstanceIdentifier<?>, DataObject> updated = change.getUpdatedOperationalData();
                for (Entry<InstanceIdentifier<?>, DataObject> updateItem : updated.entrySet()) {
                    
                    // TODO: divide to atomic methods
                    DataObject value = updateItem.getValue();
                    if (value instanceof Node) {
                        Node nd = (Node) value;
                        FlowCapableNode fcNode = nd.getAugmentation(FlowCapableNode.class);
                        
                        if (fcNode != null) {
                            List<Table> tableAmount = fcNode.getTable();
                            LOG.debug("fcNode: {}", fcNode);
                            InstanceIdentifier<Node> instId = (InstanceIdentifier<Node>) updateItem.getKey();
                            
                            //TODO: check amount of tables
                            TableKey tableKey = new TableKey((short) 0);
                            FlowId flowId = new FlowId("42");
                            FlowKey flowKey = new FlowKey(flowId);
                            InstanceIdentifier flowPath = InstanceIdentifier.builder(instId)
                                    .augmentation(FlowCapableNode.class)
                                    .child(Table.class, tableKey)
                                    .child(Flow.class, flowKey )
                                    .toInstance();
                            
                            FlowBuilder allToCtrlFlow = new FlowBuilder();
                            allToCtrlFlow.setTableId(tableKey.getId());
                            allToCtrlFlow.setFlowName("allPacketsToCtrl");
                            allToCtrlFlow.setId(new FlowId(Long.toString(allToCtrlFlow.hashCode())));
                            
                            
                            MatchBuilder match = new MatchBuilder();
                            EthernetMatchBuilder ethernetMatch = new EthernetMatchBuilder();
                            EthernetSourceBuilder ethSourceBuilder = new EthernetSourceBuilder();
                            //TODO: add src mac
//                            ethSourceBuilder.setAddress(new MacAddress(DropTestUtils.macToString(srcMac)));
                            ethernetMatch.setEthernetSource(ethSourceBuilder.build());
                            match.setEthernetMatch(ethernetMatch.build());
                            DropActionBuilder dab = new DropActionBuilder();
                            DropAction dropAction = dab.build();
                            ActionBuilder ab = new ActionBuilder();
                            ab.setAction(new DropActionCaseBuilder().setDropAction(dropAction).build());
                            
                            // Add our drop action to a list
                            ArrayList<Action> actionList = new ArrayList<>();
                            actionList.add(ab.build());
                            
                            // Create an Apply Action
                            ApplyActionsBuilder aab = new ApplyActionsBuilder();
                            aab.setAction(actionList);
                            
                            // Wrap our Apply Action in an Instruction
                            InstructionBuilder ib = new InstructionBuilder();
                            ib.setInstruction(new ApplyActionsCaseBuilder().setApplyActions(aab.build()).build());
                            
                            // Put our Instruction in a list of Instructions
                            InstructionsBuilder isb = new InstructionsBuilder();
                            ArrayList<Instruction> instructions = new ArrayList<Instruction>();
                            instructions.add(ib.build());
                            isb.setInstruction(instructions);
                            
                            allToCtrlFlow.setMatch(match.build());
                            allToCtrlFlow.setInstructions(isb.build());
                            allToCtrlFlow.setPriority(4);
                            allToCtrlFlow.setBufferId(0L);
                            allToCtrlFlow.setHardTimeout(300);
                            allToCtrlFlow.setIdleTimeout(240);
                            allToCtrlFlow.setFlags(new FlowModFlags(false, false, false, false, false));
                            
                            DataModificationTransaction addFlowTransaction = data.beginTransaction();
                            addFlowTransaction.putConfigurationData(flowPath, allToCtrlFlow.build());
                            Future<RpcResult<TransactionStatus>> result = addFlowTransaction.commit();
                        }
                    }
                }
            }
        };
        
        data.registerDataChangeListener(InstanceIdentifier.builder(Nodes.class).child(Node.class).toInstance(),
                listener);
        LOG.debug("start() <--");
    }
    
    /**
     * stopping learning switch 
     */
    public void stop() {
        LOG.debug("stop() -->");
        try {
            packetInRegistration.close();
            //TODO: remove flow (created in #start())
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }
        LOG.debug("stop() <--");
    }

    @Override
    public void onPacketReceived(PacketReceived notification) {
        LOG.debug("Received packet of reason: {}", notification.getPacketInReason());
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
