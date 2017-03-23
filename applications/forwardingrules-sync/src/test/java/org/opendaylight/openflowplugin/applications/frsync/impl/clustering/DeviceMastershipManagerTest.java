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
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceInfo;
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
    private ReconciliationRegistry reconciliationRegistry;
    @Mock
    private DeviceInfo deviceInfo;

    @Before
    public void setUp() throws Exception {
        deviceMastershipManager = new DeviceMastershipManager(reconciliationRegistry);
        Mockito.when(deviceInfo.getNodeId()).thenReturn(NODE_ID);
    }

    @Test
    public void testOnDeviceConnectedAndDisconnected() throws Exception {
        // no context
        Assert.assertFalse(deviceMastershipManager.getDeviceMasterships().contains(NODE_ID));
        // create context - register
        deviceMastershipManager.onBecomeOwner(deviceInfo);
        Assert.assertTrue(deviceMastershipManager.getDeviceMasterships().contains(NODE_ID));
        Mockito.verify(reconciliationRegistry).register(NODE_ID);
        // destroy context - unregister
        deviceMastershipManager.onLoseOwnership(deviceInfo);
        Assert.assertFalse(deviceMastershipManager.getDeviceMasterships().contains(NODE_ID));
        Mockito.verify(reconciliationRegistry).unregisterIfRegistered(NODE_ID);
    }

    @Test
    public void testIsDeviceMasteredOrSlaved() {
        // no context
        Assert.assertFalse(deviceMastershipManager.isDeviceMastered(NODE_ID));
        deviceMastershipManager.onBecomeOwner(deviceInfo);
        // is master
        Assert.assertTrue(deviceMastershipManager.isDeviceMastered(NODE_ID));
        // is not master
        deviceMastershipManager.onLoseOwnership(deviceInfo);
        Assert.assertFalse(deviceMastershipManager.isDeviceMastered(NODE_ID));
    }

}
