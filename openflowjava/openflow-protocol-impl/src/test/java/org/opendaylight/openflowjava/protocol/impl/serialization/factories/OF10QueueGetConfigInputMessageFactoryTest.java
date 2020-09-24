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
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.opendaylight.openflowjava.protocol.api.extensibility.OFSerializer;
import org.opendaylight.openflowjava.protocol.api.extensibility.SerializerRegistry;
import org.opendaylight.openflowjava.protocol.api.keys.MessageTypeKey;
import org.opendaylight.openflowjava.protocol.api.util.EncodeConstants;
import org.opendaylight.openflowjava.protocol.impl.serialization.SerializerRegistryImpl;
import org.opendaylight.openflowjava.protocol.impl.util.BufferHelper;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.PortNumber;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.GetQueueConfigInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.GetQueueConfigInputBuilder;
import org.opendaylight.yangtools.yang.common.Uint32;

/**
 * Unit tests for OF10QueueGetConfigInputMessageFactory.
 *
 * @author michal.polkorab
 */
public class OF10QueueGetConfigInputMessageFactoryTest {

    private SerializerRegistry registry;
    private OFSerializer<GetQueueConfigInput> queueFactory;

    /**
     * Initializes serializer registry and stores correct factory in field.
     */
    @Before
    public void startUp() {
        registry = new SerializerRegistryImpl();
        registry.init();
        queueFactory = registry.getSerializer(
                new MessageTypeKey<>(EncodeConstants.OF10_VERSION_ID, GetQueueConfigInput.class));
    }

    /**
     * Testing of {@link OF10QueueGetConfigInputMessageFactory} for correct translation from POJO.
     */
    @Test
    public void test() throws Exception {
        GetQueueConfigInputBuilder builder = new GetQueueConfigInputBuilder();
        BufferHelper.setupHeader(builder, EncodeConstants.OF10_VERSION_ID);
        builder.setPort(new PortNumber(Uint32.valueOf(6653)));
        GetQueueConfigInput message = builder.build();

        ByteBuf out = UnpooledByteBufAllocator.DEFAULT.buffer();
        queueFactory.serialize(message, out);

        BufferHelper.checkHeaderV10(out, (byte) 20, 12);
        Assert.assertEquals("Wrong port", 6653L, out.readUnsignedShort());
        out.skipBytes(2);
    }

}
