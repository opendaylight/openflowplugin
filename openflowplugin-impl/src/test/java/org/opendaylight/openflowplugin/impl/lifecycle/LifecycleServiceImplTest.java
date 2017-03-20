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
import org.opendaylight.openflowplugin.api.openflow.device.DeviceContext;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceInfo;
import org.opendaylight.openflowplugin.api.openflow.device.handlers.ClusterInitializationPhaseHandler;
import org.opendaylight.openflowplugin.api.openflow.device.handlers.DeviceRemovedHandler;
import org.opendaylight.openflowplugin.api.openflow.lifecycle.LifecycleService;
import org.opendaylight.openflowplugin.api.openflow.lifecycle.MastershipChangeListener;
import org.opendaylight.openflowplugin.impl.role.RoleChangeException;

@RunWith(MockitoJUnitRunner.class)
public class LifecycleServiceImplTest {

    private static final String TEST_NODE = "test node";
    private static final ServiceGroupIdentifier SERVICE_GROUP_IDENTIFIER = ServiceGroupIdentifier.create(TEST_NODE);

    @Mock
    private DeviceInfo deviceInfo;
    @Mock
    private DeviceContext deviceContext;
    @Mock
    private ClusterSingletonServiceProvider clusterSingletonServiceProvider;
    @Mock
    private ClusterSingletonServiceRegistration clusterSingletonServiceRegistration;
    @Mock
    private MastershipChangeListener mastershipChangeListener;
    @Mock
    private ClusterInitializationPhaseHandler clusterInitializationPhaseHandler;
    @Mock
    private DeviceRemovedHandler deviceRemovedHandler;

    private LifecycleService lifecycleService;

    @Before
    public void setUp() {
        Mockito.when(deviceContext.getDeviceInfo()).thenReturn(deviceInfo);
        Mockito.when(deviceContext.getServiceIdentifier()).thenReturn(SERVICE_GROUP_IDENTIFIER);
        Mockito.when(clusterSingletonServiceProvider.registerClusterSingletonService(Mockito.any()))
                .thenReturn(clusterSingletonServiceRegistration);

        lifecycleService = new LifecycleServiceImpl(mastershipChangeListener);
        lifecycleService.registerService(
                clusterSingletonServiceProvider,
                deviceContext);
    }

    @Test
    public void getIdentifier() throws Exception {
        Assert.assertEquals(lifecycleService.getIdentifier(), SERVICE_GROUP_IDENTIFIER);
    }

    @Test
    public void makeDeviceSlave() throws Exception {
        Mockito.when(deviceContext.makeDeviceSlave())
                .thenReturn(Futures.immediateFuture(null));
        lifecycleService.makeDeviceSlave(deviceContext);
        Mockito.verify(mastershipChangeListener).onSlaveRoleAcquired(Mockito.any(DeviceInfo.class));
    }

    @Test
    public void makeDeviceSlaveFailure() throws Exception {
        Mockito.when(deviceContext.makeDeviceSlave())
                .thenReturn(Futures.immediateFailedFuture(new RoleChangeException(TEST_NODE)));
        lifecycleService.makeDeviceSlave(deviceContext);
        Mockito.verify(mastershipChangeListener).onSlaveRoleNotAcquired(Mockito.any(DeviceInfo.class));
    }

    @Test
    public void instantiateServiceInstance() throws Exception {
        Mockito.when(deviceContext.onContextInstantiateService(Mockito.any()))
                .thenReturn(true);
        lifecycleService.instantiateServiceInstance();
        Mockito.verify(mastershipChangeListener).onMasterRoleAcquired(Mockito.any(DeviceInfo.class));
    }

    @Test
    public void instantiateServiceInstanceFail() throws Exception {
        Mockito.when(deviceContext.onContextInstantiateService(Mockito.any()))
                .thenReturn(false);
        lifecycleService.instantiateServiceInstance();
        Mockito.verify(mastershipChangeListener).onNotAbleToStartMastership(Mockito.any(DeviceInfo.class));
    }

    @Test
    public void close() throws Exception {
        lifecycleService.registerDeviceRemovedHandler(deviceRemovedHandler);
        lifecycleService.close();
        Mockito.verify(deviceRemovedHandler).onDeviceRemoved(Mockito.any(DeviceInfo.class));
    }

    @Test
    public void closeTwoTimes() throws Exception {
        lifecycleService.registerDeviceRemovedHandler(deviceRemovedHandler);
        lifecycleService.close();
        lifecycleService.close();
        Mockito.verify(deviceRemovedHandler, Mockito.times(1))
                .onDeviceRemoved(Mockito.any(DeviceInfo.class));
    }

    @Test
    public void closeThreeTimes() throws Exception {
        lifecycleService.registerDeviceRemovedHandler(deviceRemovedHandler);
        lifecycleService.close();
        lifecycleService.close();
        Mockito.verify(deviceRemovedHandler, Mockito.times(1))
                .onDeviceRemoved(Mockito.any(DeviceInfo.class));
    }

}