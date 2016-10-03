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
 * This interface is responsible for managing lifecycle of itself and all it's associated contexts.
 * Every manager that implements this interface must handle connection initialization and termination chain
 * by implementing methods from {@link org.opendaylight.openflowplugin.api.openflow.device.handlers.DeviceLifecycleSupervisor},
 * then it must handle initialization and termination chain of it's associated context by implementing methods from
 * {@link org.opendaylight.openflowplugin.api.openflow.device.handlers.DeviceInitializationPhaseHandler} and
 * {@link org.opendaylight.openflowplugin.api.openflow.device.handlers.DeviceTerminationPhaseHandler} and also removal
 * of these contexts from it's internal map of contexts by implementing methods from
 * {@link org.opendaylight.openflowplugin.api.openflow.device.handlers.DeviceRemovedHandler}. And at last, it must
 * handle it's own full termination by implementing {@link AutoCloseable#close()}
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
