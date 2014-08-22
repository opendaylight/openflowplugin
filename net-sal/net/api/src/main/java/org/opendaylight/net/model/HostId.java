/*
 * (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.net.model;

import org.opendaylight.util.net.IpAddress;

import static org.opendaylight.util.CommonUtils.notNull;

/**
 * Node identifier.
 *
 * @author Thomas Vachuska
 * @author Uyen Chau
 * @author Simon Hunt
 */
public class HostId implements ElementId {

    private final IpAddress ip;
    private final SegmentId segId;
    
    /**
     * Private constructor to create a node ID based on the given IP address
     * and segment ID.
     * 
     * @param ip the given IP address
     * @param segmentId the given segment ID
     */
    private HostId(IpAddress ip, SegmentId segmentId) {
        notNull(ip, segmentId);
        this.ip = ip;
        this.segId = segmentId;
    }

    /**
     * Returns the value of the given IP address and segment ID as a node ID.
     * 
     * @param ip the given IP address
     * @param segmentId the given segment ID
     * @return node ID representation
     */
    public static HostId valueOf(IpAddress ip, SegmentId segmentId) {
        return new HostId(ip, segmentId);
    }

    /**
     * Returns the IPv4 or IPv6 address associated with this node.
     * 
     * @return IP address
     */
    public IpAddress ip() {
        return ip;
    }

    /**
     * Returns the segment ID associated with this node.
     * 
     * @return segment ID
     */
    public SegmentId segmentId() {
        return segId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        HostId hostId = (HostId) o;
        return ip.equals(hostId.ip) && segId.equals(hostId.segId);
    }

    @Override
    public int hashCode() {
        int result = ip.hashCode();
        result = 31 * result + segId.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "NodeId{ip=" + ip + ", segId=" + segId + '}';
    }

}
