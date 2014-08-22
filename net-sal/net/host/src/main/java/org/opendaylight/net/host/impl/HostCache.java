/*
 * Copyright (c) 2014 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.net.host.impl;

import org.opendaylight.net.host.DefaultHostEvent;
import org.opendaylight.net.host.HostEvent;
import org.opendaylight.net.host.HostFilter;
import org.opendaylight.net.model.*;
import org.opendaylight.net.supplier.SupplierId;
import org.opendaylight.util.cache.ReadOnlyIterator;
import org.opendaylight.util.net.IpAddress;
import org.opendaylight.util.net.MacAddress;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static org.opendaylight.net.host.HostEvent.Type.HOST_ADDED;
import static org.opendaylight.net.host.HostEvent.Type.HOST_MOVED;
import static org.opendaylight.util.cache.CacheUtils.*;

/**
 * Auxiliary facility for tracking and searching through host inventory.
 *
 * @author Thomas Vachuska
 * @author Uyen Chau
 * @author Simon Hunt
 */
class HostCache {

    private final Map<HostId, DefaultHost> hosts = new ConcurrentHashMap<>();
    private final Map<SegmentId, Set<Host>> hostsBySegment = new ConcurrentHashMap<>();
    private final Map<IpAddress, Set<Host>> hostsByIp = new ConcurrentHashMap<>();
    private final Map<MacAddress, Set<Host>> hostsByMac = new ConcurrentHashMap<>();
    private final Map<DeviceId, Set<Host>> hostsByDevice = new ConcurrentHashMap<>();


    /**
     * Returns the size of the cache.
     *
     * @return the size of the cache
     */
    int size() {
        return hosts.size();
    }

    /**
     * Returns an iterator over all hosts in the inventory.
     *
     * @return host iterator
     */
    Iterator<Host> getHosts() {
        synchronized (this) {
            return new ReadOnlyIterator<Host, DefaultHost>(hosts.values());
        }
    }

    /**
     * Returns the host with the specified id.
     *
     * @param hostId host id
     * @return host or null if not found
     */
    Host getHost(HostId hostId) {
        return hosts.get(hostId);
    }

    /**
     * Get all hosts within the specified network segment.
     *
     * @param segmentId network segment
     * @return host iterator
     */
    Iterator<Host> getHosts(SegmentId segmentId) {
        return safeSet(hostsBySegment.get(segmentId)).iterator();
    }

    /**
     * Returns all hosts that have the specified IP address.
     *
     * @param ip ip address
     * @return set of hosts
     */
    Set<Host> getHosts(IpAddress ip) {
        return safeSet(hostsByIp.get(ip));
    }

    /**
     * Returns the set of hosts with the specified mac and located in the given
     * segment.
     *
     * @param mac       mac address
     * @param segmentId network segment id
     * @return set of hosts
     */
    public Set<Host> getHosts(MacAddress mac, SegmentId segmentId) {
        // FIXME: add filtering by segment
        return safeSet(hostsByMac.get(mac));
    }

    /**
     * Returns the set of hosts located at the specified connection point.
     *
     * @param cp connection point
     * @return set of hosts
     */
    Set<Host> getHosts(ConnectionPoint cp) {
        Set<Host> set = new HashSet<>();
        for (Host host : getHosts((DeviceId) cp.elementId())) {
            if (host.location().interfaceId().equals(cp.interfaceId()))
                set.add(host);
        }
        return set;
    }


    /**
     * Returns the set of hosts located at the specified device.
     *
     * @param deviceId infrastructure device id
     * @return set of hosts
     */
    Set<Host> getHosts(DeviceId deviceId) {
        return safeSet(hostsByDevice.get(deviceId));
    }

    /**
     * Returns safe iterator over a set of hosts that match the given filter.
     *
     * @param filter host filter
     * @return host iterator
     */
    Iterator<Host> getHosts(HostFilter filter) {
        List<Host> results = new ArrayList<>();
        for (Host host : hosts.values())
            if (filter.matches(host))
                results.add(host);
        return results.iterator();
    }

    /**
     * Creates host entry or updates data of an existing host entry.
     *
     * @param supplierId supplier id
     * @param hostId     host id
     * @param info       host info
     * @return host event
     */
    HostEvent createOrUpdateHost(SupplierId supplierId, HostId hostId, HostInfo info) {
        Host host = hosts.get(hostId);
        if (host == null) {
            return createHost(supplierId, hostId, info);
        } else {
            return updateHost(host, info);
        }
    }

    // Creates a new host, registers it in the inventory indexes and returns
    // the appropriate event.
    private HostEvent createHost(SupplierId supplierId, HostId hostId, HostInfo info) {
        DefaultHost host = new DefaultHost(supplierId, hostId,
                                           info.netInterface(),
                                           info.mac(), info.location());
        synchronized (this) {
            hosts.put(host.id(), host);
            addToIndex(host.segmentId(), hostsBySegment, host);
            addToIndex(host.ip(), hostsByIp, host);
            addToIndex(host.mac(), hostsByMac, host);
            addToIndex(host.location().elementId(), hostsByDevice, host);
            return new DefaultHostEvent(HOST_ADDED, host);
        }
    }

    // Updates the specified host and returns appropriate event
    private HostEvent updateHost(Host host, HostInfo info) {
        // FIXME: implement this
        return new DefaultHostEvent(HOST_MOVED, host);
    }

    /**
     * Removes the host with the specified id.
     *
     * @param hostId host id
     * @return host event if the host was deleted; null otherwise
     */
    HostEvent removeHost(HostId hostId) {
        synchronized (this) {
            Host host = hosts.remove(hostId);
            if (host == null)
                return null;

            removeFromIndex(host.segmentId(), hostsBySegment, host);
            removeFromIndex(host.ip(), hostsByIp, host);
            removeFromIndex(host.mac(), hostsByMac, host);
            removeFromIndex(host.location().elementId(), hostsByDevice, host);

            return new DefaultHostEvent(HostEvent.Type.HOST_REMOVED, host);
        }
    }
}
