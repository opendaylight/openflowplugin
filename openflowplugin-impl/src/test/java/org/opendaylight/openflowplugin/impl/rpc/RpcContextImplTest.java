/*
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collection;
import java.util.Set;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.opendaylight.mdsal.binding.api.NotificationPublishService;
import org.opendaylight.mdsal.binding.api.RpcProviderService;
import org.opendaylight.openflowplugin.api.openflow.connection.ConnectionContext;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceContext;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceInfo;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceState;
import org.opendaylight.openflowplugin.api.openflow.registry.flow.DeviceFlowRegistry;
import org.opendaylight.openflowplugin.api.openflow.statistics.ofpspecific.MessageSpy;
import org.opendaylight.openflowplugin.extension.api.core.extension.ExtensionConverterProvider;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.ConvertorExecutor;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.Nodes;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.NodeKey;
import org.opendaylight.yangtools.binding.DataObjectIdentifier;
import org.opendaylight.yangtools.binding.Rpc;
import org.opendaylight.yangtools.concepts.Registration;
import org.opendaylight.yangtools.yang.common.Uint32;
import org.opendaylight.yangtools.yang.common.Uint8;

@RunWith(MockitoJUnitRunner.class)
public class RpcContextImplTest {
    @Mock
    private RpcProviderService rpcProviderRegistry;
    @Mock
    private DeviceState deviceState;
    @Mock
    private MessageSpy messageSpy;
    @Mock
    private DeviceContext deviceContext;
    @Mock
    private ConnectionContext connectionContext;
    @Mock
    private Registration registration;
    @Mock
    private NotificationPublishService notificationPublishService;
    @Mock
    private DeviceInfo deviceInfo;
    @Mock
    private ExtensionConverterProvider extensionConverterProvider;
    @Mock
    private ConvertorExecutor convertorExecutor;
    @Mock
    private DeviceFlowRegistry flowRegistry;
    @Captor
    private ArgumentCaptor<Collection<Rpc<?, ?>>> captor;

    private DataObjectIdentifier.WithKey<Node, NodeKey> nodeInstanceIdentifier;
    private RpcContextImpl rpcContext;

    @Before
    public void setup() {
        final NodeId nodeId = new NodeId("openflow:1");
        nodeInstanceIdentifier = DataObjectIdentifier.builder(Nodes.class)
            .child(Node.class, new NodeKey(nodeId))
            .build();

        when(deviceInfo.getNodeInstanceIdentifier()).thenReturn(nodeInstanceIdentifier);
        when(deviceContext.getMessageSpy()).thenReturn(messageSpy);
        when(deviceContext.getDeviceInfo()).thenReturn(deviceInfo);
        when(deviceInfo.getNodeInstanceIdentifier()).thenReturn(nodeInstanceIdentifier);
        when(deviceInfo.reserveXidForDeviceMessage()).thenReturn(Uint32.TWO);
        when(deviceInfo.getVersion()).thenReturn(Uint8.ONE);
        when(deviceContext.getPrimaryConnectionContext()).thenReturn(connectionContext);

        rpcContext = new RpcContextImpl(
                rpcProviderRegistry,
                5,
                deviceContext,
                extensionConverterProvider,
                convertorExecutor,
                notificationPublishService, true);
    }

    @Test
    public void testStoreOrFail() {
        try (var rpcContext = new RpcContextImpl(
                rpcProviderRegistry,
                100,
                deviceContext,
                extensionConverterProvider,
                convertorExecutor,
                notificationPublishService, true)) {
            assertNotNull(rpcContext.createRequestContext());
        }
    }

    @Test
    public void testStoreOrFailThatFails() {
        try (var rpcContext = new RpcContextImpl(
                rpcProviderRegistry,
                0,
                deviceContext,
                extensionConverterProvider,
                convertorExecutor,
                notificationPublishService, true)) {
            assertNull(rpcContext.createRequestContext());
        }
    }

    @Test
    public void testStoreAndCloseOrFail() {
        try (var rpcContext = new RpcContextImpl(
                rpcProviderRegistry,
                100,
                deviceContext,
                extensionConverterProvider,
                convertorExecutor,
                notificationPublishService, true)) {
            try (var requestContext = rpcContext.createRequestContext()) {
                assertNotNull(requestContext);
            }
            verify(messageSpy).spyMessage(RpcContextImpl.class, MessageSpy.StatisticsGroup.REQUEST_STACK_FREED);
        }
    }

    /**
     * When deviceContext.reserveXidForDeviceMessage returns null, null should be returned.
     */
    @Test
    public void testCreateRequestContext1() {
        when(deviceInfo.reserveXidForDeviceMessage()).thenReturn(null);
        assertNull(rpcContext.createRequestContext());
    }

    /**
     * When deviceContext.reserveXidForDeviceMessage returns value, AbstractRequestContext should be returned.
     */
    @Test
    public void testCreateRequestContext2() {
        try (var temp = rpcContext.createRequestContext()) {
            // nothing
        }
        verify(messageSpy).spyMessage(RpcContextImpl.class, MessageSpy.StatisticsGroup.REQUEST_STACK_FREED);
    }

    @Test
    public void testInstantiateServiceInstance() {
        final var set = Set.<DataObjectIdentifier<?>>of(nodeInstanceIdentifier.toIdentifier());
        when(rpcProviderRegistry.registerRpcImplementations(any(Collection.class), eq(set))).thenReturn(registration);
        when(deviceContext.getDeviceFlowRegistry()).thenReturn(flowRegistry);

        rpcContext.instantiateServiceInstance();

        verify(rpcProviderRegistry).registerRpcImplementations(captor.capture(), eq(set));

        final var map = captor.getValue();
        assertEquals(46, map.size());
    }
}
