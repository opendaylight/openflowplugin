/*
 * Copyright (c) 2016, 2017 Pantheon Technologies s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.applications.frm.impl;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.opendaylight.mdsal.binding.api.DataBroker;
import org.opendaylight.mdsal.binding.api.RpcProviderService;
import org.opendaylight.mdsal.singleton.common.api.ClusterSingletonService;
import org.opendaylight.mdsal.singleton.common.api.ClusterSingletonServiceProvider;
import org.opendaylight.mdsal.singleton.common.api.ClusterSingletonServiceRegistration;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceInfo;
import org.opendaylight.openflowplugin.api.openflow.mastership.MastershipChangeServiceManager;
import org.opendaylight.openflowplugin.applications.frm.FlowNodeReconciliation;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.openflowplugin.app.frm.reconciliation.service.rev180227.FrmReconciliationService;

/**
 * Test for {@link DeviceMastershipManager}.
 */
@RunWith(MockitoJUnitRunner.class)
public class DeviceMastershipManagerTest {
    private DeviceMastershipManager deviceMastershipManager;
    @Mock
    private ClusterSingletonServiceRegistration registration;
    @Mock
    private ClusterSingletonServiceProvider clusterSingletonService;
    @Mock
    private FlowNodeReconciliation reconciliationAgent;
    @Mock
    private DataBroker dataBroker;
    @Mock
    private MastershipChangeServiceManager mastershipChangeServiceManager;
    @Mock
    private DeviceInfo deviceInfo;
    @Mock
    private NodeId nodeId;
    @Mock
    private RpcProviderService rpcProviderService;
    @Mock
    private FrmReconciliationService reconciliationService;

    @Before
    public void setUp() throws Exception {
        deviceMastershipManager = new DeviceMastershipManager(clusterSingletonService, reconciliationAgent, dataBroker,
                mastershipChangeServiceManager, rpcProviderService, reconciliationService);
        Mockito.lenient().when(clusterSingletonService
                .registerClusterSingletonService(ArgumentMatchers.<ClusterSingletonService>any()))
                .thenReturn(registration);
        Mockito.when(deviceInfo.getNodeId()).thenReturn(nodeId);
        Mockito.when(nodeId.getValue()).thenReturn("dummyValue");
    }

    @Test
    public void testOnDeviceConnectedAndDisconnected() throws Exception {
        // no context
        Assert.assertNull(deviceMastershipManager.getDeviceMasterships().get(deviceInfo.getNodeId()));
        deviceMastershipManager.onBecomeOwner(deviceInfo);
        DeviceMastership serviceInstance = deviceMastershipManager.getDeviceMasterships().get(deviceInfo.getNodeId());
        Assert.assertNotNull(serviceInstance);
        // destroy context - unregister
        Assert.assertNotNull(deviceMastershipManager.getDeviceMasterships().get(deviceInfo.getNodeId()));
        deviceMastershipManager.onLoseOwnership(deviceInfo);
        Assert.assertNull(deviceMastershipManager.getDeviceMasterships().get(deviceInfo.getNodeId()));
    }

    @Test
    public void testIsDeviceMasteredOrSlaved() {
        // no context
        Assert.assertFalse(deviceMastershipManager.isDeviceMastered(deviceInfo.getNodeId()));
        deviceMastershipManager.onBecomeOwner(deviceInfo);
        // is master
        deviceMastershipManager.getDeviceMasterships().get(deviceInfo.getNodeId()).instantiateServiceInstance();
        Assert.assertTrue(deviceMastershipManager.isDeviceMastered(deviceInfo.getNodeId()));
        // is not master
        deviceMastershipManager.getDeviceMasterships().get(deviceInfo.getNodeId()).closeServiceInstance();
        Assert.assertFalse(deviceMastershipManager.isDeviceMastered(deviceInfo.getNodeId()));
    }
}
