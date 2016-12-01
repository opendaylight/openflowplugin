/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.api.openflow.device.handlers;

import org.opendaylight.openflowplugin.api.openflow.connection.ConnectionContext;
import org.opendaylight.openflowplugin.api.openflow.connection.ConnectionStatus;

/**
 * Represents handler for new connected device that will propagate information about
 * established connection with device. It is important for correct order in device connection chain.
 */
public interface DeviceConnectedHandler {

    /**
     * Method is used to propagate information about established connection with device.
     * It propagates connected device's connection context.
     */
    ConnectionStatus deviceConnected(final ConnectionContext connectionContext)
            throws Exception;

}
