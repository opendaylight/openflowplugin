/*
 * Copyright (c) 2015, 2017 Ericsson India Global Services Pvt Ltd. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.applications.frm.impl;

import static java.util.Objects.requireNonNull;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import java.math.BigInteger;
import org.opendaylight.mdsal.binding.api.DataBroker;
import org.opendaylight.mdsal.binding.api.DataTreeIdentifier;
import org.opendaylight.mdsal.common.api.LogicalDatastoreType;
import org.opendaylight.openflowplugin.applications.frm.FlowNodeConnectorInventoryTranslator;
import org.opendaylight.openflowplugin.common.wait.SimpleTaskRetryLooper;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNodeConnector;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.Nodes;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.node.NodeConnector;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yangtools.concepts.ListenerRegistration;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FlowNodeConnectorInventoryTranslatorImpl extends AbstractNodeConnectorCommitter<FlowCapableNodeConnector>
        implements FlowNodeConnectorInventoryTranslator {

    private static final Logger LOG = LoggerFactory.getLogger(FlowNodeConnectorInventoryTranslatorImpl.class);

    private ListenerRegistration<FlowNodeConnectorInventoryTranslatorImpl> dataTreeChangeListenerRegistration;

    private static final String SEPARATOR = ":";

    private static final InstanceIdentifier<FlowCapableNodeConnector> II_TO_FLOW_CAPABLE_NODE_CONNECTOR
            = InstanceIdentifier.builder(Nodes.class)
            .child(Node.class)
            .child(NodeConnector.class)
            .augmentation(FlowCapableNodeConnector.class)
            .build();

    private final Multimap<BigInteger, String> dpnToPortMultiMap = Multimaps
            .synchronizedListMultimap(ArrayListMultimap.create());

    @SuppressWarnings("IllegalCatch")
    public FlowNodeConnectorInventoryTranslatorImpl(final DataBroker dataBroker) {
        requireNonNull(dataBroker, "DataBroker can not be null!");

        final DataTreeIdentifier<FlowCapableNodeConnector> treeId =
                DataTreeIdentifier.create(LogicalDatastoreType.OPERATIONAL, getWildCardPath());
        try {
            SimpleTaskRetryLooper looper = new SimpleTaskRetryLooper(ForwardingRulesManagerImpl.STARTUP_LOOP_TICK,
                    ForwardingRulesManagerImpl.STARTUP_LOOP_MAX_RETRIES);
            dataTreeChangeListenerRegistration = looper.loopUntilNoException(() -> dataBroker
                    .registerDataTreeChangeListener(treeId, FlowNodeConnectorInventoryTranslatorImpl.this));
        } catch (final Exception e) {
            LOG.warn(" FlowNodeConnectorInventoryTranslatorImpl listener registration fail!");
            LOG.debug("FlowNodeConnectorInventoryTranslatorImpl DataTreeChangeListener registration fail ..", e);
            throw new
            IllegalStateException("FlowNodeConnectorInventoryTranslatorImpl startup fail! System needs restart.", e);
        }
    }

    @Override
    protected InstanceIdentifier<FlowCapableNodeConnector> getWildCardPath() {
        return InstanceIdentifier.create(Nodes.class)
                .child(Node.class)
                .child(NodeConnector.class)
                .augmentation(FlowCapableNodeConnector.class);
    }

    @Override
    public void close() {
        if (dataTreeChangeListenerRegistration != null) {
            dataTreeChangeListenerRegistration.close();
            dataTreeChangeListenerRegistration = null;
        }
    }

    @Override
    public void remove(final InstanceIdentifier<FlowCapableNodeConnector> identifier,
            final FlowCapableNodeConnector del, final InstanceIdentifier<FlowCapableNodeConnector> nodeConnIdent) {
        if (compareInstanceIdentifierTail(identifier, II_TO_FLOW_CAPABLE_NODE_CONNECTOR)) {
            LOG.debug("Node Connector removed");
            String nodeConnectorIdentifier = nodeConnIdent.firstKeyOf(NodeConnector.class)
                    .getId().getValue();
            BigInteger dpId = getDpIdFromPortName(nodeConnectorIdentifier);

            dpnToPortMultiMap.remove(dpId, nodeConnectorIdentifier);
        }
    }

    @Override
    public void update(final InstanceIdentifier<FlowCapableNodeConnector> identifier,
            final FlowCapableNodeConnector original, final FlowCapableNodeConnector update,
            final InstanceIdentifier<FlowCapableNodeConnector> nodeConnIdent) {
        if (compareInstanceIdentifierTail(identifier, II_TO_FLOW_CAPABLE_NODE_CONNECTOR)) {
            LOG.debug("Node Connector updated");
            // Don't need to do anything as we are not considering updates here
        }
    }

    @Override
    public void add(final InstanceIdentifier<FlowCapableNodeConnector> identifier, final FlowCapableNodeConnector add,
            final InstanceIdentifier<FlowCapableNodeConnector> nodeConnIdent) {
        if (compareInstanceIdentifierTail(identifier, II_TO_FLOW_CAPABLE_NODE_CONNECTOR)) {
            LOG.debug("Node Connector added");
            String nodeConnectorIdentifier = nodeConnIdent
                    .firstKeyOf(NodeConnector.class).getId().getValue();
            BigInteger dpId = getDpIdFromPortName(nodeConnectorIdentifier);

            if (!dpnToPortMultiMap.containsEntry(dpId, nodeConnectorIdentifier)) {
                dpnToPortMultiMap.put(dpId, nodeConnectorIdentifier);
            } else {
                LOG.error("Duplicate Event.Node Connector already added");
            }
        }
    }

    private static boolean compareInstanceIdentifierTail(final InstanceIdentifier<?> identifier1,
                                  final InstanceIdentifier<?> identifier2) {
        return Iterables.getLast(identifier1.getPathArguments())
                .equals(Iterables.getLast(identifier2.getPathArguments()));
    }

    @Override
    public boolean isNodeConnectorUpdated(final BigInteger dpId, final String portName) {
        return dpnToPortMultiMap.containsEntry(dpId,portName) ;
    }


    private static BigInteger getDpIdFromPortName(final String portName) {
        String dpId = portName.substring(portName.indexOf(SEPARATOR) + 1, portName.lastIndexOf(SEPARATOR));
        return new BigInteger(dpId);
    }
}

