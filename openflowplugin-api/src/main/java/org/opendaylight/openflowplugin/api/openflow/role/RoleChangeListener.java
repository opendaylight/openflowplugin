package org.opendaylight.openflowplugin.api.openflow.role;

import java.util.concurrent.Semaphore;

import com.google.common.util.concurrent.ListenableFuture;
import org.opendaylight.controller.md.sal.common.api.clustering.Entity;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceState;
import org.opendaylight.yang.gen.v1.urn.opendaylight.role.service.rev150727.OfpRole;

/**
 * Created by kramesha on 9/19/15.
 */
public interface RoleChangeListener extends AutoCloseable {
    /**
     * Gets called by the EntityOwnershipCandidate after role change received from EntityOwnershipService
     *  @param oldRole
     * @param newRole
     */
    ListenableFuture<Void> onRoleChanged(OfpRole oldRole, OfpRole newRole);

    Entity getEntity();

    Entity getTxEntity();

    DeviceState getDeviceState();

    @Override
    void close();

    Semaphore getMainCandidateGuard();

    Semaphore getTxCandidateGuard();
}
