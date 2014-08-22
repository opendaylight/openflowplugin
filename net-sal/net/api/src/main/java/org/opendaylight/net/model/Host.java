/*
 * (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.net.model;

import org.opendaylight.util.net.IpAddress;
import org.opendaylight.util.net.MacAddress;

import java.util.List;

/**
 * Host is a network terminal entity, meaning a leaf, and has a single
 * {@link Interface}. A multi-homed system is modeled as multiple hosts.
 * <P>
 * Any host is uniquely identified by {@link IpAddress} and {@link SegmentId}.
 * Any other information about the host is not guaranteed to be unique,
 * and may be unknown.
 *
 * @author Thomas Vachuska
 * @author Uyen Chau
 * @author Simon Hunt
 */
public interface Host extends NetworkElement {

    /**
     * Returns the globally-unique identifier of this host.
     *
     * @return host identifier
     */
    @Override
    HostId id();

    // TODO: Let's assume ip/segmentId are duplicated here from id for brevity of coding

    /**
     * Returns the IPv4 or IPv6 address of this host.
     *
     * @return the IP address
     */
    IpAddress ip();

    /**
     * Returns the network segment id on which this host has been discovered.
     *
     * @return segment id.
     */
    SegmentId segmentId();

    /**
     * Returns the network interface through which the host attaches to the
     * network. This is an interface on the host itself, not the interface
     * on a {@link Device} to which the host is attached.
     *
     * @return host network interface; null if unknown
     */
    Interface netInterface();

    /**
     * Returns the MAC address of this host.
     *
     * @return the host MAC; null if unknown
     */
    MacAddress mac();

    /**
     * Returns the most recent host location.
     *
     * @return host location; null if unknown
     */
    HostLocation location();

    /**
     * Returns the list of recent host locations. The most recent location will
     * be at the beginning of the list, which may be empty if no recent
     * location is known.
     *
     * @return recent host locations
     */
    List<HostLocation> recentLocations();

}
