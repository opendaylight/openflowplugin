/*
 * Copyright (c) 2014, 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.learningswitch;

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
import org.opendaylight.yangtools.binding.PropertyIdentifier;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
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
    public static InstanceIdentifier<Node> createNodePath(final NodeId nodeId) {
        return InstanceIdentifier.builder(Nodes.class)
                .child(Node.class, new NodeKey(nodeId))
                .build();
    }

    /**
     * Shorten's node child path to node path.
     *
     * @param nodeChild child of node, from which we want node path.
     */
    public static InstanceIdentifier<Node> getNodePath(final InstanceIdentifier<?> nodeChild) {
        return nodeChild.firstIdentifierOf(Node.class);
    }

    /**
     * Creates a table path by appending table specific location to node path.
     *
     * @param nodePath the node path
     * @param tableKey the table yey
     */
    public static InstanceIdentifier<Table> createTablePath(final InstanceIdentifier<Node> nodePath,
            final TableKey tableKey) {
        return nodePath.augmentation(FlowCapableNode.class).child(Table.class, tableKey);
    }

    /**
     * Creates a path for particular flow, by appending flow-specific information
     * to table path.
     *
     * @param tablePath the table path
     * @param flowKey the flow key
     * @return path to flow
     */
    public static InstanceIdentifier<Flow> createFlowPath(final InstanceIdentifier<Table> tablePath,
            final FlowKey flowKey) {
        return tablePath.child(Flow.class, flowKey);
    }

    /**
     * Extract table id from table path.
     *
     * @param tablePath the table path
     */
    public static Uint8 getTableId(final InstanceIdentifier<Table> tablePath) {
        return tablePath.firstKeyOf(Table.class).getId();
    }

    /**
     * Extracts NodeConnectorKey from node connector path.
     */
    public static NodeConnectorKey getNodeConnectorKey(final BindingInstanceIdentifier nodeConnectorPath) {
        return switch (nodeConnectorPath) {
            case DataObjectIdentifier<?> doi -> doi.toLegacy().firstKeyOf(NodeConnector.class);
            case PropertyIdentifier<?, ?> pi -> throw new IllegalArgumentException("Unexpected " + pi);
        };
    }

    public static InstanceIdentifier<NodeConnector> createNodeConnectorPath(final InstanceIdentifier<Node> nodeKey,
            final NodeConnectorKey nodeConnectorKey) {
        return nodeKey.child(NodeConnector.class,nodeConnectorKey);
    }
}
