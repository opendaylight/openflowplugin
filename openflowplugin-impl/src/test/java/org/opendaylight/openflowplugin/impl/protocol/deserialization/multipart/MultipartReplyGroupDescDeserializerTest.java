/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.impl.protocol.deserialization.multipart;

import static org.junit.Assert.assertEquals;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.UnpooledByteBufAllocator;
import org.junit.Test;
import org.opendaylight.openflowjava.protocol.impl.util.ActionConstants;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.statistics.rev131111.multipart.reply.multipart.reply.body.MultipartReplyGroupDesc;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.GroupTypes;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.MultipartType;

public class MultipartReplyGroupDescDeserializerTest extends AbstractMultipartDeserializerTest{
    private static final byte PADDING_IN_GROUP_DESC_HEADER = 1;
    private static final byte PADDING_IN_BUCKETS_HEADER = 4;
    private static final short ITEM_LENGTH = 32;
    private static final int GROUP_TYPE = GroupTypes.GroupSelect.getIntValue();
    private static final int GROUP_ID = 1;
    private static final short BUCKET_LENGTH = 24;
    private static final short WEIGHT = 2;
    private static final int WATCH_PORT = 3;
    private static final int WATCH_GROUP = 4;

    @Test
    public void deserialize() throws Exception {
        ByteBuf buffer = UnpooledByteBufAllocator.DEFAULT.buffer();
        buffer.writeShort(ITEM_LENGTH);
        buffer.writeByte(GROUP_TYPE);
        buffer.writeZero(PADDING_IN_GROUP_DESC_HEADER);
        buffer.writeInt(GROUP_ID);
        buffer.writeShort(BUCKET_LENGTH);
        buffer.writeShort(WEIGHT);
        buffer.writeInt(WATCH_PORT);
        buffer.writeInt(WATCH_GROUP);
        buffer.writeZero(PADDING_IN_BUCKETS_HEADER);

        // POP PBB action
        buffer.writeShort(ActionConstants.POP_PBB_CODE);
        buffer.writeShort(ActionConstants.GENERAL_ACTION_LENGTH);
        buffer.writeZero(ActionConstants.PADDING_IN_ACTION_HEADER);

        final MultipartReplyGroupDesc reply = (MultipartReplyGroupDesc) deserializeMultipart(buffer);

        assertEquals(GROUP_ID, reply.getGroupDescStats().get(0).getGroupId().getValue().intValue());
        assertEquals(WEIGHT, reply.getGroupDescStats().get(0).getBuckets().getBucket().get(0).getWeight().intValue());
        assertEquals(WATCH_PORT, reply.getGroupDescStats().get(0).getBuckets().getBucket().get(0).getWatchPort().intValue());
        assertEquals(WATCH_GROUP, reply.getGroupDescStats().get(0).getBuckets().getBucket().get(0).getWatchGroup().intValue());
        assertEquals(GROUP_TYPE, reply.getGroupDescStats().get(0).getGroupType().getIntValue());
        assertEquals(0, buffer.readableBytes());
    }

    @Override
    protected int getType() {
        return MultipartType.OFPMPGROUPDESC.getIntValue();
    }
}