/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.applications.topology.manager;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.opendaylight.mdsal.binding.api.DataBroker;
import org.opendaylight.mdsal.binding.api.DataTreeModification;
import org.opendaylight.mdsal.common.api.LogicalDatastoreType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNodeConnector;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeConnectorRef;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.Nodes;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.node.NodeConnector;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.node.NodeConnectorKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.topology.inventory.rev131030.InventoryNodeConnectorBuilder;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.NodeId;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.TpId;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.topology.node.TerminationPoint;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.topology.node.TerminationPointBuilder;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.topology.node.TerminationPointKey;
import org.opendaylight.yangtools.binding.DataObjectIdentifier.WithKey;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
@Component(service = { })
public class TerminationPointChangeListenerImpl extends DataTreeChangeListenerImpl<FlowCapableNodeConnector> {
    private static final Logger LOG = LoggerFactory.getLogger(TerminationPointChangeListenerImpl.class);

    @Inject
    @Activate
    public TerminationPointChangeListenerImpl(@Reference final DataBroker dataBroker,
            @Reference final OperationProcessor operationProcessor) {
        super(operationProcessor, dataBroker,
              InstanceIdentifier.builder(Nodes.class).child(Node.class).child(NodeConnector.class)
                      .augmentation(FlowCapableNodeConnector.class).build());
    }

    @Override
    public void onDataTreeChanged(final List<DataTreeModification<FlowCapableNodeConnector>> modifications) {
        for (DataTreeModification<FlowCapableNodeConnector> modification : modifications) {
            switch (modification.getRootNode().modificationType()) {
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

    private void processRemovedTerminationPoints(final DataTreeModification<FlowCapableNodeConnector> modification) {
        final var removedNode = modification.path().toLegacy();
        final TpId terminationPointId = provideTopologyTerminationPointId(removedNode);
        final var iiToTopologyTerminationPoint = provideIIToTopologyTerminationPoint(terminationPointId, removedNode);

        if (iiToTopologyTerminationPoint != null) {
            final var node = iiToTopologyTerminationPoint.toLegacy().firstIdentifierOf(
                    org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network
                            .topology.topology.Node.class)
                .toIdentifier();
            operationProcessor.enqueueOperation(manager -> {
                Optional<org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network
                        .topology.topology.Node>
                        nodeOptional = Optional.empty();
                try {
                    nodeOptional = manager.readFromTransaction(LogicalDatastoreType.OPERATIONAL, node).get();
                } catch (InterruptedException | ExecutionException e) {
                    LOG.warn("Error occurred when trying to read NodeConnector: {}", e.getMessage());
                    LOG.debug("Error occurred when trying to read NodeConnector.. ", e);
                }
                if (nodeOptional.isPresent()) {
                    TopologyManagerUtil.removeAffectedLinks(terminationPointId, manager, II_TO_TOPOLOGY);
                    manager.addDeleteOperationToTxChain(LogicalDatastoreType.OPERATIONAL,
                                                         iiToTopologyTerminationPoint);
                }
            });
        } else {
            LOG.debug(
                    "Instance identifier to inventory wasn't translated to topology while deleting termination point.");
        }
    }

    private void processUpdatedTerminationPoints(final DataTreeModification<FlowCapableNodeConnector> modification) {
        // TODO Auto-generated method stub
    }

    private void processAddedTerminationPoints(final DataTreeModification<FlowCapableNodeConnector> modification) {
        final var iiToNodeInInventory = modification.path().toLegacy();
        TpId terminationPointIdInTopology = provideTopologyTerminationPointId(iiToNodeInInventory);
        if (terminationPointIdInTopology != null) {
            var iiToTopologyTerminationPoint = provideIIToTopologyTerminationPoint(
                    terminationPointIdInTopology, iiToNodeInInventory);
            TerminationPoint point = prepareTopologyTerminationPoint(terminationPointIdInTopology, iiToNodeInInventory);
            sendToTransactionChain(point, iiToTopologyTerminationPoint);
            removeLinks(modification.getRootNode().dataAfter(), point);
        } else {
            LOG.debug("Inventory node connector key is null. Data can't be written to topology termination point");
        }
    }

    private void removeLinks(final FlowCapableNodeConnector flowCapNodeConnector, final TerminationPoint point) {
        operationProcessor.enqueueOperation(manager -> {
            if (flowCapNodeConnector.getState() != null && flowCapNodeConnector.getState().getLinkDown()
                    || flowCapNodeConnector.getConfiguration() != null
                        && flowCapNodeConnector.getConfiguration().getPORTDOWN()) {
                TopologyManagerUtil.removeAffectedLinks(point.getTpId(), manager, II_TO_TOPOLOGY);
            }
        });
    }

    private static TerminationPoint prepareTopologyTerminationPoint(final TpId terminationPointIdInTopology,
            final InstanceIdentifier<FlowCapableNodeConnector> iiToNodeInInventory) {
        return new TerminationPointBuilder()
            .setTpId(terminationPointIdInTopology)
            .addAugmentation(new InventoryNodeConnectorBuilder()
                .setInventoryNodeConnectorRef(
                    new NodeConnectorRef(iiToNodeInInventory.firstIdentifierOf(NodeConnector.class).toIdentifier()))
                .build())
            .build();
    }

    private WithKey<TerminationPoint, TerminationPointKey> provideIIToTopologyTerminationPoint(
            final TpId terminationPointIdInTopology,
            final InstanceIdentifier<FlowCapableNodeConnector> iiToNodeInInventory) {
        NodeId nodeIdInTopology = provideTopologyNodeId(iiToNodeInInventory);
        if (terminationPointIdInTopology != null && nodeIdInTopology != null) {
            return provideIIToTopologyNode(nodeIdInTopology).toBuilder()
                .child(TerminationPoint.class, new TerminationPointKey(terminationPointIdInTopology))
                .build();
        } else {
            LOG.debug(
                "Value of termination point ID in topology is null. Instance identifier to topology cannot be built");
            return null;
        }
    }

    private static TpId provideTopologyTerminationPointId(
            final InstanceIdentifier<FlowCapableNodeConnector> iiToNodeInInventory) {
        NodeConnectorKey inventoryNodeConnectorKey = iiToNodeInInventory.firstKeyOf(NodeConnector.class);
        if (inventoryNodeConnectorKey != null) {
            return new TpId(inventoryNodeConnectorKey.getId().getValue());
        }
        return null;
    }

}
