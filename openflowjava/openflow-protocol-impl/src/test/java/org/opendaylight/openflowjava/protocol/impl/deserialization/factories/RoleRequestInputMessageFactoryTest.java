/*
 * Copyright (c) 2015 NetIDE Consortium and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowjava.protocol.impl.deserialization.factories;

import io.netty.buffer.ByteBuf;
import java.math.BigInteger;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.opendaylight.openflowjava.protocol.api.extensibility.DeserializerRegistry;
import org.opendaylight.openflowjava.protocol.api.extensibility.OFDeserializer;
import org.opendaylight.openflowjava.protocol.api.keys.MessageCodeKey;
import org.opendaylight.openflowjava.protocol.api.util.EncodeConstants;
import org.opendaylight.openflowjava.protocol.impl.deserialization.DeserializerRegistryImpl;
import org.opendaylight.openflowjava.protocol.impl.util.BufferHelper;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.ControllerRole;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.RoleRequestInput;

/**
 * @author giuseppex.petralia@intel.com
 *
 */
public class RoleRequestInputMessageFactoryTest {
    private OFDeserializer<RoleRequestInput> factory;

    @Before
    public void startUp() {
        DeserializerRegistry desRegistry = new DeserializerRegistryImpl();
        desRegistry.init();
        factory = desRegistry
                .getDeserializer(new MessageCodeKey(EncodeConstants.OF13_VERSION_ID, 24, RoleRequestInput.class));

    }

    @Test
    public void test() {
        ByteBuf bb = BufferHelper.buildBuffer("00 00 00 02 00 00 00 00 ff 01 01 01 01 01 01 01");
        RoleRequestInput deserializedMessage = BufferHelper.deserialize(factory, bb);
        BufferHelper.checkHeaderV13(deserializedMessage);
        Assert.assertEquals("Wrong role", ControllerRole.forValue(2), deserializedMessage.getRole());
        byte[] generationId = new byte[] { (byte) 0xFF, 0x01, 0x01, 0x01, 0x01, 0x01, 0x01, 0x01 };
        Assert.assertEquals("Wrong generation Id", new BigInteger(1, generationId),
                deserializedMessage.getGenerationId());
    }

}
