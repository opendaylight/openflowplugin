/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.protocol.deserialization.multipart;

import static java.util.Objects.requireNonNull;

import io.netty.buffer.ByteBuf;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import org.opendaylight.openflowjava.protocol.api.extensibility.DeserializerRegistry;
import org.opendaylight.openflowjava.protocol.api.extensibility.OFDeserializer;
import org.opendaylight.openflowjava.protocol.api.keys.MessageCodeKey;
import org.opendaylight.openflowjava.protocol.api.util.EncodeConstants;
import org.opendaylight.openflowplugin.api.OFConstants;
import org.opendaylight.openflowplugin.extension.api.path.MatchPath;
import org.opendaylight.openflowplugin.impl.protocol.deserialization.key.MessageCodeMatchKey;
import org.opendaylight.openflowplugin.impl.protocol.deserialization.util.InstructionUtil;
import org.opendaylight.openflowplugin.impl.util.MatchUtil;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev130715.Counter32;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev130715.Counter64;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.statistics.rev130819.flow.and.statistics.map.list.FlowAndStatisticsMapList;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.statistics.rev130819.flow.and.statistics.map.list.FlowAndStatisticsMapListBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.statistics.rev130819.multipart.reply.multipart.reply.body.MultipartReplyFlowStatsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.FlowCookie;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.FlowModFlags;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.InstructionsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.list.InstructionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.list.InstructionKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.Match;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.statistics.types.rev130925.duration.DurationBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.multipart.types.rev170112.multipart.reply.MultipartReplyBody;

public class MultipartReplyFlowStatsDeserializer implements OFDeserializer<MultipartReplyBody> {
    private static final MessageCodeKey MATCH_KEY = new MessageCodeMatchKey(EncodeConstants.OF13_VERSION_ID,
            EncodeConstants.EMPTY_VALUE, Match.class, MatchPath.FLOWS_STATISTICS_UPDATE_MATCH);

    private static final byte PADDING_IN_FLOW_STATS_HEADER_01 = 1;
    private static final byte PADDING_IN_FLOW_STATS_HEADER_02 = 4;

    private final DeserializerRegistry registry;

    public MultipartReplyFlowStatsDeserializer(final DeserializerRegistry registry) {
        this.registry = requireNonNull(registry);
    }

    @Override
    public MultipartReplyBody deserialize(final ByteBuf message) {
        final MultipartReplyFlowStatsBuilder builder = new MultipartReplyFlowStatsBuilder();
        final List<FlowAndStatisticsMapList> items = new ArrayList<>();

        while (message.readableBytes() > 0) {
            final FlowAndStatisticsMapListBuilder itemBuilder = new FlowAndStatisticsMapListBuilder();
            final int itemLength = message.readUnsignedShort();
            final ByteBuf itemMessage = message.readSlice(itemLength - Short.BYTES);

            itemBuilder.setTableId(itemMessage.readUnsignedByte());
            itemMessage.skipBytes(PADDING_IN_FLOW_STATS_HEADER_01);

            itemBuilder
                    .setDuration(new DurationBuilder()
                            .setSecond(new Counter32(itemMessage.readUnsignedInt()))
                            .setNanosecond(new Counter32(itemMessage.readUnsignedInt()))
                            .build())
                    .setPriority(itemMessage.readUnsignedShort())
                    .setIdleTimeout(itemMessage.readUnsignedShort())
                    .setHardTimeout(itemMessage.readUnsignedShort())
                    .setFlags(createFlowModFlagsFromBitmap(itemMessage.readUnsignedShort()));

            itemMessage.skipBytes(PADDING_IN_FLOW_STATS_HEADER_02);

            final byte[] cookie = new byte[Long.BYTES];
            itemMessage.readBytes(cookie);
            final byte[] packetCount = new byte[Long.BYTES];
            itemMessage.readBytes(packetCount);
            final byte[] byteCount = new byte[Long.BYTES];
            itemMessage.readBytes(byteCount);

            itemBuilder
                    .setCookie(new FlowCookie(new BigInteger(1, cookie)))
                    .setCookieMask(new FlowCookie(OFConstants.DEFAULT_COOKIE_MASK))
                    .setPacketCount(new Counter64(new BigInteger(1, packetCount)))
                    .setByteCount(new Counter64(new BigInteger(1, byteCount)));

            final OFDeserializer<Match> matchDeserializer = registry.getDeserializer(MATCH_KEY);
            itemBuilder.setMatch(MatchUtil.transformMatch(matchDeserializer.deserialize(itemMessage),
                    org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.Match.class));

            final int length = itemMessage.readableBytes();

            if (length > 0) {
                final List<org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.list
                        .Instruction> instructions = new ArrayList<>();
                final int startIndex = itemMessage.readerIndex();
                int offset = 0;

                while (itemMessage.readerIndex() - startIndex < length) {
                    instructions.add(new InstructionBuilder()
                            .withKey(new InstructionKey(offset))
                            .setOrder(offset)
                            .setInstruction(InstructionUtil
                                    .readInstruction(EncodeConstants.OF13_VERSION_ID, itemMessage, registry))
                            .build());

                    offset++;
                }

                itemBuilder.setInstructions(new InstructionsBuilder()
                        .setInstruction(instructions)
                        .build());
            }

            items.add(itemBuilder.build());
        }

        return builder
                .setFlowAndStatisticsMapList(items)
                .build();
    }

    private static FlowModFlags createFlowModFlagsFromBitmap(final int input) {
        final Boolean ofp_FF_SendFlowRem = (input & 1) != 0;
        final Boolean ofp_FF_CheckOverlap = (input & 1 << 1) != 0;
        final Boolean ofp_FF_ResetCounts = (input & 1 << 2) != 0;
        final Boolean ofp_FF_NoPktCounts = (input & 1 << 3) != 0;
        final Boolean ofp_FF_NoBytCounts = (input & 1 << 4) != 0;
        return new FlowModFlags(ofp_FF_CheckOverlap, ofp_FF_NoBytCounts, ofp_FF_NoPktCounts, ofp_FF_ResetCounts,
                ofp_FF_SendFlowRem);
    }
}
