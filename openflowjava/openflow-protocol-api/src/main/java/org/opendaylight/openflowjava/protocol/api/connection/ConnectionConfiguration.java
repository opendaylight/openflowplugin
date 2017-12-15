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
 * Configuration for a switch connection.
 *
 * @author mirehak
 */
public interface ConnectionConfiguration {

    /**
     * Returns address to bind.
     *
     * @return address to bind, if null, all available interfaces will be used
     */
    InetAddress getAddress();

    /**
     * Returns the port to bind.
     *
     * @return port to bind
     */
    int getPort();

    /**
     * Returns the transport protocol to use.
     *
     * @return transport protocol to use
     */
    Object getTransferProtocol();

    /**
     * Returns the TLS configuration.
     *
     * @return TLS configuration object
     */
    TlsConfiguration getTlsConfiguration();

    /**
     * Returns the swicth idle timeout.
     *
     * @return silence time (in milliseconds) - after this time
     *         {@link org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.system.rev130927.SwitchIdleEvent}
     *         message is sent upstream
     */
    long getSwitchIdleTimeout();

    /**
     * Returns the SSL context.
     *
     * @return seed for {@link javax.net.ssl.SSLEngine}
     */
    Object getSslContext();

    /**
     * Returns the thread configuration.
     *
     * @return thread numbers for TcpHandler's eventloopGroups
     */
    ThreadConfiguration getThreadConfiguration();

    /**
     * Determines if a barrier shoild be used.
     *
     * @return boolean value for usability of Barrier
     */
    boolean useBarrier();

    /**
     * Checks if group add mod messages are enabled.
     * @return true if group add mod messages are enabled
     */
    boolean isGroupAddModEnabled();

    /**
     * Returns the queue size.
     *
     * @return Configurable queue size
     */
    int getChannelOutboundQueueSize();
}
