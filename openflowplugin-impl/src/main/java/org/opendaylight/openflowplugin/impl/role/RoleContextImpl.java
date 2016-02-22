/**
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.role;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import com.google.common.base.Verify;
import com.google.common.util.concurrent.AsyncFunction;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.JdkFutureAdapters;
import com.google.common.util.concurrent.ListenableFuture;
import io.netty.util.Timeout;
import io.netty.util.TimerTask;
import java.util.concurrent.Future;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import javax.annotation.Nullable;
import org.opendaylight.controller.md.sal.common.api.clustering.CandidateAlreadyRegisteredException;
import org.opendaylight.controller.md.sal.common.api.clustering.Entity;
import org.opendaylight.controller.md.sal.common.api.clustering.EntityOwnershipCandidateRegistration;
import org.opendaylight.controller.md.sal.common.api.clustering.EntityOwnershipService;
import org.opendaylight.openflowplugin.api.OFConstants;
import org.opendaylight.openflowplugin.api.openflow.connection.ConnectionContext;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceContext;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceState;
import org.opendaylight.openflowplugin.api.openflow.device.RequestContext;
import org.opendaylight.openflowplugin.api.openflow.role.RoleContext;
import org.opendaylight.openflowplugin.impl.rpc.AbstractRequestContext;
import org.opendaylight.openflowplugin.impl.services.SalRoleServiceImpl;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeRef;
import org.opendaylight.yang.gen.v1.urn.opendaylight.role.service.rev150727.OfpRole;
import org.opendaylight.yang.gen.v1.urn.opendaylight.role.service.rev150727.SalRoleService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.role.service.rev150727.SetRoleInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.role.service.rev150727.SetRoleInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.role.service.rev150727.SetRoleOutput;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.opendaylight.yangtools.yang.common.RpcResultBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by kramesha on 9/12/15.
 */
public class RoleContextImpl implements RoleContext {
    private static final Logger LOG = LoggerFactory.getLogger(RoleContextImpl.class);

    private final EntityOwnershipService entityOwnershipService;
    private EntityOwnershipCandidateRegistration entityOwnershipCandidateRegistration;
    private final DeviceContext deviceContext;
    private final Entity entity;
    private SalRoleService salRoleService;

    private EntityOwnershipCandidateRegistration txEntityOwnershipCandidateRegistration;
    private final Entity txEntity;

    private final Semaphore mainCandidateGuard = new Semaphore(1, true);
    private final Semaphore txCandidateGuard = new Semaphore(1, true);
    private volatile boolean txLockOwned;
    private volatile OfpRole propagatingRole;

    public RoleContextImpl(final DeviceContext deviceContext, final EntityOwnershipService entityOwnershipService,
                           final Entity entity, final Entity txEnitity) {
        this.entityOwnershipService = Preconditions.checkNotNull(entityOwnershipService);
        this.deviceContext = Preconditions.checkNotNull(deviceContext);
        this.entity = Preconditions.checkNotNull(entity);
        this.txEntity = Preconditions.checkNotNull(txEnitity);

        salRoleService = new SalRoleServiceImpl(this, deviceContext);
    }

    @Override
    public void initialization() throws CandidateAlreadyRegisteredException {
        LOG.debug("Initialization RoleContext for Node {}", deviceContext.getDeviceState().getNodeId());
        final AsyncFunction<RpcResult<SetRoleOutput>, Void> initFunction = new AsyncFunction<RpcResult<SetRoleOutput>, Void>() {
            @Override
            public ListenableFuture<Void> apply(final RpcResult<SetRoleOutput> input) throws Exception {
                LOG.debug("Initialization requestOpenflowEntityOwnership for entity {}", entity);
                getDeviceState().setRole(OfpRole.BECOMESLAVE);
                entityOwnershipCandidateRegistration = entityOwnershipService.registerCandidate(entity);
                LOG.debug("RoleContextImpl : Candidate registered with ownership service for device :{}", deviceContext
                        .getPrimaryConnectionContext().getNodeId().getValue());
                return Futures.immediateFuture(null);
            }
        };
        final ListenableFuture<Void> roleChange = sendRoleChangeToDevice(OfpRole.BECOMESLAVE, initFunction);
        Futures.addCallback(roleChange, new FutureCallback<Void>() {

            @Override
            public void onSuccess(final Void result) {
                LOG.debug("Initial RoleContext for Node {} is successfull", deviceContext.getDeviceState().getNodeId());
            }

            @Override
            public void onFailure(final Throwable t) {
                LOG.warn("Initial RoleContext for Node {} fail", deviceContext.getDeviceState().getNodeId(), t);
                deviceContext.close();
            }
        });
    }

    @Override
    public ListenableFuture<Void> onRoleChanged(final OfpRole oldRole, final OfpRole newRole) {
        LOG.trace("onRoleChanged method call for Entity {}", entity);

        if (!isDeviceConnected()) {
            // this can happen as after the disconnect, we still get a last messsage from EntityOwnershipService.
            LOG.info("Device {} is disconnected from this node. Hence not attempting a role change.",
                    deviceContext.getPrimaryConnectionContext().getNodeId());
            LOG.debug("SetRole cancelled for entity [{}], reason = device disconnected.", entity);
            return Futures.immediateFailedFuture(new Exception(
                    "Device disconnected - stopped by setRole: " + deviceContext
                            .getPrimaryConnectionContext().getNodeId()));
        }

        LOG.debug("Role change received from ownership listener from {} to {} for device:{}", oldRole, newRole,
                deviceContext.getPrimaryConnectionContext().getNodeId());

        final AsyncFunction<RpcResult<SetRoleOutput>, Void> roleChangeFunction = new AsyncFunction<RpcResult<SetRoleOutput>, Void>() {
            @Override
            public ListenableFuture<Void> apply(final RpcResult<SetRoleOutput> setRoleOutputRpcResult) throws Exception {
                LOG.debug("Rolechange {} successful made on switch :{}", newRole, deviceContext.getDeviceState()
                        .getNodeId());
                getDeviceState().setRole(newRole);
                return deviceContext.onClusterRoleChange(newRole);
            }
        };
        return sendRoleChangeToDevice(newRole, roleChangeFunction);
    }

    @Override
    public void setupTxCandidate() throws CandidateAlreadyRegisteredException {
        LOG.debug("setupTxCandidate for entity {} and Transaction entity {}", entity, txEntity);
        Verify.verify(txEntity != null);

        txEntityOwnershipCandidateRegistration = entityOwnershipService.registerCandidate(txEntity);
    }

    @Override
    public void close() {
        if (entityOwnershipCandidateRegistration != null) {
            LOG.debug("Closing EntityOwnershipCandidateRegistration for {}", entity);
            entityOwnershipCandidateRegistration.close();
        }
    }

    @Override
    public Entity getEntity() {
        return entity;
    }

    @Override
    public Entity getTxEntity() {
        return txEntity;
    }

    private boolean isDeviceConnected() {
        return ConnectionContext.CONNECTION_STATE.WORKING.equals(
                deviceContext.getPrimaryConnectionContext().getConnectionState());
    }

    @Nullable
    @Override
    public <T> RequestContext<T> createRequestContext() {
        final AbstractRequestContext<T> ret = new AbstractRequestContext<T>(deviceContext.reservedXidForDeviceMessage()) {
            @Override
            public void close() {
            }
        };
        return ret;
    }

    @VisibleForTesting
    void setSalRoleService(final SalRoleService salRoleService) {
        this.salRoleService = salRoleService;
    }

    @Override
    public DeviceState getDeviceState() {
        return deviceContext.getDeviceState();
    }

    @Override
    public void suspendTxCandidate() {
        if (txEntityOwnershipCandidateRegistration != null) {
            txEntityOwnershipCandidateRegistration.close();
            txEntityOwnershipCandidateRegistration = null;
        }
    }

    @Override
    public DeviceContext getDeviceContext() {
        return deviceContext;
    }

    @Override
    public Semaphore getMainCandidateGuard() {
        return mainCandidateGuard;
    }

    @Override
    public Semaphore getTxCandidateGuard() {
        return txCandidateGuard;
    }

    @Override
    public boolean isTxLockOwned() {
        return txLockOwned;
    }

    @Override
    public void setTxLockOwned(final boolean txLockOwned) {
        this.txLockOwned = txLockOwned;
    }

    @Override
    public OfpRole getPropagatingRole() {
        return propagatingRole;
    }

    @Override
    public void setPropagatingRole(final OfpRole propagatingRole) {
        this.propagatingRole = propagatingRole;
    }

    private ListenableFuture<Void> sendRoleChangeToDevice(final OfpRole newRole, final AsyncFunction<RpcResult<SetRoleOutput>, Void> function) {
        LOG.debug("Send new Role {} to Device {}", newRole, deviceContext.getDeviceState().getNodeId());
        final Future<RpcResult<SetRoleOutput>> setRoleOutputFuture;
        if (deviceContext.getDeviceState().getFeatures().getVersion() < OFConstants.OFP_VERSION_1_3) {
            LOG.debug("Device OF version {} not support ROLE", deviceContext.getDeviceState().getFeatures().getVersion());
            setRoleOutputFuture = Futures.immediateFuture(RpcResultBuilder.<SetRoleOutput> success().build());
        } else {
            final SetRoleInput setRoleInput = (new SetRoleInputBuilder()).setControllerRole(newRole)
                    .setNode(new NodeRef(deviceContext.getDeviceState().getNodeInstanceIdentifier())).build();
            setRoleOutputFuture = salRoleService.setRole(setRoleInput);
            final TimerTask timerTask = new TimerTask() {

                @Override
                public void run(final Timeout timeout) throws Exception {
                    if (!setRoleOutputFuture.isDone()) {
                        LOG.info("New Role {} was not propagated to device {} during 10 sec. Close connection immediately.",
                                newRole, deviceContext.getDeviceState().getNodeId());
                        deviceContext.close();
                    }
                }
            };
            deviceContext.getTimer().newTimeout(timerTask, 10, TimeUnit.SECONDS);
        }
        return Futures.transform(JdkFutureAdapters.listenInPoolThread(setRoleOutputFuture), function);
    }
}
