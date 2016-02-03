/**
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.applications.inventory.manager;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.util.concurrent.CheckedFuture;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import java.util.concurrent.TimeUnit;
import org.opendaylight.controller.md.sal.binding.api.ReadWriteTransaction;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNode;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNodeConnector;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNodeConnectorUpdated;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNodeUpdated;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.Table;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.TableBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.TableKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.*;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.node.NodeConnector;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.node.NodeConnectorBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.node.NodeConnectorKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier.InstanceIdentifierBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class NodeChangeCommiter implements OpendaylightInventoryListener {
    private static final Logger LOG = LoggerFactory.getLogger(NodeChangeCommiter.class);

    private final FlowCapableInventoryProvider manager;

    // cache for nodes which were deleted, we get more than one nodeRemoved notification
    private Cache<NodeRef, Boolean> deletedNodeCache =
            CacheBuilder.newBuilder().maximumSize(10000).expireAfterWrite(10, TimeUnit.SECONDS).build();

    // cache for node-connectors which were deleted, we get more than one nodeConnectorRemoved notification
    private Cache<NodeConnectorRef, Boolean> deletedNodeConnectorCache =
            CacheBuilder.newBuilder().maximumSize(1000000).expireAfterWrite(10, TimeUnit.SECONDS).build();

    public NodeChangeCommiter(final FlowCapableInventoryProvider manager) {
        this.manager = Preconditions.checkNotNull(manager);
    }

    @Override
    public synchronized void onNodeConnectorRemoved(final NodeConnectorRemoved connector) {
        if(deletedNodeConnectorCache.getIfPresent(connector.getNodeConnectorRef()) == null){
            deletedNodeConnectorCache.put(connector.getNodeConnectorRef(), Boolean.TRUE);
        } else {
            //its been noted that creating an operation for already removed node-connectors, fails
            // the entire transaction chain, there by failing deserving removals
            LOG.debug("Already received notification to remove nodeConnector, {} - Ignored",
                    connector.getNodeConnectorRef().getValue());
            return;
        }

        if(!manager.deleteDeviceData(getNodeId(connector.getNodeConnectorRef().getValue()))) { return; }

        LOG.debug("Node connector removed notification received, {}", connector.getNodeConnectorRef().getValue());
        manager.enqueue(new InventoryOperation() {
            @Override
            public void applyOperation(final ReadWriteTransaction tx) {
                final NodeConnectorRef ref = connector.getNodeConnectorRef();
                LOG.debug("removing node connector {} ", ref.getValue());
                tx.delete(LogicalDatastoreType.OPERATIONAL, ref.getValue());
            }
        });
    }

    @Override
    public synchronized void onNodeConnectorUpdated(final NodeConnectorUpdated connector) {
        if (deletedNodeConnectorCache.getIfPresent(connector.getNodeConnectorRef()) != null){
            deletedNodeConnectorCache.invalidate(connector.getNodeConnectorRef());
        }

        LOG.debug("Node connector updated notification received.");
        manager.enqueue(new InventoryOperation() {
            @Override
            public void applyOperation(final ReadWriteTransaction tx) {
                final NodeConnectorRef ref = connector.getNodeConnectorRef();
                final NodeConnectorBuilder data = new NodeConnectorBuilder(connector);
                data.setKey(new NodeConnectorKey(connector.getId()));

                final FlowCapableNodeConnectorUpdated flowConnector = connector
                        .getAugmentation(FlowCapableNodeConnectorUpdated.class);
                if (flowConnector != null) {
                    final FlowCapableNodeConnector augment = InventoryMapping.toInventoryAugment(flowConnector);
                    data.addAugmentation(FlowCapableNodeConnector.class, augment);
                }
                InstanceIdentifier<NodeConnector> value = (InstanceIdentifier<NodeConnector>) ref.getValue();
                LOG.debug("updating node connector : {}.", value);
                NodeConnector build = data.build();
                tx.merge(LogicalDatastoreType.OPERATIONAL, value, build, true);
            }
        });
    }

    @Override
    public synchronized void onNodeRemoved(final NodeRemoved node) {

        if(deletedNodeCache.getIfPresent(node.getNodeRef()) == null){
            deletedNodeCache.put(node.getNodeRef(), Boolean.TRUE);
        } else {
            //its been noted that creating an operation for already removed node, fails
            // the entire transaction chain, there by failing deserving removals
            LOG.debug("Already received notification to remove node, {} - Ignored",
                    node.getNodeRef().getValue());
            return;
        }

        if(!manager.deleteDeviceData(getNodeId(node.getNodeRef().getValue()))) { return; }

        LOG.debug("Node removed notification received, {}", node.getNodeRef().getValue());
        manager.enqueue(new InventoryOperation() {
            @Override
            public void applyOperation(final ReadWriteTransaction tx) {
                final NodeRef ref = node.getNodeRef();
                LOG.debug("removing node : {}", ref.getValue());
                tx.delete(LogicalDatastoreType.OPERATIONAL, ref.getValue());
            }
        });
    }

    @Override
    public synchronized void onNodeUpdated(final NodeUpdated node) {
        if (deletedNodeCache.getIfPresent(node.getNodeRef()) != null){
            deletedNodeCache.invalidate(node.getNodeRef());
        }

        final FlowCapableNodeUpdated flowNode = node.getAugmentation(FlowCapableNodeUpdated.class);
        if (flowNode == null) {
            return;
        }
        LOG.debug("Node updated notification received,{}", node.getNodeRef().getValue());
        manager.enqueue(new InventoryOperation() {
            @Override
            public void applyOperation(ReadWriteTransaction tx) {
                final NodeRef ref = node.getNodeRef();
                @SuppressWarnings("unchecked")
                InstanceIdentifierBuilder<Node> builder = ((InstanceIdentifier<Node>) ref.getValue()).builder();
                InstanceIdentifierBuilder<FlowCapableNode> augmentation = builder.augmentation(FlowCapableNode.class);
                final InstanceIdentifier<FlowCapableNode> path = augmentation.build();
                CheckedFuture<Optional<FlowCapableNode>, ?> readFuture = tx.read(LogicalDatastoreType.OPERATIONAL, path);
                Futures.addCallback(readFuture, new FutureCallback<Optional<FlowCapableNode>>() {
                    @Override
                    public void onSuccess(Optional<FlowCapableNode> optional) {
                        enqueueWriteNodeDataTx(node, flowNode, path);
                        if (!optional.isPresent()) {
                            enqueuePutTable0Tx(ref);
                        }
                    }

                    @Override
                    public void onFailure(Throwable throwable) {
                        LOG.debug(String.format("Can't retrieve node data for node %s. Writing node data with table0.", node));
                        enqueueWriteNodeDataTx(node, flowNode, path);
                        enqueuePutTable0Tx(ref);
                    }
                });
            }
        });
    }

    private void enqueueWriteNodeDataTx(final NodeUpdated node, final FlowCapableNodeUpdated flowNode, final InstanceIdentifier<FlowCapableNode> path) {
        manager.enqueue(new InventoryOperation() {
            @Override
            public void applyOperation(final ReadWriteTransaction tx) {
                final FlowCapableNode augment = InventoryMapping.toInventoryAugment(flowNode);
                LOG.debug("updating node :{} ", path);
                tx.merge(LogicalDatastoreType.OPERATIONAL, path, augment, true);
            }
        });
    }

    private void enqueuePutTable0Tx(final NodeRef ref) {
        manager.enqueue(new InventoryOperation() {
            @Override
            public void applyOperation(ReadWriteTransaction tx) {
                final TableKey tKey = new TableKey((short) 0);
                final InstanceIdentifier<Table> tableIdentifier =
                        ((InstanceIdentifier<Node>) ref.getValue()).augmentation(FlowCapableNode.class).child(Table.class, new TableKey(tKey));
                TableBuilder tableBuilder = new TableBuilder();
                Table table0 = tableBuilder.setId((short) 0).build();
                LOG.debug("writing table :{} ", tableIdentifier);
                tx.merge(LogicalDatastoreType.OPERATIONAL, tableIdentifier, table0, true);
            }
        });
    }

    private NodeId getNodeId(InstanceIdentifier<?> iid) {
        return iid.firstKeyOf(Node.class).getId();
    }
}
