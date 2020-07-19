/*
 * Copyright (c) 2015 NetIDE Consortium and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowjava.protocol.impl.deserialization.factories;

import static org.opendaylight.yangtools.yang.common.netty.ByteBufUtils.readUint16;
import static org.opendaylight.yangtools.yang.common.netty.ByteBufUtils.readUint32;

import io.netty.buffer.ByteBuf;
import java.util.ArrayList;
import java.util.List;
import org.opendaylight.openflowjava.protocol.api.extensibility.DeserializerRegistry;
import org.opendaylight.openflowjava.protocol.api.extensibility.DeserializerRegistryInjector;
import org.opendaylight.openflowjava.protocol.api.extensibility.OFDeserializer;
import org.opendaylight.openflowjava.protocol.api.util.EncodeConstants;
import org.opendaylight.openflowjava.protocol.impl.util.CodeKeyMaker;
import org.opendaylight.openflowjava.protocol.impl.util.CodeKeyMakerFactory;
import org.opendaylight.openflowjava.protocol.impl.util.ListDeserializer;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.actions.grouping.Action;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.GroupId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.GroupModCommand;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.GroupType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.PortNumber;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.GroupModInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.GroupModInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.buckets.grouping.BucketsList;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.buckets.grouping.BucketsListBuilder;

/**
 * Translates GroupModInput messages.
 *
 * @author giuseppex.petralia@intel.com
 */
public class GroupModInputMessageFactory implements OFDeserializer<GroupModInput>, DeserializerRegistryInjector {

    private DeserializerRegistry registry;
    private static final byte PADDING = 1;
    private static final byte PADDING_IN_BUCKETS_HEADER = 4;
    private static final byte BUCKETS_HEADER_LENGTH = 16;

    @Override
    public void injectDeserializerRegistry(DeserializerRegistry deserializerRegistry) {
        registry = deserializerRegistry;
    }

    @Override
    public GroupModInput deserialize(ByteBuf rawMessage) {
        GroupModInputBuilder builder = new GroupModInputBuilder()
                .setVersion(EncodeConstants.OF_VERSION_1_3)
                .setXid(readUint32(rawMessage))
                .setCommand(GroupModCommand.forValue(rawMessage.readUnsignedShort()))
                .setType(GroupType.forValue(rawMessage.readUnsignedByte()));
        rawMessage.skipBytes(PADDING);
        builder.setGroupId(new GroupId(readUint32(rawMessage)));
        List<BucketsList> bucketsList = new ArrayList<>();
        while (rawMessage.readableBytes() > 0) {
            BucketsListBuilder bucketsBuilder = new BucketsListBuilder();
            final int bucketsLength = rawMessage.readUnsignedShort();
            bucketsBuilder.setWeight(readUint16(rawMessage));
            bucketsBuilder.setWatchPort(new PortNumber(readUint32(rawMessage)));
            bucketsBuilder.setWatchGroup(readUint32(rawMessage));
            rawMessage.skipBytes(PADDING_IN_BUCKETS_HEADER);
            CodeKeyMaker keyMaker = CodeKeyMakerFactory.createActionsKeyMaker(EncodeConstants.OF13_VERSION_ID);
            List<Action> actions = ListDeserializer.deserializeList(EncodeConstants.OF13_VERSION_ID,
                    bucketsLength - BUCKETS_HEADER_LENGTH, rawMessage, keyMaker, registry);
            bucketsBuilder.setAction(actions);
            bucketsList.add(bucketsBuilder.build());
        }
        builder.setBucketsList(bucketsList);
        return builder.build();
    }
}
