/**
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.applications.topology.manager;


import org.opendaylight.controller.md.sal.binding.api.ReadWriteTransaction;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.topology.NodeBuilder;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.NodeId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.NodeKey;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.TopologyId;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.TopologyKey;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.Topology;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.NetworkTopology;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.Map.Entry;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNode;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.Nodes;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.yangtools.concepts.ListenerRegistration;
import java.util.Map;
import java.util.Set;
import org.opendaylight.controller.md.sal.binding.api.DataChangeListener;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.common.api.data.AsyncDataChangeEvent;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.controller.md.sal.common.api.data.AsyncDataBroker;

public class NodeChangeListenerImpl implements DataChangeListener, AutoCloseable {
    private final static Logger LOG = LoggerFactory.getLogger(NodeChangeListenerImpl.class);

    private final static String topologyId = "topology id";

    /**
     * instance identifier to Node in network topology model (yangtools)
     */
    private static final InstanceIdentifier<Topology> II_TO_TOPOLOGY =
            InstanceIdentifier
            .builder(NetworkTopology.class)
            .child(Topology.class, new TopologyKey(new TopologyId(topologyId)))
            .build();

    private final ListenerRegistration<DataChangeListener> dataChangeListenerRegistration;
    private OperationProcessor operationProcessor;

    public NodeChangeListenerImpl(final DataBroker dataBroker, final OperationProcessor operationProcessor) {
        //TODO: listener on FlowCapableNode. what if node id in Node.class is changed (it won't be caught by this listener)
        dataChangeListenerRegistration = dataBroker.registerDataChangeListener(LogicalDatastoreType.OPERATIONAL,
                InstanceIdentifier.builder(Nodes.class).child(Node.class).augmentation(FlowCapableNode.class).build(),
                this, AsyncDataBroker.DataChangeScope.BASE);
        this.operationProcessor = operationProcessor;
    }

    @Override
    public void onDataChanged(AsyncDataChangeEvent<InstanceIdentifier<?>, DataObject> change) {
        processAddedNode(change.getCreatedData());
        processUpdatedNode(change.getUpdatedData());
        processRemovedNode(change.getRemovedPaths());
    }

    /**
     * @param removedPaths
     */
    private void processRemovedNode(Set<InstanceIdentifier<?>> removedNodes) {
        for (final InstanceIdentifier<?> removedNode : removedNodes) {
            operationProcessor.enqueueOperation(new TopologyOperation() {

                @Override
                public void applyOperation(ReadWriteTransaction transaction) {
                    transaction.delete(LogicalDatastoreType.OPERATIONAL, removedNode);
                }
            });
        }
    }

    /**
     * @param updatedData
     */
    private void processUpdatedNode(Map<InstanceIdentifier<?>, DataObject> updatedData) {
        //TODO: only node id is used from incomming data object.
        //if it is changed what should happen? Listener is on FlocCapableNode so change
        //of node id (only data which are used) isn't caught.
    }

    /**
     * @param createdData
     */
    private void processAddedNode(Map<InstanceIdentifier<?>, DataObject> addedDatas) {
        for (Entry<InstanceIdentifier<?>, DataObject> addedData : addedDatas.entrySet()) {
            if (addedData.getValue() instanceof FlowCapableNode) {
                createNewNodeInTopology(addedData.getKey(), (FlowCapableNode) (addedData.getValue()));
            } else {
                LOG.debug("Expected data of type FlowCapableNode but {} was obtainedl", addedData.getClass().getName());
            }
        }

    }

    /**
     * @param iiToNodeInInventory
     * @param addedData
     */
    private void createNewNodeInTopology(InstanceIdentifier<?> iiToNodeInInventory, final FlowCapableNode addedData) {
        final NodeBuilder topologyNodeBuilder = new NodeBuilder();
        final NodeKey inventoryNodeKey = iiToNodeInInventory.firstKeyOf(Node.class, NodeKey.class);
        if (inventoryNodeKey != null) {
            NodeId nodeIdInTopology = new NodeId(inventoryNodeKey.getId().getValue());
            org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.topology.NodeKey nodeKeyInTopology = new org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.topology.NodeKey(nodeIdInTopology);
            topologyNodeBuilder.setNodeId(nodeIdInTopology);

            final InstanceIdentifier<org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.topology.Node> iiToTopologyNode = II_TO_TOPOLOGY
            .builder()
            .child(org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.topology.Node.class, nodeKeyInTopology)
            .build();

            operationProcessor.enqueueOperation(new TopologyOperation() {

                @Override
                public void applyOperation(ReadWriteTransaction transaction) {
                    transaction.put(LogicalDatastoreType.OPERATIONAL, iiToTopologyNode, topologyNodeBuilder.build());
                }
            });
        } else {
            LOG.debug("Inventory node key is null. Data can't be written to topology");
        }

    }

    @Override
    public void close() throws Exception {
        dataChangeListenerRegistration.close();
    }

}