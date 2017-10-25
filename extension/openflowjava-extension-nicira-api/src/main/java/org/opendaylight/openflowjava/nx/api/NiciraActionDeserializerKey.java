/**
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 * 
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowjava.nx.api;

/**
 * @author msunal
 *
 */
public final class NiciraActionDeserializerKey {

    private final short version;
    private final int subtype;

    /**
     * @param version protocol wire version
     * @param subtype nx_action_subtype
     */
    public NiciraActionDeserializerKey(short version, int subtype) {
        if (!isValueUint16(subtype)) {
            throw new IllegalArgumentException(
                    "Nicira subtype is uint16. A value of subtype has to be between 0 and 65535 include.");
        }
        this.version = version;
        this.subtype = subtype;
    }

    public short getVersion() {
        return version;
    }

    public int getSubtype() {
        return subtype;
    }

    private static final boolean isValueUint16(int value) {
        if (value >= 0 && value <= 65535L) {
            return true;
        }
        return false;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + subtype;
        result = prime * result + version;
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        NiciraActionDeserializerKey other = (NiciraActionDeserializerKey) obj;
        if (subtype != other.subtype) {
            return false;
        }
        if (version != other.version) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "NiciraActionDeserializerKey [version=" + version + ", subtype=" + subtype + "]";
    }

}
