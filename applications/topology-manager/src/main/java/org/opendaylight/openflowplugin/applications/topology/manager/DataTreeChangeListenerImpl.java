/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.applications.topology.manager;

import static java.util.Objects.requireNonNull;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.mdsal.binding.api.DataBroker;
import org.opendaylight.mdsal.binding.api.DataTreeChangeListener;
import org.opendaylight.mdsal.binding.api.DataTreeIdentifier;
import org.opendaylight.mdsal.common.api.LogicalDatastoreType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.NodeKey;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.NetworkTopology;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.NodeId;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.TopologyId;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.Topology;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.TopologyKey;
import org.opendaylight.yangtools.binding.DataObject;
import org.opendaylight.yangtools.binding.DataObjectIdentifier;
import org.opendaylight.yangtools.binding.DataObjectIdentifier.WithKey;
import org.opendaylight.yangtools.concepts.Registration;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public abstract class DataTreeChangeListenerImpl<T extends DataObject> implements DataTreeChangeListener<T>,
        AutoCloseable {
    static final WithKey<Topology, TopologyKey> II_TO_TOPOLOGY = DataObjectIdentifier.builder(NetworkTopology.class)
        .child(Topology.class, new TopologyKey(new TopologyId(FlowCapableTopologyProvider.TOPOLOGY_ID)))
        .build();

    protected final Registration listenerRegistration;
    protected OperationProcessor operationProcessor;

    @SuppressFBWarnings(value = "MC_OVERRIDABLE_METHOD_CALL_IN_CONSTRUCTOR",
        justification = "'this' passed to registerDataTreeChangeListener")
    protected DataTreeChangeListenerImpl(final OperationProcessor operationProcessor, final DataBroker dataBroker,
            final InstanceIdentifier<T> ii) {
        this.operationProcessor = requireNonNull(operationProcessor);
        listenerRegistration = dataBroker.registerDataTreeChangeListener(
            DataTreeIdentifier.of(LogicalDatastoreType.OPERATIONAL, ii), this);
    }

    @Override
    public void close() {
        listenerRegistration.close();
    }

    <O extends DataObject> void sendToTransactionChain(final O node, final DataObjectIdentifier<O> iiToTopologyNode) {
        operationProcessor.enqueueOperation(
            manager -> manager.mergeToTransaction(LogicalDatastoreType.OPERATIONAL, iiToTopologyNode, node, true));
    }

    @NonNull WithKey<
            org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.topology
            .Node,
            org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.topology
            .NodeKey> provideIIToTopologyNode(final NodeId nodeIdInTopology) {
        return II_TO_TOPOLOGY.toBuilder()
            .child(org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology
                    .topology.Node.class,
                new org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology
                    .topology.NodeKey(nodeIdInTopology))
            .build();
    }

    NodeId provideTopologyNodeId(final InstanceIdentifier<T> iiToNodeInInventory) {
        final NodeKey inventoryNodeKey = iiToNodeInInventory.firstKeyOf(Node.class);
        if (inventoryNodeKey != null) {
            return new NodeId(inventoryNodeKey.getId().getValue());
        }
        return null;
    }
}
