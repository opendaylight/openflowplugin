/*
 * Copyright (c) 2013 Pantheon Technologies s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */


package org.opendaylight.openflowjava.protocol.impl.core;

import org.opendaylight.openflowjava.protocol.api.connection.ThreadConfiguration;

/**
 * Server facade interface.
 *
 * @author mirehak
 */
public interface ServerFacade extends ShutdownProvider, OnlineProvider, Runnable {

    /**
     * Sets thread configuration.
     *
     * @param threadConfig desired thread configuration
     */
    void setThreadConfig(ThreadConfiguration threadConfig);
}
