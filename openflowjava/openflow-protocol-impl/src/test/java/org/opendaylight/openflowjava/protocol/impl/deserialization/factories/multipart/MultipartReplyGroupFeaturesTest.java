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
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.ActionType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.GroupCapabilities;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.GroupTypes;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.MultipartReplyMessage;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.MultipartReplyGroupFeaturesCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.group.features._case.MultipartReplyGroupFeatures;

/**
 * Unit tests for MultipartReplyGroupFeatures.
 *
 * @author michal.polkorab
 */
public class MultipartReplyGroupFeaturesTest {

    private final MultipartReplyMessageFactory factory =
            new MultipartReplyMessageFactory(mock(DeserializerRegistry.class));

    /**
     * Testing {@link MultipartReplyMessageFactory} for correct translation into POJO.
     */
    @Test
    public void testMultipartReplyGroupFeatures() {
        ByteBuf bb = BufferHelper.buildBuffer("00 08 00 01 00 00 00 00 " + //
                                              "00 00 00 0F " + // types
                                              "00 00 00 0F " + // capabilities
                                              "00 00 00 01 " + // max groups
                                              "00 00 00 02 " + // max groups
                                              "00 00 00 03 " + // max groups
                                              "00 00 00 04 " + // max groups
                                              "0F FF 98 01 " + // actions bitmap (all actions included)
                                              "00 00 00 00 " + // actions bitmap (no actions included)
                                              "00 00 00 00 " + // actions bitmap (no actions included)
                                              "00 00 00 00"// actions bitmap (no actions included)
                                              );
        MultipartReplyMessage builtByFactory = BufferHelper.deserialize(factory, bb);

        BufferHelper.checkHeaderV13(builtByFactory);
        assertEquals("Wrong type", 8, builtByFactory.getType().getIntValue());
        assertEquals("Wrong flag", true, builtByFactory.getFlags().isOFPMPFREQMORE());
        MultipartReplyGroupFeaturesCase messageCase =
                (MultipartReplyGroupFeaturesCase) builtByFactory.getMultipartReplyBody();
        MultipartReplyGroupFeatures message = messageCase.getMultipartReplyGroupFeatures();
        assertEquals("Wrong group types", new GroupTypes(true, true, true, true), message.getTypes());
        assertEquals("Wrong capabilities", new GroupCapabilities(true, true, true, true),
                message.getCapabilities());
        assertEquals("Wrong max groups", 1, message.getMaxGroups().get(0).intValue());
        assertEquals("Wrong max groups", 2, message.getMaxGroups().get(1).intValue());
        assertEquals("Wrong max groups", 3, message.getMaxGroups().get(2).intValue());
        assertEquals("Wrong max groups", 4, message.getMaxGroups().get(3).intValue());
        assertEquals("Wrong actions bitmap", new ActionType(true, true, true, true, false, true, true, true,
                true, true, true, true, true, true, true, true, true), message.getActionsBitmap().get(0));
        assertEquals("Wrong actions bitmap", new ActionType(false, false, false, false, false, false, false,
                false, false, false, false, false, false, false, false, false, false),
                message.getActionsBitmap().get(1));
        assertEquals("Wrong actions bitmap", new ActionType(false, false, false, false, false, false, false,
                false, false, false, false, false, false, false, false, false, false),
                message.getActionsBitmap().get(2));
        assertEquals("Wrong actions bitmap", new ActionType(false, false, false, false, false, false, false,
                false, false, false, false, false, false, false, false, false, false),
                message.getActionsBitmap().get(3));
    }

    /**
     * Testing {@link MultipartReplyMessageFactory} for correct translation into POJO
     * (with different group types and capabilities).
     */
    @Test
    public void testMultipartReplyGroupFeatures2() {
        ByteBuf bb = BufferHelper.buildBuffer("00 08 00 01 00 00 00 00 " + //
                                              "00 00 00 00 " + // types
                                              "00 00 00 00 " + // capabilities
                                              "00 00 00 01 " + // max groups
                                              "00 00 00 02 " + // max groups
                                              "00 00 00 03 " + // max groups
                                              "00 00 00 04 " + // max groups
                                              "00 00 00 00 " + // actions bitmap (all actions included)
                                              "00 00 00 00 " + // actions bitmap (no actions included)
                                              "00 00 00 00 " + // actions bitmap (no actions included)
                                              "00 00 00 00"// actions bitmap (no actions included)
                                              );
        MultipartReplyMessage builtByFactory = BufferHelper.deserialize(factory, bb);

        BufferHelper.checkHeaderV13(builtByFactory);
        assertEquals("Wrong type", 8, builtByFactory.getType().getIntValue());
        assertEquals("Wrong flag", true, builtByFactory.getFlags().isOFPMPFREQMORE());
        MultipartReplyGroupFeaturesCase messageCase =
                (MultipartReplyGroupFeaturesCase) builtByFactory.getMultipartReplyBody();
        MultipartReplyGroupFeatures message = messageCase.getMultipartReplyGroupFeatures();
        assertEquals("Wrong group types", new GroupTypes(false, false, false, false), message.getTypes());
        assertEquals("Wrong capabilities", new GroupCapabilities(false, false, false, false),
                message.getCapabilities());
    }
}
