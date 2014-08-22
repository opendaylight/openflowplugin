/*
 * (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.util.api;

/**
 * Validates a given state.
 * 
 * @author Mark Mozolewski
 */
public interface StateValidator<T> {

    /**
     * Determine if the state is valid.
     * 
     * @param state the state to validate
     * @return empty string {@code ""} if state is valid, otherwise string
     *         contains reason state is invalid
     */
    String isStateValid(T state);
}
