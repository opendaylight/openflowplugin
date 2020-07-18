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
 * Unit tests for OF10QueueGetConfigReplyMessageFactory.
 *
 * @author giuseppex.petralia@intel.com
 */
public class OF10QueueGetConfigReplyMessageFactoryTest {
    private OFSerializer<GetQueueConfigOutput> factory;
    private static final byte MESSAGE_TYPE = 21;

    @Before
    public void startUp() {
        SerializerRegistry registry = new SerializerRegistryImpl();
        registry.init();
        factory = registry
                .getSerializer(new MessageTypeKey<>(EncodeConstants.OF10_VERSION_ID, GetQueueConfigOutput.class));
    }

    @Test
    public void testSerialize() throws Exception {
        GetQueueConfigOutputBuilder builder = new GetQueueConfigOutputBuilder();
        BufferHelper.setupHeader(builder, EncodeConstants.OF10_VERSION_ID);
        builder.setPort(new PortNumber(Uint32.ONE));
        builder.setQueues(createQueues());
        GetQueueConfigOutput message = builder.build();

        ByteBuf serializedBuffer = UnpooledByteBufAllocator.DEFAULT.buffer();
        factory.serialize(message, serializedBuffer);
        BufferHelper.checkHeaderV10(serializedBuffer, MESSAGE_TYPE, 40);
        Assert.assertEquals("Wrong port", message.getPort().getValue().longValue(), serializedBuffer.readShort());
        serializedBuffer.skipBytes(6);
        Assert.assertEquals("Wrong queue Id", message.getQueues().get(0).getQueueId().getValue().longValue(),
                serializedBuffer.readInt());
        Assert.assertEquals("Wrong length", 24, serializedBuffer.readShort());
        serializedBuffer.skipBytes(2);
        List<QueueProperty> properties = message.getQueues().get(0).getQueueProperty();
        Assert.assertEquals("Wrong property", properties.get(0).getProperty().getIntValue(),
                serializedBuffer.readShort());
        Assert.assertEquals("Wrong property length", 16, serializedBuffer.readShort());
        serializedBuffer.skipBytes(4);
        RateQueueProperty rateQueueProperty = properties.get(0).augmentation(RateQueueProperty.class);
        Assert.assertEquals("Wrong rate", rateQueueProperty.getRate().intValue(), serializedBuffer.readShort());
        serializedBuffer.skipBytes(6);
    }

    private static List<Queues> createQueues() {
        List<Queues> list = new ArrayList<>();
        list.add(new QueuesBuilder()
            .setQueueId(new QueueId(Uint32.ONE))
            .setQueueProperty(createPropertiesList())
            .build());
        return list;
    }

    private static List<QueueProperty> createPropertiesList() {
        final List<QueueProperty> propertiesList = new ArrayList<>();
        propertiesList.add(new QueuePropertyBuilder()
            .setProperty(QueueProperties.forValue(1))
            .addAugmentation(new RateQueuePropertyBuilder().setRate(Uint16.valueOf(5)).build())
            .build());
        return propertiesList;
    }
}
