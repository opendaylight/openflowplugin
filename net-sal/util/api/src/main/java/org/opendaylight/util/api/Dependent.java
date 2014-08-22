/*
 * (c) Copyright 2012 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.util.api;

/**
 * Dependent transport object.
 * 
 * @param <T> type of the owner.
 * @author Fabiel Zuniga
 */
public interface Dependent<T> {

    /**
     * Gets the owner of this dependent objects.
     * 
     * @return the owner.
     */
    public T getOwner();
}
