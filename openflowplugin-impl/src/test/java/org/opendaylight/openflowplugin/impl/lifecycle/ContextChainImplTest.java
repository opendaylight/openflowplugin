/**
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others. All rights reserved.
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
import org.opendaylight.mdsal.singleton.common.api.ServiceGroupIdentifier;
import org.opendaylight.openflowplugin.api.openflow.connection.ConnectionContext;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceContext;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceInfo;
import org.opendaylight.openflowplugin.api.openflow.device.handlers.ClusterInitializationPhaseHandler;
import org.opendaylight.openflowplugin.api.openflow.lifecycle.ContextChain;
import org.opendaylight.openflowplugin.api.openflow.lifecycle.LifecycleService;
import org.opendaylight.openflowplugin.api.openflow.rpc.RpcContext;
import org.opendaylight.openflowplugin.api.openflow.statistics.StatisticsContext;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.openflow.provider.config.rev160510.ContextChainState;

@RunWith(MockitoJUnitRunner.class)
public class ContextChainImplTest {

    @Mock
    private StatisticsContext statisticsContext;
    @Mock
    private RpcContext rpcContext;
    @Mock
    private DeviceContext deviceContext;
    @Mock
    private LifecycleService lifecycleService;
    @Mock
    private DeviceInfo deviceInfo;
    @Mock
    private ConnectionContext connectionContext;
    @Mock
    private ConnectionContext secondaryConnectionContext;
    @Mock
    private ClusterSingletonServiceProvider clusterSingletonServiceProvider;

    private ContextChain contextChain;

    @Before
    public void setUp() throws Exception {

        Mockito.when(deviceContext.getDeviceInfo()).thenReturn(deviceInfo);
        Mockito.when(deviceContext.stopClusterServices(Mockito.anyBoolean()))
                .thenReturn(Futures.immediateFuture(null));
        Mockito.when(rpcContext.stopClusterServices()).thenReturn(Futures.immediateFuture(null));
        Mockito.when(statisticsContext.stopClusterServices()).thenReturn(Futures.immediateFuture(null));
        Mockito.when(statisticsContext.initialGatherDynamicData()).thenReturn(Futures.immediateFuture(null));

        contextChain = new ContextChainImpl(connectionContext);
        contextChain.addContext(statisticsContext);
        contextChain.addContext(rpcContext);
        contextChain.addContext(deviceContext);
        contextChain.addLifecycleService(lifecycleService);

    }

    @Test
    public void stopChain() throws Exception {
        contextChain.stopChain(true);
        Mockito.verify(deviceContext).stopClusterServices(Mockito.anyBoolean());
        Mockito.verify(rpcContext).stopClusterServices();
        Mockito.verify(statisticsContext).stopClusterServices();
    }

    @Test
    public void startChain() throws Exception {
        Assert.assertSame(contextChain.getContextChainState(), ContextChainState.INITIALIZED);
        contextChain.startChain();
        Mockito.verify(statisticsContext).initialGatherDynamicData();
        Assert.assertSame(contextChain.getContextChainState(), ContextChainState.WORKINGMASTER);
    }

    @Test
    public void startChainTwoTimes() throws Exception {
        Assert.assertSame(contextChain.getContextChainState(), ContextChainState.INITIALIZED);
        contextChain.startChain();
        contextChain.startChain();
        Assert.assertSame(contextChain.getContextChainState(), ContextChainState.WORKINGMASTER);
        Mockito.verify(statisticsContext, Mockito.times(1)).initialGatherDynamicData();
    }

    @Test
    public void startChainThreeTimes() throws Exception {
        Assert.assertSame(contextChain.getContextChainState(), ContextChainState.INITIALIZED);
        contextChain.startChain();
        contextChain.startChain();
        Assert.assertSame(contextChain.getContextChainState(), ContextChainState.WORKINGMASTER);
        Mockito.verify(statisticsContext, Mockito.times(1)).initialGatherDynamicData();
    }

    @Test
    public void close() throws Exception {
        contextChain.close();
        Mockito.verify(statisticsContext).close();
        Mockito.verify(deviceContext).close();
        Mockito.verify(rpcContext).close();
        Mockito.verify(lifecycleService).close();
    }

    @Test
    public void changePrimaryConnection() throws Exception {
        Assert.assertSame(contextChain.getPrimaryConnectionContext(), connectionContext);
        contextChain.changePrimaryConnection(secondaryConnectionContext);
        Assert.assertSame(contextChain.getPrimaryConnectionContext(), secondaryConnectionContext);
        Mockito.verify(deviceContext).replaceConnection(Mockito.any(ConnectionContext.class));
        Mockito.verify(rpcContext).replaceConnection(Mockito.any(ConnectionContext.class));
        Mockito.verify(statisticsContext).replaceConnection(Mockito.any(ConnectionContext.class));
    }

    @Test
    public void connectionDropped() throws Exception {
        contextChain.startChain();
        Mockito.verify(statisticsContext).initialGatherDynamicData();
        Assert.assertSame(contextChain.getContextChainState(), ContextChainState.WORKINGMASTER);
        contextChain.connectionDropped();
        Mockito.verify(deviceContext).stopClusterServices(Mockito.anyBoolean());
        Mockito.verify(rpcContext).stopClusterServices();
        Mockito.verify(statisticsContext).stopClusterServices();
    }

    @Test
    public void sleepTheChainAndDropConnection() throws Exception {
        contextChain.sleepTheChainAndDropConnection();
        Assert.assertSame(contextChain.getContextChainState(), ContextChainState.SLEEPING);
        Mockito.verify(connectionContext).closeConnection(Mockito.anyBoolean());
    }

    @Test
    public void registerServices() throws Exception {
        Assert.assertSame(contextChain.getContextChainState(), ContextChainState.INITIALIZED);
        contextChain.registerServices(clusterSingletonServiceProvider);
        Mockito.verify(lifecycleService).registerService(
                Mockito.any(ClusterSingletonServiceProvider.class),
                Mockito.any(DeviceContext.class));
    }

    @Test
    public void makeDeviceSlave() throws Exception {
        contextChain.makeDeviceSlave();
        Mockito.verify(lifecycleService).makeDeviceSlave(Mockito.any(DeviceContext.class));
    }

    @Test
    public void closePrimaryConnection() throws Exception {
        contextChain.closePrimaryConnection();
        Mockito.verify(connectionContext).closeConnection(Mockito.anyBoolean());
    }

}

