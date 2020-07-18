/*
 * Copyright (c) 2015 NetIDE Consortium and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowjava.protocol.impl.serialization.factories;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.UnpooledByteBufAllocator;
import java.util.ArrayList;
import java.util.List;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.opendaylight.openflowjava.protocol.api.extensibility.OFSerializer;
import org.opendaylight.openflowjava.protocol.api.extensibility.SerializerRegistry;
import org.opendaylight.openflowjava.protocol.api.keys.MessageTypeKey;
import org.opendaylight.openflowjava.protocol.api.util.EncodeConstants;
import org.opendaylight.openflowjava.protocol.impl.serialization.SerializerRegistryImpl;
import org.opendaylight.openflowjava.protocol.impl.util.BufferHelper;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.augments.rev150225.RateQueueProperty;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.augments.rev150225.RateQueuePropertyBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.PortNumber;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.QueueId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.QueueProperties;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.GetQueueConfigOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.GetQueueConfigOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.queue.get.config.reply.Queues;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.queue.get.config.reply.QueuesBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.queue.property.header.QueueProperty;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.queue.property.header.QueuePropertyBuilder;
import org.opendaylight.yangtools.yang.common.Uint16;
import org.opendaylight.yangtools.yang.common.Uint32;

/**
 * Unit tests for QueueGetConfigReplyMessageFactory.
 *
 * @author giuseppex.petralia@intel.com
 */
public class QueueGetConfigReplyMessageFactoryTest {
    private OFSerializer<GetQueueConfigOutput> factory;
    private static final byte MESSAGE_TYPE = 23;
    private static final byte PADDING = 4;
    private static final byte QUEUE_PADDING = 6;
    private static final byte PROPERTY_HEADER_PADDING = 4;
    private static final byte PROPERTY_RATE_PADDING = 6;

    @Before
    public void startUp() {
        SerializerRegistry registry = new SerializerRegistryImpl();
        registry.init();
        factory = registry
                .getSerializer(new MessageTypeKey<>(EncodeConstants.OF13_VERSION_ID, GetQueueConfigOutput.class));
    }

    @Test
    public void testSerialize() throws Exception {
        GetQueueConfigOutputBuilder builder = new GetQueueConfigOutputBuilder();
        BufferHelper.setupHeader(builder, EncodeConstants.OF13_VERSION_ID);
        builder.setPort(new PortNumber(0x00010203L));
        builder.setQueues(createQueuesList());
        GetQueueConfigOutput message = builder.build();

        ByteBuf serializedBuffer = UnpooledByteBufAllocator.DEFAULT.buffer();
        factory.serialize(message, serializedBuffer);
        BufferHelper.checkHeaderV13(serializedBuffer, MESSAGE_TYPE, 80);
        Assert.assertEquals("Wrong port", message.getPort().getValue().longValue(), serializedBuffer.readInt());
        serializedBuffer.skipBytes(PADDING);

        Assert.assertEquals("Wrong queue Id", message.getQueues().get(0).getQueueId().getValue().longValue(),
                serializedBuffer.readInt());
        Assert.assertEquals("Wrong port", message.getQueues().get(0).getPort().getValue().longValue(),
                serializedBuffer.readInt());
        Assert.assertEquals("Wrong length", 32, serializedBuffer.readShort());
        serializedBuffer.skipBytes(QUEUE_PADDING);
        List<QueueProperty> properties = message.getQueues().get(0).getQueueProperty();
        Assert.assertEquals("Wrong property", properties.get(0).getProperty().getIntValue(),
                serializedBuffer.readShort());
        Assert.assertEquals("Wrong property length", 16, serializedBuffer.readShort());
        serializedBuffer.skipBytes(PROPERTY_HEADER_PADDING);
        RateQueueProperty rateQueueProperty = properties.get(0).augmentation(RateQueueProperty.class);
        Assert.assertEquals("Wrong rate", rateQueueProperty.getRate().intValue(), serializedBuffer.readShort());
        serializedBuffer.skipBytes(PROPERTY_RATE_PADDING);

        Assert.assertEquals("Wrong queue Id", message.getQueues().get(1).getQueueId().getValue().longValue(),
                serializedBuffer.readInt());
        Assert.assertEquals("Wrong queue Id", message.getQueues().get(1).getPort().getValue().longValue(),
                serializedBuffer.readInt());
        Assert.assertEquals("Wrong length", 32, serializedBuffer.readShort());
        serializedBuffer.skipBytes(QUEUE_PADDING);
        List<QueueProperty> propertiesTwo = message.getQueues().get(1).getQueueProperty();
        Assert.assertEquals("Wrong property", propertiesTwo.get(0).getProperty().getIntValue(),
                serializedBuffer.readShort());
        Assert.assertEquals("Wrong property length", 16, serializedBuffer.readShort());
        serializedBuffer.skipBytes(PROPERTY_HEADER_PADDING);
        RateQueueProperty rateQueuePropertyTwo = propertiesTwo.get(0).augmentation(RateQueueProperty.class);
        Assert.assertEquals("Wrong rate", rateQueuePropertyTwo.getRate().intValue(), serializedBuffer.readShort());
        serializedBuffer.skipBytes(PROPERTY_RATE_PADDING);

    }

    private static List<Queues> createQueuesList() {
        List<Queues> queuesList = new ArrayList<>();
        for (int i = 1; i < 3; i++) {
            queuesList.add(new QueuesBuilder()
                .setQueueId(new QueueId(Uint32.valueOf(i)))
                .setPort(new PortNumber(Uint32.valueOf(i)))
                .setQueueProperty(createPropertiesList())
                .build());
        }
        return queuesList;
    }

    private static List<QueueProperty> createPropertiesList() {
        final List<QueueProperty> propertiesList = new ArrayList<>();
        propertiesList.add(new QueuePropertyBuilder()
            .setProperty(QueueProperties.forValue(2))
            .addAugmentation(new RateQueuePropertyBuilder().setRate(Uint16.valueOf(5)).build())
            .build());
        return propertiesList;
    }
}
