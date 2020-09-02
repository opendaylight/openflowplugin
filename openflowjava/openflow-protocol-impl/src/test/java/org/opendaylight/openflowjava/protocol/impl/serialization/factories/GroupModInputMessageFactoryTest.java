/*
 * Copyright (c) 2013 Pantheon Technologies s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowjava.protocol.impl.serialization.factories;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.UnpooledByteBufAllocator;
import java.util.ArrayList;
import java.util.List;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.opendaylight.openflowjava.protocol.api.extensibility.SerializerRegistry;
import org.opendaylight.openflowjava.protocol.api.util.EncodeConstants;
import org.opendaylight.openflowjava.protocol.impl.serialization.SerializerRegistryImpl;
import org.opendaylight.openflowjava.protocol.impl.util.BufferHelper;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.GroupId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.GroupModCommand;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.GroupType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.PortNumber;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.GroupModInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.GroupModInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.buckets.grouping.BucketsList;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.buckets.grouping.BucketsListBuilder;

/**
 * Unit tests for GroupModInputMessageFactory.
 *
 * @author timotej.kubas
 */
public class GroupModInputMessageFactoryTest {
    private static final byte MESSAGE_TYPE = 15;
    private static final byte PADDING_IN_GROUP_MOD_MESSAGE = 1;
    private SerializerRegistry registry;
    private GroupModInputMessageFactory groupModFactory;

    /**
     * Initializes serializer registry and stores correct factory in field.
     */
    @Before
    public void startUp() {
        registry = new SerializerRegistryImpl();
        registry.init();
        groupModFactory = new GroupModInputMessageFactory(false);
    }

    /**
     * Testing of {@link GroupModInputMessageFactory} for correct translation from POJO.
     */
    @Test
    public void testGroupModInputMessage() throws Exception {
        GroupModInputBuilder builder = new GroupModInputBuilder();
        BufferHelper.setupHeader(builder, EncodeConstants.OF13_VERSION_ID);
        builder.setCommand(GroupModCommand.forValue(2));
        builder.setType(GroupType.forValue(3));
        builder.setGroupId(new GroupId(256L));
        List<BucketsList> exp = createBucketsList();
        builder.setBucketsList(exp);
        final GroupModInput message = builder.build();

        ByteBuf out = UnpooledByteBufAllocator.DEFAULT.buffer();

        // simulate parent message
        out.writeInt(1);
        out.writeZero(2);
        out.writeShort(3);

        groupModFactory.serialize(message, out);
        // read parent message
        out.readInt();
        out.skipBytes(2);
        out.readShort();

        BufferHelper.checkHeaderV13(out, MESSAGE_TYPE, 32);
        Assert.assertEquals("Wrong command", message.getCommand().getIntValue(), out.readUnsignedShort());
        Assert.assertEquals("Wrong type", message.getType().getIntValue(), out.readUnsignedByte());
        out.skipBytes(PADDING_IN_GROUP_MOD_MESSAGE);
        Assert.assertEquals("Wrong groupId", message.getGroupId().getValue().intValue(), out.readUnsignedInt());
        List<BucketsList> rec = createBucketsListFromBufer(out);
        Assert.assertArrayEquals("Wrong bucketList", exp.toArray(), rec.toArray());
    }

    private static List<BucketsList> createBucketsList() {
        final List<BucketsList> bucketsList = new ArrayList<>();
        BucketsListBuilder bucketsBuilder = new BucketsListBuilder();
        bucketsBuilder.setWeight(10);
        bucketsBuilder.setWatchPort(new PortNumber(65L));
        bucketsBuilder.setWatchGroup(22L);
        BucketsList bucket = bucketsBuilder.build();
        bucketsList.add(bucket);
        return bucketsList;
    }

    private static List<BucketsList> createBucketsListFromBufer(ByteBuf out) {
        final List<BucketsList> bucketsList = new ArrayList<>();
        BucketsListBuilder bucketsBuilder = new BucketsListBuilder();
        out.skipBytes(Short.BYTES);
        bucketsBuilder.setWeight(out.readUnsignedShort());
        bucketsBuilder.setWatchPort(new PortNumber(out.readUnsignedInt()));
        bucketsBuilder.setWatchGroup(out.readUnsignedInt());
        out.skipBytes(4);
        BucketsList bucket = bucketsBuilder.build();
        bucketsList.add(bucket);
        return bucketsList;
    }

    /**
     * Testing of {@link GroupModInputMessageFactory} for correct translation from POJO.
     */
    @Test
    public void testGroupModInputWithNoBuckets() throws Exception {
        GroupModInputBuilder builder = new GroupModInputBuilder();
        BufferHelper.setupHeader(builder, EncodeConstants.OF13_VERSION_ID);
        builder.setCommand(GroupModCommand.forValue(2));
        builder.setType(GroupType.forValue(3));
        builder.setGroupId(new GroupId(256L));
        GroupModInput message = builder.build();

        ByteBuf out = UnpooledByteBufAllocator.DEFAULT.buffer();
        groupModFactory.serialize(message, out);

        BufferHelper.checkHeaderV13(out, MESSAGE_TYPE, 16);
        Assert.assertEquals("Wrong command", message.getCommand().getIntValue(), out.readUnsignedShort());
        Assert.assertEquals("Wrong type", message.getType().getIntValue(), out.readUnsignedByte());
        out.skipBytes(PADDING_IN_GROUP_MOD_MESSAGE);
        Assert.assertEquals("Wrong groupId", message.getGroupId().getValue().intValue(), out.readUnsignedInt());
        Assert.assertTrue("Unexpected data", out.readableBytes() == 0);
    }
}
