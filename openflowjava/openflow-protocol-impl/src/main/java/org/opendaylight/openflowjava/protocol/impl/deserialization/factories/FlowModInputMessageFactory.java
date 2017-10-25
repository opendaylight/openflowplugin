/*
 * Copyright (c) 2015 NetIDE Consortium and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowjava.protocol.impl.deserialization.factories;

import io.netty.buffer.ByteBuf;
import java.math.BigInteger;
import java.util.List;
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
 * Translates FlowModInput messages
 */
public class FlowModInputMessageFactory implements OFDeserializer<FlowModInput>, DeserializerRegistryInjector {

    private static final byte PADDING = 2;
    private DeserializerRegistry registry;

    @Override
    public FlowModInput deserialize(ByteBuf rawMessage) {
        FlowModInputBuilder builder = new FlowModInputBuilder();
        builder.setVersion((short) EncodeConstants.OF13_VERSION_ID);
        builder.setXid(rawMessage.readUnsignedInt());
        byte[] cookie = new byte[EncodeConstants.SIZE_OF_LONG_IN_BYTES];
        rawMessage.readBytes(cookie);
        builder.setCookie(new BigInteger(1, cookie));
        byte[] cookie_mask = new byte[EncodeConstants.SIZE_OF_LONG_IN_BYTES];
        rawMessage.readBytes(cookie_mask);
        builder.setCookieMask(new BigInteger(1, cookie_mask));
        builder.setTableId(new TableId((long) rawMessage.readUnsignedByte()));
        builder.setCommand(FlowModCommand.forValue(rawMessage.readUnsignedByte()));
        builder.setIdleTimeout(rawMessage.readUnsignedShort());
        builder.setHardTimeout(rawMessage.readUnsignedShort());
        builder.setPriority(rawMessage.readUnsignedShort());
        builder.setBufferId(rawMessage.readUnsignedInt());
        builder.setOutPort(new PortNumber(rawMessage.readUnsignedInt()));
        builder.setOutGroup(rawMessage.readUnsignedInt());
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

    private static FlowModFlags createFlowModFlagsFromBitmap(int input) {
        final Boolean _oFPFFSENDFLOWREM = (input & (1 << 0)) > 0;
        final Boolean _oFPFFCHECKOVERLAP = (input & (1 << 1)) > 0;
        final Boolean _oFPFFRESETCOUNTS = (input & (1 << 2)) > 0;
        final Boolean _oFPFFNOPKTCOUNTS = (input & (1 << 3)) > 0;
        final Boolean _oFPFFNOBYTCOUNTS = (input & (1 << 4)) > 0;
        return new FlowModFlags(_oFPFFCHECKOVERLAP, _oFPFFNOBYTCOUNTS, _oFPFFNOPKTCOUNTS, _oFPFFRESETCOUNTS,
                _oFPFFSENDFLOWREM);
    }

    @Override
    public void injectDeserializerRegistry(DeserializerRegistry deserializerRegistry) {
        registry = deserializerRegistry;
    }
}