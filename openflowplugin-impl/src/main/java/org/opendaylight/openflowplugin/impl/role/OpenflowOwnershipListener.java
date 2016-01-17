/**
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.role;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.base.Verify;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import org.opendaylight.controller.md.sal.common.api.clustering.Entity;
import org.opendaylight.controller.md.sal.common.api.clustering.EntityOwnershipChange;
import org.opendaylight.controller.md.sal.common.api.clustering.EntityOwnershipListener;
import org.opendaylight.controller.md.sal.common.api.clustering.EntityOwnershipListenerRegistration;
import org.opendaylight.controller.md.sal.common.api.clustering.EntityOwnershipService;
import org.opendaylight.controller.md.sal.common.api.clustering.EntityOwnershipState;
import org.opendaylight.openflowplugin.api.openflow.role.RoleChangeListener;
import org.opendaylight.openflowplugin.api.openflow.role.RoleManager;
import org.opendaylight.yang.gen.v1.urn.opendaylight.role.service.rev150727.OfpRole;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class strictly separates {@link EntityOwnershipService} and OpenflowPlugin. So internal OpenflowPlugin
 * implementation will stay without change for all {@link EntityOwnershipService} API changes.
 *
 * Created by kramesha on 9/14/15.
 */
public class OpenflowOwnershipListener implements EntityOwnershipListener, AutoCloseable {

    private static final Logger LOG = LoggerFactory.getLogger(OpenflowOwnershipListener.class);

    public static boolean REMOVE_NODE_FROM_DS = true;

    private final EntityOwnershipService entityOwnershipService;
    private EntityOwnershipListenerRegistration entityOwnershipListenerRegistration;
    private final ConcurrentMap<Entity, RoleChangeListener> roleChangeListenerMap = new ConcurrentHashMap<>();

    /**
     * Initialization method has to be call from {@link org.opendaylight.openflowplugin.api.openflow.role.RoleManager}
     * @param entityOwnershipService
     */
    public OpenflowOwnershipListener(@Nonnull final EntityOwnershipService entityOwnershipService) {
        LOG.debug("New instance of OpenflowOwnershipListener is created");
        this.entityOwnershipService = Preconditions.checkNotNull(entityOwnershipService);
    }

    /**
     * Initialization method is register {@link OpenflowOwnershipListener} as listener
     * for EntityType = {@link RoleManager#ENTITY_TYPE}
     */
    public void init() {
        LOG.debug("OpenflowOwnershipListener is registred as EntityOwnershipListener for {} type", RoleManager.ENTITY_TYPE);
        entityOwnershipListenerRegistration = entityOwnershipService.registerListener(RoleManager.ENTITY_TYPE, this);
    }

    @Override
    public void ownershipChanged(final EntityOwnershipChange ownershipChange) {
        Preconditions.checkArgument(ownershipChange != null);
        final RoleChangeListener roleChangeListener = roleChangeListenerMap.get(ownershipChange.getEntity());


        if (roleChangeListener != null) {
            LOG.debug("Found local entity:{}", ownershipChange.getEntity());

            // if this was the master and entity does not have a master
            if (ownershipChange.wasOwner() && !ownershipChange.isOwner() && !ownershipChange.hasOwner()) {
                // possible the last node to be disconnected from device.
                // eligible for the device to get deleted from inventory.
                if (!entityOwnershipService.getOwnershipState(ownershipChange.getEntity()).isPresent()) {
                    LOG.info("Initiate removal from operational. Possibly the last node to be disconnected for :{}. ", ownershipChange);
                    roleChangeListener.onDeviceDisconnectedFromCluster(REMOVE_NODE_FROM_DS);
                    unregisterRoleChangeListener(roleChangeListener);
                } else {
                    LOG.info("Not initiating removal from operational. Candidates are present even if no owner is present :{}", ownershipChange);
                }
            } else {
                final OfpRole newRole = ownershipChange.isOwner() ? OfpRole.BECOMEMASTER : OfpRole.BECOMESLAVE;
                final OfpRole oldRole = ownershipChange.wasOwner() ? OfpRole.BECOMEMASTER : OfpRole.BECOMESLAVE;
                // send even if they are same. we do the check for duplicates in SalRoleService and maintain a lastKnownRole
                roleChangeListener.onRoleChanged(oldRole, newRole);
            }
        }
    }

    /**
     * Candidate registration process doesn't send Event about actual {@link EntityOwnershipState} for
     * Candidate. So we have to ask {@link EntityOwnershipService#getOwnershipState(Entity)} directly
     * for every new instance. Call this method from RoleManager after candidateRegistration.
     * @param roleChangeListener - new {@link RoleChangeListener} RoleContext
     */
    public void registerRoleChangeListener(@CheckForNull final RoleChangeListener roleChangeListener) {
        LOG.debug("registerRoleChangeListener {}", roleChangeListener);
        Preconditions.checkArgument(roleChangeListener != null);
        Verify.verify(roleChangeListenerMap.putIfAbsent(roleChangeListener.getEntity(), roleChangeListener) == null);

        final Entity entity = roleChangeListener.getEntity();

        final Optional<EntityOwnershipState> entityOwnershipStateOptional = entityOwnershipService.getOwnershipState(entity);

        if (entityOwnershipStateOptional != null && entityOwnershipStateOptional.isPresent()) {
            final EntityOwnershipState entityOwnershipState = entityOwnershipStateOptional.get();
            if (entityOwnershipState.hasOwner()) {
                LOG.debug("An owner exist for entity {}", entity);
                if (entityOwnershipState.isOwner()) {
                    LOG.debug("Ownership is here for entity {} becoming master", entity);
                    roleChangeListener.onRoleChanged(OfpRole.BECOMEMASTER, OfpRole.BECOMEMASTER);
                } else {
                    LOG.debug("Ownership is NOT here for entity {} becoming alave", entity);
                    roleChangeListener.onRoleChanged(OfpRole.BECOMESLAVE, OfpRole.BECOMESLAVE);
                }
            }
        }
    }

    /**
     * Unregistration process has to remove {@link RoleChangeListener} from internal map, so
     * we will not get anymore Events for this listener.
     * @param roleChangeListener - RoleContext
     */
    public void unregisterRoleChangeListener(@CheckForNull final RoleChangeListener roleChangeListener) {
        LOG.debug("unregisterroleChangeListener {}", roleChangeListener);
        Preconditions.checkArgument(roleChangeListener != null);
        roleChangeListenerMap.remove(roleChangeListener.getEntity(), roleChangeListener);
    }

    @Override
    public void close() throws Exception {
        if (entityOwnershipListenerRegistration != null) {
            entityOwnershipListenerRegistration.close();
        }
    }
}
