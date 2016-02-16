/**
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.rpc;

import static org.mockito.Mockito.times;
import org.junit.Before;
import org.junit.Test;
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
import org.opendaylight.openflowplugin.api.openflow.device.DeviceState;
import org.opendaylight.openflowplugin.api.openflow.device.handlers.DeviceInitializationPhaseHandler;
import org.opendaylight.openflowplugin.api.openflow.registry.ItemLifeCycleRegistry;
import org.opendaylight.openflowplugin.api.openflow.statistics.ofpspecific.MessageSpy;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeContext;
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

    private static final int AWAITED_NUM_OF_CALL_ADD_ROUTED_RPC = 11;
    private static final int INIT_AWAITED_NUM_OF_CALL_ADD_ROUTED_RPC = 1;

    private RpcManagerImpl rpcManager;
    @Mock
    private ProviderContext rpcProviderRegistry;
    @Mock
    private DeviceContext deviceContext;
    @Mock
    private DeviceInitializationPhaseHandler deviceINitializationPhaseHandler;
    @Mock
    private ConnectionContext connectionContext;
    @Mock
    private BindingAwareBroker.RoutedRpcRegistration<RpcService> routedRpcRegistration;
    @Mock
    private DeviceState deviceState;
    @Mock
    private ItemLifeCycleRegistry itemLifeCycleRegistry;
    @Mock
    private MessageSpy mockMessageSpy;

    private KeyedInstanceIdentifier<Node, NodeKey> nodePath;

    @Before
    public void setUp() {
        nodePath = KeyedInstanceIdentifier.create(Nodes.class).child(Node.class, new NodeKey(new NodeId("openflow-junit:1")));
        rpcManager = new RpcManagerImpl(rpcProviderRegistry, 5);
        rpcManager.setDeviceInitializationPhaseHandler(deviceINitializationPhaseHandler);
        final FeaturesReply features = new GetFeaturesOutputBuilder()
                .setVersion(OFConstants.OFP_VERSION_1_3)
                .build();
        Mockito.when(connectionContext.getFeatures()).thenReturn(features);
        Mockito.when(deviceContext.getPrimaryConnectionContext()).thenReturn(connectionContext);
        Mockito.when(deviceContext.getDeviceState()).thenReturn(deviceState);
        Mockito.when(deviceContext.getDeviceState().getRole()).thenReturn(OfpRole.BECOMEMASTER);
        Mockito.when(deviceContext.getItemLifeCycleSourceRegistry()).thenReturn(itemLifeCycleRegistry);
        Mockito.when(deviceContext.getMessageSpy()).thenReturn(mockMessageSpy);
        Mockito.when(deviceState.getNodeInstanceIdentifier()).thenReturn(nodePath);
    }

    @Test
    public void testOnDeviceContextLevelUp() throws Exception {

        Mockito.when(rpcProviderRegistry.addRoutedRpcImplementation(
                Matchers.<Class<RpcService>>any(), Matchers.any(RpcService.class)))
                .thenReturn(routedRpcRegistration);

        rpcManager.onDeviceContextLevelUp(deviceContext);

        Mockito.verify(rpcProviderRegistry, times(INIT_AWAITED_NUM_OF_CALL_ADD_ROUTED_RPC)).addRoutedRpcImplementation(
                Matchers.<Class<RpcService>>any(), Matchers.any(RpcService.class));
        Mockito.verify(routedRpcRegistration, times(INIT_AWAITED_NUM_OF_CALL_ADD_ROUTED_RPC)).registerPath(
                NodeContext.class, nodePath);
        Mockito.verify(deviceINitializationPhaseHandler).onDeviceContextLevelUp(deviceContext);
    }

    // FIXME : add test for role change to MASTER and test call for all 11 time RPC registration
}
