/**
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.rpc;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.opendaylight.controller.md.sal.binding.api.NotificationPublishService;
import org.opendaylight.controller.sal.binding.api.BindingAwareBroker;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceContext;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceInfo;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceState;
import org.opendaylight.openflowplugin.api.openflow.device.RequestContext;
import org.opendaylight.openflowplugin.api.openflow.device.XidSequencer;
import org.opendaylight.openflowplugin.api.openflow.rpc.RpcContext;
import org.opendaylight.openflowplugin.api.openflow.statistics.ofpspecific.MessageSpy;
import org.opendaylight.openflowplugin.extension.api.core.extension.ExtensionConverterProvider;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.ConvertorExecutor;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeContext;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.Nodes;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.NodeKey;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.binding.KeyedInstanceIdentifier;
import org.opendaylight.yangtools.yang.binding.RpcService;

@RunWith(MockitoJUnitRunner.class)
public class RpcContextImplTest {

    private static final int MAX_REQUESTS = 5;
    private RpcContextImpl rpcContext;


    @Mock
    private BindingAwareBroker.ProviderContext rpcProviderRegistry;
    @Mock
    private DeviceState deviceState;
    @Mock
    private XidSequencer xidSequencer;
    @Mock
    private MessageSpy messageSpy;
    @Mock
    private DeviceContext deviceContext;
    @Mock
    private BindingAwareBroker.RoutedRpcRegistration<TestRpcService> routedRpcReg;

    private Class<TestRpcService> serviceClass;
    @Mock
    private NotificationPublishService notificationPublishService;
    @Mock
    private TestRpcService serviceInstance;
    @Mock
    private DeviceInfo deviceInfo;
    @Mock
    private ExtensionConverterProvider extensionConverterProvider;
    @Mock
    private ConvertorExecutor convertorExecutor;

    private KeyedInstanceIdentifier<Node, NodeKey> nodeInstanceIdentifier;

    @Before
    public void setup() {
        final NodeId nodeId = new NodeId("openflow:1");
        nodeInstanceIdentifier = InstanceIdentifier.create(Nodes.class).child(Node.class, new NodeKey(nodeId));

        when(deviceContext.getDeviceState()).thenReturn(deviceState);
        when(deviceInfo.getNodeInstanceIdentifier()).thenReturn(nodeInstanceIdentifier);
        when(deviceContext.getMessageSpy()).thenReturn(messageSpy);
        when(deviceContext.getDeviceInfo()).thenReturn(deviceInfo);
        when(deviceInfo.getNodeInstanceIdentifier()).thenReturn(nodeInstanceIdentifier);

        rpcContext = new RpcContextImpl(
                rpcProviderRegistry,
                MAX_REQUESTS,
                deviceContext,
                extensionConverterProvider,
                convertorExecutor,
                notificationPublishService, true);

        when(rpcProviderRegistry.addRoutedRpcImplementation(TestRpcService.class, serviceInstance)).thenReturn(routedRpcReg);

    }

    @Test
    public void testStoreOrFail() throws Exception {
        try (final RpcContext rpcContext = new RpcContextImpl(
                rpcProviderRegistry,
                100,
                deviceContext,
                extensionConverterProvider,
                convertorExecutor,
                notificationPublishService, true)){
            final RequestContext<?> requestContext = rpcContext.createRequestContext();
            assertNotNull(requestContext);
        }
    }

    @Test
    public void testStoreOrFailThatFails() throws Exception {
        try (final RpcContext rpcContext = new RpcContextImpl(
                rpcProviderRegistry,
                0,
                deviceContext,
                extensionConverterProvider,
                convertorExecutor,
                notificationPublishService, true)){
            final RequestContext<?> requestContext = rpcContext.createRequestContext();
            assertNull(requestContext);
        }
    }

    @Test
    public void testStoreAndCloseOrFail() throws Exception {
        try (final RpcContext rpcContext = new RpcContextImpl(
                rpcProviderRegistry,
                100,
                deviceContext,
                extensionConverterProvider,
                convertorExecutor,
                notificationPublishService, true)){
            final RequestContext<?> requestContext = rpcContext.createRequestContext();
            assertNotNull(requestContext);
            requestContext.close();
            verify(messageSpy).spyMessage(RpcContextImpl.class, MessageSpy.STATISTIC_GROUP.REQUEST_STACK_FREED);
        }
    }

    public void testRegisterRpcServiceImplementation() {
        rpcContext.registerRpcServiceImplementation(TestRpcService.class, serviceInstance);
        verify(rpcProviderRegistry, Mockito.times(1)).addRoutedRpcImplementation(TestRpcService.class,serviceInstance);
        verify(routedRpcReg,Mockito.times(1)).registerPath(NodeContext.class,nodeInstanceIdentifier);
        assertEquals(rpcContext.isEmptyRpcRegistrations(), false);
    }


    @Test
    public void testLookupRpcService() {
        when(routedRpcReg.getInstance()).thenReturn(serviceInstance);
        rpcContext.registerRpcServiceImplementation(TestRpcService.class, serviceInstance);
        TestRpcService temp = rpcContext.lookupRpcService(TestRpcService.class);
        assertEquals(serviceInstance,temp);
    }

    @Test
    public void testClose() {
        serviceClass = TestRpcService.class;
        when(routedRpcReg.getServiceType()).thenReturn(serviceClass);
        rpcContext.registerRpcServiceImplementation(TestRpcService.class, serviceInstance);
        rpcContext.close();
        assertEquals(rpcContext.isEmptyRpcRegistrations(), true);
    }

    /**
     * When deviceContext.reserveXidForDeviceMessage returns null, null should be returned
     * @throws InterruptedException
     */
    @Test
    public void testCreateRequestContext1() throws InterruptedException {
        when(deviceInfo.reserveXidForDeviceMessage()).thenReturn(null);
        assertEquals(rpcContext.createRequestContext(),null);
    }

    /**
     * When deviceContext.reserveXidForDeviceMessage returns value, AbstractRequestContext should be returned
     * @throws InterruptedException
     */

    @Test
    public void testCreateRequestContext2() throws InterruptedException {
        RequestContext temp = rpcContext.createRequestContext();
        temp.close();
        verify(messageSpy).spyMessage(RpcContextImpl.class,MessageSpy.STATISTIC_GROUP.REQUEST_STACK_FREED);
    }

    @Test
    public void testUnregisterRpcServiceImpl() {
        rpcContext.registerRpcServiceImplementation(TestRpcService.class, serviceInstance);
        assertEquals(rpcContext.isEmptyRpcRegistrations(), false);
        rpcContext.unregisterRpcServiceImplementation(TestRpcService.class);
        assertEquals(rpcContext.isEmptyRpcRegistrations(), true);
    }

    //Stub for RpcService class
    public class TestRpcService implements RpcService {}
}
