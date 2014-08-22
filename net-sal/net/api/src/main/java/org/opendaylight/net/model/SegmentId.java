/*
 * (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.net.model;

import org.opendaylight.util.net.VlanId;

import java.io.Serializable;

/**
 * Identifier for a network segment. It is unique to the type
 * of network to which it applies.
 */
public class SegmentId implements Comparable<SegmentId>, Serializable {
    
    private static final long serialVersionUID = -4809435869269722897L;
    
    /** A segment ID which represents an unknown segment */
    public static final SegmentId UNKNOWN = new SegmentId(Type.INVALID, 0);

    /**
     * Network segment types.
     */
    public enum Type {
        VLAN,
        VXLAN,
        INVALID
    }
    
    private final Type type;
    private final int value;

    /**
     * Private constructor to create a segment ID with the given value and type.
     * 
     * @param type segment type
     * @param value segment value
     */
    private SegmentId(Type type, int value) {
        this.type = type;
        this.value = value;
    }

    /**
     * Returns the given VLAN ID represented as a segment ID.
     * 
     * @param vlanId VLAN ID
     * @return segment ID representation
     */
    public static SegmentId valueOf(VlanId vlanId) {
        return new SegmentId(Type.VLAN, vlanId.toInt());
    }

    /**
     * Returns the identifier as an integer value.
     * 
     * @return integer value
     */
    public int value() {
        return value;
    }

    /**
     * Returns the type of network segment this identifies.
     * 
     * @return type of network segment
     */
    public Type type() {
        return type;
    }
    
    @Override
    public int compareTo(SegmentId other) {
        if (this.type != other.type)
            return (this.type == Type.VLAN ? 1 : -1);
        return this.value - other.value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SegmentId segmentId = (SegmentId) o;
        return value == segmentId.value && type == segmentId.type;
    }

    @Override
    public int hashCode() {
        int result = type.hashCode();
        result = 31 * result + value;
        return result;
    }

    @Override
    public String toString() {
        return type.toString() + "-" + value;
    }

}
