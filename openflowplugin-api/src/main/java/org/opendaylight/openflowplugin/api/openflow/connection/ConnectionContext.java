/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.api.openflow.connection;

import org.opendaylight.openflowjava.protocol.api.connection.ConnectionAdapter;
import org.opendaylight.openflowjava.protocol.api.connection.OutboundQueue;
import org.opendaylight.openflowjava.protocol.api.connection.OutboundQueueHandlerRegistration;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceInfo;
import org.opendaylight.openflowplugin.api.openflow.device.handlers.DeviceDisconnectedHandler;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.FeaturesReply;

/**
 * <p>
 * Each OpenFlow session is tracked by a Connection Context. These attach to a particular Device Context in such a way,
 * that there is at most one primary session associated with a Device Context.
 * </p>
 */
public interface ConnectionContext {

    /**
     * @param handshakeContext corresponding handshake context used upon this connection
     */
    void setHandshakeContext(HandshakeContext handshakeContext);

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
     * Returns reference to OFJava outbound queue provider. Outbound queue is used for outbound messages processing.
     *
     * @return
     */
    OutboundQueue getOutboundQueueProvider();

    /**
     * Method sets reference to OFJava outbound queue provider.
     */
    void setOutboundQueueProvider(OutboundQueueProvider outboundQueueProvider);

    /**
     * Method returns current connection state.
     *
     * @return {@link ConnectionContext.CONNECTION_STATE}
     */
    CONNECTION_STATE getConnectionState();

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

    void setOutboundQueueHandleRegistration(OutboundQueueHandlerRegistration<OutboundQueueProvider> outboundQueueHandlerRegistration);

    /**
     * actively drop associated connection
     *
     * @param propagate true if event need to be propagated to higher contexts (device, stats, rpc..)
     *                  or false if invoked from higher context
     * @see ConnectionAdapter#disconnect()
     */
    void closeConnection(boolean propagate);

    /**
     * cleanup context upon connection closed event (by device)
     */
    void onConnectionClosed();

    /**
     * change internal state to {@link ConnectionContext.CONNECTION_STATE#HANDSHAKING}
     */
    void changeStateToHandshaking();

    /**
     * change internal state to {@link ConnectionContext.CONNECTION_STATE#TIMEOUTING}
     */
    void changeStateToTimeouting();

    /**
     * change internal state to {@link ConnectionContext.CONNECTION_STATE#WORKING}
     */
    void changeStateToWorking();

    /**
     * Create and return basic device info
     * @return
     */
    DeviceInfo getDeviceInfo();

    void createDeviceInfo();
}
