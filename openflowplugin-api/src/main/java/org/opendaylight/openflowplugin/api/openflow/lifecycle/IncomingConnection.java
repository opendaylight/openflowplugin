/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.api.openflow.lifecycle;

/**
 * Decide what to do with new incoming connection if there is already a connection to the device.
 */
public enum IncomingConnection {
    /**
     * Drop the new connection.
     */
    DROP,
    /**
     * Drop the old connection and keep the new incoming connection.
     */
    DROP_PREVIOUS,
    /**
     * Keep both connections.
     */
    KEEP_BOTH
}
