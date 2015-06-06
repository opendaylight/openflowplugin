/**
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowjava.nx.api;

import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.ActionChoice;

/**
 * @author msunal
 *
 */
public class NiciraActionSerializerKey {

    private final short version;
    private final Class<? extends ActionChoice> subtype;

    /**
     * @param version protocol wire version
     * @param subtype nx_action_subtype
     */
    public NiciraActionSerializerKey(final short version, final Class<? extends ActionChoice> subtype) {
        this.version = version;
        this.subtype = subtype;
    }

    public short getVersion() {
        return version;
    }

    public Class<? extends ActionChoice> getSubtype() {
        return subtype;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((subtype == null) ? 0 : subtype.hashCode());
        result = prime * result + version;
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
        if (subtype == null) {
            if (other.subtype != null) {
                return false;
            }
        } else if (!subtype.equals(other.subtype)) {
            return false;
        }
        if (version != other.version) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "NiciraActionSerializerKey [version=" + version + ", subtype=" + subtype + "]";
    }

}
