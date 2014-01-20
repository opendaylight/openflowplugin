/**
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.openflow.md.util;

import org.opendaylight.controller.sal.binding.api.data.DataModificationTransaction;
import org.opendaylight.openflowplugin.openflow.md.core.session.OFSessionUtil;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeConnectorId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeConnectorRef;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeConnectorUpdatedBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeRef;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeUpdatedBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.Nodes;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.node.NodeConnector;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.node.NodeConnectorBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.node.NodeConnectorKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.NodeKey;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigInteger;
import java.util.List;

public class InventoryDataServiceUtil {
    private final static Logger LOG = LoggerFactory.getLogger(InventoryDataServiceUtil.class);

    public final static String OF_URI_PREFIX = "openflow:";
    /*
     * Get an InstanceIdentifier for the Nodes class that is the root of the
     * inventory tree We use this alot, so its worth keeping around
     */
    private static InstanceIdentifier<Nodes> nodesIdentifier = InstanceIdentifier.builder().node(Nodes.class)
            .toInstance();

    public static Nodes checkForNodes() {
        Nodes nodes = null;
        LOG.error("Before Nodes - nodes: " + nodes);
        try {
            nodes = (Nodes) OFSessionUtil.getSessionManager().getDataProviderService()
                    .readOperationalData(nodesIdentifier);
        } catch (Exception e) {
            LOG.error(
                    "Caught exception from OFSessionUtil.getSessionManager().getDataProviderService().readOperationalData",
                    e);
        }
        LOG.error("After Nodes- nodes: " + nodes);
        return nodes;
    }

    public static List<Node> readAllNodes() {
        Nodes nodes = (Nodes) OFSessionUtil.getSessionManager().getDataProviderService()
                .readOperationalData(nodesIdentifier);
        return nodes.getNode();
    }

    public static Node readNode(InstanceIdentifier<Node> instance) {
        return (Node) OFSessionUtil.getSessionManager().getDataProviderService().readOperationalData(instance);
    }

    public static Node readNode(NodeRef nodeRef) {
        return readNode((InstanceIdentifier<Node>) nodeRef.getValue());
    }

    public static Node readNode(NodeKey nodeKey) {
        return readNode(nodeKeyToInstanceIdentifier(nodeKey));
    }

    public static Node readNode(NodeId nodeId) {
        return readNode(new NodeKey(nodeId));
    }

    public static Node readNodeByDataPath(BigInteger datapathId) {
        return (Node) OFSessionUtil.getSessionManager().getDataProviderService()
                .readOperationalData(identifierFromDatapathId(datapathId));
    }

    public static void putNode(Node node) {
        DataModificationTransaction transaction = OFSessionUtil.getSessionManager().getDataProviderService()
                .beginTransaction();
        transaction.putOperationalData(nodesIdentifier, node);
        transaction.commit();
    }

    public static void putNodeConnector(InstanceIdentifier<Node> instance, NodeConnector nodeConnector) {
        DataModificationTransaction transaction = OFSessionUtil.getSessionManager().getDataProviderService()
                .beginTransaction();
        transaction.putOperationalData(instance, nodeConnector);
        transaction.commit();
    }

    public static void putNodeConnector(NodeKey nodeKey, NodeConnector nodeConnector) {
        InstanceIdentifier<Node> instance = nodeKeyToInstanceIdentifier(nodeKey);
        putNodeConnector(instance, nodeConnector);
    }

    public static void putNodeConnector(NodeId nodeId, NodeConnector nodeConnector) {
        putNodeConnector(new NodeKey(nodeId), nodeConnector);
    }

    public static void putNodeConnector(BigInteger datapathId, NodeConnector nodeConnector) {
        putNodeConnector(new NodeId(OF_URI_PREFIX + datapathId), nodeConnector);
    }

    public static InstanceIdentifier<Node> identifierFromDatapathId(BigInteger datapathId) {
        NodeKey nodeKey = nodeKeyFromDatapathId(datapathId);
        return InstanceIdentifier.builder(Nodes.class).child(Node.class, nodeKey).toInstance();
    }

    public static NodeKey nodeKeyFromDatapathId(BigInteger datapathId) {
        return new NodeKey(nodeIdFromDatapathId(datapathId));
    }

    public static NodeUpdatedBuilder nodeUpdatedBuilderFromDataPathId(BigInteger datapathId) {
        NodeUpdatedBuilder builder = new NodeUpdatedBuilder();
        builder.setId(nodeIdFromDatapathId(datapathId));
        builder.setNodeRef(nodeRefFromNodeKey(new NodeKey(builder.getId())));
        return builder;
    }

    public static NodeId nodeIdFromDatapathId(BigInteger datapathId) {
        // FIXME: Convert to textual representation of datapathID
        String current = datapathId.toString();
        return new NodeId(OF_URI_PREFIX + current);
    }
    
    public static Long dataPathIdFromNodeId(NodeId nodeId) {
        String dpids = nodeId.getValue().replace(OF_URI_PREFIX, "");
        Long dpid = Long.decode(dpids);
        return dpid;
    }

    public static NodeRef nodeRefFromNode(Node node) {
        return nodeRefFromNodeKey(node.getKey());
    }

    public static NodeRef nodeRefFromNodeKey(NodeKey nodeKey) {
        return new NodeRef(nodeKeyToInstanceIdentifier(nodeKey));
    }

    public static InstanceIdentifier<Node> nodeKeyToInstanceIdentifier(NodeKey nodeKey) {
        return InstanceIdentifier.builder(Nodes.class).child(Node.class, nodeKey).toInstance();
    }

    public static InstanceIdentifier<Node> nodeIdToInstanceIdentifier(NodeId nodeId) {
        return nodeKeyToInstanceIdentifier(new NodeKey(nodeId));
    }

    public static NodeConnectorId nodeConnectorIdfromDatapathPortNo(BigInteger datapathid, Long portNo) {
        return new NodeConnectorId(OF_URI_PREFIX + datapathid + ":" + portNo);
    }
    
    public static Long portNumberfromNodeConnectorId(NodeConnectorId ncId) {
        return portNumberfromNodeConnectorId(ncId.getValue());
    }

    public static Long portNumberfromNodeConnectorId(String ncId){
        String[] split = ncId.split(":");

        // If the length is just one then this cannot be the new MD-SAL style node connector Id which
        // is of the form openflow:1:3.
        if(split.length == 1){
            return Long.decode(ncId);
        }
        String portNoString = split[split.length-1];
        Long portNo = Long.decode(portNoString);
        return portNo;
    }



    public static NodeConnectorRef nodeConnectorRefFromDatapathIdPortno(BigInteger datapathId, Long portNo) {
        return new NodeConnectorRef(nodeConnectorInstanceIdentifierFromDatapathIdPortno(datapathId, portNo));
    }

    public static InstanceIdentifier<NodeConnector> nodeConnectorInstanceIdentifierFromDatapathIdPortno(
            BigInteger datapathId, Long portNo) {
        NodeId nodeId = nodeIdFromDatapathId(datapathId);
        NodeConnectorId nodeConnectorId = nodeConnectorIdfromDatapathPortNo(datapathId, portNo);
        return InstanceIdentifier.builder(Nodes.class) //
                .child(Node.class, new NodeKey(nodeId)) //
                .child(NodeConnector.class, new NodeConnectorKey(nodeConnectorId)).toInstance();
    }

    public static NodeConnectorUpdatedBuilder nodeConnectorUpdatedBuilderFromDatapathIdPortNo(BigInteger datapathId, Long portNo) {
        NodeConnectorUpdatedBuilder builder = new NodeConnectorUpdatedBuilder();
        builder.setId(InventoryDataServiceUtil.nodeConnectorIdfromDatapathPortNo(datapathId,portNo));
        builder.setNodeConnectorRef(InventoryDataServiceUtil.nodeConnectorRefFromDatapathIdPortno(datapathId,portNo));
        return builder;
    }

    public static NodeConnectorBuilder nodeConnectorBuilderFromDatapathIdPortNo(BigInteger datapathId, Long portNo) {
        NodeConnectorBuilder builder = new NodeConnectorBuilder();
        builder.setId(InventoryDataServiceUtil.nodeConnectorIdfromDatapathPortNo(datapathId,portNo));
        return builder;
    }
}
