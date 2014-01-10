package org.opendaylight.openflowplugin.test;

import java.util.Iterator;
import java.util.List;

import org.eclipse.osgi.framework.console.CommandInterpreter;
import org.eclipse.osgi.framework.console.CommandProvider;
import org.opendaylight.controller.sal.binding.api.BindingAwareBroker.ProviderContext;
import org.opendaylight.controller.sal.binding.api.data.DataProviderService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNode;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNodeConnector;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.Table;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.TableKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.table.Flow;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.table.FlowKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.statistics.rev130819.AggregateFlowStatisticsData;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.statistics.rev130819.FlowStatisticsData;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.table.statistics.rev131215.FlowTableStatisticsData;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.Nodes;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.node.NodeConnector;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.node.NodeConnectorKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.NodeKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.port.statistics.rev131214.FlowCapableNodeConnectorStatisticsData;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.osgi.framework.BundleContext;

public class OpenflowpluginStatsTestCommandProvider implements CommandProvider {

    private DataProviderService dataProviderService;
    private final BundleContext ctx;

    public OpenflowpluginStatsTestCommandProvider(BundleContext ctx) {
        this.ctx = ctx;
    }
    

    public void onSessionInitiated(ProviderContext session) {
        dataProviderService = session.getSALService(DataProviderService.class);
        ctx.registerService(CommandProvider.class.getName(), this, null);
        
    }

    public void _portStats(CommandInterpreter ci) {
        int nodeConnectorCount = 0;
        int nodeConnectorStatsCount = 0;
            List<Node> nodes = getNodes();
            for (Iterator<Node> iterator = nodes.iterator(); iterator.hasNext();) {
                NodeKey nodeKey =  iterator.next().getKey();
                InstanceIdentifier<Node> nodeRef = InstanceIdentifier.builder(Nodes.class).child(Node.class, nodeKey).toInstance();
                Node node = (Node)dataProviderService.readOperationalData(nodeRef);
                List<NodeConnector> ports =  node.getNodeConnector();
                for (Iterator<NodeConnector> iterator2 = ports.iterator(); iterator2.hasNext();) {
                    nodeConnectorCount++;
                    NodeConnectorKey nodeConnectorKey = iterator2.next().getKey();
                    InstanceIdentifier<NodeConnector> connectorRef = InstanceIdentifier.builder(Nodes.class).child(Node.class, nodeKey).child(NodeConnector.class, nodeConnectorKey).toInstance();
                    NodeConnector nodeConnector = (NodeConnector)dataProviderService.readOperationalData(connectorRef);
                    FlowCapableNodeConnectorStatisticsData data = nodeConnector.getAugmentation(FlowCapableNodeConnectorStatisticsData.class);
                    if(null != data)
                    {
                        nodeConnectorStatsCount++;
                    }
                }
            }
            
            if(nodeConnectorCount == nodeConnectorStatsCount)
            {
                ci.println("portStats - Success");
            }
            else
            {
                ci.println("portStats - Failed");
                ci.println("System fetchs stats data in 50 seconds interval, so pls wait and try again.");
            }
                           
       }

    
    public void _portDescStats(CommandInterpreter ci) {
        int nodeConnectorCount = 0;
        int nodeConnectorDescStatsCount = 0;
            List<Node> nodes = getNodes();
            for (Iterator<Node> iterator = nodes.iterator(); iterator.hasNext();) {
                NodeKey nodeKey =  iterator.next().getKey();
                InstanceIdentifier<Node> nodeRef = InstanceIdentifier.builder(Nodes.class).child(Node.class, nodeKey).toInstance();
                Node node = (Node)dataProviderService.readOperationalData(nodeRef);

               List<NodeConnector> ports =  node.getNodeConnector();
                for (Iterator<NodeConnector> iterator2 = ports.iterator(); iterator2.hasNext();) {
                    nodeConnectorCount++;
                    NodeConnectorKey nodeConnectorKey = iterator2.next().getKey();
                    InstanceIdentifier<FlowCapableNodeConnector> connectorRef = InstanceIdentifier.builder(Nodes.class).child(Node.class, nodeKey).child(NodeConnector.class, nodeConnectorKey).augmentation(FlowCapableNodeConnector.class).toInstance();
                    FlowCapableNodeConnector nodeConnector = (FlowCapableNodeConnector)dataProviderService.readOperationalData(connectorRef);
                    if(null != nodeConnector.getName() &&
                            null != nodeConnector.getCurrentFeature() &&
                            null != nodeConnector.getState() &&
                            null != nodeConnector.getHardwareAddress() &&
                            null != nodeConnector.getPortNumber())
                    {
                        nodeConnectorDescStatsCount++;
                    }
                }
            }
            
            if(nodeConnectorCount == nodeConnectorDescStatsCount)
            {
                ci.println("portDescStats - Success");
            }
            else
            {
                ci.println("portDescStats - Failed");
                ci.println("System fetchs stats data in 50 seconds interval, so pls wait and try again.");
            }
                           
       }

    public void _flowStats(CommandInterpreter ci) {
        int flowCount = 0;
        int flowStatsCount = 0;
            List<Node> nodes = getNodes();
            for (Iterator<Node> iterator = nodes.iterator(); iterator.hasNext();) {
                NodeKey nodeKey =  iterator.next().getKey();
                InstanceIdentifier<FlowCapableNode> nodeRef = InstanceIdentifier.builder(Nodes.class).child(Node.class, nodeKey).augmentation(FlowCapableNode.class).toInstance();
                FlowCapableNode node = (FlowCapableNode)dataProviderService.readOperationalData(nodeRef);
                List<Table> tables =  node.getTable();
                for (Iterator<Table> iterator2 = tables.iterator(); iterator2.hasNext();) {
                    TableKey tableKey = iterator2.next().getKey();
                    InstanceIdentifier<Table> tableRef = InstanceIdentifier.builder(Nodes.class).child(Node.class, nodeKey).augmentation(FlowCapableNode.class).child(Table.class, tableKey).toInstance();
                    Table table = (Table)dataProviderService.readOperationalData(tableRef);
                    List<Flow> flows = table.getFlow();
                    for (Iterator<Flow> iterator3 = flows.iterator(); iterator3.hasNext();) {
                        flowCount++;
                        FlowKey flowKey =  iterator3.next().getKey();
                        InstanceIdentifier<Flow> flowRef = InstanceIdentifier.builder(Nodes.class).child(Node.class, nodeKey).augmentation(FlowCapableNode.class).child(Table.class, tableKey).child(Flow.class, flowKey).toInstance();
                        Flow flow = (Flow)dataProviderService.readOperationalData(flowRef);
                        FlowStatisticsData data = flow.getAugmentation(FlowStatisticsData.class);
                        if(null != data)
                        {
                            flowStatsCount++;
                        }
                    }                    
                }
            }
            
            if(flowCount == flowStatsCount)
            {
                ci.println("flowStats - Success");
            }
            else
            {
                ci.println("flowStats - Failed");
                ci.println("System fetchs stats data in 50 seconds interval, so pls wait and try again.");
            }
                           
       }


    public void _tableStats(CommandInterpreter ci) {
        int tableCount = 0;
        int tableStatsCount = 0;
            List<Node> nodes = getNodes();
            for (Iterator<Node> iterator = nodes.iterator(); iterator.hasNext();) {
                NodeKey nodeKey =  iterator.next().getKey();
                InstanceIdentifier<FlowCapableNode> nodeRef = InstanceIdentifier.builder(Nodes.class).child(Node.class, nodeKey).augmentation(FlowCapableNode.class).toInstance();
                FlowCapableNode node = (FlowCapableNode)dataProviderService.readOperationalData(nodeRef);

               List<Table> tables =  node.getTable();
                for (Iterator<Table> iterator2 = tables.iterator(); iterator2.hasNext();) {
                    tableCount++;
                    TableKey tableKey = iterator2.next().getKey();
                    InstanceIdentifier<Table> tableRef = InstanceIdentifier.builder(Nodes.class).child(Node.class, nodeKey).augmentation(FlowCapableNode.class).child(Table.class, tableKey).toInstance();
                    Table table = (Table)dataProviderService.readOperationalData(tableRef);
                    FlowTableStatisticsData data = table.getAugmentation(FlowTableStatisticsData.class);
                    if(null != data)
                    {
                        tableStatsCount++;
                    }
                }
            }
            
            if(tableCount == tableStatsCount)
            {
                ci.println("tableStats - Success");
            }
            else
            {
                ci.println("tableStats - Failed");
                ci.println("System fetchs stats data in 50 seconds interval, so pls wait and try again.");
            }
                           
       }

    public void _aggregateStats(CommandInterpreter ci) {
        int aggregateFlowCount = 0;
        int aggerateFlowStatsCount = 0;
            List<Node> nodes = getNodes();
            for (Iterator<Node> iterator = nodes.iterator(); iterator.hasNext();) {
                NodeKey nodeKey =  iterator.next().getKey();
                InstanceIdentifier<FlowCapableNode> nodeRef = InstanceIdentifier.builder(Nodes.class).child(Node.class, nodeKey).augmentation(FlowCapableNode.class).toInstance();
                FlowCapableNode node = (FlowCapableNode)dataProviderService.readOperationalData(nodeRef);

               List<Table> tables =  node.getTable();
                for (Iterator<Table> iterator2 = tables.iterator(); iterator2.hasNext();) {
                    aggregateFlowCount++;
                    TableKey tableKey = iterator2.next().getKey();
                    InstanceIdentifier<Table> tableRef = InstanceIdentifier.builder(Nodes.class).child(Node.class, nodeKey).augmentation(FlowCapableNode.class).child(Table.class, tableKey).toInstance();
                    Table table = (Table)dataProviderService.readOperationalData(tableRef);
                    AggregateFlowStatisticsData data = table.getAugmentation(AggregateFlowStatisticsData.class);
                    if(null != data)
                    {
                        aggerateFlowStatsCount++;
                    }
                }
            }
            
            if(aggregateFlowCount == aggerateFlowStatsCount)
            {
                ci.println("aggregateStats - Success");
            }
            else
            {
                ci.println("aggregateStats - Failed");
                ci.println("System fetchs stats data in 50 seconds interval, so pls wait and try again.");
            }
                           
       }

    public void _descStats(CommandInterpreter ci) {
        int descCount = 0;
        int descStatsCount = 0;
            List<Node> nodes = getNodes();
            for (Iterator<Node> iterator = nodes.iterator(); iterator.hasNext();) {
                descCount++;
                NodeKey nodeKey =  iterator.next().getKey();
                InstanceIdentifier<FlowCapableNode> nodeRef = InstanceIdentifier.builder(Nodes.class).child(Node.class, nodeKey).augmentation(FlowCapableNode.class).toInstance();
                FlowCapableNode node = (FlowCapableNode)dataProviderService.readOperationalData(nodeRef);
                    if(null != node.getHardware() &&
                            null != node.getManufacturer() &&
                            null != node.getSoftware())
                    {
                        descStatsCount++;
                    }
            }
            
            if(descCount == descStatsCount)
            {
                ci.println("descStats - Success");
            }
            else
            {
                ci.println("descStats - Failed");
                ci.println("System fetchs stats data in 50 seconds interval, so pls wait and try again.");
            }
                           
       }    
    
    private List<Node> getNodes()
    {
        Nodes nodes = (Nodes)dataProviderService.readOperationalData(InstanceIdentifier.builder(Nodes.class).toInstance());
        if(null == nodes)
        {
            throw new RuntimeException("nodes are not found, pls add the node.");
        }
        return nodes.getNode();
        
    }
    
 
    @Override
    public String getHelp() {
        StringBuffer help = new StringBuffer();
        help.append("---MD-SAL Stats test module---\n");
        return help.toString();
    }

 
}
