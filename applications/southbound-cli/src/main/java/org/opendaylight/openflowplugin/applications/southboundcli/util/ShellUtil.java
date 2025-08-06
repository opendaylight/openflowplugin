/*
 * Copyright (c) 2017 Ericsson India Global Services Pvt Ltd. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.applications.southboundcli.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import org.opendaylight.mdsal.binding.api.DataBroker;
import org.opendaylight.mdsal.binding.api.ReadTransaction;
import org.opendaylight.mdsal.common.api.LogicalDatastoreType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNode;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNodeConnector;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.Nodes;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.node.NodeConnector;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.NodeKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.openflowplugin.app.reconciliation.service.rev180227.ReconciliationCounter;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.openflowplugin.app.reconciliation.service.rev180227.reconciliation.counter.ReconcileCounter;
import org.opendaylight.yangtools.binding.DataObjectIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class ShellUtil {
    private static final Logger LOG = LoggerFactory.getLogger(ShellUtil.class);

    public static final String LINE_SEPARATOR = "-".repeat(100);
    public static final String NODE_PREFIX = "openflow:";

    private ShellUtil() {
    }

    public static OFNode getNode(final long nodeId, final DataBroker broker) {
        OFNode nodeInfo = getNodeInfo(nodeId, broker);
        if (nodeInfo == null) {
            LOG.info("No ports exist for this node with nodeId {}", nodeId);
            return null;
        }
        List<String> ports = new ArrayList<>();
        // OFNode State is not provided by plugin, hence using null
        if (nodeInfo.getPorts() == null) {
            LOG.info("No ports exist for this node with nodeId {}", nodeId);
            return null;
        }
        ports.addAll(nodeInfo.getPorts());
        return new OFNode(nodeId, nodeInfo.getNodeName(), ports);
    }

    public static OFNode getNodeInfo(final Long nodeId, final DataBroker broker) {
        OFNode ofNode = null;
        final var path = DataObjectIdentifier.builder(Nodes.class)
                .child(Node.class, new NodeKey(new NodeId(NODE_PREFIX + nodeId)))
                .build();

        try (ReadTransaction tx = broker.newReadOnlyTransaction()) {
            Optional<Node> result = tx.read(LogicalDatastoreType.OPERATIONAL, path).get();
            if (result.isEmpty()) {
                LOG.error("OFNode with nodeId {} not present Inventory DS", nodeId);
                return null;
            }
            Node node = result.orElseThrow();
            Collection<NodeConnector> nodeConnectors = node.nonnullNodeConnector().values();
            List<String> portList = new ArrayList<>();
            FlowCapableNode flowCapableNode = node.augmentation(FlowCapableNode.class);
            if (flowCapableNode == null) {
                LOG.error("Error while converting OFNode:{} to FlowCapableNode", node.getId());
                return null;
            }
            final var name = flowCapableNode.getDescription();
            for (NodeConnector nodeConnector : nodeConnectors) {
                FlowCapableNodeConnector flowCapableNodeConnector =
                        nodeConnector.augmentation(FlowCapableNodeConnector.class);
                if (flowCapableNodeConnector == null) {
                    LOG.error("Error for OFNode:{} while reading nodeConnectors", node.getId());
                    return null;
                }
                String portName = flowCapableNodeConnector.getName();
                portList.add(portName);
            }
            ofNode = new OFNode(nodeId, name, portList);
        } catch (ExecutionException | InterruptedException e) {
            LOG.error("Error reading node {} from Inventory DS", nodeId, e);
        }
        return ofNode;
    }

    public static Collection<ReconcileCounter> getReconcileCount(final DataBroker dataBroker) {
        try (var tx = dataBroker.newReadOnlyTransaction()) {
            final var result = tx.read(LogicalDatastoreType.OPERATIONAL,
                DataObjectIdentifier.builder(ReconciliationCounter.class).build()).get();
            return result.map(counter -> counter.nonnullReconcileCounter().values()).orElse(List.of());
        } catch (ExecutionException | InterruptedException e) {
            LOG.error("Error reading reconciliation counter from datastore", e);
            return List.of();
        }
    }
}
