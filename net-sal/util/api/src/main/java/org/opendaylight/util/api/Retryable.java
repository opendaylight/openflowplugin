/*
 * (c) Copyright 2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.util.api;

/**
 * Classes implementing this interface can express whether they should be
 * retried in the event of an unexpected condition.
 * 
 * @author Scott Simes
 * @author Fabiel Zuniga
 */
public interface Retryable {

    /**
     * Returns {@code true} if this task should be retried, {@code false}
     * otherwise.
     * 
     * @return {@code true} to retry, {@code false} otherwise
     */
    public boolean shouldRetry();
}
