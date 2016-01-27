/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.api.openflow.role;

import org.opendaylight.openflowplugin.api.openflow.device.handlers.DeviceContextClosedHandler;
import org.opendaylight.openflowplugin.api.openflow.device.handlers.DeviceInitializationPhaseHandler;
import org.opendaylight.openflowplugin.api.openflow.device.handlers.DeviceInitializator;

/**
 * Created by kramesha on 8/31/15.
 */
public interface RoleManager extends DeviceInitializator, DeviceInitializationPhaseHandler, AutoCloseable,
        DeviceContextClosedHandler {
    public static final String ENTITY_TYPE = "openflow";
    public static final String TX_ENTITY_TYPE = "ofTransaction";
}
