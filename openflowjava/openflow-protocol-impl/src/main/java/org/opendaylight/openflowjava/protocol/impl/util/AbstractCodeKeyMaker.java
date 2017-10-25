/*
 * Copyright (c) 2013 Pantheon Technologies s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowjava.protocol.impl.util;

/**
 * @author michal.polkorab
 */
public abstract class AbstractCodeKeyMaker implements CodeKeyMaker {

    private short version;

    /**
     * @param version openflow wire version
     */
    public AbstractCodeKeyMaker(short version) {
        this.version = version;

    }

    /**
     * @return the version
     */
    public short getVersion() {
        return version;
    }

}
