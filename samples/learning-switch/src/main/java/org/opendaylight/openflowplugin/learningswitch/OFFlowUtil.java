/**
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.learningswitch;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.Uri;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev100924.MacAddress;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.OutputActionCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.output.action._case.OutputActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.list.Action;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.list.ActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.list.ActionKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.Table;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.table.Flow;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.table.FlowBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.table.FlowKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.FlowModFlags;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.OutputPortValues;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.InstructionsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.MatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.instruction.ApplyActionsCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.instruction.apply.actions._case.ApplyActionsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.list.Instruction;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.list.InstructionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.list.InstructionKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeConnectorId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeConnectorRef;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeRef;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.Nodes;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.node.NodeConnector;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.node.NodeConnectorKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.NodeKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.ethernet.match.fields.EthernetDestinationBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.ethernet.match.fields.EthernetSourceBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.EthernetMatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.packet.service.rev130709.PacketProcessingService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.packet.service.rev130709.TransmitPacketInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.packet.service.rev130709.TransmitPacketInputBuilder;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

/**
 * 
 */
public abstract class OFFlowUtil {

    /**
     * @param tableId
     * @param priority 
     * @param srcMac 
     * @param dstMac 
     * @param dstPort 
     * @return {@link FlowBuilder} forwarding all packets to controller port
     */
    public static FlowBuilder createDirectMacToMacFlow(Short tableId, int priority,
            MacAddress srcMac, MacAddress dstMac, NodeConnectorRef dstPort) {
        FlowBuilder allToCtrlFlow = new FlowBuilder();
        allToCtrlFlow.setTableId(tableId);
        allToCtrlFlow.setFlowName("mac2mac");
        allToCtrlFlow.setId(new FlowId(Long.toString(allToCtrlFlow.hashCode())));
        
        MatchBuilder match = new MatchBuilder();
        EthernetMatchBuilder ethernetMatch = new EthernetMatchBuilder();
        EthernetSourceBuilder ethSourceBuilder = new EthernetSourceBuilder();
        ethSourceBuilder.setAddress(srcMac);
        ethernetMatch.setEthernetSource(ethSourceBuilder.build());
        EthernetDestinationBuilder ethDestinationBuilder = new EthernetDestinationBuilder();
        ethDestinationBuilder.setAddress(dstMac);
        ethernetMatch.setEthernetDestination(ethDestinationBuilder.build());
        match.setEthernetMatch(ethernetMatch.build());
       
        OutputActionBuilder output = new OutputActionBuilder();
        output.setMaxLength(new Integer(0xffff));
        Uri outputPort = dstPort.getValue().firstKeyOf(
                NodeConnector.class, NodeConnectorKey.class).getId();
        output.setOutputNodeConnector(outputPort);
        ActionBuilder ab = new ActionBuilder();
        ab.setAction(new OutputActionCaseBuilder().setOutputAction(output.build()).build());
        
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
        
        allToCtrlFlow.setMatch(match.build())
           .setInstructions(isb.build())
           .setPriority(priority)
           .setBufferId(0L)
           .setHardTimeout(0)
           .setIdleTimeout(0)
           .setFlags(new FlowModFlags(false, false, false, false, false));
        
        return allToCtrlFlow;
    }
    
    /**
     * @param tableId
     * @param priority
     * @param flowId 
     * @return {@link FlowBuilder} forwarding all packets to controller port
     */
    public static FlowBuilder createFwdAllToControllerFlow(Short tableId, int priority, FlowId flowId) {
        FlowBuilder allToCtrlFlow = new FlowBuilder();
        allToCtrlFlow.setTableId(tableId);
        allToCtrlFlow.setFlowName("allPacketsToCtrl");
        allToCtrlFlow.setId(flowId);
        allToCtrlFlow.setKey(new FlowKey(flowId));
        
        MatchBuilder emptyMatchBuilder = new MatchBuilder();

        // Create output action -> send to controller
        OutputActionBuilder output = new OutputActionBuilder();
        output.setMaxLength(new Integer(0xffff));
        Uri controllerPort = new Uri(OutputPortValues.CONTROLLER.toString());
        output.setOutputNodeConnector(controllerPort);
        
        ActionBuilder ab = new ActionBuilder();
        ab.setAction(new OutputActionCaseBuilder().setOutputAction(output.build()).build());
        ab.setOrder(0);
        ab.setKey(new ActionKey(0));
        
        List<Action> actionList = new ArrayList<Action>();
        actionList.add(ab.build());
        
        // Create an Apply Action
        ApplyActionsBuilder aab = new ApplyActionsBuilder();
        aab.setAction(actionList);

        // Wrap our Apply Action in an Instruction
        InstructionBuilder ib = new InstructionBuilder();
        ib.setInstruction(new ApplyActionsCaseBuilder().setApplyActions(aab.build()).build());
        ib.setOrder(0);
        ib.setKey(new InstructionKey(0));

        // Put our Instruction in a list of Instructions
        InstructionsBuilder isb = new InstructionsBuilder();
        List<Instruction> instructions = new ArrayList<Instruction>();
        instructions.add(ib.build());
        isb.setInstruction(instructions);
        
        allToCtrlFlow.setMatch(emptyMatchBuilder.build());
        allToCtrlFlow.setInstructions(isb.build());
        allToCtrlFlow.setPriority(priority);
        allToCtrlFlow.setBufferId(0L);
        allToCtrlFlow.setHardTimeout(300);
        allToCtrlFlow.setIdleTimeout(240);
        allToCtrlFlow.setFlags(new FlowModFlags(false, false, false, false, false));
        
        return allToCtrlFlow;
    }

    /**
     * @param flowId
     * @param tablePathArg 
     * @return path to flow
     */
    public static InstanceIdentifier<Flow> assemleFlowPath(FlowId flowId, InstanceIdentifier<Table> tablePathArg) {
        FlowKey flowKey = new FlowKey(flowId);
        InstanceIdentifier<Flow> flowPath = InstanceIdentifier.builder(tablePathArg)
                .child(Flow.class, flowKey )
                .toInstance();
        return flowPath;
    }

    /**
     * @param payload
     * @return destination MAC address
     */
    public static byte[] extractDstMac(byte[] payload) {
        return Arrays.copyOfRange(payload, 0, 6);
    }

    /**
     * @param payload
     * @return source MAC address
     */
    public static byte[] extractSrcMac(byte[] payload) {
        return Arrays.copyOfRange(payload, 6, 12);
    }

    /**
     * @param rawMac
     * @return {@link MacAddress} wrapping string value, baked upon binary MAC address
     */
    public static MacAddress rawMacToMac(byte[] rawMac) {
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

    /**
     * @param payload
     * @param ingress
     * @param egress
     * @param nodekey
     * @return packetOut suitable for rpc: {@link PacketProcessingService#transmitPacket(TransmitPacketInput)}
     */
    public static TransmitPacketInput buildPacketOut(byte[] payload, NodeConnectorRef ingress,
            NodeConnectorRef egress, NodeKey nodekey) {
        InstanceIdentifier<Node> nodePath = InstanceIdentifier.builder(Nodes.class)
                .child(Node.class, nodekey).toInstance();
        
        TransmitPacketInputBuilder tPackBuilder = new TransmitPacketInputBuilder();
        tPackBuilder.setPayload(payload);
        tPackBuilder.setNode(new NodeRef(nodePath));
        tPackBuilder.setCookie(null);
        tPackBuilder.setEgress(egress);
        tPackBuilder.setIngress(ingress);
        return tPackBuilder.build();
    }

    /**
     * @param nodeInstId
     * @param nodeKey
     * @param port
     * @return port wrapped into {@link NodeConnectorRef}
     */
    public static NodeConnectorRef createNodeConnRef(InstanceIdentifier<Node> nodeInstId, 
            NodeKey nodeKey, String port) {
        StringBuilder sBuild = new StringBuilder(nodeKey.getId().getValue()).append(":").append(port);
        NodeConnectorKey nConKey = new NodeConnectorKey(new NodeConnectorId(sBuild.toString()));
        InstanceIdentifier<NodeConnector> portPath = InstanceIdentifier.builder(nodeInstId)
                .child(NodeConnector.class, nConKey).toInstance();
        return new NodeConnectorRef(portPath);
    }

}
