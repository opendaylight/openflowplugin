/*
 * (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.of.controller;

/**
 * An initialization task to process a connecting datapath that runs after
 * the extended handshake but before the datapath is declared "Ready".
 *
 * @author Simon Hunt
 */
public interface PostHandshakeTask extends Runnable {

    /**
     * Marks the task as no longer valid. That is, if it has not run yet,
     * it should not run at all.
     */
    void invalidate();

    /**
     * Indicates whether the task is still valid.
     *
     * @return true, if still valid; false otherwise
     */
    boolean isValid();

}
