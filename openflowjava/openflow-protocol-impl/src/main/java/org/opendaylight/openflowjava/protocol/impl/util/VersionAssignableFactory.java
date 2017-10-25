/*
 * Copyright (c) 2016 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowjava.protocol.impl.util;

import javax.annotation.Nonnull;

/**
 * Abstract factory class to support OF protocol version assigning and reading.
 */
public abstract class VersionAssignableFactory {
    private Short version;

    /**
     * @param version OpenFlow protocol version
     */
    public void assignVersion(@Nonnull final Short version) {
        if (this.version == null) {
            this.version = version;
        } else {
            throw new IllegalStateException("Version already assigned: " + this.version);
        }
    }

    /**
     * @return OpenFlow protocol version
     */
    protected Short getVersion() {
        return this.version;
    }
}