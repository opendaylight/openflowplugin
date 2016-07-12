/*
 * Copyright (c) 2014 Pantheon Technologies s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.openflow.md.core.translator;

import static org.mockito.Mockito.when;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mock;
import org.opendaylight.openflowjava.protocol.api.util.EncodeConstants;
import org.opendaylight.openflowplugin.api.OFConstants;
import org.opendaylight.openflowplugin.api.openflow.md.core.ConnectionConductor;
import org.opendaylight.openflowplugin.api.openflow.md.core.SwitchConnectionDistinguisher;
import org.opendaylight.openflowplugin.api.openflow.md.core.session.SessionContext;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.ConvertorManagerInitialization;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.augments.rev150225.NextTableRelatedTableFeatureProperty;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.augments.rev150225.NextTableRelatedTableFeaturePropertyBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.augments.rev150225.table.features.properties.container.table.feature.properties.NextTableIds;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.augments.rev150225.table.features.properties.container.table.feature.properties.NextTableIdsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.MultipartRequestFlags;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.MultipartType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.TableConfig;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.TableFeaturesPropType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.GetFeaturesOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.HelloMessage;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.HelloMessageBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.MultipartReplyMessage;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.MultipartReplyMessageBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.OfHeader;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.MultipartReplyTableFeaturesCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.table.features._case.MultipartReplyTableFeaturesBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.table.features._case.multipart.reply.table.features.TableFeatures;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.table.features._case.multipart.reply.table.features.TableFeaturesBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.table.features.properties.grouping.TableFeatureProperties;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.table.features.properties.grouping.TableFeaturePropertiesBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.service.rev131026.TableUpdated;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table.feature.prop.type.table.feature.prop.type.NextTable;
import org.opendaylight.yangtools.yang.binding.DataObject;

/**
 * @author michal.polkorab
 *
 */
public class MultipartReplyTableFeaturesToTableUpdatedTranslatorTest extends ConvertorManagerInitialization {

    @Mock SwitchConnectionDistinguisher cookie;
    @Mock SessionContext sc;
    @Mock GetFeaturesOutput features;
    @Mock ConnectionConductor conductor;

    MultipartReplyTableFeaturesToTableUpdatedTranslator translator;

    /**
     * Initializes mocks
     */
    @Override
    public void setUp() {
        translator = new MultipartReplyTableFeaturesToTableUpdatedTranslator(getConvertorManager());
        when(sc.getPrimaryConductor()).thenReturn(conductor);
        when(sc.getFeatures()).thenReturn(features);
        when(conductor.getVersion()).thenReturn(OFConstants.OFP_VERSION_1_3);
        when(features.getDatapathId()).thenReturn(new BigInteger("42"));
    }

    /**
     * Test {@link MultipartReplyTableFeaturesToTableUpdatedTranslator#translate(SwitchConnectionDistinguisher, SessionContext, OfHeader)}
     * with wrong inputs
     */
    @Test
    public void testWrongInputs() {
        HelloMessageBuilder helloBuilder = new HelloMessageBuilder();
        HelloMessage helloMessage = helloBuilder.build();
        List<DataObject> list = translator.translate(cookie, sc, helloMessage);
        Assert.assertEquals("Wrong output", 0, list.size());
        
        MultipartReplyMessageBuilder builder = new MultipartReplyMessageBuilder();
        builder.setType(MultipartType.OFPMPFLOW);
        MultipartReplyMessage message = builder.build();
        list = translator.translate(cookie, sc, message);
        Assert.assertEquals("Wrong output", 0, list.size());
    }

    /**
     * Test {@link MultipartReplyTableFeaturesToTableUpdatedTranslator#translate(SwitchConnectionDistinguisher, SessionContext, OfHeader)}
     * with correct inputs (no table features)
     */
    @Test
    public void testEmptyTableFeaturesWithCorrectInput() {
        MultipartReplyMessageBuilder builder = new MultipartReplyMessageBuilder();
        builder.setVersion((short) EncodeConstants.OF13_VERSION_ID);
        builder.setXid(12345L);
        builder.setType(MultipartType.OFPMPTABLEFEATURES);
        builder.setFlags(new MultipartRequestFlags(false));
        
        MultipartReplyTableFeaturesCaseBuilder caseBuilder = new MultipartReplyTableFeaturesCaseBuilder();
        MultipartReplyTableFeaturesBuilder featuresBuilder = new MultipartReplyTableFeaturesBuilder();
        List<TableFeatures> features = new ArrayList<>();
        featuresBuilder.setTableFeatures(features);
        caseBuilder.setMultipartReplyTableFeatures(featuresBuilder.build());
        builder.setMultipartReplyBody(caseBuilder.build());
        MultipartReplyMessage message = builder.build();
        
        List<DataObject> list = translator.translate(cookie, sc, message);
        Assert.assertEquals("Wrong output", 1, list.size());
        TableUpdated tableUpdated = (TableUpdated) list.get(0);
        Assert.assertEquals("Wrong table features size", 0, tableUpdated.getTableFeatures().size());
    }

    /**
     * Test {@link MultipartReplyTableFeaturesToTableUpdatedTranslator#translate(SwitchConnectionDistinguisher, SessionContext, OfHeader)}
     * with correct inputs
     */
    @Test
    public void testTableFeaturesWithCorrectInput() {
        MultipartReplyMessageBuilder builder = new MultipartReplyMessageBuilder();
        builder.setVersion((short) EncodeConstants.OF13_VERSION_ID);
        builder.setXid(12345L);
        builder.setType(MultipartType.OFPMPTABLEFEATURES);
        builder.setFlags(new MultipartRequestFlags(false));
        
        MultipartReplyTableFeaturesCaseBuilder caseBuilder = new MultipartReplyTableFeaturesCaseBuilder();
        MultipartReplyTableFeaturesBuilder featuresBuilder = new MultipartReplyTableFeaturesBuilder();
        List<TableFeatures> features = new ArrayList<>();
        
        TableFeaturesBuilder tableFeatBuilder = new TableFeaturesBuilder();
        tableFeatBuilder.setTableId((short) 2);
        tableFeatBuilder.setName("Fastest table in the world");
        byte[] metadataMatch = new byte[]{0, 1, 2, 3, 4, 5, 6, 7};
        tableFeatBuilder.setMetadataMatch(metadataMatch);
        byte[] metadataWrite = new byte[]{1, 2, 3, 4, 5, 6, 7, 8};
        tableFeatBuilder.setMetadataWrite(metadataWrite);
        tableFeatBuilder.setConfig(new TableConfig(false));
        tableFeatBuilder.setMaxEntries(10L);
        List<TableFeatureProperties> properties = new ArrayList<>();
        TableFeaturePropertiesBuilder propBuilder = new TableFeaturePropertiesBuilder();
        propBuilder.setType(TableFeaturesPropType.OFPTFPTNEXTTABLES);
        NextTableRelatedTableFeaturePropertyBuilder tableBuilder = new NextTableRelatedTableFeaturePropertyBuilder();
        List<NextTableIds> tableIds = new ArrayList<>();
        NextTableIdsBuilder nextTableIdsBuilder = new NextTableIdsBuilder();
        nextTableIdsBuilder.setTableId((short) 9);
        tableIds.add(nextTableIdsBuilder.build());
        nextTableIdsBuilder = new NextTableIdsBuilder();
        nextTableIdsBuilder.setTableId((short) 10);
        tableIds.add(nextTableIdsBuilder.build());
        nextTableIdsBuilder = new NextTableIdsBuilder();
        nextTableIdsBuilder.setTableId((short) 11);
        tableIds.add(nextTableIdsBuilder.build());
        tableBuilder.setNextTableIds(tableIds);
        propBuilder.addAugmentation(NextTableRelatedTableFeatureProperty.class, tableBuilder.build());
        properties.add(propBuilder.build());
        tableFeatBuilder.setTableFeatureProperties(properties);
        features.add(tableFeatBuilder.build());
        featuresBuilder.setTableFeatures(features);
        caseBuilder.setMultipartReplyTableFeatures(featuresBuilder.build());
        builder.setMultipartReplyBody(caseBuilder.build());
        MultipartReplyMessage message = builder.build();
        
        List<DataObject> list = translator.translate(cookie, sc, message);
        Assert.assertEquals("Wrong output", 1, list.size());
        TableUpdated tableUpdated = (TableUpdated) list.get(0);
        Assert.assertEquals("Wrong table features size", 1, tableUpdated.getTableFeatures().size());
        org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table.features
        .TableFeatures feature = tableUpdated.getTableFeatures().get(0);
        Assert.assertEquals("Wrong table-id", 2, feature.getTableId().intValue());
        Assert.assertEquals("Wrong table name", "Fastest table in the world", feature.getName());
        Assert.assertEquals("Wrong metadata match", new BigInteger(metadataMatch), feature.getMetadataMatch());
        Assert.assertEquals("Wrong metadata write", new BigInteger(metadataWrite), feature.getMetadataWrite());
        Assert.assertEquals("Wrong config", false, feature.getConfig().isDEPRECATEDMASK());
        Assert.assertEquals("Wrong max entries", 10, feature.getMaxEntries().intValue());
        Assert.assertEquals("Wrong properties size", 1, feature.getTableProperties().getTableFeatureProperties().size());
        org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table.features.table.features.table.properties
        .TableFeatureProperties property = feature.getTableProperties().getTableFeatureProperties().get(0);
        Assert.assertEquals("Wrong property type", "org.opendaylight.yang.gen.v1.urn.opendaylight.table.types"
                + ".rev131026.table.feature.prop.type.table.feature.prop.type.NextTable",
                property.getTableFeaturePropType().getImplementedInterface().getName());
        NextTable nextTableProperty = (NextTable) property.getTableFeaturePropType();
        Assert.assertEquals("Wrong next tables size", 3, nextTableProperty.getTables().getTableIds().size());
        Assert.assertEquals("Wrong next tables size", 9, nextTableProperty.getTables().getTableIds().get(0).intValue());
        Assert.assertEquals("Wrong next tables size", 10, nextTableProperty.getTables().getTableIds().get(1).intValue());
        Assert.assertEquals("Wrong next tables size", 11, nextTableProperty.getTables().getTableIds().get(2).intValue());
    }
}