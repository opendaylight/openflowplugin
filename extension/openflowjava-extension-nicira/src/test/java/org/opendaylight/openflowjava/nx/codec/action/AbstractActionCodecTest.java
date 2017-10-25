/**
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowjava.nx.codec.action;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import org.junit.Before;
import org.junit.Test;
import org.opendaylight.openflowjava.nx.api.NiciraConstants;
import org.opendaylight.openflowjava.protocol.api.util.EncodeConstants;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.actions.grouping.ActionBuilder;

public class AbstractActionCodecTest {


    private ByteBuf buffer;
    private byte[] bytes = new byte[10];

    private final int msgLength = 10;
    private final byte subType = 5;


    @Before
    public void setUp() {
        buffer = ByteBufAllocator.DEFAULT.buffer();
    }


    @Test
    public void serializeHeaderTest() {
        AbstractActionCodec.serializeHeader(msgLength, subType, buffer);
        assertEquals(EncodeConstants.EXPERIMENTER_VALUE, buffer.readUnsignedShort());
        assertEquals(msgLength, buffer.readUnsignedShort());
        assertEquals(NiciraConstants.NX_VENDOR_ID.intValue(), buffer.readUnsignedInt());
        assertEquals(subType, buffer.readUnsignedShort());
    }

    @Test
    public void deserializeHeaderTest() {
        buffer.writeBytes(bytes);
        int readerIndex = buffer.readerIndex();
        ActionBuilder actionBuilder = AbstractActionCodec.deserializeHeader(buffer);
        assertNotNull(actionBuilder);
        assertEquals(NiciraConstants.NX_VENDOR_ID, actionBuilder.getExperimenterId().getValue());
        assertTrue(buffer.readerIndex() - readerIndex == 10);
    }

}