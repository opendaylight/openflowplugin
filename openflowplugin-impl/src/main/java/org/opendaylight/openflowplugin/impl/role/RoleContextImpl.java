/**
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.role;

import com.google.common.base.Preconditions;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import javax.annotation.Nullable;

import org.opendaylight.controller.md.sal.common.api.clustering.CandidateAlreadyRegisteredException;
import org.opendaylight.controller.md.sal.common.api.clustering.Entity;
import org.opendaylight.controller.md.sal.common.api.clustering.EntityOwnershipCandidateRegistration;
import org.opendaylight.controller.md.sal.common.api.clustering.EntityOwnershipService;
import org.opendaylight.openflowplugin.api.openflow.connection.ConnectionContext;
import org.opendaylight.openflowplugin.api.openflow.device.RequestContext;
import org.opendaylight.openflowplugin.api.openflow.lifecycle.RoleChangeListener;
import org.opendaylight.openflowplugin.api.openflow.role.RoleContext;
import org.opendaylight.openflowplugin.impl.LifecycleConductor;
import org.opendaylight.openflowplugin.impl.rpc.AbstractRequestContext;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.role.service.rev150727.OfpRole;
import org.opendaylight.yang.gen.v1.urn.opendaylight.role.service.rev150727.SalRoleService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.role.service.rev150727.SetRoleOutput;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Rewrote whole role context to prevent errors to change role on cluster
 * @author Jozef Bacigal
 * Date: 9/12/15
 */
class RoleContextImpl implements RoleContext {

    private static final Logger LOG = LoggerFactory.getLogger(RoleContextImpl.class);
    public static final int TIMEOUT = 12;

    private List<RoleChangeListener> listeners = new ArrayList<>();

    private final NodeId nodeId;
    private final EntityOwnershipService entityOwnershipService;
    private volatile EntityOwnershipCandidateRegistration entityOwnershipCandidateRegistration = null;
    private volatile EntityOwnershipCandidateRegistration txEntityOwnershipCandidateRegistration = null;

    private final Entity entity;
    private final Entity txEntity;

    private SalRoleService salRoleService = null;

    private final Semaphore roleChangeGuard = new Semaphore(1, true);

    public RoleContextImpl(NodeId nodeId, EntityOwnershipService entityOwnershipService, Entity entity, Entity txEntity) {
        this.entityOwnershipService = entityOwnershipService;
        this.entity = entity;
        this.txEntity = txEntity;
        this.nodeId = nodeId;
    }

    @Override
    public boolean initialization() {
        LOG.info("Initialization main candidate for node {}", nodeId);
        boolean permit = false;
        try {
            permit = roleChangeGuard.tryAcquire(TIMEOUT, TimeUnit.SECONDS);
            if (permit) {
                entityOwnershipCandidateRegistration = entityOwnershipService.registerCandidate(entity);
            }
        } catch (CandidateAlreadyRegisteredException e) {
            LOG.warn("Main candidate for ownership service already registered for node {}", nodeId);
            return false;
        } catch (InterruptedException e) {
            LOG.warn("Main candidate for ownership cannot be created, another thread interrupted call. Node: {}", nodeId);
            return false;
        } finally {
            if (permit) {
                roleChangeGuard.release();
            }
        }
        return true;
    }

    @Override
    public void termination() {
        LOG.info("Termination ownership candidates for node {}", nodeId);
        boolean permit = false;
        try {
            permit = roleChangeGuard.tryAcquire(TIMEOUT, TimeUnit.SECONDS);
            if (permit) {
                if (txEntityOwnershipCandidateRegistration != null) {
                    txEntityOwnershipCandidateRegistration.close();
                    txEntityOwnershipCandidateRegistration = null;
                }
                if (entityOwnershipCandidateRegistration != null) {
                    entityOwnershipCandidateRegistration.close();
                    entityOwnershipCandidateRegistration = null;
                }
            }
        } catch (InterruptedException e) {
            LOG.warn("Termination process in ownership cannot be done, another thread interrupted call. Node: {}", nodeId);
            if (permit) {
                roleChangeGuard.release();
            }
            return;
        }
    }

    @Nullable
    @Override
    public <T> RequestContext<T> createRequestContext() {
        final AbstractRequestContext<T> ret = new AbstractRequestContext<T>(LifecycleConductor.getInstance().reserveXidForDeviceMessage(nodeId)) {
            @Override
            public void close() {
            }
        };
        return ret;
    }

    @Override
    public void setSalRoleService(final SalRoleService salRoleService) {
        Preconditions.checkNotNull(salRoleService);
        this.salRoleService = salRoleService;
    }

    @Override
    public SalRoleService getSalRoleService() {
        return this.salRoleService;
    }

    @Override
    public void addListener(final RoleChangeListener listener) {
        this.listeners.add(listener);
    }

    public void notifyListenersRoleInitializationDone(final boolean success){
        for (RoleChangeListener listener : listeners) {
            listener.roleInitializationDone(nodeId, success);
        }
    }

    public void notifyListenersRoleChangeOnDevice(final boolean success, final OfpRole role, final boolean initializationPhase){
        for (RoleChangeListener listener : listeners) {
            listener.roleChangeOnDevice(nodeId, success, role, initializationPhase);
        }
    }

//    @Override
//    public void roleChange(final OfpRole newRole, final Entity entity) {
//
//        if (this.clusterRole.equals(newRole) && entity.equals(this.entity)) {
//            LOG.debug("The same role, skipping role change for node {} and entity {}", nodeId, entity.getType());
//            return;
//        }
//        if (this.clusterRoleTx.equals(newRole) && entity.equals(this.txEntity)) {
//            LOG.debug("The same role, skipping role change for node {} and entity {}", nodeId, entity.getType());
//            return;
//        }
//
//        if (entity.equals(this.entity)) {
//            if (newRole.equals(OfpRole.BECOMESLAVE)) {
//                if (null == txEntityOwnershipCandidateRegistration) {
//                    LOG.debug("We were master, but not get txEntity ownership so nothing to do for change for node {}", nodeId);
//                    return;
//                } else {
//                    roleToDevice(newRole, true);
//                    clusterRole = newRole;
//                }
//            } else {
//                if (newRole.equals(OfpRole.BECOMEMASTER)) {
//                    if (null != txEntityOwnershipCandidateRegistration) {
//                        LOG.debug("We were slave, but we get txEntity ownership so nothing to do for change for node {}", nodeId);
//                        return;
//                    } else {
//                        roleToDevice(newRole, false);
//                    }
//                }
//            }
//        } else {
//            if (entity.equals(txEntity)) {
//                if (newRole.equals(OfpRole.BECOMESLAVE)) {
//                    if (null == txEntityOwnershipCandidateRegistration) {
//                        LOG.debug("We try to be slave, but not txEntity ownership so nothing to do for change for node {}", nodeId);
//                        return;
//                    } else {
//                        roleToDevice(newRole, true);
//                        clusterRoleTx = newRole;
//                    }
//                } else {
//                    if (newRole.equals(OfpRole.BECOMEMASTER)) {
//                        if (null != txEntityOwnershipCandidateRegistration) {
//                            LOG.debug("We were slave, but we get txEntity ownership so nothing to do for change for node {}", nodeId);
//                            return;
//                        } else {
//                            ListenableFuture<RpcResult<SetRoleOutput>> future = sendRoleChangeToDevice(newRole);
//                            Futures.addCallback(future, new FutureCallback<RpcResult<SetRoleOutput>>() {
//                                @Override
//                                public void onSuccess(@Nullable RpcResult<SetRoleOutput> setRoleOutputRpcResult) {
//                                    LOG.info("Role {} was successful propagated on device {}", newRole, nodeId);
//                                    setTxCandidate();
//                                    notifyListenersRoleChangeOnDevice(true, newRole, false);
//                                }
//
//                                @Override
//                                public void onFailure(Throwable throwable) {
//                                    LOG.warn("Role {} was NOT successful propagated on device {}", newRole, nodeId);
//                                    suspendTxCandidate();
//                                    notifyListenersRoleChangeOnDevice(false, newRole, false);
//                                }
//                            });
//                        }
//                    }
//                }
//            }
//        }
//    }

//    private void roleToDevice(final OfpRole newRole, final boolean suspend) {
//        ListenableFuture<RpcResult<SetRoleOutput>> future = sendRoleChangeToDevice(newRole);
//        Futures.addCallback(future, new FutureCallback<RpcResult<SetRoleOutput>>() {
//            @Override
//            public void onSuccess(@Nullable RpcResult<SetRoleOutput> setRoleOutputRpcResult) {
//                LOG.info("Role {} was successful propagated on device {}", newRole, nodeId);
//
//                if (suspend) {
//                    suspendTxCandidate();
//                } else {
//                    setTxCandidate();
//                }
//                notifyListenersRoleChangeOnDevice(true, newRole, false);
//            }
//
//            @Override
//            public void onFailure(Throwable throwable) {
//                LOG.warn("Role {} was NOT successful propagated on device {}", newRole, nodeId);
//                suspendTxCandidate();
//                notifyListenersRoleChangeOnDevice(false, newRole, false);
//            }
//        });
//    }

//    private void suspendTxCandidate() {
//        boolean permit = false;
//        try {
//            permit = roleChangeGuard.tryAcquire(TIMEOUT, TimeUnit.SECONDS);
//            if (permit) {
//                if (txEntityOwnershipCandidateRegistration != null) {
//                    txEntityOwnershipCandidateRegistration.close();
//                    txEntityOwnershipCandidateRegistration = null;
//                }
//                clusterRoleTx = OfpRole.BECOMESLAVE;
//            }
//        } catch (final InterruptedException e) {
//            LOG.warn("Cannot acquire role change guard on role change on node {}", nodeId);
//        } finally {
//            if (permit) {
//                roleChangeGuard.release();
//            }
//        }
//    }
//
//    private void setTxCandidate() {
//        boolean permit = false;
//        try {
//            permit = roleChangeGuard.tryAcquire(TIMEOUT, TimeUnit.SECONDS);
//            if (permit) {
//                txEntityOwnershipCandidateRegistration = entityOwnershipService.registerCandidate(txEntity);
//            }
//        } catch (final InterruptedException e) {
//            LOG.warn("Cannot acquire role change guard on role change on node {}", nodeId);
//        } catch (final CandidateAlreadyRegisteredException e) {
//            LOG.warn("tx candidate already registered on node {}", nodeId);
//        } finally {
//            if (permit) {
//                roleChangeGuard.release();
//            }
//        }
//    }

//    public void roleChange2(final OfpRole newRole, final Entity entity) {
//        if (this.clusterRole.equals(newRole)) {
//            LOG.debug("New role {} is the same as actual role {}", newRole, this.clusterRole);
//        } else {
//            if (!ConnectionContext.CONNECTION_STATE.WORKING.equals(LifecycleConductor.getInstance().getConnectionState(nodeId))) {
//                LOG.debug("Connection for node {} was lost.", nodeId);
//                notifyListenersRoleChangeOnDevice(false, newRole, false);
//            }
//            Futures.addCallback(sendRoleChangeToDevice(newRole), new FutureCallback<RpcResult<SetRoleOutput>>() {
//                @Override
//                public void onSuccess(@Nullable final RpcResult<SetRoleOutput> setRoleOutputRpcResult) {
//                    LOG.info("Role {} successfully set on device {}", newRole, nodeId);
//                    clusterRole = newRole;
//                    if (clusterRole.equals(OfpRole.BECOMESLAVE)) {
//                        if (null == entityOwnershipCandidateRegistration) {
//                            LOG.warn("Entity ownership main candidate registration is null for node {}", nodeId);
//                            notifyListenersRoleChangeOnDevice(false, newRole, false);
//                        } else {
//                            boolean permit = false;
//                            try {
//                                permit = roleChangeGuard.tryAcquire(TIMEOUT, TimeUnit.SECONDS);
//                                if (permit) {
//                                    if (null != txEntityOwnershipCandidateRegistration) {
//                                        txEntityOwnershipCandidateRegistration.close();
//                                        txEntityOwnershipCandidateRegistration = null;
//                                    }
//                                    notifyListenersRoleChangeOnDevice(true, newRole);
//                                } else {
//                                    LOG.warn("Tx candidate for ownership cannot be suspended, thread is being hold. Node: {}", nodeId);
//                                    notifyListenersRoleChangeOnDevice(false, newRole);
//                                }
//                            } catch (InterruptedException e) {
//                                LOG.warn("Tx candidate for ownership cannot be created, another thread interrupted call. Node: {}", nodeId);
//                                if (permit) {
//                                    roleChangeGuard.release();
//                                }
//                                notifyListenersRoleChangeOnDevice(false, newRole);
//                            }
//                        }
//                    } else {
//                        if (clusterRole.equals(OfpRole.BECOMEMASTER)
//                                && null != entityOwnershipCandidateRegistration
//                                && null == txEntityOwnershipCandidateRegistration) {
//                            boolean permit = false;
//                            try {
//                                permit = roleChangeGuard.tryAcquire(TIMEOUT, TimeUnit.SECONDS);
//                                if (permit) {
//                                    txEntityOwnershipCandidateRegistration = entityOwnershipService.registerCandidate(txEntity);
//                                    notifyListenersRoleChangeOnDevice(true, newRole);
//                                    LOG.info("Tx candidate for ownership created. Node: {}", nodeId);
//                                } else {
//                                    LOG.warn("Tx candidate for ownership cannot be created, thread is being hold. Node: {}", nodeId);
//                                    notifyListenersRoleChangeOnDevice(false, newRole);
//                                }
//                            } catch (CandidateAlreadyRegisteredException e) {
//                                notifyListenersRoleChangeOnDevice(false, newRole);
//                            } catch (InterruptedException e) {
//                                LOG.warn("Tx candidate for ownership cannot be created, another thread interrupted call. Node: {}", nodeId);
//                                if (permit) {
//                                    roleChangeGuard.release();
//                                }
//                            }
//                        } else {
//                            LOG.warn("Problems during setting master on node {}", nodeId);
//                            notifyListenersRoleChangeOnDevice(false, newRole);
//                        }
//                    }
//                }
//
//                @Override
//                public void onFailure(final Throwable throwable) {
//                    LOG.warn("Failure to set role {} on device {}", newRole, nodeId);
//                    notifyListenersRoleChangeOnDevice(false, newRole);
//                }
//            });
//
//        }
//    }

    @Override
    public Entity getEntity() {
        return this.entity;
    }

    @Override
    public Entity getTxEntity() {
        return this.txEntity;
    }

    @Override
    public NodeId getNodeId() {
        return nodeId;
    }

    @Override
    public boolean isMainCandidateRegistered() {
        return entityOwnershipCandidateRegistration != null;
    }

    @Override
    public boolean isTxCandidateRegistered() {
        return txEntityOwnershipCandidateRegistration != null;
    }

    @Override
    public boolean registerCandidate(final Entity entity_) {
        boolean permit = false;
        try {
            permit = roleChangeGuard.tryAcquire(TIMEOUT, TimeUnit.SECONDS);
            if(permit) {
                if (entity_.equals(this.entity)) {
                    entityOwnershipCandidateRegistration = entityOwnershipService.registerCandidate(entity_);
                } else {
                    txEntityOwnershipCandidateRegistration = entityOwnershipService.registerCandidate(entity_);
                }
            }
        } catch (CandidateAlreadyRegisteredException e) {
            LOG.warn("Candidate for entity {} is already registered.", entity_.getType());
            return false;
        } catch (InterruptedException e) {
            LOG.warn("Cannot acquire semaphore for register entity {} candidate.", entity_.getType());
            return false;
        } finally {
            if (permit) {
                roleChangeGuard.release();
            }
        }
        return true;
    }

    @Override
    public boolean unregisterCandidate(final Entity entity_) {
        boolean permit = false;
        try {
            permit = roleChangeGuard.tryAcquire(TIMEOUT, TimeUnit.SECONDS);
            if(permit) {
                if (entity_.equals(this.entity)) {
                    if (entityOwnershipCandidateRegistration != null) {
                        entityOwnershipCandidateRegistration.close();
                        entityOwnershipCandidateRegistration = null;
                    }
                } else {
                    if (txEntityOwnershipCandidateRegistration != null) {
                        txEntityOwnershipCandidateRegistration.close();
                        txEntityOwnershipCandidateRegistration = null;
                    }
                }
            }
        } catch (InterruptedException e) {
            LOG.warn("Cannot acquire semaphore for unregister entity {} candidate.", entity_.getType());
            return false;
        } finally {
            if (permit) {
                roleChangeGuard.release();
            }
        }
        return true;
    }

    @Override
    public void close() {
        termination();
    }
}
