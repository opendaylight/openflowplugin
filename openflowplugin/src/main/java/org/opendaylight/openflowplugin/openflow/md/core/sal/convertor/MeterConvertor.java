/**
 * Copyright (c) 2014 Ericsson India Global Services Pvt Ltd. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.openflow.md.core.sal.convertor;

/****
 *
 * This class is used for converting the data from SAL layer to OF Library Layer for Meter Mod Command.
 *
 */

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.service.rev130918.meter.update.UpdatedMeter;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.band.type.band.type.Drop;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.band.type.band.type.DscpRemark;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.band.type.band.type.Experimenter;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.meter.MeterBandHeaders;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.meter.meter.band.headers.MeterBandHeader;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.augments.rev150225.ExperimenterIdMeterBand;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.augments.rev150225.ExperimenterIdMeterBandBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.ExperimenterId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.MeterBandType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.MeterFlags;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.MeterId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.MeterModCommand;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.MeterModInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.meter.band.header.MeterBand;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.meter.band.header.meter.band.MeterBandDropCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.meter.band.header.meter.band.MeterBandDscpRemarkCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.meter.band.header.meter.band.MeterBandExperimenterCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.meter.band.header.meter.band.meter.band.drop._case.MeterBandDropBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.meter.band.header.meter.band.meter.band.dscp.remark._case.MeterBandDscpRemarkBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.meter.band.header.meter.band.meter.band.experimenter._case.MeterBandExperimenterBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.meter.mod.Bands;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.meter.mod.BandsBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class MeterConvertor {
    private static final Logger LOG = LoggerFactory.getLogger(MeterConvertor.class);

    private MeterConvertor() {

    }

    // Get all the data for the meter from the Yang/SAL-Layer
    /**
     * @param version of version
     * @param source Data source
     * @return MeterModInput required by OF Library
     */
    public static MeterModInputBuilder toMeterModInput(
            org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.Meter source, short version) {

        MeterModInputBuilder meterModInputBuilder = new MeterModInputBuilder();
        List<Bands> bands = new ArrayList<Bands>();

        if (source instanceof org.opendaylight.yang.gen.v1.urn.opendaylight.meter.service.rev130918.AddMeterInput) {
            meterModInputBuilder.setCommand(MeterModCommand.OFPMCADD);
        } else if (source instanceof org.opendaylight.yang.gen.v1.urn.opendaylight.meter.service.rev130918.RemoveMeterInput) {
            meterModInputBuilder.setCommand(MeterModCommand.OFPMCDELETE);
        } else if (source instanceof UpdatedMeter) {
            meterModInputBuilder.setCommand(MeterModCommand.OFPMCMODIFY);
        }

        meterModInputBuilder.setMeterId(new MeterId(source.getMeterId().getValue()));

        if (null != source.getFlags()) {
            meterModInputBuilder.setFlags(new MeterFlags(source.getFlags().isMeterBurst(), source.getFlags()
                .isMeterKbps(), source.getFlags().isMeterPktps(), source.getFlags().isMeterStats()));
        } else {

            /*
             * As per 0F1.3.1,The rate field indicates the rate value above
             * which the corresponding band may apply to packets (see 5.7.1).
             * The rate value is in kilobit per seconds, unless the flags eld
             * includes OFPMF_PKTPS, in which case the rate is in packets per
             * seconds.
             */

            meterModInputBuilder.setFlags(new MeterFlags(false, false, true, false));
        }
        if (source.getMeterBandHeaders() != null) {
            getBandsFromSAL(source.getMeterBandHeaders(), bands);
            meterModInputBuilder.setBands(bands);
        } else {
            LOG.error("For this meter Id" + source.getMeterId().getValue() + ",no associated band data found!");
        }

        meterModInputBuilder.setVersion(version);
        return meterModInputBuilder;
    }

    private static void getBandsFromSAL(MeterBandHeaders meterBandHeaders, List<Bands> bands) {

        Iterator<MeterBandHeader> bandHeadersIterator = meterBandHeaders.getMeterBandHeader().iterator();
        MeterBandHeader meterBandHeader;

        BandsBuilder bandsB = null;

        while (bandHeadersIterator.hasNext()) {
            meterBandHeader = bandHeadersIterator.next();
            MeterBand meterBandItem = null;
            // The band types :drop,DSCP_Remark or experimenter.
            if (null != meterBandHeader.getMeterBandTypes() &&
                    null != meterBandHeader.getMeterBandTypes().getFlags()) {

                if (meterBandHeader.getMeterBandTypes().getFlags().isOfpmbtDrop()) {
                    if (meterBandHeader.getBandType() != null) {
                        MeterBandDropCaseBuilder dropCaseBuilder = new MeterBandDropCaseBuilder();
                        MeterBandDropBuilder meterBandDropBuilder = new MeterBandDropBuilder();
                        meterBandDropBuilder.setType(MeterBandType.OFPMBTDROP);
                        Drop drop = (Drop) meterBandHeader.getBandType();
                        meterBandDropBuilder.setBurstSize(drop.getDropBurstSize());
                        meterBandDropBuilder.setRate(drop.getDropRate());
                        dropCaseBuilder.setMeterBandDrop(meterBandDropBuilder.build());
                        meterBandItem = dropCaseBuilder.build();
                        bandsB = new BandsBuilder();
                        bandsB.setMeterBand(meterBandItem);
                        // Bands list
                        bands.add(bandsB.build());
                    } else {
                        logBandTypeMissing(MeterBandType.OFPMBTDROP);
                    }
                } else if (meterBandHeader.getMeterBandTypes().getFlags().isOfpmbtDscpRemark()) {
                    if (meterBandHeader.getBandType() != null) {
                        MeterBandDscpRemarkCaseBuilder dscpCaseBuilder = new MeterBandDscpRemarkCaseBuilder();
                        MeterBandDscpRemarkBuilder meterBandDscpRemarkBuilder = new MeterBandDscpRemarkBuilder();
                        meterBandDscpRemarkBuilder.setType(MeterBandType.OFPMBTDSCPREMARK);
                        DscpRemark dscpRemark = (DscpRemark) meterBandHeader.getBandType();
                        meterBandDscpRemarkBuilder.setBurstSize(dscpRemark.getDscpRemarkBurstSize());
                        meterBandDscpRemarkBuilder.setRate(dscpRemark.getDscpRemarkRate());
                        meterBandDscpRemarkBuilder.setPrecLevel(dscpRemark.getPrecLevel());
                        dscpCaseBuilder.setMeterBandDscpRemark(meterBandDscpRemarkBuilder.build());
                        meterBandItem = dscpCaseBuilder.build();
                        bandsB = new BandsBuilder();
                        bandsB.setMeterBand(meterBandItem);
                        // Bands list
                        bands.add(bandsB.build());
                    } else {
                        logBandTypeMissing(MeterBandType.OFPMBTDSCPREMARK);
                    }
                } else if (meterBandHeader.getMeterBandTypes().getFlags().isOfpmbtExperimenter()) {
                    if (meterBandHeader.getBandType() != null) {
                        MeterBandExperimenterCaseBuilder experimenterCaseBuilder = new MeterBandExperimenterCaseBuilder();
                        MeterBandExperimenterBuilder meterBandExperimenterBuilder = new MeterBandExperimenterBuilder();
                        meterBandExperimenterBuilder.setType(MeterBandType.OFPMBTEXPERIMENTER);
                        Experimenter experimenter = (Experimenter) meterBandHeader.getBandType();
                        meterBandExperimenterBuilder.setBurstSize(experimenter.getExperimenterBurstSize());
                        meterBandExperimenterBuilder.setRate(experimenter.getExperimenterRate());
                        ExperimenterIdMeterBandBuilder expBuilder = new ExperimenterIdMeterBandBuilder();
                        expBuilder.setExperimenter(new ExperimenterId(experimenter.getExperimenter()));
                        meterBandExperimenterBuilder.addAugmentation(ExperimenterIdMeterBand.class, expBuilder.build());
                        // TODO - implement / finish experimenter meter band translation
                        experimenterCaseBuilder.setMeterBandExperimenter(meterBandExperimenterBuilder.build());
                        meterBandItem = experimenterCaseBuilder.build();
                        bandsB = new BandsBuilder();
                        bandsB.setMeterBand(meterBandItem);
                        // Bands list
                        bands.add(bandsB.build());
                    } else {
                        logBandTypeMissing(MeterBandType.OFPMBTEXPERIMENTER);
                    }
                }

            } else {
                LOG.error("Invalid meter band data found.");
            }
        }

    }

    private static void logBandTypeMissing(final MeterBandType meterBandType) {
        LOG.error("BandType: {} No Band Data found", meterBandType);
    }

}
