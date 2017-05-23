/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.api.openflow.connection;

import org.opendaylight.openflowjava.protocol.api.connection.SwitchConnectionHandler;
import org.opendaylight.openflowplugin.api.openflow.device.handlers.DeviceConnectedHandler;
import org.opendaylight.openflowplugin.api.openflow.device.handlers.DeviceDisconnectedHandler;

/**
 * Connection manager manages connections with devices.
 * It instantiates and registers {@link ConnectionContext}
 * used for handling all communication with device when onSwitchConnected notification is processed.
 */
public interface ConnectionManager extends SwitchConnectionHandler {

    /**
     * Method registers handler responsible handling operations related to connected device after
     * device is connected.
     * @param deviceConnectedHandler device connected handler
     */
    void setDeviceConnectedHandler(final DeviceConnectedHandler deviceConnectedHandler);

    /**
     * Method registers handler responsible handling device disconnected event
     * @param deviceDisconnectedHandler device disconnected handler
     */
    void setDeviceDisconnectedHandler(final DeviceDisconnectedHandler deviceDisconnectedHandler);

    /**
     * Setter for echo reply timeout.
     * @param echoReplyTimeout {@link Long} timeout
     */
    void setEchoReplyTimeout(long echoReplyTimeout);
}
