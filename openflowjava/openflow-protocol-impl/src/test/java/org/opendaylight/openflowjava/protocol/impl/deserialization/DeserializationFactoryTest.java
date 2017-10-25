/*
 * Copyright (c) 2014 Pantheon Technologies s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowjava.protocol.impl.deserialization;

import static org.junit.Assert.assertEquals;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.PooledByteBufAllocator;

import org.junit.Test;
import org.opendaylight.openflowjava.protocol.api.util.EncodeConstants;

/**
 * @author michal.polkorab
 *
 */
public class DeserializationFactoryTest {

    /**
     * Test deserializer lookup & deserialization
     */
    @Test
    public void test() {
        DeserializerRegistryImpl registry = new DeserializerRegistryImpl();
        registry.init();
        DeserializationFactory factory = new DeserializationFactory();
        factory.setRegistry(registry);
        ByteBuf buffer = PooledByteBufAllocator.DEFAULT.buffer();
        buffer.writeByte(0);
        buffer.writeShort(EncodeConstants.OFHEADER_SIZE);
        buffer.writeInt(1234);
        factory.deserialize(buffer, EncodeConstants.OF13_VERSION_ID);
        assertEquals("Deserialization failed", 0, buffer.readableBytes());
    }

    /**
     * Test deserializer lookup & deserialization
     */
    @Test(expected=NullPointerException.class)
    public void testNotExistingDeserializer() {
        DeserializerRegistryImpl registry = new DeserializerRegistryImpl();
        registry.init();
        DeserializationFactory factory = new DeserializationFactory();
        factory.setRegistry(registry);
        ByteBuf buffer = PooledByteBufAllocator.DEFAULT.buffer();
        buffer.writeByte(0);
        buffer.writeShort(EncodeConstants.OFHEADER_SIZE);
        buffer.writeInt(1234);
        factory.deserialize(buffer, (short) 0);
    }
}