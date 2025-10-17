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
import org.opendaylight.mdsal.binding.api.DataObjectDeleted;
import org.opendaylight.mdsal.binding.api.DataObjectModified;
import org.opendaylight.mdsal.binding.api.DataObjectWritten;
import org.opendaylight.mdsal.binding.api.DataTreeModification;
import org.opendaylight.mdsal.common.api.LogicalDatastoreType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNodeConnector;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeConnectorRef;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.Nodes;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.node.NodeConnector;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.topology.inventory.rev131030.InventoryNodeConnectorBuilder;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.NodeId;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.TpId;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.topology.node.TerminationPoint;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.topology.node.TerminationPointBuilder;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.topology.node.TerminationPointKey;
import org.opendaylight.yangtools.binding.DataObjectIdentifier;
import org.opendaylight.yangtools.binding.DataObjectIdentifier.WithKey;
import org.opendaylight.yangtools.binding.DataObjectReference;
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
        super(operationProcessor, dataBroker, DataObjectReference.builder(Nodes.class)
            .child(Node.class)
            .child(NodeConnector.class)
            .augmentation(FlowCapableNodeConnector.class)
            .build());
    }

    @Deactivate
    @PreDestroy
    @Override
    public void close() {
        super.close();
    }

    @Override
    public void onDataTreeChanged(final List<DataTreeModification<FlowCapableNodeConnector>> modifications) {
        for (var modification : modifications) {
            switch (modification.getRootNode()) {
                case DataObjectWritten<FlowCapableNodeConnector> written ->
                    processAddedTerminationPoints(modification.path(), written.dataAfter());
                case DataObjectModified<FlowCapableNodeConnector> modified ->
                    processUpdatedTerminationPoints(modification.path());
                case DataObjectDeleted<FlowCapableNodeConnector> deleted ->
                    processRemovedTerminationPoints(modification.path());
            }
        }
    }

    private void processRemovedTerminationPoints(final DataObjectIdentifier<FlowCapableNodeConnector> path) {
        final TpId terminationPointId = provideTopologyTerminationPointId(path);
        final var iiToTopologyTerminationPoint = provideIIToTopologyTerminationPoint(terminationPointId, path);

        if (iiToTopologyTerminationPoint != null) {
            final var node = iiToTopologyTerminationPoint.trimTo(org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang
                .network.topology.rev131021.network.topology.topology.Node.class);
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

    private void processUpdatedTerminationPoints(final DataObjectIdentifier<FlowCapableNodeConnector> path) {
        // TODO Auto-generated method stub
    }

    private void processAddedTerminationPoints(final DataObjectIdentifier<FlowCapableNodeConnector> path,
            final FlowCapableNodeConnector flowCapNodeConnector) {
        TpId terminationPointIdInTopology = provideTopologyTerminationPointId(path);
        if (terminationPointIdInTopology != null) {
            var iiToTopologyTerminationPoint = provideIIToTopologyTerminationPoint(terminationPointIdInTopology, path);
            TerminationPoint point = prepareTopologyTerminationPoint(terminationPointIdInTopology, path);
            sendToTransactionChain(point, iiToTopologyTerminationPoint);
            removeLinks(flowCapNodeConnector, point);
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
            final DataObjectIdentifier<FlowCapableNodeConnector> iiToNodeInInventory) {
        return new TerminationPointBuilder()
            .setTpId(terminationPointIdInTopology)
            .addAugmentation(new InventoryNodeConnectorBuilder()
                .setInventoryNodeConnectorRef(new NodeConnectorRef(iiToNodeInInventory.trimTo(NodeConnector.class)))
                .build())
            .build();
    }

    private WithKey<TerminationPoint, TerminationPointKey> provideIIToTopologyTerminationPoint(
            final TpId terminationPointIdInTopology,
            final DataObjectIdentifier<FlowCapableNodeConnector> iiToNodeInInventory) {
        NodeId nodeIdInTopology = provideTopologyNodeId(iiToNodeInInventory);
        if (terminationPointIdInTopology != null && nodeIdInTopology != null) {
            return provideIIToTopologyNode(nodeIdInTopology).toBuilder()
                .child(TerminationPoint.class, new TerminationPointKey(terminationPointIdInTopology))
                .build();
        }
        LOG.debug("Value of termination point ID in topology is null. Instance identifier to topology cannot be built");
        return null;
    }

    private static TpId provideTopologyTerminationPointId(
            final DataObjectIdentifier<FlowCapableNodeConnector> iiToNodeInInventory) {
        final var inventoryNodeConnectorKey = iiToNodeInInventory.firstKeyOf(NodeConnector.class);
        return inventoryNodeConnectorKey == null ? null : new TpId(inventoryNodeConnectorKey.getId().getValue());
    }
}
