/**
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.role;

import com.google.common.base.Preconditions;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.opendaylight.controller.md.sal.common.api.clustering.CandidateAlreadyRegisteredException;
import org.opendaylight.controller.md.sal.common.api.clustering.Entity;
import org.opendaylight.controller.md.sal.common.api.clustering.EntityOwnershipCandidateRegistration;
import org.opendaylight.controller.md.sal.common.api.clustering.EntityOwnershipService;
import org.opendaylight.mdsal.singleton.common.api.ServiceGroupIdentifier;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceInfo;
import org.opendaylight.openflowplugin.api.openflow.device.RequestContext;
import org.opendaylight.openflowplugin.api.openflow.lifecycle.LifecycleConductor;
import org.opendaylight.openflowplugin.api.openflow.role.RoleContext;
import org.opendaylight.openflowplugin.impl.rpc.AbstractRequestContext;
import org.opendaylight.yang.gen.v1.urn.opendaylight.role.service.rev150727.SalRoleService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Role context hold information about entity ownership registration,
 * register and unregister candidate (main and tx)
 */
class RoleContextImpl implements RoleContext {

    private static final Logger LOG = LoggerFactory.getLogger(RoleContextImpl.class);
    private static final int TIMEOUT = 12;

    private final EntityOwnershipService entityOwnershipService;
    private volatile EntityOwnershipCandidateRegistration entityOwnershipCandidateRegistration = null;
    private volatile EntityOwnershipCandidateRegistration txEntityOwnershipCandidateRegistration = null;

    private final Entity entity;
    private final Entity txEntity;

    private SalRoleService salRoleService = null;

    private final Semaphore roleChangeGuard = new Semaphore(1, true);

    private final LifecycleConductor conductor;
    private final DeviceInfo deviceInfo;
    private CONTEXT_STATE state;

    RoleContextImpl(final DeviceInfo deviceInfo,
                    final EntityOwnershipService entityOwnershipService,
                    final Entity entity,
                    final Entity txEntity,
                    final LifecycleConductor lifecycleConductor) {
        this.entityOwnershipService = entityOwnershipService;
        this.entity = entity;
        this.txEntity = txEntity;
        this.conductor = lifecycleConductor;
        this.deviceInfo = deviceInfo;
        state = CONTEXT_STATE.INITIALIZATION;
    }

    @Override
    public boolean initialization() {
        LOG.info("Initialization main candidate for node {}", getDeviceInfo().getNodeId());
        setState(CONTEXT_STATE.WORKING);
        return registerCandidate(this.entity);
    }

    @Override
    public void unregisterAllCandidates() {
        LOG.info("Role context closed, unregistering all candidates for ownership for node {}", getDeviceInfo().getNodeId());
        if (isMainCandidateRegistered()) {
            unregisterCandidate(this.entity);
        }
        if (isTxCandidateRegistered()) {
            unregisterCandidate(this.txEntity);
        }
    }

    @Nullable
    @Override
    public <T> RequestContext<T> createRequestContext() {
        return new AbstractRequestContext<T>(conductor.reserveXidForDeviceMessage(getDeviceInfo())) {
            @Override
            public void close() {
            }
        };
    }

    @Override
    public void setSalRoleService(@Nonnull final SalRoleService salRoleService) {
        Preconditions.checkNotNull(salRoleService);
        this.salRoleService = salRoleService;
    }

    @Override
    public SalRoleService getSalRoleService() {
        return this.salRoleService;
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
                LOG.debug("Register candidate for entity {}", entity_);
                if (entity_.equals(this.entity)) {
                    entityOwnershipCandidateRegistration = entityOwnershipService.registerCandidate(entity_);
                } else {
                    txEntityOwnershipCandidateRegistration = entityOwnershipService.registerCandidate(entity_);
                }
            } else {
                return false;
            }
        } catch (final CandidateAlreadyRegisteredException e) {
            LOG.warn("Candidate for entity {} is already registered.", entity_.getType());
            return false;
        } catch (final InterruptedException e) {
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
                        LOG.debug("Unregister candidate for entity {}", entity_);
                        entityOwnershipCandidateRegistration.close();
                        entityOwnershipCandidateRegistration = null;
                    }
                } else {
                    if (txEntityOwnershipCandidateRegistration != null) {
                        LOG.debug("Unregister candidate for tx entity {}", entity_);
                        txEntityOwnershipCandidateRegistration.close();
                        txEntityOwnershipCandidateRegistration = null;
                    }
                }
            } else {
                return false;
            }
        } catch (final InterruptedException e) {
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
        setState(CONTEXT_STATE.TERMINATION);
        unregisterAllCandidates();
    }

    public boolean isMaster(){
        return (txEntityOwnershipCandidateRegistration != null && entityOwnershipCandidateRegistration != null);
    }

    @Override
    public CONTEXT_STATE getState() {
        return this.state;
    }

    @Override
    public void setState(CONTEXT_STATE state) {
        this.state = state;
    }

    @Override
    public ServiceGroupIdentifier getServiceIdentifier() {
        return this.deviceInfo.getServiceIdentifier();
    }

    @Override
    public DeviceInfo getDeviceInfo() {
        return this.deviceInfo;
    }

    @Override
    public void setState(CONTEXT_STATE contextState) {
        this.contextState = contextState;
    }
}
