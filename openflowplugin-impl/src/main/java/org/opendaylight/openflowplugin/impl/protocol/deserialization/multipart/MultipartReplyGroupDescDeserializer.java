/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.impl.protocol.deserialization.multipart;

import io.netty.buffer.ByteBuf;
import java.util.ArrayList;
import java.util.List;
import org.opendaylight.openflowjava.protocol.api.extensibility.DeserializerRegistry;
import org.opendaylight.openflowjava.protocol.api.extensibility.DeserializerRegistryInjector;
import org.opendaylight.openflowjava.protocol.api.extensibility.OFDeserializer;
import org.opendaylight.openflowjava.protocol.api.util.EncodeConstants;
import org.opendaylight.openflowplugin.extension.api.path.ActionPath;
import org.opendaylight.openflowplugin.impl.protocol.deserialization.util.ActionUtil;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.list.ActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.list.ActionKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.statistics.rev131111.multipart.reply.multipart.reply.body.MultipartReplyGroupDescBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.BucketId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.GroupId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.GroupTypes;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.group.BucketsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.group.buckets.Bucket;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.group.buckets.BucketBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.group.buckets.BucketKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.group.desc.stats.reply.GroupDescStats;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.group.desc.stats.reply.GroupDescStatsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.group.desc.stats.reply.GroupDescStatsKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.multipart.types.rev170112.multipart.reply.MultipartReplyBody;

public class MultipartReplyGroupDescDeserializer implements OFDeserializer<MultipartReplyBody>, DeserializerRegistryInjector {

    private static final byte PADDING_IN_GROUP_DESC_HEADER = 1;
    private static final byte PADDING_IN_BUCKETS_HEADER = 4;
    private static final byte GROUP_DESC_HEADER_LENGTH = 8;
    private static final byte BUCKETS_HEADER_LENGTH = 16;
    private DeserializerRegistry registry;

    @Override
    public MultipartReplyBody deserialize(ByteBuf message) {
        final MultipartReplyGroupDescBuilder builder = new MultipartReplyGroupDescBuilder();
        final List<GroupDescStats> items = new ArrayList<>();

        while (message.readableBytes() > 0) {
            final int itemLength = message.readUnsignedShort();

            final GroupDescStatsBuilder itemBuilder = new GroupDescStatsBuilder()
                .setGroupType(GroupTypes.forValue(message.readUnsignedByte()));

            message.skipBytes(PADDING_IN_GROUP_DESC_HEADER);
            itemBuilder.setGroupId(new GroupId(message.readUnsignedInt()));
            itemBuilder.setKey(new GroupDescStatsKey(itemBuilder.getGroupId()));

            final List<Bucket> subItems = new ArrayList<>();
            int actualLength = GROUP_DESC_HEADER_LENGTH;

            long bucketKey = 0;
            while (actualLength < itemLength) {
                final int bucketsLength = message.readUnsignedShort();

                final BucketBuilder bucketBuilder = new BucketBuilder()
                    .setBucketId(new BucketId(bucketKey))
                    .setKey(new BucketKey(new BucketId(bucketKey)))
                    .setWeight(message.readUnsignedShort())
                    .setWatchPort(message.readUnsignedInt())
                    .setWatchGroup(message.readUnsignedInt());

                message.skipBytes(PADDING_IN_BUCKETS_HEADER);
                final List<org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.list
                    .Action> actions = new ArrayList<>();
                final int startIndex = message.readerIndex();
                final int bucketLength = bucketsLength - BUCKETS_HEADER_LENGTH;
                int offset = 0;

                while ((message.readerIndex() - startIndex) < bucketLength) {
                    actions.add(new ActionBuilder()
                            .setKey(new ActionKey(offset))
                            .setOrder(offset)
                            .setAction(ActionUtil.readAction(EncodeConstants.OF13_VERSION_ID, message, registry,
                                    ActionPath.GROUPDESCSTATSUPDATED_GROUPDESCSTATS_BUCKETS_BUCKET_ACTION))
                            .build());

                    offset++;
                }

                bucketBuilder.setAction(actions);
                subItems.add(bucketBuilder.build());
                bucketKey++;
                actualLength += bucketsLength;
            }

            items.add(itemBuilder
                .setBuckets(new BucketsBuilder()
                    .setBucket(subItems)
                    .build())
                .build());
        }

        return builder
            .setGroupDescStats(items)
            .build();
    }

    @Override
    public void injectDeserializerRegistry(DeserializerRegistry deserializerRegistry) {
        registry = deserializerRegistry;
    }

}
