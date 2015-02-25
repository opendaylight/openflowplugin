/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.api.openflow.md.core.connection;

import org.opendaylight.openflowjava.protocol.api.connection.SwitchConnectionHandler;
import org.opendaylight.openflowplugin.api.openflow.md.core.device.handlers.DeviceConnectedHandler;
import org.opendaylight.openflowplugin.api.openflow.md.core.device.handlers.MessageHandler;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.OpenflowProtocolListener;

/**
 * Connection manager manages connections with devices.
 * It instantiates and registers {@link org.opendaylight.openflowplugin.api.openflow.md.core.connection.ConnectionContext}
 * used for handling all communication with device when onSwitchConnected notification is processed.
 * <p/>
 * Created by Martin Bobak <mbobak@cisco.com> on 25.2.2015.
 */
public interface ConnectionManager extends SwitchConnectionHandler {

    void setOpenflowProtocolListener(OpenflowProtocolListener openflowProtocolListener);

    /**
     * Method registers handler responsible handling operations related to connected device after
     * device is connected.
     *
     * @param deviceConnectedHandler
     */
    void setDeviceConnectedHandler(DeviceConnectedHandler deviceConnectedHandler);

    /**
     * Method registers handler responsible for operations related to connected device after
     * message from device is received.
     */
    void setMessageHandler(MessageHandler messageHandler);


}
