/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.openflow.md.core.sal.convertor;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import org.junit.Test;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.meter.config.stats.reply.MeterConfigStats;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.meter.statistics.meter.band.stats.BandStat;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.meter.statistics.reply.MeterStats;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.meter.statistics.reply.MeterStatsKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.MeterFlags;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.MeterId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.meter._case.multipart.reply.meter.MeterStatsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.meter._case.multipart.reply.meter.meter.stats.MeterBandStats;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.meter._case.multipart.reply.meter.meter.stats.MeterBandStatsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.meter.config._case.multipart.reply.meter.config.MeterConfig;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.meter.config._case.multipart.reply.meter.config.MeterConfigBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.meter.config._case.multipart.reply.meter.config.meter.config.Bands;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.meter.config._case.multipart.reply.meter.config.meter.config.BandsBuilder;
import org.opendaylight.yangtools.yang.binding.DataContainer;

/**
 * Created by Martin Bobak mbobak@cisco.com on 9/18/14.
 */
public class MeterStatsResponseConvertorTest {

    private static final MeterStatsResponseConvertor METER_STATS_RESPONSE_CONVERTOR = new MeterStatsResponseConvertor();
    private static final int PRESET_COUNT = 7;


    private static List<org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply
            .multipart.reply.body.multipart.reply.meter._case.multipart.reply.meter.MeterStats> createMeterStatsLit() {

        List<org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply
                .multipart.reply.body.multipart.reply.meter._case.multipart.reply.meter.MeterStats> allMeterStats = new ArrayList<>();
        MeterStatsBuilder meterStatsBuilder = new MeterStatsBuilder();
        for (int i = 0; i < PRESET_COUNT; i++) {
            meterStatsBuilder.setByteInCount(BigInteger.valueOf(i));
            meterStatsBuilder.setDurationNsec((long) 1000 * i);
            meterStatsBuilder.setDurationSec((long) 10 * i);
            meterStatsBuilder.setFlowCount((long) i);
            MeterBandStatsBuilder meterBandStatsBuilder = new MeterBandStatsBuilder();
            List<MeterBandStats> meterBandStatses = new ArrayList<>();
            for (int j = 0; j < PRESET_COUNT; j++) {
                meterBandStatsBuilder.setByteBandCount(BigInteger.valueOf(j));
                meterBandStatsBuilder.setPacketBandCount(BigInteger.valueOf(j));
                meterBandStatses.add(meterBandStatsBuilder.build());
            }
            meterStatsBuilder.setMeterBandStats(meterBandStatses);
            meterStatsBuilder.setMeterId(new MeterId((long) i));
            meterStatsBuilder.setPacketInCount(BigInteger.valueOf(i));

            allMeterStats.add(meterStatsBuilder.build());
        }

        return allMeterStats;
    }

    private final List<MeterConfig> createMeterConfigList(){
        List<MeterConfig> meterConfigs = new ArrayList<>();
        MeterConfigBuilder meterConfigBuilder = new MeterConfigBuilder();
        for (int i = 0; i < PRESET_COUNT; i++) {
            meterConfigBuilder.setMeterId(new MeterId((long) i));
            List<Bands> bandses = new ArrayList<>();
            for (int j = 0; j < PRESET_COUNT; j++){
                BandsBuilder bandsBuilder = new BandsBuilder();
                bandsBuilder.setMeterBand(new MockMeterBandBuilder());
                bandses.add(bandsBuilder.build());
            }
            meterConfigBuilder.setBands(bandses);
            meterConfigBuilder.setFlags(new MeterFlags(true, false, true, false));
            meterConfigs.add(meterConfigBuilder.build());
        }
        return meterConfigs;
    }
    @Test
    /**
     * Test of basic mapping functionality of {@link MeterStatsResponseConvertor#toSALMeterStatsList(java.util.List)}
     */
    public void testToSALMeterStatsList() {

        List<MeterStats> meterStatsList = METER_STATS_RESPONSE_CONVERTOR.toSALMeterStatsList(createMeterStatsLit());
        assertEquals(PRESET_COUNT, meterStatsList.size());

        int cnt = 0;
        for (MeterStats meterStats : meterStatsList) {
            assertEquals((new MeterStatsKey(new org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.MeterId((long) cnt))).getMeterId(), meterStats.getKey().getMeterId());
            assertEquals((BigInteger.valueOf(cnt)), meterStats.getByteInCount().getValue());
            assertEquals(new Long(1000 * cnt), meterStats.getDuration().getNanosecond().getValue());
            assertEquals(new Long(10 * cnt), meterStats.getDuration().getSecond().getValue());

            assertEquals(new Long(cnt), meterStats.getFlowCount().getValue());
            assertEquals(BigInteger.valueOf(cnt), meterStats.getByteInCount().getValue());

            assertEquals(PRESET_COUNT, meterStats.getMeterBandStats().getBandStat().size());
            int bandStatCount = 0;
            for (BandStat bandStat : meterStats.getMeterBandStats().getBandStat()) {
                assertEquals(BigInteger.valueOf(bandStatCount), bandStat.getByteBandCount().getValue());
                assertEquals(BigInteger.valueOf(bandStatCount), bandStat.getPacketBandCount().getValue());
                bandStatCount++;
            }
            assertEquals((new MeterStatsKey(new org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.MeterId((long) cnt))).getMeterId(), meterStats.getMeterId());
            assertEquals(BigInteger.valueOf(cnt), meterStats.getPacketInCount().getValue());
            cnt++;
        }
    }

    @Test
    /**
     * Test of basic mapping functionality of {@link MeterStatsResponseConvertor#toSALMeterStatsList(java.util.List)}
     */
    public void testToSALMeterConfigList() {
        List<MeterConfigStats> meterConfigs = METER_STATS_RESPONSE_CONVERTOR.toSALMeterConfigList(createMeterConfigList());
        assertEquals(PRESET_COUNT, meterConfigs.size());
        int cnt = 0;
        for (MeterConfigStats meterConfigStats: meterConfigs){
            assertEquals(new Long(cnt), meterConfigStats.getMeterId().getValue());
            assertTrue(meterConfigStats.getFlags().isMeterBurst());
            assertFalse(meterConfigStats.getFlags().isMeterKbps());
            assertTrue(meterConfigStats.getFlags().isMeterPktps());
            assertFalse(meterConfigStats.getFlags().isMeterStats());

            cnt++;
        }
    }


    private final class MockMeterBandBuilder implements org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.meter.band.header.MeterBand {

        @Override
        public Class<? extends DataContainer> getImplementedInterface() {
            return MockMeterBandBuilder.class;
        }
    }

}
