/*
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

import java.util.List;
import java.util.Optional;
import org.junit.Before;
import org.junit.Test;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.data.VersionConvertorData;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.service.rev130918.AddMeterInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.service.rev130918.AddMeterInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.BandId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.Meter;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.MeterBandType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.MeterFlags;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.band.type.band.type.DropBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.band.type.band.type.DscpRemarkBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.band.type.band.type.ExperimenterBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.meter.MeterBandHeaders;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.meter.MeterBandHeadersBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.meter.meter.band.headers.MeterBandHeader;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.meter.meter.band.headers.MeterBandHeaderBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.meter.meter.band.headers.MeterBandHeaderKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.meter.meter.band.headers.meter.band.header.MeterBandTypesBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.augments.rev150225.ExperimenterIdMeterBand;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.MeterModCommand;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.MeterModInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.meter.band.header.MeterBand;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.meter.band.header.meter.band.MeterBandDropCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.meter.band.header.meter.band.MeterBandDscpRemarkCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.meter.band.header.meter.band.MeterBandExperimenterCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.meter.mod.Bands;
import org.opendaylight.yangtools.yang.binding.util.BindingMap;
import org.opendaylight.yangtools.yang.common.Uint32;
import org.opendaylight.yangtools.yang.common.Uint8;

public class MeterConvertorTest {
    private ConvertorManager convertorManager;

    @Before
    public void setUp() {
        convertorManager = ConvertorManagerFactory.createDefaultManager();
    }

    @Test
    public void testMeterModCommandConvertorwithAllParameters() {
        final Uint32 burstSize = Uint32.TEN;
        final Uint32  dropRate = Uint32.valueOf(20);

        // / DROP Band
        MeterBandHeaderBuilder meterBandHeaderBuilder = new MeterBandHeaderBuilder()
            .setBandType(new DropBuilder()
                .setDropBurstSize(burstSize)
                .setDropRate(dropRate)
                .build())
            // _ofpmbtDrop
            .setMeterBandTypes(new MeterBandTypesBuilder().setFlags(new MeterBandType(true, false, false)).build())
            .withKey(new MeterBandHeaderKey(new BandId(Uint32.ZERO)));

        // DSCP Mark
        final Uint32 dscpRemarkBurstSize = Uint32.valueOf(11L);
        final Uint32 dscpRemarkRate = Uint32.valueOf(21L);
        final Uint8 dscpPercLevel = Uint8.ONE;

        MeterBandHeaderBuilder meterBandHeaderBuilder1 = new MeterBandHeaderBuilder()
            .setBandType(new DscpRemarkBuilder()
                .setDscpRemarkBurstSize(dscpRemarkBurstSize)
                .setDscpRemarkRate(dscpRemarkRate)
                .setPrecLevel(dscpPercLevel)
                .build())
            .setMeterBandTypes(new MeterBandTypesBuilder()
                .setFlags(new MeterBandType(false, true, false))
                .build())
            .withKey(new MeterBandHeaderKey(new BandId(Uint32.ONE)));


        // Experimental
        final Uint32 expBurstSize = Uint32.valueOf(12L);
        final Uint32 expRate = Uint32.valueOf(22L);
        final Uint32 expExperimenter = Uint32.valueOf(23L);

        MeterBandHeaderBuilder meterBandHeaderBuilder2 = new MeterBandHeaderBuilder()
            .setBandType(new ExperimenterBuilder()
                .setExperimenterBurstSize(expBurstSize)
                .setExperimenterRate(expRate)
                .setExperimenter(expExperimenter)
                .build())
            .setMeterBandTypes(new MeterBandTypesBuilder().setFlags(new MeterBandType(false, false, true)).build())
            .withKey(new MeterBandHeaderKey(new BandId(Uint32.TWO)));

        MeterBandHeaders meterBandHeaders = new MeterBandHeadersBuilder()
            .setMeterBandHeader(BindingMap.ordered(
                meterBandHeaderBuilder.build(), meterBandHeaderBuilder1.build(), meterBandHeaderBuilder2.build()))
            .build();

        AddMeterInputBuilder addMeterFromSAL = new AddMeterInputBuilder();

        addMeterFromSAL.setMeterBandHeaders(meterBandHeaders); // MeterBands

        // NodeKey key = new NodeKey(new NodeId("24"));
        // InstanceIdentifier<Node> path =
        // InstanceIdentifier.builder().node(Nodes.class).node(Node.class,
        // key).build();

        addMeterFromSAL.setMeterId(
            new org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.MeterId(Uint32.TEN));
        MeterFlags flagV = new MeterFlags(true, true, true, true);
        addMeterFromSAL.setFlags(flagV);

        AddMeterInput meterInputCommand = addMeterFromSAL.build();
        MeterModInputBuilder outMeterModInput = convert(meterInputCommand, new VersionConvertorData((short) 0X4));

        assertEquals(MeterModCommand.OFPMCADD, outMeterModInput.getCommand());
        assertTrue(outMeterModInput.getFlags().getOFPMFBURST());
        assertEquals(Uint32.valueOf(10L), outMeterModInput.getMeterId().getValue());
        // BandInformation starts here:

        List<Bands> bands = outMeterModInput.getBands();
        for (Bands currentBand : bands) {
            MeterBand meterBand = currentBand.getMeterBand();
            if (meterBand instanceof MeterBandDropCase) {
                assertEquals(org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731
                    .MeterBandType.OFPMBTDROP, ((MeterBandDropCase) meterBand).getMeterBandDrop().getType());
                assertEquals(burstSize, ((MeterBandDropCase) meterBand).getMeterBandDrop().getBurstSize());
                assertEquals(dropRate, ((MeterBandDropCase) meterBand).getMeterBandDrop().getRate());

            }
            if (meterBand instanceof MeterBandDscpRemarkCase) {
                assertEquals(org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731
                    .MeterBandType.OFPMBTDSCPREMARK,
                    ((MeterBandDscpRemarkCase) meterBand).getMeterBandDscpRemark().getType());
                assertEquals(dscpRemarkBurstSize,
                        ((MeterBandDscpRemarkCase) meterBand).getMeterBandDscpRemark().getBurstSize());
                assertEquals(dscpRemarkRate,
                        ((MeterBandDscpRemarkCase) meterBand).getMeterBandDscpRemark().getRate());
                assertEquals(dscpPercLevel,
                        ((MeterBandDscpRemarkCase) meterBand).getMeterBandDscpRemark().getPrecLevel());

            }
            if (meterBand instanceof MeterBandExperimenterCase) {
                assertEquals(org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731
                    .MeterBandType.OFPMBTEXPERIMENTER,
                    ((MeterBandExperimenterCase) meterBand).getMeterBandExperimenter().getType());
                assertEquals(expBurstSize,
                        ((MeterBandExperimenterCase) meterBand).getMeterBandExperimenter().getBurstSize());
                assertEquals(expRate,
                        ((MeterBandExperimenterCase) meterBand).getMeterBandExperimenter().getRate());
                ExperimenterIdMeterBand expBand = ((MeterBandExperimenterCase) meterBand).getMeterBandExperimenter()
                        .augmentation(ExperimenterIdMeterBand.class);
                assertEquals(expExperimenter, expBand.getExperimenter().getValue());
            }
        }
    }

    @Test
    public void testMeterModCommandConvertorwithNoFlags() {
        final Uint32 burstSize = Uint32.TEN;
        final Uint32 dropRate = Uint32.valueOf(20);
        // / DROP Band

        final MeterBandHeader meterBH = new MeterBandHeaderBuilder()
            .setBandType(new DropBuilder().setDropBurstSize(burstSize).setDropRate(dropRate).build())
            // _ofpmbtDrop
            .setMeterBandTypes(new MeterBandTypesBuilder().setFlags(new MeterBandType(true, false, false)).build())
            .withKey(new MeterBandHeaderKey(new BandId(Uint32.ZERO)))
            .build();

        // DSCP Mark
        final Uint32 dscpRemarkBurstSize = Uint32.valueOf(11);
        final Uint32 dscpRemarkRate = Uint32.valueOf(21);
        final Uint8 dscpPercLevel = Uint8.ONE;

        final MeterBandHeader meterBH1 = new MeterBandHeaderBuilder()
            .setBandType(new DscpRemarkBuilder()
                .setDscpRemarkBurstSize(dscpRemarkBurstSize)
                .setDscpRemarkRate(dscpRemarkRate)
                .setPrecLevel(dscpPercLevel)
                .build())
            .setMeterBandTypes(new MeterBandTypesBuilder().setFlags(new MeterBandType(false, true, false)).build())
            .withKey(new MeterBandHeaderKey(new BandId(Uint32.ONE)))
            .build();

        // Experimental
        final Uint32 expBurstSize = Uint32.valueOf(12);
        final Uint32 expRate = Uint32.valueOf(22);
        final Uint32 expExperimenter = Uint32.valueOf(23);

        MeterBandHeader meterBH2 = new MeterBandHeaderBuilder()
            .setBandType(new ExperimenterBuilder()
                .setExperimenterBurstSize(expBurstSize)
                .setExperimenterRate(expRate)
                .setExperimenter(expExperimenter)
                .build())
            .setMeterBandTypes(new MeterBandTypesBuilder().setFlags(new MeterBandType(false, false, true)).build())
            .withKey(new MeterBandHeaderKey(new BandId(Uint32.TWO)))
            .build();

        // Constructing List of Bands
        MeterBandHeaders meterBandHeaders = new MeterBandHeadersBuilder()
            .setMeterBandHeader(BindingMap.ordered(meterBH, meterBH1, meterBH2))
            .build();

        AddMeterInputBuilder addMeterFromSAL = new AddMeterInputBuilder();

        addMeterFromSAL.setMeterBandHeaders(meterBandHeaders); // MeterBands

        // NodeKey key = new NodeKey(new NodeId("24"));
        // InstanceIdentifier<Node> path =
        // InstanceIdentifier.builder().node(Nodes.class).node(Node.class,
        // key).build();

        addMeterFromSAL.setMeterId(
            new org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.MeterId(Uint32.TEN));

        AddMeterInput meterInputCommand = addMeterFromSAL.build();
        MeterModInputBuilder outMeterModInput = convert(meterInputCommand, new VersionConvertorData((short) 0X4));

        assertEquals(MeterModCommand.OFPMCADD, outMeterModInput.getCommand());
        assertFalse(outMeterModInput.getFlags().getOFPMFBURST());
        assertTrue(outMeterModInput.getFlags().getOFPMFPKTPS());
        assertEquals(Uint32.TEN, outMeterModInput.getMeterId().getValue());
        // BandInformation starts here:

        List<Bands> bands = outMeterModInput.getBands();
        for (Bands currentBand : bands) {
            MeterBand meterBand = currentBand.getMeterBand();
            if (meterBand instanceof MeterBandDropCase) {
                assertEquals(org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731
                    .MeterBandType.OFPMBTDROP, ((MeterBandDropCase) meterBand).getMeterBandDrop().getType());
                assertEquals(burstSize, ((MeterBandDropCase) meterBand).getMeterBandDrop().getBurstSize());
                assertEquals(dropRate, ((MeterBandDropCase) meterBand).getMeterBandDrop().getRate());

            }
            if (meterBand instanceof MeterBandDscpRemarkCase) {
                assertEquals(org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731
                    .MeterBandType.OFPMBTDSCPREMARK,
                    ((MeterBandDscpRemarkCase) meterBand).getMeterBandDscpRemark().getType());
                assertEquals(dscpRemarkBurstSize,
                        ((MeterBandDscpRemarkCase) meterBand).getMeterBandDscpRemark().getBurstSize());
                assertEquals(dscpRemarkRate,
                        ((MeterBandDscpRemarkCase) meterBand).getMeterBandDscpRemark().getRate());
                assertEquals(dscpPercLevel,
                        ((MeterBandDscpRemarkCase) meterBand).getMeterBandDscpRemark().getPrecLevel());

            }
            if (meterBand instanceof MeterBandExperimenterCase) {
                assertEquals(org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731
                    .MeterBandType.OFPMBTEXPERIMENTER,
                     ((MeterBandExperimenterCase) meterBand).getMeterBandExperimenter().getType());
                assertEquals(expBurstSize,
                        ((MeterBandExperimenterCase) meterBand).getMeterBandExperimenter().getBurstSize());
                assertEquals(expRate,
                        ((MeterBandExperimenterCase) meterBand).getMeterBandExperimenter().getRate());
                ExperimenterIdMeterBand expBand = ((MeterBandExperimenterCase) meterBand).getMeterBandExperimenter()
                        .augmentation(ExperimenterIdMeterBand.class);
                assertEquals(expExperimenter, expBand.getExperimenter().getValue());
            }
        }
    }

    @Test
    public void testMeterModCommandConvertorBandDataisNULL() {
        // NodeKey key = new NodeKey(new NodeId("24"));
        // InstanceIdentifier<Node> path =
        // InstanceIdentifier.builder().node(Nodes.class).node(Node.class,
        // key).build();

        MeterModInputBuilder outMeterModInput = convert(new AddMeterInputBuilder()
            .setMeterId(new org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.MeterId(Uint32.TEN))
            .setFlags(new MeterFlags(true, true, true, true))
            .build(), new VersionConvertorData((short) 0X4));

        assertEquals(MeterModCommand.OFPMCADD, outMeterModInput.getCommand());
        assertTrue(outMeterModInput.getFlags().getOFPMFBURST());
        assertEquals(Uint32.valueOf(10L), outMeterModInput.getMeterId().getValue());
    }

    @Test
    public void testMeterModCommandConvertorNoValidBandData() {
        // / DROP Band
        final MeterBandHeader meterBH = new MeterBandHeaderBuilder()
            // _ofpmbtDrop
            .setMeterBandTypes(new MeterBandTypesBuilder().setFlags(new MeterBandType(true, false, false)).build())
            .withKey(new MeterBandHeaderKey(new BandId(Uint32.ZERO)))
            .build();

        // DSCP Mark
        final MeterBandHeader meterBH1 = new MeterBandHeaderBuilder()
            .setBandType(new DscpRemarkBuilder()
                .setDscpRemarkBurstSize(Uint32.valueOf(11))
                .setDscpRemarkRate(Uint32.valueOf(21))
                .setPrecLevel(Uint8.ONE)
                .build())
            .setMeterBandTypes(new MeterBandTypesBuilder().setFlags(new MeterBandType(false, true, false)).build())
            .withKey(new MeterBandHeaderKey(new BandId(Uint32.ONE)))
            .build();

        // Experimental
        MeterBandHeader meterBH2 = new MeterBandHeaderBuilder()
            .setBandType(new ExperimenterBuilder()
                .setExperimenterBurstSize(Uint32.valueOf(12))
                .setExperimenterRate(Uint32.valueOf(22))
                .setExperimenter(Uint32.valueOf(23))
                .build())
            .setMeterBandTypes(new MeterBandTypesBuilder().build())
            .withKey(new MeterBandHeaderKey(new BandId(Uint32.TWO)))
            .build();

        // Constructing List of Bands
        MeterBandHeaders meterBandHeaders = new MeterBandHeadersBuilder()
            .setMeterBandHeader(BindingMap.ordered(meterBH, meterBH1, meterBH2))
            .build();

        AddMeterInputBuilder addMeterFromSAL = new AddMeterInputBuilder();

        addMeterFromSAL.setMeterBandHeaders(meterBandHeaders); // MeterBands

        // NodeKey key = new NodeKey(new NodeId("24"));
        // InstanceIdentifier<Node> path =
        // InstanceIdentifier.builder().node(Nodes.class).node(Node.class,
        // key).build();

        addMeterFromSAL.setMeterId(
            new org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.MeterId(Uint32.TEN));
        MeterFlags flagV = new MeterFlags(true, true, true, true);
        addMeterFromSAL.setFlags(flagV);

        AddMeterInput meterInputCommand = addMeterFromSAL.build();
        MeterModInputBuilder outMeterModInput = convert(meterInputCommand, new VersionConvertorData((short) 0X4));

        assertEquals(MeterModCommand.OFPMCADD, outMeterModInput.getCommand());
        assertTrue(outMeterModInput.getFlags().getOFPMFBURST());
        assertEquals(Uint32.valueOf(10L), outMeterModInput.getMeterId().getValue());
        // BandInformation starts here:

        List<Bands> bands = outMeterModInput.getBands();
        for (Bands currentBand : bands) {
            MeterBand meterBand = currentBand.getMeterBand();
            if (meterBand instanceof MeterBandDropCase) {

                assertEquals(org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731
                    .MeterBandType.OFPMBTDROP, ((MeterBandDropCase) meterBand).getMeterBandDrop().getType());
                assertEquals(20, ((MeterBandDropCase) meterBand).getMeterBandDrop().getBurstSize());
                assertEquals(10, ((MeterBandDropCase) meterBand).getMeterBandDrop().getRate());

            }
            if (meterBand instanceof MeterBandDscpRemarkCase) {
                assertEquals(org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731
                        .MeterBandType.OFPMBTDSCPREMARK,
                        ((MeterBandDscpRemarkCase) meterBand).getMeterBandDscpRemark().getType());
                assertEquals(11,
                        ((MeterBandDscpRemarkCase) meterBand).getMeterBandDscpRemark().getBurstSize().toJava());
                assertEquals(21,
                        ((MeterBandDscpRemarkCase) meterBand).getMeterBandDscpRemark().getRate().toJava());
                assertEquals((short) 1,
                        ((MeterBandDscpRemarkCase) meterBand).getMeterBandDscpRemark().getPrecLevel().toJava());

            }
            if (meterBand instanceof MeterBandExperimenterCase) {
                assertEquals(org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731
                        .MeterBandType.OFPMBTEXPERIMENTER,
                        ((MeterBandExperimenterCase) meterBand).getMeterBandExperimenter().getType());
                assertEquals(12,
                        ((MeterBandExperimenterCase) meterBand).getMeterBandExperimenter().getBurstSize());
                assertEquals(22,
                        ((MeterBandExperimenterCase) meterBand).getMeterBandExperimenter().getRate());
                ExperimenterIdMeterBand expBand = ((MeterBandExperimenterCase) meterBand).getMeterBandExperimenter()
                        .augmentation(ExperimenterIdMeterBand.class);
                assertEquals(23, expBand.getExperimenter().getValue());
            }
        }
    }

    private MeterModInputBuilder convert(final Meter source, final VersionConvertorData data) {
        Optional<MeterModInputBuilder> outMeterModInputOptional = convertorManager.convert(source, data);
        return outMeterModInputOptional.orElse(MeterConvertor.defaultResult(data.getVersion()));
    }
}
