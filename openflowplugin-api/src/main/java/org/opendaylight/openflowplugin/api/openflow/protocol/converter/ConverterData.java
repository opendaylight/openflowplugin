/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.api.openflow.protocol.converter;

/**
 * The base class for all converter data.
 */
public abstract class ConverterData {
    private short version;

    /**
     * Instantiates a new Converter data.
     *
     * @param version the version
     */
    public ConverterData(final short version) {
        this.version = version;
    }

    /**
     * Gets Openflow version.
     *
     * @return the version
     */
    public short getVersion() {
        return version;
    }
}
