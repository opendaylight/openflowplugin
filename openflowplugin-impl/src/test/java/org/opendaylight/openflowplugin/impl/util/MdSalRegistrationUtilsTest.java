/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.impl.util;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.opendaylight.mdsal.binding.api.NotificationPublishService;
import org.opendaylight.openflowplugin.api.openflow.FlowGroupCacheManager;
import org.opendaylight.openflowplugin.api.openflow.connection.ConnectionContext;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceContext;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceInfo;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceState;
import org.opendaylight.openflowplugin.api.openflow.rpc.RpcContext;
import org.opendaylight.openflowplugin.extension.api.core.extension.ExtensionConverterProvider;
import org.opendaylight.openflowplugin.impl.statistics.services.OpendaylightFlowStatisticsServiceImpl;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.ConvertorManager;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.ConvertorManagerFactory;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.statistics.rev130819.OpendaylightFlowStatisticsService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.FeaturesReply;
import org.opendaylight.yangtools.yang.binding.RpcService;
import org.opendaylight.yangtools.yang.common.Uint64;

@RunWith(MockitoJUnitRunner.class)
public class MdSalRegistrationUtilsTest {

    /**
     * Number of currently registrated services (can be changed)
     * (RpcContext, DeviceContext)}.
     */
    private static final int NUMBER_OF_RPC_SERVICE_REGISTRATION = 16;
    private static final int NUMBER_OF_STAT_COMPAT_RPC_SERVICE_REGISTRATION = 5;

    @Mock
    private RpcContext mockedRpcContext;
    @Mock
    private DeviceContext mockedDeviceContext;
    @Mock
    private ConnectionContext mockedConnectionContext;
    @Mock
    private DeviceState mockedDeviceState;
    @Mock
    private DeviceInfo mockedDeviceInfo;
    @Mock
    private FeaturesReply mockedFeatures;
    @Mock
    private ExtensionConverterProvider extensionConverterProvider;
    @Mock
    private NotificationPublishService notificationPublishService;

    private ConvertorManager convertorManager;
    private FlowGroupCacheManager flowGroupCacheManager;

    @Before
    public void setUp() {
        convertorManager = ConvertorManagerFactory.createDefaultManager();
        when(mockedDeviceContext.getDeviceInfo()).thenReturn(mockedDeviceInfo);
        when(mockedConnectionContext.getFeatures()).thenReturn(mockedFeatures);
        when(mockedDeviceInfo.getDatapathId()).thenReturn(Uint64.valueOf(12345));
        when(mockedDeviceContext.getPrimaryConnectionContext()).thenReturn(mockedConnectionContext);
    }

    @Test
    public void registerServiceTest() {
        MdSalRegistrationUtils.registerServices(mockedRpcContext,
                                                mockedDeviceContext,
                                                extensionConverterProvider,
                                                convertorManager,
                                                flowGroupCacheManager);
        verify(mockedRpcContext, times(NUMBER_OF_RPC_SERVICE_REGISTRATION)).registerRpcServiceImplementation(
                any(), any(RpcService.class));
    }

    @Test
    public void registerStatCompatibilityServices() {
        final OpendaylightFlowStatisticsService flowStatService = OpendaylightFlowStatisticsServiceImpl
                .createWithOook(mockedRpcContext, mockedDeviceContext, convertorManager);

        when(mockedRpcContext.lookupRpcService(OpendaylightFlowStatisticsService.class)).thenReturn(
                flowStatService);
        MdSalRegistrationUtils.registerStatCompatibilityServices(mockedRpcContext,
                                                                 mockedDeviceContext,
                                                                 notificationPublishService,
                                                                 convertorManager);
        verify(mockedRpcContext, times(NUMBER_OF_STAT_COMPAT_RPC_SERVICE_REGISTRATION))
                .registerRpcServiceImplementation(any(), any(RpcService.class));
    }

}