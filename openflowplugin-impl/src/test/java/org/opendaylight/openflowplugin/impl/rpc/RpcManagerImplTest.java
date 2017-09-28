/**
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.rpc;

import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;

import java.util.concurrent.ConcurrentMap;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.opendaylight.controller.md.sal.binding.api.NotificationPublishService;
import org.opendaylight.controller.sal.binding.api.BindingAwareBroker;
import org.opendaylight.controller.sal.binding.api.BindingAwareBroker.ProviderContext;
import org.opendaylight.openflowplugin.api.OFConstants;
import org.opendaylight.openflowplugin.api.openflow.connection.ConnectionContext;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceContext;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceInfo;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceState;
import org.opendaylight.openflowplugin.api.openflow.rpc.RpcContext;
import org.opendaylight.openflowplugin.api.openflow.statistics.ofpspecific.MessageSpy;
import org.opendaylight.openflowplugin.extension.api.core.extension.ExtensionConverterProvider;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.ConvertorExecutor;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.NodeKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.FeaturesReply;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.GetFeaturesOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.GetFeaturesOutputBuilder;
import org.opendaylight.yangtools.yang.binding.KeyedInstanceIdentifier;
import org.opendaylight.yangtools.yang.binding.RpcService;


@RunWith(MockitoJUnitRunner.class)
public class RpcManagerImplTest {

    private static final int QUOTA_VALUE = 5;
    private RpcManagerImpl rpcManager;

    @Mock
    private ProviderContext rpcProviderRegistry;
    @Mock
    private DeviceContext deviceContext;
    @Mock
    private BindingAwareBroker.RoutedRpcRegistration<RpcService> routedRpcRegistration;
    @Mock
    private DeviceState deviceState;
    @Mock
    private ConnectionContext connectionContext;
    @Mock
    private MessageSpy messageSpy;
    @Mock
    private RpcContext removedContexts;
    @Mock
    private ConcurrentMap<DeviceInfo, RpcContext> contexts;
    @Mock
    private DeviceInfo deviceInfo;
    @Mock
    private ExtensionConverterProvider extensionConverterProvider;
    @Mock
    private ConvertorExecutor convertorExecutor;
    @Mock
    private NotificationPublishService notificationPublishService;

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    private KeyedInstanceIdentifier<Node, NodeKey> nodePath;

    private NodeId nodeId = new NodeId("openflow-junit:1");

    @Before
    public void setUp() {
        final NodeKey nodeKey = new NodeKey(nodeId);
        rpcManager = new RpcManagerImpl(rpcProviderRegistry, extensionConverterProvider, convertorExecutor, notificationPublishService);
        rpcManager.setRpcRequestQuota(QUOTA_VALUE);

        GetFeaturesOutput featuresOutput = new GetFeaturesOutputBuilder()
                .setVersion(OFConstants.OFP_VERSION_1_3)
                .build();

        FeaturesReply features = featuresOutput;

        Mockito.when(connectionContext.getFeatures()).thenReturn(features);
        Mockito.when(deviceContext.getPrimaryConnectionContext()).thenReturn(connectionContext);
        Mockito.when(deviceContext.getDeviceState()).thenReturn(deviceState);
        Mockito.when(deviceInfo.getNodeInstanceIdentifier()).thenReturn(nodePath);
        Mockito.when(connectionContext.getFeatures()).thenReturn(features);
        Mockito.when(deviceContext.getPrimaryConnectionContext()).thenReturn(connectionContext);
        Mockito.when(deviceContext.getDeviceState()).thenReturn(deviceState);
        Mockito.when(deviceInfo.getNodeInstanceIdentifier()).thenReturn(nodePath);
        Mockito.when(deviceContext.getMessageSpy()).thenReturn(messageSpy);
        Mockito.when(deviceInfo.getNodeId()).thenReturn(nodeKey.getId());
        Mockito.when(rpcProviderRegistry.addRoutedRpcImplementation(
                Matchers.any(), Matchers.any(RpcService.class)))
                .thenReturn(routedRpcRegistration);
        Mockito.when(contexts.remove(deviceInfo)).thenReturn(removedContexts);
    }

    @Test
    public void close() {
        rpcManager.addRecordToContexts(deviceInfo,removedContexts);
        rpcManager.close();
        verify(removedContexts,atLeastOnce()).close();
    }
}
