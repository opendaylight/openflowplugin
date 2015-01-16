/*
 * Copyright (c) 2014 Pacnet and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.applications.lldpspeaker;

import com.google.common.collect.ImmutableSet;
import java.util.Map;
import java.util.Set;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.DataChangeListener;
import org.opendaylight.controller.md.sal.common.api.data.AsyncDataBroker;
import org.opendaylight.controller.md.sal.common.api.data.AsyncDataChangeEvent;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNodeConnector;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.port.rev130925.PortConfig;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.port.rev130925.PortState;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.Nodes;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.node.NodeConnector;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yangtools.concepts.ListenerRegistration;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * NodeConnectorInventoryEventTranslator is listening for changes in inventory operational DOM tree
 * and update LLDPSpeaker and topology.
 */
public class NodeConnectorInventoryEventTranslator implements DataChangeListener, AutoCloseable {
    private static final Logger LOG = LoggerFactory.getLogger(NodeConnectorInventoryEventTranslator.class);

    private final ListenerRegistration<DataChangeListener> dataChangeListenerRegistration;
    private final Set<NodeConnectorEventsObserver> observers;

    public NodeConnectorInventoryEventTranslator(DataBroker dataBroker, NodeConnectorEventsObserver... observers) {
        this.observers = ImmutableSet.copyOf(observers);
        dataChangeListenerRegistration = dataBroker.registerDataChangeListener(
                LogicalDatastoreType.OPERATIONAL,
                InstanceIdentifier.builder(Nodes.class)
                        .child(Node.class)
                        .child(NodeConnector.class)
                        .augmentation(FlowCapableNodeConnector.class)
                        .build(),
                this, AsyncDataBroker.DataChangeScope.BASE);
    }

    @Override
    public void close() {
        dataChangeListenerRegistration.close();
    }

    @Override
    public void onDataChanged(AsyncDataChangeEvent<InstanceIdentifier<?>, DataObject> change) {
        LOG.trace("Node connectors in inventory changed: {} created, {} updated, {} removed",
                change.getCreatedData().size(), change.getUpdatedData().size(), change.getRemovedPaths().size());

        // Iterate over created node connectors
        for (Map.Entry<InstanceIdentifier<?>, DataObject> entry : change.getCreatedData().entrySet()) {
            InstanceIdentifier<NodeConnector> nodeConnectorInstanceId =
                    entry.getKey().firstIdentifierOf(NodeConnector.class);

            FlowCapableNodeConnector flowConnector = (FlowCapableNodeConnector) entry.getValue();
            if (!isPortDown(flowConnector)) {
                notifyNodeConnectorAppeared(nodeConnectorInstanceId, flowConnector);
            }
        }

        // Iterate over updated node connectors (port down state may change)
        for (Map.Entry<InstanceIdentifier<?>, DataObject> entry : change.getUpdatedData().entrySet()) {
            InstanceIdentifier<NodeConnector> nodeConnectorInstanceId =
                    entry.getKey().firstIdentifierOf(NodeConnector.class);
            FlowCapableNodeConnector flowConnector = (FlowCapableNodeConnector) entry.getValue();
            if (isPortDown(flowConnector)) {
                notifyNodeConnectorDisappeared(nodeConnectorInstanceId);
            } else {
                notifyNodeConnectorAppeared(nodeConnectorInstanceId, flowConnector);
            }
        }

        // Iterate over removed node connectors
        for (InstanceIdentifier<?> removed : change.getRemovedPaths()) {
            InstanceIdentifier<NodeConnector> nodeConnectorInstanceId = removed.firstIdentifierOf(NodeConnector.class);
            notifyNodeConnectorDisappeared(nodeConnectorInstanceId);
        }
    }

    private static boolean isPortDown(FlowCapableNodeConnector flowCapableNodeConnector) {
        PortState portState = flowCapableNodeConnector.getState();
        PortConfig portConfig = flowCapableNodeConnector.getConfiguration();
        return portState != null && portState.isLinkDown() ||
                portConfig != null && portConfig.isPORTDOWN();
    }

    private void notifyNodeConnectorAppeared(InstanceIdentifier<NodeConnector> nodeConnectorInstanceId,
                                             FlowCapableNodeConnector flowConnector) {
        for (NodeConnectorEventsObserver observer : observers) {
            observer.nodeConnectorAdded(nodeConnectorInstanceId, flowConnector);
        }
    }

    private void notifyNodeConnectorDisappeared(InstanceIdentifier<NodeConnector> nodeConnectorInstanceId) {
        for (NodeConnectorEventsObserver observer : observers) {
            observer.nodeConnectorRemoved(nodeConnectorInstanceId);
        }
    }
}
