/*
 * Copyright (c) 2016 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.protocol.serialization.messages;

import static java.util.Objects.requireNonNull;

import com.google.common.base.MoreObjects;
import io.netty.buffer.ByteBuf;
import java.util.Comparator;
import java.util.Optional;
import org.opendaylight.openflowjava.protocol.api.extensibility.SerializerLookup;
import org.opendaylight.openflowjava.protocol.api.util.EncodeConstants;
import org.opendaylight.openflowplugin.api.OFConstants;
import org.opendaylight.openflowplugin.impl.protocol.serialization.util.ActionUtil;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.common.OrderComparator;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.GroupMessage;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.group.buckets.Bucket;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.GroupModCommand;
import org.opendaylight.yangtools.yang.common.Uint16;

/**
 * Translates GroupMod messages.
 * OF protocol versions: 1.3.
 */
public class GroupMessageSerializer extends AbstractMessageSerializer<GroupMessage> {
    private static final byte PADDING_IN_GROUP_MOD_MESSAGE = 1;
    private static final byte PADDING_IN_BUCKET = 4;
    private static final int OFPGC_ADD_OR_MOD = 32768;
    private final boolean isGroupAddModEnabled;

    private static final Comparator<Bucket> COMPARATOR = (bucket1, bucket2) -> {
        if (bucket1.getBucketId() == null || bucket2.getBucketId() == null) {
            return 0;
        }
        return bucket1.getBucketId().getValue().compareTo(bucket2.getBucketId().getValue());
    };

    private final SerializerLookup registry;

    public GroupMessageSerializer(final SerializerLookup registry, final boolean isGroupAddModEnabled) {
        this.registry = requireNonNull(registry);
        this.isGroupAddModEnabled = isGroupAddModEnabled;
    }

    @Override
    public void serialize(final GroupMessage message, final ByteBuf outBuffer) {
        final int index = outBuffer.writerIndex();
        super.serialize(message, outBuffer);
        if (isGroupAddModEnabled) {
            if (message.getCommand().equals(GroupModCommand.OFPGCADD)
                    || message.getCommand().equals(GroupModCommand.OFPGCMODIFY)) {
                outBuffer.writeShort(OFPGC_ADD_OR_MOD);
            } else {
                outBuffer.writeShort(message.getCommand().getIntValue());
            }
        } else {
            outBuffer.writeShort(message.getCommand().getIntValue());
        }
        outBuffer.writeByte(message.getGroupType().getIntValue());
        outBuffer.writeZero(PADDING_IN_GROUP_MOD_MESSAGE);
        outBuffer.writeInt(message.getGroupId().getValue().intValue());

        Optional.ofNullable(message.getBuckets())
            .filter(b -> !GroupModCommand.OFPGCDELETE.equals(message.getCommand()))
            .flatMap(b -> Optional.ofNullable(b.nonnullBucket()))
            .ifPresent(b -> b.values().stream()
                .sorted(COMPARATOR)
                .forEach(bucket -> {
                    final int bucketIndex = outBuffer.writerIndex();
                    outBuffer.writeShort(EncodeConstants.EMPTY_LENGTH);
                    outBuffer.writeShort(MoreObjects.firstNonNull(bucket.getWeight(), Uint16.ZERO).toJava());
                    outBuffer.writeInt(MoreObjects.firstNonNull(bucket.getWatchPort(), OFConstants.OFPG_ANY)
                            .intValue());
                    outBuffer.writeInt(MoreObjects.firstNonNull(bucket.getWatchGroup(), OFConstants.OFPG_ANY)
                            .intValue());
                    outBuffer.writeZero(PADDING_IN_BUCKET);

                    Optional.ofNullable(bucket.nonnullAction()).ifPresent(as -> as.values()
                            .stream()
                            .sorted(OrderComparator.build())
                            .forEach(a -> ActionUtil.writeAction(
                                    a.getAction(),
                                    OFConstants.OFP_VERSION_1_3,
                                    registry,
                                    outBuffer)));

                    outBuffer.setShort(bucketIndex, outBuffer.writerIndex() - bucketIndex);
                }));
        outBuffer.setShort(index + 2, outBuffer.writerIndex() - index);
    }

    @Override
    protected byte getMessageType() {
        return 15;
    }
}
