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
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.common.util.concurrent.Futures;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.opendaylight.infrautils.ready.SystemReadyMonitor;
import org.opendaylight.mdsal.binding.api.NotificationPublishService;
import org.opendaylight.mdsal.binding.api.RpcProviderService;
import org.opendaylight.mdsal.binding.api.WriteTransaction;
import org.opendaylight.mdsal.common.api.CommitInfo;
import org.opendaylight.mdsal.eos.binding.api.EntityOwnershipService;
import org.opendaylight.mdsal.singleton.api.ClusterSingletonServiceProvider;
import org.opendaylight.openflowjava.protocol.spi.connection.SwitchConnectionProvider;
import org.opendaylight.openflowplugin.api.openflow.configuration.ConfigurationProperty;
import org.opendaylight.openflowplugin.api.openflow.configuration.ConfigurationService;
import org.opendaylight.openflowplugin.api.openflow.mastership.MastershipChangeServiceManager;
import org.opendaylight.yangtools.concepts.Registration;
import org.opendaylight.yangtools.yang.common.Uint16;
import org.opendaylight.yangtools.yang.common.Uint32;

@RunWith(MockitoJUnitRunner.class)
public class OpenFlowPluginProviderImplTest {

    @Mock
    PingPongDataBroker dataBroker;

    @Mock
    RpcProviderService rpcProviderRegistry;

    @Mock
    NotificationPublishService notificationPublishService;

    @Mock
    DiagStatusProvider ofPluginDiagstatusProvider;

    @Mock
    SystemReadyMonitor systemReadyMonitor;

    @Mock
    WriteTransaction writeTransaction;

    @Mock
    EntityOwnershipService entityOwnershipService;

    @Mock
    Registration entityOwnershipListenerRegistration;

    @Mock
    SwitchConnectionProvider switchConnectionProvider;

    @Mock
    ClusterSingletonServiceProvider clusterSingletonServiceProvider;

    @Mock
    ConfigurationService configurationService;

    @Mock
    MastershipChangeServiceManager mastershipChangeServiceManager;

    private static final Uint16 THREAD_POOL_MIN_THREADS = Uint16.ONE;
    private static final Uint16 THREAD_POOL_MAX_THREADS = Uint16.valueOf(32000);
    private static final Uint32 THREAD_POOL_TIMEOUT = Uint32.valueOf(60);
    private static final boolean USE_SINGLE_LAYER_SERIALIZATION = false;
    private static final Uint16 DEVICE_CONNECTION_RATE_LIMIT_PER_MIN = Uint16.ZERO;
    private static final Uint16 DEVICE_CONNECTION_HOLD_TIME_IN_SECONDS = Uint16.valueOf(60);

    @Before
    public void setUp() {
        when(dataBroker.newWriteOnlyTransaction()).thenReturn(writeTransaction);
        doReturn(CommitInfo.emptyFluentFuture()).when(writeTransaction).commit();
        when(entityOwnershipService.registerListener(any(), any())).thenReturn(entityOwnershipListenerRegistration);
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
        when(configurationService.getProperty(
                eq(ConfigurationProperty.DEVICE_CONNECTION_HOLD_TIME_IN_SECONDS.toString()), any()))
                .thenReturn(DEVICE_CONNECTION_HOLD_TIME_IN_SECONDS);
    }

    @Test
    public void testInitializeAndClose() {
        try (var provider = new OpenFlowPluginProviderImpl(configurationService, List.of(switchConnectionProvider),
                dataBroker, rpcProviderRegistry, notificationPublishService, clusterSingletonServiceProvider,
                entityOwnershipService, mastershipChangeServiceManager, ofPluginDiagstatusProvider,
                systemReadyMonitor)) {
            // Calling the onSystemBootReady() callback
            provider.onSystemBootReady();
            verify(switchConnectionProvider).startup();
        }
        verify(switchConnectionProvider).shutdown();
    }
}
