/*
 * (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.net.host;

import java.util.Iterator;
import java.util.Set;

import org.opendaylight.net.model.*;
import org.opendaylight.util.net.IpAddress;
import org.opendaylight.util.net.MacAddress;

/**
 * Provides read-only access to host-related information in the network model.
 * For write access, see {@link HostSupplierService}.
 *
 * @author Thomas Vachuska
 * @author Shaun Wackerly
 * @author Vikram Bobade
 */
public interface HostService {

    /**
     * Returns an iterator over all hosts in the domain.
     *
     * @return an iterator over the set of all hosts
     */
    Iterator<Host> getHosts();

    /**
     * Returns a specific host from the domain, based upon its unique ID.
     *
     * @param id host id
     * @return the matching host; null if not found
     */
    Host getHost(HostId id);

    /**
     * Returns an iterator all hosts in a given segment within the domain.
     *
     * @param segmentId the given segment ID
     * @return an iterator over the set of all hosts in the given network
     */
    Iterator<Host> getHosts(SegmentId segmentId);

    /**
     * Returns all hosts in the domain with a given IP address. Each host
     * returned is guaranteed to be on a unique {@link org.opendaylight.net.model.SegmentId}.
     * <p/>
     * If no matches exist, an empty set will be returned.
     *
     * @param ip the given IP address
     * @return a set of all hosts with the given IP address
     */
    Set<Host> getHosts(IpAddress ip);

    /**
     * Returns all hosts in the domain with a given MAC address. Each host
     * returned is guaranteed to have a unique {@link org.opendaylight.util.net.IpAddress}
     * within the given {@link org.opendaylight.net.model.SegmentId}.
     * <p/>
     * If no matches exist, an empty set will be returned.
     *
     * @param mac       the given MAC address
     * @param segmentId the given segment ID
     * @return a set of all hosts with the given MAC address
     */
    Set<Host> getHosts(MacAddress mac, SegmentId segmentId);

    /**
     * Returns all hosts in the domain where the hosts' most recent location
     * matches the given connection point.
     * <p/>
     * If no matches exist, an empty set will be returned.
     *
     * @param cp the given connection point
     * @return a set of all hosts at the given location
     */
    Set<Host> getHosts(ConnectionPoint cp);

    /**
     * Returns all hosts in the domain where the hosts' most recent location
     * matches a connection point on the given device ID.
     * <p/>
     * If no matches exist, an empty set will be returned.
     *
     * @param device the given device ID
     * @return a set of all hosts on the given device
     */
    Set<Host> getHosts(DeviceId device);

    /**
     * Returns an iterator over all hosts which match the given filter in the
     * domain.
     *
     * @param filter the given filter
     * @return an iterator over the set of all hosts which match the given filter
     */
    Iterator<Host> getHosts(HostFilter filter);

    /**
     * Registers a listener for all host-related events. This registration will
     * override any prior listener registrations for the same object.
     *
     * @param listener the event listener
     */
    void addListener(HostListener listener);

    /**
     * Unregisters a listener from host-related events, regardless of which
     * host-related events the listener was registered to receive.
     *
     * @param listener the event listener
     * @throws IllegalArgumentException if the listener was not registered
     */
    void removeListener(HostListener listener);

    /**
     * Returns the set of listeners for host-related events.
     *
     * @return set of listeners
     */
    Set<HostListener> getListeners();
}
