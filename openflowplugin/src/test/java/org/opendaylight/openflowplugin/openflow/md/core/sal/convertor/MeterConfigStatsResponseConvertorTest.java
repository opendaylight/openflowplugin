/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.openflow.md.core.sal.convertor;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.junit.Test;
import org.opendaylight.openflowplugin.api.OFConstants;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.data.VersionConvertorData;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.meter.config.stats.reply.MeterConfigStats;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.MeterFlags;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.MeterId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.meter.band.header.meter.band.MeterBandDropCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.meter.band.header.meter.band.MeterBandDscpRemarkCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.meter.band.header.meter.band.MeterBandExperimenterCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.meter.band.header.meter.band.meter.band.drop._case.MeterBandDropBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.meter.band.header.meter.band.meter.band.dscp.remark._case.MeterBandDscpRemarkBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.meter.band.header.meter.band.meter.band.experimenter._case.MeterBandExperimenterBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.meter.config._case.multipart.reply.meter.config.MeterConfig;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.meter.config._case.multipart.reply.meter.config.MeterConfigBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.meter.config._case.multipart.reply.meter.config.meter.config.Bands;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.meter.config._case.multipart.reply.meter.config.meter.config.BandsBuilder;
import org.opendaylight.yangtools.yang.binding.DataContainer;

public class MeterConfigStatsResponseConvertorTest {
    private static final int PRESET_COUNT = 7;

    private List<MeterConfig> createMeterConfigList(){
        List<MeterConfig> meterConfigs = new ArrayList<>();
        MeterConfigBuilder meterConfigBuilder = new MeterConfigBuilder();
        for (int i = 0; i < PRESET_COUNT; i++) {
            meterConfigBuilder.setMeterId(new MeterId((long) i));
            List<Bands> bandses = new ArrayList<>();

            BandsBuilder bandsBuilder = new BandsBuilder();
            bandsBuilder.setMeterBand(new MeterBandDropCaseBuilder()
                    .setMeterBandDrop(new MeterBandDropBuilder().build()).build());
            bandses.add(bandsBuilder.build());

            bandsBuilder = new BandsBuilder();
            bandsBuilder.setMeterBand(new MeterBandDscpRemarkCaseBuilder()
                    .setMeterBandDscpRemark(new MeterBandDscpRemarkBuilder().build()).build());
            bandses.add(bandsBuilder.build());

            bandsBuilder = new BandsBuilder();
            bandsBuilder.setMeterBand(new MockMeterBandBuilder());
            bandses.add(bandsBuilder.build());

            bandsBuilder = new BandsBuilder();
            bandsBuilder.setMeterBand(new MeterBandExperimenterCaseBuilder()
                    .setMeterBandExperimenter(new MeterBandExperimenterBuilder().build()).build());
            bandses.add(bandsBuilder.build());

            meterConfigBuilder.setBands(bandses);
            meterConfigBuilder.setFlags(new MeterFlags(true, false, true, false));
            meterConfigs.add(meterConfigBuilder.build());
        }

        return meterConfigs;
    }

    @Test
    /**
     * Test of basic mapping functionality of {@link MeterConfigStatsResponseConvertor#convert(java.util.List)} }
     */
    public void testToSALMeterConfigList() {
        final ConvertorManager convertorManager = ConvertorManagerFactory.createDefaultManager();
        Optional<List<MeterConfigStats>> meterConfigsOptional = convertorManager.convert(createMeterConfigList(), new VersionConvertorData(OFConstants.OFP_VERSION_1_3));
        List<MeterConfigStats> meterConfigs = meterConfigsOptional.orElse(Collections.emptyList());

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