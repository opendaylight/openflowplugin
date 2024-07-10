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
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeConnectorId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.statistics.types.rev130925.duration.DurationBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.statistics.types.rev130925.node.connector.statistics.BytesBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.statistics.types.rev130925.node.connector.statistics.PacketsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.multipart.types.rev170112.multipart.reply.MultipartReplyBody;
import org.opendaylight.yang.gen.v1.urn.opendaylight.port.statistics.rev131214.multipart.reply.multipart.reply.body.MultipartReplyPortStatsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.port.statistics.rev131214.node.connector.statistics.and.port.number.map.NodeConnectorStatisticsAndPortNumberMap;
import org.opendaylight.yang.gen.v1.urn.opendaylight.port.statistics.rev131214.node.connector.statistics.and.port.number.map.NodeConnectorStatisticsAndPortNumberMapBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.port.statistics.rev131214.node.connector.statistics.and.port.number.map.NodeConnectorStatisticsAndPortNumberMapKey;
import org.opendaylight.yangtools.binding.util.BindingMap;

public class MultipartReplyPortStatsDeserializer implements OFDeserializer<MultipartReplyBody> {

    private static final byte PADDING_IN_PORT_STATS_HEADER = 4;

    @Override
    public MultipartReplyBody deserialize(final ByteBuf message) {
        final MultipartReplyPortStatsBuilder builder = new MultipartReplyPortStatsBuilder();
        final var items = BindingMap
            .<NodeConnectorStatisticsAndPortNumberMapKey, NodeConnectorStatisticsAndPortNumberMap>orderedBuilder();

        while (message.readableBytes() > 0) {
            final NodeConnectorStatisticsAndPortNumberMapBuilder itemBuilder =
                    new NodeConnectorStatisticsAndPortNumberMapBuilder();

            final long port = message.readUnsignedInt();
            itemBuilder.setNodeConnectorId(new NodeConnectorId(OpenflowPortsUtil
                    .getProtocolAgnosticPortUri(EncodeConstants.OF_VERSION_1_3, port)));

            message.skipBytes(PADDING_IN_PORT_STATS_HEADER);

            items.add(itemBuilder
                    .withKey(new NodeConnectorStatisticsAndPortNumberMapKey(itemBuilder.getNodeConnectorId()))
                    .setPackets(new PacketsBuilder()
                            .setReceived(readUint64(message))
                            .setTransmitted(readUint64(message))
                            .build())
                    .setBytes(new BytesBuilder()
                            .setReceived(readUint64(message))
                            .setTransmitted(readUint64(message))
                            .build())
                    .setReceiveDrops(readUint64(message))
                    .setTransmitDrops(readUint64(message))
                    .setReceiveErrors(readUint64(message))
                    .setTransmitErrors(readUint64(message))
                    .setReceiveFrameError(readUint64(message))
                    .setReceiveOverRunError(readUint64(message))
                    .setReceiveCrcError(readUint64(message))
                    .setCollisionCount(readUint64(message))
                    .setDuration(new DurationBuilder()
                            .setSecond(new Counter32(readUint32(message)))
                            .setNanosecond(new Counter32(readUint32(message)))
                            .build())
                    .build());
        }

        return builder
                .setNodeConnectorStatisticsAndPortNumberMap(items.build())
                .build();
    }

}
