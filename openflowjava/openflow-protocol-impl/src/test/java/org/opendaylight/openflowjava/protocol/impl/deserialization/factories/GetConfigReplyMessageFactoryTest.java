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
import org.junit.Assert;
import org.junit.Test;
import org.opendaylight.openflowjava.protocol.api.keys.MessageCodeKey;
import org.opendaylight.openflowjava.protocol.api.util.EncodeConstants;
import org.opendaylight.openflowjava.protocol.impl.util.BufferHelper;
import org.opendaylight.openflowjava.protocol.impl.util.DefaultDeserializerFactoryTest;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.GetConfigOutput;

/**
 * Test for {@link org.opendaylight.openflowjava.protocol.impl.deserialization.factories.GetConfigReplyMessageFactory}.
 * @author michal.polkorab
 * @author timotej.kubas
 */
public class GetConfigReplyMessageFactoryTest extends DefaultDeserializerFactoryTest<GetConfigOutput> {

    /**
     * Initializes deserializer registry and lookups OF13 deserializer.
     */
    public GetConfigReplyMessageFactoryTest() {
        super(new MessageCodeKey(EncodeConstants.OF13_VERSION_ID, 8, GetConfigOutput.class));
    }

    /**
     * Testing {@link GetConfigReplyMessageFactory} for correct header version.
     */
    @Test
    public void testVersions() {
        List<Byte> versions = new ArrayList<>(Arrays.asList(
                EncodeConstants.OF10_VERSION_ID,
                EncodeConstants.OF13_VERSION_ID,
                EncodeConstants.OF14_VERSION_ID,
                EncodeConstants.OF15_VERSION_ID
        ));
        ByteBuf bb = BufferHelper.buildBuffer("00 01 00 03");
        testHeaderVersions(versions, bb);
    }

    /**
     * Testing {@link GetConfigReplyMessageFactory} for correct translation into POJO.
     */
    @Test
    public void test() {
        ByteBuf bb = BufferHelper.buildBuffer("00 01 00 03");
        GetConfigOutput builtByFactory = BufferHelper.deserialize(factory, bb);
        Assert.assertEquals("Wrong switchConfigFlag", 0x01, builtByFactory.getFlags().getIntValue());
        Assert.assertEquals("Wrong missSendLen", 0x03, builtByFactory.getMissSendLen().intValue());
    }

}
