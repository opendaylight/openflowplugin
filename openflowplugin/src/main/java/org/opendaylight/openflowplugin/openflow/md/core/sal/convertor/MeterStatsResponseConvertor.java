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
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.common.Convertor;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.data.VersionConvertorData;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev130715.Counter32;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev130715.Counter64;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.BandId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.MeterId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.meter.statistics.DurationBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.meter.statistics.MeterBandStatsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.meter.statistics.meter.band.stats.BandStat;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.meter.statistics.meter.band.stats.BandStatBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.meter.statistics.meter.band.stats.BandStatKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.meter.statistics.reply.MeterStats;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.meter.statistics.reply.MeterStatsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.meter._case.multipart.reply.meter.meter.stats.MeterBandStats;
import org.opendaylight.yangtools.binding.util.BindingMap;
import org.opendaylight.yangtools.yang.common.Uint32;

/**
 * Converts list of OF library meter stats to MD-SAL meter stats.
 *
 * <p>
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

    private static final Set<Class<?>> TYPES = Collections.singleton(org.opendaylight.yang.gen.v1.urn.opendaylight
            .openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.meter._case.multipart
                .reply.meter.MeterStats.class);

    @Override
    public Collection<Class<?>> getTypes() {
        return TYPES;
    }

    @Override
    public List<MeterStats> convert(final List<org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731
            .multipart.reply.multipart.reply.body.multipart.reply.meter._case.multipart.reply.meter.MeterStats> source,
            final VersionConvertorData data) {
        final List<MeterStats> convertedSALMeters = new ArrayList<>(source.size());

        for (var meterStats : source) {
            // Convert MeterStats message from library to MD SAL defined MeterStats
            List<MeterBandStats> allMeterBandStats = meterStats.nonnullMeterBandStats();

            final BindingMap.Builder<BandStatKey, BandStat> bandStats =
                BindingMap.orderedBuilder(allMeterBandStats.size());
            int bandKey = 0;

            for (MeterBandStats meterBandStats : allMeterBandStats) {
                bandStats.add(new BandStatBuilder()
                    .setByteBandCount(new Counter64(meterBandStats.getByteBandCount()))
                    .setPacketBandCount(new Counter64(meterBandStats.getPacketBandCount()))
                    .setBandId(new BandId(Uint32.valueOf(bandKey)))
                    .build());
                bandKey++;
            }

            convertedSALMeters.add(new MeterStatsBuilder()
                .setByteInCount(new Counter64(meterStats.getByteInCount()))
                .setDuration(new DurationBuilder()
                    .setSecond(new Counter32(meterStats.getDurationSec()))
                    .setNanosecond(new Counter32(meterStats.getDurationNsec()))
                    .build())
                .setFlowCount(new Counter32(meterStats.getFlowCount()))
                .setMeterId(new MeterId(meterStats.getMeterId().getValue()))
                .setPacketInCount(new Counter64(meterStats.getPacketInCount()))
                .setMeterBandStats(new MeterBandStatsBuilder().setBandStat(bandStats.build()).build())
                .build());
        }

        return convertedSALMeters;
    }
}
