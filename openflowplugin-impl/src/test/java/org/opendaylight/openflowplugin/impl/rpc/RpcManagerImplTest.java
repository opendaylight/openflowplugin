/*
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
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.opendaylight.mdsal.binding.api.NotificationPublishService;
import org.opendaylight.mdsal.binding.api.RpcProviderService;
import org.opendaylight.openflowjava.protocol.api.util.EncodeConstants;
import org.opendaylight.openflowplugin.api.openflow.FlowGroupCacheManager;
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
import org.opendaylight.yangtools.concepts.ObjectRegistration;
import org.opendaylight.yangtools.yang.binding.KeyedInstanceIdentifier;
import org.opendaylight.yangtools.yang.binding.RpcService;
import org.opendaylight.yangtools.yang.common.Uint16;

@RunWith(MockitoJUnitRunner.class)
public class RpcManagerImplTest {

    private static final Uint16 QUOTA_VALUE = Uint16.valueOf(5);
    private RpcManagerImpl rpcManager;

    @Mock
    private RpcProviderService rpcProviderRegistry;
    @Mock
    private DeviceContext deviceContext;
    @Mock
    private ObjectRegistration<RpcService> routedRpcRegistration;
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
    @Mock
    private FlowGroupCacheManager flowGroupCacheManager;

    private final NodeId nodeId = new NodeId("openflow-junit:1");

    @Before
    public void setUp() {
        final NodeKey nodeKey = new NodeKey(nodeId);
        rpcManager = new RpcManagerImpl(new OpenflowProviderConfigBuilder()
                .setRpcRequestsQuota(new NonZeroUint16Type(QUOTA_VALUE))
                .setIsStatisticsRpcEnabled(false)
                .build(),
                rpcProviderRegistry, extensionConverterProvider, convertorExecutor, notificationPublishService,
                flowGroupCacheManager);

        FeaturesReply features = new GetFeaturesOutputBuilder()
                .setVersion(EncodeConstants.OF_VERSION_1_3)
                .build();

        Mockito.when(deviceInfo.getNodeInstanceIdentifier()).thenReturn(nodePath);
        Mockito.when(deviceContext.getDeviceInfo()).thenReturn(deviceInfo);
        Mockito.when(deviceContext.getMessageSpy()).thenReturn(messageSpy);
    }

    @Test
    public void createContext() {
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
