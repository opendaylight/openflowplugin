/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.applications.topology.manager;

import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeConnectorId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeConnectorRef;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeRef;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.node.NodeConnector;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.node.NodeConnectorKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.NodeKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.topology.inventory.rev131030.InventoryNodeBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.topology.inventory.rev131030.InventoryNodeConnectorBuilder;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.LinkId;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.NodeId;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.TpId;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.link.attributes.DestinationBuilder;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.link.attributes.SourceBuilder;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.topology.Link;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.topology.LinkBuilder;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.topology.NodeBuilder;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.topology.node.TerminationPoint;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.topology.node.TerminationPointBuilder;
import org.opendaylight.yangtools.binding.BindingInstanceIdentifier;
import org.opendaylight.yangtools.binding.DataObjectReference;
import org.opendaylight.yangtools.binding.EntryObject;
import org.opendaylight.yangtools.binding.Key;

public final class FlowCapableNodeMapping {
    private FlowCapableNodeMapping() {
        // Hidden on purpose
    }

    public static NodeKey getNodeKey(final NodeRef ref) {
        return firstKeyOf(ref.getValue(), Node.class);
    }

    public static NodeKey getNodeKey(final NodeConnectorRef ref) {
        return firstKeyOf(ref.getValue(), Node.class);
    }

    public static NodeConnectorKey getNodeConnectorKey(final NodeConnectorRef ref) {
        return firstKeyOf(ref.getValue(), NodeConnector.class);
    }

    private static <T extends EntryObject<T, K>, K extends Key<T>> K firstKeyOf(final BindingInstanceIdentifier id,
            final Class<T> clazz) {
        return ((DataObjectReference<?>) id).toLegacy().firstKeyOf(clazz);
    }

    public static NodeId toTopologyNodeId(
            final org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId nodeId) {
        return new NodeId(nodeId);
    }

    private static NodeId toTopologyNodeId(final NodeConnectorRef source) {
        return toTopologyNodeId(getNodeKey(source).getId());
    }

    public static TpId toTerminationPointId(final NodeConnectorId id) {
        return new TpId(id);
    }

    private static TpId toTerminationPointId(final NodeConnectorRef source) {
        return toTerminationPointId(getNodeConnectorKey(source).getId());
    }

    public static org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology
            .topology.Node toTopologyNode(
            final NodeId nodeId, final NodeRef invNodeRef) {
        return new NodeBuilder().setNodeId(nodeId).addAugmentation(new InventoryNodeBuilder()
                .setInventoryNodeRef(invNodeRef).build()).build();
    }

    public static TerminationPoint toTerminationPoint(final TpId id, final NodeConnectorRef invNodeConnectorRef) {
        return new TerminationPointBuilder()
                .setTpId(id)
                .addAugmentation(new InventoryNodeConnectorBuilder()
                    .setInventoryNodeConnectorRef(invNodeConnectorRef)
                    .build())
                .build();
    }

    public static Link toTopologyLink(
            final org.opendaylight.yang.gen.v1.urn.opendaylight.flow.topology.discovery.rev130819.Link link) {
        return new LinkBuilder()
                .setSource(new SourceBuilder()
                        .setSourceNode(toTopologyNodeId(link.getSource()))
                        .setSourceTp(toTerminationPointId(link.getSource()))
                        .build())
                .setDestination(new DestinationBuilder()
                        .setDestNode(toTopologyNodeId(link.getDestination()))
                        .setDestTp(toTerminationPointId(link.getDestination()))
                        .build())
                .setLinkId(new LinkId(getNodeConnectorKey(link.getSource()).getId()))
                .build();
    }
}
