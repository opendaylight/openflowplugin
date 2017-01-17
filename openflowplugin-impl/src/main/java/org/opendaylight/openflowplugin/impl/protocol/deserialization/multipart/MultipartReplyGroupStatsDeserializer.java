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
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.statistics.rev131111.multipart.reply.multipart.reply.body.MultipartReplyGroupBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.GroupId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.group.statistics.BucketsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.group.statistics.DurationBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.group.statistics.buckets.BucketCounter;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.group.statistics.buckets.BucketCounterBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.group.statistics.reply.GroupStats;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.group.statistics.reply.GroupStatsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.multipart.types.rev170112.multipart.reply.MultipartReplyBody;

public class MultipartReplyGroupStatsDeserializer implements OFDeserializer<MultipartReplyBody> {

    private static final byte PADDING_IN_GROUP_HEADER_01 = 2;
    private static final byte PADDING_IN_GROUP_HEADER_02 = 4;
    private static final byte GROUP_BODY_LENGTH = 40;
    private static final byte BUCKET_COUNTER_LENGTH = 16;

    @Override
    public MultipartReplyBody deserialize(ByteBuf message) {
        final MultipartReplyGroupBuilder builder = new MultipartReplyGroupBuilder();
        final List<GroupStats> items = new ArrayList<>();

        while (message.readableBytes() > 0) {
            final int itemLength = message.readUnsignedShort();
            message.skipBytes(PADDING_IN_GROUP_HEADER_01);

            final GroupStatsBuilder itemBuilder = new GroupStatsBuilder()
                .setGroupId(new GroupId(message.readUnsignedInt()))
                .setRefCount(new Counter32(message.readUnsignedInt()));

            message.skipBytes(PADDING_IN_GROUP_HEADER_02);

            itemBuilder
                .setPacketCount(new Counter64(BigInteger.valueOf(message.readLong())))
                .setByteCount(new Counter64(BigInteger.valueOf(message.readLong())))
                .setDuration(new DurationBuilder()
                        .setSecond(new Counter32(message.readUnsignedInt()))
                        .setNanosecond(new Counter32(message.readUnsignedInt()))
                        .build());

            final List<BucketCounter> subItems = new ArrayList<>();
            int actualLength = GROUP_BODY_LENGTH;

            while (actualLength < itemLength) {
                subItems.add(new BucketCounterBuilder()
                        .setPacketCount(new Counter64(BigInteger.valueOf(message.readLong())))
                        .setByteCount(new Counter64(BigInteger.valueOf(message.readLong())))
                        .build());

                actualLength += BUCKET_COUNTER_LENGTH;
            }

            items.add(itemBuilder
                    .setBuckets(new BucketsBuilder()
                        .setBucketCounter(subItems)
                        .build())
                    .build());
        }

        return builder
            .setGroupStats(items)
            .build();
    }

}
