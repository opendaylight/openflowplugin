/*
 * Copyright (c) 2016 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.impl.protocol.deserialization.messages;

import java.util.ArrayList;
import java.util.Collections;
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
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.GroupId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.GroupMessage;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.GroupMessageBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.GroupTypes;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.group.BucketsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.group.buckets.Bucket;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.group.buckets.BucketBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.GroupModCommand;

import io.netty.buffer.ByteBuf;

public class GroupMessageDeserializer implements OFDeserializer<GroupMessage>, DeserializerRegistryInjector {

    private static final byte PADDING = 1;
    private static final byte PADDING_IN_BUCKETS_HEADER = 4;
    private static final byte BUCKETS_HEADER_LENGTH = 16;

    private static final Comparator<Bucket> COMPARATOR = (bucket1, bucket2) -> {
        if (bucket1.getBucketId() == null || bucket2.getBucketId() == null) return 0;
        return bucket1.getBucketId().getValue().compareTo(bucket2.getBucketId().getValue());
    };

    private DeserializerRegistry registry;

    @Override
    public GroupMessage deserialize(ByteBuf message) {
        final GroupMessageBuilder builder = new GroupMessageBuilder()
            .setVersion((short) EncodeConstants.OF13_VERSION_ID)
            .setXid(message.readUnsignedInt())
            .setCommand(GroupModCommand.forValue(message.readUnsignedShort()));

        builder.setGroupType(GroupTypes.forValue(message.readUnsignedByte()));
        message.skipBytes(PADDING);
        builder.setGroupId(new GroupId(message.readUnsignedInt()));

        final List<Bucket> buckets = new ArrayList<>();

        while (message.readableBytes() > 0) {
            final int length = message.readUnsignedShort();

            final BucketBuilder bucket = new BucketBuilder()
                .setWeight(message.readUnsignedShort())
                .setWatchPort(message.readUnsignedInt())
                .setWatchGroup(message.readUnsignedInt());

            message.skipBytes(PADDING_IN_BUCKETS_HEADER);

            if (message.readableBytes() > 0) {
                final List<org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.list
                    .Action> actions = new ArrayList<>();
                final int startIndex = message.readerIndex();
                final int bucketLength = length - BUCKETS_HEADER_LENGTH;
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

                bucket.setAction(actions);
            }

            buckets.add(bucket.build());
        }

        Collections.sort(buckets, COMPARATOR);
        return builder
            .setBuckets(new BucketsBuilder()
                .setBucket(buckets)
                .build())
            .build();
    }

    @Override
    public void injectDeserializerRegistry(DeserializerRegistry deserializerRegistry) {
        registry = deserializerRegistry;
    }

}
