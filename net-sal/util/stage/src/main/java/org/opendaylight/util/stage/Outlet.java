/*
 * (c) Copyright 2010 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.util.stage;

/**
 * Abstraction of an outlet capable of accepting items for some form of
 * processing. Outlets may perform such processing either synchronously, or
 * asynchronously. However, typically, the latter is the preferred behaviour
 * as it allows better behaved and balanced flows.
 * 
 * @author Thomas Vachuska
 * 
 * @param <T> type of item accepted/taken by the outlet
 */
public interface Outlet<T> {

    /**
     * Accepts the specified item for processing.
     * 
     * @param item item to be processed
     * @return true if the given item was accepted; false otherwise
     */
    public boolean accept(T item);

}
