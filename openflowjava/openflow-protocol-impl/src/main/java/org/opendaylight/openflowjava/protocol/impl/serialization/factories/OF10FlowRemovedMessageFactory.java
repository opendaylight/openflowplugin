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
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.v10.grouping.MatchV10;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.FlowRemovedMessage;

/**
 * Translates FlowRemoved messages.
 *
 * @author giuseppex.petralia@intel.com
 */
public class OF10FlowRemovedMessageFactory implements OFSerializer<FlowRemovedMessage> {
    private static final byte MESSAGE_TYPE = 11;
    private static final byte PADDING = 1;

    private final SerializerLookup registry;

    public OF10FlowRemovedMessageFactory(final SerializerLookup registry) {
        this.registry = requireNonNull(registry);
    }

    @Override
    public void serialize(final FlowRemovedMessage message, final ByteBuf outBuffer) {
        ByteBufUtils.writeOFHeader(MESSAGE_TYPE, message, outBuffer, EncodeConstants.EMPTY_LENGTH);

        OFSerializer<MatchV10> matchSerializer = registry
                .getSerializer(new MessageTypeKey<>(message.getVersion().toJava(), MatchV10.class));

        matchSerializer.serialize(message.getMatchV10(), outBuffer);

        outBuffer.writeLong(message.getCookie().longValue());
        outBuffer.writeShort(message.getPriority().toJava());
        outBuffer.writeByte(message.getReason().getIntValue());
        outBuffer.writeZero(PADDING);
        outBuffer.writeInt(message.getDurationSec().intValue());
        outBuffer.writeInt(message.getDurationNsec().intValue());
        outBuffer.writeShort(message.getIdleTimeout().toJava());
        outBuffer.writeZero(PADDING);
        outBuffer.writeZero(PADDING);
        outBuffer.writeLong(message.getPacketCount().longValue());
        outBuffer.writeLong(message.getByteCount().longValue());
        ByteBufUtils.updateOFHeaderLength(outBuffer);
    }
}
