/*
 * Copyright (c) 2013 Pantheon Technologies s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowjava.protocol.impl.serialization.factories;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.UnpooledByteBufAllocator;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.opendaylight.openflowjava.protocol.api.extensibility.OFSerializer;
import org.opendaylight.openflowjava.protocol.api.extensibility.SerializerRegistry;
import org.opendaylight.openflowjava.protocol.api.keys.MessageTypeKey;
import org.opendaylight.openflowjava.protocol.api.util.EncodeConstants;
import org.opendaylight.openflowjava.protocol.impl.serialization.SerializerRegistryImpl;
import org.opendaylight.openflowjava.protocol.impl.util.BufferHelper;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.EchoInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.EchoInputBuilder;

/**
 * @author michal.polkorab
 *
 */
public class EchoInputMessageFactoryTest {

    private static final byte ECHO_REQUEST_MESSAGE_CODE_TYPE = 2;
    private SerializerRegistry registry;
    private OFSerializer<EchoInput> echoFactory;

    /**
     * Initializes serializer registry and stores correct factory in field
     */
    @Before
    public void startUp() {
        registry = new SerializerRegistryImpl();
        registry.init();
        echoFactory = registry.getSerializer(
                new MessageTypeKey<>(EncodeConstants.OF13_VERSION_ID, EchoInput.class));
    }

    /**
     * Testing of {@link EchoInputMessageFactory} for correct translation from POJO
     * @throws Exception
     */
    @Test
    public void testV13() throws Exception {
        EchoInputBuilder eib = new EchoInputBuilder();
        BufferHelper.setupHeader(eib, EncodeConstants.OF13_VERSION_ID);
        EchoInput ei = eib.build();
        ByteBuf out = UnpooledByteBufAllocator.DEFAULT.buffer();
        echoFactory.serialize(ei, out);
        BufferHelper.checkHeaderV13(out, ECHO_REQUEST_MESSAGE_CODE_TYPE, 8);
    }

    /**
     * Testing of {@link EchoInputMessageFactory} for correct translation from POJO
     * @throws Exception
     */
    @Test
    public void testV10() throws Exception {
        EchoInputBuilder eib = new EchoInputBuilder();
        BufferHelper.setupHeader(eib, EncodeConstants.OF10_VERSION_ID);
        EchoInput ei = eib.build();
        ByteBuf out = UnpooledByteBufAllocator.DEFAULT.buffer();
        echoFactory.serialize(ei, out);
        BufferHelper.checkHeaderV10(out, ECHO_REQUEST_MESSAGE_CODE_TYPE, 8);
    }

    /**
     * Testing of {@link EchoInputMessageFactory} for correct data serialization
     * @throws Exception
     */
    @Test
    public void testData() throws Exception{
        byte[] dataToTest = new byte[]{91,92,93,94,95,96,97,98};
        EchoInputBuilder eib = new EchoInputBuilder();
        BufferHelper.setupHeader(eib, EncodeConstants.OF13_VERSION_ID);
        eib.setData(dataToTest);
        EchoInput ei = eib.build();
        ByteBuf out = UnpooledByteBufAllocator.DEFAULT.buffer();
        echoFactory.serialize(ei, out);
        BufferHelper.checkHeaderV13(out, ECHO_REQUEST_MESSAGE_CODE_TYPE, 8+dataToTest.length);
        byte[] outData = new byte[dataToTest.length];
        out.readBytes(outData);
        Assert.assertArrayEquals("Wrong - different output data.", dataToTest, outData);
        out.release();
    }
}
