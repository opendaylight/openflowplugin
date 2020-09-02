/*
 * Copyright (c) 2013 Pantheon Technologies s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowjava.protocol.impl.deserialization.factories;

import static java.util.Objects.requireNonNull;
import static org.opendaylight.yangtools.yang.common.netty.ByteBufUtils.readUint16;
import static org.opendaylight.yangtools.yang.common.netty.ByteBufUtils.readUint32;

import io.netty.buffer.ByteBuf;
import java.util.ArrayList;
import java.util.List;
import org.opendaylight.openflowjava.protocol.api.extensibility.DeserializerRegistry;
import org.opendaylight.openflowjava.protocol.api.extensibility.OFDeserializer;
import org.opendaylight.openflowjava.protocol.api.util.EncodeConstants;
import org.opendaylight.openflowjava.util.ExperimenterDeserializerKeyFactory;
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

/**
 * Translates QueueGetConfigReply messages.
 *
 * @author timotej.kubas
 * @author michal.polkorab
 */
public class QueueGetConfigReplyMessageFactory implements OFDeserializer<GetQueueConfigOutput> {
    private static final byte PADDING_IN_QUEUE_GET_CONFIG_REPLY_HEADER = 4;
    private static final byte PADDING_IN_PACKET_QUEUE_HEADER = 6;
    private static final byte PADDING_IN_QUEUE_PROPERTY_HEADER = 4;
    private static final int PADDING_IN_RATE_QUEUE_PROPERTY = 6;
    private static final byte PACKET_QUEUE_LENGTH = 16;

    private final DeserializerRegistry registry;

    public QueueGetConfigReplyMessageFactory(final DeserializerRegistry registry) {
        this.registry = requireNonNull(registry);
    }

    @Override
    public GetQueueConfigOutput deserialize(final ByteBuf rawMessage) {
        GetQueueConfigOutputBuilder builder = new GetQueueConfigOutputBuilder()
                .setVersion(EncodeConstants.OF_VERSION_1_3)
                .setXid(readUint32(rawMessage))
                .setPort(new PortNumber(readUint32(rawMessage)));
        rawMessage.skipBytes(PADDING_IN_QUEUE_GET_CONFIG_REPLY_HEADER);
        builder.setQueues(createQueuesList(rawMessage));
        return builder.build();
    }

    private List<Queues> createQueuesList(final ByteBuf input) {
        List<Queues> queuesList = new ArrayList<>();
        while (input.readableBytes() > 0) {
            QueuesBuilder queueBuilder = new QueuesBuilder()
                    .setQueueId(new QueueId(readUint32(input)))
                    .setPort(new PortNumber(readUint32(input)));
            int length = input.readUnsignedShort();
            input.skipBytes(PADDING_IN_PACKET_QUEUE_HEADER);
            queueBuilder.setQueueProperty(createPropertiesList(input, length - PACKET_QUEUE_LENGTH));
            queuesList.add(queueBuilder.build());
        }
        return queuesList;
    }

    private List<QueueProperty> createPropertiesList(final ByteBuf input, final int length) {
        int propertiesLength = length;
        List<QueueProperty> propertiesList = new ArrayList<>();
        while (propertiesLength > 0) {
            int propertyStartIndex = input.readerIndex();
            QueuePropertyBuilder propertiesBuilder = new QueuePropertyBuilder();
            QueueProperties property = QueueProperties.forValue(input.readUnsignedShort());
            propertiesBuilder.setProperty(property);
            int currentPropertyLength = input.readUnsignedShort();
            propertiesLength -= currentPropertyLength;
            input.skipBytes(PADDING_IN_QUEUE_PROPERTY_HEADER);
            if (property.equals(QueueProperties.OFPQTMINRATE) || property.equals(QueueProperties.OFPQTMAXRATE)) {
                propertiesBuilder.addAugmentation(new RateQueuePropertyBuilder()
                    .setRate(readUint16(input))
                    .build());
                input.skipBytes(PADDING_IN_RATE_QUEUE_PROPERTY);
            } else if (property.equals(QueueProperties.OFPQTEXPERIMENTER)) {
                long expId = input.readUnsignedInt();
                input.readerIndex(propertyStartIndex);
                OFDeserializer<QueueProperty> deserializer = registry.getDeserializer(
                        ExperimenterDeserializerKeyFactory.createQueuePropertyDeserializerKey(
                                EncodeConstants.OF13_VERSION_ID, expId));
                propertiesList.add(deserializer.deserialize(input));
                continue;
            }
            propertiesList.add(propertiesBuilder.build());
        }
        return propertiesList;
    }
}
