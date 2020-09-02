/*
 * Copyright (c) 2014 Pantheon Technologies s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowjava.protocol.impl.deserialization.factories.multipart;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;

import io.netty.buffer.ByteBuf;
import java.util.List;
import org.junit.Test;
import org.opendaylight.openflowjava.protocol.api.extensibility.DeserializerRegistry;
import org.opendaylight.openflowjava.protocol.impl.deserialization.factories.MultipartReplyMessageFactory;
import org.opendaylight.openflowjava.protocol.impl.util.BufferHelper;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.augments.rev150225.NextTableRelatedTableFeatureProperty;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.augments.rev150225.table.features.properties.container.table.feature.properties.NextTableIds;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.TableFeaturesPropType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.MultipartReplyMessage;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.MultipartReplyTableFeaturesCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.table.features._case.MultipartReplyTableFeatures;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.table.features._case.multipart.reply.table.features.TableFeatures;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.table.features.properties.grouping.TableFeatureProperties;

/**
 * Unit tests for MultipartReplyTableFeatures.
 *
 * @author michal.polkorab
 */
public class MultipartReplyTableFeaturesTest {
    private final MultipartReplyMessageFactory factory =
            new MultipartReplyMessageFactory(mock(DeserializerRegistry.class));

    /**
     * Testing {@link MultipartReplyMessageFactory} for correct translation into POJO.
     */
    @Test
    public void testEmptyMultipartReplyTableFeatures() {
        ByteBuf bb = BufferHelper.buildBuffer("00 0C 00 00 00 00 00 00");
        MultipartReplyMessage builtByFactory = BufferHelper.deserialize(factory, bb);

        BufferHelper.checkHeaderV13(builtByFactory);
        assertEquals("Wrong type", 12, builtByFactory.getType().getIntValue());
        assertEquals("Wrong flag", false, builtByFactory.getFlags().isOFPMPFREQMORE());
        MultipartReplyTableFeaturesCase messageCase =
                (MultipartReplyTableFeaturesCase) builtByFactory.getMultipartReplyBody();
        MultipartReplyTableFeatures message = messageCase.getMultipartReplyTableFeatures();
        assertEquals("Wrong table features size", 0, message.nonnullTableFeatures().size());
    }

    /**
     * Testing {@link MultipartReplyMessageFactory} for correct translation into POJO.
     */
    @Test
    public void testMultipartReplyTableFeatures() {
        ByteBuf bb = BufferHelper.buildBuffer("00 0C 00 00 00 00 00 00 " + //
                                              // first table feature
                                              "00 40 01 00 00 00 00 00 " + // length, tableId, padding
                                              "4F 70 65 6E 64 61 79 6C 69 67 68 74 00 00 00 00 00 00 00 " + //
                                              "00 00 00 00 00 00 00 00 00 00 00 00 00 " + // name
                                              "00 00 00 00 00 00 00 01 " + // metadata match
                                              "00 00 00 00 00 00 00 02 " + // metadata write
                                              "00 00 00 00 " + // config
                                              "00 00 00 2A " + // max entries
                                              // second table feature
                                              "00 40 02 00 00 00 00 00 " + // length, tableId, padding
                                              "4F 70 65 6E 64 61 79 6C 69 67 68 74 00 00 00 00 00 00 00"
                                              + " 00 00 00 00 00 00 00 00 00 00 00 00 00 " + // name
                                              "00 00 00 00 00 00 00 03 " + // metadata match
                                              "00 00 00 00 00 00 00 04 " + // metadata write
                                              "00 00 00 03 " + // config
                                              "00 00 00 2B"  // max entries
                                              );
        MultipartReplyMessage builtByFactory = BufferHelper.deserialize(factory, bb);

        BufferHelper.checkHeaderV13(builtByFactory);
        assertEquals("Wrong type", 12, builtByFactory.getType().getIntValue());
        assertEquals("Wrong flag", false, builtByFactory.getFlags().isOFPMPFREQMORE());
        MultipartReplyTableFeaturesCase messageCase =
                (MultipartReplyTableFeaturesCase) builtByFactory.getMultipartReplyBody();
        MultipartReplyTableFeatures message = messageCase.getMultipartReplyTableFeatures();
        assertEquals("Wrong table features size", 2, message.getTableFeatures().size());
        TableFeatures feature = message.getTableFeatures().get(0);
        assertEquals("Wrong table id", 1, feature.getTableId().intValue());
        assertEquals("Wrong name", "Opendaylight", feature.getName());
        assertArrayEquals("Wrong metadata match", new byte[]{0, 0, 0, 0, 0, 0, 0, 1}, feature.getMetadataMatch());
        assertArrayEquals("Wrong metadata write", new byte[]{0, 0, 0, 0, 0, 0, 0, 2}, feature.getMetadataWrite());
        assertEquals("Wrong config", false, feature.getConfig().isOFPTCDEPRECATEDMASK());
        assertEquals("Wrong max entries", 42, feature.getMaxEntries().intValue());
        feature = message.getTableFeatures().get(1);
        assertEquals("Wrong table id", 2, feature.getTableId().intValue());
        assertEquals("Wrong name", "Opendaylight", feature.getName());
        assertArrayEquals("Wrong metadata match", new byte[]{0, 0, 0, 0, 0, 0, 0, 3}, feature.getMetadataMatch());
        assertArrayEquals("Wrong metadata write", new byte[]{0, 0, 0, 0, 0, 0, 0, 4}, feature.getMetadataWrite());
        assertEquals("Wrong config", true, feature.getConfig().isOFPTCDEPRECATEDMASK());
        assertEquals("Wrong max entries", 43, feature.getMaxEntries().intValue());
    }

    /**
     * Testing {@link MultipartReplyMessageFactory} for correct translation into POJO.
     */
    @Test
    public void testMultipartReplyTableFeatures2() {
        ByteBuf bb = BufferHelper.buildBuffer("00 0C 00 00 00 00 00 00 " + //
                                              "00 B0 01 00 00 00 00 00 " + // length, tableId, padding
                                              "4F 70 65 6E 64 61 79 6C 69 67 68 74 00 00 00 00 00 00 00 " + //
                                              "00 00 00 00 00 00 00 00 00 00 00 00 00 " + // name
                                              "00 00 00 00 00 00 00 01 " + // metadata match
                                              "00 00 00 00 00 00 00 02 " + // metadata write
                                              "00 00 00 00 " + // config
                                              "00 00 00 2A " + // max entries
                                              "00 00 00 04 00 00 00 00 " + //
                                              "00 01 00 04 00 00 00 00 " + //
                                              "00 02 00 08 01 02 03 04 " + //
                                              "00 03 00 07 05 06 07 00 " + //
                                              "00 04 00 04 00 00 00 00 " + //
                                              "00 05 00 04 00 00 00 00 " + //
                                              "00 06 00 04 00 00 00 00 " + //
                                              "00 07 00 04 00 00 00 00 " + //
                                              "00 08 00 04 00 00 00 00 " + //
                                              "00 0A 00 04 00 00 00 00 " + //
                                              "00 0C 00 04 00 00 00 00 " + //
                                              "00 0D 00 04 00 00 00 00 " + //
                                              "00 0E 00 04 00 00 00 00 " + //
                                              "00 0F 00 04 00 00 00 00"
                                              );
        MultipartReplyMessage builtByFactory = BufferHelper.deserialize(factory, bb);

        BufferHelper.checkHeaderV13(builtByFactory);
        assertEquals("Wrong type", 12, builtByFactory.getType().getIntValue());
        assertEquals("Wrong flag", false, builtByFactory.getFlags().isOFPMPFREQMORE());
        MultipartReplyTableFeaturesCase messageCase =
                (MultipartReplyTableFeaturesCase) builtByFactory.getMultipartReplyBody();
        MultipartReplyTableFeatures message = messageCase.getMultipartReplyTableFeatures();
        assertEquals("Wrong table features size", 1, message.getTableFeatures().size());
        TableFeatures feature = message.getTableFeatures().get(0);
        assertEquals("Wrong table id", 1, feature.getTableId().intValue());
        assertEquals("Wrong name", "Opendaylight", feature.getName());
        assertArrayEquals("Wrong metadata match", new byte[]{0, 0, 0, 0, 0, 0, 0, 1}, feature.getMetadataMatch());
        assertArrayEquals("Wrong metadata write", new byte[]{0, 0, 0, 0, 0, 0, 0, 2}, feature.getMetadataWrite());
        assertEquals("Wrong config", false, feature.getConfig().isOFPTCDEPRECATEDMASK());
        assertEquals("Wrong max entries", 42, feature.getMaxEntries().intValue());
        assertEquals("Wrong properties size", 14, feature.getTableFeatureProperties().size());
        assertEquals("Wrong property type", TableFeaturesPropType.OFPTFPTINSTRUCTIONS,
                feature.getTableFeatureProperties().get(0).getType());
        assertEquals("Wrong property type", TableFeaturesPropType.OFPTFPTINSTRUCTIONSMISS,
                feature.getTableFeatureProperties().get(1).getType());
        TableFeatureProperties property = feature.getTableFeatureProperties().get(2);
        assertEquals("Wrong property type", TableFeaturesPropType.OFPTFPTNEXTTABLES, property.getType());
        List<NextTableIds> tableIds = property.augmentation(NextTableRelatedTableFeatureProperty.class)
                .getNextTableIds();
        assertEquals("Wrong next table id size", 4, tableIds.size());
        assertEquals("Wrong next table id", 1, tableIds.get(0).getTableId().intValue());
        assertEquals("Wrong next table id", 2, tableIds.get(1).getTableId().intValue());
        assertEquals("Wrong next table id", 3, tableIds.get(2).getTableId().intValue());
        assertEquals("Wrong next table id", 4, tableIds.get(3).getTableId().intValue());
        property = feature.getTableFeatureProperties().get(3);
        assertEquals("Wrong property type", TableFeaturesPropType.OFPTFPTNEXTTABLESMISS,
                property.getType());
        tableIds = property.augmentation(NextTableRelatedTableFeatureProperty.class)
                .getNextTableIds();
        assertEquals("Wrong next table id size", 3, tableIds.size());
        assertEquals("Wrong next table id", 5, tableIds.get(0).getTableId().intValue());
        assertEquals("Wrong next table id", 6, tableIds.get(1).getTableId().intValue());
        assertEquals("Wrong next table id", 7, tableIds.get(2).getTableId().intValue());
        assertEquals("Wrong property type", TableFeaturesPropType.OFPTFPTWRITEACTIONS,
                feature.getTableFeatureProperties().get(4).getType());
        assertEquals("Wrong property type", TableFeaturesPropType.OFPTFPTWRITEACTIONSMISS,
                feature.getTableFeatureProperties().get(5).getType());
        assertEquals("Wrong property type", TableFeaturesPropType.OFPTFPTAPPLYACTIONS,
                feature.getTableFeatureProperties().get(6).getType());
        assertEquals("Wrong property type", TableFeaturesPropType.OFPTFPTAPPLYACTIONSMISS,
                feature.getTableFeatureProperties().get(7).getType());
        assertEquals("Wrong property type", TableFeaturesPropType.OFPTFPTMATCH,
                feature.getTableFeatureProperties().get(8).getType());
        assertEquals("Wrong property type", TableFeaturesPropType.OFPTFPTWILDCARDS,
                feature.getTableFeatureProperties().get(9).getType());
        assertEquals("Wrong property type", TableFeaturesPropType.OFPTFPTWRITESETFIELD,
                feature.getTableFeatureProperties().get(10).getType());
        assertEquals("Wrong property type", TableFeaturesPropType.OFPTFPTWRITESETFIELDMISS,
                feature.getTableFeatureProperties().get(11).getType());
        assertEquals("Wrong property type", TableFeaturesPropType.OFPTFPTAPPLYSETFIELD,
                feature.getTableFeatureProperties().get(12).getType());
        assertEquals("Wrong property type", TableFeaturesPropType.OFPTFPTAPPLYSETFIELDMISS,
                feature.getTableFeatureProperties().get(13).getType());
    }
}
