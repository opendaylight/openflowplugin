/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.openflow.md.core.sal.convertor;

import java.util.Optional;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.opendaylight.openflowplugin.api.OFConstants;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.data.VersionConvertorData;
import org.opendaylight.yang.gen.v1.urn.opendaylight.async.config.service.rev170619.FlowRemovedMask;
import org.opendaylight.yang.gen.v1.urn.opendaylight.async.config.service.rev170619.PacketInMask;
import org.opendaylight.yang.gen.v1.urn.opendaylight.async.config.service.rev170619.PortStatusMask;
import org.opendaylight.yang.gen.v1.urn.opendaylight.async.config.service.rev170619.SetAsyncInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.async.config.service.rev170619.SetAsyncInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.async.config.service.rev170619.async.config.FlowRemovedMaskBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.async.config.service.rev170619.async.config.PacketInMaskBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.async.config.service.rev170619.async.config.PortStatusMaskBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.FlowRemovedReason;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.PacketInReason;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.PortReason;

/**
 * Test for {@link AsyncConfigConvertor}.
 */
public class AsyncConfigConvertorTest {
    private static final VersionConvertorData VERSION = new VersionConvertorData(OFConstants.OFP_VERSION_1_3);
    private static final FlowRemovedMask FLOW_REMOVED_MASTER_MASK = new FlowRemovedMask(true, true, true, true);
    private static final FlowRemovedMask FLOW_REMOVED_SLAVE_MASK = new FlowRemovedMask(true, false, true, false);
    private static final PacketInMask PACKET_IN_MASTER_MASK = new PacketInMask(true, true, true);
    private static final PacketInMask PACKET_IN_SLAVE_MASK = new PacketInMask(true, false, true);
    private static final PortStatusMask PORT_STATUS_MASTER_MASK = new PortStatusMask(true, true, true);
    private static final PortStatusMask PORT_STATUS_SLAVE_MASK = new PortStatusMask(false, true, false);
    private ConvertorManager convertorManager;

    @Before
    public void setUp() throws Exception {
        convertorManager = ConvertorManagerFactory.createDefaultManager();
    }

    @Test
    public void testConvert() throws Exception {
        final SetAsyncInput setAsyncInput = new SetAsyncInputBuilder()
                .setFlowRemovedMask(new FlowRemovedMaskBuilder()
                        .setMasterMask(FLOW_REMOVED_MASTER_MASK)
                        .setSlaveMask(FLOW_REMOVED_SLAVE_MASK)
                        .build())
                .setPacketInMask(new PacketInMaskBuilder()
                        .setMasterMask(PACKET_IN_MASTER_MASK)
                        .setSlaveMask(PACKET_IN_SLAVE_MASK)
                        .build())
                .setPortStatusMask(new PortStatusMaskBuilder()
                        .setMasterMask(PORT_STATUS_MASTER_MASK)
                        .setSlaveMask(PORT_STATUS_SLAVE_MASK)
                        .build())
                .build();

        final Optional<org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731
                .SetAsyncInput> setAsyncInputOptional = convertorManager.convert(setAsyncInput, VERSION);

        final org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731
                .SetAsyncInput asyncInputResult = setAsyncInputOptional
                .orElse(AsyncConfigConvertor.defaultResult(VERSION.getVersion()));

        final org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.async.body.grouping
                .FlowRemovedMask flowRemovedMasterMask = asyncInputResult.getFlowRemovedMask().get(0);
        final org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.async.body.grouping
                .FlowRemovedMask flowRemovedSlaveMask = asyncInputResult.getFlowRemovedMask().get(1);
        final org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.async.body.grouping
                .PacketInMask packetInMasterMask = asyncInputResult.getPacketInMask().get(0);
        final org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.async.body.grouping
                .PacketInMask packetInSlaveMask = asyncInputResult.getPacketInMask().get(1);
        final org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.async.body.grouping
                .PortStatusMask portStatusMasterMask = asyncInputResult.getPortStatusMask().get(0);
        final org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.async.body.grouping
                .PortStatusMask portStatusSlaveMask = asyncInputResult.getPortStatusMask().get(1);

        Assert.assertTrue(flowRemovedMasterMask.getMask().contains(FlowRemovedReason.OFPRRDELETE));
        Assert.assertTrue(flowRemovedMasterMask.getMask().contains(FlowRemovedReason.OFPRRGROUPDELETE));
        Assert.assertTrue(flowRemovedMasterMask.getMask().contains(FlowRemovedReason.OFPRRHARDTIMEOUT));
        Assert.assertTrue(flowRemovedMasterMask.getMask().contains(FlowRemovedReason.OFPRRIDLETIMEOUT));

        Assert.assertTrue(flowRemovedSlaveMask.getMask().contains(FlowRemovedReason.OFPRRDELETE));
        Assert.assertFalse(flowRemovedSlaveMask.getMask().contains(FlowRemovedReason.OFPRRGROUPDELETE));
        Assert.assertTrue(flowRemovedSlaveMask.getMask().contains(FlowRemovedReason.OFPRRHARDTIMEOUT));
        Assert.assertFalse(flowRemovedSlaveMask.getMask().contains(FlowRemovedReason.OFPRRIDLETIMEOUT));

        Assert.assertTrue(packetInMasterMask.getMask().contains(PacketInReason.OFPRACTION));
        Assert.assertTrue(packetInMasterMask.getMask().contains(PacketInReason.OFPRINVALIDTTL));
        Assert.assertTrue(packetInMasterMask.getMask().contains(PacketInReason.OFPRNOMATCH));

        Assert.assertTrue(packetInSlaveMask.getMask().contains(PacketInReason.OFPRACTION));
        Assert.assertFalse(packetInSlaveMask.getMask().contains(PacketInReason.OFPRINVALIDTTL));
        Assert.assertTrue(packetInSlaveMask.getMask().contains(PacketInReason.OFPRNOMATCH));

        Assert.assertTrue(portStatusMasterMask.getMask().contains(PortReason.OFPPRADD));
        Assert.assertTrue(portStatusMasterMask.getMask().contains(PortReason.OFPPRDELETE));
        Assert.assertTrue(portStatusMasterMask.getMask().contains(PortReason.OFPPRMODIFY));

        Assert.assertFalse(portStatusSlaveMask.getMask().contains(PortReason.OFPPRADD));
        Assert.assertTrue(portStatusSlaveMask.getMask().contains(PortReason.OFPPRDELETE));
        Assert.assertFalse(portStatusSlaveMask.getMask().contains(PortReason.OFPPRMODIFY));
    }
}