/*
 * Copyright (c) 2015, 2017 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.applications.topology.manager;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.opendaylight.mdsal.binding.api.DataObjectModification.ModificationType.DELETE;
import static org.opendaylight.mdsal.binding.api.DataObjectModification.ModificationType.WRITE;
import static org.opendaylight.openflowplugin.applications.topology.manager.TestUtils.assertDeletedIDs;
import static org.opendaylight.openflowplugin.applications.topology.manager.TestUtils.newDestTp;
import static org.opendaylight.openflowplugin.applications.topology.manager.TestUtils.newInvNodeConnKey;
import static org.opendaylight.openflowplugin.applications.topology.manager.TestUtils.newInvNodeKey;
import static org.opendaylight.openflowplugin.applications.topology.manager.TestUtils.newLink;
import static org.opendaylight.openflowplugin.applications.topology.manager.TestUtils.newNodeConnID;
import static org.opendaylight.openflowplugin.applications.topology.manager.TestUtils.newSourceTp;
import static org.opendaylight.openflowplugin.applications.topology.manager.TestUtils.setReadFutureAsync;
import static org.opendaylight.openflowplugin.applications.topology.manager.TestUtils.setupStubbedDeletes;
import static org.opendaylight.openflowplugin.applications.topology.manager.TestUtils.setupStubbedSubmit;
import static org.opendaylight.openflowplugin.applications.topology.manager.TestUtils.verifyMockTx;
import static org.opendaylight.openflowplugin.applications.topology.manager.TestUtils.waitForDeletes;
import static org.opendaylight.openflowplugin.applications.topology.manager.TestUtils.waitForSubmit;

import com.google.common.util.concurrent.FluentFuture;
import com.google.common.util.concurrent.SettableFuture;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.opendaylight.mdsal.binding.api.DataTreeModification;
import org.opendaylight.mdsal.binding.api.ReadWriteTransaction;
import org.opendaylight.mdsal.common.api.LogicalDatastoreType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeConnectorRef;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.topology.inventory.rev131030.InventoryNodeConnector;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.NodeId;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.TpId;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.Topology;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.TopologyBuilder;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.topology.Link;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.topology.Node;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.topology.NodeBuilder;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.topology.NodeKey;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.topology.node.TerminationPoint;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.topology.node.TerminationPointKey;
import org.opendaylight.yangtools.binding.DataObjectIdentifier;
import org.opendaylight.yangtools.binding.util.BindingMap;
import org.opendaylight.yangtools.util.concurrent.FluentFutures;

public class TerminationPointChangeListenerImplTest extends DataTreeChangeListenerBase {
    @SuppressWarnings("rawtypes")
    @Test
    public void testOnNodeConnectorRemoved() {

        NodeKey topoNodeKey = new NodeKey(new NodeId("node1"));
        TerminationPointKey terminationPointKey = new TerminationPointKey(new TpId("tp1"));

        final DataObjectIdentifier<Node> topoNodeII = topologyIID.toBuilder().child(Node.class, topoNodeKey).build();
        Node topoNode = new NodeBuilder().withKey(topoNodeKey).build();

        org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes
                .NodeKey nodeKey = newInvNodeKey(topoNodeKey.getNodeId().getValue());

        org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.node.NodeConnectorKey ncKey =
                newInvNodeConnKey(terminationPointKey.getTpId().getValue());

        final DataObjectIdentifier<?> invNodeConnID = newNodeConnID(nodeKey, ncKey);

        List<Link> linkList = Arrays.asList(
                newLink("link1", newSourceTp("tp1"), newDestTp("dest")),
                newLink("link2", newSourceTp("source"), newDestTp("tp1")),
                newLink("link3", newSourceTp("source2"), newDestTp("dest2")));
        final Topology topology = new TopologyBuilder().withKey(topologyIID.key())
            .setLink(BindingMap.ordered(linkList))
            .build();

        final DataObjectIdentifier[] expDeletedIIDs = {
                topologyIID.toBuilder().child(Link.class, linkList.get(0).key()).build(),
                topologyIID.toBuilder().child(Link.class, linkList.get(1).key()).build(),
                topologyIID.toBuilder().child(Node.class, new NodeKey(new NodeId("node1")))
                        .child(TerminationPoint.class, new TerminationPointKey(new TpId("tp1"))).build()
            };

        final SettableFuture<Optional<Topology>> readFuture = SettableFuture.create();
        readFuture.set(Optional.of(topology));
        ReadWriteTransaction mockTx1 = mock(ReadWriteTransaction.class);
        doReturn(FluentFuture.from(readFuture)).when(mockTx1).read(LogicalDatastoreType.OPERATIONAL, topologyIID);

        SettableFuture<Optional<Node>> readFutureNode = SettableFuture.create();
        readFutureNode.set(Optional.of(topoNode));
        doReturn(FluentFuture.from(readFutureNode)).when(mockTx1).read(LogicalDatastoreType.OPERATIONAL, topoNodeII);

        final CountDownLatch submitLatch1 = setupStubbedSubmit(mockTx1);

        int expDeleteCalls = expDeletedIIDs.length;
        CountDownLatch deleteLatch = new CountDownLatch(expDeleteCalls);
        ArgumentCaptor<DataObjectIdentifier> deletedLinkIDs = ArgumentCaptor.forClass(DataObjectIdentifier.class);
        setupStubbedDeletes(mockTx1, deletedLinkIDs, deleteLatch);

        doReturn(mockTx1).when(mockTxChain).newReadWriteTransaction();

        DataTreeModification dataTreeModification = setupDataTreeChange(DELETE, invNodeConnID, false);
        terminationPointListener.onDataTreeChanged(List.of(dataTreeModification));

        waitForSubmit(submitLatch1);

        setReadFutureAsync(topology, readFuture);

        waitForDeletes(expDeleteCalls, deleteLatch);

        assertDeletedIDs(expDeletedIIDs, deletedLinkIDs);

        verifyMockTx(mockTx1);
    }

    @SuppressWarnings("rawtypes")
    @Test
    public void testOnNodeConnectorRemovedWithNoTopology() {

        NodeKey topoNodeKey = new NodeKey(new NodeId("node1"));
        TerminationPointKey terminationPointKey = new TerminationPointKey(new TpId("tp1"));

        DataObjectIdentifier<Node> topoNodeII = topologyIID.toBuilder().child(Node.class, topoNodeKey).build();
        Node topoNode = new NodeBuilder().withKey(topoNodeKey).build();

        org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.NodeKey
                nodeKey = newInvNodeKey(topoNodeKey.getNodeId().getValue());

        org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.node.NodeConnectorKey ncKey =
                newInvNodeConnKey(terminationPointKey.getTpId().getValue());

        final var invNodeConnID = newNodeConnID(nodeKey, ncKey);

        final DataObjectIdentifier[] expDeletedIIDs = {
            topologyIID.toBuilder()
                .child(Node.class, new NodeKey(new NodeId("node1")))
                .child(TerminationPoint.class, new TerminationPointKey(new TpId("tp1")))
                .build()
        };

        ReadWriteTransaction mockTx = mock(ReadWriteTransaction.class);
        doReturn(FluentFutures.immediateFluentFuture(Optional.empty())).when(mockTx)
                .read(LogicalDatastoreType.OPERATIONAL, topologyIID);
        final CountDownLatch submitLatch = setupStubbedSubmit(mockTx);

        doReturn(FluentFutures.immediateFluentFuture(Optional.of(topoNode))).when(mockTx)
                .read(LogicalDatastoreType.OPERATIONAL, topoNodeII);

        CountDownLatch deleteLatch = new CountDownLatch(1);
        ArgumentCaptor<DataObjectIdentifier> deletedLinkIDs = ArgumentCaptor.forClass(DataObjectIdentifier.class);
        setupStubbedDeletes(mockTx, deletedLinkIDs, deleteLatch);

        doReturn(mockTx).when(mockTxChain).newReadWriteTransaction();

        DataTreeModification dataTreeModification = setupDataTreeChange(DELETE, invNodeConnID, false);
        terminationPointListener.onDataTreeChanged(List.of(dataTreeModification));

        waitForSubmit(submitLatch);

        waitForDeletes(1, deleteLatch);

        assertDeletedIDs(expDeletedIIDs, deletedLinkIDs);
    }

    @Test
    public void testOnNodeConnectorUpdated() {

        org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.NodeKey
                                                                 nodeKey = newInvNodeKey("node1");

        org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.node.NodeConnectorKey ncKey =
                newInvNodeConnKey("tp1");

        var invNodeConnID = newNodeConnID(nodeKey, ncKey);

        ReadWriteTransaction mockTx = mock(ReadWriteTransaction.class);
        CountDownLatch submitLatch = setupStubbedSubmit(mockTx);
        doReturn(mockTx).when(mockTxChain).newReadWriteTransaction();

        DataTreeModification dataTreeModification = setupDataTreeChange(WRITE, invNodeConnID, true);
        terminationPointListener.onDataTreeChanged(List.of(dataTreeModification));

        waitForSubmit(submitLatch);

        ArgumentCaptor<TerminationPoint> mergedNode = ArgumentCaptor.forClass(TerminationPoint.class);
        NodeId expNodeId = new NodeId("node1");
        TpId expTpId = new TpId("tp1");
        final var expTpPath = topologyIID.toBuilder()
            .child(Node.class, new NodeKey(expNodeId))
            .child(TerminationPoint.class, new TerminationPointKey(expTpId))
            .build();
        verify(mockTx).mergeParentStructureMerge(eq(LogicalDatastoreType.OPERATIONAL), eq(expTpPath.toIdentifier()),
                mergedNode.capture());
        assertEquals("getTpId", expTpId, mergedNode.getValue().getTpId());
        InventoryNodeConnector augmentation = mergedNode.getValue().augmentation(
                InventoryNodeConnector.class);
        assertNotNull("Missing augmentation", augmentation);
        assertEquals("getInventoryNodeConnectorRef", new NodeConnectorRef(invNodeConnID.toIdentifier()),
                augmentation.getInventoryNodeConnectorRef());
    }

    @SuppressWarnings("rawtypes")
    @Test
    public void testOnNodeConnectorUpdatedWithLinkStateDown() {

        org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.NodeKey
                                                                 nodeKey = newInvNodeKey("node1");

        org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.node.NodeConnectorKey ncKey =
                newInvNodeConnKey("tp1");

        final var invNodeConnID = newNodeConnID(nodeKey, ncKey);

        List<Link> linkList = Arrays.asList(newLink("link1", newSourceTp("tp1"), newDestTp("dest")));
        Topology topology = new TopologyBuilder().withKey(topologyIID.key())
            .setLink(BindingMap.ordered(linkList))
            .build();

        ReadWriteTransaction mockTx = mock(ReadWriteTransaction.class);
        doReturn(FluentFutures.immediateFluentFuture(Optional.of(topology))).when(mockTx)
                .read(LogicalDatastoreType.OPERATIONAL, topologyIID);
        setupStubbedSubmit(mockTx);

        CountDownLatch deleteLatch = new CountDownLatch(1);
        ArgumentCaptor<DataObjectIdentifier> deletedLinkIDs = ArgumentCaptor.forClass(DataObjectIdentifier.class);
        setupStubbedDeletes(mockTx, deletedLinkIDs, deleteLatch);

        doReturn(mockTx).when(mockTxChain).newReadWriteTransaction();

        DataTreeModification dataTreeModification = setupDataTreeChange(WRITE, invNodeConnID, false);
        when(dataTreeModification.getRootNode().dataAfter())
                .thenReturn(provideFlowCapableNodeConnector(true, false));
        terminationPointListener.onDataTreeChanged(List.of(dataTreeModification));

        waitForDeletes(1, deleteLatch);

        DataObjectIdentifier<TerminationPoint> expTpPath = topologyIID.toBuilder()
            .child(Node.class, new NodeKey(new NodeId("node1")))
            .child(TerminationPoint.class, new TerminationPointKey(new TpId("tp1")))
            .build();

        verify(mockTx).mergeParentStructureMerge(eq(LogicalDatastoreType.OPERATIONAL), eq(expTpPath),
                any(TerminationPoint.class));

        assertDeletedIDs(new DataObjectIdentifier[] {
            topologyIID.toBuilder().child(Link.class, linkList.get(0).key()).build()
        }, deletedLinkIDs);
    }

    @SuppressWarnings("rawtypes")
    @Test
    public void testOnNodeConnectorUpdatedWithPortDown() {

        org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.NodeKey
                                                                 nodeKey = newInvNodeKey("node1");

        org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.node.NodeConnectorKey ncKey =
                newInvNodeConnKey("tp1");

        final var invNodeConnID = newNodeConnID(nodeKey, ncKey);

        List<Link> linkList = Arrays.asList(newLink("link1", newSourceTp("tp1"), newDestTp("dest")));
        Topology topology = new TopologyBuilder().withKey(topologyIID.key())
            .setLink(BindingMap.ordered(linkList))
            .build();

        ReadWriteTransaction mockTx = mock(ReadWriteTransaction.class);
        doReturn(FluentFutures.immediateFluentFuture(Optional.of(topology))).when(mockTx)
                .read(LogicalDatastoreType.OPERATIONAL, topologyIID);
        setupStubbedSubmit(mockTx);

        CountDownLatch deleteLatch = new CountDownLatch(1);
        ArgumentCaptor<DataObjectIdentifier> deletedLinkIDs = ArgumentCaptor.forClass(DataObjectIdentifier.class);
        setupStubbedDeletes(mockTx, deletedLinkIDs, deleteLatch);

        doReturn(mockTx).when(mockTxChain).newReadWriteTransaction();

        DataTreeModification dataTreeModification = setupDataTreeChange(WRITE, invNodeConnID, false);
        when(dataTreeModification.getRootNode().dataAfter())
                .thenReturn(provideFlowCapableNodeConnector(false, true));
        terminationPointListener.onDataTreeChanged(List.of(dataTreeModification));

        waitForDeletes(1, deleteLatch);

        DataObjectIdentifier<TerminationPoint> expTpPath = topologyIID.toBuilder()
            .child(Node.class, new NodeKey(new NodeId("node1")))
            .child(TerminationPoint.class, new TerminationPointKey(new TpId("tp1")))
            .build();

        verify(mockTx).mergeParentStructureMerge(eq(LogicalDatastoreType.OPERATIONAL), eq(expTpPath),
                any(TerminationPoint.class));

        assertDeletedIDs(new DataObjectIdentifier[] {
            topologyIID.toBuilder().child(Link.class, linkList.get(0).key()).build()
        }, deletedLinkIDs);
    }
}
