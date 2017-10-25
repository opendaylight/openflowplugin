/**
 * Copyright (c) 2016 Cisco Systems, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.applications.frsync.impl.clustering;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.opendaylight.mdsal.singleton.common.api.ClusterSingletonService;
import org.opendaylight.mdsal.singleton.common.api.ClusterSingletonServiceProvider;
import org.opendaylight.mdsal.singleton.common.api.ClusterSingletonServiceRegistration;
import org.opendaylight.openflowplugin.applications.frsync.util.ReconciliationRegistry;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;

/**
 * Test for {@link DeviceMastershipManager}.
 */
@RunWith(MockitoJUnitRunner.class)
public class DeviceMastershipManagerTest {
    private static final NodeId NODE_ID = new NodeId("testNode");
    private DeviceMastershipManager deviceMastershipManager;
    @Mock
    private ClusterSingletonServiceRegistration registration;
    @Mock
    private ClusterSingletonServiceProvider clusterSingletonService;

    @Before
    public void setUp() throws Exception {
        deviceMastershipManager = new DeviceMastershipManager(clusterSingletonService, new ReconciliationRegistry());
        Mockito.when(clusterSingletonService.registerClusterSingletonService(Matchers.<ClusterSingletonService>any()))
                .thenReturn(registration);
    }

    @Test
    public void testOnDeviceConnectedAndDisconnected() throws Exception {
        // no context
        Assert.assertNull(deviceMastershipManager.getDeviceMasterships().get(NODE_ID));
        // create context - register
        deviceMastershipManager.onDeviceConnected(NODE_ID);
        DeviceMastership serviceInstance = deviceMastershipManager.getDeviceMasterships().get(NODE_ID);
        Assert.assertNotNull(serviceInstance);
        Mockito.verify(clusterSingletonService).registerClusterSingletonService(serviceInstance);
        // destroy context - unregister
        deviceMastershipManager.onDeviceDisconnected(NODE_ID);
        Assert.assertNull(deviceMastershipManager.getDeviceMasterships().get(NODE_ID));
        Mockito.verify(registration).close();
    }

    @Test
    public void testIsDeviceMasteredOrSlaved() {
        // no context
        Assert.assertFalse(deviceMastershipManager.isDeviceMastered(NODE_ID));
        deviceMastershipManager.onDeviceConnected(NODE_ID);
        // is master
        deviceMastershipManager.getDeviceMasterships().get(NODE_ID).instantiateServiceInstance();
        Assert.assertTrue(deviceMastershipManager.isDeviceMastered(NODE_ID));
        // is not master
        deviceMastershipManager.getDeviceMasterships().get(NODE_ID).closeServiceInstance();
        Assert.assertFalse(deviceMastershipManager.isDeviceMastered(NODE_ID));
    }

}
