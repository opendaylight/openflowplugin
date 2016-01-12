/**
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.openflow.md.util;

import com.google.common.base.Optional;
import java.math.BigInteger;
import java.util.List;
import java.util.concurrent.ExecutionException;
import org.apache.commons.lang3.StringUtils;
import org.opendaylight.controller.md.sal.binding.api.ReadTransaction;
import org.opendaylight.controller.md.sal.binding.api.WriteTransaction;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.openflowplugin.api.openflow.md.util.OpenflowVersion;
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
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.binding.KeyedInstanceIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class InventoryDataServiceUtil {
    public static final  String OF_URI_PREFIX = "openflow:";
    private static final Logger LOG = LoggerFactory.getLogger(InventoryDataServiceUtil.class);

    /*
     * Get an InstanceIdentifier for the Nodes class that is the root of the
     * inventory tree We use this alot, so its worth keeping around
     */
    private static final InstanceIdentifier<Nodes> NODES_IDENTIFIER = InstanceIdentifier.create(Nodes.class);

    public static Nodes checkForNodes() {
        Nodes nodes = null;
        LOG.error("Before Nodes - nodes: " + nodes);
        try {
            nodes = getDataObject(OFSessionUtil.getSessionManager().getDataBroker().newReadOnlyTransaction(), NODES_IDENTIFIER);
        } catch (Exception e) {
            LOG.error(
                    "Caught exception from OFSessionUtil.getSessionManager().getDataBroker().newReadWriteTransaction()",
                    e);
        }
        LOG.error("After Nodes- nodes: " + nodes);
        return nodes;
    }

    public static List<Node> readAllNodes() {
        Nodes nodes = getDataObject(OFSessionUtil.getSessionManager().getDataBroker().newReadOnlyTransaction(), NODES_IDENTIFIER);
        return nodes.getNode();
    }

    public static Node readNode(final InstanceIdentifier<Node> instance) {
        return getDataObject(OFSessionUtil.getSessionManager().getDataBroker().newReadOnlyTransaction(), instance);
    }

    public static void putNodeConnector(final InstanceIdentifier<Node> instance, final NodeConnector nodeConnector) {
        WriteTransaction transaction = OFSessionUtil.getSessionManager().getDataBroker().newWriteOnlyTransaction();
        InstanceIdentifier<NodeConnector> nodeConnectorID = instance.child(NodeConnector.class, nodeConnector.getKey());
        transaction.merge(LogicalDatastoreType.OPERATIONAL, nodeConnectorID, nodeConnector);
        transaction.submit();
    }

    public static void putNodeConnector(final NodeKey nodeKey, final NodeConnector nodeConnector) {
        InstanceIdentifier<Node> instance = nodeKeyToInstanceIdentifier(nodeKey);
        putNodeConnector(instance, nodeConnector);
    }

    public static void putNodeConnector(final NodeId nodeId, final NodeConnector nodeConnector) {
        putNodeConnector(new NodeKey(nodeId), nodeConnector);
    }

    public static InstanceIdentifier<Node> identifierFromDatapathId(final BigInteger datapathId) {
        NodeKey nodeKey = nodeKeyFromDatapathId(datapathId);
        return NODES_IDENTIFIER.child(Node.class, nodeKey);
    }

    public static NodeKey nodeKeyFromDatapathId(final BigInteger datapathId) {
        return new NodeKey(nodeIdFromDatapathId(datapathId));
    }

    public static NodeUpdatedBuilder nodeUpdatedBuilderFromDataPathId(final BigInteger datapathId) {
        NodeUpdatedBuilder builder = new NodeUpdatedBuilder();
        builder.setId(nodeIdFromDatapathId(datapathId));
        builder.setNodeRef(nodeRefFromNodeKey(new NodeKey(builder.getId())));
        return builder;
    }

    public static NodeId nodeIdFromDatapathId(final BigInteger datapathId) {
        // FIXME: Convert to textual representation of datapathID
        String current = datapathId.toString();
        return new NodeId(OF_URI_PREFIX + current);
    }

    public static BigInteger dataPathIdFromNodeId(final NodeId nodeId) {
        String dpids = nodeId.getValue().replace(OF_URI_PREFIX, "");
        BigInteger dpid = new BigInteger(dpids);
        return dpid;
    }


    public static NodeRef nodeRefFromNodeKey(final NodeKey nodeKey) {
        return new NodeRef(nodeKeyToInstanceIdentifier(nodeKey));
    }

    public static InstanceIdentifier<Node> nodeKeyToInstanceIdentifier(final NodeKey nodeKey) {
        return NODES_IDENTIFIER.child(Node.class, nodeKey);
    }

    public static NodeConnectorId nodeConnectorIdfromDatapathPortNo(final BigInteger datapathid, final Long portNo,
                                                                    final OpenflowVersion ofVersion) {
        String logicalName = OpenflowPortsUtil.getPortLogicalName(ofVersion, portNo);
        return new NodeConnectorId(OF_URI_PREFIX + datapathid + ":" + (logicalName == null ? portNo : logicalName));
    }

    public static Long portNumberfromNodeConnectorId(final OpenflowVersion ofVersion, final NodeConnectorId ncId) {
        return portNumberfromNodeConnectorId(ofVersion, ncId.getValue());
    }

    public static String portNoStringfromNodeConnectorID(final String ncID) {
    	String[] split = ncID.split(":");

        // It can happen that token length will be just 1 i.e 2 or CONTROLLER
        // If the length is just one then this cannot be the new MD-SAL style node connector Id which
        // is of the form openflow:1:3.

        return split[split.length - 1];
    }

    public static Long portNumberfromNodeConnectorId(final OpenflowVersion ofVersion, final String ncId) {
        String portNoString = portNoStringfromNodeConnectorID(ncId);
        Long portNo = OpenflowPortsUtil.getPortFromLogicalName(ofVersion, portNoString);
        return portNo;
    }


    public static NodeConnectorRef nodeConnectorRefFromDatapathIdPortno(final BigInteger datapathId, final Long portNo, final OpenflowVersion ofVersion) {
        return new NodeConnectorRef(nodeConnectorInstanceIdentifierFromDatapathIdPortno(datapathId, portNo, ofVersion));
    }

    public static NodeConnectorRef nodeConnectorRefFromDatapathIdPortno(final BigInteger datapathId, final Long portNo,
                                                                        final OpenflowVersion ofVersion,
                                                                        final KeyedInstanceIdentifier<Node, NodeKey> nodePath) {
        return new NodeConnectorRef(nodeConnectorInstanceIdentifierFromDatapathIdPortno(datapathId, portNo, ofVersion, nodePath));
    }

    public static InstanceIdentifier<NodeConnector> nodeConnectorInstanceIdentifierFromDatapathIdPortno(
            final BigInteger datapathId, final Long portNo, final OpenflowVersion ofVersion) {
        NodeId nodeId = nodeIdFromDatapathId(datapathId);
        KeyedInstanceIdentifier<Node, NodeKey> nodePath = NODES_IDENTIFIER.child(Node.class, new NodeKey(nodeId));
        return nodeConnectorInstanceIdentifierFromDatapathIdPortno(datapathId, portNo, ofVersion, nodePath);
    }

    public static InstanceIdentifier<NodeConnector> nodeConnectorInstanceIdentifierFromDatapathIdPortno(
            final BigInteger datapathId, final Long portNo, final OpenflowVersion ofVersion, final KeyedInstanceIdentifier<Node, NodeKey> nodePath) {
        NodeConnectorId nodeConnectorId = nodeConnectorIdfromDatapathPortNo(datapathId, portNo, ofVersion);
        return nodePath.child(NodeConnector.class, new NodeConnectorKey(nodeConnectorId));
    }

    public static NodeConnectorUpdatedBuilder nodeConnectorUpdatedBuilderFromDatapathIdPortNo(final BigInteger datapathId,
                                                                                              final Long portNo, final OpenflowVersion ofVersion) {
        NodeConnectorUpdatedBuilder builder = new NodeConnectorUpdatedBuilder();
        builder.setId(InventoryDataServiceUtil.nodeConnectorIdfromDatapathPortNo(datapathId, portNo, ofVersion));
        builder.setNodeConnectorRef(InventoryDataServiceUtil.nodeConnectorRefFromDatapathIdPortno(datapathId, portNo, ofVersion));
        return builder;
    }

    public static NodeConnectorBuilder nodeConnectorBuilderFromDatapathIdPortNo(final BigInteger datapathId,
                                                                                final Long portNo, final OpenflowVersion ofVersion) {
        NodeConnectorBuilder builder = new NodeConnectorBuilder();
        builder.setId(InventoryDataServiceUtil.nodeConnectorIdfromDatapathPortNo(datapathId, portNo, ofVersion));
        return builder;
    }

    /**
     * @param dataPathId datapath id in big interger value
     * @return string of size 16, padded with '0'
     */
    public static String bigIntegerToPaddedHex(final BigInteger dataPathId) {
        return StringUtils.leftPad(dataPathId.toString(16), 16, "0");
    }

    //TODO : create new module openflowplugin-util, move there this method along with TestProviderTransactionUtil#getDataObject
    private static <T extends DataObject> T getDataObject(final ReadTransaction readOnlyTransaction, final InstanceIdentifier<T> identifier) {
        Optional<T> optionalData = null;
        try {
            optionalData = readOnlyTransaction.read(LogicalDatastoreType.OPERATIONAL, identifier).get();
            if (optionalData.isPresent()) {
                return optionalData.get();
            }
        } catch (ExecutionException | InterruptedException e) {
            LOG.error("Read transaction for identifier {} failed.", identifier, e);
        }
        return null;
    }

}