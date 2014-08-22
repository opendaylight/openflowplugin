/*
 * (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.net.model;

import org.opendaylight.net.supplier.SupplierId;
import org.opendaylight.util.net.IpAddress;
import org.opendaylight.util.net.MacAddress;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.opendaylight.util.CommonUtils.notNull;

/**
 * Default implementation of {@link Host}. This implementation allows all
 * optional fields to remain unspecified or to be changed.
 *
 * @author Shaun Wackerly
 * @author Simon Hunt
 */
public class DefaultHost extends AbstractModel implements Host {

    /**
     * The default type for any host. Host is defined as a NIC.
     */
    public static final Type DEFAULT_TYPE = Type.NIC;

    /**
     * Maximum number of stored recent locations.
     */
    static final int MAX_LOC = 5;

    private final HostId id;
    private Interface netInterface = null;
    private MacAddress mac = null;
    private final List<HostLocation> locations = new ArrayList<>(MAX_LOC);

    /**
     * Constructs a host with all optional values unspecified.
     *
     * @param id host ID
     */
    public DefaultHost(HostId id) {
        notNull(id);
        this.id = id;
    }

    /**
     * Constructs a host with the given parameters. The {@link HostId}
     * is a required field, all other fields are optional. Optional fields
     * should be specified with their default value (null) if they are unknown.
     *
     * @param supplierId the host supplier ID
     * @param id the host ID
     * @param netInterface the host network interface
     * @param mac the host MAC address
     * @param loc the host location
     */
    public DefaultHost(SupplierId supplierId, HostId id, Interface netInterface,
                       MacAddress mac, HostLocation loc) {
        super(supplierId);
        notNull(id);
        this.id = id;
        this.netInterface = netInterface;
        this.mac = mac;
        setLocation(loc);
    }

    @Override
    public Type type() {
        return Type.NIC;
    }

    @Override
    public HostId id() {
        return id;
    }

    @Override
    public IpAddress ip() {
        return id.ip();
    }

    @Override
    public SegmentId segmentId() {
        return id.segmentId();
    }

    @Override
    public String name() {
        return id.ip().toShortString();
    }

    @Override
    public MacAddress mac() {
        return mac;
    }

    /**
     * Sets the host MAC address.
     *
     * @param mac host MAC address
     */
    public void setMac(MacAddress mac) {
        this.mac = mac;
    }


    @Override
    public Interface netInterface() {
        return netInterface;
    }

    /**
     * Sets the host network interface.
     *
     * @param intf host network interface
     */
    public void setNetInterface(Interface intf) {
        this.netInterface = intf;
    }

    @Override
    public HostLocation location() {
        return locations.isEmpty() ? null : locations.get(0);
    }

    @Override
    public List<HostLocation> recentLocations() {
        return Collections.unmodifiableList(locations);
    }

    /**
     * Sets the most recent host location. If the location is cleared
     * (ie: set to 'null') then it will also clear the list of recent
     * locations.
     *
     * @param loc host location
     */
    public void setLocation(HostLocation loc) {
        if (loc == null) {
            locations.clear();
        } else {
            // locations differing by timestamp only, requires a replacement
            if (loc.equals(mostRecent()))
                locations.remove(0);

            // add the location to the head of the list
            locations.add(0, loc);

            // if our list is over capacity, trim the tail
            if (locations.size() > MAX_LOC)
                locations.remove(MAX_LOC);
        }
    }

    // return the location at the head of the list, or null if list is empty
    private HostLocation mostRecent() {
        return locations.isEmpty() ? null : locations.get(0);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DefaultHost that = (DefaultHost) o;

        if (!id.equals(that.id)) return false;
        if (mac != null ? !mac.equals(that.mac) : that.mac != null)
            return false;
        if (netInterface != null ? !netInterface.equals(that.netInterface)
                                 : that.netInterface != null)
            return false;
        return true;
    }

    @Override
    public int hashCode() {
        int result = id.hashCode();
        result = 31 * result + (netInterface != null ? netInterface.hashCode() : 0);
        result = 31 * result + (mac != null ? mac.hashCode() : 0);
        return result;
    }
}
