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
import org.opendaylight.mdsal.singleton.common.api.ClusterSingletonServiceProvider;
import org.opendaylight.openflowplugin.applications.frsync.util.ReconciliationRegistry;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;

/**
 * Test for {@link DeviceMastership}.
 */
@RunWith(MockitoJUnitRunner.class)
public class DeviceMastershipTest {
    private static final NodeId NODE_ID = new NodeId("testNode");
    private DeviceMastership deviceMastership;

    @Mock
    private DeviceMastershipManager deviceMastershipManager;
    @Mock
    private ReconciliationRegistry reconciliationRegistry;

    @Before
    public void setUp() throws Exception {
        deviceMastership = new DeviceMastership(NODE_ID, reconciliationRegistry, Mockito.mock(ClusterSingletonServiceProvider.class));
    }

    @Test
    public void testInstantiateServiceInstance() {
        deviceMastership.instantiateServiceInstance();
        Mockito.verify(reconciliationRegistry).register(NODE_ID);
        Assert.assertTrue(deviceMastership.isDeviceMastered());
    }

    @Test
    public void testCloseServiceInstance() {
        deviceMastership.closeServiceInstance();
        Mockito.verify(reconciliationRegistry).unregisterIfRegistered(NODE_ID);
        Assert.assertFalse(deviceMastership.isDeviceMastered());
    }

}