/**
 * Copyright (c) 2016 Pantheon Technologies s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.api.openflow.connection;

/**
 * Define all possible states of connection after handshake.
 */
public enum ConnectionStatus {

    // this flag should be return if no connection on this node exists
    MAY_CONTINUE,

    // this flag should be return if node still connected but not in termination state
    ALREADY_CONNECTED,

    // this flag should be return if node still connected but already in termination state
    CLOSING,

    //Flag used when auxiliary connection incomming but there is no primary connection
    REFUSING_AUXILIARY_CONNECTION
}
