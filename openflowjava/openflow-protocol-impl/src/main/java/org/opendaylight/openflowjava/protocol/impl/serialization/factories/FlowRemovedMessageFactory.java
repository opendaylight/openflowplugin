/*
 * Copyright (c) 2015 NetIDE Consortium and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowjava.protocol.impl.serialization.factories;

import io.netty.buffer.ByteBuf;
import org.opendaylight.openflowjava.protocol.api.extensibility.OFSerializer;
import org.opendaylight.openflowjava.protocol.api.extensibility.SerializerRegistry;
import org.opendaylight.openflowjava.protocol.api.extensibility.SerializerRegistryInjector;
import org.opendaylight.openflowjava.protocol.api.keys.MessageTypeKey;
import org.opendaylight.openflowjava.protocol.api.util.EncodeConstants;
import org.opendaylight.openflowjava.util.ByteBufUtils;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.grouping.Match;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.FlowRemovedMessage;

/**
 * @author giuseppex.petralia@intel.com
 *
 */
public class FlowRemovedMessageFactory implements OFSerializer<FlowRemovedMessage>, SerializerRegistryInjector {

    private static final byte MESSAGE_TYPE = 11;
    private SerializerRegistry registry;

    @Override
    public void injectSerializerRegistry(SerializerRegistry serializerRegistry) {
        this.registry = serializerRegistry;
    }

    @Override
    public void serialize(FlowRemovedMessage message, ByteBuf outBuffer) {
        int index = outBuffer.writerIndex();
        ByteBufUtils.writeOFHeader(MESSAGE_TYPE, message, outBuffer, EncodeConstants.EMPTY_LENGTH);
        outBuffer.writeLong(message.getCookie().longValue());
        outBuffer.writeShort(message.getPriority());
        outBuffer.writeByte(message.getReason().getIntValue());
        outBuffer.writeByte(message.getTableId().getValue().byteValue());
        outBuffer.writeInt(message.getDurationSec().intValue());
        outBuffer.writeInt(message.getDurationNsec().intValue());
        outBuffer.writeShort(message.getIdleTimeout());
        outBuffer.writeShort(message.getHardTimeout());
        outBuffer.writeLong(message.getPacketCount().longValue());
        outBuffer.writeLong(message.getByteCount().longValue());
        OFSerializer<Match> matchSerializer = registry
                .<Match, OFSerializer<Match>> getSerializer(new MessageTypeKey<>(message.getVersion(), Match.class));
        matchSerializer.serialize(message.getMatch(), outBuffer);
        ByteBufUtils.updateOFHeaderLength(outBuffer, index);
    }

}
