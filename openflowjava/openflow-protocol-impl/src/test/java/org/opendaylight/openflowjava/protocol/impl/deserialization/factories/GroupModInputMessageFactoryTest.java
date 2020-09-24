/*
 * Copyright (c) 2015 NetIDE Consortium and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowjava.protocol.impl.deserialization.factories;

import io.netty.buffer.ByteBuf;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.opendaylight.openflowjava.protocol.api.extensibility.DeserializerRegistry;
import org.opendaylight.openflowjava.protocol.api.extensibility.OFDeserializer;
import org.opendaylight.openflowjava.protocol.api.keys.MessageCodeKey;
import org.opendaylight.openflowjava.protocol.api.util.EncodeConstants;
import org.opendaylight.openflowjava.protocol.impl.deserialization.DeserializerRegistryImpl;
import org.opendaylight.openflowjava.protocol.impl.util.BufferHelper;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.GroupId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.GroupModCommand;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.GroupType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.PortNumber;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.GroupModInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.buckets.grouping.BucketsList;
import org.opendaylight.yangtools.yang.common.Uint32;

/**
 * Unit tests for GroupModInputMessageFactory.
 *
 * @author giuseppex.petralia@intel.com
 */
public class GroupModInputMessageFactoryTest {
    private OFDeserializer<GroupModInput> factory;

    @Before
    public void startUp() {
        DeserializerRegistry desRegistry = new DeserializerRegistryImpl();
        desRegistry.init();
        factory = desRegistry
                .getDeserializer(new MessageCodeKey(EncodeConstants.OF13_VERSION_ID, 15, GroupModInput.class));
    }

    @Test
    public void test() {
        ByteBuf bb = BufferHelper
                .buildBuffer("00 02 03 00 00 00 01 00 00 10 00 0a 00 " + "00 00 41 00 00 00 16 00 00 00 00");
        GroupModInput deserializedMessage = BufferHelper.deserialize(factory, bb);
        BufferHelper.checkHeaderV13(deserializedMessage);

        // Test Message
        Assert.assertEquals("Wrong command", GroupModCommand.forValue(2), deserializedMessage.getCommand());
        Assert.assertEquals("Wrong type", GroupType.forValue(3), deserializedMessage.getType());
        Assert.assertEquals("Wrong group id", new GroupId(Uint32.valueOf(256)), deserializedMessage.getGroupId());
        BucketsList bucket = deserializedMessage.getBucketsList().get(0);
        Assert.assertEquals("Wrong weight", 10, bucket.getWeight().intValue());
        Assert.assertEquals("Wrong watch port", new PortNumber(Uint32.valueOf(65)), bucket.getWatchPort());
        Assert.assertEquals("Wrong watch group", 22L, bucket.getWatchGroup().longValue());
    }

}
