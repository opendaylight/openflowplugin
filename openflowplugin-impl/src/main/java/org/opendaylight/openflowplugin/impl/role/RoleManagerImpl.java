/**
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.role;

import javax.annotation.CheckForNull;
import javax.annotation.Nullable;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.base.Verify;
import com.google.common.util.concurrent.AsyncFunction;
import com.google.common.util.concurrent.CheckedFuture;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.WriteTransaction;
import org.opendaylight.controller.md.sal.common.api.clustering.Entity;
import org.opendaylight.controller.md.sal.common.api.clustering.EntityOwnershipChange;
import org.opendaylight.controller.md.sal.common.api.clustering.EntityOwnershipListener;
import org.opendaylight.controller.md.sal.common.api.clustering.EntityOwnershipListenerRegistration;
import org.opendaylight.controller.md.sal.common.api.clustering.EntityOwnershipService;
import org.opendaylight.controller.md.sal.common.api.clustering.EntityOwnershipState;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.controller.md.sal.common.api.data.TransactionCommitFailedException;
import org.opendaylight.openflowplugin.api.OFConstants;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceContext;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceState;
import org.opendaylight.openflowplugin.api.openflow.device.handlers.DeviceInitializationPhaseHandler;
import org.opendaylight.openflowplugin.api.openflow.role.RoleChangeListener;
import org.opendaylight.openflowplugin.api.openflow.role.RoleContext;
import org.opendaylight.openflowplugin.api.openflow.role.RoleManager;
import org.opendaylight.openflowplugin.impl.util.DeviceInitializationUtils;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.role.service.rev150727.OfpRole;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Gets invoked from RpcManagerInitial, registers a candidate with EntityOwnershipService.
 * On receipt of the ownership notification, makes an rpc call to SalRoleSevice.
 *
 * Hands over to StatisticsManager at the end.
 */
public class RoleManagerImpl implements RoleManager, EntityOwnershipListener {
    private static final Logger LOG = LoggerFactory.getLogger(RoleManagerImpl.class);

    private DeviceInitializationPhaseHandler deviceInitializationPhaseHandler;
    private final DataBroker dataBroker;
    private final EntityOwnershipService entityOwnershipService;
    private final ConcurrentMap<Entity, RoleContext> contexts = new ConcurrentHashMap<>();
    private final EntityOwnershipListenerRegistration entityOwnershipListenerRegistration;
    private final boolean switchFeaturesMandatory;

    public RoleManagerImpl(final EntityOwnershipService entityOwnershipService, final DataBroker dataBroker, final boolean switchFeaturesMandatory) {
        this.entityOwnershipService = Preconditions.checkNotNull(entityOwnershipService);
        this.dataBroker = Preconditions.checkNotNull(dataBroker);
        this.switchFeaturesMandatory = switchFeaturesMandatory;
        this.entityOwnershipListenerRegistration = Preconditions.checkNotNull(entityOwnershipService.registerListener(RoleManager.ENTITY_TYPE, this));
        LOG.debug("Registering OpenflowOwnershipListener listening to all entity ownership changes");
    }

    @Override
    public void setDeviceInitializationPhaseHandler(final DeviceInitializationPhaseHandler handler) {
        deviceInitializationPhaseHandler = handler;
    }

    @Override
    public void onDeviceContextLevelUp(@CheckForNull final DeviceContext deviceContext) {
        LOG.debug("RoleManager called for device:{}", deviceContext.getPrimaryConnectionContext().getNodeId());
        if (deviceContext.getDeviceState().getFeatures().getVersion() < OFConstants.OFP_VERSION_1_3) {
            // Roles are not supported before OF1.3, so move forward.
            deviceInitializationPhaseHandler.onDeviceContextLevelUp(deviceContext);
            return;
        }

        final RoleContext roleContext = new RoleContextImpl(deviceContext, entityOwnershipService,
                makeEntity(deviceContext.getDeviceState().getNodeId()));
        // if the device context gets closed (mostly on connection close), we would need to cleanup
        deviceContext.addDeviceContextClosedHandler(this);
        Verify.verify(contexts.putIfAbsent(roleContext.getEntity(), roleContext) == null,
                "RoleCtx for master Node {} is still not close.", deviceContext.getDeviceState().getNodeId());

        final ListenableFuture<OfpRole> roleChangeFuture = roleContext.initialization();
        final ListenableFuture<Void> initDeviceFuture = Futures.transform(roleChangeFuture, new AsyncFunction<OfpRole, Void>() {
            @Override
            public ListenableFuture<Void> apply(final OfpRole input) throws Exception {
                final ListenableFuture<Void> nextFuture;
                if (OfpRole.BECOMEMASTER.equals(input)) {
                    LOG.debug("Node {} was initialized", deviceContext.getDeviceState().getNodeId());
                    nextFuture = DeviceInitializationUtils.initializeNodeInformation(deviceContext, switchFeaturesMandatory);
                } else {
                    LOG.debug("Node {} we are not Master so we are going to finish.", deviceContext.getDeviceState().getNodeId());
                    nextFuture = Futures.immediateFuture(null);
                }
                return nextFuture;
            }
        });

        Futures.addCallback(initDeviceFuture, new FutureCallback<Void>() {
            @Override
            public void onSuccess(final Void result) {
                LOG.debug("Initialization Node {} is done.", deviceContext.getDeviceState().getNodeId());
                getRoleContextLevelUp(deviceContext);
            }

            @Override
            public void onFailure(final Throwable t) {
                LOG.warn("Unexpected error for Node {} initialization", deviceContext.getDeviceState().getNodeId(), t);
                deviceContext.close();
            }
        });
    }

    void getRoleContextLevelUp(final DeviceContext deviceContext) {
        LOG.debug("Created role context for node {}", deviceContext.getDeviceState().getNodeId());
        LOG.debug("roleChangeFuture success for device:{}. Moving to StatisticsManager", deviceContext.getDeviceState().getNodeId());
        deviceInitializationPhaseHandler.onDeviceContextLevelUp(deviceContext);
    }

    @Override
    public void close() throws Exception {
        entityOwnershipListenerRegistration.close();
        for (final Map.Entry<Entity, RoleContext> roleContextEntry : contexts.entrySet()) {
            // got here because last known role is LEADER and DS might need clearing up
            final Entity entity = roleContextEntry.getKey();
            final Optional<EntityOwnershipState> ownershipState = entityOwnershipService.getOwnershipState(entity);
            final NodeId nodeId = roleContextEntry.getValue().getDeviceState().getNodeId();
            if (ownershipState.isPresent()) {
                if ((!ownershipState.get().hasOwner())) {
                    LOG.trace("Last role is LEADER and ownershipService returned hasOwner=false for node: {}; " +
                            "cleaning DS as being probably the last owner", nodeId);
                    removeDeviceFromOperDS(roleContextEntry.getValue());
                } else {
                    // NOOP - there is another owner
                    LOG.debug("Last role is LEADER and ownershipService returned hasOwner=true for node: {}; " +
                            "leaving DS untouched", nodeId);
                }
            } else {
                // TODO: is this safe? When could this happen?
                LOG.warn("Last role is LEADER but ownershipService returned empty ownership info for node: {}; " +
                        "cleaning DS ANYWAY!", nodeId);
                removeDeviceFromOperDS(roleContextEntry.getValue());
            }
        }
        contexts.clear();
    }

    @Override
    public void onDeviceContextClosed(final DeviceContext deviceContext) {
        final NodeId nodeId = deviceContext.getDeviceState().getNodeId();
        final OfpRole role = deviceContext.getDeviceState().getRole();
        LOG.debug("onDeviceContextClosed for node {}", nodeId);

        final Entity entity = makeEntity(nodeId);
        final RoleContext roleContext = contexts.get(entity);
        if (roleContext != null) {
            LOG.debug("Found roleContext associated to deviceContext: {}, now closing the roleContext", nodeId);
            roleContext.close();
            if (role == null || OfpRole.BECOMESLAVE.equals(role)) {
                LOG.debug("No DS commitment for device {} - LEADER is somewhere else", nodeId);
                contexts.remove(entity, roleContext);
            }
        }
    }

    private static Entity makeEntity(final NodeId nodeId) {
        return new Entity(RoleManager.ENTITY_TYPE, nodeId.getValue());
    }

    @VisibleForTesting
    CheckedFuture<Void, TransactionCommitFailedException> removeDeviceFromOperDS(final RoleChangeListener roleChangeListener) {
        Preconditions.checkArgument(roleChangeListener != null);
        final DeviceState deviceState = roleChangeListener.getDeviceState();
        final WriteTransaction delWtx = dataBroker.newWriteOnlyTransaction();
        delWtx.delete(LogicalDatastoreType.OPERATIONAL, deviceState.getNodeInstanceIdentifier());
        final CheckedFuture<Void, TransactionCommitFailedException> delFuture = delWtx.submit();
        Futures.addCallback(delFuture, new FutureCallback<Void>() {

            @Override
            public void onSuccess(final Void result) {
                LOG.debug("Delete Node {} was successful", deviceState.getNodeId());
            }

            @Override
            public void onFailure(final Throwable t) {
                LOG.warn("Delete Node {} fail.", deviceState.getNodeId(), t);
            }
        });
        return delFuture;
    }

    @Override
    public void ownershipChanged(final EntityOwnershipChange ownershipChange) {
        Preconditions.checkArgument(ownershipChange != null);
        final RoleChangeListener roleChangeListener = contexts.get(ownershipChange.getEntity());

        LOG.info("Received EntityOwnershipChange:{}, roleChangeListener-present={}", ownershipChange, (roleChangeListener != null));

        if (roleChangeListener != null) {
            LOG.debug("Found roleChangeListener for local entity:{}", ownershipChange.getEntity());
            // if this was the master and entity does not have a master
            if (!ownershipChange.hasOwner()) {
                if (!ownershipChange.isOwner() && ownershipChange.wasOwner()) {
                    unregistrationHelper(ownershipChange, roleChangeListener);
                } else {
                    // we sometimes stay as last one but not with Master role (election not finish correctly)
                    // so we have to check if we are closed RoleCtx and we don't want to ask for service if everything is OK
                    if (!entityOwnershipService.getOwnershipState(ownershipChange.getEntity()).isPresent()) {
                        unregistrationHelper(ownershipChange, roleChangeListener);
                    }
                }
            } else {
                final OfpRole newRole = ownershipChange.isOwner() ? OfpRole.BECOMEMASTER : OfpRole.BECOMESLAVE;
                final OfpRole oldRole = ownershipChange.wasOwner() ? OfpRole.BECOMEMASTER : OfpRole.BECOMESLAVE;
                // send even if they are same. we do the check for duplicates in SalRoleService and maintain a lastKnownRole
                roleChangeListener.onRoleChanged(oldRole, newRole);
            }
        }
    }

    private void unregistrationHelper(final EntityOwnershipChange ownershipChange, final RoleChangeListener roleChangeListener) {
        LOG.info("Initiate removal from operational. Possibly the last node to be disconnected for :{}. ", ownershipChange);
        Futures.addCallback(removeDeviceFromOperDS(roleChangeListener), new FutureCallback<Void>() {
            @Override
            public void onSuccess(@Nullable final Void aVoid) {
                LOG.debug("Freeing roleContext slot for device: {}", roleChangeListener.getDeviceState().getNodeId());
                contexts.remove(ownershipChange.getEntity(), roleChangeListener);
            }

            @Override
            public void onFailure(final Throwable throwable) {
                LOG.warn("NOT freeing roleContext slot for device: {}, {}", roleChangeListener.getDeviceState()
                        .getNodeId(), throwable.getMessage());
                contexts.remove(ownershipChange.getEntity(), roleChangeListener);
            }
        });
    @VisibleForTesting
    ConcurrentMap<Entity, RoleContext> getContexts() {
        return this.contexts;
    }
}
