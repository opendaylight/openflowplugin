/*
 * Copyright (c) 2013 Pantheon Technologies s.r.o. and others. All rights reserved.
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
import org.opendaylight.openflowjava.protocol.api.util.EncodeConstants;
import org.opendaylight.openflowjava.protocol.impl.util.ListSerializer;
import org.opendaylight.openflowjava.protocol.impl.util.TypeKeyMakerFactory;
import org.opendaylight.openflowjava.util.ByteBufUtils;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.PacketOutInput;

/**
 * Translates PacketOut messages.
 *
 * @author michal.polkorab
 */
public class OF10PacketOutInputMessageFactory implements OFSerializer<PacketOutInput> {
    private static final byte MESSAGE_TYPE = 13;

    private final SerializerLookup registry;

    public OF10PacketOutInputMessageFactory(final SerializerLookup registry) {
        this.registry = requireNonNull(registry);
    }

    @Override
    public void serialize(final PacketOutInput message, final ByteBuf outBuffer) {
        ByteBufUtils.writeOFHeader(MESSAGE_TYPE, message, outBuffer, EncodeConstants.EMPTY_LENGTH);
        outBuffer.writeInt(message.getBufferId().intValue());
        outBuffer.writeShort(message.getInPort().getValue().intValue());
        int actionsLengthIndex = outBuffer.writerIndex();
        outBuffer.writeShort(EncodeConstants.EMPTY_LENGTH);
        int actionsStartIndex = outBuffer.writerIndex();
        ListSerializer.serializeList(message.getAction(), TypeKeyMakerFactory
                .createActionKeyMaker(EncodeConstants.OF10_VERSION_ID), registry, outBuffer);
        outBuffer.setShort(actionsLengthIndex, outBuffer.writerIndex() - actionsStartIndex);
        byte[] data = message.getData();
        if (data != null) {
            outBuffer.writeBytes(data);
        }
        ByteBufUtils.updateOFHeaderLength(outBuffer);
    }
}
