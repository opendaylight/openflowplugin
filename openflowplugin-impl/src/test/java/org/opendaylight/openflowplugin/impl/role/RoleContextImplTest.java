/**
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.role;


import com.google.common.util.concurrent.Futures;
import io.netty.util.HashedWheelTimer;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.opendaylight.controller.md.sal.common.api.clustering.CandidateAlreadyRegisteredException;
import org.opendaylight.openflowplugin.api.OFConstants;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceInfo;
import org.opendaylight.openflowplugin.api.openflow.lifecycle.LifecycleService;
import org.opendaylight.openflowplugin.api.openflow.role.RoleContext;
import org.opendaylight.openflowplugin.api.openflow.role.RoleManager;
import org.opendaylight.openflowplugin.impl.util.DeviceStateUtil;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.role.service.rev150727.OfpRole;
import org.opendaylight.yang.gen.v1.urn.opendaylight.role.service.rev150727.SalRoleService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.role.service.rev150727.SetRoleInput;

@RunWith(MockitoJUnitRunner.class)
public class RoleContextImplTest {

    @Mock
    HashedWheelTimer hashedWheelTimer;
    @Mock
    private DeviceInfo deviceInfo;
    @Mock
    private RoleManager roleManager;
    @Mock
    private LifecycleService lifecycleService;
    @Mock
    private SalRoleService salRoleService;

    private final NodeId nodeId = NodeId.getDefaultInstance("openflow:1");
    private RoleContext roleContext;
    private RoleContextImpl roleContextSpy;

    @Before
    public void setup() throws CandidateAlreadyRegisteredException {
        roleContext = new RoleContextImpl(deviceInfo, hashedWheelTimer, roleManager, lifecycleService);
        roleContext.setSalRoleService(salRoleService);
        Mockito.when(deviceInfo.getNodeId()).thenReturn(nodeId);
        Mockito.when(salRoleService.setRole(Mockito.<SetRoleInput>any())).thenReturn(Futures.immediateFuture(null));
        Mockito.when(deviceInfo.getNodeInstanceIdentifier()).thenReturn(DeviceStateUtil.createNodeInstanceIdentifier(nodeId));
        roleContextSpy = Mockito.spy((RoleContextImpl) roleContext);
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

    @Test
    public void startupClusterServices() throws Exception {
        roleContextSpy.startupClusterServices();
        Mockito.verify(roleContextSpy).sendRoleChangeToDevice(OfpRole.BECOMEMASTER);
    }

    @Test
    public void startupClusterServicesVersion10() throws Exception {
        Mockito.when(deviceInfo.getVersion()).thenReturn(OFConstants.OFP_VERSION_1_0);
        roleContextSpy.startupClusterServices();
        Mockito.verify(roleContextSpy).sendRoleChangeToDevice(OfpRole.BECOMEMASTER);
    }

    @Test
    public void startupClusterServicesVersion13() throws Exception {
        Mockito.when(deviceInfo.getVersion()).thenReturn(OFConstants.OFP_VERSION_1_3);
        roleContextSpy.startupClusterServices();
        Mockito.verify(roleContextSpy).sendRoleChangeToDevice(OfpRole.BECOMEMASTER);
    }

    @Test
    public void stopClusterServicesNotDisconnected() throws Exception {
        roleContextSpy.stopClusterServices(false);
        Mockito.verify(roleContextSpy).sendRoleChangeToDevice(OfpRole.BECOMESLAVE);
        Mockito.verify(roleManager, Mockito.never()).removeDeviceFromOperationalDS(Mockito.<DeviceInfo>any());
    }

    @Test
    public void stopClusterServicesDisconnected() throws Exception {
        roleContextSpy.stopClusterServices(true);
        Mockito.verify(roleManager, Mockito.atLeastOnce()).removeDeviceFromOperationalDS(Mockito.<DeviceInfo>any());
    }

    @Test
    public void makeDeviceSlave() throws Exception {
        roleContextSpy.makeDeviceSlave();
        Mockito.verify(roleContextSpy).sendRoleChangeToDevice(OfpRole.BECOMESLAVE);
    }

}
