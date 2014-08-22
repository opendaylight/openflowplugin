/*
 * (c) Copyright 2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */


package org.opendaylight.of.controller;

import org.opendaylight.of.lib.ProtocolVersion;
import org.opendaylight.of.lib.dt.DataPathId;
import org.opendaylight.of.lib.msg.OfmFeaturesReply;
import org.opendaylight.of.lib.msg.OfmHello;
import org.opendaylight.util.net.IpAddress;
import org.opendaylight.util.net.PortNumber;

/**
 * Represents information about a specific "OpenFlow connection" (a network
 * connection to a datapath, with a given auxiliary id), as part of an
 * "OpenFlow channel" (the set of all connections to a given datapath).
 *
 * @author Simon Hunt
 */
public interface ConnectionDetails {

    /** The id of the datapath to which this is a connection.
     *
     * @return the dpid
     */
    DataPathId dpid();

    /** A reference to the OpenFlow HELLO message sent from the
     * controller to the datapath. This will be {@code null} until the
     * message has been sent.
     *
     * @return the outgoing HELLO message
     */
    OfmHello helloOut();

    /** A reference to the OpenFlow HELLO message received by the
     * controller from the datapath. This will be {@code null} until the
     * message has been received.
     *
     * @return the incoming HELLO message
     */
    OfmHello helloIn();

    /** Returns the version of the OpenFlow protocol that the datapath
     * has negotiated with the controller, via the exchange of HELLO
     * messages.
     *
     * @return the negotiated protocol version
     */
    ProtocolVersion negotiated();

    /** Returns the OpenFlow FEATURES_REPLY message that the datapath
     * issued during its handshake with the controller (on this connection).
     *
     * @return the features-reply message
     */
    OfmFeaturesReply features();

    /** Returns the auxiliary id (u8) of this connection.
     * The "main" connection will return 0; all "auxiliary" connections
     * will return non-zero.
     *
     * @return the auxiliary id
     */
    int auxId();

    /** Returns the time at which the datapath completed the handshake
     * with the controller (on this connection).
     *
     * @return the "ready" timestamp
     */
    long readyAt();

    /** Returns the time of the last message received from the datapath
     * (on this connection).
     *
     * @return the "last message" timestamp
     */
    long lastMessageAt();

    /** Returns the IP address of the switch (on this connection).
     *
     * @return the switch IP address
     */
    IpAddress remoteAddress();

    /** Returns the TCP port (on this connection).
     *
     * @return the switch TCP port
     */
    PortNumber remotePort();

}
