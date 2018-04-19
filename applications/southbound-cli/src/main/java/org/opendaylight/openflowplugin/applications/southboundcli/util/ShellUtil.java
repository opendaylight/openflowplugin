/*
 * Copyright (c) 2017 Ericsson India Global Services Pvt Ltd. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.applications.southboundcli.util;

import com.google.common.base.Optional;
import com.google.common.util.concurrent.CheckedFuture;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;
import javax.annotation.Nonnull;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.ReadOnlyTransaction;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.controller.md.sal.common.api.data.ReadFailedException;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNode;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNodeConnector;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.Nodes;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.node.NodeConnector;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.NodeKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.openflowplugin.app.admin.reconciliation.service.rev180227.ReconciliationCounter;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.openflowplugin.app.admin.reconciliation.service.rev180227.reconciliation.counter.ReconcileCounter;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class ShellUtil {
    private static final Logger LOG = LoggerFactory.getLogger(ShellUtil.class);

    public static final String NODE_PREFIX = "openflow:";

    private ShellUtil() {
    }

    @Nonnull
    public static List<OFNode> getAllNodes(final DataBroker broker) {
        List<Node> nodes = null;
        ReadOnlyTransaction tx = broker.newReadOnlyTransaction();
        InstanceIdentifier<Nodes> path = InstanceIdentifier.builder(Nodes.class).build();
        try {
            CheckedFuture<Optional<Nodes>, ReadFailedException> checkedFuture =
                    tx.read(LogicalDatastoreType.OPERATIONAL, path);
            Optional<Nodes> result = checkedFuture.get();
            if (result.isPresent()) {
                nodes = result.get().getNode();
            }
        } catch (ExecutionException | InterruptedException | NullPointerException e) {
            LOG.error("Error reading nodes from Inventory DS", e);
        }
        if (nodes != null) {
            List<OFNode> nodeList = new ArrayList<>();
            for (Node node : nodes) {
                String[] nodeId = node.getId().getValue().split(":");
                String name = null;
                FlowCapableNode flowCapableNode = node.<FlowCapableNode>getAugmentation(FlowCapableNode.class);
                if (flowCapableNode != null) {
                    name = node.<FlowCapableNode>getAugmentation(FlowCapableNode.class).getDescription();
                } else {
                    LOG.error("Error while converting OFNode: {} to FlowCapableNode", node.getId());
                    return Collections.emptyList();
                }
                OFNode ofNode = new OFNode(Long.parseLong(nodeId[1]), name);
                LOG.trace("Added OFNode: {} to the list", ofNode.getNodeId());
                nodeList.add(ofNode);
            }
            Collections.sort(nodeList);
            return nodeList;
        }
        return Collections.emptyList();
    }

    public static OFNode getNode(final long nodeId, final DataBroker broker) {
        OFNode nodeInfo = getNodeInfo(nodeId, broker);
        if (nodeInfo == null) {
            LOG.info("No ports exist for this node with nodeId {}", nodeId);
            return null;
        } else {
            List<String> ports = new ArrayList<>();
            // OFNode State is not provided by plugin, hence using null
            if (nodeInfo.getPorts() == null) {
                LOG.info("No ports exist for this node with nodeId {}", nodeId);
                return null;
            } else {
                for (String port : nodeInfo.getPorts()) {
                    ports.add(port);
                }
                return new OFNode(nodeId, nodeInfo.getNodeName(), ports);
            }
        }
    }

    public static OFNode getNodeInfo(final Long nodeId, final DataBroker broker) {
        OFNode ofNode = null;
        ReadOnlyTransaction tx = broker.newReadOnlyTransaction();
        InstanceIdentifier<Node> path = InstanceIdentifier.builder(Nodes.class)
                .child(Node.class, new NodeKey(new NodeId(NODE_PREFIX + nodeId))).build();
        Optional<Node> result;
        try {
            CheckedFuture<Optional<Node>, ReadFailedException> checkedFuture =
                    tx.read(LogicalDatastoreType.OPERATIONAL, path);
            result = checkedFuture.get();
            if (result.isPresent()) {
                Node node = result.get();
                String name = null;
                List<NodeConnector> nodeConnectors = null;
                List<String> portList = new ArrayList<>();
                FlowCapableNode flowCapableNode = node.<FlowCapableNode>getAugmentation(FlowCapableNode.class);
                if (flowCapableNode != null) {
                    name = node.<FlowCapableNode>getAugmentation(FlowCapableNode.class).getDescription();
                } else {
                    LOG.error("Error while converting OFNode:{} to FlowCapableNode: {}", node.getId());
                    return null;
                }
                nodeConnectors = node.getNodeConnector();
                for (NodeConnector nodeConnector : nodeConnectors) {
                    FlowCapableNodeConnector flowCapableNodeConnector =
                            nodeConnector.getAugmentation(FlowCapableNodeConnector.class);
                    if (flowCapableNodeConnector == null) {
                        LOG.error("Error for OFNode:{} while reading nodeConnectors {}", node.getId());
                        return null;
                    } else {
                        String portName = flowCapableNodeConnector.getName();
                        portList.add(portName);
                    }
                }
                ofNode = new OFNode(nodeId, name, portList);
            } else {
                LOG.error("OFNode with nodeId {} not present Inventory DS: {}", nodeId);
                return null;
            }
        } catch (ExecutionException | InterruptedException e) {
            LOG.error("Error reading node {} from Inventory DS: {}", nodeId, e);
        }
        return ofNode;
    }

    public static List<ReconcileCounter> getReconcileCount(final DataBroker dataBroker) {
        ReadOnlyTransaction tx = dataBroker.newReadOnlyTransaction();
        InstanceIdentifier<ReconciliationCounter> instanceIdentifier = InstanceIdentifier
                .builder(ReconciliationCounter.class).build();
        List<ReconcileCounter> output = Collections.emptyList();
        try {
            CheckedFuture<Optional<ReconciliationCounter>, ReadFailedException> checkedFuture =
                    tx.read(LogicalDatastoreType.OPERATIONAL, instanceIdentifier);
            Optional<ReconciliationCounter> result = checkedFuture.get();
            if (result.isPresent()) {
                output = result.get().getReconcileCounter();
            }
        } catch (ExecutionException | InterruptedException | NullPointerException e) {
            LOG.error("Error reading reconciliation counter from datastore", e);
        }
        return output;
    }
}
