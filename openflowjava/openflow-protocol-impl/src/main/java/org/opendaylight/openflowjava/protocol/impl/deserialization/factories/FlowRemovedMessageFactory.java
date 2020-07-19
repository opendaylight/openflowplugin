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
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.FlowRemovedReason;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.TableId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.grouping.Match;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.FlowRemovedMessage;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.FlowRemovedMessageBuilder;

/**
 * Translates FlowRemoved messages.
 *
 * @author michal.polkorab
 * @author timotej.kubas
 */
public class FlowRemovedMessageFactory implements OFDeserializer<FlowRemovedMessage>,
        DeserializerRegistryInjector {

    private DeserializerRegistry registry;

    @Override
    @SuppressFBWarnings("UWF_FIELD_NOT_INITIALIZED_IN_CONSTRUCTOR") // FB doesn't recognize Objects.requireNonNull
    public FlowRemovedMessage deserialize(ByteBuf rawMessage) {
        Objects.requireNonNull(registry);

        FlowRemovedMessageBuilder builder = new FlowRemovedMessageBuilder()
                .setVersion(EncodeConstants.OF_VERSION_1_3)
                .setXid(readUint32(rawMessage))
                .setCookie(readUint64(rawMessage))
                .setPriority(readUint16(rawMessage))
                .setReason(FlowRemovedReason.forValue(rawMessage.readUnsignedByte()))
                .setTableId(new TableId(readUint8(rawMessage).toUint32()))
                .setDurationSec(readUint32(rawMessage))
                .setDurationNsec(readUint32(rawMessage))
                .setIdleTimeout(readUint16(rawMessage))
                .setHardTimeout(readUint16(rawMessage))
                .setPacketCount(readUint64(rawMessage))
                .setByteCount(readUint64(rawMessage));
        OFDeserializer<Match> matchDeserializer = registry.getDeserializer(new MessageCodeKey(
                EncodeConstants.OF13_VERSION_ID, EncodeConstants.EMPTY_VALUE, Match.class));
        builder.setMatch(matchDeserializer.deserialize(rawMessage));
        return builder.build();
    }

    @Override
    public void injectDeserializerRegistry(DeserializerRegistry deserializerRegistry) {
        registry = deserializerRegistry;
    }
}
