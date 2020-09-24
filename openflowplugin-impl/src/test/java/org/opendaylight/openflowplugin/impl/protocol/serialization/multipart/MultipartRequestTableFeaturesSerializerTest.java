/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.protocol.serialization.multipart;

import static org.junit.Assert.assertEquals;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.UnpooledByteBufAllocator;
import java.util.Collections;
import org.junit.Test;
import org.opendaylight.openflowjava.protocol.api.keys.MessageTypeKey;
import org.opendaylight.openflowjava.protocol.api.util.EncodeConstants;
import org.opendaylight.openflowjava.util.ByteBufUtils;
import org.opendaylight.openflowplugin.impl.protocol.serialization.AbstractSerializerTest;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.TableFeaturesPropType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.TableConfig;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.multipart.request.multipart.request.body.MultipartRequestTableFeatures;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.multipart.request.multipart.request.body.MultipartRequestTableFeaturesBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table.feature.prop.type.table.feature.prop.type.NextTable;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table.feature.prop.type.table.feature.prop.type.NextTableBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table.feature.prop.type.table.feature.prop.type.next.table.TablesBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table.features.TableFeaturesBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table.features.table.features.TablePropertiesBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table.features.table.features.table.properties.TableFeaturePropertiesBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table.features.table.features.table.properties.TableFeaturePropertiesKey;
import org.opendaylight.yangtools.yang.common.Uint32;
import org.opendaylight.yangtools.yang.common.Uint64;
import org.opendaylight.yangtools.yang.common.Uint8;

public class MultipartRequestTableFeaturesSerializerTest extends AbstractSerializerTest {
    private static final byte PADDING_IN_MULTIPART_REQUEST_TABLE_FEATURES_BODY = 5;
    private static final byte MAX_TABLE_NAME_LENGTH = 32;
    private static final Uint8 TABLE_ID = Uint8.valueOf(42);
    private static final String NAME = "table_prop";
    private static final Uint64 METADATA_MATCH = Uint64.ONE;
    private static final Uint64 METADATA_WRITE = Uint64.TEN;
    private static final Uint32 MAX_ENTRIES = Uint32.valueOf(12);
    private static final boolean IS_DEPRECATED_MASK = true;
    private static final Uint8 NEXT_TABLE_ID = Uint8.valueOf(43);

    private static final TableFeaturesPropType NEXT_TABLE_TYPE = TableFeaturesPropType.OFPTFPTNEXTTABLES;
    private static final NextTable NEXT_TABLE = new NextTableBuilder()
            .setTables(new TablesBuilder()
                    .setTableIds(Collections.singletonList(NEXT_TABLE_ID))
                    .build())
            .build();
    private static final MultipartRequestTableFeatures BODY = new MultipartRequestTableFeaturesBuilder()
            .setTableFeatures(Collections.singletonList(new TableFeaturesBuilder()
                    .setTableId(TABLE_ID)
                    .setName(NAME)
                    .setMetadataMatch(METADATA_MATCH)
                    .setMetadataWrite(METADATA_WRITE)
                    .setConfig(new TableConfig(IS_DEPRECATED_MASK))
                    .setMaxEntries(MAX_ENTRIES)
                    .setTableProperties(new TablePropertiesBuilder()
                            .setTableFeatureProperties(Collections.singletonList(new TableFeaturePropertiesBuilder()
                                    .setOrder(0)
                                    .withKey(new TableFeaturePropertiesKey(0))
                                    .setTableFeaturePropType(NEXT_TABLE)
                                    .build()))
                            .build())
                    .build()))
            .build();

    private MultipartRequestTableFeaturesSerializer serializer;

    @Override
    protected void init() {
        serializer = getRegistry().getSerializer(new MessageTypeKey<>(EncodeConstants.OF13_VERSION_ID,
                MultipartRequestTableFeatures.class));
    }

    @Test
    public void testSerialize() {
        final ByteBuf out = UnpooledByteBufAllocator.DEFAULT.buffer();
        serializer.serialize(BODY, out);

        out.skipBytes(Short.BYTES); // skip length
        assertEquals(TABLE_ID.shortValue(), out.readUnsignedByte());
        out.skipBytes(PADDING_IN_MULTIPART_REQUEST_TABLE_FEATURES_BODY);
        assertEquals(NAME, ByteBufUtils.decodeNullTerminatedString(out, MAX_TABLE_NAME_LENGTH));
        assertEquals(METADATA_MATCH.longValue(), out.readLong());
        assertEquals(METADATA_WRITE.longValue(), out.readLong());
        assertEquals(IS_DEPRECATED_MASK, (out.readUnsignedInt() & 3) != 0);
        assertEquals(MAX_ENTRIES.longValue(), out.readUnsignedInt());
        assertEquals(NEXT_TABLE_TYPE.getIntValue(), out.readUnsignedShort());
        final int propLength = out.readUnsignedShort();
        final int paddingRemainder = propLength % EncodeConstants.PADDING;
        assertEquals(NEXT_TABLE_ID.toJava(), out.readUnsignedByte());

        if (paddingRemainder != 0) {
            out.skipBytes(EncodeConstants.PADDING - paddingRemainder);
        }

        assertEquals(out.readableBytes(), 0);
    }

}