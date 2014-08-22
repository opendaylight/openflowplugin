/*
 * (C) Copyright 2011 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.util;

/**
 * Provider.
 * 
 * @param <P> type of the provision.
 * @param <E> type of the entity to get the provision for.
 */
public interface Provider<P, E> {

    /**
     * Gets the provision for the given entity.
     * 
     * @param entity entity to get the provision for.
     * @return entity's provision.
     */
    public P get(E entity);
}
