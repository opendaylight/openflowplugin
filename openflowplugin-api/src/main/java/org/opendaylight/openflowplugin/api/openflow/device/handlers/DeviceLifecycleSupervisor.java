/**
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.api.openflow.device.handlers;

/**
 * openflowplugin-api
 * org.opendaylight.openflowplugin.api.openflow.device.handlers
 *
 * Interface has to implement all relevant manager to correctly handling
 * device initialization and termination phase. Methods are used for order
 * handlers in initialization/termination phase. Ordering is easily changed
 * by definition.
 *
 */
public interface DeviceLifecycleSupervisor {

    /**
     * Method sets relevant {@link DeviceInitializationPhaseHandler} for building
     * handler's chain for new Device initial phase.
     *
     * @param handler initialization phase handler
     */
    void setDeviceInitializationPhaseHandler(DeviceInitializationPhaseHandler handler);

    /**
     * Method sets relevant {@link DeviceInitializationPhaseHandler} for annihilating
     * handler's chain for dead Device termination phase.
     *
     * @param handler termination phase handler
     */
    void setDeviceTerminationPhaseHandler(DeviceTerminationPhaseHandler handler);
}
