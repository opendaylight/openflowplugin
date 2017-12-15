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
 * Test implementation for ConnectionConfiguration.
 *
 * @author michal.polkorab
 */
public class ConnectionConfigurationImpl implements ConnectionConfiguration {

    private final InetAddress address;
    private final int port;
    private Object transferProtocol;
    private final TlsConfiguration tlsConfig;
    private final long switchIdleTimeout;
    private ThreadConfiguration threadConfig;
    private final boolean useBarrier;
    private final boolean isGroupAddModEnabled;
    private final int channelOutboundQueueSize;

    /**
     * Creates {@link ConnectionConfigurationImpl}.
     */
    public ConnectionConfigurationImpl(final InetAddress address, final int port, final TlsConfiguration tlsConfig,
            final long switchIdleTimeout, final boolean useBarrier, final boolean isGroupAddModEnabled,
                                       final int channelOutboundQueueSize) {
        this.address = address;
        this.port = port;
        this.tlsConfig = tlsConfig;
        this.switchIdleTimeout = switchIdleTimeout;
        this.useBarrier = useBarrier;
        this.isGroupAddModEnabled = isGroupAddModEnabled;
        this.channelOutboundQueueSize = channelOutboundQueueSize;
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

    @Override
    public int getChannelOutboundQueueSize() {
        return channelOutboundQueueSize;
    }

    /**
     * Used for testing - sets transport protocol.
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
     * Sets the ThreadConfiguration.
     *
     * @param config thread model configuration (configures threads used)
     */
    public void setThreadConfiguration(final ThreadConfiguration config) {
        this.threadConfig = config;
    }

    @Override
    public boolean useBarrier() {
        return useBarrier;
    }

    @Override
    public boolean isGroupAddModEnabled() {
        return isGroupAddModEnabled;
    }
}
