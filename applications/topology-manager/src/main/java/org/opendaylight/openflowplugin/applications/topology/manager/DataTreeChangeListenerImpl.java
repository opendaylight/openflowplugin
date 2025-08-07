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
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.NetworkTopology;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.NodeId;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.TopologyId;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.Topology;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.TopologyKey;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.topology.Node;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.topology.NodeKey;
import org.opendaylight.yangtools.binding.DataObject;
import org.opendaylight.yangtools.binding.DataObjectIdentifier;
import org.opendaylight.yangtools.binding.DataObjectIdentifier.WithKey;
import org.opendaylight.yangtools.concepts.Registration;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

abstract class DataTreeChangeListenerImpl<T extends DataObject> implements DataTreeChangeListener<T>,
        AutoCloseable {
    static final WithKey<Topology, TopologyKey> II_TO_TOPOLOGY = DataObjectIdentifier.builder(NetworkTopology.class)
        .child(Topology.class, new TopologyKey(new TopologyId(FlowCapableTopologyProvider.TOPOLOGY_ID)))
        .build();

    final Registration listenerRegistration;
    final OperationProcessor operationProcessor;

    @SuppressFBWarnings(value = "MC_OVERRIDABLE_METHOD_CALL_IN_CONSTRUCTOR",
        justification = "'this' passed to registerDataTreeChangeListener")
    DataTreeChangeListenerImpl(final OperationProcessor operationProcessor, final DataBroker dataBroker,
            final InstanceIdentifier<T> ii) {
        this.operationProcessor = requireNonNull(operationProcessor);
        listenerRegistration = dataBroker.registerDataTreeChangeListener(
            DataTreeIdentifier.of(LogicalDatastoreType.OPERATIONAL, ii), this);
    }

    @Override
    public void close() {
        listenerRegistration.close();
    }

    final <O extends DataObject> void sendToTransactionChain(final O node, final DataObjectIdentifier<O> nodePath) {
        operationProcessor.enqueueOperation(
            manager -> manager.mergeToTransaction(LogicalDatastoreType.OPERATIONAL, nodePath, node, true));
    }

    static final @NonNull WithKey<Node, NodeKey> provideIIToTopologyNode(final NodeId nodeIdInTopology) {
        return II_TO_TOPOLOGY.toBuilder().child(Node.class, new NodeKey(nodeIdInTopology)).build();
    }

    final NodeId provideTopologyNodeId(final InstanceIdentifier<T> iiToNodeInInventory) {
        final var key = iiToNodeInInventory.firstKeyOf(org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819
            .nodes.Node.class);
        return key == null ? null : new NodeId(key.getId().getValue());
    }
}
