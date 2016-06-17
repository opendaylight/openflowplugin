/**
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.rpc;


import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceContext;
import org.opendaylight.openflowplugin.api.openflow.rpc.listener.ItemLifecycleListener;
import org.opendaylight.openflowplugin.impl.rpc.listener.ItemLifecycleListenerImpl;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.Nodes;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.NodeKey;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.binding.KeyedInstanceIdentifier;

@RunWith(MockitoJUnitRunner.class)
public class ItemLifecycleListenerImplTest {

    @Mock
    private DeviceContext deviceContext;

    @Mock
    private Node node;

    private KeyedInstanceIdentifier<Node, NodeKey> nodeInstanceIdentifier;
    private ItemLifecycleListener itemLifecycleListener;


    @Before
    public void setUp() {
        final NodeId nodeId = new NodeId("openflow:1");
        nodeInstanceIdentifier = InstanceIdentifier.create(Nodes.class).child(Node.class, new NodeKey(nodeId));
        itemLifecycleListener = new ItemLifecycleListenerImpl(deviceContext);
    }

    @After
    public void tearDown() {
        verifyNoMoreInteractions(deviceContext);
    }

    @Test
    public void testOnAdded() throws Exception {
        itemLifecycleListener.onAdded(nodeInstanceIdentifier, node);
        verify(deviceContext).writeToTransaction(eq(LogicalDatastoreType.OPERATIONAL), eq(nodeInstanceIdentifier), eq(node));
        verify(deviceContext).submitTransaction();
    }

    @Test
    public void testOnRemoved() throws Exception {
        itemLifecycleListener.onRemoved(nodeInstanceIdentifier);
        verify(deviceContext).addDeleteToTxChain(eq(LogicalDatastoreType.OPERATIONAL), eq(nodeInstanceIdentifier));
        verify(deviceContext).submitTransaction();
    }
}
