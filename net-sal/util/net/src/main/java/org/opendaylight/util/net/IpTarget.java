/*
 * (C) Copyright 2013,2014 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.util.net;

import org.opendaylight.util.api.Addressable;


/**
 * Represents an binding target as a pair of IP address and port number.
 * <p>
 * Instances of this class are immutable, making them inherently threadsafe.
 * <p>
 * This class overrides {@link #equals} and {@link #hashCode} so that instances
 * play nicely with the Collection classes.
 *
 * @author Fabiel Zuniga
 * @author Simon Hunt
 */
public final class IpTarget implements Addressable<IpAddress> {

    private static final String E_NULL_PARAM = "Parameters cannot be null";

    private final IpAddress ip;
    private final PortNumber port;

    // TODO: Be consistent with other classes in this package, Host.valueOf(...)
    // ... and make the constructor private.
    /**
     * Creates a host.
     *
     * @param ipAddress host's IP address
     * @param port port the host is listening to
     */
    public IpTarget(IpAddress ipAddress, PortNumber port) {
        if (ipAddress == null || port == null)
            throw new NullPointerException(E_NULL_PARAM);
        this.ip = ipAddress;
        this.port = port;
    }

    @Override
    public IpAddress address() {
        return ip;
    }

    /**
     * Returns the port on which the host is listening.
     *
     * @return the host's port
     */
    public PortNumber port() {
        return port;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        IpTarget ipTarget = (IpTarget) o;
        return ip.equals(ipTarget.ip) && port.equals(ipTarget.port);
    }

    @Override
    public int hashCode() {
        int result = ip.hashCode();
        result = 31 * result + port.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[ip=" + ip + ", port=" + port
                + ']';
    }
}
