/**
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.lifecycle;

import com.google.common.util.concurrent.Futures;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.opendaylight.openflowplugin.api.openflow.connection.ConnectionContext;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceContext;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceInfo;
import org.opendaylight.openflowplugin.api.openflow.lifecycle.ContextChain;
import org.opendaylight.openflowplugin.api.openflow.lifecycle.ContextChainMastershipState;
import org.opendaylight.openflowplugin.api.openflow.lifecycle.LifecycleService;
import org.opendaylight.openflowplugin.api.openflow.rpc.RpcContext;
import org.opendaylight.openflowplugin.api.openflow.statistics.StatisticsContext;

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

    private ContextChain contextChain;

    @Before
    public void setUp() throws Exception {

        Mockito.when(deviceContext.getDeviceInfo()).thenReturn(deviceInfo);
        Mockito.when(deviceContext.stopClusterServices())
                .thenReturn(Futures.immediateFuture(null));
        Mockito.when(rpcContext.stopClusterServices()).thenReturn(Futures.immediateFuture(null));
        Mockito.when(statisticsContext.stopClusterServices()).thenReturn(Futures.immediateFuture(null));
        Mockito.when(statisticsContext.gatherDynamicData()).thenReturn(Futures.immediateFuture(null));
        Mockito.when(connectionContext.getDeviceInfo()).thenReturn(deviceInfo);

        contextChain = new ContextChainImpl(connectionContext);
        contextChain.addContext(statisticsContext);
        contextChain.addContext(rpcContext);
        contextChain.addContext(deviceContext);
        contextChain.addLifecycleService(lifecycleService);

    }

    @Test
    public void stopChain() throws Exception {
        contextChain.stopChain();
        Mockito.verify(deviceContext).stopClusterServices();
        Mockito.verify(rpcContext).stopClusterServices();
        Mockito.verify(statisticsContext).stopClusterServices();
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
    public void connectionDropped() throws Exception {
        contextChain.isMastered(ContextChainMastershipState.INITIAL_GATHERING);
        contextChain.isMastered(ContextChainMastershipState.INITIAL_SUBMIT);
        contextChain.isMastered(ContextChainMastershipState.MASTER_ON_DEVICE);
        contextChain.isMastered(ContextChainMastershipState.INITIAL_FLOW_REGISTRY_FILL);
        contextChain.connectionDropped();
        Mockito.verify(deviceContext).stopClusterServices();
        Mockito.verify(rpcContext).stopClusterServices();
        Mockito.verify(statisticsContext).stopClusterServices();
    }

    @Test
    public void makeDeviceSlave() throws Exception {
        contextChain.makeDeviceSlave();
        Mockito.verify(lifecycleService).makeDeviceSlave(Mockito.any(DeviceContext.class));
    }

}

