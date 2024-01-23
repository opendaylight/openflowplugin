/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.protocol.deserialization.multipart;

import static org.opendaylight.yangtools.yang.common.netty.ByteBufUtils.readUint32;

import io.netty.buffer.ByteBuf;
import org.opendaylight.openflowjava.protocol.api.extensibility.DeserializerRegistry;
import org.opendaylight.openflowjava.protocol.api.extensibility.DeserializerRegistryInjector;
import org.opendaylight.openflowjava.protocol.api.extensibility.OFDeserializer;
import org.opendaylight.openflowjava.protocol.api.keys.ExperimenterIdTypeDeserializerKey;
import org.opendaylight.openflowjava.protocol.api.keys.MessageTypeKey;
import org.opendaylight.openflowjava.protocol.api.util.EncodeConstants;
import org.opendaylight.openflowjava.util.ExperimenterDeserializerKeyFactory;
import org.opendaylight.openflowplugin.extension.api.ConvertorMessageFromOFJava;
import org.opendaylight.openflowplugin.extension.api.path.MessagePath;
import org.opendaylight.openflowplugin.openflow.md.core.session.OFSessionUtil;
import org.opendaylight.yang.gen.v1.urn.opendaylight.multipart.types.rev170112.multipart.reply.MultipartReplyBody;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.experimenter.core.ExperimenterDataOfChoice;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.experimenter.types.rev151020.experimenter.core.message.ExperimenterMessageOfChoice;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.experimenter.types.rev151020.multipart.reply.multipart.reply.body.MultipartReplyExperimenterBuilder;
import org.opendaylight.yangtools.yang.common.Uint32;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MultipartReplyExperimenterDeserializer implements OFDeserializer<MultipartReplyBody>,
        DeserializerRegistryInjector {
    private static final Logger LOG = LoggerFactory.getLogger(MultipartReplyExperimenterDeserializer.class);
    private DeserializerRegistry registry;

    @Override
    @SuppressWarnings("unchecked")
    public MultipartReplyBody deserialize(final ByteBuf message) {
        final MultipartReplyExperimenterBuilder builder = new MultipartReplyExperimenterBuilder();
        final Uint32 expId = readUint32(message);
        final long expType = message.readUnsignedInt();

        try {
            final OFDeserializer<ExperimenterMessageOfChoice> deserializer = registry
                    .getDeserializer(new ExperimenterIdTypeDeserializerKey(
                            EncodeConstants.OF_VERSION_1_3, expId, expType, ExperimenterMessageOfChoice.class));

            builder.setExperimenterMessageOfChoice(deserializer.deserialize(message));
        } catch (ClassCastException | IllegalStateException es) {
            final OFDeserializer<ExperimenterDataOfChoice> deserializer = registry.getDeserializer(
                    ExperimenterDeserializerKeyFactory.createMultipartReplyMessageDeserializerKey(
                            EncodeConstants.OF_VERSION_1_3, expId, expType));

            final ExperimenterDataOfChoice data = deserializer.deserialize(message);
            final MessageTypeKey<? extends ExperimenterDataOfChoice> key = new MessageTypeKey<>(
                    EncodeConstants.OF_VERSION_1_3,
                    (Class<? extends ExperimenterDataOfChoice>) data.implementedInterface());

            final ConvertorMessageFromOFJava<ExperimenterDataOfChoice, MessagePath> convertor = OFSessionUtil
                    .getExtensionConvertorProvider()
                    .getMessageConverter(key);

            builder.setExperimenterMessageOfChoice(convertor.convert(data, MessagePath.MPMESSAGE_RPC_OUTPUT));
        }

        return builder.build();
    }

    @Override
    public void injectDeserializerRegistry(final DeserializerRegistry deserializerRegistry) {
        registry = deserializerRegistry;
    }
}
