/**
 * Copyright (c) 2016 Cisco Systems, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.applications.frsync.util;

import java.util.Date;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;

/**
 * Test for {@link ReconciliationRegistry}.
 */
@RunWith(MockitoJUnitRunner.class)
public class ReconciliationRegistryTest {

    private static final NodeId NODE_ID = new NodeId("testNode");
    private ReconciliationRegistry reconciliationRegistry;

    @Before
    public void setUp() throws Exception {
        reconciliationRegistry = new ReconciliationRegistry();
    }

    @Test
    public void testRegister() {
        Date timestamp = reconciliationRegistry.register(NODE_ID);
        Assert.assertEquals(true, reconciliationRegistry.isRegistered(NODE_ID));
        Assert.assertNotNull(timestamp);
    }

    @Test
    public void testUnregisterIfRegistered() {
        reconciliationRegistry.register(NODE_ID);
        Date timestamp = reconciliationRegistry.unregisterIfRegistered(NODE_ID);
        Assert.assertEquals(false, reconciliationRegistry.isRegistered(NODE_ID));
        Assert.assertNotNull(timestamp);
    }

    @Test
    public void testUnregisterIfNotRegistered() {
        Date timestamp = reconciliationRegistry.unregisterIfRegistered(NODE_ID);
        Assert.assertEquals(false, reconciliationRegistry.isRegistered(NODE_ID));
        Assert.assertNull(timestamp);
    }

}
