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
 * Unit tests for GetQueueConfigInputMessageFactory.
 *
 * @author timotej.kubas
 * @author michal.polkorab
 */
public class GetQueueConfigInputMessageFactoryTest {
    private static final byte GET_QUEUE_CONFIG_INPUT_MESSAGE_CODE_TYPE = 22;
    private static final byte PADDING_IN_QUEUE_CONFIG_INPUT_MESSAGE = 4;
    private SerializerRegistry registry;
    private OFSerializer<GetQueueConfigInput> getQueueFactory;

    /**
     * Initializes serializer registry and stores correct factory in field.
     */
    @Before
    public void startUp() {
        registry = new SerializerRegistryImpl();
        registry.init();
        getQueueFactory = registry.getSerializer(
                new MessageTypeKey<>(EncodeConstants.OF13_VERSION_ID, GetQueueConfigInput.class));
    }

    /**
     * Testing of {@link GetQueueConfigInputMessageFactory} for correct translation from POJO.
     */
    @Test
    public void testGetQueueConfigInputMessage() throws Exception {
        GetQueueConfigInputBuilder builder = new GetQueueConfigInputBuilder();
        BufferHelper.setupHeader(builder, EncodeConstants.OF13_VERSION_ID);
        builder.setPort(new PortNumber(Uint32.valueOf(0x00010203)));
        GetQueueConfigInput message = builder.build();

        ByteBuf out = UnpooledByteBufAllocator.DEFAULT.buffer();
        getQueueFactory.serialize(message, out);

        BufferHelper.checkHeaderV13(out, GET_QUEUE_CONFIG_INPUT_MESSAGE_CODE_TYPE, 16);
        Assert.assertEquals("Wrong port", 0x00010203, out.readUnsignedInt());
        out.skipBytes(PADDING_IN_QUEUE_CONFIG_INPUT_MESSAGE);
    }
}
