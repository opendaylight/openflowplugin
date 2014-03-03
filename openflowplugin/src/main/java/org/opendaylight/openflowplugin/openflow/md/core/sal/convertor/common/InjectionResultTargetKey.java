/**
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.common;

/**
 * 
 */
public class InjectionResultTargetKey extends InjectionKey {

    private String resultClazz;
    
    /**
     * @param version
     * @param targetClazz
     * @param resultClazz 
     */
    public InjectionResultTargetKey(int version, String targetClazz, String resultClazz) {
        super(version, targetClazz);
        this.resultClazz = resultClazz;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result
                + ((resultClazz == null) ? 0 : resultClazz.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!super.equals(obj))
            return false;
        if (getClass() != obj.getClass())
            return false;
        InjectionResultTargetKey other = (InjectionResultTargetKey) obj;
        if (resultClazz == null) {
            if (other.resultClazz != null)
                return false;
        } else if (!resultClazz.equals(other.resultClazz))
            return false;
        return true;
    }
}
