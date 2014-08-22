/*
 * Copyright (c) 2014 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.util.cache;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;

/**
 * Read-only iterator for collections of model entities.
 *
 * @author Thomas Vachuska
 */
public class ReadOnlyIterator<M, E extends M> implements Iterator<M> {

    private final Iterator<E> iter;

    /**
     * Creates a read-only iterator for the specified collection.
     *
     * @param collection item collection
     */
    public ReadOnlyIterator(Collection<E> collection) {
        iter = new HashSet<>(collection).iterator();
    }

    public boolean hasNext() {
        return iter.hasNext();
    }

    @Override
    public E next() {
        return iter.next();
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException();
    }

}

