/**
 * Copyright (c) 2016 Cisco Systems, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.applications.frsync.impl;

import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import java.util.concurrent.TimeUnit;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.openflowplugin.applications.frsync.util.RetryRegistry;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNode;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.Nodes;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.NodeKey;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Test for {@link SyncReactorFutureZipDecorator}.
 */
@RunWith(MockitoJUnitRunner.class)
public class SyncReactorFutureZipRetryDecoratorTest {

    private static final Logger LOG = LoggerFactory.getLogger(SyncReactorFutureZipRetryDecoratorTest.class);
    private static final NodeId NODE_ID = new NodeId("testNode");
    private SyncReactorFutureZipRetryDecorator reactor;
    private InstanceIdentifier<FlowCapableNode> fcNodePath;
    private ListeningExecutorService syncThreadPool;

    @Mock
    private SyncReactorGuardDecorator delegate;
    @Mock
    private RetryRegistry retryRegistry;

    @Before
    public void setUp() {
        syncThreadPool = FrmExecutors.instance()
                .newFixedThreadPool(1, new ThreadFactoryBuilder()
                        .setNameFormat(SyncReactorFutureDecorator.FRM_RPC_CLIENT_PREFIX + "%d")
                        .setDaemon(false)
                        .build());
        reactor = new SyncReactorFutureZipRetryDecorator(delegate, syncThreadPool, retryRegistry);
        fcNodePath = InstanceIdentifier.create(Nodes.class).child(Node.class, new NodeKey(NODE_ID))
                .augmentation(FlowCapableNode.class);
    }

    @Test
    public void testSyncupConfigRetryOn() throws InterruptedException {
        final FlowCapableNode configActual = Mockito.mock(FlowCapableNode.class);
        final FlowCapableNode configNew1 = Mockito.mock(FlowCapableNode.class);
        final FlowCapableNode configNew2 = Mockito.mock(FlowCapableNode.class);
        final LogicalDatastoreType dsType = LogicalDatastoreType.CONFIGURATION;
        Mockito.when(retryRegistry.isRegistered(NODE_ID)).thenReturn(true);

        reactor.syncup(fcNodePath, configNew1, configActual, dsType);
        reactor.syncup(fcNodePath, configNew2, configNew1, dsType);

        Assert.assertNull(reactor.getCompressionQueue().get(fcNodePath));
        Mockito.verifyZeroInteractions(delegate);
    }

    @Test
    public void testSyncupOperationalRetryOn() throws InterruptedException {
        final FlowCapableNode dataOperational = Mockito.mock(FlowCapableNode.class);
        final FlowCapableNode dataConfig = Mockito.mock(FlowCapableNode.class);
        final LogicalDatastoreType dsType = LogicalDatastoreType.OPERATIONAL;
        Mockito.when(retryRegistry.isRegistered(NODE_ID)).thenReturn(true);
        Mockito.when(delegate.syncup(Matchers.<InstanceIdentifier<FlowCapableNode>>any(), Matchers.<FlowCapableNode>any(),
                Matchers.<FlowCapableNode>any(), Matchers.<LogicalDatastoreType>any())).thenReturn(Futures.immediateFuture(Boolean.TRUE));

        reactor.syncup(fcNodePath, dataConfig, dataOperational, dsType);

        syncThreadPool.shutdown();
        boolean terminated = syncThreadPool.awaitTermination(1, TimeUnit.SECONDS);
        if (!terminated) {
            LOG.info("thread pool not terminated.");
            syncThreadPool.shutdownNow();
        }

        Assert.assertNull(reactor.getCompressionQueue().get(fcNodePath));
        Mockito.verify(delegate).syncup(fcNodePath, dataConfig, dataOperational, dsType);
        Mockito.verifyNoMoreInteractions(delegate);
    }

    @Test
    public void testSyncupOperationalZipRetryOn() throws InterruptedException {
        final FlowCapableNode dataOperational1 = Mockito.mock(FlowCapableNode.class);
        final FlowCapableNode dataOperational2 = Mockito.mock(FlowCapableNode.class);
        final FlowCapableNode dataConfig1 = Mockito.mock(FlowCapableNode.class);
        final FlowCapableNode dataConfig2 = Mockito.mock(FlowCapableNode.class);
        final LogicalDatastoreType dsType = LogicalDatastoreType.OPERATIONAL;
        Mockito.when(retryRegistry.isRegistered(NODE_ID)).thenReturn(true);
        reactor.getCompressionQueue().put(fcNodePath, Pair.of(dataConfig1, dataOperational1));

        reactor.syncup(fcNodePath, dataConfig2, dataOperational2, dsType);

        Assert.assertNotNull(reactor.getCompressionQueue().get(fcNodePath));
        Mockito.verifyZeroInteractions(delegate);
    }

    @After
    public void tearDown() {
        syncThreadPool.shutdownNow();
    }
}