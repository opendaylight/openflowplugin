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
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.PortConfig;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.PortFeatures;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.PortState;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.PortStatusMessage;

/**
 * @author timotej.kubas
 * @author michal.polkorab
 */
public class PortStatusMessageFactoryTest {

    private OFDeserializer<PortStatusMessage> statusFactory;

    /**
     * Initializes deserializer registry and lookups correct deserializer
     */
    @Before
    public void startUp() {
        DeserializerRegistry registry = new DeserializerRegistryImpl();
        registry.init();
        statusFactory = registry.getDeserializer(
                new MessageCodeKey(EncodeConstants.OF13_VERSION_ID, 12, PortStatusMessage.class));
    }

    /**
     * Testing {@link PortStatusMessageFactory} for correct translation into POJO
     */
    @Test
    public void test(){
        ByteBuf bb = BufferHelper.buildBuffer("01 " + //reason
                                              "00 00 00 00 00 00 00 " + //padding
                                              "00 01 02 03 " + //port no
                                              "00 00 00 00 " + //padding in ofp_port1
                                              "08 00 27 00 B0 EB " + //mac address
                                              "00 00 " + //padding in ofp_port2
                                              "73 31 2d 65 74 68 31 00 00 00 00 00 00 00 00 00 " + // port name, String "s1-eth1"
                                              "00 00 00 41 " + //port config
                                              "00 00 00 05 " + //port state
                                              "00 00 00 81 " + //current features
                                              "00 00 00 A1 " + //advertised features
                                              "00 00 00 B1 " + //supported features
                                              "00 00 00 81 " + //peer features
                                              "00 00 00 81 " + //curr speed
                                              "00 00 00 80" //max speed
                                              );

        PortStatusMessage builtByFactory = BufferHelper.deserialize(statusFactory, bb);

        BufferHelper.checkHeaderV13(builtByFactory);
        Assert.assertEquals("Wrong reason", 0x01, builtByFactory.getReason().getIntValue());
        Assert.assertEquals("Wrong portNumber", 66051L, builtByFactory.getPortNo().longValue());
        Assert.assertEquals("Wrong macAddress", new MacAddress("08:00:27:00:b0:eb"), builtByFactory.getHwAddr());
        Assert.assertEquals("Wrong name", "s1-eth1", builtByFactory.getName());
        Assert.assertEquals("Wrong portConfig", new PortConfig(false, true, false, true), builtByFactory.getConfig());
        Assert.assertEquals("Wrong portState", new PortState(false, true, true), builtByFactory.getState());
        Assert.assertEquals("Wrong currentFeatures", new PortFeatures(false, false, false, false,
                                             false, true, false, false, false, true, false, false,
                                             false, false, false, false), builtByFactory.getCurrentFeatures());
        Assert.assertEquals("Wrong advertisedFeatures", new PortFeatures(false, false, false, false,
                                             false, true, true, false, false, true, false, false,
                                             false, false, false, false), builtByFactory.getAdvertisedFeatures());
        Assert.assertEquals("Wrong supportedFeatures", new PortFeatures(false, false, false, false,
                                             false, true, true, true, false, true, false, false,
                                             false, false, false, false), builtByFactory.getSupportedFeatures());
        Assert.assertEquals("Wrong peerFeatures", new PortFeatures(false, false, false, false,
                                                  false, true, false, false, false, true, false, false,
                                                  false, false, false, false), builtByFactory.getPeerFeatures());
        Assert.assertEquals("Wrong currSpeed", 129L, builtByFactory.getCurrSpeed().longValue());
        Assert.assertEquals("Wrong maxSpeed", 128L, builtByFactory.getMaxSpeed().longValue());
    }

    /**
     * Testing {@link PortStatusMessageFactory} for correct translation into POJO
     */
    @Test
    public void testWithDifferentBitmaps(){
        ByteBuf bb = BufferHelper.buildBuffer("01 00 00 00 00 00 00 00 " + //reason, padding
                                              "00 01 02 03 00 00 00 00 " + //port no, padding
                                              "08 00 27 00 B0 EB 00 00 " + //mac address, padding
                                              "73 31 2d 65 74 68 31 00 00 00 00 00 00 00 00 00 " + // port name, String "s1-eth1"
                                              "00 00 00 24 " + //port config
                                              "00 00 00 02 " + //port state
                                              "00 00 00 81 00 00 00 A1 " + //current + advertised features
                                              "00 00 FF FF 00 00 00 00 " + //supported + peer features
                                              "00 00 00 81 00 00 00 80" //curr speed, max speed
                                              );
        PortStatusMessage message = BufferHelper.deserialize(statusFactory, bb);

        Assert.assertEquals("Wrong portConfig", new PortConfig(true, false, true, false), message.getConfig());
        Assert.assertEquals("Wrong portState", new PortState(true, false, false), message.getState());
        Assert.assertEquals("Wrong supportedFeatures", new PortFeatures(true, true, true, true,
                     true, true, true, true, true, true, true, true, true, true, true, true),
                     message.getSupportedFeatures());
        Assert.assertEquals("Wrong peerFeatures", new PortFeatures(false, false, false, false,
                     false, false, false, false, false, false, false, false, false, false,
                     false, false), message.getPeerFeatures());
    }
}
