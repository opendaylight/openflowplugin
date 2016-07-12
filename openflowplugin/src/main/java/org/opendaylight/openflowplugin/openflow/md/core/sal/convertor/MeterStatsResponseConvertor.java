/*
 * Copyright (c) 2013, 2015 IBM Corporation and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.openflow.md.core.sal.convertor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev130715.Counter32;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev130715.Counter64;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.common.Convertor;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.data.VersionConvertorData;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.BandId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.MeterId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.meter.statistics.DurationBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.meter.statistics.MeterBandStatsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.meter.statistics.meter.band.stats.BandStat;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.meter.statistics.meter.band.stats.BandStatBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.meter.statistics.meter.band.stats.BandStatKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.meter.statistics.reply.MeterStats;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.meter.statistics.reply.MeterStatsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.meter.statistics.reply.MeterStatsKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.meter._case.multipart.reply.meter.meter.stats.MeterBandStats;
import org.opendaylight.yangtools.yang.binding.DataContainer;

/**
 * Converts list of OF library meter stats to MD-SAL meter stats.
 *
 * Example usage:
 * <pre>
 * {@code
 * VersionConvertorData data = new VersionConvertorData(version);
 * Optional<List<MeterStats>> salMeterStats = convertorManager.convert(ofMeterStats, data);
 * }
 * </pre>
 */
public class MeterStatsResponseConvertor extends Convertor<
        List<org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply
                .multipart.reply.body.multipart.reply.meter._case.multipart.reply.meter.MeterStats>,
        List<MeterStats>,
        VersionConvertorData> {

    private static final Set<Class<? extends DataContainer>> TYPES = Collections.singleton(org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.meter._case.multipart.reply.meter.MeterStats.class);

    @Override
    public Collection<Class<? extends DataContainer>> getTypes() {
        return TYPES;
    }

    @Override
    public List<MeterStats> convert(List<org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.meter._case.multipart.reply.meter.MeterStats> source, VersionConvertorData data) {
        List<MeterStats> convertedSALMeters = new ArrayList<>();

        for (org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.
                multipart.reply.body.multipart.reply.meter._case.multipart.reply.meter.MeterStats meterStats : source) {
            // Convert MeterStats message from library to MD SAL defined MeterStats
            MeterStatsBuilder salMeterStats = new MeterStatsBuilder();
            salMeterStats.setByteInCount(new Counter64(meterStats.getByteInCount()));

            DurationBuilder time = new DurationBuilder();
            time.setSecond(new Counter32(meterStats.getDurationSec()));
            time.setNanosecond(new Counter32(meterStats.getDurationNsec()));
            salMeterStats.setDuration(time.build());

            salMeterStats.setFlowCount(new Counter32(meterStats.getFlowCount()));
            salMeterStats.setMeterId(new MeterId(meterStats.getMeterId().getValue()));
            salMeterStats.setPacketInCount(new Counter64(meterStats.getPacketInCount()));
            salMeterStats.setKey(new MeterStatsKey(salMeterStats.getMeterId()));

            List<MeterBandStats> allMeterBandStats = meterStats.getMeterBandStats();

            MeterBandStatsBuilder meterBandStatsBuilder = new MeterBandStatsBuilder();
            List<BandStat> listAllBandStats = new ArrayList<>();
            int bandKey = 0;

            for (MeterBandStats meterBandStats : allMeterBandStats) {
                BandStatBuilder bandStatBuilder = new BandStatBuilder();
                bandStatBuilder.setByteBandCount(new Counter64(meterBandStats.getByteBandCount()));
                bandStatBuilder.setPacketBandCount(new Counter64(meterBandStats.getPacketBandCount()));
                BandId bandId = new BandId((long) bandKey);
                bandStatBuilder.setKey(new BandStatKey(bandId));
                bandStatBuilder.setBandId(bandId);
                bandKey++;
                listAllBandStats.add(bandStatBuilder.build());
            }

            meterBandStatsBuilder.setBandStat(listAllBandStats);
            salMeterStats.setMeterBandStats(meterBandStatsBuilder.build());
            convertedSALMeters.add(salMeterStats.build());
        }

        return convertedSALMeters;
    }
}