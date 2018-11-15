/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.impl;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.common.collect.Lists;
import com.google.common.util.concurrent.Futures;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.opendaylight.controller.md.sal.binding.api.NotificationPublishService;
import org.opendaylight.controller.md.sal.binding.api.WriteTransaction;
import org.opendaylight.controller.sal.binding.api.BindingAwareBroker;
import org.opendaylight.controller.sal.binding.api.RpcProviderRegistry;
import org.opendaylight.infrautils.ready.SystemReadyMonitor;
import org.opendaylight.mdsal.eos.binding.api.EntityOwnershipListenerRegistration;
import org.opendaylight.mdsal.eos.binding.api.EntityOwnershipService;
import org.opendaylight.mdsal.singleton.common.api.ClusterSingletonServiceProvider;
import org.opendaylight.openflowjava.protocol.spi.connection.SwitchConnectionProvider;
import org.opendaylight.openflowjava.protocol.spi.connection.SwitchConnectionProviderList;
import org.opendaylight.openflowplugin.api.openflow.configuration.ConfigurationProperty;
import org.opendaylight.openflowplugin.api.openflow.configuration.ConfigurationService;
import org.opendaylight.openflowplugin.api.openflow.mastership.MastershipChangeServiceManager;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.openflowplugin.sm.control.rev150812.StatisticsManagerControlService;

@RunWith(MockitoJUnitRunner.class)
public class OpenFlowPluginProviderImplTest {

    @Mock
    PingPongDataBroker dataBroker;

    @Mock
    RpcProviderRegistry rpcProviderRegistry;

    @Mock
    NotificationPublishService notificationPublishService;

    @Mock
    OpenflowPluginDiagStatusProvider ofPluginDiagstatusProvider;

    @Mock
    SystemReadyMonitor systemReadyMonitor;

    @Mock
    WriteTransaction writeTransaction;

    @Mock
    EntityOwnershipService entityOwnershipService;

    @Mock
    EntityOwnershipListenerRegistration entityOwnershipListenerRegistration;

    @Mock
    BindingAwareBroker.RpcRegistration<StatisticsManagerControlService> controlServiceRegistration;

    @Mock
    SwitchConnectionProvider switchConnectionProvider;

    @Mock
    ClusterSingletonServiceProvider clusterSingletonServiceProvider;

    @Mock
    ConfigurationService configurationService;

    @Mock
    MastershipChangeServiceManager mastershipChangeServiceManager;

    private static final int RPC_REQUESTS_QUOTA = 500;
    private static final long GLOBAL_NOTIFICATION_QUOTA = 131072;
    private static final int THREAD_POOL_MIN_THREADS = 1;
    private static final int THREAD_POOL_MAX_THREADS = 32000;
    private static final long THREAD_POOL_TIMEOUT = 60;
    private static final long BASIC_TIMER_DELAY = 1L;
    private static final boolean USE_SINGLE_LAYER_SERIALIZATION = false;
    private static final int DEVICE_CONNECTION_RATE_LIMIT_PER_MIN = 0;

    @Before
    public void setUp() throws Exception {
        when(dataBroker.newWriteOnlyTransaction()).thenReturn(writeTransaction);
        when(writeTransaction.submit()).thenReturn(Futures.immediateCheckedFuture(null));
        when(entityOwnershipService.registerListener(any(), any())).thenReturn(entityOwnershipListenerRegistration);
        when(rpcProviderRegistry.addRpcImplementation(eq(StatisticsManagerControlService.class), any()))
                .thenReturn(controlServiceRegistration);
        when(switchConnectionProvider.startup()).thenReturn(Futures.immediateFuture(true));
        when(switchConnectionProvider.shutdown()).thenReturn(Futures.immediateFuture(true));
        when(configurationService.getProperty(eq(ConfigurationProperty.USE_SINGLE_LAYER_SERIALIZATION.toString()),
                any())).thenReturn(USE_SINGLE_LAYER_SERIALIZATION);
        when(configurationService.getProperty(eq(ConfigurationProperty.THREAD_POOL_MIN_THREADS.toString()), any()))
                .thenReturn(THREAD_POOL_MIN_THREADS);
        when(configurationService.getProperty(eq(ConfigurationProperty.THREAD_POOL_MAX_THREADS.toString()), any()))
                .thenReturn(THREAD_POOL_MAX_THREADS);
        when(configurationService.getProperty(eq(ConfigurationProperty.THREAD_POOL_TIMEOUT.toString()), any()))
                .thenReturn(THREAD_POOL_TIMEOUT);
        when(configurationService.getProperty(eq(ConfigurationProperty.DEVICE_CONNECTION_RATE_LIMIT_PER_MIN.toString()),
                any())).thenReturn(DEVICE_CONNECTION_RATE_LIMIT_PER_MIN);
    }

    @Test
    public void testInitializeAndClose() throws Exception {
        final OpenFlowPluginProviderImpl provider = new OpenFlowPluginProviderImpl(
                configurationService,
                new SwitchConnectionProviderList(Lists.newArrayList(switchConnectionProvider)),
                dataBroker,
                rpcProviderRegistry,
                notificationPublishService,
                clusterSingletonServiceProvider,
                entityOwnershipService,
                mastershipChangeServiceManager,
                ofPluginDiagstatusProvider,
                systemReadyMonitor);

        provider.initialize();
        // Calling the onSystemBootReady() callback
        provider.onSystemBootReady();
        verify(switchConnectionProvider).startup();
        provider.close();
        verify(switchConnectionProvider).shutdown();
    }
}
