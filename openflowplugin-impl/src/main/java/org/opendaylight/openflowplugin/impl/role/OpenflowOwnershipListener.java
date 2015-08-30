package org.opendaylight.openflowplugin.impl.role;

import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
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
    private final Map<Entity, Boolean> ownershipMap = new ConcurrentHashMap<>();
    private final ExecutorService roleChangeExecutor = Executors.newSingleThreadExecutor();

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

        ownershipMap.put(ownershipChange.getEntity(), ownershipChange.isOwner());

        if (roleChangeListener != null) {
            LOG.debug("Found local entity:{}", ownershipChange.getEntity());

            // if this was the master and entity does not have a master
            if (ownershipChange.wasOwner() && !ownershipChange.isOwner() && !ownershipChange.hasOwner()) {
                // possible the last node to be disconnected from device.
                // eligible for the device to get deleted from inventory.
                LOG.debug("Initiate removal from operational. Possibly the last node to be disconnected for :{}. ", ownershipChange);
                ownershipMap.remove(ownershipChange.getEntity());
                roleChangeListener.onDeviceDisconnectedFromCluster();

            } else {
                OfpRole newRole = ownershipChange.isOwner() ? OfpRole.BECOMEMASTER : OfpRole.BECOMESLAVE;
                OfpRole oldRole = ownershipChange.wasOwner() ? OfpRole.BECOMEMASTER : OfpRole.BECOMESLAVE;
                // send even if they are same. we do the check for duplicates in SalRoleService and maintain a lastKnownRole
                roleChangeListener.onRoleChanged(oldRole, newRole);
            }
        }
    }

    public void registerRoleChangeListener(final RoleChangeListener roleChangeListener) {
        roleChangeListenerMap.put(roleChangeListener.getEntity(), roleChangeListener);

        final Entity entity = roleChangeListener.getEntity();
        final OpenflowOwnershipListener self = this;

        if(this.hasOwner(entity)) {
            LOG.debug("An owner exist for entity {}", entity);
            roleChangeExecutor.submit(new Callable<Object>() {
                @Override
                public Object call() throws Exception {
                    if(self.isOwner(entity)){
                        LOG.debug("Ownership is here for entity {} becoming master", entity);
                        roleChangeListener.onRoleChanged(OfpRole.BECOMEMASTER, OfpRole.BECOMEMASTER);
                    } else {
                        LOG.debug("Ownership is NOT here for entity {} becoming alave", entity);
                        roleChangeListener.onRoleChanged(OfpRole.BECOMESLAVE, OfpRole.BECOMESLAVE);

                    }

                    return null;
                }
            });
        }
    }

    @Override
    public void close() throws Exception {
        if (entityOwnershipListenerRegistration != null) {
            entityOwnershipListenerRegistration.close();
        }
    }

    private boolean hasOwner(Entity entity){
        return ownershipMap.containsKey(entity);
    }

    private boolean isOwner(Entity entity){
        if(hasOwner(entity)){
            Boolean isOwner = ownershipMap.get(entity);
            if(isOwner != null) {
                return isOwner.booleanValue();
            }
        }

        return false;
    }
}
