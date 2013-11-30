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
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.meter.band.header.MeterBand;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.meter.band.header.meter.band.MeterBandDrop;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.meter.band.header.meter.band.MeterBandDscpRemark;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.meter.band.header.meter.band.MeterBandExperimenter;
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
        drop.setBurstSize(10L);
        drop.setRate(20L);
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
        dscp.setBurstSize(11L);
        dscp.setRate(21L);
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
        exp.setBurstSize(12L);
        exp.setRate(22L);
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
        MeterModInput outMeterModInput = MeterConvertor.toMeterModInput(meterInputCommand, (short) 0X4);

        Assert.assertEquals(MeterModCommand.OFPMCADD, outMeterModInput.getCommand());
        Assert.assertTrue(outMeterModInput.getFlags().isOFPMFBURST());
        Assert.assertEquals(temp, outMeterModInput.getMeterId().getValue());
        // BandInformation starts here:

        List<Bands> bands = outMeterModInput.getBands();
        for (Bands currentBand : bands) {
            MeterBand meterBand = currentBand.getMeterBand();
            if (meterBand instanceof MeterBandDrop) {

                Assert.assertEquals(
                        org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.MeterBandType.OFPMBTDROP,
                        ((MeterBandDrop) meterBand).getType());
                Assert.assertEquals((long) 20, (long) ((MeterBandDrop) meterBand).getBurstSize());
                Assert.assertEquals((long) 10, (long) ((MeterBandDrop) meterBand).getRate());

            }
            if (meterBand instanceof MeterBandDscpRemark) {
                Assert.assertEquals(
                        org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.MeterBandType.OFPMBTDSCPREMARK,
                        ((MeterBandDscpRemark) meterBand).getType());
                Assert.assertEquals((long) 11, (long) ((MeterBandDscpRemark) meterBand).getBurstSize());
                Assert.assertEquals((long) 21, (long) ((MeterBandDscpRemark) meterBand).getRate());
                Assert.assertEquals((short) 1, (short) ((MeterBandDscpRemark) meterBand).getPrecLevel());

            }
            if (meterBand instanceof MeterBandExperimenter) {
                Assert.assertEquals(
                        org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.MeterBandType.OFPMBTEXPERIMENTER,
                        ((MeterBandExperimenter) meterBand).getType());
                Assert.assertEquals((long) 12, (long) ((MeterBandExperimenter) meterBand).getBurstSize());
                Assert.assertEquals((long) 22, (long) ((MeterBandExperimenter) meterBand).getRate());
                Assert.assertEquals((long) 23, (long) ((MeterBandExperimenter) meterBand).getExperimenter());

            }

        }

    }

}
