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
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.augments.rev150225.RateQueueProperty;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.QueueProperties;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.GetQueueConfigOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.queue.get.config.reply.Queues;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.queue.property.header.QueueProperty;

/**
 * @author giuseppex.petralia@intel.com
 *
 */
public class OF10QueueGetConfigReplyMessageFactory implements OFSerializer<GetQueueConfigOutput> {

    private static final byte MESSAGE_TYPE = 21;
    private static final byte PADDING = 6;
    private static final int QUEUE_LENGTH_INDEX = 4;
    private static final byte QUEUE_PADDING = 2;
    private static final byte QUEUE_PROPERTY_PADDING = 6;
    private static final int QUEUE_PROPERTY_LENGTH_INDEX = 2;

    @Override
    public void serialize(GetQueueConfigOutput message, ByteBuf outBuffer) {
        ByteBufUtils.writeOFHeader(MESSAGE_TYPE, message, outBuffer, EncodeConstants.EMPTY_LENGTH);
        outBuffer.writeShort(message.getPort().getValue().intValue());
        outBuffer.writeZero(PADDING);
        for (Queues queue : message.getQueues()) {
            ByteBuf queueBuff = UnpooledByteBufAllocator.DEFAULT.buffer();
            queueBuff.writeInt(queue.getQueueId().getValue().intValue());
            queueBuff.writeShort(EncodeConstants.EMPTY_LENGTH);
            queueBuff.writeZero(QUEUE_PADDING);
            for (QueueProperty queueProperty : queue.getQueueProperty()) {
                ByteBuf queuePropertyBuff = UnpooledByteBufAllocator.DEFAULT.buffer();
                queuePropertyBuff.writeShort(queueProperty.getProperty().getIntValue());
                queuePropertyBuff.writeShort(EncodeConstants.EMPTY_LENGTH);
                queuePropertyBuff.writeZero(4);
                if (queueProperty.getProperty() == QueueProperties.OFPQTMINRATE) {
                    RateQueueProperty body = queueProperty.getAugmentation(RateQueueProperty.class);
                    queuePropertyBuff.writeShort(body.getRate().intValue());
                    queuePropertyBuff.writeZero(QUEUE_PROPERTY_PADDING);
                }
                queuePropertyBuff.setShort(QUEUE_PROPERTY_LENGTH_INDEX, queuePropertyBuff.readableBytes());
                queueBuff.writeBytes(queuePropertyBuff);
            }
            queueBuff.setShort(QUEUE_LENGTH_INDEX, queueBuff.readableBytes());
            outBuffer.writeBytes(queueBuff);
        }

        ByteBufUtils.updateOFHeaderLength(outBuffer);
    }
}
