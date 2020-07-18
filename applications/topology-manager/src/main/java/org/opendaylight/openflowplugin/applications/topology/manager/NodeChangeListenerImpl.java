/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.applications.topology.manager;

import java.util.Collection;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.apache.aries.blueprint.annotation.service.Reference;
import org.opendaylight.mdsal.binding.api.DataBroker;
import org.opendaylight.mdsal.binding.api.DataTreeModification;
import org.opendaylight.mdsal.common.api.LogicalDatastoreType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNode;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeRef;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.Nodes;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.topology.inventory.rev131030.InventoryNodeBuilder;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.NodeId;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.topology.NodeBuilder;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class NodeChangeListenerImpl extends DataTreeChangeListenerImpl<FlowCapableNode> {
    private static final Logger LOG = LoggerFactory.getLogger(NodeChangeListenerImpl.class);

    @Inject
    public NodeChangeListenerImpl(@Reference final DataBroker dataBroker, final OperationProcessor operationProcessor) {
        // TODO: listener on FlowCapableNode. what if node id in Node.class is changed (it won't be caught by this
        // listener)
        super(operationProcessor, dataBroker,
              InstanceIdentifier.builder(Nodes.class).child(Node.class).augmentation(FlowCapableNode.class).build());
    }

    @Override
    public void onDataTreeChanged(final Collection<DataTreeModification<FlowCapableNode>> modifications) {
        for (DataTreeModification modification : modifications) {
            switch (modification.getRootNode().getModificationType()) {
                case WRITE:
                    processAddedNode(modification);
                    break;
                case SUBTREE_MODIFIED:
                    // NOOP
                    break;
                case DELETE:
                    processRemovedNode(modification);
                    break;
                default:
                    throw new IllegalArgumentException(
                            "Unhandled modification type: {}" + modification.getRootNode().getModificationType());
            }
        }
    }

    @Override
    @PreDestroy
    public void close() {
        super.close();
    }

    private void processRemovedNode(final DataTreeModification<FlowCapableNode> modification) {
        final InstanceIdentifier<FlowCapableNode> iiToNodeInInventory = modification.getRootPath().getRootIdentifier();
        final NodeId nodeId = provideTopologyNodeId(iiToNodeInInventory);
        final InstanceIdentifier<org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology
                .rev131021.network.topology.topology.Node>
                iiToTopologyRemovedNode = provideIIToTopologyNode(nodeId);
        if (iiToTopologyRemovedNode != null) {
            operationProcessor.enqueueOperation(manager -> {
                manager.addDeleteOperationToTxChain(LogicalDatastoreType.OPERATIONAL, iiToTopologyRemovedNode);
                TopologyManagerUtil.removeAffectedLinks(nodeId, manager, II_TO_TOPOLOGY);
            });
        } else {
            LOG.debug("Instance identifier to inventory wasn't translated to topology while deleting node.");
        }
    }

    private void processAddedNode(final DataTreeModification<FlowCapableNode> modification) {
        final InstanceIdentifier<FlowCapableNode> iiToNodeInInventory = modification.getRootPath().getRootIdentifier();
        final NodeId nodeIdInTopology = provideTopologyNodeId(iiToNodeInInventory);
        if (nodeIdInTopology != null) {
            final InstanceIdentifier<org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology
                    .rev131021.network.topology.topology.Node>
                    iiToTopologyNode = provideIIToTopologyNode(nodeIdInTopology);
            sendToTransactionChain(prepareTopologyNode(nodeIdInTopology, iiToNodeInInventory), iiToTopologyNode);
        } else {
            LOG.debug("Inventory node key is null. Data can't be written to topology");
        }
    }

    private static org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network
            .topology.topology.Node prepareTopologyNode(
            final NodeId nodeIdInTopology, final InstanceIdentifier<FlowCapableNode> iiToNodeInInventory) {
        return new NodeBuilder()
                .setNodeId(nodeIdInTopology)
                .addAugmentation(new InventoryNodeBuilder()
                    .setInventoryNodeRef(new NodeRef(iiToNodeInInventory.firstIdentifierOf(Node.class)))
                    .build())
                .build();
    }
}
