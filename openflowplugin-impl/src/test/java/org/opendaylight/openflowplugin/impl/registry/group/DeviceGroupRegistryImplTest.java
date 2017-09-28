/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.impl.registry.group;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.GroupId;

/**
 * Test for {@link DeviceGroupRegistryImpl}.
 */
public class DeviceGroupRegistryImplTest {

    private GroupId groupId;
    private GroupId groupId2;
    private DeviceGroupRegistryImpl deviceGroupRegistry;

    @Before
    public void setUp() throws Exception {
        deviceGroupRegistry = new DeviceGroupRegistryImpl();
        groupId = new GroupId(42L);
        groupId2 = new GroupId(84L);
        Assert.assertEquals(0, deviceGroupRegistry.getAllGroupIds().size());
        deviceGroupRegistry.store(groupId);
        Assert.assertEquals(1, deviceGroupRegistry.getAllGroupIds().size());
    }

    @Test
    public void testStore() throws Exception {
        deviceGroupRegistry.store(groupId2);
        Assert.assertEquals(2, deviceGroupRegistry.getAllGroupIds().size());
    }

    @Test
    public void testRemoveMarked() throws Exception {
        deviceGroupRegistry.addMark(groupId);
        deviceGroupRegistry.processMarks();
        Assert.assertEquals(0, deviceGroupRegistry.getAllGroupIds().size());
    }

    @Test
    public void testClose() throws Exception {
        deviceGroupRegistry.addMark(groupId);
        deviceGroupRegistry.close();

        Assert.assertEquals(0, deviceGroupRegistry.getAllGroupIds().size());
        deviceGroupRegistry.store(groupId2);
        Assert.assertEquals(1, deviceGroupRegistry.getAllGroupIds().size());
        deviceGroupRegistry.addMark(groupId2);
        deviceGroupRegistry.processMarks();
        Assert.assertEquals(0, deviceGroupRegistry.getAllGroupIds().size());

    }
}
