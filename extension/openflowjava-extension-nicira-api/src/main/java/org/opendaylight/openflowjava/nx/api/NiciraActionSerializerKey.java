/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowjava.nx.api;

import static java.util.Objects.requireNonNull;

import java.util.Objects;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.ActionChoice;
import org.opendaylight.yangtools.yang.common.Uint8;

/**
 * Serializer key for a Nicira action.
 *
 * @author msunal
 */
public class NiciraActionSerializerKey {

    private final Uint8 version;
    private final Class<? extends ActionChoice> subtype;

    /**
     * Contructor.
     *
     * @param version protocol wire version
     * @param subtype nx_action_subtype
     */
    public NiciraActionSerializerKey(final Uint8 version, final Class<? extends ActionChoice> subtype) {
        this.version = requireNonNull(version);
        this.subtype = subtype;
    }

    public Uint8 getVersion() {
        return version;
    }

    public Class<? extends ActionChoice> getSubtype() {
        return subtype;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + Objects.hashCode(subtype);
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
        NiciraActionSerializerKey other = (NiciraActionSerializerKey) obj;
        return Objects.equals(subtype, other.subtype) && version.equals(other.version);
    }

    @Override
    public String toString() {
        return "NiciraActionSerializerKey [version=" + version + ", subtype=" + subtype + "]";
    }

}
