/*
 * Copyright (c) 2016 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.impl.protocol.deserialization.messages;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.opendaylight.openflowjava.protocol.api.util.EncodeConstants;
import org.opendaylight.openflowjava.protocol.impl.util.ActionConstants;
import org.opendaylight.openflowplugin.impl.protocol.deserialization.AbstractDeserializerTest;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.PopPbbActionCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.GroupMessage;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.GroupTypes;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.group.buckets.Bucket;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.GroupModCommand;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.UnpooledByteBufAllocator;

public class GroupMessageDeserializerTest extends AbstractDeserializerTest {

    private static final byte PADDING = 1;
    private static final byte PADDING_IN_BUCKETS_HEADER = 4;

    private static final int TYPE = 15;
    private static final int XID = 42;
    private static final GroupModCommand COMMAND = GroupModCommand.OFPGCADD;
    private static final GroupTypes GROUP_TYPE = GroupTypes.GroupAll;
    private static final int GROUP_ID = 26;
    private static final short WEIGHT = 50;
    private static final int WATCH_PORT = 22;
    private static final int WATCH_GROUP = 25;

    private ByteBuf buffer;

    @Override
    protected void init() {
        buffer = UnpooledByteBufAllocator.DEFAULT.buffer();
    }

    @Test
    public void deserialize() throws Exception {
        // Group header
        buffer.writeByte(TYPE);
        buffer.writeShort(EncodeConstants.EMPTY_LENGTH);
        buffer.writeInt(XID);
        buffer.writeShort(COMMAND.getIntValue());
        buffer.writeByte(GROUP_TYPE.getIntValue());
        buffer.writeZero(PADDING);
        buffer.writeInt(GROUP_ID);

        // Buckets header
        int index = buffer.writerIndex();
        buffer.writeShort(EncodeConstants.EMPTY_LENGTH);
        buffer.writeShort(WEIGHT);
        buffer.writeInt(WATCH_PORT);
        buffer.writeInt(WATCH_GROUP);
        buffer.writeZero(PADDING_IN_BUCKETS_HEADER);

        // POP PBB action
        buffer.writeShort(ActionConstants.POP_PBB_CODE);
        buffer.writeShort(ActionConstants.GENERAL_ACTION_LENGTH);
        buffer.writeZero(ActionConstants.PADDING_IN_ACTION_HEADER);

        // Count total length of buckets
        buffer.setShort(index, buffer.writerIndex() - index);

        // Deserialize and check everything
        final GroupMessage message = (GroupMessage) getFactory()
            .deserialize(buffer, EncodeConstants.OF13_VERSION_ID);

        assertEquals(XID, message.getXid().intValue());
        assertEquals(COMMAND.getIntValue(), message.getCommand().getIntValue());
        assertEquals(GROUP_TYPE.getIntValue(), message.getGroupType().getIntValue());
        assertEquals(1, message.getBuckets().getBucket().size());

        final Bucket bucket = message.getBuckets().getBucket().get(0);
        assertEquals(WEIGHT, bucket.getWeight().shortValue());
        assertEquals(WATCH_PORT, bucket.getWatchPort().intValue());
        assertEquals(WATCH_GROUP, bucket.getWatchGroup().intValue());
        assertEquals(1, bucket.getAction().size());
        assertEquals(PopPbbActionCase.class, bucket.getAction().get(0).getAction().getImplementedInterface());
    }

}
