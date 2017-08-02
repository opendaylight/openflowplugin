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
import java.util.function.Consumer;
import javax.annotation.Nonnull;
import org.opendaylight.openflowplugin.api.openflow.OFPContext;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceContext;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceInfo;
import org.opendaylight.openflowplugin.api.openflow.role.RoleContext;
import org.opendaylight.openflowplugin.api.openflow.role.RoleManager;
import org.opendaylight.openflowplugin.impl.services.sal.SalRoleServiceImpl;
import org.opendaylight.openflowplugin.impl.util.ItemScheduler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RoleManagerImpl implements RoleManager {
    private static final Logger LOG = LoggerFactory.getLogger(RoleManagerImpl.class);
    private static final long CHECK_ROLE_MASTER_TIMEOUT = 20000L;
    private static final long CHECK_ROLE_MASTER_TOLERANCE = CHECK_ROLE_MASTER_TIMEOUT / 2;

    private final ConcurrentMap<DeviceInfo, RoleContext> contexts = new ConcurrentHashMap<>();
    private final ItemScheduler<DeviceInfo, RoleContext> scheduler;
    private final HashedWheelTimer timer;
    private final Consumer<DeviceInfo> schedulerCloseJob;

    public RoleManagerImpl(final HashedWheelTimer timer) {
        this.timer = timer;
        this.scheduler = new ItemScheduler<>(
                timer,
                CHECK_ROLE_MASTER_TIMEOUT,
                CHECK_ROLE_MASTER_TOLERANCE,
                RoleContext::makeDeviceSlave);

        schedulerCloseJob = scheduler::remove;
    }

    @Override
    public RoleContext createContext(@Nonnull final DeviceContext deviceContext) {
        final DeviceInfo deviceInfo = deviceContext.getDeviceInfo();
        final RoleContextImpl roleContext = new RoleContextImpl(
                deviceContext.getPrimaryConnectionContext(),
                new SalRoleServiceImpl(deviceContext, deviceContext),
                timer,
                schedulerCloseJob);

        contexts.put(deviceInfo, roleContext);
        scheduler.add(deviceInfo, roleContext);
        scheduler.startIfNotRunning();
        LOG.info("Started timer for setting SLAVE role on node {} if no role will be set in {}s.",
                deviceInfo,
                CHECK_ROLE_MASTER_TIMEOUT / 1000L);

        return roleContext;
    }

    @Override
    public void onDeviceRemoved(final DeviceInfo deviceInfo) {
        schedulerCloseJob.accept(deviceInfo);
        contexts.remove(deviceInfo);
    }

    @Override
    public void close() {
        scheduler.close();
        contexts.values().forEach(OFPContext::close);
        contexts.clear();
    }
}