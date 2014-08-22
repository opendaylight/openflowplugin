/*
 * (c) Copyright 2013,2014 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */


package org.opendaylight.of.lib.dt;

import org.opendaylight.of.lib.ProtocolVersion;
import org.opendaylight.of.lib.msg.Capability;
import org.opendaylight.of.lib.msg.Port;
import org.opendaylight.util.net.IpAddress;
import org.opendaylight.util.net.PortNumber;

import java.util.List;
import java.util.Set;

/**
 * Represents information about an OpenFlow datapath.
 * <p>
 * This includes data from the <em>FeaturesReply</em> message
 * received from the datapath during the handshake sequence.
 * <p>
 * All timestamps are measured in milliseconds since
 * midnight, January 1, 1970 UTC. (See {@link System#currentTimeMillis()}).
 *
 * @author Simon Hunt
 */
// TODO: Move back to net-of-controller public API package
public interface DataPathInfo {

    /** 
     * Returns the datapath identifier.
     *
     * @return the datapath ID
     */
    DataPathId dpid();

    /** 
     * Returns the version of the OpenFlow protocol that the datapath
     * has negotiated with the controller, via the exchange of <em>Hello</em>
     * messages (on the "main" connection).
     *
     * @return the negotiated protocol version
     */
    ProtocolVersion negotiated();

    /** 
     * Returns the time at which the datapath completed the handshake
     * with the controller (on the "main" connection).
     *
     * @return the "datapath ready" timestamp
     */
    long readyAt();

    /** 
     * Returns the time of the last message received from the datapath
     * (on <em>any</em> connection).
     *
     * @return the "last message" timestamp
     */
    long lastMessageAt();

    /** 
     * Returns the current known state of all the OpenFlow ports on
     * the datapath. This method is guaranteed to always return a list.
     *
     * @return the known state of the ports
     */
    List<Port> ports();

    /** 
     * Returns the maximum number of packets the switch can buffer at once.
     *
     * @return the number of packet buffers
     */
    long numBuffers();

    /** 
     * Returns the number of tables supported by the switch.
     *
     * @return the number of tables
     */
    int numTables();

    /** 
     * Returns the capabilities supported by the switch.
     *
     * @return the set of capabilities
     */
    Set<Capability> capabilities();

    /** 
     * Returns the IP address of the switch (on the "main" connection).
     *
     * @return the switch IP address
     */
    IpAddress remoteAddress();

    /** 
     * Returns the TCP port for the switch's "main" connection.
     *
     * @return the switch TCP port
     */
    PortNumber remotePort();

    /**
     * Returns a textual description of the datapath. 
     *
     * @return the datapath description
     */
    String datapathDescription();

    /**
     * Returns a description of the manufacturer. 
     * 
     * @return the manufacturer description
     */
    String manufacturerDescription();

    /**
     * Returns a description of the hardware.
     * 
     * @return the hardware description
     */
    String hardwareDescription();

    /**
     * Returns a description of the software.
     * 
     * @return the software description
     */
    String softwareDescription();

    /**
     * Returns the serial number.
     * 
     * @return the serial number
     */
    String serialNumber();

    /**
     * Returns the type name of the device. This is used as a key to look up
     * appropriate implementations in the device driver subsystem.
     *
     * @return the device type name
     */
    String deviceTypeName();
}
