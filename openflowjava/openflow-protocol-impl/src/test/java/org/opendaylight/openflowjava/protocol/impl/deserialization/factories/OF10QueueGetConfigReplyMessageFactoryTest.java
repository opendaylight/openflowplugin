/*
 * Copyright (c) 2013 Pantheon Technologies s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowjava.protocol.impl.deserialization.factories;

import io.netty.buffer.ByteBuf;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.opendaylight.openflowjava.protocol.api.extensibility.DeserializerRegistry;
import org.opendaylight.openflowjava.protocol.api.extensibility.OFDeserializer;
import org.opendaylight.openflowjava.protocol.api.keys.MessageCodeKey;
import org.opendaylight.openflowjava.protocol.api.util.EncodeConstants;
import org.opendaylight.openflowjava.protocol.impl.deserialization.DeserializerRegistryImpl;
import org.opendaylight.openflowjava.protocol.impl.util.BufferHelper;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.augments.rev150225.RateQueueProperty;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.QueueProperties;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.GetQueueConfigOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.queue.get.config.reply.Queues;

/**
 * @author michal.polkorab
 *
 */
public class OF10QueueGetConfigReplyMessageFactoryTest {

    private OFDeserializer<GetQueueConfigOutput> queueFactory;

    /**
     * Initializes deserializer registry and lookups correct deserializer
     */
    @Before
    public void startUp() {
        DeserializerRegistry registry = new DeserializerRegistryImpl();
        registry.init();
        queueFactory = registry.getDeserializer(
                new MessageCodeKey(EncodeConstants.OF10_VERSION_ID, 21, GetQueueConfigOutput.class));
    }

    /**
     * Testing of {@link OF10QueueGetConfigReplyMessageFactory} for correct
     * translation into POJO
     */
    @Test
    public void test() {
        ByteBuf bb = BufferHelper.buildBuffer("00 01 00 00 00 00 00 00 "
                + "00 00 00 08 00 10 00 00 00 00 00 08 00 00 00 00 "
                + "00 00 00 02 00 28 00 00 00 01 00 10 00 00 00 00 00 20 00 00 00 00 00 00 "
                + "00 01 00 10 00 00 00 00 00 30 00 00 00 00 00 00");
        GetQueueConfigOutput builtByFactory = BufferHelper.deserialize(
                queueFactory, bb);

        BufferHelper.checkHeaderV10(builtByFactory);
        Assert.assertEquals("Wrong port", 1, builtByFactory.getPort().getValue().intValue());
        Assert.assertEquals("Wrong queues size", 2, builtByFactory.getQueues().size());
        Queues queue1 = builtByFactory.getQueues().get(0);
        Queues queue2 = builtByFactory.getQueues().get(1);
        Assert.assertEquals("Wrong queueId", 8, queue1.getQueueId().getValue().intValue());
        Assert.assertEquals("Wrong queue - # properties", 1, queue1.getQueueProperty().size());
        Assert.assertEquals("Wrong queue - wrong property", QueueProperties.OFPQTNONE,
                queue1.getQueueProperty().get(0).getProperty());
        Assert.assertEquals("Wrong queueId", 2, queue2.getQueueId().getValue().intValue());
        Assert.assertEquals("Wrong queue - # properties", 2, queue2.getQueueProperty().size());
        Assert.assertEquals("Wrong queue - wrong property", QueueProperties.OFPQTMINRATE,
                queue2.getQueueProperty().get(0).getProperty());
        Assert.assertEquals("Wrong queue - wrong property", QueueProperties.OFPQTMINRATE,
                queue2.getQueueProperty().get(1).getProperty());
        RateQueueProperty rate1 = queue2.getQueueProperty().get(0).getAugmentation(RateQueueProperty.class);
        RateQueueProperty rate2 = queue2.getQueueProperty().get(1).getAugmentation(RateQueueProperty.class);
        Assert.assertEquals("Wrong queue - wrong property rate", 32, rate1.getRate().intValue());
        Assert.assertEquals("Wrong queue - wrong property rate", 48, rate2.getRate().intValue());
    }
}
