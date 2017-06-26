/*
 * Copyright (c) 2016 Pantheon Technologies s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowjava.protocol.impl.util;

import io.netty.buffer.ByteBuf;
import java.util.List;
import org.opendaylight.openflowjava.protocol.api.extensibility.DeserializerRegistry;
import org.opendaylight.openflowjava.protocol.api.extensibility.OFDeserializer;
import org.opendaylight.openflowjava.protocol.api.keys.MessageCodeKey;
import org.opendaylight.openflowjava.protocol.impl.deserialization.DeserializerRegistryImpl;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.OfHeader;
import org.opendaylight.yangtools.yang.binding.DataContainer;

/**
 * Super class for common stuff of deserialization factories tests.
 */
public abstract class DefaultDeserializerFactoryTest<T extends DataContainer> {

    private DeserializerRegistry registry;
    protected OFDeserializer<T> factory;
    protected MessageCodeKey messageCodeKey;

    public DefaultDeserializerFactoryTest(final MessageCodeKey key) {
        this.registry = new DeserializerRegistryImpl();
        this.registry.init();
        this.messageCodeKey = key;
        this.factory = registry.getDeserializer(key);
    }

    /**
     * Test correct version after deserialization for all supported OF versions.
     * @param versions supported OF versions
     * @param buffer byte buffer to deserialze
     */
    protected void testHeaderVersions(final List<Byte> versions, final ByteBuf buffer) {
        for (short version : versions) {
            ByteBuf bb = buffer.copy();
            OFDeserializer<T> factory = registry.getDeserializer(
                    new MessageCodeKey(version, messageCodeKey.getMsgType(), messageCodeKey.getClazz()));
            T builtByFactory = BufferHelper.deserialize(factory, bb);
            BufferHelper.checkHeader((OfHeader) builtByFactory, version);
        }
    }
}
