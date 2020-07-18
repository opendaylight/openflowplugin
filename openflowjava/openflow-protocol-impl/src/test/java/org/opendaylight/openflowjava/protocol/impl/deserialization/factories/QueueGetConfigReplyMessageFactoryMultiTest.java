/*
 * Copyright (c) 2013 Pantheon Technologies s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowjava.protocol.impl.deserialization.factories;

import io.netty.buffer.ByteBuf;
import java.util.ArrayList;
import java.util.List;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.opendaylight.openflowjava.protocol.api.extensibility.DeserializerRegistry;
import org.opendaylight.openflowjava.protocol.api.extensibility.OFDeserializer;
import org.opendaylight.openflowjava.protocol.api.keys.MessageCodeKey;
import org.opendaylight.openflowjava.protocol.api.util.EncodeConstants;
import org.opendaylight.openflowjava.protocol.impl.deserialization.DeserializerRegistryImpl;
import org.opendaylight.openflowjava.protocol.impl.util.BufferHelper;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.augments.rev150225.RateQueuePropertyBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.PortNumber;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.QueueId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.QueueProperties;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.GetQueueConfigOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.queue.get.config.reply.Queues;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.queue.get.config.reply.QueuesBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.queue.property.header.QueueProperty;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.queue.property.header.QueuePropertyBuilder;
import org.opendaylight.yangtools.yang.common.Uint16;
import org.opendaylight.yangtools.yang.common.Uint32;

/**
 * Unit tests for QueueGetConfigReplyMessageFactoryMulti.
 *
 * @author timotej.kubas
 * @author michal.polkorab
 */
public class QueueGetConfigReplyMessageFactoryMultiTest {

    private OFDeserializer<GetQueueConfigOutput> queueFactory;

    /**
     * Initializes deserializer registry and lookups correct deserializer.
     */
    @Before
    public void startUp() {
        DeserializerRegistry registry = new DeserializerRegistryImpl();
        registry.init();
        queueFactory = registry.getDeserializer(
                new MessageCodeKey(EncodeConstants.OF13_VERSION_ID, 23, GetQueueConfigOutput.class));
    }

    /**
     * Testing of {@link QueueGetConfigReplyMessageFactory} for correct
     * translation into POJO.
     */
    @Test
    public void test() {
        ByteBuf bb = BufferHelper.buildBuffer("00 01 02 03 " + // port
                "00 00 00 00 " + // padding
                "00 00 00 01 " + // queueId
                "00 00 00 01 " + // port
                "00 20 " + // length
                "00 00 00 00 00 00 " + // pad
                "00 02 " + // property
                "00 10 " + // length
                "00 00 00 00 " + // pad
                "00 05 " + // rate
                "00 00 00 00 00 00 " + // pad
                "00 00 00 02 " + // queueId
                "00 00 00 02 " + // port
                "00 20 " + // length
                "00 00 00 00 00 00 " + // pad
                "00 02 " + // property
                "00 10 " + // length
                "00 00 00 00 " + // pad
                "00 05 " + // rate
                "00 00 00 00 00 00" // pad
        );

        GetQueueConfigOutput builtByFactory = BufferHelper.deserialize(
                queueFactory, bb);

        BufferHelper.checkHeaderV13(builtByFactory);
        Assert.assertEquals("Wrong port", 66051L, builtByFactory.getPort().getValue().longValue());
        Assert.assertEquals("Wrong queues", createQueuesList(), builtByFactory.getQueues());
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
