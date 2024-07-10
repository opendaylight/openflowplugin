/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.protocol.deserialization.multipart;

import static org.opendaylight.yangtools.yang.common.netty.ByteBufUtils.readUint32;
import static org.opendaylight.yangtools.yang.common.netty.ByteBufUtils.readUint64;

import io.netty.buffer.ByteBuf;
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
import org.opendaylight.yangtools.binding.util.BindingMap;

public class MultipartReplyQueueStatsDeserializer implements OFDeserializer<MultipartReplyBody> {

    @Override
    public MultipartReplyBody deserialize(final ByteBuf message) {
        final var items = BindingMap.<QueueIdAndStatisticsMapKey, QueueIdAndStatisticsMap>orderedBuilder();

        while (message.readableBytes() > 0) {
            final long port = message.readUnsignedInt();
            final NodeConnectorId nodeConnectorId = new NodeConnectorId(OpenflowPortsUtil
                    .getProtocolAgnosticPortUri(EncodeConstants.OF_VERSION_1_3, port));

            items.add(new QueueIdAndStatisticsMapBuilder()
                .setNodeConnectorId(nodeConnectorId)
                .setQueueId(new QueueId(readUint32(message)))
                .setTransmittedBytes(new Counter64(readUint64(message)))
                .setTransmittedPackets(new Counter64(readUint64(message)))
                .setTransmissionErrors(new Counter64(readUint64(message)))
                .setDuration(new DurationBuilder()
                    .setSecond(new Counter32(readUint32(message)))
                    .setNanosecond(new Counter32(readUint32(message)))
                    .build())
                .build());
        }

        return new MultipartReplyQueueStatsBuilder()
            .setQueueIdAndStatisticsMap(items.build())
            .build();
    }
}
