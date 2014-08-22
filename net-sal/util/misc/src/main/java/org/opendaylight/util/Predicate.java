/*
 * (C) Copyright 2012 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.util;

/**
 * Predicate.
 * 
 * @param <T> type of the predicate input
 * @author Fabiel Zuniga
 */
public interface Predicate<T> {

    /**
     * Evaluates the predicate.
     * 
     * @param input input
     * @return {@code true} if the predicate is true when it is evaluated against the given input,
     *         {@code false} otherwise
     */
    public boolean evaluate(T input);
}
