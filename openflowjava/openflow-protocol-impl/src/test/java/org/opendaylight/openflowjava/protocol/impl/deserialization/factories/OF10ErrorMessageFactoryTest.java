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
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.ErrorMessage;

/**
 * @author michal.polkorab
 */
public class OF10ErrorMessageFactoryTest {
    private OFDeserializer<ErrorMessage> errorFactory;

    /**
     * Initializes deserializer registry and lookups correct deserializer
     */
    @Before
    public void startUp() {
        DeserializerRegistry registry = new DeserializerRegistryImpl();
        registry.init();
        errorFactory = registry.getDeserializer(
                new MessageCodeKey(EncodeConstants.OF10_VERSION_ID, 1, ErrorMessage.class));
    }

    /**
     * Test of {@link ErrorMessageFactory} for correct translation into POJO
     */
    @Test
    public void testWithoutData() {
        ByteBuf bb = BufferHelper.buildBuffer("00 00 00 00");
        ErrorMessage builtByFactory = BufferHelper.deserialize(errorFactory, bb);

        BufferHelper.checkHeaderV10(builtByFactory);
        Assert.assertEquals("Wrong type", 0, builtByFactory.getType().intValue());
        Assert.assertEquals("Wrong code", 0, builtByFactory.getCode().intValue());
        Assert.assertEquals("Wrong type string", "HELLOFAILED", builtByFactory.getTypeString());
        Assert.assertEquals("Wrong code string", "INCOMPATIBLE", builtByFactory.getCodeString());
        Assert.assertNull("Data is not null", builtByFactory.getData());

        bb = BufferHelper.buildBuffer("00 01 00 00");
        builtByFactory = BufferHelper.deserialize(errorFactory, bb);

        BufferHelper.checkHeaderV10(builtByFactory);
        Assert.assertEquals("Wrong type", 1, builtByFactory.getType().intValue());
        Assert.assertEquals("Wrong code", 0, builtByFactory.getCode().intValue());
        Assert.assertEquals("Wrong type string", "BADREQUEST", builtByFactory.getTypeString());
        Assert.assertEquals("Wrong code string", "BADVERSION", builtByFactory.getCodeString());
        Assert.assertNull("Data is not null", builtByFactory.getData());

        bb = BufferHelper.buildBuffer("00 02 00 00");
        builtByFactory = BufferHelper.deserialize(errorFactory, bb);

        BufferHelper.checkHeaderV10(builtByFactory);
        Assert.assertEquals("Wrong type", 2, builtByFactory.getType().intValue());
        Assert.assertEquals("Wrong code", 0, builtByFactory.getCode().intValue());
        Assert.assertEquals("Wrong type string", "BADACTION", builtByFactory.getTypeString());
        Assert.assertEquals("Wrong code string", "BADTYPE", builtByFactory.getCodeString());
        Assert.assertNull("Data is not null", builtByFactory.getData());

        bb = BufferHelper.buildBuffer("00 03 00 00");
        builtByFactory = BufferHelper.deserialize(errorFactory, bb);

        BufferHelper.checkHeaderV10(builtByFactory);
        Assert.assertEquals("Wrong type", 3, builtByFactory.getType().intValue());
        Assert.assertEquals("Wrong code", 0, builtByFactory.getCode().intValue());
        Assert.assertEquals("Wrong type string", "FLOWMODFAILED", builtByFactory.getTypeString());
        Assert.assertEquals("Wrong code string", "ALLTABLESFULL", builtByFactory.getCodeString());
        Assert.assertNull("Data is not null", builtByFactory.getData());

        bb = BufferHelper.buildBuffer("00 04 00 00");
        builtByFactory = BufferHelper.deserialize(errorFactory, bb);

        BufferHelper.checkHeaderV10(builtByFactory);
        Assert.assertEquals("Wrong type", 4, builtByFactory.getType().intValue());
        Assert.assertEquals("Wrong code", 0, builtByFactory.getCode().intValue());
        Assert.assertEquals("Wrong type string", "PORTMODFAILED", builtByFactory.getTypeString());
        Assert.assertEquals("Wrong code string", "BADPORT", builtByFactory.getCodeString());
        Assert.assertNull("Data is not null", builtByFactory.getData());

        bb = BufferHelper.buildBuffer("00 05 00 00");
        builtByFactory = BufferHelper.deserialize(errorFactory, bb);

        BufferHelper.checkHeaderV10(builtByFactory);
        Assert.assertEquals("Wrong type", 5, builtByFactory.getType().intValue());
        Assert.assertEquals("Wrong code", 0, builtByFactory.getCode().intValue());
        Assert.assertEquals("Wrong type string", "QUEUEOPFAILED", builtByFactory.getTypeString());
        Assert.assertEquals("Wrong code string", "BADPORT", builtByFactory.getCodeString());
        Assert.assertNull("Data is not null", builtByFactory.getData());
    }

    /**
     * Test of {@link ErrorMessageFactory} for correct translation into POJO
     * - not existing code used
     */
    @Test
    public void testWithoutData2() {
        ByteBuf bb = BufferHelper.buildBuffer("00 00 FF FF");
        ErrorMessage builtByFactory = BufferHelper.deserialize(errorFactory, bb);

        BufferHelper.checkHeaderV10(builtByFactory);
        Assert.assertEquals("Wrong type", 0, builtByFactory.getType().intValue());
        Assert.assertEquals("Wrong code", 65535, builtByFactory.getCode().intValue());
        Assert.assertEquals("Wrong type string", "HELLOFAILED", builtByFactory.getTypeString());
        Assert.assertEquals("Wrong code string", "UNKNOWN_CODE", builtByFactory.getCodeString());
        Assert.assertNull("Data is not null", builtByFactory.getData());

        bb = BufferHelper.buildBuffer("00 01 FF FF");
        builtByFactory = BufferHelper.deserialize(errorFactory, bb);

        BufferHelper.checkHeaderV10(builtByFactory);
        Assert.assertEquals("Wrong type", 1, builtByFactory.getType().intValue());
        Assert.assertEquals("Wrong code", 65535, builtByFactory.getCode().intValue());
        Assert.assertEquals("Wrong type string", "BADREQUEST", builtByFactory.getTypeString());
        Assert.assertEquals("Wrong code string", "UNKNOWN_CODE", builtByFactory.getCodeString());
        Assert.assertNull("Data is not null", builtByFactory.getData());

        bb = BufferHelper.buildBuffer("00 02 FF FF");
        builtByFactory = BufferHelper.deserialize(errorFactory, bb);

        BufferHelper.checkHeaderV10(builtByFactory);
        Assert.assertEquals("Wrong type", 2, builtByFactory.getType().intValue());
        Assert.assertEquals("Wrong code", 65535, builtByFactory.getCode().intValue());
        Assert.assertEquals("Wrong type string", "BADACTION", builtByFactory.getTypeString());
        Assert.assertEquals("Wrong code string", "UNKNOWN_CODE", builtByFactory.getCodeString());
        Assert.assertNull("Data is not null", builtByFactory.getData());

        bb = BufferHelper.buildBuffer("00 03 FF FF");
        builtByFactory = BufferHelper.deserialize(errorFactory, bb);

        BufferHelper.checkHeaderV10(builtByFactory);
        Assert.assertEquals("Wrong type", 3, builtByFactory.getType().intValue());
        Assert.assertEquals("Wrong code", 65535, builtByFactory.getCode().intValue());
        Assert.assertEquals("Wrong type string", "FLOWMODFAILED", builtByFactory.getTypeString());
        Assert.assertEquals("Wrong code string", "UNKNOWN_CODE", builtByFactory.getCodeString());
        Assert.assertNull("Data is not null", builtByFactory.getData());

        bb = BufferHelper.buildBuffer("00 04 FF FF");
        builtByFactory = BufferHelper.deserialize(errorFactory, bb);

        BufferHelper.checkHeaderV10(builtByFactory);
        Assert.assertEquals("Wrong type", 4, builtByFactory.getType().intValue());
        Assert.assertEquals("Wrong code", 65535, builtByFactory.getCode().intValue());
        Assert.assertEquals("Wrong type string", "PORTMODFAILED", builtByFactory.getTypeString());
        Assert.assertEquals("Wrong code string", "UNKNOWN_CODE", builtByFactory.getCodeString());
        Assert.assertNull("Data is not null", builtByFactory.getData());

        bb = BufferHelper.buildBuffer("00 05 FF FF");
        builtByFactory = BufferHelper.deserialize(errorFactory, bb);

        BufferHelper.checkHeaderV10(builtByFactory);
        Assert.assertEquals("Wrong type", 5, builtByFactory.getType().intValue());
        Assert.assertEquals("Wrong code", 65535, builtByFactory.getCode().intValue());
        Assert.assertEquals("Wrong type string", "QUEUEOPFAILED", builtByFactory.getTypeString());
        Assert.assertEquals("Wrong code string", "UNKNOWN_CODE", builtByFactory.getCodeString());
        Assert.assertNull("Data is not null", builtByFactory.getData());
    }

    /**
     * Test of {@link OF10ErrorMessageFactory} for correct translation into POJO
     */
    @Test
    public void testWithData() {
        ByteBuf bb = BufferHelper.buildBuffer("00 00 00 01 00 01 02 03");
        ErrorMessage builtByFactory = BufferHelper.deserialize(errorFactory, bb);

        BufferHelper.checkHeaderV10(builtByFactory);
        Assert.assertEquals("Wrong type", 0, builtByFactory.getType().intValue());
        Assert.assertEquals("Wrong code", 1, builtByFactory.getCode().intValue());
        Assert.assertEquals("Wrong type string", "HELLOFAILED", builtByFactory.getTypeString());
        Assert.assertEquals("Wrong code string", "EPERM", builtByFactory.getCodeString());
        Assert.assertArrayEquals("Wrong data", new byte[]{0x00, 0x01, 0x02, 0x03}, builtByFactory.getData());
    }

    /**
     * Test of {@link OF10ErrorMessageFactory} for correct translation into POJO
     */
    @Test
    public void testWithIncorrectTypeEnum() {
        ByteBuf bb = BufferHelper.buildBuffer("00 0A 00 05 00 01 02 03");
        ErrorMessage builtByFactory = BufferHelper.deserialize(errorFactory, bb);

        BufferHelper.checkHeaderV10(builtByFactory);
        Assert.assertEquals("Wrong type", 10, builtByFactory.getType().intValue());
        Assert.assertEquals("Wrong code", 5, builtByFactory.getCode().intValue());
        Assert.assertEquals("Wrong type string", "UNKNOWN_TYPE", builtByFactory.getTypeString());
        Assert.assertEquals("Wrong code string", "UNKNOWN_CODE", builtByFactory.getCodeString());
        Assert.assertArrayEquals("Wrong data", new byte[]{0x00, 0x01, 0x02, 0x03}, builtByFactory.getData());
    }

    /**
     * Test of {@link OF10ErrorMessageFactory} for correct translation into POJO
     */
    @Test
    public void testWithIncorrectCodeEnum() {
        ByteBuf bb = BufferHelper.buildBuffer("00 03 00 06 00 01 02 03");
        ErrorMessage builtByFactory = BufferHelper.deserialize(errorFactory, bb);

        BufferHelper.checkHeaderV10(builtByFactory);
        Assert.assertEquals("Wrong type", 3, builtByFactory.getType().intValue());
        Assert.assertEquals("Wrong code", 6, builtByFactory.getCode().intValue());
        Assert.assertEquals("Wrong type string", "FLOWMODFAILED", builtByFactory.getTypeString());
        Assert.assertEquals("Wrong code string", "UNKNOWN_CODE", builtByFactory.getCodeString());
        Assert.assertArrayEquals("Wrong data", new byte[]{0x00, 0x01, 0x02, 0x03}, builtByFactory.getData());
    }

}
