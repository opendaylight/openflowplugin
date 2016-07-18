/**
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.lifecycle;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.opendaylight.mdsal.singleton.common.api.ServiceGroupIdentifier;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceContext;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceInfo;
import org.opendaylight.openflowplugin.api.openflow.lifecycle.LifecycleService;
import org.opendaylight.openflowplugin.api.openflow.role.RoleContext;
import org.opendaylight.openflowplugin.api.openflow.rpc.RpcContext;
import org.opendaylight.openflowplugin.api.openflow.statistics.StatisticsContext;

@RunWith(MockitoJUnitRunner.class)
public class LifecycleServiceImplTest {

    private static final ServiceGroupIdentifier SERVICE_GROUP_IDENTIFIER = ServiceGroupIdentifier.create("test node");
    @Mock
    private DeviceInfo deviceInfo;
    @Mock
    private DeviceContext deviceContext;
    @Mock
    private RpcContext rpcContext;
    @Mock
    private RoleContext roleContext;
    @Mock
    private StatisticsContext statContext;

    private LifecycleService lifecycleService;

    @Before
    public void setUp() {
        lifecycleService = new LifecycleServiceImpl(deviceContext, rpcContext, roleContext, statContext);
        Mockito.when(deviceInfo.getServiceIdentifier()).thenReturn(SERVICE_GROUP_IDENTIFIER);
    }

    @Test
    public void instantiateServiceInstance() throws Exception {
        lifecycleService.instantiateServiceInstance();
        Mockito.verify(deviceContext).startupClusterServices();
    }

    @Test
    public void closeServiceInstance() throws Exception {
        lifecycleService.closeServiceInstance();
        Mockito.verify(deviceContext).stopClusterServices();
    }

}