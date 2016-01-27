/**
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.role;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.base.Verify;
import com.google.common.util.concurrent.AsyncFunction;
import com.google.common.util.concurrent.CheckedFuture;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import javax.annotation.CheckForNull;
import javax.annotation.Nullable;
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
    private final ConcurrentMap<Entity, RoleContext> txContexts = new ConcurrentHashMap<>();
    private final EntityOwnershipListenerRegistration entityOwnershipListenerRegistration;
    private final EntityOwnershipListenerRegistration txEntityOwnershipListenerRegistration;
    private final boolean switchFeaturesMandatory;

    public RoleManagerImpl(final EntityOwnershipService entityOwnershipService, final DataBroker dataBroker, final boolean switchFeaturesMandatory) {
        this.entityOwnershipService = Preconditions.checkNotNull(entityOwnershipService);
        this.dataBroker = Preconditions.checkNotNull(dataBroker);
        this.switchFeaturesMandatory = switchFeaturesMandatory;
        this.entityOwnershipListenerRegistration = Preconditions.checkNotNull(entityOwnershipService.registerListener(RoleManager.ENTITY_TYPE, this));
        this.txEntityOwnershipListenerRegistration = Preconditions.checkNotNull(entityOwnershipService.registerListener(TX_ENTITY_TYPE, this));
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
                makeEntity(deviceContext.getDeviceState().getNodeId()),
                makeTxEntity(deviceContext.getDeviceState().getNodeId()));
        // if the device context gets closed (mostly on connection close), we would need to cleanup
        deviceContext.addDeviceContextClosedHandler(this);
        Verify.verify(contexts.putIfAbsent(roleContext.getEntity(), roleContext) == null,
                "RoleCtx for master Node {} is still not close.", deviceContext.getDeviceState().getNodeId());

        final ListenableFuture<OfpRole> roleChangeFuture = roleContext.initialization();

        final ListenableFuture<Void> txFreeFuture = Futures.transform(roleChangeFuture, new AsyncFunction<OfpRole, Void>() {
            @Override
            public ListenableFuture<Void> apply(final OfpRole input) throws Exception {
                final ListenableFuture<Void> nextFuture;
                if (OfpRole.BECOMEMASTER.equals(input)) {
                    LOG.debug("Node {} has marked as LEADER", deviceContext.getDeviceState().getNodeId());
                    Verify.verify(txContexts.putIfAbsent(roleContext.getTxEntity(), roleContext) == null,
                            "RoleCtx for TxEntity {} master Node {} is still not close.",
                            roleContext.getTxEntity(), deviceContext.getDeviceState().getNodeId());
                    nextFuture = roleContext.setupTxCandidate();
                } else {
                    LOG.debug("Node {} was marked as FOLLOWER", deviceContext.getDeviceState().getNodeId());
                    nextFuture = Futures.immediateFuture(null);
                }
                return nextFuture;
            }
        });

        final ListenableFuture<Void> initDeviceFuture = Futures.transform(txFreeFuture,new AsyncFunction<Void, Void>() {
            @Override
            public ListenableFuture<Void> apply(final Void input) throws Exception {
                LOG.debug("Node {} will be initialized", deviceContext.getDeviceState().getNodeId());
                return DeviceInitializationUtils.initializeNodeInformation(deviceContext, switchFeaturesMandatory);
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
        LOG.debug("onDeviceContextClosed for node {}", nodeId);
        final Entity entity = makeEntity(nodeId);
        final RoleContext roleContext = contexts.get(entity);
        if (roleContext != null) {
            LOG.debug("Found roleContext associated to deviceContext: {}, now closing the roleContext", nodeId);
            final Optional<EntityOwnershipState> actState = entityOwnershipService.getOwnershipState(entity);
            if (actState.isPresent()) {
                if (!actState.get().isOwner()) {
                    LOG.debug("No DS commitment for device {} - LEADER is somewhere else", nodeId);
                    contexts.remove(entity, roleContext);
                }
            } else {
                LOG.warn("EntityOwnershipService doesn't return state for entity: {} in close proces", entity);
            }
            roleContext.close();
        }
    }

    private static Entity makeEntity(final NodeId nodeId) {
        return new Entity(RoleManager.ENTITY_TYPE, nodeId.getValue());
    }

    private static Entity makeTxEntity(final NodeId nodeId) {
        return new Entity(RoleManager.TX_ENTITY_TYPE, nodeId.getValue());
    }

    @Override
    public void ownershipChanged(final EntityOwnershipChange ownershipChange) {
        Preconditions.checkArgument(ownershipChange != null);
        final RoleContext roleContext = contexts.get(ownershipChange.getEntity());
        if (roleContext != null) {
            changeForEntity(ownershipChange, roleContext);
        } else {
            final RoleContext txRoleContext = txContexts.get(ownershipChange.getEntity());
            if (txRoleContext != null) {
                changeForTxEntity(ownershipChange, txRoleContext);
            } else {
                LOG.debug("We are not able to find Entity {} ownershipChange {}", ownershipChange.getEntity(), ownershipChange);
            }
        }
    }

    private void changeForTxEntity(final EntityOwnershipChange ownershipChange, final RoleContext roleTxChangeListener) {
        LOG.info("Received EntityOwnershipChange:{}, roleChangeListener-present={}", ownershipChange, (roleTxChangeListener != null));

        if (roleTxChangeListener != null) {
            if (ownershipChange.wasOwner() && !ownershipChange.isOwner()) {
                txContexts.remove(roleTxChangeListener.getTxEntity(), roleTxChangeListener);
            } else if (!ownershipChange.wasOwner() && ownershipChange.isOwner()) {
                LOG.debug("TxRoleChange for entity {}", ownershipChange.getEntity());
                final OfpRole newRole = ownershipChange.isOwner() ? OfpRole.BECOMEMASTER : OfpRole.BECOMESLAVE;
                final OfpRole oldRole = ownershipChange.wasOwner() ? OfpRole.BECOMEMASTER : OfpRole.BECOMESLAVE;
                roleTxChangeListener.onTxRoleChange(oldRole, newRole);
            } else {
                LOG.warn("Unexpected state for TxEntity {} ", roleTxChangeListener.getTxEntity());
            }
        }
    }

    private static FutureCallback<Void> makeTxEntitySupendCallback(final RoleContext roleChangeListener) {
        return new FutureCallback<Void>() {
            @Override
            public void onSuccess(final Void result) {
                roleChangeListener.suspendTxCandidate();
            }

            @Override
            public void onFailure(final Throwable t) {
                roleChangeListener.suspendTxCandidate();
            }
        };
    }

    private static FutureCallback<Void> makeTxEntitySetupCallbakck(final RoleContext roleChangeListener) {
        return new FutureCallback<Void>() {
            @Override
            public void onSuccess(final Void result) {
                roleChangeListener.setupTxCandidate();
            }

            @Override
            public void onFailure(final Throwable t) {
                LOG.warn("Unexpected state but we want continue txEntity {}", roleChangeListener.getTxEntity(), t);
                roleChangeListener.setupTxCandidate();
            }
        };
    }

    private void changeForEntity(final EntityOwnershipChange ownershipChange, final RoleContext roleChangeListener) {
        //FIXME : check again implementation for double candidate scenario
        LOG.info("Received EntityOwnershipChange:{}, roleChangeListener-present={}", ownershipChange, (roleChangeListener != null));
        if (roleChangeListener != null) {
            if (roleChangeListener.getDeviceState().isValid()) {
                LOG.debug("RoleChange for entity {}", ownershipChange.getEntity());
                final OfpRole newRole = ownershipChange.isOwner() ? OfpRole.BECOMEMASTER : OfpRole.BECOMESLAVE;
                final OfpRole oldRole = ownershipChange.wasOwner() ? OfpRole.BECOMEMASTER : OfpRole.BECOMESLAVE;
                // send even if they are same. we do the check for duplicates in SalRoleService and maintain a lastKnownRole
                final FutureCallback<Void> callback;
                if (ownershipChange.wasOwner() && !ownershipChange.isOwner() && ownershipChange.hasOwner()) {
                    callback = makeTxEntitySupendCallback(roleChangeListener);
                } else if (!ownershipChange.wasOwner() && ownershipChange.isOwner() && ownershipChange.isOwner()) {
                    // FIXME : make different code path deviceContext.onClusterRoleChange(newRole); has to call from onTxRoleChange (for master)
                    callback = makeTxEntitySetupCallbakck(roleChangeListener);
                } else {
                    callback = null;
                }
                roleChangeListener.onRoleChanged(oldRole, newRole, callback);
            } else {
                LOG.debug("We are closing connection for entity {}", ownershipChange.getEntity());
                if (!ownershipChange.hasOwner() && !ownershipChange.isOwner() && ownershipChange.wasOwner()) {
                    unregistrationHelper(ownershipChange, roleChangeListener);
                } else if (ownershipChange.hasOwner() && !ownershipChange.isOwner() && ownershipChange.wasOwner()) {
                    contexts.remove(ownershipChange.getEntity(), roleChangeListener);
                    roleChangeListener.suspendTxCandidate();
                } else {
                    LOG.info("Unexpected role change msg {} for entity {}", ownershipChange, ownershipChange.getEntity());
                }
            }
        }
    }

    private CheckedFuture<Void, TransactionCommitFailedException> removeDeviceFromOperDS(
            final RoleChangeListener roleChangeListener) {
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

    private void unregistrationHelper(final EntityOwnershipChange ownershipChange, final RoleChangeListener roleChangeListener) {
        LOG.info("Initiate removal from operational. Possibly the last node to be disconnected for :{}. ", ownershipChange);
        Futures.addCallback(removeDeviceFromOperDS(roleChangeListener), new FutureCallback<Void>() {
            @Override
            public void onSuccess(@Nullable final Void aVoid) {
                LOG.debug("Freeing roleContext slot for device: {}", roleChangeListener.getDeviceState().getNodeId());
                contexts.remove(ownershipChange.getEntity(), roleChangeListener);
                ((RoleContext) roleChangeListener).suspendTxCandidate();
            }

            @Override
            public void onFailure(final Throwable throwable) {
                LOG.warn("NOT freeing roleContext slot for device: {}, {}", roleChangeListener.getDeviceState()
                        .getNodeId(), throwable.getMessage());
                contexts.remove(ownershipChange.getEntity(), roleChangeListener);
                ((RoleContext) roleChangeListener).suspendTxCandidate();
            }
        });
    }

    @VisibleForTesting
    ConcurrentMap<Entity, RoleContext> getContexts() {
        return this.contexts;
    }

    @VisibleForTesting
    ConcurrentMap<Entity, RoleContext> getTxContexts() {
        return this.txContexts;
    }
}
