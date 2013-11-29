
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
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.DropAction;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.DropActionBuilder;
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
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.EthType;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.osgi.framework.BundleContext;



public class OpenflowpluginTestCommandProvider implements CommandProvider {

	private DataBrokerService dataBrokerService;
	private ProviderContext pc;
	private BundleContext ctx;
	private Flow testFlow;
    private Node testNode;
    private String originalFlowName = "Foo";
    private String updatedFlowName = "Bar";

	public OpenflowpluginTestCommandProvider(BundleContext ctx) {
	    this.ctx = ctx;
	}

	public void onSessionInitiated(ProviderContext session) {
	    pc = session;
		dataBrokerService = session.getSALService(DataBrokerService.class);
		ctx.registerService(CommandProvider.class.getName(), this, null);
		//createTestNode();
		
    }

	private void createTestNode() {
	    NodeRef nodeOne = createNodeRef(OpenflowpluginTestActivator.NODE_ID);
        NodeBuilder builder = new NodeBuilder();
        builder.setId(new NodeId(OpenflowpluginTestActivator.NODE_ID));
        builder.setKey(new NodeKey(builder.getId()));
        testNode = builder.build();
	}
	
	private void createUserNode(String nodeRef) {
        NodeRef nodeOne = createNodeRef(nodeRef);
        NodeBuilder builder = new NodeBuilder();
        builder.setId(new NodeId(nodeRef));
        builder.setKey(new NodeKey(builder.getId()));
        testNode = builder.build();
    }

	private InstanceIdentifier<Node> nodeToInstanceId(Node node) {
	    return InstanceIdentifier.builder(Nodes.class).child(Node.class, node.getKey()).toInstance();
	}

	private void createTestFlow() {
        // Sample data , committing to DataStore
        DataModification modification = (DataModification) dataBrokerService.beginTransaction();
        long id = 123;
        FlowKey key = new FlowKey(id, new NodeRef(new NodeRef(nodeToInstanceId(testNode))));
        FlowBuilder flow = new FlowBuilder();
        flow.setKey(key);
        MatchBuilder match = new MatchBuilder();
        EthernetMatchBuilder eth = 
                new EthernetMatchBuilder();
        EthernetTypeBuilder ethTypeBuilder = new EthernetTypeBuilder();
        ethTypeBuilder.setType(new EtherType((long) 0x86dd));
        eth.setEthernetType(ethTypeBuilder.build());
     //   Ipv4MatchBuilder ipv4Match = new Ipv4MatchBuilder();
        // ipv4Match.setIpv4Destination(new Ipv4Prefix(cliInput.get(4)));
       // Ipv4Prefix prefix = new Ipv4Prefix("10.0.0.1/24");
       // ipv4Match.setIpv4Destination(prefix);
       // Ipv4Match i4m = ipv4Match.build();
      //  match.setLayer3Match(i4m);
        flow.setMatch(match.build());        
        DropAction dropAction = new DropActionBuilder().build();
        ApplyActionsBuilder aab = new ApplyActionsBuilder();
        ActionBuilder ab = new ActionBuilder();
        ab.setAction(dropAction);
        List<Action> actionList = new ArrayList<Action>();
        actionList.add(ab.build());
        aab.setAction(actionList);
        InstructionBuilder ib = new InstructionBuilder();
        ib.setInstruction(aab.build());
        flow.setTableId((short) 0);
        InstructionsBuilder isb = new InstructionsBuilder();
        List<Instruction> instructions = new ArrayList<Instruction>();
        isb.setInstruction(instructions);
        flow.setInstructions(isb.build());
      //  ActionBuilder action = new ActionBuilder();

      //  List<org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev130819.flow.Action> actions = Collections
             //   .singletonList(action.build());
     //   flow.setAction(actions);
        flow.setPriority(2);
        flow.setFlowName(originalFlowName);
        testFlow = flow.build();
	}

	public void _mdremoveFlow(CommandInterpreter ci) {
	    DataModification modification = (DataModification) dataBrokerService.beginTransaction();
        InstanceIdentifier<Flow> path1 = InstanceIdentifier.builder(Flows.class).child(Flow.class, testFlow.getKey()).toInstance();
        DataObject cls = (DataObject) modification.readConfigurationData(path1);
        modification.removeOperationalData(nodeToInstanceId(testNode));
        modification.removeOperationalData(path1);
        modification.removeConfigurationData(nodeToInstanceId(testNode));
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

	public void _mdaddFlow(CommandInterpreter ci) {
	    String nref = ci.nextArgument();
	    
	    if (nref == null) {
	        ci.println("test node added");
	        createTestNode();
	    } else {
	        ci.println("User node added" + nref);
	        createUserNode(nref);
	    }
	    createTestFlow();
        writeFlow(ci, testFlow);
    }

	private void writeFlow(CommandInterpreter ci,Flow flow) {
	    DataModification modification = (DataModification) dataBrokerService.beginTransaction();
        InstanceIdentifier<Flow> path1 = InstanceIdentifier.builder(Flows.class).child(Flow.class, flow.getKey()).toInstance();
        DataObject cls = (DataObject) modification.readConfigurationData(path1);
        modification.putOperationalData(nodeToInstanceId(testNode), testNode);
        modification.putOperationalData(path1, flow);
        modification.putConfigurationData(nodeToInstanceId(testNode), testNode);
        modification.putConfigurationData(path1, flow);
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

	public void _mdmodifyFlow(CommandInterpreter ci) {
	    FlowBuilder flow = new FlowBuilder(testFlow);
	    flow.setFlowName(updatedFlowName);
	    writeFlow(ci, flow.build());
	    flow.setFlowName(originalFlowName);
	    writeFlow(ci, flow.build());
	}

	private static NodeRef createNodeRef(String string) {
        NodeKey key = new NodeKey(new NodeId(string));
        InstanceIdentifier<Node> path =
        	InstanceIdentifier.builder(Nodes.class).child(Node.class, key).toInstance();

        return new NodeRef(path);
    }

    @Override
    public String getHelp() {
        StringBuffer help = new StringBuffer();
        help.append("---FRM MD-SAL test module---\n");
        help.append("\t mdaddFlow <node id>        - node ref\n");
        help.append("\t mdmodifyFlow <node id>        - node ref\n");
        help.append("\t mdremoveFlow <node id>        - node ref\n");
       
        return help.toString();
    }
}

