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
                LOG.debug("Got permit from role change guard, unregistering candidates.");
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
        LOG.debug("Notifying registered listeners for role initialization done, no. of listeners {}", listeners.size());
        for (RoleChangeListener listener : listeners) {
            listener.roleInitializationDone(nodeId, success);
        }
    }

    public void notifyListenersRoleChangeOnDevice(final boolean success, final OfpRole role, final boolean initializationPhase){
        LOG.debug("Notifying registered listeners for role change, no. of listeners {}", listeners.size());
        for (RoleChangeListener listener : listeners) {
            listener.roleChangeOnDevice(nodeId, success, role, initializationPhase);
        }
    }

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
        LOG.debug("Register candidate for entity {}", entity_);
        boolean permit = false;
        try {
            permit = roleChangeGuard.tryAcquire(TIMEOUT, TimeUnit.SECONDS);
            if(permit) {
                LOG.debug("Got permit for role change guard, registering candidate {}", entity_);
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
        LOG.debug("Unregister candidate for entity {}", entity_);
        boolean permit = false;
        try {
            permit = roleChangeGuard.tryAcquire(TIMEOUT, TimeUnit.SECONDS);
            if(permit) {
                LOG.debug("Got permit for role change guard, unregistering candidate {}", entity_);
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

    public boolean isMaster(){
        return (txEntityOwnershipCandidateRegistration != null && entityOwnershipCandidateRegistration != null);
    }
}
