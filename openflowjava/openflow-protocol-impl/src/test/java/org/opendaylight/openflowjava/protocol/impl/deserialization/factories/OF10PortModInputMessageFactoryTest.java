/*
 * Copyright (c) 2015 NetIDE Consortium and others.  All rights reserved.
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
import org.opendaylight.openflowjava.protocol.api.util.EncodeConstants;
import org.opendaylight.openflowjava.protocol.impl.deserialization.DeserializerRegistryImpl;
import org.opendaylight.openflowjava.protocol.impl.util.BufferHelper;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev130715.MacAddress;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.PortConfigV10;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.PortFeaturesV10;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.PortNumber;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.PortModInput;
import org.opendaylight.yangtools.yang.common.Uint32;

/**
 * Unit tests for OF10PortModInputMessageFactory.
 *
 * @author giuseppex.petralia@intel.com
 */
public class OF10PortModInputMessageFactoryTest {
    private OFDeserializer<PortModInput> factory;

    @Before
    public void startUp() {
        DeserializerRegistry desRegistry = new DeserializerRegistryImpl();
        desRegistry.init();
        factory = desRegistry
                .getDeserializer(new MessageCodeKey(EncodeConstants.OF10_VERSION_ID, 15, PortModInput.class));
    }

    @Test
    public void test() {
        ByteBuf bb = BufferHelper
                .buildBuffer("19 e9 08 00 27 00 b0 eb " + "00 00 00 15 00 00 00 62 00 00 02 8c 00 00 00 00 ");
        PortModInput deserializedMessage = BufferHelper.deserialize(factory, bb);
        BufferHelper.checkHeaderV10(deserializedMessage);
        Assert.assertEquals("Wrong port", new PortNumber(Uint32.valueOf(6633)), deserializedMessage.getPortNo());
        Assert.assertEquals("Wrong hwAddr", new MacAddress("08:00:27:00:b0:eb"), deserializedMessage.getHwAddress());
        Assert.assertEquals("Wrong config", new PortConfigV10(true, false, false, true, false, false, true),
                deserializedMessage.getConfigV10());
        Assert.assertEquals("Wrong mask", new PortConfigV10(false, true, true, false, false, true, false),
                deserializedMessage.getMaskV10());
        Assert.assertEquals("Wrong advertise",
                new PortFeaturesV10(true, true, false, false, false, false, false, true, true, false, false, false),
                deserializedMessage.getAdvertiseV10());
    }
}
