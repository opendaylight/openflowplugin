/*
 * (c) Copyright 2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */


package org.opendaylight.of.common;

import org.opendaylight.of.lib.ProtocolVersion;
import org.opendaylight.of.lib.dt.DataPathId;
import org.opendaylight.of.lib.msg.OpenflowMessage;
import org.opendaylight.util.net.IpAddress;

/**
 * Defines the interface between the {@code OpenflowController} and
 * the layer above it. This is where the controller will notify of
 * the comings and goings of datapath connections, and to where
 * it will post incoming OpenFlow messages.
 *
 * @author Simon Hunt
 * @author Scoot Simes
 * @author Frank Wood
 */
public interface MessageSink {

    /**
     * Invoked by the controller when a new datapath has completed the
     * handshake sequence and is ready to talk OpenFlow.
     * The IP address is that of the remote end of the connection
     * (i.e. the IP address of the switch).
     *
     * @param dpid the datapath id
     * @param negotiated the negotiated protocol version
     * @param ip the IP address of the switch
     */
    void dataPathAdded(DataPathId dpid, ProtocolVersion negotiated,
                       IpAddress ip);

    /**
     * Invoked by the controller when a datapath connection is dropped.
     * The IP address is that of the remote end of the connection
     * (i.e. the IP address of the switch).
     *
     * @param dpid the datapath id
     * @param negotiated the negotiated protocol version
     * @param ip the IP address of the switch
     */
    void dataPathRemoved(DataPathId dpid, ProtocolVersion negotiated,
                         IpAddress ip);

    /**
     * Invoked by the controller when a datapath connection is refused
     * because a datapath with the given ID is already connected.
     *
     * @param dpid the datapath id (duplicated)
     * @param negotiated the negotiated version
     * @param ip the IP address of the switch
     */
    void dataPathRevoked(DataPathId dpid, ProtocolVersion negotiated,
                         IpAddress ip);

    /**
     * OpenFlow message received from the specified datapath on the given
     * auxiliary channel.
     *
     * @param msg the message
     * @param dpid the datapath ID
     * @param auxId the auxiliary channel ID
     * @param negotiated the negotiated protocol version
     */
    void msgRx(OpenflowMessage msg, DataPathId dpid, int auxId,
               ProtocolVersion negotiated);
}
