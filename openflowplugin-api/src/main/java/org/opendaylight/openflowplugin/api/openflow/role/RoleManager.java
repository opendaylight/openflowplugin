/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.api.openflow.role;

import org.opendaylight.openflowplugin.api.openflow.OFPManager;
import org.opendaylight.openflowplugin.api.openflow.device.handlers.DeviceInitializationPhaseHandler;
import org.opendaylight.openflowplugin.api.openflow.device.handlers.DeviceLifecycleSupervisor;
import org.opendaylight.openflowplugin.api.openflow.device.handlers.DeviceTerminationPhaseHandler;
import org.opendaylight.openflowplugin.api.openflow.lifecycle.RoleChangeListener;

/**
 * Created by kramesha on 8/31/15.
 */
public interface RoleManager extends DeviceLifecycleSupervisor, DeviceInitializationPhaseHandler, AutoCloseable,
        DeviceTerminationPhaseHandler, OFPManager {
    String ENTITY_TYPE = "openflow";
    String TX_ENTITY_TYPE = "ofTransaction";

    /**
     * Adding listener to by notified for role changes
     * API for listener {@link RoleChangeListener}
     * @param roleChangeListener
     */
    void addRoleChangeListener(RoleChangeListener roleChangeListener);

}
