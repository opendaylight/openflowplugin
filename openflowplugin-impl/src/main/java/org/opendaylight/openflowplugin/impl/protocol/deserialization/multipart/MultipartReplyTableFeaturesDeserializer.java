/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.impl.protocol.deserialization.multipart;

import io.netty.buffer.ByteBuf;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import org.opendaylight.openflowjava.protocol.api.extensibility.OFDeserializer;
import org.opendaylight.openflowjava.protocol.api.util.EncodeConstants;
import org.opendaylight.openflowjava.util.ByteBufUtils;
import org.opendaylight.yang.gen.v1.urn.opendaylight.multipart.types.rev170112.multipart.reply.MultipartReplyBody;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.TableFeaturesPropType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.service.rev131026.multipart.reply.multipart.reply.body.MultipartReplyTableFeaturesBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.TableConfig;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table.feature.prop.type.table.feature.prop.type.NextTableBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table.feature.prop.type.table.feature.prop.type.NextTableMissBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table.feature.prop.type.table.feature.prop.type.next.table.TablesBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table.feature.prop.type.table.feature.prop.type.next.table.miss.TablesMissBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table.features.TableFeatures;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table.features.TableFeaturesBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table.features.table.features.TableProperties;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table.features.table.features.TablePropertiesBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table.features.table.features.table.properties.TableFeatureProperties;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table.features.table.features.table.properties.TableFeaturePropertiesBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MultipartReplyTableFeaturesDeserializer implements OFDeserializer<MultipartReplyBody> {
    private static final Logger LOG = LoggerFactory.getLogger(MultipartReplyTableFeaturesDeserializer.class);
    private static final byte PADDING_IN_MULTIPART_REPLY_TABLE_FEATURES = 5;
    private static final byte MAX_TABLE_NAME_LENGTH = 32;
    private static final byte MULTIPART_REPLY_TABLE_FEATURES_STRUCTURE_LENGTH = 64;
    private static final byte COMMON_PROPERTY_LENGTH = 4;

    @Override
    public MultipartReplyBody deserialize(ByteBuf message) {
        final MultipartReplyTableFeaturesBuilder builder = new MultipartReplyTableFeaturesBuilder();
        final List<TableFeatures> items = new ArrayList<>();

        while (message.readableBytes() > 0) {
            final int itemLength = message.readUnsignedShort();
            final TableFeaturesBuilder itemBuilder = new TableFeaturesBuilder()
                .setTableId(message.readUnsignedByte());

            message.skipBytes(PADDING_IN_MULTIPART_REPLY_TABLE_FEATURES);

            items.add(itemBuilder
                    .setName(ByteBufUtils.decodeNullTerminatedString(message, MAX_TABLE_NAME_LENGTH))
                    .setMetadataMatch(BigInteger.valueOf(message.readLong()))
                    .setMetadataWrite(BigInteger.valueOf(message.readLong()))
                    .setConfig(readTableConfig(message))
                    .setMaxEntries(message.readUnsignedInt())
                    .setTableProperties(readTableProperties(message,
                            itemLength - MULTIPART_REPLY_TABLE_FEATURES_STRUCTURE_LENGTH))
                    .build());
        }

        return builder
            .setTableFeatures(items)
            .build();
    }

    private static final TableConfig readTableConfig(ByteBuf message) {
        final long input = message.readUnsignedInt();
        final boolean deprecated = (input & 3) != 0;

        return new TableConfig(deprecated);
    }

    private static final TableProperties readTableProperties(ByteBuf message, int length) {
        final List<TableFeatureProperties> items = new ArrayList<>();
        int tableFeaturesLength = length;

        while (tableFeaturesLength > 0) {
            final int propStartIndex = message.readerIndex();
            final TableFeaturesPropType propType = TableFeaturesPropType.forValue(message.readUnsignedShort());
            int propertyLength = message.readUnsignedShort();
            final int paddingRemainder = propertyLength % EncodeConstants.PADDING;
            tableFeaturesLength -= propertyLength;

            final TableFeaturePropertiesBuilder propBuilder = new TableFeaturePropertiesBuilder();

            switch (propType) {
                // Instructions
                case OFPTFPTINSTRUCTIONS:

                case OFPTFPTINSTRUCTIONSMISS:
                    break;
                case OFPTFPTNEXTTABLES:
                    propBuilder.setTableFeaturePropType(new NextTableBuilder()
                            .setTables(new TablesBuilder()
                                .setTableIds(readNextTableIds(message, propertyLength))
                                .build())
                            .build());
 
                case OFPTFPTNEXTTABLESMISS:
                    propBuilder.setTableFeaturePropType(new NextTableMissBuilder()
                            .setTablesMiss(new TablesMissBuilder()
                                .setTableIds(readNextTableIds(message, propertyLength))
                                .build())
                            .build());
                    break;
                case OFPTFPTWRITEACTIONS:
                case OFPTFPTWRITEACTIONSMISS:
                case OFPTFPTAPPLYACTIONS:
                case OFPTFPTAPPLYACTIONSMISS:
                    break;
                case OFPTFPTMATCH:
                case OFPTFPTWILDCARDS:
                case OFPTFPTWRITESETFIELD:
                case OFPTFPTWRITESETFIELDMISS:
                case OFPTFPTAPPLYSETFIELD:
                case OFPTFPTAPPLYSETFIELDMISS:
                    break;
                case OFPTFPTEXPERIMENTER:
                case OFPTFPTEXPERIMENTERMISS:
                    // TODO: Finish experimenter table features
                    LOG.debug("Table feature property type {} is not handled yet.", propType);
                    break;
            }

            items.add(propBuilder.build());
        }


        return new TablePropertiesBuilder()
            .setTableFeatureProperties(items)
            .build();
    }

    private static List<Short> readNextTableIds(ByteBuf message, int length) {
        length -= COMMON_PROPERTY_LENGTH;
        final List<Short> tableIds = new ArrayList<>();

        while (length > 0) {
            tableIds.add(message.readUnsignedByte());
            length--;
        }

        return tableIds;
    }

}
