/*
 * Copyright (c) 2015 NetIDE Consortium and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowjava.protocol.impl.deserialization.factories;

import static java.util.Objects.requireNonNull;
import static org.opendaylight.yangtools.yang.common.netty.ByteBufUtils.readUint16;
import static org.opendaylight.yangtools.yang.common.netty.ByteBufUtils.readUint32;
import static org.opendaylight.yangtools.yang.common.netty.ByteBufUtils.readUint64;
import static org.opendaylight.yangtools.yang.common.netty.ByteBufUtils.readUint8;

import io.netty.buffer.ByteBuf;
import org.opendaylight.openflowjava.protocol.api.extensibility.DeserializerRegistry;
import org.opendaylight.openflowjava.protocol.api.extensibility.DeserializerRegistryInjector;
import org.opendaylight.openflowjava.protocol.api.extensibility.OFDeserializer;
import org.opendaylight.openflowjava.protocol.api.keys.MessageCodeKey;
import org.opendaylight.openflowjava.protocol.api.util.EncodeConstants;
import org.opendaylight.openflowjava.protocol.impl.util.CodeKeyMakerFactory;
import org.opendaylight.openflowjava.protocol.impl.util.ListDeserializer;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.FlowModCommand;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.FlowModFlags;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.PortNumber;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.TableId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.grouping.Match;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.FlowModInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.FlowModInputBuilder;

/**
 * Translates FlowModInput messages.
 */
public class FlowModInputMessageFactory implements OFDeserializer<FlowModInput>, DeserializerRegistryInjector {
    private static final byte PADDING = 2;

    private DeserializerRegistry registry = null;

    @Override
    public FlowModInput deserialize(final ByteBuf rawMessage) {
        FlowModInputBuilder builder = new FlowModInputBuilder()
                .setVersion(EncodeConstants.OF_VERSION_1_3)
                .setXid(readUint32(rawMessage))
                .setCookie(readUint64(rawMessage))
                .setCookieMask(readUint64(rawMessage))
                .setTableId(new TableId(readUint8(rawMessage).toUint32()))
                .setCommand(FlowModCommand.forValue(rawMessage.readUnsignedByte()))
                .setIdleTimeout(readUint16(rawMessage))
                .setHardTimeout(readUint16(rawMessage))
                .setPriority(readUint16(rawMessage))
                .setBufferId(readUint32(rawMessage))
                .setOutPort(new PortNumber(readUint32(rawMessage)))
                .setOutGroup(readUint32(rawMessage))
                .setFlags(createFlowModFlagsFromBitmap(rawMessage.readUnsignedShort()));
        rawMessage.skipBytes(PADDING);
        OFDeserializer<Match> matchDeserializer = registry.getDeserializer(
                new MessageCodeKey(EncodeConstants.OF_VERSION_1_3, EncodeConstants.EMPTY_VALUE, Match.class));
        return builder
            .setMatch(matchDeserializer.deserialize(rawMessage))
            .setInstruction(ListDeserializer.deserializeList(EncodeConstants.OF13_VERSION_ID,
                rawMessage.readableBytes(), rawMessage,
                CodeKeyMakerFactory.createInstructionsKeyMaker(EncodeConstants.OF_VERSION_1_3), registry))
            .build();
    }

    @SuppressWarnings("checkstyle:AbbreviationAsWordInName")
    private static FlowModFlags createFlowModFlagsFromBitmap(final int input) {
        final Boolean _oFPFFSENDFLOWREM = (input & 1 << 0) != 0;
        final Boolean _oFPFFCHECKOVERLAP = (input & 1 << 1) != 0;
        final Boolean _oFPFFRESETCOUNTS = (input & 1 << 2) != 0;
        final Boolean _oFPFFNOPKTCOUNTS = (input & 1 << 3) != 0;
        final Boolean _oFPFFNOBYTCOUNTS = (input & 1 << 4) != 0;
        return new FlowModFlags(_oFPFFCHECKOVERLAP, _oFPFFNOBYTCOUNTS, _oFPFFNOPKTCOUNTS, _oFPFFRESETCOUNTS,
                _oFPFFSENDFLOWREM);
    }

    @Override
    public void injectDeserializerRegistry(final DeserializerRegistry deserializerRegistry) {
        registry = requireNonNull(deserializerRegistry);
    }
}
