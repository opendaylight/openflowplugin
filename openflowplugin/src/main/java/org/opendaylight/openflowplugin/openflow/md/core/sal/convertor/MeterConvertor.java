/*
 * Copyright (c) 2014 Ericsson India Global Services Pvt Ltd. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.openflow.md.core.sal.convertor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.common.Convertor;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.data.VersionConvertorData;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.service.rev130918.AddMeterInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.service.rev130918.RemoveMeterInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.service.rev130918.meter.update.UpdatedMeter;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.Meter;
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
import org.opendaylight.yangtools.yang.binding.DataContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Converts a MD-SAL meter mod command into the OF library meter mod command.
 * <p>
 * Example usage:
 * <pre>
 * {@code
 * VersionConvertorData data = new VersionConvertorData(version);
 * Optional<MeterModInputBuilder> ofMeter = convertorManager.convert(salMeter, data);
 * }
 * </pre>
 */
public class MeterConvertor extends Convertor<Meter, MeterModInputBuilder, VersionConvertorData> {
    private static final Logger LOG = LoggerFactory.getLogger(MeterConvertor.class);
    private static final List<Class<? extends DataContainer>> TYPES = Arrays.asList(Meter.class, AddMeterInput.class, RemoveMeterInput.class, UpdatedMeter.class);

    /**
     * Create default empty meter mot input builder.
     * Use this method, if result from convertor is empty.
     *
     * @param version Openflow version
     * @return default empty meter mod input builder
     */
    public static MeterModInputBuilder defaultResult(short version) {
        return new MeterModInputBuilder()
                .setVersion(version)
                .setFlags(new MeterFlags(false, false, true, false));
    }

    private static void getBandsFromSAL(MeterBandHeaders meterBandHeaders, List<Bands> bands) {
        for (MeterBandHeader meterBandHeader : meterBandHeaders.getMeterBandHeader()) {
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
                        MeterBand meterBandItem = dropCaseBuilder.build();
                        BandsBuilder bandsB = new BandsBuilder();
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
                        MeterBand meterBandItem = dscpCaseBuilder.build();
                        BandsBuilder bandsB = new BandsBuilder();
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
                        MeterBand meterBandItem = experimenterCaseBuilder.build();
                        BandsBuilder bandsB = new BandsBuilder();
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

    @Override
    public Collection<Class<? extends DataContainer>> getTypes() {
        return  TYPES;
    }

    @Override
    public MeterModInputBuilder convert(Meter source, VersionConvertorData data) {
        MeterModInputBuilder meterModInputBuilder = new MeterModInputBuilder();
        List<Bands> bands = new ArrayList<>();

        if (source instanceof AddMeterInput) {
            meterModInputBuilder.setCommand(MeterModCommand.OFPMCADD);
        } else if (source instanceof RemoveMeterInput) {
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

        meterModInputBuilder.setVersion(data.getVersion());
        return meterModInputBuilder;
    }
}
