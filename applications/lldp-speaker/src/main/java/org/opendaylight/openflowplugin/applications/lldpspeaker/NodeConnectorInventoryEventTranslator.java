/*
 * Copyright (c) 2014 Pacnet and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.applications.lldpspeaker;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import javax.annotation.Nonnull;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.DataTreeChangeListener;
import org.opendaylight.controller.md.sal.binding.api.DataTreeIdentifier;
import org.opendaylight.controller.md.sal.binding.api.DataTreeModification;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNodeConnector;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNodeConnectorBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.port.rev130925.PortConfig;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.port.rev130925.PortState;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.port.rev130925.flow.capable.port.State;
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
public class NodeConnectorInventoryEventTranslator<T extends DataObject> implements DataTreeChangeListener<T>, AutoCloseable {

    private static final InstanceIdentifier<State> II_TO_STATE 
        = InstanceIdentifier.builder(Nodes.class)
            .child(Node.class)
            .child(NodeConnector.class)
            .augmentation(FlowCapableNodeConnector.class)
            .child(State.class)
            .build();

    private static final InstanceIdentifier<FlowCapableNodeConnector> II_TO_FLOW_CAPABLE_NODE_CONNECTOR
        = InstanceIdentifier.builder(Nodes.class)
            .child(Node.class)
            .child(NodeConnector.class)
            .augmentation(FlowCapableNodeConnector.class)
            .build();

    private static final Logger LOG = LoggerFactory.getLogger(NodeConnectorInventoryEventTranslator.class);

    private final ListenerRegistration<DataTreeChangeListener> dataChangeListenerRegistration;
    private final ListenerRegistration<DataTreeChangeListener> listenerOnPortStateRegistration;
    private final Set<NodeConnectorEventsObserver> observers;
    private final Map<InstanceIdentifier<?>,FlowCapableNodeConnector> iiToDownFlowCapableNodeConnectors = new HashMap<>();

    public NodeConnectorInventoryEventTranslator(DataBroker dataBroker, NodeConnectorEventsObserver... observers) {
        this.observers = ImmutableSet.copyOf(observers);
        DataTreeIdentifier<T> dtiToNodeConnector =
                new DataTreeIdentifier(LogicalDatastoreType.OPERATIONAL, II_TO_FLOW_CAPABLE_NODE_CONNECTOR);
        DataTreeIdentifier<T> dtiToNodeConnectorState =
                new DataTreeIdentifier(LogicalDatastoreType.OPERATIONAL, II_TO_STATE);
        dataChangeListenerRegistration = dataBroker.registerDataTreeChangeListener(dtiToNodeConnector, this);
        listenerOnPortStateRegistration = dataBroker.registerDataTreeChangeListener(dtiToNodeConnectorState, this);
    }

    @Override
    public void close() {
        if (dataChangeListenerRegistration != null) {
            dataChangeListenerRegistration.close();
        }
        if (listenerOnPortStateRegistration != null) {
            listenerOnPortStateRegistration.close();
        }
    }

    @Override
    public void onDataTreeChanged(@Nonnull Collection<DataTreeModification<T>> modifications) {
        for(DataTreeModification modification : modifications) {
            LOG.trace("Node connectors in inventory changed -> {}", modification.getRootNode().getModificationType());
            switch (modification.getRootNode().getModificationType()) {
                case WRITE:
                    processAddedConnector(modification);
                    break;
                case SUBTREE_MODIFIED:
                    processUpdatedConnector(modification);
                    break;
                case DELETE:
                    processRemovedConnector(modification);
                    break;
                default:
                    throw new IllegalArgumentException("Unhandled modification type: {}" +
                            modification.getRootNode().getModificationType());
            }
        }
    }

    private void processAddedConnector(final DataTreeModification<T> modification) {
        final InstanceIdentifier<T> identifier = modification.getRootPath().getRootIdentifier();
        InstanceIdentifier<NodeConnector> nodeConnectorInstanceId =identifier.firstIdentifierOf(NodeConnector.class);
        if (compareIITail(identifier, II_TO_FLOW_CAPABLE_NODE_CONNECTOR)) {
            FlowCapableNodeConnector flowConnector = (FlowCapableNodeConnector) modification.getRootNode().getDataAfter();
            if (!isPortDown(flowConnector)) {
                notifyNodeConnectorAppeared(nodeConnectorInstanceId, flowConnector);
            } else {
                iiToDownFlowCapableNodeConnectors.put(nodeConnectorInstanceId, flowConnector);
            }
        }
    }

    private void processUpdatedConnector(final DataTreeModification<T> modification) {
        final InstanceIdentifier<T> identifier = modification.getRootPath().getRootIdentifier();
        InstanceIdentifier<NodeConnector> nodeConnectorInstanceId = identifier.firstIdentifierOf(NodeConnector.class);
        if (compareIITail(identifier, II_TO_FLOW_CAPABLE_NODE_CONNECTOR)) {
            FlowCapableNodeConnector flowConnector = (FlowCapableNodeConnector) modification.getRootNode().getDataAfter();
            if (isPortDown(flowConnector)) {
                notifyNodeConnectorDisappeared(nodeConnectorInstanceId);
            } else {
                notifyNodeConnectorAppeared(nodeConnectorInstanceId, flowConnector);
            }
        } else if (compareIITail(identifier, II_TO_STATE)) {
            FlowCapableNodeConnector flowNodeConnector = iiToDownFlowCapableNodeConnectors.get(nodeConnectorInstanceId);
            if (flowNodeConnector != null) {
                State state = (State) modification.getRootNode().getDataAfter();
                if (!state.isLinkDown()) {
                    FlowCapableNodeConnectorBuilder flowCapableNodeConnectorBuilder = new FlowCapableNodeConnectorBuilder(flowNodeConnector);
                    flowCapableNodeConnectorBuilder.setState(state);
                    notifyNodeConnectorAppeared(nodeConnectorInstanceId, flowCapableNodeConnectorBuilder.build());
                    iiToDownFlowCapableNodeConnectors.remove(nodeConnectorInstanceId);
                }
            }
        }
    }

    private void processRemovedConnector(final DataTreeModification<T> modification) {
        final InstanceIdentifier<T> identifier = modification.getRootPath().getRootIdentifier();
        if (compareIITail(identifier, II_TO_FLOW_CAPABLE_NODE_CONNECTOR)) {
            InstanceIdentifier<NodeConnector> nodeConnectorInstanceId = identifier.firstIdentifierOf(NodeConnector.class);
            notifyNodeConnectorDisappeared(nodeConnectorInstanceId);
        }
    }

    private boolean compareIITail(final InstanceIdentifier<?> ii1, final InstanceIdentifier<?> ii2) {
        return Iterables.getLast(ii1.getPathArguments()).equals(Iterables.getLast(ii2.getPathArguments()));
    }

    private static boolean isPortDown(final FlowCapableNodeConnector flowCapableNodeConnector) {
        PortState portState = flowCapableNodeConnector.getState();
        PortConfig portConfig = flowCapableNodeConnector.getConfiguration();
        return portState != null && portState.isLinkDown()
                || portConfig != null && portConfig.isPORTDOWN();
    }

    private void notifyNodeConnectorAppeared(final InstanceIdentifier<NodeConnector> nodeConnectorInstanceId,
                                             final FlowCapableNodeConnector flowConnector) {
        for (NodeConnectorEventsObserver observer : observers) {
            observer.nodeConnectorAdded(nodeConnectorInstanceId, flowConnector);
        }
    }

    private void notifyNodeConnectorDisappeared(final InstanceIdentifier<NodeConnector> nodeConnectorInstanceId) {
        for (NodeConnectorEventsObserver observer : observers) {
            observer.nodeConnectorRemoved(nodeConnectorInstanceId);
        }
    }
}
