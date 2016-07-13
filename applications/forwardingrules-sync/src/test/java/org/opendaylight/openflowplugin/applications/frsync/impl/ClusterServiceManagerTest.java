/**
 * Copyright (c) 2016 Cisco Systems, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.applications.frsync.impl;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.opendaylight.mdsal.singleton.common.api.ClusterSingletonService;
import org.opendaylight.mdsal.singleton.common.api.ClusterSingletonServiceRegistration;
import org.opendaylight.mdsal.singleton.dom.api.DOMClusterSingletonServiceProvider;
import org.opendaylight.openflowplugin.applications.frsync.util.ReconciliationRegistry;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;

/**
 * Test for {@link ClusterServiceManager}.
 */
@RunWith(MockitoJUnitRunner.class)
public class ClusterServiceManagerTest {
    private static final NodeId NODE_ID = new NodeId("testNode");
    private ClusterServiceManager clusterServiceManager;
    @Mock
    private ClusterSingletonServiceRegistration registration;
    @Mock
    private DOMClusterSingletonServiceProvider clusterSingletonService;

    @Before
    public void setUp() throws Exception {
        clusterServiceManager = new ClusterServiceManager(clusterSingletonService, new ReconciliationRegistry());
        Mockito.when(clusterSingletonService.registerClusterSingletonService(Matchers.<ClusterSingletonService>any()))
                .thenReturn(registration);
    }

    @Test
    public void testOnDeviceConnectedAndDisconnected() {
        // no context
        Assert.assertNull(clusterServiceManager.getContexts().get(NODE_ID));
        // create context - register
        clusterServiceManager.onDeviceConnected(NODE_ID);
        ClusterServiceContext registration = clusterServiceManager.getContexts().get(NODE_ID);
        Assert.assertNotNull(registration);
        Mockito.verify(clusterSingletonService).registerClusterSingletonService(registration);
        // destroy context - unregister
        clusterServiceManager.onDeviceDisconnected(NODE_ID);
        Assert.assertNull(clusterServiceManager.getContexts().get(NODE_ID));
    }

    @Test
    public void testIsDeviceMasteredOrSlaved() {
        // no context
        Assert.assertFalse(clusterServiceManager.isDeviceMastered(NODE_ID));
        clusterServiceManager.onDeviceConnected(NODE_ID);
        // is master
        clusterServiceManager.getContexts().get(NODE_ID).instantiateServiceInstance();
        Assert.assertTrue(clusterServiceManager.isDeviceMastered(NODE_ID));
        // is not master
        clusterServiceManager.getContexts().get(NODE_ID).closeServiceInstance();
        Assert.assertFalse(clusterServiceManager.isDeviceMastered(NODE_ID));
    }

}
