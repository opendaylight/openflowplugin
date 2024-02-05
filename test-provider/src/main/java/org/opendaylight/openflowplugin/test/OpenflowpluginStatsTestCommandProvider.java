/*
 * Copyright (c) 2014, 2015 IBM Corporation and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.test;

import static java.util.Objects.requireNonNull;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.util.Collection;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.eclipse.osgi.framework.console.CommandInterpreter;
import org.eclipse.osgi.framework.console.CommandProvider;
import org.opendaylight.mdsal.binding.api.DataBroker;
import org.opendaylight.mdsal.binding.api.ReadTransaction;
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
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.table.statistics.rev131215.FlowTableStatisticsData;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.statistics.rev131111.NodeGroupDescStats;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.statistics.rev131111.NodeGroupStatistics;
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
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
@Component(service = CommandProvider.class, immediate = true)
@SuppressWarnings("checkstyle:MethodName")
public final class OpenflowpluginStatsTestCommandProvider implements CommandProvider {
    private static final Logger LOG = LoggerFactory.getLogger(OpenflowpluginStatsTestCommandProvider.class);

    private final DataBroker dataBroker;

    @Inject
    @Activate
    public OpenflowpluginStatsTestCommandProvider(@Reference final DataBroker dataBroker) {
        this.dataBroker = requireNonNull(dataBroker);
    }

    public void _portStats(final CommandInterpreter ci) {
        int nodeConnectorCount = 0;
        int nodeConnectorStatsCount = 0;
        Collection<Node> nodes = getNodes();
        for (Node node2 : nodes) {
            NodeKey nodeKey = node2.key();
            InstanceIdentifier<Node> nodeRef = InstanceIdentifier.create(Nodes.class).child(Node.class, nodeKey);
            ReadTransaction readOnlyTransaction = dataBroker.newReadOnlyTransaction();
            Node node = TestProviderTransactionUtil.getDataObject(readOnlyTransaction, nodeRef);
            if (node != null) {
                if (node.getNodeConnector() != null) {
                    Collection<NodeConnector> ports = node.nonnullNodeConnector().values();

                    for (NodeConnector nodeConnector2 : ports) {
                        nodeConnectorCount++;
                        NodeConnectorKey nodeConnectorKey = nodeConnector2.key();
                        InstanceIdentifier<NodeConnector> connectorRef = InstanceIdentifier.create(Nodes.class)
                                .child(Node.class, nodeKey).child(NodeConnector.class, nodeConnectorKey);
                        NodeConnector nodeConnector = TestProviderTransactionUtil.getDataObject(readOnlyTransaction,
                                connectorRef);
                        if (nodeConnector != null) {
                            FlowCapableNodeConnectorStatisticsData data = nodeConnector
                                    .augmentation(FlowCapableNodeConnectorStatisticsData.class);
                            if (null != data) {
                                nodeConnectorStatsCount++;
                            }
                        }
                    }
                }
            }
        }

        if (nodeConnectorCount == nodeConnectorStatsCount) {
            LOG.debug("portStats - Success");
        } else {
            LOG.debug("portStats - Failed");
            LOG.debug("System fetchs stats data in 50 seconds interval, so pls wait and try again.");
        }

    }

    public void _portDescStats(final CommandInterpreter ci) {
        int nodeConnectorCount = 0;
        int nodeConnectorDescStatsCount = 0;
        Collection<Node> nodes = getNodes();
        for (Node node2 : nodes) {
            NodeKey nodeKey = node2.key();
            InstanceIdentifier<Node> nodeRef = InstanceIdentifier.create(Nodes.class).child(Node.class, nodeKey);

            ReadTransaction readOnlyTransaction = dataBroker.newReadOnlyTransaction();
            Node node = TestProviderTransactionUtil.getDataObject(readOnlyTransaction, nodeRef);
            if (node != null) {
                if (node.getNodeConnector() != null) {
                    Collection<NodeConnector> ports = node.nonnullNodeConnector().values();
                    for (NodeConnector nodeConnector2 : ports) {
                        nodeConnectorCount++;
                        NodeConnectorKey nodeConnectorKey = nodeConnector2.key();
                        InstanceIdentifier<FlowCapableNodeConnector> connectorRef = InstanceIdentifier
                                .create(Nodes.class).child(Node.class, nodeKey)
                                .child(NodeConnector.class, nodeConnectorKey)
                                .augmentation(FlowCapableNodeConnector.class);
                        FlowCapableNodeConnector nodeConnector = TestProviderTransactionUtil
                                .getDataObject(readOnlyTransaction, connectorRef);
                        if (nodeConnector != null) {
                            if (null != nodeConnector.getName() && null != nodeConnector.getCurrentFeature()
                                    && null != nodeConnector.getState() && null != nodeConnector.getHardwareAddress()
                                    && null != nodeConnector.getPortNumber()) {
                                nodeConnectorDescStatsCount++;
                            }
                        }
                    }
                }

            }
        }

        if (nodeConnectorCount == nodeConnectorDescStatsCount) {
            LOG.debug("portDescStats - Success");
        } else {
            LOG.debug("portDescStats - Failed");
            LOG.debug("System fetchs stats data in 50 seconds interval, so pls wait and try again.");
        }

    }

    @SuppressFBWarnings("SLF4J_SIGN_ONLY_FORMAT")
    public void _flowStats(final CommandInterpreter ci) {
        int flowCount = 0;
        int flowStatsCount = 0;
        Collection<Node> nodes = getNodes();
        for (Node node2 : nodes) {
            NodeKey nodeKey = node2.key();
            InstanceIdentifier<FlowCapableNode> nodeRef = InstanceIdentifier.create(Nodes.class)
                    .child(Node.class, nodeKey).augmentation(FlowCapableNode.class);

            ReadTransaction readOnlyTransaction = dataBroker.newReadOnlyTransaction();
            FlowCapableNode node = TestProviderTransactionUtil.getDataObject(readOnlyTransaction, nodeRef);

            if (node != null) {
                Collection<Table> tables = node.nonnullTable().values();
                for (Table table2 : tables) {
                    TableKey tableKey = table2.key();
                    InstanceIdentifier<Table> tableRef = InstanceIdentifier.create(Nodes.class)
                            .child(Node.class, nodeKey).augmentation(FlowCapableNode.class)
                            .child(Table.class, tableKey);
                    Table table = TestProviderTransactionUtil.getDataObject(readOnlyTransaction, tableRef);
                    if (table != null) {
                        if (table.getFlow() != null) {
                            Collection<Flow> flows = table.nonnullFlow().values();
                            for (Flow flow2 : flows) {
                                flowCount++;
                                FlowKey flowKey = flow2.key();
                                InstanceIdentifier<Flow> flowRef = InstanceIdentifier.create(Nodes.class)
                                        .child(Node.class, nodeKey).augmentation(FlowCapableNode.class)
                                        .child(Table.class, tableKey).child(Flow.class, flowKey);
                                Flow flow = TestProviderTransactionUtil.getDataObject(readOnlyTransaction, flowRef);
                                if (flow != null) {
                                    FlowStatisticsData data = flow.augmentation(FlowStatisticsData.class);
                                    if (null != data) {
                                        flowStatsCount++;
                                        LOG.debug("--------------------------------------------");
                                        ci.print(data);
                                    }
                                }

                            }
                        }
                    }
                }
            }
        }

        if (flowCount == flowStatsCount) {
            LOG.debug("flowStats - Success");
        } else {
            LOG.debug("flowStats - Failed");
            LOG.debug("System fetchs stats data in 50 seconds interval, so pls wait and try again.");
        }

    }

    public void _tableStats(final CommandInterpreter ci) {
        int tableCount = 0;
        int tableStatsCount = 0;
        Collection<Node> nodes = getNodes();
        for (Node node2 : nodes) {
            NodeKey nodeKey = node2.key();
            InstanceIdentifier<FlowCapableNode> nodeRef = InstanceIdentifier.create(Nodes.class)
                    .child(Node.class, nodeKey).augmentation(FlowCapableNode.class);

            ReadTransaction readOnlyTransaction = dataBroker.newReadOnlyTransaction();
            FlowCapableNode node = TestProviderTransactionUtil.getDataObject(readOnlyTransaction, nodeRef);
            if (node != null) {
                Collection<Table> tables = node.nonnullTable().values();
                for (Table table2 : tables) {
                    tableCount++;
                    TableKey tableKey = table2.key();
                    InstanceIdentifier<Table> tableRef = InstanceIdentifier.create(Nodes.class)
                            .child(Node.class, nodeKey).augmentation(FlowCapableNode.class)
                            .child(Table.class, tableKey);
                    Table table = TestProviderTransactionUtil.getDataObject(readOnlyTransaction, tableRef);
                    if (table != null) {
                        FlowTableStatisticsData data = table.augmentation(FlowTableStatisticsData.class);
                        if (null != data) {
                            tableStatsCount++;
                        }
                    }
                }
            }
        }

        if (tableCount == tableStatsCount) {
            LOG.debug("tableStats - Success");
        } else {
            LOG.debug("tableStats - Failed");
            LOG.debug("System fetchs stats data in 50 seconds interval, so pls wait and try again.");
        }

    }

    public void _groupStats(final CommandInterpreter ci) {
        int groupCount = 0;
        int groupStatsCount = 0;
        NodeGroupStatistics data = null;
        Collection<Node> nodes = getNodes();
        for (Node node2 : nodes) {
            NodeKey nodeKey = node2.key();
            InstanceIdentifier<FlowCapableNode> nodeRef = InstanceIdentifier.create(Nodes.class)
                    .child(Node.class, nodeKey).augmentation(FlowCapableNode.class);
            ReadTransaction readOnlyTransaction = dataBroker.newReadOnlyTransaction();
            FlowCapableNode node = TestProviderTransactionUtil.getDataObject(readOnlyTransaction, nodeRef);
            if (node != null) {
                if (node.getGroup() != null) {
                    Collection<Group> groups = node.nonnullGroup().values();
                    for (Group group2 : groups) {
                        groupCount++;
                        GroupKey groupKey = group2.key();
                        InstanceIdentifier<Group> groupRef = InstanceIdentifier.create(Nodes.class)
                                .child(Node.class, nodeKey).augmentation(FlowCapableNode.class)
                                .child(Group.class, groupKey);
                        Group group = TestProviderTransactionUtil.getDataObject(readOnlyTransaction, groupRef);
                        if (group != null) {
                            data = group.augmentation(NodeGroupStatistics.class);
                            if (null != data) {
                                groupStatsCount++;
                            }
                        }
                    }
                }

            }
        }

        if (groupCount == groupStatsCount) {
            LOG.debug("---------------------groupStats - Success-------------------------------");
        } else {
            LOG.debug("------------------------------groupStats - Failed--------------------------");
            LOG.debug("System fetchs stats data in 50 seconds interval, so pls wait and try again.");
        }
    }

    public void _groupDescStats(final CommandInterpreter ci) {
        int groupCount = 0;
        int groupDescStatsCount = 0;
        NodeGroupDescStats data = null;
        Collection<Node> nodes = getNodes();
        for (Node node2 : nodes) {
            NodeKey nodeKey = node2.key();
            InstanceIdentifier<FlowCapableNode> nodeRef = InstanceIdentifier.create(Nodes.class)
                    .child(Node.class, nodeKey).augmentation(FlowCapableNode.class);
            ReadTransaction readOnlyTransaction = dataBroker.newReadOnlyTransaction();
            FlowCapableNode node = TestProviderTransactionUtil.getDataObject(readOnlyTransaction, nodeRef);

            if (node != null) {
                if (node.getGroup() != null) {
                    Collection<Group> groups = node.nonnullGroup().values();
                    for (Group group2 : groups) {
                        groupCount++;
                        GroupKey groupKey = group2.key();
                        InstanceIdentifier<Group> groupRef = InstanceIdentifier.create(Nodes.class)
                                .child(Node.class, nodeKey).augmentation(FlowCapableNode.class)
                                .child(Group.class, groupKey);
                        Group group = TestProviderTransactionUtil.getDataObject(readOnlyTransaction, groupRef);
                        if (group != null) {
                            data = group.augmentation(NodeGroupDescStats.class);
                            if (null != data) {
                                groupDescStatsCount++;
                            }
                        }

                    }
                }
            }

            if (groupCount == groupDescStatsCount) {
                LOG.debug("---------------------groupDescStats - Success-------------------------------");
            } else {
                LOG.debug("------------------------------groupDescStats - Failed--------------------------");
                LOG.debug("System fetchs stats data in 50 seconds interval, so pls wait and try again.");
            }
        }
    }

    public void _meterStats(final CommandInterpreter ci) {
        int meterCount = 0;
        int meterStatsCount = 0;
        NodeMeterStatistics data = null;
        Collection<Node> nodes = getNodes();
        for (Node node2 : nodes) {
            NodeKey nodeKey = node2.key();
            InstanceIdentifier<FlowCapableNode> nodeRef = InstanceIdentifier.create(Nodes.class)
                    .child(Node.class, nodeKey).augmentation(FlowCapableNode.class);
            ReadTransaction readOnlyTransaction = dataBroker.newReadOnlyTransaction();
            FlowCapableNode node = TestProviderTransactionUtil.getDataObject(readOnlyTransaction, nodeRef);
            if (node != null) {
                if (node.getMeter() != null) {
                    Collection<Meter> meters = node.nonnullMeter().values();
                    for (Meter meter2 : meters) {
                        meterCount++;
                        MeterKey meterKey = meter2.key();
                        InstanceIdentifier<Meter> meterRef = InstanceIdentifier.create(Nodes.class)
                                .child(Node.class, nodeKey).augmentation(FlowCapableNode.class)
                                .child(Meter.class, meterKey);
                        Meter meter = TestProviderTransactionUtil.getDataObject(readOnlyTransaction, meterRef);
                        if (meter != null) {
                            data = meter.augmentation(NodeMeterStatistics.class);
                            if (null != data) {
                                meterStatsCount++;
                            }
                        }
                    }

                }
            }
        }

        if (meterCount == meterStatsCount) {
            LOG.debug("---------------------------meterStats - Success-------------------------------------");
        } else {
            LOG.debug("----------------------------meterStats - Failed-------------------------------------");
            LOG.debug("System fetchs stats data in 50 seconds interval, so pls wait and try again.");
        }
    }

    public void _meterConfigStats(final CommandInterpreter ci) {
        int meterCount = 0;
        int meterConfigStatsCount = 0;
        NodeMeterConfigStats data = null;
        Collection<Node> nodes = getNodes();
        for (Node node2 : nodes) {
            NodeKey nodeKey = node2.key();
            InstanceIdentifier<FlowCapableNode> nodeRef = InstanceIdentifier.create(Nodes.class)
                    .child(Node.class, nodeKey).augmentation(FlowCapableNode.class);
            ReadTransaction readOnlyTransaction = dataBroker.newReadOnlyTransaction();
            FlowCapableNode node = TestProviderTransactionUtil.getDataObject(readOnlyTransaction, nodeRef);
            if (node != null) {
                if (node.getMeter() != null) {
                    Collection<Meter> meters = node.nonnullMeter().values();
                    for (Meter meter2 : meters) {
                        meterCount++;
                        MeterKey meterKey = meter2.key();
                        InstanceIdentifier<Meter> meterRef = InstanceIdentifier.create(Nodes.class)
                                .child(Node.class, nodeKey).augmentation(FlowCapableNode.class)
                                .child(Meter.class, meterKey);
                        Meter meter = TestProviderTransactionUtil.getDataObject(readOnlyTransaction, meterRef);
                        if (meter != null) {
                            data = meter.augmentation(NodeMeterConfigStats.class);
                            if (null != data) {
                                meterConfigStatsCount++;
                            }
                        }
                    }

                }
            }
        }

        if (meterCount == meterConfigStatsCount) {
            LOG.debug("---------------------------meterConfigStats - Success-------------------------------------");
            ci.print(data);
        } else {
            LOG.debug("----------------------------meterConfigStats - Failed-------------------------------------");
            LOG.debug("System fetchs stats data in 50 seconds interval, so pls wait and try again.");
        }
    }

    public void _aggregateStats(final CommandInterpreter ci) {
        int aggregateFlowCount = 0;
        int aggerateFlowStatsCount = 0;
        Collection<Node> nodes = getNodes();
        for (Node node2 : nodes) {
            NodeKey nodeKey = node2.key();
            InstanceIdentifier<FlowCapableNode> nodeRef = InstanceIdentifier.create(Nodes.class)
                    .child(Node.class, nodeKey).augmentation(FlowCapableNode.class);
            ReadTransaction readOnlyTransaction = dataBroker.newReadOnlyTransaction();
            FlowCapableNode node = TestProviderTransactionUtil.getDataObject(readOnlyTransaction, nodeRef);
            if (node != null) {
                Collection<Table> tables = node.nonnullTable().values();
                for (Table table2 : tables) {
                    aggregateFlowCount++;
                    TableKey tableKey = table2.key();
                    InstanceIdentifier<Table> tableRef = InstanceIdentifier.create(Nodes.class)
                            .child(Node.class, nodeKey).augmentation(FlowCapableNode.class)
                            .child(Table.class, tableKey);
                    Table table = TestProviderTransactionUtil.getDataObject(readOnlyTransaction, tableRef);
                    if (table != null) {
                        AggregateFlowStatisticsData data = table.augmentation(AggregateFlowStatisticsData.class);
                        if (null != data) {
                            aggerateFlowStatsCount++;
                        }
                    }
                }
            }
        }

        if (aggregateFlowCount == aggerateFlowStatsCount) {
            LOG.debug("aggregateStats - Success");
        } else {
            LOG.debug("aggregateStats - Failed");
            LOG.debug("System fetchs stats data in 50 seconds interval, so pls wait and try again.");
        }

    }

    public void _descStats(final CommandInterpreter ci) {
        int descCount = 0;
        int descStatsCount = 0;
        Collection<Node> nodes = getNodes();
        for (Node node2 : nodes) {
            descCount++;
            NodeKey nodeKey = node2.key();
            InstanceIdentifier<FlowCapableNode> nodeRef = InstanceIdentifier.create(Nodes.class)
                    .child(Node.class, nodeKey).augmentation(FlowCapableNode.class);
            ReadTransaction readOnlyTransaction = dataBroker.newReadOnlyTransaction();
            FlowCapableNode node = TestProviderTransactionUtil.getDataObject(readOnlyTransaction, nodeRef);
            if (node != null) {
                if (null != node.getHardware() && null != node.getManufacturer() && null != node.getSoftware()) {
                    descStatsCount++;
                }
            }
        }

        if (descCount == descStatsCount) {
            LOG.debug("descStats - Success");
        } else {
            LOG.debug("descStats - Failed");
            LOG.debug("System fetches stats data in 50 seconds interval, so please wait and try again.");
        }

    }

    private Collection<Node> getNodes() {
        ReadTransaction readOnlyTransaction = dataBroker.newReadOnlyTransaction();
        InstanceIdentifier<Nodes> nodesID = InstanceIdentifier.create(Nodes.class);
        Nodes nodes = TestProviderTransactionUtil.getDataObject(readOnlyTransaction, nodesID);
        if (nodes == null) {
            throw new IllegalStateException("nodes are not found, pls add the node.");
        }
        return nodes.nonnullNode().values();
    }

    @Override
    public String getHelp() {
        StringBuilder help = new StringBuilder();
        help.append("---MD-SAL Stats test module---\n");
        return help.toString();
    }

}
