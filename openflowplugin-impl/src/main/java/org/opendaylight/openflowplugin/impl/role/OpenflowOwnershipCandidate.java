/**
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.role;

import org.opendaylight.controller.md.sal.common.api.clustering.Entity;
import org.opendaylight.controller.md.sal.common.api.clustering.EntityOwnershipCandidate;
import org.opendaylight.openflowplugin.api.openflow.role.RoleManager;
import org.opendaylight.yang.gen.v1.urn.opendaylight.role.service.rev150727.OfpRole;

/**
 * Created by kramesha on 9/1/15.
 */
public class OpenflowOwnershipCandidate implements EntityOwnershipCandidate {
    private final RoleManager roleManager;

    public OpenflowOwnershipCandidate(RoleManager roleManager) {
        this.roleManager = roleManager;
    }

    @Override
    public void ownershipChanged(Entity entity, boolean wasOwner, boolean isOwner) {
        OfpRole newRole = isOwner ? OfpRole.BECOMEMASTER : OfpRole.BECOMESLAVE;
        OfpRole oldRole = wasOwner ? OfpRole.BECOMEMASTER : OfpRole.BECOMESLAVE;

        this.roleManager.onRoleChanged(oldRole, newRole);
    }
}
