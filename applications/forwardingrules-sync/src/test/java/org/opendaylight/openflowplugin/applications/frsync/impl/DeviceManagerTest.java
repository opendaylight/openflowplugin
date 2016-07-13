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
 * Test for {@link DeviceManager}.
 */
@RunWith(MockitoJUnitRunner.class)
public class DeviceManagerTest {
    private static final NodeId NODE_ID = new NodeId("testNode");
    private DeviceManager deviceManager;

    @Before
    public void setUp() throws Exception {
        deviceManager = new DeviceManager(new ReconciliationRegistry());
    }

    @Test
    public void testOnDeviceConnectedAndDisconnected() {
        // no context
        Assert.assertNull(deviceManager.getDeviceContexts().get(NODE_ID));
        deviceManager.onDeviceConnected(NODE_ID);
        // create context - register
        Assert.assertEquals(1L, deviceManager.getDeviceContexts().size());
        deviceManager.onDeviceDisconnected(NODE_ID);
        // destroy context - unregister
        Assert.assertNull(deviceManager.getDeviceContexts().get(NODE_ID));
    }

    @Test
    public void testIsDeviceMasteredOrSlaved() {
        // no context
        Assert.assertFalse(deviceManager.isDeviceMastered(NODE_ID));
        deviceManager.onDeviceConnected(NODE_ID);
        // is master
        deviceManager.getDeviceContexts().get(NODE_ID).instantiateServiceInstance();
        Assert.assertTrue(deviceManager.isDeviceMastered(NODE_ID));
        // is not master
        deviceManager.getDeviceContexts().get(NODE_ID).closeServiceInstance();
        Assert.assertFalse(deviceManager.isDeviceMastered(NODE_ID));
    }

}
