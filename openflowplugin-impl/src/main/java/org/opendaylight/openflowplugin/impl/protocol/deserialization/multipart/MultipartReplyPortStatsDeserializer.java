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
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeConnectorId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.statistics.types.rev130925.duration.DurationBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.statistics.types.rev130925.node.connector.statistics.BytesBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.statistics.types.rev130925.node.connector.statistics.PacketsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.multipart.types.rev170112.multipart.reply.MultipartReplyBody;
import org.opendaylight.yang.gen.v1.urn.opendaylight.port.statistics.rev131214.multipart.reply.multipart.reply.body.MultipartReplyPortStatsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.port.statistics.rev131214.node.connector.statistics.and.port.number.map.NodeConnectorStatisticsAndPortNumberMap;
import org.opendaylight.yang.gen.v1.urn.opendaylight.port.statistics.rev131214.node.connector.statistics.and.port.number.map.NodeConnectorStatisticsAndPortNumberMapBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.port.statistics.rev131214.node.connector.statistics.and.port.number.map.NodeConnectorStatisticsAndPortNumberMapKey;

public class MultipartReplyPortStatsDeserializer implements OFDeserializer<MultipartReplyBody> {

    private static final byte PADDING_IN_PORT_STATS_HEADER = 4;

    @Override
    public MultipartReplyBody deserialize(ByteBuf message) {
        final MultipartReplyPortStatsBuilder builder = new MultipartReplyPortStatsBuilder();
        final List<NodeConnectorStatisticsAndPortNumberMap> items = new ArrayList<>();

        while (message.readableBytes() > 0) {
            final NodeConnectorStatisticsAndPortNumberMapBuilder itemBuilder =
                new NodeConnectorStatisticsAndPortNumberMapBuilder();

            final long port = message.readUnsignedInt();
            itemBuilder.setNodeConnectorId(new NodeConnectorId(OpenflowPortsUtil.getProtocolAgnosticPortUri(EncodeConstants.OF13_VERSION_ID, port)));

            message.skipBytes(PADDING_IN_PORT_STATS_HEADER);

            final byte[] recPack = new byte[EncodeConstants.SIZE_OF_LONG_IN_BYTES];
            message.readBytes(recPack);
            final byte[] txPack = new byte[EncodeConstants.SIZE_OF_LONG_IN_BYTES];
            message.readBytes(txPack);
            final byte[] recByt = new byte[EncodeConstants.SIZE_OF_LONG_IN_BYTES];
            message.readBytes(recByt);
            final byte[] txByt = new byte[EncodeConstants.SIZE_OF_LONG_IN_BYTES];
            message.readBytes(txByt);
            final byte[] recDrop = new byte[EncodeConstants.SIZE_OF_LONG_IN_BYTES];
            message.readBytes(recDrop);
            final byte[] txDrop = new byte[EncodeConstants.SIZE_OF_LONG_IN_BYTES];
            message.readBytes(txDrop);
            final byte[] recError = new byte[EncodeConstants.SIZE_OF_LONG_IN_BYTES];
            message.readBytes(recError);
            final byte[] txError = new byte[EncodeConstants.SIZE_OF_LONG_IN_BYTES];
            message.readBytes(txError);
            final byte[] recFrameError = new byte[EncodeConstants.SIZE_OF_LONG_IN_BYTES];
            message.readBytes(recFrameError);
            final byte[] recOverRunError = new byte[EncodeConstants.SIZE_OF_LONG_IN_BYTES];
            message.readBytes(recOverRunError);
            final byte[] recCrcError = new byte[EncodeConstants.SIZE_OF_LONG_IN_BYTES];
            message.readBytes(recCrcError);
            final byte[] collisionCount = new byte[EncodeConstants.SIZE_OF_LONG_IN_BYTES];
            message.readBytes(collisionCount);

            items.add(itemBuilder
                .setKey(new NodeConnectorStatisticsAndPortNumberMapKey(itemBuilder.getNodeConnectorId()))
                .setPackets(new PacketsBuilder()
                    .setReceived(new BigInteger(1, recPack))
                    .setTransmitted(new BigInteger(1, txPack))
                    .build())
                .setBytes(new BytesBuilder()
                    .setReceived(new BigInteger(1, recByt))
                    .setTransmitted(new BigInteger(1, txByt))
                    .build())
                .setReceiveDrops(new BigInteger(1, recDrop))
                .setTransmitDrops(new BigInteger(1, txDrop))
                .setReceiveErrors(new BigInteger(1, recError))
                .setTransmitErrors(new BigInteger(1, txError))
                .setReceiveFrameError(new BigInteger(1, recFrameError))
                .setReceiveOverRunError(new BigInteger(1, recOverRunError))
                .setReceiveCrcError(new BigInteger(1, recCrcError))
                .setCollisionCount(new BigInteger(1, collisionCount))
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
