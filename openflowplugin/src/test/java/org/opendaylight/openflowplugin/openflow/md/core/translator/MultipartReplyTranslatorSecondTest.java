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
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.opendaylight.openflowjava.protocol.api.util.EncodeConstants;
import org.opendaylight.openflowplugin.api.openflow.md.core.ConnectionConductor;
import org.opendaylight.openflowplugin.api.openflow.md.core.SwitchConnectionDistinguisher;
import org.opendaylight.openflowplugin.api.openflow.md.core.session.SessionContext;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.ConvertorManager;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.ConvertorManagerFactory;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.statistics.rev130819.AggregateFlowStatisticsUpdate;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.statistics.rev131111.GroupFeaturesUpdated;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.statistics.rev131111.MeterFeaturesUpdated;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.ActionType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.GroupCapabilities;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.GroupTypes;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.MeterBandTypeBitmap;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.MeterFlags;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.MultipartRequestFlags;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.MultipartType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.GetFeaturesOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.MultipartReplyMessage;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.MultipartReplyMessageBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.OfHeader;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.MultipartReplyAggregateCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.MultipartReplyGroupFeaturesCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.MultipartReplyMeterFeaturesCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.aggregate._case.MultipartReplyAggregateBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.group.features._case.MultipartReplyGroupFeaturesBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.meter.features._case.MultipartReplyMeterFeaturesBuilder;
import org.opendaylight.yangtools.yang.binding.DataObject;

/**
 * @author michal.polkorab
 *
 */
@RunWith(MockitoJUnitRunner.class)
public class MultipartReplyTranslatorSecondTest {

    @Mock SwitchConnectionDistinguisher cookie;
    @Mock SessionContext sc;
    @Mock ConnectionConductor conductor;
    @Mock GetFeaturesOutput features;

    MultipartReplyTranslator translator;

    /**
     * Initializes mocks
     */
    @Before
    public void startUp() {
        final ConvertorManager convertorManager = ConvertorManagerFactory.createDefaultManager();
        translator = new MultipartReplyTranslator(convertorManager);
        when(sc.getPrimaryConductor()).thenReturn(conductor);
        when(conductor.getVersion()).thenReturn((short) EncodeConstants.OF13_VERSION_ID);
        when(sc.getFeatures()).thenReturn(features);
        when(features.getDatapathId()).thenReturn(new BigInteger("42"));
    }

    /**
     * Test {@link MultipartReplyTranslator#translate(SwitchConnectionDistinguisher, SessionContext, OfHeader)}
     * with aggregate stat
     */
    @Test
    public void testAggregateCase() {
        MultipartReplyMessageBuilder mpBuilder = new MultipartReplyMessageBuilder();
        mpBuilder.setVersion((short) EncodeConstants.OF13_VERSION_ID);
        mpBuilder.setXid(123L);
        mpBuilder.setFlags(new MultipartRequestFlags(false));
        mpBuilder.setType(MultipartType.OFPMPAGGREGATE);
        
        MultipartReplyAggregateCaseBuilder caseBuilder = new MultipartReplyAggregateCaseBuilder();
        MultipartReplyAggregateBuilder aggBuilder = new MultipartReplyAggregateBuilder();
        aggBuilder.setByteCount(new BigInteger("52"));
        aggBuilder.setFlowCount(6587L);
        aggBuilder.setPacketCount(new BigInteger("25"));
        caseBuilder.setMultipartReplyAggregate(aggBuilder.build());
        mpBuilder.setMultipartReplyBody(caseBuilder.build());
        MultipartReplyMessage message = mpBuilder.build();

        List<DataObject> list = translator.translate(cookie, sc, message);

        Assert.assertEquals("Wrong list size", 1, list.size());
        AggregateFlowStatisticsUpdate aggUpdate = (AggregateFlowStatisticsUpdate) list.get(0);
        Assert.assertEquals("Wrong node-id", "openflow:42", aggUpdate.getId().getValue());
        Assert.assertEquals("Wrong more-replies", false, aggUpdate.isMoreReplies());
        Assert.assertEquals("Wrong transaction-id", 123, aggUpdate.getTransactionId().getValue().intValue());
        Assert.assertEquals("Wrong byte count", 52, aggUpdate.getByteCount().getValue().intValue());
        Assert.assertEquals("Wrong packet count", 25, aggUpdate.getPacketCount().getValue().intValue());
        Assert.assertEquals("Wrong flow count", 6587, aggUpdate.getFlowCount().getValue().intValue());
    }

    /**
     * Test {@link MultipartReplyTranslator#translate(SwitchConnectionDistinguisher, SessionContext, OfHeader)}
     * with group features stat
     */
    @Test
    public void testGroupFeaturesCase() {
        MultipartReplyMessageBuilder mpBuilder = new MultipartReplyMessageBuilder();
        mpBuilder.setVersion((short) EncodeConstants.OF13_VERSION_ID);
        mpBuilder.setXid(123L);
        mpBuilder.setFlags(new MultipartRequestFlags(false));
        mpBuilder.setType(MultipartType.OFPMPGROUPFEATURES);
        
        MultipartReplyGroupFeaturesCaseBuilder caseBuilder = new MultipartReplyGroupFeaturesCaseBuilder();
        MultipartReplyGroupFeaturesBuilder featBuilder = new MultipartReplyGroupFeaturesBuilder();
        featBuilder.setTypes(new GroupTypes(true, false, true, false));
        featBuilder.setCapabilities(new GroupCapabilities(false, true, false, true));
        List<Long> maxGroups = new ArrayList<>();
        maxGroups.add(1L);
        maxGroups.add(2L);
        maxGroups.add(3L);
        maxGroups.add(4L);
        featBuilder.setMaxGroups(maxGroups);
        List<ActionType> actionTypes = new ArrayList<>();
        ActionType actionType1 = new ActionType(true, false, true, false, false, false, true,
                false, true, false, true, false, true, false, true, false, true);
        actionTypes.add(actionType1);
        ActionType actionType2 = new ActionType(false, true, false, true, false, true, false,
                true, false, true, false, true, false, true, false, true, false);
        actionTypes.add(actionType2);
        ActionType actionType3 = new ActionType(false, false, false, false, false, false, false,
                true, true, true, true, true, true, true, true, true, true);
        actionTypes.add(actionType3);
        ActionType actionType4 = new ActionType(true, true, true, true, true, true, true,
                false, false, false, false, false, false, false, false, false, false);
        actionTypes.add(actionType4);
        featBuilder.setActionsBitmap(actionTypes);
        caseBuilder.setMultipartReplyGroupFeatures(featBuilder.build());
        mpBuilder.setMultipartReplyBody(caseBuilder.build());
        MultipartReplyMessage message = mpBuilder.build();

        List<DataObject> list = translator.translate(cookie, sc, message);

        Assert.assertEquals("Wrong list size", 1, list.size());
        GroupFeaturesUpdated groupUpdate = (GroupFeaturesUpdated) list.get(0);
        Assert.assertEquals("Wrong node-id", "openflow:42", groupUpdate.getId().getValue());
        Assert.assertEquals("Wrong more-replies", false, groupUpdate.isMoreReplies());
        Assert.assertEquals("Wrong transaction-id", 123, groupUpdate.getTransactionId().getValue().intValue());
        Assert.assertEquals("Wrong group types size", 2, groupUpdate.getGroupTypesSupported().size());
        Assert.assertEquals("Wrong group type", "org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.GroupAll",
                groupUpdate.getGroupTypesSupported().get(0).getName());
        Assert.assertEquals("Wrong group type", "org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.GroupIndirect",
                groupUpdate.getGroupTypesSupported().get(1).getName());
        Assert.assertEquals("Wrong group capabilities size", 2, groupUpdate.getGroupCapabilitiesSupported().size());
        Assert.assertEquals("Wrong group capability", "org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.ChainingChecks",
                groupUpdate.getGroupCapabilitiesSupported().get(0).getName());
        Assert.assertEquals("Wrong group capability", "org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.SelectWeight",
                groupUpdate.getGroupCapabilitiesSupported().get(1).getName());
        Assert.assertEquals("Wrong max groups", maxGroups, groupUpdate.getMaxGroups());
        Assert.assertEquals("Wrong actions bitmap", 137072641, groupUpdate.getActions().get(0).longValue());
        Assert.assertEquals("Wrong actions bitmap", 131336192, groupUpdate.getActions().get(1).longValue());
        Assert.assertEquals("Wrong actions bitmap", 247365632, groupUpdate.getActions().get(2).longValue());
        Assert.assertEquals("Wrong actions bitmap", 21043201, groupUpdate.getActions().get(3).longValue());
    }

    /**
     * Test {@link MultipartReplyTranslator#translate(SwitchConnectionDistinguisher, SessionContext, OfHeader)}
     * with meter features stat
     */
    @Test
    public void testMeterFeaturesCase() {
        MultipartReplyMessageBuilder mpBuilder = new MultipartReplyMessageBuilder();
        mpBuilder.setVersion((short) EncodeConstants.OF13_VERSION_ID);
        mpBuilder.setXid(123L);
        mpBuilder.setFlags(new MultipartRequestFlags(false));
        mpBuilder.setType(MultipartType.OFPMPMETERFEATURES);
        
        MultipartReplyMeterFeaturesCaseBuilder caseBuilder = new MultipartReplyMeterFeaturesCaseBuilder();
        MultipartReplyMeterFeaturesBuilder featBuilder = new MultipartReplyMeterFeaturesBuilder();
        featBuilder.setMaxMeter(1L);
        featBuilder.setBandTypes(new MeterBandTypeBitmap(true, true));
        featBuilder.setCapabilities(new MeterFlags(true, true, true, true));
        featBuilder.setMaxBands((short) 2);
        featBuilder.setMaxColor((short) 3);
        caseBuilder.setMultipartReplyMeterFeatures(featBuilder.build());
        mpBuilder.setMultipartReplyBody(caseBuilder.build());
        MultipartReplyMessage message = mpBuilder.build();

        List<DataObject> list = translator.translate(cookie, sc, message);

        Assert.assertEquals("Wrong list size", 1, list.size());
        MeterFeaturesUpdated megterUpdate = (MeterFeaturesUpdated) list.get(0);
        Assert.assertEquals("Wrong node-id", "openflow:42", megterUpdate.getId().getValue());
        Assert.assertEquals("Wrong more-replies", false, megterUpdate.isMoreReplies());
        Assert.assertEquals("Wrong transaction-id", 123, megterUpdate.getTransactionId().getValue().intValue());
        Assert.assertEquals("Wrong max meter", 1, megterUpdate.getMaxMeter().getValue().intValue());
        Assert.assertEquals("Wrong max bands", 2, megterUpdate.getMaxBands().intValue());
        Assert.assertEquals("Wrong max color", 3, megterUpdate.getMaxColor().intValue());
        Assert.assertEquals("Wrong capabilities size", 4, megterUpdate.getMeterCapabilitiesSupported().size());
        Assert.assertEquals("Wrong capability", "org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918"
                + ".MeterBurst", megterUpdate.getMeterCapabilitiesSupported().get(0).getName());
        Assert.assertEquals("Wrong capability", "org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918"
                + ".MeterKbps", megterUpdate.getMeterCapabilitiesSupported().get(1).getName());
        Assert.assertEquals("Wrong capability", "org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918"
                + ".MeterPktps", megterUpdate.getMeterCapabilitiesSupported().get(2).getName());
        Assert.assertEquals("Wrong capability", "org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918"
                + ".MeterStats", megterUpdate.getMeterCapabilitiesSupported().get(3).getName());
        Assert.assertEquals("Wrong band types size", 2, megterUpdate.getMeterBandSupported().size());
        Assert.assertEquals("Wrong band type", "org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.MeterBandDrop",
                megterUpdate.getMeterBandSupported().get(0).getName());
        Assert.assertEquals("Wrong band type", "org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.MeterBandDscpRemark",
                megterUpdate.getMeterBandSupported().get(1).getName());
    }
}