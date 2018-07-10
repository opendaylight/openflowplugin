/*
 * Copyright (c) 2017 Lumina Networks, Inc.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.applications.ownershipstatusservice.impl;

import com.google.common.base.Optional;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.regex.Pattern;
import org.opendaylight.mdsal.eos.binding.api.EntityOwnershipChange;
import org.opendaylight.mdsal.eos.binding.api.EntityOwnershipListener;
import org.opendaylight.mdsal.eos.binding.api.EntityOwnershipService;
import org.opendaylight.mdsal.eos.common.api.EntityOwnershipState;
import org.opendaylight.openflowplugin.applications.ownershipstatusservice.DeviceOwnershipStatusService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.mdsal.core.general.entity.rev150930.Entity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DeviceOwnershipStatusServiceImpl implements DeviceOwnershipStatusService, EntityOwnershipListener {
    private static final Logger LOG = LoggerFactory.getLogger(DeviceOwnershipStatusService.class);
    private static final String SERVICE_ENTITY_TYPE = "org.opendaylight.mdsal.ServiceEntityType";
    private static final Pattern NODE_ID_PATTERN = Pattern.compile("^openflow:\\d+");

    private final EntityOwnershipService eos;
    private final ConcurrentMap<String, EntityOwnershipState> ownershipStateCache = new ConcurrentHashMap<>();

    public DeviceOwnershipStatusServiceImpl(final EntityOwnershipService entityOwnershipService) {
        this.eos = entityOwnershipService;
    }

    public void start() {
        registerEntityOwnershipListener();
        LOG.info("DeviceOwnershipStatusService started");
    }

    @Override
    public boolean isEntityOwned(final String nodeId) {
        EntityOwnershipState state = ownershipStateCache.get(nodeId);
        if (state == null) {
            java.util.Optional<EntityOwnershipState> status = getCurrentOwnershipStatus(nodeId);
            if (status.isPresent()) {
                state = status.get();
                ownershipStateCache.put(nodeId, state);
            } else {
                LOG.warn("Fetching ownership status failed for node {}", nodeId);
            }
        }
        return state != null && state.equals(EntityOwnershipState.IS_OWNER);
    }

    public List<String> getOwnedNodes() {
        List<String> nodes = new ArrayList<>();
        ownershipStateCache.forEach((node, change) -> {
            if (isEntityOwned(node)) {
                nodes.add(node);
            }
        });
        return nodes;
    }

    @Override
    @SuppressFBWarnings("BC_UNCONFIRMED_CAST_OF_RETURN_VALUE")
    public void ownershipChanged(final EntityOwnershipChange ownershipChange) {
        final String entityName = ownershipChange.getEntity().getIdentifier().firstKeyOf(Entity.class).getName();
        if (entityName != null && isOpenFlowEntity(entityName)) {
            LOG.info("Entity ownership change received for node : {} : {}", entityName, ownershipChange);
            if (!ownershipChange.getState().isOwner() && !ownershipChange.getState().hasOwner()
                    && !ownershipChange.inJeopardy()) {
                LOG.debug("Entity for node {} is unregistered.", entityName);
                ownershipStateCache.remove(entityName);
            } else if (!ownershipChange.getState().isOwner() && ownershipChange.getState().hasOwner()) {
                ownershipStateCache.put(entityName, EntityOwnershipState.OWNED_BY_OTHER);
            } else if (ownershipChange.getState().isOwner()) {
                ownershipStateCache.put(entityName, EntityOwnershipState.IS_OWNER);
            }
        }
    }

    private java.util.Optional<EntityOwnershipState> getCurrentOwnershipStatus(final String nodeId) {
        org.opendaylight.mdsal.eos.binding.api.Entity entity = createNodeEntity(nodeId);
        Optional<EntityOwnershipState> ownershipStatus = eos.getOwnershipState(entity);

        if (ownershipStatus.isPresent()) {
            LOG.debug("Fetched ownership status for node {} is {}", nodeId, ownershipStatus.get());
            return java.util.Optional.of(ownershipStatus.get());
        }
        return java.util.Optional.empty();
    }

    private org.opendaylight.mdsal.eos.binding.api.Entity createNodeEntity(final String nodeId) {
        return new org.opendaylight.mdsal.eos.binding.api.Entity(SERVICE_ENTITY_TYPE, nodeId);
    }

    private void registerEntityOwnershipListener() {
        this.eos.registerListener(SERVICE_ENTITY_TYPE, this);
    }

    private boolean isOpenFlowEntity(String entity) {
        return NODE_ID_PATTERN.matcher(entity).matches();
    }
}
