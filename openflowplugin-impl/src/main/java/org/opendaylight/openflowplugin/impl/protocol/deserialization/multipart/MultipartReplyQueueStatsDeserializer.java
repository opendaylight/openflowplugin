/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.impl.protocol.deserialization.multipart;

import io.netty.buffer.ByteBuf;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import org.opendaylight.openflowjava.protocol.api.extensibility.OFDeserializer;
import org.opendaylight.openflowjava.protocol.api.util.EncodeConstants;
import org.opendaylight.openflowplugin.openflow.md.util.OpenflowPortsUtil;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev130715.Counter32;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev130715.Counter64;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.queue.rev130925.QueueId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeConnectorId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.statistics.types.rev130925.duration.DurationBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.multipart.types.rev170112.multipart.reply.MultipartReplyBody;
import org.opendaylight.yang.gen.v1.urn.opendaylight.queue.statistics.rev131216.multipart.reply.multipart.reply.body.MultipartReplyQueueStatsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.queue.statistics.rev131216.queue.id.and.statistics.map.QueueIdAndStatisticsMap;
import org.opendaylight.yang.gen.v1.urn.opendaylight.queue.statistics.rev131216.queue.id.and.statistics.map.QueueIdAndStatisticsMapBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.queue.statistics.rev131216.queue.id.and.statistics.map.QueueIdAndStatisticsMapKey;

public class MultipartReplyQueueStatsDeserializer implements OFDeserializer<MultipartReplyBody> {

    @Override
    public MultipartReplyBody deserialize(ByteBuf message) {
        final MultipartReplyQueueStatsBuilder builder = new MultipartReplyQueueStatsBuilder();
        final List<QueueIdAndStatisticsMap> items = new ArrayList<>();

        while (message.readableBytes() > 0) {
            final long port = message.readUnsignedInt();
            final NodeConnectorId nodeConnectorId = new NodeConnectorId(OpenflowPortsUtil.getProtocolAgnosticPortUri(EncodeConstants.OF13_VERSION_ID, port));
            final QueueId queueId = new QueueId(message.readUnsignedInt());

            final byte[] txBytes = new byte[EncodeConstants.SIZE_OF_LONG_IN_BYTES];
            message.readBytes(txBytes);
            final byte[] txPackets = new byte[EncodeConstants.SIZE_OF_LONG_IN_BYTES];
            message.readBytes(txPackets);
            final byte[] txErrors = new byte[EncodeConstants.SIZE_OF_LONG_IN_BYTES];
            message.readBytes(txErrors);

            items.add(new QueueIdAndStatisticsMapBuilder()
                .setKey(new QueueIdAndStatisticsMapKey(nodeConnectorId, queueId))
                .setNodeConnectorId(nodeConnectorId)
                .setQueueId(queueId)
                .setTransmittedBytes(new Counter64(new BigInteger(1, txBytes)))
                .setTransmittedPackets(new Counter64(new BigInteger(1, txPackets)))
                .setTransmissionErrors(new Counter64(new BigInteger(1, txErrors)))
                .setDuration(new DurationBuilder()
                    .setSecond(new Counter32(message.readUnsignedInt()))
                    .setNanosecond(new Counter32(message.readUnsignedInt()))
                    .build())
                .build());
        }

        return builder
            .setQueueIdAndStatisticsMap(items)
            .build();
    }

}
