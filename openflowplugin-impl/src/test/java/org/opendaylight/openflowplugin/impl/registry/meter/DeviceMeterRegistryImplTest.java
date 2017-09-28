/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.impl.registry.meter;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.MeterId;

/**
 * Test for {@link DeviceMeterRegistryImpl}.
 */
public class DeviceMeterRegistryImplTest {

    private MeterId meterId;
    private MeterId meterId2;
    private DeviceMeterRegistryImpl deviceMeterRegistry;

    @Before
    public void setUp() throws Exception {
        deviceMeterRegistry = new DeviceMeterRegistryImpl();
        meterId = new MeterId(42L);
        meterId2 = new MeterId(84L);
        Assert.assertEquals(0, deviceMeterRegistry.getAllMeterIds().size());
        deviceMeterRegistry.store(meterId);
        Assert.assertEquals(1, deviceMeterRegistry.getAllMeterIds().size());
    }

    @Test
    public void testStore() throws Exception {
        deviceMeterRegistry.store(meterId2);
        Assert.assertEquals(2, deviceMeterRegistry.getAllMeterIds().size());
    }

    @Test
    public void testRemoveMarked() throws Exception {
        deviceMeterRegistry.addMark(meterId);
        deviceMeterRegistry.processMarks();
        Assert.assertEquals(0, deviceMeterRegistry.getAllMeterIds().size());
    }


    @Test
    public void testClose() throws Exception {
        deviceMeterRegistry.addMark(meterId);
        deviceMeterRegistry.close();

        Assert.assertEquals(0, deviceMeterRegistry.getAllMeterIds().size());
        deviceMeterRegistry.store(meterId2);
        Assert.assertEquals(1, deviceMeterRegistry.getAllMeterIds().size());
        deviceMeterRegistry.addMark(meterId2);
        deviceMeterRegistry.processMarks();
        Assert.assertEquals(0, deviceMeterRegistry.getAllMeterIds().size());

    }
}
