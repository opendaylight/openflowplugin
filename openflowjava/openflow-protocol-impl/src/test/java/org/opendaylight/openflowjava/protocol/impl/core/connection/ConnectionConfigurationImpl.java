/*
 * Copyright (c) 2013 Pantheon Technologies s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowjava.protocol.impl.core.connection;

import java.net.InetAddress;
import org.opendaylight.openflowjava.protocol.api.connection.ConnectionConfiguration;
import org.opendaylight.openflowjava.protocol.api.connection.ThreadConfiguration;
import org.opendaylight.openflowjava.protocol.api.connection.TlsConfiguration;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.config.rev140630.TransportProtocol;

/**
 * @author michal.polkorab
 *
 */
public class ConnectionConfigurationImpl implements ConnectionConfiguration {

    private final InetAddress address;
    private final int port;
    private Object transferProtocol;
    private final TlsConfiguration tlsConfig;
    private final long switchIdleTimeout;
    private ThreadConfiguration threadConfig;
    private final boolean useBarrier;

    /**
     * Creates {@link ConnectionConfigurationImpl}
     *
     * @param address
     * @param port
     * @param tlsConfig
     * @param switchIdleTimeout
     * @param useBarrier
     */
    public ConnectionConfigurationImpl(final InetAddress address, final int port, final TlsConfiguration tlsConfig,
            final long switchIdleTimeout, final boolean useBarrier) {
        this.address = address;
        this.port = port;
        this.tlsConfig = tlsConfig;
        this.switchIdleTimeout = switchIdleTimeout;
        this.useBarrier = useBarrier;
    }

    @Override
    public InetAddress getAddress() {
        return address;
    }

    @Override
    public int getPort() {
        return port;
    }

    @Override
    public Object getTransferProtocol() {
        return transferProtocol;
    }

    /**
     * Used for testing - sets transport protocol
     * @param protocol
     */
    public void setTransferProtocol(final TransportProtocol protocol) {
        this.transferProtocol = protocol;
    }

    @Override
    public long getSwitchIdleTimeout() {
        return switchIdleTimeout;
    }

    @Override
    public Object getSslContext() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public TlsConfiguration getTlsConfiguration() {
        return tlsConfig;
    }

    @Override
    public ThreadConfiguration getThreadConfiguration() {
        return threadConfig;
    }

    /**
     * @param threadConfig thread model configuration (configures threads used)
     */
    public void setThreadConfiguration(final ThreadConfiguration threadConfig) {
        this.threadConfig = threadConfig;
    }

    @Override
    public boolean useBarrier() {
        return useBarrier;
    }
}