/**
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 * 
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowjava.cof.api;

import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev130731.ExperimenterActionSubType;

/**
 * @author msunal
 *
 */
public class CiscoActionSerializerKey {
    
    private final short version;
    private final Class<? extends ExperimenterActionSubType> subtype;

    /**
     * @param version protocol wire version
     * @param subtype cof_action_subtype
     */
    public CiscoActionSerializerKey(short version, Class<? extends ExperimenterActionSubType> subtype) {
        this.version = version;
        this.subtype = subtype;
    }

    public short getVersion() {
        return version;
    }

    public Class<? extends ExperimenterActionSubType> getSubtype() {
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
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        CiscoActionSerializerKey other = (CiscoActionSerializerKey) obj;
        if (subtype == null) {
            if (other.subtype != null)
                return false;
        } else if (!subtype.equals(other.subtype))
            return false;
        if (version != other.version)
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "CiscoActionSerializerKey [version=" + version + ", subtype=" + subtype + "]";
    }

}
