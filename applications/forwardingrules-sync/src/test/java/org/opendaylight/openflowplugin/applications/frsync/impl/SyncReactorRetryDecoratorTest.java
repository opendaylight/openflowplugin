/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.applications.frsync.impl;

import com.google.common.util.concurrent.Futures;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.opendaylight.openflowplugin.applications.frsync.SyncReactor;
import org.opendaylight.openflowplugin.applications.frsync.util.ReconciliationRegistry;
import org.opendaylight.openflowplugin.applications.frsync.util.SyncupEntry;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNode;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.Nodes;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.NodeKey;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

/**
 * Test for {@link SyncReactorRetryDecorator}
 */
@RunWith(MockitoJUnitRunner.class)
public class SyncReactorRetryDecoratorTest {

    private static final NodeId NODE_ID = new NodeId("test-node");
    private SyncReactorRetryDecorator reactor;
    private InstanceIdentifier<FlowCapableNode> fcNodePath;

    @Mock
    private SyncReactor delegate;
    @Mock
    private ReconciliationRegistry reconciliationRegistry;
    @Mock
    private SyncupEntry syncupEntry;

    @Before
    public void setUp() {
        reactor = new SyncReactorRetryDecorator(delegate, reconciliationRegistry);
        InstanceIdentifier<Node> nodePath = InstanceIdentifier.create(Nodes.class).child(Node.class, new NodeKey(NODE_ID));
        fcNodePath = nodePath.augmentation(FlowCapableNode.class);
    }

    @Test
    public void testSyncupSuccess() {
        Mockito.when(delegate.syncup(Matchers.<InstanceIdentifier<FlowCapableNode>>any(), Matchers.<SyncupEntry>any()))
                .thenReturn(Futures.immediateFuture(Boolean.TRUE));

        reactor.syncup(fcNodePath, syncupEntry);

        Mockito.verify(delegate).syncup(fcNodePath, syncupEntry);
        Mockito.verifyNoMoreInteractions(delegate);
        Mockito.verify(reconciliationRegistry).unregisterIfRegistered(NODE_ID);
    }

    @Test
    public void testSyncupFail() {
        Mockito.when(delegate.syncup(Matchers.<InstanceIdentifier<FlowCapableNode>>any(), Matchers.<SyncupEntry>any()))
                .thenReturn(Futures.immediateFuture(Boolean.FALSE));

        reactor.syncup(fcNodePath, syncupEntry);

        Mockito.verify(delegate).syncup(fcNodePath, syncupEntry);
        Mockito.verifyNoMoreInteractions(delegate);
        Mockito.verify(reconciliationRegistry).register(NODE_ID);
    }

    @Test
    public void testSyncupConfigIgnoreInRetry() {
        Mockito.when(reconciliationRegistry.isRegistered(NODE_ID)).thenReturn(true);
        Mockito.when(syncupEntry.isOptimizedConfigDelta()).thenReturn(true);

        reactor.syncup(fcNodePath, syncupEntry);

        Mockito.verifyZeroInteractions(delegate);
    }

}