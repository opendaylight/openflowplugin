/**
 * Copyright (c) 2016 Cisco Systems, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.applications.frsync.impl;

import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;
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
public class SyncReactorFutureZipDecoratorTest {

    private static final Logger LOG = LoggerFactory.getLogger(SyncReactorFutureZipDecoratorTest.class);
    private static final NodeId NODE_ID = new NodeId("testNode");
    private SyncReactorFutureZipDecorator reactor;
    private InstanceIdentifier<FlowCapableNode> fcNodePath;

    @Mock
    private SyncReactorGuardDecorator delegate;
    private ListeningExecutorService syncThreadPool;

    @Before
    public void setUp() {
        syncThreadPool = FrmExecutors.instance()
                .newFixedThreadPool(1, new ThreadFactoryBuilder()
                        .setNameFormat(SyncReactorFutureDecorator.FRM_RPC_CLIENT_PREFIX + "%d")
                        .setDaemon(false)
                        .build());

        reactor = new SyncReactorFutureZipDecorator(delegate, syncThreadPool);
        fcNodePath = InstanceIdentifier.create(Nodes.class).child(Node.class, new NodeKey(NODE_ID))
                .augmentation(FlowCapableNode.class);
    }

    @Test
    public void testSyncupCompressionWanted() throws Exception {
        final FlowCapableNode dataBefore = Mockito.mock(FlowCapableNode.class);
        final FlowCapableNode dataAfter = Mockito.mock(FlowCapableNode.class);
        final FlowCapableNode dataAfter2 = Mockito.mock(FlowCapableNode.class);
        final CountDownLatch latchForFirst = new CountDownLatch(1);
        final CountDownLatch latchForNext = new CountDownLatch(1);

        Mockito.when(delegate.syncup(Matchers.<InstanceIdentifier<FlowCapableNode>>any(), Matchers.<FlowCapableNode>any(),
                Matchers.<FlowCapableNode>any())).thenAnswer(new Answer<ListenableFuture<Boolean>>() {
                    @Override
                    public ListenableFuture<Boolean> answer(final InvocationOnMock invocationOnMock) throws Throwable {
                        LOG.info("unlocking next configs");
                        latchForNext.countDown();
                        latchForFirst.await();
                        LOG.info("unlocking first delegate");
                        return Futures.immediateFuture(Boolean.TRUE);
                    }
                }).thenReturn(Futures.immediateFuture(Boolean.TRUE));

        final List<ListenableFuture<Boolean>> allResults = new ArrayList<>();
        allResults.add(reactor.syncup(fcNodePath, dataBefore, null));
        latchForNext.await();

        allResults.add(reactor.syncup(fcNodePath, dataAfter, dataBefore));
        allResults.add(reactor.syncup(fcNodePath, null, dataAfter));
        allResults.add(reactor.syncup(fcNodePath, dataAfter2, null));
        latchForFirst.countDown();

        Futures.allAsList(allResults).get(1, TimeUnit.SECONDS);
        LOG.info("all configs done");

        syncThreadPool.shutdown();
        boolean terminated = syncThreadPool.awaitTermination(1, TimeUnit.SECONDS);
        if (!terminated) {
            LOG.info("thread pool not terminated.");
            syncThreadPool.shutdownNow();
        }
        final InOrder inOrder = Mockito.inOrder(delegate);
        inOrder.verify(delegate).syncup(fcNodePath, dataBefore, null);
        inOrder.verify(delegate).syncup(fcNodePath, dataAfter2, dataBefore);
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void testSyncupCompressionNotWanted() throws Exception {
        final FlowCapableNode dataBefore = Mockito.mock(FlowCapableNode.class);
        final FlowCapableNode dataAfter = Mockito.mock(FlowCapableNode.class);
        final CountDownLatch latchForNext = new CountDownLatch(1);

        Mockito.when(delegate.syncup(Matchers.<InstanceIdentifier<FlowCapableNode>>any(), Matchers.<FlowCapableNode>any(),
                Matchers.<FlowCapableNode>any())).thenAnswer(new Answer<ListenableFuture<Boolean>>() {
            @Override
            public ListenableFuture<Boolean> answer(final InvocationOnMock invocationOnMock) throws Throwable {
                LOG.info("unlocking next config");
                latchForNext.countDown();
                return Futures.immediateFuture(Boolean.TRUE);
            }
            }).thenReturn(Futures.immediateFuture(Boolean.TRUE));

        reactor.syncup(fcNodePath, dataBefore, null);
        latchForNext.await();
        reactor.syncup(fcNodePath, dataAfter, dataBefore);

        boolean terminated = syncThreadPool.awaitTermination(1, TimeUnit.SECONDS);
        if (!terminated) {
            LOG.info("thread pool not terminated.");
            syncThreadPool.shutdownNow();
        }
        final InOrder inOrder = Mockito.inOrder(delegate);
        inOrder.verify(delegate).syncup(fcNodePath, dataBefore, null);
        inOrder.verify(delegate).syncup(fcNodePath, dataAfter, dataBefore);
        inOrder.verifyNoMoreInteractions();

    }

    @After
    public void tearDown() {
        syncThreadPool.shutdownNow();
    }
}