/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl;

import static org.mockito.ArgumentMatchers.any;
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
import org.opendaylight.openflowplugin.api.openflow.mastership.MastershipChangeServiceManager;
import org.opendaylight.openflowplugin.api.openflow.statistics.ofpspecific.MessageIntelligenceAgency;
import org.opendaylight.openflowplugin.impl.configuration.OSGiConfiguration;
import org.opendaylight.yangtools.concepts.Registration;

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
    OSGiConfiguration configuration;
    @Mock
    MastershipChangeServiceManager mastershipChangeServiceManager;
    @Mock
    MessageIntelligenceAgency messageIntelligenceAgency;

    @Before
    public void setUp() {
        when(dataBroker.newWriteOnlyTransaction()).thenReturn(writeTransaction);
        doReturn(CommitInfo.emptyFluentFuture()).when(writeTransaction).commit();
        when(entityOwnershipService.registerListener(any(), any())).thenReturn(entityOwnershipListenerRegistration);
        when(switchConnectionProvider.startup()).thenReturn(Futures.immediateFuture(true));
        when(switchConnectionProvider.shutdown()).thenReturn(Futures.immediateFuture(true));

        when(configuration.use$_$single$_$layer$_$()).thenReturn(false);
        when(configuration.thread$_$pool$_$min$_$threads()).thenReturn(1);
        when(configuration.thread$_$pool$_$max$_$threads()).thenReturn(32000);
        when(configuration.thread$_$pool$_$timeout()).thenReturn(60);
        when(configuration.device$_$connection$_$hold$_$time$_$in$_$seconds()).thenReturn(60);
        when(configuration.device$_$connection$_$rate$_$limit$_$per$_$min()).thenReturn(0);
    }

    @Test
    public void testInitializeAndClose() {
        try (var provider = new OpenFlowPluginProviderImpl(List.of(switchConnectionProvider), dataBroker,
                rpcProviderRegistry, notificationPublishService, clusterSingletonServiceProvider,
                entityOwnershipService, mastershipChangeServiceManager, messageIntelligenceAgency,
                ofPluginDiagstatusProvider, systemReadyMonitor, configuration)) {
            // Calling the onSystemBootReady() callback
            provider.onSystemBootReady();
            verify(switchConnectionProvider).startup();
        }
        verify(switchConnectionProvider).shutdown();
    }
}
