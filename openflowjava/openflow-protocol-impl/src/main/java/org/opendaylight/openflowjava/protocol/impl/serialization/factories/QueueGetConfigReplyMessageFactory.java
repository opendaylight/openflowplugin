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
import org.opendaylight.openflowjava.protocol.api.extensibility.OFSerializer;
import org.opendaylight.openflowjava.protocol.api.util.EncodeConstants;
import org.opendaylight.openflowjava.util.ByteBufUtils;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.augments.rev150225.ExperimenterIdQueueProperty;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.augments.rev150225.RateQueueProperty;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.GetQueueConfigOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.queue.get.config.reply.Queues;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.queue.property.header.QueueProperty;

/**
 * @author giuseppex.petralia@intel.com
 *
 */
public class QueueGetConfigReplyMessageFactory implements OFSerializer<GetQueueConfigOutput> {

    private static final byte MESSAGE_TYPE = 23;
    private static final byte PADDING = 4;
    public static final int QUEUE_LENGTH_INDEX = 8;
    public static final int PROPERTY_LENGTH_INDEX = 2;
    private static final byte QUEUE_PADDING = 6;
    private static final byte PROPERTY_HEADER_PADDING = 4;
    private static final byte PROPERTY_RATE_PADDING = 6;
    private static final byte PROPERTY_EXPERIMENTER_PADDING = 4;

    @Override
    public void serialize(GetQueueConfigOutput message, ByteBuf outBuffer) {
        ByteBufUtils.writeOFHeader(MESSAGE_TYPE, message, outBuffer, EncodeConstants.EMPTY_LENGTH);
        outBuffer.writeInt(message.getPort().getValue().intValue());
        outBuffer.writeZero(PADDING);
        for (Queues queue : message.getQueues()) {
            ByteBuf queueBuff = UnpooledByteBufAllocator.DEFAULT.buffer();
            queueBuff.writeInt(queue.getQueueId().getValue().intValue());
            queueBuff.writeInt(queue.getPort().getValue().intValue());
            queueBuff.writeShort(EncodeConstants.EMPTY_LENGTH);
            queueBuff.writeZero(QUEUE_PADDING);

            for (QueueProperty property : queue.getQueueProperty()) {
                ByteBuf propertyBuff = UnpooledByteBufAllocator.DEFAULT.buffer();
                propertyBuff.writeShort(property.getProperty().getIntValue());
                propertyBuff.writeShort(EncodeConstants.EMPTY_LENGTH);
                propertyBuff.writeZero(PROPERTY_HEADER_PADDING);
                switch (property.getProperty()) {
                case OFPQTMINRATE:
                    serializeRateBody(property.getAugmentation(RateQueueProperty.class), propertyBuff);
                    break;
                case OFPQTMAXRATE:
                    serializeRateBody(property.getAugmentation(RateQueueProperty.class), propertyBuff);
                    break;
                case OFPQTEXPERIMENTER:
                    serializeExperimenterBody(property.getAugmentation(ExperimenterIdQueueProperty.class),
                            propertyBuff);
                    break;
                default:
                    break;
                }
                propertyBuff.setShort(PROPERTY_LENGTH_INDEX, propertyBuff.readableBytes());
                queueBuff.writeBytes(propertyBuff);
            }

            queueBuff.setShort(QUEUE_LENGTH_INDEX, queueBuff.readableBytes());
            outBuffer.writeBytes(queueBuff);
        }
        ByteBufUtils.updateOFHeaderLength(outBuffer);
    }

    private void serializeRateBody(RateQueueProperty body, ByteBuf outBuffer) {
        outBuffer.writeShort(body.getRate());
        outBuffer.writeZero(PROPERTY_RATE_PADDING);
    }

    private void serializeExperimenterBody(ExperimenterIdQueueProperty body, ByteBuf outBuffer) {
        // TODO: Experimenter Data is vendor specific that should implement its
        // own serializer
        outBuffer.writeInt(body.getExperimenter().getValue().intValue());
        outBuffer.writeZero(PROPERTY_EXPERIMENTER_PADDING);
    }
}
