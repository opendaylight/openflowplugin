/*
 * (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.net.model;

import org.opendaylight.net.model.Interface.State;
import org.opendaylight.util.net.BigPortNumber;
import org.opendaylight.util.net.IfType;
import org.opendaylight.util.net.IpAddress;
import org.opendaylight.util.net.MacAddress;

import java.util.Set;

/**
 * Collection of interface attributes.
 *
 * @author Thomas Vachuska
 */
public interface InterfaceInfo {

    /**
     * Interface/port index.
     *
     * @return interface/port id
     */
    BigPortNumber id();

    /**
     * Interface/port state.
     *
     * @return interface/port state
     */
     Set<State> state();

     /**
      * Returns true if the port is administratively enabled.
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
     * Optional reference to the parent interface through which this one
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
