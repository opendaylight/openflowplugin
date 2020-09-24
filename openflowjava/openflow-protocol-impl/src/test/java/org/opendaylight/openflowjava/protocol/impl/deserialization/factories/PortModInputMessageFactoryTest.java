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
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.PortConfig;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.PortFeatures;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.PortNumber;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.PortModInput;
import org.opendaylight.yangtools.yang.common.Uint32;

/**
 * Unit tests for PortModInputMessageFactory.
 *
 * @author giuseppex.petralia@intel.com
 */
public class PortModInputMessageFactoryTest {
    private OFDeserializer<PortModInput> factory;

    @Before
    public void startUp() {
        DeserializerRegistry desRegistry = new DeserializerRegistryImpl();
        desRegistry.init();
        factory = desRegistry
                .getDeserializer(new MessageCodeKey(EncodeConstants.OF13_VERSION_ID, 16, PortModInput.class));
    }

    @Test
    public void test() {
        ByteBuf bb = BufferHelper.buildBuffer(
                "00 00 00 09 00 00 00 00 08 00 27 00 " + "b0 eb 00 00 00 00 00 24 00 00 00 41 00 00 01 10 00 00 00 00");
        PortModInput deserializedMessage = BufferHelper.deserialize(factory, bb);
        BufferHelper.checkHeaderV13(deserializedMessage);

        // Test Message
        Assert.assertEquals("Wrong port", new PortNumber(Uint32.valueOf(9)), deserializedMessage.getPortNo());
        Assert.assertEquals("Wrong hwAddr", new MacAddress("08:00:27:00:b0:eb"), deserializedMessage.getHwAddress());
        Assert.assertEquals("Wrong config", new PortConfig(true, false, true, false), deserializedMessage.getConfig());
        Assert.assertEquals("Wrong mask", new PortConfig(false, true, false, true), deserializedMessage.getMask());
        Assert.assertEquals("Wrong advertise", new PortFeatures(true, false, false, false, false, false, false, true,
                false, false, false, false, false, false, false, false), deserializedMessage.getAdvertise());
    }
}
