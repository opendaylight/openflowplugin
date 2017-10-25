/**
 * Copyright (c) 2014 Ericsson India Global Services Pvt Ltd. and others.  All rights reserved.
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
import java.util.List;
import java.util.Optional;
import org.junit.Before;
import org.junit.Test;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.data.VersionConvertorData;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.service.rev130918.AddMeterInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.service.rev130918.AddMeterInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.Meter;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.MeterBandType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.MeterFlags;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.band.type.band.type.Drop;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.band.type.band.type.DropBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.band.type.band.type.DscpRemark;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.band.type.band.type.DscpRemarkBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.band.type.band.type.Experimenter;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.band.type.band.type.ExperimenterBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.meter.MeterBandHeaders;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.meter.MeterBandHeadersBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.meter.meter.band.headers.MeterBandHeader;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.meter.meter.band.headers.MeterBandHeaderBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.meter.meter.band.headers.meter.band.header.MeterBandTypesBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.augments.rev150225.ExperimenterIdMeterBand;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.MeterModCommand;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.MeterModInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.meter.band.header.MeterBand;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.meter.band.header.meter.band.MeterBandDropCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.meter.band.header.meter.band.MeterBandDscpRemarkCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.meter.band.header.meter.band.MeterBandExperimenterCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.meter.mod.Bands;

public class MeterConvertorTest {
    private ConvertorManager convertorManager;

    @Before
    public void setUp() {
        convertorManager = ConvertorManagerFactory.createDefaultManager();
    }

    @Test
    public void testMeterModCommandConvertorwithAllParameters() {
        long BURST_SIZE = 10L;
        long DROP_RATE = 20L;
        // / DROP Band
        MeterBandHeaderBuilder meterBandHeaderBuilder = new MeterBandHeaderBuilder();
        MeterBandTypesBuilder meterBandTypesB = new MeterBandTypesBuilder();

        MeterBandType bandFlag = new MeterBandType(true, false, false);
        meterBandTypesB.setFlags(bandFlag);// _ofpmbtDrop
        DropBuilder drop = new DropBuilder();
        drop.setDropBurstSize(BURST_SIZE);
        drop.setDropRate(DROP_RATE);
        Drop drp = drop.build();
        meterBandHeaderBuilder.setBandType(drp);
        meterBandHeaderBuilder.setMeterBandTypes(meterBandTypesB.build());

        MeterBandHeader meterBH = meterBandHeaderBuilder.build();

        // DSCP Mark
        long DSCP_REMARK_BURST_SIZE = 11L;
        long DSCP_REMARK_RATE = 21L;
        short DSCP_PERC_LEVEL = 1;

        MeterBandHeaderBuilder meterBandHeaderBuilder1 = new MeterBandHeaderBuilder();
        MeterBandTypesBuilder meterBandTypesB1 = new MeterBandTypesBuilder();
        MeterBandType bandFlag1 = new MeterBandType(false, true, false);

        meterBandTypesB1.setFlags(bandFlag1);
        DscpRemarkBuilder dscp = new DscpRemarkBuilder();
        dscp.setDscpRemarkBurstSize(DSCP_REMARK_BURST_SIZE);
        dscp.setDscpRemarkRate(DSCP_REMARK_RATE);
        dscp.setPrecLevel(DSCP_PERC_LEVEL);
        DscpRemark dscpRemark = dscp.build();
        meterBandHeaderBuilder1.setBandType(dscpRemark);
        meterBandHeaderBuilder1.setMeterBandTypes(meterBandTypesB1.build());

        MeterBandHeader meterBH1 = meterBandHeaderBuilder1.build();

        // Experimental
        long EXP_BURST_SIZE = 12L;
        long EXP_RATE = 22L;
        long EXP_EXPERIMENTER = 23L;

        MeterBandHeaderBuilder meterBandHeaderBuilder2 = new MeterBandHeaderBuilder();
        MeterBandTypesBuilder meterBandTypesB2 = new MeterBandTypesBuilder();
        MeterBandType bandFlag2 = new MeterBandType(false, false, true);
        meterBandTypesB2.setFlags(bandFlag2);

        ExperimenterBuilder exp = new ExperimenterBuilder();
        exp.setExperimenterBurstSize(EXP_BURST_SIZE);
        exp.setExperimenterRate(EXP_RATE);
        exp.setExperimenter(EXP_EXPERIMENTER);
        Experimenter experimenter = exp.build();
        meterBandHeaderBuilder2.setBandType(experimenter);
        meterBandHeaderBuilder2.setMeterBandTypes(meterBandTypesB2.build());
        MeterBandHeader meterBH2 = meterBandHeaderBuilder2.build();

        List<MeterBandHeader> meterBandList = new ArrayList<MeterBandHeader>();
        meterBandList.add(0, meterBH);
        meterBandList.add(1, meterBH1);
        meterBandList.add(2, meterBH2);

        // Constructing List of Bands
        MeterBandHeadersBuilder meterBandHeadersBuilder = new MeterBandHeadersBuilder();
        meterBandHeadersBuilder.setMeterBandHeader(meterBandList);

        MeterBandHeaders meterBandHeaders = meterBandHeadersBuilder.build();

        AddMeterInputBuilder addMeterFromSAL = new AddMeterInputBuilder();

        addMeterFromSAL.setMeterBandHeaders(meterBandHeaders); // MeterBands
        // added to the
        // meter command.
        Long temp = 10L;

        // NodeKey key = new NodeKey(new NodeId("24"));
        // InstanceIdentifier<Node> path =
        // InstanceIdentifier.builder().node(Nodes.class).node(Node.class,
        // key).build();

        addMeterFromSAL
                .setMeterId(new org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.MeterId(10L));
        MeterFlags flagV = new MeterFlags(true, true, true, true);
        addMeterFromSAL.setFlags(flagV);

        AddMeterInput meterInputCommand = addMeterFromSAL.build();
        MeterModInputBuilder outMeterModInput = convert(meterInputCommand, new VersionConvertorData((short) 0X4));

        assertEquals(MeterModCommand.OFPMCADD, outMeterModInput.getCommand());
        assertTrue(outMeterModInput.getFlags().isOFPMFBURST());
        assertEquals(temp, outMeterModInput.getMeterId().getValue());
        // BandInformation starts here:

        List<Bands> bands = outMeterModInput.getBands();
        for (Bands currentBand : bands) {
            MeterBand meterBand = currentBand.getMeterBand();
            if (meterBand instanceof MeterBandDropCase) {

                assertEquals(
                        org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.MeterBandType.OFPMBTDROP,
                        ((MeterBandDropCase) meterBand).getMeterBandDrop().getType());
                assertEquals(BURST_SIZE, (long) ((MeterBandDropCase) meterBand).getMeterBandDrop().getBurstSize());
                assertEquals(DROP_RATE, (long) ((MeterBandDropCase) meterBand).getMeterBandDrop().getRate());

            }
            if (meterBand instanceof MeterBandDscpRemarkCase) {
                assertEquals(
                        org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.MeterBandType.OFPMBTDSCPREMARK,
                        ((MeterBandDscpRemarkCase) meterBand).getMeterBandDscpRemark().getType());
                assertEquals(DSCP_REMARK_BURST_SIZE, (long) ((MeterBandDscpRemarkCase) meterBand).getMeterBandDscpRemark().getBurstSize());
                assertEquals(DSCP_REMARK_RATE, (long) ((MeterBandDscpRemarkCase) meterBand).getMeterBandDscpRemark().getRate());
                assertEquals(DSCP_PERC_LEVEL, (short) ((MeterBandDscpRemarkCase) meterBand).getMeterBandDscpRemark().getPrecLevel());

            }
            if (meterBand instanceof MeterBandExperimenterCase) {
                assertEquals(
                        org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.MeterBandType.OFPMBTEXPERIMENTER,
                        ((MeterBandExperimenterCase) meterBand).getMeterBandExperimenter().getType());
                assertEquals(EXP_BURST_SIZE, (long) ((MeterBandExperimenterCase) meterBand).getMeterBandExperimenter().getBurstSize());
                assertEquals(EXP_RATE, (long) ((MeterBandExperimenterCase) meterBand).getMeterBandExperimenter().getRate());
                ExperimenterIdMeterBand expBand = ((MeterBandExperimenterCase) meterBand).getMeterBandExperimenter()
                        .getAugmentation(ExperimenterIdMeterBand.class);
                assertEquals(EXP_EXPERIMENTER, (long) expBand.getExperimenter().getValue());

            }

        }

    }

    @Test
    public void testMeterModCommandConvertorwithNoFlags() {
        long BURST_SIZE = 10L;
        long DROP_RATE = 20L;
        // / DROP Band
        MeterBandHeaderBuilder meterBandHeaderBuilder = new MeterBandHeaderBuilder();
        MeterBandTypesBuilder meterBandTypesB = new MeterBandTypesBuilder();

        MeterBandType bandFlag = new MeterBandType(true, false, false);
        meterBandTypesB.setFlags(bandFlag);// _ofpmbtDrop
        DropBuilder drop = new DropBuilder();
        drop.setDropBurstSize(BURST_SIZE);
        drop.setDropRate(DROP_RATE);
        Drop drp = drop.build();
        meterBandHeaderBuilder.setBandType(drp);
        meterBandHeaderBuilder.setMeterBandTypes(meterBandTypesB.build());

        MeterBandHeader meterBH = meterBandHeaderBuilder.build();

        // DSCP Mark
        long DSCP_REMARK_BURST_SIZE = 11L;
        long DSCP_REMARK_RATE = 21L;
        short DSCP_PERC_LEVEL = 1;

        MeterBandHeaderBuilder meterBandHeaderBuilder1 = new MeterBandHeaderBuilder();
        MeterBandTypesBuilder meterBandTypesB1 = new MeterBandTypesBuilder();
        MeterBandType bandFlag1 = new MeterBandType(false, true, false);

        meterBandTypesB1.setFlags(bandFlag1);
        DscpRemarkBuilder dscp = new DscpRemarkBuilder();

        dscp.setDscpRemarkBurstSize(DSCP_REMARK_BURST_SIZE);
        dscp.setDscpRemarkRate(DSCP_REMARK_RATE);
        dscp.setPrecLevel(DSCP_PERC_LEVEL);
        DscpRemark dscpRemark = dscp.build();
        meterBandHeaderBuilder1.setBandType(dscpRemark);
        meterBandHeaderBuilder1.setMeterBandTypes(meterBandTypesB1.build());

        MeterBandHeader meterBH1 = meterBandHeaderBuilder1.build();

        // Experimental
        long EXP_BURST_SIZE = 12L;
        long EXP_RATE = 22L;
        long EXP_EXPERIMENTER = 23L;

        MeterBandHeaderBuilder meterBandHeaderBuilder2 = new MeterBandHeaderBuilder();
        MeterBandTypesBuilder meterBandTypesB2 = new MeterBandTypesBuilder();
        MeterBandType bandFlag2 = new MeterBandType(false, false, true);
        meterBandTypesB2.setFlags(bandFlag2);

        ExperimenterBuilder exp = new ExperimenterBuilder();

        exp.setExperimenterBurstSize(EXP_BURST_SIZE);
        exp.setExperimenterRate(EXP_RATE);
        exp.setExperimenter(EXP_EXPERIMENTER);
        Experimenter experimenter = exp.build();
        meterBandHeaderBuilder2.setBandType(experimenter);
        meterBandHeaderBuilder2.setMeterBandTypes(meterBandTypesB2.build());
        MeterBandHeader meterBH2 = meterBandHeaderBuilder2.build();

        List<MeterBandHeader> meterBandList = new ArrayList<MeterBandHeader>();
        meterBandList.add(0, meterBH);
        meterBandList.add(1, meterBH1);
        meterBandList.add(2, meterBH2);

        // Constructing List of Bands
        MeterBandHeadersBuilder meterBandHeadersBuilder = new MeterBandHeadersBuilder();
        meterBandHeadersBuilder.setMeterBandHeader(meterBandList);

        MeterBandHeaders meterBandHeaders = meterBandHeadersBuilder.build();

        AddMeterInputBuilder addMeterFromSAL = new AddMeterInputBuilder();

        addMeterFromSAL.setMeterBandHeaders(meterBandHeaders); // MeterBands
        // added to the
        // meter command.
        Long temp = 10L;

        // NodeKey key = new NodeKey(new NodeId("24"));
        // InstanceIdentifier<Node> path =
        // InstanceIdentifier.builder().node(Nodes.class).node(Node.class,
        // key).build();

        addMeterFromSAL
                .setMeterId(new org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.MeterId(10L));


        AddMeterInput meterInputCommand = addMeterFromSAL.build();
        MeterModInputBuilder outMeterModInput = convert(meterInputCommand, new VersionConvertorData((short) 0X4));

        assertEquals(MeterModCommand.OFPMCADD, outMeterModInput.getCommand());
        assertFalse(outMeterModInput.getFlags().isOFPMFBURST());
        assertTrue(outMeterModInput.getFlags().isOFPMFPKTPS());
        assertEquals(temp, outMeterModInput.getMeterId().getValue());
        // BandInformation starts here:

        List<Bands> bands = outMeterModInput.getBands();
        for (Bands currentBand : bands) {
            MeterBand meterBand = currentBand.getMeterBand();
            if (meterBand instanceof MeterBandDropCase) {

                assertEquals(
                        org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.MeterBandType.OFPMBTDROP,
                        ((MeterBandDropCase) meterBand).getMeterBandDrop().getType());
                assertEquals(BURST_SIZE, (long) ((MeterBandDropCase) meterBand).getMeterBandDrop().getBurstSize());
                assertEquals(DROP_RATE, (long) ((MeterBandDropCase) meterBand).getMeterBandDrop().getRate());

            }
            if (meterBand instanceof MeterBandDscpRemarkCase) {
                assertEquals(
                        org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.MeterBandType.OFPMBTDSCPREMARK,
                        ((MeterBandDscpRemarkCase) meterBand).getMeterBandDscpRemark().getType());
                assertEquals(DSCP_REMARK_BURST_SIZE, (long) ((MeterBandDscpRemarkCase) meterBand).getMeterBandDscpRemark().getBurstSize());
                assertEquals(DSCP_REMARK_RATE, (long) ((MeterBandDscpRemarkCase) meterBand).getMeterBandDscpRemark().getRate());
                assertEquals(DSCP_PERC_LEVEL, (short) ((MeterBandDscpRemarkCase) meterBand).getMeterBandDscpRemark().getPrecLevel());

            }
            if (meterBand instanceof MeterBandExperimenterCase) {
                assertEquals(
                        org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.MeterBandType.OFPMBTEXPERIMENTER,
                        ((MeterBandExperimenterCase) meterBand).getMeterBandExperimenter().getType());
                assertEquals(EXP_BURST_SIZE, (long) ((MeterBandExperimenterCase) meterBand).getMeterBandExperimenter().getBurstSize());
                assertEquals(EXP_RATE, (long) ((MeterBandExperimenterCase) meterBand).getMeterBandExperimenter().getRate());
                ExperimenterIdMeterBand expBand = ((MeterBandExperimenterCase) meterBand).getMeterBandExperimenter()
                        .getAugmentation(ExperimenterIdMeterBand.class);
                assertEquals(EXP_EXPERIMENTER, (long) expBand.getExperimenter().getValue());

            }

        }

    }

    @Test
    public void testMeterModCommandConvertorBandDataisNULL() {
        AddMeterInputBuilder addMeterFromSAL = new AddMeterInputBuilder();

        Long temp = 10L;

        // NodeKey key = new NodeKey(new NodeId("24"));
        // InstanceIdentifier<Node> path =
        // InstanceIdentifier.builder().node(Nodes.class).node(Node.class,
        // key).build();

        addMeterFromSAL
                .setMeterId(new org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.MeterId(10L));
        MeterFlags flagV = new MeterFlags(true, true, true, true);
        addMeterFromSAL.setFlags(flagV);

        AddMeterInput meterInputCommand = addMeterFromSAL.build();
        MeterModInputBuilder outMeterModInput = convert(meterInputCommand, new VersionConvertorData((short) 0X4));

        assertEquals(MeterModCommand.OFPMCADD, outMeterModInput.getCommand());
        assertTrue(outMeterModInput.getFlags().isOFPMFBURST());
        assertEquals(temp, outMeterModInput.getMeterId().getValue());
    }

    @Test
    public void testMeterModCommandConvertorNoValidBandData() {

        // / DROP Band
        MeterBandHeaderBuilder meterBandHeaderBuilder = new MeterBandHeaderBuilder();
        MeterBandTypesBuilder meterBandTypesB = new MeterBandTypesBuilder();

        MeterBandType bandFlag = new MeterBandType(true, false, false);
        meterBandTypesB.setFlags(bandFlag);// _ofpmbtDrop

        meterBandHeaderBuilder.setMeterBandTypes(meterBandTypesB.build());

        MeterBandHeader meterBH = meterBandHeaderBuilder.build();

        // DSCP Mark
        MeterBandHeaderBuilder meterBandHeaderBuilder1 = new MeterBandHeaderBuilder();
        MeterBandTypesBuilder meterBandTypesB1 = new MeterBandTypesBuilder();
        MeterBandType bandFlag1 = new MeterBandType(false, true, false);

        meterBandTypesB1.setFlags(bandFlag1);
        DscpRemarkBuilder dscp = new DscpRemarkBuilder();
        dscp.setDscpRemarkBurstSize(11L);
        dscp.setDscpRemarkRate(21L);
        dscp.setPrecLevel((short) 1);
        DscpRemark dscpRemark = dscp.build();
        meterBandHeaderBuilder1.setBandType(dscpRemark);
        meterBandHeaderBuilder1.setMeterBandTypes(meterBandTypesB1.build());

        MeterBandHeader meterBH1 = meterBandHeaderBuilder1.build();

        // Experimental

        MeterBandHeaderBuilder meterBandHeaderBuilder2 = new MeterBandHeaderBuilder();
        MeterBandTypesBuilder meterBandTypesB2 = new MeterBandTypesBuilder();


        ExperimenterBuilder exp = new ExperimenterBuilder();
        exp.setExperimenterBurstSize(12L);
        exp.setExperimenterRate(22L);
        exp.setExperimenter(23L);
        Experimenter experimenter = exp.build();
        meterBandHeaderBuilder2.setBandType(experimenter);
        meterBandHeaderBuilder2.setMeterBandTypes(meterBandTypesB2.build());
        MeterBandHeader meterBH2 = meterBandHeaderBuilder2.build();

        List<MeterBandHeader> meterBandList = new ArrayList<MeterBandHeader>();
        meterBandList.add(0, meterBH);
        meterBandList.add(1, meterBH1);
        meterBandList.add(2, meterBH2);

        // Constructing List of Bands
        MeterBandHeadersBuilder meterBandHeadersBuilder = new MeterBandHeadersBuilder();
        meterBandHeadersBuilder.setMeterBandHeader(meterBandList);

        MeterBandHeaders meterBandHeaders = meterBandHeadersBuilder.build();

        AddMeterInputBuilder addMeterFromSAL = new AddMeterInputBuilder();

        addMeterFromSAL.setMeterBandHeaders(meterBandHeaders); // MeterBands
        // added to the
        // meter command.
        Long temp = 10L;

        // NodeKey key = new NodeKey(new NodeId("24"));
        // InstanceIdentifier<Node> path =
        // InstanceIdentifier.builder().node(Nodes.class).node(Node.class,
        // key).build();

        addMeterFromSAL
                .setMeterId(new org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.MeterId(10L));
        MeterFlags flagV = new MeterFlags(true, true, true, true);
        addMeterFromSAL.setFlags(flagV);

        AddMeterInput meterInputCommand = addMeterFromSAL.build();
        MeterModInputBuilder outMeterModInput = convert(meterInputCommand, new VersionConvertorData((short) 0X4));

        assertEquals(MeterModCommand.OFPMCADD, outMeterModInput.getCommand());
        assertTrue(outMeterModInput.getFlags().isOFPMFBURST());
        assertEquals(temp, outMeterModInput.getMeterId().getValue());
        // BandInformation starts here:

        List<Bands> bands = outMeterModInput.getBands();
        for (Bands currentBand : bands) {
            MeterBand meterBand = currentBand.getMeterBand();
            if (meterBand instanceof MeterBandDropCase) {

                assertEquals(
                        org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.MeterBandType.OFPMBTDROP,
                        ((MeterBandDropCase) meterBand).getMeterBandDrop().getType());
                assertEquals((long) 20, (long) ((MeterBandDropCase) meterBand).getMeterBandDrop().getBurstSize());
                assertEquals((long) 10, (long) ((MeterBandDropCase) meterBand).getMeterBandDrop().getRate());

            }
            if (meterBand instanceof MeterBandDscpRemarkCase) {
                assertEquals(
                        org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.MeterBandType.OFPMBTDSCPREMARK,
                        ((MeterBandDscpRemarkCase) meterBand).getMeterBandDscpRemark().getType());
                assertEquals((long) 11, (long) ((MeterBandDscpRemarkCase) meterBand).getMeterBandDscpRemark().getBurstSize());
                assertEquals((long) 21, (long) ((MeterBandDscpRemarkCase) meterBand).getMeterBandDscpRemark().getRate());
                assertEquals((short) 1, (short) ((MeterBandDscpRemarkCase) meterBand).getMeterBandDscpRemark().getPrecLevel());

            }
            if (meterBand instanceof MeterBandExperimenterCase) {
                assertEquals(
                        org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.MeterBandType.OFPMBTEXPERIMENTER,
                        ((MeterBandExperimenterCase) meterBand).getMeterBandExperimenter().getType());
                assertEquals((long) 12, (long) ((MeterBandExperimenterCase) meterBand).getMeterBandExperimenter().getBurstSize());
                assertEquals((long) 22, (long) ((MeterBandExperimenterCase) meterBand).getMeterBandExperimenter().getRate());
                ExperimenterIdMeterBand expBand = ((MeterBandExperimenterCase) meterBand).getMeterBandExperimenter()
                        .getAugmentation(ExperimenterIdMeterBand.class);
                assertEquals((long) 23, (long) expBand.getExperimenter().getValue());

            }

        }

    }

    private MeterModInputBuilder convert(Meter source, VersionConvertorData data) {
        Optional<MeterModInputBuilder> outMeterModInputOptional = convertorManager.convert(source, data);
        return outMeterModInputOptional.orElse(MeterConvertor.defaultResult(data.getVersion()));
    }
}