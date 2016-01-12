/**
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.common;

import com.google.common.base.MoreObjects.ToStringHelper;
import com.google.common.base.Preconditions;

/**
 *
 */
public class InjectionResultTargetKey extends InjectionKey {

    private final Class<?> resultClazz;

    /**
     * @param version openflow version
     * @param targetClazz target class
     * @param resultClazz result class
     */
    public InjectionResultTargetKey(final int version, final Class<?> targetClazz, final Class<?> resultClazz) {
        super(version, targetClazz);
        this.resultClazz = Preconditions.checkNotNull(resultClazz);
    }

    @Override
    public int hashCode() {
        return super.hashCode() ^ resultClazz.hashCode();
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (!super.equals(obj)) {
            return false;
        }
        final InjectionResultTargetKey other = (InjectionResultTargetKey) obj;
        return resultClazz.equals(other.resultClazz);
    }

    @Override
    protected ToStringHelper addToStringAttributes(final ToStringHelper toStringHelper) {
        return super.addToStringAttributes(toStringHelper).add("resultClazz", resultClazz);
    }
}
