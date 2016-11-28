/*
 * Copyright (c) 2016 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.impl.protocol.serialization.messages;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.UnpooledByteBufAllocator;
import java.util.Collections;
import java.util.List;
import org.junit.Test;
import org.opendaylight.openflowjava.protocol.api.keys.MessageTypeKey;
import org.opendaylight.openflowjava.protocol.api.util.EncodeConstants;
import org.opendaylight.openflowjava.protocol.impl.util.ActionConstants;
import org.opendaylight.openflowplugin.impl.protocol.serialization.AbstractSerializerTest;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Ipv4Prefix;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.SetNwSrcActionCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.set.nw.src.action._case.SetNwSrcActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.list.Action;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.list.ActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.list.ActionKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.address.address.Ipv4Builder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.BucketId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.GroupId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.GroupMessage;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.GroupMessageBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.GroupTypes;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.group.Buckets;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.group.BucketsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.group.buckets.BucketBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.group.buckets.BucketKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.GroupModCommand;

public class GroupMessageSerializerTest extends AbstractSerializerTest {
    private static final byte PADDING_IN_GROUP_MOD_MESSAGE = 1;
    private static final byte PADDING_IN_BUCKET = 4;

    private static final Long XID = 42L;
    private static final Short VERSION = EncodeConstants.OF13_VERSION_ID;
    private static final Boolean BARRIER = false;
    private static final GroupModCommand COMMAND = GroupModCommand.OFPGCADD;
    private static final String CONTAINER_NAME = "openflow:1";
    private static final GroupId GROUP_ID = new GroupId(41L);
    private static final String GROUP_NAME = "group41";
    private static final GroupTypes GROUP_TYPE = GroupTypes.GroupAll;

    private static final BucketId BUCKET_ID = new BucketId(40L);
    private static final BucketKey BUCKET_KEY = new BucketKey(BUCKET_ID);
    private static final Long BUCKET_WATCH_GROUP = 12L;
    private static final Long BUCKET_WATCH_PORT = 6L;
    private static final Integer BUCKET_WEIGHT = 50;
    private static final Integer ACTION_ORDER = 0;
    private static final ActionKey ACTION_KEY = new ActionKey(ACTION_ORDER);
    private static final Ipv4Prefix IPV4_PREFIX = new Ipv4Prefix("192.168.76.0/32");

    private static final List<Action> ACTIONS = Collections.singletonList(
            new ActionBuilder()
                    .setAction(new SetNwSrcActionCaseBuilder()
                            .setSetNwSrcAction(new SetNwSrcActionBuilder()
                                    .setAddress(new Ipv4Builder()
                                            .setIpv4Address(new Ipv4Prefix(IPV4_PREFIX))
                                            .build())
                                    .build())
                            .build())
                    .setOrder(ACTION_ORDER)
                    .setKey(ACTION_KEY)
                    .build()
    );

    private static final Buckets BUCKETS = new BucketsBuilder()
            .setBucket(Collections.singletonList(new BucketBuilder()
                    .setBucketId(BUCKET_ID)
                    .setKey(BUCKET_KEY)
                    .setWatchGroup(BUCKET_WATCH_GROUP)
                    .setWatchPort(BUCKET_WATCH_PORT)
                    .setWeight(BUCKET_WEIGHT)
                    .setAction(ACTIONS)
                    .build()))
            .build();

    private static final GroupMessage MESSAGE = new GroupMessageBuilder()
            .setBarrier(BARRIER)
            .setBuckets(BUCKETS)
            .setCommand(COMMAND)
            .setContainerName(CONTAINER_NAME)
            .setGroupId(GROUP_ID)
            .setGroupName(GROUP_NAME)
            .setGroupType(GROUP_TYPE)
            .setVersion(VERSION)
            .setXid(XID)
            .build();

    private GroupMessageSerializer serializer;

    @Override
    protected void init() {
        serializer = getRegistry().getSerializer(new MessageTypeKey<>(EncodeConstants.OF13_VERSION_ID, GroupMessage.class));
    }

    @Test
    public void testSerialize() throws Exception {
        final ByteBuf out = UnpooledByteBufAllocator.DEFAULT.buffer();
        serializer.serialize(MESSAGE, out);

        // Header
        assertEquals(out.readByte(), VERSION.shortValue());
        assertEquals(out.readByte(), serializer.getMessageType());
        out.readShort(); // LENGTH, TODO: assert it
        assertEquals(out.readInt(), XID.intValue());

        // Body
        assertEquals(out.readUnsignedShort(), COMMAND.getIntValue());
        assertEquals(out.readByte(), GROUP_TYPE.getIntValue());
        out.skipBytes(PADDING_IN_GROUP_MOD_MESSAGE);
        assertEquals(out.readUnsignedInt(), GROUP_ID.getValue().longValue());

        // Bucket
        out.readShort(); // Bucket length, TODO: assert it
        assertEquals(out.readUnsignedShort(), BUCKET_WEIGHT.intValue());
        assertEquals(out.readUnsignedInt(), BUCKET_WATCH_PORT.longValue());
        assertEquals(out.readUnsignedInt(), BUCKET_WATCH_GROUP.longValue());
        out.skipBytes(PADDING_IN_BUCKET);

        // Action
        assertEquals(out.readUnsignedShort(), ActionConstants.SET_FIELD_CODE);
        assertEquals(out.readUnsignedShort(), EncodeConstants.SIZE_OF_SHORT_IN_BYTES // Size of action type
                        + EncodeConstants.SIZE_OF_SHORT_IN_BYTES // Size of action length
                        + EncodeConstants.SIZE_OF_SHORT_IN_BYTES // Match entry OXM class
                        + EncodeConstants.SIZE_OF_BYTE_IN_BYTES // Match entry field and mask
                        + EncodeConstants.SIZE_OF_BYTE_IN_BYTES // Match entry length
                        + EncodeConstants.PADDING); // Size of set field (match entry)
        // Skip match entry header, we have tests for this elsewhere
        out.skipBytes(EncodeConstants.SIZE_OF_INT_IN_BYTES);

        // Actual match body
        byte[] addressBytes = new byte[4];
        out.readBytes(addressBytes);
        assertArrayEquals(addressBytes, new byte[] { (byte) 192, (byte) 168, 76, 0 });

        int paddingRemainder = out.readerIndex() % EncodeConstants.PADDING;

        if (paddingRemainder != 0) {
            out.skipBytes(EncodeConstants.PADDING - paddingRemainder);
        }

        assertEquals(out.readableBytes(), 0);
    }

}