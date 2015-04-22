/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.api.openflow.connection;

import org.opendaylight.openflowjava.protocol.api.connection.ConnectionAdapter;
import org.opendaylight.openflowplugin.api.openflow.device.handlers.DeviceDisconnectedHandler;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.FeaturesReply;

/**
 * <p>
 * Each OpenFlow session is tracked by a Connection Context. These attach to a particular Device Context in such a way,
 * that there is at most one primary session associated with a Device Context.
 * </p>
 * Created by Martin Bobak &lt;mbobak@cisco.com&gt; on 25.2.2015.
 */
public interface ConnectionContext {

    /**
     * distinguished connection states
     */
    enum CONNECTION_STATE {
        /**
         * initial phase of talking to switch
         */
        HANDSHAKING,
        /**
         * standard phase - interacting with switch
         */
        WORKING,
        /**
         * connection is idle, waiting for echo reply from switch
         */
        TIMEOUTING,
        /**
         * talking to switch is over - resting in pieces
         */
        RIP
    }

    /**
     * setter for nodeId
     *
     * @param nodeId
     */
    void setNodeId(NodeId nodeId);

    /**
     * Method returns identifier of device whic connection represents this context.
     *
     * @return {@link org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId}
     */
    NodeId getNodeId();

    /**
     * @return the connectionAdapter
     */
    ConnectionAdapter getConnectionAdapter();


    /**
     * Method returns current connection state.
     *
     * @return {@link ConnectionContext.CONNECTION_STATE}
     */
    CONNECTION_STATE getConnectionState();

    /**
     * Method sets connection state of current context.
     *
     * @param connectionState
     */
    void setConnectionState(CONNECTION_STATE connectionState);

    /**
     * @param featuresReply as received from device during handshake
     */
    void setFeatures(FeaturesReply featuresReply);

    /**
     * @return featureReply as received from device during handshake
     */
    FeaturesReply getFeatures();

    /**
     * Method sets handler for handling closing connections.
     *
     * @param deviceDisconnectedHandler
     */
    void setDeviceDisconnectedHandler(DeviceDisconnectedHandler deviceDisconnectedHandler);

    /**
     * Method provides propagates info about closed connection to handler for handling closing connections.
     */
    void propagateClosingConnection();
}
