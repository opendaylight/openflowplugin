/*
 * Copyright (c) 2013 Pantheon Technologies s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowjava.protocol.impl.deserialization.factories;

import static org.mockito.ArgumentMatchers.any;

import io.netty.buffer.ByteBuf;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.opendaylight.openflowjava.protocol.api.extensibility.DeserializerRegistry;
import org.opendaylight.openflowjava.protocol.api.keys.MessageCodeKey;
import org.opendaylight.openflowjava.protocol.impl.util.BufferHelper;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.ErrorMessage;

/**
 * Unit tests for ErrorMessageFactory.
 *
 * @author michal.polkorab
 * @author timotej.kubas
 */
@RunWith(MockitoJUnitRunner.class)
public class ErrorMessageFactoryTest {

    @Mock DeserializerRegistry registry;
    @Mock ErrorMessageFactory deserializer;

    private ErrorMessageFactory errorFactory;

    /**
     * Initializes deserializer registry and lookups correct deserializer..
     */
    @Before
    public void startUp() {
        errorFactory = new ErrorMessageFactory(registry);
        Mockito.when(registry.getDeserializer(any(MessageCodeKey.class))).thenReturn(deserializer);
    }

    /**
     * Test of {@link ErrorMessageFactory} for correct translation into POJO.
     */
    @Test
    public void testWithoutData() {
        ByteBuf bb = BufferHelper.buildBuffer("00 00 00 00");
        ErrorMessage builtByFactory = BufferHelper.deserialize(errorFactory, bb);

        BufferHelper.checkHeaderV13(builtByFactory);
        Assert.assertEquals("Wrong type", 0, builtByFactory.getType().intValue());
        Assert.assertEquals("Wrong code", 0, builtByFactory.getCode().intValue());
        Assert.assertEquals("Wrong type string", "HELLOFAILED", builtByFactory.getTypeString());
        Assert.assertEquals("Wrong code string", "INCOMPATIBLE", builtByFactory.getCodeString());
        Assert.assertNull("Data is not null", builtByFactory.getData());

        bb = BufferHelper.buildBuffer("00 01 00 00");
        builtByFactory = BufferHelper.deserialize(errorFactory, bb);

        BufferHelper.checkHeaderV13(builtByFactory);
        Assert.assertEquals("Wrong type", 1, builtByFactory.getType().intValue());
        Assert.assertEquals("Wrong code", 0, builtByFactory.getCode().intValue());
        Assert.assertEquals("Wrong type string", "BADREQUEST", builtByFactory.getTypeString());
        Assert.assertEquals("Wrong code string", "BADVERSION", builtByFactory.getCodeString());
        Assert.assertNull("Data is not null", builtByFactory.getData());

        bb = BufferHelper.buildBuffer("00 02 00 00");
        builtByFactory = BufferHelper.deserialize(errorFactory, bb);

        BufferHelper.checkHeaderV13(builtByFactory);
        Assert.assertEquals("Wrong type", 2, builtByFactory.getType().intValue());
        Assert.assertEquals("Wrong code", 0, builtByFactory.getCode().intValue());
        Assert.assertEquals("Wrong type string", "BADACTION", builtByFactory.getTypeString());
        Assert.assertEquals("Wrong code string", "BADTYPE", builtByFactory.getCodeString());
        Assert.assertNull("Data is not null", builtByFactory.getData());

        bb = BufferHelper.buildBuffer("00 03 00 00");
        builtByFactory = BufferHelper.deserialize(errorFactory, bb);

        BufferHelper.checkHeaderV13(builtByFactory);
        Assert.assertEquals("Wrong type", 3, builtByFactory.getType().intValue());
        Assert.assertEquals("Wrong code", 0, builtByFactory.getCode().intValue());
        Assert.assertEquals("Wrong type string", "BADINSTRUCTION", builtByFactory.getTypeString());
        Assert.assertEquals("Wrong code string", "UNKNOWNINST", builtByFactory.getCodeString());
        Assert.assertNull("Data is not null", builtByFactory.getData());

        bb = BufferHelper.buildBuffer("00 04 00 00");
        builtByFactory = BufferHelper.deserialize(errorFactory, bb);

        BufferHelper.checkHeaderV13(builtByFactory);
        Assert.assertEquals("Wrong type", 4, builtByFactory.getType().intValue());
        Assert.assertEquals("Wrong code", 0, builtByFactory.getCode().intValue());
        Assert.assertEquals("Wrong type string", "BADMATCH", builtByFactory.getTypeString());
        Assert.assertEquals("Wrong code string", "BADTYPE", builtByFactory.getCodeString());
        Assert.assertNull("Data is not null", builtByFactory.getData());

        bb = BufferHelper.buildBuffer("00 05 00 00");
        builtByFactory = BufferHelper.deserialize(errorFactory, bb);

        BufferHelper.checkHeaderV13(builtByFactory);
        Assert.assertEquals("Wrong type", 5, builtByFactory.getType().intValue());
        Assert.assertEquals("Wrong code", 0, builtByFactory.getCode().intValue());
        Assert.assertEquals("Wrong type string", "FLOWMODFAILED", builtByFactory.getTypeString());
        Assert.assertEquals("Wrong code string", "UNKNOWN", builtByFactory.getCodeString());
        Assert.assertNull("Data is not null", builtByFactory.getData());

        bb = BufferHelper.buildBuffer("00 06 00 00");
        builtByFactory = BufferHelper.deserialize(errorFactory, bb);

        BufferHelper.checkHeaderV13(builtByFactory);
        Assert.assertEquals("Wrong type", 6, builtByFactory.getType().intValue());
        Assert.assertEquals("Wrong code", 0, builtByFactory.getCode().intValue());
        Assert.assertEquals("Wrong type string", "GROUPMODFAILED", builtByFactory.getTypeString());
        Assert.assertEquals("Wrong code string", "GROUPEXISTS", builtByFactory.getCodeString());
        Assert.assertNull("Data is not null", builtByFactory.getData());

        bb = BufferHelper.buildBuffer("00 07 00 00");
        builtByFactory = BufferHelper.deserialize(errorFactory, bb);

        BufferHelper.checkHeaderV13(builtByFactory);
        Assert.assertEquals("Wrong type", 7, builtByFactory.getType().intValue());
        Assert.assertEquals("Wrong code", 0, builtByFactory.getCode().intValue());
        Assert.assertEquals("Wrong type string", "PORTMODFAILED", builtByFactory.getTypeString());
        Assert.assertEquals("Wrong code string", "BADPORT", builtByFactory.getCodeString());
        Assert.assertNull("Data is not null", builtByFactory.getData());

        bb = BufferHelper.buildBuffer("00 08 00 00");
        builtByFactory = BufferHelper.deserialize(errorFactory, bb);

        BufferHelper.checkHeaderV13(builtByFactory);
        Assert.assertEquals("Wrong type", 8, builtByFactory.getType().intValue());
        Assert.assertEquals("Wrong code", 0, builtByFactory.getCode().intValue());
        Assert.assertEquals("Wrong type string", "TABLEMODFAILED", builtByFactory.getTypeString());
        Assert.assertEquals("Wrong code string", "BADTABLE", builtByFactory.getCodeString());
        Assert.assertNull("Data is not null", builtByFactory.getData());

        bb = BufferHelper.buildBuffer("00 09 00 00");
        builtByFactory = BufferHelper.deserialize(errorFactory, bb);

        BufferHelper.checkHeaderV13(builtByFactory);
        Assert.assertEquals("Wrong type", 9, builtByFactory.getType().intValue());
        Assert.assertEquals("Wrong code", 0, builtByFactory.getCode().intValue());
        Assert.assertEquals("Wrong type string", "QUEUEOPFAILED", builtByFactory.getTypeString());
        Assert.assertEquals("Wrong code string", "BADPORT", builtByFactory.getCodeString());
        Assert.assertNull("Data is not null", builtByFactory.getData());

        bb = BufferHelper.buildBuffer("00 0A 00 00");
        builtByFactory = BufferHelper.deserialize(errorFactory, bb);

        BufferHelper.checkHeaderV13(builtByFactory);
        Assert.assertEquals("Wrong type", 10, builtByFactory.getType().intValue());
        Assert.assertEquals("Wrong code", 0, builtByFactory.getCode().intValue());
        Assert.assertEquals("Wrong type string", "SWITCHCONFIGFAILED", builtByFactory.getTypeString());
        Assert.assertEquals("Wrong code string", "BADFLAGS", builtByFactory.getCodeString());
        Assert.assertNull("Data is not null", builtByFactory.getData());

        bb = BufferHelper.buildBuffer("00 0B 00 00");
        builtByFactory = BufferHelper.deserialize(errorFactory, bb);

        BufferHelper.checkHeaderV13(builtByFactory);
        Assert.assertEquals("Wrong type", 11, builtByFactory.getType().intValue());
        Assert.assertEquals("Wrong code", 0, builtByFactory.getCode().intValue());
        Assert.assertEquals("Wrong type string", "ROLEREQUESTFAILED", builtByFactory.getTypeString());
        Assert.assertEquals("Wrong code string", "STALE", builtByFactory.getCodeString());
        Assert.assertNull("Data is not null", builtByFactory.getData());

        bb = BufferHelper.buildBuffer("00 0C 00 00");
        builtByFactory = BufferHelper.deserialize(errorFactory, bb);

        BufferHelper.checkHeaderV13(builtByFactory);
        Assert.assertEquals("Wrong type", 12, builtByFactory.getType().intValue());
        Assert.assertEquals("Wrong code", 0, builtByFactory.getCode().intValue());
        Assert.assertEquals("Wrong type string", "METERMODFAILED", builtByFactory.getTypeString());
        Assert.assertEquals("Wrong code string", "UNKNOWN", builtByFactory.getCodeString());
        Assert.assertNull("Data is not null", builtByFactory.getData());

        bb = BufferHelper.buildBuffer("00 0D 00 00");
        builtByFactory = BufferHelper.deserialize(errorFactory, bb);

        BufferHelper.checkHeaderV13(builtByFactory);
        Assert.assertEquals("Wrong type", 13, builtByFactory.getType().intValue());
        Assert.assertEquals("Wrong code", 0, builtByFactory.getCode().intValue());
        Assert.assertEquals("Wrong type string", "TABLEFEATURESFAILED", builtByFactory.getTypeString());
        Assert.assertEquals("Wrong code string", "BADTABLE", builtByFactory.getCodeString());
        Assert.assertNull("Data is not null", builtByFactory.getData());
    }

    /**
     * Test of {@link ErrorMessageFactory} for correct translation into POJO
     * - not existing code used.
     */
    @Test
    public void testWithoutData2() {
        ByteBuf bb = BufferHelper.buildBuffer("00 00 FF FF");
        ErrorMessage builtByFactory = BufferHelper.deserialize(errorFactory, bb);

        BufferHelper.checkHeaderV13(builtByFactory);
        Assert.assertEquals("Wrong type", 0, builtByFactory.getType().intValue());
        Assert.assertEquals("Wrong code", 65535, builtByFactory.getCode().intValue());
        Assert.assertEquals("Wrong type string", "HELLOFAILED", builtByFactory.getTypeString());
        Assert.assertEquals("Wrong code string", "UNKNOWN_CODE", builtByFactory.getCodeString());
        Assert.assertNull("Data is not null", builtByFactory.getData());

        bb = BufferHelper.buildBuffer("00 01 FF FF");
        builtByFactory = BufferHelper.deserialize(errorFactory, bb);

        BufferHelper.checkHeaderV13(builtByFactory);
        Assert.assertEquals("Wrong type", 1, builtByFactory.getType().intValue());
        Assert.assertEquals("Wrong code", 65535, builtByFactory.getCode().intValue());
        Assert.assertEquals("Wrong type string", "BADREQUEST", builtByFactory.getTypeString());
        Assert.assertEquals("Wrong code string", "UNKNOWN_CODE", builtByFactory.getCodeString());
        Assert.assertNull("Data is not null", builtByFactory.getData());

        bb = BufferHelper.buildBuffer("00 02 FF FF");
        builtByFactory = BufferHelper.deserialize(errorFactory, bb);

        BufferHelper.checkHeaderV13(builtByFactory);
        Assert.assertEquals("Wrong type", 2, builtByFactory.getType().intValue());
        Assert.assertEquals("Wrong code", 65535, builtByFactory.getCode().intValue());
        Assert.assertEquals("Wrong type string", "BADACTION", builtByFactory.getTypeString());
        Assert.assertEquals("Wrong code string", "UNKNOWN_CODE", builtByFactory.getCodeString());
        Assert.assertNull("Data is not null", builtByFactory.getData());

        bb = BufferHelper.buildBuffer("00 03 FF FF");
        builtByFactory = BufferHelper.deserialize(errorFactory, bb);

        BufferHelper.checkHeaderV13(builtByFactory);
        Assert.assertEquals("Wrong type", 3, builtByFactory.getType().intValue());
        Assert.assertEquals("Wrong code", 65535, builtByFactory.getCode().intValue());
        Assert.assertEquals("Wrong type string", "BADINSTRUCTION", builtByFactory.getTypeString());
        Assert.assertEquals("Wrong code string", "UNKNOWN_CODE", builtByFactory.getCodeString());
        Assert.assertNull("Data is not null", builtByFactory.getData());

        bb = BufferHelper.buildBuffer("00 04 FF FF");
        builtByFactory = BufferHelper.deserialize(errorFactory, bb);

        BufferHelper.checkHeaderV13(builtByFactory);
        Assert.assertEquals("Wrong type", 4, builtByFactory.getType().intValue());
        Assert.assertEquals("Wrong code", 65535, builtByFactory.getCode().intValue());
        Assert.assertEquals("Wrong type string", "BADMATCH", builtByFactory.getTypeString());
        Assert.assertEquals("Wrong code string", "UNKNOWN_CODE", builtByFactory.getCodeString());
        Assert.assertNull("Data is not null", builtByFactory.getData());

        bb = BufferHelper.buildBuffer("00 05 FF FF");
        builtByFactory = BufferHelper.deserialize(errorFactory, bb);

        BufferHelper.checkHeaderV13(builtByFactory);
        Assert.assertEquals("Wrong type", 5, builtByFactory.getType().intValue());
        Assert.assertEquals("Wrong code", 65535, builtByFactory.getCode().intValue());
        Assert.assertEquals("Wrong type string", "FLOWMODFAILED", builtByFactory.getTypeString());
        Assert.assertEquals("Wrong code string", "UNKNOWN_CODE", builtByFactory.getCodeString());
        Assert.assertNull("Data is not null", builtByFactory.getData());

        bb = BufferHelper.buildBuffer("00 06 FF FF");
        builtByFactory = BufferHelper.deserialize(errorFactory, bb);

        BufferHelper.checkHeaderV13(builtByFactory);
        Assert.assertEquals("Wrong type", 6, builtByFactory.getType().intValue());
        Assert.assertEquals("Wrong code", 65535, builtByFactory.getCode().intValue());
        Assert.assertEquals("Wrong type string", "GROUPMODFAILED", builtByFactory.getTypeString());
        Assert.assertEquals("Wrong code string", "UNKNOWN_CODE", builtByFactory.getCodeString());
        Assert.assertNull("Data is not null", builtByFactory.getData());

        bb = BufferHelper.buildBuffer("00 07 FF FF");
        builtByFactory = BufferHelper.deserialize(errorFactory, bb);

        BufferHelper.checkHeaderV13(builtByFactory);
        Assert.assertEquals("Wrong type", 7, builtByFactory.getType().intValue());
        Assert.assertEquals("Wrong code", 65535, builtByFactory.getCode().intValue());
        Assert.assertEquals("Wrong type string", "PORTMODFAILED", builtByFactory.getTypeString());
        Assert.assertEquals("Wrong code string", "UNKNOWN_CODE", builtByFactory.getCodeString());
        Assert.assertNull("Data is not null", builtByFactory.getData());

        bb = BufferHelper.buildBuffer("00 08 FF FF");
        builtByFactory = BufferHelper.deserialize(errorFactory, bb);

        BufferHelper.checkHeaderV13(builtByFactory);
        Assert.assertEquals("Wrong type", 8, builtByFactory.getType().intValue());
        Assert.assertEquals("Wrong code", 65535, builtByFactory.getCode().intValue());
        Assert.assertEquals("Wrong type string", "TABLEMODFAILED", builtByFactory.getTypeString());
        Assert.assertEquals("Wrong code string", "UNKNOWN_CODE", builtByFactory.getCodeString());
        Assert.assertNull("Data is not null", builtByFactory.getData());

        bb = BufferHelper.buildBuffer("00 09 FF FF");
        builtByFactory = BufferHelper.deserialize(errorFactory, bb);

        BufferHelper.checkHeaderV13(builtByFactory);
        Assert.assertEquals("Wrong type", 9, builtByFactory.getType().intValue());
        Assert.assertEquals("Wrong code", 65535, builtByFactory.getCode().intValue());
        Assert.assertEquals("Wrong type string", "QUEUEOPFAILED", builtByFactory.getTypeString());
        Assert.assertEquals("Wrong code string", "UNKNOWN_CODE", builtByFactory.getCodeString());
        Assert.assertNull("Data is not null", builtByFactory.getData());

        bb = BufferHelper.buildBuffer("00 0A FF FF");
        builtByFactory = BufferHelper.deserialize(errorFactory, bb);

        BufferHelper.checkHeaderV13(builtByFactory);
        Assert.assertEquals("Wrong type", 10, builtByFactory.getType().intValue());
        Assert.assertEquals("Wrong code", 65535, builtByFactory.getCode().intValue());
        Assert.assertEquals("Wrong type string", "SWITCHCONFIGFAILED", builtByFactory.getTypeString());
        Assert.assertEquals("Wrong code string", "UNKNOWN_CODE", builtByFactory.getCodeString());
        Assert.assertNull("Data is not null", builtByFactory.getData());

        bb = BufferHelper.buildBuffer("00 0B FF FF");
        builtByFactory = BufferHelper.deserialize(errorFactory, bb);

        BufferHelper.checkHeaderV13(builtByFactory);
        Assert.assertEquals("Wrong type", 11, builtByFactory.getType().intValue());
        Assert.assertEquals("Wrong code", 65535, builtByFactory.getCode().intValue());
        Assert.assertEquals("Wrong type string", "ROLEREQUESTFAILED", builtByFactory.getTypeString());
        Assert.assertEquals("Wrong code string", "UNKNOWN_CODE", builtByFactory.getCodeString());
        Assert.assertNull("Data is not null", builtByFactory.getData());

        bb = BufferHelper.buildBuffer("00 0C FF FF");
        builtByFactory = BufferHelper.deserialize(errorFactory, bb);

        BufferHelper.checkHeaderV13(builtByFactory);
        Assert.assertEquals("Wrong type", 12, builtByFactory.getType().intValue());
        Assert.assertEquals("Wrong code", 65535, builtByFactory.getCode().intValue());
        Assert.assertEquals("Wrong type string", "METERMODFAILED", builtByFactory.getTypeString());
        Assert.assertEquals("Wrong code string", "UNKNOWN_CODE", builtByFactory.getCodeString());
        Assert.assertNull("Data is not null", builtByFactory.getData());

        bb = BufferHelper.buildBuffer("00 0D FF FF");
        builtByFactory = BufferHelper.deserialize(errorFactory, bb);

        BufferHelper.checkHeaderV13(builtByFactory);
        Assert.assertEquals("Wrong type", 13, builtByFactory.getType().intValue());
        Assert.assertEquals("Wrong code", 65535, builtByFactory.getCode().intValue());
        Assert.assertEquals("Wrong type string", "TABLEFEATURESFAILED", builtByFactory.getTypeString());
        Assert.assertEquals("Wrong code string", "UNKNOWN_CODE", builtByFactory.getCodeString());
        Assert.assertNull("Data is not null", builtByFactory.getData());
    }

    /**
     * Test of {@link ErrorMessageFactory} for correct translation into POJO.
     */
    @Test
    public void testWithData() {
        ByteBuf bb = BufferHelper.buildBuffer("00 00 00 01 00 01 02 03");
        ErrorMessage builtByFactory = BufferHelper.deserialize(errorFactory, bb);

        BufferHelper.checkHeaderV13(builtByFactory);
        Assert.assertEquals("Wrong type", 0, builtByFactory.getType().intValue());
        Assert.assertEquals("Wrong code", 1, builtByFactory.getCode().intValue());
        Assert.assertEquals("Wrong type string", "HELLOFAILED", builtByFactory.getTypeString());
        Assert.assertEquals("Wrong code string", "EPERM", builtByFactory.getCodeString());
        Assert.assertArrayEquals("Wrong data", new byte[]{0x00, 0x01, 0x02, 0x03}, builtByFactory.getData());
    }

    /**
     * Test of {@link ErrorMessageFactory} for correct translation into POJO.
     */
    @Test
    public void testWithIncorrectTypeEnum() {
        ByteBuf bb = BufferHelper.buildBuffer("00 20 00 05 00 01 02 03");
        ErrorMessage builtByFactory = BufferHelper.deserialize(errorFactory, bb);

        BufferHelper.checkHeaderV13(builtByFactory);
        Assert.assertEquals("Wrong type", 32, builtByFactory.getType().intValue());
        Assert.assertEquals("Wrong code", 5, builtByFactory.getCode().intValue());
        Assert.assertEquals("Wrong type string", "UNKNOWN_TYPE", builtByFactory.getTypeString());
        Assert.assertEquals("Wrong code string", "UNKNOWN_CODE", builtByFactory.getCodeString());
        Assert.assertArrayEquals("Wrong data", new byte[]{0x00, 0x01, 0x02, 0x03}, builtByFactory.getData());
    }

    /**
     * Test of {@link ErrorMessageFactory} for correct translation into POJO.
     */
    @Test
    public void testWithIncorrectCodeEnum() {
        ByteBuf bb = BufferHelper.buildBuffer("00 03 00 10 00 01 02 03");
        ErrorMessage builtByFactory = BufferHelper.deserialize(errorFactory, bb);

        BufferHelper.checkHeaderV13(builtByFactory);
        Assert.assertEquals("Wrong type", 3, builtByFactory.getType().intValue());
        Assert.assertEquals("Wrong code", 16, builtByFactory.getCode().intValue());
        Assert.assertEquals("Wrong type string", "BADINSTRUCTION", builtByFactory.getTypeString());
        Assert.assertEquals("Wrong code string", "UNKNOWN_CODE", builtByFactory.getCodeString());
        Assert.assertArrayEquals("Wrong data", new byte[]{0x00, 0x01, 0x02, 0x03}, builtByFactory.getData());
    }

    /**
     * Test of {@link ErrorMessageFactory} for correct translation into POJO.
     */
    @Test
    public void testExperimenterError() {
        ByteBuf bb = BufferHelper.buildBuffer("FF FF 00 00 00 01");
        BufferHelper.deserialize(errorFactory, bb);

        Mockito.verify(deserializer, Mockito.times(1)).deserialize(bb);
    }
}
