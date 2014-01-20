/**
 * Copyright (c) 2014 Ericsson India Global Services Pvt Ltd. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributor: usha.m.s@ericsson.com
 */
package org.opendaylight.openflowplugin.openflow.md.core.sal.convertor;

import java.util.ArrayList;
import java.util.List;

import junit.framework.Assert;

import org.junit.Test;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.service.rev130918.AddMeterInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.service.rev130918.AddMeterInputBuilder;
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
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.MeterModCommand;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.MeterModInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.MeterModInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.meter.band.header.MeterBand;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.meter.band.header.meter.band.MeterBandDropCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.meter.band.header.meter.band.MeterBandDscpRemarkCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.meter.band.header.meter.band.MeterBandExperimenterCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.meter.mod.Bands;

public class MeterConvertorTest {

    @Test
    public void testMeterModCommandConvertorwithAllParameters() {

        // / DROP Band
        MeterBandHeaderBuilder meterBandHeaderBuilder = new MeterBandHeaderBuilder();
        MeterBandTypesBuilder meterBandTypesB = new MeterBandTypesBuilder();

        MeterBandType bandFlag = new MeterBandType(true, false, false);
        meterBandTypesB.setFlags(bandFlag);// _ofpmbtDrop
        DropBuilder drop = new DropBuilder();
        drop.setDropBurstSize(10L);
        drop.setDropRate(20L);
        Drop drp = drop.build();
        meterBandHeaderBuilder.setBandType(drp);
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
        dscp.setPercLevel((short) 1);
        DscpRemark dscpRemark = dscp.build();
        meterBandHeaderBuilder1.setBandType(dscpRemark);
        meterBandHeaderBuilder1.setMeterBandTypes(meterBandTypesB1.build());

        MeterBandHeader meterBH1 = meterBandHeaderBuilder1.build();

        // Experimental

        MeterBandHeaderBuilder meterBandHeaderBuilder2 = new MeterBandHeaderBuilder();
        MeterBandTypesBuilder meterBandTypesB2 = new MeterBandTypesBuilder();
        MeterBandType bandFlag2 = new MeterBandType(false, false, true);
        meterBandTypesB2.setFlags(bandFlag2);

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
        // key).toInstance();

        addMeterFromSAL
                .setMeterId(new org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.MeterId(10L));
        MeterFlags flagV = new MeterFlags(true, true, true, true);
        addMeterFromSAL.setFlags(flagV);

        AddMeterInput meterInputCommand = addMeterFromSAL.build();
        MeterModInputBuilder outMeterModInput = MeterConvertor.toMeterModInput(meterInputCommand, (short) 0X4);

        Assert.assertEquals(MeterModCommand.OFPMCADD, outMeterModInput.getCommand());
        Assert.assertTrue(outMeterModInput.getFlags().isOFPMFBURST());
        Assert.assertEquals(temp, outMeterModInput.getMeterId().getValue());
        // BandInformation starts here:

        List<Bands> bands = outMeterModInput.getBands();
        for (Bands currentBand : bands) {
            MeterBand meterBand = currentBand.getMeterBand();
            if (meterBand instanceof MeterBandDropCase) {

                Assert.assertEquals(
                        org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.MeterBandType.OFPMBTDROP,
                        ((MeterBandDropCase) meterBand).getMeterBandDrop().getType());
                Assert.assertEquals((long) 20, (long) ((MeterBandDropCase) meterBand).getMeterBandDrop().getBurstSize());
                Assert.assertEquals((long) 10, (long) ((MeterBandDropCase) meterBand).getMeterBandDrop().getRate());

            }
            if (meterBand instanceof MeterBandDscpRemarkCase) {
                Assert.assertEquals(
                        org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.MeterBandType.OFPMBTDSCPREMARK,
                        ((MeterBandDscpRemarkCase) meterBand).getMeterBandDscpRemark().getType());
                Assert.assertEquals((long) 11, (long) ((MeterBandDscpRemarkCase) meterBand).getMeterBandDscpRemark().getBurstSize());
                Assert.assertEquals((long) 21, (long) ((MeterBandDscpRemarkCase) meterBand).getMeterBandDscpRemark().getRate());
                Assert.assertEquals((short) 1, (short) ((MeterBandDscpRemarkCase) meterBand).getMeterBandDscpRemark().getPrecLevel());

            }
            if (meterBand instanceof MeterBandExperimenterCase) {
                Assert.assertEquals(
                        org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.MeterBandType.OFPMBTEXPERIMENTER,
                        ((MeterBandExperimenterCase) meterBand).getMeterBandExperimenter().getType());
                Assert.assertEquals((long) 12, (long) ((MeterBandExperimenterCase) meterBand).getMeterBandExperimenter().getBurstSize());
                Assert.assertEquals((long) 22, (long) ((MeterBandExperimenterCase) meterBand).getMeterBandExperimenter().getRate());
                Assert.assertEquals((long) 23, (long) ((MeterBandExperimenterCase) meterBand).getMeterBandExperimenter().getExperimenter());

            }

        }

    }
    @Test
    public void testMeterModCommandConvertorwithNoFlags() {

        // / DROP Band
        MeterBandHeaderBuilder meterBandHeaderBuilder = new MeterBandHeaderBuilder();
        MeterBandTypesBuilder meterBandTypesB = new MeterBandTypesBuilder();

        MeterBandType bandFlag = new MeterBandType(true, false, false);
        meterBandTypesB.setFlags(bandFlag);// _ofpmbtDrop
        DropBuilder drop = new DropBuilder();
        drop.setDropBurstSize(10L);
        drop.setDropRate(20L);
        Drop drp = drop.build();
        meterBandHeaderBuilder.setBandType(drp);
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
        dscp.setPercLevel((short) 1);
        DscpRemark dscpRemark = dscp.build();
        meterBandHeaderBuilder1.setBandType(dscpRemark);
        meterBandHeaderBuilder1.setMeterBandTypes(meterBandTypesB1.build());

        MeterBandHeader meterBH1 = meterBandHeaderBuilder1.build();

        // Experimental

        MeterBandHeaderBuilder meterBandHeaderBuilder2 = new MeterBandHeaderBuilder();
        MeterBandTypesBuilder meterBandTypesB2 = new MeterBandTypesBuilder();
        MeterBandType bandFlag2 = new MeterBandType(false, false, true);
        meterBandTypesB2.setFlags(bandFlag2);

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
        // key).toInstance();

        addMeterFromSAL
                .setMeterId(new org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.MeterId(10L));


        AddMeterInput meterInputCommand = addMeterFromSAL.build();
        MeterModInputBuilder outMeterModInput = MeterConvertor.toMeterModInput(meterInputCommand, (short) 0X4);

        Assert.assertEquals(MeterModCommand.OFPMCADD, outMeterModInput.getCommand());
        Assert.assertFalse(outMeterModInput.getFlags().isOFPMFBURST());
        Assert.assertTrue(outMeterModInput.getFlags().isOFPMFPKTPS());
        Assert.assertEquals(temp, outMeterModInput.getMeterId().getValue());
        // BandInformation starts here:

        List<Bands> bands = outMeterModInput.getBands();
        for (Bands currentBand : bands) {
            MeterBand meterBand = currentBand.getMeterBand();
            if (meterBand instanceof MeterBandDropCase) {

                Assert.assertEquals(
                        org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.MeterBandType.OFPMBTDROP,
                        ((MeterBandDropCase) meterBand).getMeterBandDrop().getType());
                Assert.assertEquals((long) 20, (long) ((MeterBandDropCase) meterBand).getMeterBandDrop().getBurstSize());
                Assert.assertEquals((long) 10, (long) ((MeterBandDropCase) meterBand).getMeterBandDrop().getRate());

            }
            if (meterBand instanceof MeterBandDscpRemarkCase) {
                Assert.assertEquals(
                        org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.MeterBandType.OFPMBTDSCPREMARK,
                        ((MeterBandDscpRemarkCase) meterBand).getMeterBandDscpRemark().getType());
                Assert.assertEquals((long) 11, (long) ((MeterBandDscpRemarkCase) meterBand).getMeterBandDscpRemark().getBurstSize());
                Assert.assertEquals((long) 21, (long) ((MeterBandDscpRemarkCase) meterBand).getMeterBandDscpRemark().getRate());
                Assert.assertEquals((short) 1, (short) ((MeterBandDscpRemarkCase) meterBand).getMeterBandDscpRemark().getPrecLevel());

            }
            if (meterBand instanceof MeterBandExperimenterCase) {
                Assert.assertEquals(
                        org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.MeterBandType.OFPMBTEXPERIMENTER,
                        ((MeterBandExperimenterCase) meterBand).getMeterBandExperimenter().getType());
                Assert.assertEquals((long) 12, (long) ((MeterBandExperimenterCase) meterBand).getMeterBandExperimenter().getBurstSize());
                Assert.assertEquals((long) 22, (long) ((MeterBandExperimenterCase) meterBand).getMeterBandExperimenter().getRate());
                Assert.assertEquals((long) 23, (long) ((MeterBandExperimenterCase) meterBand).getMeterBandExperimenter().getExperimenter());

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
        // key).toInstance();

        addMeterFromSAL
                .setMeterId(new org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.MeterId(10L));
        MeterFlags flagV = new MeterFlags(true, true, true, true);
        addMeterFromSAL.setFlags(flagV);

        AddMeterInput meterInputCommand = addMeterFromSAL.build();
        MeterModInputBuilder outMeterModInput = MeterConvertor.toMeterModInput(meterInputCommand, (short) 0X4);

        Assert.assertEquals(MeterModCommand.OFPMCADD, outMeterModInput.getCommand());
        Assert.assertTrue(outMeterModInput.getFlags().isOFPMFBURST());
        Assert.assertEquals(temp, outMeterModInput.getMeterId().getValue());
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
        dscp.setPercLevel((short) 1);
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
        // key).toInstance();

        addMeterFromSAL
                .setMeterId(new org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.MeterId(10L));
        MeterFlags flagV = new MeterFlags(true, true, true, true);
        addMeterFromSAL.setFlags(flagV);

        AddMeterInput meterInputCommand = addMeterFromSAL.build();
        MeterModInputBuilder outMeterModInput = MeterConvertor.toMeterModInput(meterInputCommand, (short) 0X4);

        Assert.assertEquals(MeterModCommand.OFPMCADD, outMeterModInput.getCommand());
        Assert.assertTrue(outMeterModInput.getFlags().isOFPMFBURST());
        Assert.assertEquals(temp, outMeterModInput.getMeterId().getValue());
        // BandInformation starts here:

        List<Bands> bands = outMeterModInput.getBands();
        for (Bands currentBand : bands) {
            MeterBand meterBand = currentBand.getMeterBand();
            if (meterBand instanceof MeterBandDropCase) {

                Assert.assertEquals(
                        org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.MeterBandType.OFPMBTDROP,
                        ((MeterBandDropCase) meterBand).getMeterBandDrop().getType());
                Assert.assertEquals((long) 20, (long) ((MeterBandDropCase) meterBand).getMeterBandDrop().getBurstSize());
                Assert.assertEquals((long) 10, (long) ((MeterBandDropCase) meterBand).getMeterBandDrop().getRate());

            }
            if (meterBand instanceof MeterBandDscpRemarkCase) {
                Assert.assertEquals(
                        org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.MeterBandType.OFPMBTDSCPREMARK,
                        ((MeterBandDscpRemarkCase) meterBand).getMeterBandDscpRemark().getType());
                Assert.assertEquals((long) 11, (long) ((MeterBandDscpRemarkCase) meterBand).getMeterBandDscpRemark().getBurstSize());
                Assert.assertEquals((long) 21, (long) ((MeterBandDscpRemarkCase) meterBand).getMeterBandDscpRemark().getRate());
                Assert.assertEquals((short) 1, (short) ((MeterBandDscpRemarkCase) meterBand).getMeterBandDscpRemark().getPrecLevel());

            }
            if (meterBand instanceof MeterBandExperimenterCase) {
                Assert.assertEquals(
                        org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.MeterBandType.OFPMBTEXPERIMENTER,
                        ((MeterBandExperimenterCase) meterBand).getMeterBandExperimenter().getType());
                Assert.assertEquals((long) 12, (long) ((MeterBandExperimenterCase) meterBand).getMeterBandExperimenter().getBurstSize());
                Assert.assertEquals((long) 22, (long) ((MeterBandExperimenterCase) meterBand).getMeterBandExperimenter().getRate());
                Assert.assertEquals((long) 23, (long) ((MeterBandExperimenterCase) meterBand).getMeterBandExperimenter().getExperimenter());

            }

        }

    }

}
