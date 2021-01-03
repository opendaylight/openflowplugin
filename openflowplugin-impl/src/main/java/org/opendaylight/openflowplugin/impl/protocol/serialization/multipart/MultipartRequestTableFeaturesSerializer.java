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
import java.nio.charset.StandardCharsets;
import org.opendaylight.openflowjava.protocol.api.extensibility.OFSerializer;
import org.opendaylight.openflowjava.protocol.api.extensibility.SerializerRegistry;
import org.opendaylight.openflowjava.protocol.api.extensibility.SerializerRegistryInjector;
import org.opendaylight.openflowjava.protocol.api.keys.MessageTypeKey;
import org.opendaylight.openflowjava.protocol.api.util.EncodeConstants;
import org.opendaylight.openflowjava.util.ByteBufUtils;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.multipart.request.multipart.request.body.MultipartRequestTableFeatures;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table.feature.prop.type.TableFeaturePropType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table.features.TableFeatures;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table.features.table.features.TableProperties;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table.features.table.features.table.properties.TableFeatureProperties;

public class MultipartRequestTableFeaturesSerializer implements OFSerializer<MultipartRequestTableFeatures>,
        SerializerRegistryInjector {
    private static final byte PADDING_IN_MULTIPART_REQUEST_TABLE_FEATURES_BODY = 5;

    private SerializerRegistry registry = null;

    @Override
    public void serialize(final MultipartRequestTableFeatures multipartRequestTableFeatures, final ByteBuf byteBuf) {
        for (TableFeatures tableFeature : multipartRequestTableFeatures.nonnullTableFeatures().values()) {
            final int featureIndex = byteBuf.writerIndex();
            byteBuf.writeShort(EncodeConstants.EMPTY_LENGTH);
            byteBuf.writeByte(tableFeature.getTableId().byteValue());
            byteBuf.writeZero(PADDING_IN_MULTIPART_REQUEST_TABLE_FEATURES_BODY);

            final byte[] nameBytes = tableFeature.getName().getBytes(StandardCharsets.UTF_8);
            byteBuf.writeBytes(nameBytes);
            byteBuf.writeZero(32 - nameBytes.length);
            // FIXME: use writeUint() instead of .longValue()/intValue() here
            byteBuf.writeLong(tableFeature.getMetadataMatch().longValue());
            byteBuf.writeLong(tableFeature.getMetadataWrite().longValue());
            byteBuf.writeInt(ByteBufUtils.fillBitMask(0, tableFeature.getConfig().getDEPRECATEDMASK()));
            byteBuf.writeInt(tableFeature.getMaxEntries().intValue());
            serializeProperties(tableFeature.getTableProperties(), byteBuf);
            byteBuf.setShort(featureIndex, byteBuf.writerIndex() - featureIndex);
        }
    }

    @SuppressWarnings("unchecked")
    private void serializeProperties(final TableProperties tableProperties, final ByteBuf byteBuf) {
        if (tableProperties != null) {
            for (TableFeatureProperties property : tableProperties.nonnullTableFeatureProperties().values()) {
                final Class<? extends TableFeaturePropType> clazz =
                    (Class<? extends TableFeaturePropType>) property.getTableFeaturePropType().implementedInterface();

                registry.<TableFeaturePropType, OFSerializer<TableFeaturePropType>>getSerializer(
                        new MessageTypeKey<>(EncodeConstants.OF13_VERSION_ID, clazz))
                            .serialize(property.getTableFeaturePropType(), byteBuf);
            }
        }
    }

    @Override
    public void injectSerializerRegistry(final SerializerRegistry serializerRegistry) {
        registry = requireNonNull(serializerRegistry);
    }
}
