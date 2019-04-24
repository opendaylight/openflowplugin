/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.impl.role;

import io.netty.util.HashedWheelTimer;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.openflowplugin.api.openflow.OFPContext;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceContext;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceInfo;
import org.opendaylight.openflowplugin.api.openflow.role.RoleContext;
import org.opendaylight.openflowplugin.api.openflow.role.RoleManager;
import org.opendaylight.openflowplugin.impl.services.sal.SalRoleServiceImpl;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.openflow.provider.config.rev160510.OpenflowProviderConfig;

public class RoleManagerImpl implements RoleManager {
    // Timeout after what we will give up on waiting for master role
    private static final long CHECK_ROLE_MASTER_TIMEOUT = 20000;

    private final ConcurrentMap<DeviceInfo, RoleContext> contexts = new ConcurrentHashMap<>();
    private final HashedWheelTimer timer;
    private final OpenflowProviderConfig config;
    private final ExecutorService executorService;

    public RoleManagerImpl(final HashedWheelTimer timer,
                           final OpenflowProviderConfig config,
                           final ExecutorService executorService) {
        this.timer = timer;
        this.config = config;
        this.executorService = executorService;
    }

    @Override
    public RoleContext createContext(@NonNull final DeviceContext deviceContext) {
        final DeviceInfo deviceInfo = deviceContext.getDeviceInfo();
        final RoleContextImpl roleContext = new RoleContextImpl(
                deviceContext.getDeviceInfo(),
                timer, CHECK_ROLE_MASTER_TIMEOUT, config, executorService);

        roleContext.setRoleService(new SalRoleServiceImpl(roleContext, deviceContext));
        contexts.put(deviceInfo, roleContext);
        return roleContext;
    }

    @Override
    public void onDeviceRemoved(final DeviceInfo deviceInfo) {
        contexts.remove(deviceInfo);
    }

    @Override
    public void close() {
        contexts.values().forEach(OFPContext::close);
        contexts.clear();
    }
}