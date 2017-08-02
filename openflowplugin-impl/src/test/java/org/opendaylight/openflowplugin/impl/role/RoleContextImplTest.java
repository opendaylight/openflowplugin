/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.impl.role;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

import com.google.common.util.concurrent.Futures;
import io.netty.util.HashedWheelTimer;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.opendaylight.openflowplugin.api.OFConstants;
import org.opendaylight.openflowplugin.api.openflow.connection.ConnectionContext;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceInfo;
import org.opendaylight.openflowplugin.api.openflow.lifecycle.ContextChainMastershipState;
import org.opendaylight.openflowplugin.api.openflow.lifecycle.ContextChainMastershipWatcher;
import org.opendaylight.openflowplugin.api.openflow.role.RoleContext;
import org.opendaylight.openflowplugin.impl.util.DeviceStateUtil;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeRef;
import org.opendaylight.yang.gen.v1.urn.opendaylight.role.service.rev150727.OfpRole;
import org.opendaylight.yang.gen.v1.urn.opendaylight.role.service.rev150727.SalRoleService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.role.service.rev150727.SetRoleInputBuilder;

@RunWith(MockitoJUnitRunner.class)
public class RoleContextImplTest {
    @Mock
    private SalRoleService roleService;
    @Mock
    private ConnectionContext connectionContext;
    @Mock
    private HashedWheelTimer timer;
    @Mock
    private ContextChainMastershipWatcher contextChainMastershipWatcher;
    @Mock
    private DeviceInfo deviceInfo;
    private RoleContext roleContext;

    @Before
    public void setUp() throws Exception {
        when(deviceInfo.getNodeInstanceIdentifier()).thenReturn(DeviceStateUtil
                .createNodeInstanceIdentifier(new NodeId("openflow:1")));
        when(deviceInfo.getVersion()).thenReturn(OFConstants.OFP_VERSION_1_3);
        when(connectionContext.getDeviceInfo()).thenReturn(deviceInfo);
        when(connectionContext.getConnectionState()).thenReturn(ConnectionContext.CONNECTION_STATE.WORKING);
        when(roleService.setRole(any())).thenReturn(Futures.immediateFuture(null));

        roleContext = new RoleContextImpl(connectionContext, roleService, timer);
        roleContext.registerMastershipWatcher(contextChainMastershipWatcher);
    }

    @After
    public void tearDown() throws Exception {
        roleContext.close();
    }

    @Test
    public void makeDeviceSlave() throws Exception {
        roleContext.makeDeviceSlave().get();
        Mockito.verify(roleService).setRole(new SetRoleInputBuilder()
                .setControllerRole(OfpRole.BECOMESLAVE)
                .setNode(new NodeRef(deviceInfo.getNodeInstanceIdentifier()))
                .build());
        Mockito.verify(contextChainMastershipWatcher).onSlaveRoleAcquired(deviceInfo);
    }

    @Test
    public void instantiateServiceInstance() throws Exception {
        roleContext.instantiateServiceInstance();
        Mockito.verify(roleService).setRole(new SetRoleInputBuilder()
                .setControllerRole(OfpRole.BECOMEMASTER)
                .setNode(new NodeRef(deviceInfo.getNodeInstanceIdentifier()))
                .build());
        Mockito.verify(contextChainMastershipWatcher).onMasterRoleAcquired(
                deviceInfo,
                ContextChainMastershipState.MASTER_ON_DEVICE);
    }

    @Test
    public void closeServiceInstance() throws Exception {
        roleContext.closeServiceInstance().get();
        Mockito.verify(roleService).setRole(new SetRoleInputBuilder()
                .setControllerRole(OfpRole.BECOMESLAVE)
                .setNode(new NodeRef(deviceInfo.getNodeInstanceIdentifier()))
                .build());
        Mockito.verify(contextChainMastershipWatcher).onSlaveRoleAcquired(deviceInfo);
    }

}