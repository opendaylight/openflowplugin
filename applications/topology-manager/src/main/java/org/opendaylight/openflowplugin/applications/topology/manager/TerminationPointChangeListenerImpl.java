/**
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.applications.topology.manager;

import com.google.common.base.Optional;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.ReadWriteTransaction;
import org.opendaylight.controller.md.sal.common.api.data.AsyncDataChangeEvent;
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
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TerminationPointChangeListenerImpl extends DataChangeListenerImpl {
    private static final Logger LOG = LoggerFactory.getLogger(TerminationPointChangeListenerImpl.class);

    public TerminationPointChangeListenerImpl(final DataBroker dataBroker, final OperationProcessor operationProcessor) {
        super(operationProcessor, dataBroker, InstanceIdentifier.builder(Nodes.class).child(Node.class)
                .child(NodeConnector.class).augmentation(FlowCapableNodeConnector.class).build());
        this.operationProcessor = operationProcessor;
    }

    @Override
    public void onDataChanged(final AsyncDataChangeEvent<InstanceIdentifier<?>, DataObject> change) {
        processAddedTerminationPoints(change.getCreatedData());
        processUpdatedTerminationPoints(change.getUpdatedData());
        processRemovedTerminationPoints(change.getRemovedPaths());
    }

    /**
     * @param removedPaths
     */
    private void processRemovedTerminationPoints(final Set<InstanceIdentifier<?>> removedNodes) {
        for (final InstanceIdentifier<?> removedNode : removedNodes) {
            final TpId terminationPointId = provideTopologyTerminationPointId(removedNode);
            final InstanceIdentifier<TerminationPoint> iiToTopologyTerminationPoint = provideIIToTopologyTerminationPoint(
                    terminationPointId, removedNode);

            if (iiToTopologyTerminationPoint != null) {
                final InstanceIdentifier<org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.topology.Node> node = iiToTopologyTerminationPoint.firstIdentifierOf(org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.topology.Node.class);
                operationProcessor.enqueueOperation(new TopologyOperation() {

                    @Override
                    public void applyOperation(final ReadWriteTransaction transaction) {
                        Optional<org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.topology.Node> nodeOptional = Optional.absent();
                        try {
                            nodeOptional = transaction.read(LogicalDatastoreType.OPERATIONAL, node).checkedGet();
                        } catch (ReadFailedException e) {
                            LOG.warn("Error occured when trying to read NodeConnector: {}", e.getMessage());
                            LOG.debug("Error occured when trying to read NodeConnector.. ", e);
                        }
                        if (nodeOptional.isPresent()) {
                            TopologyManagerUtil.removeAffectedLinks(terminationPointId, transaction, II_TO_TOPOLOGY);
                            transaction.delete(LogicalDatastoreType.OPERATIONAL, iiToTopologyTerminationPoint);
                        }
                    }
                });

            } else {
                LOG.debug("Instance identifier to inventory wasn't translated to topology while deleting termination point.");
            }
        }
    }

    /**
     * @param updatedData
     */
    private void processUpdatedTerminationPoints(final Map<InstanceIdentifier<?>, DataObject> updatedData) {
        // TODO Auto-generated method stub
    }

    private void processAddedTerminationPoints(final Map<InstanceIdentifier<?>, DataObject> addedDatas) {
        for (Entry<InstanceIdentifier<?>, DataObject> addedData : addedDatas.entrySet()) {
            createData(addedData.getKey(), addedData.getValue());
        }
    }

    protected void createData(final InstanceIdentifier<?> iiToNodeInInventory, final DataObject data) {
        TpId terminationPointIdInTopology = provideTopologyTerminationPointId(iiToNodeInInventory);
        if (terminationPointIdInTopology != null) {
            InstanceIdentifier<TerminationPoint> iiToTopologyTerminationPoint = provideIIToTopologyTerminationPoint(
                    terminationPointIdInTopology, iiToNodeInInventory);
            TerminationPoint point = prepareTopologyTerminationPoint(terminationPointIdInTopology, iiToNodeInInventory);
            sendToTransactionChain(point, iiToTopologyTerminationPoint);
            if (data instanceof FlowCapableNodeConnector) {
                removeLinks((FlowCapableNodeConnector) data, point);
            }

        } else {
            LOG.debug("Inventory node connector key is null. Data can't be written to topology termination point");
        }
    }

    /**
     * @param data
     */
    private void removeLinks(final FlowCapableNodeConnector flowCapNodeConnector, final TerminationPoint point) {
        operationProcessor.enqueueOperation(new TopologyOperation() {

            @Override
            public void applyOperation(final ReadWriteTransaction transaction) {
                if ((flowCapNodeConnector.getState() != null && flowCapNodeConnector.getState().isLinkDown())
                        || (flowCapNodeConnector.getConfiguration() != null && flowCapNodeConnector.getConfiguration().isPORTDOWN())) {
                    TopologyManagerUtil.removeAffectedLinks(point.getTpId(), transaction, II_TO_TOPOLOGY);
                }
            }
        });
    }

    private static TerminationPoint prepareTopologyTerminationPoint(final TpId terminationPointIdInTopology,
            final InstanceIdentifier<?> iiToNodeInInventory) {
        final InventoryNodeConnector inventoryNodeConnector = new InventoryNodeConnectorBuilder()
                .setInventoryNodeConnectorRef(
                        new NodeConnectorRef(iiToNodeInInventory.firstIdentifierOf(NodeConnector.class))).build();

        final TerminationPointBuilder terminationPointBuilder = new TerminationPointBuilder();
        terminationPointBuilder.setTpId(terminationPointIdInTopology);
        terminationPointBuilder.addAugmentation(InventoryNodeConnector.class, inventoryNodeConnector);
        return terminationPointBuilder.build();
    }

    /**
     * @param terminationPointIdInTopology
     * @return
     */
    private InstanceIdentifier<TerminationPoint> provideIIToTopologyTerminationPoint(final TpId terminationPointIdInTopology,
            final InstanceIdentifier<?> iiToNodeInInventory) {
        NodeId nodeIdInTopology = provideTopologyNodeId(iiToNodeInInventory);
        if (terminationPointIdInTopology != null && nodeIdInTopology != null) {
            InstanceIdentifier<org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.topology.Node> iiToTopologyNode = provideIIToTopologyNode(nodeIdInTopology);
            return iiToTopologyNode.builder().child(TerminationPoint.class, new TerminationPointKey(terminationPointIdInTopology)).build();
        } else {
            LOG.debug("Value of termination point ID in topology is null. Instance identifier to topology can't be built");
            return null;
        }
    }

    /**
     * @param iiToNodeInInventory
     * @return
     */
    private static TpId provideTopologyTerminationPointId(final InstanceIdentifier<?> iiToNodeInInventory) {
        NodeConnectorKey inventoryNodeConnectorKey = iiToNodeInInventory.firstKeyOf(NodeConnector.class,
                NodeConnectorKey.class);
        if (inventoryNodeConnectorKey != null) {
            return new TpId(inventoryNodeConnectorKey.getId().getValue());
        }
        return null;
    }

}
