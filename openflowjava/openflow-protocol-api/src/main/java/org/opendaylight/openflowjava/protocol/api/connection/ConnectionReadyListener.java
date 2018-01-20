/*
 * Copyright (c) 2013 Pantheon Technologies s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowjava.protocol.api.connection;

/**
 * Listener that is notified when a switch connection is ready.
 *
 * @author mirehak
 */
public interface ConnectionReadyListener {

    /**
     * Fired when connection becomes ready-to-use.
     */
    void onConnectionReady();
}
