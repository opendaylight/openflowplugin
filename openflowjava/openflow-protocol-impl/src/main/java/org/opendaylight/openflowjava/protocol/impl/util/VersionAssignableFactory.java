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
import org.opendaylight.yangtools.yang.common.Uint8;

/**
 * Abstract factory class to support OF protocol version assigning and reading.
 */
public abstract class VersionAssignableFactory<T extends DataContainer> implements OFDeserializer<T> {
    private Uint8 version;

    /**
     * Assigns the version.
     *
     * @param newVersion OpenFlow protocol version
     */
    public void assignVersion(final @NonNull Uint8 newVersion) {
        if (version != null) {
            throw new IllegalStateException("Version already assigned: " + version);
        }
        version = newVersion;
    }

    /**
     * Returns the OpenFlow protocol version.
     */
    protected final Uint8 getVersion() {
        return this.version;
    }
}
