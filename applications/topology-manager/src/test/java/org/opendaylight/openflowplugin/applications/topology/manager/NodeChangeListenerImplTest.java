/**
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.applications.topology.manager;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.opendaylight.controller.md.sal.binding.api.DataObjectModification.ModificationType.DELETE;
import static org.opendaylight.controller.md.sal.binding.api.DataObjectModification.ModificationType.WRITE;
import static org.opendaylight.openflowplugin.applications.topology.manager.TestUtils.assertDeletedIDs;
import static org.opendaylight.openflowplugin.applications.topology.manager.TestUtils.newDestNode;
import static org.opendaylight.openflowplugin.applications.topology.manager.TestUtils.newInvNodeKey;
import static org.opendaylight.openflowplugin.applications.topology.manager.TestUtils.newLink;
import static org.opendaylight.openflowplugin.applications.topology.manager.TestUtils.newSourceNode;
import static org.opendaylight.openflowplugin.applications.topology.manager.TestUtils.setReadFutureAsync;
import static org.opendaylight.openflowplugin.applications.topology.manager.TestUtils.setupStubbedDeletes;
import static org.opendaylight.openflowplugin.applications.topology.manager.TestUtils.setupStubbedSubmit;
import static org.opendaylight.openflowplugin.applications.topology.manager.TestUtils.verifyMockTx;
import static org.opendaylight.openflowplugin.applications.topology.manager.TestUtils.waitForDeletes;
import static org.opendaylight.openflowplugin.applications.topology.manager.TestUtils.waitForSubmit;

import com.google.common.base.Optional;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.SettableFuture;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.opendaylight.controller.md.sal.binding.api.DataTreeModification;
import org.opendaylight.controller.md.sal.binding.api.ReadWriteTransaction;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.controller.md.sal.common.api.data.ReadFailedException;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeRef;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.Nodes;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.topology.inventory.rev131030.InventoryNode;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.NodeId;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.Topology;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.TopologyBuilder;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.topology.Link;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.topology.Node;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.topology.NodeBuilder;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.topology.NodeKey;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class NodeChangeListenerImplTest extends DataTreeChangeListenerBase {
    @SuppressWarnings({ "rawtypes" })
    @Test
    public void testOnNodeRemoved() {

        NodeKey topoNodeKey = new NodeKey(new NodeId("node1"));
        InstanceIdentifier<Node> topoNodeII = topologyIID.child(Node.class, topoNodeKey);
        Node topoNode = new NodeBuilder().setKey(topoNodeKey).build();

        org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.NodeKey
                                                                nodeKey = newInvNodeKey(topoNodeKey.getNodeId().getValue());
        InstanceIdentifier<?> invNodeID = InstanceIdentifier.create(Nodes.class).child(
                org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node.class,
                nodeKey);

        List<Link> linkList = Arrays.asList(
                newLink("link1", newSourceNode("node1"), newDestNode("dest")),
                newLink("link2", newSourceNode("source"), newDestNode("node1")),
                newLink("link2", newSourceNode("source2"), newDestNode("dest2")));
        final Topology topology = new TopologyBuilder().setLink(linkList).build();

        InstanceIdentifier[] expDeletedIIDs = {
                topologyIID.child(Link.class, linkList.get(0).getKey()),
                topologyIID.child(Link.class, linkList.get(1).getKey()),
                topologyIID.child(Node.class, new NodeKey(new NodeId("node1")))
            };

        SettableFuture<Optional<Topology>> readFuture = SettableFuture.create();
        readFuture.set(Optional.of(topology));
        ReadWriteTransaction mockTx1 = mock(ReadWriteTransaction.class);
        doReturn(Futures.makeChecked(readFuture, ReadFailedException.MAPPER)).when(mockTx1)
                .read(LogicalDatastoreType.OPERATIONAL, topologyIID);

        SettableFuture<Optional<Node>> readFutureNode = SettableFuture.create();
        readFutureNode.set(Optional.of(topoNode));
        doReturn(Futures.makeChecked(readFutureNode, ReadFailedException.MAPPER)).when(mockTx1)
                .read(LogicalDatastoreType.OPERATIONAL, topoNodeII);

        CountDownLatch submitLatch1 = setupStubbedSubmit(mockTx1);

        int expDeleteCalls = expDeletedIIDs.length;
        CountDownLatch deleteLatch = new CountDownLatch(expDeleteCalls);
        ArgumentCaptor<InstanceIdentifier> deletedLinkIDs =
                ArgumentCaptor.forClass(InstanceIdentifier.class);
        setupStubbedDeletes(mockTx1, deletedLinkIDs, deleteLatch);

        doReturn(mockTx1).when(mockTxChain).newReadWriteTransaction();

        DataTreeModification dataTreeModification = setupDataTreeChange(DELETE, invNodeID);
        nodeChangeListener.onDataTreeChanged(Collections.singleton(dataTreeModification));

        waitForSubmit(submitLatch1);

        setReadFutureAsync(topology, readFuture);

        waitForDeletes(expDeleteCalls, deleteLatch);

        assertDeletedIDs(expDeletedIIDs, deletedLinkIDs);

        verifyMockTx(mockTx1);
    }

    @SuppressWarnings({ "rawtypes" })
    @Test
    public void testOnNodeRemovedWithNoTopology() {

        NodeKey topoNodeKey = new NodeKey(new NodeId("node1"));
        InstanceIdentifier<Node> topoNodeII = topologyIID.child(Node.class, topoNodeKey);
        Node topoNode = new NodeBuilder().setKey(topoNodeKey).build();

        org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.NodeKey
                nodeKey = newInvNodeKey(topoNodeKey.getNodeId().getValue());
        InstanceIdentifier<?> invNodeID = InstanceIdentifier.create(Nodes.class).child(
                org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node.class,
                nodeKey);

        InstanceIdentifier[] expDeletedIIDs = {
                topologyIID.child(Node.class, new NodeKey(new NodeId("node1")))
            };

        ReadWriteTransaction mockTx = mock(ReadWriteTransaction.class);
        doReturn(Futures.immediateCheckedFuture(Optional.absent())).when(mockTx)
                .read(LogicalDatastoreType.OPERATIONAL, topologyIID);
        CountDownLatch submitLatch = setupStubbedSubmit(mockTx);

        SettableFuture<Optional<Node>> readFutureNode = SettableFuture.create();
        readFutureNode.set(Optional.of(topoNode));
        doReturn(Futures.makeChecked(readFutureNode, ReadFailedException.MAPPER)).when(mockTx)
                .read(LogicalDatastoreType.OPERATIONAL, topoNodeII);

        CountDownLatch deleteLatch = new CountDownLatch(1);
        ArgumentCaptor<InstanceIdentifier> deletedLinkIDs =
                ArgumentCaptor.forClass(InstanceIdentifier.class);
        setupStubbedDeletes(mockTx, deletedLinkIDs, deleteLatch);

        doReturn(mockTx).when(mockTxChain).newReadWriteTransaction();

        DataTreeModification dataTreeModification = setupDataTreeChange(DELETE, invNodeID);
        nodeChangeListener.onDataTreeChanged(Collections.singleton(dataTreeModification));

        waitForSubmit(submitLatch);

        waitForDeletes(1, deleteLatch);

        assertDeletedIDs(expDeletedIIDs, deletedLinkIDs);
    }

    @Test
    public void testOnNodeAdded() {

        org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.NodeKey
                                                            nodeKey = newInvNodeKey("node1");
        InstanceIdentifier<?> invNodeID = InstanceIdentifier.create(Nodes.class).child(
                org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node.class,
                nodeKey);

        ReadWriteTransaction mockTx = mock(ReadWriteTransaction.class);
        CountDownLatch submitLatch = setupStubbedSubmit(mockTx);
        doReturn(mockTx).when(mockTxChain).newReadWriteTransaction();

        DataTreeModification dataTreeModification = setupDataTreeChange(WRITE, invNodeID);
        nodeChangeListener.onDataTreeChanged(Collections.singleton(dataTreeModification));

        waitForSubmit(submitLatch);

        ArgumentCaptor<Node> mergedNode = ArgumentCaptor.forClass(Node.class);
        NodeId expNodeId = new NodeId("node1");
        verify(mockTx).merge(eq(LogicalDatastoreType.OPERATIONAL), eq(topologyIID.child(Node.class,
                new NodeKey(expNodeId))), mergedNode.capture(), eq(true));
        assertEquals("getNodeId", expNodeId, mergedNode.getValue().getNodeId());
        InventoryNode augmentation = mergedNode.getValue().getAugmentation(InventoryNode.class);
        assertNotNull("Missing augmentation", augmentation);
        assertEquals("getInventoryNodeRef", new NodeRef(invNodeID), augmentation.getInventoryNodeRef());
    }

}
