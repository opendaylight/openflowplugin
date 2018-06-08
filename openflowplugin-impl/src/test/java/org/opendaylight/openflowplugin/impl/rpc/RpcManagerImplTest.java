/**
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.rpc;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;

import java.util.concurrent.ConcurrentMap;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.opendaylight.controller.md.sal.binding.api.NotificationPublishService;
import org.opendaylight.controller.sal.binding.api.BindingAwareBroker;
import org.opendaylight.controller.sal.binding.api.RpcProviderRegistry;
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
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.GetFeaturesOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.openflow.provider.config.rev160510.NonZeroUint16Type;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.openflow.provider.config.rev160510.OpenflowProviderConfigBuilder;
import org.opendaylight.yangtools.yang.binding.KeyedInstanceIdentifier;
import org.opendaylight.yangtools.yang.binding.RpcService;


@RunWith(MockitoJUnitRunner.class)
public class RpcManagerImplTest {

    private static final int QUOTA_VALUE = 5;
    private RpcManagerImpl rpcManager;

    @Mock
    private RpcProviderRegistry rpcProviderRegistry;
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
    @Mock
    private KeyedInstanceIdentifier<Node, NodeKey> nodePath;

    private final NodeId nodeId = new NodeId("openflow-junit:1");

    @Before
    public void setUp() {
        final NodeKey nodeKey = new NodeKey(nodeId);
        rpcManager = new RpcManagerImpl(new OpenflowProviderConfigBuilder()
                .setRpcRequestsQuota(new NonZeroUint16Type(QUOTA_VALUE))
                .setIsStatisticsRpcEnabled(false)
                .build(),
                rpcProviderRegistry, extensionConverterProvider, convertorExecutor, notificationPublishService);

        FeaturesReply features = new GetFeaturesOutputBuilder()
                .setVersion(OFConstants.OFP_VERSION_1_3)
                .build();

        Mockito.when(deviceInfo.getNodeId()).thenReturn(nodeKey.getId());
        Mockito.when(deviceInfo.getNodeInstanceIdentifier()).thenReturn(nodePath);
        Mockito.when(connectionContext.getFeatures()).thenReturn(features);
        Mockito.when(deviceContext.getPrimaryConnectionContext()).thenReturn(connectionContext);
        Mockito.when(deviceContext.getDeviceState()).thenReturn(deviceState);
        Mockito.when(deviceContext.getDeviceInfo()).thenReturn(deviceInfo);
        Mockito.when(deviceContext.getMessageSpy()).thenReturn(messageSpy);
        Mockito.when(rpcProviderRegistry.addRoutedRpcImplementation(
                Matchers.any(), Matchers.any(RpcService.class)))
                .thenReturn(routedRpcRegistration);
        Mockito.when(contexts.remove(deviceInfo)).thenReturn(removedContexts);
    }

    @Test
    public void createContext() throws Exception {
        final RpcContext context = rpcManager.createContext(deviceContext);
        assertEquals(deviceInfo, context.getDeviceInfo());
    }

    @Test
    public void close() {
        rpcManager.addRecordToContexts(deviceInfo,removedContexts);
        rpcManager.close();
        verify(removedContexts,atLeastOnce()).close();
    }
}
