/**
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.common;

import com.google.common.base.MoreObjects;
import com.google.common.base.MoreObjects.ToStringHelper;
import com.google.common.base.Preconditions;


/**
 * injection lookup key based on version and target object
 */
public class InjectionKey {

    private final int version;
    private final Class<?> targetClazz;

    /**
     * @param version openflow version
     * @param targetClazz target class
     */
    public InjectionKey(final int version, final Class<?> targetClazz) {
        this.version = version;
        this.targetClazz = Preconditions.checkNotNull(targetClazz);
    }

    @Override
    public int hashCode() {
        return 31 * version + targetClazz.hashCode();
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
        final InjectionKey other = (InjectionKey) obj;
        if (version != other.version) {
            return false;
        }
        return targetClazz.equals(other.targetClazz);
    }

    @Override
    public final String toString() {
        return addToStringAttributes(MoreObjects.toStringHelper(this)).toString();
    }

    protected ToStringHelper addToStringAttributes(final ToStringHelper toStringHelper) {
        return toStringHelper.add("version", version).add("targetClazz", targetClazz);
    }
}
