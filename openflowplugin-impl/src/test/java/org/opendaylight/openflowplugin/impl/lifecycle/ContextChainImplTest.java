/**
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.lifecycle;

import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.MoreExecutors;
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
import org.opendaylight.openflowplugin.api.openflow.device.handlers.DeviceRemovedHandler;
import org.opendaylight.openflowplugin.api.openflow.lifecycle.ContextChain;
import org.opendaylight.openflowplugin.api.openflow.lifecycle.MastershipChangeListener;
import org.opendaylight.openflowplugin.api.openflow.rpc.RpcContext;
import org.opendaylight.openflowplugin.api.openflow.statistics.StatisticsContext;

@RunWith(MockitoJUnitRunner.class)
public class ContextChainImplTest {

    private static final String TEST_NODE = "test node";
    private static final ServiceGroupIdentifier SERVICE_GROUP_IDENTIFIER = ServiceGroupIdentifier.create(TEST_NODE);

    @Mock
    private StatisticsContext statisticsContext;
    @Mock
    private RpcContext rpcContext;
    @Mock
    private DeviceContext deviceContext;
    @Mock
    private DeviceInfo deviceInfo;
    @Mock
    private ConnectionContext connectionContext;
    @Mock
    private ClusterSingletonServiceProvider clusterSingletonServiceProvider;
    @Mock
    private ClusterSingletonServiceRegistration clusterSingletonServiceRegistration;
    @Mock
    private MastershipChangeListener mastershipChangeListener;
    @Mock
    private DeviceRemovedHandler deviceRemovedHandler;

    private ContextChain contextChain;

    @Before
    public void setUp() throws Exception {
        Mockito.when(deviceContext.getDeviceInfo()).thenReturn(deviceInfo);
        Mockito.when(deviceInfo.getServiceIdentifier()).thenReturn(SERVICE_GROUP_IDENTIFIER);
        Mockito.when(deviceContext.closeServiceInstance()).thenReturn(Futures.immediateFuture(null));
        Mockito.when(rpcContext.closeServiceInstance()).thenReturn(Futures.immediateFuture(null));
        Mockito.when(statisticsContext.closeServiceInstance()).thenReturn(Futures.immediateFuture(null));
        Mockito.when(statisticsContext.gatherDynamicData()).thenReturn(Futures.immediateFuture(null));
        Mockito.when(connectionContext.getDeviceInfo()).thenReturn(deviceInfo);
        Mockito.when(connectionContext.getConnectionState()).thenReturn(ConnectionContext.CONNECTION_STATE.WORKING);
        Mockito.when(clusterSingletonServiceProvider.registerClusterSingletonService(Mockito.any()))
                .thenReturn(clusterSingletonServiceRegistration);

        contextChain = new ContextChainImpl(mastershipChangeListener, connectionContext,
                MoreExecutors.newDirectExecutorService());
        contextChain.addContext(statisticsContext);
        contextChain.addContext(rpcContext);
        contextChain.addContext(deviceContext);
        contextChain.registerServices(clusterSingletonServiceProvider);
    }

    @Test
    public void closeServiceInstance() throws Exception {
        contextChain.closeServiceInstance();
        Mockito.verify(deviceContext).closeServiceInstance();
        Mockito.verify(rpcContext).closeServiceInstance();
        Mockito.verify(statisticsContext).closeServiceInstance();
    }

    @Test
    public void close() throws Exception {
        contextChain.registerDeviceRemovedHandler(deviceRemovedHandler);
        contextChain.close();
        Mockito.verify(statisticsContext).close();
        Mockito.verify(deviceContext).close();
        Mockito.verify(rpcContext).close();
        Mockito.verify(deviceRemovedHandler).onDeviceRemoved(Mockito.any(DeviceInfo.class));
    }

    @Test
    public void closeTwoTimes() throws Exception {
        contextChain.registerDeviceRemovedHandler(deviceRemovedHandler);
        contextChain.close();
        contextChain.close();
        Mockito.verify(deviceRemovedHandler, Mockito.times(1))
                .onDeviceRemoved(Mockito.any(DeviceInfo.class));
    }

    @Test
    public void closeThreeTimes() throws Exception {
        contextChain.registerDeviceRemovedHandler(deviceRemovedHandler);
        contextChain.close();
        contextChain.close();
        Mockito.verify(deviceRemovedHandler, Mockito.times(1))
                .onDeviceRemoved(Mockito.any(DeviceInfo.class));
    }

    @Test
    public void getIdentifier() throws Exception {
        Assert.assertEquals(contextChain.getIdentifier(), SERVICE_GROUP_IDENTIFIER);
    }

    @Test
    public void instantiateServiceInstanceFail() throws Exception {
        Mockito.doThrow(new IllegalStateException()).when(deviceContext).instantiateServiceInstance();
        contextChain.instantiateServiceInstance();
        Mockito.verify(mastershipChangeListener).onNotAbleToStartMastershipMandatory(Mockito.any(DeviceInfo.class), Mockito.anyString());
    }
}
