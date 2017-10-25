/*
 * Copyright (c) 2013 Pantheon Technologies s.r.o. and others. All rights reserved.
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
import org.opendaylight.openflowjava.protocol.impl.deserialization.DeserializerRegistryImpl;
import org.opendaylight.openflowjava.protocol.impl.util.BufferHelper;
import org.opendaylight.openflowjava.protocol.api.util.EncodeConstants;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.FlowRemovedReason;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.PacketInReason;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.PortReason;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.GetAsyncOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.async.body.grouping.FlowRemovedMask;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.async.body.grouping.FlowRemovedMaskBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.async.body.grouping.PacketInMask;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.async.body.grouping.PacketInMaskBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.async.body.grouping.PortStatusMask;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.async.body.grouping.PortStatusMaskBuilder;

/**
 * @author timotej.kubas
 *
 */
public class GetAsyncReplyMessageFactoryTest {

    private OFDeserializer<GetAsyncOutput> asyncFactory;

    /**
     * Initializes deserializer registry and lookups correct deserializer
     */
    @Before
    public void startUp() {
        DeserializerRegistry registry = new DeserializerRegistryImpl();
        registry.init();
        asyncFactory = registry.getDeserializer(
                new MessageCodeKey(EncodeConstants.OF13_VERSION_ID, 27, GetAsyncOutput.class));
    }

    /**
     * Testing {@link GetAsyncReplyMessageFactory} for correct translation into POJO
     */
    @Test
    public void testGetAsyncReplyMessage() {
        ByteBuf bb = BufferHelper.buildBuffer("00 00 00 07 "+
                                              "00 00 00 00 "+
                                              "00 00 00 07 "+
                                              "00 00 00 00 "+
                                              "00 00 00 0F "+
                                              "00 00 00 00");
        GetAsyncOutput builtByFactory = BufferHelper.deserialize(asyncFactory, bb);

        BufferHelper.checkHeaderV13(builtByFactory);
        Assert.assertEquals("Wrong packetInMask",createPacketInMask(), builtByFactory.getPacketInMask());
        Assert.assertEquals("Wrong portStatusMask",createPortStatusMask(), builtByFactory.getPortStatusMask());
        Assert.assertEquals("Wrong flowRemovedMask",createFlowRemovedMask(), builtByFactory.getFlowRemovedMask());
    }

    private static List<PacketInMask> createPacketInMask() {
        List<PacketInMask> inMasks = new ArrayList<>();
        PacketInMaskBuilder maskBuilder;
        // OFPCR_ROLE_EQUAL or OFPCR_ROLE_MASTER
        maskBuilder = new PacketInMaskBuilder();
        List<PacketInReason> reasons = new ArrayList<>();
        reasons.add(PacketInReason.OFPRNOMATCH);
        reasons.add(PacketInReason.OFPRACTION);
        reasons.add(PacketInReason.OFPRINVALIDTTL);
        maskBuilder.setMask(reasons);
        inMasks.add(maskBuilder.build());
        // OFPCR_ROLE_SLAVE
        maskBuilder = new PacketInMaskBuilder();
        reasons = new ArrayList<>();
        maskBuilder.setMask(reasons);
        inMasks.add(maskBuilder.build());
        return inMasks;
    }

    private static List<PortStatusMask> createPortStatusMask() {
        List<PortStatusMask> inMasks = new ArrayList<>();
        PortStatusMaskBuilder maskBuilder;
        // OFPCR_ROLE_EQUAL or OFPCR_ROLE_MASTER
        maskBuilder = new PortStatusMaskBuilder();
        List<PortReason> reasons = new ArrayList<>();
        reasons.add(PortReason.OFPPRADD);
        reasons.add(PortReason.OFPPRDELETE);
        reasons.add(PortReason.OFPPRMODIFY);
        inMasks.add(maskBuilder.setMask(reasons).build());
        // OFPCR_ROLE_SLAVE
        maskBuilder = new PortStatusMaskBuilder();
        reasons = new ArrayList<>();
        maskBuilder.setMask(reasons);
        inMasks.add(maskBuilder.build());
        return inMasks;
    }

    private static List<FlowRemovedMask> createFlowRemovedMask() {
        List<FlowRemovedMask> inMasks = new ArrayList<>();
        FlowRemovedMaskBuilder maskBuilder;
        // OFPCR_ROLE_EQUAL or OFPCR_ROLE_MASTER
        maskBuilder = new FlowRemovedMaskBuilder();
        List<FlowRemovedReason> reasons = new ArrayList<>();
        reasons.add(FlowRemovedReason.OFPRRIDLETIMEOUT);
        reasons.add(FlowRemovedReason.OFPRRHARDTIMEOUT);
        reasons.add(FlowRemovedReason.OFPRRDELETE);
        reasons.add(FlowRemovedReason.OFPRRGROUPDELETE);
        maskBuilder.setMask(reasons);
        inMasks.add(maskBuilder.build());
        // OFPCR_ROLE_SLAVE
        maskBuilder = new FlowRemovedMaskBuilder();
        reasons = new ArrayList<>();
        maskBuilder.setMask(reasons);
        inMasks.add(maskBuilder.build());
        return inMasks;
    }
}