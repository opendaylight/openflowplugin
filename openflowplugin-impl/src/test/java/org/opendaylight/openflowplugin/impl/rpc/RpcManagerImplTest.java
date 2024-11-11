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
import org.opendaylight.openflowplugin.api.openflow.connection.ConnectionContext;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceContext;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceInfo;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceState;
import org.opendaylight.openflowplugin.api.openflow.rpc.RpcContext;
import org.opendaylight.openflowplugin.api.openflow.statistics.ofpspecific.MessageSpy;
import org.opendaylight.openflowplugin.extension.api.core.extension.ExtensionConverterProvider;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.ConvertorExecutor;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.Nodes;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.NodeKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.openflow.provider.config.rev160510.NonZeroUint16Type;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.openflow.provider.config.rev160510.OpenflowProviderConfigBuilder;
import org.opendaylight.yangtools.binding.DataObjectIdentifier;
import org.opendaylight.yangtools.yang.common.Uint16;

@RunWith(MockitoJUnitRunner.class)
public class RpcManagerImplTest {
    private static final Uint16 QUOTA_VALUE = Uint16.valueOf(5);
    private static final DataObjectIdentifier.WithKey<Node, NodeKey> NODE_PATH =
        DataObjectIdentifier.builder(Nodes.class).child(Node.class, new NodeKey(new NodeId("one"))).build();

    private RpcManagerImpl rpcManager;

    @Mock
    private RpcProviderService rpcProviderRegistry;
    @Mock
    private DeviceContext deviceContext;
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

    @Before
    public void setUp() {
        rpcManager = new RpcManagerImpl(new OpenflowProviderConfigBuilder()
                .setRpcRequestsQuota(new NonZeroUint16Type(QUOTA_VALUE))
                .setIsStatisticsRpcEnabled(false)
                .build(),
                rpcProviderRegistry, extensionConverterProvider, convertorExecutor, notificationPublishService);

        Mockito.when(deviceInfo.getNodeInstanceIdentifier()).thenReturn(NODE_PATH);
        Mockito.when(deviceContext.getDeviceInfo()).thenReturn(deviceInfo);
        Mockito.when(deviceContext.getMessageSpy()).thenReturn(messageSpy);
    }

    @Test
    public void createContext() {
        final var context = rpcManager.createContext(deviceContext);
        assertEquals(deviceInfo, context.getDeviceInfo());
    }

    @Test
    public void close() {
        rpcManager.addRecordToContexts(deviceInfo,removedContexts);
        rpcManager.close();
        verify(removedContexts,atLeastOnce()).close();
    }
}
