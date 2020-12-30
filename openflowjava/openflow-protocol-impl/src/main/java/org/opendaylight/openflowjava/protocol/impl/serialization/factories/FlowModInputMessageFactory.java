/*
 * Copyright (c) 2013 Pantheon Technologies s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowjava.protocol.impl.serialization.factories;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.netty.buffer.ByteBuf;
import java.util.Objects;
import org.opendaylight.openflowjava.protocol.api.extensibility.OFSerializer;
import org.opendaylight.openflowjava.protocol.api.extensibility.SerializerRegistry;
import org.opendaylight.openflowjava.protocol.api.extensibility.SerializerRegistryInjector;
import org.opendaylight.openflowjava.protocol.api.keys.MessageTypeKey;
import org.opendaylight.openflowjava.protocol.api.util.EncodeConstants;
import org.opendaylight.openflowjava.protocol.impl.util.ListSerializer;
import org.opendaylight.openflowjava.protocol.impl.util.TypeKeyMaker;
import org.opendaylight.openflowjava.protocol.impl.util.TypeKeyMakerFactory;
import org.opendaylight.openflowjava.util.ByteBufUtils;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.instruction.rev130731.instructions.grouping.Instruction;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.FlowModFlags;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.grouping.Match;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.FlowMod;

/**
 * Translates FlowMod messages. OF protocol versions: 1.3.
 *
 * @author timotej.kubas
 * @author michal.polkorab
 */
public class FlowModInputMessageFactory implements OFSerializer<FlowMod>, SerializerRegistryInjector {
    private static final byte MESSAGE_TYPE = 14;
    private static final byte PADDING_IN_FLOW_MOD_MESSAGE = 2;
    private static final TypeKeyMaker<Instruction> INSTRUCTION_KEY_MAKER =
            TypeKeyMakerFactory.createInstructionKeyMaker(EncodeConstants.OF13_VERSION_ID);
    private SerializerRegistry registry;

    @Override
    @SuppressFBWarnings("UWF_FIELD_NOT_INITIALIZED_IN_CONSTRUCTOR") // FB doesn't recognize Objects.requireNonNull
    public void serialize(final FlowMod message, final ByteBuf outBuffer) {
        Objects.requireNonNull(registry);

        final int index = outBuffer.writerIndex();
        ByteBufUtils.writeOFHeader(MESSAGE_TYPE, message, outBuffer, EncodeConstants.EMPTY_LENGTH);
        outBuffer.writeLong(message.getCookie().longValue());
        outBuffer.writeLong(message.getCookieMask().longValue());
        outBuffer.writeByte(message.getTableId().getValue().byteValue());
        outBuffer.writeByte(message.getCommand().getIntValue());
        outBuffer.writeShort(message.getIdleTimeout().toJava());
        outBuffer.writeShort(message.getHardTimeout().toJava());
        outBuffer.writeShort(message.getPriority().toJava());
        outBuffer.writeInt(message.getBufferId().intValue());
        outBuffer.writeInt(message.getOutPort().getValue().intValue());
        outBuffer.writeInt(message.getOutGroup().intValue());
        outBuffer.writeShort(createFlowModFlagsBitmask(message.getFlags()));
        outBuffer.writeZero(PADDING_IN_FLOW_MOD_MESSAGE);
        registry.<Match, OFSerializer<Match>>getSerializer(
            new MessageTypeKey<>(message.getVersion().toJava(), Match.class)) .serialize(message.getMatch(), outBuffer);
        ListSerializer.serializeList(message.getInstruction(), INSTRUCTION_KEY_MAKER, registry, outBuffer);
        ByteBufUtils.updateOFHeaderLength(outBuffer, index);
    }

    @Override
    public void injectSerializerRegistry(final SerializerRegistry serializerRegistry) {
        this.registry = serializerRegistry;
    }

    private static int createFlowModFlagsBitmask(final FlowModFlags flags) {
        return ByteBufUtils.fillBitMask(0,
                flags.getOFPFFSENDFLOWREM(),
                flags.getOFPFFCHECKOVERLAP(),
                flags.getOFPFFRESETCOUNTS(),
                flags.getOFPFFNOPKTCOUNTS(),
                flags.getOFPFFNOBYTCOUNTS());
    }

}
