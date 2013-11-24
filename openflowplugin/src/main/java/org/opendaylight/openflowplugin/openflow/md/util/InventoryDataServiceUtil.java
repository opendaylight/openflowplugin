package org.opendaylight.openflowplugin.openflow.md.util;

import java.math.BigInteger;
import java.util.List;

import org.opendaylight.controller.sal.binding.api.data.DataModificationTransaction;
import org.opendaylight.openflowplugin.openflow.md.core.session.OFSessionUtil;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNode;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.TableId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.TableUpdatedBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.flow.node.Table;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.flow.node.TableKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.FlowTableRef;
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

    public static TableUpdatedBuilder tableUpdatedBuilderFromDataPathIdTableId(BigInteger datapathId,Short tableId) {
        TableUpdatedBuilder builder = new TableUpdatedBuilder();
        builder.setId(new TableId(tableId));
        builder.setTableRef(tableRefFromDataPathIdTableId(datapathId,tableId));
        return builder;
    }

    private static FlowTableRef tableRefFromDataPathIdTableId(BigInteger datapathId, Short tableId) {
        return new FlowTableRef(tableRefFromDataPathIdTableId(datapathId, new TableId(tableId)));
    }

    private static InstanceIdentifier<Table> tableRefFromDataPathIdTableId(BigInteger datapathId, TableId tableId) {
        NodeId nodeId = nodeIdFromDatapathId(datapathId);
        return InstanceIdentifier.builder(Nodes.class) //
                .child(Node.class, new NodeKey(nodeId)).augmentation(FlowCapableNode.class) //
                .child(Table.class, new TableKey(tableId)).toInstance();
    }


}
