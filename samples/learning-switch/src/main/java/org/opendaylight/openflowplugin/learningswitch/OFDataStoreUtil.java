/**
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.learningswitch;

import java.util.ArrayList;
import java.util.List;

import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.Uri;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev100924.MacAddress;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.OutputActionCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.output.action._case.OutputActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.list.Action;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.list.ActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.list.ActionKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNode;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.Table;
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
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeConnectorRef;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.node.NodeConnector;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.node.NodeConnectorKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.ethernet.match.fields.EthernetDestinationBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.ethernet.match.fields.EthernetSourceBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.EthernetMatchBuilder;
import org.opendaylight.yangtools.yang.binding.DataObject;

/**
 * 
 */
public abstract class OFDataStoreUtil {

    /**
     * @param value inspected {@link Node}
     * @return underlying {@link FlowCapableNode} - if available via augmentation
     */
    public static FlowCapableNode distillFlowCapableNode(DataObject value) {
        FlowCapableNode resultNode = null;
        if (value instanceof Node) {
            Node nd = (Node) value;
            resultNode = nd.getAugmentation(FlowCapableNode.class);
        }
        
        return resultNode;
    }
    
    /**
     * @param tableBag list of tables provided by {@link FlowCapableNode}
     * @param tableId id to search
     * @return table with given id or null
     */
    public static Table findTable(List<Table> tableBag, Short tableId) {
        Table table = null;
        
        if (tableId != null) {
            for (Table tableItem : tableBag) {
                if (tableId.equals(tableItem.getId())) {
                    table = tableItem;
                    break;
                }
            }
        }
        return table;
    }
    
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
        allToCtrlFlow.setFlowName("allPacketsToCtrl");
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
           .setHardTimeout(300)
           .setIdleTimeout(240)
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

}
