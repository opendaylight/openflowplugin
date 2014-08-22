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
 * @author Anuradha Musunuri
 *
 */
public class TunnelIndex implements Comparable<TunnelIndex> {

    private BigPortNumber tunnelIndex;
    
    private static final BigPortNumber NONE_TUNNELINDEX = BigPortNumber.bpn(0);

    /**
     * A static value used to indicate an unknown/unnecessary tunnel index.
     */
    public static final TunnelIndex NONE = new TunnelIndex(NONE_TUNNELINDEX);

    /**
     * Private constructor to create an tunnel index based on the given index.
     * 
     * @param tunnelIndex the given port
     */
    private TunnelIndex(BigPortNumber tunnelIndex) {
        this.tunnelIndex = tunnelIndex;
    }

    /**
     * Returns the value of the given index as TunnelIndex.
     * 
     * @param tunnelIndex the given index
     * @return TunnelIndex representation
     */
    public static TunnelIndex valueOf(BigPortNumber tunnelIndex) {
        if (tunnelIndex == null)
            throw new IllegalArgumentException("TunnelIndex cannot be null");

        return new TunnelIndex(tunnelIndex);
    }

    /**
     * Returns the value of this Tunnel Id as a tunnel index . If the
     * Tunnel Index cannot be represented as an index, null is returned.
     * 
     * @return tunnelIndex
     */
    public BigPortNumber index() {
        return this.tunnelIndex;
    }

    @Override
    public String toString() {
        return this.tunnelIndex.toString();
    }

    @Override
    public int compareTo(TunnelIndex o) {
        return this.tunnelIndex.compareTo(o.tunnelIndex);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TunnelIndex that = (TunnelIndex) o;
        return (this.tunnelIndex.equals(that.tunnelIndex));
    }

    @Override
    public int hashCode() {
        return(tunnelIndex.hashCode());
    } 
}
