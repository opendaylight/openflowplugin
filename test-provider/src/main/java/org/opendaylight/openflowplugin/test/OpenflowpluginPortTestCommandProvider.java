package org.opendaylight.openflowplugin.test;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.eclipse.osgi.framework.console.CommandInterpreter;
import org.eclipse.osgi.framework.console.CommandProvider;
import org.opendaylight.controller.md.sal.common.api.TransactionStatus;
import org.opendaylight.controller.md.sal.common.api.data.DataModification;
import org.opendaylight.controller.sal.binding.api.BindingAwareBroker.ProviderContext;
import org.opendaylight.controller.sal.binding.api.data.DataBrokerService;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev100924.MacAddress;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNode;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNodeConnector;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.Table;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.table.Flow;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.table.FlowBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.port.rev130925.CommonPort;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.port.rev130925.PortConfig;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.port.rev130925.PortFeatures;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.port.rev130925.port.mod.Port;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.port.rev130925.port.mod.PortBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.port.rev130925.port.mod.PortKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.groups.Group;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.groups.GroupKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeConnectorId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeConnectorRef;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeRef;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.Nodes;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.node.NodeConnector;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.node.NodeConnectorBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.node.NodeConnectorKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.NodeBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.NodeKey;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OpenflowpluginPortTestCommandProvider implements CommandProvider {

    private static final Logger LOG = LoggerFactory.getLogger(OpenflowpluginPortTestCommandProvider.class);
    
    private DataBrokerService dataBrokerService;
    private ProviderContext pc;
    private final BundleContext ctx;
    private Node testNode;
    private NodeConnector testNodeConnector;

    public OpenflowpluginPortTestCommandProvider(BundleContext ctx) {
        this.ctx = ctx;
    }

    public void onSessionInitiated(ProviderContext session) {
        pc = session;
        dataBrokerService = session.getSALService(DataBrokerService.class);
        ctx.registerService(CommandProvider.class.getName(), this, null);
        createTestNode();
    }

    private void createUserNode(String nodeRef) {
        NodeRef nodeOne = createNodeRef(nodeRef);
        NodeBuilder builder = new NodeBuilder();
        builder.setId(new NodeId(nodeRef));
        builder.setKey(new NodeKey(builder.getId()));
        testNode = builder.build();
    }

    private void createTestNode() {
        NodeRef nodeOne = createNodeRef(OpenflowpluginTestActivator.NODE_ID);
        NodeBuilder builder = new NodeBuilder();
        builder.setId(new NodeId(OpenflowpluginTestActivator.NODE_ID));
        builder.setKey(new NodeKey(builder.getId()));
        testNode = builder.build();
    }
    /*
    private void createUserNodeConnector(String nodeConnectorRef) {
      //  NodeConnectorRef nodeOne = createNodeConnectorRef(nodeConnectorRef);
        NodeConnectorBuilder builder = new NodeConnectorBuilder();
        builder.setId(new NodeConnectorId(nodeConnectorRef));
        builder.setKey(new NodeConnectorKey(builder.getId()));
        testNodeConnector = builder.build();
    }

    private void createTestNodeConnector(PortBuilder port) {
      //  NodeConnectorRef nodeOne = createNodeConnectorRef(OpenflowpluginTestActivator.NODE_ID);
        NodeConnectorBuilder builder = new NodeConnectorBuilder();
        builder.setId(new NodeConnectorId(OpenflowpluginTestActivator.NODE_ID));
        builder.setKey(new NodeConnectorKey(builder.getId()+":"+port.get));
        testNodeConnector = builder.build();
    }
*/
    private InstanceIdentifier<Node> nodeToInstanceId(Node node) {
        return InstanceIdentifier.builder(Nodes.class).child(Node.class, node.getKey()).toInstance();
    }

    
    private PortBuilder createTestPortUpdate(String port, String HwAddress, String caseType) {
    	
    	PortBuilder portBuilder = new PortBuilder();
    	
    	NodeConnectorId nodeConnectorId = new NodeConnectorId(testNode.getKey()+":"+port);
    	
    	
    	if(caseType == null) {
    	    caseType = "p1";
    	}
    	
    	switch(caseType) {
    	
    	case "p1":
    	    PortConfig config = new PortConfig(true,true,true,false);
    	    portBuilder.setConfiguration(config);          //0x64
    	    portBuilder.setMask(config);             //0x64
    	    break;
    	case "p2":
    	    PortConfig config1 = new PortConfig(false,true,false,false);
    	    portBuilder.setConfiguration(config1);
    	    portBuilder.setMask(config1);
    	    break;
    	default : 
    	    LOG.warn("port case not understood: {}", caseType);
    	}
    	portBuilder.setPortModOrder(nodeConnectorId);
    	portBuilder.setPortNumber(new Long(port));
    	portBuilder.setHardwareAddress(new MacAddress(HwAddress));
    	portBuilder.setPortName("dp1.3");
    	PortKey portKey = new PortKey(nodeConnectorId); 
    	portBuilder.setKey(portKey);
    	
    	
    	PortFeatures features = new PortFeatures(true, true, true, true, false, true, false, true, false, true, false, true, false, true, false, true);
    	portBuilder.setAdvertisedFeatures(features);
    
    	return portBuilder;
    }

    private void writeGroup(CommandInterpreter ci, PortBuilder port) {
        DataModification<InstanceIdentifier<?>, DataObject> modification = dataBrokerService.beginTransaction();
        InstanceIdentifier<Port> path1 = InstanceIdentifier.builder(Nodes.class)
                .child(Node.class, testNode.getKey()).augmentation(FlowCapableNode.class)
                .child(Port.class, port.getKey()).toInstance();
                
        modification.putOperationalData(nodeToInstanceId(testNode), testNode);
        modification.putOperationalData(path1, port.build());
        modification.putConfigurationData(nodeToInstanceId(testNode), testNode);
        modification.putConfigurationData(path1, port.build());
        Future<RpcResult<TransactionStatus>> commitFuture = modification.commit();
        try {
            RpcResult<TransactionStatus> result = commitFuture.get();
            TransactionStatus status = result.getResult();
            ci.println("Status of Port Data Loaded Transaction: " + status);

        } catch (InterruptedException e) {
        	 e.printStackTrace();
        } catch (ExecutionException e) {
        	 e.printStackTrace();
        }
    }
    

    public void _updatePort(CommandInterpreter ci) {
        String nref = ci.nextArgument();
       
        PortBuilder port = createTestPortUpdate(ci.nextArgument(),ci.nextArgument(), ci.nextArgument());
        if (nref == null) {
            ci.println("test node added");
            createTestNode();
           // createTestNodeConnector(port);
        } else {
            ci.println("User node added" + nref);
            createUserNode(nref);
          //  createUserNodeConnector(nref, port);
        }
      
       
       writeGroup(ci, port);
    }

    @Override
    public String getHelp() {
        StringBuffer help = new StringBuffer();
        help.append("---FRM MD-SAL Port test module---\n");
        help.append("\t modifyGroup <node id>        - node ref\n");

        return help.toString();
    }

    private static NodeRef createNodeRef(String string) {
        NodeKey key = new NodeKey(new NodeId(string));
        InstanceIdentifier<Node> path = InstanceIdentifier.builder(Nodes.class).child(Node.class, key).toInstance();

        return new NodeRef(path);
    }

    
    private static void removeMeImFaick() {

    }
}