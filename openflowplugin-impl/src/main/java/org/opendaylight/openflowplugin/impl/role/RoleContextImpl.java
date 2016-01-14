/**
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.role;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.JdkFutureAdapters;
import com.google.common.util.concurrent.SettableFuture;
import java.util.concurrent.Future;
import javax.annotation.Nullable;
import org.opendaylight.controller.md.sal.common.api.clustering.CandidateAlreadyRegisteredException;
import org.opendaylight.controller.md.sal.common.api.clustering.Entity;
import org.opendaylight.controller.md.sal.common.api.clustering.EntityOwnershipCandidateRegistration;
import org.opendaylight.controller.md.sal.common.api.clustering.EntityOwnershipService;
import org.opendaylight.controller.sal.binding.api.RpcProviderRegistry;
import org.opendaylight.openflowplugin.api.openflow.connection.ConnectionContext;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceContext;
import org.opendaylight.openflowplugin.api.openflow.device.RequestContext;
import org.opendaylight.openflowplugin.api.openflow.role.RoleContext;
import org.opendaylight.openflowplugin.api.openflow.role.RoleManager;
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
    private final RpcProviderRegistry rpcProviderRegistry;
    private final DeviceContext deviceContext;
    private final Entity entity;
    private final OpenflowOwnershipListener openflowOwnershipListener;
    private SalRoleService salRoleService;
    private FutureCallback<Boolean> roleChangeCallback;

    private final SettableFuture<OfpRole> initRoleChangeFuture;

    public RoleContextImpl(final DeviceContext deviceContext, final RpcProviderRegistry rpcProviderRegistry,
                           final EntityOwnershipService entityOwnershipService, final OpenflowOwnershipListener openflowOwnershipListener) {
        this.entityOwnershipService = entityOwnershipService;
        this.rpcProviderRegistry = rpcProviderRegistry;
        this.deviceContext = deviceContext;
        entity = new Entity(RoleManager.ENTITY_TYPE, deviceContext.getPrimaryConnectionContext().getNodeId().getValue());

        this.openflowOwnershipListener =  openflowOwnershipListener;
        salRoleService = new SalRoleServiceImpl(this, deviceContext);

        initRoleChangeFuture = SettableFuture.create();
    }

    @Override
    public Future<OfpRole> initialization() throws CandidateAlreadyRegisteredException {
        LOG.debug("Initialization requestOpenflowEntityOwnership for entity {}", entity);
        openflowOwnershipListener.registerRoleChangeListener(this);
        entityOwnershipCandidateRegistration = entityOwnershipService.registerCandidate(entity);
        LOG.info("RoleContextImpl : Candidate registered with ownership service for device :{}", deviceContext
                .getPrimaryConnectionContext().getNodeId().getValue());
        return initRoleChangeFuture;
    }

    @Override
    public void facilitateRoleChange(final FutureCallback<Boolean> roleChangeCallback) {
        this.roleChangeCallback = roleChangeCallback;
        if (!isDeviceConnected()) {
            throw new IllegalStateException(
                    "Device is disconnected. Giving up on Role Change:" + deviceContext.getDeviceState().getNodeId());
        }
    }

    private void requestOpenflowEntityOwnership() {

        LOG.debug("requestOpenflowEntityOwnership for entity {}", entity);
        try {
            entityOwnershipCandidateRegistration = entityOwnershipService.registerCandidate(entity);

            // The role change listener must be registered after registering a candidate
            openflowOwnershipListener.registerRoleChangeListener(this);
            LOG.info("RoleContextImpl : Candidate registered with ownership service for device :{}", deviceContext.getPrimaryConnectionContext().getNodeId().getValue());
        } catch (final CandidateAlreadyRegisteredException e) {
            // we can log and move for this error, as listener is present and role changes will be served.
            LOG.error("Candidate - Entity already registered with Openflow candidate ", entity, e );
        }
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
                if (roleChangeCallback != null) {
                    roleChangeCallback.onSuccess(true);
                }
            }

            @Override
            public void onFailure(final Throwable throwable) {
                LOG.error("Error in setRole {} for device {} ", newRole,
                        deviceContext.getPrimaryConnectionContext().getNodeId(), throwable);
                if (roleChangeCallback != null) {
                    roleChangeCallback.onFailure(throwable);
                }
            }
        });
    }

    @Override
    public void close() throws Exception {
        if (entityOwnershipCandidateRegistration != null) {
            LOG.debug("Closing EntityOwnershipCandidateRegistration for {}", entity);
            entityOwnershipCandidateRegistration.close();
        }
    }

    @Override
    public void onDeviceContextClosed(final DeviceContext deviceContext) {
        try {
            LOG.debug("onDeviceContextClosed called");
            this.close();
        } catch (final Exception e) {
            LOG.error("Exception in onDeviceContextClosed of RoleContext", e);
        }
    }

    @Override
    public Entity getEntity() {
        return entity;
    }

    @Override
    public void onDeviceDisconnectedFromCluster(final boolean removeNodeFromDS) {
        LOG.info("Called onDeviceDisconnectedFromCluster in DeviceContext for entity:{}", entity);
        deviceContext.onDeviceDisconnectedFromCluster(removeNodeFromDS);
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
    public void setSalRoleService(final SalRoleService salRoleService) {
        this.salRoleService = salRoleService;
    }
}
