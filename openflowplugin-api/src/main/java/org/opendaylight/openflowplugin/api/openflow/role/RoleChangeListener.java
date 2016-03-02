/**
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.api.openflow.role;

import com.google.common.util.concurrent.FutureCallback;
import javax.annotation.Nullable;
import org.opendaylight.controller.md.sal.common.api.clustering.Entity;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceState;
import org.opendaylight.yang.gen.v1.urn.opendaylight.role.service.rev150727.OfpRole;

/**
 * Created by kramesha on 9/19/15.
 */
public interface RoleChangeListener extends AutoCloseable {
    /**
     * Gets called by the EntityOwnershipCandidate after role change received from EntityOwnershipService
     * @param oldRole
     * @param newRole
     * @param callback
     */
    void onRoleChanged(OfpRole oldRole, OfpRole newRole, @Nullable FutureCallback<Void> callback);

    void onTxRoleChange(OfpRole oldRole, OfpRole newRole);

    Entity getEntity();

    Entity getTxEntity();

    DeviceState getDeviceState();

    @Override
    void close();
}
