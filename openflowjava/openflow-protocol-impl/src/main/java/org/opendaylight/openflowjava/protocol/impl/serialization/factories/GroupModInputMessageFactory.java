/*
 * Copyright (c) 2013 Pantheon Technologies s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowjava.protocol.impl.serialization.factories;

import static java.util.Objects.requireNonNull;

import io.netty.buffer.ByteBuf;
import java.util.List;
import org.opendaylight.openflowjava.protocol.api.extensibility.OFSerializer;
import org.opendaylight.openflowjava.protocol.api.extensibility.SerializerLookup;
import org.opendaylight.openflowjava.protocol.api.util.EncodeConstants;
import org.opendaylight.openflowjava.protocol.impl.util.ListSerializer;
import org.opendaylight.openflowjava.protocol.impl.util.TypeKeyMakerFactory;
import org.opendaylight.openflowjava.util.ByteBufUtils;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.GroupModCommand;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.GroupMod;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.buckets.grouping.BucketsList;

/**
 * Translates GroupMod messages. OF protocol versions: 1.3.
 *
 * @author timotej.kubas
 * @author michal.polkorab
 */
public class GroupModInputMessageFactory implements OFSerializer<GroupMod> {
    private static final byte MESSAGE_TYPE = 15;
    private static final byte PADDING_IN_GROUP_MOD_MESSAGE = 1;
    private static final byte PADDING_IN_BUCKET = 4;
    private static final int OFPGC_ADD_OR_MOD = 32768;

    private final SerializerLookup registry;
    private final boolean isGroupAddModEnaled;

    public GroupModInputMessageFactory(final SerializerLookup registry, final boolean isGroupAddModEnaled) {
        this.registry = requireNonNull(registry);
        this.isGroupAddModEnaled = isGroupAddModEnaled;
    }

    @Override
    public void serialize(final GroupMod message, final ByteBuf outBuffer) {
        final int index = outBuffer.writerIndex();
        ByteBufUtils.writeOFHeader(MESSAGE_TYPE, message, outBuffer, EncodeConstants.EMPTY_LENGTH);
        if (isGroupAddModEnaled) {
            if (message.getCommand().equals(GroupModCommand.OFPGCADD)
                    || message.getCommand().equals(GroupModCommand.OFPGCMODIFY)) {
                outBuffer.writeShort(OFPGC_ADD_OR_MOD);
            } else {
                outBuffer.writeShort(message.getCommand().getIntValue());
            }
        } else {
            outBuffer.writeShort(message.getCommand().getIntValue());
        }
        outBuffer.writeByte(message.getType().getIntValue());
        outBuffer.writeZero(PADDING_IN_GROUP_MOD_MESSAGE);
        outBuffer.writeInt(message.getGroupId().getValue().intValue());
        serializerBuckets(message.getBucketsList(), outBuffer);
        ByteBufUtils.updateOFHeaderLength(outBuffer, index);
    }

    private void serializerBuckets(final List<BucketsList> buckets, final ByteBuf outBuffer) {
        if (buckets != null) {
            for (BucketsList currentBucket : buckets) {
                final int bucketLengthIndex = outBuffer.writerIndex();
                outBuffer.writeShort(EncodeConstants.EMPTY_LENGTH);
                outBuffer.writeShort(currentBucket.getWeight().shortValue());
                outBuffer.writeInt(currentBucket.getWatchPort().getValue().intValue());
                outBuffer.writeInt(currentBucket.getWatchGroup().intValue());
                outBuffer.writeZero(PADDING_IN_BUCKET);
                ListSerializer.serializeList(currentBucket.getAction(), TypeKeyMakerFactory
                        .createActionKeyMaker(EncodeConstants.OF13_VERSION_ID), registry, outBuffer);
                outBuffer.setShort(bucketLengthIndex, outBuffer.writerIndex() - bucketLengthIndex);
            }
        }
    }
}
