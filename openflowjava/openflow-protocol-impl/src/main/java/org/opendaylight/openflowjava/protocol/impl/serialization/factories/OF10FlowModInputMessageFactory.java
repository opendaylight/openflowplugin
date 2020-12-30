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
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.actions.grouping.Action;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.FlowModFlagsV10;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.v10.grouping.MatchV10;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.FlowModInput;

/**
 * Translates FlowMod messages.
 *
 * @author michal.polkorab
 */
public class OF10FlowModInputMessageFactory implements OFSerializer<FlowModInput>, SerializerRegistryInjector {

    private static final byte MESSAGE_TYPE = 14;
    private static final TypeKeyMaker<Action> ACTION_KEY_MAKER =
            TypeKeyMakerFactory.createActionKeyMaker(EncodeConstants.OF10_VERSION_ID);
    private SerializerRegistry registry;

    @Override
    @SuppressFBWarnings("UWF_FIELD_NOT_INITIALIZED_IN_CONSTRUCTOR") // FB doesn't recognize Objects.requireNonNull
    public void serialize(final FlowModInput message, final ByteBuf outBuffer) {
        Objects.requireNonNull(registry);

        ByteBufUtils.writeOFHeader(MESSAGE_TYPE, message, outBuffer, EncodeConstants.EMPTY_LENGTH);
        OFSerializer<MatchV10> matchSerializer = registry.getSerializer(new MessageTypeKey<>(
                message.getVersion().toJava(), MatchV10.class));
        matchSerializer.serialize(message.getMatchV10(), outBuffer);
        outBuffer.writeLong(message.getCookie().longValue());
        outBuffer.writeShort(message.getCommand().getIntValue());
        outBuffer.writeShort(message.getIdleTimeout().intValue());
        outBuffer.writeShort(message.getHardTimeout().intValue());
        outBuffer.writeShort(message.getPriority().toJava());
        outBuffer.writeInt(message.getBufferId().intValue());
        outBuffer.writeShort(message.getOutPort().getValue().intValue());
        outBuffer.writeShort(createFlowModFlagsBitmask(message.getFlagsV10()));
        ListSerializer.serializeList(message.getAction(), ACTION_KEY_MAKER, registry, outBuffer);
        ByteBufUtils.updateOFHeaderLength(outBuffer);
    }

    private static int createFlowModFlagsBitmask(final FlowModFlagsV10 flags) {
        return ByteBufUtils.fillBitMask(0,
                flags.getOFPFFSENDFLOWREM(),
                flags.getOFPFFCHECKOVERLAP(),
                flags.getOFPFFEMERG());
    }

    @Override
    public void injectSerializerRegistry(final SerializerRegistry serializerRegistry) {
        this.registry = serializerRegistry;
    }
}
