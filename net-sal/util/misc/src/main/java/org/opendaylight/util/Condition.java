/*
 * (C) Copyright 2014 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.util;

/**
 * Condition.
 * 
 * @author Fabiel Zuniga
 */
public interface Condition {

    /**
     * Evaluates the condition.
     *
     * @return {@code true} if the condition is true, {@code false} otherwise
     */
    public boolean evaluate();
}
