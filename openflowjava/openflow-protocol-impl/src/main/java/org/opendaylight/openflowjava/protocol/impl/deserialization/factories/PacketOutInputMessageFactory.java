/*
 * Copyright (c) 2015 NetIDE Consortium and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowjava.protocol.impl.deserialization.factories;

import io.netty.buffer.ByteBuf;
import java.util.List;
import org.opendaylight.openflowjava.protocol.api.extensibility.DeserializerRegistry;
import org.opendaylight.openflowjava.protocol.api.extensibility.DeserializerRegistryInjector;
import org.opendaylight.openflowjava.protocol.api.extensibility.OFDeserializer;
import org.opendaylight.openflowjava.protocol.api.util.EncodeConstants;
import org.opendaylight.openflowjava.protocol.impl.util.CodeKeyMaker;
import org.opendaylight.openflowjava.protocol.impl.util.CodeKeyMakerFactory;
import org.opendaylight.openflowjava.protocol.impl.util.ListDeserializer;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.actions.grouping.Action;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.PortNumber;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.PacketOutInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.PacketOutInputBuilder;

public class PacketOutInputMessageFactory implements OFDeserializer<PacketOutInput>, DeserializerRegistryInjector {
    private DeserializerRegistry registry;
    private final byte PADDING = 6;

    @Override
    public PacketOutInput deserialize(ByteBuf rawMessage) {
        PacketOutInputBuilder builder = new PacketOutInputBuilder();
        builder.setVersion((short) EncodeConstants.OF13_VERSION_ID);
        builder.setXid(rawMessage.readUnsignedInt());
        builder.setBufferId(rawMessage.readUnsignedInt());
        builder.setInPort(new PortNumber(rawMessage.readUnsignedInt()));
        int actions_len = rawMessage.readShort();
        rawMessage.skipBytes(PADDING);
        CodeKeyMaker keyMaker = CodeKeyMakerFactory.createActionsKeyMaker(EncodeConstants.OF13_VERSION_ID);
        List<Action> actions = ListDeserializer.deserializeList(EncodeConstants.OF13_VERSION_ID, actions_len,
                rawMessage, keyMaker, registry);
        builder.setAction(actions);
        byte[] data = new byte[rawMessage.readableBytes()];
        rawMessage.readBytes(data);
        if (data != null) {
            builder.setData(data);
        }
        return builder.build();
    }

    @Override
    public void injectDeserializerRegistry(DeserializerRegistry deserializerRegistry) {
        registry = deserializerRegistry;
    }
}