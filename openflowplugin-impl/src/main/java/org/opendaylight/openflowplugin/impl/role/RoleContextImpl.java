/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.impl.role;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.JdkFutureAdapters;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.MoreExecutors;
import io.netty.util.HashedWheelTimer;
import io.netty.util.Timeout;
import io.netty.util.TimerTask;
import java.util.Objects;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.opendaylight.mdsal.singleton.common.api.ServiceGroupIdentifier;
import org.opendaylight.openflowplugin.api.OFConstants;
import org.opendaylight.openflowplugin.api.openflow.connection.ConnectionContext;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceInfo;
import org.opendaylight.openflowplugin.api.openflow.lifecycle.ContextChainMastershipState;
import org.opendaylight.openflowplugin.api.openflow.lifecycle.ContextChainMastershipWatcher;
import org.opendaylight.openflowplugin.api.openflow.role.RoleContext;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeRef;
import org.opendaylight.yang.gen.v1.urn.opendaylight.role.service.rev150727.OfpRole;
import org.opendaylight.yang.gen.v1.urn.opendaylight.role.service.rev150727.SalRoleService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.role.service.rev150727.SetRoleInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.role.service.rev150727.SetRoleInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.role.service.rev150727.SetRoleOutput;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RoleContextImpl implements RoleContext {
    private static final Logger LOG = LoggerFactory.getLogger(RoleContextImpl.class);

    // Timeout  after what we will give up on propagating role
    private static final long SET_ROLE_TIMEOUT = 10000;

    // Timeout after what we will give up on waiting for master role
    private static final long CHECK_ROLE_MASTER_TIMEOUT = 20000;

    private final DeviceInfo deviceInfo;
    private final SalRoleService roleService;
    private final HashedWheelTimer timer;
    private final ConnectionContext connectionContext;
    private final AtomicReference<ListenableFuture<RpcResult<SetRoleOutput>>> lastRoleFuture = new AtomicReference<>();
    private final Timeout slaveTask;
    private ContextChainMastershipWatcher contextChainMastershipWatcher;

    RoleContextImpl(@Nonnull final ConnectionContext connectionContext,
                    @Nonnull final SalRoleService roleService,
                    @Nonnull final HashedWheelTimer timer) {
        this.connectionContext = connectionContext;
        this.deviceInfo = connectionContext.getDeviceInfo();
        this.roleService = roleService;
        this.timer = timer;
        slaveTask = timer.newTimeout((t) -> makeDeviceSlave(), CHECK_ROLE_MASTER_TIMEOUT, TimeUnit.MILLISECONDS);

        LOG.info("Started timer for setting SLAVE role on device {} if no role will be set in {}s.",
                deviceInfo,
                CHECK_ROLE_MASTER_TIMEOUT / 1000L);
    }

    @Override
    public DeviceInfo getDeviceInfo() {
        return deviceInfo;
    }

    @Override
    public void registerMastershipWatcher(@Nonnull final ContextChainMastershipWatcher contextChainMastershipWatcher) {
        this.contextChainMastershipWatcher = contextChainMastershipWatcher;
    }

    @Override
    public void close() {
        slaveTask.cancel();
        changeLastRoleFuture(null);
    }

    @Override
    public void instantiateServiceInstance() {
        final ListenableFuture<RpcResult<SetRoleOutput>> future = sendRoleChangeToDevice(OfpRole.BECOMEMASTER);
        changeLastRoleFuture(future);
        Futures.addCallback(future, new MasterRoleCallback(), MoreExecutors.directExecutor());
    }

    @Override
    public ListenableFuture<Void> closeServiceInstance() {
        return Futures.transform(makeDeviceSlave(), (result) -> null, MoreExecutors.directExecutor());
    }

    @Nonnull
    @Override
    public ServiceGroupIdentifier getIdentifier() {
        return deviceInfo.getServiceIdentifier();
    }

    private void changeLastRoleFuture(final ListenableFuture<RpcResult<SetRoleOutput>> newFuture) {
        lastRoleFuture.getAndUpdate(lastFuture -> {
            if (Objects.nonNull(lastFuture) && !lastFuture.isCancelled() && !lastFuture.isDone()) {
                lastFuture.cancel(true);
            }

            return newFuture;
        });
    }

    private ListenableFuture<RpcResult<SetRoleOutput>> makeDeviceSlave() {
        final ListenableFuture<RpcResult<SetRoleOutput>> future = sendRoleChangeToDevice(OfpRole.BECOMESLAVE);
        changeLastRoleFuture(future);
        Futures.addCallback(future, new SlaveRoleCallback(), MoreExecutors.directExecutor());
        return future;
    }

    private ListenableFuture<RpcResult<SetRoleOutput>> sendRoleChangeToDevice(final OfpRole newRole) {
        if (ConnectionContext.CONNECTION_STATE.RIP.equals(connectionContext.getConnectionState())) {
            return Futures.immediateFuture(null);
        }

        LOG.debug("Sending new role {} to device {}", newRole, deviceInfo);

        if (deviceInfo.getVersion() >= OFConstants.OFP_VERSION_1_3) {
            final SetRoleInput setRoleInput = new SetRoleInputBuilder()
                    .setControllerRole(newRole)
                    .setNode(new NodeRef(deviceInfo.getNodeInstanceIdentifier()))
                    .build();

            final Future<RpcResult<SetRoleOutput>> setRoleOutputFuture = roleService.setRole(setRoleInput);

            final TimerTask timerTask = timeout -> {
                if (!setRoleOutputFuture.isDone()) {
                    LOG.warn("New role {} was not propagated to device {} during {} sec", newRole,
                            deviceInfo, SET_ROLE_TIMEOUT);
                    setRoleOutputFuture.cancel(true);
                }
            };

            timer.newTimeout(timerTask, SET_ROLE_TIMEOUT, TimeUnit.MILLISECONDS);
            return JdkFutureAdapters.listenInPoolThread(setRoleOutputFuture);
        }

        LOG.info("Device: {} with version: {} does not support role {}", deviceInfo, deviceInfo.getVersion(), newRole);
        return Futures.immediateFuture(null);
    }

    private final class MasterRoleCallback implements FutureCallback<RpcResult<SetRoleOutput>> {
        @Override
        public void onSuccess(@Nullable RpcResult<SetRoleOutput> setRoleOutputRpcResult) {
            slaveTask.cancel();
            contextChainMastershipWatcher.onMasterRoleAcquired(
                    deviceInfo,
                    ContextChainMastershipState.MASTER_ON_DEVICE);
            LOG.debug("Role MASTER was successfully set on device, node {}", deviceInfo);
        }

        @Override
        public void onFailure(@Nonnull final Throwable throwable) {
            slaveTask.cancel();
            contextChainMastershipWatcher.onNotAbleToStartMastershipMandatory(
                    deviceInfo,
                    "Was not able to set MASTER role on device");
        }
    }

    private final class SlaveRoleCallback implements FutureCallback<RpcResult<SetRoleOutput>> {
        @Override
        public void onSuccess(@Nullable final RpcResult<SetRoleOutput> result) {
            slaveTask.cancel();
            contextChainMastershipWatcher.onSlaveRoleAcquired(deviceInfo);
        }

        @Override
        public void onFailure(@Nonnull final Throwable t) {
            slaveTask.cancel();
            contextChainMastershipWatcher.onSlaveRoleNotAcquired(deviceInfo);
        }
    }
}