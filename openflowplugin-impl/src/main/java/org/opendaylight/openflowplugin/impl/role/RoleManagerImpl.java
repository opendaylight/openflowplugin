/**
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.role;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.base.Throwables;
import com.google.common.base.Verify;
import com.google.common.collect.Iterators;
import com.google.common.util.concurrent.CheckedFuture;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Semaphore;
import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.WriteTransaction;
import org.opendaylight.controller.md.sal.common.api.clustering.CandidateAlreadyRegisteredException;
import org.opendaylight.controller.md.sal.common.api.clustering.Entity;
import org.opendaylight.controller.md.sal.common.api.clustering.EntityOwnershipChange;
import org.opendaylight.controller.md.sal.common.api.clustering.EntityOwnershipListener;
import org.opendaylight.controller.md.sal.common.api.clustering.EntityOwnershipListenerRegistration;
import org.opendaylight.controller.md.sal.common.api.clustering.EntityOwnershipService;
import org.opendaylight.controller.md.sal.common.api.clustering.EntityOwnershipState;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.controller.md.sal.common.api.data.TransactionCommitFailedException;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceContext;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceState;
import org.opendaylight.openflowplugin.api.openflow.device.handlers.DeviceInitializationPhaseHandler;
import org.opendaylight.openflowplugin.api.openflow.role.RoleChangeListener;
import org.opendaylight.openflowplugin.api.openflow.role.RoleContext;
import org.opendaylight.openflowplugin.api.openflow.role.RoleManager;
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

    public RoleManagerImpl(final EntityOwnershipService entityOwnershipService, final DataBroker dataBroker) {
        this.entityOwnershipService = Preconditions.checkNotNull(entityOwnershipService);
        this.dataBroker = Preconditions.checkNotNull(dataBroker);
        this.entityOwnershipListenerRegistration = Preconditions.checkNotNull(entityOwnershipService.registerListener(RoleManager.ENTITY_TYPE, this));
        this.txEntityOwnershipListenerRegistration = Preconditions.checkNotNull(entityOwnershipService.registerListener(TX_ENTITY_TYPE, this));
        LOG.debug("Register OpenflowOwnershipListener to all entity ownership changes");
    }

    @Override
    public void setDeviceInitializationPhaseHandler(final DeviceInitializationPhaseHandler handler) {
        deviceInitializationPhaseHandler = handler;
    }

    @Override
    public void onDeviceContextLevelUp(@CheckForNull final DeviceContext deviceContext) throws Exception {
        LOG.trace("Role manager called for device:{}", deviceContext.getPrimaryConnectionContext().getNodeId());
        final RoleContext roleContext = new RoleContextImpl(deviceContext, entityOwnershipService,
                makeEntity(deviceContext.getDeviceState().getNodeId()),
                makeTxEntity(deviceContext.getDeviceState().getNodeId()));

        Verify.verify(contexts.putIfAbsent(roleContext.getEntity(), roleContext) == null, "Role context for master Node {} is still not closed.", deviceContext.getDeviceState().getNodeId());
        Verify.verify(!txContexts.containsKey(roleContext.getTxEntity()),
                "Role context for master Node {} is still not closed. TxEntity was not unregistered yet.", deviceContext.getDeviceState().getNodeId());

        // if the device context gets closed (mostly on connection close), we would need to cleanup
        deviceContext.addDeviceContextClosedHandler(this);
        roleContext.initialization();
        deviceInitializationPhaseHandler.onDeviceContextLevelUp(deviceContext);
    }

    @Override
    public void close() {
        entityOwnershipListenerRegistration.close();
        txEntityOwnershipListenerRegistration.close();
        for (final Iterator<Entry<Entity, RoleContext>> iterator = Iterators.consumingIterator(contexts.entrySet()
                .iterator()); iterator.hasNext();) {
            // got here because last known role is LEADER and DS might need clearing up
            final Entry<Entity, RoleContext> entry = iterator.next();
            final RoleContext roleCtx = entry.getValue();
            final NodeId nodeId = roleCtx.getDeviceState().getNodeId();
            if (OfpRole.BECOMEMASTER.equals(roleCtx.getDeviceState().getRole())) {
                LOG.trace("Last role is LEADER and ownershipService returned hasOwner=false for node: {}; "
                        + "cleaning DS as being probably the last owner", nodeId);
                removeDeviceFromOperDS(roleCtx);
            } else {
                // NOOP - there is another owner
                LOG.debug("Last role is LEADER and ownershipService returned hasOwner=true for node: {}; "
                        + "leaving DS untouched", nodeId);
            }
            roleCtx.suspendTxCandidate();
            txContexts.remove(roleCtx.getTxEntity(), roleCtx);
            roleCtx.close();
        }
    }

    @Override
    public void onDeviceContextClosed(final DeviceContext deviceContext) {
        final NodeId nodeId = deviceContext.getDeviceState().getNodeId();
        LOG.trace("onDeviceContextClosed for node {}", nodeId);
        final Entity entity = makeEntity(nodeId);
        final RoleContext roleContext = contexts.get(entity);
        if (roleContext != null) {
            LOG.debug("Found roleContext associated to deviceContext: {}, now closing the roleContext", nodeId);
            final Optional<EntityOwnershipState> actState = entityOwnershipService.getOwnershipState(entity);
            if (actState.isPresent()) {
                if (actState.get().isOwner()) {
                    if (!txContexts.containsKey(roleContext.getTxEntity())) {
                        try {
                            txContexts.putIfAbsent(roleContext.getTxEntity(), roleContext);
                            roleContext.setupTxCandidate();
                            // we'd like to wait for registration response
                            return;
                        } catch (final CandidateAlreadyRegisteredException e) {
                            // NOOP
                        }
                    }
                } else {
                    LOG.debug("No DS commitment for device {} - LEADER is somewhere else", nodeId);
                    contexts.remove(entity, roleContext);
                    // TODO : is there a chance to have TxEntity ?
                }
            } else {
                LOG.warn("EntityOwnershipService doesn't return state for entity: {} in close process", entity);
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
        RoleContext roleContext = null;
        try {
            roleContext = contexts.get(ownershipChange.getEntity());
            if (roleContext != null) {
                changeForEntity(ownershipChange, roleContext);
                return;
            }

            roleContext = txContexts.get(ownershipChange.getEntity());
            if (roleContext != null) {
                changeForTxEntity(ownershipChange, roleContext);
                return;
            }
        } catch (final Exception e) {
            LOG.warn("fail to acquire semaphore: {}", ownershipChange.getEntity(), e);
            if (roleContext != null) {
                roleContext.getDeviceContext().close();
            }
        }

        LOG.debug("We are not able to find Entity {} ownershipChange {} - disregarding ownership notification",
                ownershipChange.getEntity(), ownershipChange);
    }

    private void changeForTxEntity(final EntityOwnershipChange ownershipChange, @Nonnull final RoleContext roleContext)
            throws InterruptedException {
        LOG.info("Received TX-EntityOwnershipChange:{}", ownershipChange);
        final Semaphore txCandidateGuard = roleContext.getTxCandidateGuard();
        LOG.trace("txCandidate lock queue: " + txCandidateGuard.getQueueLength());
        txCandidateGuard.acquire();

        ListenableFuture<Void> processingClosure;
        final DeviceContext deviceContext = roleContext.getDeviceContext();
        final NodeId nodeId = roleContext.getDeviceState().getNodeId();

        if (!ownershipChange.wasOwner() && ownershipChange.isOwner()) {
            // SLAVE -> MASTER - acquired transition lock
            LOG.debug("Acquired tx-lock for entity {}", ownershipChange.getEntity());

            // activate txChainManager, activate rpcs
            if (roleContext.getDeviceState().isValid()) {
                processingClosure = roleContext.onRoleChanged(OfpRole.BECOMESLAVE, OfpRole.BECOMEMASTER);
            } else {
                // We are not able to send anything to device, but we need to handle closing state clearly
                roleContext.close();
                processingClosure = Futures.immediateFuture(null);
            }
            // activate stats - accomplished automatically by changing role in deviceState
            processingClosure = Futures.transform(processingClosure, new Function<Void, Void>() {
                @Nullable
                @Override
                public Void apply(@Nullable final Void aVoid) {
                    deviceContext.getDeviceState().setRole(OfpRole.BECOMEMASTER);
                    return null;
                }
            });
        } else if (ownershipChange.wasOwner() && !ownershipChange.isOwner()) {
            // MASTER -> SLAVE - released tx-lock
            LOG.debug("Released tx-lock for entity {}", ownershipChange.getEntity());
            txContexts.remove(roleContext.getTxEntity(), roleContext);
            processingClosure = Futures.immediateFuture(null);
        } else {
            LOG.debug("NOOP state transition for TxEntity {} ", roleContext.getTxEntity());
            processingClosure = Futures.immediateFuture(null);
        }

        // handle result of executed steps
        Futures.addCallback(processingClosure, new FutureCallback<Void>()

                {
                    @Override
                    public void onSuccess(@Nullable final Void aVoid) {
                        // propagating role must be BECOMEMASTER in order to run this processing
                        // removing it will disable redundant processing of BECOMEMASTER
                        txCandidateGuard.release();
                    }

                    @Override
                    public void onFailure(final Throwable throwable) {
                        LOG.warn("Unexpected error for Node {} -> terminating device context", nodeId, throwable);
                        txCandidateGuard.release();
                        deviceContext.close();
                    }
                }

        );
    }

    private static Function<Void, Void> makeTxEntitySuspendCallback(final RoleContext roleChangeListener) {
        return new Function<Void, Void>() {
            @Override
            public Void apply(final Void result) {
                roleChangeListener.suspendTxCandidate();
                return null;
            }
        };
    }

    private Function<Void, Void> makeTxEntitySetupCallback(final RoleContext roleContext) {
        return new Function<Void, Void>() {
            @Override
            public Void apply(final Void result) {
                final NodeId nodeId = roleContext.getDeviceState().getNodeId();
                try {
                    LOG.debug("Node {} is marked as LEADER", nodeId);
                    Verify.verify(txContexts.putIfAbsent(roleContext.getTxEntity(), roleContext) == null,
                            "RoleCtx for TxEntity {} master Node {} is still not closed.", roleContext.getTxEntity(), nodeId);
                    // try to register tx-candidate via ownership service
                    roleContext.setupTxCandidate();
                } catch (final CandidateAlreadyRegisteredException e) {
                    LOG.warn("txCandidate registration failed {}", roleContext.getDeviceState().getNodeId(), e);
                    // --- CLEAN UP ---
                    // withdraw context from map in order to have it as before
                    txContexts.remove(roleContext.getTxEntity(), roleContext);
                    // no more propagating any role - there is no txCandidate lock approaching
                    Throwables.propagate(e);
                }
                return null;
            }
        };
    }

    private void changeForEntity(final EntityOwnershipChange ownershipChange, @Nonnull final RoleContext roleContext) throws InterruptedException {
        final Semaphore mainCandidateGuard = roleContext.getMainCandidateGuard();
        LOG.trace("mainCandidate lock queue: " + mainCandidateGuard.getQueueLength());
        mainCandidateGuard.acquire();
        LOG.info("Received EntityOwnershipChange:{}", ownershipChange);

        if (roleContext.getDeviceState().isValid()) {
            LOG.debug("RoleChange for entity {}", ownershipChange.getEntity());
            final OfpRole newRole = ownershipChange.isOwner() ? OfpRole.BECOMEMASTER : OfpRole.BECOMESLAVE;
            final OfpRole oldRole = ownershipChange.wasOwner() ? OfpRole.BECOMEMASTER : OfpRole.BECOMESLAVE;

            // propagation start point
            ListenableFuture<Void> rolePropagationFx = Futures.immediateFuture(null);
            final Function<Void, Void> txProcessCallback;

            if (ownershipChange.wasOwner() && !ownershipChange.isOwner() && ownershipChange.hasOwner()) {
                // MASTER -> SLAVE
                rolePropagationFx = roleContext.onRoleChanged(oldRole, newRole);
                txProcessCallback = makeTxEntitySuspendCallback(roleContext);
            } else if (!ownershipChange.wasOwner() && ownershipChange.isOwner() && ownershipChange.hasOwner()) {
                // SLAVE -> MASTER
                txProcessCallback = makeTxEntitySetupCallback(roleContext);
            } else {
                LOG.debug("Main candidate role change case not covered: {} -> {} .. NOOP", oldRole, newRole);
                txProcessCallback = null;
            }

            if (txProcessCallback != null) {
                rolePropagationFx = Futures.transform(rolePropagationFx, txProcessCallback);
            }

            // catching result
            Futures.addCallback(rolePropagationFx, new FutureCallback<Void>() {
                @Override
                public void onSuccess(@Nullable final Void aVoid) {
                    LOG.debug("Role of main candidate successfully propagated: {}, {} -> {}",
                            ownershipChange.getEntity(), oldRole, newRole);
                    mainCandidateGuard.release();
                }

                @Override
                public void onFailure(final Throwable throwable) {
                    LOG.warn("Main candidate role propagation FAILED for entity: {}, {} -> {}",
                            ownershipChange.getEntity(), oldRole, newRole, throwable);
                    mainCandidateGuard.release();
                    roleContext.getDeviceContext().close();
                }
            });

        } else {
            LOG.debug("We are closing connection for entity {}", ownershipChange.getEntity());
            mainCandidateGuard.release();
            // expecting that this roleContext will get closed in a moment
            // FIXME: reconsider location of following cleanup logic
            if (!ownershipChange.hasOwner() && !ownershipChange.isOwner() && ownershipChange.wasOwner()) {
                unregistrationHelper(ownershipChange, roleContext);
            } else if (ownershipChange.hasOwner() && !ownershipChange.isOwner() && ownershipChange.wasOwner()) {
                contexts.remove(ownershipChange.getEntity(), roleContext);
                roleContext.suspendTxCandidate();
            } else {
                LOG.info("Unexpected role change msg {} for entity {}", ownershipChange, ownershipChange.getEntity());
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


    private void unregistrationHelper(final EntityOwnershipChange ownershipChange, final RoleContext roleContext) {
        LOG.info("Initiate removal from operational. Possibly the last node to be disconnected for :{}. ", ownershipChange);
        Futures.addCallback(removeDeviceFromOperDS(roleContext), new FutureCallback<Void>() {
            @Override
            public void onSuccess(@Nullable final Void aVoid) {
                LOG.debug("Removing context for device: {}", roleContext.getDeviceState().getNodeId());
                contexts.remove(ownershipChange.getEntity(), roleContext);
                roleContext.suspendTxCandidate();
            }

            @Override
            public void onFailure(final Throwable throwable) {
                LOG.warn("Removing role context for device: {}, but {}", roleContext.getDeviceState()
                        .getNodeId(), throwable.getMessage());
                contexts.remove(ownershipChange.getEntity(), roleContext);
                roleContext.suspendTxCandidate();
            }
        });
    }
}
