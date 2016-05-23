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
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
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
 * Test for {@link SyncReactorFutureZipRetryOperationalDecorator}.
 */
@RunWith(MockitoJUnitRunner.class)
public class SyncReactorFutureZipRetryOperationalDecoratorTest {

    private static final Logger LOG = LoggerFactory.getLogger(SyncReactorFutureZipRetryOperationalDecoratorTest.class);
    private static final NodeId NODE_ID = new NodeId("testNode");
    private SyncReactorFutureZipRetryOperationalDecorator reactor;
    private ListeningExecutorService syncThreadPool;

    private InstanceIdentifier<FlowCapableNode> fcNodePath;
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
        reactor = new SyncReactorFutureZipRetryOperationalDecorator(delegate, syncThreadPool, retryRegistry);
        fcNodePath = InstanceIdentifier.create(Nodes.class).child(Node.class, new NodeKey(NODE_ID))
                .augmentation(FlowCapableNode.class);
    }

    @Test
    public void testSyncupOperationalRetryOn() throws InterruptedException {
        final FlowCapableNode configActual = Mockito.mock(FlowCapableNode.class);
        final FlowCapableNode freshOperational = Mockito.mock(FlowCapableNode.class);
        Mockito.when(retryRegistry.isRegistered(NODE_ID)).thenReturn(Boolean.TRUE);
        Mockito.when(delegate.syncup(Matchers.<InstanceIdentifier<FlowCapableNode>>any(), Matchers.<FlowCapableNode>any(),
                Matchers.<FlowCapableNode>any())).thenReturn(Futures.immediateFuture(Boolean.TRUE));
        reactor.getCompressionQueue().put(fcNodePath, Pair.of(configActual, configActual));

        reactor.syncup(fcNodePath, configActual, freshOperational);
        syncThreadPool.shutdown();
        boolean terminated = syncThreadPool.awaitTermination(1, TimeUnit.SECONDS);
        if (!terminated) {
            LOG.info("thread pool not terminated.");
            syncThreadPool.shutdownNow();
        }

        Mockito.verify(delegate).syncup(fcNodePath, configActual, freshOperational);
        Mockito.verifyNoMoreInteractions(delegate);
    }

    @After
    public void tearDown() {
        syncThreadPool.shutdown();
    }
}