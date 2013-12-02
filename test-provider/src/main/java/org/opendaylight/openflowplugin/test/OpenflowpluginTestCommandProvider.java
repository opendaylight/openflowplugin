
/*
 * Copyright (c) 2013 Ericsson , Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.test;


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
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.Ipv4Prefix;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.DecNwTtl;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.DecNwTtlBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.list.Action;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.list.ActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.config.rev130819.Flows;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.config.rev130819.flows.Flow;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.config.rev130819.flows.FlowBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.config.rev130819.flows.FlowKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.InstructionsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.MatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.instruction.ApplyActionsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.list.Instruction;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.list.InstructionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeRef;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.Nodes;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.NodeBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.NodeKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.l2.types.rev130827.EtherType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.ethernet.match.fields.EthernetTypeBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.EthernetMatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.layer._3.match.Ipv4Match;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.layer._3.match.Ipv4MatchBuilder;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.osgi.framework.BundleContext;



public class OpenflowpluginTestCommandProvider implements CommandProvider {

	private DataBrokerService dataBrokerService;
	private ProviderContext pc;
	private BundleContext ctx;
	private FlowBuilder testFlow;
    private NodeBuilder testNode;
    private String originalFlowName = "Foo";
    private String updatedFlowName = "Bar";

	public OpenflowpluginTestCommandProvider(BundleContext ctx) {
	    this.ctx = ctx;
	}

	public void onSessionInitiated(ProviderContext session) {
	    pc = session;
		dataBrokerService = session.getSALService(DataBrokerService.class);
		ctx.registerService(CommandProvider.class.getName(), this, null);
		createTestFlow(createTestNode(null));
    }

	private NodeBuilder createTestNode(String nodeId) {
	    if(nodeId == null) {
	        nodeId = OpenflowpluginTestActivator.NODE_ID;
	    }
	    NodeRef nodeOne = createNodeRef(nodeId);
        NodeBuilder builder = new NodeBuilder();
        builder.setId(new NodeId(nodeId));
        builder.setKey(new NodeKey(builder.getId()));
        testNode = builder;
        return builder;
	}

	private InstanceIdentifier<Node> nodeBuilderToInstanceId(NodeBuilder node) {
	    return InstanceIdentifier.builder(Nodes.class).child(Node.class, node.getKey()).toInstance();
	}

	private FlowBuilder createTestFlow(NodeBuilder nodeBuilder) {

        long id = 123;
        FlowKey key = new FlowKey(id, new NodeRef(new NodeRef(nodeBuilderToInstanceId(nodeBuilder))));
        FlowBuilder flow = new FlowBuilder();
        flow.setKey(key);
        MatchBuilder match = new MatchBuilder();
        Ipv4MatchBuilder ipv4Match = new Ipv4MatchBuilder();
        // ipv4Match.setIpv4Destination(new Ipv4Prefix(cliInput.get(4)));
        Ipv4Prefix prefix = new Ipv4Prefix("10.0.0.1/24");
        ipv4Match.setIpv4Destination(prefix);
        Ipv4Match i4m = ipv4Match.build();
        match.setLayer3Match(i4m);
        
        EthernetMatchBuilder eth = new EthernetMatchBuilder();
        EthernetTypeBuilder ethTypeBuilder = new EthernetTypeBuilder();
        ethTypeBuilder.setType(new EtherType(0x0800L));
        eth.setEthernetType(ethTypeBuilder.build());
        match.setEthernetMatch(eth.build());
        
        flow.setMatch(match.build());

        // Create a drop action
        /*
         * Note: We are mishandling drop actions
        DropAction dropAction = new DropActionBuilder().build();
        ActionBuilder ab = new ActionBuilder();
        ab.setAction(dropAction);
        */

        DecNwTtlBuilder ta = new DecNwTtlBuilder();
        DecNwTtl decNwTtl = ta.build();
        ActionBuilder ab = new ActionBuilder();
        ab.setAction(decNwTtl);

        // Add our drop action to a list
        List<Action> actionList = new ArrayList<Action>();
        actionList.add(ab.build());

        // Create an Apply Action
        ApplyActionsBuilder aab = new ApplyActionsBuilder();
        aab.setAction(actionList);

        // Wrap our Apply Action in an Instruction
        InstructionBuilder ib = new InstructionBuilder();
        ib.setInstruction(aab.build());

        // Put our Instruction in a list of Instructions
        InstructionsBuilder isb = new InstructionsBuilder();
        List<Instruction> instructions = new ArrayList<Instruction>();
        instructions.add(ib.build());
        isb.setInstruction(instructions);

        // Add our instructions to the flow
        flow.setInstructions(isb.build());


        flow.setPriority(2);
        flow.setFlowName(originalFlowName);
        testFlow = flow;
        return flow;
	}

	public void _removeMDFlow(CommandInterpreter ci) {
	    DataModification<InstanceIdentifier<?>, DataObject> modification = dataBrokerService.beginTransaction();
	    NodeBuilder tn = createTestNode(ci.nextArgument());
	    FlowBuilder tf = createTestFlow(tn);
        InstanceIdentifier<Flow> path1 = InstanceIdentifier.builder(Flows.class).child(Flow.class, tf.getKey()).toInstance();
        modification.removeOperationalData(nodeBuilderToInstanceId(tn));
        modification.removeOperationalData(path1);
        modification.removeConfigurationData(nodeBuilderToInstanceId(tn));
        modification.removeConfigurationData(path1);
        Future<RpcResult<TransactionStatus>> commitFuture = modification.commit();
        try {
            RpcResult<TransactionStatus> result = commitFuture.get();
            TransactionStatus status = result.getResult();
            ci.println("Status of Flow Data Loaded Transaction: " + status);

        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (ExecutionException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
	}

	public void _addMDFlow(CommandInterpreter ci) {
        NodeBuilder tn = createTestNode(ci.nextArgument());
        FlowBuilder tf = createTestFlow(tn);
        writeFlow(ci, tf, tn);
    }

	private void writeFlow(CommandInterpreter ci,FlowBuilder flow, NodeBuilder nodeBuilder) {
	    DataModification<InstanceIdentifier<?>, DataObject> modification = dataBrokerService.beginTransaction();
        InstanceIdentifier<Flow> path1 = InstanceIdentifier.builder(Flows.class).child(Flow.class, flow.getKey()).toInstance();
        modification.putOperationalData(nodeBuilderToInstanceId(nodeBuilder), nodeBuilder.build());
        modification.putOperationalData(path1, flow.build());
        modification.putConfigurationData(nodeBuilderToInstanceId(nodeBuilder), nodeBuilder.build());
        modification.putConfigurationData(path1, flow.build());
        Future<RpcResult<TransactionStatus>> commitFuture = modification.commit();
        try {
            RpcResult<TransactionStatus> result = commitFuture.get();
            TransactionStatus status = result.getResult();
            ci.println("Status of Flow Data Loaded Transaction: " + status);

        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (ExecutionException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
	}

	public void _modifyMDFlow(CommandInterpreter ci) {
        NodeBuilder tn = createTestNode(ci.nextArgument());
	    FlowBuilder tf = createTestFlow(tn);
	    tf.setFlowName(updatedFlowName);
	    writeFlow(ci, tf,tn);
	    tf.setFlowName(originalFlowName);
	    writeFlow(ci, tf,tn);
	}

	private static NodeRef createNodeRef(String string) {
        NodeKey key = new NodeKey(new NodeId(string));
        InstanceIdentifier<Node> path =
        	InstanceIdentifier.builder().node(Nodes.class).node(Node.class, key).toInstance();

        return new NodeRef(path);
    }

    @Override
    public String getHelp() {
        return "No help";
    }
}

