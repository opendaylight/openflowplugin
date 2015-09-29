/*
 * Copyright (c) 2013, 2015 IBM Corporation and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.openflow.md.core.sal.convertor;

import java.util.ArrayList;
import java.util.List;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev100924.Counter32;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev100924.Counter64;
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
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.meter.statistics.DurationBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.meter.statistics.MeterBandStatsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.meter.statistics.meter.band.stats.BandStat;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.meter.statistics.meter.band.stats.BandStatBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.meter.statistics.meter.band.stats.BandStatKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.meter.statistics.reply.MeterStats;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.meter.statistics.reply.MeterStatsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.meter.statistics.reply.MeterStatsKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.meter.band.header.meter.band.MeterBandDropCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.meter.band.header.meter.band.MeterBandDscpRemarkCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.meter.band.header.meter.band.MeterBandExperimenterCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.meter.band.header.meter.band.meter.band.drop._case.MeterBandDrop;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.meter.band.header.meter.band.meter.band.dscp.remark._case.MeterBandDscpRemark;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.meter.band.header.meter.band.meter.band.experimenter._case.MeterBandExperimenter;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.meter._case.multipart.reply.meter.meter.stats.MeterBandStats;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.meter.config._case.multipart.reply.meter.config.MeterConfig;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.meter.config._case.multipart.reply.meter.config.meter.config.Bands;

/**
 * Class is an utility class for converting group related statistics messages coming from switch to MD-SAL
 * messages.
 *
 * @author avishnoi@in.ibm.com
 */
public class MeterStatsResponseConvertor {

    /**
     * Method converts list of OF Meter Stats to SAL Meter Stats.
     *
     * @param allMeterStats all meter stats
     * @return List of MeterStats
     */
    public List<MeterStats> toSALMeterStatsList(
            List<org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply
                    .multipart.reply.body.multipart.reply.meter._case.multipart.reply.meter.MeterStats> allMeterStats) {
        List<MeterStats> convertedSALMeters = new ArrayList<MeterStats>();
        for (org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.
                multipart.reply.body.multipart.reply.meter._case.multipart.reply.meter.MeterStats meter : allMeterStats) {
            convertedSALMeters.add(toSALMeterStats(meter));
        }
        return convertedSALMeters;

    }

    /**
     * Method convert MeterStats message from library to MD SAL defined MeterStats
     *
     * @param meterStats MeterStats from library
     * @return MeterStats -- MeterStats defined in MD-SAL
     */
    public MeterStats toSALMeterStats(org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply
                                              .multipart.reply.body.multipart.reply.meter._case.multipart.reply.meter.MeterStats meterStats) {

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
        List<BandStat> listAllBandStats = new ArrayList<BandStat>();
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
        return salMeterStats.build();
    }

    /**
     * Method convert list of OF Meter config Stats to SAL Meter Config stats
     *
     * @param allMeterConfigs all meter configs
     * @return list of MeterConfigStats
     */
    public List<MeterConfigStats> toSALMeterConfigList(List<MeterConfig> allMeterConfigs) {

        List<MeterConfigStats> listMeterConfigStats = new ArrayList<MeterConfigStats>();
        for (MeterConfig meterConfig : allMeterConfigs) {
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
            List<MeterBandHeader> listBandHeaders = new ArrayList<MeterBandHeader>();
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
