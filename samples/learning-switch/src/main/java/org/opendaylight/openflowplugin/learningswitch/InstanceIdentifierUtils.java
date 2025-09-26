/*
 * Copyright (c) 2014, 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.learningswitch;

import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNode;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.Table;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.TableKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.table.Flow;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.table.FlowKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.Nodes;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.node.NodeConnector;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.node.NodeConnectorKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.NodeKey;
import org.opendaylight.yangtools.binding.BindingInstanceIdentifier;
import org.opendaylight.yangtools.binding.DataObjectIdentifier;
import org.opendaylight.yangtools.binding.DataObjectIdentifier.WithKey;
import org.opendaylight.yangtools.yang.common.Uint8;

public final class InstanceIdentifierUtils {
    private InstanceIdentifierUtils() {
        //hiding constructor for util class
    }

    /**
     * Creates an Instance Identifier (path) for node with specified id.
     *
     * @param nodeId the NodeId
     */
    public static @NonNull WithKey<Node, NodeKey> createNodePath(final NodeId nodeId) {
        return DataObjectIdentifier.builder(Nodes.class).child(Node.class, new NodeKey(nodeId)).build();
    }

    /**
     * Shorten's node child path to node path.
     *
     * @param nodeChild child of node, from which we want node path.
     */
    public static @NonNull DataObjectIdentifier<Node> getNodePath(final DataObjectIdentifier<?> nodeChild) {
        return nodeChild.trimTo(Node.class);
    }

    /**
     * Creates a table path by appending table specific location to node path.
     *
     * @param nodePath the node path
     * @param tableKey the table yey
     */
    public static @NonNull WithKey<Table, TableKey> createTablePath(final DataObjectIdentifier<Node> nodePath,
            final TableKey tableKey) {
        return nodePath.toBuilder().augmentation(FlowCapableNode.class).child(Table.class, tableKey).build();
    }

    /**
     * Creates a path for particular flow, by appending flow-specific information to table path.
     *
     * @param tablePath the table path
     * @param flowKey the flow key
     * @return path to flow
     */
    public static @NonNull WithKey<Flow, FlowKey> createFlowPath(final DataObjectIdentifier<Table> tablePath,
            final FlowKey flowKey) {
        return tablePath.toBuilder().child(Flow.class, flowKey).build();
    }

    /**
     * Extract table id from table path.
     *
     * @param tablePath the table path
     */
    public static @NonNull Uint8 getTableId(final DataObjectIdentifier<Table> tablePath) {
        return tablePath.getFirstKeyOf(Table.class).getId();
    }

    /**
     * Extracts NodeConnectorKey from node connector path.
     */
    public static @NonNull NodeConnectorKey getNodeConnectorKey(final BindingInstanceIdentifier nodeConnectorPath) {
        return nodeConnectorPath.getFirstKeyOf(NodeConnector.class);
    }

    public static @NonNull WithKey<NodeConnector, NodeConnectorKey> createNodeConnectorPath(
            final DataObjectIdentifier<Node> nodeKey, final NodeConnectorKey nodeConnectorKey) {
        return nodeKey.toBuilder().child(NodeConnector.class, nodeConnectorKey).build();
    }
}
