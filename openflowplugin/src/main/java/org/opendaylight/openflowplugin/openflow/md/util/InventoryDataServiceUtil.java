package org.opendaylight.openflowplugin.openflow.md.util;

import java.math.BigInteger;
import java.util.Collections;
import java.util.List;

import org.opendaylight.controller.sal.binding.api.data.DataModificationTransaction;
import org.opendaylight.controller.sal.binding.api.data.DataProviderService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeRef;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeUpdatedBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.Nodes;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodesBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.node.NodeConnector;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.NodeKey;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier.InstanceIdentifierBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class InventoryDataServiceUtil {
    private final static Logger LOG = LoggerFactory.getLogger(InventoryDataServiceUtil.class);
    /*
     *   Get an InstanceIdentifier for the Nodes class that is the root of the inventory tree
     *   We use this alot, so its worth keeping around
     */
    private  InstanceIdentifier<Nodes> nodesIdentifier = InstanceIdentifier.builder().node(Nodes.class).toInstance();

    /*
     * The DataProviderService is how we access the DataTree
     */
    private  DataProviderService dataService;

    public InventoryDataServiceUtil(DataProviderService dataService) {
        // Save the dataService
        this.dataService = dataService;

        /*
         *  Make sure we have a root nodes even if we start without children
         *  Unfortunately, if we don't do this, this node doesn't get created
         *  until we add children to it (and then it get autocreated).
         */

        /*
         *  Ask the DataProviderService for Nodes
         */
        Nodes nodes = checkForNodes();
        /*
         *  If the DataProviderService didn't have Nodes, add it, so its there if someone
         *  comes looking for it who's not smart enough to handle it not being there,
         *  like the REST connector
         */

        if(nodes == null) {
            NodesBuilder nodesBuilder = new NodesBuilder();
            nodesBuilder.setNode(Collections.<Node>emptyList());
            nodes = nodesBuilder.build();
            DataModificationTransaction transaction = dataService.beginTransaction();
            try {
                transaction.putOperationalData(nodesIdentifier, nodes);
            } catch (Exception e) {
                LOG.error("Caught exception from transaction.commit()",e);
            }
            transaction.commit();
        }
        nodes = checkForNodes();
    }

    public Nodes checkForNodes() {
        Nodes nodes = null;
        LOG.error("Before Nodes - nodes: " + nodes);
        try {
           nodes = (Nodes) dataService.readOperationalData(nodesIdentifier);
        } catch (Exception e) {
            LOG.error("Caught exception from dataService.readOperationalData",e);
        }
        LOG.error("After Nodes- nodes: " + nodes);
        return nodes;
    }

    public  List<Node> readAllNodes() {
        Nodes nodes = (Nodes) dataService.readOperationalData(nodesIdentifier);
        return nodes.getNode();
    }

    public Node readNode(InstanceIdentifier<Node> instance) {
        return (Node) dataService.readOperationalData(instance);
    }

    public Node readNode(NodeRef nodeRef) {
        return readNode((InstanceIdentifier<Node>) nodeRef.getValue());
    }

    public Node readNode(NodeKey nodeKey) {
        return readNode(nodeKeyToInstanceIdentifier(nodeKey));
    }

    public Node readNode(NodeId nodeId) {
        return readNode(new NodeKey(nodeId));
    }


    public  Node readNodeByDataPath(BigInteger datapathId) {
        return (Node) dataService.readOperationalData(identifierFromDatapathId(datapathId));
    }

    public void putNode(Node node) {
        DataModificationTransaction transaction = dataService.beginTransaction();
        transaction.putOperationalData(nodesIdentifier, node);
        transaction.commit();
    }

    public void putNodeConnector(InstanceIdentifier<Node> instance,NodeConnector nodeConnector) {
        DataModificationTransaction transaction = dataService.beginTransaction();
        transaction.putOperationalData(instance, nodeConnector);
        transaction.commit();
    }

    public void putNodeConnector(NodeKey nodeKey,NodeConnector nodeConnector) {
        InstanceIdentifier<Node> instance = nodeKeyToInstanceIdentifier(nodeKey);
        putNodeConnector(instance,nodeConnector);
    }



    public void putNodeConnector(NodeId nodeId,NodeConnector nodeConnector) {
        putNodeConnector(new NodeKey(nodeId),nodeConnector);
    }

    public void putNodeConnector(BigInteger datapathId, NodeConnector nodeConnector) {
        putNodeConnector(new NodeId("openflow:" + datapathId),nodeConnector);
    }

    public static InstanceIdentifier<Node> identifierFromDatapathId(BigInteger datapathId) {
        InstanceIdentifierBuilder<?> builder = InstanceIdentifier.builder().node(Nodes.class);

        NodeKey nodeKey = nodeKeyFromDatapathId(datapathId);
        return builder.node(Node.class, nodeKey).toInstance();
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
        return new NodeId("openflow:" + current);
    }

    public static NodeRef nodeRefFromNode(Node node) {
        InstanceIdentifierBuilder<?> builder = InstanceIdentifier.builder().node(Nodes.class);
        return new NodeRef(builder.node(Node.class,node.getKey()).toInstance());
    }

    public static NodeRef nodeRefFromNodeKey(NodeKey nodeKey) {
        InstanceIdentifierBuilder<?> builder = InstanceIdentifier.builder().node(Nodes.class);
        return new NodeRef(builder.node(Node.class,nodeKey).toInstance());
    }

    public static InstanceIdentifier<Node> nodeKeyToInstanceIdentifier(NodeKey nodeKey) {
        InstanceIdentifierBuilder<?> builder = InstanceIdentifier.builder().node(Nodes.class);
        InstanceIdentifier<Node> instance = builder.node(Node.class,nodeKey).toInstance();
        return instance;
    }

}
