/**
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
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.opendaylight.controller.sal.binding.api.BindingAwareBroker.RoutedRpcRegistration;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;

/**
 * Test for {@link DeviceMastership}.
 */
@RunWith(MockitoJUnitRunner.class)
public class DeviceMastershipTest {
    private static final NodeId NODE_ID = new NodeId("testNode");
    private DeviceMastership deviceMastership;
    @Mock
    private RoutedRpcRegistration routedRpcRegistration;

    @Before
    public void setUp() throws Exception {
        deviceMastership = new DeviceMastership(NODE_ID, routedRpcRegistration);
    }

    @Test
    public void testInstantiateServiceInstance() {
        deviceMastership.instantiateServiceInstance();
        Assert.assertTrue(deviceMastership.isDeviceMastered());
    }

    @Test
    public void testCloseServiceInstance() {
        deviceMastership.closeServiceInstance();
        Assert.assertFalse(deviceMastership.isDeviceMastered());
    }
}