/**
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.role;

import javax.annotation.Nullable;
import java.util.concurrent.Future;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.JdkFutureAdapters;
import com.google.common.util.concurrent.SettableFuture;
import org.opendaylight.controller.md.sal.common.api.clustering.CandidateAlreadyRegisteredException;
import org.opendaylight.controller.md.sal.common.api.clustering.Entity;
import org.opendaylight.controller.md.sal.common.api.clustering.EntityOwnershipCandidateRegistration;
import org.opendaylight.controller.md.sal.common.api.clustering.EntityOwnershipService;
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

    private final SettableFuture<OfpRole> initRoleChangeFuture;

    public RoleContextImpl(final DeviceContext deviceContext, final EntityOwnershipService entityOwnershipService, final Entity entity) {
        this.entityOwnershipService = Preconditions.checkNotNull(entityOwnershipService);
        this.deviceContext = Preconditions.checkNotNull(deviceContext);
        this.entity = Preconditions.checkNotNull(entity);

        salRoleService = new SalRoleServiceImpl(this, deviceContext);

        initRoleChangeFuture = SettableFuture.create();
    }

    @Override
    public Future<OfpRole> initialization() throws CandidateAlreadyRegisteredException {
        LOG.debug("Initialization requestOpenflowEntityOwnership for entity {}", entity);
        entityOwnershipCandidateRegistration = entityOwnershipService.registerCandidate(entity);
        LOG.info("RoleContextImpl : Candidate registered with ownership service for device :{}", deviceContext
                .getPrimaryConnectionContext().getNodeId().getValue());
        return initRoleChangeFuture;
    }

    @Override
    public void onRoleChanged(final OfpRole oldRole, final OfpRole newRole) {
        LOG.trace("onRoleChanged method call for Entity {}", entity);

        if (!isDeviceConnected()) {
            // this can happen as after the disconnect, we still get a last messsage from EntityOwnershipService.
            LOG.info("Device {} is disconnected from this node. Hence not attempting a role change.",
                    deviceContext.getPrimaryConnectionContext().getNodeId());
            if (!initRoleChangeFuture.isDone()) {
                LOG.debug("RoleChange is not valid for initialization Entity {} anymore - Device is disconnected", entity);
                initRoleChangeFuture.cancel(true);
            }
            return;
        }

        if (!initRoleChangeFuture.isDone()) {
            LOG.debug("Initialization Role for entity {} is chosed {}", entity, newRole);
            // we don't want to wait for Device RoleChangeResponse in initial phase
            deviceContext.onClusterRoleChange(newRole);
            initRoleChangeFuture.set(newRole);
        }

        LOG.debug("Role change received from ownership listener from {} to {} for device:{}", oldRole, newRole,
                deviceContext.getPrimaryConnectionContext().getNodeId());

        final SetRoleInput setRoleInput = (new SetRoleInputBuilder())
                .setControllerRole(newRole)
                .setNode(new NodeRef(deviceContext.getDeviceState().getNodeInstanceIdentifier()))
                .build();

        final Future<RpcResult<SetRoleOutput>> setRoleOutputFuture = salRoleService.setRole(setRoleInput);

        Futures.addCallback(JdkFutureAdapters.listenInPoolThread(setRoleOutputFuture), new FutureCallback<RpcResult<SetRoleOutput>>() {
            @Override
            public void onSuccess(final RpcResult<SetRoleOutput> setRoleOutputRpcResult) {
                LOG.debug("Rolechange {} successful made on switch :{}", newRole,
                        deviceContext.getPrimaryConnectionContext().getNodeId());
                deviceContext.getDeviceState().setRole(newRole);
                deviceContext.onClusterRoleChange(newRole);
            }

            @Override
            public void onFailure(final Throwable throwable) {
                LOG.error("Error in setRole {} for device {} ", newRole,
                        deviceContext.getPrimaryConnectionContext().getNodeId(), throwable);
            }
        });
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

    private boolean isDeviceConnected() {
        return ConnectionContext.CONNECTION_STATE.WORKING.equals(
                deviceContext.getPrimaryConnectionContext().getConnectionState());
    }

    @Nullable
    @Override
    public <T> RequestContext<T> createRequestContext() {
        final AbstractRequestContext<T> ret = new AbstractRequestContext<T>(deviceContext.getReservedXid()) {
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
}
