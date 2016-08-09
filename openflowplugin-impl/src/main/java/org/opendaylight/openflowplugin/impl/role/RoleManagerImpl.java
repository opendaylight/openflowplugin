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
import com.google.common.collect.Iterators;
import com.google.common.util.concurrent.CheckedFuture;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.JdkFutureAdapters;
import com.google.common.util.concurrent.ListenableFuture;
import io.netty.util.TimerTask;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.WriteTransaction;
import org.opendaylight.controller.md.sal.common.api.clustering.Entity;
import org.opendaylight.controller.md.sal.common.api.clustering.EntityOwnershipChange;
import org.opendaylight.controller.md.sal.common.api.clustering.EntityOwnershipListener;
import org.opendaylight.controller.md.sal.common.api.clustering.EntityOwnershipListenerRegistration;
import org.opendaylight.controller.md.sal.common.api.clustering.EntityOwnershipService;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.controller.md.sal.common.api.data.TransactionCommitFailedException;
import org.opendaylight.openflowplugin.api.OFConstants;
import org.opendaylight.openflowplugin.api.openflow.OFPContext;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceContext;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceInfo;
import org.opendaylight.openflowplugin.api.openflow.device.handlers.DeviceInitializationPhaseHandler;
import org.opendaylight.openflowplugin.api.openflow.device.handlers.DeviceTerminationPhaseHandler;
import org.opendaylight.openflowplugin.api.openflow.lifecycle.LifecycleConductor;
import org.opendaylight.openflowplugin.api.openflow.lifecycle.RoleChangeListener;
import org.opendaylight.openflowplugin.api.openflow.lifecycle.ServiceChangeListener;
import org.opendaylight.openflowplugin.api.openflow.role.RoleContext;
import org.opendaylight.openflowplugin.api.openflow.role.RoleManager;
import org.opendaylight.openflowplugin.impl.services.SalRoleServiceImpl;
import org.opendaylight.openflowplugin.impl.util.DeviceStateUtil;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeRef;
import org.opendaylight.yang.gen.v1.urn.opendaylight.role.service.rev150727.OfpRole;
import org.opendaylight.yang.gen.v1.urn.opendaylight.role.service.rev150727.SetRoleInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.role.service.rev150727.SetRoleInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.role.service.rev150727.SetRoleOutput;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Gets invoked from RpcManagerInitial, registers a candidate with EntityOwnershipService.
 * On receipt of the ownership notification, makes an rpc call to SalRoleService.
 *
 * Hands over to StatisticsManager at the end.
 */
public class RoleManagerImpl implements RoleManager, EntityOwnershipListener, ServiceChangeListener {
    private static final Logger LOG = LoggerFactory.getLogger(RoleManagerImpl.class);

    // Maximum limit of timeout retries when cleaning DS, to prevent infinite recursive loops
    private static final int MAX_CLEAN_DS_RETRIES = 3;

    private DeviceInitializationPhaseHandler deviceInitializationPhaseHandler;
    private DeviceTerminationPhaseHandler deviceTerminationPhaseHandler;
    private final DataBroker dataBroker;
    private final EntityOwnershipService entityOwnershipService;
    private final ConcurrentMap<DeviceInfo, RoleContext> contexts = new ConcurrentHashMap<>();
    private final ConcurrentMap<Entity, RoleContext> watchingEntities = new ConcurrentHashMap<>();
    private final EntityOwnershipListenerRegistration entityOwnershipListenerRegistration;
    private final EntityOwnershipListenerRegistration txEntityOwnershipListenerRegistration;
    private List<RoleChangeListener> listeners = new ArrayList<>();

    private final LifecycleConductor conductor;

    public RoleManagerImpl(final EntityOwnershipService entityOwnershipService, final DataBroker dataBroker, final LifecycleConductor lifecycleConductor) {
        this.entityOwnershipService = Preconditions.checkNotNull(entityOwnershipService);
        this.dataBroker = Preconditions.checkNotNull(dataBroker);
        this.entityOwnershipListenerRegistration = Preconditions.checkNotNull(entityOwnershipService.registerListener(RoleManager.ENTITY_TYPE, this));
        this.txEntityOwnershipListenerRegistration = Preconditions.checkNotNull(entityOwnershipService.registerListener(TX_ENTITY_TYPE, this));
        this.conductor = lifecycleConductor;
        LOG.debug("Register OpenflowOwnershipListener to all entity ownership changes");
    }

    @Override
    public void setDeviceInitializationPhaseHandler(final DeviceInitializationPhaseHandler handler) {
        deviceInitializationPhaseHandler = handler;
    }

    @Override
    public void onDeviceContextLevelUp(@CheckForNull final DeviceInfo deviceInfo) throws Exception {
        final DeviceContext deviceContext = Preconditions.checkNotNull(conductor.getDeviceContext(deviceInfo));
        final RoleContext roleContext = new RoleContextImpl(deviceInfo, entityOwnershipService, makeEntity(deviceInfo.getNodeId()), makeTxEntity(deviceInfo.getNodeId()), conductor);
        roleContext.setSalRoleService(new SalRoleServiceImpl(roleContext, deviceContext));
        Verify.verify(contexts.putIfAbsent(deviceInfo, roleContext) == null, "Role context for master Node %s is still not closed.", deviceInfo.getNodeId());
        makeDeviceRoleChange(OfpRole.BECOMESLAVE, roleContext, true);
        /* First start to watch entity so we don't miss any notification, and then try to register in EOS */
        watchingEntities.put(roleContext.getEntity(), roleContext);
        notifyListenersRoleInitializationDone(roleContext.getDeviceInfo(), roleContext.initialization());
        deviceInitializationPhaseHandler.onDeviceContextLevelUp(deviceInfo);
    }

    @Override
    public void close() {
        LOG.debug("Close method on role manager was called.");
        entityOwnershipListenerRegistration.close();
        txEntityOwnershipListenerRegistration.close();
        for (final Iterator<RoleContext> iterator = Iterators.consumingIterator(contexts.values().iterator()); iterator.hasNext();) {
            // got here because last known role is LEADER and DS might need clearing up
            final RoleContext roleContext = iterator.next();
            watchingEntities.remove(roleContext.getEntity());
            watchingEntities.remove(roleContext.getTxEntity());
            contexts.remove(roleContext.getDeviceInfo());
            if (roleContext.isTxCandidateRegistered()) {
                LOG.info("Node {} was holder txEntity, so trying to remove device from operational DS.", roleContext.getDeviceInfo().getNodeId().getValue());
                removeDeviceFromOperationalDS(roleContext.getDeviceInfo(), MAX_CLEAN_DS_RETRIES);
            }
            roleContext.close();
        }
    }

    @Override
    public void onDeviceContextLevelDown(final DeviceInfo deviceInfo) {
        LOG.trace("onDeviceContextLevelDown for node {}", deviceInfo.getNodeId());
        final RoleContext roleContext = contexts.remove(deviceInfo);
        if (roleContext != null) {
            LOG.debug("Found roleContext associated to deviceContext: {}, now trying close the roleContext", deviceInfo.getNodeId());
            roleContext.setState(OFPContext.CONTEXT_STATE.TERMINATION);
            roleContext.unregisterCandidate(roleContext.getEntity());
            if (roleContext.isTxCandidateRegistered()) {
                LOG.info("Node {} was holder txEntity, so trying to remove device from operational DS.", deviceInfo.getNodeId().getValue());
                removeDeviceFromOperationalDS(roleContext.getDeviceInfo(), MAX_CLEAN_DS_RETRIES);
            }
            roleContext.close();
        }
        deviceTerminationPhaseHandler.onDeviceContextLevelDown(deviceInfo);
    }

    @VisibleForTesting
    static Entity makeEntity(final NodeId nodeId) {
        return new Entity(RoleManager.ENTITY_TYPE, nodeId.getValue());
    }

    @VisibleForTesting
    static Entity makeTxEntity(final NodeId nodeId) {
        return new Entity(RoleManager.TX_ENTITY_TYPE, nodeId.getValue());
    }

    @Override
    public void ownershipChanged(final EntityOwnershipChange ownershipChange) {

        Preconditions.checkArgument(ownershipChange != null);
        final RoleContext roleContext = watchingEntities.get(ownershipChange.getEntity());

        if (Objects.nonNull(roleContext) && !roleContext.getState().equals(OFPContext.CONTEXT_STATE.TERMINATION)) {

            LOG.debug("Received EOS message: wasOwner:{} isOwner:{} hasOwner:{} inJeopardy:{} for entity type {} and node {}",
                    ownershipChange.wasOwner(), ownershipChange.isOwner(), ownershipChange.hasOwner(), ownershipChange.inJeopardy(),
                    ownershipChange.getEntity().getType(),
                    roleContext.getDeviceInfo().getNodeId());

            if (ownershipChange.getEntity().equals(roleContext.getEntity())) {
                changeOwnershipForMainEntity(ownershipChange, roleContext);
            } else {
                changeOwnershipForTxEntity(ownershipChange, roleContext);
            }

        } else {

            LOG.debug("Role context for entity type {} is in state closing, disregarding ownership change notification.", ownershipChange.getEntity().getType());
            watchingEntities.remove(ownershipChange.getEntity());

        }

    }

    @VisibleForTesting
    void changeOwnershipForMainEntity(final EntityOwnershipChange ownershipChange, final RoleContext roleContext) {

        if (roleContext.isMainCandidateRegistered()) {
            LOG.debug("Main-EntityOwnershipRegistration is active for entity type {} and node {}",
                    ownershipChange.getEntity().getType(), roleContext.getDeviceInfo().getNodeId());
            if (!ownershipChange.wasOwner() && ownershipChange.isOwner() && !ownershipChange.inJeopardy()) {
                // SLAVE -> MASTER
                LOG.debug("SLAVE to MASTER for node {}", roleContext.getDeviceInfo().getNodeId());
                if (roleContext.registerCandidate(roleContext.getTxEntity())) {
                    LOG.debug("Starting watching tx entity for node {}", roleContext.getDeviceInfo().getNodeId());
                    watchingEntities.putIfAbsent(roleContext.getTxEntity(), roleContext);
                }
            } else if ((ownershipChange.wasOwner() && !ownershipChange.isOwner()) || (ownershipChange.inJeopardy())) {
                // MASTER -> SLAVE
                LOG.debug("MASTER to SLAVE for node {}", roleContext.getDeviceInfo().getNodeId());
                conductor.addOneTimeListenerWhenServicesChangesDone(this, roleContext.getDeviceInfo());
                makeDeviceRoleChange(OfpRole.BECOMESLAVE, roleContext, false);
            }
        } else {
            LOG.debug("Main-EntityOwnershipRegistration is not active for entity type {} and node {}",
                    ownershipChange.getEntity(), roleContext.getDeviceInfo().getNodeId());
            watchingEntities.remove(ownershipChange.getEntity(), roleContext);
            if (roleContext.isTxCandidateRegistered()) {
                LOG.debug("tx candidate still registered for node {}, probably connection lost, trying to unregister tx candidate", roleContext.getDeviceInfo().getNodeId());
                roleContext.unregisterCandidate(roleContext.getTxEntity());
                if (ownershipChange.wasOwner() && !ownershipChange.isOwner() && !ownershipChange.hasOwner()) {
                    LOG.debug("Trying to remove from operational node: {}", roleContext.getDeviceInfo().getNodeId());
                    removeDeviceFromOperationalDS(roleContext.getDeviceInfo(), MAX_CLEAN_DS_RETRIES);
                }
            } else {
                contexts.remove(roleContext.getDeviceInfo(), roleContext);
                roleContext.close();
                conductor.closeConnection(roleContext.getDeviceInfo());
            }
        }
    }

    @VisibleForTesting
    void changeOwnershipForTxEntity(final EntityOwnershipChange ownershipChange,
            @Nonnull final RoleContext roleContext) {

        if (roleContext.isTxCandidateRegistered()) {
            LOG.debug("Tx-EntityOwnershipRegistration is active for entity type {} and node {}",
                    ownershipChange.getEntity().getType(),
                    roleContext.getDeviceInfo().getNodeId());
            if (ownershipChange.inJeopardy()) {
                LOG.warn("Getting 'inJeopardy' flag from EOS. Removing txCandidate and stopping watching txCandidate.");
                watchingEntities.remove(roleContext.getTxEntity());
                roleContext.unregisterCandidate(roleContext.getTxEntity());
            } else {
                if (!ownershipChange.wasOwner() && ownershipChange.isOwner()) {
                    // SLAVE -> MASTER
                    LOG.debug("SLAVE to MASTER for node {}", roleContext.getDeviceInfo().getNodeId());
                    makeDeviceRoleChange(OfpRole.BECOMEMASTER, roleContext, false);
                } else if (ownershipChange.wasOwner() && !ownershipChange.isOwner()) {
                    // MASTER -> SLAVE
                    LOG.debug("MASTER to SLAVE for node {}", roleContext.getDeviceInfo().getNodeId());
                    LOG.warn("Tx-EntityOwnershipRegistration lost leadership entity type {} and node {}",
                            ownershipChange.getEntity().getType(), roleContext.getDeviceInfo().getNodeId());
                    watchingEntities.remove(roleContext.getTxEntity(), roleContext);
                    watchingEntities.remove(roleContext.getEntity(), roleContext);
                    roleContext.unregisterCandidate(roleContext.getEntity());
                    roleContext.unregisterCandidate(roleContext.getTxEntity());
                    if (!ownershipChange.hasOwner()) {
                        LOG.debug("Trying to remove from operational node: {}", roleContext.getDeviceInfo().getNodeId());
                        removeDeviceFromOperationalDS(roleContext.getDeviceInfo(), MAX_CLEAN_DS_RETRIES);
                    } else {
                        contexts.remove(roleContext.getDeviceInfo(), roleContext);
                        roleContext.close();
                        conductor.closeConnection(roleContext.getDeviceInfo());
                    }
                }
            }
        } else {
            LOG.debug("Tx-EntityOwnershipRegistration is not active for entity type {} and node {}", ownershipChange.getEntity().getType(), roleContext.getDeviceInfo().getNodeId());
            watchingEntities.remove(roleContext.getTxEntity(), roleContext);
            contexts.remove(roleContext.getDeviceInfo(), roleContext);
            roleContext.close();
            conductor.closeConnection(roleContext.getDeviceInfo());
        }
    }

    @VisibleForTesting
    void makeDeviceRoleChange(final OfpRole role, final RoleContext roleContext, final Boolean init) {
        final ListenableFuture<RpcResult<SetRoleOutput>> roleChangeFuture = sendRoleChangeToDevice(role, roleContext);
        Futures.addCallback(roleChangeFuture, new FutureCallback<RpcResult<SetRoleOutput>>() {
            @Override
            public void onSuccess(@Nullable final RpcResult<SetRoleOutput> setRoleOutputRpcResult) {
                LOG.info("Role {} successfully set on device {}", role, roleContext.getDeviceInfo().getNodeId());
                if (!init) {
                    notifyListenersRoleChangeOnDevice(roleContext.getDeviceInfo(), role);
                }
            }

            @Override
            public void onFailure(@Nonnull final Throwable throwable) {
                LOG.warn("Unable to set role {} on device {}", role, roleContext.getDeviceInfo().getNodeId());
                conductor.closeConnection(roleContext.getDeviceInfo());
            }
        });
    }

    @VisibleForTesting
    ListenableFuture<RpcResult<SetRoleOutput>> sendRoleChangeToDevice(final OfpRole newRole, final RoleContext roleContext) {
        LOG.debug("Sending new role {} to device {}", newRole, roleContext.getDeviceInfo().getNodeId());
        final Future<RpcResult<SetRoleOutput>> setRoleOutputFuture;
        final Short version = roleContext.getDeviceInfo().getVersion();
        if (null == version) {
            LOG.debug("Device version is null");
            return Futures.immediateFuture(null);
        }
        if (version < OFConstants.OFP_VERSION_1_3) {
            LOG.debug("Device version not support ROLE");
            return Futures.immediateFuture(null);
        } else {
            final SetRoleInput setRoleInput = (new SetRoleInputBuilder()).setControllerRole(newRole)
                    .setNode(new NodeRef(DeviceStateUtil.createNodeInstanceIdentifier(roleContext.getDeviceInfo().getNodeId()))).build();
            setRoleOutputFuture = roleContext.getSalRoleService().setRole(setRoleInput);
            final TimerTask timerTask = timeout -> {
                if (!setRoleOutputFuture.isDone()) {
                    LOG.warn("New role {} was not propagated to device {} during 10 sec", newRole, roleContext.getDeviceInfo().getNodeId());
                    setRoleOutputFuture.cancel(true);
                }
            };
            conductor.newTimeout(timerTask, 10, TimeUnit.SECONDS);
        }
        return JdkFutureAdapters.listenInPoolThread(setRoleOutputFuture);
    }

    @VisibleForTesting
    CheckedFuture<Void, TransactionCommitFailedException> removeDeviceFromOperationalDS(final DeviceInfo deviceInfo, final int numRetries) {
        final WriteTransaction delWtx = dataBroker.newWriteOnlyTransaction();
        delWtx.delete(LogicalDatastoreType.OPERATIONAL, DeviceStateUtil.createNodeInstanceIdentifier(deviceInfo.getNodeId()));
        final CheckedFuture<Void, TransactionCommitFailedException> delFuture = delWtx.submit();

        Futures.addCallback(delFuture, new FutureCallback<Void>() {
            @Override
            public void onSuccess(final Void result) {
                LOG.debug("Delete Node {} was successful", deviceInfo);
                final RoleContext roleContext = contexts.remove(deviceInfo);
                if (roleContext != null) {
                    roleContext.close();
                }
            }

            @Override
            public void onFailure(@Nonnull final Throwable t) {
                // If we have any retries left, we will try to clean the datastore again
                if (numRetries > 0) {
                    // We "used" one retry here, so decrement it
                    final int curRetries = numRetries - 1;
                    LOG.debug("Delete node {} failed with exception {}. Trying again (retries left: {})", deviceInfo.getNodeId(), t, curRetries);
                    // Recursive call to this method with "one less" retry
                    removeDeviceFromOperationalDS(deviceInfo, curRetries);
                    return;
                }

                // No retries left, so we will just close the role context, and ignore datastore cleanup
                LOG.warn("Delete node {} failed with exception {}. No retries left, aborting", deviceInfo.getNodeId(), t);
                final RoleContext roleContext = contexts.remove(deviceInfo);
                if (roleContext != null) {
                    roleContext.close();
                }
            }
        });

        return delFuture;
    }

    @Override
    public void setDeviceTerminationPhaseHandler(final DeviceTerminationPhaseHandler handler) {
        deviceTerminationPhaseHandler = handler;
    }

    @Override
    public void servicesChangeDone(final DeviceInfo deviceInfo, final boolean success) {
        LOG.debug("Services stopping done for node {} as " + (success ? "successful" : "unsuccessful"), deviceInfo);
        final RoleContext roleContext = contexts.get(deviceInfo);
        if (null != roleContext) {
            /* Services stopped or failure */
            roleContext.unregisterCandidate(roleContext.getTxEntity());
        }
    }

    @VisibleForTesting
    RoleContext getRoleContext(final DeviceInfo deviceInfo){
        return contexts.get(deviceInfo);
    }

    /**
     * This method is only for testing
     */
    @VisibleForTesting
    void setRoleContext(DeviceInfo deviceInfo, RoleContext roleContext){
        if(!contexts.containsKey(deviceInfo)) {
            contexts.put(deviceInfo, roleContext);
        }
    }

    @Override
    public void addRoleChangeListener(final RoleChangeListener roleChangeListener) {
        this.listeners.add(roleChangeListener);
    }

    /**
     * Invoked when initialization phase is done
     * @param deviceInfo node identification
     * @param success true if initialization done ok, false otherwise
     */
    @VisibleForTesting
    void notifyListenersRoleInitializationDone(final DeviceInfo deviceInfo, final boolean success){
        LOG.debug("Notifying registered listeners for role initialization done, no. of listeners {}", listeners.size());
        for (final RoleChangeListener listener : listeners) {
            listener.roleInitializationDone(deviceInfo, success);
        }
    }

    /**
     * Notifies registered listener on role change. Role is the new role on device
     * If initialization phase is true, we may skip service starting
     * @param deviceInfo
     * @param role new role meant to be set on device
     */
    @VisibleForTesting
    void notifyListenersRoleChangeOnDevice(final DeviceInfo deviceInfo, final OfpRole role){
        LOG.debug("Notifying registered listeners for role change, no. of listeners {}", listeners.size());
        for (final RoleChangeListener listener : listeners) {
            listener.roleChangeOnDevice(deviceInfo, role);
        }
    }

    @Override
    public <T extends OFPContext> T gainContext(DeviceInfo deviceInfo) {
        return (T) contexts.get(deviceInfo);
    }
}
