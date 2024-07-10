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
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev130715.Counter32;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev130715.Counter64;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.statistics.rev131111.multipart.reply.multipart.reply.body.MultipartReplyGroupStatsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.BucketId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.GroupId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.group.statistics.BucketsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.group.statistics.DurationBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.group.statistics.buckets.BucketCounter;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.group.statistics.buckets.BucketCounterBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.group.statistics.buckets.BucketCounterKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.group.statistics.reply.GroupStats;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.group.statistics.reply.GroupStatsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.group.statistics.reply.GroupStatsKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.multipart.types.rev170112.multipart.reply.MultipartReplyBody;
import org.opendaylight.yangtools.binding.util.BindingMap;
import org.opendaylight.yangtools.yang.common.Uint32;

public class MultipartReplyGroupStatsDeserializer implements OFDeserializer<MultipartReplyBody> {
    private static final byte PADDING_IN_GROUP_HEADER_01 = 2;
    private static final byte PADDING_IN_GROUP_HEADER_02 = 4;
    private static final byte GROUP_BODY_LENGTH = 40;
    private static final byte BUCKET_COUNTER_LENGTH = 16;

    @Override
    public MultipartReplyBody deserialize(final ByteBuf message) {
        final var items = BindingMap.<GroupStatsKey, GroupStats>orderedBuilder();

        while (message.readableBytes() > 0) {
            final int itemLength = message.readUnsignedShort();
            message.skipBytes(PADDING_IN_GROUP_HEADER_01);

            final GroupStatsBuilder itemBuilder = new GroupStatsBuilder()
                .setGroupId(new GroupId(readUint32(message)))
                .setRefCount(new Counter32(readUint32(message)));

            message.skipBytes(PADDING_IN_GROUP_HEADER_02);

            itemBuilder
                .setPacketCount(new Counter64(readUint64(message)))
                .setByteCount(new Counter64(readUint64(message)))
                .setDuration(new DurationBuilder()
                        .setSecond(new Counter32(readUint32(message)))
                        .setNanosecond(new Counter32(readUint32(message)))
                        .build());

            final var subItems = BindingMap.<BucketCounterKey, BucketCounter>orderedBuilder();
            int actualLength = GROUP_BODY_LENGTH;
            int bucketKey = 0;

            while (actualLength < itemLength) {
                subItems.add(new BucketCounterBuilder()
                    .setBucketId(new BucketId(Uint32.valueOf(bucketKey++)))
                    .setPacketCount(new Counter64(readUint64(message)))
                    .setByteCount(new Counter64(readUint64(message)))
                    .build());

                actualLength += BUCKET_COUNTER_LENGTH;
            }

            items.add(itemBuilder
                    .setBuckets(new BucketsBuilder()
                        .setBucketCounter(subItems.build())
                        .build())
                    .build());
        }

        return new MultipartReplyGroupStatsBuilder()
            .setGroupStats(items.build())
            .build();
    }
}
