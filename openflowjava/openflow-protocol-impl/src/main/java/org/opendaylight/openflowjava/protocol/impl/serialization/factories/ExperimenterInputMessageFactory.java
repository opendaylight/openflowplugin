/*
 * Copyright (c) 2013 Pantheon Technologies s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowjava.protocol.impl.serialization.factories;

import io.netty.buffer.ByteBuf;
import org.opendaylight.openflowjava.protocol.api.extensibility.OFSerializer;
import org.opendaylight.openflowjava.protocol.api.extensibility.SerializerRegistry;
import org.opendaylight.openflowjava.protocol.api.extensibility.SerializerRegistryInjector;
import org.opendaylight.openflowjava.protocol.api.util.EncodeConstants;
import org.opendaylight.openflowjava.util.ByteBufUtils;
import org.opendaylight.openflowjava.util.ExperimenterSerializerKeyFactory;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.ExperimenterOfMessage;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.experimenter.core.ExperimenterDataOfChoice;

/**
 * Translates Experimenter messages (both: symmetric request and single reply)
 * @author michal.polkorab
 */
public class ExperimenterInputMessageFactory implements OFSerializer<ExperimenterOfMessage>,
        SerializerRegistryInjector {

    private SerializerRegistry registry;

    /** Code type of symmetric Experimenter message */
    private static final byte MESSAGE_TYPE = 4;

    @Override
    public void serialize(ExperimenterOfMessage message, ByteBuf outBuffer) {
        long expId = message.getExperimenter().getValue();
        OFSerializer<ExperimenterDataOfChoice> serializer = registry.getSerializer(
                ExperimenterSerializerKeyFactory.createExperimenterMessageSerializerKey(
                        EncodeConstants.OF13_VERSION_ID, expId, message.getExpType().longValue()));
        ByteBufUtils.writeOFHeader(MESSAGE_TYPE, message, outBuffer, EncodeConstants.EMPTY_LENGTH);

        // write experimenterId and type
        outBuffer.writeInt(message.getExperimenter().getValue().intValue());
        outBuffer.writeInt(message.getExpType().intValue());

        serializer.serialize(message.getExperimenterDataOfChoice(), outBuffer);
        ByteBufUtils.updateOFHeaderLength(outBuffer);
    }

    @Override
    public void injectSerializerRegistry(SerializerRegistry serializerRegistry) {
        this.registry = serializerRegistry;
    }
}