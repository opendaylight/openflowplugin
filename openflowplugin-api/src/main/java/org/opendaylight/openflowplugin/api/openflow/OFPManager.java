/*
 * Copyright (c) 2016 Pantheon Technologies s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.api.openflow;

import org.opendaylight.openflowplugin.api.openflow.device.handlers.DeviceInitializationPhaseHandler;
import org.opendaylight.openflowplugin.api.openflow.device.handlers.DeviceLifecycleSupervisor;
import org.opendaylight.openflowplugin.api.openflow.device.handlers.DeviceRemovedHandler;
import org.opendaylight.openflowplugin.api.openflow.device.handlers.DeviceTerminationPhaseHandler;

/**
 * General API for all OFP managers
 */
public interface OFPManager extends
        DeviceLifecycleSupervisor,
        DeviceInitializationPhaseHandler,
        DeviceTerminationPhaseHandler,
        DeviceRemovedHandler,
        AutoCloseable {

    @Override
    void close();
}
