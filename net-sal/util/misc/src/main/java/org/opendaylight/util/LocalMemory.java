/*
 * (C) Copyright 2012 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.util;

/**
 * Local memory. This class makes sure all threads access the same data and
 * not a local copy.
 * 
 * @param <D> type of the data
 * @author Fabiel Zuniga
 */
public class LocalMemory<D> implements Memory<D> {

    private D value;

    /**
     * Creates a local shared memory
     */
    public LocalMemory() {
        this(null);
    }

    /**
     * Creates a local shared memory.
     *
     * @param data data
     */
    public LocalMemory(D data) {
        this.value = data;
    }

    @Override
    public synchronized D read() {
        // Synchronized method to ensure all threads access the same data and
        // not a local copy.
        return this.value;
    }

    @Override
    public synchronized void write(D data) {
        // Synchronized method to ensure all threads access the same data and
        // not a local copy.
        this.value = data;
    }

    @Override
    public String toString() {
        return ObjectToStringConverter.toString(
                this,
                Property.valueOf("value", this.value)
        );
    }
}
