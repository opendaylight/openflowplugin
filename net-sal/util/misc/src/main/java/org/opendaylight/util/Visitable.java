/*
 * (C) Copyright 2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.util;

/**
 * Visitable (See visitor pattern).
 *
 * @param <V> type of the visitor (To apply visitor pattern to different types of visitables).
 */
public interface Visitable<V> {

    /**
     * Accepts a visitor (See visitor pattern).
     * 
     * @param visitor visitor.
     */
    public void accept(V visitor);
}
