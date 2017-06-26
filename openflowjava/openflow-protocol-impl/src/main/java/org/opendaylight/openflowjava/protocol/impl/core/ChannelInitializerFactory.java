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
 * @author michal.polkorab
 *
 */
public class ChannelInitializerFactory {

    private long switchIdleTimeOut;
    private DeserializationFactory deserializationFactory;
    private SerializationFactory serializationFactory;
    private TlsConfiguration tlsConfig;
    private SwitchConnectionHandler switchConnectionHandler;
    private boolean useBarrier;

    /**
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
        return initializer;
    }

    /**
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
     * @param switchIdleTimeOut
     */
    public void setSwitchIdleTimeout(final long switchIdleTimeOut) {
        this.switchIdleTimeOut = switchIdleTimeOut;
    }

    /**
     * @param deserializationFactory
     */
    public void setDeserializationFactory(final DeserializationFactory deserializationFactory) {
        this.deserializationFactory = deserializationFactory;
    }

    /**
     * @param serializationFactory
     */
    public void setSerializationFactory(final SerializationFactory serializationFactory) {
        this.serializationFactory = serializationFactory;
    }

    /**
     * @param tlsConfig
     */
    public void setTlsConfig(final TlsConfiguration tlsConfig) {
        this.tlsConfig = tlsConfig;
    }

    /**
     * @param switchConnectionHandler
     */
    public void setSwitchConnectionHandler(final SwitchConnectionHandler switchConnectionHandler) {
        this.switchConnectionHandler = switchConnectionHandler;
    }

    /**
     * @param useBarrier
     */
    public void setUseBarrier(final boolean useBarrier) {
        this.useBarrier = useBarrier;
    }
}