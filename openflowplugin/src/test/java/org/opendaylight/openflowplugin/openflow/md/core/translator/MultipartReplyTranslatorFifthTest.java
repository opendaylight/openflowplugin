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
import org.mockito.MockitoAnnotations;
import org.opendaylight.openflowjava.protocol.api.util.EncodeConstants;
import org.opendaylight.openflowplugin.api.openflow.md.core.ConnectionConductor;
import org.opendaylight.openflowplugin.api.openflow.md.core.SwitchConnectionDistinguisher;
import org.opendaylight.openflowplugin.api.openflow.md.core.session.SessionContext;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.ConvertorManagerInitialization;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.statistics.rev131111.MeterConfigStatsUpdated;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.statistics.rev131111.MeterStatisticsUpdated;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.meter.config.stats.reply.MeterConfigStats;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.meter.meter.band.headers.MeterBandHeader;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.MeterBandType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.MeterFlags;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.MeterId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.MultipartRequestFlags;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.MultipartType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.GetFeaturesOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.MultipartReplyMessage;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.MultipartReplyMessageBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.OfHeader;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.meter.band.header.meter.band.MeterBandDropCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.meter.band.header.meter.band.MeterBandDscpRemarkCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.meter.band.header.meter.band.meter.band.drop._case.MeterBandDropBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.meter.band.header.meter.band.meter.band.dscp.remark._case.MeterBandDscpRemarkBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.MultipartReplyMeterCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.MultipartReplyMeterConfigCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.meter._case.MultipartReplyMeterBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.meter._case.multipart.reply.meter.MeterStats;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.meter._case.multipart.reply.meter.MeterStatsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.meter._case.multipart.reply.meter.meter.stats.MeterBandStats;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.meter._case.multipart.reply.meter.meter.stats.MeterBandStatsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.meter.config._case.MultipartReplyMeterConfigBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.meter.config._case.multipart.reply.meter.config.MeterConfig;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.meter.config._case.multipart.reply.meter.config.MeterConfigBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.meter.config._case.multipart.reply.meter.config.meter.config.Bands;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.meter.config._case.multipart.reply.meter.config.meter.config.BandsBuilder;
import org.opendaylight.yangtools.yang.binding.DataObject;

/**
 * @author michal.polkorab
 */
public class MultipartReplyTranslatorFifthTest extends ConvertorManagerInitialization {

    @Mock
    SwitchConnectionDistinguisher cookie;
    @Mock
    SessionContext sc;
    @Mock
    ConnectionConductor conductor;
    @Mock
    GetFeaturesOutput features;

    MultipartReplyTranslator translator;

    /**
     * Initializes mocks
     */
    @Override
    public void setUp() {
        translator = new MultipartReplyTranslator(getConvertorManager());
        MockitoAnnotations.initMocks(this);
        when(sc.getPrimaryConductor()).thenReturn(conductor);
        when(conductor.getVersion()).thenReturn((short) EncodeConstants.OF13_VERSION_ID);
        when(sc.getFeatures()).thenReturn(features);
        when(features.getDatapathId()).thenReturn(new BigInteger("42"));
    }

    /**
     * Test {@link MultipartReplyTranslator#translate(SwitchConnectionDistinguisher, SessionContext, OfHeader)}
     * with empty meter stats
     */
    @Test
    public void testEmptyMeterStats() {
        MultipartReplyMessageBuilder mpBuilder = new MultipartReplyMessageBuilder();
        mpBuilder.setVersion((short) EncodeConstants.OF13_VERSION_ID);
        mpBuilder.setXid(123L);
        mpBuilder.setFlags(new MultipartRequestFlags(false));
        mpBuilder.setType(MultipartType.OFPMPMETER);

        MultipartReplyMeterCaseBuilder caseBuilder = new MultipartReplyMeterCaseBuilder();
        MultipartReplyMeterBuilder meterBuilder = new MultipartReplyMeterBuilder();
        List<MeterStats> meterStats = new ArrayList<>();
        meterBuilder.setMeterStats(meterStats);
        caseBuilder.setMultipartReplyMeter(meterBuilder.build());
        mpBuilder.setMultipartReplyBody(caseBuilder.build());
        MultipartReplyMessage message = mpBuilder.build();

        List<DataObject> list = translator.translate(cookie, sc, message);

        Assert.assertEquals("Wrong list size", 1, list.size());
        MeterStatisticsUpdated statUpdate = (MeterStatisticsUpdated) list.get(0);
        Assert.assertEquals("Wrong node-id", "openflow:42", statUpdate.getId().getValue());
        Assert.assertEquals("Wrong more-replies", false, statUpdate.isMoreReplies());
        Assert.assertEquals("Wrong transaction-id", 123, statUpdate.getTransactionId().getValue().intValue());
        Assert.assertEquals("Wrong meter stats size", 0, statUpdate.getMeterStats().size());
    }

    /**
     * Test {@link MultipartReplyTranslator#translate(SwitchConnectionDistinguisher, SessionContext, OfHeader)}
     * with meter stats
     */
    @Test
    public void testMeterStats() {
        MultipartReplyMessageBuilder mpBuilder = new MultipartReplyMessageBuilder();
        mpBuilder.setVersion((short) EncodeConstants.OF13_VERSION_ID);
        mpBuilder.setXid(123L);
        mpBuilder.setFlags(new MultipartRequestFlags(false));
        mpBuilder.setType(MultipartType.OFPMPMETER);

        MultipartReplyMeterCaseBuilder caseBuilder = new MultipartReplyMeterCaseBuilder();
        MultipartReplyMeterBuilder meterBuilder = new MultipartReplyMeterBuilder();
        List<MeterStats> meterStats = new ArrayList<>();
        MeterStatsBuilder builder = new MeterStatsBuilder();
        builder.setMeterId(new MeterId(15L));
        builder.setFlowCount(16L);
        builder.setPacketInCount(new BigInteger("17"));
        builder.setByteInCount(new BigInteger("18"));
        builder.setDurationSec(19L);
        builder.setDurationNsec(20L);
        List<MeterBandStats> meterBands = new ArrayList<>();
        MeterBandStatsBuilder meterStatsBuilder = new MeterBandStatsBuilder();
        meterStatsBuilder.setPacketBandCount(new BigInteger("21"));
        meterStatsBuilder.setByteBandCount(new BigInteger("22"));
        meterBands.add(meterStatsBuilder.build());
        meterStatsBuilder = new MeterBandStatsBuilder();
        meterStatsBuilder.setPacketBandCount(new BigInteger("23"));
        meterStatsBuilder.setByteBandCount(new BigInteger("24"));
        meterBands.add(meterStatsBuilder.build());
        builder.setMeterBandStats(meterBands);
        meterStats.add(builder.build());
        builder = new MeterStatsBuilder();
        builder.setMeterId(new MeterId(150L));
        builder.setFlowCount(160L);
        builder.setPacketInCount(new BigInteger("170"));
        builder.setByteInCount(new BigInteger("180"));
        builder.setDurationSec(190L);
        builder.setDurationNsec(200L);
        meterBands = new ArrayList<>();
        builder.setMeterBandStats(meterBands);
        meterStats.add(builder.build());
        meterBuilder.setMeterStats(meterStats);
        caseBuilder.setMultipartReplyMeter(meterBuilder.build());
        mpBuilder.setMultipartReplyBody(caseBuilder.build());
        MultipartReplyMessage message = mpBuilder.build();

        List<DataObject> list = translator.translate(cookie, sc, message);

        Assert.assertEquals("Wrong list size", 1, list.size());
        MeterStatisticsUpdated statUpdate = (MeterStatisticsUpdated) list.get(0);
        Assert.assertEquals("Wrong node-id", "openflow:42", statUpdate.getId().getValue());
        Assert.assertEquals("Wrong more-replies", false, statUpdate.isMoreReplies());
        Assert.assertEquals("Wrong transaction-id", 123, statUpdate.getTransactionId().getValue().intValue());
        Assert.assertEquals("Wrong group stats size", 2, statUpdate.getMeterStats().size());
        org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.meter.statistics.reply
                .MeterStats stat = statUpdate.getMeterStats().get(0);
        Assert.assertEquals("Wrong meter-id", 15, stat.getMeterId().getValue().intValue());
        Assert.assertEquals("Wrong flow count", 16, stat.getFlowCount().getValue().intValue());
        Assert.assertEquals("Wrong packet in count", 17, stat.getPacketInCount().getValue().intValue());
        Assert.assertEquals("Wrong byte in count", 18, stat.getByteInCount().getValue().intValue());
        Assert.assertEquals("Wrong duration sec", 19, stat.getDuration().getSecond().getValue().intValue());
        Assert.assertEquals("Wrong duration n sec", 20, stat.getDuration().getNanosecond().getValue().intValue());
        Assert.assertEquals("Wrong meter band stats size", 2, stat.getMeterBandStats().getBandStat().size());
        Assert.assertEquals("Wrong band id", 0, stat.getMeterBandStats().getBandStat().get(0)
                .getBandId().getValue().intValue());
        Assert.assertEquals("Wrong packet band count", 21, stat.getMeterBandStats().getBandStat().get(0)
                .getPacketBandCount().getValue().intValue());
        Assert.assertEquals("Wrong byte band count", 22, stat.getMeterBandStats().getBandStat().get(0)
                .getByteBandCount().getValue().intValue());
        Assert.assertEquals("Wrong band id", 1, stat.getMeterBandStats().getBandStat().get(1)
                .getBandId().getValue().intValue());
        Assert.assertEquals("Wrong packet band count", 23, stat.getMeterBandStats().getBandStat().get(1)
                .getPacketBandCount().getValue().intValue());
        Assert.assertEquals("Wrong byte band count", 24, stat.getMeterBandStats().getBandStat().get(1)
                .getByteBandCount().getValue().intValue());
        stat = statUpdate.getMeterStats().get(1);
        Assert.assertEquals("Wrong meter-id", 150, stat.getMeterId().getValue().intValue());
        Assert.assertEquals("Wrong flow count", 160, stat.getFlowCount().getValue().intValue());
        Assert.assertEquals("Wrong packet in count", 170, stat.getPacketInCount().getValue().intValue());
        Assert.assertEquals("Wrong byte in count", 180, stat.getByteInCount().getValue().intValue());
        Assert.assertEquals("Wrong duration sec", 190, stat.getDuration().getSecond().getValue().intValue());
        Assert.assertEquals("Wrong duration n sec", 200, stat.getDuration().getNanosecond().getValue().intValue());
        Assert.assertEquals("Wrong meter band stats size", 0, stat.getMeterBandStats().getBandStat().size());
    }

    /**
     * Test {@link MultipartReplyTranslator#translate(SwitchConnectionDistinguisher, SessionContext, OfHeader)}
     * with empty meter config stats
     */
    @Test
    public void testEmptyMeterConfigStats() {
        MultipartReplyMessageBuilder mpBuilder = new MultipartReplyMessageBuilder();
        mpBuilder.setVersion((short) EncodeConstants.OF13_VERSION_ID);
        mpBuilder.setXid(123L);
        mpBuilder.setFlags(new MultipartRequestFlags(false));
        mpBuilder.setType(MultipartType.OFPMPMETERCONFIG);

        MultipartReplyMeterConfigCaseBuilder caseBuilder = new MultipartReplyMeterConfigCaseBuilder();
        MultipartReplyMeterConfigBuilder meterBuilder = new MultipartReplyMeterConfigBuilder();
        List<MeterConfig> meterStats = new ArrayList<>();
        meterBuilder.setMeterConfig(meterStats);
        caseBuilder.setMultipartReplyMeterConfig(meterBuilder.build());
        mpBuilder.setMultipartReplyBody(caseBuilder.build());
        MultipartReplyMessage message = mpBuilder.build();

        List<DataObject> list = translator.translate(cookie, sc, message);

        Assert.assertEquals("Wrong list size", 1, list.size());
        MeterConfigStatsUpdated statUpdate = (MeterConfigStatsUpdated) list.get(0);
        Assert.assertEquals("Wrong node-id", "openflow:42", statUpdate.getId().getValue());
        Assert.assertEquals("Wrong more-replies", false, statUpdate.isMoreReplies());
        Assert.assertEquals("Wrong transaction-id", 123, statUpdate.getTransactionId().getValue().intValue());
        Assert.assertEquals("Wrong meter config stats size", 0, statUpdate.getMeterConfigStats().size());
    }

    /**
     * Test {@link MultipartReplyTranslator#translate(SwitchConnectionDistinguisher, SessionContext, OfHeader)}
     * with meter config stats
     */
    @Test
    public void testMeterConfigStats() {
        MultipartReplyMessageBuilder mpBuilder = new MultipartReplyMessageBuilder();
        mpBuilder.setVersion((short) EncodeConstants.OF13_VERSION_ID);
        mpBuilder.setXid(123L);
        mpBuilder.setFlags(new MultipartRequestFlags(false));
        mpBuilder.setType(MultipartType.OFPMPMETERCONFIG);

        MultipartReplyMeterConfigCaseBuilder caseBuilder = new MultipartReplyMeterConfigCaseBuilder();
        MultipartReplyMeterConfigBuilder meterBuilder = new MultipartReplyMeterConfigBuilder();
        List<MeterConfig> meterStats = new ArrayList<>();
        MeterConfigBuilder builder = new MeterConfigBuilder();
        builder.setFlags(new MeterFlags(false, true, false, true));
        builder.setMeterId(new MeterId(20L));
        List<Bands> bands = new ArrayList<>();
        BandsBuilder bandBuilder = new BandsBuilder();
        MeterBandDropCaseBuilder dropCaseBuilder = new MeterBandDropCaseBuilder();
        MeterBandDropBuilder dropBuilder = new MeterBandDropBuilder();
        dropBuilder.setType(MeterBandType.OFPMBTDROP);
        dropBuilder.setRate(21L);
        dropBuilder.setBurstSize(22L);
        dropCaseBuilder.setMeterBandDrop(dropBuilder.build());
        bandBuilder.setMeterBand(dropCaseBuilder.build());
        bands.add(bandBuilder.build());
        bandBuilder = new BandsBuilder();
        MeterBandDscpRemarkCaseBuilder dscpCaseBuilder = new MeterBandDscpRemarkCaseBuilder();
        MeterBandDscpRemarkBuilder dscpBuilder = new MeterBandDscpRemarkBuilder();
        dscpBuilder.setType(MeterBandType.OFPMBTDSCPREMARK);
        dscpBuilder.setRate(23L);
        dscpBuilder.setBurstSize(24L);
        dscpBuilder.setPrecLevel((short) 25);
        dscpCaseBuilder.setMeterBandDscpRemark(dscpBuilder.build());
        bandBuilder.setMeterBand(dscpCaseBuilder.build());
        bands.add(bandBuilder.build());
        builder.setBands(bands);
        meterStats.add(builder.build());
        builder = new MeterConfigBuilder();
        builder.setFlags(new MeterFlags(true, false, true, false));
        builder.setMeterId(new MeterId(26L));
        bands = new ArrayList<>();
        builder.setBands(bands);
        meterStats.add(builder.build());
        meterBuilder.setMeterConfig(meterStats);
        caseBuilder.setMultipartReplyMeterConfig(meterBuilder.build());
        mpBuilder.setMultipartReplyBody(caseBuilder.build());
        MultipartReplyMessage message = mpBuilder.build();

        List<DataObject> list = translator.translate(cookie, sc, message);

        Assert.assertEquals("Wrong list size", 1, list.size());
        MeterConfigStatsUpdated statUpdate = (MeterConfigStatsUpdated) list.get(0);
        Assert.assertEquals("Wrong node-id", "openflow:42", statUpdate.getId().getValue());
        Assert.assertEquals("Wrong more-replies", false, statUpdate.isMoreReplies());
        Assert.assertEquals("Wrong transaction-id", 123, statUpdate.getTransactionId().getValue().intValue());
        Assert.assertEquals("Wrong meter config stats size", 2, statUpdate.getMeterConfigStats().size());
        MeterConfigStats stat = statUpdate.getMeterConfigStats().get(0);
        Assert.assertEquals("Wrong flag", false, stat.getFlags().isMeterBurst());
        Assert.assertEquals("Wrong flag", false, stat.getFlags().isMeterPktps());
        Assert.assertEquals("Wrong flag", true, stat.getFlags().isMeterKbps());
        Assert.assertEquals("Wrong flag", true, stat.getFlags().isMeterStats());
        Assert.assertEquals("Wrong meter-id", 20, stat.getMeterId().getValue().intValue());
        Assert.assertEquals("Wrong bands size", 2, stat.getMeterBandHeaders().getMeterBandHeader().size());
        MeterBandHeader header = stat.getMeterBandHeaders().getMeterBandHeader().get(0);
        Assert.assertEquals("Wrong band type", "org.opendaylight.yang.gen.v1.urn.opendaylight.meter"
                + ".types.rev130918.band.type.band.type.Drop", header.getBandType().getImplementedInterface().getName());
        Assert.assertEquals("Wrong band rate", 21, header.getBandRate().intValue());
        Assert.assertEquals("Wrong band burst size", 22, header.getBandBurstSize().intValue());
        Assert.assertEquals("Wrong flag", true, header.getMeterBandTypes().getFlags().isOfpmbtDrop());
        Assert.assertEquals("Wrong flag", false, header.getMeterBandTypes().getFlags().isOfpmbtDscpRemark());
        Assert.assertEquals("Wrong flag", false, header.getMeterBandTypes().getFlags().isOfpmbtExperimenter());
        header = stat.getMeterBandHeaders().getMeterBandHeader().get(1);
        Assert.assertEquals("Wrong band type", "org.opendaylight.yang.gen.v1.urn.opendaylight.meter"
                + ".types.rev130918.band.type.band.type.DscpRemark", header.getBandType().getImplementedInterface().getName());
        Assert.assertEquals("Wrong band rate", 23, header.getBandRate().intValue());
        Assert.assertEquals("Wrong band burst size", 24, header.getBandBurstSize().intValue());
        Assert.assertEquals("Wrong flag", false, header.getMeterBandTypes().getFlags().isOfpmbtDrop());
        Assert.assertEquals("Wrong flag", true, header.getMeterBandTypes().getFlags().isOfpmbtDscpRemark());
        Assert.assertEquals("Wrong flag", false, header.getMeterBandTypes().getFlags().isOfpmbtExperimenter());
        stat = statUpdate.getMeterConfigStats().get(1);
        Assert.assertEquals("Wrong flag", true, stat.getFlags().isMeterBurst());
        Assert.assertEquals("Wrong flag", true, stat.getFlags().isMeterPktps());
        Assert.assertEquals("Wrong flag", false, stat.getFlags().isMeterKbps());
        Assert.assertEquals("Wrong flag", false, stat.getFlags().isMeterStats());
        Assert.assertEquals("Wrong meter-id", 26, stat.getMeterId().getValue().intValue());
        Assert.assertEquals("Wrong bands size", 0, stat.getMeterBandHeaders().getMeterBandHeader().size());
    }
}