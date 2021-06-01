/*
 * Copyright (c) 2013 Pantheon Technologies s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowjava.protocol.impl.util;

import static java.util.Objects.requireNonNull;

import org.opendaylight.yangtools.yang.common.Uint8;

/**
 * Base class for a CodeKeyMaker.
 *
 * @author michal.polkorab
 */
public abstract class AbstractCodeKeyMaker implements CodeKeyMaker {
    private final Uint8 version;

    /**
     * Constractor.
     *
     * @param version openflow wire version
     */
    public AbstractCodeKeyMaker(final Uint8 version) {
        this.version = requireNonNull(version);

    }

    /**
     * Returns the version.
     */
    public Uint8 getVersion() {
        return version;
    }
}
