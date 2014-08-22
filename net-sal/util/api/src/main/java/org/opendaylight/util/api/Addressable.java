/*
 * (c) Copyright 2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.util.api;

/**
 * Addressable.
 * 
 * @param <A> type of the address
 */
public interface Addressable<A> {

    /**
     * Gets the address.
     * 
     * @return the address
     */
    public A address();
}
