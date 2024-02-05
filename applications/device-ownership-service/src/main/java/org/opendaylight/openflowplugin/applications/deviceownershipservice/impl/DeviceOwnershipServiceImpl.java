/*
 * Copyright (c) 2017 Lumina Networks, Inc.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.applications.deviceownershipservice.impl;

import static java.util.Objects.requireNonNull;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.regex.Pattern;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.opendaylight.mdsal.eos.binding.api.EntityOwnershipListener;
import org.opendaylight.mdsal.eos.binding.api.EntityOwnershipService;
import org.opendaylight.mdsal.eos.common.api.EntityOwnershipState;
import org.opendaylight.mdsal.eos.common.api.EntityOwnershipStateChange;
import org.opendaylight.openflowplugin.applications.deviceownershipservice.DeviceOwnershipService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.mdsal.core.general.entity.rev150930.Entity;
import org.opendaylight.yangtools.concepts.Registration;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
@Component(service = DeviceOwnershipService.class)
public final class DeviceOwnershipServiceImpl
        implements DeviceOwnershipService, EntityOwnershipListener, AutoCloseable {
    private static final Logger LOG = LoggerFactory.getLogger(DeviceOwnershipServiceImpl.class);
    private static final String SERVICE_ENTITY_TYPE = "org.opendaylight.mdsal.ServiceEntityType";
    private static final Pattern NODE_ID_PATTERN = Pattern.compile("^openflow:\\d+");

    private final ConcurrentMap<String, EntityOwnershipState> ownershipStateCache = new ConcurrentHashMap<>();
    private final EntityOwnershipService entityOwnershipService;
    private final Registration registration;

    @Inject
    @Activate
    public DeviceOwnershipServiceImpl(@Reference final EntityOwnershipService entityOwnershipService) {
        this.entityOwnershipService = requireNonNull(entityOwnershipService);
        registration = entityOwnershipService.registerListener(SERVICE_ENTITY_TYPE, this);
        LOG.info("DeviceOwnershipService started");
    }

    @PreDestroy
    @Deactivate
    @Override
    public void close() {
        registration.close();
        LOG.info("DeviceOwnershipService closed");
    }

    @Override
    public boolean isEntityOwned(final String nodeId) {
        EntityOwnershipState state = ownershipStateCache.get(nodeId);
        if (state == null) {
            LOG.debug("The ownership state for node {} is not cached. Retrieving from the EOS Datastore", nodeId);
            Optional<EntityOwnershipState> status = getCurrentOwnershipStatus(nodeId);
            if (status.isPresent()) {
                state = status.orElseThrow();
                ownershipStateCache.put(nodeId, state);
            } else {
                LOG.warn("Fetching ownership status failed for node {}", nodeId);
            }
        }
        return state != null && state.equals(EntityOwnershipState.IS_OWNER);
    }

    @Override
    public void ownershipChanged(final org.opendaylight.mdsal.eos.binding.api.Entity entity,
            final EntityOwnershipStateChange change, final boolean inJeopardy) {
        final String entityName = entity.getIdentifier().firstKeyOf(Entity.class).getName();
        if (entityName != null && isOpenFlowEntity(entityName)) {
            LOG.info("Entity ownership change received for node : {} : {}", entityName, change);
            if (!change.isOwner() && !change.hasOwner() && !inJeopardy) {
                LOG.debug("Entity for node {} is unregistered.", entityName);
                ownershipStateCache.remove(entityName);
            } else if (!change.isOwner() && change.hasOwner()) {
                ownershipStateCache.put(entityName, EntityOwnershipState.OWNED_BY_OTHER);
            } else if (change.isOwner()) {
                LOG.trace("Entity for node : {} is registered", entityName);
                ownershipStateCache.put(entityName, EntityOwnershipState.IS_OWNER);
            }
        }
    }

    private Optional<EntityOwnershipState> getCurrentOwnershipStatus(final String nodeId) {
        org.opendaylight.mdsal.eos.binding.api.Entity entity = createNodeEntity(nodeId);
        final var ownershipStatus = entityOwnershipService.getOwnershipState(entity);
        ownershipStatus.ifPresent(status -> {
            LOG.trace("Fetched ownership status for node {} is {}", nodeId, status);
        });
        return ownershipStatus;
    }

    private static org.opendaylight.mdsal.eos.binding.api.Entity createNodeEntity(final String nodeId) {
        return new org.opendaylight.mdsal.eos.binding.api.Entity(SERVICE_ENTITY_TYPE, nodeId);
    }

    private static boolean isOpenFlowEntity(final String entity) {
        return NODE_ID_PATTERN.matcher(entity).matches();
    }
}
