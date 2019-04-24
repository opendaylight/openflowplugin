/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.openflow.md.util;

import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import java.math.BigInteger;
import java.util.List;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.openflowplugin.api.OFConstants;
import org.opendaylight.openflowplugin.api.openflow.md.util.OpenflowVersion;
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
import org.opendaylight.yangtools.yang.binding.KeyedInstanceIdentifier;
import org.opendaylight.yangtools.yang.common.Uint32;
import org.opendaylight.yangtools.yang.common.Uint64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class InventoryDataServiceUtil {
    private static final Splitter COLON_SPLITTER = Splitter.on(":");
    private static final Logger LOG = LoggerFactory.getLogger(InventoryDataServiceUtil.class);

    /*
     * Get an InstanceIdentifier for the Nodes class that is the root of the
     * inventory tree We use this alot, so its worth keeping around
     */
    private static final InstanceIdentifier<Nodes> NODES_IDENTIFIER = InstanceIdentifier.create(Nodes.class);

    public static InstanceIdentifier<Node> identifierFromDatapathId(final Uint64 datapathId) {
        NodeKey nodeKey = nodeKeyFromDatapathId(datapathId);
        return NODES_IDENTIFIER.child(Node.class, nodeKey);
    }

    public static NodeKey nodeKeyFromDatapathId(final Uint64 datapathId) {
        return new NodeKey(nodeIdFromDatapathId(datapathId));
    }

    public static NodeUpdatedBuilder nodeUpdatedBuilderFromDataPathId(final Uint64 datapathId) {
        NodeUpdatedBuilder builder = new NodeUpdatedBuilder();
        builder.setId(nodeIdFromDatapathId(datapathId));
        builder.setNodeRef(nodeRefFromNodeKey(new NodeKey(builder.getId())));
        return builder;
    }

    public static NodeId nodeIdFromDatapathId(final Uint64 datapathId) {
        // FIXME: Convert to textual representation of datapathID
        String current = datapathId.toString();
        return new NodeId(OFConstants.OF_URI_PREFIX + current);
    }

    public static Uint64 dataPathIdFromNodeId(final NodeId nodeId) {
        return Uint64.valueOf(nodeId.getValue().replace(OFConstants.OF_URI_PREFIX, ""));
    }

    public static NodeRef nodeRefFromNodeKey(final NodeKey nodeKey) {
        return new NodeRef(nodeKeyToInstanceIdentifier(nodeKey));
    }

    public static InstanceIdentifier<Node> nodeKeyToInstanceIdentifier(final NodeKey nodeKey) {
        return NODES_IDENTIFIER.child(Node.class, nodeKey);
    }

    public static NodeConnectorId nodeConnectorIdfromDatapathPortNo(final Uint64 datapathid, final Uint32 portNo,
                                                                    final OpenflowVersion ofVersion) {
        String logicalName = OpenflowPortsUtil.getPortLogicalName(ofVersion, portNo);
        return new NodeConnectorId(OFConstants.OF_URI_PREFIX + datapathid + ":" + (logicalName == null
                ? portNo : logicalName));
    }

    @Nullable
    public static Uint32 portNumberfromNodeConnectorId(final OpenflowVersion ofVersion, final NodeConnectorId ncId) {
        return portNumberfromNodeConnectorId(ofVersion, ncId.getValue());
    }

    @Nullable
    public static Uint32 portNumberfromNodeConnectorId(final OpenflowVersion ofVersion, @NonNull final String ncId) {
        String portNoString = portNoStringfromNodeConnectorID(ncId);
        return OpenflowPortsUtil.getPortFromLogicalName(ofVersion, portNoString);
    }

    public static String portNoStringfromNodeConnectorID(final String ncID) {

        List<String> splitStringList = COLON_SPLITTER.splitToList(ncID);

        // It can happen that token length will be just 1 i.e 2 or CONTROLLER
        // If the length is just one then this cannot be the new MD-SAL style node connector Id which
        // is of the form openflow:1:3.

        return splitStringList.get(splitStringList.size() - 1);
    }


    public static NodeConnectorRef nodeConnectorRefFromDatapathIdPortno(final Uint64 datapathId, final Uint32 portNo,
            final OpenflowVersion ofVersion) {
        return new NodeConnectorRef(nodeConnectorInstanceIdentifierFromDatapathIdPortno(datapathId, portNo, ofVersion));
    }

    public static NodeConnectorRef nodeConnectorRefFromDatapathIdPortno(final Uint64 datapathId, final Uint32 portNo,
            final OpenflowVersion ofVersion, final KeyedInstanceIdentifier<Node, NodeKey> nodePath) {
        return new NodeConnectorRef(
                nodeConnectorInstanceIdentifierFromDatapathIdPortno(datapathId, portNo, ofVersion, nodePath));
    }

    public static InstanceIdentifier<NodeConnector> nodeConnectorInstanceIdentifierFromDatapathIdPortno(
            final Uint64 datapathId, final Uint32 portNo, final OpenflowVersion ofVersion) {
        NodeId nodeId = nodeIdFromDatapathId(datapathId);
        KeyedInstanceIdentifier<Node, NodeKey> nodePath = NODES_IDENTIFIER.child(Node.class, new NodeKey(nodeId));
        return nodeConnectorInstanceIdentifierFromDatapathIdPortno(datapathId, portNo, ofVersion, nodePath);
    }

    public static InstanceIdentifier<NodeConnector> nodeConnectorInstanceIdentifierFromDatapathIdPortno(
            final Uint64 datapathId, final Uint32 portNo, final OpenflowVersion ofVersion,
            final KeyedInstanceIdentifier<Node, NodeKey> nodePath) {
        NodeConnectorId nodeConnectorId = nodeConnectorIdfromDatapathPortNo(datapathId, portNo, ofVersion);
        return nodePath.child(NodeConnector.class, new NodeConnectorKey(nodeConnectorId));
    }

    public static NodeConnectorUpdatedBuilder nodeConnectorUpdatedBuilderFromDatapathIdPortNo(
            final Uint64 datapathId, final Uint32 portNo, final OpenflowVersion ofVersion) {
        NodeConnectorUpdatedBuilder builder = new NodeConnectorUpdatedBuilder();
        builder.setId(InventoryDataServiceUtil.nodeConnectorIdfromDatapathPortNo(datapathId, portNo, ofVersion));
        builder.setNodeConnectorRef(InventoryDataServiceUtil.nodeConnectorRefFromDatapathIdPortno(
                datapathId, portNo, ofVersion));
        return builder;
    }

    public static NodeConnectorBuilder nodeConnectorBuilderFromDatapathIdPortNo(final Uint64 datapathId,
            final Uint32 portNo, final OpenflowVersion ofVersion) {
        NodeConnectorBuilder builder = new NodeConnectorBuilder();
        builder.setId(InventoryDataServiceUtil.nodeConnectorIdfromDatapathPortNo(datapathId, portNo, ofVersion));
        return builder;
    }

    /**
     * Converts a BigInteger to a padded hex value.
     *
     * @param dataPathId datapath id in big interger value
     * @return string of size 16, padded with '0'
     */
    public static String bigIntegerToPaddedHex(final BigInteger dataPathId) {
        return Strings.padStart(dataPathId.toString(16), 16, '0');
    }

    public static Uint64 extractDatapathId(final NodeRef ref) {
        return InventoryDataServiceUtil.dataPathIdFromNodeId(ref.getValue().firstKeyOf(Node.class).getId());
    }
}
