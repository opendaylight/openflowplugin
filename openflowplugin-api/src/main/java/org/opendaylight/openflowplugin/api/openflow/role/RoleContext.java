/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.api.openflow.role;

import org.opendaylight.openflowplugin.api.openflow.OFPContext;
import org.opendaylight.openflowplugin.api.openflow.device.RequestContextStack;
import org.opendaylight.yang.gen.v1.urn.opendaylight.role.service.rev150727.SalRoleService;

/**
 * Handles propagation of SLAVE and MASTER roles on connected devices.
 */
public interface RoleContext extends OFPContext, RequestContextStack {
    /**
     * Sets role service.
     *
     * @param salRoleService the sal role service
     */
    void setRoleService(SalRoleService salRoleService);
}