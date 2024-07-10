/*
 * Copyright (c) 2013 Pantheon Technologies s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowjava.protocol.impl.serialization;

import io.netty.buffer.ByteBuf;
import org.opendaylight.openflowjava.protocol.api.extensibility.OFSerializer;
import org.opendaylight.openflowjava.protocol.api.extensibility.SerializerRegistry;
import org.opendaylight.openflowjava.protocol.api.keys.MessageTypeKey;
import org.opendaylight.yangtools.binding.DataContainer;
import org.opendaylight.yangtools.yang.common.Uint8;

/**
 * Serializes messages.
 *
 * @author michal.polkorab
 * @author timotej.kubas
 */
public class SerializationFactory {
    private final SerializerRegistry registry;

    public SerializationFactory(final SerializerRegistry registry) {
        this.registry = registry;
    }

    /**
     * Transforms POJO message into ByteBuf.
     *
     * @param version version used for encoding received message
     * @param out ByteBuf for storing and sending transformed message
     * @param message POJO message
     */
    public void messageToBuffer(final Uint8 version, final ByteBuf out, final DataContainer message) {
        OFSerializer<DataContainer> serializer = registry.getSerializer(
                new MessageTypeKey<>(version, message.implementedInterface()));
        serializer.serialize(message, out);
    }
}
