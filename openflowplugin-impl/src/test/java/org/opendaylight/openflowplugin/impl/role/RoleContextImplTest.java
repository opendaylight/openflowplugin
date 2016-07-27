/**
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.role;


import io.netty.util.HashedWheelTimer;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.opendaylight.controller.md.sal.common.api.clustering.CandidateAlreadyRegisteredException;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceInfo;
import org.opendaylight.openflowplugin.api.openflow.lifecycle.LifecycleService;
import org.opendaylight.openflowplugin.api.openflow.role.RoleContext;
import org.opendaylight.openflowplugin.api.openflow.role.RoleManager;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RunWith(MockitoJUnitRunner.class)
public class RoleContextImplTest {

    private static final Logger LOG = LoggerFactory.getLogger(RoleContextImpl.class);

    @Mock
    HashedWheelTimer hashedWheelTimer;
    @Mock
    private DeviceInfo deviceInfo;
    @Mock
    private RoleManager roleManager;
    @Mock
    private LifecycleService lifecycleService;

    private final NodeId nodeId = NodeId.getDefaultInstance("openflow:1");
    private RoleContext roleContext;

    @Before
    public void setup() throws CandidateAlreadyRegisteredException {
        roleContext = new RoleContextImpl(deviceInfo, hashedWheelTimer, roleManager, lifecycleService);
        Mockito.when(deviceInfo.getNodeId()).thenReturn(nodeId);
    }

    @Test
    public void testCreateRequestContext() throws Exception {
        roleContext.createRequestContext();
        Mockito.verify(deviceInfo).reserveXidForDeviceMessage();
    }

    @Test(expected = NullPointerException.class)
    public void testSetSalRoleService() throws Exception {
        roleContext.setSalRoleService(null);
    }

    @Test
    public void testGetNodeId() throws Exception {
        Assert.assertTrue(roleContext.getDeviceInfo().getNodeId().equals(nodeId));
    }
}
