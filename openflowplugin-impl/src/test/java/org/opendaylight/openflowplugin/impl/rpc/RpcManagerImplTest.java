/**
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.rpc;

import com.google.common.base.VerifyException;
import static org.mockito.Mockito.times;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.opendaylight.controller.sal.binding.api.BindingAwareBroker;
import org.opendaylight.controller.sal.binding.api.BindingAwareBroker.ProviderContext;
import org.opendaylight.openflowplugin.api.OFConstants;
import org.opendaylight.openflowplugin.api.openflow.connection.ConnectionContext;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceContext;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceManager;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceState;
import org.opendaylight.openflowplugin.api.openflow.device.handlers.DeviceInitializationPhaseHandler;
import org.opendaylight.openflowplugin.api.openflow.lifecycle.LifecycleConductor;
import org.opendaylight.openflowplugin.api.openflow.registry.ItemLifeCycleRegistry;
import org.opendaylight.openflowplugin.api.openflow.statistics.ofpspecific.MessageSpy;
import org.opendaylight.openflowplugin.impl.LifecycleConductorImpl;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.Nodes;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.NodeKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.FeaturesReply;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.GetFeaturesOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.role.service.rev150727.OfpRole;
import org.opendaylight.yangtools.yang.binding.KeyedInstanceIdentifier;
import org.opendaylight.yangtools.yang.binding.RpcService;

@RunWith(MockitoJUnitRunner.class)
public class RpcManagerImplTest {

    private static final int QUOTA_VALUE = 5;
    private static final int AWAITED_NUM_OF_CALL_ADD_ROUTED_RPC = 12;

    private RpcManagerImpl rpcManager;

    @Mock
    private ProviderContext rpcProviderRegistry;
    @Mock
    private DeviceContext deviceContext;
    @Mock
    private DeviceInitializationPhaseHandler deviceINitializationPhaseHandler;
    @Mock
    private DeviceState deviceState;
    @Mock
    private MessageSpy mockMsgSpy;
    @Mock
    private LifecycleConductor conductor;

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    private KeyedInstanceIdentifier<Node, NodeKey> nodePath;

    private NodeId nodeId = new NodeId("openflow-junit:1");

    @Before
    public void setUp() {
        final NodeKey nodeKey = new NodeKey(nodeId);
        rpcManager = new RpcManagerImpl(rpcProviderRegistry, QUOTA_VALUE, conductor);
        rpcManager.setDeviceInitializationPhaseHandler(deviceINitializationPhaseHandler);
        Mockito.when(deviceContext.getDeviceState()).thenReturn(deviceState);
        Mockito.when(deviceContext.getMessageSpy()).thenReturn(mockMsgSpy);
        Mockito.when(deviceState.getNodeId()).thenReturn(nodeKey.getId());
        Mockito.when(conductor.getDeviceContext(Mockito.<NodeId>any())).thenReturn(deviceContext);
    }

    @Test
    public void onDeviceContextLevelUp() throws Exception {
        rpcManager.onDeviceContextLevelUp(nodeId);
        Mockito.verify(conductor).getDeviceContext(Mockito.<NodeId>any());
    }

    @Test
    public void onDeviceContextLevelUpTwice() throws Exception {
        rpcManager.onDeviceContextLevelUp(nodeId);
        expectedException.expect(VerifyException.class);
        rpcManager.onDeviceContextLevelUp(nodeId);
    }
}
