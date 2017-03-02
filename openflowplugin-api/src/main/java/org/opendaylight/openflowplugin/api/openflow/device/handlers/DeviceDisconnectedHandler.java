/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.api.openflow.device.handlers;

import org.opendaylight.openflowplugin.api.openflow.connection.ConnectionContext;

/**
 * Represents handler for just disconnected device that will propagate device's
 * connection context. It is important for correct order in device disconnection chain.
 */
public interface DeviceDisconnectedHandler {

    /**
     * Method is used to propagate information about closed connection with device.
     * It propagates connected device's connection context.
     */
    void onDeviceDisconnected(final ConnectionContext connectionContext);
}