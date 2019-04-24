/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.api.openflow.role;

import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.openflowplugin.api.openflow.OFPManager;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceContext;

/**
 * Manages creation and termination of role contexts.
 * @see org.opendaylight.openflowplugin.api.openflow.role.RoleContext
 */
public interface RoleManager extends OFPManager {
    /**
     * Create role context.
     *
     * @param deviceContext the device context
     * @return the role context
     */
    RoleContext createContext(@NonNull DeviceContext deviceContext);
}