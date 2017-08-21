/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.applications.frm.impl;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceInfo;
import org.opendaylight.openflowplugin.api.openflow.mastership.MastershipChangeRegistration;
import org.opendaylight.openflowplugin.api.openflow.mastership.MastershipChangeServiceManager;
import org.opendaylight.openflowplugin.applications.frm.DeviceMastershipManager;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;

@RunWith(MockitoJUnitRunner.class)
public class DeviceMastershipManagerImplTest {
    @Mock
    private MastershipChangeServiceManager mastershipChangeServiceManager;
    @Mock
    private MastershipChangeRegistration mastershipChangeRegistration;
    @Mock
    private DeviceInfo deviceInfo;
    @Mock
    private NodeId nodeId;

    private DeviceMastershipManager deviceMastershipManager;

    @Before
    public void setUp() throws Exception {
        when(mastershipChangeServiceManager.register(any())).thenReturn(mastershipChangeRegistration);
        when(deviceInfo.getNodeId()).thenReturn(nodeId);
        deviceMastershipManager = new DeviceMastershipManagerImpl(mastershipChangeServiceManager);
    }

    @After
    public void tearDown() throws Exception {
        deviceMastershipManager.close();
    }

    @Test
    public void onBecomeOwner() throws Exception {
        deviceMastershipManager.onBecomeOwner(deviceInfo);
        assertTrue(deviceMastershipManager.isDeviceMastered(nodeId));
    }

    @Test
    public void onLoseOwnership() throws Exception {
        deviceMastershipManager.onBecomeOwner(deviceInfo);
        assertTrue(deviceMastershipManager.isDeviceMastered(nodeId));
        deviceMastershipManager.onLoseOwnership(deviceInfo);
        assertFalse(deviceMastershipManager.isDeviceMastered(nodeId));
    }
}