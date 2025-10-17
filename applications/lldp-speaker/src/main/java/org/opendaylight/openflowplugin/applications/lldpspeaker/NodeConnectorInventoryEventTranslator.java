/*
 * Copyright (c) 2014 Pacnet and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.applications.lldpspeaker;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.mdsal.binding.api.DataBroker;
import org.opendaylight.mdsal.binding.api.DataObjectDeleted;
import org.opendaylight.mdsal.binding.api.DataObjectModification.WithDataAfter;
import org.opendaylight.mdsal.binding.api.DataTreeChangeListener;
import org.opendaylight.mdsal.binding.api.DataTreeModification;
import org.opendaylight.mdsal.common.api.LogicalDatastoreType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNodeConnector;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.Nodes;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.node.NodeConnector;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.node.NodeConnectorKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yangtools.binding.DataObjectIdentifier.WithKey;
import org.opendaylight.yangtools.binding.DataObjectReference;
import org.opendaylight.yangtools.concepts.Registration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * NodeConnectorInventoryEventTranslator is listening for changes in inventory operational DOM tree
 * and update LLDPSpeaker and topology.
 */
public final class NodeConnectorInventoryEventTranslator
        implements DataTreeChangeListener<FlowCapableNodeConnector>, AutoCloseable {
    private static final Logger LOG = LoggerFactory.getLogger(NodeConnectorInventoryEventTranslator.class);

    private final HashSet<WithKey<NodeConnector, NodeConnectorKey>> nodeConnectors = new HashSet<>();
    private final List<NodeConnectorEventsObserver> observers;
    private final Registration registration;

    public NodeConnectorInventoryEventTranslator(final DataBroker dataBroker,
            final NodeConnectorEventsObserver... observers) {
        this.observers = Arrays.stream(observers).distinct().collect(Collectors.toUnmodifiableList());

        registration = dataBroker.registerTreeChangeListener(LogicalDatastoreType.OPERATIONAL,
            DataObjectReference.builder(Nodes.class)
                .child(Node.class)
                .child(NodeConnector.class)
                .augmentation(FlowCapableNodeConnector.class)
            .build(), this);
        LOG.info("NodeConnectorInventoryEventTranslator has started.");
    }

    @Override
    public void close() {
        if (registration != null) {
            registration.close();
        }
    }

    @Override
    public void onDataTreeChanged(final List<DataTreeModification<FlowCapableNodeConnector>> modifications) {
        for (var modification : modifications) {
            @SuppressWarnings("unchecked")
            final var nodeConnectorInstanceId = (WithKey<NodeConnector, NodeConnectorKey>)
                modification.path().trimTo(NodeConnector.class);

            switch (modification.getRootNode()) {
                case DataObjectDeleted<?> deleted -> {
                    LOG.trace("Node connectors in inventory removed");
                    removeNodeConnector(nodeConnectorInstanceId);
                }
                case WithDataAfter<FlowCapableNodeConnector> present -> {
                    LOG.trace("Node connectors in inventory updated");
                    final var flowConnector = present.dataAfter();
                    if (isPortDown(flowConnector)) {
                        removeNodeConnector(nodeConnectorInstanceId);
                    } else if (nodeConnectors.add(nodeConnectorInstanceId)) {
                        for (var observer : observers) {
                            observer.onNodeConnectorUp(nodeConnectorInstanceId, flowConnector);
                        }
                    }
                }
            }
        }
    }


    private static boolean isPortDown(final FlowCapableNodeConnector flowCapableNodeConnector) {
        final var portState = flowCapableNodeConnector.getState();
        final var portConfig = flowCapableNodeConnector.getConfiguration();
        return portState != null && portState.getLinkDown() || portConfig != null && portConfig.getPORTDOWN();
    }

    private void removeNodeConnector(final @NonNull WithKey<NodeConnector, NodeConnectorKey> nodeConnectorInstanceId) {
        if (nodeConnectors.remove(nodeConnectorInstanceId)) {
            for (var observer : observers) {
                observer.onNodeConnectorDown(nodeConnectorInstanceId);
            }
        }
    }
}
