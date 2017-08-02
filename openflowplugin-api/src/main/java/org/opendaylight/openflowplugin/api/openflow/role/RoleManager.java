/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.api.openflow.role;

import javax.annotation.Nonnull;
import org.opendaylight.openflowplugin.api.openflow.OFPManager;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceContext;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceInfo;

/**
 * Manager to send MASTER and SLAVE roles to devices
 */
public interface RoleManager extends OFPManager {
    /**
     * Create role context.
     *
     * @param deviceContext the device context
     * @return the role context
     */
    RoleContext createContext(@Nonnull DeviceContext deviceContext);

    /**
     * Notifies role manager that SLAVE or MASTER was acquired for device
     *
     * @param deviceInfo device info
     */
    void onRoleAcquired(@Nonnull DeviceInfo deviceInfo);
}