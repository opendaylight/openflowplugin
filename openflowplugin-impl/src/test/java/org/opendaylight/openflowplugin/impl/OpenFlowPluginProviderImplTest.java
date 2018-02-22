/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.impl;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.common.collect.Lists;
import com.google.common.util.concurrent.Futures;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.NotificationPublishService;
import org.opendaylight.controller.md.sal.binding.api.WriteTransaction;
import org.opendaylight.controller.sal.binding.api.BindingAwareBroker;
import org.opendaylight.controller.sal.binding.api.RpcProviderRegistry;
import org.opendaylight.mdsal.eos.binding.api.EntityOwnershipListenerRegistration;
import org.opendaylight.mdsal.eos.binding.api.EntityOwnershipService;
import org.opendaylight.mdsal.singleton.common.api.ClusterSingletonServiceProvider;
import org.opendaylight.openflowjava.protocol.spi.connection.SwitchConnectionProvider;
import org.opendaylight.openflowplugin.api.openflow.OpenFlowPluginProvider;
import org.opendaylight.openflowplugin.api.openflow.configuration.ConfigurationProperty;
import org.opendaylight.openflowplugin.api.openflow.configuration.ConfigurationService;
import org.opendaylight.openflowplugin.api.openflow.mastership.MastershipChangeServiceManager;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.openflowplugin.sm.control.rev150812.StatisticsManagerControlService;

@RunWith(MockitoJUnitRunner.class)
public class OpenFlowPluginProviderImplTest {

    @Mock
    DataBroker dataBroker;

    @Mock
    RpcProviderRegistry rpcProviderRegistry;

    @Mock
    NotificationPublishService notificationPublishService;

    @Mock
    WriteTransaction writeTransaction;

    @Mock
    EntityOwnershipService entityOwnershipService;

    @Mock
    org.opendaylight.controller.md.sal.common.api.clustering
            .EntityOwnershipService clusteredEntityOwnershipService;

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
        when(configurationService.getProperty(eq(ConfigurationProperty.RPC_REQUESTS_QUOTA.toString()), any()))
                .thenReturn(RPC_REQUESTS_QUOTA);
        when(configurationService.getProperty(eq(ConfigurationProperty.GLOBAL_NOTIFICATION_QUOTA.toString()), any()))
                .thenReturn(GLOBAL_NOTIFICATION_QUOTA);
        when(configurationService.getProperty(eq(ConfigurationProperty.BASIC_TIMER_DELAY.toString()), any()))
                .thenReturn(BASIC_TIMER_DELAY);
    }

    @Test
    public void testInitializeAndClose() throws Exception {
        final OpenFlowPluginProvider provider = new OpenFlowPluginProviderFactoryImpl().newInstance(
                configurationService,
                dataBroker,
                rpcProviderRegistry,
                notificationPublishService,
                entityOwnershipService,
                clusteredEntityOwnershipService,
                Lists.newArrayList(switchConnectionProvider),
                clusterSingletonServiceProvider,
                mastershipChangeServiceManager);

        verify(switchConnectionProvider).startup();
        provider.close();
        verify(switchConnectionProvider).shutdown();
    }
}
