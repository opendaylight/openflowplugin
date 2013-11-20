
/*
 * Copyright (c) 2013 Ericsson , Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.test;


import org.eclipse.osgi.framework.console.CommandInterpreter;
import org.eclipse.osgi.framework.console.CommandProvider;
import org.opendaylight.controller.md.sal.common.api.data.DataModification;
import org.opendaylight.controller.sal.binding.api.BindingAwareBroker.ProviderContext;
import org.opendaylight.controller.sal.binding.api.data.DataBrokerService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.DropAction;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.DropActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.config.rev130819.Flows;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.config.rev130819.flows.Flow;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.config.rev130819.flows.FlowBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.config.rev130819.flows.FlowKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.MatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeRef;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.Nodes;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.NodeKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.layer._3.match.Ipv4MatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.layer._4.match.TcpMatchBuilder;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.osgi.framework.BundleContext;



public class OpenflowpluginTestCommandProvider implements CommandProvider {

	private DataBrokerService dataBrokerService;
	private BundleContext ctx;

	public OpenflowpluginTestCommandProvider(BundleContext ctx) {
	    this.ctx = ctx;
	}

	public void onSessionInitiated(ProviderContext session) {
		dataBrokerService = session.getSALService(DataBrokerService.class);
		ctx.registerService(CommandProvider.class.getName(), this, null);
    }



	public void _loadFlowData(CommandInterpreter ci) {
		NodeRef nodeOne = createNodeRef(OpenflowpluginTestActivator.NODE_ID);
        // Sample data , committing to DataStore
        DataModification modification = (DataModification) dataBrokerService.beginTransaction();
        long id = 123;
        FlowKey key = new FlowKey(id, nodeOne);
        InstanceIdentifier<?> path1;
        FlowBuilder flow = new FlowBuilder();
        flow.setKey(key);
        MatchBuilder match = new MatchBuilder();
        Ipv4MatchBuilder ipv4Match = new Ipv4MatchBuilder();
        // ipv4Match.setIpv4Destination(new Ipv4Prefix(cliInput.get(4)));
        match.setLayer4Match(new TcpMatchBuilder().build());
        flow.setMatch(match.build());
        DropAction dropAction = new DropActionBuilder().build();
     //   ActionBuilder action = new ActionBuilder();

      //  List<org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev130819.flow.Action> actions = Collections
             //   .singletonList(action.build());
     //   flow.setAction(actions);
        flow.setPriority(2);

        path1 = InstanceIdentifier.builder(Flows.class).child(Flow.class, key).toInstance();
        DataObject cls = (DataObject) modification.readConfigurationData(path1);
        modification.putConfigurationData(path1, flow.build());
        modification.commit();
        ci.print("Flow Data Loaded");
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

