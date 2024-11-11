/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.applications.topology.manager;

import java.util.List;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import javax.inject.Singleton;
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
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
@Component(service = { })
public final class NodeChangeListenerImpl extends DataTreeChangeListenerImpl<FlowCapableNode> {
    private static final Logger LOG = LoggerFactory.getLogger(NodeChangeListenerImpl.class);

    @Inject
    @Activate
    public NodeChangeListenerImpl(@Reference final DataBroker dataBroker,
            @Reference final OperationProcessor operationProcessor) {
        // TODO: listener on FlowCapableNode. what if node id in Node.class is changed (it won't be caught by this
        // listener)
        super(operationProcessor, dataBroker,
              InstanceIdentifier.builder(Nodes.class).child(Node.class).augmentation(FlowCapableNode.class).build());
    }

    @Override
    public void onDataTreeChanged(final List<DataTreeModification<FlowCapableNode>> modifications) {
        for (DataTreeModification<FlowCapableNode> modification : modifications) {
            switch (modification.getRootNode().modificationType()) {
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
                            "Unhandled modification type: {}" + modification.getRootNode().modificationType());
            }
        }
    }

    @Deactivate
    @PreDestroy
    @Override
    public void close() {
        super.close();
    }

    private void processRemovedNode(final DataTreeModification<FlowCapableNode> modification) {
        final var iiToNodeInInventory = modification.getRootPath().path();
        final var nodeId = provideTopologyNodeId(iiToNodeInInventory);
        final var iiToTopologyRemovedNode = provideIIToTopologyNode(nodeId);
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
        final InstanceIdentifier<FlowCapableNode> iiToNodeInInventory = modification.getRootPath().path();
        final NodeId nodeIdInTopology = provideTopologyNodeId(iiToNodeInInventory);
        if (nodeIdInTopology != null) {
            final var iiToTopologyNode = provideIIToTopologyNode(nodeIdInTopology);
            sendToTransactionChain(prepareTopologyNode(nodeIdInTopology, iiToNodeInInventory), iiToTopologyNode);
        } else {
            LOG.debug("Inventory node key is null. Data can't be written to topology");
        }
    }

    private static org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network
            .topology.topology.Node prepareTopologyNode(final NodeId nodeIdInTopology,
                final InstanceIdentifier<FlowCapableNode> iiToNodeInInventory) {
        return new NodeBuilder()
            .setNodeId(nodeIdInTopology)
            .addAugmentation(new InventoryNodeBuilder()
                .setInventoryNodeRef(new NodeRef(iiToNodeInInventory.firstIdentifierOf(Node.class).toIdentifier()))
                .build())
            .build();
    }
}
