/*
 * (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.net.model;

import org.opendaylight.util.net.IfType;
import org.opendaylight.util.net.IpAddress;
import org.opendaylight.util.net.MacAddress;

import java.util.Set;

/**
 * Interface is capable of transmit &amp; receive of network packets. It has a
 * globally unique identifier and a unique index within the context of the
 * {@link Device} or {@link Host} it is associated with.
 * 
 * @author Thomas Vachuska
 * @author Uyen Chau
 * @author Simon Hunt
 */
public interface Interface {

    /**
     * State of the network interface.
     */
    enum State {
        UP, DOWN, BLOCKED, PROVISIONING, UNKNOWN
    }

    /**
     * Interface/port state.
     * 
     * @return interface/port state
     */
     Set<State> state();
          
     /**
      * Returns true only if the port is administratively and 
      * operationally enabled
      *
      * @return true if the port is enabled
      */
     boolean isEnabled();
     
     /**
      * Returns true if this port is prevented from being used for flooding.
      * <p>
      * The "blocked" flag indicates that a switch protocol outside of
      * OpenFlow, such as 802.1D Spanning Tree, is preventing the use of
      * the port with FLOOD.
      * <p>
      * @return true if the port is blocked
      */
     boolean isBlocked(); 

    /**
     * Device unique identifier of the interface/port.
     *
     * @return interface identifier
     */
    InterfaceId id();

    /**
     * Label or a friendly name, which can be set on the device.
     *
     * @return friendly interface name
     */
    String name();

    /**
     * Parent network element to which this interface belongs. eg Device / Node
     *
     * @return parent network element
     */
    ElementId hostedBy();

    /**
     * Optional reference to the parent element through which this one
     * is realized.
     *
     * @return parent interface
     */
    ElementId realizedByElement();

    /**
     * Optional reference to the parent interface through which this one
     * is realized.
     *
     * @return parent interface
     */
    InterfaceId realizedByInterface();

    /**
     * Optional Interface/port physical address.
     *
     * @return mac address of the interface
     */
    MacAddress mac();

    /**
     * Interface/port type as defined in IfType
     *
     * @return interface type
     */
    IfType type();

    /**
     * Optional IP Address(es) associated with the interface.
     *
     * @return ip addresses of the interface
     */
    Set<IpAddress> ipAddresses();
}
