/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.registry.group;

import java.util.concurrent.atomic.AtomicInteger;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.GroupId;
import org.opendaylight.yangtools.yang.common.Uint32;

/**
 * Test for {@link DeviceGroupRegistryImpl}.
 */
public class DeviceGroupRegistryImplTest {

    private GroupId groupId;
    private GroupId groupId2;
    private DeviceGroupRegistryImpl deviceGroupRegistry;

    @Before
    public void setUp() {
        deviceGroupRegistry = new DeviceGroupRegistryImpl();
        groupId = new GroupId(Uint32.valueOf(42));
        groupId2 = new GroupId(Uint32.valueOf(84));
        Assert.assertEquals(0, deviceGroupRegistry.getAllGroupIds().size());
        deviceGroupRegistry.store(groupId);
        Assert.assertEquals(1, deviceGroupRegistry.getAllGroupIds().size());
    }

    @Test
    public void testStore() {
        deviceGroupRegistry.store(groupId2);
        Assert.assertEquals(2, deviceGroupRegistry.getAllGroupIds().size());
    }

    @Test
    public void testRemoveMarked() {
        deviceGroupRegistry.addMark(groupId);
        deviceGroupRegistry.processMarks();
        Assert.assertEquals(0, deviceGroupRegistry.getAllGroupIds().size());
    }

    @Test
    public void testRemoveMarkedNegative() {
        deviceGroupRegistry.addMark(groupId2);
        deviceGroupRegistry.processMarks();
        Assert.assertEquals(1, deviceGroupRegistry.getAllGroupIds().size());
    }

    @Test
    public void testClose() {
        deviceGroupRegistry.addMark(groupId);
        deviceGroupRegistry.close();

        Assert.assertEquals(0, deviceGroupRegistry.getAllGroupIds().size());
        deviceGroupRegistry.store(groupId);
        Assert.assertEquals(1, deviceGroupRegistry.getAllGroupIds().size());
        deviceGroupRegistry.addMark(groupId);
        deviceGroupRegistry.processMarks();
        Assert.assertEquals(0, deviceGroupRegistry.getAllGroupIds().size());
    }

    @Test
    public void testForEach() {
        final AtomicInteger counter = new AtomicInteger(0);
        deviceGroupRegistry.store(groupId2);
        deviceGroupRegistry.forEach(group -> counter.incrementAndGet());
        Assert.assertEquals(2, counter.get());
    }
}
