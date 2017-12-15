/*
 * Copyright (c) 2014 Pantheon Technologies s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowjava.protocol.impl.core;

import org.opendaylight.openflowjava.protocol.api.connection.SwitchConnectionHandler;
import org.opendaylight.openflowjava.protocol.api.connection.TlsConfiguration;
import org.opendaylight.openflowjava.protocol.impl.deserialization.DeserializationFactory;
import org.opendaylight.openflowjava.protocol.impl.serialization.SerializationFactory;

/**
 * Factory for ChannelInitializer instances.
 *
 * @author michal.polkorab
 */
public class ChannelInitializerFactory {

    private long switchIdleTimeOut;
    private DeserializationFactory deserializationFactory;
    private SerializationFactory serializationFactory;
    private TlsConfiguration tlsConfig;
    private SwitchConnectionHandler switchConnectionHandler;
    private boolean useBarrier;
    private int channelOutboundQueueSize;

    /**
     * Creates a TCP publishing channel initializer.
     *
     * @return PublishingChannelInitializer that initializes new channels
     */
    public TcpChannelInitializer createPublishingChannelInitializer() {
        final TcpChannelInitializer initializer = new TcpChannelInitializer();
        initializer.setSwitchIdleTimeout(switchIdleTimeOut);
        initializer.setDeserializationFactory(deserializationFactory);
        initializer.setSerializationFactory(serializationFactory);
        initializer.setTlsConfiguration(tlsConfig);
        initializer.setSwitchConnectionHandler(switchConnectionHandler);
        initializer.setUseBarrier(useBarrier);
        initializer.setChannelOutboundQueueSize(channelOutboundQueueSize);
        return initializer;
    }

    /**
     * Creates a UDP channel initializer.
     *
     * @return PublishingChannelInitializer that initializes new channels
     */
    public UdpChannelInitializer createUdpChannelInitializer() {
        final UdpChannelInitializer initializer = new UdpChannelInitializer();
        initializer.setSwitchIdleTimeout(switchIdleTimeOut);
        initializer.setDeserializationFactory(deserializationFactory);
        initializer.setSerializationFactory(serializationFactory);
        initializer.setSwitchConnectionHandler(switchConnectionHandler);
        return initializer;
    }

    /**
     * Sets the switch idle timeout.
     *
     * @param timeout the timeout
     */
    public void setSwitchIdleTimeout(final long timeout) {
        this.switchIdleTimeOut = timeout;
    }

    /**
     * Sets the DeserializationFactory.
     *
     * @param deserializationFactory the DeserializationFactory
     */
    public void setDeserializationFactory(final DeserializationFactory deserializationFactory) {
        this.deserializationFactory = deserializationFactory;
    }

    /**
     * Sets the SerializationFactory.
     *
     * @param serializationFactory the SerializationFactory
     */
    public void setSerializationFactory(final SerializationFactory serializationFactory) {
        this.serializationFactory = serializationFactory;
    }

    /**
     * Sets the TlsConfiguration.
     *
     * @param tlsConfig the TlsConfiguration
     */
    public void setTlsConfig(final TlsConfiguration tlsConfig) {
        this.tlsConfig = tlsConfig;
    }

    /**
     * Sets the SwitchConnectionHandler.
     *
     * @param switchConnectionHandler the SwitchConnectionHandler
     */
    public void setSwitchConnectionHandler(final SwitchConnectionHandler switchConnectionHandler) {
        this.switchConnectionHandler = switchConnectionHandler;
    }

    /**
     * Sets whether or not to use a barrier.
     */
    public void setUseBarrier(final boolean useBarrier) {
        this.useBarrier = useBarrier;
    }

    /**
     * Sets the channelOutboundQueueSize.
     *
     * @param channelOutboundQueueSize the channelOutboundQueueSize
     */
    public void setChannelOutboundQueueSize(final int channelOutboundQueueSize) {
        this.channelOutboundQueueSize = channelOutboundQueueSize;
    }
}
