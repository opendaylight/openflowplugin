/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.impl.role;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import io.netty.util.HashedWheelTimer;
import java.util.concurrent.Executors;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.opendaylight.openflowplugin.api.OFConstants;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceContext;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceInfo;
import org.opendaylight.openflowplugin.api.openflow.lifecycle.ContextChainMastershipState;
import org.opendaylight.openflowplugin.api.openflow.lifecycle.ContextChainMastershipWatcher;
import org.opendaylight.openflowplugin.api.openflow.role.RoleContext;
import org.opendaylight.openflowplugin.impl.util.DeviceStateUtil;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeRef;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.openflow.provider.config.rev160510.OpenflowProviderConfig;
import org.opendaylight.yang.gen.v1.urn.opendaylight.role.service.rev150727.OfpRole;
import org.opendaylight.yang.gen.v1.urn.opendaylight.role.service.rev150727.SalRoleService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.role.service.rev150727.SetRoleInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.role.service.rev150727.SetRoleOutput;
import org.opendaylight.yangtools.yang.common.RpcResult;

@RunWith(MockitoJUnitRunner.class)
public class RoleContextImplTest {
    // Timeout  after what we will give up on propagating role
    private static final long SET_ROLE_TIMEOUT = 10000;
    @Mock
    private SalRoleService roleService;
    @Mock
    private ContextChainMastershipWatcher contextChainMastershipWatcher;
    @Mock
    private DeviceInfo deviceInfo;
    @Mock
    private DeviceContext deviceContext;
    @Mock
    private ListenableFuture<RpcResult<SetRoleOutput>> setRoleFuture;
    @Mock
    private OpenflowProviderConfig config;
    private RoleContext roleContext;

    @Before
    public void setUp() {
        when(deviceInfo.getNodeInstanceIdentifier()).thenReturn(DeviceStateUtil
                .createNodeInstanceIdentifier(new NodeId("openflow:1")));
        when(deviceInfo.getVersion()).thenReturn(OFConstants.OFP_VERSION_1_3);
        when(roleService.setRole(any())).thenReturn(Futures.immediateFuture(null));

        roleContext = new RoleContextImpl(deviceInfo, new HashedWheelTimer(), 20000, config,
                Executors.newSingleThreadExecutor());
        roleContext.registerMastershipWatcher(contextChainMastershipWatcher);
        roleContext.setRoleService(roleService);
    }

    @After
    public void tearDown() {
        roleContext.close();
    }

    @Test
    public void instantiateServiceInstance() {
        roleContext.instantiateServiceInstance();
        verify(roleService).setRole(new SetRoleInputBuilder()
                .setControllerRole(OfpRole.BECOMEMASTER)
                .setNode(new NodeRef(deviceInfo.getNodeInstanceIdentifier()))
                .build());
        verify(contextChainMastershipWatcher, timeout(SET_ROLE_TIMEOUT)).onMasterRoleAcquired(
                deviceInfo,
                ContextChainMastershipState.MASTER_ON_DEVICE);
    }

    @Test
    public void terminateServiceInstance() throws Exception {
        when(setRoleFuture.isCancelled()).thenReturn(false);
        when(setRoleFuture.isDone()).thenReturn(false);
        when(roleService.setRole(any())).thenReturn(setRoleFuture);
        roleContext.instantiateServiceInstance();
        roleContext.closeServiceInstance().get();
        verify(setRoleFuture).cancel(true);
    }

}
