/*
 * Copyright (c) 2013 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowjava.protocol.api.keys;

import static java.util.Objects.requireNonNull;

import org.opendaylight.yangtools.yang.common.Uint8;

/**
 * Key for a class type.
 *
 * @author michal.polkorab
 */
public class TypeToClassKey {
    private final Uint8 version;
    private final int type;

    /**
     * Constructor.
     *
     * @param version wire protocol version
     * @param type message type / code
     */
    public TypeToClassKey(final Uint8 version, final int type) {
        this.version = requireNonNull(version);
        this.type = type;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + type;
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
        TypeToClassKey other = (TypeToClassKey) obj;
        return type == other.type && version.equals(other.version);
    }
}
