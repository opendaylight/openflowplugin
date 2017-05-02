/**
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.lifecycle;

import com.google.common.util.concurrent.Futures;
import io.netty.util.HashedWheelTimer;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.opendaylight.mdsal.singleton.common.api.ClusterSingletonServiceProvider;
import org.opendaylight.openflowplugin.api.openflow.connection.ConnectionContext;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceContext;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceInfo;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceManager;
import org.opendaylight.openflowplugin.api.openflow.rpc.RpcContext;
import org.opendaylight.openflowplugin.api.openflow.rpc.RpcManager;
import org.opendaylight.openflowplugin.api.openflow.statistics.StatisticsContext;
import org.opendaylight.openflowplugin.api.openflow.statistics.StatisticsManager;

@RunWith(MockitoJUnitRunner.class)
public class ContextChainHolderImplTest {

    @Mock
    private HashedWheelTimer timer;
    @Mock
    private StatisticsManager statisticsManager;
    @Mock
    private RpcManager rpcManager;
    @Mock
    private DeviceManager deviceManager;
    @Mock
    private StatisticsContext statisticsContext;
    @Mock
    private RpcContext rpcContext;
    @Mock
    private DeviceContext deviceContext;
    @Mock
    private ConnectionContext connectionContext;
    @Mock
    private DeviceInfo deviceInfo;
    @Mock
    private ClusterSingletonServiceProvider singletonServicesProvider;

    private ContextChainHolderImpl contextChainHolder;

    @Before
    public void setUp() throws Exception {
        contextChainHolder = new ContextChainHolderImpl(timer);
        contextChainHolder.addManager(statisticsManager);
        contextChainHolder.addManager(rpcManager);
        contextChainHolder.addManager(deviceManager);
        contextChainHolder.addSingletonServicesProvider(singletonServicesProvider);
        Mockito.when(connectionContext.getDeviceInfo()).thenReturn(deviceInfo);
        Mockito.when(deviceManager.createContext(connectionContext)).thenReturn(deviceContext);
        Mockito.when(rpcManager.createContext(
                deviceInfo,
                deviceContext))
                .thenReturn(rpcContext);
        Mockito.when(statisticsManager.createContext(deviceContext)).thenReturn(statisticsContext);
        Mockito.when(deviceContext.makeDeviceSlave()).thenReturn(Futures.immediateFuture(null));
    }

    @Test
    public void addManager() throws Exception {
        Assert.assertTrue(contextChainHolder.checkAllManagers());
    }

    @Test
    public void createContextChain() throws Exception {
        contextChainHolder.createContextChain(connectionContext);
        Mockito.verify(deviceManager).createContext(Mockito.any(ConnectionContext.class));
        Mockito.verify(rpcManager).createContext(Mockito.any(DeviceInfo.class), Mockito.any(DeviceContext.class));
        Mockito.verify(statisticsManager).createContext(Mockito.any(DeviceContext.class));
    }

    @Test
    public void destroyContextChain() throws Exception {

    }

    @Test
    public void pairConnection() throws Exception {

    }

    @Test
    public void deviceConnected() throws Exception {

    }

    @Test
    public void onNotAbleToStartMastership() throws Exception {

    }

    @Test
    public void onMasterRoleAcquired() throws Exception {

    }

    @Test
    public void onSlaveRoleAcquired() throws Exception {

    }

    @Test
    public void onSlaveRoleNotAcquired() throws Exception {

    }

    @Test
    public void onDeviceDisconnected() throws Exception {

    }

}