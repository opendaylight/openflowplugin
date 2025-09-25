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
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import java.math.BigInteger;
import org.opendaylight.mdsal.binding.api.DataBroker;
import org.opendaylight.mdsal.common.api.LogicalDatastoreType;
import org.opendaylight.openflowplugin.applications.frm.FlowNodeConnectorInventoryTranslator;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNodeConnector;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.Nodes;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.node.NodeConnector;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yangtools.binding.DataObjectIdentifier;
import org.opendaylight.yangtools.binding.DataObjectReference;
import org.opendaylight.yangtools.concepts.Registration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class FlowNodeConnectorInventoryTranslatorImpl
        extends AbstractNodeConnectorCommitter<FlowCapableNodeConnector>
        implements FlowNodeConnectorInventoryTranslator {
    private static final Logger LOG = LoggerFactory.getLogger(FlowNodeConnectorInventoryTranslatorImpl.class);
    private static final String SEPARATOR = ":";
    private static final DataObjectReference<FlowCapableNodeConnector> II_TO_FLOW_CAPABLE_NODE_CONNECTOR =
        DataObjectReference.builder(Nodes.class)
            .child(Node.class)
            .child(NodeConnector.class)
            .augmentation(FlowCapableNodeConnector.class)
            .build();

    private Registration listenerRegistration;

    private final Multimap<BigInteger, String> dpnToPortMultiMap = Multimaps.synchronizedListMultimap(
        ArrayListMultimap.create());

    public FlowNodeConnectorInventoryTranslatorImpl(final DataBroker dataBroker) {
        requireNonNull(dataBroker, "DataBroker can not be null!");
        listenerRegistration = dataBroker.registerLegacyTreeChangeListener(
            LogicalDatastoreType.OPERATIONAL, getWildCardPath(), this);
    }

    @Override
    protected DataObjectReference<FlowCapableNodeConnector> getWildCardPath() {
        return DataObjectReference.builder(Nodes.class)
                .child(Node.class)
                .child(NodeConnector.class)
                .augmentation(FlowCapableNodeConnector.class)
                .build();
    }

    @Override
    public void close() {
        if (listenerRegistration != null) {
            listenerRegistration.close();
            listenerRegistration = null;
        }
    }

    @Override
    public void remove(final DataObjectIdentifier<FlowCapableNodeConnector> identifier,
            final FlowCapableNodeConnector del, final DataObjectIdentifier<FlowCapableNodeConnector> nodeConnIdent) {
        if (compareInstanceIdentifierTail(identifier, II_TO_FLOW_CAPABLE_NODE_CONNECTOR)) {
            LOG.debug("Node Connector removed");
            String nodeConnectorIdentifier = nodeConnIdent.getFirstKeyOf(NodeConnector.class).getId().getValue();
            BigInteger dpId = getDpIdFromPortName(nodeConnectorIdentifier);

            dpnToPortMultiMap.remove(dpId, nodeConnectorIdentifier);
        }
    }

    @Override
    public void update(final DataObjectIdentifier<FlowCapableNodeConnector> identifier,
            final FlowCapableNodeConnector original, final FlowCapableNodeConnector update,
            final DataObjectIdentifier<FlowCapableNodeConnector> nodeConnIdent) {
        if (compareInstanceIdentifierTail(identifier, II_TO_FLOW_CAPABLE_NODE_CONNECTOR)) {
            LOG.debug("Node Connector updated");
            // Don't need to do anything as we are not considering updates here
        }
    }

    @Override
    public void add(final DataObjectIdentifier<FlowCapableNodeConnector> identifier, final FlowCapableNodeConnector add,
            final DataObjectIdentifier<FlowCapableNodeConnector> nodeConnIdent) {
        if (compareInstanceIdentifierTail(identifier, II_TO_FLOW_CAPABLE_NODE_CONNECTOR)) {
            LOG.debug("Node Connector added");
            String nodeConnectorIdentifier = nodeConnIdent.getFirstKeyOf(NodeConnector.class).getId().getValue();
            BigInteger dpId = getDpIdFromPortName(nodeConnectorIdentifier);

            if (!dpnToPortMultiMap.containsEntry(dpId, nodeConnectorIdentifier)) {
                dpnToPortMultiMap.put(dpId, nodeConnectorIdentifier);
            } else {
                LOG.error("Duplicate Event.Node Connector already added");
            }
        }
    }

    private static boolean compareInstanceIdentifierTail(final DataObjectIdentifier<?> identifier1,
                                  final DataObjectReference<?> identifier2) {
        return identifier1.lastStep().equals(identifier2.lastStep());
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

