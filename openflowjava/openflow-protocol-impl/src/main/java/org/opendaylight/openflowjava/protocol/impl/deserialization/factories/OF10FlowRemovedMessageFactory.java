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

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.netty.buffer.ByteBuf;
import java.util.Objects;
import org.opendaylight.openflowjava.protocol.api.extensibility.DeserializerRegistry;
import org.opendaylight.openflowjava.protocol.api.extensibility.DeserializerRegistryInjector;
import org.opendaylight.openflowjava.protocol.api.extensibility.OFDeserializer;
import org.opendaylight.openflowjava.protocol.api.keys.MessageCodeKey;
import org.opendaylight.openflowjava.protocol.api.util.EncodeConstants;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.FlowRemovedReason;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.v10.grouping.MatchV10;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.FlowRemovedMessage;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.FlowRemovedMessageBuilder;

/**
 * Translates FlowRemoved messages (OpenFlow v1.0).
 *
 * @author michal.polkorab
 */
public class OF10FlowRemovedMessageFactory implements OFDeserializer<FlowRemovedMessage>,
        DeserializerRegistryInjector {
    private static final byte PADDING_IN_FLOW_REMOVED_MESSAGE = 1;
    private static final byte PADDING_IN_FLOW_REMOVED_MESSAGE_2 = 2;

    private DeserializerRegistry registry;

    @Override
    @SuppressFBWarnings("UWF_FIELD_NOT_INITIALIZED_IN_CONSTRUCTOR") // FB doesn't recognize Objects.requireNonNull
    public FlowRemovedMessage deserialize(ByteBuf rawMessage) {
        Objects.requireNonNull(registry);

        FlowRemovedMessageBuilder builder = new FlowRemovedMessageBuilder()
                .setVersion(EncodeConstants.OF_VERSION_1_0)
                .setXid(readUint32(rawMessage));
        OFDeserializer<MatchV10> matchDeserializer = registry.getDeserializer(
                new MessageCodeKey(EncodeConstants.OF10_VERSION_ID, EncodeConstants.EMPTY_VALUE, MatchV10.class));
        builder.setMatchV10(matchDeserializer.deserialize(rawMessage));
        builder.setCookie(readUint64(rawMessage));
        builder.setPriority(readUint16(rawMessage));
        builder.setReason(FlowRemovedReason.forValue(rawMessage.readUnsignedByte()));
        rawMessage.skipBytes(PADDING_IN_FLOW_REMOVED_MESSAGE);
        builder.setDurationSec(readUint32(rawMessage));
        builder.setDurationNsec(readUint32(rawMessage));
        builder.setIdleTimeout(readUint16(rawMessage));
        rawMessage.skipBytes(PADDING_IN_FLOW_REMOVED_MESSAGE_2);
        builder.setPacketCount(readUint64(rawMessage));
        builder.setByteCount(readUint64(rawMessage));
        return builder.build();
    }

    @Override
    public void injectDeserializerRegistry(DeserializerRegistry deserializerRegistry) {
        registry = deserializerRegistry;
    }
}
