/*
 * Copyright (c) 2015 NetIDE Consortium and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowjava.protocol.impl.deserialization.factories;

import io.netty.buffer.ByteBuf;
import java.util.ArrayList;
import java.util.List;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.opendaylight.openflowjava.protocol.api.extensibility.DeserializerRegistry;
import org.opendaylight.openflowjava.protocol.api.extensibility.OFDeserializer;
import org.opendaylight.openflowjava.protocol.api.keys.MessageCodeKey;
import org.opendaylight.openflowjava.protocol.api.util.EncodeConstants;
import org.opendaylight.openflowjava.protocol.impl.deserialization.DeserializerRegistryImpl;
import org.opendaylight.openflowjava.protocol.impl.util.BufferHelper;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.MeterBandType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.MeterFlags;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.MeterId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.MeterModCommand;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.MeterModInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.meter.band.header.meter.band.MeterBandDropCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.meter.band.header.meter.band.MeterBandDscpRemarkCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.meter.band.header.meter.band.meter.band.drop._case.MeterBandDropBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.meter.band.header.meter.band.meter.band.dscp.remark._case.MeterBandDscpRemarkBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.meter.mod.Bands;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.meter.mod.BandsBuilder;
import org.opendaylight.yangtools.yang.common.Uint32;
import org.opendaylight.yangtools.yang.common.Uint8;

/**
 * Unit tests for MeterModInputMessageFactory.
 *
 * @author giuseppex.petralia@intel.com
 */
public class MeterModInputMessageFactoryTest {

    private OFDeserializer<MeterModInput> factory;

    @Before
    public void startUp() {
        DeserializerRegistry desRegistry = new DeserializerRegistryImpl();
        desRegistry.init();
        factory = desRegistry
                .getDeserializer(new MessageCodeKey(EncodeConstants.OF13_VERSION_ID, 29, MeterModInput.class));

    }

    @Test
    public void test() {
        ByteBuf bb = BufferHelper.buildBuffer("00 01 00 03 00 00 08 c8 00 "
                + "01 00 10 00 00 00 01 00 00 00 02 00 00 00 " + "00 00 02 00 10 00 00 00 01 00 00 00 02 03 00 00 00");
        MeterModInput deserializedMessage = BufferHelper.deserialize(factory, bb);
        BufferHelper.checkHeaderV13(deserializedMessage);

        Assert.assertEquals("Wrong command", MeterModCommand.forValue(1), deserializedMessage.getCommand());
        Assert.assertEquals("Wrong flags", new MeterFlags(false, true, true, false), deserializedMessage.getFlags());
        Assert.assertEquals("Wrong meter id", new MeterId(Uint32.valueOf(2248)), deserializedMessage.getMeterId());
        Assert.assertEquals("Wrong band", createBandsList().get(0), deserializedMessage.getBands().get(0));
        Assert.assertEquals("Wrong band", createBandsList().get(1), deserializedMessage.getBands().get(1));
    }

    private static List<Bands> createBandsList() {
        final List<Bands> bandsList = new ArrayList<>();
        final BandsBuilder bandsBuilder = new BandsBuilder();
        final MeterBandDropCaseBuilder dropCaseBuilder = new MeterBandDropCaseBuilder();
        MeterBandDropBuilder dropBand = new MeterBandDropBuilder();
        dropBand.setType(MeterBandType.OFPMBTDROP);
        dropBand.setRate(Uint32.ONE);
        dropBand.setBurstSize(Uint32.TWO);
        dropCaseBuilder.setMeterBandDrop(dropBand.build());
        bandsList.add(bandsBuilder.setMeterBand(dropCaseBuilder.build()).build());
        final MeterBandDscpRemarkCaseBuilder dscpCaseBuilder = new MeterBandDscpRemarkCaseBuilder();
        MeterBandDscpRemarkBuilder dscpRemarkBand = new MeterBandDscpRemarkBuilder();
        dscpRemarkBand.setType(MeterBandType.OFPMBTDSCPREMARK);
        dscpRemarkBand.setRate(Uint32.ONE);
        dscpRemarkBand.setBurstSize(Uint32.TWO);
        dscpRemarkBand.setPrecLevel(Uint8.valueOf(3));
        dscpCaseBuilder.setMeterBandDscpRemark(dscpRemarkBand.build());
        bandsList.add(bandsBuilder.setMeterBand(dscpCaseBuilder.build()).build());
        return bandsList;
    }
}
