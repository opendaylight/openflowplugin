/*
 * Copyright (c) 2016 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowjava.protocol.impl.util;

import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.openflowjava.protocol.api.extensibility.OFDeserializer;
import org.opendaylight.yangtools.yang.binding.DataContainer;

/**
 * Abstract factory class to support OF protocol version assigning and reading.
 */
public abstract class VersionAssignableFactory<T extends DataContainer> implements OFDeserializer<T> {
    private Short version;

    /**
     * Assigns the version.
     *
     * @param newVersion OpenFlow protocol version
     */
    public void assignVersion(@NonNull final Short newVersion) {
        if (this.version == null) {
            this.version = newVersion;
        } else {
            throw new IllegalStateException("Version already assigned: " + this.version);
        }
    }

    /**
     * Returns the OpenFlow protocol version.
     */
    protected Short getVersion() {
        return this.version;
    }
}
