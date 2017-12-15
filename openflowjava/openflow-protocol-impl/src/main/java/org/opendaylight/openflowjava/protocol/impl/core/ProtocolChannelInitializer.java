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
 * Initializer for protocol channels.
 *
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
    private int channelOutboundQueueSize;

    /**
     * Sets the SwitchConnectionHandler.
     *
     * @param switchConnectionHandler the switchConnectionHandler to set
     */
    public void setSwitchConnectionHandler(final SwitchConnectionHandler switchConnectionHandler) {
        this.switchConnectionHandler = switchConnectionHandler;
    }

    /**
     * Sets the switch idle timeout.
     *
     * @param switchIdleTimeout the switchIdleTimeout to set
     */
    public void setSwitchIdleTimeout(final long switchIdleTimeout) {
        this.switchIdleTimeout = switchIdleTimeout;
    }

    public void setSerializationFactory(final SerializationFactory serializationFactory) {
        this.serializationFactory = serializationFactory;
    }

    public void setDeserializationFactory(final DeserializationFactory deserializationFactory) {
        this.deserializationFactory = deserializationFactory;
    }

    public void setTlsConfiguration(final TlsConfiguration tlsConfiguration) {
        this.tlsConfiguration = tlsConfiguration;
    }

    public SwitchConnectionHandler getSwitchConnectionHandler() {
        return switchConnectionHandler;
    }

    public long getSwitchIdleTimeout() {
        return switchIdleTimeout;
    }

    public SerializationFactory getSerializationFactory() {
        return serializationFactory;
    }

    public DeserializationFactory getDeserializationFactory() {
        return deserializationFactory;
    }

    public TlsConfiguration getTlsConfiguration() {
        return tlsConfiguration;
    }

    public void setUseBarrier(final boolean useBarrier) {
        this.useBarrier = useBarrier;
    }

    public boolean useBarrier() {
        return useBarrier;
    }

    /**
     * Sets the channelOutboundQueueSize.
     *
     * @param channelOutboundQueueSize the channelOutboundQueueSize
     */
    public void setChannelOutboundQueueSize(final int channelOutboundQueueSize) {
        this.channelOutboundQueueSize = channelOutboundQueueSize;
    }

    /**
     * Returns the channelOutboundQueueSize.
     *
     * @return channelOutboundQueueSize
     */
    public int getChannelOutboundQueueSize()  {
        return channelOutboundQueueSize;
    }
}
