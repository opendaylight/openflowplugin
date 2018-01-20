/*
 * Copyright (c) 2013 Pantheon Technologies s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowjava.protocol.impl.util;


/**
 * Base class for a TypeKeyMaker.
 *
 * @author michal.polkorab
 * @param <T> type the key maker is based on
 */
public abstract class AbstractTypeKeyMaker<T> implements TypeKeyMaker<T> {

    private final short version;

    /**
     * Constructor.
     *
     * @param version openflow wire version
     */
    public AbstractTypeKeyMaker(short version) {
        this.version = version;

    }

    /**
     * Returns the version.
     */
    public short getVersion() {
        return version;
    }
}
