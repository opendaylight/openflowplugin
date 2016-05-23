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
import org.opendaylight.openflowplugin.applications.frsync.util.RetryRegistry;
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
    private SyncReactorImpl delegate;
    @Mock
    private RetryRegistry retryRegistry;
    @Mock
    private FlowCapableNode fcConfigNode;
    @Mock
    private FlowCapableNode fcOperationalNode;

    @Before
    public void setUp() {
        reactor = new SyncReactorRetryDecorator(delegate, retryRegistry);
        InstanceIdentifier<Node> nodePath = InstanceIdentifier.create(Nodes.class).child(Node.class, new NodeKey(NODE_ID));
        fcNodePath = nodePath.augmentation(FlowCapableNode.class);

        final Node operationalNode = Mockito.mock(Node.class);
        Mockito.when(operationalNode.getId()).thenReturn(NODE_ID);
        Mockito.when(operationalNode.getAugmentation(FlowCapableNode.class)).thenReturn(fcOperationalNode);
    }

    @Test
    public void testSyncupSuccess() throws Exception {
        Mockito.when(delegate.syncup(Matchers.<InstanceIdentifier<FlowCapableNode>>any(),Matchers.<FlowCapableNode>any(),
                Matchers.<FlowCapableNode>any())).thenReturn(Futures.immediateFuture(Boolean.TRUE));
        reactor.syncup(fcNodePath, fcConfigNode, fcOperationalNode);
        Mockito.verify(delegate).syncup(fcNodePath, fcConfigNode, fcOperationalNode);
    }

    @Test
    public void testSyncupRetry() throws Exception {
        Mockito.when(delegate.syncup(Matchers.<InstanceIdentifier<FlowCapableNode>>any(),Matchers.<FlowCapableNode>any(),
                Matchers.<FlowCapableNode>any())).thenReturn(Futures.immediateFuture(Boolean.FALSE));
        reactor.syncup(fcNodePath, fcConfigNode, fcOperationalNode);
        Mockito.verify(delegate).syncup(fcNodePath, fcConfigNode, fcOperationalNode);
        Mockito.verify(retryRegistry).register(NODE_ID);
    }

}