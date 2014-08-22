package org.opendaylight.openflowplugin.test;

import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutionException;

import org.eclipse.osgi.framework.console.CommandInterpreter;
import org.eclipse.osgi.framework.console.CommandProvider;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.ReadOnlyTransaction;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.controller.sal.binding.api.BindingAwareBroker.ProviderContext;
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
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OpenflowpluginStatsTestCommandProvider implements CommandProvider {

	private static final Logger LOG = LoggerFactory
			.getLogger(OpenflowpluginStatsTestCommandProvider.class);
	private DataBroker dataProviderService;
	private final BundleContext ctx;

	public OpenflowpluginStatsTestCommandProvider(BundleContext ctx) {
		this.ctx = ctx;
	}

	public void onSessionInitiated(ProviderContext session) {
		dataProviderService = session.getSALService(DataBroker.class);
		ctx.registerService(CommandProvider.class.getName(), this, null);

	}

	public void _portStats(CommandInterpreter ci) {
		int nodeConnectorCount = 0;
		int nodeConnectorStatsCount = 0;
		List<Node> nodes = getNodes();
		for (Iterator<Node> iterator = nodes.iterator(); iterator.hasNext();) {
			NodeKey nodeKey = iterator.next().getKey();
			InstanceIdentifier<Node> nodeRef = InstanceIdentifier
					.builder(Nodes.class).child(Node.class, nodeKey)
					.toInstance();

			ReadOnlyTransaction readOnlyTransaction = dataProviderService
					.newReadOnlyTransaction();
			Node node = null;
			try {
				node = (Node) readOnlyTransaction
						.read(LogicalDatastoreType.OPERATIONAL, nodeRef).get()
						.get();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ExecutionException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			List<NodeConnector> ports = node.getNodeConnector();
			for (Iterator<NodeConnector> iterator2 = ports.iterator(); iterator2
					.hasNext();) {
				nodeConnectorCount++;
				NodeConnectorKey nodeConnectorKey = iterator2.next().getKey();
				InstanceIdentifier<NodeConnector> connectorRef = InstanceIdentifier
						.builder(Nodes.class).child(Node.class, nodeKey)
						.child(NodeConnector.class, nodeConnectorKey)
						.toInstance();
				NodeConnector nodeConnector = null;
				try {
					nodeConnector = (NodeConnector) readOnlyTransaction
							.read(LogicalDatastoreType.OPERATIONAL,
									connectorRef).get().get();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (ExecutionException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				FlowCapableNodeConnectorStatisticsData data = nodeConnector
						.getAugmentation(FlowCapableNodeConnectorStatisticsData.class);

				if (null != data) {
					nodeConnectorStatsCount++;

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

	public void _portDescStats(CommandInterpreter ci) {
		int nodeConnectorCount = 0;
		int nodeConnectorDescStatsCount = 0;
		List<Node> nodes = getNodes();
		for (Iterator<Node> iterator = nodes.iterator(); iterator.hasNext();) {
			NodeKey nodeKey = iterator.next().getKey();
			InstanceIdentifier<Node> nodeRef = InstanceIdentifier
					.builder(Nodes.class).child(Node.class, nodeKey)
					.toInstance();

			ReadOnlyTransaction readOnlyTransaction = dataProviderService
					.newReadOnlyTransaction();
			Node node = null;
			try {
				node = (Node) readOnlyTransaction
						.read(LogicalDatastoreType.OPERATIONAL, nodeRef).get()
						.get();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ExecutionException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			List<NodeConnector> ports = node.getNodeConnector();
			for (Iterator<NodeConnector> iterator2 = ports.iterator(); iterator2
					.hasNext();) {
				nodeConnectorCount++;
				NodeConnectorKey nodeConnectorKey = iterator2.next().getKey();
				InstanceIdentifier<FlowCapableNodeConnector> connectorRef = InstanceIdentifier
						.builder(Nodes.class).child(Node.class, nodeKey)
						.child(NodeConnector.class, nodeConnectorKey)
						.augmentation(FlowCapableNodeConnector.class)
						.toInstance();
				FlowCapableNodeConnector nodeConnector = null;
				try {
					nodeConnector = (FlowCapableNodeConnector) readOnlyTransaction
							.read(LogicalDatastoreType.OPERATIONAL,
									connectorRef).get().get();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (ExecutionException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				if (null != nodeConnector.getName()
						&& null != nodeConnector.getCurrentFeature()
						&& null != nodeConnector.getState()) {
					// && null != nodeConnector.getHardwareAddress()) {
					// && null != nodeConnector.getPortNumber()) {
					nodeConnectorDescStatsCount++;
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

	public void _flowStats(CommandInterpreter ci) {
		int flowCount = 0;
		int flowStatsCount = 0;
		List<Node> nodes = getNodes();
		for (Iterator<Node> iterator = nodes.iterator(); iterator.hasNext();) {
			NodeKey nodeKey = iterator.next().getKey();
			InstanceIdentifier<FlowCapableNode> nodeRef = InstanceIdentifier
					.builder(Nodes.class).child(Node.class, nodeKey)
					.augmentation(FlowCapableNode.class).toInstance();

			ReadOnlyTransaction readOnlyTransaction = dataProviderService
					.newReadOnlyTransaction();
			FlowCapableNode node = null;
			try {
				node = (FlowCapableNode) readOnlyTransaction.read(
						LogicalDatastoreType.OPERATIONAL, nodeRef).get().get();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ExecutionException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			List<Table> tables = node.getTable();
			for (Iterator<Table> iterator2 = tables.iterator(); iterator2
					.hasNext();) {
				TableKey tableKey = iterator2.next().getKey();
				InstanceIdentifier<Table> tableRef = InstanceIdentifier
						.builder(Nodes.class).child(Node.class, nodeKey)
						.augmentation(FlowCapableNode.class)
						.child(Table.class, tableKey).toInstance();
				Table table = null;
				try {
					table = (Table) readOnlyTransaction.read(
							LogicalDatastoreType.OPERATIONAL, tableRef).get().get();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (ExecutionException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				List<Flow> flows = table.getFlow();
				for (Iterator<Flow> iterator3 = flows.iterator(); iterator3
						.hasNext();) {
					flowCount++;
					FlowKey flowKey = iterator3.next().getKey();
					InstanceIdentifier<Flow> flowRef = InstanceIdentifier
							.builder(Nodes.class).child(Node.class, nodeKey)
							.augmentation(FlowCapableNode.class)
							.child(Table.class, tableKey)
							.child(Flow.class, flowKey).toInstance();
					Flow flow = null;
					try {
						flow = (Flow) readOnlyTransaction.read(
								LogicalDatastoreType.OPERATIONAL, flowRef).get().get();
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (ExecutionException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					FlowStatisticsData data = flow
							.getAugmentation(FlowStatisticsData.class);
					if (null != data) {
						flowStatsCount++;
						LOG.debug("--------------------------------------------");
						ci.print(data);
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

	public void _tableStats(CommandInterpreter ci) {
		int tableCount = 0;
		int tableStatsCount = 0;
		List<Node> nodes = getNodes();
		for (Iterator<Node> iterator = nodes.iterator(); iterator.hasNext();) {
			NodeKey nodeKey = iterator.next().getKey();
			InstanceIdentifier<FlowCapableNode> nodeRef = InstanceIdentifier
					.builder(Nodes.class).child(Node.class, nodeKey)
					.augmentation(FlowCapableNode.class).toInstance();

			ReadOnlyTransaction readOnlyTransaction = dataProviderService
					.newReadOnlyTransaction();
			FlowCapableNode node = null;
			try {
				node = (FlowCapableNode) readOnlyTransaction.read(
						LogicalDatastoreType.OPERATIONAL, nodeRef).get().get();
			} catch (InterruptedException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} catch (ExecutionException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}

			List<Table> tables = node.getTable();
			for (Iterator<Table> iterator2 = tables.iterator(); iterator2
					.hasNext();) {
				tableCount++;
				TableKey tableKey = iterator2.next().getKey();
				InstanceIdentifier<Table> tableRef = InstanceIdentifier
						.builder(Nodes.class).child(Node.class, nodeKey)
						.augmentation(FlowCapableNode.class)
						.child(Table.class, tableKey).toInstance();
				Table table = null;
				try {
					table = (Table) readOnlyTransaction.read(
							LogicalDatastoreType.OPERATIONAL, tableRef).get().get();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (ExecutionException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				FlowTableStatisticsData data = table
						.getAugmentation(FlowTableStatisticsData.class);
				if (null != data) {
					tableStatsCount++;
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

	public void _groupStats(CommandInterpreter ci) {
		int groupCount = 0;
		int groupStatsCount = 0;
		NodeGroupStatistics data = null;
		List<Node> nodes = getNodes();
		for (Iterator<Node> iterator = nodes.iterator(); iterator.hasNext();) {
			NodeKey nodeKey = iterator.next().getKey();
			InstanceIdentifier<FlowCapableNode> nodeRef = InstanceIdentifier
					.builder(Nodes.class).child(Node.class, nodeKey)
					.augmentation(FlowCapableNode.class).toInstance();
			ReadOnlyTransaction readOnlyTransaction = dataProviderService
					.newReadOnlyTransaction();
			FlowCapableNode node = null;
			try {
				node = (FlowCapableNode) readOnlyTransaction.read(
						LogicalDatastoreType.OPERATIONAL, nodeRef).get().get();
			} catch (InterruptedException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} catch (ExecutionException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}

			List<Group> groups = node.getGroup();
			for (Iterator<Group> iterator2 = groups.iterator(); iterator2
					.hasNext();) {
				groupCount++;
				GroupKey groupKey = iterator2.next().getKey();
				InstanceIdentifier<Group> groupRef = InstanceIdentifier
						.builder(Nodes.class).child(Node.class, nodeKey)
						.augmentation(FlowCapableNode.class)
						.child(Group.class, groupKey).toInstance();
				Group group = null;
				try {
					group = (Group) readOnlyTransaction.read(
							LogicalDatastoreType.OPERATIONAL, groupRef).get().get();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (ExecutionException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				data = group.getAugmentation(NodeGroupStatistics.class);
				if (null != data) {
					groupStatsCount++;
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

	public void _groupDescStats(CommandInterpreter ci) {
		int groupCount = 0;
		int groupDescStatsCount = 0;
		NodeGroupDescStats data = null;
		List<Node> nodes = getNodes();
		for (Iterator<Node> iterator = nodes.iterator(); iterator.hasNext();) {
			NodeKey nodeKey = iterator.next().getKey();
			InstanceIdentifier<FlowCapableNode> nodeRef = InstanceIdentifier
					.builder(Nodes.class).child(Node.class, nodeKey)
					.augmentation(FlowCapableNode.class).toInstance();
			ReadOnlyTransaction readOnlyTransaction = dataProviderService
					.newReadOnlyTransaction();
			FlowCapableNode node = null;
			try {
				node = (FlowCapableNode) readOnlyTransaction.read(
						LogicalDatastoreType.OPERATIONAL, nodeRef).get().get();
			} catch (InterruptedException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} catch (ExecutionException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}

			List<Group> groups = node.getGroup();
			for (Iterator<Group> iterator2 = groups.iterator(); iterator2
					.hasNext();) {
				groupCount++;
				GroupKey groupKey = iterator2.next().getKey();
				InstanceIdentifier<Group> groupRef = InstanceIdentifier
						.builder(Nodes.class).child(Node.class, nodeKey)
						.augmentation(FlowCapableNode.class)
						.child(Group.class, groupKey).toInstance();
				Group group = null;
				try {
					group = (Group) readOnlyTransaction.read(
							LogicalDatastoreType.OPERATIONAL, groupRef).get().get();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (ExecutionException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				data = group.getAugmentation(NodeGroupDescStats.class);
				if (null != data) {
					groupDescStatsCount++;
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

	public void _meterStats(CommandInterpreter ci) {
		int meterCount = 0;
		int meterStatsCount = 0;
		NodeMeterStatistics data = null;
		List<Node> nodes = getNodes();
		for (Iterator<Node> iterator = nodes.iterator(); iterator.hasNext();) {
			NodeKey nodeKey = iterator.next().getKey();
			InstanceIdentifier<FlowCapableNode> nodeRef = InstanceIdentifier
					.builder(Nodes.class).child(Node.class, nodeKey)
					.augmentation(FlowCapableNode.class).toInstance();
			ReadOnlyTransaction readOnlyTransaction = dataProviderService
					.newReadOnlyTransaction();
			FlowCapableNode node = null;
			try {
				node = (FlowCapableNode) readOnlyTransaction.read(
						LogicalDatastoreType.OPERATIONAL, nodeRef).get().get();
			} catch (InterruptedException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} catch (ExecutionException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}

			List<Meter> meters = node.getMeter();
			for (Iterator<Meter> iterator2 = meters.iterator(); iterator2
					.hasNext();) {
				meterCount++;
				MeterKey meterKey = iterator2.next().getKey();
				InstanceIdentifier<Meter> meterRef = InstanceIdentifier
						.builder(Nodes.class).child(Node.class, nodeKey)
						.augmentation(FlowCapableNode.class)
						.child(Meter.class, meterKey).toInstance();
				Meter meter = null;
				try {
					meter = (Meter) readOnlyTransaction.read(
							LogicalDatastoreType.OPERATIONAL, meterRef).get().get();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (ExecutionException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				data = meter.getAugmentation(NodeMeterStatistics.class);
				if (null != data) {
					meterStatsCount++;
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

	public void _meterConfigStats(CommandInterpreter ci) {
		int meterCount = 0;
		int meterConfigStatsCount = 0;
		NodeMeterConfigStats data = null;
		List<Node> nodes = getNodes();
		for (Iterator<Node> iterator = nodes.iterator(); iterator.hasNext();) {
			NodeKey nodeKey = iterator.next().getKey();
			InstanceIdentifier<FlowCapableNode> nodeRef = InstanceIdentifier
					.builder(Nodes.class).child(Node.class, nodeKey)
					.augmentation(FlowCapableNode.class).toInstance();
			ReadOnlyTransaction readOnlyTransaction = dataProviderService
					.newReadOnlyTransaction();
			FlowCapableNode node = null;
			try {
				node = (FlowCapableNode) readOnlyTransaction.read(
						LogicalDatastoreType.OPERATIONAL, nodeRef).get().get();
			} catch (InterruptedException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} catch (ExecutionException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}

			List<Meter> meters = node.getMeter();
			for (Iterator<Meter> iterator2 = meters.iterator(); iterator2
					.hasNext();) {
				meterCount++;
				MeterKey meterKey = iterator2.next().getKey();
				InstanceIdentifier<Meter> meterRef = InstanceIdentifier
						.builder(Nodes.class).child(Node.class, nodeKey)
						.augmentation(FlowCapableNode.class)
						.child(Meter.class, meterKey).toInstance();
				Meter meter = null;
				try {
					meter = (Meter) readOnlyTransaction.read(
							LogicalDatastoreType.OPERATIONAL, meterRef).get().get();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (ExecutionException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				data = meter.getAugmentation(NodeMeterConfigStats.class);
				if (null != data) {
					meterConfigStatsCount++;
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

	public void _aggregateStats(CommandInterpreter ci) {
		int aggregateFlowCount = 0;
		int aggerateFlowStatsCount = 0;
		List<Node> nodes = getNodes();
		for (Iterator<Node> iterator = nodes.iterator(); iterator.hasNext();) {
			NodeKey nodeKey = iterator.next().getKey();
			InstanceIdentifier<FlowCapableNode> nodeRef = InstanceIdentifier
					.builder(Nodes.class).child(Node.class, nodeKey)
					.augmentation(FlowCapableNode.class).toInstance();
			ReadOnlyTransaction readOnlyTransaction = dataProviderService
					.newReadOnlyTransaction();
			FlowCapableNode node = null;
			try {
				node = (FlowCapableNode) readOnlyTransaction.read(
						LogicalDatastoreType.OPERATIONAL, nodeRef).get().get();
			} catch (InterruptedException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} catch (ExecutionException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}

			List<Table> tables = node.getTable();
			for (Iterator<Table> iterator2 = tables.iterator(); iterator2
					.hasNext();) {
				aggregateFlowCount++;
				TableKey tableKey = iterator2.next().getKey();
				InstanceIdentifier<Table> tableRef = InstanceIdentifier
						.builder(Nodes.class).child(Node.class, nodeKey)
						.augmentation(FlowCapableNode.class)
						.child(Table.class, tableKey).toInstance();
				Table table = null;
				try {
					table = (Table) readOnlyTransaction.read(
							LogicalDatastoreType.OPERATIONAL, tableRef).get().get();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (ExecutionException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				AggregateFlowStatisticsData data = table
						.getAugmentation(AggregateFlowStatisticsData.class);
				if (null != data) {
					aggerateFlowStatsCount++;
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

	public void _descStats(CommandInterpreter ci) {
		int descCount = 0;
		int descStatsCount = 0;
		List<Node> nodes = getNodes();
		for (Iterator<Node> iterator = nodes.iterator(); iterator.hasNext();) {
			descCount++;
			NodeKey nodeKey = iterator.next().getKey();
			InstanceIdentifier<FlowCapableNode> nodeRef = InstanceIdentifier
					.builder(Nodes.class).child(Node.class, nodeKey)
					.augmentation(FlowCapableNode.class).toInstance();
			ReadOnlyTransaction readOnlyTransaction = dataProviderService
					.newReadOnlyTransaction();
			FlowCapableNode node = null;
			try {
				node = (FlowCapableNode) readOnlyTransaction.read(
						LogicalDatastoreType.OPERATIONAL, nodeRef).get().get();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ExecutionException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if (null != node.getHardware() && null != node.getManufacturer()
					&& null != node.getSoftware()) {
				descStatsCount++;
			}
		}

		if (descCount == descStatsCount) {
			LOG.debug("descStats - Success");
		} else {
			LOG.debug("descStats - Failed");
			LOG.debug("System fetchs stats data in 50 seconds interval, so pls wait and try again.");
		}

	}

	public void _testPortStats(CommandInterpreter ci) {
		ReadOnlyTransaction readOnlyTransaction = dataProviderService
				.newReadOnlyTransaction();
		try {
			Nodes nodes = (Nodes) readOnlyTransaction.read(
					LogicalDatastoreType.OPERATIONAL,
					InstanceIdentifier.builder(Nodes.class).toInstance()).get().get();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ExecutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private List<Node> getNodes() {
		ReadOnlyTransaction readOnlyTransaction = dataProviderService
				.newReadOnlyTransaction();
		Nodes nodes = null;
		try {
			nodes = (Nodes) readOnlyTransaction
					.read(LogicalDatastoreType.OPERATIONAL,
							InstanceIdentifier.builder(Nodes.class)
									.toInstance()).get().get();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ExecutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (null == nodes) {
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
