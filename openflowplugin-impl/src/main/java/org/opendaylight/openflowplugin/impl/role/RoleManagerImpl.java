/**
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.role;

import com.google.common.base.Preconditions;
import com.google.common.base.Verify;
import com.google.common.collect.Iterators;
import com.google.common.util.concurrent.CheckedFuture;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import io.netty.util.Timeout;
import io.netty.util.TimerTask;
import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;
import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.WriteTransaction;
import org.opendaylight.controller.md.sal.common.api.clustering.Entity;
import org.opendaylight.controller.md.sal.common.api.clustering.EntityOwnershipChange;
import org.opendaylight.controller.md.sal.common.api.clustering.EntityOwnershipListener;
import org.opendaylight.controller.md.sal.common.api.clustering.EntityOwnershipListenerRegistration;
import org.opendaylight.controller.md.sal.common.api.clustering.EntityOwnershipService;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.controller.md.sal.common.api.data.TransactionCommitFailedException;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceContext;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceState;
import org.opendaylight.openflowplugin.api.openflow.device.handlers.DeviceInitializationPhaseHandler;
import org.opendaylight.openflowplugin.api.openflow.device.handlers.DeviceTerminationPhaseHandler;
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
    private DeviceTerminationPhaseHandler deviceTerminationPhaseHandler;
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
        roleContext.initializationRoleContext();
        deviceInitializationPhaseHandler.onDeviceContextLevelUp(deviceContext);
    }

    @Override
    public void close() {
        entityOwnershipListenerRegistration.close();
        txEntityOwnershipListenerRegistration.close();
        for (final Iterator<RoleContext> iterator = Iterators.consumingIterator(contexts.values().iterator()); iterator.hasNext();) {
            // got here because last known role is LEADER and DS might need clearing up
            final RoleContext roleCtx = iterator.next();
            final NodeId nodeId = roleCtx.getDeviceState().getNodeId();
            if (OfpRole.BECOMEMASTER.equals(roleCtx.getClusterRole())) {
                LOG.debug("Last role is LEADER and ownershipService returned hasOwner=false for node: {}; "
                        + "cleaning DS as being probably the last owner", nodeId);
                removeDeviceFromOperDS(roleCtx);
            } else {
                // NOOP - there is another owner
                LOG.debug("Last role is LEADER and ownershipService returned hasOwner=true for node: {}; "
                        + "leaving DS untouched", nodeId);
            }
            txContexts.remove(roleCtx.getTxEntity(), roleCtx);
            roleCtx.close();
        }
    }

    @Override
    public void onDeviceContextLevelDown(final DeviceContext deviceContext) {
        final NodeId nodeId = deviceContext.getDeviceState().getNodeId();
        LOG.trace("onDeviceContextLevelDown for node {}", nodeId);
        final Entity entity = makeEntity(nodeId);
        final RoleContext roleContext = contexts.get(entity);
        if (roleContext != null) {
            LOG.debug("Found roleContext associated to deviceContext: {}, now closing the roleContext", nodeId);
            roleContext.terminationRoleContext();
            final TimerTask timerTask = new TimerTask() {

                @Override
                public void run(final Timeout timeout) throws Exception {
                    final RoleContext foundMainRoleCtx = contexts.get(roleContext.getEntity());
                    final RoleContext foundTxRoleCtx = txContexts.get(roleContext.getTxEntity());

                    if (roleContext.equals(foundMainRoleCtx)) {
                        LOG.info("OldRoleCtx was not remove for entity {} from contexts", roleContext.getEntity());
                        contexts.remove(roleContext.getEntity(), roleContext);
                        foundMainRoleCtx.close();
                    }

                    if (roleContext.equals(foundTxRoleCtx)) {
                        LOG.info("OldRoleCtx was not remove for txEntity {} from contexts", roleContext.getTxEntity());
                        txContexts.remove(roleContext.getTxEntity(), roleContext);
                        foundTxRoleCtx.close();
                    }
                }
            };
            deviceContext.getTimer().newTimeout(timerTask, 10, TimeUnit.SECONDS);
        }
        deviceTerminationPhaseHandler.onDeviceContextLevelDown(deviceContext);
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
                changeOwnershipForMainEntity(ownershipChange, roleContext);
                return;
            }

            roleContext = txContexts.get(ownershipChange.getEntity());
            if (roleContext != null) {
                changeOwnershipForTxEntity(ownershipChange, roleContext);
                return;
            }
        } catch (final Exception e) {
            LOG.warn("fail to acquire semaphore: {}", ownershipChange.getEntity(), e);
            if (roleContext != null) {
                roleContext.getDeviceContext().shutdownConnection();
            }
        }

        LOG.debug("We are not able to find Entity {} ownershipChange {} - disregarding ownership notification",
                ownershipChange.getEntity(), ownershipChange);
    }

    private void changeOwnershipForMainEntity(final EntityOwnershipChange ownershipChange,
            @CheckForNull final RoleContext roleContext) {

        LOG.debug("Received Main-EntityOwnershipChange:{}", ownershipChange);
        Preconditions.checkArgument(roleContext != null);
        if (roleContext.isMainCandidateRegistered()) {
            LOG.debug("Main-EntityOwnershipRegistration is active for entity {}", ownershipChange.getEntity());
            if (!ownershipChange.wasOwner() && ownershipChange.isOwner()) {
                // SLAVE -> MASTER
                txContexts.put(roleContext.getTxEntity(), roleContext);
                roleContext.onDeviceTryToTakeClusterLeadership();
            } else if (ownershipChange.wasOwner() && !ownershipChange.isOwner()) {
                // MASTER -> SLAVE
                roleContext.onDeviceLostClusterLeadership();
            } else if (LOG.isDebugEnabled()) {
                LOG.debug("Not processed Ownership Main Entity {} Event {}", ownershipChange.getEntity(), ownershipChange);
            }
        } else {
            LOG.debug("Main-EntityOwnershipRegistration is not active for entity {}", ownershipChange.getEntity());
            contexts.remove(ownershipChange.getEntity(), roleContext);
            if (!ownershipChange.hasOwner() && !ownershipChange.isOwner() && ownershipChange.wasOwner()) {
                /* Method has to clean all context and registrations */
                unregistrationHelper(ownershipChange, roleContext);
            } else {
                txContexts.remove(roleContext.getTxEntity(), roleContext);
                roleContext.close();
            }
        }
    }

    private void changeOwnershipForTxEntity(final EntityOwnershipChange ownershipChange,
            @Nonnull final RoleContext roleContext) {

        LOG.debug("Received TX-EntityOwnershipChange:{}", ownershipChange);
        Preconditions.checkArgument(roleContext != null);
        if (roleContext.isTxCandidateRegistered()) {
            LOG.debug("Tx-EntityOwnershipRegistration is active for entity {}", ownershipChange.getEntity());
            if (!ownershipChange.wasOwner() && ownershipChange.isOwner()) {
                // SLAVE -> MASTER
                roleContext.onDeviceTakeClusterLeadership();
            } else if (ownershipChange.wasOwner() && !ownershipChange.isOwner()) {
                // MASTER -> SLAVE
                LOG.warn("Tx-EntityOwnershipRegistration unexpected lost Leadership entity {}", ownershipChange.getEntity());
                roleContext.getDeviceContext().shutdownConnection();
            } else {
                LOG.debug("NOOP state transition for TxEntity {} ", roleContext.getTxEntity());
            }
        } else {
            LOG.debug("Tx-EntityOwnershipRegistration is not active for entity {}", ownershipChange.getEntity());
            txContexts.remove(ownershipChange.getEntity(), roleContext);
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
                LOG.warn("Delete Node {} failed.", deviceState.getNodeId(), t);
            }
        });
        return delFuture;
    }

    private void unregistrationHelper(final EntityOwnershipChange ownershipChange, final RoleContext roleContext) {
        LOG.info("Initiate removal from operational. Possibly the last node to be disconnected for :{}. ",
                ownershipChange);
        Futures.addCallback(removeDeviceFromOperDS(roleContext), new FutureCallback<Void>() {
            @Override
            public void onSuccess(final Void aVoid) {
                LOG.debug("Removing context for device: {}", roleContext.getDeviceState().getNodeId());
                txContexts.remove(roleContext.getTxEntity(), roleContext);
                roleContext.close();
            }

            @Override
            public void onFailure(final Throwable throwable) {
                LOG.warn("Removing role context for device: {}, but {}", roleContext.getDeviceState().getNodeId(),
                        throwable.getMessage());
                txContexts.remove(roleContext.getTxEntity(), roleContext);
                roleContext.close();
            }
        });
    }

    @Override
    public void setDeviceTerminationPhaseHandler(final DeviceTerminationPhaseHandler handler) {
        deviceTerminationPhaseHandler = handler;
    }
}
