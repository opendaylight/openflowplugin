/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
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
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.BandId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.MeterBandType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.MeterFlags;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.MeterId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.band.type.band.type.DropBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.band.type.band.type.DscpRemarkBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.band.type.band.type.ExperimenterBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.meter.MeterBandHeadersBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.meter.config.stats.reply.MeterConfigStats;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.meter.config.stats.reply.MeterConfigStatsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.meter.config.stats.reply.MeterConfigStatsKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.meter.meter.band.headers.MeterBandHeader;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.meter.meter.band.headers.MeterBandHeaderBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.meter.meter.band.headers.MeterBandHeaderKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.meter.meter.band.headers.meter.band.header.MeterBandTypesBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.meter.band.header.meter.band.MeterBandDropCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.meter.band.header.meter.band.MeterBandDscpRemarkCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.meter.band.header.meter.band.MeterBandExperimenterCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.meter.band.header.meter.band.meter.band.drop._case.MeterBandDrop;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.meter.band.header.meter.band.meter.band.dscp.remark._case.MeterBandDscpRemark;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.meter.band.header.meter.band.meter.band.experimenter._case.MeterBandExperimenter;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.meter.config._case.multipart.reply.meter.config.MeterConfig;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.meter.config._case.multipart.reply.meter.config.meter.config.Bands;
import org.opendaylight.yangtools.yang.binding.DataContainer;

/**
 * Converts list of OF library config meter stats to MD-SAL config meter stats.
 *
 * Example usage:
 * <pre>
 * {@code
 * VersionConvertorData data = new VersionConvertorData(version);
 * Optional<List<MeterConfigStats>> salMeterConfigStats = convertorManager.convert(ofMeterConfigStats, data);
 * }
 * </pre>
 */
public class MeterConfigStatsResponseConvertor extends Convertor<List<MeterConfig>, List<MeterConfigStats>, VersionConvertorData> {

    private static final Set<Class<? extends DataContainer>> TYPES = Collections.singleton(MeterConfig.class);

    @Override
    public Collection<Class<? extends DataContainer>> getTypes() {
        return TYPES;
    }

    @Override
    public List<MeterConfigStats> convert(List<MeterConfig> source, VersionConvertorData data) {
        List<MeterConfigStats> listMeterConfigStats = new ArrayList<>();

        for (MeterConfig meterConfig : source) {
            MeterConfigStatsBuilder meterConfigStatsBuilder = new MeterConfigStatsBuilder();
            meterConfigStatsBuilder.setMeterId(new MeterId(meterConfig.getMeterId().getValue()));
            meterConfigStatsBuilder.setKey(new MeterConfigStatsKey(meterConfigStatsBuilder.getMeterId()));
            MeterBandHeadersBuilder meterBandHeadersBuilder = new MeterBandHeadersBuilder();
            List<Bands> bands = meterConfig.getBands();

            MeterFlags meterFlags = new MeterFlags(meterConfig.getFlags().isOFPMFBURST(),
                    meterConfig.getFlags().isOFPMFKBPS(),
                    meterConfig.getFlags().isOFPMFPKTPS(),
                    meterConfig.getFlags().isOFPMFSTATS());

            meterConfigStatsBuilder.setFlags(meterFlags);
            List<MeterBandHeader> listBandHeaders = new ArrayList<>();
            int bandKey = 0;

            for (Bands band : bands) {
                MeterBandHeaderBuilder meterBandHeaderBuilder = new MeterBandHeaderBuilder();
                if (band.getMeterBand() instanceof MeterBandDropCase) {
                    MeterBandDropCase dropCaseBand = (MeterBandDropCase) band.getMeterBand();
                    MeterBandDrop dropBand = dropCaseBand.getMeterBandDrop();
                    DropBuilder dropBuilder = new DropBuilder();
                    dropBuilder.setDropBurstSize(dropBand.getBurstSize());
                    dropBuilder.setDropRate(dropBand.getRate());
                    meterBandHeaderBuilder.setBandType(dropBuilder.build());

                    meterBandHeaderBuilder.setBandBurstSize(dropBand.getBurstSize());
                    meterBandHeaderBuilder.setBandRate(dropBand.getRate());
                    BandId bandId = new BandId((long) bandKey);
                    meterBandHeaderBuilder.setKey(new MeterBandHeaderKey(bandId));
                    meterBandHeaderBuilder.setBandId(bandId);

                    MeterBandTypesBuilder meterBandTypesBuilder = new MeterBandTypesBuilder();
                    meterBandTypesBuilder.setFlags(new MeterBandType(true, false, false));
                    meterBandHeaderBuilder.setMeterBandTypes(meterBandTypesBuilder.build());

                    listBandHeaders.add(meterBandHeaderBuilder.build());
                } else if (band.getMeterBand() instanceof MeterBandDscpRemarkCase) {
                    MeterBandDscpRemarkCase dscpRemarkCaseBand = (MeterBandDscpRemarkCase) band.getMeterBand();
                    MeterBandDscpRemark dscpRemarkBand = dscpRemarkCaseBand.getMeterBandDscpRemark();
                    DscpRemarkBuilder dscpRemarkBuilder = new DscpRemarkBuilder();
                    dscpRemarkBuilder.setDscpRemarkBurstSize(dscpRemarkBand.getBurstSize());
                    dscpRemarkBuilder.setDscpRemarkRate(dscpRemarkBand.getRate());
                    dscpRemarkBuilder.setPrecLevel(dscpRemarkBand.getPrecLevel());
                    meterBandHeaderBuilder.setBandType(dscpRemarkBuilder.build());

                    meterBandHeaderBuilder.setBandBurstSize(dscpRemarkBand.getBurstSize());
                    meterBandHeaderBuilder.setBandRate(dscpRemarkBand.getRate());
                    BandId bandId = new BandId((long) bandKey);
                    meterBandHeaderBuilder.setKey(new MeterBandHeaderKey(bandId));
                    meterBandHeaderBuilder.setBandId(bandId);

                    MeterBandTypesBuilder meterBandTypesBuilder = new MeterBandTypesBuilder();
                    meterBandTypesBuilder.setFlags(new MeterBandType(false, true, false));
                    meterBandHeaderBuilder.setMeterBandTypes(meterBandTypesBuilder.build());
                    listBandHeaders.add(meterBandHeaderBuilder.build());

                } else if (band.getMeterBand() instanceof MeterBandExperimenterCase) {
                    MeterBandExperimenterCase experimenterCaseBand = (MeterBandExperimenterCase) band.getMeterBand();
                    MeterBandExperimenter experimenterBand = experimenterCaseBand.getMeterBandExperimenter();
                    ExperimenterBuilder experimenterBuilder = new ExperimenterBuilder();
                    experimenterBuilder.setExperimenterBurstSize(experimenterBand.getBurstSize());
                    experimenterBuilder.setExperimenterRate(experimenterBand.getRate());
                    meterBandHeaderBuilder.setBandType(experimenterBuilder.build());

                    meterBandHeaderBuilder.setBandBurstSize(experimenterBand.getBurstSize());
                    meterBandHeaderBuilder.setBandRate(experimenterBand.getRate());
                    BandId bandId = new BandId((long) bandKey);
                    meterBandHeaderBuilder.setKey(new MeterBandHeaderKey(bandId));
                    meterBandHeaderBuilder.setBandId(bandId);

                    MeterBandTypesBuilder meterBandTypesBuilder = new MeterBandTypesBuilder();
                    meterBandTypesBuilder.setFlags(new MeterBandType(false, false, true));
                    meterBandHeaderBuilder.setMeterBandTypes(meterBandTypesBuilder.build());

                    listBandHeaders.add(meterBandHeaderBuilder.build());

                }

                bandKey++;
            }

            meterBandHeadersBuilder.setMeterBandHeader(listBandHeaders);
            meterConfigStatsBuilder.setMeterBandHeaders(meterBandHeadersBuilder.build());
            listMeterConfigStats.add(meterConfigStatsBuilder.build());
        }

        return listMeterConfigStats;
    }
}