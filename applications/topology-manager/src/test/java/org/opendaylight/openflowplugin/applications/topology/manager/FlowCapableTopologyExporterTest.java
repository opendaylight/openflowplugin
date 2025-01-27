/*
 * Copyright (c) 2014 Brocade Communications Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.applications.topology.manager;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.opendaylight.openflowplugin.applications.topology.manager.TestUtils.newDestTp;
import static org.opendaylight.openflowplugin.applications.topology.manager.TestUtils.newInvNodeConnKey;
import static org.opendaylight.openflowplugin.applications.topology.manager.TestUtils.newInvNodeKey;
import static org.opendaylight.openflowplugin.applications.topology.manager.TestUtils.newLink;
import static org.opendaylight.openflowplugin.applications.topology.manager.TestUtils.newNodeConnID;
import static org.opendaylight.openflowplugin.applications.topology.manager.TestUtils.newSourceTp;
import static org.opendaylight.openflowplugin.applications.topology.manager.TestUtils.setupStubbedSubmit;
import static org.opendaylight.openflowplugin.applications.topology.manager.TestUtils.waitForSubmit;

import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.opendaylight.mdsal.binding.api.DataBroker;
import org.opendaylight.mdsal.binding.api.ReadWriteTransaction;
import org.opendaylight.mdsal.binding.api.TransactionChain;
import org.opendaylight.mdsal.common.api.LogicalDatastoreType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.topology.discovery.rev130819.LinkDiscoveredBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.topology.discovery.rev130819.LinkRemovedBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeConnectorRef;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.LinkId;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.NetworkTopology;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.TopologyId;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.Topology;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.TopologyKey;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.topology.Link;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.topology.LinkKey;
import org.opendaylight.yangtools.binding.DataObjectIdentifier;
import org.opendaylight.yangtools.binding.DataObjectIdentifier.WithKey;
import org.opendaylight.yangtools.util.concurrent.FluentFutures;

@RunWith(MockitoJUnitRunner.class)
public class FlowCapableTopologyExporterTest {

    private OperationProcessor processor;
    private FlowCapableTopologyExporter exporter;
    private WithKey<Topology, TopologyKey> topologyIID;
    private final ExecutorService executor = Executors.newFixedThreadPool(1);
    @Mock
    private DataBroker mockDataBroker;
    @Mock
    private TransactionChain mockTxChain;

    @Before
    public void setUp() {
        doReturn(mockTxChain).when(mockDataBroker).createTransactionChain();

        processor = new OperationProcessor(mockDataBroker);

        topologyIID = DataObjectIdentifier.builder(NetworkTopology.class)
                .child(Topology.class, new TopologyKey(new TopologyId("flow:1")))
                .build();
        exporter = new FlowCapableTopologyExporter(processor, topologyIID);
        executor.execute(processor);
    }

    @After
    public void tearDown() {
        executor.shutdownNow();
    }


    @Test
    public void testOnLinkDiscovered() {

        org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.NodeKey
                sourceNodeKey = newInvNodeKey("sourceNode");
        org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.node.NodeConnectorKey
                sourceNodeConnKey = newInvNodeConnKey("sourceTP");
        var sourceConnID = newNodeConnID(sourceNodeKey, sourceNodeConnKey);

        org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.NodeKey
                destNodeKey = newInvNodeKey("destNode");
        org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.node.NodeConnectorKey
                destNodeConnKey = newInvNodeConnKey("destTP");
        var destConnID = newNodeConnID(destNodeKey, destNodeConnKey);

        ReadWriteTransaction mockTx = mock(ReadWriteTransaction.class);
        CountDownLatch submitLatch = setupStubbedSubmit(mockTx);
        doReturn(mockTx).when(mockTxChain).newReadWriteTransaction();

        exporter.onLinkDiscovered(new LinkDiscoveredBuilder()
            .setSource(new NodeConnectorRef(sourceConnID))
            .setDestination(new NodeConnectorRef(destConnID))
            .build());

        waitForSubmit(submitLatch);

        ArgumentCaptor<Link> mergedNode = ArgumentCaptor.forClass(Link.class);
        verify(mockTx).mergeParentStructureMerge(eq(LogicalDatastoreType.OPERATIONAL), eq(topologyIID.toBuilder()
            .child(Link.class, new LinkKey(new LinkId(sourceNodeConnKey.getId())))
            .build()), mergedNode.capture());
        assertEquals("Source node ID", "sourceNode",
                mergedNode.getValue().getSource().getSourceNode().getValue());
        assertEquals("Dest TP ID", "sourceTP",
                mergedNode.getValue().getSource().getSourceTp().getValue());
        assertEquals("Dest node ID", "destNode",
                mergedNode.getValue().getDestination().getDestNode().getValue());
        assertEquals("Dest TP ID", "destTP",
                mergedNode.getValue().getDestination().getDestTp().getValue());
    }

    @Test
    public void testOnLinkRemoved() {

        org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.NodeKey
                sourceNodeKey = newInvNodeKey("sourceNode");
        org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.node.NodeConnectorKey
                sourceNodeConnKey = newInvNodeConnKey("sourceTP");
        var sourceConnID = newNodeConnID(sourceNodeKey, sourceNodeConnKey);

        org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.NodeKey
                destNodeKey = newInvNodeKey("destNode");
        org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.node.NodeConnectorKey
                destNodeConnKey = newInvNodeConnKey("destTP");
        var destConnID = newNodeConnID(destNodeKey, destNodeConnKey);

        Link link = newLink(sourceNodeConnKey.getId().getValue(), newSourceTp(sourceNodeConnKey.getId().getValue()),
                newDestTp(destNodeConnKey.getId().getValue()));

        ReadWriteTransaction mockTx = mock(ReadWriteTransaction.class);
        final CountDownLatch submitLatch = setupStubbedSubmit(mockTx);
        doReturn(mockTx).when(mockTxChain).newReadWriteTransaction();
        doReturn(FluentFutures.immediateFluentFuture(Optional.of(link))).when(mockTx)
                .read(LogicalDatastoreType.OPERATIONAL, topologyIID.toBuilder()
                      .child(Link.class, new LinkKey(new LinkId(sourceNodeConnKey.getId())))
                      .build());

        exporter.onLinkRemoved(new LinkRemovedBuilder().setSource(
                new NodeConnectorRef(sourceConnID)).setDestination(
                    new NodeConnectorRef(destConnID)).build());

        waitForSubmit(submitLatch);

        verify(mockTx).delete(LogicalDatastoreType.OPERATIONAL, topologyIID.toBuilder()
            .child(Link.class, new LinkKey(new LinkId(sourceNodeConnKey.getId())))
            .build());
    }

    @Test
    public void testOnLinkRemovedLinkDoesNotExist() {

        org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.NodeKey
                sourceNodeKey = newInvNodeKey("sourceNode");
        org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.node.NodeConnectorKey
                sourceNodeConnKey = newInvNodeConnKey("sourceTP");
        var sourceConnID = newNodeConnID(sourceNodeKey, sourceNodeConnKey);

        org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.NodeKey
                destNodeKey = newInvNodeKey("destNode");
        org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.node.NodeConnectorKey
                destNodeConnKey = newInvNodeConnKey("destTP");
        var destConnID = newNodeConnID(destNodeKey, destNodeConnKey);

        ReadWriteTransaction mockTx = mock(ReadWriteTransaction.class);
        final CountDownLatch submitLatch = setupStubbedSubmit(mockTx);
        doReturn(mockTx).when(mockTxChain).newReadWriteTransaction();
        doReturn(FluentFutures.immediateFluentFuture(Optional.empty())).when(mockTx)
                .read(LogicalDatastoreType.OPERATIONAL, topologyIID.toBuilder()
                    .child(Link.class, new LinkKey(new LinkId(sourceNodeConnKey.getId())))
                    .build());

        exporter.onLinkRemoved(new LinkRemovedBuilder().setSource(
                new NodeConnectorRef(sourceConnID)).setDestination(
                    new NodeConnectorRef(destConnID)).build());

        waitForSubmit(submitLatch);

        verify(mockTx, never()).delete(LogicalDatastoreType.OPERATIONAL, topologyIID.toBuilder()
            .child(Link.class, new LinkKey(new LinkId(sourceNodeConnKey.getId())))
            .build());
    }
}
