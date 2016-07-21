/**
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.applications.topology.manager;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.ReadWriteTransaction;
import org.opendaylight.controller.md.sal.common.api.data.AsyncDataChangeEvent;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNode;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeRef;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.Nodes;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.topology.inventory.rev131030.InventoryNode;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.topology.inventory.rev131030.InventoryNodeBuilder;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.NodeId;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.topology.NodeBuilder;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NodeChangeListenerImpl extends DataChangeListenerImpl {
    private static final Logger LOG = LoggerFactory.getLogger(NodeChangeListenerImpl.class);

    public NodeChangeListenerImpl(final DataBroker dataBroker, final OperationProcessor operationProcessor) {
        // TODO: listener on FlowCapableNode. what if node id in Node.class is changed (it won't be caught by this
        // listener)
        super(operationProcessor, dataBroker, InstanceIdentifier.builder(Nodes.class).child(Node.class)
                .augmentation(FlowCapableNode.class).build());
    }

    @Override
    public void onDataChanged(final AsyncDataChangeEvent<InstanceIdentifier<?>, DataObject> change) {
        processAddedNode(change.getCreatedData());
        // processUpdatedNode(change.getUpdatedData());
        processRemovedNode(change.getRemovedPaths());
    }

    /**
     * @param removedNodes
     */
    private void processRemovedNode(final Set<InstanceIdentifier<?>> removedNodes) {
        for (InstanceIdentifier<?> removedNode : removedNodes) {
            final NodeId nodeId = provideTopologyNodeId(removedNode);
            final InstanceIdentifier<org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.topology.Node> iiToTopologyRemovedNode = provideIIToTopologyNode(nodeId);
            if (iiToTopologyRemovedNode != null) {
                operationProcessor.enqueueOperation(new TopologyOperation() {

                    @Override
                    public void applyOperation(final ReadWriteTransaction transaction) {
                        transaction.delete(LogicalDatastoreType.OPERATIONAL, iiToTopologyRemovedNode);
                        TopologyManagerUtil.removeAffectedLinks(nodeId, transaction, II_TO_TOPOLOGY);
                    }
                });
            } else {
                LOG.debug("Instance identifier to inventory wasn't translated to topology while deleting node.");
            }
        }
    }

//    /**
//     * @param updatedData
//     */
//     private void processUpdatedNode(Map<InstanceIdentifier<?>, DataObject> updatedData) {
//         //TODO: only node id is used from incoming data object.
//         //if it is changed what should happen? Listener is on FlowCapableNode so change
//         //of node id (only data which are used) isn't caught.
//     }

    /**
     * @param addedDatas
     */
    private void processAddedNode(final Map<InstanceIdentifier<?>, DataObject> addedDatas) {
        for (Entry<InstanceIdentifier<?>, DataObject> addedData : addedDatas.entrySet()) {
            createData(addedData.getKey());
        }
    }

    protected void createData(final InstanceIdentifier<?> iiToNodeInInventory) {
        final NodeId nodeIdInTopology = provideTopologyNodeId(iiToNodeInInventory);
        if (nodeIdInTopology != null) {
            final InstanceIdentifier<org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.topology.Node> iiToTopologyNode = provideIIToTopologyNode(nodeIdInTopology);
            sendToTransactionChain(prepareTopologyNode(nodeIdInTopology, iiToNodeInInventory), iiToTopologyNode);
        } else {
            LOG.debug("Inventory node key is null. Data can't be written to topology");
        }
    }

    /**
     * @param nodeIdInTopology
     * @param iiToNodeInInventory
     * @return Node
     */
    private static org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.topology.Node prepareTopologyNode(final NodeId nodeIdInTopology, final InstanceIdentifier<?> iiToNodeInInventory) {
        final InventoryNode inventoryNode = new InventoryNodeBuilder()
        .setInventoryNodeRef(new NodeRef(iiToNodeInInventory.firstIdentifierOf(Node.class)))
        .build();

        final NodeBuilder topologyNodeBuilder = new NodeBuilder();
        topologyNodeBuilder.setNodeId(nodeIdInTopology);
        topologyNodeBuilder.addAugmentation(InventoryNode.class, inventoryNode);

        return topologyNodeBuilder.build();
    }
}