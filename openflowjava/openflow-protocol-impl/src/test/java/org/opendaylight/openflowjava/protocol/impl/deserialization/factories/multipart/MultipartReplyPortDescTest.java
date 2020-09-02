/*
 * Copyright (c) 2014 Pantheon Technologies s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowjava.protocol.impl.deserialization.factories.multipart;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;

import io.netty.buffer.ByteBuf;
import org.junit.Test;
import org.opendaylight.openflowjava.protocol.api.extensibility.DeserializerRegistry;
import org.opendaylight.openflowjava.protocol.impl.deserialization.factories.MultipartReplyMessageFactory;
import org.opendaylight.openflowjava.protocol.impl.util.BufferHelper;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev130715.MacAddress;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.PortConfig;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.PortFeatures;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.PortState;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.MultipartReplyMessage;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.MultipartReplyPortDescCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.port.desc._case.MultipartReplyPortDesc;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.port.desc._case.multipart.reply.port.desc.Ports;

/**
 * Unit tests for MultipartReplyPortDesc.
 *
 * @author michal.polkorab
 */
public class MultipartReplyPortDescTest {
    private final MultipartReplyMessageFactory factory =
            new MultipartReplyMessageFactory(mock(DeserializerRegistry.class));

    /**
     * Testing {@link MultipartReplyMessageFactory} for correct translation into POJO.
     */
    @Test
    public void testEmptyMultipartReplyPortDesc() {
        ByteBuf bb = BufferHelper.buildBuffer("00 0D 00 00 00 00 00 00");
        MultipartReplyMessage builtByFactory = BufferHelper.deserialize(factory, bb);

        BufferHelper.checkHeaderV13(builtByFactory);
        assertEquals("Wrong type", 13, builtByFactory.getType().getIntValue());
        assertEquals("Wrong flag", false, builtByFactory.getFlags().isOFPMPFREQMORE());
        MultipartReplyPortDescCase messageCase = (MultipartReplyPortDescCase) builtByFactory.getMultipartReplyBody();
        MultipartReplyPortDesc message = messageCase.getMultipartReplyPortDesc();
        assertEquals("Wrong table features size", 0, message.nonnullPorts().size());
    }

    /**
     * Testing {@link MultipartReplyMessageFactory} for correct translation into POJO.
     */
    @Test
    public void testMultipartReplyPortDesc() {
        ByteBuf bb = BufferHelper.buildBuffer("00 0D 00 00 00 00 00 00 " + //
                                              // first port desc
                                              "00 01 02 03 00 00 00 00 " + // portNo, padding
                                              "08 00 27 00 B0 EB 00 00 " + // mac address, padding
                                              "4F 70 65 6E 64 61 79 6C 69 67 68 74 00 00 00 00 " + // name
                                              "00 00 00 65 " + //port config
                                              "00 00 00 07 " + //port state
                                              "00 00 00 81 " + //current features
                                              "00 00 FF FF " + //advertised features
                                              "00 00 C1 89 " + //supported features
                                              "00 00 C5 8D " + //peer features
                                              "00 00 00 81 " + //curr speed
                                              "00 00 00 80 " + //max speed
                                              // second port desc
                                              "00 00 00 01 00 00 00 00 " + // portNo, padding
                                              "08 00 27 00 B0 EB 00 00 " + // mac address, padding
                                              "4F 70 65 6E 64 61 79 6C 69 67 68 74 00 00 00 00 " + // name
                                              "00 00 00 00 " + //port config
                                              "00 00 00 00 " + //port state
                                              "00 00 00 00 " + //current features
                                              "00 00 00 00 " + //advertised features
                                              "00 00 00 00 " + //supported features
                                              "00 00 00 00 " + //peer features
                                              "00 00 00 05 " + //curr speed
                                              "00 00 00 06" //max speed
                                              );
        MultipartReplyMessage builtByFactory = BufferHelper.deserialize(factory, bb);

        BufferHelper.checkHeaderV13(builtByFactory);
        assertEquals("Wrong type", 13, builtByFactory.getType().getIntValue());
        assertEquals("Wrong flag", false, builtByFactory.getFlags().isOFPMPFREQMORE());
        MultipartReplyPortDescCase messageCase = (MultipartReplyPortDescCase) builtByFactory.getMultipartReplyBody();
        MultipartReplyPortDesc message = messageCase.getMultipartReplyPortDesc();
        assertEquals("Wrong port desc size", 2, message.getPorts().size());
        Ports port = message.getPorts().get(0);
        assertEquals("Wrong portNo", 66051L, port.getPortNo().longValue());
        assertEquals("Wrong macAddress", new MacAddress("08:00:27:00:b0:eb"), port.getHwAddr());
        assertEquals("Wrong portName", "Opendaylight", port.getName());
        assertEquals("Wrong portConfig", new PortConfig(true, true, true, true), port.getConfig());
        assertEquals("Wrong portState", new PortState(true, true, true), port.getState());
        assertEquals("Wrong currentFeatures", new PortFeatures(false, false, false, false, false, true,
                false, false, false, true, false, false, false, false, false, false), port.getCurrentFeatures());
        assertEquals("Wrong advertisedFeatures",  new PortFeatures(true, true, true, true, true, true,
                true, true, true, true, true, true, true, true, true, true), port.getAdvertisedFeatures());
        assertEquals("Wrong supportedFeatures", new PortFeatures(true, true, false, false, false, true,
                false, false, false, true, false, false, false, false, true, true), port.getSupportedFeatures());
        assertEquals("Wrong peerFeatures", new PortFeatures(true, true, true, false, false, true, false,
                false, false, true, false, false, false, true, true, true), port.getPeerFeatures());
        assertEquals("Wrong currSpeed", 129L, port.getCurrSpeed().longValue());
        assertEquals("Wrong maxSpeed", 128L, port.getMaxSpeed().longValue());
        port = message.getPorts().get(1);
        assertEquals("Wrong portNo", 1L, port.getPortNo().longValue());
        assertEquals("Wrong macAddress", new MacAddress("08:00:27:00:b0:eb"), port.getHwAddr());
        assertEquals("Wrong portName", "Opendaylight", port.getName());
        assertEquals("Wrong portConfig", new PortConfig(false, false, false, false), port.getConfig());
        assertEquals("Wrong portState", new PortState(false, false, false), port.getState());
        assertEquals("Wrong currentFeatures", new PortFeatures(false, false, false, false, false, false, false,
                false, false, false, false, false, false, false, false, false), port.getCurrentFeatures());
        assertEquals("Wrong advertisedFeatures",
                new PortFeatures(false, false, false, false, false, false, false,
                false, false, false, false, false, false, false, false, false), port.getAdvertisedFeatures());
        assertEquals("Wrong supportedFeatures", new PortFeatures(false, false, false, false, false, false, false,
                false, false, false, false, false, false, false, false, false), port.getSupportedFeatures());
        assertEquals("Wrong peerFeatures", new PortFeatures(false, false, false, false, false, false, false,
                false, false, false, false, false, false, false, false, false), port.getPeerFeatures());
        assertEquals("Wrong currSpeed", 5L, port.getCurrSpeed().longValue());
        assertEquals("Wrong maxSpeed", 6L, port.getMaxSpeed().longValue());
    }
}
