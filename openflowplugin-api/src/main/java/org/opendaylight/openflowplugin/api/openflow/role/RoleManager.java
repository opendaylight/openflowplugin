/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.api.openflow.role;

import org.opendaylight.openflowplugin.api.openflow.device.handlers.DeviceInitializationPhaseHandler;
import org.opendaylight.openflowplugin.api.openflow.device.handlers.DeviceInitializator;
import org.opendaylight.yang.gen.v1.urn.opendaylight.role.service.rev150727.OfpRole;

/**
 * Created by kramesha on 8/31/15.
 */
public interface RoleManager extends DeviceInitializator, DeviceInitializationPhaseHandler, AutoCloseable {
    /**
     * Gets called by the EntityOwnershipCandidate after role change received from EntityOwnershipService
     * @param oldRole
     * @param newRole
     */
    void onRoleChanged(OfpRole oldRole, OfpRole newRole);
}
