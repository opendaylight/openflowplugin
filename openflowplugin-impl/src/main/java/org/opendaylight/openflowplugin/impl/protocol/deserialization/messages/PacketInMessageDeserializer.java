/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.protocol.deserialization.messages;

import static java.util.Objects.requireNonNull;

import io.netty.buffer.ByteBuf;
import java.math.BigInteger;
import org.opendaylight.openflowjava.protocol.api.extensibility.DeserializerRegistry;
import org.opendaylight.openflowjava.protocol.api.extensibility.OFDeserializer;
import org.opendaylight.openflowjava.protocol.api.keys.MessageCodeKey;
import org.opendaylight.openflowjava.protocol.api.util.EncodeConstants;
import org.opendaylight.openflowplugin.extension.api.path.MatchPath;
import org.opendaylight.openflowplugin.impl.protocol.deserialization.key.MessageCodeMatchKey;
import org.opendaylight.openflowplugin.impl.util.MatchUtil;
import org.opendaylight.openflowplugin.impl.util.PacketInUtil;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.FlowCookie;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.Match;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.PacketInReason;
import org.opendaylight.yang.gen.v1.urn.opendaylight.packet.service.rev130709.PacketInMessage;
import org.opendaylight.yang.gen.v1.urn.opendaylight.packet.service.rev130709.PacketInMessageBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.TableId;

public class PacketInMessageDeserializer implements OFDeserializer<PacketInMessage> {
    private static final byte PADDING_IN_PACKET_IN_HEADER = 2;
    private static final MessageCodeKey MATCH_KEY = new MessageCodeMatchKey(EncodeConstants.OF13_VERSION_ID,
            EncodeConstants.EMPTY_VALUE, Match.class,
            MatchPath.PACKET_IN_MESSAGE_MATCH);

    private final DeserializerRegistry registry;

    public PacketInMessageDeserializer(final DeserializerRegistry registry) {
        this.registry = requireNonNull(registry);
    }

    @Override
    public PacketInMessage deserialize(final ByteBuf message) {
        final PacketInMessageBuilder packetInMessageBuilder = new PacketInMessageBuilder()
                .setVersion((short) EncodeConstants.OF13_VERSION_ID)
                .setXid(message.readUnsignedInt());

        // We are ignoring buffer id and total len as it is not specified in OpenFlowPlugin models
        message.readUnsignedInt();
        message.readUnsignedShort();

        packetInMessageBuilder
                .setPacketInReason(PacketInUtil
                        .getMdSalPacketInReason(PacketInReason
                                .forValue(message.readUnsignedByte())))
                .setTableId(new TableId(message.readUnsignedByte()));

        final byte[] cookie = new byte[Long.BYTES];
        message.readBytes(cookie);

        packetInMessageBuilder
                .setFlowCookie(new FlowCookie(new BigInteger(1, cookie)));

        final OFDeserializer<Match> matchDeserializer = registry.getDeserializer(MATCH_KEY);

        packetInMessageBuilder.setMatch(MatchUtil.transformMatch(matchDeserializer.deserialize(message),
                org.opendaylight.yang.gen.v1.urn.opendaylight.packet.service.rev130709.packet.in.message
                        .Match.class));

        message.skipBytes(PADDING_IN_PACKET_IN_HEADER);
        final byte[] data = new byte[message.readableBytes()];
        message.readBytes(data);

        return packetInMessageBuilder
                .setPayload(data)
                .build();
    }
}
