/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.impl.protocol.serialization.multipart;

import io.netty.buffer.ByteBuf;
import java.util.Optional;
import org.opendaylight.openflowjava.protocol.api.extensibility.OFSerializer;
import org.opendaylight.openflowjava.protocol.api.extensibility.SerializerRegistry;
import org.opendaylight.openflowjava.protocol.api.extensibility.SerializerRegistryInjector;
import org.opendaylight.openflowjava.protocol.api.keys.MessageTypeKey;
import org.opendaylight.openflowjava.protocol.api.util.EncodeConstants;
import org.opendaylight.openflowjava.util.ExperimenterSerializerKeyFactory;
import org.opendaylight.openflowplugin.api.OFConstants;
import org.opendaylight.openflowplugin.extension.api.TypeVersionKey;
import org.opendaylight.openflowplugin.extension.api.exception.ConversionException;
import org.opendaylight.openflowplugin.openflow.md.core.session.OFSessionUtil;
import org.opendaylight.yang.gen.v1.urn.opendaylight.multipart.types.rev170112.multipart.request.MultipartRequestBody;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.experimenter.core.ExperimenterDataOfChoice;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.experimenter.types.rev151020.experimenter.core.message.ExperimenterMessageOfChoice;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.experimenter.types.rev151020.multipart.request.multipart.request.body.MultipartRequestExperimenter;

public class MultipartRequestExperimenterSerializer implements OFSerializer<MultipartRequestBody>, SerializerRegistryInjector {

    private SerializerRegistry registry;

    @Override
    @SuppressWarnings("unchecked")
    public void serialize(final MultipartRequestBody multipartRequestBody, final ByteBuf byteBuf) {
        final MultipartRequestExperimenter multipartRequestExperimenter = MultipartRequestExperimenter
            .class
            .cast(multipartRequestBody);

        try {
            final OFSerializer<ExperimenterMessageOfChoice> serializer = registry
                .getSerializer(new MessageTypeKey<>(
                    EncodeConstants.OF13_VERSION_ID,
                    multipartRequestExperimenter.getExperimenterMessageOfChoice().getImplementedInterface()));

            serializer.serialize(multipartRequestExperimenter.getExperimenterMessageOfChoice(), byteBuf);
        } catch (ClassCastException | IllegalStateException ex) {
            Optional
                .ofNullable(OFSessionUtil.getExtensionConvertorProvider().<ExperimenterMessageOfChoice, ExperimenterDataOfChoice>getMessageConverter(new TypeVersionKey<>(
                    (Class<ExperimenterMessageOfChoice>)multipartRequestExperimenter.getExperimenterMessageOfChoice().getImplementedInterface(),
                    OFConstants.OFP_VERSION_1_3)))
                .ifPresent(converter -> {
                    final OFSerializer<ExperimenterDataOfChoice> serializer = registry
                        .getSerializer(ExperimenterSerializerKeyFactory
                            .createMultipartRequestSerializerKey(
                                EncodeConstants.OF13_VERSION_ID,
                                converter.getExperimenterId().getValue(),
                                converter.getType()));

                    try {
                        serializer.serialize(converter.convert(multipartRequestExperimenter.getExperimenterMessageOfChoice()), byteBuf);
                    } catch (ConversionException e) {
                        throw new IllegalStateException(e);
                    }
                });
        }
    }

    @Override
    public void injectSerializerRegistry(final SerializerRegistry serializerRegistry) {
        registry = serializerRegistry;
    }

}
