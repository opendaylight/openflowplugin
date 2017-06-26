/*
 * Copyright (c) 2015 Pantheon Technologies s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowjava.protocol.impl.core;

/**
 * @author martin.uhlir
 *
 */
public interface ConnectionInitializer {

    /**
     * Initiates connection towards device
     * @param host - host IP
     * @param port - port number
     */
    void initiateConnection(String host, int port);

}
