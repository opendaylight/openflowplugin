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
import java.util.Optional;
import org.opendaylight.openflowjava.protocol.api.extensibility.OFSerializer;
import org.opendaylight.openflowjava.protocol.api.extensibility.SerializerLookup;
import org.opendaylight.openflowjava.protocol.api.keys.MessageTypeKey;
import org.opendaylight.openflowjava.protocol.api.util.EncodeConstants;
import org.opendaylight.openflowjava.util.ExperimenterSerializerKeyFactory;
import org.opendaylight.openflowplugin.api.OFConstants;
import org.opendaylight.openflowplugin.extension.api.ConvertorData;
import org.opendaylight.openflowplugin.extension.api.TypeVersionKey;
import org.opendaylight.openflowplugin.extension.api.exception.ConversionException;
import org.opendaylight.openflowplugin.openflow.md.core.session.OFSessionUtil;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.experimenter.core.ExperimenterDataOfChoice;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.experimenter.types.rev151020.experimenter.core.message.ExperimenterMessageOfChoice;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.experimenter.types.rev151020.multipart.request.multipart.request.body.MultipartRequestExperimenter;

public class MultipartRequestExperimenterSerializer implements OFSerializer<MultipartRequestExperimenter> {
    private final SerializerLookup registry;

    public MultipartRequestExperimenterSerializer(final SerializerLookup registry) {
        this.registry = requireNonNull(registry);
    }

    @Override
    @SuppressWarnings("unchecked")
    public void serialize(final MultipartRequestExperimenter multipartRequestExperimenter, final ByteBuf byteBuf) {
        try {
            final OFSerializer<ExperimenterMessageOfChoice> serializer = registry.getSerializer(new MessageTypeKey<>(
                    EncodeConstants.OF13_VERSION_ID,
                            multipartRequestExperimenter.getExperimenterMessageOfChoice().implementedInterface()));

            serializer.serialize(multipartRequestExperimenter.getExperimenterMessageOfChoice(), byteBuf);
        } catch (ClassCastException | IllegalStateException ex) {
            Optional
                    .ofNullable(OFSessionUtil.getExtensionConvertorProvider().<ExperimenterMessageOfChoice,
                            ExperimenterDataOfChoice, ConvertorData>getMessageConverter(new TypeVersionKey<>(
                            (Class<ExperimenterMessageOfChoice>) multipartRequestExperimenter
                                    .getExperimenterMessageOfChoice().implementedInterface(),
                            OFConstants.OFP_VERSION_1_3)))
                    .ifPresent(converter -> {
                        final OFSerializer<ExperimenterDataOfChoice> serializer = registry.getSerializer(
                            ExperimenterSerializerKeyFactory.createMultipartRequestSerializerKey(
                                EncodeConstants.OF13_VERSION_ID, converter.getExperimenterId().getValue().toJava(),
                                converter.getType()));

                        try {
                            serializer.serialize(converter.convert(multipartRequestExperimenter
                                    .getExperimenterMessageOfChoice(), null), byteBuf);
                        } catch (ConversionException e) {
                            throw new IllegalStateException(e);
                        }
                    });
        }
    }
}
