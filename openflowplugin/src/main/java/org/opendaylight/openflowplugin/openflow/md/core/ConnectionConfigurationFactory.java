/**
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.openflow.md.core;

import java.net.InetAddress;

import org.opendaylight.openflowjava.protocol.api.connection.ConnectionConfiguration;

/**
 * @author mirehak
 */
public abstract class ConnectionConfigurationFactory {

    /** OF default listening port */
    private static final int DEFAULT_OF_PORT = 6653;
    /** OF legacy default listening port */
    private static final int LEGACY_OF_PORT = 6633;

    /**
     * @return default connection configuration
     */
    public static ConnectionConfiguration getDefault() {
        return new ConnectionConfiguration() {

            @Override
            public InetAddress getAddress() {
                // all interfaces
                return null;
            }

            @Override
            public int getPort() {
                return DEFAULT_OF_PORT;
            }

            @Override
            public FEATURE_SUPPORT getTlsSupport() {
                return FEATURE_SUPPORT.NOT_SUPPORTED;
            }

            @Override
            public Object getTransferProtocol() {
                // TODO:: TCP/UDP ...
                return null;
            }

            @Override
            public long getSwitchIdleTimeout() {
                return 15000;
            }

            @Override
            public Object getSslContext() {
                return null;
            }
        };
    }
    
    /**
     * @return default connection configuration
     */
    public static ConnectionConfiguration getLegacy() {
        return new ConnectionConfiguration() {

            @Override
            public InetAddress getAddress() {
                // all interfaces
                return null;
            }

            @Override
            public int getPort() {
                return LEGACY_OF_PORT;
            }

            @Override
            public FEATURE_SUPPORT getTlsSupport() {
                return FEATURE_SUPPORT.NOT_SUPPORTED;
            }

            @Override
            public Object getTransferProtocol() {
                // TODO:: TCP/UDP ...
                return null;
            }

            @Override
            public long getSwitchIdleTimeout() {
                return 15000;
            }

            @Override
            public Object getSslContext() {
                return null;
            }
        };
    }

}
