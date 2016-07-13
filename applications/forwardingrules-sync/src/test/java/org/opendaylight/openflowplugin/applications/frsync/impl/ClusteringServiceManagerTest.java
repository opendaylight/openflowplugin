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
import org.mockito.runners.MockitoJUnitRunner;
import org.opendaylight.openflowplugin.applications.frsync.util.ReconciliationRegistry;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;

/**
 * Test for {@link ClusteringServiceManager}.
 */
@RunWith(MockitoJUnitRunner.class)
public class ClusteringServiceManagerTest {
    private static final NodeId NODE_ID = new NodeId("testNode");
    private ClusteringServiceManager clusteringServiceManager;

    @Before
    public void setUp() throws Exception {
        clusteringServiceManager = new ClusteringServiceManager(new ReconciliationRegistry());
    }

    @Test
    public void testOnDeviceConnectedAndDisconnected() {
        // no context
        Assert.assertNull(clusteringServiceManager.getClusterRegistrations().get(NODE_ID));
        clusteringServiceManager.onDeviceConnected(NODE_ID);
        // create context - register
        Assert.assertEquals(1L, clusteringServiceManager.getClusterRegistrations().size());
        clusteringServiceManager.onDeviceDisconnected(NODE_ID);
        // destroy context - unregister
        Assert.assertNull(clusteringServiceManager.getClusterRegistrations().get(NODE_ID));
    }

    @Test
    public void testIsDeviceMasteredOrSlaved() {
        // no context
        Assert.assertFalse(clusteringServiceManager.isDeviceMastered(NODE_ID));
        clusteringServiceManager.onDeviceConnected(NODE_ID);
        // is master
        clusteringServiceManager.getClusterRegistrations().get(NODE_ID).instantiateServiceInstance();
        Assert.assertTrue(clusteringServiceManager.isDeviceMastered(NODE_ID));
        // is not master
        clusteringServiceManager.getClusterRegistrations().get(NODE_ID).closeServiceInstance();
        Assert.assertFalse(clusteringServiceManager.isDeviceMastered(NODE_ID));
    }

}
