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
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.opendaylight.openflowplugin.applications.frsync.util.ReconciliationRegistry;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;

/**
 * Test for {@link ClusterServiceContext}.
 */
@RunWith(MockitoJUnitRunner.class)
public class ClusterServiceContextTest {
    private static final NodeId NODE_ID = new NodeId("testNode");
    private ClusterServiceContext clusterServiceContext;

    @Mock
    private ClusterServiceManager clusterServiceManager;
    @Mock
    private ReconciliationRegistry reconciliationRegistry;

    @Before
    public void setUp() throws Exception {
        clusterServiceContext = new ClusterServiceContext(NODE_ID, reconciliationRegistry);
    }

    @Test
    public void instantiateServiceInstance() {
        clusterServiceContext.instantiateServiceInstance();
        Mockito.verify(reconciliationRegistry).register(NODE_ID);
        Assert.assertTrue(clusterServiceContext.isDeviceMastered());
    }

    @Test
    public void closeServiceInstance() {
        clusterServiceContext.closeServiceInstance();
        Mockito.verify(reconciliationRegistry).unregisterIfRegistered(NODE_ID);
        Assert.assertFalse(clusterServiceContext.isDeviceMastered());
    }
}