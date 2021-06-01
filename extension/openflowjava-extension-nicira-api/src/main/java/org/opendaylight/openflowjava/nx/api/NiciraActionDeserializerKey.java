/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowjava.nx.api;

import static java.util.Objects.requireNonNull;

import org.opendaylight.yangtools.yang.common.Uint8;

/**
 * Deserializer key for a Nicira action.
 *
 * @author msunal
 */
public final class NiciraActionDeserializerKey {

    private final Uint8 version;
    private final int subtype;

    /**
     * Contructor.
     *
     * @param version protocol wire version
     * @param subtype nx_action_subtype
     */
    public NiciraActionDeserializerKey(final Uint8 version, final int subtype) {
        if (!isValueUint16(subtype)) {
            throw new IllegalArgumentException(
                    "Nicira subtype is uint16. A value of subtype has to be between 0 and 65535 include.");
        }
        this.version = requireNonNull(version);
        this.subtype = subtype;
    }

    public Uint8 getVersion() {
        return version;
    }

    public int getSubtype() {
        return subtype;
    }

    private static boolean isValueUint16(final int value) {
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
        result = prime * result + version.hashCode();
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
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
        return subtype == other.subtype && version == other.version;
    }

    @Override
    public String toString() {
        return "NiciraActionDeserializerKey [version=" + version + ", subtype=" + subtype + "]";
    }

}
