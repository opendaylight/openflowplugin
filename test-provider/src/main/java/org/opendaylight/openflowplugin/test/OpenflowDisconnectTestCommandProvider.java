package org.opendaylight.openflowplugin.test;

import org.eclipse.osgi.framework.console.CommandInterpreter;
import org.eclipse.osgi.framework.console.CommandProvider;
import org.opendaylight.controller.sal.binding.api.BindingAwareBroker.ProviderContext;
import org.opendaylight.controller.sal.binding.api.data.DataBrokerService;
import org.osgi.framework.BundleContext;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeRef;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.Nodes;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.OpendaylightInventoryService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.NodeBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.NodeKey;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.controller.disconnect.DisconnectSwitchImpl;


public class OpenflowDisconnectTestCommandProvider implements CommandProvider {


    private DataBrokerService dataBrokerService;
    private ProviderContext pc;
    private final BundleContext ctx;
    private  OpendaylightInventoryService disconnect;
    private Node testNode1;
    private NodeBuilder testNode;
    
    public OpenflowDisconnectTestCommandProvider(BundleContext ctx) {
        this.ctx = ctx;
    }
    
    private NodeBuilder createTestNode(String nodeId) {
        if (nodeId == null) {
            nodeId = OpenflowpluginTestActivator.NODE_ID;
        }
        NodeRef nodeOne = createNodeRef(nodeId);
        NodeBuilder builder = new NodeBuilder();
        builder.setId(new NodeId(nodeId));
        builder.setKey(new NodeKey(builder.getId()));
        testNode = builder;
        return builder;
    }
    
    public void onSessionInitiated(ProviderContext session) {
        pc = session;
        dataBrokerService = session.getSALService(DataBrokerService.class);
        ctx.registerService(CommandProvider.class.getName(), this, null);
        createTestNode();
    }
    
    private void createTestNode() {
        NodeRef nodeOne = createNodeRef(OpenflowpluginTestActivator.NODE_ID);
        NodeBuilder builder = new NodeBuilder();
        builder.setId(new NodeId(OpenflowpluginTestActivator.NODE_ID));
        builder.setKey(new NodeKey(builder.getId()));
        testNode1 = builder.build();
    }
    

    private static NodeRef createNodeRef(String string) {
        NodeKey key = new NodeKey(new NodeId(string));
        InstanceIdentifier<Node> path = InstanceIdentifier.builder().node(Nodes.class).node(Node.class, key)
                .toInstance();

        return new NodeRef(path);
    }

    
    public void _disconnectSwitch(CommandInterpreter ci)
    {

        DisconnectSwitchImpl dsimp = new DisconnectSwitchImpl(ctx);
        String cmd = ci.nextArgument();
        createTestNode();
       
        if(cmd != null)
        {
        	System.out.println("****Disconnect Command Executed*****");
        	dsimp.disconnectSwitch(cmd);
        }
        else
        {
        	System.out.println("****Please provide DPN-ID****");
        }      
               
    	
    }
	@Override
	public String getHelp() {
		// TODO Auto-generated method stub
		return null;
	}
	
    private void createUserNode(String nodeRef) {
        NodeRef nodeOne = createNodeRef(nodeRef);
        NodeBuilder builder = new NodeBuilder();
        builder.setId(new NodeId(nodeRef));
        builder.setKey(new NodeKey(builder.getId()));
        testNode1 = builder.build();
    }

}
