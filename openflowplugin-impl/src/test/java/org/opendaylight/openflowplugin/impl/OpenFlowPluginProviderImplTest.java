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
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.common.collect.Lists;
import com.google.common.util.concurrent.Futures;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.NotificationService;
import org.opendaylight.controller.md.sal.binding.api.WriteTransaction;
import org.opendaylight.controller.md.sal.common.api.clustering.EntityOwnershipListenerRegistration;
import org.opendaylight.controller.md.sal.common.api.clustering.EntityOwnershipService;
import org.opendaylight.controller.sal.binding.api.BindingAwareBroker;
import org.opendaylight.controller.sal.binding.api.RpcProviderRegistry;
import org.opendaylight.openflowjava.protocol.spi.connection.SwitchConnectionProvider;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.openflowplugin.sm.control.rev150812.StatisticsManagerControlService;

@RunWith(MockitoJUnitRunner.class)
public class OpenFlowPluginProviderImplTest {

    @Mock
    DataBroker dataBroker;

    @Mock
    RpcProviderRegistry rpcProviderRegistry;

    @Mock
    NotificationService notificationService;

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

    private static final long RPC_REQUESTS_QUOTA = 500;
    private static final long GLOBAL_NOTIFICATION_QUOTA = 131072;
    private static final int THREAD_POOL_MIN_THREADS = 1;
    private static final int THREAD_POOL_MAX_THREADS = 32000;
    private static final long THREAD_POOL_TIMEOUT = 60;

    private OpenFlowPluginProviderImpl provider;

    @Before
    public void setUp() throws Exception {
        when(dataBroker.newWriteOnlyTransaction()).thenReturn(writeTransaction);
        when(writeTransaction.submit()).thenReturn(Futures.immediateCheckedFuture(null));
        when(entityOwnershipService.registerListener(any(), any())).thenReturn(entityOwnershipListenerRegistration);
        when(rpcProviderRegistry.addRpcImplementation(eq(StatisticsManagerControlService.class), any())).thenReturn(controlServiceRegistration);
        when(switchConnectionProvider.startup()).thenReturn(Futures.immediateCheckedFuture(null));

        provider = new OpenFlowPluginProviderImpl(
                RPC_REQUESTS_QUOTA,
                GLOBAL_NOTIFICATION_QUOTA,
                THREAD_POOL_MIN_THREADS,
                THREAD_POOL_MAX_THREADS,
                THREAD_POOL_TIMEOUT);

        provider.setDataBroker(dataBroker);
        provider.setRpcProviderRegistry(rpcProviderRegistry);
        provider.setNotificationProviderService(notificationService);
        provider.setEntityOwnershipService(entityOwnershipService);
        provider.setSwitchConnectionProviders(Lists.newArrayList(switchConnectionProvider));
    }

    @After
    public void tearDown() throws Exception {

    }

    @Test
    public void testInitializeAndClose() throws Exception {
        provider.initialize();
        verify(switchConnectionProvider).startup();

        provider.close();
        verify(entityOwnershipListenerRegistration, times(2)).close();
    }
}