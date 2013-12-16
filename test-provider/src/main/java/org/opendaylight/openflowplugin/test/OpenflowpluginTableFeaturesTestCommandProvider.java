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
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNode;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeRef;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.Nodes;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.NodeBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.NodeKey;

import org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table.features.TableFeatures;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.TableBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.Table;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.TableKey;

import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.osgi.framework.BundleContext;


public class OpenflowpluginTableFeaturesTestCommandProvider implements CommandProvider {

    private DataBrokerService dataBrokerService;
    private ProviderContext pc;
    private final BundleContext ctx;
    private Table testTable;
    private Node testNode;
    private final String originalTableName = "Foo";
    private final String updatedTableName = "Bar";

    public OpenflowpluginTableFeaturesTestCommandProvider(BundleContext ctx) {
        this.ctx = ctx;
    }

    public void onSessionInitiated(ProviderContext session) {
        pc = session;
        dataBrokerService = session.getSALService(DataBrokerService.class);
        ctx.registerService(CommandProvider.class.getName(), this, null);
        createTestNode();
        createTestTable();
    }

    private void createUserNode(String nodeRef) {        
        NodeBuilder builder = new NodeBuilder();
        builder.setId(new NodeId(nodeRef));
        builder.setKey(new NodeKey(builder.getId()));
        testNode = builder.build();
    }
    
    private void createTestNode() {       
        NodeBuilder builder = new NodeBuilder();
        builder.setId(new NodeId(OpenflowpluginTestActivator.NODE_ID));
        builder.setKey(new NodeKey(builder.getId()));
        testNode = builder.build();
    }

    private InstanceIdentifier<Node> nodeToInstanceId(Node node) {
        return InstanceIdentifier.builder(Nodes.class).child(Node.class, node.getKey()).toInstance();
    }

    private TableBuilder createTestTable() {
        // Sample data , committing to DataStore
        DataModification modification = dataBrokerService.beginTransaction();
        short id = 12;
        TableKey key = new TableKey(id) ;
        
        TableBuilder table = new TableBuilder();
        
        table.setId(id) ;
        table.setKey(key) ;
 
              
        // Send empty table features
        List<TableFeatures> features = new ArrayList<TableFeatures>() ;
        table.setTableFeatures(features) ;
                
        testTable = table.build();
        return table;
    }

    
    private void writeTable(CommandInterpreter ci, Table table) {
        DataModification modification = dataBrokerService.beginTransaction();
               
        InstanceIdentifier<Table> path1 = InstanceIdentifier.builder(Nodes.class)
                .child(Node.class, testNode.getKey()).augmentation(FlowCapableNode.class).child(Table.class, new TableKey(table.getId())).build() ;
                
                        
        modification.putOperationalData(nodeToInstanceId(testNode), testNode);
        modification.putOperationalData(path1, table);
        modification.putConfigurationData(nodeToInstanceId(testNode), testNode);
        modification.putConfigurationData(path1, table);
        Future<RpcResult<TransactionStatus>> commitFuture = modification.commit();
        try {
            RpcResult<TransactionStatus> result = commitFuture.get();
            TransactionStatus status = result.getResult();
            ci.println("Status of Table Data Loaded Transaction: " + status);

        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (ExecutionException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public void _modifyTable(CommandInterpreter ci) {
        String nref = ci.nextArgument();
        ci.println( " Table Command Provider modify" ) ;
        
        if (nref == null) {
            ci.println("test node added");
            createTestNode();
        } else {
            ci.println("User node added" + nref);
            createUserNode(nref);
        }
        TableBuilder table = createTestTable(); 
           
        writeTable(ci, table.build());
    }
    
    @Override
    public String getHelp() {
        StringBuffer help = new StringBuffer();
        help.append("---FRM MD-SAL Table test module---\n");
        help.append("\t modifyTable <node id>        - node ref\n");
               
        return help.toString();
    }    
   
}

