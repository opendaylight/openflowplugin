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
import io.netty.util.Timeout;
import io.netty.util.TimerTask;
import java.util.Iterator;
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
import org.opendaylight.openflowplugin.api.openflow.device.DeviceContext;
import org.opendaylight.openflowplugin.api.openflow.device.handlers.DeviceInitializationPhaseHandler;
import org.opendaylight.openflowplugin.api.openflow.device.handlers.DeviceTerminationPhaseHandler;
import org.opendaylight.openflowplugin.api.openflow.lifecycle.ServiceChangeListener;
import org.opendaylight.openflowplugin.api.openflow.role.RoleContext;
import org.opendaylight.openflowplugin.api.openflow.role.RoleManager;
import org.opendaylight.openflowplugin.impl.LifecycleConductor;
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
 * On receipt of the ownership notification, makes an rpc call to SalRoleSevice.
 *
 * Hands over to StatisticsManager at the end.
 */
public class RoleManagerImpl implements RoleManager, EntityOwnershipListener, ServiceChangeListener {
    private static final Logger LOG = LoggerFactory.getLogger(RoleManagerImpl.class);

    private DeviceInitializationPhaseHandler deviceInitializationPhaseHandler;
    private DeviceTerminationPhaseHandler deviceTerminationPhaseHandler;
    private final DataBroker dataBroker;
    private final EntityOwnershipService entityOwnershipService;
    private final ConcurrentMap<NodeId, RoleContext> contexts = new ConcurrentHashMap<>();
    private final ConcurrentMap<Entity, RoleContext> watchingEntities = new ConcurrentHashMap<>();
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
        NodeId nodeId = deviceContext.getPrimaryConnectionContext().getNodeId();
        LOG.trace("Role manager called for device:{}", nodeId);
        final RoleContext roleContext =
                new RoleContextBuilder(nodeId, entityOwnershipService, makeEntity(nodeId), makeTxEntity(nodeId))
                        .setSalRoleService(deviceContext)
                        .addListener(LifecycleConductor.getInstance()).build();

        Verify.verify(contexts.putIfAbsent(nodeId, roleContext) == null, "Role context for master Node {} is still not closed.", nodeId);
        deviceContext.addDeviceContextClosedHandler(this);
        makeDeviceRoleChange(OfpRole.BECOMESLAVE, roleContext, true);
        roleContext.notifyListenersRoleInitializationDone(roleContext.initialization());
        watchingEntities.putIfAbsent(roleContext.getEntity(), roleContext);
        deviceInitializationPhaseHandler.onDeviceContextLevelUp(deviceContext);
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
            contexts.remove(roleContext.getNodeId());
            if (roleContext.isTxCandidateRegistered()) {
                LOG.info("Node {} was holder txEntity, so trying to remove device from operational DS.");
                removeDeviceFromOperationalDS(roleContext.getNodeId());
            }
            roleContext.close();
        }
    }

    @Override
    public void onDeviceContextLevelDown(final DeviceContext deviceContext) {
        final NodeId nodeId = deviceContext.getPrimaryConnectionContext().getNodeId();
        LOG.trace("onDeviceContextLevelDown for node {}", nodeId);
        final RoleContext roleContext = contexts.get(nodeId);
        if (roleContext != null) {
            LOG.debug("Found roleContext associated to deviceContext: {}, now trying close the roleContext", nodeId);
            watchingEntities.remove(roleContext.getTxEntity(), roleContext);
            if (!roleContext.isMaster()) {
                watchingEntities.remove(roleContext.getEntity(), roleContext);
            } else {
                LOG.debug("Node {} is/was master, so need to wait for EOS message. Stop watching only tx registration",nodeId);
            }
            contexts.remove(nodeId, roleContext);
            roleContext.close();
        }
        deviceTerminationPhaseHandler.onDeviceContextLevelDown(deviceContext);
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
        RoleContext roleContext = null;

        roleContext = watchingEntities.get(ownershipChange.getEntity());
        if (roleContext != null) {
            if (ownershipChange.getEntity().equals(roleContext.getEntity())) {
                changeOwnershipForMainEntity(ownershipChange, roleContext);
            } else {
                changeOwnershipForTxEntity(ownershipChange, roleContext);
            }
            return;
        }

        LOG.debug("We are not able to find Entity {} ownershipChange {} - disregarding ownership notification",
                ownershipChange.getEntity().getType(), ownershipChange);
    }

    @VisibleForTesting
    void changeOwnershipForMainEntity(final EntityOwnershipChange ownershipChange, final RoleContext roleContext) {

        LOG.debug("Received Main-EntityOwnershipChange:{}", ownershipChange);
        if (roleContext.isMainCandidateRegistered()) {
            LOG.debug("Main-EntityOwnershipRegistration is active for entity {}", ownershipChange.getEntity());
            if (!ownershipChange.wasOwner() && ownershipChange.isOwner()) {
                // SLAVE -> MASTER
                LOG.debug("SLAVE to MASTER for node {}", roleContext.getNodeId());
                if (roleContext.registerCandidate(roleContext.getTxEntity())) {
                    LOG.debug("Starting watching tx entity for node {}", roleContext.getNodeId());
                    watchingEntities.putIfAbsent(roleContext.getTxEntity(), roleContext);
                }
            } else if (ownershipChange.wasOwner() && !ownershipChange.isOwner()) {
                // MASTER -> SLAVE
                LOG.debug("MASTER to SLAVE for node {}", roleContext.getNodeId());
                if (roleContext.unregisterCandidate(roleContext.getEntity())) {
                    watchingEntities.remove(roleContext.getEntity(), roleContext);
                    makeDeviceRoleChange(OfpRole.BECOMESLAVE, roleContext, false);
                    LifecycleConductor.getInstance().notifyMeWhenServicesChangesDone(this, roleContext.getNodeId());
                }
            }
        } else {
            LOG.debug("Main-EntityOwnershipRegistration is not active for entity {}", ownershipChange.getEntity());
            final Entity entity = roleContext.getEntity();
            final Entity txEntity = roleContext.getTxEntity();
            final NodeId nodeId = roleContext.getNodeId();
            watchingEntities.remove(ownershipChange.getEntity(), roleContext);
            watchingEntities.remove(txEntity, roleContext);
            if (!ownershipChange.hasOwner() && !ownershipChange.isOwner() && ownershipChange.wasOwner()) {
                /* Method has to clean all context and registrations */
                removeDeviceFromOperationalDS(nodeId);
            }
            roleContext.unregisterCandidate(entity);
            roleContext.unregisterCandidate(txEntity);
            contexts.remove(nodeId, roleContext);
            roleContext.close();
        }
    }

    @VisibleForTesting
    void changeOwnershipForTxEntity(final EntityOwnershipChange ownershipChange,
            @Nonnull final RoleContext roleContext) {

        LOG.debug("Received TX-EntityOwnershipChange:{}", ownershipChange);
        if (roleContext.isTxCandidateRegistered()) {
            LOG.debug("Tx-EntityOwnershipRegistration is active for entity {}", ownershipChange.getEntity().getType());
            if (!ownershipChange.wasOwner() && ownershipChange.isOwner()) {
                // SLAVE -> MASTER
                LOG.debug("SLAVE to MASTER for node {}", roleContext.getNodeId());
                makeDeviceRoleChange(OfpRole.BECOMEMASTER, roleContext,false);
            } else if (ownershipChange.wasOwner() && !ownershipChange.isOwner()) {
                // MASTER -> SLAVE
                LOG.debug("MASTER to SLAVE for node {}", roleContext.getNodeId());
                LOG.warn("Tx-EntityOwnershipRegistration unexpected lost leadership entity {}", ownershipChange.getEntity().getType());
                roleContext.unregisterCandidate(roleContext.getEntity());
                roleContext.unregisterCandidate(roleContext.getTxEntity());
                watchingEntities.remove(roleContext.getTxEntity(), roleContext);
                watchingEntities.remove(roleContext.getEntity(), roleContext);
                contexts.remove(roleContext.getNodeId(), roleContext);
                roleContext.close();
            }
        } else {
            LOG.debug("Tx-EntityOwnershipRegistration is not active for entity {}", ownershipChange.getEntity());
            watchingEntities.remove(roleContext.getTxEntity(), roleContext);
        }
    }

    @VisibleForTesting
    void makeDeviceRoleChange(final OfpRole role, final RoleContext roleContext, final Boolean init) {
        ListenableFuture<RpcResult<SetRoleOutput>> roleChangeFuture = sendRoleChangeToDevice(role, roleContext);
        Futures.addCallback(roleChangeFuture, new FutureCallback<RpcResult<SetRoleOutput>>() {
            @Override
            public void onSuccess(@Nullable final RpcResult<SetRoleOutput> setRoleOutputRpcResult) {
                LOG.info("Role {} successfully set on device {}", role, roleContext.getNodeId());
                roleContext.notifyListenersRoleChangeOnDevice(true, role, init);
            }

            @Override
            public void onFailure(final Throwable throwable) {
                LOG.warn("Unable to set role {} on device {}", role, roleContext.getNodeId());
                roleContext.notifyListenersRoleChangeOnDevice(false, role, init);
            }
        });
    }


    private ListenableFuture<RpcResult<SetRoleOutput>> sendRoleChangeToDevice(final OfpRole newRole, final RoleContext roleContext) {
        LOG.debug("Sending new role {} to device {}", newRole, roleContext.getNodeId());
        final Future<RpcResult<SetRoleOutput>> setRoleOutputFuture;
        final Short version = LifecycleConductor.getInstance().getVersion(roleContext.getNodeId());
        if (null == version) {
            LOG.debug("Device version is null");
            return Futures.immediateFuture(null);
        }
        if (version < OFConstants.OFP_VERSION_1_3) {
            LOG.debug("Device version not support ROLE");
            return Futures.immediateFuture(null);
        } else {
            final SetRoleInput setRoleInput = (new SetRoleInputBuilder()).setControllerRole(newRole)
                    .setNode(new NodeRef(DeviceStateUtil.createNodeInstanceIdentifier(roleContext.getNodeId()))).build();
            setRoleOutputFuture = roleContext.getSalRoleService().setRole(setRoleInput);
            final TimerTask timerTask = new TimerTask() {

                @Override
                public void run(final Timeout timeout) throws Exception {
                    if (!setRoleOutputFuture.isDone()) {
                        LOG.warn("New role {} was not propagated to device {} during 10 sec", newRole, roleContext.getNodeId());
                        setRoleOutputFuture.cancel(true);
                    }
                }
            };
            LifecycleConductor.getInstance().getTimer().newTimeout(timerTask, 10, TimeUnit.SECONDS);
        }
        return JdkFutureAdapters.listenInPoolThread(setRoleOutputFuture);
    }

    @VisibleForTesting
    CheckedFuture<Void, TransactionCommitFailedException> removeDeviceFromOperationalDS(final NodeId nodeId) {

        final WriteTransaction delWtx = dataBroker.newWriteOnlyTransaction();
        delWtx.delete(LogicalDatastoreType.OPERATIONAL, DeviceStateUtil.createNodeInstanceIdentifier(nodeId));
        final CheckedFuture<Void, TransactionCommitFailedException> delFuture = delWtx.submit();
        Futures.addCallback(delFuture, new FutureCallback<Void>() {

            @Override
            public void onSuccess(final Void result) {
                LOG.debug("Delete Node {} was successful", nodeId);
            }

            @Override
            public void onFailure(final Throwable t) {
                LOG.warn("Delete Node {} failed. {}", nodeId, t);
            }
        });
        return delFuture;
    }

    @Override
    public void setDeviceTerminationPhaseHandler(final DeviceTerminationPhaseHandler handler) {
        deviceTerminationPhaseHandler = handler;
    }

    @Override
    public void servicesChangeDone(NodeId nodeId, boolean success) {
        LOG.debug("Services stopping done for node {} as " + (success ? "successful" : "unsuccessful"), nodeId);
        RoleContext roleContext = contexts.get(nodeId);
        if (null != roleContext) {
            /* Stopping services */
            if (roleContext.isTxCandidateRegistered()) {
                LOG.debug("Removing entity {}", roleContext.getTxEntity());
                watchingEntities.remove(roleContext.getTxEntity(), roleContext);
                roleContext.unregisterCandidate(roleContext.getTxEntity());
            }
        }
    }

    @VisibleForTesting
    RoleContext getRoleContext(NodeId nodeId){
        return contexts.get(nodeId);
    }
}
