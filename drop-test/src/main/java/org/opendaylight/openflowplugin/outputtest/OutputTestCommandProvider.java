/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 * 
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.outputtest;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.eclipse.osgi.framework.console.CommandInterpreter;
import org.eclipse.osgi.framework.console.CommandProvider;
import org.opendaylight.controller.md.sal.common.api.TransactionStatus;
import org.opendaylight.controller.md.sal.common.api.data.DataModification;
import org.opendaylight.controller.sal.binding.api.BindingAwareBroker.ProviderContext;
import org.opendaylight.controller.sal.binding.api.data.DataBrokerService;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.Uri;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.OutputActionCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.output.action._case.OutputActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.list.Action;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.list.ActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.list.ActionKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNode;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.Table;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.TableKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.table.Flow;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.table.FlowBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.InstructionsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.MatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.instruction.ApplyActionsCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.instruction.apply.actions._case.ApplyActionsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.list.Instruction;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.list.InstructionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.list.InstructionKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeConnectorId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeConnectorRef;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeRef;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.Nodes;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.node.NodeConnector;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.node.NodeConnectorKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.NodeBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.NodeKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.packet.service.rev130709.PacketProcessingService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.packet.service.rev130709.TransmitPacketInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.packet.service.rev130709.TransmitPacketInputBuilder;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OutputTestCommandProvider implements CommandProvider {

	private PacketProcessingService packetProcessingService;
	private ProviderContext pc;
	private BundleContext ctx;
	private boolean sessionInitiated = false;
	private static Logger LOG = LoggerFactory
			.getLogger(OutputTestCommandProvider.class);

	public OutputTestCommandProvider(BundleContext ctx) {
		this.ctx = ctx;
	}

	public void onSessionInitiated(ProviderContext session) {
		pc = session;
		packetProcessingService = session.getRpcService(PacketProcessingService.class);
		ctx.registerService(CommandProvider.class.getName(), this, null);
		this.sessionInitiated = true;
	}

	public void _sendOutputMsg(CommandInterpreter ci) {
		/* Sending package OUT */
		LOG.info("SendOutMsg");
		if (sessionInitiated) {
			String inNodeKey = ci.nextArgument();
//			NodeBuilder nodeBuilder = createNodeBuilder(inNodeKey);
//			FlowBuilder flowBuilder = flowBuilder(nodeBuilder);
//			
//			DataBrokerService dataBrokerService = pc.getSALService(DataBrokerService.class);
//			DataModification<InstanceIdentifier<?>, DataObject> modif = dataBrokerService.beginTransaction();
//			
//	        InstanceIdentifier<Flow> path1 = InstanceIdentifier.builder(Nodes.class)
//	                .child(Node.class, nodeBuilder.getKey()).augmentation(FlowCapableNode.class)
//	                .child(Table.class, new TableKey(flowBuilder.getTableId())).child(Flow.class, flowBuilder.getKey())
//	                .build();
//	        modif.putConfigurationData(path1, flowBuilder.build());
//	        Future<RpcResult<TransactionStatus>> commitFuture = modif.commit();
//	        try {
//	            RpcResult<TransactionStatus> result = commitFuture.get();
//	            TransactionStatus status = result.getResult();
//	            ci.println("Status of Flow Data Loaded Transaction: " + status);
//
//	        } catch (InterruptedException e) {
//	            // TODO Auto-generated catch block
//	            e.printStackTrace();
//	        } catch (ExecutionException e) {
//	            // TODO Auto-generated catch block
//	            e.printStackTrace();
//	        }
	        
			NodeRef ref = createNodeRef(inNodeKey);
			TransmitPacketInputBuilder transPack = new TransmitPacketInputBuilder();
			transPack.setPayload(new String("BRM").getBytes());
			transPack.setNode(ref);
			transPack.setCookie(null);
			NodeConnectorRef nConRef = new NodeConnectorRef(createNodeConectorRef(inNodeKey,"0xfffffffd"));
			transPack.setEgress(nConRef);
			
			TransmitPacketInput input = transPack.build();
			
			packetProcessingService.transmitPacket(input);
		} else {
			ci.println("Session not initiated, try again in a few seconds");
		}
	}

	@Override
	public String getHelp() {
		StringBuilder strBuf = new StringBuilder(
				"-------------- OUT Package ----------\n")
				.append(" sendOutputMsg command + nodeId as param sends empty package out \n ");
		return strBuf.toString();
	}
	
	private NodeRef createNodeRef(String nodeId) {
        NodeKey key = new NodeKey(new NodeId(nodeId));
        InstanceIdentifier<Node> path =
                InstanceIdentifier.builder(Nodes.class).child(Node.class, key).toInstance();
        
        
        return new NodeRef(path);
    }
	
	private NodeConnectorRef createNodeConectorRef(String nodeId, String port) {
		NodeConnectorKey nConKey = new NodeConnectorKey(new NodeConnectorId(nodeId + ":" + port));
        InstanceIdentifier<NodeConnector> path =
                InstanceIdentifier.builder(Nodes.class)
                				.child(Node.class, new NodeKey(new NodeId(nodeId)))
                				.child(NodeConnector.class, nConKey).toInstance();
        return new NodeConnectorRef(path);
    }
	
	private NodeBuilder createNodeBuilder(String nodeId) {
        NodeRef nodeOne = createNodeRef(nodeId);
        NodeBuilder builder = new NodeBuilder();
        builder.setId(new NodeId(nodeId));
        builder.setKey(new NodeKey(builder.getId()));
        return builder;
    }
	
	private FlowBuilder flowBuilder (NodeBuilder nodeBuilder) {
		FlowBuilder flowBuilder = new FlowBuilder();
        flowBuilder.setMatch(new MatchBuilder().build());
        
        List<Action> actionList = new ArrayList<Action>();
        ActionBuilder ab = new ActionBuilder();

        OutputActionBuilder output = new OutputActionBuilder();
        output.setMaxLength(56);
        Uri value = new Uri("CONTROLLER");
        output.setOutputNodeConnector(value);
        ab.setAction(new OutputActionCaseBuilder().setOutputAction(output.build()).build());
        ab.setOrder(0);
        ab.setKey(new ActionKey(0));
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
        
        flowBuilder.setInstructions(isb.build());
        return flowBuilder;
	}
}
