/*
 * Copyright (c) 2013 Pantheon Technologies s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowjava.protocol.impl.serialization.factories;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.UnpooledByteBufAllocator;

import org.junit.Before;
import org.junit.Test;
import org.opendaylight.openflowjava.protocol.api.extensibility.OFSerializer;
import org.opendaylight.openflowjava.protocol.api.extensibility.SerializerRegistry;
import org.opendaylight.openflowjava.protocol.api.keys.MessageTypeKey;
import org.opendaylight.openflowjava.protocol.impl.serialization.SerializerRegistryImpl;
import org.opendaylight.openflowjava.protocol.impl.util.BufferHelper;
import org.opendaylight.openflowjava.protocol.api.util.EncodeConstants;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.HelloInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.HelloInputBuilder;

/**
 * @author michal.polkorab
 *
 */
public class OF10HelloInputMessageFactoryTest {

    private SerializerRegistry registry;
    private OFSerializer<HelloInput> helloFactory;

    /**
     * Initializes serializer registry and stores correct factory in field
     */
    @Before
    public void startUp() {
        registry = new SerializerRegistryImpl();
        registry.init();
        helloFactory = registry.getSerializer(
                new MessageTypeKey<>(EncodeConstants.OF10_VERSION_ID, HelloInput.class));
    }

    /**
     * Testing of {@link OF10HelloInputMessageFactory} for correct translation from POJO
     * @throws Exception
     */
    @Test
    public void testWithoutElementsSet() throws Exception {
        HelloInputBuilder hib = new HelloInputBuilder();
        BufferHelper.setupHeader(hib, EncodeConstants.OF10_VERSION_ID);
        HelloInput hi = hib.build();

        ByteBuf out = UnpooledByteBufAllocator.DEFAULT.buffer();
        helloFactory.serialize(hi, out);

        BufferHelper.checkHeaderV10(out, (byte) 0, 8);
    }

}
