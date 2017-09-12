/**
 * Copyright (c) 2015, 2017 Ericsson India Global Services Pvt Ltd. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.applications.frm.impl;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import java.math.BigInteger;
import java.util.Objects;
import java.util.concurrent.Future;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.openflowplugin.applications.frm.FlowNodeConnectorInventoryTranslator;
import org.opendaylight.openflowplugin.applications.frm.ForwardingRulesManager;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNode;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNodeConnector;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.Nodes;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.node.NodeConnector;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.node.NodeConnectorKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.opendaylight.yangtools.yang.common.RpcResultBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FlowNodeConnectorInventoryTranslatorImpl
        extends AbstractListeningCommiter<FlowCapableNodeConnector>
        implements FlowNodeConnectorInventoryTranslator {

    private static final Logger LOG = LoggerFactory.getLogger(FlowNodeConnectorInventoryTranslatorImpl.class);
    private static final String SEPARATOR = ":";

    private static final InstanceIdentifier<FlowCapableNodeConnector> II_TO_FLOW_CAPABLE_NODE_CONNECTOR
            = InstanceIdentifier.builder(Nodes.class)
            .child(Node.class)
            .child(NodeConnector.class)
            .augmentation(FlowCapableNodeConnector.class)
            .build();

    private final Multimap<BigInteger, String> dpnToPortMultiMap = Multimaps
            .synchronizedListMultimap(ArrayListMultimap.<BigInteger, String>create());

    public FlowNodeConnectorInventoryTranslatorImpl(final ForwardingRulesManager manager, final DataBroker dataBroker) {
        super(manager, dataBroker);
    }

    @Override
    protected InstanceIdentifier<FlowCapableNodeConnector> getWildCardPath() {
        return InstanceIdentifier.create(Nodes.class)
                .child(Node.class)
                .child(NodeConnector.class)
                .augmentation(FlowCapableNodeConnector.class);
    }

    @Override
    public boolean isNodeConnectorUpdated(BigInteger dpId, String portName) {
        return dpnToPortMultiMap.containsEntry(dpId,portName) ;
    }

    @Override
    public void remove(final InstanceIdentifier<FlowCapableNodeConnector> identifier,
                       final FlowCapableNodeConnector del,
                       final InstanceIdentifier<FlowCapableNode> nodeIdent) {
        if (compareInstanceIdentifierTail(identifier, II_TO_FLOW_CAPABLE_NODE_CONNECTOR)) {
            LOG.debug("Node Connector removed");
            String nodeConnectorIdentifier = identifier.firstKeyOf(NodeConnector.class, NodeConnectorKey.class)
                    .getId().getValue();
            BigInteger dpId = getDpIdFromPortName(nodeConnectorIdentifier);
            dpnToPortMultiMap.remove(dpId, nodeConnectorIdentifier);
        }
    }

    @Override
    public void update(final InstanceIdentifier<FlowCapableNodeConnector> identifier,
                       final FlowCapableNodeConnector original,
                       final FlowCapableNodeConnector update,
                       final InstanceIdentifier<FlowCapableNode> nodeIdent) {
        // NOOP
    }

    @Override
    public Future<? extends RpcResult<?>> add(final InstanceIdentifier<FlowCapableNodeConnector> identifier,
                                              final FlowCapableNodeConnector add,
                                              final InstanceIdentifier<FlowCapableNode> nodeIdent) {
        if (compareInstanceIdentifierTail(identifier, II_TO_FLOW_CAPABLE_NODE_CONNECTOR)) {
            LOG.debug("Node Connector added");
            String nodeConnectorIdentifier = identifier
                    .firstKeyOf(NodeConnector.class, NodeConnectorKey.class).getId().getValue();
            BigInteger dpId = getDpIdFromPortName(nodeConnectorIdentifier);
            dpnToPortMultiMap.put(dpId, nodeConnectorIdentifier);
        }

        return RpcResultBuilder.success().buildFuture();
    }

    @Override
    public void createStaleMarkEntity(final InstanceIdentifier<FlowCapableNodeConnector> identifier,
                                      final FlowCapableNodeConnector del,
                                      final InstanceIdentifier<FlowCapableNode> nodeIdent) {
        // NOOP
    }

    @Override
    protected boolean preConfigurationCheck(final InstanceIdentifier<FlowCapableNode> nodeIdent) {
        return Objects.nonNull(nodeIdent);
    }

    private boolean compareInstanceIdentifierTail(InstanceIdentifier<?> identifier1,
                                                  InstanceIdentifier<?> identifier2) {
        return Iterables.getLast(identifier1.getPathArguments())
                .equals(Iterables.getLast(identifier2.getPathArguments()));
    }

    private BigInteger getDpIdFromPortName(String portName) {
        String dpId = portName.substring(portName.indexOf(SEPARATOR) + 1, portName.lastIndexOf(SEPARATOR));
        return new BigInteger(dpId);
    }
}