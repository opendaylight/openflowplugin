/*
 * Copyright (c) 2015 NetIDE Consortium and others.  All rights reserved.
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
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.SwitchConfigFlag;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.SetConfigInput;

/**
 * Test for {@link org.opendaylight.openflowjava.protocol.impl.deserialization.factories.SetConfigInputMessageFactory}.
 * @author giuseppex.petralia@intel.com
 */
public class SetConfigInputMessageFactoryTest extends DefaultDeserializerFactoryTest<SetConfigInput> {

    /**
     * Initializes deserializer registry and lookups OF13 deserializer.
     */
    public SetConfigInputMessageFactoryTest() {
        super(new MessageCodeKey(EncodeConstants.OF13_VERSION_ID, 9, SetConfigInput.class));
    }

    /**
     * Testing {@link SetConfigInputMessageFactory} for correct header version.
     */
    @Test
    public void testVersions() {
        List<Byte> versions = new ArrayList<>(Arrays.asList(
                EncodeConstants.OF10_VERSION_ID,
                EncodeConstants.OF13_VERSION_ID,
                EncodeConstants.OF14_VERSION_ID,
                EncodeConstants.OF15_VERSION_ID
        ));
        ByteBuf bb = BufferHelper.buildBuffer("00 02 " + "00 0a");
        testHeaderVersions(versions, bb);
    }

    @Test
    public void test() {
        ByteBuf bb = BufferHelper.buildBuffer("00 02 " + "00 0a");
        SetConfigInput deserializedMessage = BufferHelper.deserialize(factory, bb);

        // Test Message
        Assert.assertEquals("Wrong flags ", SwitchConfigFlag.forValue(2), deserializedMessage.getFlags());
        Assert.assertEquals("Wrong Miss Send len ", 10, deserializedMessage.getMissSendLen().intValue());
    }

}