/**
 * Copyright (c) 2016 Cisco Systems, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.applications.frsync.impl;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.opendaylight.openflowplugin.applications.frsync.SyncReactor;
import org.opendaylight.openflowplugin.applications.frsync.impl.clustering.DeviceMastershipManager;
import org.opendaylight.openflowplugin.applications.frsync.util.SyncupEntry;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNode;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.Nodes;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.NodeKey;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

/**
 * Test for {@link SyncReactorClusterDecorator}.
 */
@RunWith(MockitoJUnitRunner.class)
public class SyncReactorClusterDecoratorTest {

    private static final NodeId NODE_ID = new NodeId("test-node");
    private SyncReactorClusterDecorator reactor;
    private InstanceIdentifier<FlowCapableNode> fcNodePath;

    @Mock
    private SyncReactor delegate;
    @Mock
    private DeviceMastershipManager deviceMastershipManager;
    @Mock
    private SyncupEntry syncupEntry;

    @Before
    public void setUp() {
        reactor = new SyncReactorClusterDecorator(delegate, deviceMastershipManager);

        InstanceIdentifier<Node> nodePath = InstanceIdentifier.create(Nodes.class).child(Node.class, new NodeKey(NODE_ID));
        fcNodePath = nodePath.augmentation(FlowCapableNode.class);
    }

    @Test
    public void testSyncupMaster() {
        Mockito.when(deviceMastershipManager.isDeviceMastered(NODE_ID)).thenReturn(true);

        reactor.syncup(fcNodePath, syncupEntry);

        Mockito.verify(delegate).syncup(fcNodePath, syncupEntry);
        Mockito.verifyNoMoreInteractions(delegate);
    }

    @Test
    public void testSyncupSlave() {
        Mockito.when(deviceMastershipManager.isDeviceMastered(NODE_ID)).thenReturn(false);

        reactor.syncup(fcNodePath, syncupEntry);

        Mockito.verifyZeroInteractions(delegate);
    }

}