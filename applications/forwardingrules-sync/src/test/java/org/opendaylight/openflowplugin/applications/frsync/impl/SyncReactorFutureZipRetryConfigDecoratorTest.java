/**
 * Copyright (c) 2016 Cisco Systems, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.applications.frsync.impl;

import com.google.common.util.concurrent.ListeningExecutorService;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
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

/**
 * Test for {@link SyncReactorFutureZipDecorator}.
 */
@RunWith(MockitoJUnitRunner.class)
public class SyncReactorFutureZipRetryConfigDecoratorTest {

    private static final NodeId NODE_ID = new NodeId("testNode");
    private SyncReactorFutureZipRetryConfigDecorator reactor;
    private InstanceIdentifier<FlowCapableNode> fcNodePath;

    @Mock
    private SyncReactorGuardDecorator delegate;
    @Mock
    private RetryRegistry retryRegistry;

    @Before
    public void setUp() {
        final ListeningExecutorService syncThreadPool = Mockito.mock(ListeningExecutorService.class);
        reactor = new SyncReactorFutureZipRetryConfigDecorator(delegate, syncThreadPool, retryRegistry);
        fcNodePath = InstanceIdentifier.create(Nodes.class).child(Node.class, new NodeKey(NODE_ID))
                .augmentation(FlowCapableNode.class);
    }

    @Test
    public void testSyncupConfigRetryOn() throws InterruptedException {
        final FlowCapableNode configActual = Mockito.mock(FlowCapableNode.class);
        final FlowCapableNode configNew1 = Mockito.mock(FlowCapableNode.class);
        final FlowCapableNode configNew2 = Mockito.mock(FlowCapableNode.class);
        final FlowCapableNode configNew3 = Mockito.mock(FlowCapableNode.class);

        Mockito.when(retryRegistry.isRegistered(NODE_ID)).thenReturn(Boolean.TRUE);
        reactor.getCompressionQueue().put(fcNodePath, Pair.of(configActual, configActual));

        reactor.syncup(fcNodePath, configNew1, configActual);
        reactor.syncup(fcNodePath, configNew2, configNew1);
        reactor.syncup(fcNodePath, configNew3, configNew2);

        Assert.assertEquals(configNew3, reactor.getCompressionQueue().get(fcNodePath).getLeft());
        Assert.assertEquals(configActual, reactor.getCompressionQueue().get(fcNodePath).getRight());
        Mockito.verifyZeroInteractions(delegate);
    }

}