/*
 * Copyright (c) 2015, 2017 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.applications.topology.manager;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;

import com.google.common.util.concurrent.SettableFuture;
import com.google.common.util.concurrent.Uninterruptibles;
import java.util.HashSet;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.opendaylight.mdsal.binding.api.ReadWriteTransaction;
import org.opendaylight.mdsal.common.api.CommitInfo;
import org.opendaylight.mdsal.common.api.LogicalDatastoreType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.Nodes;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.node.NodeConnector;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.node.NodeConnectorKey;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.LinkId;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.NodeId;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.TpId;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.link.attributes.Destination;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.link.attributes.DestinationBuilder;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.link.attributes.Source;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.link.attributes.SourceBuilder;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.Topology;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.topology.Link;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.topology.LinkBuilder;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.topology.LinkKey;
import org.opendaylight.yangtools.binding.DataObjectIdentifier;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.binding.KeyedInstanceIdentifier;

public final class TestUtils {
    private TestUtils() {
    }

    static void verifyMockTx(final ReadWriteTransaction mockTx) {
        InOrder inOrder = inOrder(mockTx);
        inOrder.verify(mockTx, atLeast(0)).commit();
        inOrder.verify(mockTx, never()).delete(eq(LogicalDatastoreType.OPERATIONAL), any(InstanceIdentifier.class));
    }

    @SuppressWarnings("rawtypes")
    static void assertDeletedIDs(final InstanceIdentifier[] expDeletedIIDs,
                                 final ArgumentCaptor<DataObjectIdentifier> deletedLinkIDs) {
        final var actualIIDs = new HashSet<>(deletedLinkIDs.getAllValues());
        for (var id : expDeletedIIDs) {
            assertTrue("Missing expected deleted IID " + id, actualIIDs.contains(id.toIdentifier()));
        }
    }

    static void setReadFutureAsync(final Topology topology, final SettableFuture<Optional<Topology>> readFuture) {
        new Thread(() -> {
            Uninterruptibles.sleepUninterruptibly(5, TimeUnit.SECONDS);
            readFuture.set(Optional.of(topology));
        }).start();
    }

    static void waitForSubmit(final CountDownLatch latch) {
        assertEquals("Transaction submitted", true, Uninterruptibles.awaitUninterruptibly(latch, 5, TimeUnit.SECONDS));
    }

    static void waitForDeletes(final int expDeleteCalls, final CountDownLatch latch) {
        boolean done = Uninterruptibles.awaitUninterruptibly(latch, 5, TimeUnit.SECONDS);
        if (!done) {
            fail("Expected " + expDeleteCalls + " delete calls. Actual: " + (expDeleteCalls - latch.getCount()));
        }
    }

    static CountDownLatch setupStubbedSubmit(final ReadWriteTransaction mockTx) {
        final CountDownLatch latch = new CountDownLatch(1);
        doAnswer(invocation -> {
            latch.countDown();
            return CommitInfo.emptyFluentFuture();
        }).when(mockTx).commit();

        return latch;
    }

    @SuppressWarnings("rawtypes")
    static void setupStubbedDeletes(final ReadWriteTransaction mockTx, final ArgumentCaptor<DataObjectIdentifier> deletedLinkIDs,
                                    final CountDownLatch latch) {
        doAnswer(invocation -> {
            latch.countDown();
            return null;
        }).when(mockTx).delete(eq(LogicalDatastoreType.OPERATIONAL), deletedLinkIDs.capture());
    }

    static org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.NodeKey newInvNodeKey(final String id) {
        org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.NodeKey nodeKey
                = new org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.NodeKey(
                    new org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId(id));
        return nodeKey;
    }

    static NodeConnectorKey newInvNodeConnKey(final String id) {
        return new org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.node.NodeConnectorKey(
                new org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeConnectorId(id));
    }

    static KeyedInstanceIdentifier<NodeConnector, NodeConnectorKey> newNodeConnID(
            final org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.NodeKey nodeKey,
            final org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.node.NodeConnectorKey ncKey) {
        return InstanceIdentifier.create(Nodes.class)
                .child(org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node.class, nodeKey)
                .child(org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.node.NodeConnector.class,
                       ncKey);
    }

    static Link newLink(final String id, final Source source, final Destination dest) {
        return new LinkBuilder().setLinkId(new LinkId(id)).setSource(source).setDestination(dest)
                .withKey(new LinkKey(new LinkId(id))).build();
    }

    static Destination newDestTp(final String id) {
        return new DestinationBuilder().setDestTp(new TpId(id)).build();
    }

    static Source newSourceTp(final String id) {
        return new SourceBuilder().setSourceTp(new TpId(id)).build();
    }

    static Destination newDestNode(final String id) {
        return new DestinationBuilder().setDestNode(new NodeId(id)).build();
    }

    static Source newSourceNode(final String id) {
        return new SourceBuilder().setSourceNode(new NodeId(id)).build();
    }

}
