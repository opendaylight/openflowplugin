/*
 * Copyright (c) 2013 Pantheon Technologies s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */


package org.opendaylight.openflowjava.protocol.api.connection;

import java.net.InetAddress;

/**
 * @author mirehak
 * @author michal.polkorab
 *
 */
public interface SwitchConnectionHandler {

    /**
     * @param connection to switch proving message sending/receiving, connection management
     */
    void onSwitchConnected(ConnectionAdapter connection);

    /**
     * @param switchAddress address of incoming connection (address + port)
     * @return true, if connection from switch having given address shell be accepted; false otherwise
     */
    boolean accept(InetAddress switchAddress);

}
