/*
 * Copyright (c) 2018 Ericsson India Global Services Pvt Ltd. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.extension.api;

import static java.util.Objects.requireNonNull;

import org.opendaylight.yangtools.yang.common.Uint8;

/**
 * The base class for all convertor data.
 */
public abstract class ConvertorData {
    private final Uint8 version;

    /**
     *Instantiates a new Convertor data.
     * @param version the version
     */
    public ConvertorData(final Uint8 version) {
        this.version = requireNonNull(version);
    }

    /**
     * Gets Openflow version.
     *
     * @return the version
     */
    public Uint8 getVersion() {
        return version;
    }
}
