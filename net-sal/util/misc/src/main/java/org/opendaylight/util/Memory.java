/*
 * (C) Copyright 2014 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.util;

/**
 * Read/Write memory.
 * 
 * @param <D> type of the data
 * @author Fabiel Zuniga
 */
public interface Memory<D> {

    /**
     * Reads the memory.
     * 
     * @return memory's data
     */
    public D read();

    /**
     * Writes data to the memory.
     *
     * @param data data to write
     */
    public void write(D data);
}
