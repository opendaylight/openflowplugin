/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.applications.topology.manager;

import java.util.Collection;
import java.util.Optional;
import javax.annotation.Nonnull;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.DataTreeModification;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.controller.md.sal.common.api.data.ReadFailedException;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNodeConnector;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeConnectorRef;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.Nodes;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.node.NodeConnector;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.node.NodeConnectorKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.topology.inventory.rev131030.InventoryNodeConnector;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.topology.inventory.rev131030.InventoryNodeConnectorBuilder;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.NodeId;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.TpId;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.topology.node.TerminationPoint;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.topology.node.TerminationPointBuilder;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.topology.node.TerminationPointKey;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TerminationPointChangeListenerImpl extends DataTreeChangeListenerImpl<FlowCapableNodeConnector> {
    private static final Logger LOG = LoggerFactory.getLogger(TerminationPointChangeListenerImpl.class);

    public TerminationPointChangeListenerImpl(final DataBroker dataBroker, final OperationProcessor operationProcessor) {
        super(operationProcessor, dataBroker, InstanceIdentifier.builder(Nodes.class).child(Node.class)
                .child(NodeConnector.class).augmentation(FlowCapableNodeConnector.class).build());
        this.operationProcessor = operationProcessor;
    }

    @Override
    public void onDataTreeChanged(@Nonnull Collection<DataTreeModification<FlowCapableNodeConnector>> modifications) {
        for (DataTreeModification<FlowCapableNodeConnector> modification : modifications) {
            switch (modification.getRootNode().getModificationType()) {
                case WRITE:
                    processAddedTerminationPoints(modification);
                    break;
                case SUBTREE_MODIFIED:
                    processUpdatedTerminationPoints(modification);
                    break;
                case DELETE:
                    processRemovedTerminationPoints(modification);
                    break;
                default:
                    throw new IllegalArgumentException("Unhandled modification type: {}" +
                            modification.getRootNode().getModificationType());
            }
        }
    }

    private void processRemovedTerminationPoints(final DataTreeModification<FlowCapableNodeConnector> modification) {
        final InstanceIdentifier<FlowCapableNodeConnector> removedNode = modification.getRootPath().getRootIdentifier();
        final TpId terminationPointId = provideTopologyTerminationPointId(removedNode);
        final InstanceIdentifier<TerminationPoint> iiToTopologyTerminationPoint = provideIIToTopologyTerminationPoint(
                terminationPointId, removedNode);

        if (iiToTopologyTerminationPoint != null) {
            final InstanceIdentifier<org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.topology.Node> node = iiToTopologyTerminationPoint.firstIdentifierOf(org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.topology.Node.class);
            operationProcessor.enqueueOperation(manager -> {
                Optional<org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.topology.Node> nodeOptional = Optional.empty();
                try {
                    nodeOptional = Optional.ofNullable(
                            manager.readFromTransaction(LogicalDatastoreType.OPERATIONAL, node).checkedGet().orNull());
                } catch (ReadFailedException e) {
                    LOG.warn("Error occurred when trying to read NodeConnector: {}", e.getMessage());
                    LOG.debug("Error occurred when trying to read NodeConnector.. ", e);
                }
                if (nodeOptional.isPresent()) {
                    TopologyManagerUtil.removeAffectedLinks(terminationPointId, manager, II_TO_TOPOLOGY);
                    manager.addDeleteOperationToTxChain(LogicalDatastoreType.OPERATIONAL, iiToTopologyTerminationPoint);
                }
            });
        } else {
            LOG.debug("Instance identifier to inventory wasn't translated to topology while deleting termination point.");
        }
    }

    private void processUpdatedTerminationPoints(final DataTreeModification<FlowCapableNodeConnector> modification) {
        // TODO Auto-generated method stub
    }

    private void processAddedTerminationPoints(final DataTreeModification<FlowCapableNodeConnector> modification) {
        final InstanceIdentifier<FlowCapableNodeConnector> iiToNodeInInventory = modification.getRootPath().getRootIdentifier();
        TpId terminationPointIdInTopology = provideTopologyTerminationPointId(iiToNodeInInventory);
        if (terminationPointIdInTopology != null) {
            InstanceIdentifier<TerminationPoint> iiToTopologyTerminationPoint = provideIIToTopologyTerminationPoint(
                    terminationPointIdInTopology, iiToNodeInInventory);
            TerminationPoint point = prepareTopologyTerminationPoint(terminationPointIdInTopology, iiToNodeInInventory);
            sendToTransactionChain(point, iiToTopologyTerminationPoint);
            removeLinks(modification.getRootNode().getDataAfter(), point);
        } else {
            LOG.debug("Inventory node connector key is null. Data can't be written to topology termination point");
        }
    }

    private void removeLinks(final FlowCapableNodeConnector flowCapNodeConnector, final TerminationPoint point) {
        operationProcessor.enqueueOperation(manager -> {
            if ((flowCapNodeConnector.getState() != null && flowCapNodeConnector.getState().isLinkDown())
                    || (flowCapNodeConnector.getConfiguration() != null && flowCapNodeConnector.getConfiguration().isPORTDOWN())) {
                TopologyManagerUtil.removeAffectedLinks(point.getTpId(), manager, II_TO_TOPOLOGY);
            }
        });
    }

    private static TerminationPoint prepareTopologyTerminationPoint(final TpId terminationPointIdInTopology,
                                                                    final InstanceIdentifier<FlowCapableNodeConnector> iiToNodeInInventory) {
        final InventoryNodeConnector inventoryNodeConnector = new InventoryNodeConnectorBuilder()
                .setInventoryNodeConnectorRef(new NodeConnectorRef(iiToNodeInInventory.firstIdentifierOf(NodeConnector.class))).build();
        final TerminationPointBuilder terminationPointBuilder = new TerminationPointBuilder();
        terminationPointBuilder.setTpId(terminationPointIdInTopology);
        terminationPointBuilder.addAugmentation(InventoryNodeConnector.class, inventoryNodeConnector);
        return terminationPointBuilder.build();
    }

    private InstanceIdentifier<TerminationPoint> provideIIToTopologyTerminationPoint(final TpId terminationPointIdInTopology,
                                                                                     final InstanceIdentifier<FlowCapableNodeConnector> iiToNodeInInventory) {
        NodeId nodeIdInTopology = provideTopologyNodeId(iiToNodeInInventory);
        if (terminationPointIdInTopology != null && nodeIdInTopology != null) {
            InstanceIdentifier<org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.topology.Node> iiToTopologyNode = provideIIToTopologyNode(nodeIdInTopology);
            return iiToTopologyNode.builder().child(TerminationPoint.class, new TerminationPointKey(terminationPointIdInTopology)).build();
        } else {
            LOG.debug("Value of termination point ID in topology is null. Instance identifier to topology can't be built");
            return null;
        }
    }

    private static TpId provideTopologyTerminationPointId(final InstanceIdentifier<FlowCapableNodeConnector> iiToNodeInInventory) {
        NodeConnectorKey inventoryNodeConnectorKey = iiToNodeInInventory.firstKeyOf(NodeConnector.class);
        if (inventoryNodeConnectorKey != null) {
            return new TpId(inventoryNodeConnectorKey.getId().getValue());
        }
        return null;
    }

}
