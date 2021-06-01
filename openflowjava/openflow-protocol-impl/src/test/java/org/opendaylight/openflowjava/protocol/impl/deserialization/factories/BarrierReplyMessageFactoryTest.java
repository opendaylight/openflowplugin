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
import java.util.Arrays;
import java.util.List;
import org.junit.Test;
import org.opendaylight.openflowjava.protocol.api.keys.MessageCodeKey;
import org.opendaylight.openflowjava.protocol.api.util.EncodeConstants;
import org.opendaylight.openflowjava.protocol.impl.util.BufferHelper;
import org.opendaylight.openflowjava.protocol.impl.util.DefaultDeserializerFactoryTest;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.BarrierOutput;
import org.opendaylight.yangtools.yang.common.Uint8;

/**
 * Test for {@link org.opendaylight.openflowjava.protocol.impl.deserialization.factories.BarrierReplyMessageFactory}.
 * @author michal.polkorab
 * @author timotej.kubas
 */
public class BarrierReplyMessageFactoryTest extends DefaultDeserializerFactoryTest<BarrierOutput> {
    /**
     * Initializes deserializer registry and lookups OF13 deserializer.
     */
    public BarrierReplyMessageFactoryTest() {
        super(new MessageCodeKey(EncodeConstants.OF_VERSION_1_3, 21, BarrierOutput.class));
    }

    /**
     * Testing of {@link BarrierReplyMessageFactory} for correct header version.
     */
    @Test
    public void testVersions() {
        List<Uint8> versions = new ArrayList<>(Arrays.asList(
                EncodeConstants.OF_VERSION_1_3,
                EncodeConstants.OF_VERSION_1_4,
                EncodeConstants.OF_VERSION_1_5
        ));
        ByteBuf bb = BufferHelper.buildBuffer();
        testHeaderVersions(versions, bb);

        // OFP v1.0 need to be tested separately cause of different message type value
        messageCodeKey = new MessageCodeKey(EncodeConstants.OF_VERSION_1_0, 19, BarrierOutput.class);
        testHeaderVersions(List.of(EncodeConstants.OF_VERSION_1_0), bb);
    }
}
