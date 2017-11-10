/*
 * Copyright (c) 2017 Lumina Networks, Inc.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.applications.lldpspeaker;

import com.google.common.base.Optional;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.regex.Pattern;
import org.opendaylight.controller.md.sal.common.api.clustering.Entity;
import org.opendaylight.controller.md.sal.common.api.clustering.EntityOwnershipChange;
import org.opendaylight.controller.md.sal.common.api.clustering.EntityOwnershipListener;
import org.opendaylight.controller.md.sal.common.api.clustering.EntityOwnershipService;
import org.opendaylight.controller.md.sal.common.api.clustering.EntityOwnershipState;
import org.opendaylight.mdsal.eos.dom.api.DOMEntity;
import org.opendaylight.mdsal.singleton.common.api.ServiceGroupIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DeviceOwnershipStatusService implements EntityOwnershipListener {
    private static final Logger LOG = LoggerFactory.getLogger(DeviceOwnershipStatusService.class);
    private static final String SERVICE_ENTITY_TYPE = "org.opendaylight.mdsal.ServiceEntityType";
    private static final Pattern nodeIdPattern = Pattern.compile("^openflow:\\d+");

    private final EntityOwnershipService eos;
    private final ConcurrentMap<String, EntityOwnershipChange> ownershipChangeCache = new ConcurrentHashMap<>();

    public DeviceOwnershipStatusService(final EntityOwnershipService entityOwnershipService) {
        this.eos = entityOwnershipService;
        registerEntityOwnershipListener();
    }

    public boolean isEntityOwned(final String nodeId) {
        EntityOwnershipChange change = ownershipChangeCache.get(nodeId);
        if(change == null) {
            Optional<EntityOwnershipChange> status = getCurrentOwnershipStatus(nodeId);
            if (status.isPresent()) {
                change = status.get();
                ownershipChangeCache.put(nodeId, change);
            } else {
                LOG.warn("Fetching ownership status failed for node {}", nodeId);
            }
        }
        return change != null? change.isOwner():false;
    }

    public List<String> getOwnedNodes() {
        List<String> nodes = new ArrayList<>();
        ownershipChangeCache.forEach((node, change) -> {
            if (isEntityOwned(node)) {
                nodes.add(node);
            }
        });
        return nodes;
    }

    @Override
    public void ownershipChanged(final EntityOwnershipChange ownershipChange) {
        final YangInstanceIdentifier yii = ownershipChange.getEntity().getId();
        final YangInstanceIdentifier.NodeIdentifierWithPredicates niiwp =
                (YangInstanceIdentifier.NodeIdentifierWithPredicates) yii.getLastPathArgument();
        String entityName =  niiwp.getKeyValues().values().iterator().next().toString();
        if (entityName != null && isOpenFlowEntity(entityName)){
            LOG.info("Entity ownership change received for node : {} : {}", entityName, ownershipChange);
            if (!ownershipChange.isOwner() && !ownershipChange.hasOwner() && !ownershipChange.inJeopardy()) {
                LOG.debug("Entity for node {} is unregistered.", entityName);
                ownershipChangeCache.remove(entityName);
            } else {
                ownershipChangeCache.put(entityName, ownershipChange);
            }
        }
    }

    private Optional<EntityOwnershipChange> getCurrentOwnershipStatus(final String nodeId) {
        Entity entity = createNodeEntity(nodeId);
        Optional<EntityOwnershipState> ownershipStatus = eos.getOwnershipState(entity);

        if(ownershipStatus.isPresent()) {
            LOG.debug("Fetched ownership status for node {} is {}", nodeId, ownershipStatus.get());
            return Optional.of(new EntityOwnershipChange(entity, false, ownershipStatus.get().isOwner(), ownershipStatus
                    .get().hasOwner(), false));
        }
        return Optional.absent();
    }

    private Entity createNodeEntity(final String nodeId) {
        DOMEntity domEntity = new DOMEntity("org.opendaylight.mdsal.ServiceEntityType",
                ServiceGroupIdentifier.create(nodeId).getValue());
        return new Entity(domEntity.getType(), domEntity.getIdentifier());
    }

    private void registerEntityOwnershipListener() {
        this.eos.registerListener(SERVICE_ENTITY_TYPE, this);
    }

    private boolean isOpenFlowEntity(String entity) {
        return nodeIdPattern.matcher(entity).matches();
    }
}
