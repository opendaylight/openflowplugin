/*
 * (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.net.model;

/**
 * Default implementation of {@link HostLocation}.
 *
 * @author Shaun Wackerly
 */
public class DefaultHostLocation implements HostLocation {

    private static final String E_NULL_PARAM = "Parameters cannot be null";

    private final DeviceId id;
    private final InterfaceId intfId;
    private final long timestamp;

    /**
     * Constructs a host location at the given device and interface, using
     * the current system timestamp.
     * 
     * @param id given device ID
     * @param intfId given interface ID
     */
    public DefaultHostLocation(DeviceId id, InterfaceId intfId) {
        if (id == null || intfId == null)
            throw new NullPointerException(E_NULL_PARAM);
        
        this.id = id;
        this.intfId = intfId;
        this.timestamp = System.currentTimeMillis();
    }
    
    /**
     * Constructs a host location from the given connection point. If the
     * connection point element ID is not a device ID, then an
     * {@link IllegalArgumentException} will be thrown.
     * 
     * @param cp connection point
     * @throws IllegalArgumentException if connection point is not on device
     */
    public DefaultHostLocation(ConnectionPoint cp) {
        ElementId elem = cp.elementId();
        if (!(elem instanceof DeviceId)) {
            throw new IllegalArgumentException(elem.getClass().getName() +
                                               " is not a DeviceId");
        }

        this.id = (DeviceId) elem;
        this.intfId = cp.interfaceId();
        this.timestamp = System.currentTimeMillis();
    }

    @Override
    public DeviceId elementId() {
        return id;
    }

    @Override
    public InterfaceId interfaceId() {
        return intfId;
    }

    @Override
    public long ts() {
        return timestamp;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        // NOTE: Timestamp is NOT included in the equivalence test. Callers
        // must explicitly compare timestamps if they want that comparison.
        DefaultHostLocation that = (DefaultHostLocation) o;
        return id.equals(that.id) && intfId.equals(that.intfId);
    }

    @Override
    public int hashCode() {
        int result = id.hashCode();
        result = 31 * result + intfId.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return id+"/"+intfId;
    }

}
