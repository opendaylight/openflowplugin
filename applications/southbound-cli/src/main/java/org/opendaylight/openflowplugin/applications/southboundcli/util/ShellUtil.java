/*
 * Copyright (c) 2017 Ericsson India Global Services Pvt Ltd. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.applications.southboundcli.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import org.opendaylight.mdsal.binding.api.DataBroker;
import org.opendaylight.mdsal.binding.api.ReadTransaction;
import org.opendaylight.mdsal.common.api.LogicalDatastoreType;
import org.opendaylight.openflowplugin.applications.southboundcli.NodeListener;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNode;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNodeConnector;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.Nodes;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.node.NodeConnector;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.NodeKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.openflowplugin.app.reconciliation.service.rev180227.ReconciliationCounter;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.openflowplugin.app.reconciliation.service.rev180227.reconciliation.counter.ReconcileCounter;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class ShellUtil {
    private static final Logger LOG = LoggerFactory.getLogger(ShellUtil.class);

    public static final String NODE_PREFIX = "openflow:";

    private ShellUtil() {
    }

    public static List<OFNode> getAllNodes(final NodeListener nodeListener) {
        List<OFNode> dpnList = new ArrayList<>();
        for (Map.Entry<Long, String> entry : nodeListener.getDpnIdToNameCache().entrySet()) {
            OFNode dpn = new OFNode(entry.getKey(), entry.getValue());
            dpnList.add(dpn);
            LOG.trace("Added OFNode: {} to the list", dpn.getNodeId());
        }
        Collections.sort(dpnList);
        return dpnList;
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
        InstanceIdentifier<Node> path = InstanceIdentifier.builder(Nodes.class)
                .child(Node.class, new NodeKey(new NodeId(NODE_PREFIX + nodeId))).build();

        try (ReadTransaction tx = broker.newReadOnlyTransaction()) {
            Optional<Node> result = tx.read(LogicalDatastoreType.OPERATIONAL, path).get();
            if (result.isPresent()) {
                Node node = result.get();
                String name = null;
                List<NodeConnector> nodeConnectors = null;
                List<String> portList = new ArrayList<>();
                FlowCapableNode flowCapableNode = node.<FlowCapableNode>augmentation(FlowCapableNode.class);
                if (flowCapableNode != null) {
                    name = node.<FlowCapableNode>augmentation(FlowCapableNode.class).getDescription();
                } else {
                    LOG.error("Error while converting OFNode:{} to FlowCapableNode", node.getId());
                    return null;
                }
                nodeConnectors = node.getNodeConnector();
                for (NodeConnector nodeConnector : nodeConnectors) {
                    FlowCapableNodeConnector flowCapableNodeConnector =
                            nodeConnector.augmentation(FlowCapableNodeConnector.class);
                    if (flowCapableNodeConnector == null) {
                        LOG.error("Error for OFNode:{} while reading nodeConnectors", node.getId());
                        return null;
                    } else {
                        String portName = flowCapableNodeConnector.getName();
                        portList.add(portName);
                    }
                }
                ofNode = new OFNode(nodeId, name, portList);
            } else {
                LOG.error("OFNode with nodeId {} not present Inventory DS", nodeId);
                return null;
            }
        } catch (ExecutionException | InterruptedException e) {
            LOG.error("Error reading node {} from Inventory DS", nodeId, e);
        }
        return ofNode;
    }

    public static List<ReconcileCounter> getReconcileCount(final DataBroker dataBroker) {
        InstanceIdentifier<ReconciliationCounter> instanceIdentifier = InstanceIdentifier
                .builder(ReconciliationCounter.class).build();
        List<ReconcileCounter> output = Collections.emptyList();
        try (ReadTransaction tx = dataBroker.newReadOnlyTransaction()) {
            Optional<ReconciliationCounter> result =
                    tx.read(LogicalDatastoreType.OPERATIONAL, instanceIdentifier).get();
            if (result.isPresent()) {
                output = result.get().getReconcileCounter();
            }
        } catch (ExecutionException | InterruptedException | NullPointerException e) {
            LOG.error("Error reading reconciliation counter from datastore", e);
        }
        return output;
    }
}
