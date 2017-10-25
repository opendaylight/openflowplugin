/*
 * Copyright (c) 2013 Pantheon Technologies s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowjava.protocol.impl.deserialization.factories;

import io.netty.buffer.ByteBuf;

import java.math.BigInteger;

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
 * Translates FlowRemoved messages (OpenFlow v1.0)
 * @author michal.polkorab
 */
public class OF10FlowRemovedMessageFactory implements OFDeserializer<FlowRemovedMessage>,
        DeserializerRegistryInjector {

    private static final byte PADDING_IN_FLOW_REMOVED_MESSAGE = 1;
    private static final byte PADDING_IN_FLOW_REMOVED_MESSAGE_2 = 2;
    private DeserializerRegistry registry;

    @Override
    public FlowRemovedMessage deserialize(ByteBuf rawMessage) {
        FlowRemovedMessageBuilder builder = new FlowRemovedMessageBuilder();
        builder.setVersion((short) EncodeConstants.OF10_VERSION_ID);
        builder.setXid(rawMessage.readUnsignedInt());
        OFDeserializer<MatchV10> matchDeserializer = registry.getDeserializer(
                new MessageCodeKey(EncodeConstants.OF10_VERSION_ID, EncodeConstants.EMPTY_VALUE, MatchV10.class));
        builder.setMatchV10(matchDeserializer.deserialize(rawMessage));
        byte[] cookie = new byte[EncodeConstants.SIZE_OF_LONG_IN_BYTES];
        rawMessage.readBytes(cookie);
        builder.setCookie(new BigInteger(1, cookie));
        builder.setPriority(rawMessage.readUnsignedShort());
        builder.setReason(FlowRemovedReason.forValue(rawMessage.readUnsignedByte()));
        rawMessage.skipBytes(PADDING_IN_FLOW_REMOVED_MESSAGE);
        builder.setDurationSec(rawMessage.readUnsignedInt());
        builder.setDurationNsec(rawMessage.readUnsignedInt());
        builder.setIdleTimeout(rawMessage.readUnsignedShort());
        rawMessage.skipBytes(PADDING_IN_FLOW_REMOVED_MESSAGE_2);
        byte[] packetCount = new byte[EncodeConstants.SIZE_OF_LONG_IN_BYTES];
        rawMessage.readBytes(packetCount);
        builder.setPacketCount(new BigInteger(1, packetCount));
        byte[] byteCount = new byte[EncodeConstants.SIZE_OF_LONG_IN_BYTES];
        rawMessage.readBytes(byteCount);
        builder.setByteCount(new BigInteger(1, byteCount));
        return builder.build();
    }

    @Override
    public void injectDeserializerRegistry(DeserializerRegistry deserializerRegistry) {
        registry = deserializerRegistry;
    }
}
