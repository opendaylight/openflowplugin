/*
 * Copyright (c) 2014 Pantheon Technologies s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowjava.protocol.impl.core;

import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import org.opendaylight.openflowjava.protocol.api.connection.SwitchConnectionHandler;
import org.opendaylight.openflowjava.protocol.api.connection.TlsConfiguration;
import org.opendaylight.openflowjava.protocol.impl.deserialization.DeserializationFactory;
import org.opendaylight.openflowjava.protocol.impl.serialization.SerializationFactory;

/**
 * @param <C> Channel type
 * @author michal.polkorab
 */
public abstract class ProtocolChannelInitializer<C extends Channel>
        extends ChannelInitializer<C> {

    private SwitchConnectionHandler switchConnectionHandler;
    private long switchIdleTimeout;
    private SerializationFactory serializationFactory;
    private DeserializationFactory deserializationFactory;
    private TlsConfiguration tlsConfiguration;
    private boolean useBarrier;

    /**
     * @param switchConnectionHandler the switchConnectionHandler to set
     */
    public void setSwitchConnectionHandler(final SwitchConnectionHandler switchConnectionHandler) {
        this.switchConnectionHandler = switchConnectionHandler;
    }

    /**
     * @param switchIdleTimeout the switchIdleTimeout to set
     */
    public void setSwitchIdleTimeout(final long switchIdleTimeout) {
        this.switchIdleTimeout = switchIdleTimeout;
    }

    /**
     * @param serializationFactory
     */
    public void setSerializationFactory(final SerializationFactory serializationFactory) {
        this.serializationFactory = serializationFactory;
    }

    /**
     * @param deserializationFactory
     */
    public void setDeserializationFactory(final DeserializationFactory deserializationFactory) {
        this.deserializationFactory = deserializationFactory;
    }

    /**
     * @param tlsConfiguration
     */
    public void setTlsConfiguration(final TlsConfiguration tlsConfiguration) {
        this.tlsConfiguration = tlsConfiguration;
    }

    /**
     * @return switch connection handler
     */
    public SwitchConnectionHandler getSwitchConnectionHandler() {
        return switchConnectionHandler;
    }

    /**
     * @return switch idle timeout
     */
    public long getSwitchIdleTimeout() {
        return switchIdleTimeout;
    }

    /**
     * @return serialization factory
     */
    public SerializationFactory getSerializationFactory() {
        return serializationFactory;
    }

    /**
     * @return deserialization factory
     */
    public DeserializationFactory getDeserializationFactory() {
        return deserializationFactory;
    }

    /**
     * @return TLS configuration
     */
    public TlsConfiguration getTlsConfiguration() {
        return tlsConfiguration;
    }

    /**
     * @param useBarrier
     */
    public void setUseBarrier(final boolean useBarrier) {
        this.useBarrier = useBarrier;
    }

    /**
     * @return useBarrrier
     */
    public boolean useBarrier() {
        return useBarrier;
    }
}