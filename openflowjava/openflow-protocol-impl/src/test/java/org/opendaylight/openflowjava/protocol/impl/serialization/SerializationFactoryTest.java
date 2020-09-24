/*
 * Copyright (c) 2014 Pantheon Technologies s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowjava.protocol.impl.serialization;

import static org.junit.Assert.assertEquals;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.PooledByteBufAllocator;
import org.junit.Test;
import org.opendaylight.openflowjava.protocol.api.extensibility.SerializerRegistry;
import org.opendaylight.openflowjava.protocol.api.util.EncodeConstants;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.HelloInputBuilder;
import org.opendaylight.yangtools.yang.common.Uint32;

/**
 * Unit tests for SerializationFactory.
 *
 * @author michal.polkorab
 */
public class SerializationFactoryTest {

    /**
     * Test serializer lookup & serialization.
     */
    @Test
    public void test() {
        SerializerRegistry registry = new SerializerRegistryImpl();
        registry.init();
        final SerializationFactory factory = new SerializationFactory(registry);
        final ByteBuf buffer = PooledByteBufAllocator.DEFAULT.buffer();
        HelloInputBuilder helloBuilder = new HelloInputBuilder();
        helloBuilder.setVersion(EncodeConstants.OF_VERSION_1_0);
        helloBuilder.setXid(Uint32.valueOf(123456));
        helloBuilder.setElements(null);
        factory.messageToBuffer(EncodeConstants.OF10_VERSION_ID, buffer, helloBuilder.build());
        assertEquals("Serialization failed", EncodeConstants.OFHEADER_SIZE, buffer.readableBytes());
    }

    /**
     * Test serializer not found scenario.
     */
    @Test(expected = IllegalStateException.class)
    public void testNotExistingSerializer() {
        SerializerRegistry registry = new SerializerRegistryImpl();
        registry.init();
        final SerializationFactory factory = new SerializationFactory(registry);
        final ByteBuf buffer = PooledByteBufAllocator.DEFAULT.buffer();
        HelloInputBuilder helloBuilder = new HelloInputBuilder();
        helloBuilder.setVersion(EncodeConstants.OF_VERSION_1_0);
        helloBuilder.setXid(Uint32.valueOf(123456));
        helloBuilder.setElements(null);
        factory.messageToBuffer((short) 0, buffer, helloBuilder.build());
    }
}
