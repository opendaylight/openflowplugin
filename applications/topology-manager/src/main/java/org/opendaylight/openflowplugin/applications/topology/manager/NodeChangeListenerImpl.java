/**
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.applications.topology.manager;

import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeRef;

import org.opendaylight.yang.gen.v1.urn.opendaylight.model.topology.inventory.rev131030.InventoryNode;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.topology.inventory.rev131030.InventoryNodeBuilder;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.NodeId;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.topology.NodeBuilder;
import java.util.Set;
import org.opendaylight.controller.md.sal.binding.api.ReadWriteTransaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.Map.Entry;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNode;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.Nodes;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import java.util.Map;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.common.api.data.AsyncDataChangeEvent;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class NodeChangeListenerImpl extends DataChangeListenerImpl {
    private final static Logger LOG = LoggerFactory.getLogger(NodeChangeListenerImpl.class);

    public NodeChangeListenerImpl(final DataBroker dataBroker, final OperationProcessor operationProcessor) {
        // TODO: listener on FlowCapableNode. what if node id in Node.class is changed (it won't be caught by this
        // listener)
        super(operationProcessor, dataBroker, InstanceIdentifier.builder(Nodes.class).child(Node.class)
                .augmentation(FlowCapableNode.class).build());
    }

    @Override
    public void onDataChanged(AsyncDataChangeEvent<InstanceIdentifier<?>, DataObject> change) {
        processAddedNode(change.getCreatedData());
        // processUpdatedNode(change.getUpdatedData());
        processRemovedNode(change.getRemovedPaths());
    }

    /**
     * @param removedPaths
     */
    private void processRemovedNode(Set<InstanceIdentifier<?>> removedNodes) {
        for (InstanceIdentifier<?> removedNode : removedNodes) {
            final InstanceIdentifier<org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.topology.Node> iiToTopologyRemovedNode = provideIIToTopologyNode(provideTopologyNodeId(removedNode));
            if (iiToTopologyRemovedNode != null) {
                operationProcessor.enqueueOperation(new TopologyOperation() {

                    @Override
                    public void applyOperation(ReadWriteTransaction transaction) {
                        transaction.delete(LogicalDatastoreType.OPERATIONAL, iiToTopologyRemovedNode);
                    }
                });
            } else {
                LOG.debug("Instance identifier to inventory wasn't translated to topology while deleting node.");
            }
        }
    }

    /**
     * @param updatedData
     */
    // private void processUpdatedNode(Map<InstanceIdentifier<?>, DataObject> updatedData) {
    // //TODO: only node id is used from incomming data object.
    // //if it is changed what should happen? Listener is on FlocCapableNode so change
    // //of node id (only data which are used) isn't caught.
    // }

    /**
     * @param createdData
     */
    private void processAddedNode(Map<InstanceIdentifier<?>, DataObject> addedDatas) {
        for (Entry<InstanceIdentifier<?>, DataObject> addedData : addedDatas.entrySet()) {
            createData(addedData.getKey());
        }
    }

    protected void createData(InstanceIdentifier<?> iiToNodeInInventory) {
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
     * @return
     */
    private org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.topology.Node prepareTopologyNode(NodeId nodeIdInTopology, InstanceIdentifier<?> iiToNodeInInventory) {
        final InventoryNode inventoryNode = new InventoryNodeBuilder()
        .setInventoryNodeRef(new NodeRef(iiToNodeInInventory.firstIdentifierOf(Node.class)))
        .build();

        final NodeBuilder topologyNodeBuilder = new NodeBuilder();
        topologyNodeBuilder.setNodeId(nodeIdInTopology);
        topologyNodeBuilder.addAugmentation(InventoryNode.class, inventoryNode);

        return topologyNodeBuilder.build();
    }
}