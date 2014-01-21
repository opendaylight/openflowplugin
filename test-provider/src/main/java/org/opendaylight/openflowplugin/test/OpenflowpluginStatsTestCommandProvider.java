package org.opendaylight.openflowplugin.test;

import java.util.Iterator;
import java.util.List;

import org.eclipse.osgi.framework.console.CommandInterpreter;
import org.eclipse.osgi.framework.console.CommandProvider;
import org.opendaylight.controller.sal.binding.api.BindingAwareBroker.ProviderContext;
import org.opendaylight.controller.sal.binding.api.data.DataProviderService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNode;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNodeConnector;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.meters.Meter;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.meters.MeterKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.Table;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.TableKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.table.Flow;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.table.FlowKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.statistics.rev130819.AggregateFlowStatisticsData;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.statistics.rev130819.FlowStatisticsData;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.statistics.rev130819.GetFlowStatisticsFromFlowTableInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.statistics.rev130819.GetFlowStatisticsFromFlowTableInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.statistics.rev130819.OpendaylightFlowStatisticsService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.table.statistics.rev131215.FlowTableStatisticsData;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.statistics.rev131111.NodeGroupDescStats;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.statistics.rev131111.NodeGroupStatistics;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.statistics.rev131111.group.statistics.GroupStatistics;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.groups.Group;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.groups.GroupKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.Nodes;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.node.NodeConnector;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.node.NodeConnectorKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.NodeKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.statistics.rev131111.NodeMeterConfigStats;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.statistics.rev131111.NodeMeterStatistics;
import org.opendaylight.yang.gen.v1.urn.opendaylight.port.statistics.rev131214.FlowCapableNodeConnectorStatisticsData;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OpenflowpluginStatsTestCommandProvider implements CommandProvider {

	private static final Logger LOG = LoggerFactory.getLogger(OpenflowpluginStatsTestCommandProvider.class);
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
                LOG.debug("portStats - Success");
            }
            else
            {
                LOG.debug("portStats - Failed");
                LOG.debug("System fetchs stats data in 50 seconds interval, so pls wait and try again.");
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
                LOG.debug("portDescStats - Success");
            }
            else
            {
                LOG.debug("portDescStats - Failed");
                LOG.debug("System fetchs stats data in 50 seconds interval, so pls wait and try again.");
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
                            LOG.debug("--------------------------------------------");
                            ci.print(data);
                        }
                    }                    
                }
            }
            
            if(flowCount == flowStatsCount)
            {
                LOG.debug("flowStats - Success");
            }
            else
            {
                LOG.debug("flowStats - Failed");
                LOG.debug("System fetchs stats data in 50 seconds interval, so pls wait and try again.");
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
                LOG.debug("tableStats - Success");
            }
            else
            {
                LOG.debug("tableStats - Failed");
                LOG.debug("System fetchs stats data in 50 seconds interval, so pls wait and try again.");
            }
                           
       }

    public void _groupStats(CommandInterpreter ci) {
        int groupCount = 0;
        int groupStatsCount = 0;
        NodeGroupStatistics data = null;
            List<Node> nodes = getNodes();
            for (Iterator<Node> iterator = nodes.iterator(); iterator.hasNext();) {
                NodeKey nodeKey =  iterator.next().getKey();
                InstanceIdentifier<FlowCapableNode> nodeRef = InstanceIdentifier.builder(Nodes.class).child(Node.class, nodeKey).augmentation(FlowCapableNode.class).toInstance();
                FlowCapableNode node = (FlowCapableNode)dataProviderService.readOperationalData(nodeRef);

               List<Group> groups =  node.getGroup();
                for (Iterator<Group> iterator2 = groups.iterator(); iterator2.hasNext();) {
                    groupCount++;
                    GroupKey groupKey = iterator2.next().getKey();
                    InstanceIdentifier<Group> groupRef = InstanceIdentifier.builder(Nodes.class).child(Node.class, nodeKey).augmentation(FlowCapableNode.class).child(Group.class, groupKey).toInstance();
                    Group group = (Group)dataProviderService.readOperationalData(groupRef);
                    data = group.getAugmentation(NodeGroupStatistics.class);
                    if(null != data)
                    {
                        groupStatsCount++;
                    }
                }
            }
            
            if(groupCount == groupStatsCount)
            {
                LOG.debug("---------------------groupStats - Success-------------------------------");           
            }
            else
            {
                LOG.debug("------------------------------groupStats - Failed--------------------------");
                LOG.debug("System fetchs stats data in 50 seconds interval, so pls wait and try again.");
            }                        
       }
    
    public void _groupDescStats(CommandInterpreter ci) {
        int groupCount = 0;
        int groupDescStatsCount = 0;
        NodeGroupDescStats data = null;
            List<Node> nodes = getNodes();
            for (Iterator<Node> iterator = nodes.iterator(); iterator.hasNext();) {
                NodeKey nodeKey =  iterator.next().getKey();
                InstanceIdentifier<FlowCapableNode> nodeRef = InstanceIdentifier.builder(Nodes.class).child(Node.class, nodeKey).augmentation(FlowCapableNode.class).toInstance();
                FlowCapableNode node = (FlowCapableNode)dataProviderService.readOperationalData(nodeRef);

               List<Group> groups =  node.getGroup();
                for (Iterator<Group> iterator2 = groups.iterator(); iterator2.hasNext();) {
                    groupCount++;
                    GroupKey groupKey = iterator2.next().getKey();
                    InstanceIdentifier<Group> groupRef = InstanceIdentifier.builder(Nodes.class).child(Node.class, nodeKey).augmentation(FlowCapableNode.class).child(Group.class, groupKey).toInstance();
                    Group group = (Group)dataProviderService.readOperationalData(groupRef);
                    data = group.getAugmentation(NodeGroupDescStats.class);
                    if(null != data)
                    {
                        groupDescStatsCount++;
                    }
                }
            }
            
            if(groupCount == groupDescStatsCount)
            {
                LOG.debug("---------------------groupDescStats - Success-------------------------------");
            }
            else
            {
                LOG.debug("------------------------------groupDescStats - Failed--------------------------");
                LOG.debug("System fetchs stats data in 50 seconds interval, so pls wait and try again.");
            }                        
       }
    
    public void _meterStats(CommandInterpreter ci) {
        int meterCount = 0;
        int meterStatsCount = 0;
        NodeMeterStatistics data = null;
            List<Node> nodes = getNodes();
            for (Iterator<Node> iterator = nodes.iterator(); iterator.hasNext();) {
                NodeKey nodeKey =  iterator.next().getKey();
                InstanceIdentifier<FlowCapableNode> nodeRef = InstanceIdentifier.builder(Nodes.class).child(Node.class, nodeKey).augmentation(FlowCapableNode.class).toInstance();
                FlowCapableNode node = (FlowCapableNode)dataProviderService.readOperationalData(nodeRef);

               List<Meter> meters =  node.getMeter();
                for (Iterator<Meter> iterator2 = meters.iterator(); iterator2.hasNext();) {
                    meterCount++;
                    MeterKey meterKey = iterator2.next().getKey();
                    InstanceIdentifier<Meter> meterRef = InstanceIdentifier.builder(Nodes.class).child(Node.class, nodeKey).augmentation(FlowCapableNode.class).child(Meter.class, meterKey).toInstance();
                    Meter meter = (Meter)dataProviderService.readOperationalData(meterRef);
                    data = meter.getAugmentation(NodeMeterStatistics.class);
                    if(null != data)
                    {
                        meterStatsCount++;
                    }
                }
            }
            
            if(meterCount == meterStatsCount)
            {
                LOG.debug("---------------------------meterStats - Success-------------------------------------");
            }
            else
            {
                LOG.debug("----------------------------meterStats - Failed-------------------------------------");
                LOG.debug("System fetchs stats data in 50 seconds interval, so pls wait and try again.");
            }                        
       }
 
    public void _meterConfigStats(CommandInterpreter ci) {
        int meterCount = 0;
        int meterConfigStatsCount = 0;
        NodeMeterConfigStats data = null;
            List<Node> nodes = getNodes();
            for (Iterator<Node> iterator = nodes.iterator(); iterator.hasNext();) {
                NodeKey nodeKey =  iterator.next().getKey();
                InstanceIdentifier<FlowCapableNode> nodeRef = InstanceIdentifier.builder(Nodes.class).child(Node.class, nodeKey).augmentation(FlowCapableNode.class).toInstance();
                FlowCapableNode node = (FlowCapableNode)dataProviderService.readOperationalData(nodeRef);

               List<Meter> meters =  node.getMeter();
                for (Iterator<Meter> iterator2 = meters.iterator(); iterator2.hasNext();) {
                    meterCount++;
                    MeterKey meterKey = iterator2.next().getKey();
                    InstanceIdentifier<Meter> meterRef = InstanceIdentifier.builder(Nodes.class).child(Node.class, nodeKey).augmentation(FlowCapableNode.class).child(Meter.class, meterKey).toInstance();
                    Meter meter = (Meter)dataProviderService.readOperationalData(meterRef);
                    data = meter.getAugmentation(NodeMeterConfigStats.class);
                    if(null != data)
                    {
                        meterConfigStatsCount++;
                    }
                }
            }
            
            if(meterCount == meterConfigStatsCount)
            {
                LOG.debug("---------------------------meterConfigStats - Success-------------------------------------");
                ci.print(data);
            }
            else
            {
                LOG.debug("----------------------------meterConfigStats - Failed-------------------------------------");
                LOG.debug("System fetchs stats data in 50 seconds interval, so pls wait and try again.");
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
                LOG.debug("aggregateStats - Success");
            }
            else
            {
                LOG.debug("aggregateStats - Failed");
                LOG.debug("System fetchs stats data in 50 seconds interval, so pls wait and try again.");
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
                LOG.debug("descStats - Success");
            }
            else
            {
                LOG.debug("descStats - Failed");
                LOG.debug("System fetchs stats data in 50 seconds interval, so pls wait and try again.");
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
