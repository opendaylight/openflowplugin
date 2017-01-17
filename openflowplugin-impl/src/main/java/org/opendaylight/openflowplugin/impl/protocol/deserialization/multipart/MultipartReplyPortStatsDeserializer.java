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
import java.util.Objects;
import org.opendaylight.openflowjava.protocol.api.extensibility.OFDeserializer;
import org.opendaylight.openflowjava.protocol.api.util.EncodeConstants;
import org.opendaylight.openflowplugin.openflow.md.util.OpenflowPortsUtil;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev130715.Counter32;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeConnectorId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.statistics.types.rev130925.duration.DurationBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.statistics.types.rev130925.node.connector.statistics.BytesBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.statistics.types.rev130925.node.connector.statistics.PacketsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.multipart.types.rev170112.multipart.reply.MultipartReplyBody;
import org.opendaylight.yang.gen.v1.urn.opendaylight.port.statistics.rev131214.multipart.reply.multipart.reply.body.MultipartReplyNodeConnectorBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.port.statistics.rev131214.node.connector.statistics.and.port.number.map.NodeConnectorStatisticsAndPortNumberMap;
import org.opendaylight.yang.gen.v1.urn.opendaylight.port.statistics.rev131214.node.connector.statistics.and.port.number.map.NodeConnectorStatisticsAndPortNumberMapBuilder;

public class MultipartReplyPortStatsDeserializer implements OFDeserializer<MultipartReplyBody> {

    private static final byte PADDING_IN_PORT_STATS_HEADER = 4;

    @Override
    public MultipartReplyBody deserialize(ByteBuf message) {
        final MultipartReplyNodeConnectorBuilder builder = new MultipartReplyNodeConnectorBuilder();
        final List<NodeConnectorStatisticsAndPortNumberMap> items = new ArrayList<>();

        while (message.readableBytes() > 0) {
            final NodeConnectorStatisticsAndPortNumberMapBuilder itemBuilder =
                new NodeConnectorStatisticsAndPortNumberMapBuilder();

            final long port = message.readUnsignedInt();
            final String portName = OpenflowPortsUtil.getPortLogicalName(EncodeConstants.OF13_VERSION_ID, port);

            // We do not assign datapath ID here, because we simply do not have it
            itemBuilder.setNodeConnectorId(new NodeConnectorId(Objects.isNull(portName)
                        ? String.valueOf(port)
                        : portName));

            message.skipBytes(PADDING_IN_PORT_STATS_HEADER);

            items.add(itemBuilder
                    .setPackets(new PacketsBuilder()
                        .setReceived(BigInteger.valueOf(message.readLong()))
                        .setTransmitted(BigInteger.valueOf(message.readLong()))
                        .build())
                    .setBytes(new BytesBuilder()
                        .setReceived(BigInteger.valueOf(message.readLong()))
                        .setTransmitted(BigInteger.valueOf(message.readLong()))
                        .build())
                    .setReceiveDrops(BigInteger.valueOf(message.readLong()))
                    .setTransmitDrops(BigInteger.valueOf(message.readLong()))
                    .setReceiveErrors(BigInteger.valueOf(message.readLong()))
                    .setTransmitErrors(BigInteger.valueOf(message.readLong()))
                    .setReceiveFrameError(BigInteger.valueOf(message.readLong()))
                    .setReceiveOverRunError(BigInteger.valueOf((message.readLong())))
                    .setReceiveCrcError(BigInteger.valueOf((message.readLong())))
                    .setCollisionCount(BigInteger.valueOf(message.readLong()))
                    .setDuration(new DurationBuilder()
                        .setSecond(new Counter32(message.readUnsignedInt()))
                        .setNanosecond(new Counter32(message.readUnsignedInt()))
                        .build())
                    .build());
        }

        return builder
            .setNodeConnectorStatisticsAndPortNumberMap(items)
            .build();
    }

}
