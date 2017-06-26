/*
 * Copyright (c) 2013 Pantheon Technologies s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowjava.protocol.impl.deserialization.factories;

import io.netty.buffer.ByteBuf;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.opendaylight.openflowjava.protocol.api.extensibility.DeserializerRegistry;
import org.opendaylight.openflowjava.protocol.api.extensibility.OFDeserializer;
import org.opendaylight.openflowjava.protocol.api.keys.MessageCodeKey;
import org.opendaylight.openflowjava.protocol.impl.deserialization.DeserializerRegistryImpl;
import org.opendaylight.openflowjava.protocol.impl.util.BufferHelper;
import org.opendaylight.openflowjava.protocol.api.util.EncodeConstants;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev130715.MacAddress;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.ActionTypeV10;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.CapabilitiesV10;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.PortConfigV10;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.PortFeaturesV10;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.PortStateV10;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.GetFeaturesOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.features.reply.PhyPort;

/**
 * @author michal.polkorab
 *
 */
public class OF10FeaturesReplyMessageFactoryTest {

    private OFDeserializer<GetFeaturesOutput> featuresFactory;

    /**
     * Initializes deserializer registry and lookups correct deserializer
     */
    @Before
    public void startUp() {
        DeserializerRegistry registry = new DeserializerRegistryImpl();
        registry.init();
        featuresFactory = registry.getDeserializer(
                new MessageCodeKey(EncodeConstants.OF10_VERSION_ID, 6, GetFeaturesOutput.class));
    }

    /**
     * Testing {@link OF10FeaturesReplyMessageFactory} for correct translation into POJO
     */
    @Test
    public void test() {
        ByteBuf bb = BufferHelper.buildBuffer("00 01 02 03 04 05 06 07 00 01 02 03 01 00 00 00 "
                + "00 00 00 FF 00 00 0F FF "
                + "00 10 01 01 05 01 04 02 41 4C 4F 48 41 00 00 00 00 00 00 00 00 00 00 00 00 00 00 7F 00 00 02 00 "
                + "00 00 0F FF 00 00 00 00 00 00 03 0C 00 00 08 88");
        GetFeaturesOutput builtByFactory = BufferHelper.deserialize(featuresFactory, bb);

        BufferHelper.checkHeaderV10(builtByFactory);
        Assert.assertEquals("Wrong datapathId", 0x0001020304050607L, builtByFactory.getDatapathId().longValue());
        Assert.assertEquals("Wrong n-buffers", 0x00010203L, builtByFactory.getBuffers().longValue());
        Assert.assertEquals("Wrong n-tables", 0x01, builtByFactory.getTables().shortValue());
        Assert.assertEquals("Wrong capabilities", new CapabilitiesV10(true, true, true, true, true, true, true, true),
                builtByFactory.getCapabilitiesV10());
        Assert.assertEquals("Wrong actions", new ActionTypeV10(true, true, true, true, true, true, true,
                true, true, true, true, true, false), builtByFactory.getActionsV10());
        PhyPort port = builtByFactory.getPhyPort().get(0);
        Assert.assertEquals("Wrong port - port-no", 16, port.getPortNo().intValue());
        Assert.assertEquals("Wrong port - hw-addr", new MacAddress("01:01:05:01:04:02"), port.getHwAddr());
        Assert.assertEquals("Wrong port - name", new String("ALOHA"), port.getName());
        Assert.assertEquals("Wrong port - config", new PortConfigV10(true, true, true, true, true, true, true),
                port.getConfigV10());
        Assert.assertEquals("Wrong port - state",  new PortStateV10(false, false, false, false, true, false, true, false),
                port.getStateV10());
        Assert.assertEquals("Wrong port - curr", new PortFeaturesV10(true, true, true, true, true, true, true,
                true, true, true, true, true), port.getCurrentFeaturesV10());
        Assert.assertEquals("Wrong port - advertised", new PortFeaturesV10(false, false, false, false, false, false,
                false, false, false, false, false, false), port.getAdvertisedFeaturesV10());
        Assert.assertEquals("Wrong port - supported", new PortFeaturesV10(true, true, false, false, false, false,
                false, true, false, true, false, false), port.getSupportedFeaturesV10());
        Assert.assertEquals("Wrong port - peer", new PortFeaturesV10(true, false, false, false, false, false, false,
                false, true, false, false, true), port.getPeerFeaturesV10());
    }

    /**
     * Testing {@link OF10FeaturesReplyMessageFactory} for correct translation into POJO
     */
    @Test
    public void testWithTwoPortsSet() {
        ByteBuf bb = BufferHelper.buildBuffer("00 01 02 03 04 05 06 07 00 01 02 03 01 00 00 00 "
                + "00 00 00 8B 00 00 03 B5 "
                + "00 10 01 01 05 01 04 02 41 4C 4F 48 41 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 07 01 "
                + "00 00 00 31 00 00 04 42 00 00 03 0C 00 00 08 88 "
                + "00 10 01 01 05 01 04 02 41 4C 4F 48 41 00 00 00 00 00 00 00 00 00 00 00 00 00 00 15 00 00 00 00 "
                + "00 00 00 31 00 00 04 42 00 00 03 0C 00 00 08 88");
        GetFeaturesOutput builtByFactory = BufferHelper.deserialize(featuresFactory, bb);

        BufferHelper.checkHeaderV10(builtByFactory);
        Assert.assertEquals("Wrong ports size", 2, builtByFactory.getPhyPort().size());
        PhyPort port = builtByFactory.getPhyPort().get(0);
        Assert.assertEquals("Wrong port - port-no", 16, port.getPortNo().intValue());
        Assert.assertEquals("Wrong port - hw-addr", new MacAddress("01:01:05:01:04:02"), port.getHwAddr());
        Assert.assertEquals("Wrong port - name", new String("ALOHA"), port.getName());
        Assert.assertEquals("Wrong port - config", new PortConfigV10(false, false, false, false, false, false, false),
                port.getConfigV10());
        Assert.assertEquals("Wrong port - state",  new PortStateV10(false, true, false, true, true, true, false, true),
                port.getStateV10());
        port = builtByFactory.getPhyPort().get(1);
        Assert.assertEquals("Wrong port - state",  new PortStateV10(false, false, false, false, false, false, true, false),
                port.getStateV10());
    }

    /**
     * Testing {@link OF10FeaturesReplyMessageFactory} for correct translation into POJO
     */
    @Test
    public void testWithNoPortsSet() {
        ByteBuf bb = BufferHelper.buildBuffer("00 01 02 03 04 05 06 07 00 01 02 03 01 00 00 00 "
                + "00 00 00 00 00 00 00 00");
        GetFeaturesOutput builtByFactory = BufferHelper.deserialize(featuresFactory, bb);

        BufferHelper.checkHeaderV10(builtByFactory);
        Assert.assertEquals("Wrong capabilities", new CapabilitiesV10(false, false, false, false, false, false, false, false),
                builtByFactory.getCapabilitiesV10());
        Assert.assertEquals("Wrong actions", new ActionTypeV10(false, false, false, false, false, false, false,
                false, false, false, false, false, false), builtByFactory.getActionsV10());
        Assert.assertEquals("Wrong ports size", 0, builtByFactory.getPhyPort().size());
    }
}
