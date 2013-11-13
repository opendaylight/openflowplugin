package org.opendaylight.openflowplugin.openflow.md.util;

import java.math.BigInteger;
import java.util.List;

import org.opendaylight.controller.sal.binding.api.data.DataProviderService;
import org.opendaylight.controller.sal.binding.api.data.DataModificationTransaction;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeConnectorId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.Nodes;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.node.NodeConnector;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.NodeKey;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier.InstanceIdentifierBuilder;

public class InventoryDataServiceUtil {

    /*
     *   Get an InstanceIdentifier for the Nodes class that is the root of the inventory tree
     *   We use this alot, so its worth keeping around
     */
    private  InstanceIdentifier nodesIdentifier = InstanceIdentifier.builder().node(Nodes.class).toInstance();

    /*
     * The DataProviderService is how we access the DataTree
     */
    private  DataProviderService dataService;

    public InventoryDataServiceUtil(DataProviderService dataService) {
        // Save the dataService
        dataService = dataService;

        /*
         *  Make sure we have a root nodes even if we start without children
         *  Unfortunately, if we don't do this, this node doesn't get created
         *  until we add children to it (and then it get autocreated).
         */

        // Ask the DataProviderService for Nodes
        Nodes nodes = (Nodes) dataService.readOperationalData(nodesIdentifier);

        /*
         *  If the DataProviderService didn't have Nodes, add it, so its there if someone
         *  comes looking for it who's not smart enough to handle it not being there,
         *  like the REST connector
         */

        if(nodes == null) {
            DataModificationTransaction transaction = dataService.beginTransaction();
            transaction.putOperationalData(nodesIdentifier, nodes);
            transaction.commit();
        }
    }

    public  List<Node> readAllNodes() {
        Nodes nodes = (Nodes) dataService.readOperationalData(nodesIdentifier);
        return nodes.getNode();
    }

    public  Node readNodeByDataPath(BigInteger datapathId) {
        return (Node) dataService.readOperationalData(identifierFromDatapathId(datapathId));
    }

    public void putNode(Node node) {
        DataModificationTransaction transaction = dataService.beginTransaction();
        transaction.putOperationalData(nodesIdentifier, node);
        transaction.commit();
    }

    public void putNodeConnector(BigInteger datapathId, NodeConnector nodeConnector) {
        InstanceIdentifier instanceIdentifier;
        DataModificationTransaction transaction = dataService.beginTransaction();
        transaction.putOperationalData(identifierFromDatapathId(datapathId), nodeConnector);
        transaction.commit();
    }

    public static InstanceIdentifier<Node> identifierFromDatapathId(BigInteger datapathId) {
        InstanceIdentifierBuilder<?> builder = InstanceIdentifier.builder().node(Nodes.class);

        NodeKey nodeKey = nodeKeyFromDatapathId(datapathId);
        return builder.node(Node.class, nodeKey).toInstance();
    }

    public static NodeKey nodeKeyFromDatapathId(BigInteger datapathId) {
        return new NodeKey(nodeIdFromDatapathId(datapathId));
    }

    public static NodeId nodeIdFromDatapathId(BigInteger datapathId) {
        // FIXME: Convert to textual representation of datapathID
        String current = datapathId.toString();
        return new NodeId("openflow:" + current);
    }

}
