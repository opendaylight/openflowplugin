/*
 * Copyright (c) 2014 Pantheon Technologies s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowjava.protocol.api.connection;

/**
 * Threading configuration.
 *
 * @author michal.polkorab
 */
public interface ThreadConfiguration {

    /**
     * Returns the desired number of Openflow I/O worker threads.
     *
     * @return desired number of worker threads processing the Openflow I/O
     */
    int getWorkerThreadCount();

    /**
     * Returns the desired number of incoming Openflow connection threads.
     *
     * @return desired number of bossThreads registering incoming Openflow connections
     */
    int getBossThreadCount();
}
