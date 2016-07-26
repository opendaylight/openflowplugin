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
import com.google.common.util.concurrent.MoreExecutors;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
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
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.openflowplugin.applications.frsync.util.SyncupEntry;
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
    private ListeningExecutorService syncThreadPool;

    @Mock
    private SyncReactorGuardDecorator delegate;
    @Mock
    private SyncupEntry syncupEntry;

    @Before
    public void setUp() {
        final ExecutorService executorService = Executors.newSingleThreadExecutor(new ThreadFactoryBuilder()
                .setDaemon(false)
                .setNameFormat("frsync-test%d")
                .setUncaughtExceptionHandler((thread, e) -> LOG.error("Uncaught exception {}", thread, e))
                .build());
        syncThreadPool = MoreExecutors.listeningDecorator(executorService);
        reactor = new SyncReactorFutureZipDecorator(delegate, syncThreadPool);
        fcNodePath = InstanceIdentifier.create(Nodes.class).child(Node.class, new NodeKey(NODE_ID))
                .augmentation(FlowCapableNode.class);
    }

    @Test
    public void testSyncupWithOptimizedConfigDeltaCompression() throws Exception {
        final FlowCapableNode dataBefore = Mockito.mock(FlowCapableNode.class);
        final FlowCapableNode dataAfter = Mockito.mock(FlowCapableNode.class);
        final FlowCapableNode dataAfter2 = Mockito.mock(FlowCapableNode.class);
        final CountDownLatch latchForFirst = new CountDownLatch(1);
        final CountDownLatch latchForNext = new CountDownLatch(1);
        final LogicalDatastoreType dsType = LogicalDatastoreType.CONFIGURATION;

        Mockito.when(delegate.syncup(Matchers.<InstanceIdentifier<FlowCapableNode>>any(), Matchers.<SyncupEntry>any()))
                .thenAnswer(new Answer<ListenableFuture<Boolean>>() {
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
//        allResults.add(reactor.syncup(fcNodePath, dataBefore, null, dsType));
        allResults.add(reactor.syncup(fcNodePath, syncupEntry));
        latchForNext.await();

//        allResults.add(reactor.syncup(fcNodePath, dataAfter, dataBefore, dsType));
        allResults.add(reactor.syncup(fcNodePath, syncupEntry));
//        allResults.add(reactor.syncup(fcNodePath, null, dataAfter, dsType));
        allResults.add(reactor.syncup(fcNodePath, syncupEntry));
//        allResults.add(reactor.syncup(fcNodePath, dataAfter2, null, dsType));
        allResults.add(reactor.syncup(fcNodePath, syncupEntry));
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
//        inOrder.verify(delegate).syncup(fcNodePath, dataBefore, null, dsType);
        inOrder.verify(delegate).syncup(fcNodePath, syncupEntry);
//        inOrder.verify(delegate).syncup(fcNodePath, dataAfter2, dataBefore, dsType);
        inOrder.verify(delegate).syncup(fcNodePath, syncupEntry);
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void testSyncupConfigEmptyQueue() throws Exception {
        final FlowCapableNode dataBefore = Mockito.mock(FlowCapableNode.class);
        final FlowCapableNode dataAfter = Mockito.mock(FlowCapableNode.class);
        final CountDownLatch latchForNext = new CountDownLatch(1);
        final LogicalDatastoreType dsType = LogicalDatastoreType.CONFIGURATION;

        Mockito.when(delegate.syncup(Matchers.<InstanceIdentifier<FlowCapableNode>>any(), Matchers.<SyncupEntry>any()))
                .thenAnswer(new Answer<ListenableFuture<Boolean>>() {
            @Override
            public ListenableFuture<Boolean> answer(final InvocationOnMock invocationOnMock) throws Throwable {
                LOG.info("unlocking next config");
                latchForNext.countDown();
                return Futures.immediateFuture(Boolean.TRUE);
            }
            }).thenReturn(Futures.immediateFuture(Boolean.TRUE));

//        reactor.syncup(fcNodePath, dataBefore, null, dsType);
        reactor.syncup(fcNodePath, syncupEntry);
        latchForNext.await();
//        reactor.syncup(fcNodePath, dataAfter, dataBefore, dsType);
        reactor.syncup(fcNodePath, syncupEntry);

        boolean terminated = syncThreadPool.awaitTermination(1, TimeUnit.SECONDS);
        if (!terminated) {
            LOG.info("thread pool not terminated.");
            syncThreadPool.shutdownNow();
        }
        final InOrder inOrder = Mockito.inOrder(delegate);
//        inOrder.verify(delegate).syncup(fcNodePath, dataBefore, null, dsType);
        inOrder.verify(delegate).syncup(fcNodePath, syncupEntry);
//        inOrder.verify(delegate).syncup(fcNodePath, dataAfter, dataBefore, dsType);
        inOrder.verify(delegate).syncup(fcNodePath, syncupEntry);
        inOrder.verifyNoMoreInteractions();

    }

    @Test
    public void testSyncupRewriteZipEntryWithOperationalDelta() throws Exception {
        final FlowCapableNode configBefore = Mockito.mock(FlowCapableNode.class);
        final FlowCapableNode configAfter = Mockito.mock(FlowCapableNode.class);
        final FlowCapableNode configActual = Mockito.mock(FlowCapableNode.class);
        final FlowCapableNode freshOperational = Mockito.mock(FlowCapableNode.class);
        final CountDownLatch latchForFirst = new CountDownLatch(1);
        final CountDownLatch latchForNext = new CountDownLatch(1);

        Mockito.when(delegate.syncup(Matchers.<InstanceIdentifier<FlowCapableNode>>any(), Matchers.<SyncupEntry>any()))
                .thenAnswer(new Answer<ListenableFuture<Boolean>>() {
            @Override
            public ListenableFuture<Boolean> answer(final InvocationOnMock invocationOnMock) throws Throwable {
                LOG.info("unlocking for fresh operational");
                latchForNext.countDown();
                latchForFirst.await();
                LOG.info("unlocking first delegate");
                return Futures.immediateFuture(Boolean.TRUE);
            }
        }).thenReturn(Futures.immediateFuture(Boolean.TRUE));

//        reactor.syncup(fcNodePath, configAfter, configBefore, LogicalDatastoreType.CONFIGURATION);
        reactor.syncup(fcNodePath, syncupEntry);
        latchForNext.await();

//        reactor.syncup(fcNodePath, configActual, freshOperational, LogicalDatastoreType.OPERATIONAL);
        reactor.syncup(fcNodePath, syncupEntry);
        latchForFirst.countDown();

        syncThreadPool.shutdown();
        boolean terminated = syncThreadPool.awaitTermination(1, TimeUnit.SECONDS);
        if (!terminated) {
            LOG.info("thread pool not terminated.");
            syncThreadPool.shutdownNow();
        }
//        Mockito.verify(delegate, Mockito.times(1)).syncup(fcNodePath, configActual, freshOperational, LogicalDatastoreType.OPERATIONAL);
        Mockito.verify(delegate, Mockito.times(1)).syncup(fcNodePath, syncupEntry);
    }

    @After
    public void tearDown() {
        syncThreadPool.shutdownNow();
    }
}