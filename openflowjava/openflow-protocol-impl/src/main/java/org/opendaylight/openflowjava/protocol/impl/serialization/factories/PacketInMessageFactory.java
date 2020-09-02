/*
 * Copyright (c) 2015 NetIDE Consortium and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowjava.protocol.impl.serialization.factories;

import static java.util.Objects.requireNonNull;

import io.netty.buffer.ByteBuf;
import org.opendaylight.openflowjava.protocol.api.extensibility.OFSerializer;
import org.opendaylight.openflowjava.protocol.api.extensibility.SerializerLookup;
import org.opendaylight.openflowjava.protocol.api.keys.MessageTypeKey;
import org.opendaylight.openflowjava.protocol.api.util.EncodeConstants;
import org.opendaylight.openflowjava.util.ByteBufUtils;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.grouping.Match;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.PacketInMessage;

/**
 * Translates PacketIn messages.
 */
public class PacketInMessageFactory implements OFSerializer<PacketInMessage> {
    private static final byte PADDING = 2;
    private static final byte MESSAGE_TYPE = 10;

    private final SerializerLookup registry;

    public PacketInMessageFactory(final SerializerLookup registry) {
        this.registry = requireNonNull(registry);
    }

    @Override
    public void serialize(final PacketInMessage message, final ByteBuf outBuffer) {
        ByteBufUtils.writeOFHeader(MESSAGE_TYPE, message, outBuffer, EncodeConstants.EMPTY_LENGTH);
        outBuffer.writeInt(message.getBufferId().intValue());
        outBuffer.writeShort(message.getTotalLen().intValue());
        outBuffer.writeByte(message.getReason().getIntValue());
        outBuffer.writeByte(message.getTableId().getValue().byteValue());
        outBuffer.writeLong(message.getCookie().longValue());
        OFSerializer<Match> matchSerializer = registry.getSerializer(
                new MessageTypeKey<>(message.getVersion().toJava(), Match.class));
        matchSerializer.serialize(message.getMatch(), outBuffer);
        outBuffer.writeZero(PADDING);

        byte[] data = message.getData();

        if (data != null) {
            outBuffer.writeBytes(data);
        }
        ByteBufUtils.updateOFHeaderLength(outBuffer);
    }
}
