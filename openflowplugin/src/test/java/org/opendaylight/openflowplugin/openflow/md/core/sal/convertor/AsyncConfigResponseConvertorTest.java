/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.openflow.md.core.sal.convertor;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.opendaylight.openflowplugin.api.OFConstants;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.data.VersionConvertorData;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.FlowRemovedReason;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.PacketInReason;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.PortReason;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.GetAsyncOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.GetAsyncOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.async.body.grouping.FlowRemovedMask;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.async.body.grouping.FlowRemovedMaskBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.async.body.grouping.PacketInMask;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.async.body.grouping.PacketInMaskBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.async.body.grouping.PortStatusMask;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.async.body.grouping.PortStatusMaskBuilder;

/**
 * Test for {@link AsyncConfigResponseConvertor}.
 */
public class AsyncConfigResponseConvertorTest {
    private static final VersionConvertorData VERSION = new VersionConvertorData(OFConstants.OFP_VERSION_1_3);
    private ConvertorManager convertorManager;
    @Before
    public void setUp() throws Exception {
        convertorManager = ConvertorManagerFactory.createDefaultManager();
    }

    @Test
    public void testConvert() throws Exception {
        final GetAsyncOutput getAsyncOutput = new GetAsyncOutputBuilder()
                .setFlowRemovedMask(createFlowRemowedMask())
                .setPacketInMask(createPacketInMask())
                .setPortStatusMask(createPortStatusMask())
                .build();

        final Optional<org.opendaylight.yang.gen.v1.urn.opendaylight.async.config.service.rev170619.GetAsyncOutput>
                getAsyncOutputOptional = convertorManager.convert(getAsyncOutput, VERSION);

        final org.opendaylight.yang.gen.v1.urn.opendaylight.async.config.service.rev170619.GetAsyncOutput
                asyncOutputResult = getAsyncOutputOptional.orElse(AsyncConfigResponseConvertor.defaultResult());

        final org.opendaylight.yang.gen.v1.urn.opendaylight.async.config.service.rev170619.FlowRemovedMask
                flowRemovedMasterMask = asyncOutputResult.getFlowRemovedMask().getMasterMask();
        final org.opendaylight.yang.gen.v1.urn.opendaylight.async.config.service.rev170619.FlowRemovedMask
                flowRemovedSlaveMask = asyncOutputResult.getFlowRemovedMask().getSlaveMask();
        final org.opendaylight.yang.gen.v1.urn.opendaylight.async.config.service.rev170619.PacketInMask
                packetInMasterMask = asyncOutputResult.getPacketInMask().getMasterMask();
        final org.opendaylight.yang.gen.v1.urn.opendaylight.async.config.service.rev170619.PacketInMask
                packetInSlaveMask = asyncOutputResult.getPacketInMask().getSlaveMask();
        final org.opendaylight.yang.gen.v1.urn.opendaylight.async.config.service.rev170619.PortStatusMask
                portStatusMasterMask = asyncOutputResult.getPortStatusMask().getMasterMask();
        final org.opendaylight.yang.gen.v1.urn.opendaylight.async.config.service.rev170619.PortStatusMask
                portStatusSlaveMask = asyncOutputResult.getPortStatusMask().getSlaveMask();

        Assert.assertTrue(flowRemovedMasterMask.isDELETE());
        Assert.assertTrue(flowRemovedMasterMask.isGROUPDELETE());
        Assert.assertTrue(flowRemovedMasterMask.isHARDTIMEOUT());
        Assert.assertTrue(flowRemovedMasterMask.isIDLETIMEOUT());
        Assert.assertFalse(flowRemovedSlaveMask.isDELETE());
        Assert.assertFalse(flowRemovedSlaveMask.isGROUPDELETE());
        Assert.assertFalse(flowRemovedSlaveMask.isHARDTIMEOUT());
        Assert.assertFalse(flowRemovedSlaveMask.isIDLETIMEOUT());
        Assert.assertTrue(packetInMasterMask.isACTION());
        Assert.assertFalse(packetInMasterMask.isINVALIDTTL());
        Assert.assertTrue(packetInMasterMask.isNOMATCH());
        Assert.assertFalse(packetInSlaveMask.isACTION());
        Assert.assertFalse(packetInSlaveMask.isINVALIDTTL());
        Assert.assertFalse(packetInSlaveMask.isNOMATCH());
        Assert.assertTrue(portStatusMasterMask.isDELETE());
        Assert.assertTrue(portStatusMasterMask.isADD());
        Assert.assertTrue(portStatusMasterMask.isUPDATE());
        Assert.assertTrue(portStatusSlaveMask.isDELETE());
        Assert.assertTrue(portStatusSlaveMask.isADD());
        Assert.assertTrue(portStatusSlaveMask.isUPDATE());
    }

    private static List<PacketInMask> createPacketInMask() {
        List<PacketInMask> masks = new ArrayList<>();
        PacketInMaskBuilder builder;
        // OFPCR_ROLE_EQUAL or OFPCR_ROLE_MASTER
        builder = new PacketInMaskBuilder();
        List<PacketInReason> packetInReasonList = new ArrayList<>();
        packetInReasonList.add(PacketInReason.OFPRNOMATCH);
        packetInReasonList.add(PacketInReason.OFPRACTION);
        builder.setMask(packetInReasonList);
        masks.add(builder.build());
        // OFPCR_ROLE_SLAVE
        builder = new PacketInMaskBuilder();
        packetInReasonList = new ArrayList<>();
        builder.setMask(packetInReasonList);
        masks.add(builder.build());
        return masks;
    }

    private static List<PortStatusMask> createPortStatusMask() {
        List<PortStatusMask> masks = new ArrayList<>();
        PortStatusMaskBuilder builder;
        builder = new PortStatusMaskBuilder();
        // OFPCR_ROLE_EQUAL or OFPCR_ROLE_MASTER
        List<PortReason> portReasonList = new ArrayList<>();
        portReasonList.add(PortReason.OFPPRADD);
        portReasonList.add(PortReason.OFPPRDELETE);
        portReasonList.add(PortReason.OFPPRMODIFY);
        builder.setMask(portReasonList);
        masks.add(builder.build());
        // OFPCR_ROLE_SLAVE
        builder = new PortStatusMaskBuilder();
        builder.setMask(portReasonList);
        masks.add(builder.build());
        return masks;
    }

    private static List<FlowRemovedMask> createFlowRemowedMask() {
        List<FlowRemovedMask> masks = new ArrayList<>();
        FlowRemovedMaskBuilder builder;
        // OFPCR_ROLE_EQUAL or OFPCR_ROLE_MASTER
        builder = new FlowRemovedMaskBuilder();
        List<FlowRemovedReason> flowRemovedReasonList = new ArrayList<>();
        flowRemovedReasonList.add(FlowRemovedReason.OFPRRIDLETIMEOUT);
        flowRemovedReasonList.add(FlowRemovedReason.OFPRRHARDTIMEOUT);
        flowRemovedReasonList.add(FlowRemovedReason.OFPRRDELETE);
        flowRemovedReasonList.add(FlowRemovedReason.OFPRRGROUPDELETE);
        builder.setMask(flowRemovedReasonList);
        masks.add(builder.build());
        // OFPCR_ROLE_SLAVE
        builder = new FlowRemovedMaskBuilder();
        flowRemovedReasonList = new ArrayList<>();
        builder.setMask(flowRemovedReasonList);
        masks.add(builder.build());
        return masks;
    }
}