/*
 * Copyright (c) 2015 NetIDE Consortium and others.  All rights reserved.
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
import java.util.List;
import java.util.Objects;
import org.opendaylight.openflowjava.protocol.api.extensibility.DeserializerRegistry;
import org.opendaylight.openflowjava.protocol.api.extensibility.DeserializerRegistryInjector;
import org.opendaylight.openflowjava.protocol.api.extensibility.OFDeserializer;
import org.opendaylight.openflowjava.protocol.api.keys.MessageCodeKey;
import org.opendaylight.openflowjava.protocol.api.util.EncodeConstants;
import org.opendaylight.openflowjava.protocol.impl.util.CodeKeyMaker;
import org.opendaylight.openflowjava.protocol.impl.util.CodeKeyMakerFactory;
import org.opendaylight.openflowjava.protocol.impl.util.ListDeserializer;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.instruction.rev130731.instructions.grouping.Instruction;
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
    private DeserializerRegistry registry;

    @Override
    @SuppressFBWarnings("UWF_FIELD_NOT_INITIALIZED_IN_CONSTRUCTOR") // FB doesn't recognize Objects.requireNonNull
    public FlowModInput deserialize(ByteBuf rawMessage) {
        Objects.requireNonNull(registry);

        FlowModInputBuilder builder = new FlowModInputBuilder();
        builder.setVersion((short) EncodeConstants.OF13_VERSION_ID);
        builder.setXid(readUint32(rawMessage));
        builder.setCookie(readUint64(rawMessage));
        builder.setCookieMask(readUint64(rawMessage));
        builder.setTableId(new TableId(readUint8(rawMessage).toUint32()));
        builder.setCommand(FlowModCommand.forValue(rawMessage.readUnsignedByte()));
        builder.setIdleTimeout(readUint16(rawMessage));
        builder.setHardTimeout(readUint16(rawMessage));
        builder.setPriority(readUint16(rawMessage));
        builder.setBufferId(readUint32(rawMessage));
        builder.setOutPort(new PortNumber(readUint32(rawMessage)));
        builder.setOutGroup(readUint32(rawMessage));
        builder.setFlags(createFlowModFlagsFromBitmap(rawMessage.readUnsignedShort()));
        rawMessage.skipBytes(PADDING);
        OFDeserializer<Match> matchDeserializer = registry.getDeserializer(
                new MessageCodeKey(EncodeConstants.OF13_VERSION_ID, EncodeConstants.EMPTY_VALUE, Match.class));
        builder.setMatch(matchDeserializer.deserialize(rawMessage));
        CodeKeyMaker keyMaker = CodeKeyMakerFactory.createInstructionsKeyMaker(EncodeConstants.OF13_VERSION_ID);
        List<Instruction> instructions = ListDeserializer.deserializeList(EncodeConstants.OF13_VERSION_ID,
                rawMessage.readableBytes(), rawMessage, keyMaker, registry);
        builder.setInstruction(instructions);
        return builder.build();
    }

    @SuppressWarnings("checkstyle:AbbreviationAsWordInName")
    private static FlowModFlags createFlowModFlagsFromBitmap(int input) {
        final Boolean _oFPFFSENDFLOWREM = (input & 1 << 0) != 0;
        final Boolean _oFPFFCHECKOVERLAP = (input & 1 << 1) != 0;
        final Boolean _oFPFFRESETCOUNTS = (input & 1 << 2) != 0;
        final Boolean _oFPFFNOPKTCOUNTS = (input & 1 << 3) != 0;
        final Boolean _oFPFFNOBYTCOUNTS = (input & 1 << 4) != 0;
        return new FlowModFlags(_oFPFFCHECKOVERLAP, _oFPFFNOBYTCOUNTS, _oFPFFNOPKTCOUNTS, _oFPFFRESETCOUNTS,
                _oFPFFSENDFLOWREM);
    }

    @Override
    public void injectDeserializerRegistry(DeserializerRegistry deserializerRegistry) {
        registry = deserializerRegistry;
    }
}
