/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.api.openflow.md.core.connection;

import org.opendaylight.openflowjava.protocol.api.connection.ConnectionAdapter;
import org.opendaylight.openflowplugin.api.openflow.md.core.HandshakeManager;
import org.opendaylight.openflowplugin.api.openflow.md.core.device.handlers.DeviceConnectedHandler;
import org.opendaylight.openflowplugin.api.openflow.md.core.device.handlers.DeviceMessageHandler;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.OpenflowProtocolListener;

/**
 * Each OpenFlow session is tracked by a Connection Context. These attach to a particular Device Context in such a way,
 * that there is at most one primary session associated with a Device Context.
 * <p/>
 * Created by Martin Bobak <mbobak@cisco.com> on 25.2.2015.
 */
public interface ConnectionContext {

    /**
     * Method returns identifier of device whic connection represents this context.
     *
     * @return {@link org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId}
     */
    public NodeId getNodeId();


    void setHandshakeManager(HandshakeManager handshakeManager);

    void setOpenflowProtocolListener(OpenflowProtocolListener openflowProtocolListener);

    /**
     * @return the connectionAdapter
     */
    public ConnectionAdapter getConnectionAdapter();


    /**
     * Method that send
     */
    public void sendHelloMessage();

    /**
     * Method registers handler responsible handling operations related to connected device after
     * device is connected.
     *
     * @param deviceConnectedHandler
     */
    public void setDeviceConnectionHandler(DeviceConnectedHandler deviceConnectedHandler);

    /**
     * Method registers hanlder responsible for operations related to connected device after
     * message from device is received.
     */
    public void setDeviceMessageHandler(DeviceMessageHandler deviceMessageHandler);


}
