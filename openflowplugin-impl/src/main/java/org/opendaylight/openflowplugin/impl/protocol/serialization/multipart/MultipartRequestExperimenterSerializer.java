/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.protocol.serialization.multipart;

import static java.util.Objects.requireNonNull;

import io.netty.buffer.ByteBuf;
import org.opendaylight.openflowjava.protocol.api.extensibility.OFSerializer;
import org.opendaylight.openflowjava.protocol.api.extensibility.SerializerRegistry;
import org.opendaylight.openflowjava.protocol.api.extensibility.SerializerRegistryInjector;
import org.opendaylight.openflowjava.protocol.api.keys.MessageTypeKey;
import org.opendaylight.openflowjava.protocol.api.util.EncodeConstants;
import org.opendaylight.openflowjava.util.ExperimenterSerializerKeyFactory;
import org.opendaylight.openflowplugin.api.OFConstants;
import org.opendaylight.openflowplugin.extension.api.ConverterMessageToOFJava;
import org.opendaylight.openflowplugin.extension.api.ConvertorData;
import org.opendaylight.openflowplugin.extension.api.TypeVersionKey;
import org.opendaylight.openflowplugin.extension.api.exception.ConversionException;
import org.opendaylight.openflowplugin.openflow.md.core.session.OFSessionUtil;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.experimenter.core.ExperimenterDataOfChoice;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.experimenter.types.rev151020.experimenter.core.message.ExperimenterMessageOfChoice;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.experimenter.types.rev151020.multipart.request.multipart.request.body.MultipartRequestExperimenter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MultipartRequestExperimenterSerializer implements OFSerializer<MultipartRequestExperimenter>,
        SerializerRegistryInjector {
    private static final Logger LOG = LoggerFactory.getLogger(MultipartRequestExperimenterSerializer.class);

    private SerializerRegistry registry = null;

    @Override
    public void serialize(final MultipartRequestExperimenter input, final ByteBuf byteBuf) {
        final OFSerializer<ExperimenterMessageOfChoice> serializer = registry.getSerializer(new MessageTypeKey<>(
            EncodeConstants.OF_VERSION_1_3,
            input.getExperimenterMessageOfChoice().implementedInterface()));

        try {
            serializer.serialize(input.getExperimenterMessageOfChoice(), byteBuf);
        } catch (ClassCastException | IllegalStateException e) {
            LOG.debug("Failed to serialize {}, attempting to convert", input, e);
            serializeConverted(input, byteBuf);
        }
    }

    private void serializeConverted(final MultipartRequestExperimenter input, final ByteBuf byteBuf) {
        @SuppressWarnings("unchecked")
        final ConverterMessageToOFJava<ExperimenterMessageOfChoice, ExperimenterDataOfChoice, ConvertorData> converter =
            OFSessionUtil.getExtensionConvertorProvider()
                .<ExperimenterMessageOfChoice, ExperimenterDataOfChoice, ConvertorData>getMessageConverter(
                    new TypeVersionKey<>((Class<ExperimenterMessageOfChoice>)
                        input.getExperimenterMessageOfChoice().implementedInterface(), OFConstants.OFP_VERSION_1_3));
        if (converter != null) {
            final OFSerializer<ExperimenterDataOfChoice> serializer = registry.getSerializer(
                ExperimenterSerializerKeyFactory.createMultipartRequestSerializerKey(
                    EncodeConstants.OF_VERSION_1_3, converter.getExperimenterId().getValue().toJava(),
                    converter.getType()));

            try {
                serializer.serialize(converter.convert(input.getExperimenterMessageOfChoice(), null), byteBuf);
            } catch (ConversionException e) {
                throw new IllegalStateException(e);
            }
        }
    }

    @Override
    public void injectSerializerRegistry(final SerializerRegistry serializerRegistry) {
        registry = requireNonNull(serializerRegistry);
    }
}
