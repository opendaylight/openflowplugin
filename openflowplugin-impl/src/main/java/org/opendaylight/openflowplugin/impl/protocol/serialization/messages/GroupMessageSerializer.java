/*
 * Copyright (c) 2016 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.impl.protocol.serialization.messages;

import com.google.common.base.MoreObjects;
import io.netty.buffer.ByteBuf;
import java.util.Comparator;
import java.util.Optional;
import org.opendaylight.openflowjava.protocol.api.extensibility.SerializerRegistry;
import org.opendaylight.openflowjava.protocol.api.extensibility.SerializerRegistryInjector;
import org.opendaylight.openflowjava.protocol.api.util.EncodeConstants;
import org.opendaylight.openflowjava.util.ByteBufUtils;
import org.opendaylight.openflowplugin.api.OFConstants;
import org.opendaylight.openflowplugin.impl.protocol.serialization.util.ActionUtil;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.GroupMessage;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.group.buckets.Bucket;

/**
 * Translates GroupMod messages.
 * OF protocol versions: 1.3.
 */
public class GroupMessageSerializer extends AbstractMessageSerializer<GroupMessage> implements SerializerRegistryInjector {
    private static final byte PADDING_IN_GROUP_MOD_MESSAGE = 1;
    private static final byte PADDING_IN_BUCKET = 4;

    private static final Comparator<Bucket> COMPARATOR = (bucket1, bucket2) -> {
        if (bucket1.getBucketId() == null || bucket2.getBucketId() == null) return 0;
        return bucket1.getBucketId().getValue().compareTo(bucket2.getBucketId().getValue());
    };

    private SerializerRegistry registry;

    @Override
    public void serialize(GroupMessage message, ByteBuf outBuffer) {
        super.serialize(message, outBuffer);
        outBuffer.writeShort(message.getCommand().getIntValue());
        outBuffer.writeByte(message.getGroupType().getIntValue());
        outBuffer.writeZero(PADDING_IN_GROUP_MOD_MESSAGE);
        outBuffer.writeInt(message.getGroupId().getValue().intValue());

        Optional.ofNullable(message.getBuckets())
                .flatMap(b -> Optional.ofNullable(b.getBucket()))
                .ifPresent(b -> b.stream()
                        .sorted(COMPARATOR)
                        .forEach(bucket -> {
                            int index = outBuffer.writerIndex();
                            outBuffer.writeShort(EncodeConstants.EMPTY_LENGTH);
                            outBuffer.writeShort(MoreObjects.firstNonNull(bucket.getWeight(), 0));
                            outBuffer.writeInt(MoreObjects.firstNonNull(bucket.getWatchPort(), OFConstants.OFPG_ANY).intValue());
                            outBuffer.writeInt(MoreObjects.firstNonNull(bucket.getWatchGroup(), OFConstants.OFPG_ANY).intValue());
                            outBuffer.writeZero(PADDING_IN_BUCKET);

                            Optional.ofNullable(bucket.getAction()).ifPresent(as -> as.forEach(a ->
                                    ActionUtil.writeAction(
                                            a.getAction(),
                                            OFConstants.OFP_VERSION_1_3,
                                            registry,
                                            outBuffer)));

                            outBuffer.setShort(index, outBuffer.writerIndex() - index);
                        }));

        ByteBufUtils.updateOFHeaderLength(outBuffer);
    }

    @Override
    protected byte getMessageType() {
        return 15;
    }

    @Override
    public void injectSerializerRegistry(SerializerRegistry serializerRegistry) {
        registry = serializerRegistry;
    }
}
