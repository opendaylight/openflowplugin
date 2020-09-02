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
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.MeterBandTypeBitmap;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.MeterFlags;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.MultipartReplyMessage;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.MultipartReplyMeterFeaturesCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.meter.features._case.MultipartReplyMeterFeatures;

/**
 * Unit tests for MultipartReplyMeterFeatures.
 *
 * @author michal.polkorab
 */
public class MultipartReplyMeterFeaturesTest {
    private final MultipartReplyMessageFactory factory =
            new MultipartReplyMessageFactory(mock(DeserializerRegistry.class));

    /**
     * Testing {@link MultipartReplyMessageFactory} for correct translation into POJO.
     */
    @Test
    public void testMultipartReplyMeterFeatures() {
        ByteBuf bb = BufferHelper.buildBuffer("00 0B 00 01 00 00 00 00 " + //
                                              "00 00 00 0A " + // maxMeter
                                              "00 00 00 06 " + // bandTypes
                                              "00 00 00 0F " + // capabilities
                                              "07 08 00 00" // maxBands, maxColor, padding
                                              );
        MultipartReplyMessage builtByFactory = BufferHelper.deserialize(factory, bb);

        BufferHelper.checkHeaderV13(builtByFactory);
        assertEquals("Wrong type", 11, builtByFactory.getType().getIntValue());
        assertEquals("Wrong flag", true, builtByFactory.getFlags().isOFPMPFREQMORE());
        MultipartReplyMeterFeaturesCase messageCase =
                (MultipartReplyMeterFeaturesCase) builtByFactory.getMultipartReplyBody();
        MultipartReplyMeterFeatures message = messageCase.getMultipartReplyMeterFeatures();
        assertEquals("Wrong maxMeter", 10, message.getMaxMeter().intValue());
        assertEquals("Wrong bandTypes", new MeterBandTypeBitmap(true, true), message.getBandTypes());
        assertEquals("Wrong capabilities", new MeterFlags(true, true, true, true), message.getCapabilities());
        assertEquals("Wrong maxBands", 7, message.getMaxBands().intValue());
        assertEquals("Wrong maxColor", 8, message.getMaxColor().intValue());
    }

    /**
     * Testing {@link MultipartReplyMessageFactory} for correct translation into POJO.
     */
    @Test
    public void testMultipartReplyMeterFeatures2() {
        ByteBuf bb = BufferHelper.buildBuffer("00 0B 00 01 00 00 00 00 " + //
                                              "00 00 00 09 " + // maxMeter
                                              "00 00 00 00 " + // bandTypes
                                              "00 00 00 00 " + // capabilities
                                              "03 04 00 00" // maxBands, maxColor, padding
                                              );
        MultipartReplyMessage builtByFactory = BufferHelper.deserialize(factory, bb);

        BufferHelper.checkHeaderV13(builtByFactory);
        assertEquals("Wrong type", 11, builtByFactory.getType().getIntValue());
        assertEquals("Wrong flag", true, builtByFactory.getFlags().isOFPMPFREQMORE());
        MultipartReplyMeterFeaturesCase messageCase =
                (MultipartReplyMeterFeaturesCase) builtByFactory.getMultipartReplyBody();
        MultipartReplyMeterFeatures message = messageCase.getMultipartReplyMeterFeatures();
        assertEquals("Wrong maxMeter", 9, message.getMaxMeter().intValue());
        assertEquals("Wrong bandTypes", new MeterBandTypeBitmap(false, false), message.getBandTypes());
        assertEquals("Wrong capabilities", new MeterFlags(false, false, false, false), message.getCapabilities());
        assertEquals("Wrong maxBands", 3, message.getMaxBands().intValue());
        assertEquals("Wrong maxColor", 4, message.getMaxColor().intValue());
    }
}
