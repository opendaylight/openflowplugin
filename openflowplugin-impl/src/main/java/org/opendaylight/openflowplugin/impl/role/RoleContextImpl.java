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
import com.google.common.util.concurrent.ListenableFuture;
import io.netty.util.HashedWheelTimer;
import io.netty.util.Timeout;
import io.netty.util.TimerTask;
import java.util.Collection;
import java.util.HashSet;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.mdsal.singleton.common.api.ServiceGroupIdentifier;
import org.opendaylight.openflowplugin.api.OFConstants;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceInfo;
import org.opendaylight.openflowplugin.api.openflow.device.RequestContext;
import org.opendaylight.openflowplugin.api.openflow.lifecycle.ContextChainMastershipState;
import org.opendaylight.openflowplugin.api.openflow.lifecycle.ContextChainMastershipWatcher;
import org.opendaylight.openflowplugin.api.openflow.role.RoleContext;
import org.opendaylight.openflowplugin.impl.rpc.AbstractRequestContext;
import org.opendaylight.openflowplugin.impl.services.util.RequestContextUtil;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeRef;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.openflow.provider.config.rev160510.OpenflowProviderConfig;
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
    private static final Logger OF_EVENT_LOG = LoggerFactory.getLogger("OfEventLog");

    // Timeout  after what we will give up on propagating role
    private static final long SET_ROLE_TIMEOUT = 10000;

    private final DeviceInfo deviceInfo;
    private final HashedWheelTimer timer;
    private final AtomicReference<ListenableFuture<RpcResult<SetRoleOutput>>> lastRoleFuture = new AtomicReference<>();
    private final Collection<RequestContext<?>> requestContexts = new HashSet<>();
    private final Timeout slaveTask;
    private final OpenflowProviderConfig config;
    private final ExecutorService executorService;
    private ContextChainMastershipWatcher contextChainMastershipWatcher;
    private SalRoleService roleService;

    RoleContextImpl(@NonNull final DeviceInfo deviceInfo,
                    @NonNull final HashedWheelTimer timer,
                    final long checkRoleMasterTimeout,
                    final OpenflowProviderConfig config,
                    final ExecutorService executorService) {
        this.deviceInfo = deviceInfo;
        this.timer = timer;
        this.config = config;
        this.executorService = executorService;
        slaveTask = timer.newTimeout(timerTask -> makeDeviceSlave(), checkRoleMasterTimeout, TimeUnit.MILLISECONDS);

        LOG.info("Started timer for setting SLAVE role on device {} if no role will be set in {}s.",
                deviceInfo,
                checkRoleMasterTimeout / 1000L);
    }

    @Override
    public DeviceInfo getDeviceInfo() {
        return deviceInfo;
    }

    @Override
    public void setRoleService(final SalRoleService salRoleService) {
        roleService = salRoleService;
    }

    @Override
    public void registerMastershipWatcher(@NonNull final ContextChainMastershipWatcher newWatcher) {
        this.contextChainMastershipWatcher = newWatcher;
    }

    @Override
    public void close() {
        changeLastRoleFuture(null);
        requestContexts.forEach(requestContext -> RequestContextUtil
                .closeRequestContextWithRpcError(requestContext, "Connection closed."));
        requestContexts.clear();
    }

    @Override
    public void instantiateServiceInstance() {
        final ListenableFuture<RpcResult<SetRoleOutput>> future = sendRoleChangeToDevice(OfpRole.BECOMEMASTER);
        changeLastRoleFuture(future);
        Futures.addCallback(future, new MasterRoleCallback(), executorService);
    }

    @Override
    public ListenableFuture<Void> closeServiceInstance() {
        changeLastRoleFuture(null);
        return Futures.immediateFuture(null);
    }

    @Override
    public <T> RequestContext<T> createRequestContext() {
        final AbstractRequestContext<T> ret = new AbstractRequestContext<>(deviceInfo.reserveXidForDeviceMessage()) {
            @Override
            public void close() {
                requestContexts.remove(this);
            }
        };

        requestContexts.add(ret);
        return ret;
    }

    @NonNull
    @Override
    public ServiceGroupIdentifier getIdentifier() {
        return deviceInfo.getServiceIdentifier();
    }

    private void changeLastRoleFuture(final ListenableFuture<RpcResult<SetRoleOutput>> newFuture) {
        slaveTask.cancel();
        lastRoleFuture.getAndUpdate(lastFuture -> {
            if (lastFuture != null && !lastFuture.isCancelled() && !lastFuture.isDone()) {
                lastFuture.cancel(true);
            }

            return newFuture;
        });
    }

    private ListenableFuture<RpcResult<SetRoleOutput>> makeDeviceSlave() {
        final ListenableFuture<RpcResult<SetRoleOutput>> future = sendRoleChangeToDevice(OfpRole.BECOMESLAVE);
        changeLastRoleFuture(future);
        Futures.addCallback(future, new SlaveRoleCallback(), executorService);
        return future;
    }

    private ListenableFuture<RpcResult<SetRoleOutput>> sendRoleChangeToDevice(final OfpRole newRole) {
        final Boolean isEqualRole = config.getEnableEqualRole();
        if (isEqualRole) {
            LOG.warn("Skip sending role change request to device {} as user enabled"
                    + " equal role for controller", deviceInfo);
            return Futures.immediateFuture(null);
        }
        LOG.debug("Sending new role {} to device {}", newRole, deviceInfo);

        if (deviceInfo.getVersion() >= OFConstants.OFP_VERSION_1_3) {
            final SetRoleInput setRoleInput = new SetRoleInputBuilder()
                    .setControllerRole(newRole)
                    .setNode(new NodeRef(deviceInfo.getNodeInstanceIdentifier()))
                    .build();

            final ListenableFuture<RpcResult<SetRoleOutput>> setRoleOutputFuture = roleService.setRole(setRoleInput);

            final TimerTask timerTask = timeout -> {
                if (!setRoleOutputFuture.isDone()) {
                    LOG.warn("New role {} was not propagated to device {} during {} sec", newRole,
                            deviceInfo, SET_ROLE_TIMEOUT);
                    setRoleOutputFuture.cancel(true);
                }
            };

            timer.newTimeout(timerTask, SET_ROLE_TIMEOUT, TimeUnit.MILLISECONDS);
            return setRoleOutputFuture;
        }

        LOG.info("Device: {} with version: {} does not support role {}", deviceInfo, deviceInfo.getVersion(), newRole);
        return Futures.immediateFuture(null);
    }

    private final class MasterRoleCallback implements FutureCallback<RpcResult<SetRoleOutput>> {
        @Override
        public void onSuccess(final RpcResult<SetRoleOutput> setRoleOutputRpcResult) {
            contextChainMastershipWatcher.onMasterRoleAcquired(
                    deviceInfo,
                    ContextChainMastershipState.MASTER_ON_DEVICE);
            OF_EVENT_LOG.debug("Master Elected, Node: {}", deviceInfo.getDatapathId());
            LOG.debug("Role MASTER was successfully set on device, node {}", deviceInfo);
        }

        @Override
        public void onFailure(final Throwable throwable) {
            if (!(throwable instanceof CancellationException)) {
                contextChainMastershipWatcher.onNotAbleToStartMastershipMandatory(
                        deviceInfo,
                        "Was not able to propagate MASTER role on device. Error: " + throwable.toString());
            }
        }
    }

    private final class SlaveRoleCallback implements FutureCallback<RpcResult<SetRoleOutput>> {
        @Override
        public void onSuccess(final RpcResult<SetRoleOutput> result) {
            contextChainMastershipWatcher.onSlaveRoleAcquired(deviceInfo);
            LOG.debug("Role SLAVE was successfully set on device, node {}", deviceInfo);
            OF_EVENT_LOG.debug("Role SLAVE was successfully set on device, node {}", deviceInfo);
        }

        @Override
        public void onFailure(final Throwable throwable) {
            if (!(throwable instanceof CancellationException)) {
                contextChainMastershipWatcher.onSlaveRoleNotAcquired(deviceInfo,
                        "Was not able to propagate SLAVE role on device. Error: " + throwable.toString());
            }
        }
    }
}
