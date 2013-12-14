/**
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.common;

/**
 * injection lookup key based on version and target object
 */
public class InjectionKey {

    private int version;
    private String targetClazz;
    
    /**
     * @param version
     * @param targetClazz
     */
    public InjectionKey(int version, String targetClazz) {
        this.version = version;
        this.targetClazz = targetClazz;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
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
        InjectionKey other = (InjectionKey) obj;
        if (targetClazz == null) {
            if (other.targetClazz != null)
                return false;
        } else if (!targetClazz.equals(other.targetClazz))
            return false;
        if (version != other.version)
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "InjectionKey [version=" + version + ", targetClazz="
                + targetClazz + "]";
    }
}
