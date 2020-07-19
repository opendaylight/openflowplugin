/*
 * Copyright (c) 2013 Pantheon Technologies s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowjava.protocol.impl.deserialization.factories;

import static org.opendaylight.yangtools.yang.common.netty.ByteBufUtils.readUint16;
import static org.opendaylight.yangtools.yang.common.netty.ByteBufUtils.readUint32;
import static org.opendaylight.yangtools.yang.common.netty.ByteBufUtils.readUint64;
import static org.opendaylight.yangtools.yang.common.netty.ByteBufUtils.readUint8;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.netty.buffer.ByteBuf;
import java.util.Objects;
import org.opendaylight.openflowjava.protocol.api.extensibility.DeserializerRegistry;
import org.opendaylight.openflowjava.protocol.api.extensibility.DeserializerRegistryInjector;
import org.opendaylight.openflowjava.protocol.api.extensibility.OFDeserializer;
import org.opendaylight.openflowjava.protocol.api.keys.MessageCodeKey;
import org.opendaylight.openflowjava.protocol.api.util.EncodeConstants;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.PacketInReason;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.TableId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.grouping.Match;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.PacketInMessage;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.PacketInMessageBuilder;

/**
 * Translates PacketIn messages.
 *
 * @author michal.polkorab
 * @author timotej.kubas
 */
public class PacketInMessageFactory implements OFDeserializer<PacketInMessage>,
        DeserializerRegistryInjector {

    private static final byte PADDING_IN_PACKET_IN_HEADER = 2;
    private static final MessageCodeKey MATCH_KEY = new MessageCodeKey(
            EncodeConstants.OF13_VERSION_ID, EncodeConstants.EMPTY_VALUE, Match.class);
    private DeserializerRegistry registry;

    @Override
    @SuppressFBWarnings("UWF_FIELD_NOT_INITIALIZED_IN_CONSTRUCTOR") // FB doesn't recognize Objects.requireNonNull
    public PacketInMessage deserialize(final ByteBuf rawMessage) {
        Objects.requireNonNull(registry);

        PacketInMessageBuilder builder = new PacketInMessageBuilder()
                .setVersion(EncodeConstants.OF_VERSION_1_3)
                .setXid(readUint32(rawMessage))
                .setBufferId(readUint32(rawMessage))
                .setTotalLen(readUint16(rawMessage))
                .setReason(PacketInReason.forValue(rawMessage.readUnsignedByte()))
                .setTableId(new TableId(readUint8(rawMessage).toUint32()))
                .setCookie(readUint64(rawMessage));
        OFDeserializer<Match> matchDeserializer = registry.getDeserializer(MATCH_KEY);
        builder.setMatch(matchDeserializer.deserialize(rawMessage));
        rawMessage.skipBytes(PADDING_IN_PACKET_IN_HEADER);
        byte[] data = new byte[rawMessage.readableBytes()];
        rawMessage.readBytes(data);
        builder.setData(data);
        return builder.build();
    }

    @Override
    public void injectDeserializerRegistry(final DeserializerRegistry deserializerRegistry) {
        registry = deserializerRegistry;
    }
}
