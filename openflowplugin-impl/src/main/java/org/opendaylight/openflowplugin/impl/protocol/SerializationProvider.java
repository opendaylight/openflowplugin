/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.protocol;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import java.lang.reflect.Field;
import java.util.List;
import org.opendaylight.openflowjava.protocol.impl.serialization.SerializationFactory;
import org.opendaylight.openflowjava.protocol.spi.connection.SwitchConnectionProvider;
import org.opendaylight.openflowplugin.impl.OpenFlowPluginProviderImpl;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SerializationProvider {
    private static final Logger LOG = LoggerFactory.getLogger(OpenFlowPluginProviderImpl.class);
    private final boolean isAvailable;
    private SerializationFactory serializationFactory;

    public SerializationProvider(final boolean enabled,
                                 final boolean useSingleLayer,
                                 final List<SwitchConnectionProvider> switchConnectionProviders) {
        boolean available = enabled && useSingleLayer;

        if (!available) {
            isAvailable = false;
            return;
        }

        if (!switchConnectionProviders.isEmpty()) {
            try {
                // TODO: Replace reflection with actual calls, like adding .serialize method to switch provider
                final SwitchConnectionProvider switchConnectionProvider = switchConnectionProviders.get(0);
                final Field serializationFactory = switchConnectionProvider
                        .getClass()
                        .getDeclaredField("serializationFactory");

                serializationFactory.setAccessible(true);
                this.serializationFactory = (SerializationFactory) serializationFactory.get(switchConnectionProvider);
            } catch (NoSuchFieldException | IllegalAccessException ignored) {
                available = false;
            }
        } else {
            available = false;
        }

        if (!available) {
            LOG.warn("Serialization factory not found, disabling pre-serialization.");
        }

        isAvailable = available;
    }

    public boolean isAvailable() {
        return isAvailable;
    }

    public byte[] serialize(final short version, final DataObject message) {
        if (!isAvailable()) {
            return new byte[0];
        }

        final ByteBuf byteBuf = ByteBufAllocator.DEFAULT.buffer();
        serializationFactory.messageToBuffer(version, byteBuf, message);
        final byte[] bytes = new byte[byteBuf.readableBytes()];
        byteBuf.getBytes(byteBuf.readerIndex(), bytes);
        byteBuf.release();
        return bytes;
    }
}
