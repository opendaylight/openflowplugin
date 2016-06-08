/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.api.openflow.device.handlers;

import javax.annotation.CheckForNull;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceContext;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceInfo;

/**
 * Interface represent handler for dead device connection annihilating cycle.
 * Every implementation have some unnecessary steps which has to be done before
 * dead Device is removing.
 */
public interface DeviceTerminationPhaseHandler {

    /**
     * Method represents a termination cycle for {@link DeviceContext}.
     *
     * @param deviceInfo - {@link DeviceInfo}
     */
    void onDeviceContextLevelDown(@CheckForNull DeviceInfo deviceInfo);
}
