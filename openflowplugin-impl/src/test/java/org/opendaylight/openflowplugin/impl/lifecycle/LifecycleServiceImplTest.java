/**
 * Copyright (c) 2016 Pantheon Technologies s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.lifecycle;

import com.google.common.util.concurrent.Futures;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.opendaylight.mdsal.singleton.common.api.ClusterSingletonServiceProvider;
import org.opendaylight.mdsal.singleton.common.api.ClusterSingletonServiceRegistration;
import org.opendaylight.mdsal.singleton.common.api.ServiceGroupIdentifier;
import org.opendaylight.openflowplugin.api.openflow.connection.ConnectionContext;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceContext;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceInfo;
import org.opendaylight.openflowplugin.api.openflow.lifecycle.LifecycleService;
import org.opendaylight.openflowplugin.api.openflow.registry.flow.DeviceFlowRegistry;
import org.opendaylight.openflowplugin.api.openflow.rpc.RpcContext;
import org.opendaylight.openflowplugin.api.openflow.statistics.StatisticsContext;

@RunWith(MockitoJUnitRunner.class)
public class LifecycleServiceImplTest {

    private static final String TEST_NODE = "test node";
    private static final ServiceGroupIdentifier SERVICE_GROUP_IDENTIFIER = ServiceGroupIdentifier.create(TEST_NODE);

    @Mock
    private DeviceInfo deviceInfo;
    @Mock
    private DeviceContext deviceContext;
    @Mock
    private RpcContext rpcContext;
    @Mock
    private StatisticsContext statContext;
    @Mock
    private ConnectionContext connectionContext;
    @Mock
    private DeviceFlowRegistry deviceFlowRegistry;
    @Mock
    private ClusterSingletonServiceProvider clusterSingletonServiceProvider;
    @Mock
    private ClusterSingletonServiceRegistration clusterSingletonServiceRegistration;

    private LifecycleService lifecycleService;

    @Before
    public void setUp() {
        Mockito.when(deviceContext.getDeviceInfo()).thenReturn(deviceInfo);
        Mockito.when(deviceContext.getPrimaryConnectionContext()).thenReturn(connectionContext);
        Mockito.when(deviceContext.getDeviceFlowRegistry()).thenReturn(deviceFlowRegistry);
        Mockito.when(deviceContext.getServiceIdentifier()).thenReturn(SERVICE_GROUP_IDENTIFIER);
        Mockito.when(deviceFlowRegistry.fill()).thenReturn(Futures.immediateFuture(null));
        Mockito.when(connectionContext.getConnectionState()).thenReturn(ConnectionContext.CONNECTION_STATE.WORKING);
        Mockito.when(deviceInfo.getLOGValue()).thenReturn(TEST_NODE);
        Mockito.when(clusterSingletonServiceProvider.registerClusterSingletonService(Mockito.any()))
                .thenReturn(clusterSingletonServiceRegistration);

        Mockito.when(deviceContext.stopClusterServices(Mockito.anyBoolean())).thenReturn(Futures.immediateFuture(null));
        Mockito.when(statContext.stopClusterServices(Mockito.anyBoolean())).thenReturn(Futures.immediateFuture(null));
        Mockito.when(rpcContext.stopClusterServices(Mockito.anyBoolean())).thenReturn(Futures.immediateFuture(null));

        lifecycleService = new LifecycleServiceImpl();
        lifecycleService.setDeviceContext(deviceContext);
        lifecycleService.setRpcContext(rpcContext);
        lifecycleService.setStatContext(statContext);
        lifecycleService.registerService(clusterSingletonServiceProvider);
    }

    @Test
    public void instantiateServiceInstance() throws Exception {
        lifecycleService.instantiateServiceInstance();
        Mockito.verify(deviceContext).setLifecycleInitializationPhaseHandler(Mockito.any());
        Mockito.verify(statContext).setLifecycleInitializationPhaseHandler(Mockito.any());
        Mockito.verify(statContext).setInitialSubmitHandler(Mockito.any());
        Mockito.verify(rpcContext).setLifecycleInitializationPhaseHandler(Mockito.any());
    }

    @Test
    public void closeServiceInstance() throws Exception {
        lifecycleService.closeServiceInstance().get();
        Mockito.verify(statContext).stopClusterServices(false);
        Mockito.verify(deviceContext).stopClusterServices(false);
        Mockito.verify(rpcContext).stopClusterServices(false);
    }

    @Test
    public void getIdentifier() throws Exception {
        Assert.assertEquals(lifecycleService.getIdentifier(), SERVICE_GROUP_IDENTIFIER);
    }

    @Test
    public void closeConnection() throws Exception {
        lifecycleService.closeConnection();
        Mockito.verify(deviceContext).shutdownConnection();
    }

}
