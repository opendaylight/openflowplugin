/*
 * Copyright (c) 2016 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.protocol.deserialization.messages;

import static org.opendaylight.yangtools.yang.common.netty.ByteBufUtils.readUint16;
import static org.opendaylight.yangtools.yang.common.netty.ByteBufUtils.readUint32;

import io.netty.buffer.ByteBuf;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import org.opendaylight.openflowjava.protocol.api.extensibility.DeserializerRegistry;
import org.opendaylight.openflowjava.protocol.api.extensibility.DeserializerRegistryInjector;
import org.opendaylight.openflowjava.protocol.api.extensibility.OFDeserializer;
import org.opendaylight.openflowjava.protocol.api.util.EncodeConstants;
import org.opendaylight.openflowplugin.extension.api.path.ActionPath;
import org.opendaylight.openflowplugin.impl.protocol.deserialization.util.ActionUtil;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.list.ActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.list.ActionKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.BucketId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.GroupId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.GroupMessage;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.GroupMessageBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.GroupTypes;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.group.BucketsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.group.buckets.Bucket;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.group.buckets.BucketBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.GroupModCommand;
import org.opendaylight.yangtools.yang.binding.util.BindingMap;
import org.opendaylight.yangtools.yang.common.Uint32;

public class GroupMessageDeserializer implements OFDeserializer<GroupMessage>, DeserializerRegistryInjector {

    private static final byte PADDING = 1;
    private static final byte PADDING_IN_BUCKETS_HEADER = 4;
    private static final byte BUCKETS_HEADER_LENGTH = 16;

    private static final Comparator<Bucket> COMPARATOR = (bucket1, bucket2) -> {
        if (bucket1.getBucketId() == null || bucket2.getBucketId() == null) {
            return 0;
        }
        return bucket1.getBucketId().getValue().compareTo(bucket2.getBucketId().getValue());
    };

    private DeserializerRegistry registry;

    @Override
    public GroupMessage deserialize(final ByteBuf message) {
        final GroupMessageBuilder builder = new GroupMessageBuilder()
            .setVersion(EncodeConstants.OF_VERSION_1_3)
            .setXid(readUint32(message))
            .setCommand(GroupModCommand.forValue(message.readUnsignedShort()));

        builder.setGroupType(GroupTypes.forValue(message.readUnsignedByte()));
        message.skipBytes(PADDING);
        builder.setGroupId(new GroupId(readUint32(message)));

        final List<Bucket> buckets = new ArrayList<>();

        while (message.readableBytes() > 0) {
            final int length = message.readUnsignedShort();

            final BucketBuilder bucket = new BucketBuilder()
                .setWeight(readUint16(message))
                .setWatchPort(readUint32(message))
                .setWatchGroup(readUint32(message));

            message.skipBytes(PADDING_IN_BUCKETS_HEADER);

            if (message.readableBytes() > 0) {
                final var actions = BindingMap.<
                    org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.list.ActionKey,
                    org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.list.Action>
                        orderedBuilder();
                final int startIndex = message.readerIndex();
                final int bucketLength = length - BUCKETS_HEADER_LENGTH;
                int offset = 0;

                while (message.readerIndex() - startIndex < bucketLength) {
                    actions.add(new ActionBuilder()
                        .withKey(new ActionKey(offset))
                        .setOrder(offset)
                        .setAction(ActionUtil.readAction(EncodeConstants.OF_VERSION_1_3, message, registry,
                                ActionPath.GROUP_DESC_STATS_UPDATED_BUCKET_ACTION))
                        .build());

                    offset++;
                }

                bucket.setAction(actions.build());
            }

            buckets.add(bucket.setBucketId(new BucketId(Uint32.valueOf(buckets.size()))).build());
        }

        buckets.sort(COMPARATOR);
        return builder
            .setBuckets(new BucketsBuilder()
                .setBucket(BindingMap.ordered(buckets))
                .build())
            .build();
    }

    @Override
    public void injectDeserializerRegistry(final DeserializerRegistry deserializerRegistry) {
        registry = deserializerRegistry;
    }

}
