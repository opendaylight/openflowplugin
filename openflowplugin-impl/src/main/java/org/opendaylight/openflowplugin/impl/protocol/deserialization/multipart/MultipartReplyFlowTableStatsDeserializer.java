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
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev130715.Counter32;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev130715.Counter64;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.table.statistics.rev131215.flow.table.and.statistics.map.FlowTableAndStatisticsMap;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.table.statistics.rev131215.flow.table.and.statistics.map.FlowTableAndStatisticsMapBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.table.statistics.rev131215.multipart.reply.multipart.reply.body.MultipartReplyFlowTableBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.multipart.types.rev170112.multipart.reply.MultipartReplyBody;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.TableId;

public class MultipartReplyFlowTableStatsDeserializer implements OFDeserializer<MultipartReplyBody> {

    private static final byte PADDING_IN_TABLE_HEADER = 3;

    @Override
    public MultipartReplyBody deserialize(ByteBuf message) {
        final MultipartReplyFlowTableBuilder builder = new MultipartReplyFlowTableBuilder();
        final List<FlowTableAndStatisticsMap> items = new ArrayList<>();

        while (message.readableBytes() > 0) {
            final FlowTableAndStatisticsMapBuilder itemBuilder = new FlowTableAndStatisticsMapBuilder()
                .setTableId(new TableId(message.readUnsignedByte()));

            message.skipBytes(PADDING_IN_TABLE_HEADER);

            items.add(itemBuilder
                    .setActiveFlows(new Counter32(message.readUnsignedInt()))
                    .setPacketsLookedUp(new Counter64(BigInteger.valueOf(message.readLong())))
                    .setPacketsMatched(new Counter64(BigInteger.valueOf(message.readLong())))
                    .build());
        }

        return builder
            .setFlowTableAndStatisticsMap(items)
            .build();
    }

}
