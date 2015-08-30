package org.opendaylight.openflowplugin.impl.role;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.opendaylight.controller.md.sal.common.api.clustering.Entity;
import org.opendaylight.controller.md.sal.common.api.clustering.EntityOwnershipChange;
import org.opendaylight.controller.md.sal.common.api.clustering.EntityOwnershipListener;
import org.opendaylight.controller.md.sal.common.api.clustering.EntityOwnershipListenerRegistration;
import org.opendaylight.controller.md.sal.common.api.clustering.EntityOwnershipService;
import org.opendaylight.openflowplugin.api.openflow.role.RoleChangeListener;
import org.opendaylight.openflowplugin.api.openflow.role.RoleManager;
import org.opendaylight.yang.gen.v1.urn.opendaylight.role.service.rev150727.OfpRole;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by kramesha on 9/14/15.
 */
public class OpenflowOwnershipListener implements EntityOwnershipListener, AutoCloseable {

    private static final Logger LOG = LoggerFactory.getLogger(RoleContextImpl.class);

    private EntityOwnershipService entityOwnershipService;
    private EntityOwnershipListenerRegistration entityOwnershipListenerRegistration;
    private Map<Entity, RoleChangeListener> roleChangeListenerMap = new ConcurrentHashMap<>();

    public OpenflowOwnershipListener(EntityOwnershipService entityOwnershipService) {
        this.entityOwnershipService = entityOwnershipService;
    }

    public void init() {
        entityOwnershipListenerRegistration = entityOwnershipService.registerListener(RoleManager.ENTITY_TYPE, this);
    }

    @Override
    public void ownershipChanged(EntityOwnershipChange ownershipChange) {
        LOG.debug("Received EntityOwnershipChange:{}", ownershipChange);

        RoleChangeListener roleChangeListener = roleChangeListenerMap.get(ownershipChange.getEntity());

        if (roleChangeListener != null) {
            LOG.debug("Found local entity:{}", ownershipChange.getEntity());

            // if this was the master and entity does not have a master
            if (ownershipChange.wasOwner() && !ownershipChange.isOwner() && !ownershipChange.hasOwner()) {
                // possible the last node to be disconnected from device.
                // eligible for the device to get deleted from inventory.
                LOG.debug("Initiate removal from operational. Possibly the last node to be disconnected for :{}. ", ownershipChange);
                roleChangeListener.onDeviceDisconnectedFromCluster();

            } else {
                OfpRole newRole = ownershipChange.isOwner() ? OfpRole.BECOMEMASTER : OfpRole.BECOMESLAVE;
                OfpRole oldRole = ownershipChange.wasOwner() ? OfpRole.BECOMEMASTER : OfpRole.BECOMESLAVE;
                // send even if they are same. we do the check for duplicates in SalRoleService and maintain a lastKnownRole
                roleChangeListener.onRoleChanged(oldRole, newRole);
            }
        }
    }

    public void registerRoleChangeListener(RoleChangeListener roleChangeListener) {
        roleChangeListenerMap.put(roleChangeListener.getEntity(), roleChangeListener);
    }

    @Override
    public void close() throws Exception {
        if (entityOwnershipListenerRegistration != null) {
            entityOwnershipListenerRegistration.close();
        }
    }
}
