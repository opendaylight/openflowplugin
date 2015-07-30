/**
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.rpc;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.opendaylight.controller.sal.binding.api.BindingAwareBroker;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceContext;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceState;
import org.opendaylight.openflowplugin.api.openflow.device.RequestContext;
import org.opendaylight.openflowplugin.api.openflow.rpc.RpcContext;
import org.opendaylight.openflowplugin.api.openflow.statistics.ofpspecific.MessageSpy;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.SalFlowService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeContext;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.Nodes;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.NodeKey;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.binding.KeyedInstanceIdentifier;

/**
 * @author joe
 */
@RunWith(MockitoJUnitRunner.class)
public class RpcContextImplTest {

    @Mock
    private BindingAwareBroker.ProviderContext mockedRpcProviderRegistry;
    @Mock
    private DeviceState deviceState;
    @Mock
    private DeviceContext deviceContext;
    @Mock
    private MessageSpy messageSpy;
    @Mock
    private SalFlowService salFlowServiceInstance;
    @Mock
    BindingAwareBroker.RoutedRpcRegistration<SalFlowService> routedRpcRegistration;

    private KeyedInstanceIdentifier<Node, NodeKey> nodeInstanceIdentifier;

    @Before
    public void setup() {
        NodeId nodeId = new NodeId("openflow:1");
        nodeInstanceIdentifier = InstanceIdentifier.create(Nodes.class).child(Node.class, new NodeKey(nodeId));

        when(deviceState.getNodeInstanceIdentifier()).thenReturn(nodeInstanceIdentifier);
        when(deviceContext.getDeviceState()).thenReturn(deviceState);
        when(mockedRpcProviderRegistry.addRoutedRpcImplementation(
                Matchers.<Class<SalFlowService>>any(), Matchers.any(SalFlowService.class)))
                .thenReturn(routedRpcRegistration);
    }

    @Test
    public void testCreateRequestContext() throws Exception {
        try (final RpcContext rpcContext = new RpcContextImpl(messageSpy, mockedRpcProviderRegistry, deviceContext, 1)) {
            RequestContext<?> requestContext1 = rpcContext.createRequestContext();
            assertNotNull(requestContext1);

            // quota exceeded
            RequestContext<?> requestContext2 = rpcContext.createRequestContext();
            assertNull(requestContext2);

            requestContext1.close();
            RequestContext<?> requestContext3 = rpcContext.createRequestContext();
            assertNotNull(requestContext3);
        }
    }

    @Test
    public void testRegisterRpcServiceImplementation() throws Exception {
        try (final RpcContext rpcContext = new RpcContextImpl(messageSpy, mockedRpcProviderRegistry, deviceContext, 10)) {
            rpcContext.registerRpcServiceImplementation(SalFlowService.class, salFlowServiceInstance);
            Mockito.verify(routedRpcRegistration).registerPath(NodeContext.class, nodeInstanceIdentifier);
        }
    }

    @Test
    public void testClose() throws Exception {
        try (final RpcContext rpcContext = new RpcContextImpl(messageSpy, mockedRpcProviderRegistry, deviceContext, 10)) {
            rpcContext.registerRpcServiceImplementation(SalFlowService.class, salFlowServiceInstance);
            rpcContext.close();
            Mockito.verify(routedRpcRegistration).unregisterPath(NodeContext.class, nodeInstanceIdentifier);
            Mockito.verify(routedRpcRegistration).close();
        }
    }

    @Test
    public void testOnDeviceContextClosed() throws Exception {
        try (final RpcContext rpcContext = new RpcContextImpl(messageSpy, mockedRpcProviderRegistry, deviceContext, 10)) {
            rpcContext.registerRpcServiceImplementation(SalFlowService.class, salFlowServiceInstance);
            rpcContext.onDeviceContextClosed(deviceContext);
            Mockito.verify(routedRpcRegistration).unregisterPath(NodeContext.class, nodeInstanceIdentifier);
            Mockito.verify(routedRpcRegistration).close();
        }
    }
}
