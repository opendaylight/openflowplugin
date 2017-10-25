/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.api.openflow.device;

import com.google.common.base.Preconditions;

public final class Xid {
    private final Long value;

    public Xid(final Long value) {
        this.value = Preconditions.checkNotNull(value);
    }

    public Long getValue() {
        return value;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + value.hashCode();
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof Xid)) {
            return false;
        }
        return value.equals(((Xid) obj).value);
    }

    @Override
    public String toString() {
        return "Xid [value=" + value + "]";
    }
}
