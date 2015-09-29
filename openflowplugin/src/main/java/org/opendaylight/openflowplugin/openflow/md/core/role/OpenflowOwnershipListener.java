/**
 * Copyright (c) 2013, 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.openflow.md.core.role;

import org.opendaylight.controller.md.sal.common.api.clustering.Entity;
import org.opendaylight.controller.md.sal.common.api.clustering.EntityOwnershipListener;
import org.opendaylight.controller.md.sal.common.api.clustering.EntityOwnershipChange;

public class OpenflowOwnershipListener implements EntityOwnershipListener {
    private final OfEntityManager entManager;

    public OpenflowOwnershipListener(OfEntityManager entManager) {
        this.entManager = entManager;
    }

    @Override
    public void ownershipChanged(EntityOwnershipChange ownershipChange) {
        this.entManager.onRoleChanged(ownershipChange);
    }
}
