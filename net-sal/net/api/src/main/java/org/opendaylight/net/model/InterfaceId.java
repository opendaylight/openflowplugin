/*
 * (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.net.model;

import org.opendaylight.util.net.BigPortNumber;

/**
 * Interface identifier unique to the network element to which the interface
 * belongs.
 *
 * @author Thomas Vachuska
 * @author Uyen Chau
 * @author Simon Hunt
 */
public class InterfaceId implements Comparable<InterfaceId> {
    
    private final BigPortNumber port;
    
    private static final BigPortNumber NONE_PORT = BigPortNumber.bpn(0);
    
    /**
     * A static value used to indicate an unknown/unnecessary interface ID.
     */
    public static final InterfaceId NONE = new InterfaceId(NONE_PORT);
    
    /**
     * Private constructor to create an interface ID based on the given port.
     * 
     * @param port the given port
     */
    protected InterfaceId(BigPortNumber port) {
        this.port = port;
    }

    /**
     * Returns the value of the given port as an interface ID.
     * 
     * @param port the given port
     * @return interface ID representation
     */
    public static InterfaceId valueOf(BigPortNumber port) {
        if (port == null)
            throw new IllegalArgumentException("Port cannot be null");

        return new InterfaceId(port);
    }

    /**
     * Returns the value of this interface ID as a port number. If the
     * interface ID cannot be represented as a port, null is returned.
     * 
     * @return port number
     */
    public BigPortNumber port() {
        return this.port;
    }

    @Override
    public String toString() {
        return this.port.toString();
    }

    @Override
    public int compareTo(InterfaceId o) {
        return this.port.compareTo(o.port);
    }

    @Override
    public boolean equals(Object o) {
       if (this == o) return true;
       if (o == null || getClass() != o.getClass()) return false;

        InterfaceId that = (InterfaceId) o;
        return (this.port.equals(that.port));
    }

    @Override
    public int hashCode() {
        return(port.hashCode());
    }
}
