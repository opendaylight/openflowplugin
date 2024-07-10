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
import static org.opendaylight.yangtools.yang.common.netty.ByteBufUtils.readUint8;

import io.netty.buffer.ByteBuf;
import org.opendaylight.openflowjava.protocol.api.extensibility.OFDeserializer;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev130715.Counter32;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev130715.Counter64;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.table.statistics.rev131215.flow.table.and.statistics.map.FlowTableAndStatisticsMap;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.table.statistics.rev131215.flow.table.and.statistics.map.FlowTableAndStatisticsMapBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.table.statistics.rev131215.flow.table.and.statistics.map.FlowTableAndStatisticsMapKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.table.statistics.rev131215.multipart.reply.multipart.reply.body.MultipartReplyFlowTableStatsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.multipart.types.rev170112.multipart.reply.MultipartReplyBody;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.TableId;
import org.opendaylight.yangtools.binding.util.BindingMap;

public class MultipartReplyFlowTableStatsDeserializer implements OFDeserializer<MultipartReplyBody> {
    private static final byte PADDING_IN_TABLE_HEADER = 3;

    @Override
    public MultipartReplyBody deserialize(final ByteBuf message) {
        final MultipartReplyFlowTableStatsBuilder builder = new MultipartReplyFlowTableStatsBuilder();
        final var items = BindingMap.<FlowTableAndStatisticsMapKey, FlowTableAndStatisticsMap>orderedBuilder();

        while (message.readableBytes() > 0) {
            final FlowTableAndStatisticsMapBuilder itemBuilder = new FlowTableAndStatisticsMapBuilder()
                .setTableId(new TableId(readUint8(message)));

            message.skipBytes(PADDING_IN_TABLE_HEADER);

            items.add(itemBuilder
                .setActiveFlows(new Counter32(readUint32(message)))
                .setPacketsLookedUp(new Counter64(readUint64(message)))
                .setPacketsMatched(new Counter64(readUint64(message)))
                .build());
        }

        return builder
            .setFlowTableAndStatisticsMap(items.build())
            .build();
    }
}
