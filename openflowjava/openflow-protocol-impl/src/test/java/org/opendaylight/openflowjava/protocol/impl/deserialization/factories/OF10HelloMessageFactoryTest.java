/*
 * Copyright (c) 2013 Pantheon Technologies s.r.o. and others. All rights reserved.
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
import org.opendaylight.openflowjava.protocol.impl.deserialization.DeserializerRegistryImpl;
import org.opendaylight.openflowjava.protocol.impl.util.BufferHelper;
import org.opendaylight.openflowjava.protocol.api.util.EncodeConstants;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.HelloMessage;

/**
 * @author michal.polkorab
 */
public class OF10HelloMessageFactoryTest {

    private OFDeserializer<HelloMessage> helloFactory;

    /**
     * Initializes deserializer registry and lookups correct deserializer
     */
    @Before
    public void startUp() {
        DeserializerRegistry registry = new DeserializerRegistryImpl();
        registry.init();
        helloFactory = registry.getDeserializer(
                new MessageCodeKey(EncodeConstants.OF10_VERSION_ID, 0, HelloMessage.class));
    }

    /**
     * Testing {@link OF10HelloMessageFactory} for correct translation into POJO
     */
    @Test
    public void testWithoutElements() {
        ByteBuf bb = BufferHelper.buildBuffer();
        HelloMessage builtByFactory = BufferHelper.deserialize(
                helloFactory, bb);

        BufferHelper.checkHeaderV10(builtByFactory);
        Assert.assertNull("Wrong elements", builtByFactory.getElements());
    }

	/**
     * Testing {@link OF10HelloMessageFactory} for correct translation into POJO
     */
    @Test
    public void testWithElements() {
        ByteBuf bb = BufferHelper.buildBuffer("00 01 " // type
                                            + "00 0c " // length
                                            + "00 00 00 11 " // bitmap 1
                                            + "00 00 00 00 " // bitmap 2
                                            + "00 00 00 00"  // padding
                );
        HelloMessage builtByFactory = BufferHelper.deserialize(
                helloFactory, bb);

        BufferHelper.checkHeaderV10(builtByFactory);
        Assert.assertNull("Wrong elements", builtByFactory.getElements());
    }

}
