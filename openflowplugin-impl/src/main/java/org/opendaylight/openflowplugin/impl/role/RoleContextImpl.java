/**
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.role;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.JdkFutureAdapters;
import com.google.common.util.concurrent.ListenableFuture;
import io.netty.util.HashedWheelTimer;
import io.netty.util.TimerTask;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.opendaylight.mdsal.singleton.common.api.ServiceGroupIdentifier;
import org.opendaylight.openflowplugin.api.OFConstants;
import org.opendaylight.openflowplugin.api.openflow.connection.ConnectionContext;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceInfo;
import org.opendaylight.openflowplugin.api.openflow.device.RequestContext;
import org.opendaylight.openflowplugin.api.openflow.device.handlers.ClusterInitializationPhaseHandler;
import org.opendaylight.openflowplugin.api.openflow.role.RoleContext;
import org.opendaylight.openflowplugin.api.openflow.role.RoleManager;
import org.opendaylight.openflowplugin.impl.rpc.AbstractRequestContext;
import org.opendaylight.openflowplugin.impl.util.DeviceStateUtil;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeRef;
import org.opendaylight.yang.gen.v1.urn.opendaylight.role.service.rev150727.OfpRole;
import org.opendaylight.yang.gen.v1.urn.opendaylight.role.service.rev150727.SalRoleService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.role.service.rev150727.SetRoleInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.role.service.rev150727.SetRoleInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.role.service.rev150727.SetRoleOutput;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Role context try to make change device role on device
 */
class RoleContextImpl implements RoleContext {

    private static final Logger LOG = LoggerFactory.getLogger(RoleContextImpl.class);
    // Maximum limit of timeout retries when cleaning DS, to prevent infinite recursive loops
    private static final int MAX_CLEAN_DS_RETRIES = 3;

    private SalRoleService salRoleService = null;
    private final HashedWheelTimer hashedWheelTimer;
    private final DeviceInfo deviceInfo;
    private CONTEXT_STATE state;
    private final RoleManager myManager;
    private ClusterInitializationPhaseHandler clusterInitializationPhaseHandler;

    RoleContextImpl(final DeviceInfo deviceInfo,
                    final HashedWheelTimer hashedWheelTimer,
                    final RoleManager myManager) {
        this.deviceInfo = deviceInfo;
        state = CONTEXT_STATE.WORKING;
        this.myManager = myManager;
        this.hashedWheelTimer = hashedWheelTimer;
    }

    @Nullable
    @Override
    public <T> RequestContext<T> createRequestContext() {
        return new AbstractRequestContext<T>(deviceInfo.reserveXidForDeviceMessage()) {
            @Override
            public void close() {
            }
        };
    }

    @Override
    public void setSalRoleService(@Nonnull final SalRoleService salRoleService) {
        Preconditions.checkNotNull(salRoleService);
        this.salRoleService = salRoleService;
    }

    @Override
    public CONTEXT_STATE getState() {
        return this.state;
    }

    @Override
    public void setState(CONTEXT_STATE state) {
        this.state = state;
    }

    @Override
    public ServiceGroupIdentifier getServiceIdentifier() {
        return this.deviceInfo.getServiceIdentifier();
    }

    @Override
    public DeviceInfo getDeviceInfo() {
        return this.deviceInfo;
    }

    public void startupClusterServices() throws ExecutionException, InterruptedException {
        Futures.addCallback(sendRoleChangeToDevice(OfpRole.BECOMEMASTER), new FutureCallback<RpcResult<SetRoleOutput>>() {
            @Override
            public void onSuccess(@Nullable RpcResult<SetRoleOutput> setRoleOutputRpcResult) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Role MASTER was successfully set on device, node {}", deviceInfo.getLOGValue());
                }
            }

            @Override
            public void onFailure(final Throwable throwable) {
                LOG.warn("Was not able to set MASTER role on device, node {}", deviceInfo.getLOGValue());
            }
        });
    }

    @Override
    public ListenableFuture<Void> stopClusterServices(final boolean deviceDisconnected) {

        if (!deviceDisconnected) {
            ListenableFuture<Void> future = Futures.transform(makeDeviceSlave(), new Function<RpcResult<SetRoleOutput>, Void>() {
                @Nullable
                @Override
                public Void apply(@Nullable RpcResult<SetRoleOutput> setRoleOutputRpcResult) {
                    return null;
                }
            });

            Futures.addCallback(future, new FutureCallback<Void>() {
                @Override
                public void onSuccess(@Nullable Void aVoid) {
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("Role SLAVE was successfully propagated on device, node {}", deviceInfo.getLOGValue());
                    }
                }

                @Override
                public void onFailure(final Throwable throwable) {
                    LOG.warn("Was not able to set role SLAVE to device on node {} ", deviceInfo.getLOGValue());
                    LOG.trace("Error occurred on device role setting, probably connection loss: ", throwable);
                    myManager.removeDeviceFromOperationalDS(deviceInfo, MAX_CLEAN_DS_RETRIES);

                }
            });
            return future;
        } else {
            return myManager.removeDeviceFromOperationalDS(deviceInfo, MAX_CLEAN_DS_RETRIES);
        }
    }

    @Override
    public ListenableFuture<RpcResult<SetRoleOutput>> makeDeviceSlave(){
        return sendRoleChangeToDevice(OfpRole.BECOMESLAVE);
    }

    @VisibleForTesting
    ListenableFuture<RpcResult<SetRoleOutput>> sendRoleChangeToDevice(final OfpRole newRole) {
        LOG.debug("Sending new role {} to device {}", newRole, deviceInfo.getNodeId());
        final Future<RpcResult<SetRoleOutput>> setRoleOutputFuture;
        final Short version = deviceInfo.getVersion();
        if (null == version) {
            LOG.debug("Device version is null");
            return Futures.immediateFuture(null);
        }
        if (version < OFConstants.OFP_VERSION_1_3) {
            LOG.debug("Device version not support ROLE");
            return Futures.immediateFuture(null);
        } else {
            final SetRoleInput setRoleInput = (new SetRoleInputBuilder()).setControllerRole(newRole)
                    .setNode(new NodeRef(DeviceStateUtil.createNodeInstanceIdentifier(deviceInfo.getNodeId()))).build();
            setRoleOutputFuture = this.salRoleService.setRole(setRoleInput);
            final TimerTask timerTask = timeout -> {
                if (!setRoleOutputFuture.isDone()) {
                    LOG.warn("New role {} was not propagated to device {} during 10 sec", newRole, deviceInfo.getLOGValue());
                    setRoleOutputFuture.cancel(true);
                }
            };
            hashedWheelTimer.newTimeout(timerTask, 10, TimeUnit.SECONDS);
        }
        return JdkFutureAdapters.listenInPoolThread(setRoleOutputFuture);
    }

    @Override
    public void setLifecycleInitializationPhaseHandler(final ClusterInitializationPhaseHandler handler) {
        this.clusterInitializationPhaseHandler = handler;
    }

    @Override
    public boolean onContextBecomeMasterInitialized(final ConnectionContext connectionContext) {

        if (connectionContext.getConnectionState().equals(ConnectionContext.CONNECTION_STATE.RIP)) {
            LOG.warn("Connection on device {} was interrupted, will stop starting master services.", deviceInfo.getLOGValue());
            return false;
        }

        Futures.addCallback(sendRoleChangeToDevice(OfpRole.BECOMEMASTER), new FutureCallback<RpcResult<SetRoleOutput>>() {
            @Override
            public void onSuccess(@Nullable RpcResult<SetRoleOutput> setRoleOutputRpcResult) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Role MASTER was successfully set on device, node {}", deviceInfo.getLOGValue());
                }
            }

            @Override
            public void onFailure(final Throwable throwable) {
                LOG.warn("Was not able to set MASTER role on device, node {}", deviceInfo.getLOGValue());
            }
        });

        return this.clusterInitializationPhaseHandler.onContextBecomeMasterInitialized(connectionContext);
    }
}
