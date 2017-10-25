/*
 * Copyright (c) 2013 Pantheon Technologies s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowjava.protocol.impl.serialization.factories;

import org.junit.Assert;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.UnpooledByteBufAllocator;

import org.junit.Before;
import org.junit.Test;
import org.opendaylight.openflowjava.protocol.api.extensibility.OFSerializer;
import org.opendaylight.openflowjava.protocol.api.extensibility.SerializerRegistry;
import org.opendaylight.openflowjava.protocol.api.keys.MessageTypeKey;
import org.opendaylight.openflowjava.protocol.api.util.EncodeConstants;
import org.opendaylight.openflowjava.protocol.impl.serialization.SerializerRegistryImpl;
import org.opendaylight.openflowjava.protocol.impl.util.BufferHelper;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.EchoReplyInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.EchoReplyInputBuilder;

/**
 * @author michal.polkorab
 *
 */
public class EchoReplyInputMessageFactoryTest {

    private static final byte ECHO_REPLY_MESSAGE_CODE_TYPE = 3;
    private SerializerRegistry registry;
    private OFSerializer<EchoReplyInput> echoFactory;

    /**
     * Initializes serializer registry and stores correct factory in field
     */
    @Before
    public void startUp() {
        registry = new SerializerRegistryImpl();
        registry.init();
        echoFactory = registry.getSerializer(
                new MessageTypeKey<>(EncodeConstants.OF13_VERSION_ID, EchoReplyInput.class));
    }

    /**
     * Testing of {@link EchoReplyInputMessageFactory} for correct translation from POJO
     * @throws Exception
     */
    @Test
    public void testV13() throws Exception {
        EchoReplyInputBuilder erib = new EchoReplyInputBuilder();
        BufferHelper.setupHeader(erib, EncodeConstants.OF13_VERSION_ID);
        EchoReplyInput eri = erib.build();
        ByteBuf out = UnpooledByteBufAllocator.DEFAULT.buffer();
        echoFactory.serialize(eri, out);
        BufferHelper.checkHeaderV13(out, ECHO_REPLY_MESSAGE_CODE_TYPE, 8);
    }

    /**
     * Testing of {@link EchoReplyInputMessageFactory} for correct translation from POJO
     * @throws Exception
     */
    @Test
    public void testV10() throws Exception {
        EchoReplyInputBuilder erib = new EchoReplyInputBuilder();
        BufferHelper.setupHeader(erib, EncodeConstants.OF10_VERSION_ID);
        EchoReplyInput eri = erib.build();
        ByteBuf out = UnpooledByteBufAllocator.DEFAULT.buffer();
        echoFactory.serialize(eri, out);
        BufferHelper.checkHeaderV10(out, ECHO_REPLY_MESSAGE_CODE_TYPE, 8);
    }

    /**
     * Testing of {@link EchoReplyInputMessageFactory} for correct message serialization
     * @throws Exception
     */
    @Test
    public void testDataSerialize()throws Exception {
        byte[] dataToTest = new byte[]{91,92,93,94,95,96,97,98};
        EchoReplyInputBuilder erib = new EchoReplyInputBuilder();
        BufferHelper.setupHeader(erib, EncodeConstants.OF13_VERSION_ID);
        erib.setData(dataToTest);
        EchoReplyInput eri = erib.build();
        ByteBuf out = UnpooledByteBufAllocator.DEFAULT.buffer();
        echoFactory.serialize(eri, out);
        BufferHelper.checkHeaderV13(out, ECHO_REPLY_MESSAGE_CODE_TYPE, 8+dataToTest.length);
        byte[] outData = new byte[dataToTest.length];
        out.readBytes(outData);
        Assert.assertArrayEquals("Wrong - different output data.",dataToTest, outData);
        out.release();
    }
}
