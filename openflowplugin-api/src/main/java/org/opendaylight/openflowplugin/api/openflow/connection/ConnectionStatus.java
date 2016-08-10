package org.opendaylight.openflowplugin.api.openflow.connection;

/**
 * Define all possible states of connection after handshake
 */
public enum ConnectionStatus {

    // this flag should be return if no connection on this node exists
    MAY_CONTINUE,

    // this flag should be return if node still connected but not in termination state
    ALREADY_CONNECTED,

    // this flag should be return if node still connected but already in termination state
    CLOSING
}
